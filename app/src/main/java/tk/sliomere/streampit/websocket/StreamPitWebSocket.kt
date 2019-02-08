package tk.sliomere.streampit.websocket

import android.os.Message
import android.util.Base64
import android.util.Log
import org.java_websocket.WebSocket
import org.java_websocket.client.WebSocketClient
import org.java_websocket.exceptions.WebsocketNotConnectedException
import org.java_websocket.handshake.ServerHandshake
import org.json.JSONObject
import tk.sliomere.streampit.CardAction
import tk.sliomere.streampit.MainActivity
import tk.sliomere.streampit.cards.SwitchSceneCard
import tk.sliomere.streampit.cards.ToggleVisibilityCard
import tk.sliomere.streampit.obs.OBSHolder
import java.net.URI
import java.security.MessageDigest

class StreamPitWebSocket(uri: URI, var password: String, var onConnectCallback: () -> Unit) : WebSocketClient(uri) {

    companion object {
        val obsHolder = OBSHolder()
    }

    var changingPwd = false
    private var newPwd = password
    private var callbacks: HashMap<String, (json: JSONObject) -> Unit> = HashMap()
    private var messageIdCounter = 0
    private var authenticated = false
    private var cardsReady = false

    override fun onOpen(handshakedata: ServerHandshake?) {
        this.sendMessage("GetAuthRequired", JSONObject(), callback = { msg: JSONObject ->
            if (msg.getBoolean("authRequired")) {
                val authResponse = calculateAuthResponse(password, msg.getString("salt"), msg.getString("challenge"))

                val args = JSONObject()
                args.put("auth", authResponse)
                this.sendMessage("Authenticate", args, callback = { authMsg: JSONObject ->
                    if (authMsg.has("error")) {
                        val message = Message()
                        message.obj = MainActivity.eventAuthFailed
                        MainActivity.handler.sendMessage(message)
                    } else {
                        authenticated = true
                        if (cardsReady) {
                            onReady()
                        }
                        onConnectCallback.invoke()
                        this.sendMessage("GetSourcesList", JSONObject(), callback = { msg: JSONObject ->
                            obsHolder.parseSourcesList(msg.getJSONArray("sources"))
                        })
                    }
                })
            }
        })
    }

    fun onReady() {
        if (authenticated) {
            this.cardsReady = false
            this.sendMessage("GetCurrentScene", JSONObject(), callback = { msg: JSONObject ->
                val sceneName = msg.getString("name")
                if (MainActivity.listeningCards.containsKey(CardAction.SWITCH_SCENE)) {
                    for (card in MainActivity.listeningCards[CardAction.SWITCH_SCENE]!!) {
                        if (card is SwitchSceneCard) {
                            card.onSceneUpdate(sceneName)
                        }
                    }
                }
            })
            this.sendMessage("GetStudioModeStatus", JSONObject(), callback = { msg: JSONObject ->
                MainActivity.OBS_STUDIO_MODE_ENABLED = msg.getBoolean("studio-mode")
            })
            if (MainActivity.listeningCards.containsKey(CardAction.TOGGLE_VISIBILITY)) {
                for (card in MainActivity.listeningCards[CardAction.TOGGLE_VISIBILITY]!!) {
                    if (card is ToggleVisibilityCard) {
                        card.reloadCard()
                    }
                }
            }

            if (MainActivity.listeningCards.containsKey(CardAction.TOGGLE_MUTE)) {
                for (card in MainActivity.listeningCards[CardAction.TOGGLE_MUTE]!!) {
                    card.reloadCard()
                }
            }
        } else {
            this.cardsReady = true
        }
    }

    override fun onClose(code: Int, reason: String?, remote: Boolean) {
        Log.d("StreamPit", "WebSocket closed: $reason")
        if (this.changingPwd) {
            password = newPwd
            val msg = Message()
            msg.obj = MainActivity.eventReconnectWebSocket
            MainActivity.handler.sendMessage(msg)
        }
    }

    override fun onMessage(message: String?) {
//        Log.d("StreamPit", "Message received: ")
//        Log.d("StreamPit", message)
        val msg = JSONObject(message)
        if (msg.has("status") && msg.getString("status") == "error") {
            Log.d("StreamPit", "An error occured: " + msg.getString("error"))
            if (!msg.getString("error").contains("Authentication")) {
                return
            }
        }
        if (msg.has("message-id")) {
            if (callbacks.containsKey(msg.getString("message-id"))) {
                val mid = msg.getString("message-id")
                if (callbacks.containsKey(mid)) {
                    callbacks.remove(mid)!!.invoke(msg)
                }
            }
        } else if (msg.has("update-type")) {
            //Event
            when (msg.getString("update-type")) {
                "SceneItemVisibilityChanged" -> {
                    if (MainActivity.listeningCards.containsKey(CardAction.TOGGLE_VISIBILITY)) {
                        for (card in MainActivity.listeningCards[CardAction.TOGGLE_VISIBILITY]!!) {
                            if (card is ToggleVisibilityCard) {
                                card.onVisibilityUpdate(msg.getString("item-name")!!, msg.getBoolean("item-visible"))
                            }
                        }
                    }
                }
                "SwitchScenes" -> {
                    val name = msg.getString("scene-name")
                    if (MainActivity.listeningCards.containsKey(CardAction.SWITCH_SCENE)) {
                        for (card in MainActivity.listeningCards[CardAction.SWITCH_SCENE]!!) {
                            if (card is SwitchSceneCard) {
                                card.onSceneUpdate(name)
                            }
                        }
                        val sources = msg.getJSONArray("sources")!!
                        for (i in 0 until sources.length()) {
                            val source = sources.getJSONObject(i)
                            for (card in MainActivity.listeningCards[CardAction.TOGGLE_VISIBILITY]!!) {
                                if (card is ToggleVisibilityCard) {
                                    card.onVisibilityUpdate(source.getString("name"), source.getBoolean("render"))
                                }
                            }
                        }
                    }
                }
                "StudioModeSwitched" -> {
                    MainActivity.OBS_STUDIO_MODE_ENABLED = msg.getBoolean("new-state")
                }
            }
        }
    }

    override fun onError(ex: Exception?) {
        if (ex != null) {
            Log.d("StreamPit", "Error: " + ex.message)
        }
    }

    fun sendMessage(requestType: String, args: JSONObject) {
        if (!this.connection.isOpen) {
            throw WebsocketNotConnectedException()
        }
        val mid = messageIdCounter++
        args.put("request-type", requestType)
        args.put("message-id", mid.toString())
        this.send(args.toString())
    }

    fun sendMessage(requestType: String, args: JSONObject, callback: (json: JSONObject) -> Unit) {
        if (this.connection.readyState != WebSocket.READYSTATE.OPEN) {
            throw WebsocketNotConnectedException()
        }
        val mid = messageIdCounter++
        args.put("request-type", requestType)
        args.put("message-id", mid.toString())
//        Log.d("StreamPit", args.toString())
        this.callbacks[mid.toString()] = callback
        this.send(args.toString())
    }

    private fun calculateAuthResponse(pwd: String, salt: String, challenge: String): String {
        val md = MessageDigest.getInstance("SHA-256")

        val secretString = pwd + salt
        val secret = Base64.encodeToString(md.digest(secretString.toByteArray()), Base64.NO_WRAP)

        val authResponseString = secret + challenge
        val authResponseHash = md.digest(authResponseString.toByteArray())

        return Base64.encodeToString(authResponseHash, Base64.NO_WRAP)
    }

    fun passwordChanged(newPwd: String) {
        changingPwd = true
        this.newPwd = newPwd
        this.close()
    }
}
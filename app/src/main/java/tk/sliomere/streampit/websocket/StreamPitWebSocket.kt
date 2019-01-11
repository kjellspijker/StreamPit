package tk.sliomere.streampit.websocket

import android.util.Base64
import android.util.Log
import org.java_websocket.client.WebSocketClient
import org.java_websocket.drafts.Draft_17
import org.java_websocket.exceptions.WebsocketNotConnectedException
import org.java_websocket.handshake.ServerHandshake
import org.json.JSONObject
import tk.sliomere.streampit.MainActivity
import java.net.URI
import java.security.MessageDigest

class StreamPitWebSocket(uri: URI, var password: String) : WebSocketClient(uri, Draft_17()) {
    var callbacks: HashMap<String, (json: JSONObject) -> Unit> = HashMap()
    var messageIdCounter = 0

    override fun onOpen(handshakedata: ServerHandshake?) {
        Log.d("StreamPit", "WebSocket opened")
        MainActivity.webSocketClient.sendMessage("GetAuthRequired", JSONObject(), callback = { msg: JSONObject ->
            if (msg.getBoolean("authRequired")) {
                val authResponse = calculateAuthResponse(password, msg.getString("salt"), msg.getString("challenge"))

                val args = JSONObject()
                args.put("auth", authResponse)
                this.sendMessage("Authenticate", args)
            }
        })
    }

    override fun onClose(code: Int, reason: String?, remote: Boolean) {
        Log.d("StreamPit", "WebSocket closed: $reason")
    }

    override fun onMessage(message: String?) {
        Log.d("StreamPit", "Message received: ")
        Log.d("StreamPit", message)
        val msg = JSONObject(message)
        if (callbacks.containsKey(msg.getString("message-id"))) {
            val mid = msg.getString("message-id")
            callbacks.remove(mid)!!.invoke(msg)
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
        Log.d("StreamPit", "Sending: " + args.toString())
        this.send(args.toString())
    }

    fun sendMessage(requestType: String, args: JSONObject, callback: (json: JSONObject) -> Unit) {
        if (!this.connection.isOpen) {
            throw WebsocketNotConnectedException()
        }
        val mid = messageIdCounter++
        args.put("request-type", requestType)
        args.put("message-id", mid.toString())
        Log.d("StreamPit", args.toString())
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
}
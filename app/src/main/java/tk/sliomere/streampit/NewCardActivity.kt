package tk.sliomere.streampit

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.material.textfield.TextInputEditText
import com.jaredrummler.android.colorpicker.ColorPickerDialog
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener
import kotlinx.android.synthetic.main.activity_new_card.*
import tk.sliomere.streampit.cards.*
import tk.sliomere.streampit.dialog.DialogFragment

class NewCardActivity : AppCompatActivity(), ColorPickerDialogListener {

    companion object {
        const val eventDialogClosed = "DIALOGCLOSED"
        val availableIcons: Array<String> = arrayOf("icon_scene", "icon_play", "icon_volume", "icon_record_rec", "icon_visible")
        var color: Int = -1
    }

    private lateinit var icon: String
    private lateinit var titleEditText: TextInputEditText
    private lateinit var targetEditText: TextInputEditText
    private lateinit var cardIconImageBtn: ImageButton
    private lateinit var colorImageButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_card)
        setSupportActionBar(toolbar)


        icon = resources.getResourceEntryName(R.drawable.icon_play)
        color = resources.getColor(R.color.cardPrimary, this.theme)
        titleEditText = findViewById(R.id.card_title_edit_text)
        targetEditText = findViewById(R.id.card_target_edit_text)
        cardIconImageBtn = findViewById(R.id.card_icon_image_button)
        colorImageButton = findViewById(R.id.color_image_button)

        cardIconImageBtn.setOnClickListener { _: View? ->
            DialogFragment().show(supportFragmentManager, "ChooseIconDialog")
        }

        colorImageButton.setOnClickListener { _: View? ->
            ColorPickerDialog.newBuilder().setColor(resources.getColor(R.color.cardPrimary, this.theme)).show(this)
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                icon = intent!!.getStringExtra(DialogFragment.extraIconName)
                cardIconImageBtn.setImageDrawable(resources.getDrawable(resources.getIdentifier(icon, "drawable", "tk.sliomere.streampit"), theme))
            }
        }, IntentFilter(eventDialogClosed))

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_new_card, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_save -> {
                val title = titleEditText.text.toString()
                val target = targetEditText.text.toString()
                if (title.isEmpty() || target.isEmpty()) {
                    Toast.makeText(this, "Please fill in all fields!", Toast.LENGTH_LONG).show()
                    if (title.isEmpty()) {
                        titleEditText.requestFocus()
                        (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager ).showSoftInput(titleEditText, InputMethodManager.SHOW_IMPLICIT)
                    } else if (target.isEmpty()) {
                        targetEditText.requestFocus()
                        (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager ).showSoftInput(targetEditText, InputMethodManager.SHOW_IMPLICIT)
                    }
                } else {
                    val cardAction = CardAction.parse(icon)
                    val card = when (cardAction) {
                        CardAction.TOGGLE_MUTE -> ToggleMuteCard((MainActivity.cardIDCounter++).toString(), title, color, icon, target)
                        CardAction.TOGGLE_RECORDING -> ToggleRecordingCard((MainActivity.cardIDCounter++).toString(), title, color, icon, target)
                        CardAction.SWITCH_SCENE -> SwitchSceneCard((MainActivity.cardIDCounter++).toString(), title, color, icon, target)
                        CardAction.TOGGLE_STREAMING -> ToggleStreamingCard((MainActivity.cardIDCounter++).toString(), title, color, icon, target)
                        CardAction.TOGGLE_VISIBILITY -> ToggleVisibilityCard((MainActivity.cardIDCounter++).toString(), title, color, icon, target)
                        CardAction.NOTHING -> NothingCard((MainActivity.cardIDCounter++).toString(), title, color, icon, target)
                    }
//                MainActivity.addCard(card)
                    val intent = Intent(MainActivity.eventDataSetChanged)
                    intent.putExtra(MainActivity.cardExtra, card)
                    LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
                    finish()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDialogDismissed(dialogId: Int) {

    }

    override fun onColorSelected(dialogId: Int, color: Int) {
        NewCardActivity.color = color
        cardIconImageBtn.setBackgroundColor(color)
        colorImageButton.setBackgroundColor(color)
    }

}

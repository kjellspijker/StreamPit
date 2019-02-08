package tk.sliomere.streampit

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.material.textfield.TextInputEditText
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : AppCompatActivity() {

    lateinit var ipEditText: TextInputEditText
    lateinit var portEditText: TextInputEditText
    lateinit var passwordEditText: TextInputEditText
    lateinit var resetDefaultButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_settings)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        ipEditText = findViewById(R.id.computer_ip_edit_text)
        portEditText = findViewById(R.id.computer_port_edit_text)
        passwordEditText = findViewById(R.id.password_edit_text)
        resetDefaultButton = findViewById(R.id.reset_all_button)

        resetDefaultButton.setOnClickListener{v: View ->
            AlertDialog.Builder(this)
                    .setTitle(resources.getString(R.string.confirm_reset_all_cards))
                    .setMessage(resources.getString(R.string.confirm_reset_all_cards_message))
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.yes) { _: DialogInterface?, _: Int ->
                        val editor = getSharedPreferences(MainActivity.PREF_NAME, Context.MODE_PRIVATE).edit()
                        editor.remove(MainActivity.PREF_CARDS_JSON)
                        editor.commit()
                        LocalBroadcastManager.getInstance(this).sendBroadcast(Intent(MainActivity.eventResetCards))
                        finish()
                    }
                    .setNegativeButton(android.R.string.no, null).show()
        }

        if (intent.hasExtra(MainActivity.firstRunExtra)) Toast.makeText(this, resources.getString(R.string.first_run_toast), Toast.LENGTH_LONG).show()
        else if (intent.hasExtra(MainActivity.authFailedExtra)) {
            Toast.makeText(this, "Authentication with OBS Failed!\nPlease enter the correct password!", Toast.LENGTH_LONG).show()
            setPrefValues()
            passwordEditText.requestFocus()
        }
        else setPrefValues()
    }

    fun setPrefValues() {
        val prefs = getSharedPreferences(MainActivity.PREF_NAME, Context.MODE_PRIVATE)
//            prefs.edit().remove(MainActivity.PREF_IP).remove(MainActivity.PREF_PORT).apply()
        val ip = prefs.getString(MainActivity.PREF_IP, "")
        ipEditText.setText(ip)
        portEditText.setText(prefs.getInt(MainActivity.PREF_PORT, 4444).toString())
        passwordEditText.setText(prefs.getString(MainActivity.PREF_PASSWORD, ""))

    }

    override fun finish() {
        if (!Patterns.IP_ADDRESS.matcher(ipEditText.text.toString()).matches()) {
            Toast.makeText(this, resources.getString(R.string.no_ip_entered), Toast.LENGTH_LONG).show()
        } else {
            val port = portEditText.text.toString().toInt()
            if (port in 1..65534) {
                val pref = getSharedPreferences(MainActivity.PREF_NAME, Context.MODE_PRIVATE)
                val editor = pref.edit()
                editor.putString(MainActivity.PREF_IP, ipEditText.text.toString())
                editor.putInt(MainActivity.PREF_PORT, portEditText.text.toString().toInt())
                val oldPwd = pref.getString(MainActivity.PREF_PASSWORD, "")
                val newPwd = passwordEditText.text.toString()
                if (oldPwd != newPwd) {
                    MainActivity.webSocketClient.passwordChanged(newPwd)
                    editor.putString(MainActivity.PREF_PASSWORD, newPwd)
                }
                editor.commit()
                Toast.makeText(this, resources.getString(R.string.settings_saved), Toast.LENGTH_SHORT).show()
                setResult(0)
                super.finish()
            } else {
                Toast.makeText(this, resources.getString(R.string.invalid_port_entered), Toast.LENGTH_LONG).show()
            }
        }
    }

}

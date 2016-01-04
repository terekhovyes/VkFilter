package me.alexeyterekhov.vkfilter.GUI.LoginActivity

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.vk.sdk.VKScope
import com.vk.sdk.VKSdk
import me.alexeyterekhov.vkfilter.GUI.DialogsActivity.DialogsActivity
import me.alexeyterekhov.vkfilter.R
import me.alexeyterekhov.vkfilter.Util.AppContext


public class LoginActivity: AppCompatActivity() {
    private var loginPressed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super<AppCompatActivity>.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("loginPressed"))
                loginPressed = savedInstanceState.getBoolean("loginPressed")
        }
        // val fingerprint = VKUtil.getCertificateFingerprint(this, this.getPackageName())
        // Log.d("Fingerprint", fingerprint[0])
        if (VKSdk.wakeUpSession(AppContext.instance))
            startDialogActivity()
        loginPressed = false
        init()
    }

    override fun onResume() {
        super<AppCompatActivity>.onResume()
        if (loginPressed) {
            loginPressed = false
            if (VKSdk.wakeUpSession(AppContext.instance))
                startDialogActivity()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("loginPressed", loginPressed)
    }

    private fun startDialogActivity() {
        startActivity(Intent(this, DialogsActivity::class.java))
        finish()
    }

    private fun init() {
        findViewById(R.id.loginButton).setOnClickListener {
            VKSdk.login(this,
                    VKScope.FRIENDS,
                    VKScope.MESSAGES,
                    VKScope.PHOTOS,
                    VKScope.VIDEO,
                    VKScope.NOHTTPS)
            loginPressed = true
        }
    }
}
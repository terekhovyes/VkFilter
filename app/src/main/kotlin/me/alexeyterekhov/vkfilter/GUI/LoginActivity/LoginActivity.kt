package me.alexeyterekhov.vkfilter.GUI.LoginActivity

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.ActionBarActivity
import android.util.Log
import android.widget.Toast
import com.google.android.gms.gcm.GoogleCloudMessaging
import com.vk.sdk.VKSdk
import com.vk.sdk.VKUIHelper
import me.alexeyterekhov.vkfilter.Common.AppContext
import me.alexeyterekhov.vkfilter.Common.GooglePlay
import me.alexeyterekhov.vkfilter.GUI.DialogListActivity.DialogListActivity
import me.alexeyterekhov.vkfilter.Internet.VkApi.RunFun
import me.alexeyterekhov.vkfilter.Internet.VkSdkInitializer
import me.alexeyterekhov.vkfilter.R
import java.io.IOException


public class LoginActivity: ActionBarActivity() {
    private var loginPressed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super<ActionBarActivity>.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        if (savedInstanceState != null) {
            if (savedInstanceState containsKey "loginPressed")
                loginPressed = savedInstanceState getBoolean "loginPressed"
        }
        VKUIHelper.onCreate(this)
        VkSdkInitializer.init()
        // String[] fingerprint = VKUtil.getCertificateFingerprint(this, this.getPackageName());
        // Log.d("Fingerprint", fingerprint[0]);
        if (VKSdk.wakeUpSession())
            startDialogActivity()
        loginPressed = false
        init()
    }

    override fun onResume() {
        super<ActionBarActivity>.onResume()
        VKUIHelper.onResume(this)
        if (loginPressed) {
            loginPressed = false
            if (VKSdk.wakeUpSession())
                startDialogActivity()
        }
    }

    override fun onDestroy() {
        super<ActionBarActivity>.onDestroy()
        VKUIHelper.onDestroy(this)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("loginPressed", loginPressed)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super<ActionBarActivity>.onActivityResult(requestCode, resultCode, data)
        VKUIHelper.onActivityResult(this, requestCode, resultCode, data)
    }

    private fun startDialogActivity() {
        startActivity(Intent(this, javaClass<DialogListActivity>()))
        registerForGCM()
        finish()
    }

    private fun init() {
        findViewById(R.id.loginButton) setOnClickListener {
            VKSdk.authorize(VkSdkInitializer.vkScopes, true, false)
            loginPressed = true
        }

        findViewById(R.id.secondLoginButton) setOnClickListener {
            VKSdk.authorize(VkSdkInitializer.vkScopes, true, true)
            loginPressed = true
        }
    }

    private fun registerForGCM() {
        if (GooglePlay.checkGooglePlayServices(this)) {
            (object: AsyncTask<Unit, Unit, Unit>() {
                override fun doInBackground(vararg params: Unit?) {
                    try {
                        val gcm = GoogleCloudMessaging.getInstance(AppContext.instance)
                        val regId = gcm register "419930423637"
                        RunFun registerGCM regId
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }).execute()
        } else {
            val error = "No valid Google Play Services APK found."
            Log.d("Google Play Services", error)
            Toast.makeText(this, error, Toast.LENGTH_SHORT)
        }
    }
}
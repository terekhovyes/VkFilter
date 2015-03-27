package me.alexeyterekhov.vkfilter.GUI.LoginActivity

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.ActionBarActivity
import android.util.Log
import android.view.View
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


public class LoginActivity: ActionBarActivity(), View.OnClickListener {
    private var loginPressed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super<ActionBarActivity>.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        VKUIHelper.onCreate(this)
        VkSdkInitializer.init()
        registerForGCM()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super<ActionBarActivity>.onActivityResult(requestCode, resultCode, data)
        VKUIHelper.onActivityResult(this, requestCode, resultCode, data)
    }

    override fun onClick(v: View) {
        VKSdk.authorize(VkSdkInitializer.vkScopes, true, false)
        loginPressed = true
    }

    private fun startDialogActivity() {
        startActivity(Intent(this, javaClass<DialogListActivity>()))
        finish()
    }

    private fun init() {
        findViewById(R.id.loginButton) setOnClickListener this
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
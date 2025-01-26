package com.goldbookapp

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.view.WindowManager
import com.goldbookapp.inapplock.managers.LockManager
import com.goldbookapp.ui.activity.settings.AppLockActivityNew
import com.hamzasharuf.networkmonitor.ConnectivityStateHolder.registerConnectivityBroadcaster


class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        appContext = applicationContext
        val lockManager: LockManager<AppLockActivityNew> =
            LockManager.getInstance() as LockManager<AppLockActivityNew>
        lockManager.enableAppLock(this, AppLockActivityNew::class.java)
        lockManager.getAppLock().logoId = R.mipmap.ic_launcher

        // This starts the broadcast of network events to NetworkState and all Activity implementing NetworkConnectivityListener
        registerConnectivityBroadcaster()

        //disable screenshot capture
      //  setupActivityListener()
    }

    private fun setupActivityListener() {
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                activity.window.setFlags(
                    WindowManager.LayoutParams.FLAG_SECURE,
                    WindowManager.LayoutParams.FLAG_SECURE
                )
            }

            override fun onActivityStarted(activity: Activity) {}
            override fun onActivityResumed(activity: Activity) {}
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {}
        })
    }


    companion object {
        lateinit var appContext: Context

    }
}
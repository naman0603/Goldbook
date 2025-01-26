package com.goldbookapp.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.goldbookapp.MyApplication
import com.goldbookapp.inapplock.managers.AppLock
import com.goldbookapp.ui.activity.settings.AppLockActivityNew

class ScreenOnOffReceiver(appContext: Context) : BroadcastReceiver() {
    companion object {
        private var instance: ScreenOnOffReceiver? = null


        fun getInstance(context: Context): ScreenOnOffReceiver {
            synchronized(ScreenOnOffReceiver) {
                if (instance == null) {
                    instance = ScreenOnOffReceiver(MyApplication.appContext)
                }
                return instance as ScreenOnOffReceiver
            }
        }
    }



    override fun onReceive(context: Context, intent: Intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
           // Log.v("Screen mode", "Screen is in off State”");
        }

        //Your logic comes here whatever you want perform when screen is in off state			                  			            }
        else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
          //  Log.v("Screen mode", "Screen is in On State”");
            //Your logic comes here whatever you want perform when screen is in on state

            //start activity

            //start activity
            if(!AppLockActivityNew.isRunning){
                val i = Intent()
                i.putExtra(AppLock.EXTRA_TYPE, AppLock.UNLOCK_PIN)
                i.setClassName("com.goldbookapp", "com.goldbookapp.ui.activity.settings.AppLockActivityNew")
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(i)
             //   Log.v("LockActivity", "opened from receiver");
                AppLockActivityNew.isRunning = true
            }




           /* val lockManager: LockManager<CustomPinActivity> = LockManager.getInstance() as LockManager<CustomPinActivity>
            lockManager.enableAppLock(contxt, CustomPinActivity::class.java)
            lockManager.getAppLock().logoId = R.drawable.security_lock*/


        }

    }
}


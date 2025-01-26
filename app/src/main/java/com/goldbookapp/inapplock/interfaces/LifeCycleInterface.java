package com.goldbookapp.inapplock.interfaces;

import android.app.Activity;


public interface LifeCycleInterface {

    public void onActivityResumed(Activity activity);

    /**
     * Called in {@link Activity#onUserInteraction()}
     */
    public void onActivityUserInteraction(Activity activity);


    public void onActivityPaused(Activity activity);
}

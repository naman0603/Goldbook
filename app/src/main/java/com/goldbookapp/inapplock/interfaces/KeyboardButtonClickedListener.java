package com.goldbookapp.inapplock.interfaces;

import com.goldbookapp.inapplock.enums.KeyboardButtonEnum;


public interface KeyboardButtonClickedListener {

    /**
     * Receive the click of a button, just after a {@link android.view.View.OnClickListener} has fired.
     * Called before {@link #onRippleAnimationEnd()}.
     * @param keyboardButtonEnum The organized enum of the clicked button
     */
    public void onKeyboardClick(KeyboardButtonEnum keyboardButtonEnum);


    public void onRippleAnimationEnd();

}

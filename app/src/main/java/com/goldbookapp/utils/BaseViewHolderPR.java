package com.goldbookapp.utils;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import com.goldbookapp.model.SearchListPayment;
import com.goldbookapp.model.SearchListReceipt;

import org.jetbrains.annotations.NotNull;

public abstract class BaseViewHolderPR extends RecyclerView.ViewHolder {

    private int mCurrentPosition;

    public BaseViewHolderPR(View itemView) {
        super(itemView);
    }

    protected abstract void clear();

  /*public void onBind(int position) {
    mCurrentPosition = position;
    clear();
  }*/

  /*public int getCurrentPosition() {
    return mCurrentPosition;
  }*/

    public abstract void onBind(@NotNull SearchListPayment.DataPayment dataPayment);

    public abstract void onBind(@NotNull SearchListReceipt.DataReceipt dataReceipt);
}

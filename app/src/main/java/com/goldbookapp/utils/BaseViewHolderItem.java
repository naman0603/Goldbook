package com.goldbookapp.utils;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import com.goldbookapp.model.GetItemListModel;

import org.jetbrains.annotations.NotNull;

public abstract class BaseViewHolderItem extends RecyclerView.ViewHolder {

    private int mCurrentPosition;

    public BaseViewHolderItem(View itemView) {
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

    public abstract void onBind(@NotNull GetItemListModel.Data1077697879 data1077697879);

}

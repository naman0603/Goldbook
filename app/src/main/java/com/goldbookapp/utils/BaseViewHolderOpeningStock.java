package com.goldbookapp.utils;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import com.goldbookapp.model.SearchListOpeningStockModel;
import com.goldbookapp.model.SearchListPurchaseModel;

import org.jetbrains.annotations.NotNull;

public abstract class BaseViewHolderOpeningStock extends RecyclerView.ViewHolder {

    private int mCurrentPosition;

    public BaseViewHolderOpeningStock(View itemView) {
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



    public abstract void onBind(@NotNull SearchListOpeningStockModel.DataOpeningStock dataOpeningStock);
}

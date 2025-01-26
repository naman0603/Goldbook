package com.goldbookapp.utils;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import com.goldbookapp.model.SearchListPurchaseModel;
import com.goldbookapp.model.SearchListSalesModel;

import org.jetbrains.annotations.NotNull;

public abstract class BaseViewHolderSP extends RecyclerView.ViewHolder {

    private int mCurrentPosition;

    public BaseViewHolderSP(View itemView) {
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

    public abstract void onBind(@NotNull SearchListSalesModel.Data1465085328 data1465085328);

    public abstract void onBind(@NotNull SearchListPurchaseModel.DataPurchase dataPurchase);
}

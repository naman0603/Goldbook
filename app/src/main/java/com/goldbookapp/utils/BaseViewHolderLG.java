package com.goldbookapp.utils;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import com.goldbookapp.model.AddChequeBookModel;
import com.goldbookapp.model.SearchListGroupModel;
import com.goldbookapp.model.SearchListLedgerModel;
import com.goldbookapp.model.SearchListPurchaseModel;
import com.goldbookapp.model.SearchListSalesModel;

import org.jetbrains.annotations.NotNull;

public abstract class BaseViewHolderLG extends RecyclerView.ViewHolder {

    private int mCurrentPosition;

    public BaseViewHolderLG(View itemView) {
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

    public abstract void onBind(@NotNull SearchListLedgerModel.DataLedger dataLedger);

    public abstract void onBind(@NotNull SearchListGroupModel.DataGroup dataGroup);
   // public abstract void onBind(@NotNull AddChequeBookModel.AddChequeBookModelItem dataCheque);
}

package com.goldbookapp.utils;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import com.goldbookapp.model.GetListSupplierModel;
import com.goldbookapp.model.SearchListCustomerModel;

import org.jetbrains.annotations.NotNull;

public abstract class BaseViewHolder extends RecyclerView.ViewHolder {

  private int mCurrentPosition;

  public BaseViewHolder(View itemView) {
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

  public abstract void onBind(@NotNull SearchListCustomerModel.Data1037062284 data1037062284);

  public abstract void onBind(@NotNull GetListSupplierModel.Data344525142 data344525142);
}

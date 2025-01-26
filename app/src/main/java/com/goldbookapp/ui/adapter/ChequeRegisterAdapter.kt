package com.goldbookapp.ui.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.goldbookapp.R
import com.goldbookapp.model.AddChequeBookModel
import com.goldbookapp.ui.activity.ledger.ChequeRegisterActivity
import com.goldbookapp.ui.activity.ledger.NewChequeBookActivity
import com.goldbookapp.utils.CommonUtils.Companion.clickWithDebounce
import com.goldbookapp.utils.Constants
import com.google.gson.Gson
import kotlinx.android.synthetic.main.row_cheque_list.view.*

class ChequeRegisterAdapter(
    private val mList: List<AddChequeBookModel.AddChequeBookModelItem>,
    private val isFromEdit: Boolean
) :
    RecyclerView.Adapter<ChequeRegisterAdapter.ViewHolder>() {

    // create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // inflates the card_view_design view
        // that is used to hold list item
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_cheque_list, parent, false)

        return ViewHolder(view)
    }

    // binds the list items to a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val ItemsViewModel = mList[position]


        holder.bind(mList[position], position, isFromEdit)

    }

    // return the number of the items in the list
    override fun getItemCount(): Int {
        return mList.size
    }

    // Holds the views for adding it to image and text
    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        fun bind(
            itemViewModel: AddChequeBookModel.AddChequeBookModelItem,
            position: Int,
            isFromEdit: Boolean?
        ) {
            itemView.apply {
                tvChequeName.setText(itemViewModel.chequeName)
                tvChequeFrom.setText("From: " + itemViewModel.chequeFrom.toString())
                tvChequeTo.setText("To: " + itemViewModel.chequeTo.toString())
                tvChequeTotal.setText("Total Cheques:" + itemViewModel.totalCheque.toString())


                when (isFromEdit) {
                    true -> {
                        imgRight2editCheque.clickWithDebounce {

                            imgRight2editCheque.context.startActivity(
                                Intent(
                                    imgRight2editCheque.context,
                                    NewChequeBookActivity::class.java
                                )
                                    .putExtra(Constants.PREF_CHEQUE_BOOK_POS, position)
                                    //2->Edit Cheque From Edit
                                    .putExtra(Constants.CHEQUE_SAVE_TYPE, "2")
                                    .putExtra(
                                        Constants.PREF_CHEQUE_BOOK_EDITKEY,
                                        Gson().toJson(itemViewModel)
                                    )
                            )

                        }
                        imgRightdelCheque.clickWithDebounce {
                            (context as ChequeRegisterActivity).deleteCheque(position, false)
                        }
                    }
                    false -> {
                        imgRight2editCheque.clickWithDebounce {

                            imgRight2editCheque.context.startActivity(
                                Intent(
                                    imgRight2editCheque.context,
                                    NewChequeBookActivity::class.java
                                )
                                    .putExtra(Constants.PREF_CHEQUE_BOOK_POS, position)
                                    //Edit Cheque From New
                                    .putExtra(Constants.CHEQUE_SAVE_TYPE, "3")
                                    .putExtra(
                                        Constants.PREF_CHEQUE_BOOK_KEY,
                                        Gson().toJson(itemViewModel)
                                    )
                            )

                        }
                        imgRightdelCheque.clickWithDebounce {
                            (context as ChequeRegisterActivity).deleteCheque(position, true)
                        }

                    }
                    else->{

                    }
                }

            }
        }


    }
}
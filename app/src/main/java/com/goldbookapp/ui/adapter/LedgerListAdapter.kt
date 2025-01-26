package com.goldbookapp.ui.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.goldbookapp.R
import com.goldbookapp.model.SearchListGroupModel
import com.goldbookapp.model.SearchListLedgerModel
import com.goldbookapp.ui.activity.ledger.LedgerDetailsActivity
import com.goldbookapp.utils.BaseViewHolderLG
import com.goldbookapp.utils.CommonUtils.Companion.clickWithDebounce
import com.goldbookapp.utils.Constants
import com.google.gson.Gson
import kotlinx.android.synthetic.main.row_ledger_group.view.*

class LedgerListAdapter(
    private val ledgerList: ArrayList<SearchListLedgerModel.DataLedger>,
    private var totalPage: Int
) : RecyclerView.Adapter<BaseViewHolderLG>() {


    companion object {

        private const val VIEW_TYPE_LOADING = 0
        private const val VIEW_TYPE_NORMAL = 1
        private var isLoaderVisible = false
        private var viewDetail: Boolean = false
    }

    class ViewHolder internal constructor(itemView: View?) : BaseViewHolderLG(itemView) {
        override fun clear() {
            //
        }

        override fun onBind(dataLedger: SearchListLedgerModel.DataLedger) {
            bind(dataLedger)
        }

        override fun onBind(dataGroup: SearchListGroupModel.DataGroup) {
            TODO("Not yet implemented")
        }

        fun bind(ledgerModel: SearchListLedgerModel.DataLedger) {
            itemView.apply {
                tv_sl_firstRow.text = ledgerModel.ledger_name
                tv_sl_secondRow.text = "Group: " + ledgerModel.group_name

                if (!ledgerModel.sub_group_name.isNullOrEmpty()) {
                    tv_sl_thirdRow.text = "Sub Group: " + ledgerModel.sub_group_name
                } else {
                    tv_sl_thirdRow.text = "Sub Group: -"
                }
                cardSalesItemRowLedgerGroup.clickWithDebounce {
                    when (viewDetail) {
                        true -> {
                            cardSalesItemRowLedgerGroup.context.startActivity(
                                Intent(
                                    cardSalesItemRowLedgerGroup.context,
                                    LedgerDetailsActivity::class.java
                                )
                                    .putExtra(
                                        Constants.LEDGER_DETAIL_KEY,
                                        Gson().toJson(ledgerModel)
                                    )
                            )
                        }
                        else->{

                        }
                    }

                }
            }
        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolderLG {
        return when (viewType) {
            VIEW_TYPE_NORMAL -> ViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.row_ledger_group, parent, false)
            )
            VIEW_TYPE_LOADING -> ProgressHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_loading, parent, false)
            )
            else -> throw IllegalArgumentException("Different View Type")
        }
    }

    override fun getItemCount(): Int = ledgerList.size

    override fun onBindViewHolder(holder: BaseViewHolderLG, position: Int) {
        holder.onBind(ledgerList[position])
    }

    fun addLoading() {
        isLoaderVisible = true
        notifyItemInserted(ledgerList.size - 1)
    }

    fun removeLoading() {
        isLoaderVisible = false
        notifyItemRemoved(ledgerList.size - 1)
        // }
    }

    fun clear() {
        ledgerList.clear()
        notifyDataSetChanged()
    }

    class ProgressHolder internal constructor(itemView: View?) : BaseViewHolderLG(itemView) {
        override fun clear() {

        }

        override fun onBind(dataLedger: SearchListLedgerModel.DataLedger) {

        }

        override fun onBind(dataGroup: SearchListGroupModel.DataGroup) {
            TODO("Not yet implemented")
        }


    }

    override fun getItemViewType(position: Int): Int {
        when (totalPage > 1) {
            true -> {
                return if (isLoaderVisible) {
                    if (position == ledgerList.size - 1) VIEW_TYPE_LOADING else VIEW_TYPE_NORMAL
                } else {
                    VIEW_TYPE_NORMAL
                }
            }
            false -> {
                return VIEW_TYPE_NORMAL
            }
        }

    }

    fun addLedger(
        ledgerList: ArrayList<SearchListLedgerModel.DataLedger>?,
        fromSearch: Boolean,
        pageSize: Int,
        currentPage: Int,
        totalPage: Int
    ) {
        this.totalPage = totalPage
        if ((fromSearch && this.ledgerList.size <= pageSize) && !(currentPage > 1) /*&& totalPage == 1*/) {
            this.ledgerList.clear()

        }
        this.ledgerList.apply {
            //clear()
            if (ledgerList != null) {
                addAll(ledgerList)
            }
        }

    }

    fun viewDetail(viewdetail: Boolean) {
        viewDetail = viewdetail
    }

}



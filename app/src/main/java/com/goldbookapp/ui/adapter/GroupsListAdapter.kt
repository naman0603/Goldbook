package com.goldbookapp.ui.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.goldbookapp.R
import com.goldbookapp.model.SearchListGroupModel
import com.goldbookapp.model.SearchListLedgerModel
import com.goldbookapp.ui.activity.group.GroupDetailsActivity
import com.goldbookapp.ui.activity.group.ParentGroupDetailsActivity
import com.goldbookapp.utils.BaseViewHolderLG
import com.goldbookapp.utils.CommonUtils.Companion.clickWithDebounce
import com.goldbookapp.utils.Constants
import com.google.gson.Gson
import kotlinx.android.synthetic.main.row_ledger_group.view.*

class GroupsListAdapter(
    private val groupList: ArrayList<SearchListGroupModel.DataGroup>,
    private var totalPage: Int
) : RecyclerView.Adapter<BaseViewHolderLG>() {

    companion object {

        private const val VIEW_TYPE_LOADING = 0
        private const val VIEW_TYPE_NORMAL = 1
        private var isLoaderVisible = false
        private var viewDetail: Boolean = false
        private var viewSubGroupDetail: Boolean = false
    }

    class ViewHolder internal constructor(itemView: View?) : BaseViewHolderLG(itemView) {
        override fun clear() {

        }

        override fun onBind(dataLedger: SearchListLedgerModel.DataLedger) {
            TODO("Not yet implemented")
        }


        override fun onBind(dataGroup: SearchListGroupModel.DataGroup) {
            bind(dataGroup)
        }

        fun bind(groupModel: SearchListGroupModel.DataGroup) {
            itemView.apply {
                tv_sl_firstRow.text = groupModel.group_name
                if (groupModel.parent_group_name.isBlank()) {
                    tv_sl_secondRow.text = groupModel.nature_group_name
                    cardSalesItemRowLedgerGroup.clickWithDebounce {
                        when (viewDetail) {
                            true -> {
                                cardSalesItemRowLedgerGroup.context.startActivity(
                                    Intent(
                                        cardSalesItemRowLedgerGroup.context,
                                        GroupDetailsActivity::class.java
                                    )
                                        .putExtra(
                                            Constants.GROUP_DETAIL_KEY,
                                            Gson().toJson(groupModel)
                                        )
                                )
                            }
                            else->{

                            }
                        }
                    }
                } else {
                    tv_sl_secondRow.text = "Parent Group: " + groupModel.parent_group_name
                    cardSalesItemRowLedgerGroup.clickWithDebounce {
                        when (viewSubGroupDetail) {
                            true -> {
                                cardSalesItemRowLedgerGroup.context.startActivity(
                                    Intent(
                                        cardSalesItemRowLedgerGroup.context,
                                        ParentGroupDetailsActivity::class.java
                                    )
                                        .putExtra(
                                            Constants.GROUP_DETAIL_KEY,
                                            Gson().toJson(groupModel)
                                        )
                                )
                            }
                            else->{

                            }
                        }

                    }
                }
                tv_sl_thirdRow.visibility = GONE

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
    } /*=
        DataViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.row_sales, parent, false))
*/

    override fun getItemCount(): Int = groupList.size

    override fun onBindViewHolder(holder: BaseViewHolderLG, position: Int) {
        holder.onBind(groupList[position])
    }

    fun addLoading() {
        isLoaderVisible = true
        notifyItemInserted(groupList.size - 1)
    }

    fun removeLoading() {
        isLoaderVisible = false
        notifyItemRemoved(groupList.size - 1)
        // }
    }

    fun clear() {
        groupList.clear()
        notifyDataSetChanged()
    }

    class ProgressHolder internal constructor(itemView: View?) : BaseViewHolderLG(itemView) {
        override fun clear() {

        }

        override fun onBind(dataLedger: SearchListLedgerModel.DataLedger) {
            TODO("Not yet implemented")
        }

        override fun onBind(dataGroup: SearchListGroupModel.DataGroup) {

        }


    }

    override fun getItemViewType(position: Int): Int {
        when (totalPage > 1) {
            true -> {
                return if (isLoaderVisible) {
                    if (position == groupList.size - 1) VIEW_TYPE_LOADING else VIEW_TYPE_NORMAL
                } else {
                    VIEW_TYPE_NORMAL
                }
            }
            false -> {
                return VIEW_TYPE_NORMAL
            }
        }

    }

    fun addGroup(
        groupList: ArrayList<SearchListGroupModel.DataGroup>?,
        fromSearch: Boolean,
        pageSize: Int,
        currentPage: Int,
        totalPage: Int
    ) {
        this.totalPage = totalPage
        if ((fromSearch && this.groupList.size <= pageSize) && !(currentPage > 1) /*&& totalPage == 1*/) {
            this.groupList.clear()
            /*Log.v("clearlist","purchase list cleared")*/
        }
        this.groupList.apply {
            //clear()
            if (groupList != null) {
                addAll(groupList)
            }
        }

    }

    fun viewDetail(viewdetail: Boolean) {
        viewDetail = viewdetail
    }

    fun viewSubGroupDetail(viewsubgroupdetail: Boolean) {
        viewSubGroupDetail = viewsubgroupdetail
    }


}
package com.goldbookapp.ui.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.goldbookapp.R
import com.goldbookapp.model.BranchListModel
import com.goldbookapp.ui.activity.settings.BranchListActivity
import com.goldbookapp.utils.CommonUtils
import com.goldbookapp.utils.CommonUtils.Companion.clickWithDebounce
import kotlinx.android.synthetic.main.row_branchlist.view.*

class BranchListAdapter(
    private val branchList: ArrayList<BranchListModel.Data.Branches>,
    private var viewDetail: Boolean
) :
    RecyclerView.Adapter<BranchListAdapter.DataViewHolder>() {

    class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(branchlistModel: BranchListModel.Data.Branches, position: Int, viewDetail: Boolean) {
            itemView.apply {
                txtBranchNameRowBranch.text = branchlistModel.branch_name
                txtStateRowBranch.text = branchlistModel.state_name
                ll_row_Branch.setOnFocusChangeListener { view, b ->
                    CommonUtils.hideKeyboardnew(
                        ll_row_Branch.context as Activity
                    )
                }

                cardRowBranch.clickWithDebounce {
                    when(viewDetail){
                        true ->{
                            (context as BranchListActivity).branchDetails(branchlistModel.id)
                        }else->{

                    }
                    }

                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataViewHolder =
        DataViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.row_branchlist, parent, false)
        )

    override fun getItemCount(): Int = branchList.size

    override fun onBindViewHolder(holder: DataViewHolder, position: Int) {
        holder.bind(branchList[position], position, viewDetail)
    }

    fun addBranches(branchList: ArrayList<BranchListModel.Data.Branches>?, viewDetail: Boolean) {
        this.branchList.apply {
            clear()
            if (branchList != null) {
                addAll(branchList)
            }
        }
        this.viewDetail = viewDetail

    }
}


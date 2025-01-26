package com.goldbookapp.ui.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.goldbookapp.R
import com.goldbookapp.model.GetUserCompaniesModel
import com.goldbookapp.ui.activity.organization.OrganizationListActivity
import com.goldbookapp.utils.CommonUtils
import com.goldbookapp.utils.CommonUtils.Companion.clickWithDebounce
import kotlinx.android.synthetic.main.row_organization.view.*

class OrganizationsListAdapter(
    private val organizationsList: ArrayList<GetUserCompaniesModel.Data.Companies>,
    private var viewDetail: Boolean
) : RecyclerView.Adapter<OrganizationsListAdapter.DataViewHolder>(){

    class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(
            organizationsModel: GetUserCompaniesModel.Data.Companies,
            position: Int,
            viewDetail: Boolean
        ) {
            itemView.apply {
                tvOrgName.text = organizationsModel.company_name

                ll_row_Org.setOnFocusChangeListener { view, b -> CommonUtils.hideKeyboardnew(
                    ll_row_Org.context as Activity
                ) }

                cardOrgsItemRowOrg.clickWithDebounce {
                    when(viewDetail){
                        true -> {
                            (context as OrganizationListActivity).orgDetails(organizationsModel.id)
                        }else->{

                    }
                    }

                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataViewHolder =
        DataViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.row_organization, parent, false))

    override fun getItemCount(): Int = organizationsList.size

    override fun onBindViewHolder(holder: DataViewHolder, position: Int) {
        holder.bind(organizationsList[position],position, viewDetail)
    }

    fun addOrg(
        organizationsList: ArrayList<GetUserCompaniesModel.Data.Companies>?,
        viewDetail: Boolean
    ) {
        this.organizationsList.apply {
            clear()
            if (organizationsList != null) {
                addAll(organizationsList)
            }
        }
        this.viewDetail = viewDetail

    }
}


package com.goldbookapp.ui.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.goldbookapp.R
import com.goldbookapp.model.GetListSupplierModel
import com.goldbookapp.model.SearchListCustomerModel
import com.goldbookapp.ui.activity.customer.CustomerDetailsActivity
import com.goldbookapp.utils.BaseViewHolder
import com.goldbookapp.utils.CommonUtils.Companion.clickWithDebounce
import com.goldbookapp.utils.Constants
import com.google.gson.Gson
import kotlinx.android.synthetic.main.row_customer.view.*

class CustomerListAdapter(
    private val customerList: ArrayList<SearchListCustomerModel.Data1037062284>,
    private var totalPage: Int

) : RecyclerView.Adapter<BaseViewHolder>() {
    companion object {
        private const val VIEW_TYPE_LOADING = 0
        private const val VIEW_TYPE_NORMAL = 1
        private var isLoaderVisible = false
        private var viewDetail: Boolean = false
    }

    class ViewHolder internal constructor(itemView: View?) : BaseViewHolder(itemView) {
        override fun clear() {}


        override fun onBind(custmodel: SearchListCustomerModel.Data1037062284) {
            bind(custmodel)
        }

        override fun onBind(data344525142: GetListSupplierModel.Data344525142) {
            //TODO("Not yet implemented")
        }

        fun bind(
            customerModel: SearchListCustomerModel.Data1037062284
        ) {
            itemView.apply {
                tvNameRowCust.text = customerModel.display_name
                when (customerModel.fine_balance) {
                    "0.000" -> {
                        tvLeftBalRowCust.text = customerModel.fine_balance + " (fine)"
                        tvLeftBalRowCust.setTextColor(
                            ContextCompat.getColor(
                                context,
                                R.color.header_black_text
                            )
                        )
                    }

                    else -> {
                        tvLeftBalRowCust.text =
                            customerModel.fine_balance + " " + customerModel.opening_fine_default_term
                        when (customerModel.opening_fine_default_term) {
                            "Dr" -> {
                                tvLeftBalRowCust.setTextColor(
                                    ContextCompat.getColor(
                                        context,
                                        R.color.debit_color
                                    )
                                )
                            }
                            "U" -> {
                                tvLeftBalRowCust.setTextColor(
                                    ContextCompat.getColor(
                                        context,
                                        R.color.debit_color
                                    )
                                )
                            }
                            "Rec"->{
                                tvLeftBalRowCust.setTextColor(
                                    ContextCompat.getColor(
                                        context,
                                        R.color.debit_color
                                    )
                                )
                            }
                            "L"->{
                                tvLeftBalRowCust.setTextColor(
                                    ContextCompat.getColor(
                                        context,
                                        R.color.debit_color
                                    )
                                )
                            }
                            else -> {
                                tvLeftBalRowCust.setTextColor(
                                    ContextCompat.getColor(
                                        context,
                                        R.color.credit_color
                                    )
                                )
                            }
                        }
                    }
                        /*if (tvLeftBalRowCust.text.contains("Dr", ignoreCase = true)) {
                            tvLeftBalRowCust.setTextColor(
                                ContextCompat.getColor(
                                    context,
                                    R.color.debit_color
                                )
                            )
                        } else


                    }*/
                }


                when (customerModel.silver_fine_balance) {
                    "0.000" -> {
                        tvLeftSilverBalRowCust.text = customerModel.silver_fine_balance + " (fine)"
                        tvLeftSilverBalRowCust.setTextColor(
                            ContextCompat.getColor(
                                context,
                                R.color.header_black_text
                            )
                        )
                    }

                    else -> {
                        tvLeftSilverBalRowCust.text =
                            customerModel.silver_fine_balance + " " + customerModel.opening_silver_fine_default_term
                        when (customerModel.opening_silver_fine_default_term) {
                            "Dr" -> {
                                tvLeftSilverBalRowCust.setTextColor(
                                    ContextCompat.getColor(
                                        context,
                                        R.color.debit_color
                                    )
                                )
                            }
                            "U" -> {
                                tvLeftSilverBalRowCust.setTextColor(
                                    ContextCompat.getColor(
                                        context,
                                        R.color.debit_color
                                    )
                                )
                            }
                            "Rec"->{
                                tvLeftSilverBalRowCust.setTextColor(
                                    ContextCompat.getColor(
                                        context,
                                        R.color.debit_color
                                    )
                                )
                            }
                            "L"->{
                                tvLeftSilverBalRowCust.setTextColor(
                                    ContextCompat.getColor(
                                        context,
                                        R.color.debit_color
                                    )
                                )
                            }
                            else -> {
                                tvLeftSilverBalRowCust.setTextColor(
                                    ContextCompat.getColor(
                                        context,
                                        R.color.credit_color
                                    )
                                )
                            }
                        }
                    }
                    /*if (tvLeftBalRowCust.text.contains("Dr", ignoreCase = true)) {
                        tvLeftBalRowCust.setTextColor(
                            ContextCompat.getColor(
                                context,
                                R.color.debit_color
                            )
                        )
                    } else


                }*/
                }

                when (customerModel.cash_balance) {
                    "0.00" -> {
                        tvRightBalRowCust.text = customerModel.cash_balance + " (cash)"
                        tvRightBalRowCust.setTextColor(
                            ContextCompat.getColor(
                                context,
                                R.color.header_black_text
                            )
                        )
                    }
                    else -> {
                        tvRightBalRowCust.text =
                            customerModel.cash_balance + " " + customerModel.opening_cash_default_term
                        when (customerModel.opening_cash_default_term) {
                            "Dr" -> {
                                tvRightBalRowCust.setTextColor(
                                    ContextCompat.getColor(
                                        context,
                                        R.color.debit_color
                                    )
                                )
                            }
                            "U" -> {
                                tvRightBalRowCust.setTextColor(
                                    ContextCompat.getColor(
                                        context,
                                        R.color.debit_color
                                    )
                                )
                            }
                            "Rec"->{
                                tvRightBalRowCust.setTextColor(
                                    ContextCompat.getColor(
                                        context,
                                        R.color.debit_color
                                    )
                                )
                            }
                            "L"->{
                                tvRightBalRowCust.setTextColor(
                                    ContextCompat.getColor(
                                        context,
                                        R.color.debit_color
                                    )
                                )
                            }
                            else -> {
                                tvRightBalRowCust.setTextColor(
                                    ContextCompat.getColor(
                                        context,
                                        R.color.credit_color
                                    )
                                )
                            }
                        }
                    }
                }

                cardCustomerItemRowCust.clickWithDebounce {
                    when(viewDetail){
                        true -> {
                            cardCustomerItemRowCust.context.startActivity(
                                Intent(cardCustomerItemRowCust.context, CustomerDetailsActivity::class.java)
                                    .putExtra(Constants.CUSTOMER_DETAIL_KEY, Gson().toJson(customerModel))
                            )
                        }else->{

                    }
                    }
                }
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {

        return when (viewType) {
            VIEW_TYPE_NORMAL -> ViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.row_customer, parent, false)
            )
            VIEW_TYPE_LOADING -> ProgressHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_loading, parent, false)
            )
            else -> throw IllegalArgumentException("Different View Type")
        }

    }


    override fun getItemCount(): Int = customerList.size

    fun addLoading() {
        isLoaderVisible = true

        notifyItemInserted(customerList.size - 1)
    }

    fun removeLoading() {
        isLoaderVisible = false
        notifyItemRemoved(customerList.size - 1)

    }

    fun viewDetail(viewdetail: Boolean) {
        viewDetail = viewdetail
    }
    fun clear() {
        customerList.clear()
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.onBind(customerList[position])

    }

    override fun getItemViewType(position: Int): Int {
        when (totalPage > 1) {
            true -> {
                return if (isLoaderVisible) {
                    if (position == customerList.size - 1) VIEW_TYPE_LOADING else VIEW_TYPE_NORMAL
                } else {
                    VIEW_TYPE_NORMAL
                }
            }
            false -> {
                return VIEW_TYPE_NORMAL
            }
        }

    }


    class ProgressHolder internal constructor(itemView: View?) : BaseViewHolder(itemView) {
        override fun clear() {}
        override fun onBind(data1037062284: SearchListCustomerModel.Data1037062284) {
            // nothing to do
        }

        override fun onBind(data344525142: GetListSupplierModel.Data344525142) {

        }

    }

    fun addCustomer(
        customerList: List<SearchListCustomerModel.Data1037062284>?,
        fromSearch: Boolean,
        pageSize: Int,
        currentPage: Int,
        totalPage: Int
    ) {
        this.totalPage = totalPage
        if ((fromSearch && this.customerList.size <= pageSize) && !(currentPage > 1) /*&& totalPage == 1*/) {
            this.customerList.clear()

        }


        this.customerList.apply {
            // clear()
            if (customerList != null) {
                addAll(customerList)
            }
        }

    }
}


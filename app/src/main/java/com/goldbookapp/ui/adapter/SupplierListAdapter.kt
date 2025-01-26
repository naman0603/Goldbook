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
import com.goldbookapp.ui.activity.supplier.SupplierDetailsActivity
import com.goldbookapp.utils.BaseViewHolder
import com.goldbookapp.utils.CommonUtils.Companion.clickWithDebounce
import com.goldbookapp.utils.Constants
import com.google.gson.Gson
import kotlinx.android.synthetic.main.row_supplier.view.*

class SupplierListAdapter(
    private val supplierList: ArrayList<GetListSupplierModel.Data344525142>,
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
            //
        }

        override fun onBind(suppModel: GetListSupplierModel.Data344525142) {
            bind(suppModel)
        }

        fun bind(supplierModel: GetListSupplierModel.Data344525142) {
            itemView.apply {
                tvNameRowSupplier.text = supplierModel.display_name
                when (supplierModel.fine_balance) {
                    "0.000" -> {
                        tvLeftBalRowSupplier.text = supplierModel.fine_balance
                        tvLeftBalRowSupplier.setTextColor(
                            ContextCompat.getColor(
                                context,
                                R.color.header_black_text
                            )
                        )
                    }
                    else -> {
                        tvLeftBalRowSupplier.text =
                            supplierModel.fine_balance + " " + supplierModel.opening_fine_default_term

                        when (supplierModel.opening_fine_default_term) {
                            "Dr" -> {
                                tvLeftBalRowSupplier.setTextColor(
                                    ContextCompat.getColor(
                                        context,
                                        R.color.debit_color
                                    )
                                )
                            }
                            "U" -> {
                                tvLeftBalRowSupplier.setTextColor(
                                    ContextCompat.getColor(
                                        context,
                                        R.color.debit_color
                                    )
                                )
                            }
                            "Rec" -> {
                                tvLeftBalRowSupplier.setTextColor(
                                    ContextCompat.getColor(
                                        context,
                                        R.color.debit_color
                                    )
                                )
                            }
                            "L" -> {
                                tvLeftBalRowSupplier.setTextColor(
                                    ContextCompat.getColor(
                                        context,
                                        R.color.debit_color
                                    )
                                )
                            }
                            else -> {
                                tvLeftBalRowSupplier.setTextColor(
                                    ContextCompat.getColor(
                                        context,
                                        R.color.credit_color
                                    )
                                )
                            }
                        }
                    }

                }

                when (supplierModel.silver_fine_balance) {
                    "0.000" -> {
                        tvLeftSilverBalRowSupplier.text = supplierModel.silver_fine_balance
                        tvLeftSilverBalRowSupplier.setTextColor(
                            ContextCompat.getColor(
                                context,
                                R.color.header_black_text
                            )
                        )
                    }
                    else -> {
                        tvLeftSilverBalRowSupplier.text =
                            supplierModel.silver_fine_balance + " " + supplierModel.opening_silver_fine_default_term

                        when (supplierModel.opening_silver_fine_default_term) {
                            "Dr" -> {
                                tvLeftSilverBalRowSupplier.setTextColor(
                                    ContextCompat.getColor(
                                        context,
                                        R.color.debit_color
                                    )
                                )
                            }
                            "U" -> {
                                tvLeftSilverBalRowSupplier.setTextColor(
                                    ContextCompat.getColor(
                                        context,
                                        R.color.debit_color
                                    )
                                )
                            }
                            "Rec" -> {
                                tvLeftSilverBalRowSupplier.setTextColor(
                                    ContextCompat.getColor(
                                        context,
                                        R.color.debit_color
                                    )
                                )
                            }
                            "L" -> {
                                tvLeftSilverBalRowSupplier.setTextColor(
                                    ContextCompat.getColor(
                                        context,
                                        R.color.debit_color
                                    )
                                )
                            }
                            else -> {
                                tvLeftSilverBalRowSupplier.setTextColor(
                                    ContextCompat.getColor(
                                        context,
                                        R.color.credit_color
                                    )
                                )
                            }
                        }
                    }

                }

                when (supplierModel.cash_balance) {
                    "0.00" -> {
                        tvRightBalRowSupplier.text = supplierModel.cash_balance
                        tvRightBalRowSupplier.setTextColor(
                            ContextCompat.getColor(
                                context,
                                R.color.header_black_text
                            )
                        )
                    }
                    else -> {
                        tvRightBalRowSupplier.text =
                            supplierModel.cash_balance + " " + supplierModel.opening_cash_default_term

                        when (supplierModel.opening_cash_default_term) {
                            "Dr" -> {
                                tvRightBalRowSupplier.setTextColor(
                                    ContextCompat.getColor(
                                        context,
                                        R.color.debit_color
                                    )
                                )
                            }
                            "U" -> {
                                tvRightBalRowSupplier.setTextColor(
                                    ContextCompat.getColor(
                                        context,
                                        R.color.debit_color
                                    )
                                )
                            }
                            "Rec" -> {
                                tvRightBalRowSupplier.setTextColor(
                                    ContextCompat.getColor(
                                        context,
                                        R.color.debit_color
                                    )
                                )
                            }
                            "L" -> {
                                tvRightBalRowSupplier.setTextColor(
                                    ContextCompat.getColor(
                                        context,
                                        R.color.debit_color
                                    )
                                )
                            }
                            else -> {
                                tvRightBalRowSupplier.setTextColor(
                                    ContextCompat.getColor(
                                        context,
                                        R.color.credit_color
                                    )
                                )
                            }
                        }

                    }

                }


                cardSupplierItemRowSupplier.clickWithDebounce {
                    when (viewDetail) {
                        true -> {
                            cardSupplierItemRowSupplier.context.startActivity(
                                Intent(
                                    cardSupplierItemRowSupplier.context,
                                    SupplierDetailsActivity::class.java
                                )
                                    .putExtra(
                                        Constants.SUPPLIER_DETAIL_KEY,
                                        Gson().toJson(supplierModel)
                                    )
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
                    .inflate(R.layout.row_supplier, parent, false)
            )
            VIEW_TYPE_LOADING -> ProgressHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_loading, parent, false)
            )
            else -> throw IllegalArgumentException("Different View Type")
        }
        //}
    }

    override fun getItemCount(): Int = supplierList.size

    fun addLoading() {
        SupplierListAdapter.isLoaderVisible = true
        notifyItemInserted(supplierList.size - 1)
    }

    fun removeLoading() {
        SupplierListAdapter.isLoaderVisible = false
        notifyItemRemoved(supplierList.size - 1)
    }

    fun clear() {
        supplierList.clear()
        notifyDataSetChanged()
    }


    fun addSupplier(
        customerList: List<GetListSupplierModel.Data344525142>?,
        fromSearch: Boolean,
        pageSize: Int,
        currentPage: Int,
        totalPage: Int
    ) {

        this.totalPage = totalPage
        if ((fromSearch && this.supplierList.size <= pageSize) && !(currentPage > 1) /*&& totalPage == 1*/) {
            this.supplierList.clear()

        }


        this.supplierList.apply {
            //clear()
            if (customerList != null) {
                addAll(customerList)
            }
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.onBind(supplierList[position])
    }

    override fun getItemViewType(position: Int): Int {
        when (totalPage > 1) {
            true -> {
                return if (isLoaderVisible) {
                    if (position == supplierList.size - 1) VIEW_TYPE_LOADING else VIEW_TYPE_NORMAL
                } else {
                    VIEW_TYPE_NORMAL
                }
            }
            false -> {
                return VIEW_TYPE_NORMAL
            }
        }

    }

    fun viewDetail(viewdetail: Boolean) {
        viewDetail = viewdetail
    }

    class ProgressHolder internal constructor(itemView: View?) : BaseViewHolder(itemView) {
        override fun clear() {}
        override fun onBind(data1037062284: SearchListCustomerModel.Data1037062284) {
            // nothing to do
        }

        override fun onBind(data344525142: GetListSupplierModel.Data344525142) {

        }


    }

}
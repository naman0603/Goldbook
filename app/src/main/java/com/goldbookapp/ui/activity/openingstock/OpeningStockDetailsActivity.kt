package com.goldbookapp.ui.activity.openingstock

import OpeningStockDetailModel
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.NetworkUtils
import com.goldbookapp.R
import com.goldbookapp.api.ApiHelper
import com.goldbookapp.api.RetrofitBuilder
import com.goldbookapp.databinding.ActivityOpeningStockDetailsBinding
import com.goldbookapp.model.LoginModel
import com.goldbookapp.model.OpeningStockItemCalculationModel
import com.goldbookapp.model.SearchListOpeningStockModel
import com.goldbookapp.ui.activity.viewmodel.OpeningStockDetailViewModel
import com.goldbookapp.ui.activity.viewmodel.ViewModelFactory
import com.goldbookapp.ui.adapter.OpeningStockItemAdapter
import com.goldbookapp.utils.CommonUtils
import com.goldbookapp.utils.CommonUtils.Companion.clickWithDebounce
import com.goldbookapp.utils.Constants
import com.goldbookapp.utils.PreferenceHelper
import com.goldbookapp.utils.PreferenceHelper.get
import com.goldbookapp.utils.Status
import com.google.gson.Gson
import com.hamzasharuf.networkmonitor.ConnectivityStateHolder
import com.hamzasharuf.networkmonitor.Event
import com.hamzasharuf.networkmonitor.NetworkEvents
import kotlinx.android.synthetic.main.activity_opening_stock_details.*
import kotlinx.android.synthetic.main.activity_opening_stock_details.view.*
import kotlinx.android.synthetic.main.toolbar_layout.*

class OpeningStockDetailsActivity : AppCompatActivity() {

    private lateinit var viewModel: OpeningStockDetailViewModel
    lateinit var binding: ActivityOpeningStockDetailsBinding
    lateinit var openingStockDetailItemModel: OpeningStockDetailModel.OpeningStock
    lateinit var loginModel: LoginModel
    var transactionID: String? = ""
    lateinit var popupMenu: PopupMenu
    private var status: String? = "3"
    private lateinit var adapter: OpeningStockItemAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_opening_stock_details)
        val view = binding.root
        setupViewModel()
        setupUIandListner()
    }

    private fun setupViewModel() {
        viewModel =
            ViewModelProvider(this, ViewModelFactory(ApiHelper(RetrofitBuilder.apiService))).get(
                OpeningStockDetailViewModel::class.java
            )
        binding.setLifecycleOwner(this)

    }

    override fun onResume() {
        super.onResume()

        NetworkEvents.observe(this, Observer {
            if (it is Event.ConnectivityEvent) {
                handleConnectivityChange()
            }
        })
    }

    private fun handleConnectivityChange() {
        if (ConnectivityStateHolder.isConnected) {
            // Network is available
            CommonUtils.hideInternetDialog()

            openingStockDetailAPI(
                loginModel?.data?.bearer_access_token,
                transactionID
            )

        }

        if (!ConnectivityStateHolder.isConnected) {
            // Network is not available
            CommonUtils.showDialog(this, getString(R.string.please_check_internet_msg))

        }

    }

    private fun setupUIandListner() {
        val prefs = PreferenceHelper.defaultPrefs(this)
        loginModel = Gson().fromJson(
            prefs[Constants.PREF_LOGIN_DETAIL_KEY, ""],
            LoginModel::class.java
        ) //getter

        tvTitle.setText("Opening Stock Details")
        imgLeft.setImageResource(R.drawable.ic_back)
        imgRight2.setImageResource(R.drawable.ic_edit)
        imgRight.setImageResource(R.drawable.ic_delete_icon)


        binding.root.rv_openingStockdetail_item.layoutManager = LinearLayoutManager(this)
        adapter = OpeningStockItemAdapter(arrayListOf(), false)
        binding.root.rv_openingStockdetail_item.adapter = adapter

        imgLeft?.clickWithDebounce {
            onBackPressed()
        }

        imgRight2.clickWithDebounce {
            if (this::openingStockDetailItemModel.isInitialized) {
                Log.v("click", "true")
                startActivity(
                    Intent(this, NewOpeningStockActivity::class.java)
                        .putExtra(
                            Constants.OPENING_STOCK_DETAIL_KEY,
                            Gson().toJson(openingStockDetailItemModel)
                        )
                )
                finish()
            }
        }


        imgRight.clickWithDebounce {
            ensureDeleteDialog()

        }
        if (intent.extras?.containsKey(Constants.ModuleID)!!) {
            transactionID = intent.getStringExtra(Constants.ModuleID)
        }


        if (intent.extras?.containsKey(Constants.OPENING_STOCK_DETAIL_KEY)!!) {
            var openingStock_str: String? =
                intent.getStringExtra(Constants.OPENING_STOCK_DETAIL_KEY)
            var openingStockDetailModel: SearchListOpeningStockModel.DataOpeningStock =
                Gson().fromJson(
                    openingStock_str,
                    SearchListOpeningStockModel.DataOpeningStock::class.java
                )
            transactionID = openingStockDetailModel.transaction_id
            /* tvTitle.text = purchaseDetailModel.invoice_number
             tv_purchasebilldetail_custname.text = purchaseDetailModel.contact_name
             tv_purchasebilldetail_noofitems.text =
                 purchaseDetailModel.no_of_items.toString() + " item"
             tv_purchasebilldetail_transactiondate.text = purchaseDetailModel.transaction_date*/

        }


    }

    private fun ensureDeleteDialog() {
        val builder = AlertDialog.Builder(this)
        val DeleteClick = { dialog: DialogInterface, which: Int ->
            //item delete api call
            deleteOpeningStock(loginModel?.data?.bearer_access_token, transactionID)
        }
        val dialogdismiss = { dialog: DialogInterface, which: Int ->
            dialog.dismiss()
        }
        with(builder)
        {
            setTitle(context.getString(R.string.delItemDialog2Title))
            setMessage(context.getString(R.string.itemDialog2Message))
            setPositiveButton(context.getString(R.string.Cancel), dialogdismiss)
            setNeutralButton(
                context.getString(R.string.Delete),
                DialogInterface.OnClickListener(function = DeleteClick)
            )
            show()
        }
    }

    private fun deleteOpeningStock(token: String?, transaction_id: String?) {
        if (NetworkUtils.isConnected()) {
            if (CommonUtils.isValidClickPressed()) {
                viewModel.deleteOpeningStock(token, transaction_id).observe(this, Observer {
                    it?.let { resource ->
                        when (resource.status) {
                            Status.SUCCESS -> {
                                if (it.data?.status == true) {

                                    Toast.makeText(
                                        this,
                                        it.data?.message,
                                        Toast.LENGTH_LONG
                                    )
                                        .show()
                                    finish()

                                } else {

                                    when (it.data!!.code == Constants.ErrorCode) {
                                        true -> {
                                            Toast.makeText(
                                                this,
                                                it.data.errormessage?.message,
                                                Toast.LENGTH_LONG
                                            )
                                                .show()
                                        }
                                        false -> {
                                            CommonUtils.somethingWentWrong(this)
                                        }

                                    }
                                }

                                CommonUtils.hideProgress()


                            }
                            Status.ERROR -> {
                                CommonUtils.hideProgress()

                            }
                            Status.LOADING -> {
                                CommonUtils.showProgress(this)
                            }
                        }
                    }
                })
            }
        }
    }

    fun openingStockDetailAPI(
        token: String?,
        transaction_id: String?
    ) {

        if (NetworkUtils.isConnected()) {

            viewModel.openingStockDetail(token, transaction_id).observe(this, Observer {
                it?.let { resource ->
                    when (resource.status) {
                        Status.SUCCESS -> {

                            if (it.data?.status == true) {
                                llOpeningStockDetail_root.visibility = View.VISIBLE
                                openingStockDetailItemModel = it.data.data!!
                                Log.v(
                                    "transaction",
                                    "" + openingStockDetailItemModel.transaction_date
                                )
                                binding.tvPurchasebilldetailTransactiondate.setText(
                                    openingStockDetailItemModel.transaction_date
                                )
                                binding.tvOpeningStockdetailRightGrossWt.setText(openingStockDetailItemModel.total_gross_wt)
                                binding.tvOpeningStockdetailRightLessWt.setText(openingStockDetailItemModel.total_less_wt)
                                binding.tvOpeningStockdetailRightFineWt.setText(openingStockDetailItemModel.total_fine_wt)
                                binding.tvOpeningStockdetailNetWt.setText(openingStockDetailItemModel.total_net_wt)
                                binding.tvOpeningStockdetailRightTotalCharges.setText(openingStockDetailItemModel.total_misc_charges)
                                binding.tvOpeningStockdetailNotes.setText(openingStockDetailItemModel.remarks)
                                // tvTitle.text = openingStockDetailModel.data.
                                retrieveListforitem(openingStockDetailItemModel.item)
                            }
                            CommonUtils.hideProgress()
                        }
                        Status.ERROR -> {
                            CommonUtils.hideProgress()

                        }
                        Status.LOADING -> {
                            CommonUtils.showProgress(this)
                        }
                    }
                }
            })
        }
    }

    private fun retrieveListforitem(item: List<OpeningStockItemCalculationModel.OpeningStockItemCalcModelItem>?) {
        adapter.apply {
            addpurchasebillrow_item(item)
            notifyDataSetChanged()
        }

    }


}
package com.goldbookapp.ui.activity.sales

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.goldbookapp.R
import com.goldbookapp.databinding.AddSalesLineActivityBinding
import com.goldbookapp.model.LoginModel
import com.goldbookapp.utils.CommonUtils
import com.goldbookapp.utils.CommonUtils.Companion.clickWithDebounce
import com.goldbookapp.utils.CommonUtils.Companion.isValidClickPressed
import com.goldbookapp.utils.Constants
import com.goldbookapp.utils.PreferenceHelper
import com.goldbookapp.utils.PreferenceHelper.get
import com.google.gson.Gson
import com.hamzasharuf.networkmonitor.ConnectivityStateHolder
import com.hamzasharuf.networkmonitor.Event
import com.hamzasharuf.networkmonitor.NetworkEvents
import kotlinx.android.synthetic.main.toolbar_layout.*

class AddSalesLineActivity : AppCompatActivity() {
    var checkedRowNo: String? = "1"
    var Selected_Transaction_Type: String? = ""
    var transaction_id: String? = ""
    lateinit var binding: AddSalesLineActivityBinding
    lateinit var prefs: SharedPreferences
    lateinit var loginModel: LoginModel
    var Transaction_Type: String? = ""
    var Transaction_ID: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.add_sales_line_activity)

        setupUIandListner()


    }

    private fun setupUIandListner() {
        imgLeft.setImageResource(R.drawable.ic_back)
        tvTitle.setText(R.string.add_payment_line)

        imgLeft?.clickWithDebounce {
            onBackPressed()
        }
        prefs = PreferenceHelper.defaultPrefs(this)
        loginModel = Gson().fromJson(
            prefs[Constants.PREF_LOGIN_DETAIL_KEY, ""],
            LoginModel::class.java
        ) //getter


        if (intent.extras != null && intent.extras!!.containsKey(Constants.TRANSACTION_ID)) {
            Transaction_ID = intent.getStringExtra(Constants.TRANSACTION_ID)
        }
        if (intent.extras != null && intent.extras!!.containsKey(Constants.TRANSACTION_TYPE)) {
            Transaction_Type = intent.getStringExtra(Constants.TRANSACTION_TYPE)
            when (Transaction_Type) {
                "sales" -> {
                    //GST
                    if (loginModel.data?.branch_info?.branch_type.equals("1", true)) {

                        binding.rbCashRecAddSaleLine.visibility = View.VISIBLE
                        binding.rbCashPayAddSaleLine.visibility = View.VISIBLE
                        binding.rbBankRecAddSaleLine.visibility = View.VISIBLE
                        binding.rbBankPayAddSaleLine.visibility = View.VISIBLE
                        binding.rbMetalRecAddSaleLine.visibility = View.GONE
                        binding.rbMetalPayAddSaleLine.visibility = View.GONE
                        binding.rbRateCutAddSaleLine.visibility = View.GONE
                        binding.rbAdjustAddSaleLine.visibility = View.GONE
                    }
                    //Non Gst
                    else {
                        binding.rbCashRecAddSaleLine.visibility = View.VISIBLE
                        binding.rbCashPayAddSaleLine.visibility = View.VISIBLE
                        binding.rbBankRecAddSaleLine.visibility = View.VISIBLE
                        binding.rbBankPayAddSaleLine.visibility = View.VISIBLE
                        binding.rbMetalRecAddSaleLine.visibility = View.VISIBLE
                        binding.rbMetalPayAddSaleLine.visibility = View.VISIBLE
                        binding.rbRateCutAddSaleLine.visibility = View.VISIBLE
                        binding.rbAdjustAddSaleLine.visibility = View.GONE
                    }
                }
                "receipt" -> {
                    //GST
                    if (loginModel.data?.branch_info?.branch_type.equals("1", true)) {

                        binding.rbCashRecAddSaleLine.visibility = View.VISIBLE
                        binding.rbCashPayAddSaleLine.visibility = View.GONE
                        binding.rbBankRecAddSaleLine.visibility = View.VISIBLE
                        binding.rbBankPayAddSaleLine.visibility = View.GONE
                        binding.rbMetalRecAddSaleLine.visibility = View.GONE
                        binding.rbMetalPayAddSaleLine.visibility = View.GONE
                        binding.rbRateCutAddSaleLine.visibility = View.GONE
                        binding.rbAdjustAddSaleLine.visibility = View.VISIBLE
                    }
                    //Non Gst
                    else {
                        binding.rbCashRecAddSaleLine.visibility = View.VISIBLE
                        binding.rbCashPayAddSaleLine.visibility = View.GONE
                        binding.rbBankRecAddSaleLine.visibility = View.VISIBLE
                        binding.rbBankPayAddSaleLine.visibility = View.GONE
                        binding.rbMetalRecAddSaleLine.visibility = View.GONE
                        binding.rbMetalPayAddSaleLine.visibility = View.GONE
                        binding.rbAdjustAddSaleLine.visibility = View.VISIBLE
                        binding.rbRateCutAddSaleLine.visibility = View.VISIBLE
                    }
                }
                "payment" -> {
                    //GST
                    if (loginModel.data?.branch_info?.branch_type.equals("1", true)) {

                        binding.rbCashRecAddSaleLine.visibility = View.GONE
                        binding.rbCashPayAddSaleLine.visibility = View.VISIBLE
                        binding.rbBankRecAddSaleLine.visibility = View.GONE
                        binding.rbBankPayAddSaleLine.visibility = View.VISIBLE
                        binding.rbMetalRecAddSaleLine.visibility = View.GONE
                        binding.rbMetalPayAddSaleLine.visibility = View.GONE
                        binding.rbRateCutAddSaleLine.visibility = View.GONE
                        binding.rbAdjustAddSaleLine.visibility = View.VISIBLE
                    }
                    //Non Gst
                    else {
                        binding.rbCashRecAddSaleLine.visibility = View.GONE
                        binding.rbCashPayAddSaleLine.visibility = View.VISIBLE
                        binding.rbBankRecAddSaleLine.visibility = View.GONE
                        binding.rbBankPayAddSaleLine.visibility = View.VISIBLE
                        binding.rbMetalRecAddSaleLine.visibility = View.GONE
                        binding.rbMetalPayAddSaleLine.visibility = View.GONE
                        binding.rbAdjustAddSaleLine.visibility = View.VISIBLE
                        binding.rbRateCutAddSaleLine.visibility = View.VISIBLE
                    }
                }
            }

        }



        binding.rgAddSaleLine.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { radioGroup, checkedId ->
            when (checkedId) {
                binding.rbCashRecAddSaleLine.id -> checkedRowNo = "1"
                binding.rbCashPayAddSaleLine.id -> checkedRowNo = "2"
                binding.rbBankRecAddSaleLine.id -> checkedRowNo = "3"
                binding.rbBankPayAddSaleLine.id -> checkedRowNo = "4"
                binding.rbMetalRecAddSaleLine.id -> checkedRowNo = "5"
                binding.rbMetalPayAddSaleLine.id -> checkedRowNo = "6"
                binding.rbRateCutAddSaleLine.id -> checkedRowNo = "7"
                binding.rbAdjustAddSaleLine.id -> checkedRowNo = "8"
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        CommonUtils.hideProgress()
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

        }

        if (!ConnectivityStateHolder.isConnected) {
            // Network is not available
            CommonUtils.showDialog(this, getString(R.string.please_check_internet_msg))

        }
    }

    fun nextClicked(view: View) {
        if (isValidClickPressed()) {
            when (checkedRowNo) {
                // Cash Receipt
                "1" -> startActivity(
                    Intent(
                        this,
                        AddCashBankActivity::class.java
                    ).putExtra(Constants.SaleLine_Row_No, checkedRowNo)

                )
                //Cash Payment
                "2" -> startActivity(
                    Intent(
                        this,
                        AddCashBankActivity::class.java
                    ).putExtra(Constants.SaleLine_Row_No, checkedRowNo)
                )
                //Bank Receipt
                "3" -> startActivity(
                    Intent(
                        this,
                        AddCashBankActivity::class.java
                    ).putExtra(Constants.SaleLine_Row_No, checkedRowNo)
                )
                //Bank Payment
                "4" -> startActivity(
                    Intent(
                        this,
                        AddCashBankActivity::class.java
                    ).putExtra(Constants.SaleLine_Row_No, checkedRowNo)
                )
                //Metal Receipt
                "5" -> startActivity(
                    Intent(
                        this,
                        AddCashBankActivity::class.java
                    ).putExtra(Constants.SaleLine_Row_No, checkedRowNo)
                        .putExtra(Constants.TRANSACTION_TYPE, Transaction_Type)
                        .putExtra(Constants.TRANSACTION_ID, Transaction_ID)
                )
                //Metal Payment
                "6" -> startActivity(
                    Intent(
                        this,
                        AddCashBankActivity::class.java
                    ).putExtra(Constants.SaleLine_Row_No, checkedRowNo)
                        .putExtra(Constants.TRANSACTION_TYPE, Transaction_Type)
                        .putExtra(Constants.TRANSACTION_ID, Transaction_ID)
                )
                //Rate-cut
                "7" -> startActivity(
                    Intent(
                        this,
                        AddCashBankActivity::class.java
                    ).putExtra(Constants.SaleLine_Row_No, checkedRowNo)
                )
                //adjustment
                "8" -> startActivity(
                    Intent(
                        this,
                        AddCashBankActivity::class.java
                    ).putExtra(Constants.SaleLine_Row_No, checkedRowNo)
                )
            }
            finish()
        }
    }
}
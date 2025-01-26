package com.goldbookapp.ui.activity.ledger

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.blankj.utilcode.util.NetworkUtils
import com.goldbookapp.R
import com.goldbookapp.api.ApiHelper
import com.goldbookapp.api.RetrofitBuilder
import com.goldbookapp.databinding.ActivityLedgerDetailsBinding
import com.goldbookapp.model.LedgerDetailsModel
import com.goldbookapp.model.LoginModel
import com.goldbookapp.model.SearchListLedgerModel
import com.goldbookapp.model.UserWiseRestrictionModel
import com.goldbookapp.ui.activity.viewmodel.LedgerDetailsViewModal
import com.goldbookapp.ui.activity.viewmodel.ViewModelFactory
import com.goldbookapp.utils.CommonUtils
import com.goldbookapp.utils.CommonUtils.Companion.clickWithDebounce
import com.goldbookapp.utils.CommonUtils.Companion.isValidClickPressed
import com.goldbookapp.utils.Constants
import com.goldbookapp.utils.PreferenceHelper
import com.goldbookapp.utils.PreferenceHelper.get
import com.goldbookapp.utils.Status
import com.google.gson.Gson
import com.hamzasharuf.networkmonitor.ConnectivityStateHolder
import com.hamzasharuf.networkmonitor.Event
import com.hamzasharuf.networkmonitor.NetworkEvents
import kotlinx.android.synthetic.main.activity_ledger_details.*
import kotlinx.android.synthetic.main.toolbar_layout.*

class LedgerDetailsActivity : AppCompatActivity() {
    var is_delete: Boolean = false
    var is_add_edit: Boolean = false
    lateinit var binding: ActivityLedgerDetailsBinding
    lateinit var loginModel: LoginModel
    var ledgerId: String? = ""
    lateinit var ledgerDetailsModel: LedgerDetailsModel
    private lateinit var viewModel: LedgerDetailsViewModal
    var is_system_ledger: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_ledger_details)
        setupViewModel()
        setupUIandListner()
    }

    private fun setupViewModel() {
        viewModel =
            ViewModelProvider(this, ViewModelFactory(ApiHelper(RetrofitBuilder.apiService))).get(
                LedgerDetailsViewModal::class.java
            )
        binding.setLifecycleOwner(this)

    }

    private fun setupUIandListner() {

        val prefs = PreferenceHelper.defaultPrefs(this)
        loginModel = Gson().fromJson(
            prefs[Constants.PREF_LOGIN_DETAIL_KEY, ""],
            LoginModel::class.java
        ) //getter

        imgLeft.setImageResource(R.drawable.ic_back)
        imgRight2.setImageResource(R.drawable.ic_edit)
        imgEnd.setImageResource(R.drawable.ic_edit)
        imgRight2.visibility = GONE
        imgRight.setImageResource(R.drawable.ic_delete_icon)
        imgRight.visibility = GONE
        tvTitle.setText("Ledger Details")


        imgRight2?.clickWithDebounce {
            if (this::ledgerDetailsModel.isInitialized) {
                startActivity(
                    Intent(this, EditLedgerActivity::class.java)
                        .putExtra(Constants.LEDGER_DETAIL_KEY, Gson().toJson(ledgerDetailsModel))
                )
                finish()
            }
        }

        imgRight?.clickWithDebounce {
            if (this::ledgerDetailsModel.isInitialized) {
                // delete category api call
                ensureDeleteDialog(ledgerId.toString())
            }
        }
        imgEnd?.clickWithDebounce {
            if (this::ledgerDetailsModel.isInitialized) {
                startActivity(
                    Intent(this, EditLedgerActivity::class.java)
                        .putExtra(
                            Constants.LEDGER_DETAIL_KEY,
                            Gson().toJson(ledgerDetailsModel)
                        )
                )
                finish()
            }
        }

        if (intent.extras?.containsKey(Constants.LEDGER_DETAIL_KEY)!!) {
            var group_str: String? = intent.getStringExtra(Constants.LEDGER_DETAIL_KEY)
            var ledgerDetailsModel: SearchListLedgerModel.DataLedger = Gson().fromJson(
                group_str,
                SearchListLedgerModel.DataLedger::class.java
            )

            ledgerId = ledgerDetailsModel.ledger_id.toString()

        }

        imgLeft?.clickWithDebounce {
            onBackPressed()
        }

    }

    private fun ensureDeleteDialog(ledger_id: String) {
        val builder = AlertDialog.Builder(this)
        val DeleteClick = { dialog: DialogInterface, which: Int ->
            //itemCategory delete api call
            deleteLedger(loginModel?.data?.bearer_access_token, ledger_id)
        }
        val dialogdismiss = { dialog: DialogInterface, which: Int ->
            dialog.dismiss()
        }
        with(builder)
        {
            setTitle(context.getString(R.string.delLedgerDialog2Title))
            setMessage(context.getString(R.string.ledgerDialog2Message))
            setPositiveButton(context.getString(R.string.Cancel), dialogdismiss)
            setNeutralButton(
                context.getString(R.string.Delete),
                DialogInterface.OnClickListener(function = DeleteClick)
            )
            show()
        }
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
            when (loginModel.data!!.user_info!!.user_type.equals("user", true)) {
                // user type user
                true -> {
                    // apply restriciton
                     defaultDisableAllButtonnUI()
                    userWiseRestriction(loginModel.data?.bearer_access_token)
                }
                else->{

                }

                // user_type -> admin or super_admin or any other

            }
            ledgerDetailAPI(loginModel?.data?.bearer_access_token, ledgerId)

        }

        if (!ConnectivityStateHolder.isConnected) {
            // Network is not available
            CommonUtils.showDialog(this, getString(R.string.please_check_internet_msg))

        }
    }
    private fun defaultDisableAllButtonnUI() {
        imgRight.visibility = View.GONE
        imgRight2.visibility = View.GONE
        imgEnd.visibility = View.GONE
    }

    private fun userWiseRestriction(token: String?) {
        if (NetworkUtils.isConnected()) {
            viewModel.userWiseRestriction(token)
                .observe(this, Observer {
                    it?.let { resource ->
                        when (resource.status) {

                            Status.SUCCESS -> {
                                if (it.data?.status == true) {
                                    applyUserWiseRestriction(it.data.data)
                                } else {
                                    when (it.data!!.code == Constants.ErrorCode) {
                                        true -> {
                                            Toast.makeText(
                                                this,
                                                it.data.errormessage.message,
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


    private fun applyUserWiseRestriction(data: UserWiseRestrictionModel.Data) {
        for (i in 0 until data.permission!!.size) {
            if (data.permission!!.get(i).startsWith(getString(R.string.ledgr))) {
                // Restriction check for Ledger
                when (data.permission!!.get(i).endsWith(getString(R.string.ledger_delete), true)) {
                    true -> {
                        if (is_system_ledger.equals("1")) {
                            imgRight.visibility = GONE
                        } else {
                            is_delete = true
                            if (is_add_edit) {
                                imgRight2.visibility = View.VISIBLE
                                imgEnd.visibility = View.GONE
                                imgRight.visibility = View.VISIBLE
                            } else {
                                //  Log.v("groupdelete", "true")
                                imgRight.visibility = View.VISIBLE
                                imgEnd.visibility = View.GONE
                                imgRight2.visibility = View.GONE
                            }
                        }
                    }
                    else->{

                    }
                }
                when (data.permission!!.get(i)
                    .endsWith(getString(R.string.add_edit), true)) {
                    true -> {
                        if (is_system_ledger.equals("1")) {
                            imgRight2.visibility = GONE
                            imgEnd.visibility = GONE
                        } else {
                            is_add_edit = true
                            if (is_delete) {
                                // Log.v("checkdelete", "true")
                                imgRight2.visibility = View.VISIBLE
                                imgEnd.visibility = View.GONE
                                imgRight.visibility = View.VISIBLE
                            } else {
                                //  Log.v("groupaddedit", "true")
                                imgRight2.visibility = View.GONE
                                imgEnd.visibility = View.VISIBLE
                                imgRight.visibility = View.GONE
                            }
                        }

                    }
                    else->{

                    }
                }
            }
        }
    }


    fun ledgerDetailAPI(
        token: String?,
        ledger_id: String?
    ) {

        if (NetworkUtils.isConnected()) {
            viewModel.ledgerDetail(token, ledger_id).observe(this, Observer {
                it?.let { resource ->
                    when (resource.status) {
                        Status.SUCCESS -> {
                            if (it.data?.status == true) {

                                ll_ledger_detail_root.visibility = VISIBLE
                                ledgerDetailsModel = it.data
                                is_system_ledger =
                                    ledgerDetailsModel.data.ledger.ledgerData.is_system_ledger

                                if (!loginModel.data!!.user_info!!.user_type.equals("user", true)) {
                                    if (is_system_ledger.equals("1")) {
                                        imgRight.visibility = GONE
                                        imgRight2.visibility = GONE
                                        imgEnd.visibility = View.GONE
                                    } else {
                                        imgRight.visibility = VISIBLE
                                        imgRight2.visibility = VISIBLE
                                        imgEnd.visibility = View.GONE
                                    }
                                }

                                //binding.tvDebitUserDetaLedger.setText(ledgerDetailsModel.data.ledger.ledgerClosingBalance.total_balance.toString() + "" + ledgerDetailsModel.data.ledger.ledgerClosingBalance.amount_default_term)

                                binding.tvLedgerCode.setText(ledgerDetailsModel.data.ledger.ledgerData.code)
                                binding.tvLedgerGroup.setText(ledgerDetailsModel.data.ledger.ledgerData.group_name)
                                if (ledgerDetailsModel.data.ledger.ledgerClosingBalance.total_balance.toString()
                                        .equals("0")
                                ) {
                                    binding.tvDebitUserDetaLedger.text =
                                        ledgerDetailsModel.data.ledger.ledgerClosingBalance.total_balance.toString()
                                    binding.tvDebitUserDetaLedger.setTextColor(
                                        ContextCompat.getColor(
                                            this,
                                            R.color.header_black_text
                                        )
                                    )
                                } else {
                                    binding.tvDebitUserDetaLedger.text =
                                        ledgerDetailsModel.data.ledger.ledgerClosingBalance.total_balance.toString() + " " + ledgerDetailsModel.data.ledger.ledgerClosingBalance.amount_default_short_term
                                    if (ledgerDetailsModel.data.ledger.ledgerClosingBalance.amount_default_term.equals(
                                            "Dr", ignoreCase = true
                                        )
                                    ) {
                                        tvDebitUserDetaLedger.setTextColor(
                                            ContextCompat.getColor(
                                                this,
                                                R.color.debit_color
                                            )
                                        )
                                    } else
                                        tvDebitUserDetaLedger.setTextColor(
                                            ContextCompat.getColor(this, R.color.credit_color)
                                        )
                                }

                                if (!ledgerDetailsModel.data.ledger.ledgerData.sub_group_name.equals(
                                        ""
                                    )
                                ) {
                                    ly_subGroup_ledgerdetail.visibility = VISIBLE
                                    binding.tvLedgerSubGroup.setText(ledgerDetailsModel.data.ledger.ledgerData.sub_group_name)
                                } else if (!ledgerDetailsModel.data.ledger.ledgerData.gstin.equals("")) {
                                    ly_gstin_ledgerdetail.visibility = VISIBLE
                                    binding.tvLedgerGST.setText(ledgerDetailsModel.data.ledger.ledgerData.gstin)
                                } else if (!ledgerDetailsModel.data.ledger.ledgerData.pan_card.equals(
                                        ""
                                    )
                                ) {
                                    ly_pan_ledgerdetail.visibility = VISIBLE
                                    binding.tvLedgerPan.setText(ledgerDetailsModel.data.ledger.ledgerData.pan_card)
                                } else if (!ledgerDetailsModel.data.ledger.ledgerData.notes.equals("")) {
                                    ly_note_ledgerdetail.visibility = VISIBLE
                                    binding.tvLedgerNote.setText(ledgerDetailsModel.data.ledger.ledgerData.notes)
                                } else {

                                    CommonUtils.hideProgress()
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


    private fun deleteLedger(token: String?, ledger_id: String?) {
        if (NetworkUtils.isConnected()) {
            if (isValidClickPressed()) {
                viewModel.deleteLedger(token, ledger_id).observe(this, Observer {
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
                                    onBackPressed()

                                } else {
                                    Toast.makeText(
                                        this,
                                        it.data?.errormessage?.message,
                                        Toast.LENGTH_LONG
                                    )
                                        .show()
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
                                Toast.makeText(
                                    this,
                                    it.data?.errormessage?.message,
                                    Toast.LENGTH_LONG
                                )
                                    .show()
                            }
                            Status.LOADING -> {
                                CommonUtils.showProgress(this)
                            }
                        }
                    }
                })
            }
        } else {
            CommonUtils.showDialog(this, getString(R.string.please_check_internet_msg))
        }

    }

}
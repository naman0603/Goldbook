package com.goldbookapp.ui.ui.send

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.blankj.utilcode.util.NetworkUtils
import com.goldbookapp.R
import com.goldbookapp.api.ApiHelper
import com.goldbookapp.api.RetrofitBuilder
import com.goldbookapp.databinding.FragmentReportsBinding
import com.goldbookapp.model.LoginModel
import com.goldbookapp.model.UserWiseRestrictionModel
import com.goldbookapp.ui.MainActivity
import com.goldbookapp.ui.activity.report.ReportTypesCommon
import com.goldbookapp.ui.activity.viewmodel.ViewModelFactory
import com.goldbookapp.ui.ui.gallery.ReportsViewModel
import com.goldbookapp.utils.CommonUtils
import com.goldbookapp.utils.CommonUtils.Companion.clickWithDebounce
import com.goldbookapp.utils.Constants
import com.goldbookapp.utils.PreferenceHelper
import com.goldbookapp.utils.PreferenceHelper.get
import com.goldbookapp.utils.Status
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_reports.view.*
import kotlinx.android.synthetic.main.toolbar_layout.view.*

class ReportsFragment : Fragment() {
    private lateinit var viewModel: ReportsViewModel
    lateinit var binding: FragmentReportsBinding
    private var reportsTrackNo: String = "1"

    lateinit var loginModel: LoginModel
    lateinit var prefs: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_reports, container, false)
        val view = binding.root

        setupViewModel()
        setupUIandListner(view)

        return view
    }

    private fun setupViewModel() {
        viewModel =
            ViewModelProvider(this, ViewModelFactory(ApiHelper(RetrofitBuilder.apiService))).get(
                ReportsViewModel::class.java
            )
        binding.setLifecycleOwner(this)

    }
    fun setupUIandListner(root: View) {
        prefs = PreferenceHelper.defaultPrefs(activity?.applicationContext!!)
        loginModel = Gson().fromJson(
            prefs[Constants.PREF_LOGIN_DETAIL_KEY, ""],
            LoginModel::class.java
        ) //getter

        //hides keyboard on focus change from editext(search) to tap anywhere in the screen (below toolbar).
        root.ll_root_fragreports.setOnFocusChangeListener { view, b ->
            CommonUtils.hideKeyboardnew(
                requireActivity()
            )
        }
        root.imgLeft.setImageResource(R.drawable.ic_back)
        root.tvTitle.setText(getString(R.string.reports))

        root.imgLeft.clickWithDebounce {
            (activity as MainActivity).onBackPressed()
        }
        root.frag_Reports_Contacts.clickWithDebounce {
            reportsTrackNo = "1"
            startActivity(
                Intent(
                    activity,
                    ReportTypesCommon::class.java
                ).putExtra(Constants.ReportsTrackNo, reportsTrackNo)
            )
        }
        root.frag_Reports_SummaryContacts.clickWithDebounce {
            reportsTrackNo = "10"
            startActivity(
                Intent(
                    activity,
                    ReportTypesCommon::class.java
                ).putExtra(Constants.ReportsTrackNo, reportsTrackNo)
            )
        }
        root.frag_Reports_Stock.clickWithDebounce {
            reportsTrackNo = "2"
            startActivity(
                Intent(
                    activity,
                    ReportTypesCommon::class.java
                ).putExtra(Constants.ReportsTrackNo, reportsTrackNo)
            )
        }

        root.frag_Reports_tagstock.clickWithDebounce {
            reportsTrackNo = "8"
            startActivity(
                Intent(
                    activity,
                    ReportTypesCommon::class.java
                ).putExtra(Constants.ReportsTrackNo, reportsTrackNo)
            )
        }

        root.frag_Reports_stockitem.clickWithDebounce {
            reportsTrackNo = "9"
            startActivity(
                Intent(
                    activity,
                    ReportTypesCommon::class.java
                ).putExtra(Constants.ReportsTrackNo, reportsTrackNo)
            )
        }

        root.frag_Reports_SalesRegister.clickWithDebounce {
            reportsTrackNo = "3"
            startActivity(
                Intent(
                    activity,
                    ReportTypesCommon::class.java
                ).putExtra(Constants.ReportsTrackNo, reportsTrackNo)
            )
        }
        root.frag_Reports_PurchaseRegister.clickWithDebounce {
            reportsTrackNo = "4"
            startActivity(
                Intent(
                    activity,
                    ReportTypesCommon::class.java
                ).putExtra(Constants.ReportsTrackNo, reportsTrackNo)
            )
        }
        root.frag_Reports_Daybook.clickWithDebounce {
            reportsTrackNo = "5"
            startActivity(
                Intent(
                    activity,
                    ReportTypesCommon::class.java
                ).putExtra(Constants.ReportsTrackNo, reportsTrackNo)
            )
        }
        root.frag_Reports_CashBankBook.clickWithDebounce {
            reportsTrackNo = "6"
            startActivity(
                Intent(
                    activity,
                    ReportTypesCommon::class.java
                ).putExtra(Constants.ReportsTrackNo, reportsTrackNo)
            )
        }
        root.frag_Reports_Ledger.clickWithDebounce {
            reportsTrackNo = "7"
            startActivity(
                Intent(
                    activity,
                    ReportTypesCommon::class.java
                ).putExtra(Constants.ReportsTrackNo, reportsTrackNo)
            )
        }


    }

    private fun defaultDisableAllButtonnUI() {
       binding.fragReportsContacts.visibility = View.GONE
       binding.fragReportsContactsBelowline.visibility = View.GONE
       binding.fragReportsStock.visibility = View.GONE

        binding.fragReportsSalesRegister.visibility = View.GONE
        binding.fragReportsSalesRegisterBelowline.visibility = View.GONE
        binding.fragReportsPurchaseRegister.visibility = View.GONE

        binding.fragReportsDaybook.visibility = View.GONE
        binding.fragReportsDaybookBelowline.visibility = View.GONE
        binding.fragReportsCashBankBook.visibility = View.GONE

        binding.fragReportsLedger.visibility = View.GONE

    }
    private fun defaultEnableAllButtonnUI() {
        binding.fragReportsContacts.visibility = View.VISIBLE
        binding.fragReportsContactsBelowline.visibility = View.VISIBLE
        binding.fragReportsStock.visibility = View.VISIBLE

        binding.fragReportsSalesRegister.visibility = View.VISIBLE
        binding.fragReportsSalesRegisterBelowline.visibility = View.VISIBLE
        binding.fragReportsPurchaseRegister.visibility = View.VISIBLE

        binding.fragReportsDaybook.visibility = View.VISIBLE
        binding.fragReportsDaybookBelowline.visibility = View.VISIBLE
        binding.fragReportsCashBankBook.visibility = View.VISIBLE

        binding.fragReportsLedger.visibility = View.VISIBLE

    }


    override fun onResume() {
        super.onResume()
        (activity as MainActivity).hideBottomSheet()

        when(loginModel.data!!.user_info!!.user_type.equals("user",true)){
            // user type user
            true -> {
                // apply restriciton
                defaultDisableAllButtonnUI()
                userWiseRestriction(loginModel.data?.bearer_access_token)
            }
            // user_type -> admin or super_admin or any other
            false -> {
                defaultEnableAllButtonnUI()
            }
        }


    }
    private fun userWiseRestriction(token: String?) {
        if (NetworkUtils.isConnected()) {
            viewModel.userWiseRestriction(token)
                .observe(requireActivity(), Observer {
                    it?.let { resource ->
                        when (resource.status) {

                            Status.SUCCESS -> {
                                if (it.data?.status == true) {
                                    applyUserWiseRestriction(it.data.data)
                                } else {
                                    when (it.data!!.code == Constants.ErrorCode) {
                                        true -> {
                                            Toast.makeText(
                                                context,
                                                it.data.errormessage?.message,
                                                Toast.LENGTH_LONG
                                            )
                                                .show()
                                        }
                                        false -> {
                                            context?.let { it1 -> CommonUtils.somethingWentWrong(it1)}
                                        }

                                    }
                                }
                                CommonUtils.hideProgress()
                            }
                            Status.ERROR -> {
                                CommonUtils.hideProgress()
                            }
                            Status.LOADING -> {
                                CommonUtils.showProgress(requireContext())
                            }
                        }
                    }
                })
        }
    }
    private fun applyUserWiseRestriction(data: UserWiseRestrictionModel.Data) {
        for (i in 0 until data.permission!!.size) {
            if (data.permission!!.get(i).contains(getString(R.string.reprt))) {
                if (data.permission!!.get(i).startsWith(getString(R.string.ledger_statement))) {
                    when (data.permission!!.get(i).endsWith(getString(R.string.report_view), true)) {
                        true -> {
                            binding.fragReportsContacts.visibility = View.VISIBLE
                            if(binding.fragReportsStock.visibility == View.VISIBLE){
                                binding.fragReportsContactsBelowline.visibility = View.VISIBLE
                            }
                        }else->{

                    }
                    }
                }
                if (data.permission!!.get(i).startsWith(getString(R.string.stock_statement))) {
                    when (data.permission!!.get(i).endsWith(getString(R.string.report_view), true)) {
                        true -> {
                            binding.fragReportsStock.visibility = View.VISIBLE
                            if(binding.fragReportsContacts.visibility == View.VISIBLE){
                                binding.fragReportsContactsBelowline.visibility = View.VISIBLE
                            }
                        }else->{

                    }
                    }
                }
                if (data.permission!!.get(i).startsWith(getString(R.string.sales_))) {
                    when (data.permission!!.get(i).endsWith(getString(R.string.report_view), true)) {
                        true -> {
                            binding.fragReportsSalesRegister.visibility = View.VISIBLE
                            if(binding.fragReportsPurchaseRegister.visibility == View.VISIBLE){
                                binding.fragReportsSalesRegisterBelowline.visibility = View.VISIBLE
                            }
                        }else->{

                    }
                    }
                }
                if (data.permission!!.get(i).startsWith(getString(R.string.purchase_))) {
                    when (data.permission!!.get(i).endsWith(getString(R.string.report_view), true)) {
                        true -> {
                            binding.fragReportsPurchaseRegister.visibility = View.VISIBLE
                            if(binding.fragReportsSalesRegister.visibility == View.VISIBLE){
                                binding.fragReportsSalesRegisterBelowline.visibility = View.VISIBLE
                            }
                        }else->{

                    }
                    }
                }
                if (data.permission!!.get(i).startsWith(getString(R.string.day_book_))) {
                    when (data.permission!!.get(i).endsWith(getString(R.string.report_view), true)) {
                        true -> {
                            binding.fragReportsDaybook.visibility = View.VISIBLE
                            if(binding.fragReportsCashBankBook.visibility == View.VISIBLE){
                                binding.fragReportsDaybookBelowline.visibility = View.VISIBLE
                            }
                        }else->{

                    }
                    }
                }
                if (data.permission!!.get(i).startsWith(getString(R.string.cash_book_))) {
                    when (data.permission!!.get(i).endsWith(getString(R.string.report_view), true)) {
                        true -> {
                            binding.fragReportsCashBankBook.visibility = View.VISIBLE
                            if(binding.fragReportsDaybook.visibility == View.VISIBLE){
                                binding.fragReportsDaybookBelowline.visibility = View.VISIBLE
                            }
                        }else->{

                    }
                    }
                }
                if (data.permission!!.get(i).contains(getString(R.string.ledgr_report))) {
                    when (data.permission!!.get(i).endsWith(getString(R.string.report_view), true)) {
                        true -> {
                            binding.fragReportsLedger.visibility = View.VISIBLE
                        }else->{

                    }
                    }
                }


            }

        }
    }

}






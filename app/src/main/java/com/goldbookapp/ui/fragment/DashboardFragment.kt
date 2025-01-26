package com.goldbookapp.ui.ui.home

import UserLimitAccessModel
import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.InputFilter
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.util.Log
import android.view.*
import android.widget.ArrayAdapter
import android.widget.CompoundButton
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.NetworkUtils
import com.goldbookapp.MyApplication
import com.goldbookapp.R
import com.goldbookapp.api.ApiHelper
import com.goldbookapp.api.RetrofitBuilder
import com.goldbookapp.databinding.FragmentDashboardBinding
import com.goldbookapp.inapplock.managers.LockManager
import com.goldbookapp.model.*
import com.goldbookapp.ui.MainActivity
import com.goldbookapp.ui.activity.customer.NewCustomerActivity
import com.goldbookapp.ui.activity.item.NewItemActivity
import com.goldbookapp.ui.activity.payment.NewPaymentActivity
import com.goldbookapp.ui.activity.purchase.NewPurchaseActivity
import com.goldbookapp.ui.activity.receipt.NewReceiptActivity
import com.goldbookapp.ui.activity.sales.NewInvoiceActivity
import com.goldbookapp.ui.activity.settings.AppLockActivityNew
import com.goldbookapp.ui.activity.supplier.NewSupplierActivity
import com.goldbookapp.ui.activity.auth.LoginActivity
import com.goldbookapp.ui.activity.viewmodel.ViewModelFactory
import com.goldbookapp.ui.adapter.RecentTransactionAdapter
import com.goldbookapp.utils.*
import com.goldbookapp.utils.CommonUtils.Companion.clickWithDebounce
import com.goldbookapp.utils.CommonUtils.Companion.isValidClickPressed
import com.goldbookapp.utils.PreferenceHelper.get
import com.goldbookapp.utils.PreferenceHelper.set
import com.google.android.material.textfield.TextInputEditText
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.leinardi.android.speeddial.SpeedDialActionItem
import com.leinardi.android.speeddial.SpeedDialView
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.dash_branch_switch_dialog.*
import kotlinx.android.synthetic.main.dash_fiscal_year_dialog.*
import kotlinx.android.synthetic.main.dash_todays_goldrate_dialog.*
import kotlinx.android.synthetic.main.fragment_dashboard.*
import kotlinx.android.synthetic.main.fragment_dashboard.view.*
import kotlinx.android.synthetic.main.toolbar_layout.view.*
import java.text.DecimalFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


class DashboardFragment : Fragment(), SpeedDialView.OnActionSelectedListener {

    private lateinit var swithchBranchDialog: Dialog
    lateinit var dialog: Dialog
    lateinit var dialog1: Dialog
    lateinit var adapter: RecentTransactionAdapter
    lateinit var permission: UserWiseRestrictionModel.Data
    lateinit var permissionList: ArrayList<String>

    private lateinit var dashboardViewModel: DashboardViewModel
    lateinit var cashrateUpdatedValue: String
    lateinit var billrateUpdatedValue: String
    var changeGoldRate: Boolean = false
    var isSaleSpeedDialAdded: Boolean = false
    var isPurchaseSpeedDialAdded: Boolean = false
    var isPaymeentSpeedDialAdded: Boolean = false
    var isReceiptSpeedDialAdded: Boolean = false
    var speedDialAddedCount: Int = 0
    lateinit var paymentaction: SpeedDialActionItem
    lateinit var receiptaction: SpeedDialActionItem
    lateinit var purchaseaction: SpeedDialActionItem
    lateinit var saleaction: SpeedDialActionItem
    lateinit var customeraction: SpeedDialActionItem
    lateinit var supplieraction: SpeedDialActionItem
    lateinit var itemaction: SpeedDialActionItem

    private lateinit var lockManager: LockManager<AppLockActivityNew>

    lateinit var onoffReceiver: BroadcastReceiver
    lateinit var receiverManager: ReceiverManager

    lateinit var prefs: SharedPreferences
    lateinit var filter: IntentFilter

    private lateinit var selectedCompanyBranchesModel: SelectedCompanyBranchesModel
    lateinit var todaysGoldRateDialogValue: String
    lateinit var loginModel: LoginModel
    lateinit var fiscalYearModel: FiscalYearModel


    lateinit var binding: FragmentDashboardBinding
    lateinit var branchNameAdapter: ArrayAdapter<String>
    var branchInfoList: List<SelectedCompanyBranchesModel.Data.Branch>? = null
    var branchNameList: List<String>? = null

    var otherBranchesInfoList: List<SelectedCompanyBranchesModel.Data.Branch>? = null
    var otherBranchNameList: List<String>? = null

    private lateinit var dashboardGoldrate: String
    var selectedBranchID: String? = null
    var selectedGoldrateToShow: String = "0"
    var isFromDashboardClick: Boolean = false
    lateinit var popupMenu: PopupMenu
    val c = Calendar.getInstance()
    private var storeparse = Date()
    var thisFiscalSD: String? = null
    var thisFiscalED: String? = null
    var thisPreviousFiscalSD: String? = null
    var thisPreviousFiscalED: String? = null
    var currentYear = 0
    var prevYear = 0
    var nextYear = 0
    var start_date: String? = null
    var end_date: String? = null
    var isFiscalYearFromPref: Boolean = false
    var selectFiscalYearFromMenu: String? = "0"



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_dashboard, container, false)
        val view = binding.root
        setupViewModel()
        receiverManager = ReceiverManager.init(activity?.applicationContext!!)
        initView(view)
        return view
    }

    private fun registerScreenOnoff_Boadcast() {
        if (prefs.contains(Constants.PASSWORD_PREFERENCE_KEY)) {
            onoffReceiver = ScreenOnOffReceiver.getInstance(activity?.applicationContext!!)
            receiverManager = ReceiverManager.init(activity?.applicationContext!!)

            lockManager = LockManager.getInstance() as LockManager<AppLockActivityNew>
            if (loginModel.data!!.company_info!!.id.equals(Constants.DemoCompanyId)) {

                try {

                    lockManager.disableAppLock()
                    (requireActivity().application as MyApplication).unregisterReceiver(
                        ScreenOnOffReceiver.getInstance(activity?.applicationContext!!)
                    )

                } catch (e: Exception) {

                }
            } else {

                filter = IntentFilter(Intent.ACTION_SCREEN_ON)
                filter.addAction(Intent.ACTION_SCREEN_OFF)

                lockManager.enableAppLock(requireActivity(), AppLockActivityNew::class.java)
                lockManager.getAppLock().logoId = R.mipmap.ic_launcher
                AppLockActivityNew.isRunning = false

                (requireActivity().application as MyApplication).registerReceiver(
                    ScreenOnOffReceiver.getInstance(activity?.applicationContext!!),
                    filter
                )

            }
        }
    }


    override fun onPause() {
        super.onPause()

        if (this::swithchBranchDialog.isInitialized)
            swithchBranchDialog.dismiss()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (this::dialog.isInitialized) {
            dialog.dismiss()
        }

        onoffReceiver = ScreenOnOffReceiver.getInstance(activity?.applicationContext!!)

        if (this::dialog.isInitialized)
            dialog.dismiss()
        if (this::swithchBranchDialog.isInitialized)
            swithchBranchDialog.dismiss()
    }

    override fun onResume() {
        super.onResume()
        receiverManager = ReceiverManager.init(activity?.applicationContext!!)
        if (this::receiverManager.isInitialized) {
            registerScreenOnoff_Boadcast()

            }

        (activity as MainActivity).showBottomSheet()
        (activity as MainActivity).defaultdashboardselected()

        //Setting the branchname
        binding.txtDashBranchName.setText(loginModel?.data?.branch_info?.branch_name)

        var firstPart: String = "Today's Gold Rate: "
        //var secondPart: String = "+91-9876543210. "
        var secondPart: String = "$dashboardGoldrate"
        var finalString: String = firstPart + secondPart
        var spannableStringBuilder: SpannableStringBuilder = SpannableStringBuilder(finalString)
        val clickablegoldrate = object : ClickableSpan() {
            override fun onClick(widget: View) {
                // nothing to do
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color =
                    ContextCompat.getColor(activity?.applicationContext!!, android.R.color.black)
                ds.typeface = ResourcesCompat.getFont(
                    activity?.applicationContext!!,
                    R.font.proxima_nova_bold
                )
                ds.isUnderlineText = false
            }

        }
        spannableStringBuilder.setSpan(
            clickablegoldrate,
            19,
            19 + dashboardGoldrate.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        tvLeftDash_TodaysRate.setText(spannableStringBuilder/*"Today's Gold Rate: $dashboardGoldrate"*/)
        //CommonUtils.goldRateDialogShow = CommonUtils.goldRateDialogShow + 1
        when (loginModel.data!!.user_info!!.user_type.equals("user", true)) {
            // user type user
            true -> {
                // apply restriciton
                defaultDisableAllButtonnUI()
                userWiseRestriction(loginModel.data?.bearer_access_token)
            }
            // user_type -> admin or super_admin or any other
            false -> {
                defaultEnableAllButtonnUI()
                //savePermissionModelForAdminUsers()
                checkGoldRateValueZero()
                loadDashboardDetails(
                    loginModel.data?.bearer_access_token,
                    true
                )
//                userLimitAccess(loginModel?.data?.bearer_access_token)
                getUpdateSessionDetails(loginModel?.data?.bearer_access_token)

            }
        }


        // userwise restric api call (for applying user restriction)


    }


    private fun savePermissionModelForAdminUsers() {
        permissionList = arrayListOf(
            "Sales|list",
            "Sales|add_edit",
            "Purchase|list",
            "Purchase|add_edit",
            "Payment|list",
            "Payment|add_edit",
            "Receipt|list",
            "Receipt|add_edit"
        )
        permission = UserWiseRestrictionModel.Data(permissionList, arrayListOf())
        permission.permission = arrayListOf()
        permission.permission = permissionList
        permission.fields = arrayListOf()
    }

    private fun defaultDisableAllButtonnUI() {
        changeGoldRate = false

    }

    private fun defaultEnableAllButtonnUI() {
        changeGoldRate = true

    }

    private fun userWiseRestriction(token: String?) {
        if (NetworkUtils.isConnected()) {
            dashboardViewModel.userWiseRestriction(token)
                .observe(requireActivity(), Observer {
                    it?.let { resource ->
                        when (resource.status) {

                            Status.SUCCESS -> {
                                if (it.data?.status == true) {
                                    (activity as MainActivity).updateDrawerList(it.data.data)
                                    applyUserWiseRestriction(it.data.data)
                                    permission = UserWiseRestrictionModel.Data(
                                        it.data.data.permission,
                                        it.data.data.fields
                                    )
                                    restricUserPermissionWise(it.data.data)
                                    setupRecentTransAdapter()
                                    loadDashboardDetails(loginModel.data?.bearer_access_token, true)
                                    checkGoldRateValueZero()
                                } else {
                                    CommonUtils.hideProgress()
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
                                            context?.let { it1 -> CommonUtils.somethingWentWrong(it1) }
                                        }

                                    }
                                }
                                // CommonUtils.hideProgress()
                            }
                            Status.ERROR -> {
                                CommonUtils.hideProgress()
                            }
                            Status.LOADING -> {
                                CommonUtils.showProgress(requireActivity())
                            }
                        }
                    }
                })
        }
    }

    private fun applyUserWiseRestriction(data: UserWiseRestrictionModel.Data) {
        permission = data
        for (i in 0 until data.permission!!.size) {
            if (data.permission!!.get(i).startsWith(getString(R.string.change_gold_rate))) {
                // Restriction check for Customer
                when (data.permission!!.get(i).endsWith(getString(R.string.sav), true)) {
                    true -> {
                        changeGoldRate = true
                    }
                    else -> {

                    }
                }
            }

        }
    }


    private fun setupViewModel() {
        dashboardViewModel =
            ViewModelProvider(this, ViewModelFactory(ApiHelper(RetrofitBuilder.apiService))).get(
                DashboardViewModel::class.java
            )
        binding.setLifecycleOwner(this)

    }

    fun initView(root: View) {

        root.imgLeft.setImageResource(R.drawable.ic_menu_black_24dp)
        root.imgRight.setImageResource(R.drawable.ic_branchswitch)
        prefs = PreferenceHelper.defaultPrefs(activity?.applicationContext!!)
        loginModel = Gson().fromJson(
            prefs[Constants.PREF_LOGIN_DETAIL_KEY, ""],
            LoginModel::class.java
        ) //getter


        getLoginModelFromPrefs();
        root.tvTitle.setText(getString(R.string.dashboard))


        root.imgLeft.clickWithDebounce {
            (activity as MainActivity).openCloseDrawer()
        }

        root.imgRight.clickWithDebounce {
            if (this::swithchBranchDialog.isInitialized) {

                getLoginModelFromPrefs()
                getSelectedCompanyBranches(loginModel?.data?.bearer_access_token)
            }
        }

        root.dashcardCust.clickWithDebounce {
            (activity as MainActivity).loadSelectedFragment(1)
        }

        root.dashcardSupp.clickWithDebounce {
            (activity as MainActivity).loadSelectedFragment(2)
        }

        root.dashcardItem.clickWithDebounce {
            (activity as MainActivity).loadSelectedFragment(3)
        }

        root.dashcardSales.clickWithDebounce {
            (activity as MainActivity).loadSelectedFragment(4)
        }

        setupGoldrateDialog()
        setupFiscalYearDialog()

        if (prefs.contains(Constants.FiscalYear)) {
            isFiscalYearFromPref = true
            val collectionType =
                object :
                    TypeToken<FiscalYearModel>() {}.type
            fiscalYearModel =
                Gson().fromJson(
                    prefs[Constants.FiscalYear, ""],
                    collectionType
                )
            binding.tvLeftDashFiscalYear.setText(
                "F.Y.: " + fiscalYearModel.start_date +
                        " to " + fiscalYearModel.end_date
            )
            start_date = fiscalYearModel.start_date
            end_date = fiscalYearModel.end_date
            selectFiscalYearFromMenu = fiscalYearModel.select_from
        }
        getFiscalYear()

        when (loginModel.data!!.user_info!!.user_type.equals("user", true)) {
            // user type user
            // user_type -> admin or super_admin or any other
            false -> {
                savePermissionModelForAdminUsers()
                setupRecentTransAdapter()
            }
            else -> {

            }
        }

        root.linear_TodayRate.clickWithDebounce {
            isFromDashboardClick = true
            if (changeGoldRate) {
                if (this::dialog.isInitialized) {
                    dialog.tvDashSave.visibility = View.VISIBLE
                }

            } else {
                if (this::dialog.isInitialized) {
                    dialog.tvDashSave.visibility = View.GONE
                }

            }
            openGoldRateDialog(isFromDashboardClick)
        }

        root.linear_FiscalYear.clickWithDebounce {
            openFiscalYearDialog()

        }

        if (loginModel.data?.company_info?.gold_rate?.type.equals("1", true)) {
            dashboardGoldrate =
                loginModel.data?.company_info?.gold_rate?.bill_rate_amount.toString()
        } else
            dashboardGoldrate =
                loginModel.data?.company_info?.gold_rate?.cash_rate_amount.toString()
        /*if (dashboardGoldrate.equals("0.00")) {
            isFromDashboardClick = false

            if(changeGoldRate) {
                if (this::dialog.isInitialized) {
                    dialog.tvDashSave.visibility = View.VISIBLE
                }

            }
            else{
                if (this::dialog.isInitialized) {
                    dialog.tvDashSave.visibility = View.GONE
                }

            }
            openGoldRateDialog(isFromDashboardClick)
        }*/

        swithchBranchDialog = Dialog(requireContext(), R.style.Full_Dialog)
        swithchBranchDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        requireActivity().window.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        );

        swithchBranchDialog.setContentView(R.layout.dash_branch_switch_dialog)
        swithchBranchDialog.setCancelable(true)
        swithchBranchDialog.setCanceledOnTouchOutside(true)

    }

    private fun getFiscalYear() {
        currentYear = Calendar.getInstance()[Calendar.YEAR]
        prevYear = currentYear - 1
        nextYear = currentYear + 1
        // val month = Calendar.getInstance()[Calendar.MONTH] + 1

        val month_date = SimpleDateFormat("MMM")
        val month = month_date.format(Calendar.getInstance().time)
        Log.v("month", "" + month)
        val date = Calendar.getInstance()[Calendar.DATE]

        val cuerrent_date: String = currentYear.toString() + "-" + month + "-" + date
        // val cuerrent_date: String = currentYear.toString() + "-" + "Apr" + "-" + "01"
        val match_date: String = currentYear.toString() + "-" + "Mar" + "-" + "31"
        compareDates(cuerrent_date, match_date)

    }

    private fun openFiscalYearDialog() {

        if (isFiscalYearFromPref) {
            when (selectFiscalYearFromMenu) {
                "1" -> {
                    dialog1.txtFiscalYear.setText("This Fiscal Year")
                    dialog1.ly_custom_range.visibility = View.GONE
                    /* start_date = thisFiscalSD
                     end_date = thisFiscalED*/
                }
                "2" -> {
                    dialog1.txtFiscalYear.setText("Previous Fiscal Year")
                    dialog1.ly_custom_range.visibility = View.GONE
                }
                "3" -> {
                    dialog1.txtFiscalYear.setText("Custom Range")
                    dialog1.ly_custom_range.visibility = View.VISIBLE
                    dialog1.txtFromDate.setText(start_date)
                    dialog1.txtToDate.setText(end_date)
                }
                else -> {


                }
            }
        } else {
            dialog1.txtFiscalYear.setText("This Fiscal Year")
            start_date = thisFiscalSD
            end_date = thisFiscalED
            selectFiscalYearFromMenu = "1"
        }

        dialog1.tvFiscalCancel.clickWithDebounce {
            dialog1.dismiss()
        }
        dialog1.txtFiscalYear.clickWithDebounce {
            openFicalYearPopup()
        }

        // Log.v("default",""+getFirstDateOfMonth(Date())+thisFiscalSD)


        dialog1.txtFromDate.clickWithDebounce {
            openDatePicker(true)
        }

        dialog1.txtToDate.clickWithDebounce {
            openDatePicker(false)
        }

        dialog1.tvFiscalSave.clickWithDebounce {
            val childModel = FiscalYearModel(start_date!!, end_date!!, selectFiscalYearFromMenu)
            prefs[Constants.FiscalYear] = Gson().toJson(childModel)
            dialog1.dismiss()
            binding.tvLeftDashFiscalYear.setText("F.Y.: " + start_date + " to " + end_date)
        }



        dialog1.show()
    }

    fun getFirstDateOfMonth(date: Date?): Date? {
        val cal = Calendar.getInstance()
        cal.time = date
        cal[Calendar.DAY_OF_MONTH] = cal.getActualMinimum(Calendar.DAY_OF_MONTH)
        return cal.time
    }

    fun getLastDateOfMonth(date: Date?): Date? {
        val cal = Calendar.getInstance()
        cal.time = date
        cal[Calendar.DAY_OF_MONTH] = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        return cal.time
    }


    fun openDatePicker(isFromDate: Boolean) {
//        val c = Calendar.getInstance()
        val sdf = SimpleDateFormat("dd-MMM-yyyy")
        var parse = Date()
        when (isFromDate) {
            // from date(for all reporttypes)
            true -> {

                parse = sdf.parse(dialog1.txtFromDate.text.toString())


                //start_date = SimpleDateFormat("dd-MMM-yyyy").format(dialog1.txtFromDate.text.toString())
            }
            // To date(for all reporttypes)
            false -> {

                parse = sdf.parse(dialog1.txtToDate.text.toString())

            }
        }
        c.setTime(parse)


        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)


        val dpd = DatePickerDialog(
            requireContext(),
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->

                c.set(Calendar.YEAR, year)
                c.set(Calendar.MONTH, monthOfYear)
                c.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                // Display Selected date in textbox
                when (isFromDate) {
                    // from date(for all reporttypes)
                    true -> {


                        dialog1.txtFromDate.setText(
                            "" + String.format(
                                "%02d",
                                dayOfMonth
                            ) + "-" + SimpleDateFormat("MMM").format(c.time) + "-" + year.toString()
                                .substring(0, 4)
                        )
                        parse = sdf.parse(dialog1.txtFromDate.text.toString())
                        start_date = SimpleDateFormat("dd-MMM-yyyy").format(parse)
                        Log.v("date", "" + start_date + parse)
                        if (parse.after(sdf.parse(dialog1.txtFromDate.text.toString()))) {
                            dialog1.txtToDate.setText(dialog1.txtFromDate.text)
                            end_date =
                                SimpleDateFormat("dd-MMM-yyyy").format(dialog1.txtToDate.text.toString())
                        }


                    }
                    // To date(for all reporttypes)
                    false -> {

                        dialog1.txtToDate.setText(
                            "" + String.format(
                                "%02d",
                                dayOfMonth
                            ) + "-" + SimpleDateFormat("MMM").format(c.time) + "-" + year.toString()
                                .substring(0, 4)
                        )
                        parse = sdf.parse(dialog1.txtToDate.text.toString())
                        end_date = SimpleDateFormat("dd-MMM-yyyy").format(parse)

                    }
                }
            },

            year,
            month,
            day
        )


        //dpd.datePicker.minDate = Date().time
        dpd.show()

    }


    fun openFicalYearPopup() {
        popupMenu = PopupMenu(requireContext(), dialog1.txtFiscalYear)
        popupMenu.menu.add("This Fiscal Year")
        popupMenu.menu.add("Previous Fiscal Year")
        popupMenu.menu.add("Custom Range")



        popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
            (dialog1.txtFiscalYear as TextInputEditText).setText(item.title)
            when (item.title) {
                "This Fiscal Year" -> {
                    dialog1.ly_custom_range.visibility = View.GONE
                    start_date = thisFiscalSD
                    end_date = thisFiscalED
                    selectFiscalYearFromMenu = "1"
                }
                "Previous Fiscal Year" -> {
                    dialog1.ly_custom_range.visibility = View.GONE
                    start_date = thisPreviousFiscalSD
                    end_date = thisPreviousFiscalED
                    selectFiscalYearFromMenu = "2"
                }
                "Custom Range" -> {
                    dialog1.ly_custom_range.visibility = View.VISIBLE
                    dialog1.txtFromDate.setText(thisFiscalSD)
                    dialog1.txtToDate.setText(thisFiscalED)
                    start_date = dialog1.txtFromDate.text.toString()
                    end_date = dialog1.txtToDate.text.toString()
                    selectFiscalYearFromMenu = "3"
                }

            }
            true
        })

        popupMenu.show()
    }

    private fun setupFiscalYearDialog() {
        dialog1 = Dialog(requireContext(), R.style.Full_Dialog)
        dialog1.requestWindowFeature(Window.FEATURE_NO_TITLE)
        requireActivity().window.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        );

        dialog1.setCancelable(false)
        dialog1.setCanceledOnTouchOutside(false)
        dialog1.setContentView(R.layout.dash_fiscal_year_dialog)
    }


    private fun setupRecentTransAdapter() {
        binding.dashRvRecentTransactions.layoutManager = LinearLayoutManager(activity)
        adapter = RecentTransactionAdapter(arrayListOf(), permission, this)
        binding.dashRvRecentTransactions.adapter = adapter
    }

    private fun setupGoldrateDialog() {
        dialog = Dialog(requireContext(), R.style.Full_Dialog)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        requireActivity().window.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        );

        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setContentView(R.layout.dash_todays_goldrate_dialog)
    }

    private fun getLoginModelFromPrefs() {
        prefs = PreferenceHelper.defaultPrefs(activity?.applicationContext!!)
        loginModel = Gson().fromJson(
            prefs[Constants.PREF_LOGIN_DETAIL_KEY, ""],
            LoginModel::class.java
        ) //getter
    }

    private fun setupSwitchBranchDialog() {

        // setting current branch
        swithchBranchDialog.tvbranchSwitchCurrentBranch.setText(loginModel.data?.branch_info?.branch_name)
        when (branchInfoList?.size!! > 1) {
            true -> swithchBranchDialog.tvbranchSwichBranchName.isEnabled = true
            false -> swithchBranchDialog.tvbranchSwichBranchName.isEnabled = false

        }

        swithchBranchDialog.txtbranchSwitchName.setOnItemClickListener { adapterView, view, position, i
            ->
            val selected: String = adapterView.getItemAtPosition(position).toString()
            val pos: Int? = otherBranchNameList?.indexOf(selected)

            selectedBranchID = pos?.let { it1 -> otherBranchesInfoList?.get(it1)?.id }
            val selectedBranch: String =
                pos?.let { it1 -> otherBranchesInfoList?.get(it1)?.branch_name }!!


        }
        swithchBranchDialog.tvbranchSwitchSave.clickWithDebounce {

            if (swithchBranchDialog.txtbranchSwitchName.text.length > 0) {
                switchBranchAPI(loginModel.data?.bearer_access_token, selectedBranchID)
                swithchBranchDialog.tvbranchSwitchCurrentBranch.text =
                    swithchBranchDialog.txtbranchSwitchName.text
                swithchBranchDialog.txtbranchSwitchName.setText("")
                swithchBranchDialog.dismiss()
            } else {
                Toast.makeText(context, "Please select branch to switch", Toast.LENGTH_SHORT).show()
            }

        }
        swithchBranchDialog.tvbranchSwitchCancel.clickWithDebounce {

            swithchBranchDialog.txtbranchSwitchName.setText("")
            swithchBranchDialog.dismiss()
        }



        branchNameAdapter = ArrayAdapter(
            activity?.applicationContext!!,
            android.R.layout.simple_list_item_1,
            this.otherBranchNameList!!
        )
        swithchBranchDialog.txtbranchSwitchName.setAdapter(branchNameAdapter)

    }


    private fun getSelectedCompanyBranches(token: String?) {
        if (NetworkUtils.isConnected()) {
            dashboardViewModel.getSelectedCompanyBranches(token, loginModel?.data?.company_info?.id)
                .observe(requireActivity(), Observer {
                    it?.let { resource ->
                        when (resource.status) {
                            Status.SUCCESS -> {
                                if (it.data?.status == true) {
                                    branchInfoList = it.data.data?.branch
                                    branchNameList = branchInfoList?.map { it.branch_name }

                                    otherBranchesInfoList = it.data.data?.other_branches
                                    otherBranchNameList =
                                        otherBranchesInfoList?.map { it.branch_name }

                                    setupSwitchBranchDialog()
                                    swithchBranchDialog.show()


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
                                            context?.let { it1 ->
                                                CommonUtils.somethingWentWrong(
                                                    it1
                                                )
                                            }
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

    private fun loadDashboardDetails(token: String?, isCallFromUserRestriction: Boolean) {
        if (NetworkUtils.isConnected()) {
            dashboardViewModel.dashboardDetails(token)
                .observe(requireActivity(), Observer {
                    it?.let { resource ->
                        when (resource.status) {
                            Status.SUCCESS -> {

                                if (it.data?.status == true) {
                                    fillDashboardDetails(it.data)
                                } else {
                                    CommonUtils.hideProgress()
                                }
                                //CommonUtils.hideProgress()
                            }
                            Status.ERROR -> {
                                CommonUtils.hideProgress()
                            }
                            Status.LOADING -> {
                                CommonUtils.showProgress(requireActivity())
                                /*if(!isCallFromUserRestriction){
                                    if (!Constants.isDashboardLoadedOnce) {
                                        CommonUtils.showProgress(requireActivity())
                                    }
                                }*/
                            }
                        }
                    }
                })
        }
    }

    private fun fillDashboardDetails(data: DashboardDetailsModel) {

        prefs[Constants.PREF_DASHBOARD_DETAIL_KEY] =
            Gson().toJson(data)

        Constants.isDashboardLoadedOnce = false

        when (data.data!!.to_collect!!.fine_balance) {
            "0.000" -> {
                dashToCollectVal1.text = data.data!!.to_collect!!.fine_balance + " (F)"
                dashToCollectVal1.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.header_black_text
                    )
                )
            }

            else -> {
                dashToCollectVal1.text =
                    data.data!!.to_collect!!.fine_balance + " " + data.data!!.to_collect!!.fine_default_term + " (F)"

                when (data.data!!.to_collect!!.fine_default_term) {
                    "Dr" -> {
                        dashToCollectVal1.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.debit_color
                            )
                        )
                    }
                    "U" -> {
                        dashToCollectVal1.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.debit_color
                            )
                        )
                    }
                    "Rec" -> {
                        dashToCollectVal1.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.debit_color
                            )
                        )
                    }
                    "L" -> {
                        dashToCollectVal1.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.debit_color
                            )
                        )
                    }
                    else -> {
                        dashToCollectVal1.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.credit_color
                            )
                        )
                    }
                }
            }
        }
        when (data.data!!.to_collect!!.silver_fine_balance) {
            "0.000" -> {
                dashToCollectVal2.text = data.data!!.to_collect!!.silver_fine_balance + " (S)"
                dashToCollectVal2.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.header_black_text
                    )
                )
            }

            else -> {
                dashToCollectVal2.text =
                    data.data!!.to_collect!!.silver_fine_balance + " " + data.data!!.to_collect!!.silver_fine_default_term + " (S)"

                when (data.data!!.to_collect!!.silver_fine_default_term) {
                    "Dr" -> {
                        dashToCollectVal2.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.debit_color
                            )
                        )
                    }
                    "U" -> {
                        dashToCollectVal2.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.debit_color
                            )
                        )
                    }
                    "Rec" -> {
                        dashToCollectVal2.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.debit_color
                            )
                        )
                    }
                    "L" -> {
                        dashToCollectVal2.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.debit_color
                            )
                        )
                    }
                    else -> {
                        dashToCollectVal2.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.credit_color
                            )
                        )
                    }
                }
            }
        }
        when (data.data!!.to_collect!!.cash_balance) {
            "0.00" -> {
                dashToCollectVal3.text = data.data!!.to_collect!!.cash_balance + " (C)"
                dashToCollectVal3.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.header_black_text
                    )
                )
            }
            else -> {
                dashToCollectVal3.text =
                    data.data!!.to_collect!!.cash_balance + " " + data.data!!.to_collect!!.cash_default_term + " (C)"

                when (data.data!!.to_collect!!.cash_default_term) {
                    "Dr" -> {
                        dashToCollectVal3.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.debit_color
                            )
                        )
                    }
                    "U" -> {
                        dashToCollectVal3.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.debit_color
                            )
                        )
                    }
                    "Rec" -> {
                        dashToCollectVal3.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.debit_color
                            )
                        )
                    }
                    "L" -> {
                        dashToCollectVal3.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.debit_color
                            )
                        )
                    }
                    else -> {
                        dashToCollectVal3.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.credit_color
                            )
                        )
                    }
                }
            }
        }
        // To Pay
        when (data.data!!.to_pay!!.fine_balance) {
            "0.000" -> {
                dashToPayVal1.text = data.data!!.to_pay!!.fine_balance + " (F)"
                dashToPayVal1.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.header_black_text
                    )
                )
            }

            else -> {
                dashToPayVal1.text =
                    data.data!!.to_pay!!.fine_balance + " " + data.data!!.to_pay!!.fine_default_term + " (F)"
                when (data.data!!.to_pay!!.fine_default_term) {
                    "Dr" -> {
                        dashToPayVal1.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.debit_color
                            )
                        )
                    }
                    "U" -> {
                        dashToPayVal1.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.debit_color
                            )
                        )
                    }
                    "Rec" -> {
                        dashToPayVal1.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.debit_color
                            )
                        )
                    }
                    "L" -> {
                        dashToPayVal1.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.debit_color
                            )
                        )
                    }
                    else -> {
                        dashToPayVal1.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.credit_color
                            )
                        )
                    }
                }

            }
        }
        when (data.data!!.to_pay!!.silver_fine_balance) {
            "0.000" -> {
                dashToPayVal2.text = data.data!!.to_pay!!.silver_fine_balance + " (S)"
                dashToPayVal2.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.header_black_text
                    )
                )
            }

            else -> {
                dashToPayVal2.text =
                    data.data!!.to_pay!!.silver_fine_balance + " " + data.data!!.to_pay!!.silver_fine_default_term + " (S)"
                when (data.data!!.to_pay!!.silver_fine_default_term) {
                    "Dr" -> {
                        dashToPayVal1.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.debit_color
                            )
                        )
                    }
                    "U" -> {
                        dashToPayVal1.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.debit_color
                            )
                        )
                    }
                    "Rec" -> {
                        dashToPayVal1.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.debit_color
                            )
                        )
                    }
                    "L" -> {
                        dashToPayVal1.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.debit_color
                            )
                        )
                    }
                    else -> {
                        dashToPayVal1.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.credit_color
                            )
                        )
                    }
                }

            }
        }
        when (data.data!!.to_pay!!.cash_balance) {
            "0.00" -> {
                dashToPayVal3.text = data.data!!.to_pay!!.cash_balance + " (C)"
                dashToPayVal3.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.header_black_text
                    )
                )
            }
            else -> {
                dashToPayVal3.text =
                    data.data!!.to_pay!!.cash_balance + " " + data.data!!.to_pay!!.cash_default_term + " (C)"
                when (data.data!!.to_pay!!.cash_default_term) {
                    "Dr" -> {
                        dashToPayVal3.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.debit_color
                            )
                        )
                    }
                    "U" -> {
                        dashToPayVal3.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.debit_color
                            )
                        )
                    }
                    "Rec" -> {
                        dashToPayVal3.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.debit_color
                            )
                        )
                    }
                    "L " -> {
                        dashToPayVal3.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.debit_color
                            )
                        )
                    }
                    else -> {
                        dashToPayVal3.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.credit_color
                            )
                        )
                    }
                }
            }
        }

        dashStockVal1.text = "${data.data.stock_value!!.item_count} items"
        dashStockVal2.text = "${data.data.stock_value.fine_wt}${Constants.WEIGHT_GM_APPEND} (F)"

        dashSalesVal1.text = "${data.data.monthly_sale!!.total_net}${Constants.WEIGHT_GM_APPEND}"
        dashSalesVal2.text = "${Constants.AMOUNT_RS_APPEND}${data.data.monthly_sale.total_amount}"

        when (data.data.recent_transactions!!.get(0).module.isNullOrBlank()) {
            true -> {
                dash_rv_RecentTransactions.visibility = View.GONE
                tvNoRecorddashRecentTrans.visibility = View.VISIBLE
//                enableBtnsHideProgress()
            }
            false -> {
                dash_rv_RecentTransactions.visibility = View.VISIBLE
                tvNoRecorddashRecentTrans.visibility = View.GONE
                adapter.apply {
                    addRecentTransList(data.data.recent_transactions, permission)
                    notifyDataSetChanged()
                }
            }
        }
        CommonUtils.hideProgress()
        nsDashBoard.visibility = View.VISIBLE
    }

    private fun userLimitAccess(token: String?) {
        if (NetworkUtils.isConnected()) {
            dashboardViewModel.userLimitAccess(token)
                .observe(requireActivity(), Observer {
                    it?.let { resource ->
                        when (resource.status) {
                            Status.SUCCESS -> {

                                if (it.data?.status == true) {
                                    restrictUser(it.data.data)

                                    /*when(loginModel.data!!.user_info!!.user_type.equals("user",true)){
                                        // user type user
                                        true -> {
                                            // apply restriciton
                                            defaultDisableAllButtonnUI()
                                            userWiseRestriction(loginModel.data?.bearer_access_token)
                                        }
                                        // user_type -> admin or super_admin or any other
                                        false -> {
                                            defaultEnableAllButtonnUI()
                                            savePermissionModelForAdminUsers()
                                            checkGoldRateValueZero()
                                            loadDashboardDetails(
                                                loginModel.data?.bearer_access_token,
                                                true
                                            )
                                        }
                                    }*/


                                } else {
                                }
                                //CommonUtils.hideProgress()
                            }
                            Status.ERROR -> {
                                CommonUtils.hideProgress()
                            }
                            Status.LOADING -> {
                                CommonUtils.showProgress(requireActivity())
                            }
                        }
                    }
                })
        }
    }

    private fun checkGoldRateValueZero() {
        if (dashboardGoldrate.equals("0.00")) {
            isFromDashboardClick = false

            if (changeGoldRate) {
                if (this::dialog.isInitialized) {
                    dialog.tvDashSave.visibility = View.VISIBLE
                }

            } else {
                if (this::dialog.isInitialized) {
                    dialog.tvDashSave.visibility = View.GONE
                }

            }
            openGoldRateDialog(isFromDashboardClick)
        }
    }

    private fun restricUserPermissionWise(data: UserWiseRestrictionModel.Data) {
        if (speedDialAddedCount > 0) {
            if (isPaymeentSpeedDialAdded) speedDial.removeActionItem(paymentaction)
            if (isReceiptSpeedDialAdded) speedDial.removeActionItem(receiptaction)
            if (isPurchaseSpeedDialAdded) speedDial.removeActionItem(purchaseaction)
            if (isSaleSpeedDialAdded) speedDial.removeActionItem(saleaction)
            speedDial.removeActionItem(customeraction)
            speedDial.removeActionItem(supplieraction)
            speedDial.removeActionItem(itemaction)
        }

        isPaymeentSpeedDialAdded = false
        isReceiptSpeedDialAdded = false
        isPurchaseSpeedDialAdded = false
        isSaleSpeedDialAdded = false
        speedDialAddedCount = 0

        for (i in 0 until data.permission!!.size) {
            if (data.permission!!.get(i).startsWith(getString(R.string.sales))) {
                // Restriction check for Customer
                when (data.permission!!.get(i).endsWith(getString(R.string.add_edit), true)) {
                    true -> {
                        saleaction = SpeedDialActionItem.Builder(
                            R.id.fab_sale_action, R.drawable
                                .ic_speeddial_sales
                        )
                            .setFabBackgroundColor(Color.WHITE)
                            .setLabelColor(Color.WHITE)
                            .setLabel(getString(R.string.sale))
                            .setLabelBackgroundColor(
                                ContextCompat.getColor(
                                    activity?.applicationContext!!,
                                    R.color.fab_label_bg
                                )
                            )
                            .create()
                        speedDial.addActionItem(saleaction)
                        isSaleSpeedDialAdded = true
                        speedDialAddedCount = speedDialAddedCount + 1
                    }
                    else -> {

                    }
                }
            }
            if (data.permission!!.get(i).startsWith(getString(R.string.purchase))) {
                // Restriction check for Customer
                when (data.permission!!.get(i).endsWith(getString(R.string.add_edit), true)) {
                    true -> {
                        purchaseaction = SpeedDialActionItem.Builder(
                            R.id.fab_purchase_action, R.drawable
                                .ic_speeddial_purchase
                        )
                            .setFabBackgroundColor(Color.WHITE)
                            .setLabelColor(Color.WHITE)
                            .setLabel(getString(R.string.purchase))
                            .setLabelBackgroundColor(
                                ContextCompat.getColor(
                                    activity?.applicationContext!!,
                                    R.color.fab_label_bg
                                )
                            )
                            .create()
                        speedDial.addActionItem(purchaseaction)
                        isPurchaseSpeedDialAdded = true
                        speedDialAddedCount = speedDialAddedCount + 1
                    }
                    else -> {

                    }
                }
            }

            if (data.permission!!.get(i).startsWith(getString(R.string.payment))) {
                // Restriction check for Customer
                when (data.permission!!.get(i).endsWith(getString(R.string.add_edit), true)) {
                    true -> {
                        paymentaction = SpeedDialActionItem.Builder(
                            R.id.fab_payment_action, R.drawable
                                .ic_speeddial_payment
                        )
                            .setFabBackgroundColor(Color.WHITE)
                            .setLabelColor(Color.WHITE)
                            .setLabel(getString(R.string.payment))
                            .setLabelBackgroundColor(
                                ContextCompat.getColor(
                                    activity?.applicationContext!!,
                                    R.color.fab_label_bg
                                )
                            )
                            .create()
                        speedDial.addActionItem(paymentaction)
                        isPaymeentSpeedDialAdded = true
                        speedDialAddedCount = speedDialAddedCount + 1
                    }
                    else -> {

                    }
                }
            }

            if (data.permission!!.get(i).startsWith(getString(R.string.receipt))) {
                // Restriction check for Customer
                when (data.permission!!.get(i).endsWith(getString(R.string.add_edit), true)) {
                    true -> {
                        receiptaction = SpeedDialActionItem.Builder(
                            R.id.fab_receipt_action, R.drawable
                                .ic_speeddial_receipt
                        )
                            .setFabBackgroundColor(Color.WHITE)
                            .setLabelColor(Color.WHITE)
                            .setLabel(getString(R.string.receipt))
                            .setLabelBackgroundColor(
                                ContextCompat.getColor(
                                    activity?.applicationContext!!,
                                    R.color.fab_label_bg
                                )
                            )
                            .create()
                        speedDial.addActionItem(receiptaction)
                        isReceiptSpeedDialAdded = true
                        speedDialAddedCount = speedDialAddedCount + 1
                    }
                    else -> {

                    }
                }
            }
            if (data.permission!!.get(i).startsWith(getString(R.string.items))) {
                // Restriction check for Customer
                when (data.permission!!.get(i).endsWith(getString(R.string.add_edit), true)) {
                    true -> {
                        itemaction = SpeedDialActionItem.Builder(
                            R.id.fab_item_action, R.drawable
                                .ic_speeddial_items
                        )
                            .setFabBackgroundColor(Color.WHITE)
                            .setLabelColor(Color.WHITE)
                            .setLabel(getString(R.string.item))
                            .setLabelBackgroundColor(
                                ContextCompat.getColor(
                                    activity?.applicationContext!!,
                                    R.color.fab_label_bg
                                )
                            )
                            .create()
                        speedDial.addActionItem(itemaction)
                        speedDialAddedCount = speedDialAddedCount + 1

                    }
                    else -> {

                    }
                }
            }
            if (data.permission!!.get(i).startsWith(getString(R.string.customers))) {
                // Restriction check for Customer
                when (data.permission!!.get(i).endsWith(getString(R.string.add_edit), true)) {
                    true -> {
                        customeraction = SpeedDialActionItem.Builder(
                            R.id.fab_customer_action, R.drawable
                                .ic_speeddial_customer
                        )
                            .setFabBackgroundColor(Color.WHITE)
                            .setLabelColor(Color.WHITE)
                            .setLabel(getString(R.string.customer))
                            .setLabelBackgroundColor(
                                ContextCompat.getColor(
                                    activity?.applicationContext!!,
                                    R.color.fab_label_bg
                                )
                            )
                            .create()
                        speedDial.addActionItem(customeraction)
                        speedDialAddedCount = speedDialAddedCount + 1
                    }
                    else -> {

                    }
                }
            }
            if (data.permission!!.get(i).startsWith(getString(R.string.supp))) {
                // Restriction check for Customer
                when (data.permission!!.get(i).endsWith(getString(R.string.add_edit), true)) {
                    true -> {
                        supplieraction = SpeedDialActionItem.Builder(
                            R.id.fab_supplier_action, R.drawable
                                .ic_speeddial_supplier
                        )
                            .setFabBackgroundColor(Color.WHITE)
                            .setLabelColor(Color.WHITE)
                            .setLabel(getString(R.string.supp))
                            .setLabelBackgroundColor(
                                ContextCompat.getColor(
                                    activity?.applicationContext!!,
                                    R.color.fab_label_bg
                                )
                            )
                            .create()
                        speedDial.addActionItem(supplieraction)
                        speedDialAddedCount = speedDialAddedCount + 1
                    }
                    else -> {

                    }
                }
            }

        }

        if (speedDialAddedCount > 0) {
            speedDial.visibility = View.VISIBLE
        } else {
            speedDial.visibility = View.GONE
        }

        speedDial.setOnActionSelectedListener(this)
    }

    private fun restrictUser(data: UserLimitAccessModel.Data?) {
        if (speedDialAddedCount > 0) {
            if (isPaymeentSpeedDialAdded) speedDial.removeActionItem(paymentaction)
            if (isReceiptSpeedDialAdded) speedDial.removeActionItem(receiptaction)
            if (isPurchaseSpeedDialAdded) speedDial.removeActionItem(purchaseaction)
            if (isSaleSpeedDialAdded) speedDial.removeActionItem(saleaction)
            speedDial.removeActionItem(customeraction)
            speedDial.removeActionItem(supplieraction)
            speedDial.removeActionItem(itemaction)
        }

        isPaymeentSpeedDialAdded = false
        isReceiptSpeedDialAdded = false
        isPurchaseSpeedDialAdded = false
        isSaleSpeedDialAdded = false
        speedDialAddedCount = 0

        when (data!!.can_add_payment.equals("1")) {
            true -> {
                paymentaction = SpeedDialActionItem.Builder(
                    R.id.fab_payment_action, R.drawable
                        .ic_speeddial_payment
                )
                    .setFabBackgroundColor(Color.WHITE)
                    .setLabelColor(Color.WHITE)
                    .setLabel(getString(R.string.payment))
                    .setLabelBackgroundColor(
                        ContextCompat.getColor(
                            activity?.applicationContext!!,
                            R.color.fab_label_bg
                        )
                    )
                    .create()
                speedDial.addActionItem(paymentaction)
                isPaymeentSpeedDialAdded = true
                speedDialAddedCount = speedDialAddedCount + 1
            }
            else -> {

            }

        }
        when (data.can_add_receipt.equals("1")) {
            true -> {
                receiptaction = SpeedDialActionItem.Builder(
                    R.id.fab_receipt_action, R.drawable
                        .ic_speeddial_receipt
                )
                    .setFabBackgroundColor(Color.WHITE)
                    .setLabelColor(Color.WHITE)
                    .setLabel(getString(R.string.receipt))
                    .setLabelBackgroundColor(
                        ContextCompat.getColor(
                            activity?.applicationContext!!,
                            R.color.fab_label_bg
                        )
                    )
                    .create()
                speedDial.addActionItem(receiptaction)
                isReceiptSpeedDialAdded = true
                speedDialAddedCount = speedDialAddedCount + 1
            }
            else -> {

            }
        }
        when (data.can_add_purchase.equals("1")) {
            true -> {
                purchaseaction = SpeedDialActionItem.Builder(
                    R.id.fab_purchase_action, R.drawable
                        .ic_speeddial_purchase
                )
                    .setFabBackgroundColor(Color.WHITE)
                    .setLabelColor(Color.WHITE)
                    .setLabel(getString(R.string.purchase))
                    .setLabelBackgroundColor(
                        ContextCompat.getColor(
                            activity?.applicationContext!!,
                            R.color.fab_label_bg
                        )
                    )
                    .create()
                speedDial.addActionItem(purchaseaction)
                isPurchaseSpeedDialAdded = true
                speedDialAddedCount = speedDialAddedCount + 1
            }
            else -> {

            }
        }
        when (data.can_add_sales.equals("1")) {
            true -> {
                saleaction = SpeedDialActionItem.Builder(
                    R.id.fab_sale_action, R.drawable
                        .ic_speeddial_sales
                )
                    .setFabBackgroundColor(Color.WHITE)
                    .setLabelColor(Color.WHITE)
                    .setLabel(getString(R.string.sale))
                    .setLabelBackgroundColor(
                        ContextCompat.getColor(
                            activity?.applicationContext!!,
                            R.color.fab_label_bg
                        )
                    )
                    .create()
                speedDial.addActionItem(saleaction)
                isSaleSpeedDialAdded = true
                speedDialAddedCount = speedDialAddedCount + 1
            }
            else -> {

            }
        }
        itemaction = SpeedDialActionItem.Builder(
            R.id.fab_item_action, R.drawable
                .ic_speeddial_items
        )
            .setFabBackgroundColor(Color.WHITE)
            .setLabelColor(Color.WHITE)
            .setLabel(getString(R.string.item))
            .setLabelBackgroundColor(
                ContextCompat.getColor(
                    activity?.applicationContext!!,
                    R.color.fab_label_bg
                )
            )
            .create()
        speedDial.addActionItem(itemaction)
        speedDialAddedCount = speedDialAddedCount + 1

        supplieraction = SpeedDialActionItem.Builder(
            R.id.fab_supplier_action, R.drawable
                .ic_speeddial_supplier
        )
            .setFabBackgroundColor(Color.WHITE)
            .setLabelColor(Color.WHITE)
            .setLabel(getString(R.string.supp))
            .setLabelBackgroundColor(
                ContextCompat.getColor(
                    activity?.applicationContext!!,
                    R.color.fab_label_bg
                )
            )
            .create()
        speedDial.addActionItem(supplieraction)
        speedDialAddedCount = speedDialAddedCount + 1

        customeraction = SpeedDialActionItem.Builder(
            R.id.fab_customer_action, R.drawable
                .ic_speeddial_customer
        )
            .setFabBackgroundColor(Color.WHITE)
            .setLabelColor(Color.WHITE)
            .setLabel(getString(R.string.customer))
            .setLabelBackgroundColor(
                ContextCompat.getColor(
                    activity?.applicationContext!!,
                    R.color.fab_label_bg
                )
            )
            .create()
        speedDial.addActionItem(customeraction)
        speedDialAddedCount = speedDialAddedCount + 1

        if (speedDialAddedCount > 0) {
            speedDial.visibility = View.VISIBLE
        } else {
            speedDial.visibility = View.GONE
        }

        speedDial.setOnActionSelectedListener(this)
    }

    private fun logout() {
        if (NetworkUtils.isConnected()) {
            dashboardViewModel.logout(loginModel?.data?.bearer_access_token)
                .observe(this, Observer {
                    it?.let { resource ->
                        when (resource.status) {
                            Status.SUCCESS -> {

                                if (it.data?.status == true) {
                                    Toast.makeText(
                                        context,
                                        it.data?.message,
                                        Toast.LENGTH_LONG
                                    )
                                        .show()

                                    val prefs = context?.let { it1 ->
                                        PreferenceHelper.defaultPrefs(
                                            it1
                                        )
                                    }
                                    prefs?.let { it1 -> CommonUtils.clearAllAppPrefs(it1) }
                                    startActivity(
                                        Intent(context, LoginActivity::class.java).setFlags(
                                            Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_NEW_TASK
                                        )
                                    )
                                } else {

                                }
                                CommonUtils.hideProgress()

                            }
                            Status.ERROR -> {

                                CommonUtils.hideProgress()
                            }
                            Status.LOADING -> {

                                this!!.context?.let { it1 -> CommonUtils.showProgress(it1) }
                            }
                        }
                    }
                })
        }
    }

    private fun switchBranchAPI(token: String?, selectedBranchID: String?) {
        if (NetworkUtils.isConnected()) {
            dashboardViewModel.branchSwitch(token, selectedBranchID)
                .observe(requireActivity(), Observer {
                    it?.let { resource ->
                        when (resource.status) {
                            Status.SUCCESS -> {
                                if (it.data?.status == true) {

                                    //updating loginmodel data
                                    loginModel.data?.branch_info = it.data?.data?.branch_info
                                    loginModel.data?.user_info = it.data?.data?.user_info
                                    loginModel.data?.company_info = it.data?.data?.company_info

                                    //updating login_detail pref
                                    val prefs =
                                        PreferenceHelper.defaultPrefs(activity?.applicationContext!!)
                                    prefs.edit().remove(Constants.PREF_LOGIN_DETAIL_KEY).apply()
                                    prefs[Constants.PREF_LOGIN_DETAIL_KEY] =
                                        Gson().toJson(loginModel) //setter
                                    Toast.makeText(
                                        activity?.applicationContext!!,
                                        it.data.message,
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    loadDashboardDetails(loginModel.data?.bearer_access_token,true)
                                    //own fragment refresh to load data according to switched branch
                                    val ft: FragmentTransaction =
                                        parentFragmentManager.beginTransaction()
                                    if (Build.VERSION.SDK_INT >= 26) {
                                        ft.setReorderingAllowed(false)
                                    }
                                    ft.detach(this).attach(this).commitAllowingStateLoss()


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
                                            context?.let { it1 -> CommonUtils.somethingWentWrong(it1) }
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

    override fun onActionSelected(actionItem: SpeedDialActionItem?): Boolean {
        when (actionItem?.id) {
            R.id.fab_payment_action -> { /*(activity as MainActivity).navController?.navigate(R.id.nav_payment)*/
                startActivity(Intent(activity, NewPaymentActivity::class.java))
            }
            R.id.fab_receipt_action -> { /*(activity as MainActivity).navController?.navigate(R.id.nav_receipt)*/
                (activity as MainActivity).bottom_navigation.setCurrentItem(3, false)
                startActivity(Intent(activity, NewReceiptActivity::class.java))
            }
            R.id.fab_purchase_action -> {
                startActivity(Intent(activity, NewPurchaseActivity::class.java))
            }
            R.id.fab_sale_action -> {
                startActivity(Intent(activity, NewInvoiceActivity::class.java))
                (activity as MainActivity).bottom_navigation.setCurrentItem(2, false)
            }
            R.id.fab_customer_action -> {
                startActivity(Intent(activity, NewCustomerActivity::class.java))
            }
            R.id.fab_supplier_action -> {
                startActivity(Intent(activity, NewSupplierActivity::class.java))
            }
            R.id.fab_item_action -> {
                startActivity(Intent(activity, NewItemActivity::class.java))
            }
        }
        return true // To keep the Speed Dial open
    }


    private fun openGoldRateDialog(fromDashboardClick: Boolean) {
        val df1 = DecimalFormat("0.00")



        if (fromDashboardClick) dialog.tvDashCancel.visibility = View.VISIBLE
        else dialog.tvDashCancel.visibility = View.GONE
        dialog.tvDashCancel.clickWithDebounce {
            dialog.dismiss()
        }
        cashrateUpdatedValue = loginModel.data?.company_info?.gold_rate?.cash_rate_amount.toString()
        dialog.txtDashCashRate.setText(cashrateUpdatedValue)
        dialog.txtDashCashRate.setSelection(cashrateUpdatedValue.length)
        billrateUpdatedValue = loginModel.data?.company_info?.gold_rate?.bill_rate_amount.toString()
        dialog.txtDashBillRate.setText(billrateUpdatedValue)
        dialog.txtDashBillRate.setSelection(billrateUpdatedValue.length)
        if (loginModel.data?.company_info?.gold_rate?.type.equals("1")) {
            dialog.radioDashBillRate.isChecked = true
            dialog.txtDashBillRate.requestFocus()
        } else {
            dialog.radioDashCashRate.isChecked = true
            dialog.txtDashCashRate.requestFocus()

        }
        dialog.radioDashBillRate.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                dialog.radioDashCashRate.isChecked = false
                dialog.txtDashBillRate.requestFocus()
            }
        })
        dialog.radioDashCashRate.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                dialog.radioDashBillRate.isChecked = false
                dialog.txtDashCashRate.requestFocus()
            }
        })
        dialog.txtDashCashRate.setFilters(
            arrayOf<InputFilter>(
                DigitsInputFilter(
                    10,
                    2,
                    9999999999.99
                )
            )
        )
        dialog.txtDashBillRate.setFilters(
            arrayOf<InputFilter>(
                DigitsInputFilter(
                    10,
                    2,
                    9999999999.99
                )
            )
        )
        dialog.txtDashCashRate.doAfterTextChanged {
            val str: String = dialog.txtDashCashRate.text.toString()

            if (str.isEmpty()) return@doAfterTextChanged
            val str2: String = CommonUtils.perfectDecimal(str, 10, 2).toString()
            if (!str2.equals(str)) {
                //val str3:String = df.format(str2.toDouble())
                dialog.txtDashCashRate.setText(str2)
                dialog.txtDashCashRate.setSelection(str2.length)
            }

            cashrateUpdatedValue = df1.format(str2.toDouble())
        }
        dialog.txtDashBillRate.doAfterTextChanged {
            val str: String = dialog.txtDashBillRate.text.toString()

            if (str.isEmpty()) return@doAfterTextChanged
            val str2: String = CommonUtils.perfectDecimal(str, 10, 2).toString()
            if (!str2.equals(str)) {
                //val str3:String = df.format(str2.toDouble())
                dialog.txtDashBillRate.setText(str2)
                dialog.txtDashBillRate.setSelection(str2.length)
            }
            billrateUpdatedValue = df1.format(str2.toDouble())
        }
        dialog.txtDashCashRate.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                if (this::cashrateUpdatedValue.isInitialized) {
                    when (dialog.txtDashCashRate.text.isNullOrBlank()) {
                        true -> {
                            cashrateUpdatedValue = "0.00"
                            dialog.txtDashCashRate.setText(cashrateUpdatedValue)
                            dialog.txtDashCashRate.setSelection(cashrateUpdatedValue.length)
                        }
                        else -> {
                            dialog.txtDashCashRate.setText(cashrateUpdatedValue)
                            dialog.txtDashCashRate.setSelection(cashrateUpdatedValue.length)
                        }

                    }

                }
            }

        }
        dialog.txtDashBillRate.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                if (this::billrateUpdatedValue.isInitialized) {
                    when (dialog.txtDashBillRate.text.isNullOrBlank()) {
                        true -> {
                            billrateUpdatedValue = "0.00"
                            dialog.txtDashBillRate.setText(billrateUpdatedValue)
                            dialog.txtDashBillRate.setSelection(billrateUpdatedValue.length)
                        }
                        else -> {
                            dialog.txtDashBillRate.setText(billrateUpdatedValue)
                            dialog.txtDashBillRate.setSelection(billrateUpdatedValue.length)
                        }
                    }
                }
            }

        }


        dialog.tvDashSave.clickWithDebounce {
            dialog.txtDashCashRate.clearFocus()
            dialog.txtDashBillRate.clearFocus()
            when (CommonUtils.checkForValidPositiveNegativeDecimal(
                requireContext(),
                dialog.txtDashCashRate.text.toString(),
                "Cash Rate"
            )) {
                true -> when (CommonUtils.checkForValidPositiveNegativeDecimal(
                    requireContext(),
                    dialog.txtDashBillRate.text.toString(),
                    "Bill Rate"
                )) {
                    true -> if (dialog.txtDashCashRate.text.toString().trim()
                            .isBlank() && dialog.radioDashCashRate.isChecked && !dialog.radioDashBillRate.isChecked
                    ) {
                        Toast.makeText(
                            context,
                            getString(R.string.enter_cash_rate_msg),
                            Toast.LENGTH_LONG
                        ).show()
                        dialog.txtDashCashRate.requestFocus()
                    } else if (dialog.txtDashBillRate.text.toString().trim()
                            .isBlank() && !dialog.radioDashCashRate.isChecked && dialog.radioDashBillRate.isChecked
                    ) {
                        Toast.makeText(
                            context,
                            getString(R.string.enter_bill_rate_msg),
                            Toast.LENGTH_LONG
                        ).show()
                        dialog.txtDashBillRate.requestFocus()
                    } else if (!dialog.txtDashCashRate.text.toString().trim()
                            .isBlank() && dialog.txtDashBillRate.text.toString().trim()
                            .isBlank() && dialog.radioDashCashRate.isChecked && !dialog.radioDashBillRate.isChecked
                    ) {
                        if (dialog.txtDashCashRate.text.toString().trim().toDouble() <= 0) {
                            Toast.makeText(
                                context,
                                getString(R.string.enter_valid_cash_rate_msg),
                                Toast.LENGTH_LONG
                            ).show()
                            dialog.txtDashCashRate.requestFocus()

                        } else {
                            selectedGoldrateToShow = "0"
                            updateGoldrateAPI(
                                loginModel?.data?.bearer_access_token,
                                loginModel?.data?.company_info?.id,
                                "0.00",
                                dialog.txtDashCashRate.text.toString().trim(),
                                selectedGoldrateToShow
                            )
                            dialog.dismiss()
                        }


                    } else if (dialog.txtDashCashRate.text.toString().trim()
                            .isBlank() && !dialog.txtDashBillRate.text.toString().trim()
                            .isBlank() && dialog.radioDashBillRate.isChecked
                    ) {
                        if (dialog.txtDashBillRate.text.toString().trim().toDouble() <= 0) {
                            Toast.makeText(
                                context,
                                getString(R.string.enter_valid_billrate_msg),
                                Toast.LENGTH_LONG
                            ).show()
                            dialog.txtDashBillRate.requestFocus()

                        } else {
                            selectedGoldrateToShow = "1"
                            updateGoldrateAPI(
                                loginModel?.data?.bearer_access_token,
                                loginModel?.data?.company_info?.id,
                                dialog.txtDashBillRate.text.toString().trim(),
                                "0.00",
                                selectedGoldrateToShow
                            )
                            dialog.dismiss()
                        }


                    } else if (!dialog.txtDashCashRate.text.toString().trim()
                            .isBlank() && !dialog.txtDashBillRate.text.toString().trim()
                            .isBlank() && !dialog.radioDashCashRate.isChecked && dialog.radioDashBillRate.isChecked
                    ) {
                        if (dialog.txtDashBillRate.text.toString().trim().toDouble() <= 0) {
                            Toast.makeText(
                                context,
                                getString(R.string.enter_valid_billrate_msg),
                                Toast.LENGTH_LONG
                            ).show()
                            dialog.txtDashBillRate.requestFocus()

                        } else {
                            selectedGoldrateToShow = "1"
                            updateGoldrateAPI(
                                loginModel?.data?.bearer_access_token,
                                loginModel?.data?.company_info?.id,
                                dialog.txtDashBillRate.text.toString().trim(),
                                dialog.txtDashCashRate.text.toString().trim(),
                                selectedGoldrateToShow
                            )
                            dialog.dismiss()
                        }

                    } else if (!dialog.txtDashCashRate.text.toString().trim()
                            .isBlank() && !dialog.txtDashBillRate.text.toString().trim()
                            .isBlank() && dialog.radioDashCashRate.isChecked && !dialog.radioDashBillRate.isChecked
                    )
                        if (dialog.txtDashCashRate.text.toString().trim().toDouble() <= 0) {
                            Toast.makeText(
                                context,
                                getString(R.string.enter_valid_cash_rate_msg),
                                Toast.LENGTH_LONG
                            ).show()
                            dialog.txtDashCashRate.requestFocus()

                        } else {
                            selectedGoldrateToShow = "0"
                            updateGoldrateAPI(
                                loginModel?.data?.bearer_access_token,
                                loginModel?.data?.company_info?.id,
                                dialog.txtDashBillRate.text.toString().trim(),
                                dialog.txtDashCashRate.text.toString().trim(),
                                selectedGoldrateToShow
                            )
                            dialog.dismiss()
                        }
                    else -> {}
                }

                else -> {}
            }

        }

        dialog.show()

    }

    fun updateGoldrateAPI(
        token: String?,
        companyID: String?,
        bill_rate: String?,
        cash_rate: String?,
        type: String
    ) {
        if (isValidClickPressed()) {
            if (NetworkUtils.isConnected()) {

                dashboardViewModel.updateGoldrate(token, companyID, bill_rate, cash_rate, type)
                    .observe(requireActivity(), Observer {
                        it?.let { resource ->
                            when (resource.status) {
                                Status.SUCCESS -> {
                                    if (it.data?.status == true) {
                                        loginModel.data?.company_info?.gold_rate?.cash_rate_amount =
                                            cash_rate
                                        loginModel.data?.company_info?.gold_rate?.bill_rate_amount =
                                            bill_rate
                                        loginModel.data?.company_info?.gold_rate?.type =
                                            selectedGoldrateToShow
                                        //updating login_detail pref
                                        val prefs =
                                            PreferenceHelper.defaultPrefs(activity?.applicationContext!!)
                                        prefs.edit().remove(Constants.PREF_LOGIN_DETAIL_KEY).apply()
                                        prefs[Constants.PREF_LOGIN_DETAIL_KEY] =
                                            Gson().toJson(loginModel) //setter

                                        //own fragment refresh to load data according to switched branch
                                        val ft: FragmentTransaction =
                                            parentFragmentManager.beginTransaction()
                                        if (Build.VERSION.SDK_INT >= 26) {
                                            ft.setReorderingAllowed(false)
                                        }
                                        ft.detach(this).attach(this).commitAllowingStateLoss()
                                        Log.i("Fragment Refresh", "Yes");


                                        Toast.makeText(
                                            activity?.applicationContext!!,
                                            getString(R.string.todays_goldrate_updated),
                                            Toast.LENGTH_SHORT
                                        ).show()

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
                                                context?.let { it1 ->
                                                    CommonUtils.somethingWentWrong(
                                                        it1
                                                    )
                                                }
                                            }

                                        }
                                    }
                                    CommonUtils.hideProgress()

                                }
                                Status.ERROR -> {
                                    CommonUtils.hideProgress()
                                }
                                Status.LOADING -> {
                                    CommonUtils.showProgress(requireActivity())
                                }
                            }
                        }
                    })
            }
        }
    }


    private fun getUpdateSessionDetails(token: String?) {
        if (NetworkUtils.isConnected()) {
            CommonUtils.hideInternetDialog()
            dashboardViewModel.getUpdateSessionDetails(token)
                .observe(this, Observer {
                    it?.let { resource ->
                        when (resource.status) {
                            Status.SUCCESS -> {
                                if (it.data?.status == true) {

                                    //updating loginmodel data
                                    loginModel.data?.branch_info = it.data?.data?.branch_info
                                    loginModel.data?.user_info = it.data?.data?.user_info
                                    loginModel.data?.company_info = it.data?.data?.company_info

                                    //updating login_detail pref
                                    val prefs =
                                        PreferenceHelper.defaultPrefs(activity?.applicationContext!!)
                                    prefs.edit().remove(Constants.PREF_LOGIN_DETAIL_KEY).apply()
                                    prefs[Constants.PREF_LOGIN_DETAIL_KEY] =
                                        Gson().toJson(loginModel) //setter

                                    if (loginModel.data?.company_info?.gold_rate?.type.equals(
                                            "1",
                                            true
                                        )
                                    ) {
                                        dashboardGoldrate =
                                            loginModel.data?.company_info?.gold_rate?.bill_rate_amount.toString()
                                    } else
                                        dashboardGoldrate =
                                            loginModel.data?.company_info?.gold_rate?.cash_rate_amount.toString()


                                    var firstPart: String = "Today's Gold Rate: "
                                    //var secondPart: String = "+91-9876543210. "
                                    var secondPart: String = "$dashboardGoldrate"
                                    var finalString: String = firstPart + secondPart
                                    var spannableStringBuilder: SpannableStringBuilder =
                                        SpannableStringBuilder(finalString)
                                    val clickablegoldrate = object : ClickableSpan() {
                                        override fun onClick(widget: View) {
                                            // nothing to do
                                        }

                                        override fun updateDrawState(ds: TextPaint) {
                                            super.updateDrawState(ds)
                                            ds.color =
                                                ContextCompat.getColor(
                                                    activity?.applicationContext!!,
                                                    android.R.color.black
                                                )
                                            ds.typeface = ResourcesCompat.getFont(
                                                activity?.applicationContext!!,
                                                R.font.proxima_nova_bold
                                            )
                                            ds.isUnderlineText = false
                                        }

                                    }
                                    spannableStringBuilder.setSpan(
                                        clickablegoldrate,
                                        19,
                                        19 + dashboardGoldrate.length,
                                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                                    )
                                    tvLeftDash_TodaysRate.setText(spannableStringBuilder/*"Today's Gold Rate: $dashboardGoldrate"*/)

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
                                            CommonUtils.somethingWentWrong(requireActivity())
                                        }

                                    }

                                }
//                                CommonUtils.hideProgress()

                            }
                            Status.ERROR -> {
                                CommonUtils.hideProgress()
                                /*Toast.makeText(
                                    this,
                                    it.data?.errormessage?.message,
                                    Toast.LENGTH_LONG
                                )
                                    .show()*/
                            }
                            Status.LOADING -> {
                                CommonUtils.showProgress(requireActivity())
                            }
                        }
                    }
                })
        } else {
            /*CommonUtils.showDialog(this, getString(R.string.please_check_internet_msg))*/
        }
    }


    fun enableBtnsHideProgress() {
        CommonUtils.hideProgress()
    }

    fun compareDates(d1: String?, d2: String?) {
        try {
            val sdf = SimpleDateFormat("yyyy-MMM-dd")
            val date1 = sdf.parse(d1)
            val date2 = sdf.parse(d2)
            if (date1.after(date2)) {
                thisFiscalSD = "01-Apr-$currentYear"
                thisFiscalED = "31-Mar-$nextYear"
                Log.v("thisFiscal", "" + thisFiscalSD + "" + thisFiscalED)
                thisPreviousFiscalSD = "01-Apr-$prevYear"
                thisPreviousFiscalED = "31-Mar-$currentYear"
                Log.v("thisPreviousFiscal", "" + thisPreviousFiscalSD + "" + thisPreviousFiscalED)

            } else {
                currentYear = currentYear - 1
                nextYear = currentYear + 1
                prevYear = currentYear - 1
                thisFiscalSD = "01-Apr-$currentYear"
                thisFiscalED = "31-Mar-$nextYear"
                Log.v("thisFiscal", "" + thisFiscalSD + "" + thisFiscalED)
                thisPreviousFiscalSD = "01-Apr-$prevYear"
                thisPreviousFiscalED = "31-Mar-$currentYear"
                Log.v("thisPreviousFiscal", "" + thisPreviousFiscalSD + "" + thisPreviousFiscalED)
            }
            if (!isFiscalYearFromPref) {
                binding.tvLeftDashFiscalYear.setText(
                    "F.Y.: " + thisFiscalSD +
                            " to " + thisFiscalED
                )
                val childModel =
                    FiscalYearModel(thisFiscalSD, thisFiscalED, selectFiscalYearFromMenu)
                prefs[Constants.FiscalYear] = Gson().toJson(childModel)
            }


        } catch (ex: ParseException) {
            ex.printStackTrace()
        }
    }

}
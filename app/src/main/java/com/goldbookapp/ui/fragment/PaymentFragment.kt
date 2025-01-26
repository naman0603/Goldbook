package com.goldbookapp.ui.ui.send

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.NetworkUtils
import com.goldbookapp.R
import com.goldbookapp.api.ApiHelper
import com.goldbookapp.api.RetrofitBuilder
import com.goldbookapp.databinding.FragmentPaymentBinding
import com.goldbookapp.databinding.SalesSortPopupBinding
import com.goldbookapp.model.FiscalYearModel
import com.goldbookapp.model.LoginModel
import com.goldbookapp.model.SearchListPayment
import com.goldbookapp.model.UserWiseRestrictionModel
import com.goldbookapp.ui.AccessDeniedActivity
import com.goldbookapp.ui.MainActivity
import com.goldbookapp.ui.activity.payment.NewPaymentActivity
import com.goldbookapp.ui.activity.viewmodel.ViewModelFactory
import com.goldbookapp.ui.adapter.PaymentListAdapter
import com.goldbookapp.utils.*
import com.goldbookapp.utils.CommonUtils.Companion.clickWithDebounce
import com.goldbookapp.utils.PreferenceHelper.get
import com.goldbookapp.utils.PreferenceHelper.set
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.fragment_payment.*
import kotlinx.android.synthetic.main.fragment_payment.view.*
import kotlinx.android.synthetic.main.toolbar_layout.*
import kotlinx.android.synthetic.main.toolbar_layout.view.*

class PaymentFragment : Fragment() {

    lateinit var popupWindow: PopupWindow
    private var viewDetail:Boolean = false
    lateinit var binding: FragmentPaymentBinding
    private lateinit var viewModel: PaymentViewModel
    private var currentPage: Int = Constants.PAGE_START
    private var page_size: Int = Constants.PAGE_SIZE
    private var isLastPage = false
    private var totalPage = 1
    private var isLoading = false
    private var isFromSearch = false
    var itemCount = 0


    var sortByField: String = ""
    var sortByAscDesc: String = ""
    var sortByColumn: String = ""
    var sortType: String = ""


    lateinit var prefs: SharedPreferences
    private var searchPaymentApiTrackNo: String = "5"

    lateinit var loginModel: LoginModel
    lateinit var fiscalYearModel: FiscalYearModel
    private lateinit var adapter: PaymentListAdapter


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_payment, container, false)
        val view = binding.root

        setupViewModel()
        setupUIandListner(view)

        return view
    }

    private fun setupViewModel() {
        viewModel =
            ViewModelProvider(this, ViewModelFactory(ApiHelper(RetrofitBuilder.apiService))).get(
                PaymentViewModel::class.java
            )
        binding.setLifecycleOwner(this)

    }

    fun setupUIandListner(root: View) {
        prefs = PreferenceHelper.defaultPrefs(activity?.applicationContext!!)
        loginModel = Gson().fromJson(
            prefs[Constants.PREF_LOGIN_DETAIL_KEY, ""],
            LoginModel::class.java
        ) //getter
        fiscalYearModel = Gson().fromJson(
            prefs[Constants.FiscalYear, ""],
            FiscalYearModel::class.java
        ) //getter

        //default sorting is name ascending
        sortByField = getString(R.string.date); sortByAscDesc = Constants.SORT_TYPE_DESCENDING;
        sortByColumn = "transaction_date"
        sortType = "desc"

        root.llFragPayment.setOnFocusChangeListener { view, b ->
            CommonUtils.hideKeyboardnew(
                requireActivity()
            )
        }
        root.imgLeft.setImageResource(R.drawable.ic_menu_black_24dp)
        root.tvTitle.setText(getString(R.string.payment))
        root.imgRight.setImageResource(R.drawable.ic_sort)


        root.imgLeft.clickWithDebounce {
            (activity as MainActivity).openCloseDrawer()
        }

        root.imgRight.clickWithDebounce {
            openSortingPopup()
        }

        prefs.edit().remove(Constants.PREF_Payment_Ref_Selected_Trans_Ids).apply()
        prefs.edit().remove(Constants.PREF_Payment_Ref_Selected_Invoice_Nos).apply()
        prefs.edit().remove(Constants.PREF_SELECTED_STOCK_ID_DETAILS).apply()
        prefs.edit().remove(Constants.PREF_ADD_ITEM_PAYMENT_KEY).apply()

        root.recyclerViewPaymentList.layoutManager = LinearLayoutManager(activity)
        root.recyclerViewPaymentList.addOnScrollListener(object :
            PaginationListener(root.recyclerViewPaymentList.layoutManager as LinearLayoutManager) {
            override fun loadMoreItems() {
                isLoading = true
                currentPage++

                callAccordingToTrack(searchPaymentApiTrackNo, currentPage, false)
            }

            override fun isLastPage(): Boolean {
                return isLastPage
            }

            override fun isLoading(): Boolean {
                return isLoading
            }
        })


        //hides keyboard on focus change from editext(search) to tap anywhere in the screen (below toolbar).
        root.llFragPayment.setOnFocusChangeListener { view, b ->
            CommonUtils.hideKeyboardnew(
                requireActivity()
            )
        }

        root.txtSearchPaymentList.setOnDebounceTextWatcher(lifecycle) { input ->
            if (NetworkUtils.isConnected()) {
                applySearch(root)
                binding.root.pb_loader_payment.visibility = View.VISIBLE
            }
        }

        root.txtSearchPaymentList.setDrawableClickListener(object : DrawableClickListener {
            override fun onClick(target: DrawableClickListener.DrawablePosition?) {
                if (CommonUtils.isValidClickPressed()) {
                    when (target) {
                        DrawableClickListener.DrawablePosition.LEFT -> {
                            if (NetworkUtils.isConnected()) {
                                applySearch(root)
                                binding.root.pb_loader_payment.visibility = View.VISIBLE
                            }
                        }
                        DrawableClickListener.DrawablePosition.RIGHT -> {
                            root.txtSearchPaymentList.text!!.clear()
                            resetToDefault()
                            isFromSearch = true
                            searchPaymentApiTrackNo = "1"
                            //isFromSearch = false
                            callAccordingToTrack(searchPaymentApiTrackNo, currentPage, true)
                        }else->{

                    }
                    }
                }
            }
        })
        root.txtSearchPaymentList.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                // Call your code here
                if (NetworkUtils.isConnected()) {
                    applySearch(root)
                    binding.root.pb_loader_payment.visibility = View.VISIBLE
                }
                true
            }
            false
        }

        binding.root.swipeContainerPaymentList.setColorSchemeResources(R.color.colorAccent)
        binding.root.swipeContainerPaymentList.setOnRefreshListener {
            resetToDefault()
            searchPaymentApiTrackNo = "1"

            callAccordingToTrack(searchPaymentApiTrackNo, currentPage, true)

            binding.swipeContainerPaymentList.isRefreshing = false
        }

        setupAdapter()

    }

    private fun setupAdapter() {
        adapter = PaymentListAdapter(arrayListOf(), totalPage)
        binding.recyclerViewPaymentList.setHasFixedSize(true)
        binding.recyclerViewPaymentList.adapter = adapter
        resetToDefault()
    }

    private fun applySearch(root: View) {
        if (root.txtSearchPaymentList?.toString()?.length!! == 0) {
            resetToDefault()
            //isFromSearch = false
            searchPaymentApiTrackNo = "1"
            callAccordingToTrack(searchPaymentApiTrackNo, currentPage, false)
            root.txtSearchPaymentList.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_search_black_24dp,
                0,
                R.drawable.ic_cancel_blank_24dp,
                0
            )
        } else {
            if (root.txtSearchPaymentList?.toString()?.length!! > 0) {
                searchPaymentApiTrackNo = "2"
                resetToDefault()
                isFromSearch = true
                callAccordingToTrack(searchPaymentApiTrackNo, currentPage, false)
                root.txtSearchPaymentList.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_search_black_24dp,
                    0,
                    0,
                    0
                )
            }
        }

    }

    fun resetToDefault() {
        itemCount = 0
        currentPage = Constants.PAGE_START
        isLastPage = false
        if (this::adapter.isInitialized)
            adapter.clear()
        isFromSearch = false
    }

    override fun onResume() {
        super.onResume()

        (activity as MainActivity).hideBottomSheet()

        /*   sortByField = ""
           sortByAscDesc = ""*/
        val paymentSortType =
            object : TypeToken<String>() {}.type
        searchPaymentApiTrackNo = Gson().fromJson(
            prefs[Constants.PREF_PAYMENT_SORT_TRACKNO, "5"],
            paymentSortType
        ) //getter to maintain track of user with sorting option
        if (NetworkUtils.isConnected()) {

            when(loginModel.data!!.user_info!!.user_type.equals("user",true)){
                // user type user
                true -> {
                    // apply restriciton
                    defaultDisableAllButtonnUI()
                    binding.root.addItemPaymentList.clickWithDebounce {
                        startActivity(
                            Intent(
                                activity,
                                NewPaymentActivity::class.java
                            )
                        )
                    }
                    userWiseRestriction(loginModel.data?.bearer_access_token)
                }
                false->{
                    //admin /super_admin
                   defaultEnableAllButtonnUI()
                    userLimitAccess(loginModel?.data?.bearer_access_token)

                }
            }
            resetToDefault()
            callAccordingToTrack(searchPaymentApiTrackNo, currentPage, true)
        }
        binding.txtSearchPaymentList.clearFocus()

    }

    override fun onPause() {
        super.onPause()

        if (this::popupWindow.isInitialized) {
            popupWindow.dismiss()
        }
    }

    private fun defaultDisableAllButtonnUI() {
        binding.addItemPaymentList.visibility = View.GONE
    }

    private fun defaultEnableAllButtonnUI() {
        binding.addItemPaymentList.visibility = View.VISIBLE
        viewDetail = true
        adapter.viewDetail(viewDetail)
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
            if (data.permission!!.get(i).startsWith(getString(R.string.payment))) {
                // Restriction check for Customer
                when (data.permission!!.get(i).endsWith(getString(R.string.list), true)) {
                    true -> {
                        viewDetail = true
                        adapter.viewDetail(viewDetail)
                    }else->{

                }
                }
            }
            if (data.permission!!.get(i).startsWith(getString(R.string.payment))) {
                // Restriction check for Customer
                when (data.permission!!.get(i).endsWith(getString(R.string.add_edit), true)) {
                    true -> {
                        binding.addItemPaymentList.visibility = View.VISIBLE
                    }else->{

                }
                }
            }

        }
    }

    private fun userLimitAccess(token: String?) {
        if (NetworkUtils.isConnected()) {
            viewModel.userLimitAccess(token)
                .observe(requireActivity(), Observer {
                    it?.let { resource ->
                        when (resource.status) {
                            Status.SUCCESS -> {

                                if (it.data?.status == true) {
                                    val message_payment = it.data.data!!.message_payment
                                    // restrictUsertoAddPayment(it.data.data!!.can_add_payment)
                                    if (it.data.data!!.can_add_payment.equals("0")) {
                                        binding.root.addItemPaymentList.clickWithDebounce {

                                            startActivity(
                                                Intent(
                                                    requireActivity(),
                                                    AccessDeniedActivity::class.java
                                                ).setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_NEW_TASK)
                                                    .putExtra(
                                                        Constants.isFromListRestrict,
                                                        Constants.isFromListRestrict
                                                    )
                                                    .putExtra(
                                                        Constants.restrict_msg,
                                                        message_payment
                                                    )
                                            )

                                        }
                                    } else {
                                        binding.root.addItemPaymentList.clickWithDebounce {
                                            startActivity(
                                                Intent(
                                                    activity,
                                                    NewPaymentActivity::class.java
                                                )
                                            )
                                        }
                                    }

                                } else {

                                }


                            }
                            Status.ERROR -> {

                            }
                            Status.LOADING -> {

                            }
                        }
                    }
                })
        }
    }

    private fun callAccordingToTrack(
        searchPaymentApiTrackNo: String,
        current_page: Int?,
        showLoading: Boolean
    ) {
        when (searchPaymentApiTrackNo) {
            "1" -> {
                // default call(first time & while reset click)
                searchListPayment(
                    showLoading,
                    loginModel?.data?.bearer_access_token,
                    txtSearchPaymentList.text.toString(),
                    sortByColumn,
                    sortType,
                    fiscalYearModel.start_date,
                    fiscalYearModel.end_date
                )
            }
            "2" -> {
                searchListPayment(
                    showLoading,
                    loginModel?.data?.bearer_access_token,
                    txtSearchPaymentList.text.toString(),
                    sortByColumn,
                    sortType,
                    fiscalYearModel.start_date,
                    fiscalYearModel.end_date
                )
            }
            /* "3"->{
                 searchListPayment(showLoading, loginModel?.data?.bearer_access_token,"", "", "")
             }*/
            "4" -> {
                sortByField = getString(R.string.date); sortByAscDesc =
                    Constants.SORT_TYPE_ASCENDING;
                sortByColumn = "transaction_date"
                sortType = "asc"
                searchListPayment(
                    showLoading,
                    loginModel?.data?.bearer_access_token,
                    txtSearchPaymentList.text.toString(),
                    sortByColumn,
                    sortType,
                    fiscalYearModel.start_date,
                    fiscalYearModel.end_date
                )
            }
            "5" -> {
                sortByField = getString(R.string.date); sortByAscDesc =
                    Constants.SORT_TYPE_DESCENDING;
                sortByColumn = "transaction_date"
                sortType = "desc"
                searchListPayment(
                    showLoading,
                    loginModel?.data?.bearer_access_token,
                    txtSearchPaymentList.text.toString(),
                    sortByColumn,
                    sortType,
                    fiscalYearModel.start_date,
                    fiscalYearModel.end_date
                )
            }
            "6" -> {
                sortByField = getString(R.string.paymentno); sortByAscDesc =
                    Constants.SORT_TYPE_ASCENDING;
                sortByColumn = "invoice_number"
                sortType = "asc"
                searchListPayment(
                    showLoading,
                    loginModel?.data?.bearer_access_token,
                    txtSearchPaymentList.text.toString(),
                    sortByColumn,
                    sortType,
                    fiscalYearModel.start_date,
                    fiscalYearModel.end_date
                )
            }
            "7" -> {
                sortByField = getString(R.string.paymentno); sortByAscDesc =
                    Constants.SORT_TYPE_DESCENDING;
                sortByColumn = "invoice_number"
                sortType = "desc"
                searchListPayment(
                    showLoading,
                    loginModel?.data?.bearer_access_token,
                    txtSearchPaymentList.text.toString(),
                    sortByColumn,
                    sortType,
                    fiscalYearModel.start_date,
                    fiscalYearModel.end_date
                )
            }
            "8" -> {
                sortByField = getString(R.string.partyname); sortByAscDesc =
                    Constants.SORT_TYPE_ASCENDING;
                sortByColumn = "contact_name"
                sortType = "asc"
                searchListPayment(
                    showLoading,
                    loginModel?.data?.bearer_access_token,
                    txtSearchPaymentList.text.toString(),
                    sortByColumn,
                    sortType,
                    fiscalYearModel.start_date,
                    fiscalYearModel.end_date
                )
            }
            "9" -> {
                sortByField = getString(R.string.partyname); sortByAscDesc =
                    Constants.SORT_TYPE_DESCENDING;
                sortByColumn = "contact_name"
                sortType = "desc"
                searchListPayment(
                    showLoading,
                    loginModel?.data?.bearer_access_token,
                    txtSearchPaymentList.text.toString(),
                    sortByColumn,
                    sortType,
                    fiscalYearModel.start_date,
                    fiscalYearModel.end_date
                )
            }

        }

    }

    private fun openSortingPopup() {

        val layoutInflater =
            requireContext()!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        var binding: SalesSortPopupBinding =
            DataBindingUtil.inflate(layoutInflater, R.layout.sales_sort_popup, null, false)
        val view = binding.root
        popupWindow = PopupWindow(
            view,
            requireContext().resources.getDimension(R.dimen._200sdp).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )
        popupWindow.contentView = view
        popupWindow.setBackgroundDrawable(ColorDrawable(Color.WHITE));
        popupWindow.elevation = 20.0f
        popupWindow.showAsDropDown(imgRight2)
        binding.saleSortvoucherNo.setText(R.string.paymentno)
        //popupWindow.animationStyle = (R.style.CustomSortPopupWindow)

        refreshSortPopupView(binding);

        highlightSortingColor(binding)


        binding.dateAscSortSales.clickWithDebounce {
            refreshSortPopupView(binding); sortByField = getString(R.string.date); sortByAscDesc =
            Constants.SORT_TYPE_ASCENDING;
            (binding.dateAscSortSales as ImageView).setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorPrimary
                )
            )
            searchPaymentApiTrackNo = "4"
            resetToDefault()
            callAccordingToTrack(searchPaymentApiTrackNo, currentPage, true)
        }

        binding.dateDescSortSales.clickWithDebounce {
            refreshSortPopupView(binding); sortByField = getString(R.string.date); sortByAscDesc =
            Constants.SORT_TYPE_DESCENDING;
            (binding.dateDescSortSales as ImageView).setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorPrimary
                )
            )
            searchPaymentApiTrackNo = "5"
            resetToDefault()
            callAccordingToTrack(searchPaymentApiTrackNo, currentPage, true)
        }

        binding.voucherAscSortSales.clickWithDebounce {
            refreshSortPopupView(binding); sortByField =
            getString(R.string.paymentno); sortByAscDesc = Constants.SORT_TYPE_ASCENDING;
            (binding.voucherAscSortSales as ImageView).setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorPrimary
                )
            )
            searchPaymentApiTrackNo = "6"
            resetToDefault()
            callAccordingToTrack(searchPaymentApiTrackNo, currentPage, true)
        }

        binding.voucherDescSortSales.clickWithDebounce {
            refreshSortPopupView(binding); sortByField =
            getString(R.string.paymentno); sortByAscDesc = Constants.SORT_TYPE_DESCENDING;
            (binding.voucherDescSortSales as ImageView).setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorPrimary
                )
            )
            searchPaymentApiTrackNo = "7"
            resetToDefault()
            callAccordingToTrack(searchPaymentApiTrackNo, currentPage, true)
        }


        binding.partynameAscSortSales.clickWithDebounce {
            refreshSortPopupView(binding); sortByField =
            getString(R.string.partyname); sortByAscDesc = Constants.SORT_TYPE_ASCENDING;
            (binding.partynameAscSortSales as ImageView).setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorPrimary
                )
            )
            searchPaymentApiTrackNo = "8"
            resetToDefault()
            callAccordingToTrack(searchPaymentApiTrackNo, currentPage, true)
        }

        binding.partynameDescSortSales.clickWithDebounce {
            refreshSortPopupView(binding); sortByField =
            getString(R.string.partyname); sortByAscDesc = Constants.SORT_TYPE_DESCENDING;
            (binding.partynameDescSortSales as ImageView).setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorPrimary
                )
            )
            searchPaymentApiTrackNo = "9"
            resetToDefault()
            callAccordingToTrack(searchPaymentApiTrackNo, currentPage, true)
        }

        binding.resetAllSalesSort.clickWithDebounce {
            refreshSortPopupView(binding);
            searchPaymentApiTrackNo = "5"
            resetToDefault()
            //default sorting is trans desc
            sortByField = getString(R.string.date); sortByAscDesc = Constants.SORT_TYPE_DESCENDING;
            sortByColumn = "transaction_date"
            sortType = "desc"
            highlightSortingColor(binding)
            if (txtSearchPaymentList.text.toString().length > 0)
                txtSearchPaymentList.setText("")
            callAccordingToTrack(searchPaymentApiTrackNo, currentPage, true)

        }


    }

    private fun highlightSortingColor(binding: SalesSortPopupBinding) {
        if (sortByField == getString(R.string.date) && sortByAscDesc == Constants.SORT_TYPE_ASCENDING) {
            binding.dateAscSortSales.setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorPrimary
                )
            )
        } else if (sortByField == getString(R.string.date) && sortByAscDesc == Constants.SORT_TYPE_DESCENDING) {
            binding.dateDescSortSales.setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorPrimary
                )
            )
        } else if (sortByField == getString(R.string.paymentno) && sortByAscDesc == Constants.SORT_TYPE_ASCENDING) {
            binding.voucherAscSortSales.setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorPrimary
                )
            )
        } else if (sortByField == getString(R.string.paymentno) && sortByAscDesc == Constants.SORT_TYPE_DESCENDING) {
            binding.voucherDescSortSales.setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorPrimary
                )
            )
        } else if (sortByField == getString(R.string.partyname) && sortByAscDesc == Constants.SORT_TYPE_ASCENDING) {
            binding.partynameAscSortSales.setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorPrimary
                )
            )
        } else if (sortByField == getString(R.string.partyname) && sortByAscDesc == Constants.SORT_TYPE_DESCENDING) {
            binding.partynameDescSortSales.setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorPrimary
                )
            )
        }
    }

    fun refreshSortPopupView(binding: SalesSortPopupBinding) {
        binding.dateAscSortSales.setColorFilter(
            ContextCompat.getColor(
                requireContext(),
                android.R.color.transparent
            )
        )
        binding.dateDescSortSales.setColorFilter(
            ContextCompat.getColor(
                requireContext(),
                android.R.color.transparent
            )
        )
        binding.voucherAscSortSales.setColorFilter(
            ContextCompat.getColor(
                requireContext(),
                android.R.color.transparent
            )
        )
        binding.voucherDescSortSales.setColorFilter(
            ContextCompat.getColor(
                requireContext(),
                android.R.color.transparent
            )
        )
        binding.partynameAscSortSales.setColorFilter(
            ContextCompat.getColor(
                requireContext(),
                android.R.color.transparent
            )
        )
        binding.partynameDescSortSales.setColorFilter(
            ContextCompat.getColor(
                requireContext(),
                android.R.color.transparent
            )
        )

    }

    fun searchListPayment(
        showLoading: Boolean, token: String?, searchName: String?, sort_by_column: String?,
        sort_type: String?,date_range_from: String?,date_range_to: String?
    ) {

        if (NetworkUtils.isConnected()) {

            viewModel.searchListPayment(token, currentPage, searchName, sort_by_column,
                sort_type,date_range_from,date_range_to)
                .observe(requireActivity(), Observer {
                    it?.let { resource ->
                        when (resource.status) {
                            Status.SUCCESS -> {
                                if (it.data?.status == true) {
                                    itemCount = it.data.data?.size!!
                                    totalPage = it.data.total_page!!
                                    page_size = it.data.page_limit!!
                                    retrieveListPayment(it.data?.data, page_size)

                                    //val nums = listOf(it.data?.data)
                                    //val listSize: Int? = it.data?.data?.size

                                    if (it.data?.data?.isNotEmpty()!!) {
                                        binding.recyclerViewPaymentList.visibility = View.VISIBLE
                                        binding.tvNoRecordPaymentList.visibility = View.GONE
                                    } else {
                                        binding.tvNoRecordPaymentList.text = it.data.message

                                        binding.recyclerViewPaymentList.visibility = View.GONE
                                        binding.tvNoRecordPaymentList.visibility = View.VISIBLE
                                    }

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
                                binding.root.pb_loader_payment.visibility = View.GONE
                            }
                            Status.ERROR -> {
                                CommonUtils.hideProgress()

                            }
                            Status.LOADING -> {
                                if (showLoading)
                                    CommonUtils.showProgress(requireContext())
                            }
                        }
                    }
                })
        }
    }

    private fun retrieveListPayment(
        paymentList: List<SearchListPayment.DataPayment>?,
        pageSize: Int
    ) {

        if (currentPage != Constants.PAGE_START) adapter.removeLoading()
        //adapter.addCustomer(customersList)
        adapter.apply {
            addPayment(paymentList, isFromSearch, pageSize, currentPage, totalPage)
            notifyDataSetChanged()
        }

        binding.swipeContainerPaymentList.setRefreshing(false)

        // check weather is last page or not
        if (currentPage < totalPage) {
            adapter.addLoading()
        } else {
            isLastPage = true
        }
        isLoading = false
        prefs[Constants.PREF_PAYMENT_SORT_TRACKNO] =
            searchPaymentApiTrackNo //setter (for payment sort tracking while relaunching app)
    }
}






package com.goldbookapp.ui.fragment

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
import com.goldbookapp.databinding.FragmentSaleBinding
import com.goldbookapp.databinding.SalesSortPopupBinding
import com.goldbookapp.model.FiscalYearModel
import com.goldbookapp.model.LoginModel
import com.goldbookapp.model.SearchListSalesModel
import com.goldbookapp.model.UserWiseRestrictionModel
import com.goldbookapp.ui.AccessDeniedActivity
import com.goldbookapp.ui.MainActivity
import com.goldbookapp.ui.activity.sales.NewInvoiceActivity
import com.goldbookapp.ui.activity.viewmodel.ViewModelFactory
import com.goldbookapp.ui.adapter.SalesListAdapter
import com.goldbookapp.ui.fragment.viewmodel.SaleViewModel
import com.goldbookapp.utils.*
import com.goldbookapp.utils.CommonUtils.Companion.clickWithDebounce
import com.goldbookapp.utils.PreferenceHelper.get
import com.goldbookapp.utils.PreferenceHelper.set
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.fragment_sale.*
import kotlinx.android.synthetic.main.fragment_sale.view.*
import kotlinx.android.synthetic.main.toolbar_layout.*
import kotlinx.android.synthetic.main.toolbar_layout.view.*

class SaleFragment : Fragment() {

    lateinit var popupWindow: PopupWindow

    private var viewDetail: Boolean = false
    private lateinit var viewModel: SaleViewModel
    private var currentPage: Int = Constants.PAGE_START
    private var page_size: Int = Constants.PAGE_SIZE
    private var isLastPage = false
    private var totalPage = 1
    private var isLoading = false
    private var isFromSearch = false
    var itemCount = 0

    lateinit var binding: FragmentSaleBinding
    private var searchSalesApiTrackNo: String = "5"
    private lateinit var adapter: SalesListAdapter
    lateinit var prefs: SharedPreferences
    lateinit var loginModel: LoginModel
    lateinit var fiscalYearModel: FiscalYearModel

    var sortByField: String = ""
    var sortByAscDesc: String = ""
    var sortByColumn: String = ""
    var sortType: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_sale, container, false)
        val view = binding.root

        setupViewModel()
        setupUIandListner(view)

        return view
    }

    private fun setupViewModel() {
        viewModel =
            ViewModelProvider(this, ViewModelFactory(ApiHelper(RetrofitBuilder.apiService))).get(
                SaleViewModel::class.java
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

        CommonUtils.hideInternetDialog()

        binding.root.imgLeft.setImageResource(R.drawable.ic_menu_black_24dp)
        binding.root.tvTitle.setText(getString(R.string.sale))
        binding.root.imgRight.setImageResource(R.drawable.ic_sort)


        root.imgLeft?.clickWithDebounce {
            (activity as MainActivity).openCloseDrawer()
        }

        root.imgRight?.clickWithDebounce {
            openSortingPopup()
        }

        root.recyclerViewSales.layoutManager = LinearLayoutManager(activity)
        root.recyclerViewSales.addOnScrollListener(object :
            PaginationListener(root.recyclerViewSales.layoutManager as LinearLayoutManager) {
            override fun loadMoreItems() {
                isLoading = true
                currentPage++
                //Toast.makeText(context,"reyscrollcurrentPage" + "$currentPage",Toast.LENGTH_SHORT).show()
                callAccordingToTrack(searchSalesApiTrackNo, currentPage, false)
            }

            override fun isLastPage(): Boolean {
                return isLastPage
            }

            override fun isLoading(): Boolean {
                return isLoading
            }
        })


        //hides keyboard on focus change from editext(search) to tap anywhere in the screen
        root.isClickable = true
        root.isFocusable = true
        root.isFocusableInTouchMode = true
        root.setOnFocusChangeListener { v, hasFocus -> CommonUtils.hideKeyboardnew(requireActivity()) }


        root.txtSearchSale.setOnDebounceTextWatcher(lifecycle) { input ->
            if (NetworkUtils.isConnected()) {
                applySearch(root)
                binding.root.pb_loader_sale.visibility = View.VISIBLE
            }
        }

        root.txtSearchSale.setDrawableClickListener(object : DrawableClickListener {
            override fun onClick(target: DrawableClickListener.DrawablePosition?) {
                if (CommonUtils.isValidClickPressed()) {
                    when (target) {
                        DrawableClickListener.DrawablePosition.LEFT -> {
                            if (NetworkUtils.isConnected()) {
                                applySearch(root)
                                binding.root.pb_loader_sale.visibility = View.VISIBLE
                            }

                        }
                        DrawableClickListener.DrawablePosition.RIGHT -> {
                            root.txtSearchSale.text!!.clear()

                        }else->{

                    }
                    }
                }
            }
        })
        root.txtSearchSale.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                // Call your code here
                if (NetworkUtils.isConnected()) {
                    applySearch(root)
                    binding.root.pb_loader_sale.visibility = View.VISIBLE
                }

                true
            }
            false
        }
        binding.root.swipeContainerSale.setColorSchemeResources(R.color.colorAccent)
        binding.root.swipeContainerSale.setOnRefreshListener {
            resetToDefault()

            callAccordingToTrack(searchSalesApiTrackNo, currentPage, true)

            binding.swipeContainerSale.isRefreshing = false
        }
        (activity as MainActivity).showBottomSheet()

        setupAdapter()
    }

    private fun setupAdapter() {
        adapter = SalesListAdapter(arrayListOf(), totalPage)
        binding.recyclerViewSales.setHasFixedSize(true)
        binding.recyclerViewSales.adapter = adapter
        resetToDefault()
    }

    private fun applySearch(root: View) {
        if (root.txtSearchSale?.toString()?.length!! == 0) {
            resetToDefault()
            //isFromSearch = false
            searchSalesApiTrackNo = "1"
            callAccordingToTrack(searchSalesApiTrackNo, currentPage, false)
            root.txtSearchSale.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_search_black_24dp,
                0,
                R.drawable.ic_cancel_blank_24dp,
                0
            )
        } else {
            if (root.txtSearchSale?.toString()?.length!! > 0) {
                searchSalesApiTrackNo = "2"
                resetToDefault()
                isFromSearch = true
                callAccordingToTrack(searchSalesApiTrackNo, currentPage, false)
                root.txtSearchSale.setCompoundDrawablesWithIntrinsicBounds(
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

        val salesSortType =
            object : TypeToken<String>() {}.type
        searchSalesApiTrackNo = Gson().fromJson(
            prefs[Constants.PREF_SALES_SORT_TRACKNO, "5"],
            salesSortType
        ) //getter to maintain track of user with sorting option
        // callAccordingToTrack(searchSalesApiTrackNo)
        if (NetworkUtils.isConnected()) {

            when (loginModel.data!!.user_info!!.user_type.equals("user", true)) {
                // user type user
                true -> {
                    // apply restriciton
                    defaultDisableAllButtonnUI()
                    binding.root.addItem_sale.clickWithDebounce {

                        startActivity(
                            Intent(
                                activity,
                                NewInvoiceActivity::class.java
                            )
                        )
                    }
                    userWiseRestriction(loginModel.data?.bearer_access_token)
                }
                false -> {
                    //admin /super_admin
                    defaultEnableAllButtonnUI()
                    userLimitAccess(loginModel?.data?.bearer_access_token)

                }
            }
            resetToDefault()
            callAccordingToTrack(searchSalesApiTrackNo, currentPage, true)
        }
        binding.txtSearchSale.clearFocus()

    }

    override fun onPause() {
        super.onPause()

        if (this::popupWindow.isInitialized) {
            popupWindow.dismiss()
        }
    }


    private fun defaultDisableAllButtonnUI() {
        binding.addItemSale.visibility = View.GONE
    }

    private fun defaultEnableAllButtonnUI() {
        binding.addItemSale.visibility = View.VISIBLE
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
                                    (activity as MainActivity).updateDrawerList(it.data.data)
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

    private fun applyUserWiseRestriction(data: UserWiseRestrictionModel.Data) {
        for (i in 0 until data.permission!!.size) {
            if (data.permission!!.get(i).startsWith(getString(R.string.sales))) {
                // Restriction check for Customer
                when (data.permission!!.get(i).endsWith(getString(R.string.list), true)) {
                    true -> {
                        viewDetail = true
                        adapter.viewDetail(viewDetail)
                    }else->{

                }
                }
            }
            if (data.permission!!.get(i).startsWith(getString(R.string.sales))) {
                // Restriction check for Customer
                when (data.permission!!.get(i).endsWith(getString(R.string.add_edit), true)) {
                    true -> {
                        binding.addItemSale.visibility = View.VISIBLE
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
                                    // restrictUsertoAddSale(it.data.data!!.can_add_sales)
                                    val message_sales = it.data.data!!.message_sales
                                    if (it.data.data!!.can_add_sales.equals("0")) {
                                        binding.root.addItem_sale.clickWithDebounce {
                                            startActivity(
                                                Intent(
                                                    requireActivity(),
                                                    AccessDeniedActivity::class.java
                                                ).setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_NEW_TASK)
                                                    .putExtra(
                                                        Constants.isFromListRestrict,
                                                        Constants.isFromListRestrict
                                                    )
                                                    .putExtra(Constants.restrict_msg, message_sales)
                                            )
                                            CommonUtils.hideInternetDialog()

                                        }
                                    } else {
                                        binding.root.addItem_sale.clickWithDebounce {

                                            startActivity(
                                                Intent(
                                                    activity,
                                                    NewInvoiceActivity::class.java
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
        searchSalesApiTrackNo: String,
        currentPage: Int,
        showLoading: Boolean
    ) {
        when (searchSalesApiTrackNo) {
            "1" -> {
                searchListSalesAPI(
                    showLoading,
                    loginModel?.data?.bearer_access_token,
                    currentPage,
                    binding.txtSearchSale.text.toString(),
                    sortByColumn,
                    sortType,
                    fiscalYearModel.start_date,
                    fiscalYearModel.end_date
                )
            }
            "2" -> {
                searchListSalesAPI(
                    showLoading,
                    loginModel?.data?.bearer_access_token,
                    currentPage,
                    binding.txtSearchSale.text.toString(),
                    sortByColumn,
                    sortType,
                    fiscalYearModel.start_date,
                    fiscalYearModel.end_date
                )
            }
            /* "3"->{
                 searchListSalesAPI(showLoading, loginModel?.data?.bearer_access_token,
                     loginModel?.data?.company_info?.id,txtSearchSale.text.toString(), sortByColumn, sortType)
             }*/
            "4" -> {
                sortByField = getString(R.string.date); sortByAscDesc =
                    Constants.SORT_TYPE_ASCENDING;
                sortByColumn = "transaction_date"
                sortType = "asc"
                searchListSalesAPI(
                    showLoading,
                    loginModel?.data?.bearer_access_token,
                    currentPage,
                    binding.txtSearchSale.text.toString(),
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
                searchListSalesAPI(
                    showLoading,
                    loginModel?.data?.bearer_access_token,
                    currentPage,
                    binding.txtSearchSale.text.toString(),
                    sortByColumn,
                    sortType,
                    fiscalYearModel.start_date,
                    fiscalYearModel.end_date
                )
            }
            "6" -> {
                sortByField = getString(R.string.voucher); sortByAscDesc =
                    Constants.SORT_TYPE_ASCENDING;
                sortByColumn = "invoice_number"
                sortType = "asc"
                searchListSalesAPI(
                    showLoading,
                    loginModel?.data?.bearer_access_token,
                    currentPage,
                    binding.txtSearchSale.text.toString(),
                    sortByColumn,
                    sortType,
                    fiscalYearModel.start_date,
                    fiscalYearModel.end_date
                )
            }
            "7" -> {
                sortByField = getString(R.string.voucher); sortByAscDesc =
                    Constants.SORT_TYPE_DESCENDING;
                sortByColumn = "invoice_number"
                sortType = "desc"
                searchListSalesAPI(
                    showLoading,
                    loginModel?.data?.bearer_access_token,
                    currentPage,
                    binding.txtSearchSale.text.toString(),
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
                searchListSalesAPI(
                    showLoading,
                    loginModel?.data?.bearer_access_token,
                    currentPage,
                    binding.txtSearchSale.text.toString(),
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
                searchListSalesAPI(
                    showLoading,
                    loginModel?.data?.bearer_access_token,
                    currentPage,
                    binding.txtSearchSale.text.toString(),
                    sortByColumn,
                    sortType,
                    fiscalYearModel.start_date,
                    fiscalYearModel.end_date
                )
            }

        }

    }

    fun searchListSalesAPI(
        showLoading: Boolean,
        token: String?,
        curret_page: Int?,
        searchName: String?,
        sort_by_column: String?,
        sort_type: String?,
        date_range_from: String?,
        date_range_to: String?
    ) {

        if (NetworkUtils.isConnected()) {

            viewModel.searchListSales(
                token,
                currentPage,
                searchName,
                sort_by_column,
                sort_type,
                date_range_from,
                date_range_to
            )
                .observe(requireActivity(), Observer {
                    it?.let { resource ->
                        when (resource.status) {
                            Status.SUCCESS -> {
                                if (it.data?.status == true) {
                                    itemCount = it.data.data?.size!!
                                    totalPage = it.data.total_page!!
                                    page_size = it.data.page_limit!!
                                    retrieveList(it.data?.data, page_size)

                                    //val nums = listOf(it.data?.data)
                                    //val listSize: Int? = it.data?.data?.size

                                    if (it.data?.data?.isNotEmpty()!!) {
                                        binding.recyclerViewSales.visibility = View.VISIBLE
                                        binding.tvNoRecordSales.visibility = View.GONE
                                    } else {
                                        binding.tvNoRecordSales.text = it.data.message

                                        binding.recyclerViewSales.visibility = View.GONE
                                        binding.tvNoRecordSales.visibility = View.VISIBLE
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
                                CommonUtils.hideInternetDialog()
                                binding.root.pb_loader_sale.visibility = View.GONE
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

    private fun retrieveList(
        salesList: List<SearchListSalesModel.Data1465085328>?,
        pageSize: Int
    ) {

        if (currentPage != Constants.PAGE_START) adapter.removeLoading()
        //adapter.addCustomer(customersList)
        adapter.apply {
            addSale(salesList, isFromSearch, pageSize, currentPage, totalPage)
            notifyDataSetChanged()
        }

        binding.swipeContainerSale.setRefreshing(false)


        // check weather is last page or not
        if (currentPage < totalPage) {
            adapter.addLoading()
        } else {
            isLastPage = true
        }
        isLoading = false
        prefs[Constants.PREF_SALES_SORT_TRACKNO] =
            searchSalesApiTrackNo //setter (for customer sort tracking while relaunching app)
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
            searchSalesApiTrackNo = "4"
            resetToDefault()
            callAccordingToTrack(searchSalesApiTrackNo, currentPage, true)
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
            searchSalesApiTrackNo = "5"
            resetToDefault()
            callAccordingToTrack(searchSalesApiTrackNo, currentPage, true)
        }

        binding.voucherAscSortSales.clickWithDebounce {

            refreshSortPopupView(binding); sortByField =
            getString(R.string.voucher); sortByAscDesc = Constants.SORT_TYPE_ASCENDING;
            (binding.voucherAscSortSales as ImageView).setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorPrimary
                )
            )
            searchSalesApiTrackNo = "6"
            resetToDefault()
            callAccordingToTrack(searchSalesApiTrackNo, currentPage, true)
        }

        binding.voucherDescSortSales.clickWithDebounce {
            refreshSortPopupView(binding); sortByField =
            getString(R.string.voucher); sortByAscDesc = Constants.SORT_TYPE_DESCENDING;
            (binding.voucherDescSortSales as ImageView).setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorPrimary
                )
            )
            searchSalesApiTrackNo = "7"
            resetToDefault()
            callAccordingToTrack(searchSalesApiTrackNo, currentPage, true)
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
            searchSalesApiTrackNo = "8"
            resetToDefault()
            callAccordingToTrack(searchSalesApiTrackNo, currentPage, true)
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
            searchSalesApiTrackNo = "9"
            resetToDefault()
            callAccordingToTrack(searchSalesApiTrackNo, currentPage, true)
        }

        binding.resetAllSalesSort.clickWithDebounce {
            refreshSortPopupView(binding);
            searchSalesApiTrackNo = "5"
            resetToDefault()
            sortByField = getString(R.string.date); sortByAscDesc = Constants.SORT_TYPE_DESCENDING;
            sortByColumn = "transaction_date"
            sortType = "desc"
            highlightSortingColor(binding)
            if (txtSearchSale.text.toString().length > 0)
                txtSearchSale.setText("")
            callAccordingToTrack(searchSalesApiTrackNo, currentPage, true)

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
        } else if (sortByField == getString(R.string.voucher) && sortByAscDesc == Constants.SORT_TYPE_ASCENDING) {
            binding.voucherAscSortSales.setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorPrimary
                )
            )
        } else if (sortByField == getString(R.string.voucher) && sortByAscDesc == Constants.SORT_TYPE_DESCENDING) {
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
}
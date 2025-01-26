package com.goldbookapp.ui.ui.send

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
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
import com.goldbookapp.databinding.FragmentOpeningStockBinding
import com.goldbookapp.databinding.SalesSortPopupBinding
import com.goldbookapp.model.FiscalYearModel
import com.goldbookapp.model.LoginModel
import com.goldbookapp.model.SearchListOpeningStockModel
import com.goldbookapp.model.UserWiseRestrictionModel
import com.goldbookapp.ui.AccessDeniedActivity
import com.goldbookapp.ui.MainActivity
import com.goldbookapp.ui.activity.purchase.NewPurchaseActivity
import com.goldbookapp.ui.activity.openingstock.NewOpeningStockActivity
import com.goldbookapp.ui.activity.viewmodel.ViewModelFactory
import com.goldbookapp.ui.adapter.OpeningStockListAdapter
import com.goldbookapp.ui.ui.share.OpeningStockFragViewModel
import com.goldbookapp.utils.*
import com.goldbookapp.utils.CommonUtils.Companion.clickWithDebounce
import com.goldbookapp.utils.PreferenceHelper.get
import com.goldbookapp.utils.PreferenceHelper.set
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.fragment_opening_stock.view.*
import kotlinx.android.synthetic.main.fragment_purchase.view.*
import kotlinx.android.synthetic.main.toolbar_layout.*
import kotlinx.android.synthetic.main.toolbar_layout.view.*


class OpeningStockFragment : Fragment() {
    lateinit var popupWindow: PopupWindow
    private var viewDetail:Boolean = false
    private lateinit var opeingstockViewModel: OpeningStockFragViewModel
    private var currentPage: Int = Constants.PAGE_START
    private var page_size: Int = Constants.PAGE_SIZE
    private var isLastPage = false
    private var totalPage = 1
    private var isLoading = false
    private var isFromSearch = false
    var itemCount = 0

    lateinit var prefs: SharedPreferences
    private var searchOpeningStockApiTrackNo: String = "8"

    lateinit var binding: FragmentOpeningStockBinding

    private lateinit var adapter: OpeningStockListAdapter

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

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_opening_stock, container, false)
        val view = binding.root

        setupViewModel()
        setupUIandListner(view)

        return view
    }

    private fun setupViewModel() {
        opeingstockViewModel =
            ViewModelProvider(this, ViewModelFactory(ApiHelper(RetrofitBuilder.apiService))).get(
                OpeningStockFragViewModel::class.java
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

        //default sorting is trans date desc
        sortByField = getString(R.string.date); sortByAscDesc = Constants.SORT_TYPE_DESCENDING;
        sortByColumn = "transaction_date"
        sortType = "desc"

        binding.root.imgLeft.setImageResource(R.drawable.ic_menu_black_24dp)
        binding.root.tvTitle.setText(getString(R.string.opening_stock))
        binding.root.imgRight.setImageResource(R.drawable.ic_sort)



        binding.root.imgLeft.clickWithDebounce {
            (activity as MainActivity).openCloseDrawer()
        }

        binding.root.imgRight.clickWithDebounce {
            openSortingPopup(root)
        }

        binding.root.recyclerViewOpeningStock.layoutManager = LinearLayoutManager(activity)
        binding.root.recyclerViewOpeningStock.addOnScrollListener(object :
            PaginationListener(root.recyclerViewOpeningStock.layoutManager as LinearLayoutManager) {
            override fun loadMoreItems() {
                isLoading = true
                currentPage++

                callAccordingToTrack(searchOpeningStockApiTrackNo, currentPage, false)
            }

            override fun isLastPage(): Boolean {
                return isLastPage
            }

            override fun isLoading(): Boolean {
                return isLoading
            }
        })

        //hides keyboard on focus change from editext(search) to tap anywhere in the screen (below toolbar).
        binding.root.llFragOpeningStock.setOnFocusChangeListener { view, b ->
            CommonUtils.hideKeyboardnew(
                requireActivity()
            )
        }

        binding.root.txtSearchOpeningStock.setOnDebounceTextWatcher(lifecycle) { input ->
            if (NetworkUtils.isConnected()) {
                applySearch(root)
                binding.root.pb_loader_openingStock.visibility = View.VISIBLE
            }
        }
        binding.root.txtSearchOpeningStock.setDrawableClickListener(object : DrawableClickListener {
            override fun onClick(target: DrawableClickListener.DrawablePosition?) {
                when (target) {
                    DrawableClickListener.DrawablePosition.LEFT -> {

                        if (NetworkUtils.isConnected()) {
                            applySearch(root)
                            binding.root.pb_loader_openingStock.visibility = View.VISIBLE
                        }

                    }
                    DrawableClickListener.DrawablePosition.RIGHT -> {
                        binding.root.txtSearchOpeningStock.text!!.clear()

                    }else->{

                }
                }
            }
        })

        binding.root.txtSearchOpeningStock.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                // Call your code here
                if (NetworkUtils.isConnected()) {
                    applySearch(root)
                    binding.root.pb_loader_openingStock.visibility = View.VISIBLE
                }

                true
            }
            false
        }

        binding.root.swipeContainerOpeningStock.setColorSchemeResources(R.color.colorAccent)
        binding.root.swipeContainerOpeningStock.setOnRefreshListener {
            resetToDefault()


            callAccordingToTrack(searchOpeningStockApiTrackNo, currentPage, true)

            binding.swipeContainerOpeningStock.isRefreshing = false
        }

        binding.root.addOpeningStock.clickWithDebounce {
            startActivity(
                Intent(
                    activity,
                    NewOpeningStockActivity::class.java
                )
            )
        }


        setupAdapter()
    }

    private fun setupAdapter() {
        adapter = OpeningStockListAdapter(arrayListOf(), totalPage)
        binding.recyclerViewOpeningStock.setHasFixedSize(true)
        binding.recyclerViewOpeningStock.adapter = adapter
        resetToDefault()
    }

    private fun applySearch(root: View) {
        if (binding.root.txtSearchOpeningStock?.toString()?.length!! == 0) {
            resetToDefault()
            //isFromSearch = false
            searchOpeningStockApiTrackNo = "1"
            callAccordingToTrack(searchOpeningStockApiTrackNo, currentPage, false)
            binding.root.txtSearchOpeningStock.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_search_black_24dp,
                0,
                R.drawable.ic_cancel_blank_24dp,
                0
            )

        } else {
            if (binding.root.txtSearchOpeningStock?.toString()?.length!! > 0) {
                searchOpeningStockApiTrackNo = "2"
                resetToDefault()
                isFromSearch = true
                callAccordingToTrack(searchOpeningStockApiTrackNo, currentPage, false)
                binding.root.txtSearchOpeningStock.setCompoundDrawablesWithIntrinsicBounds(
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

        val openingstockSortType =
            object : TypeToken<String>() {}.type
        searchOpeningStockApiTrackNo = Gson().fromJson(
            prefs[Constants.PREF_OPENINGSTOCK_SORT_TRACKNO, "5"],
            openingstockSortType
        ) //getter to maintain track of user with sorting option

        if (NetworkUtils.isConnected()) {

            when(loginModel.data!!.user_info!!.user_type.equals("user",true)){
                // user type user
                true -> {
                    // apply restriciton
                    defaultDisableAllButtonnUI()

                    userWiseRestriction(loginModel.data?.bearer_access_token)
                }
                false->{
                    //admin /super_admin
                    defaultEnableAllButtonnUI()
                    userLimitAccess(loginModel?.data?.bearer_access_token)

                }
            }
            resetToDefault()
            callAccordingToTrack(searchOpeningStockApiTrackNo, currentPage, true)
        }
        binding.txtSearchOpeningStock.clearFocus()

    }

    private fun defaultDisableAllButtonnUI() {
        binding.addOpeningStock.visibility = View.GONE
    }

    private fun defaultEnableAllButtonnUI() {
        binding.addOpeningStock.visibility = View.VISIBLE
        viewDetail = true
        adapter.viewDetail(viewDetail)
    }


    private fun userWiseRestriction(token: String?) {
        if (NetworkUtils.isConnected()) {
            opeingstockViewModel.userWiseRestriction(token)
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
            if (data.permission!!.get(i).startsWith(getString(R.string.opening_stock))) {
                // Restriction check for Customer
                when (data.permission!!.get(i).endsWith(getString(R.string.details), true)) {
                    true -> {
                        viewDetail = true
                        adapter.viewDetail(viewDetail)
                    }else->{

                }
                }
            }
            if (data.permission!!.get(i).startsWith(getString(R.string.opening_stock))) {
                // Restriction check for Customer
                when (data.permission!!.get(i).endsWith(getString(R.string.add_edit), true)) {
                    true -> {
                        binding.addOpeningStock.visibility = View.VISIBLE
                    }else->{

                }
                }
            }

        }
    }

    private fun userLimitAccess(token: String?) {
        if (NetworkUtils.isConnected()) {
            opeingstockViewModel.userLimitAccess(token)
                .observe(requireActivity(), Observer {
                    it?.let { resource ->
                        when (resource.status) {
                            Status.SUCCESS -> {

                                if (it.data?.status == true) {
                                    val message_purchase = it.data.data!!.message_purchase

                                    if (it.data.data!!.can_add_purchase.equals("0")) {
                                        binding.root.addItemPurchase.clickWithDebounce {
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
                                                        message_purchase
                                                    )
                                            )

                                        }
                                    } else {
                                        binding.root.addItemPurchase.clickWithDebounce {
                                            startActivity(
                                                Intent(
                                                    activity,
                                                    NewPurchaseActivity::class.java
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
        searchOpeningStockApiTrackNo: String,
        currentPage: Int,
        showLoading: Boolean
    ) {
        when (searchOpeningStockApiTrackNo) {
            "1" -> {
                // default call(first time & while reset click)
                searchListOpeningStockAPI(
                    showLoading,
                    loginModel?.data?.bearer_access_token,
                    loginModel?.data?.company_info?.id,
                    currentPage,
                    binding.txtSearchOpeningStock.text.toString(),
                    sortByColumn,
                    sortType,
                    fiscalYearModel.start_date,
                    fiscalYearModel.end_date
                )
            }
            "2" -> {
                searchListOpeningStockAPI(
                    showLoading,
                    loginModel?.data?.bearer_access_token,
                    loginModel?.data?.company_info?.id,
                    currentPage,
                    binding.txtSearchOpeningStock.text.toString(),
                    sortByColumn,
                    sortType,
                    fiscalYearModel.start_date,
                    fiscalYearModel.end_date
                )
            }
            /*"3"->{
                searchListPurchaseAPI(false, loginModel?.data?.bearer_access_token, loginModel?.data?.company_info?.id,"", "", "")
            }*/
            "4" -> {
                sortByField = getString(R.string.date); sortByAscDesc =
                    Constants.SORT_TYPE_ASCENDING;
                sortByColumn = "transaction_date"
                sortType = "asc"
                searchListOpeningStockAPI(
                    showLoading,
                    loginModel?.data?.bearer_access_token,
                    loginModel?.data?.company_info?.id,
                    currentPage,
                    binding.txtSearchOpeningStock.text.toString(),
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
                searchListOpeningStockAPI(
                    showLoading,
                    loginModel?.data?.bearer_access_token,
                    loginModel?.data?.company_info?.id,
                    currentPage,
                    binding.txtSearchOpeningStock.text.toString(),
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
                searchListOpeningStockAPI(
                    showLoading,
                    loginModel?.data?.bearer_access_token,
                    loginModel?.data?.company_info?.id,
                    currentPage,
                    binding.txtSearchOpeningStock.text.toString(),
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
                searchListOpeningStockAPI(
                    showLoading,
                    loginModel?.data?.bearer_access_token,
                    loginModel?.data?.company_info?.id,
                    currentPage,
                    binding.txtSearchOpeningStock.text.toString(),
                    sortByColumn,
                    sortType,
                    fiscalYearModel.start_date,
                    fiscalYearModel.end_date
                )
            }


        }

    }

    fun searchListOpeningStockAPI(
        showLoading: Boolean,
        token: String?,
        companyID: String?,
        curret_page: Int?,
        searchName: String?,
        sort_by_column: String?,
        sort_type: String?,
        date_range_from: String?,
        date_range_to: String?
    ) {

        if (NetworkUtils.isConnected()) {

            opeingstockViewModel.searchListOpeningStock(
                token,
                companyID,
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
                                        binding.recyclerViewOpeningStock.visibility = View.VISIBLE
                                        binding.tvNoRecordOpeningStock.visibility = View.GONE
                                    } else {
                                        binding.tvNoRecordOpeningStock.text = it.data.message

                                        binding.recyclerViewOpeningStock.visibility = View.GONE
                                        binding.tvNoRecordOpeningStock.visibility = View.VISIBLE
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
                                binding.root.pb_loader_openingStock.visibility = View.GONE
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
        openingStockList: List<SearchListOpeningStockModel.DataOpeningStock>?,
        pageSize: Int
    ) {
        Log.v("currentpge", currentPage.toString())
        Log.v("isfromsearch", isFromSearch.toString())
        Log.v("pagesize", pageSize.toString())
        Log.v("totalpage", totalPage.toString())
        if (currentPage != Constants.PAGE_START) adapter.removeLoading()
        //adapter.addCustomer(customersList)
        adapter.apply {
            addOpeningStock(openingStockList, isFromSearch, pageSize, currentPage, totalPage)
            notifyDataSetChanged()
        }

        binding.swipeContainerOpeningStock.setRefreshing(false)

        // check weather is last page or not
        if (currentPage < totalPage) {
            adapter.addLoading()
        } else {
            isLastPage = true
        }
        isLoading = false
        prefs[Constants.PREF_OPENINGSTOCK_SORT_TRACKNO] =
            searchOpeningStockApiTrackNo //setter (for purchase sort tracking while relaunching app)
    }

    private fun openSortingPopup(root: View) {

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
        binding.saleSortvoucherNo.setText(R.string.voucherhash)
        binding.salespopupLastrow.visibility = View.GONE
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
            searchOpeningStockApiTrackNo = "4"
            resetToDefault()
            callAccordingToTrack(searchOpeningStockApiTrackNo, currentPage, true)
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
            searchOpeningStockApiTrackNo = "5"
            resetToDefault()
            callAccordingToTrack(searchOpeningStockApiTrackNo, currentPage, true)
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
            searchOpeningStockApiTrackNo = "6"
            resetToDefault()
            callAccordingToTrack(searchOpeningStockApiTrackNo, currentPage, true)
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
            searchOpeningStockApiTrackNo = "7"
            resetToDefault()
            callAccordingToTrack(searchOpeningStockApiTrackNo, currentPage, true)
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
            searchOpeningStockApiTrackNo = "8"
            resetToDefault()
            callAccordingToTrack(searchOpeningStockApiTrackNo, currentPage, true)
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
            searchOpeningStockApiTrackNo = "9"
            resetToDefault()
            callAccordingToTrack(searchOpeningStockApiTrackNo, currentPage, true)
        }

        binding.resetAllSalesSort.clickWithDebounce {
            refreshSortPopupView(binding);
            searchOpeningStockApiTrackNo = "5"
            resetToDefault()
            sortByField = getString(R.string.date); sortByAscDesc = Constants.SORT_TYPE_DESCENDING;
            sortByColumn = "transaction_date"
            sortType = "desc"
            highlightSortingColor(binding)
            if (root.txtSearchOpeningStock.text.toString().length > 0)
                root.txtSearchOpeningStock.setText("")
            callAccordingToTrack(searchOpeningStockApiTrackNo, currentPage, true)

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
package com.goldbookapp.ui.ui.gallery

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.PopupWindow
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.NetworkUtils
import com.goldbookapp.R
import com.goldbookapp.api.ApiHelper
import com.goldbookapp.api.RetrofitBuilder
import com.goldbookapp.databinding.CustomerSortPopupBinding
import com.goldbookapp.databinding.FragmentCustomersBinding
import com.goldbookapp.model.LoginModel
import com.goldbookapp.model.SearchListCustomerModel
import com.goldbookapp.model.UserWiseRestrictionModel
import com.goldbookapp.ui.MainActivity
import com.goldbookapp.ui.activity.customer.NewCustomerActivity
import com.goldbookapp.ui.activity.viewmodel.ViewModelFactory
import com.goldbookapp.ui.adapter.CustomerListAdapter
import com.goldbookapp.utils.*
import com.goldbookapp.utils.CommonUtils.Companion.clickWithDebounce
import com.goldbookapp.utils.CommonUtils.Companion.isValidClickPressed
import com.goldbookapp.utils.Constants.Companion.PAGE_SIZE
import com.goldbookapp.utils.Constants.Companion.PAGE_START
import com.goldbookapp.utils.PreferenceHelper.get
import com.goldbookapp.utils.PreferenceHelper.set
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.fragment_customers.view.*
import kotlinx.android.synthetic.main.toolbar_layout.*
import kotlinx.android.synthetic.main.toolbar_layout.view.*


class CustomersFragment : Fragment() {
    lateinit var sortingpopupWindow: PopupWindow
    lateinit var popupMenu: PopupMenu

    private var viewDetail:Boolean = false
    private lateinit var viewModel: CustomersViewModel
    private var currentPage: Int = PAGE_START
    private var page_size: Int = PAGE_SIZE
    private var isLastPage = false
    private var totalPage = 1
    private var isLoading = false
    private var isFromSearch = false
    var itemCount = 0


    //    private var totalPages: Int = 0
    private var searchCustApiTrackNo: String = "6"
    lateinit var binding: FragmentCustomersBinding
    lateinit var prefs: SharedPreferences
    private lateinit var adapter: CustomerListAdapter
    private var status: String? = "3"
    //1,2,3 ( 1= Active, 2=Inactive, 3=All)

    lateinit var loginModel: LoginModel


    var sortByField: String = ""
    var sortByAscDesc: String = ""
    var sortByColumn: String = ""
    var sortType: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_customers, container, false)
        val view = binding.root

        setupViewModel()
        setupUIandListner(view)

        return view
    }

    private fun setupViewModel() {
        viewModel =
            ViewModelProvider(this, ViewModelFactory(ApiHelper(RetrofitBuilder.apiService))).get(
                CustomersViewModel::class.java
            )
        binding.setLifecycleOwner(this)

    }

    fun setupUIandListner(root: View) {

        var address_str_1: String = ", ,  ,  ,one,,, , ,two  three, , , ,,four, , , , ,"
        address_str_1 = address_str_1.replace(", $".toRegex(), "");


        var address_str_2: String = ", ,  ,  ,one,,, , ,two  three, , , ,,four, , , , ,"
        address_str_2 = address_str_2.replace("^(,|\\s)*|(,|\\s)*$".toRegex(), "")
            .replace("(\\,\\s*)+".toRegex(), ", ")


        prefs = PreferenceHelper.defaultPrefs(activity?.applicationContext!!)
        loginModel = Gson().fromJson(
            prefs[Constants.PREF_LOGIN_DETAIL_KEY, ""],
            LoginModel::class.java
        ) //getter

        //default sorting is name ascending
        sortByField = getString(R.string.name); sortByAscDesc = Constants.SORT_TYPE_ASCENDING
        sortByColumn = "name"
        sortType = "asc"

        CommonUtils.hideInternetDialog()

        //prefs = PreferenceHelper.defaultPrefs(this)
        if (prefs.contains(Constants.PREF_BILLING_ADDRESS_KEY) && prefs.contains(Constants.PREF_SHIPPING_ADDRESS_KEY)) {
            prefs.edit().remove(Constants.PREF_BILLING_ADDRESS_KEY).apply()
            prefs.edit().remove(Constants.PREF_SHIPPING_ADDRESS_KEY).apply()
        }
        if (prefs.contains(Constants.PREF_TCS_TDS_SHARE_DATA)) {
            prefs.edit().remove(Constants.PREF_TCS_TDS_SHARE_DATA).apply()
        }

        root.imgLeft.setImageResource(R.drawable.ic_menu_black_24dp)
        root.imgRight2.setImageResource(R.drawable.ic_sort)
        root.imgRight.setImageResource(R.drawable.ic_more)
        root.tvTitle.setText(getString(R.string.all_customers))

        root.imgLeft.clickWithDebounce {
            (activity as MainActivity).openCloseDrawer()
        }

        root.btnAddCustomerFloating.clickWithDebounce {
            startActivity(Intent(activity, NewCustomerActivity::class.java))
        }

        root.imgRight2.clickWithDebounce {
            openSortingPopup()
        }

        root.recyclerViewCustomers.layoutManager = LinearLayoutManager(activity)
        root.recyclerViewCustomers.addOnScrollListener(object :
            PaginationListener(root.recyclerViewCustomers.layoutManager as LinearLayoutManager) {
            override fun loadMoreItems() {
                isLoading = true
                currentPage++

                callAccordingToTrack(searchCustApiTrackNo, currentPage, false)
            }

            override fun isLastPage(): Boolean {
                return isLastPage
            }

            override fun isLoading(): Boolean {
                return isLoading
            }
        })


        //add scroll listener while user reach in bottom load more will call
        root.imgRight.clickWithDebounce {

            popupMenu = PopupMenu(requireContext(), binding.root.imgRight)
            context?.let { it1 -> CommonUtils.applyFontToMenu(popupMenu.menu, it1) }
            popupMenu.menuInflater.inflate(R.menu.popup_menu_customer, popupMenu.menu)
            when (status) {
                "3" -> {
                    val newTitle: Spannable =
                        SpannableString(popupMenu.menu.getItem(0).title.toString())
                    val newColor = ContextCompat.getColor(requireContext(), R.color.colorPrimary)
                    newTitle.setSpan(
                        ForegroundColorSpan(newColor),
                        0, newTitle.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    popupMenu.menu.getItem(0).title = newTitle
                }
                "1" -> {
                    val newTitle: Spannable =
                        SpannableString(popupMenu.menu.getItem(1).title.toString())
                    val newColor = ContextCompat.getColor(requireContext(), R.color.colorPrimary)
                    newTitle.setSpan(
                        ForegroundColorSpan(newColor),
                        0, newTitle.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    popupMenu.menu.getItem(1).title = newTitle
                }
                "2" -> {
                    val newTitle: Spannable =
                        SpannableString(popupMenu.menu.getItem(2).title.toString())
                    val newColor = ContextCompat.getColor(requireContext(), R.color.colorPrimary)
                    newTitle.setSpan(
                        ForegroundColorSpan(newColor),
                        0, newTitle.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    popupMenu.menu.getItem(2).title = newTitle
                }
            }
            popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.actionAllCustomer -> {
                        status = "3"
                        searchCustApiTrackNo = "1"
                        binding.root.tvTitle.setText(getString(R.string.all_customers))
                        // isFromSearch = false
                        resetToDefault()
                        callAccordingToTrack(searchCustApiTrackNo, currentPage, true)
                        // searchListCustomerAPI(true, loginModel?.data?.bearer_access_token, status, currentPage,txtSearchCustomer.text.toString(), sortByColumn, sortType)
                    }

                    R.id.actionActiveCustomers -> {
                        status = "1"
                        searchCustApiTrackNo = "2"
                        binding.root.tvTitle.setText(getString(R.string.active_customers))
                        //isFromSearch = false
                        resetToDefault()
                        callAccordingToTrack(searchCustApiTrackNo, currentPage, true)


                    }

                    R.id.actionInActiveCustomers -> {
                        status = "2"
                        searchCustApiTrackNo = "3"
                        binding.root.tvTitle.setText(getString(R.string.inactive_customers))
                        //isFromSearch = false
                        resetToDefault()
                        callAccordingToTrack(searchCustApiTrackNo, currentPage, true)

                    }

                }
                true
            })
            popupMenu.show()
        }

        root.txtSearchCustomer.setOnDebounceTextWatcher(lifecycle) { input ->
            if (NetworkUtils.isConnected()) {
                applySearch(root)
                binding.root.pb_loader.visibility = View.VISIBLE
            }

        }

        root.txtSearchCustomer.setDrawableClickListener(object : DrawableClickListener {
            override fun onClick(target: DrawableClickListener.DrawablePosition?) {
                if (isValidClickPressed()) {
                    when (target) {
                        DrawableClickListener.DrawablePosition.LEFT -> {
                            if (NetworkUtils.isConnected()) {
                                applySearch(root)
                                binding.root.pb_loader.visibility = View.VISIBLE
                            }

                        }
                        DrawableClickListener.DrawablePosition.RIGHT -> {
                            root.txtSearchCustomer.text!!.clear()
                        }else->{

                    }
                    }
                }
            }
        })


        root.txtSearchCustomer.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                // Call your code here
                if (NetworkUtils.isConnected()) {
                    applySearch(root)
                    binding.root.pb_loader.visibility = View.VISIBLE
                }

                true
            }
            false
        }

        root.txtSearchCustomer.doOnTextChanged { text, start, before, count ->
            if (text?.length!! > 0) {
                root.txtSearchCustomer.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_search_black_24dp,
                    0,
                    R.drawable.ic_cancel_blank_24dp,
                    0
                );

            } else {
                root.txtSearchCustomer.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_search_black_24dp,
                    0,
                    0,
                    0
                );
               /* resetToDefault()
                isFromSearch = true
                callAccordingToTrack(searchCustApiTrackNo, currentPage, true)*/
            }


        }
        binding.root.swipeContainerCustomer.setColorSchemeResources(R.color.colorAccent)
        binding.root.swipeContainerCustomer.setOnRefreshListener {
            resetToDefault()

            // offset = currentPage
            callAccordingToTrack(searchCustApiTrackNo, currentPage, true)

            binding.swipeContainerCustomer.isRefreshing = false
        }

        (activity as MainActivity).showBottomSheet()

        setupAdapter()

    }

    override fun onPause() {
        super.onPause()
        if(this::sortingpopupWindow.isInitialized){
            sortingpopupWindow.dismiss()
        }
        if(this::popupMenu.isInitialized){
            popupMenu.dismiss()
        }
    }

    private fun applySearch(root: View) {
        if (root.txtSearchCustomer?.toString()?.length!! == 0) {
            resetToDefault()
            //isFromSearch = false
            isFromSearch = true
            callAccordingToTrack(searchCustApiTrackNo, currentPage, false)

        } else {
            if (root.txtSearchCustomer?.toString()?.length!! > 0) {
                searchCustApiTrackNo = "4"
                resetToDefault()
                isFromSearch = true
                callAccordingToTrack(searchCustApiTrackNo, currentPage, false)

            }
        }
    }

    override fun onResume() {
        super.onResume()
        val custSortType =
            object : TypeToken<String>() {}.type
        searchCustApiTrackNo = Gson().fromJson(
            prefs[Constants.PREF_CUST_SORT_TRACKNO, "6"],
            custSortType
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

                    viewDetail = true
                    adapter.viewDetail(viewDetail)
                }
            }
            resetToDefault()
            callAccordingToTrack(searchCustApiTrackNo, currentPage, true)
        }
        binding.txtSearchCustomer.clearFocus()

    }

    private fun defaultDisableAllButtonnUI() {
        binding.btnAddCustomerFloating.visibility = View.GONE
    }

    private fun defaultEnableAllButtonnUI() {
        binding.btnAddCustomerFloating.visibility = View.VISIBLE
    }

    private fun callAccordingToTrack(
        searchCustApiTrackNo: String,
        currentPage: Int,
        showLoading: Boolean
    ) {

        when (searchCustApiTrackNo) {
            "1" -> {
                // all customers
                binding.root.tvTitle.setText(getString(R.string.all_customers))
                status = "3"
                searchListCustomerAPI(
                    showLoading,
                    loginModel?.data?.bearer_access_token,
                    status,
                    currentPage,
                    binding.txtSearchCustomer.text.toString(),
                    sortByColumn,
                    sortType
                )
            }
            "2" -> {
                // active customers
                binding.root.tvTitle.setText(getString(R.string.active_customers))
                status = "1"
                searchListCustomerAPI(
                    showLoading,
                    loginModel?.data?.bearer_access_token,
                    status,
                    currentPage,
                    binding.txtSearchCustomer.text.toString(),
                    sortByColumn,
                    sortType
                )
            }
            "3" -> {
                //inactive customers
                status = "2"
                binding.root.tvTitle.setText(getString(R.string.inactive_customers))
                searchListCustomerAPI(
                    showLoading,
                    loginModel?.data?.bearer_access_token,
                    status,
                    currentPage,
                    binding.txtSearchCustomer.text.toString(),
                    sortByColumn,
                    sortType
                )
            }
            "4" -> searchListCustomerAPI(
                showLoading,
                loginModel?.data?.bearer_access_token,
                status,
                currentPage,
                binding.txtSearchCustomer.text.toString(),
                sortByColumn,
                sortType
            )
            "5" -> searchListCustomerAPI(
                showLoading,
                loginModel?.data?.bearer_access_token,
                status,
                currentPage,
                binding.txtSearchCustomer.text.toString(),
                sortByColumn,
                sortType
            )
            "6" -> {
                sortByField = getString(R.string.name); sortByAscDesc =
                    Constants.SORT_TYPE_ASCENDING
                sortByColumn = "name"
                sortType = "asc"
                searchListCustomerAPI(
                    showLoading,
                    loginModel?.data?.bearer_access_token,
                    status,
                    currentPage,
                    binding.txtSearchCustomer.text.toString(),
                    sortByColumn,
                    sortType
                )
            }
            "7" -> {
                sortByField = getString(R.string.name); sortByAscDesc =
                    Constants.SORT_TYPE_DESCENDING;
                sortByColumn = "name"
                sortType = "desc"
                searchListCustomerAPI(
                    showLoading,
                    loginModel?.data?.bearer_access_token,
                    status,
                    currentPage,
                    binding.txtSearchCustomer.text.toString(),
                    sortByColumn,
                    sortType
                )
            }
            "8" -> {
                sortByField = getString(R.string.o_s_fine_bal); sortByAscDesc =
                    Constants.SORT_TYPE_ASCENDING;
                sortByColumn = "fine_balance"
                sortType = "asc"
                searchListCustomerAPI(
                    showLoading,
                    loginModel?.data?.bearer_access_token,
                    status,
                    currentPage,
                    binding.txtSearchCustomer.text.toString(),
                    sortByColumn,
                    sortType
                )
            }
            "9" -> {
                sortByField = getString(R.string.o_s_fine_bal); sortByAscDesc =
                    Constants.SORT_TYPE_DESCENDING;
                sortByColumn = "fine_balance"
                sortType = "desc"
                searchListCustomerAPI(
                    showLoading,
                    loginModel?.data?.bearer_access_token,
                    status,
                    currentPage,
                    binding.txtSearchCustomer.text.toString(),
                    sortByColumn,
                    sortType
                )
            }
            "10" -> {
                sortByField = getString(R.string.o_s_cash_bal); sortByAscDesc =
                    Constants.SORT_TYPE_ASCENDING;
                sortByColumn = "cash_balance"
                sortType = "asc"
                searchListCustomerAPI(
                    showLoading,
                    loginModel?.data?.bearer_access_token,
                    status,
                    currentPage,
                    binding.txtSearchCustomer.text.toString(),
                    sortByColumn,
                    sortType
                )
            }
            "11" -> {
                sortByField = getString(R.string.o_s_cash_bal); sortByAscDesc =
                    Constants.SORT_TYPE_DESCENDING;
                sortByColumn = "cash_balance"
                sortType = "desc"
                searchListCustomerAPI(
                    showLoading,
                    loginModel?.data?.bearer_access_token,
                    status,
                    currentPage,
                    binding.txtSearchCustomer.text.toString(),
                    sortByColumn,
                    sortType
                )
            }
            "12" -> {
                sortByField = getString(R.string.last_transaction); sortByAscDesc =
                    Constants.SORT_TYPE_ASCENDING;
                sortByColumn = "transaction"
                sortType = "asc"
                searchListCustomerAPI(
                    showLoading,
                    loginModel?.data?.bearer_access_token,
                    status,
                    currentPage,
                    binding.txtSearchCustomer.text.toString(),
                    sortByColumn,
                    sortType
                )
            }
            "13" -> {
                sortByField = getString(R.string.last_transaction); sortByAscDesc =
                    Constants.SORT_TYPE_DESCENDING;
                sortByColumn = "transaction"
                sortType = "desc"
                searchListCustomerAPI(
                    showLoading,
                    loginModel?.data?.bearer_access_token,
                    status,
                    currentPage,
                    binding.txtSearchCustomer.text.toString(),
                    sortByColumn,
                    sortType
                )
            }
        }

    }

    fun resetToDefault() {
        itemCount = 0
        currentPage = PAGE_START
        isLastPage = false
        if (this::adapter.isInitialized)
            adapter.clear()
        isFromSearch = false
    }


    private fun retrieveList(
        customersList: List<SearchListCustomerModel.Data1037062284>?,
        pageSize: Int
    ) {

        if (currentPage != PAGE_START) adapter.removeLoading()
        //adapter.addCustomer(customersList)
        adapter.apply {
            addCustomer(customersList, isFromSearch, pageSize, currentPage, totalPage)
            notifyDataSetChanged()
        }

        binding.swipeContainerCustomer.setRefreshing(false)

        // check weather is last page or not
        if (currentPage < totalPage) {
            adapter.addLoading()
        } else {
            isLastPage = true
        }
        isLoading = false

        prefs[Constants.PREF_CUST_SORT_TRACKNO] =
            searchCustApiTrackNo //setter (for customer sort tracking while relaunching app)
    }

    private fun openSortingPopup() {

        val layoutInflater =
            requireContext()!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        var sortbinding: CustomerSortPopupBinding =
            DataBindingUtil.inflate(layoutInflater, R.layout.customer_sort_popup, null, false)
        val view = sortbinding.root
        sortingpopupWindow = PopupWindow(
            view,
            requireContext().resources.getDimension(R.dimen._200sdp).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )
        sortingpopupWindow.contentView = view
        sortingpopupWindow.setBackgroundDrawable(ColorDrawable(Color.WHITE));
        sortingpopupWindow.elevation = 20.0f
        sortingpopupWindow.showAsDropDown(imgRight2)
        //popupWindow.animationStyle = (R.style.CustomSortPopupWindow)

        refreshSortPopupView(sortbinding);

        highlightSortingColor(sortbinding)

        sortbinding.imgNameAscSortCust.clickWithDebounce {
            refreshSortPopupView(sortbinding); sortByField = getString(R.string.name); sortByAscDesc =
            Constants.SORT_TYPE_ASCENDING;
            (sortbinding.imgNameAscSortCust as ImageView).setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorPrimary
                )
            )
            searchCustApiTrackNo = "6"
            resetToDefault()
            callAccordingToTrack(searchCustApiTrackNo, currentPage, true)
        }

        sortbinding.imgNameDescSortCust.clickWithDebounce {
            refreshSortPopupView(sortbinding); sortByField = getString(R.string.name); sortByAscDesc =
            Constants.SORT_TYPE_DESCENDING;
            (sortbinding.imgNameDescSortCust as ImageView).setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorPrimary
                )
            )
            searchCustApiTrackNo = "7"
            resetToDefault()
            callAccordingToTrack(searchCustApiTrackNo, currentPage, true)
        }

        sortbinding.imgFineAscSortCust.clickWithDebounce {
            refreshSortPopupView(sortbinding); sortByField =
            getString(R.string.o_s_fine_bal); sortByAscDesc = Constants.SORT_TYPE_ASCENDING;
            (sortbinding.imgFineAscSortCust as ImageView).setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorPrimary
                )
            )
            searchCustApiTrackNo = "8"
            resetToDefault()
            callAccordingToTrack(searchCustApiTrackNo, currentPage, true)
        }

        sortbinding.imgFineDescSortCust.clickWithDebounce {
            refreshSortPopupView(sortbinding); sortByField =
            getString(R.string.o_s_fine_bal); sortByAscDesc = Constants.SORT_TYPE_DESCENDING;
            (sortbinding.imgFineDescSortCust as ImageView).setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorPrimary
                )
            )
            searchCustApiTrackNo = "9"
            resetToDefault()
            callAccordingToTrack(searchCustApiTrackNo, currentPage, true)
        }


        sortbinding.imgCashAscSortCust.clickWithDebounce {
            refreshSortPopupView(sortbinding); sortByField =
            getString(R.string.o_s_cash_bal); sortByAscDesc = Constants.SORT_TYPE_ASCENDING;
            (sortbinding.imgCashAscSortCust as ImageView).setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorPrimary
                )
            )
            searchCustApiTrackNo = "10"
            resetToDefault()
            callAccordingToTrack(searchCustApiTrackNo, currentPage, true)
        }

        sortbinding.imgCashDescSortCust.clickWithDebounce {
            refreshSortPopupView(sortbinding); sortByField =
            getString(R.string.o_s_cash_bal); sortByAscDesc = Constants.SORT_TYPE_DESCENDING;
            (sortbinding.imgCashDescSortCust as ImageView).setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorPrimary
                )
            )
            searchCustApiTrackNo = "11"
            resetToDefault()
            callAccordingToTrack(searchCustApiTrackNo, currentPage, true)
        }


        sortbinding.imgLastTranAscSortCust.clickWithDebounce {
            refreshSortPopupView(sortbinding); sortByField =
            getString(R.string.last_transaction); sortByAscDesc = Constants.SORT_TYPE_ASCENDING;
            (sortbinding.imgLastTranAscSortCust as ImageView).setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorPrimary
                )
            )
            searchCustApiTrackNo = "12"
            resetToDefault()
            callAccordingToTrack(searchCustApiTrackNo, currentPage, true)
        }

        sortbinding.imgLastTranDescSortCust.clickWithDebounce {

            refreshSortPopupView(sortbinding); sortByField =
            getString(R.string.last_transaction); sortByAscDesc = Constants.SORT_TYPE_DESCENDING;
            (sortbinding.imgLastTranDescSortCust as ImageView).setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorPrimary
                )
            )
            searchCustApiTrackNo = "13"
            resetToDefault()
            callAccordingToTrack(searchCustApiTrackNo, currentPage, true)
        }

        sortbinding.resetAllCustSort.clickWithDebounce {

            refreshSortPopupView(sortbinding);
            searchCustApiTrackNo = "6"
            resetToDefault()
            sortByField = getString(R.string.name); sortByAscDesc = Constants.SORT_TYPE_ASCENDING
            sortByColumn = "name"
            sortType = "asc"
            highlightSortingColor(sortbinding)
            if (binding.txtSearchCustomer.text.toString().length > 0)
                binding.txtSearchCustomer.setText("")
            callAccordingToTrack(searchCustApiTrackNo, currentPage, true)
        }


    }

    private fun highlightSortingColor(binding: CustomerSortPopupBinding) {
        if (sortByField == getString(R.string.name) && sortByAscDesc == Constants.SORT_TYPE_ASCENDING) {
            binding.imgNameAscSortCust.setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorPrimary
                )
            )
        } else if (sortByField == getString(R.string.name) && sortByAscDesc == Constants.SORT_TYPE_DESCENDING) {
            binding.imgNameDescSortCust.setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorPrimary
                )
            )
        } else if (sortByField == getString(R.string.o_s_fine_bal) && sortByAscDesc == Constants.SORT_TYPE_ASCENDING) {
            binding.imgFineAscSortCust.setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorPrimary
                )
            )
        } else if (sortByField == getString(R.string.o_s_fine_bal) && sortByAscDesc == Constants.SORT_TYPE_DESCENDING) {
            binding.imgFineDescSortCust.setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorPrimary
                )
            )
        } else if (sortByField == getString(R.string.o_s_cash_bal) && sortByAscDesc == Constants.SORT_TYPE_ASCENDING) {
            binding.imgCashAscSortCust.setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorPrimary
                )
            )
        } else if (sortByField == getString(R.string.o_s_cash_bal) && sortByAscDesc == Constants.SORT_TYPE_DESCENDING) {
            binding.imgCashDescSortCust.setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorPrimary
                )
            )
        } else if (sortByField == getString(R.string.last_transaction) && sortByAscDesc == Constants.SORT_TYPE_ASCENDING) {
            binding.imgLastTranAscSortCust.setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorPrimary
                )
            )
        } else if (sortByField == getString(R.string.last_transaction) && sortByAscDesc == Constants.SORT_TYPE_DESCENDING) {
            binding.imgLastTranDescSortCust.setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorPrimary
                )
            )
        }
    }

    fun refreshSortPopupView(binding: CustomerSortPopupBinding) {
        binding.imgNameAscSortCust.setColorFilter(
            ContextCompat.getColor(
                requireContext(),
                android.R.color.transparent
            )
        )
        binding.imgNameDescSortCust.setColorFilter(
            ContextCompat.getColor(
                requireContext(),
                android.R.color.transparent
            )
        )
        binding.imgFineAscSortCust.setColorFilter(
            ContextCompat.getColor(
                requireContext(),
                android.R.color.transparent
            )
        )
        binding.imgFineDescSortCust.setColorFilter(
            ContextCompat.getColor(
                requireContext(),
                android.R.color.transparent
            )
        )
        binding.imgCashAscSortCust.setColorFilter(
            ContextCompat.getColor(
                requireContext(),
                android.R.color.transparent
            )
        )
        binding.imgCashDescSortCust.setColorFilter(
            ContextCompat.getColor(
                requireContext(),
                android.R.color.transparent
            )
        )
        binding.imgLastTranAscSortCust.setColorFilter(
            ContextCompat.getColor(
                requireContext(),
                android.R.color.transparent
            )
        )
        binding.imgLastTranDescSortCust.setColorFilter(
            ContextCompat.getColor(
                requireContext(),
                android.R.color.transparent
            )
        )
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
            if (data.permission!!.get(i).startsWith(getString(R.string.customers))) {
                // Restriction check for Customer
                when (data.permission!!.get(i).endsWith(getString(R.string.details), true)) {
                    true -> {
                        viewDetail = true
                        adapter.viewDetail(viewDetail)
                    }else->{

                }
                }
            }
            if (data.permission!!.get(i).startsWith(getString(R.string.customers))) {
                // Restriction check for Customer
                when (data.permission!!.get(i).endsWith(getString(R.string.add_edit), true)) {
                    true -> {
                        binding.btnAddCustomerFloating.visibility = View.VISIBLE
                    }else->{

                }
                }
            }

        }
    }
    private fun setupAdapter() {
        adapter = CustomerListAdapter(arrayListOf(), totalPage)
        binding.recyclerViewCustomers.setHasFixedSize(true)
        binding.recyclerViewCustomers.adapter = adapter
        resetToDefault()
    }
    fun searchListCustomerAPI(
        showLoading: Boolean,
        token: String?,
        status: String?,
        currentPg: Int,
        searchName: String?,
        sort_by_column: String?,
        sort_type: String?
    ) {

        if (NetworkUtils.isConnected()) {

            viewModel.searchListCustomer(
                token,
                status,
                currentPage,
                searchName,
                sort_by_column,
                sort_type
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
                                        binding.recyclerViewCustomers.visibility = View.VISIBLE
                                        binding.tvNoRecordCustomer.visibility = View.GONE
                                    } else {
                                        binding.tvNoRecordCustomer.text = it.data.message
                                        binding.recyclerViewCustomers.visibility = View.GONE
                                        binding.tvNoRecordCustomer.visibility = View.VISIBLE
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
                                binding.root.pb_loader.visibility = View.GONE
                            }
                            Status.ERROR -> {
                                CommonUtils.hideProgress()

                            }
                            Status.LOADING -> {
                             //   Log.v("showLoading", showLoading.toString())
                                if (showLoading)
                                    CommonUtils.showProgress(requireContext())
                            }
                        }
                    }
                })
        }
    }
}


package com.goldbookapp.ui.fragment

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
import com.goldbookapp.databinding.FragmentSupplierBinding
import com.goldbookapp.model.GetListSupplierModel
import com.goldbookapp.model.LoginModel
import com.goldbookapp.model.UserWiseRestrictionModel
import com.goldbookapp.ui.MainActivity
import com.goldbookapp.ui.activity.supplier.NewSupplierActivity
import com.goldbookapp.ui.activity.viewmodel.SupplierViewModel
import com.goldbookapp.ui.activity.viewmodel.ViewModelFactory
import com.goldbookapp.ui.adapter.SupplierListAdapter
import com.goldbookapp.utils.*
import com.goldbookapp.utils.CommonUtils.Companion.clickWithDebounce
import com.goldbookapp.utils.PreferenceHelper.get
import com.goldbookapp.utils.PreferenceHelper.set
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.fragment_supplier.*
import kotlinx.android.synthetic.main.fragment_supplier.view.*
import kotlinx.android.synthetic.main.toolbar_layout.*
import kotlinx.android.synthetic.main.toolbar_layout.view.*

class SupplierFragment : Fragment() {

    lateinit var sortingpopupWindow: PopupWindow
    lateinit var popupMenu: PopupMenu

    private var viewDetail:Boolean = false
    private lateinit var viewModel: SupplierViewModel
    private var currentPage: Int = Constants.PAGE_START
    private var page_size: Int = Constants.PAGE_SIZE
    private var isLastPage = false
    private var totalPage = 1
    private var isLoading = false
    private var isFromSearch = false
    var itemCount = 0

    lateinit var binding: FragmentSupplierBinding
    private var searchSuppApiTrackNo: String = "6"
    private lateinit var adapter: SupplierListAdapter
    lateinit var prefs: SharedPreferences

    lateinit var loginModel: LoginModel
    private var status: String? = "3"
    //1,2,3 ( 1= Active, 2=Inactive, 3=All)

    var sortByField: String = ""
    var sortByAscDesc: String = ""
    var sortByColumn: String = ""
    var sortType: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_supplier, container, false)
        val view = binding.root

        setupViewModel()
        setupUIandListner(view)

        return view
    }

    private fun setupViewModel() {
        viewModel =
            ViewModelProvider(this, ViewModelFactory(ApiHelper(RetrofitBuilder.apiService))).get(
                SupplierViewModel::class.java
            )
        binding.setLifecycleOwner(this)

    }

    fun setupUIandListner(root: View) {

        prefs = PreferenceHelper.defaultPrefs(activity?.applicationContext!!)
        loginModel = Gson().fromJson(
            prefs[Constants.PREF_LOGIN_DETAIL_KEY, ""],
            LoginModel::class.java
        )

        //default sorting is name ascending
        sortByField = getString(R.string.name); sortByAscDesc = Constants.SORT_TYPE_ASCENDING
        sortByColumn = "name"
        sortType = "asc"

        root.imgLeft.setImageResource(R.drawable.ic_menu_black_24dp)
        root.tvTitle.setText(R.string.all_supplier)
        root.imgRight2.setImageResource(R.drawable.ic_sort)
        root.imgRight.setImageResource(R.drawable.ic_more)

        if (prefs.contains(Constants.PREF_BILLING_ADDRESS_KEY) && prefs.contains(Constants.PREF_SHIPPING_ADDRESS_KEY)) {
            prefs.edit().remove(Constants.PREF_BILLING_ADDRESS_KEY).apply()
            prefs.edit().remove(Constants.PREF_SHIPPING_ADDRESS_KEY).apply()
        }
        if (prefs.contains(Constants.PREF_TCS_TDS_SHARE_DATA)) {
            prefs.edit().remove(Constants.PREF_TCS_TDS_SHARE_DATA).apply()
        }


        root.imgLeft?.clickWithDebounce {
            (activity as MainActivity).openCloseDrawer()
        }
        root.btnaddSupplier?.clickWithDebounce {
            startActivity(Intent(requireContext(), NewSupplierActivity::class.java))
        }
        root.imgRight2?.clickWithDebounce {
            openSortingPopup()
        }

        root.recyclerViewSupplier.layoutManager = LinearLayoutManager(activity)
        root.recyclerViewSupplier.addOnScrollListener(object :
            PaginationListener(root.recyclerViewSupplier.layoutManager as LinearLayoutManager) {
            override fun loadMoreItems() {
                isLoading = true
                currentPage++

                callAccordingToTrack(searchSuppApiTrackNo, currentPage, false)
            }

            override fun isLastPage(): Boolean {
                return isLastPage
            }

            override fun isLoading(): Boolean {
                return isLoading
            }
        })


        root.imgRight.clickWithDebounce {

           popupMenu = PopupMenu(requireContext(), binding.root.imgRight)
            context?.let { it1 -> CommonUtils.applyFontToMenu(popupMenu.menu, it1) }
            popupMenu.menuInflater.inflate(R.menu.popup_menu_supplier, popupMenu.menu)
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
                    R.id.actionAllSupplier -> {
                        status = "3"
                        searchSuppApiTrackNo = "1"
                        binding.root.tvTitle.setText(R.string.all_supplier)
                        //isFromSearch = false
                        resetToDefault()
                        callAccordingToTrack(searchSuppApiTrackNo, currentPage, true)

                    }


                    R.id.actionActiveSupplier -> {
                        status = "1"
                        searchSuppApiTrackNo = "2"
                        binding.root.tvTitle.setText(R.string.active_suppiler)
                        //isFromSearch = false
                        resetToDefault()
                        callAccordingToTrack(searchSuppApiTrackNo, currentPage, true)

                    }

                    R.id.actionInActiveSupplier -> {
                        status = "2"
                        searchSuppApiTrackNo = "3"
                        binding.root.tvTitle.setText(R.string.inactive_suppiler)
                        //isFromSearch = false
                        resetToDefault()
                        callAccordingToTrack(searchSuppApiTrackNo, currentPage, true)

                    }
                }
                true
            })
            popupMenu.show()
        }


        root.txtSearchSupplier.setOnDebounceTextWatcher(lifecycle) { input ->
            if (NetworkUtils.isConnected()) {
                applySearch(root)
                binding.root.pb_loader_Supp.visibility = View.VISIBLE
            }
        }

        // search from drawable left/right button
        root.txtSearchSupplier.setDrawableClickListener(object : DrawableClickListener {
            override fun onClick(target: DrawableClickListener.DrawablePosition?) {
                if (CommonUtils.isValidClickPressed()) {
                    when (target) {
                        DrawableClickListener.DrawablePosition.LEFT -> {
                            if (NetworkUtils.isConnected()) {
                                applySearch(root)
                                binding.root.pb_loader_Supp.visibility = View.VISIBLE
                            }
                        }
                        DrawableClickListener.DrawablePosition.RIGHT -> {
                            root.txtSearchSupplier.text!!.clear()

                        }else->{

                    }
                    }
                }
            }
        })
        // search from keypad
        root.txtSearchSupplier.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                // Call your code here
                if (NetworkUtils.isConnected()) {
                    applySearch(root)
                    binding.root.pb_loader_Supp.visibility = View.VISIBLE
                }
                true
            }
            false
        }
        binding.root.swipeContainerSupplier.setColorSchemeResources(R.color.colorAccent)
        binding.root.swipeContainerSupplier.setOnRefreshListener {
            resetToDefault()

            callAccordingToTrack(searchSuppApiTrackNo, currentPage, true)

            binding.swipeContainerSupplier.isRefreshing = false
        }
        setupAdapter()
    }

    private fun applySearch(root: View) {
        if (root.txtSearchSupplier?.toString()?.length!! == 0) {
            resetToDefault()
            isFromSearch = true
            callAccordingToTrack(searchSuppApiTrackNo, currentPage, false)
            root.txtSearchSupplier.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_search_black_24dp,
                0,
                R.drawable.ic_cancel_blank_24dp,
                0
            )
        } else {
            if (root.txtSearchSupplier?.toString()?.length!! > 0) {
                searchSuppApiTrackNo = "4"
                resetToDefault()
                isFromSearch = true
                callAccordingToTrack(searchSuppApiTrackNo, currentPage, false)
                root.txtSearchSupplier.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_search_black_24dp,
                    0,
                    0,
                    0
                )
            }
        }


    }

    override fun onResume() {
        super.onResume()

        val suppSortType =
            object : TypeToken<String>() {}.type
        searchSuppApiTrackNo = Gson().fromJson(
            prefs[Constants.PREF_SUPPL_SORT_TRACKNO, "6"],
            suppSortType
        ) //getter to maintain track of user with sorting option
        //callAccordingToTrack(searchSuppApiTrackNo)
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

                }
            }
            resetToDefault()
            callAccordingToTrack(searchSuppApiTrackNo, currentPage, true)
        }
        binding.txtSearchSupplier.clearFocus()
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


    private fun defaultDisableAllButtonnUI() {
        binding.btnaddSupplier.visibility = View.GONE
    }

    private fun defaultEnableAllButtonnUI() {
        binding.btnaddSupplier.visibility = View.VISIBLE
        viewDetail = true
        adapter.viewDetail(viewDetail)
    }

    private fun setupAdapter() {
        adapter = SupplierListAdapter(arrayListOf(), totalPage)
        binding.recyclerViewSupplier.setHasFixedSize(true)
        binding.recyclerViewSupplier.adapter = adapter
        resetToDefault()
    }

    fun resetToDefault() {
        itemCount = 0
        currentPage = Constants.PAGE_START
        isLastPage = false
        if (this::adapter.isInitialized)
            adapter.clear()
        isFromSearch = false
    }



    private fun callAccordingToTrack(
        searchSuppApiTrackNo: String,
        currentPage: Int,
        showLoading: Boolean
    ) {
        when (searchSuppApiTrackNo) {
            "1" -> {
                // all suppliers
                tvTitle.setText(R.string.all_supplier)
                status = "3"
                getListSupplierAPI(
                    showLoading,
                    loginModel?.data?.bearer_access_token,
                    status,
                    currentPage,
                    binding.txtSearchSupplier.text.toString(),
                    sortByColumn,
                    sortType
                )
            }
            "2" -> {
                // active suppliers
                tvTitle.setText(R.string.active_suppiler)
                status = "1"
                getListSupplierAPI(
                    showLoading,
                    loginModel?.data?.bearer_access_token,
                    status,
                    currentPage,
                    binding.txtSearchSupplier.text.toString(),
                    sortByColumn,
                    sortType
                )
            }
            "3" -> {
                //inactive suppliers
                tvTitle.setText(R.string.inactive_suppiler)
                status = "2"
                getListSupplierAPI(
                    showLoading,
                    loginModel?.data?.bearer_access_token,
                    status,
                    currentPage,
                    binding.txtSearchSupplier.text.toString(),
                    sortByColumn,
                    sortType
                )
            }
            "4" -> getListSupplierAPI(
                showLoading,
                loginModel?.data?.bearer_access_token,
                status,
                currentPage,
                binding.txtSearchSupplier.text.toString(),
                sortByColumn,
                sortType
            )
            "5" -> getListSupplierAPI(
                showLoading,
                loginModel?.data?.bearer_access_token,
                status,
                currentPage,
                binding.txtSearchSupplier.text.toString(),
                sortByColumn,
                sortType
            )
            "6" -> {
                sortByField = getString(R.string.name); sortByAscDesc =
                    Constants.SORT_TYPE_ASCENDING
                sortByColumn = "name"
                sortType = "asc"
                getListSupplierAPI(
                    showLoading,
                    loginModel?.data?.bearer_access_token,
                    status,
                    currentPage,
                    binding.txtSearchSupplier.text.toString(),
                    sortByColumn,
                    sortType
                )
            }
            "7" -> {
                sortByField = getString(R.string.name); sortByAscDesc =
                    Constants.SORT_TYPE_DESCENDING;
                sortByColumn = "name"
                sortType = "desc"
                getListSupplierAPI(
                    showLoading,
                    loginModel?.data?.bearer_access_token,
                    status,
                    currentPage,
                    binding.txtSearchSupplier.text.toString(),
                    sortByColumn,
                    sortType
                )
            }
            "8" -> {
                sortByField = getString(R.string.o_s_fine_bal); sortByAscDesc =
                    Constants.SORT_TYPE_ASCENDING;
                sortByColumn = "fine_balance"
                sortType = "asc"
                getListSupplierAPI(
                    showLoading,
                    loginModel?.data?.bearer_access_token,
                    status,
                    currentPage,
                    binding.txtSearchSupplier.text.toString(),
                    sortByColumn,
                    sortType
                )
            }
            "9" -> {
                sortByField = getString(R.string.o_s_fine_bal); sortByAscDesc =
                    Constants.SORT_TYPE_DESCENDING;
                sortByColumn = "fine_balance"
                sortType = "desc"
                getListSupplierAPI(
                    showLoading,
                    loginModel?.data?.bearer_access_token,
                    status,
                    currentPage,
                    binding.txtSearchSupplier.text.toString(),
                    sortByColumn,
                    sortType
                )
            }
            "10" -> {
                sortByField = getString(R.string.o_s_cash_bal); sortByAscDesc =
                    Constants.SORT_TYPE_ASCENDING;
                sortByColumn = "cash_balance"
                sortType = "asc"
                getListSupplierAPI(
                    showLoading,
                    loginModel?.data?.bearer_access_token,
                    status,
                    currentPage,
                    binding.txtSearchSupplier.text.toString(),
                    sortByColumn,
                    sortType
                )
            }
            "11" -> {
                sortByField = getString(R.string.o_s_cash_bal); sortByAscDesc =
                    Constants.SORT_TYPE_DESCENDING;
                sortByColumn = "cash_balance"
                sortType = "desc"
                getListSupplierAPI(
                    showLoading,
                    loginModel?.data?.bearer_access_token,
                    status,
                    currentPage,
                    binding.txtSearchSupplier.text.toString(),
                    sortByColumn,
                    sortType
                )
            }
            "12" -> {
                sortByField = getString(R.string.last_transaction); sortByAscDesc =
                    Constants.SORT_TYPE_ASCENDING;
                sortByColumn = "transaction"
                sortType = "asc"
                getListSupplierAPI(
                    showLoading,
                    loginModel?.data?.bearer_access_token,
                    status,
                    currentPage,
                    binding.txtSearchSupplier.text.toString(),
                    sortByColumn,
                    sortType
                )
            }
            "13" -> {
                sortByField = getString(R.string.last_transaction); sortByAscDesc =
                    Constants.SORT_TYPE_DESCENDING;
                sortByColumn = "transaction"
                sortType = "desc"
                getListSupplierAPI(
                    showLoading,
                    loginModel?.data?.bearer_access_token,
                    status,
                    currentPage,
                    binding.txtSearchSupplier.text.toString(),
                    sortByColumn,
                    sortType
                )
            }
        }

    }

    private fun retrieveList(
        supplierList: List<GetListSupplierModel.Data344525142>?,
        pageSize: Int
    ) {

        if (currentPage != Constants.PAGE_START) adapter.removeLoading()
        //adapter.addCustomer(customersList)
        adapter.apply {
            addSupplier(supplierList, isFromSearch, pageSize, currentPage, totalPage)
            notifyDataSetChanged()
        }

        binding.swipeContainerSupplier.setRefreshing(false)

        // check weather is last page or not
        if (currentPage < totalPage) {
            adapter.addLoading()
        } else {
            isLastPage = true
        }
        isLoading = false

        prefs[Constants.PREF_SUPPL_SORT_TRACKNO] =
            searchSuppApiTrackNo //setter (for customer sort tracking while relaunching app)
    }

    private fun openSortingPopup() {

        val layoutInflater =
            requireContext()!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        var binding: CustomerSortPopupBinding =
            DataBindingUtil.inflate(layoutInflater, R.layout.customer_sort_popup, null, false)
        val view = binding.root
        sortingpopupWindow = PopupWindow(
            view,
            this.resources.getDimension(R.dimen._200sdp).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )
        sortingpopupWindow.contentView = view
        sortingpopupWindow.setBackgroundDrawable(ColorDrawable(Color.WHITE));
        sortingpopupWindow.elevation = 20.0f
        sortingpopupWindow.showAsDropDown(imgRight2)
        //popupWindow.animationStyle = (R.style.CustomSortPopupWindow)

        refreshSortPopupView(binding);

        highlightSortingColor(binding)

        binding.imgNameAscSortCust.clickWithDebounce {
            refreshSortPopupView(binding); sortByField = getString(R.string.name); sortByAscDesc =
            Constants.SORT_TYPE_ASCENDING;
            (binding.imgNameAscSortCust as ImageView).setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorPrimary
                )
            )
            searchSuppApiTrackNo = "6"
            resetToDefault()
            callAccordingToTrack(searchSuppApiTrackNo, currentPage, true)
        }

        binding.imgNameDescSortCust.clickWithDebounce {
            refreshSortPopupView(binding); sortByField = getString(R.string.name); sortByAscDesc =
            Constants.SORT_TYPE_DESCENDING;
            (binding.imgNameDescSortCust as ImageView).setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorPrimary
                )
            )
            searchSuppApiTrackNo = "7"
            resetToDefault()
            callAccordingToTrack(searchSuppApiTrackNo, currentPage, true)
        }


        binding.imgFineAscSortCust.clickWithDebounce {
            refreshSortPopupView(binding); sortByField =
            getString(R.string.o_s_fine_bal); sortByAscDesc = Constants.SORT_TYPE_ASCENDING;
            (binding.imgFineAscSortCust as ImageView).setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorPrimary
                )
            )
            searchSuppApiTrackNo = "8"
            resetToDefault()
            callAccordingToTrack(searchSuppApiTrackNo, currentPage, true)
        }

        binding.imgFineDescSortCust.clickWithDebounce {
            refreshSortPopupView(binding); sortByField =
            getString(R.string.o_s_fine_bal); sortByAscDesc = Constants.SORT_TYPE_DESCENDING;
            (binding.imgFineDescSortCust as ImageView).setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorPrimary
                )
            )
            searchSuppApiTrackNo = "9"
            resetToDefault()
            callAccordingToTrack(searchSuppApiTrackNo, currentPage, true)
        }


        binding.imgCashAscSortCust.clickWithDebounce {
            refreshSortPopupView(binding); sortByField =
            getString(R.string.o_s_cash_bal); sortByAscDesc = Constants.SORT_TYPE_ASCENDING;
            (binding.imgCashAscSortCust as ImageView).setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorPrimary
                )
            )
            searchSuppApiTrackNo = "10"
            resetToDefault()
            callAccordingToTrack(searchSuppApiTrackNo, currentPage, true)
        }

        binding.imgCashDescSortCust.clickWithDebounce {
            refreshSortPopupView(binding); sortByField =
            getString(R.string.o_s_cash_bal); sortByAscDesc = Constants.SORT_TYPE_DESCENDING;
            (binding.imgCashDescSortCust as ImageView).setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorPrimary
                )
            )
            searchSuppApiTrackNo = "11"
            resetToDefault()
            callAccordingToTrack(searchSuppApiTrackNo, currentPage, true)
        }


        binding.imgLastTranAscSortCust.clickWithDebounce {
            refreshSortPopupView(binding); sortByField =
            getString(R.string.last_transaction); sortByAscDesc = Constants.SORT_TYPE_ASCENDING;
            (binding.imgLastTranAscSortCust as ImageView).setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorPrimary
                )
            )
            searchSuppApiTrackNo = "12"
            resetToDefault()
            callAccordingToTrack(searchSuppApiTrackNo, currentPage, true)
        }

        binding.imgLastTranDescSortCust.clickWithDebounce {
            refreshSortPopupView(binding); sortByField =
            getString(R.string.last_transaction); sortByAscDesc = Constants.SORT_TYPE_DESCENDING;
            (binding.imgLastTranDescSortCust as ImageView).setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorPrimary
                )
            )
            searchSuppApiTrackNo = "13"
            resetToDefault()
            callAccordingToTrack(searchSuppApiTrackNo, currentPage, true)
        }

        binding.resetAllCustSort.clickWithDebounce {
            refreshSortPopupView(binding);
            searchSuppApiTrackNo = "6"
            resetToDefault()
            sortByField = getString(R.string.name); sortByAscDesc = Constants.SORT_TYPE_ASCENDING
            sortByColumn = "name"
            sortType = "asc"
            highlightSortingColor(binding)
            if (txtSearchSupplier.text.toString().length > 0)
                txtSearchSupplier.setText("")
            callAccordingToTrack(searchSuppApiTrackNo, currentPage, true)
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
            if (data.permission!!.get(i).startsWith(getString(R.string.supp))) {
                // Restriction check for Customer
                when (data.permission!!.get(i).endsWith(getString(R.string.details), true)) {
                    true -> {
                        viewDetail = true
                        adapter.viewDetail(viewDetail)
                    }else->{

                }
                }
            }
            if (data.permission!!.get(i).startsWith(getString(R.string.supp))) {
                // Restriction check for Customer
                when (data.permission!!.get(i).endsWith(getString(R.string.add_edit), true)) {
                    true -> {
                        binding.btnaddSupplier.visibility = View.VISIBLE
                    }else->{

                }
                }
            }

        }
    }


    fun getListSupplierAPI(
        showLoading: Boolean,
        token: String?,
        status: String?,
        currentPg: Int,
        searchName: String?,
        sort_by_column: String?,
        sort_type: String?
    ) {

        if (NetworkUtils.isConnected()) {

            viewModel.getListSupplier(
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
                                    retrieveList(it.data.data, page_size)

                                    //val nums = listOf(it.data?.data)
                                    //val listSize: Int? = it.data?.data?.size

                                    if (it.data.data.isNotEmpty()) {
                                        binding.recyclerViewSupplier.visibility = View.VISIBLE
                                        binding.tvNoRecordSupplier.visibility = View.GONE
                                    } else {
                                        binding.tvNoRecordSupplier.text = it.data.message

                                        binding.recyclerViewSupplier.visibility = View.GONE
                                        binding.tvNoRecordSupplier.visibility = View.VISIBLE
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
                                binding.root.pb_loader_Supp.visibility = View.GONE
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

}
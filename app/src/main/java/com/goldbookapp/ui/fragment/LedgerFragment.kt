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
import com.goldbookapp.databinding.FragmentLeadgerBinding
import com.goldbookapp.databinding.LedgerSortPopupBinding
import com.goldbookapp.model.LoginModel
import com.goldbookapp.model.SearchListLedgerModel
import com.goldbookapp.model.UserWiseRestrictionModel
import com.goldbookapp.ui.MainActivity
import com.goldbookapp.ui.activity.ledger.NewLedgerActivity
import com.goldbookapp.ui.activity.viewmodel.ViewModelFactory
import com.goldbookapp.ui.adapter.LedgerListAdapter
import com.goldbookapp.ui.fragment.viewmodel.LedgerViewModel
import com.goldbookapp.utils.*
import com.goldbookapp.utils.CommonUtils.Companion.clickWithDebounce
import com.goldbookapp.utils.CommonUtils.Companion.isValidClickPressed
import com.goldbookapp.utils.PreferenceHelper.get
import com.goldbookapp.utils.PreferenceHelper.set
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.fragment_leadger.view.*
import kotlinx.android.synthetic.main.ledger_sort_popup.view.*
import kotlinx.android.synthetic.main.toolbar_layout.*
import kotlinx.android.synthetic.main.toolbar_layout.view.*

class LedgerFragment : Fragment() {

    lateinit var popupWindow: PopupWindow
    private var viewDetail:Boolean = false
    private lateinit var ledgerViewModel: LedgerViewModel

    private var currentPage: Int = Constants.PAGE_START
    private var isLastPage = false
    private var isFromSearch = false
    private var page_size: Int = Constants.PAGE_SIZE
    private var totalPage = 1
    var itemCount = 0
    private var isLoading = false

    lateinit var prefs: SharedPreferences
    lateinit var binding: FragmentLeadgerBinding

    private lateinit var adapter: LedgerListAdapter
    lateinit var loginModel: LoginModel

    var sortByField: String = ""
    var sortByAscDesc: String = ""
    var sortByColumn: String = ""
    var sortType: String = ""
    private var searchLedgerApiTrackNo: String = "4"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_leadger, container, false)
        val view = binding.root

        setupUIandListner(view)
        setupViewModel()

        return view
    }

    private fun setupViewModel() {
        ledgerViewModel =
            ViewModelProvider(this, ViewModelFactory(ApiHelper(RetrofitBuilder.apiService))).get(
                LedgerViewModel::class.java
            )
        binding.setLifecycleOwner(this)
    }


    fun setupUIandListner(root: View) {

        prefs = PreferenceHelper.defaultPrefs(activity?.applicationContext!!)
        loginModel = Gson().fromJson(
            prefs[Constants.PREF_LOGIN_DETAIL_KEY, ""],
            LoginModel::class.java
        ) //getter

        if (prefs.contains(Constants.PREF_CHEQUE_BOOK_KEY)) {
            prefs.edit().remove(Constants.PREF_CHEQUE_BOOK_KEY).apply()
        }
        if (prefs.contains(Constants.PREF_CHEQUE_BOOK_EDITKEY)) {
            prefs.edit().remove(Constants.PREF_CHEQUE_BOOK_EDITKEY).apply()
        }
        //default sorting is ledger name desc
        sortByField = getString(R.string.ledgerName); sortByAscDesc =
            Constants.SORT_TYPE_DESCENDING;
        sortByColumn = "ledger_name"
        sortType = "desc"

        binding.root.imgLeft.setImageResource(R.drawable.ic_menu_black_24dp)
        binding.root.tvTitle.setText(getString(R.string.ledger))
        binding.root.imgRight.setImageResource(R.drawable.ic_sort)


        root.imgLeft.clickWithDebounce {
            (activity as MainActivity).openCloseDrawer()
        }
        root.imgRight.clickWithDebounce {
            openSortingPopup()
        }
        root.floatingaddLedger.clickWithDebounce {
            startActivity(Intent(activity, NewLedgerActivity::class.java))
        }

        root.recyclerViewLedger.layoutManager = LinearLayoutManager(activity)
        root.recyclerViewLedger.addOnScrollListener(object :
            PaginationListener(root.recyclerViewLedger.layoutManager as LinearLayoutManager) {
            override fun loadMoreItems() {
                isLoading = true
                currentPage++

                callAccordingToTrack(searchLedgerApiTrackNo, currentPage, false)
            }

            override fun isLastPage(): Boolean {
                return isLastPage
            }

            override fun isLoading(): Boolean {
                return isLoading
            }
        })

        root.isClickable = true
        root.isFocusable = true
        root.isFocusableInTouchMode = true
        root.setOnFocusChangeListener { v, hasFocus -> CommonUtils.hideKeyboardnew(requireActivity()) }


        root.txtSearchLedger.setOnDebounceTextWatcher(lifecycle) { input ->
            if (NetworkUtils.isConnected()) {
                applySearch(root)
                binding.root.pb_loader_ledger.visibility = View.VISIBLE
            }
        }

        root.txtSearchLedger.setDrawableClickListener(object : DrawableClickListener {
            override fun onClick(target: DrawableClickListener.DrawablePosition?) {
                if (isValidClickPressed()) {
                when (target) {
                    DrawableClickListener.DrawablePosition.LEFT -> {
                        if (NetworkUtils.isConnected()) {
                            applySearch(root)
                            binding.root.pb_loader_ledger.visibility = View.VISIBLE
                        }

                    }
                    DrawableClickListener.DrawablePosition.RIGHT -> {
                        root.txtSearchLedger.text!!.clear()
                    }else->{

                }
                }
                }
            }
        })
        root.txtSearchLedger.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                // Call your code here
                if (NetworkUtils.isConnected()) {
                    applySearch(root)
                    binding.root.pb_loader_ledger.visibility = View.VISIBLE
                }

                true
            }
            false
        }

        binding.root.swipeContainerLedger.setColorSchemeResources(R.color.colorAccent)
        binding.root.swipeContainerLedger.setOnRefreshListener {
            resetToDefault()
            callAccordingToTrack(searchLedgerApiTrackNo, currentPage, true)
            binding.root.swipeContainerLedger.isRefreshing = false
        }
        setupAdapter()
    }

    private fun setupAdapter() {
         adapter = LedgerListAdapter(arrayListOf(), totalPage)
        binding.recyclerViewLedger.setHasFixedSize(true)
        binding.recyclerViewLedger.adapter = adapter
        resetToDefault()
    }

    private fun applySearch(root: View) {
        if (root.txtSearchLedger?.toString()?.length!! == 0) {
            resetToDefault()
            //isFromSearch = false
            searchLedgerApiTrackNo = "1"
            callAccordingToTrack(searchLedgerApiTrackNo, currentPage, false)
            root.txtSearchLedger.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_search_black_24dp,
                0,
                R.drawable.ic_cancel_blank_24dp,
                0
            )

        } else {
            if (root.txtSearchLedger?.toString()?.length!! > 0) {
                searchLedgerApiTrackNo = "2"
                resetToDefault()
                isFromSearch = true
                callAccordingToTrack(searchLedgerApiTrackNo, currentPage, false)
                root.txtSearchLedger.setCompoundDrawablesWithIntrinsicBounds(
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

        val ledgerSortType =
            object : TypeToken<String>() {}.type
        searchLedgerApiTrackNo = Gson().fromJson(
            prefs[Constants.PREF_LEDGER_SORT_TRACKNO, "4"],
            ledgerSortType
        )

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
            callAccordingToTrack(searchLedgerApiTrackNo, currentPage, true)
        }
        binding.txtSearchLedger.clearFocus()

    }

    override fun onPause() {
        super.onPause()

        if (this::popupWindow.isInitialized) {
            popupWindow.dismiss()
        }
    }

    private fun defaultDisableAllButtonnUI() {
        binding.floatingaddLedger.visibility = View.GONE
    }

    private fun defaultEnableAllButtonnUI() {
        binding.floatingaddLedger.visibility = View.VISIBLE
        viewDetail = true
        adapter.viewDetail(viewDetail)
    }

    private fun userWiseRestriction(token: String?) {
        if (NetworkUtils.isConnected()) {
            ledgerViewModel.userWiseRestriction(token)
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
            if (data.permission!!.get(i).startsWith(getString(R.string.ledgr))) {
                // Restriction check for Customer
                when (data.permission!!.get(i).endsWith(getString(R.string.details), true)) {
                    true -> {
                        viewDetail = true
                        adapter.viewDetail(viewDetail)
                    }else->{

                }
                }
            }
            if (data.permission!!.get(i).startsWith(getString(R.string.ledgr))) {
                // Restriction check for Customer
                when (data.permission!!.get(i).endsWith(getString(R.string.add_edit), true)) {
                    true -> {
                        binding.floatingaddLedger.visibility = View.VISIBLE
                    }else->{

                }
                }
            }

        }
    }

    private fun callAccordingToTrack(
        searchLedgerApiTrackNo: String,
        currentPage: Int,
        showLoading: Boolean
    ) {
        when (searchLedgerApiTrackNo) {
            "1" -> {
                searchListLedgerAPI(
                    showLoading,
                    loginModel?.data?.bearer_access_token,
                    loginModel?.data?.company_info?.id,
                    currentPage,
                    binding.txtSearchLedger.text.toString(),
                    sortByColumn,
                    sortType
                )
            }
            "2" -> {
                searchListLedgerAPI(
                    showLoading,
                    loginModel?.data?.bearer_access_token,
                    loginModel?.data?.company_info?.id,
                    currentPage,
                    binding.txtSearchLedger.text.toString(),
                    sortByColumn,
                    sortType
                )
            }

            "3" -> {
                sortByField = getString(R.string.ledgerName); sortByAscDesc =
                    Constants.SORT_TYPE_ASCENDING;
                sortByColumn = "ledger_name"
                sortType = "asc"
                searchListLedgerAPI(
                    showLoading,
                    loginModel?.data?.bearer_access_token,
                    loginModel?.data?.company_info?.id,
                    currentPage,
                    binding.txtSearchLedger.text.toString(),
                    sortByColumn,
                    sortType
                )
            }
            "4" -> {
                sortByField = getString(R.string.ledgerName); sortByAscDesc =
                    Constants.SORT_TYPE_DESCENDING;
                sortByColumn = "ledger_name"
                sortType = "desc"
                searchListLedgerAPI(
                    showLoading,
                    loginModel?.data?.bearer_access_token,
                    loginModel?.data?.company_info?.id,
                    currentPage,
                    binding.txtSearchLedger.text.toString(),
                    sortByColumn,
                    sortType
                )
            }
            "5" -> {
                sortByField = getString(R.string.lasttransDate); sortByAscDesc =
                    Constants.SORT_TYPE_ASCENDING;
                sortByColumn = "date"
                sortType = "asc"
                searchListLedgerAPI(
                    showLoading,
                    loginModel?.data?.bearer_access_token,
                    loginModel?.data?.company_info?.id,
                    currentPage,
                    binding.txtSearchLedger.text.toString(),
                    sortByColumn,
                    sortType
                )
            }
            "6" -> {
                sortByField = getString(R.string.lasttransDate); sortByAscDesc =
                    Constants.SORT_TYPE_DESCENDING;
                sortByColumn = "date"
                sortType = "desc"
                searchListLedgerAPI(
                    showLoading,
                    loginModel?.data?.bearer_access_token,
                    loginModel?.data?.company_info?.id,
                    currentPage,
                    binding.txtSearchLedger.text.toString(),
                    sortByColumn,
                    sortType
                )
            }

        }

    }


    fun searchListLedgerAPI(
        showLoading: Boolean,
        token: String?,
        companyID: String?,
        curret_page: Int?,
        searchName: String?,
        sort_by_column: String?,
        sort_type: String?
    ) {

        if (NetworkUtils.isConnected()) {

            ledgerViewModel.searchListLedger(
                token,
                companyID,
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
                                        binding.recyclerViewLedger.visibility = View.VISIBLE
                                        binding.tvNoRecordLedger.visibility = View.GONE
                                    } else {
                                        binding.tvNoRecordLedger.text = it.data.message

                                        binding.recyclerViewLedger.visibility = View.GONE
                                        binding.tvNoRecordLedger.visibility = View.VISIBLE
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
                                binding.root.pb_loader_ledger.visibility = View.GONE
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


    private fun retrieveList(ledgerList: List<SearchListLedgerModel.DataLedger>?, pageSize: Int) {

        if (currentPage != Constants.PAGE_START) adapter.removeLoading()
        //adapter.addCustomer(customersList)
        adapter.apply {
            addLedger(
                ledgerList as ArrayList<SearchListLedgerModel.DataLedger>?,
                isFromSearch,
                pageSize,
                currentPage,
                totalPage
            )
            notifyDataSetChanged()
        }

        binding.swipeContainerLedger.setRefreshing(false)

        if (currentPage < totalPage) {
            adapter.addLoading()
        } else {
            isLastPage = true
        }
        isLoading = false
        prefs[Constants.PREF_LEDGER_SORT_TRACKNO] =
            searchLedgerApiTrackNo //setter (for ledger sort tracking while relaunching app)
    }

    private fun openSortingPopup() {

        val layoutInflater =
            requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val sortbinding: LedgerSortPopupBinding =
            DataBindingUtil.inflate(layoutInflater, R.layout.ledger_sort_popup, null, false)
        val view = sortbinding.root
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
        view.sortTypeName.setText(R.string.ledgerName)
        view.sortType.setText(R.string.lasttransDate)

        refreshSortPopupView(sortbinding)

        highlightSortingColor(sortbinding)

        sortbinding.nameAscSortLedger.clickWithDebounce {
            refreshSortPopupView(sortbinding); sortByField =
            getString(R.string.ledgerName); sortByAscDesc = Constants.SORT_TYPE_ASCENDING;
            (sortbinding.nameAscSortLedger as ImageView).setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorPrimary
                )
            )
            searchLedgerApiTrackNo = "3"
            resetToDefault()
            callAccordingToTrack(searchLedgerApiTrackNo, currentPage, true)
        }

        sortbinding.nameDescSortLedger.clickWithDebounce {
            refreshSortPopupView(sortbinding); sortByField =
            getString(R.string.ledgerName); sortByAscDesc = Constants.SORT_TYPE_DESCENDING;
            (sortbinding.nameDescSortLedger as ImageView).setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorPrimary
                )
            )
            searchLedgerApiTrackNo = "4"
            resetToDefault()
            callAccordingToTrack(searchLedgerApiTrackNo, currentPage, true)
        }

        sortbinding.transAscSortLedger.clickWithDebounce {
            refreshSortPopupView(sortbinding); sortByField =
            getString(R.string.lasttransDate); sortByAscDesc = Constants.SORT_TYPE_ASCENDING;
            (sortbinding.transAscSortLedger as ImageView).setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorPrimary
                )
            )
            searchLedgerApiTrackNo = "5"
            resetToDefault()
            callAccordingToTrack(searchLedgerApiTrackNo, currentPage, true)
        }

        sortbinding.transDescSortLedger.clickWithDebounce {
            refreshSortPopupView(sortbinding); sortByField =
            getString(R.string.lasttransDate); sortByAscDesc = Constants.SORT_TYPE_DESCENDING;
            (sortbinding.transDescSortLedger as ImageView).setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorPrimary
                )
            )
            searchLedgerApiTrackNo = "6"
            resetToDefault()
            callAccordingToTrack(searchLedgerApiTrackNo, currentPage, true)
        }
        sortbinding.resetAllLedgerSort.clickWithDebounce {

            refreshSortPopupView(sortbinding);
            searchLedgerApiTrackNo = "4"
            resetToDefault()
            highlightSortingColor(sortbinding)
            if (binding.root.txtSearchLedger.text.toString().length > 0)
                binding.root.txtSearchLedger.setText("")
            callAccordingToTrack(searchLedgerApiTrackNo, currentPage, true)
        }



    }

    private fun highlightSortingColor(binding: LedgerSortPopupBinding) {
        if (sortByField == getString(R.string.ledgerName) && sortByAscDesc == Constants.SORT_TYPE_ASCENDING) {
            binding.nameAscSortLedger.setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorPrimary
                )
            )
        } else if (sortByField == getString(R.string.ledgerName) && sortByAscDesc == Constants.SORT_TYPE_DESCENDING) {
            binding.nameDescSortLedger.setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorPrimary
                )
            )
        } else if (sortByField == getString(R.string.lasttransDate) && sortByAscDesc == Constants.SORT_TYPE_ASCENDING) {
            binding.transAscSortLedger.setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorPrimary
                )
            )
        } else if (sortByField == getString(R.string.lasttransDate) && sortByAscDesc == Constants.SORT_TYPE_DESCENDING) {
            binding.transDescSortLedger.setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorPrimary
                )
            )
        }
    }

    fun refreshSortPopupView(binding: LedgerSortPopupBinding) {
        binding.nameAscSortLedger.setColorFilter(
            ContextCompat.getColor(
                requireContext(),
                android.R.color.transparent
            )
        )
        binding.nameDescSortLedger.setColorFilter(
            ContextCompat.getColor(
                requireContext(),
                android.R.color.transparent
            )
        )
        binding.transAscSortLedger.setColorFilter(
            ContextCompat.getColor(
                requireContext(),
                android.R.color.transparent
            )
        )
        binding.transDescSortLedger.setColorFilter(
            ContextCompat.getColor(
                requireContext(),
                android.R.color.transparent
            )
        )

    }
}
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
import com.goldbookapp.databinding.FragmentGroupsBinding
import com.goldbookapp.databinding.LedgerSortPopupBinding
import com.goldbookapp.model.LoginModel
import com.goldbookapp.model.SearchListGroupModel
import com.goldbookapp.model.UserWiseRestrictionModel
import com.goldbookapp.ui.MainActivity
import com.goldbookapp.ui.activity.group.NewGroupActivity
import com.goldbookapp.ui.activity.viewmodel.ViewModelFactory
import com.goldbookapp.ui.adapter.GroupsListAdapter
import com.goldbookapp.ui.fragment.viewmodel.GroupViewModel
import com.goldbookapp.utils.*
import com.goldbookapp.utils.CommonUtils.Companion.clickWithDebounce
import com.goldbookapp.utils.CommonUtils.Companion.isValidClickPressed
import com.goldbookapp.utils.PreferenceHelper.get
import com.goldbookapp.utils.PreferenceHelper.set
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.fragment_groups.view.*
import kotlinx.android.synthetic.main.fragment_leadger.view.*
import kotlinx.android.synthetic.main.ledger_sort_popup.view.*
import kotlinx.android.synthetic.main.toolbar_layout.*
import kotlinx.android.synthetic.main.toolbar_layout.view.*


class GroupsFragment : Fragment() {

    lateinit var popupWindow: PopupWindow
    private var viewDetail:Boolean = false
    private var viewSubGroupDetail:Boolean = false
    private lateinit var groupViewModel: GroupViewModel

    private var currentPage: Int = Constants.PAGE_START
    private var isLastPage = false
    private var isFromSearch = false
    var itemCount = 0
    private var page_size: Int = Constants.PAGE_SIZE
    private var totalPage = 1
    private var isLoading = false

    lateinit var prefs: SharedPreferences
    lateinit var binding: FragmentGroupsBinding

    private lateinit var adapter: GroupsListAdapter
    lateinit var loginModel: LoginModel

    var sortByField: String = ""
    var sortByAscDesc: String = ""
    var sortByColumn: String = ""
    var sortType: String = ""
    private var searchGroupApiTrackNo: String = "4"


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_groups, container, false)
        val view = binding.root
        setupUIandListner(view)
        setupViewModel()
        return view
    }

    private fun setupViewModel() {
        groupViewModel =
            ViewModelProvider(this, ViewModelFactory(ApiHelper(RetrofitBuilder.apiService))).get(
                GroupViewModel::class.java
            )
        binding.setLifecycleOwner(this)
    }

    fun setupUIandListner(root: View) {

        prefs = PreferenceHelper.defaultPrefs(activity?.applicationContext!!)
        loginModel = Gson().fromJson(
            prefs[Constants.PREF_LOGIN_DETAIL_KEY, ""],
            LoginModel::class.java
        ) //getter

        //default sorting is group name  desc
        sortByField = getString(R.string.groupName); sortByAscDesc = Constants.SORT_TYPE_DESCENDING;
        sortByColumn = "group_name"
        sortType = "desc"

        binding.root.imgLeft.setImageResource(R.drawable.ic_menu_black_24dp)
        binding.root.tvTitle.setText(getString(R.string.group))
        binding.root.imgRight.setImageResource(R.drawable.ic_sort)



        root.imgLeft.clickWithDebounce {
            (activity as MainActivity).openCloseDrawer()
        }
        root.floatingaddGroups.clickWithDebounce {
            startActivity(Intent(activity, NewGroupActivity::class.java))
        }
        root.imgRight.clickWithDebounce {
            openSortingPopup()
        }

        root.recyclerViewGroups.layoutManager = LinearLayoutManager(activity)
        root.recyclerViewGroups.addOnScrollListener(object :
            PaginationListener(root.recyclerViewGroups.layoutManager as LinearLayoutManager) {
            override fun loadMoreItems() {
                isLoading = true
                currentPage++

                callAccordingToTrack(searchGroupApiTrackNo, currentPage, false)
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


        root.txtSearchGroups.setOnDebounceTextWatcher(lifecycle) { input ->
            applySearch(root)
            if (NetworkUtils.isConnected()) {
                binding.root.pb_loader_group.visibility = View.VISIBLE
            }
        }

        root.txtSearchGroups.setDrawableClickListener(object : DrawableClickListener {
            override fun onClick(target: DrawableClickListener.DrawablePosition?) {
                if (isValidClickPressed()) {
                    when (target) {
                        DrawableClickListener.DrawablePosition.LEFT -> {
                            if (NetworkUtils.isConnected()) {
                                applySearch(root)
                                binding.root.pb_loader_group.visibility = View.VISIBLE
                            }

                        }
                        DrawableClickListener.DrawablePosition.RIGHT -> {
                            root.txtSearchGroups.text!!.clear()

                        }
                        else->{

                        }
                    }
                }
            }
        })
        root.txtSearchGroups.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                // Call your code here
                if (NetworkUtils.isConnected()) {
                    applySearch(root)
                    binding.root.pb_loader_group.visibility = View.VISIBLE
                }

                true
            }
            false
        }

        binding.root.swipeContainerGroups.setColorSchemeResources(R.color.colorAccent)
        binding.root.swipeContainerGroups.setOnRefreshListener {
            resetToDefault()
            callAccordingToTrack(searchGroupApiTrackNo, currentPage, true)
            binding.root.swipeContainerGroups.isRefreshing = false
        }

        setupAdapter()
    }

    private fun setupAdapter() {
        adapter = GroupsListAdapter(arrayListOf(), totalPage)
        binding.recyclerViewGroups.setHasFixedSize(true)
        binding.recyclerViewGroups.adapter = adapter
        resetToDefault()
    }

    private fun applySearch(root: View) {
        if (root.txtSearchGroups?.toString()?.length!! == 0) {
            resetToDefault()
            //isFromSearch = false
            searchGroupApiTrackNo = "1"
            callAccordingToTrack(searchGroupApiTrackNo, currentPage, false)
            root.txtSearchGroups.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_search_black_24dp,
                0,
                R.drawable.ic_cancel_blank_24dp,
                0
            )

        } else {
            if (root.txtSearchGroups?.toString()?.length!! > 0) {
                searchGroupApiTrackNo = "2"
                resetToDefault()
                isFromSearch = true
                callAccordingToTrack(searchGroupApiTrackNo, currentPage, false)
                root.txtSearchGroups.setCompoundDrawablesWithIntrinsicBounds(
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

        val groupSortType =
            object : TypeToken<String>() {}.type
        searchGroupApiTrackNo = Gson().fromJson(
            prefs[Constants.PREF_GROUP_SORT_TRACKNO, "4"],
            groupSortType
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
            callAccordingToTrack(searchGroupApiTrackNo, currentPage, true)
        }
        binding.txtSearchGroups.clearFocus()
    }

    override fun onPause() {
        super.onPause()

        if (this::popupWindow.isInitialized) {
            popupWindow.dismiss()
        }
    }

    private fun defaultDisableAllButtonnUI() {
        binding.floatingaddGroups.visibility = View.GONE
    }

    private fun defaultEnableAllButtonnUI() {
        binding.floatingaddGroups.visibility = View.VISIBLE
        viewDetail = true
        adapter.viewDetail(viewDetail)
        viewSubGroupDetail = true
        adapter.viewSubGroupDetail(viewSubGroupDetail)
    }

    private fun userWiseRestriction(token: String?) {
        if (NetworkUtils.isConnected()) {
            groupViewModel.userWiseRestriction(token)
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
            if (data.permission!!.get(i).startsWith(getString(R.string.ledger_Group))) {
                // Restriction check for Customer
                when (data.permission!!.get(i).endsWith(getString(R.string.ledger_group_details), true)) {
                    true -> {
                        viewDetail = true
                        adapter.viewDetail(viewDetail)
                    }else->{

                }
                }
            }
            if (data.permission!!.get(i).startsWith(getString(R.string.ledger_Group))) {
                // Restriction check for Customer
                when (data.permission!!.get(i).endsWith(getString(R.string.ledger_subgroup_details), true)) {
                    true -> {
                        viewSubGroupDetail = true
                        adapter.viewSubGroupDetail(viewSubGroupDetail)
                    }else->{

                }
                }
            }
            if (data.permission!!.get(i).startsWith(getString(R.string.ledger_Group))) {
                // Restriction check for Customer
                when (data.permission!!.get(i).endsWith(getString(R.string.ledger_group_add_edit), true)) {
                    true -> {
                        binding.floatingaddGroups.visibility = View.VISIBLE
                    }else->{

                }
                }
            }

        }
    }


    private fun callAccordingToTrack(
        searchGroupApiTrackNo: String,
        currentPage: Int,
        showLoading: Boolean
    ) {
        when (searchGroupApiTrackNo) {
            "1" -> {
                searchListGroupAPI(
                    showLoading,
                    loginModel?.data?.bearer_access_token,
                    loginModel?.data?.company_info?.id,
                    currentPage,
                    binding.txtSearchGroups.text.toString(),
                    sortByColumn,
                    sortType
                )
            }
            "2" -> {
                searchListGroupAPI(
                    showLoading,
                    loginModel?.data?.bearer_access_token,
                    loginModel?.data?.company_info?.id,
                    currentPage,
                    binding.txtSearchGroups.text.toString(),
                    sortByColumn,
                    sortType
                )
            }

            "3" -> {
                sortByField = getString(R.string.groupName); sortByAscDesc =
                    Constants.SORT_TYPE_ASCENDING;
                sortByColumn = "group_name"
                sortType = "asc"
                searchListGroupAPI(
                    showLoading,
                    loginModel?.data?.bearer_access_token,
                    loginModel?.data?.company_info?.id,
                    currentPage,
                    binding.txtSearchGroups.text.toString(),
                    sortByColumn,
                    sortType
                )
            }
            "4" -> {
                sortByField = getString(R.string.groupName); sortByAscDesc =
                    Constants.SORT_TYPE_DESCENDING;
                sortByColumn = "group_name"
                sortType = "desc"
                searchListGroupAPI(
                    showLoading,
                    loginModel?.data?.bearer_access_token,
                    loginModel?.data?.company_info?.id,
                    currentPage,
                    binding.txtSearchGroups.text.toString(),
                    sortByColumn,
                    sortType
                )
            }
            "5" -> {
                sortByField = getString(R.string.parentgroupName); sortByAscDesc =
                    Constants.SORT_TYPE_ASCENDING;
                sortByColumn = "parent_group_name"
                sortType = "asc"
                searchListGroupAPI(
                    showLoading,
                    loginModel?.data?.bearer_access_token,
                    loginModel?.data?.company_info?.id,
                    currentPage,
                    binding.txtSearchGroups.text.toString(),
                    sortByColumn,
                    sortType
                )
            }
            "6" -> {
                sortByField = getString(R.string.parentgroupName); sortByAscDesc =
                    Constants.SORT_TYPE_DESCENDING;
                sortByColumn = "parent_group_name"
                sortType = "desc"
                searchListGroupAPI(
                    showLoading,
                    loginModel?.data?.bearer_access_token,
                    loginModel?.data?.company_info?.id,
                    currentPage,
                    binding.txtSearchGroups.text.toString(),
                    sortByColumn,
                    sortType
                )
            }
        }
    }


    fun searchListGroupAPI(
        showLoading: Boolean,
        token: String?,
        companyID: String?,
        curret_page: Int?,
        searchName: String?,
        sort_by_column: String?,
        sort_type: String?
    ) {

        if (NetworkUtils.isConnected()) {

            groupViewModel.searchListGroup(
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
                                    retrieveList(it.data.data, page_size)


                                    if (it.data.data.isNotEmpty()) {
                                        binding.recyclerViewGroups.visibility = View.VISIBLE
                                        binding.tvNoRecordGroups.visibility = View.GONE
                                    } else {
                                        binding.tvNoRecordGroups.text = it.data.message

                                        binding.recyclerViewGroups.visibility = View.GONE
                                        binding.tvNoRecordGroups.visibility = View.VISIBLE
                                    }

                                } else {

                                    when (it.data!!.code == Constants.ErrorCode) {
                                        true -> {
                                            Toast.makeText(
                                                context,
                                                " it.data?.errormessage?.message",
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
                                binding.root.pb_loader_group.visibility = View.GONE
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

    private fun retrieveList(groupList: List<SearchListGroupModel.DataGroup>?, pageSize: Int) {

        if (currentPage != Constants.PAGE_START) adapter.removeLoading()
        //adapter.addCustomer(customersList)
        adapter.apply {
            addGroup(
                groupList as ArrayList<SearchListGroupModel.DataGroup>?,
                isFromSearch,
                pageSize,
                currentPage,
                totalPage
            )
            notifyDataSetChanged()
        }

        binding.swipeContainerGroups.setRefreshing(false)

        if (currentPage < totalPage) {
            adapter.addLoading()
        } else {
            isLastPage = true
        }
        isLoading = false
        prefs[Constants.PREF_GROUP_SORT_TRACKNO] =
            searchGroupApiTrackNo //setter (for group sort tracking while relaunching app)
    }


    private fun openSortingPopup() {

        val layoutInflater =
            requireContext()!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
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
        view.sortTypeName.setText(R.string.groupName)
        view.sortType.setText(R.string.parentgroupName)

        refreshSortPopupView(sortbinding)
        highlightSortingColor(sortbinding)

        sortbinding.nameAscSortLedger.clickWithDebounce {
            refreshSortPopupView(sortbinding); sortByField =
            getString(R.string.groupName); sortByAscDesc = Constants.SORT_TYPE_ASCENDING;
            (sortbinding.nameAscSortLedger as ImageView).setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorPrimary
                )
            )
            searchGroupApiTrackNo = "3"
            resetToDefault()
            callAccordingToTrack(searchGroupApiTrackNo, currentPage, true)
        }

        sortbinding.nameDescSortLedger.clickWithDebounce {
            refreshSortPopupView(sortbinding); sortByField =
            getString(R.string.groupName); sortByAscDesc = Constants.SORT_TYPE_DESCENDING;
            (sortbinding.nameDescSortLedger as ImageView).setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorPrimary
                )
            )
            searchGroupApiTrackNo = "4"
            resetToDefault()
            callAccordingToTrack(searchGroupApiTrackNo, currentPage, true)
        }

        sortbinding.transAscSortLedger.clickWithDebounce {
            refreshSortPopupView(sortbinding); sortByField =
            getString(R.string.parentgroupName); sortByAscDesc = Constants.SORT_TYPE_ASCENDING;
            (sortbinding.transAscSortLedger as ImageView).setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorPrimary
                )
            )
            searchGroupApiTrackNo = "5"
            resetToDefault()
            callAccordingToTrack(searchGroupApiTrackNo, currentPage, true)
        }

        sortbinding.transDescSortLedger.clickWithDebounce {
            refreshSortPopupView(sortbinding); sortByField =
            getString(R.string.parentgroupName); sortByAscDesc = Constants.SORT_TYPE_DESCENDING;
            (sortbinding.transDescSortLedger as ImageView).setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorPrimary
                )
            )
            searchGroupApiTrackNo = "6"
            resetToDefault()
            callAccordingToTrack(searchGroupApiTrackNo, currentPage, true)
        }
        sortbinding.resetAllLedgerSort.clickWithDebounce {

            refreshSortPopupView(sortbinding);
            searchGroupApiTrackNo = "4"
            resetToDefault()
            highlightSortingColor(sortbinding)
            if (binding.root.txtSearchLedger.text.toString().length > 0)
                binding.root.txtSearchLedger.setText("")
            callAccordingToTrack(searchGroupApiTrackNo, currentPage, true)
        }

    }

    private fun highlightSortingColor(binding: LedgerSortPopupBinding) {
        if (sortByField == getString(R.string.groupName) && sortByAscDesc == Constants.SORT_TYPE_ASCENDING) {
            binding.nameAscSortLedger.setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorPrimary
                )
            )
        } else if (sortByField == getString(R.string.groupName) && sortByAscDesc == Constants.SORT_TYPE_DESCENDING) {
            binding.nameDescSortLedger.setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorPrimary
                )
            )
        } else if (sortByField == getString(R.string.parentgroupName) && sortByAscDesc == Constants.SORT_TYPE_ASCENDING) {
            binding.transAscSortLedger.setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorPrimary
                )
            )
        } else if (sortByField == getString(R.string.parentgroupName) && sortByAscDesc == Constants.SORT_TYPE_DESCENDING) {
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
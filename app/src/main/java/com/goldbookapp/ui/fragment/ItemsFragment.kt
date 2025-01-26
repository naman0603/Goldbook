package com.goldbookapp.ui.ui.slideshow

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
import com.goldbookapp.databinding.FragmentItemsBinding
import com.goldbookapp.databinding.ItemSortPopupBinding
import com.goldbookapp.model.GetItemListModel
import com.goldbookapp.model.LoginModel
import com.goldbookapp.model.UserWiseRestrictionModel
import com.goldbookapp.ui.MainActivity
import com.goldbookapp.ui.activity.item.NewItemActivity
import com.goldbookapp.ui.activity.viewmodel.ViewModelFactory
import com.goldbookapp.ui.adapter.ItemListAdapter
import com.goldbookapp.ui.fragment.viewmodel.ItemsViewModel
import com.goldbookapp.utils.*
import com.goldbookapp.utils.CommonUtils.Companion.clickWithDebounce
import com.goldbookapp.utils.PreferenceHelper.get
import com.goldbookapp.utils.PreferenceHelper.set
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.fragment_items.*
import kotlinx.android.synthetic.main.fragment_items.view.*
import kotlinx.android.synthetic.main.toolbar_layout.*
import kotlinx.android.synthetic.main.toolbar_layout.view.*

class ItemsFragment : Fragment() {

    lateinit var sortingpopupWindow: PopupWindow
    lateinit var popupMenu: PopupMenu

    private var viewDetail: Boolean = false
    private lateinit var viewModel: ItemsViewModel
    private var currentPage: Int = Constants.PAGE_START
    private var page_size: Int = Constants.PAGE_SIZE
    private var isLastPage = false
    private var totalPage = 1
    private var isLoading = false
    private var isFromSearch = false
    var itemCount = 0

    private var searchItemApiTrackNo: String = "6"
    lateinit var binding: FragmentItemsBinding
    private var status: String? = "3"
    private lateinit var adapter: ItemListAdapter
    lateinit var prefs: SharedPreferences

    var sortByField: String = ""
    var sortByAscDesc: String = ""
    var sortByColumn: String = ""
    var sortType: String = ""

    lateinit var loginModel: LoginModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_items, container, false)
        val view = binding.root

        setupViewModel()
        setupUIandListner(view)

        return view
    }

    private fun setupViewModel() {
        viewModel =
            ViewModelProvider(this, ViewModelFactory(ApiHelper(RetrofitBuilder.apiService))).get(
                ItemsViewModel::class.java
            )
        binding.setLifecycleOwner(this)

    }


    fun setupUIandListner(root: View) {

        prefs = PreferenceHelper.defaultPrefs(activity?.applicationContext!!)
        loginModel = Gson().fromJson(
            prefs[Constants.PREF_LOGIN_DETAIL_KEY, ""],
            LoginModel::class.java
        ) //getter

        //default sorting is name ascending
        sortByField = getString(R.string.name); sortByAscDesc = Constants.SORT_TYPE_ASCENDING
        sortByColumn = "name"
        sortType = "asc"

        root.imgLeft.setImageResource(R.drawable.ic_menu_black_24dp)
        root.tvTitle.setText(getString(R.string.all_items))
        root.imgRight2.setImageResource(R.drawable.ic_sort)
        root.imgRight.setImageResource(R.drawable.ic_more)



        root.floatingaddItem.clickWithDebounce {
            startActivity(Intent(activity, NewItemActivity::class.java))
        }
        root.imgLeft.clickWithDebounce {
            (activity as MainActivity).openCloseDrawer()
        }
        root.imgRight2.clickWithDebounce {
            openSortingPopup()
        }
        root.imgRight.clickWithDebounce {
            popupMenu = PopupMenu(requireContext(), binding.root.imgRight)
            context?.let { it1 -> CommonUtils.applyFontToMenu(popupMenu.menu, it1) }
            popupMenu.menuInflater.inflate(R.menu.popup_menu_item, popupMenu.menu)
            when (status) {
                "3" -> {
                    val newTitle: Spannable =
                        SpannableString(popupMenu.menu.getItem(0).title.toString())
                    val newColor =
                        ContextCompat.getColor(requireContext(), R.color.colorPrimary)
                    newTitle.setSpan(
                        ForegroundColorSpan(newColor),
                        0, newTitle.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    popupMenu.menu.getItem(0).title = newTitle
                }
                "1" -> {
                    val newTitle: Spannable =
                        SpannableString(popupMenu.menu.getItem(1).title.toString())
                    val newColor =
                        ContextCompat.getColor(requireContext(), R.color.colorPrimary)
                    newTitle.setSpan(
                        ForegroundColorSpan(newColor),
                        0, newTitle.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    popupMenu.menu.getItem(1).title = newTitle
                }
                "2" -> {
                    val newTitle: Spannable =
                        SpannableString(popupMenu.menu.getItem(2).title.toString())
                    val newColor =
                        ContextCompat.getColor(requireContext(), R.color.colorPrimary)
                    newTitle.setSpan(
                        ForegroundColorSpan(newColor),
                        0, newTitle.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    popupMenu.menu.getItem(2).title = newTitle
                }
            }
            popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.actionAllItems -> {
                        status = "3"
                        searchItemApiTrackNo = "1"
                        binding.root.tvTitle.setText(getString(R.string.all_items))
                        //isFromSearch = false
                        resetToDefault()
                        callAccordingToTrack(searchItemApiTrackNo, currentPage, true)

                    }

                    R.id.actionActiveItems -> {
                        status = "1"
                        searchItemApiTrackNo = "2"
                        binding.root.tvTitle.setText(getString(R.string.active_items))
                        //isFromSearch = false
                        resetToDefault()
                        callAccordingToTrack(searchItemApiTrackNo, currentPage, true)


                    }

                    R.id.actionInActiveItems -> {
                        status = "2"
                        searchItemApiTrackNo = "3"
                        binding.root.tvTitle.setText(getString(R.string.inactive_items))
                        //isFromSearch = false
                        resetToDefault()
                        callAccordingToTrack(searchItemApiTrackNo, currentPage, true)
                    }

                }
                true
            })
            popupMenu.show()
        }
        root.recyclerViewItem.layoutManager = LinearLayoutManager(activity)
        root.recyclerViewItem.addOnScrollListener(object :
            PaginationListener(root.recyclerViewItem.layoutManager as LinearLayoutManager) {
            override fun loadMoreItems() {
                isLoading = true
                currentPage++
                //Toast.makeText(context,"reyscrollcurrentPage" + "$currentPage",Toast.LENGTH_SHORT).show()
                callAccordingToTrack(searchItemApiTrackNo, currentPage, false)
            }

            override fun isLastPage(): Boolean {
                return isLastPage
            }

            override fun isLoading(): Boolean {
                return isLoading
            }
        })

        root.txtSearchItem.setOnDebounceTextWatcher(lifecycle) { input ->
            if (NetworkUtils.isConnected()) {
                applySearch(root)
                binding.root.pb_loader_items.visibility = View.VISIBLE
            }
        }

        root.txtSearchItem.setDrawableClickListener(object : DrawableClickListener {
            override fun onClick(target: DrawableClickListener.DrawablePosition?) {
                if (CommonUtils.isValidClickPressed()) {
                    when (target) {
                        DrawableClickListener.DrawablePosition.LEFT -> {
                            if (NetworkUtils.isConnected()) {
                                applySearch(root)
                                binding.root.pb_loader_items.visibility = View.VISIBLE
                            }
                        }
                        DrawableClickListener.DrawablePosition.RIGHT -> {
                            root.txtSearchItem.text!!.clear()
                            resetToDefault()
                            //isFromSearch = false
                            callAccordingToTrack(searchItemApiTrackNo, currentPage, true)
                        }else->{

                    }
                    }
                }
            }
        })

        root.txtSearchItem.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                // Call your code here
                if (NetworkUtils.isConnected()) {
                    applySearch(root)
                    binding.root.pb_loader_items.visibility = View.VISIBLE
                }

                true
            }
            false
        }
        /*root.txtSearchItem.doOnTextChanged { text, start, before, count ->

            if (text?.length!! > 0) {
                root.txtSearchItem.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_search_black_24dp,
                    0,
                    R.drawable.ic_cancel_blank_24dp,
                    0
                );

            } else {
                root.txtSearchItem.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_search_black_24dp,
                    0,
                    0,
                    0
                );
                *//* resetToDefault()
                 //isFromSearch = false
                 callAccordingToTrack(searchItemApiTrackNo, currentPage, true)*//*
            }

        }*/

        binding.root.swipeContainerItem.setOnRefreshListener {
            resetToDefault()

            callAccordingToTrack(searchItemApiTrackNo, currentPage, true)

            binding.swipeContainerItem.isRefreshing = false
        }

        setupAdapter()

    }

    private fun setupAdapter() {
        adapter = ItemListAdapter(arrayListOf(), totalPage)
        binding.recyclerViewItem.setHasFixedSize(true)
        binding.recyclerViewItem.adapter = adapter
        resetToDefault()
    }

    private fun applySearch(root: View) {
        if (root.txtSearchItem?.toString()?.length!! == 0) {
            resetToDefault()
            //isFromSearch = false
            callAccordingToTrack(searchItemApiTrackNo, currentPage, false)
            root.txtSearchItem.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_search_black_24dp,
                0,
                R.drawable.ic_cancel_blank_24dp,
                0
            )

        } else {
            if (root.txtSearchItem?.toString()?.length!! > 0) {
                searchItemApiTrackNo = "4"
                resetToDefault()
                isFromSearch = true
                callAccordingToTrack(searchItemApiTrackNo, currentPage, false)
                root.txtSearchItem.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_search_black_24dp,
                    0,
                    0,
                    0
                )
            }
        }

    }

    private fun retrieveList(
        itemList: List<GetItemListModel.Data1077697879>?,
        pageSize: Int
    ) {

        if (currentPage != Constants.PAGE_START) adapter.removeLoading()
        //adapter.addCustomer(customersList)
        adapter.apply {
            addItem(itemList, isFromSearch, pageSize, currentPage, totalPage)
            notifyDataSetChanged()
        }

        binding.swipeContainerItem.setRefreshing(false)

        // check weather is last page or not
        if (currentPage < totalPage) {
            adapter.addLoading()
        } else {
            isLastPage = true
        }
        isLoading = false
        prefs[Constants.PREF_ITEM_SORT_TRACKNO] =
            searchItemApiTrackNo //setter (for customer sort tracking while relaunching app)
    }

    override fun onResume() {
        super.onResume()

        //refreshData()
        val itemSortType =
            object : TypeToken<String>() {}.type
        searchItemApiTrackNo = Gson().fromJson(
            prefs[Constants.PREF_ITEM_SORT_TRACKNO, "6"],
            itemSortType
        ) //getter to maintain track of user with sorting option

        if (NetworkUtils.isConnected()) {

            when (loginModel.data!!.user_info!!.user_type.equals("user", true)) {
                // user type user
                true -> {
                    // apply restriciton
                    defaultDisableAllButtonnUI()
                    userWiseRestriction(loginModel.data?.bearer_access_token)
                }
                false -> {
                    //admin /super_admin
                    defaultEnableAllButtonnUI()

                }
            }
            resetToDefault()
            callAccordingToTrack(searchItemApiTrackNo, currentPage, true)
        }
        binding.txtSearchItem.clearFocus()
        //(activity as MainActivity).showBottomSheet()

    }

    override fun onPause() {
        super.onPause()
        if (this::sortingpopupWindow.isInitialized) {
            sortingpopupWindow.dismiss()
        }
        if (this::popupMenu.isInitialized) {
            popupMenu.dismiss()
        }
    }

    private fun defaultDisableAllButtonnUI() {
        binding.floatingaddItem.visibility = View.GONE
    }

    private fun defaultEnableAllButtonnUI() {
        binding.floatingaddItem.visibility = View.VISIBLE
        viewDetail = true
        adapter.viewDetail(viewDetail)
    }

    fun resetToDefault() {
        itemCount = 0
        currentPage = Constants.PAGE_START
        isLastPage = false
        if (this::adapter.isInitialized)
            adapter.clear()
        isFromSearch = false
    }


    private fun openSortingPopup() {

        val layoutInflater =
            requireContext()!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        var binding: ItemSortPopupBinding =
            DataBindingUtil.inflate(layoutInflater, R.layout.item_sort_popup, null, false)
        val view = binding.root
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
            searchItemApiTrackNo = "6"
            resetToDefault()
            callAccordingToTrack(searchItemApiTrackNo, currentPage, true)
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
            searchItemApiTrackNo = "7"
            resetToDefault()
            callAccordingToTrack(searchItemApiTrackNo, currentPage, true)
        }

        binding.imgStockAscSortCust.clickWithDebounce {
            refreshSortPopupView(binding); sortByField =
            getString(R.string.stock_in_hand); sortByAscDesc = Constants.SORT_TYPE_ASCENDING;
            (binding.imgStockAscSortCust as ImageView).setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorPrimary
                )
            )
            searchItemApiTrackNo = "8"
            resetToDefault()
            callAccordingToTrack(searchItemApiTrackNo, currentPage, true)
        }

        binding.imgStockDescSortCust.clickWithDebounce {
            refreshSortPopupView(binding); sortByField =
            getString(R.string.stock_in_hand); sortByAscDesc = Constants.SORT_TYPE_DESCENDING;
            (binding.imgStockDescSortCust as ImageView).setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorPrimary
                )
            )
            searchItemApiTrackNo = "9"
            resetToDefault()
            callAccordingToTrack(searchItemApiTrackNo, currentPage, true)
        }

        binding.resetAllItemSort.clickWithDebounce {
            refreshSortPopupView(binding);
            resetToDefault()
            searchItemApiTrackNo = "6"
            sortByField = getString(R.string.name); sortByAscDesc = Constants.SORT_TYPE_ASCENDING
            sortByColumn = "name"
            sortType = "asc"
            highlightSortingColor(binding)
            if (txtSearchItem.text.toString().length > 0)
                txtSearchItem.setText("")
            callAccordingToTrack(searchItemApiTrackNo, currentPage, true)
        }

    }

    private fun highlightSortingColor(binding: ItemSortPopupBinding) {
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
        } else if (sortByField == getString(R.string.stock_in_hand) && sortByAscDesc == Constants.SORT_TYPE_ASCENDING) {
            binding.imgStockAscSortCust.setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorPrimary
                )
            )
        } else if (sortByField == getString(R.string.stock_in_hand) && sortByAscDesc == Constants.SORT_TYPE_DESCENDING) {
            binding.imgStockDescSortCust.setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorPrimary
                )
            )
        }
    }

    fun refreshSortPopupView(binding: ItemSortPopupBinding) {
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
        binding.imgStockAscSortCust.setColorFilter(
            ContextCompat.getColor(
                requireContext(),
                android.R.color.transparent
            )
        )
        binding.imgStockDescSortCust.setColorFilter(
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
            if (data.permission!!.get(i).startsWith(getString(R.string.items))) {
                // Restriction check for Customer
                when (data.permission!!.get(i).endsWith(getString(R.string.list), true)) {
                    true -> {
                        viewDetail = true
                        adapter.viewDetail(viewDetail)
                    }else->{

                }
                }
            }
            if (data.permission!!.get(i).startsWith(getString(R.string.items))) {
                // Restriction check for Customer
                when (data.permission!!.get(i).endsWith(getString(R.string.add_edit), true)) {
                    true -> {
                        binding.floatingaddItem.visibility = View.VISIBLE
                    }else->{

                }
                }
            }

        }
    }


    fun getItemListAPI(
        showLoading: Boolean,
        token: String?,
        staus: String?,
        current_page: Int?,
        searchName: String?,
        sort_by_column: String?,
        sort_type: String?

    ) {

        if (NetworkUtils.isConnected()) {

            viewModel.getItemList(
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
                                        binding.recyclerViewItem.visibility = View.VISIBLE
                                        binding.tvNoRecordItem.visibility = View.GONE
                                    } else {
                                        binding.tvNoRecordItem.text = it.data.message

                                        binding.recyclerViewItem.visibility = View.GONE
                                        binding.tvNoRecordItem.visibility = View.VISIBLE
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
                                binding.root.pb_loader_items.visibility = View.GONE
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


    private fun callAccordingToTrack(
        searchItemApiTrackNo: String,
        currentPage: Int,
        showLoading: Boolean
    ) {
        when (searchItemApiTrackNo) {
            "1" -> {
                // all items
                binding.root.tvTitle.setText(getString(R.string.all_items))
                status = "3"
                getItemListAPI(
                    showLoading,
                    loginModel?.data?.bearer_access_token,
                    status,
                    currentPage,
                    binding.txtSearchItem.text.toString(),
                    sortByColumn,
                    sortType

                )
            }
            "2" -> {
                // active items
                binding.root.tvTitle.setText(getString(R.string.active_items))
                status = "1"
                getItemListAPI(
                    showLoading,
                    loginModel?.data?.bearer_access_token,
                    status,
                    currentPage,
                    binding.txtSearchItem.text.toString(),
                    sortByColumn,
                    sortType

                )
            }
            "3" -> {
                //inactive items
                binding.root.tvTitle.setText(getString(R.string.inactive_items))
                status = "2"
                getItemListAPI(
                    showLoading,
                    loginModel?.data?.bearer_access_token,
                    status,
                    currentPage,
                    binding.txtSearchItem.text.toString(),
                    sortByColumn,
                    sortType
                )
            }
            "4" -> getItemListAPI(
                showLoading,
                loginModel?.data?.bearer_access_token,
                status,
                currentPage,
                binding.txtSearchItem.text.toString(),
                sortByColumn,
                sortType
            )
            "5" -> getItemListAPI(
                showLoading,
                loginModel?.data?.bearer_access_token,
                status,
                currentPage,
                binding.txtSearchItem.text.toString(),
                sortByColumn,
                sortType
            )
            "6" -> {
                sortByField = getString(R.string.name); sortByAscDesc =
                    Constants.SORT_TYPE_ASCENDING
                sortByColumn = "name"
                sortType = "asc"
                getItemListAPI(
                    showLoading,
                    loginModel?.data?.bearer_access_token,
                    status,
                    currentPage,
                    binding.txtSearchItem.text.toString(),
                    sortByColumn,
                    sortType
                )
            }
            "7" -> {
                sortByField = getString(R.string.name); sortByAscDesc =
                    Constants.SORT_TYPE_DESCENDING;
                sortByColumn = "name"
                sortType = "desc"
                getItemListAPI(
                    showLoading,
                    loginModel?.data?.bearer_access_token,
                    status,
                    currentPage,
                    binding.txtSearchItem.text.toString(),
                    sortByColumn,
                    sortType
                )
            }
            "8" -> {
                sortByField = getString(R.string.stock_in_hand); sortByAscDesc =
                    Constants.SORT_TYPE_ASCENDING;
                sortByColumn = "quantity"
                sortType = "asc"
                getItemListAPI(
                    showLoading,
                    loginModel?.data?.bearer_access_token,
                    status,
                    currentPage,
                    binding.txtSearchItem.text.toString(),
                    sortByColumn,
                    sortType
                )
            }
            "9" -> {
                sortByField = getString(R.string.stock_in_hand); sortByAscDesc =
                    Constants.SORT_TYPE_DESCENDING;
                sortByColumn = "quantity"
                sortType = "desc"
                getItemListAPI(
                    showLoading,
                    loginModel?.data?.bearer_access_token,
                    status,
                    currentPage,
                    binding.txtSearchItem.text.toString(),
                    sortByColumn,
                    sortType
                )
            }

        }

    }
}
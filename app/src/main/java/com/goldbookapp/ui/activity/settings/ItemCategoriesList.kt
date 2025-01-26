package com.goldbookapp.ui.activity.settings

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.NetworkUtils
import com.goldbookapp.R
import com.goldbookapp.api.ApiHelper
import com.goldbookapp.api.RetrofitBuilder
import com.goldbookapp.databinding.ActivityItemCategoryListBinding
import com.goldbookapp.model.ItemCategoryModel
import com.goldbookapp.model.LoginModel
import com.goldbookapp.model.UserWiseRestrictionModel
import com.goldbookapp.ui.activity.ItemCategoriesListViewModel
import com.goldbookapp.ui.activity.viewmodel.ViewModelFactory
import com.goldbookapp.ui.adapter.ItemCategoriesListAdapter
import com.goldbookapp.utils.*
import com.goldbookapp.utils.CommonUtils.Companion.clickWithDebounce
import com.goldbookapp.utils.PreferenceHelper.get
import com.google.gson.Gson
import com.hamzasharuf.networkmonitor.ConnectivityStateHolder
import com.hamzasharuf.networkmonitor.Event
import com.hamzasharuf.networkmonitor.NetworkEvents
import hk.ids.gws.android.sclick.SClick
import kotlinx.android.synthetic.main.activity_item_category_list.*
import kotlinx.android.synthetic.main.activity_item_category_list.view.*
import kotlinx.android.synthetic.main.toolbar_layout.*
import kotlinx.android.synthetic.main.toolbar_layout.view.*

class ItemCategoriesList : AppCompatActivity() {

    private lateinit var viewModel: ItemCategoriesListViewModel

    lateinit var binding: ActivityItemCategoryListBinding

    private lateinit var adapter: ItemCategoriesListAdapter

    lateinit var loginModel: LoginModel
    private lateinit var itemCatList: List<ItemCategoryModel.Data2101931085>
    var isChangeStaus:Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_item_category_list)

        val view = binding.root

        setupViewModel()
        setupUIandListner(view)


    }
    private fun setupViewModel() {
        viewModel =
            ViewModelProvider(this, ViewModelFactory(ApiHelper(RetrofitBuilder.apiService))).get(
                ItemCategoriesListViewModel::class.java
            )
        binding.setLifecycleOwner(this)

    }
    fun setupUIandListner(root: View){

        val prefs = PreferenceHelper.defaultPrefs(this)
        loginModel = Gson().fromJson(
            prefs[Constants.PREF_LOGIN_DETAIL_KEY, ""],
            LoginModel::class.java
        ) //getter

        binding.root.imgLeft.setImageResource(R.drawable.ic_back)
        binding.root.tvTitle.setText(getString(R.string.item_categories))


        root.imgLeft.clickWithDebounce{
            onBackPressed()
        }
        root.addItemCategory.clickWithDebounce{
            if (this::itemCatList.isInitialized)
                startActivity(
                    Intent(this, NewItemCategory::class.java)
                    .putExtra(Constants.Change_Status, isChangeStaus))
        }


        //recyclerviewsetup
        root.recyclerViewItemCat.layoutManager = LinearLayoutManager(this)
        adapter = ItemCategoriesListAdapter(arrayListOf())
        root.recyclerViewItemCat.adapter = adapter

    }


    override fun onDestroy() {
        super.onDestroy()
        CommonUtils.hideProgress()
    }
    override fun onResume() {
        super.onResume()

        NetworkEvents.observe(this, Observer {
            if (it is Event.ConnectivityEvent) {
                handleConnectivityChange()
            }
        })
    }

    private fun handleConnectivityChange() {
        if (ConnectivityStateHolder.isConnected) {
            // Network is available
            CommonUtils.hideInternetDialog()
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
            getItemCategories( loginModel.data?.bearer_access_token,"")
        }

        if (!ConnectivityStateHolder.isConnected) {
            // Network is not available
            CommonUtils.showDialog(this, getString(R.string.please_check_internet_msg))

        }
    }
    private fun defaultDisableAllButtonnUI() {
        binding.addItemCategory.visibility = View.GONE
        isChangeStaus = false
    }
    private fun defaultEnableAllButtonnUI() {
        binding.addItemCategory.visibility = View.VISIBLE
        isChangeStaus = true
    }
    private fun userWiseRestriction(token: String?) {
        if (NetworkUtils.isConnected()) {
            viewModel.userWiseRestriction(token)
                .observe(this, Observer {
                    it?.let { resource ->
                        when (resource.status) {

                            Status.SUCCESS -> {
                                if (it.data?.status == true) {
                                    applyUserWiseRestriction(it.data.data)
                                } else {
                                    when (it.data!!.code == Constants.ErrorCode) {
                                        true -> {
                                            Toast.makeText(
                                                this,
                                                it.data.errormessage.message,
                                                Toast.LENGTH_LONG
                                            )
                                                .show()
                                        }
                                        false -> {
                                            CommonUtils.somethingWentWrong(this)
                                        }

                                    }
                                }
                                CommonUtils.hideProgress()
                            }
                            Status.ERROR -> {
                                CommonUtils.hideProgress()
                            }
                            Status.LOADING -> {
                                CommonUtils.showProgress(this)
                            }
                        }
                    }
                })
        }
    }

    private fun applyUserWiseRestriction(data: UserWiseRestrictionModel.Data) {
        for (i in 0 until data.permission!!.size) {
            if (data.permission!!.get(i).startsWith(getString(R.string.item_category))) {
                // Restriction check for Customerl
                when (data.permission!!.get(i).endsWith(getString(R.string.add_edit), true)) {
                    true -> {
                        binding.addItemCategory.visibility = View.VISIBLE
                    }else->{

                }
                }
                when (data.permission!!.get(i).endsWith(getString(R.string.change_status), true)) {
                    true -> {
                        isChangeStaus =true
                    }else->{

                }
                }

            }




        }
    }
    fun getItemCategories( token: String?,
                           offset: String?){

        if(NetworkUtils.isConnected()) {

            viewModel.getItemCategories(token,offset)
                .observe(this, Observer {
                    it?.let { resource ->
                        when (resource.status) {
                            Status.SUCCESS -> {
                                if (it.data?.status == true) {
                                    itemCatList = it.data?.data!!
                                    retrieveList(itemCatList)

                                    //val nums = listOf(it.data?.data)
                                    //val listSize: Int? = it.data?.data?.size

                                    if(it.data.data?.isNotEmpty()!!){
                                        recyclerViewItemCat.visibility = View.VISIBLE
                                        tvNoRecordItemCat.visibility = View.GONE
                                    }else{
                                        tvNoRecordItemCat.text = it.data.message

                                        recyclerViewItemCat.visibility = View.GONE
                                        tvNoRecordItemCat.visibility = View.VISIBLE
                                    }

                                } else {

                                    when(it.data!!.code == Constants.ErrorCode){
                                        true-> {
                                            Toast.makeText(
                                                this,
                                                it.data.errormessage?.message,
                                                Toast.LENGTH_LONG
                                            )
                                                .show()
                                        }
                                        false->{
                                            CommonUtils.somethingWentWrong(this)
                                        }

                                    }
                                }
                                CommonUtils.hideProgress()

                            }
                            Status.ERROR -> {
                                CommonUtils.hideProgress()

                            }
                            Status.LOADING -> {
                                CommonUtils.showProgress(this)
                            }
                        }
                    }
                })
        }
    }
    private fun retrieveList(itemCatList: List<ItemCategoryModel.Data2101931085>?) {
        adapter.apply {
            addItemCategories(itemCatList)
            notifyDataSetChanged()
        }
    }
}
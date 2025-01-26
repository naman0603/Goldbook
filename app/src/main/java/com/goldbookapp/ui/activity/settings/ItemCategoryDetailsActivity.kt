package com.goldbookapp.ui.activity.settings

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.blankj.utilcode.util.NetworkUtils
import com.goldbookapp.R
import com.goldbookapp.api.ApiHelper
import com.goldbookapp.api.RetrofitBuilder
import com.goldbookapp.databinding.ActivityItemCategoryDetailsBinding
import com.goldbookapp.model.ChangeStatusItemCategoryModel
import com.goldbookapp.model.GetItemCategoriesModel
import com.goldbookapp.model.LoginModel
import com.goldbookapp.model.UserWiseRestrictionModel
import com.goldbookapp.ui.activity.viewmodel.ItemCategoryDetailViewModel
import com.goldbookapp.ui.activity.viewmodel.ViewModelFactory
import com.goldbookapp.utils.*
import com.goldbookapp.utils.CommonUtils.Companion.clickWithDebounce
import com.goldbookapp.utils.CommonUtils.Companion.isValidClickPressed
import com.goldbookapp.utils.PreferenceHelper.get
import com.google.gson.Gson
import com.hamzasharuf.networkmonitor.ConnectivityStateHolder
import com.hamzasharuf.networkmonitor.Event
import com.hamzasharuf.networkmonitor.NetworkEvents
import hk.ids.gws.android.sclick.SClick
import kotlinx.android.synthetic.main.activity_item_category_details.*
import kotlinx.android.synthetic.main.toolbar_layout.*

class ItemCategoryDetailsActivity: AppCompatActivity() {
    var is_delete: Boolean = false
    var is_add_edit: Boolean = false
    lateinit var binding: ActivityItemCategoryDetailsBinding
    lateinit var getItemCategoriesModel: GetItemCategoriesModel.Data.ItemCatInfo
    private lateinit var viewModel: ItemCategoryDetailViewModel
    private lateinit var changeStatusItemCategoryModel: ChangeStatusItemCategoryModel

    var isChangeStaus:Boolean = false
    lateinit var loginModel: LoginModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_item_category_details)
        setupViewModel()
        setupUIandListner()
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
        }

        if (!ConnectivityStateHolder.isConnected) {
            // Network is not available
            CommonUtils.showDialog(this, getString(R.string.please_check_internet_msg))
        }
    }
    private fun defaultDisableAllButtonnUI() {
        imgRight.visibility = View.GONE
        imgRight2.visibility = View.GONE
        imgEnd.visibility = View.GONE
        isChangeStaus = false
    }
    private fun defaultEnableAllButtonnUI() {
        imgRight.visibility = View.VISIBLE
        imgRight2.visibility = View.VISIBLE
        imgEnd.visibility = View.GONE
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
                when (data.permission!!.get(i).endsWith(getString(R.string.del), true)) {
                    true -> {
                        is_delete = true
                        if (is_add_edit) {
                            imgRight2.visibility = View.VISIBLE
                            imgEnd.visibility = View.GONE
                            imgRight.visibility = View.VISIBLE
                        } else {
                            //  Log.v("groupdelete", "true")
                            imgRight.visibility = View.VISIBLE
                            imgEnd.visibility = View.GONE
                            imgRight2.visibility = View.GONE
                        }
                    }
                    else->{

                    }
                }
                when (data.permission!!.get(i).endsWith(getString(R.string.add_edit), true)) {
                    true -> {
                        is_add_edit = true
                        if (is_delete) {
                            // Log.v("checkdelete", "true")
                            imgRight2.visibility = View.VISIBLE
                            imgEnd.visibility = View.GONE
                            imgRight.visibility = View.VISIBLE
                        } else {
                            //  Log.v("groupaddedit", "true")
                            imgRight2.visibility = View.GONE
                            imgEnd.visibility = View.VISIBLE
                            imgRight.visibility = View.GONE
                        }
                    }
                    else->{

                    }
                }
                when (data.permission!!.get(i).endsWith(getString(R.string.change_status), true)) {
                    true -> {
                        isChangeStaus = true

                    }else->{

                }
                }
            }
        }
    }

    private fun setupViewModel() {
        viewModel =
            ViewModelProvider(this, ViewModelFactory(ApiHelper(RetrofitBuilder.apiService))).get(
                ItemCategoryDetailViewModel::class.java
            )
        binding.setLifecycleOwner(this)

    }

    private fun setupUIandListner() {

        val prefs = PreferenceHelper.defaultPrefs(this)
        loginModel = Gson().fromJson(
            prefs[Constants.PREF_LOGIN_DETAIL_KEY, ""],
            LoginModel::class.java
        ) //getter

        imgLeft.setImageResource(R.drawable.ic_back)
        imgRight2.setImageResource(R.drawable.ic_edit)
        imgEnd.setImageResource(R.drawable.ic_edit)
        imgRight.setImageResource(R.drawable.ic_delete_icon)
        //tvTitle.setText("ABC Jewellers")

        imgLeft?.clickWithDebounce {
            onBackPressed()
        }

        imgRight2?.clickWithDebounce {
            if (this::getItemCategoriesModel.isInitialized) {
                startActivity(
                    Intent(this, EditItemCategory::class.java)
                        .putExtra(Constants.Change_Status, isChangeStaus)
                        .putExtra(Constants.ITEMCATEGORY_DETAIL_KEY, Gson().toJson(getItemCategoriesModel))

                )
                this.finish()
            }
        }
        imgEnd?.clickWithDebounce {
            if (this::getItemCategoriesModel.isInitialized) {
                startActivity(
                    Intent(this, EditItemCategory::class.java)
                        .putExtra(Constants.ITEMCATEGORY_DETAIL_KEY, Gson().toJson(getItemCategoriesModel))

                )
                this.finish()
            }
        }
        imgRight?.clickWithDebounce {
            if (this::getItemCategoriesModel.isInitialized) {
                // delete category api call
                ensureDeleteDialog(getItemCategoriesModel.category_name.toString())

            }
        }


        if (intent.extras?.containsKey(Constants.ITEMCATEGORY_DETAIL_KEY)!!) {
            var itemcat_str: String? = intent.getStringExtra(Constants.ITEMCATEGORY_DETAIL_KEY)
            getItemCategoriesModel = Gson().fromJson(
                itemcat_str,
                GetItemCategoriesModel.Data.ItemCatInfo::class.java
            )

            tvTitle.text = getItemCategoriesModel.category_name
            tvcatNameItemCatDetail.text = getItemCategoriesModel.category_name
            tvcatCodeItemCatDetail.text = getItemCategoriesModel.category_code
            if(getItemCategoriesModel.status?.toString().equals("1",true)!!){
                tvcatStatusItemCatDetail.text = "Active"
            }
            else{
                tvcatStatusItemCatDetail.text = "Inactive"
            }

            imgLeft?.clickWithDebounce {
                onBackPressed()
            }

        }


    }

    private fun deleteItemCatDialog(customer: String) {
        val builder = AlertDialog.Builder(this)
        val InActiveClick = { dialog: DialogInterface, which: Int ->
            changeStatusItemCatAPI(loginModel?.data?.bearer_access_token,getItemCategoriesModel.id.toString(),"2")

        }
        val dialogdismiss = { dialog: DialogInterface, which: Int ->
            dialog.dismiss()
        }
        with(builder)
        {
            setTitle(context.getString(R.string.delItemCatDialog1Title))
            setMessage(context.getString(R.string.itemCatDialog1Message))
            setPositiveButton(context.getString(R.string.Cancel),dialogdismiss)
            setNeutralButton(context.getString(R.string.mark_as_inactive), DialogInterface.OnClickListener(function = InActiveClick))
            show()
        }
    }

    private fun ensureDeleteDialog(itemCategory: String) {
        val builder = AlertDialog.Builder(this)
        val DeleteClick = { dialog: DialogInterface, which: Int ->
            //itemCategory delete api call
            deleteItemCategory(loginModel?.data?.bearer_access_token,getItemCategoriesModel.id.toString())
        }
        val dialogdismiss = { dialog: DialogInterface, which: Int ->
            dialog.dismiss()
        }
        with(builder)
        {
            setTitle(context.getString(R.string.delItemCatDialog2Title))
            setMessage(context.getString(R.string.itemCatDialog2Message))
            setPositiveButton(context.getString(R.string.Cancel),dialogdismiss)
            setNeutralButton(context.getString(R.string.Delete), DialogInterface.OnClickListener(function = DeleteClick))
            show()
        }
    }
    private fun deleteItemCategory(token: String?, id: String?) {
        if(NetworkUtils.isConnected()) {
            if (CommonUtils.isValidClickPressed()) {
                viewModel.deleteItemCategory(token, id).observe(this, Observer {
                    it?.let { resource ->
                        when (resource.status) {
                            Status.SUCCESS -> {
                                if (it.data?.status == true) {

                                    Toast.makeText(
                                        this,
                                        it.data?.message,
                                        Toast.LENGTH_LONG
                                    )
                                        .show()
                                    finish()

                                } else {

                                    when (it.data!!.code == Constants.ErrorCode) {
                                        true -> {
                                            Toast.makeText(
                                                this,
                                                it.data.errormessage?.message,
                                                Toast.LENGTH_LONG
                                            )
                                                .show()
                                        }
                                        false -> {
                                            CommonUtils.somethingWentWrong(this)
                                        }

                                    }
                                    if (it.data?.data?.status.equals("1")) {
                                        deleteItemCatDialog(getItemCategoriesModel.category_name.toString())
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
    }
    fun changeStatusItemCatAPI(token: String?,
                            item_id: String?,
                            status: String?){

        if(NetworkUtils.isConnected()) {
            if (CommonUtils.isValidClickPressed()) {
                viewModel.changeStatusItemCategory(token, item_id, status).observe(this, Observer {
                    it?.let { resource ->
                        when (resource.status) {
                            Status.SUCCESS -> {
                                if (it.data?.status == true) {

                                    changeStatusItemCategoryModel = it.data
                                    Toast.makeText(
                                        this,
                                        it.data?.message,
                                        Toast.LENGTH_LONG
                                    )
                                        .show()

                                } else {
                                    when (it.data!!.code == Constants.ErrorCode) {
                                        true -> {
                                            Toast.makeText(
                                                this,
                                                it.data.errormessage?.message,
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
                                this.finish()

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
    }
}
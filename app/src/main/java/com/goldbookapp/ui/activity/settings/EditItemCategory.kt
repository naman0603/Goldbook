package com.goldbookapp.ui.activity.settings

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.blankj.utilcode.util.NetworkUtils
import com.goldbookapp.R
import com.goldbookapp.api.ApiHelper
import com.goldbookapp.api.RetrofitBuilder
import com.goldbookapp.databinding.ActivityEditItemCategoryBinding
import com.goldbookapp.model.*
import com.goldbookapp.ui.activity.viewmodel.*
import com.goldbookapp.utils.*
import com.goldbookapp.utils.CommonUtils.Companion.clickWithDebounce
import com.goldbookapp.utils.PreferenceHelper.get
import com.google.gson.Gson
import com.hamzasharuf.networkmonitor.ConnectivityStateHolder
import com.hamzasharuf.networkmonitor.Event
import com.hamzasharuf.networkmonitor.NetworkEvents
import hk.ids.gws.android.sclick.SClick
import kotlinx.android.synthetic.main.activity_edit_item_category.*
import kotlinx.android.synthetic.main.new_item_category.*
import kotlinx.android.synthetic.main.toolbar_layout.*


class EditItemCategory : AppCompatActivity() {

    private lateinit var status: Number
    private lateinit var category_id: String
    private lateinit var viewModel: EditItemCatViewModel
    lateinit var GetItemCategoriesModel: GetItemCategoriesModel.Data.ItemCatInfo
    lateinit var binding: ActivityEditItemCategoryBinding
    lateinit var prefs: SharedPreferences

    private var changeStatus: Boolean = false
    lateinit var loginModel: LoginModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit_item_category)

        setupViewModel()
        setupUIandListner()
    }

    private fun setupViewModel() {
        viewModel =
            ViewModelProvider(this, ViewModelFactory(ApiHelper(RetrofitBuilder.apiService))).get(
                EditItemCatViewModel::class.java
            )
        binding.setLifecycleOwner(this)

    }

    private fun setupUIandListner(){

        prefs = PreferenceHelper.defaultPrefs(this)
        loginModel = Gson().fromJson(
            prefs[Constants.PREF_LOGIN_DETAIL_KEY, ""],
            LoginModel::class.java
        )

        imgLeft.setImageResource(R.drawable.ic_back)
        tvTitle.setText(R.string.edit_itemcategory)


        radiogrpEditItemCat.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { radioGroup, checkedId ->
            when (checkedId) {
                radiocatActiveEditCat.id -> status = 1

                radiocatInActiveEditCat.id -> status = 0

            }
        })


        imgLeft?.clickWithDebounce{
            onBackPressed()
        }

        btnSaveEdit_ItemCat?.clickWithDebounce{
            if (performValidation()) {
                if (NetworkUtils.isConnected()) {
                    if (this::GetItemCategoriesModel.isInitialized)
                        updateItemCategoryAPI(
                            loginModel?.data?.bearer_access_token,
                            txtCatNameEditCat.text.toString().trim(),
                            category_id,
                            txtCatCodeEditCat.text.toString().trim(),
                            status
                        )
                }
            }
        }

        if (intent.extras?.containsKey(Constants.ITEMCATEGORY_DETAIL_KEY)!!) {
            var itemcat_str: String? = intent.getStringExtra(Constants.ITEMCATEGORY_DETAIL_KEY)
            GetItemCategoriesModel = Gson().fromJson(
                itemcat_str,
                com.goldbookapp.model.GetItemCategoriesModel.Data.ItemCatInfo::class.java
            )

           // tvTitle.text = GetItemCategoriesModel.category_name
            txtCatNameEditCat.setText(GetItemCategoriesModel.category_name)
            txtCatCodeEditCat.setText(GetItemCategoriesModel.category_code)
            category_id= GetItemCategoriesModel.id.toString()
            if(GetItemCategoriesModel.status?.toString().equals("1",true)!!){
                radiocatActiveEditCat.isChecked = true
                status = 1
            }
            else{
                radiocatInActiveEditCat.isChecked = true
                status = 0
            }

        }

        if (intent.extras != null && intent.extras!!.containsKey(Constants.Change_Status)) {
            changeStatus = intent.getBooleanExtra(Constants.Change_Status,false)
        }

        when(changeStatus){
            false->{
                if(status == 1){
                    radiocatInActiveEditCat.isEnabled = false
                }else{
                    radiocatActiveEditCat.isEnabled = false
                }
            }else->{

        }
        }

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

        }

        if (!ConnectivityStateHolder.isConnected) {
            // Network is not available
            CommonUtils.showDialog(this, getString(R.string.please_check_internet_msg))

        }
    }



    fun performValidation(): Boolean {
        if(txtCatNameEditCat.text.toString().isBlank()){
            CommonUtils.showDialog(this, getString(R.string.enter_category_name_msg))
            txtCatNameEditCat.requestFocus()
            return false
        }else if(txtCatCodeEditCat.text.toString().isBlank()){
            CommonUtils.showDialog(this, getString(R.string.enter_category_code_msg))
            txtCatCodeEditCat.requestFocus()
            return false
        }
        else if(radiogrpEditItemCat.getCheckedRadioButtonId() == -1){ // no radio buttons are checked
            Toast.makeText(
                this,
                getString(R.string.check_status_active_inactive_msg),
                Toast.LENGTH_LONG
            ).show()
            return false
        }
        return true
    }


    fun updateItemCategoryAPI(token: String?,
                           category_name: String?,
                              category_id : String?,
                           category_code: String?,
                           status: Number?){

        viewModel.updateItemCategory(token, category_name,category_id,
            category_code,
            status
        ).observe(this, Observer {
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

                            onBackPressed()

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
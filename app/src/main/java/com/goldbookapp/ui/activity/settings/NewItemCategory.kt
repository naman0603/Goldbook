package com.goldbookapp.ui.activity.settings

import android.content.Intent
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
import com.goldbookapp.databinding.NewItemCategoryBinding
import com.goldbookapp.model.*
import com.goldbookapp.ui.activity.viewmodel.NewItemCatViewModel
import com.goldbookapp.ui.activity.viewmodel.ViewModelFactory
import com.goldbookapp.utils.*
import com.goldbookapp.utils.CommonUtils.Companion.clickWithDebounce
import com.goldbookapp.utils.PreferenceHelper.get
import com.google.gson.Gson
import com.hamzasharuf.networkmonitor.ConnectivityStateHolder
import com.hamzasharuf.networkmonitor.Event
import com.hamzasharuf.networkmonitor.NetworkEvents
import hk.ids.gws.android.sclick.SClick
import kotlinx.android.synthetic.main.activity_inventory_metal.*
import kotlinx.android.synthetic.main.activity_new_cheque_book.*
import kotlinx.android.synthetic.main.new_item_category.*
import kotlinx.android.synthetic.main.toolbar_layout.*

class NewItemCategory : AppCompatActivity() {

    private lateinit var status: String
    private lateinit var viewModel: NewItemCatViewModel
    lateinit var binding: NewItemCategoryBinding
    lateinit var newItemCatModel: NewItemCatModel.Data.Item
    lateinit var prefs: SharedPreferences

    var itemSaveAdd: String? = "0"
    private var changeStatus: Boolean = false

    lateinit var loginModel: LoginModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.new_item_category)

        setupViewModel()
        setupUIandListner()


    }

    private fun setupViewModel() {
        viewModel =
            ViewModelProvider(this, ViewModelFactory(ApiHelper(RetrofitBuilder.apiService))).get(
                NewItemCatViewModel::class.java
            )
        binding.setLifecycleOwner(this)

    }

    private fun setupUIandListner() {

        prefs = PreferenceHelper.defaultPrefs(this)
        loginModel = Gson().fromJson(
            prefs[Constants.PREF_LOGIN_DETAIL_KEY, ""],
            LoginModel::class.java
        )

        if (intent.extras != null && intent.extras!!.containsKey(Constants.ITEM_SAVE_TYPE)) {
            itemSaveAdd = intent.getStringExtra(Constants.ITEM_SAVE_TYPE)
        }
        if (itemSaveAdd == "1") {
            txtCatNameNewCat.clearFocus()
            txtCatCodeNewCat.clearFocus()
        }

        if (intent.extras != null && intent.extras!!.containsKey(Constants.Change_Status)) {
            changeStatus = intent.getBooleanExtra(Constants.Change_Status, false)
        }
        when (loginModel.data!!.user_info!!.user_type.equals("user", true)) {
            // user type user
            true -> {
                when (changeStatus) {
                    false -> {
                        radiocatInActiveNewCat.isEnabled = false
                    }else->{

                }
                }
            }else->{

        }
        }

        imgLeft.setImageResource(R.drawable.ic_back)
        tvTitle.setText(R.string.new_itemcategory)

        status = "1"
        radiogrpNewItemCat.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { radioGroup, checkedId ->
            when (checkedId) {
                radiocatActiveNewCat.id -> status = "1"

                radiocatInActiveNewCat.id -> status = "0"

            }
        })


        imgLeft?.clickWithDebounce {
            onBackPressed()
        }

        btnSaveAdd_AddItem?.clickWithDebounce {
            if (performValidation()) {
                if (NetworkUtils.isConnected()) {
                    addItemCategoryAPI(
                        loginModel?.data?.bearer_access_token,
                        txtCatNameNewCat.text.toString().trim(),
                        txtCatCodeNewCat.text.toString().trim(),
                        status,
                        true
                    )

                }
            }
        }
        btnSaveCloseAddItem?.clickWithDebounce {
            if (NetworkUtils.isConnected()) {
                addItemCategoryAPI(
                    loginModel?.data?.bearer_access_token,
                    txtCatNameNewCat.text.toString().trim(),
                    txtCatCodeNewCat.text.toString().trim(),
                    status,
                    false
                )

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
        if (txtCatNameNewCat.text.toString().isBlank()) {
            CommonUtils.showDialog(this, getString(R.string.enter_category_name_msg))
            txtCatNameNewCat.requestFocus()
            return false
        } else if (txtCatCodeNewCat.text.toString().isBlank()) {
            //tvGenderEditInputLayout?.error = getString(R.string.gender_validation_msg)
            CommonUtils.showDialog(this, getString(R.string.enter_category_code_msg))
            txtCatCodeNewCat.requestFocus()
            return false
        } else if (radiogrpNewItemCat.getCheckedRadioButtonId() == -1) { // no radio buttons are checked
            Toast.makeText(
                this,
                getString(R.string.check_status_active_inactive_msg),
                Toast.LENGTH_LONG
            ).show()
            return false
        }
        return true
    }


    fun addItemCategoryAPI(
        token: String?,
        category_name: String?,
        category_code: String?,
        status: String?,
        is_from_saveAdd: Boolean
    ) {
        if (NetworkUtils.isConnected()) {
            viewModel.addItemCategory(
                token, category_name,
                category_code,
                status

            ).observe(this, Observer {
                it?.let { resource ->
                    when (resource.status) {
                        Status.SUCCESS -> {
                            if (it.data?.status == true) {
                                newItemCatModel = it?.data.data?.item!!
                                Toast.makeText(
                                    this,
                                    it.data?.message,
                                    Toast.LENGTH_LONG
                                )
                                    .show()

                                when (is_from_saveAdd) {
                                    true -> {
                                        this.finish()
                                        startActivity(
                                            Intent(
                                                this,
                                                NewItemCategory::class.java
                                            ).putExtra(Constants.ITEM_SAVE_TYPE, "1")
                                        )
                                    }
                                    false -> onBackPressed()
                                }


                            } else {
                                /* Toast.makeText(
                                     this,
                                     it.data?.errormessage?.message,
                                     Toast.LENGTH_LONG
                                 )
                                     .show()*/
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
                        }
                        Status.ERROR -> {
                            CommonUtils.hideProgress()
                            /* Toast.makeText(this, it.data?.errormessage?.message, Toast.LENGTH_LONG)
                                 .show()*/
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
package com.goldbookapp.ui.activity.branch

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
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
import com.goldbookapp.databinding.NewCompanybranchActivityBinding
import com.goldbookapp.model.*
import com.goldbookapp.ui.activity.organization.CompanyAddressDetailsActivity
import com.goldbookapp.ui.activity.viewmodel.NewCompanyBranchViewModel
import com.goldbookapp.ui.activity.viewmodel.ViewModelFactory
import com.goldbookapp.utils.*
import com.goldbookapp.utils.CommonUtils.Companion.clickWithDebounce
import com.goldbookapp.utils.PreferenceHelper.get
import com.goldbookapp.utils.PreferenceHelper.set
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import com.hamzasharuf.networkmonitor.ConnectivityStateHolder
import com.hamzasharuf.networkmonitor.Event
import com.hamzasharuf.networkmonitor.NetworkEvents
import kotlinx.android.synthetic.main.new_companybranch_activity.*
import kotlinx.android.synthetic.main.toolbar_layout.*
import java.lang.StringBuilder

class NewCompanyBranch : AppCompatActivity() {

    private lateinit var isGSTRegistered: String
    private lateinit var viewModel: NewCompanyBranchViewModel
    lateinit var binding: NewCompanybranchActivityBinding

    lateinit var prefs: SharedPreferences

    lateinit var branchAddressModel: BranchAddressModel


    var selectedTermBalanceID: String? = null

    lateinit var loginModel: LoginModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.new_companybranch_activity)


        setupViewModel()
        setupUIandListner()


    }

    private fun setupViewModel() {
        viewModel =
            ViewModelProvider(this, ViewModelFactory(ApiHelper(RetrofitBuilder.apiService))).get(
                NewCompanyBranchViewModel::class.java
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
        tvTitle.setText(R.string.new_branch)

        prefs.edit().remove(Constants.PREF_BRANCH_ADDRESS_KEY).apply()

        imgLeft?.clickWithDebounce{
            onBackPressed()
        }

        cardCompanyAddNewBranch.clickWithDebounce {
            startActivity(
                Intent(
                    this,
                    CompanyAddressDetailsActivity::class.java
                ).putExtra("isFromBranch", true)
            )
        }

        btnSaveAdd_Branch?.clickWithDebounce {
            if (performValidation()) {
                if (NetworkUtils.isConnected()) {
                    addCompanyBranchAPI(
                        loginModel?.data?.bearer_access_token,
                        txtBranchNameNewBranch.text.toString().trim(),
                        txtBranchCodeNewBranch.text.toString().trim(),
                        branchAddressModel.branch_address.toString().trim(),
                        txtMobileNewBranch.text.toString().trim(),
                        txtAddNumberNewBranch.text.toString().trim(),
                        txtFirstNameNewBranch.text.toString().trim(),
                        txtLastNameNewBranch.text.toString().trim(),
                        txtEmailNewBranch.text.toString().trim(),
                        branchAddressModel.country_id.toString().trim(),
                        branchAddressModel.state_id.toString(),
                        branchAddressModel.city_id.toString(),
                        branchAddressModel.area.toString().trim(),
                        branchAddressModel.landmark.toString().trim(),
                        isGSTRegistered,
                        txtGSTINNewBranch.text.toString().trim(),
                        branchAddressModel.pincode
                    )
                }
            }
        }

        if(loginModel?.data?.company_info?.gst_register?.equals("1",true)!!){
                radiobtnGSTYesNewBranch.isChecked = true
                radiobtnGSTYesNewBranch.isEnabled = false
                radiobtnGSTNoNewBranch.isEnabled = false
                isGSTRegistered = "1"
                tvGSTINNewBranch.visibility = View.VISIBLE
                radiogrpNewBranch.isEnabled = true


        }else{
             isGSTRegistered = "0"
            tvGSTINNewBranch.visibility = View.GONE
            radiobtnGSTNoNewBranch.isChecked = true
            radiogrpNewBranch.isEnabled = false
            radiobtnGSTYesNewBranch.isEnabled = false
            radiobtnGSTNoNewBranch.isEnabled = false
        }

        radiogrpNewBranch.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { radioGroup, checkedId ->
            when (checkedId) {
                radiobtnGSTYesNewBranch.id -> {tvGSTINNewBranch.isEnabled = true; isGSTRegistered = "1"
                    tvGSTINNewBranch.visibility = View.VISIBLE}
                radiobtnGSTNoNewBranch.id -> {tvGSTINNewBranch.isEnabled = false; isGSTRegistered = "0"
                    tvGSTINNewBranch.visibility = View.GONE}
            }
        })
        saveBranchAddressModel()


    }
    fun saveBranchAddressModel(){
        branchAddressModel = BranchAddressModel(
            "",
            "",
            "",
            loginModel.data?.company_info?.country_id.toString(),
            loginModel.data?.company_info?.country_name,
            loginModel.data?.company_info?.state_id.toString(),
            loginModel.data?.company_info?.state_name,
                loginModel.data?.company_info?.city_id.toString(),
            loginModel.data?.company_info?.city_name,
            ""
        )

        val prefs = PreferenceHelper.defaultPrefs(this)
        prefs[Constants.PREF_BRANCH_ADDRESS_KEY] = Gson().toJson(branchAddressModel) //setter

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


        if (prefs.contains(Constants.PREF_BRANCH_ADDRESS_KEY) ) {
            var branchAddress: BranchAddressModel = Gson().fromJson(
                prefs[Constants.PREF_BRANCH_ADDRESS_KEY, ""],
                BranchAddressModel::class.java
            )
            var addressStringBuilder: StringBuilder = StringBuilder()
            addressStringBuilder
                .append(branchAddress.branch_address.toString().trim()).append(", ")
                .append(branchAddress.area.toString().trim()).append(", ")
                .append(branchAddress.landmark.toString().trim()).append(", ")
                .append(branchAddress.country_name.toString().trim()).append(", ")
                .append(branchAddress.state_name.toString().trim()).append(", ")
                .append(branchAddress.city_name.toString().trim()).append(", ")
                .append(branchAddress.pincode.toString().trim()).append(", ")

            tv_branch_address.text = CommonUtils.removeUnwantedComma(addressStringBuilder.toString())

            tvAddAddressNewBranch.visibility = View.GONE
            linear_branch_address.visibility = View.VISIBLE
        }
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
        if(txtFirstNameNewBranch.text.toString().isBlank()){
            CommonUtils.showDialog(this, "Please Enter First Name")
            txtFirstNameNewBranch.requestFocus()
            return false
        }
        else if(txtBranchNameNewBranch.text.toString().isBlank()){
            CommonUtils.showDialog(this, "Please Enter Branch Name")
            txtBranchNameNewBranch.requestFocus()
            return false
        }
        else if(txtBranchCodeNewBranch.text.toString().isBlank()){
            CommonUtils.showDialog(this, "Please Enter Branch Code")
            txtBranchCodeNewBranch.requestFocus()
            return false
        }

        else if(txtMobileNewBranch.text.toString().isBlank()){
            CommonUtils.showDialog(this, "Please Enter Mobile Number")
            txtMobileNewBranch.requestFocus()
            return false
        }
        else if(txtMobileNewBranch.text?.length!! < 10 ){
            CommonUtils.showDialog(this, "Please Enter Valid Mobile Number")
            txtMobileNewBranch.requestFocus()
            return false
        }

        else if(!txtEmailNewBranch.text.toString().isBlank() && !CommonUtils.isValidEmail(txtEmailNewBranch.text.toString())){
            CommonUtils.showDialog(this, getString(R.string.email_validation_msg))
            txtEmailNewBranch.requestFocus()
            return false
        }else if (!prefs.contains(Constants.PREF_BRANCH_ADDRESS_KEY)) {
            CommonUtils.showDialog(this, getString(R.string.enter_address_details_msg))
            return false
        }
        else if(radiogrpNewBranch.getCheckedRadioButtonId() == -1){ // no radio buttons are checked
            Toast.makeText(this, getString(R.string.select_radio_gst_reg_branch_yesno_msg), Toast.LENGTH_LONG).show()
            radiogrpNewBranch.requestFocus()
            return false
        }else if(radiobtnGSTYesNewBranch.isChecked && txtGSTINNewBranch.text.toString().isBlank()){
            tvGSTINNewBranch.error = getString(R.string.enter_gstin_msg)/*"Please Enter GSTIN"*/
            txtGSTINNewBranch.requestFocus()
            return false
        }
        else if(radiobtnGSTYesNewBranch.isChecked && txtGSTINNewBranch.text?.length!! < 15){
            tvGSTINNewBranch.error = getString(R.string.enter_valid_gstin_msg)/*"Please Enter Valid GSTIN"*/
            txtGSTINNewBranch.requestFocus()
            return false
        }

        branchAddressModel = Gson().fromJson(
            prefs[Constants.PREF_BRANCH_ADDRESS_KEY, ""],
            BranchAddressModel::class.java
        )

        return true
    }



    fun openNameTitlePopup(view: View?){
        val popupMenu: PopupMenu = PopupMenu(this, view)
        popupMenu.menu.add("Mr")
        popupMenu.menu.add("Mrs")
        popupMenu.menu.add("Ms")

        popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
            (view as TextInputEditText).setText(item.title)
            true
        })

        popupMenu.show()
    }

    fun addCompanyBranchAPI(token: String?,
                            branch_name: String?,
                            branch_code: String?,
                            branch_address: String?,
                            branch_contact_no: String?,
                            secondary_contact:String?,
                            contact_person_fname: String?,
                            contact_person_lname: String?,
                            branch_email: String?,
                            business_location: String?,
                            state_id: String?,
                            city_id: String?,
                            area: String?,
                            landmark: String?,
                            gst_register: String?,
                            gst_tin_number: String?,
                            pincode : String?){

        viewModel.addCompanyBranch(token, branch_name,
            branch_code,
            branch_address,
            branch_contact_no,
            secondary_contact,
            contact_person_fname,
            contact_person_lname,
            branch_email,
            business_location,
            state_id,
            city_id,
            area,
            landmark,
            gst_register,
            gst_tin_number,pincode).observe(this, Observer {
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
                        Toast.makeText(this, it.data?.errormessage?.message, Toast.LENGTH_LONG)
                            .show()
                    }
                    Status.LOADING -> {
                        CommonUtils.showProgress(this)
                    }
                }
            }
        })
    }

}
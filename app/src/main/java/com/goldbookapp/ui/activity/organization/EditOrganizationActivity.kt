package com.goldbookapp.ui.activity.organization

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.blankj.utilcode.util.NetworkUtils
import com.goldbookapp.R
import com.goldbookapp.api.ApiHelper
import com.goldbookapp.api.RetrofitBuilder
import com.goldbookapp.databinding.EditOrganizationActivityBinding
import com.goldbookapp.model.CompanyAddressModel
import com.goldbookapp.model.GetUserCompanyModel
import com.goldbookapp.model.LoginModel
import com.goldbookapp.ui.activity.viewmodel.EditOrganizationViewModel
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
import kotlinx.android.synthetic.main.edit_organization_activity.*
import kotlinx.android.synthetic.main.toolbar_layout.*


class EditOrganizationActivity : AppCompatActivity() {

    private lateinit var viewModel: EditOrganizationViewModel
    lateinit var binding: EditOrganizationActivityBinding

    lateinit var prefs: SharedPreferences
    lateinit var orgDetailModel: GetUserCompanyModel.Data

    lateinit var companyAddress: CompanyAddressModel
    var isGSTRegistered: String = "0"
    lateinit var loginModel: LoginModel

    var companyID: String? = ""
    var selectedTermBalanceID: String? = null
    var selectedTermBalanceName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.edit_organization_activity)

        setupViewModel()
        setupUIandListner()


    }

    private fun setupViewModel() {
        viewModel =
            ViewModelProvider(this, ViewModelFactory(ApiHelper(RetrofitBuilder.apiService))).get(
                EditOrganizationViewModel::class.java
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
        tvTitle.setText(R.string.edit_organization)

        prefs.edit().remove(Constants.PREF_COMPANY_ADDRESS_KEY).apply()

        imgLeft?.clickWithDebounce{
            onBackPressed()
        }
        btnSaveEdit_Org?.clickWithDebounce{
            if (performValidation()) {
                if (NetworkUtils.isConnected()) {
                    updateUserCompanyAPI(
                        loginModel?.data?.bearer_access_token,
                        orgDetailModel.id,
                        txtOrgNameEditOrg.text.toString().trim(),
                        companyAddress.location.toString().trim(),
                        companyAddress.area.toString().toString().trim(),
                        companyAddress.landmark.toString().trim(),
                        companyAddress.country_id.toString(),
                        companyAddress.state_id.toString(),
                        companyAddress.city_id.toString(),
                        companyAddress.pincode.toString().trim(),
                        txtFirstNameEditOrg.text.toString().trim(),
                        txtLastNameEditOrg.text.toString().trim(),
                        txtMobileEditOrg.text.toString().trim(),
                        txtAddNumberEditOrg.text.toString().trim(),
                        txtEmailEditOrg.text.toString().trim(),
                        orgDetailModel.fiscal_year_id,
                        txtPanEditOrg.text.toString().trim(),
                        txtCorpIdNoEditOrg.text.toString().trim(),
                        selectedTermBalanceID,
                        selectedTermBalanceName,
                        isGSTRegistered,
                        txtGSTINEditOrg.text.toString().trim()
                    )
                }
            }
        }
        cardCompanyAddEditOrg?.clickWithDebounce{
            startActivity(
                Intent(
                    this,
                    CompanyAddressDetailsActivity::class.java
                )
            )
        }


        txtTermBalEditOrg.clickWithDebounce {
            openDefaultTermPopup(txtTermBalEditOrg)
        }


        txtGSTINEditOrg.doAfterTextChanged { tvGSTINEditOrg.error = null }
        if(intent.extras?.containsKey(Constants.ORGS_DETAIL_KEY)!!) {
            var company_str: String? = intent.getStringExtra(Constants.ORGS_DETAIL_KEY)
            orgDetailModel = Gson().fromJson(
                company_str,
                GetUserCompanyModel.Data::class.java
            )

            companyID = orgDetailModel.id.toString()
            //selectedTermBalanceID="1"

            setData(orgDetailModel);
        }


    }
    fun openDefaultTermPopup(view: View){
        val popupMenu: PopupMenu = PopupMenu(this, view)
        popupMenu.menu.add(Menu.NONE, 1, 1, getString(R.string.debit_credit)) //add(groupId, itemId, order, title);
        popupMenu.menu.add(Menu.NONE, 2, 2,getString(R.string.udhar_jama))
        popupMenu.menu.add(Menu.NONE, 3, 3,R.string.rec_pay)
        popupMenu.menu.add(Menu.NONE, 4, 4,R.string.len_den)

        popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
            when(item.itemId.toString()){
                // 1-> true //0-> false
                "1" -> {
                    selectedTermBalanceID = "1"
                    selectedTermBalanceName = "setting_debit_credit"
                }
                "2" -> {
                    selectedTermBalanceID = "2"
                    selectedTermBalanceName = "setting_udhar_jama"
                }
                "3" -> {
                    selectedTermBalanceID = "3"
                    selectedTermBalanceName = "setting_receivable_payable"

                }
                "4" -> {
                    selectedTermBalanceID = "4"
                    selectedTermBalanceName = "setting_len_den"

                }
            }
            txtTermBalEditOrg.setText(item.title)
           // selectedTermBalanceID = item.itemId.toString()
            true
        })

        popupMenu.show()
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

        if (prefs.contains(Constants.PREF_COMPANY_ADDRESS_KEY) ) {
            var companyAddress: CompanyAddressModel = Gson().fromJson(
                prefs[Constants.PREF_COMPANY_ADDRESS_KEY, ""],
                CompanyAddressModel::class.java
            )
            var addressStringBuilder: java.lang.StringBuilder = java.lang.StringBuilder()
            addressStringBuilder
                .append(companyAddress.location.toString().trim()).append(", ")
                .append(companyAddress.area.toString().trim()).append(", ")
                .append(companyAddress.landmark.toString().trim()).append(", ")
                .append(companyAddress.country_name.toString().trim()).append(", ")
                .append(companyAddress.state_name.toString().trim()).append(", ")
                .append(companyAddress.city_name.toString().trim()).append(", ")
                .append(companyAddress.pincode.toString().trim()).append(", ")

            tv_company_addressEditOrg.text = CommonUtils.removeUnwantedComma(addressStringBuilder.toString())

            tvAddAddressEditOrg.visibility = View.GONE
            linear_company_addressEditOrg.visibility = View.VISIBLE


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



    fun setData(orgDetailModel: GetUserCompanyModel.Data){
        txtFirstNameEditOrg.setText(orgDetailModel.contact_person_first_name)
        txtLastNameEditOrg.setText(orgDetailModel.contact_person_last_name)
        txtOrgNameEditOrg.setText(orgDetailModel.company_name)
        txtMobileEditOrg.setText(orgDetailModel.mobile_number)
        txtAddNumberEditOrg.setText(orgDetailModel.alternate_number)
        txtEmailEditOrg.setText(orgDetailModel.email)



        var addressStringBuilder: java.lang.StringBuilder = java.lang.StringBuilder()

            saveAddressmodel(false)


        // disable gst or not gst checkbox to change company
        radiobtnGSTYesEditOrg.isEnabled = false
        radiobtnGSTNoEditOrg.isEnabled = false

        if(orgDetailModel?.gst_register == "1"){
            isGSTRegistered = "1"
            radiobtnGSTYesEditOrg.isChecked = true
            txtGSTINEditOrg.setText(orgDetailModel?.gst_tin_number)
        }else{
            isGSTRegistered = "0"
            tvGSTINEditOrg.visibility = View.GONE
            tvPANEditOrg.visibility = View.GONE
            radiobtnGSTNoEditOrg.isChecked = true
        }

        txtFinYearEditOrg.setText(orgDetailModel.fiscal_year_name)
       // if(orgDetailModel.default_term_balance.equals("1",ignoreCase = true))
        selectedTermBalanceID = orgDetailModel.default_term_balance

            when(orgDetailModel.default_term_balance){
                "1"->{
                    txtTermBalEditOrg.setText(getString(R.string.debit_credit))
                }
                "2"->{
                    txtTermBalEditOrg.setText(getString(R.string.udhar_jama))
                }
                "3"->{
                    txtTermBalEditOrg.setText(getString(R.string.rec_pay))
                }
                "4"->{
                    txtTermBalEditOrg.setText(getString(R.string.len_den))
                }
            }
        selectedTermBalanceName = txtTermBalEditOrg.text.toString()
        txtPanEditOrg.setText(orgDetailModel.pan_number)
        txtCorpIdNoEditOrg.setText(orgDetailModel.cin_number)

    }
    fun saveAddressmodel(removePref:Boolean){
        val childBranchModel = CompanyAddressModel(
            orgDetailModel.reg_address.toString().trim(),
            orgDetailModel.area.toString().trim(),
            orgDetailModel.landmark.toString().trim(),
            orgDetailModel.country_id.toString().trim(),
            orgDetailModel.country_name.toString().trim(),
            orgDetailModel.state_id.toString().trim(),
            orgDetailModel.state_name.toString().trim(),
            orgDetailModel.city_id.toString().trim(),
            orgDetailModel.city_name.toString().trim(),
            orgDetailModel.postal_code.toString().trim()
        )

        prefs = PreferenceHelper.defaultPrefs(this)
        if(removePref) prefs.edit().remove(Constants.PREF_COMPANY_ADDRESS_KEY).apply()
        else
            prefs[Constants.PREF_COMPANY_ADDRESS_KEY] = Gson().toJson(childBranchModel) //setter
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
    fun performValidation(): Boolean {
        companyAddress = Gson().fromJson(
            prefs[Constants.PREF_COMPANY_ADDRESS_KEY, ""],
            CompanyAddressModel::class.java
        )
        if(txtFirstNameEditOrg.text.toString().isBlank()){
            CommonUtils.showDialog(this, "Please Enter First Name")
            txtFirstNameEditOrg.requestFocus()
            return false
        }else if(txtLastNameEditOrg.text.toString().isBlank()){
            //tvGenderEditInputLayout?.error = getString(R.string.gender_validation_msg)
            CommonUtils.showDialog(this, "Please Enter Last Name")
            txtLastNameEditOrg.requestFocus()
            return false
        }
        else if(txtOrgNameEditOrg.text.toString().isBlank()){
            CommonUtils.showDialog(this, "Please Enter Organization Name")
            txtOrgNameEditOrg.requestFocus()
            return false
        }

        else if(txtMobileEditOrg.text.toString().isBlank()){
            CommonUtils.showDialog(this, "Please Enter Mobile Number")
            txtMobileEditOrg.requestFocus()
            return false
        }
        else if(txtMobileEditOrg.text?.length!! < 10){
            CommonUtils.showDialog(this, "Please Enter Valid Mobile Number")
            txtMobileEditOrg.requestFocus()
            return false
        }

        else if(txtEmailEditOrg.text.toString().isBlank()){
            CommonUtils.showDialog(this, "Please Enter Email")
            txtEmailEditOrg.requestFocus()
            return false
        }
        else if(txtEmailEditOrg.text.toString().isBlank() || !CommonUtils.isValidEmail(txtEmailEditOrg.text.toString())){
            CommonUtils.showDialog(this, getString(R.string.email_validation_msg))
            txtEmailEditOrg.requestFocus()
            return false
        }else if (!prefs.contains(Constants.PREF_COMPANY_ADDRESS_KEY)) {
            CommonUtils.showDialog(this, "Please Enter Address Details")
            return false
        }
        else if (companyAddress.location.isNullOrBlank()) {
            CommonUtils.showDialog(this, "Please Enter Complete Company Address Details")
            return false
        }
       else if(txtTermBalEditOrg.text.toString().isBlank()){
            CommonUtils.showDialog(this, getString(R.string.enter_term_balance_msg))
            txtTermBalEditOrg.requestFocus()
            return false
        }
        else if(radiogrpEditOrg.getCheckedRadioButtonId() == -1){ // no radio buttons are checked
            Toast.makeText(this, getString(R.string.select_radio_gst_reg_company_yesno_msg), Toast.LENGTH_LONG).show()
            return false
        }else if(radiobtnGSTYesEditOrg.isChecked && txtGSTINEditOrg.text.toString().isBlank()){

            CommonUtils.showDialog(this,getString(R.string.enter_gstin_msg))
            return false
        }
        else if(radiobtnGSTYesEditOrg.isChecked && !CommonUtils.isValidGSTNo(txtGSTINEditOrg.text.toString())/*txtGSTINEditOrg.text!!.length < 15*/){

            CommonUtils.showDialog(this,getString(R.string.enter_valid_gstin_msg))
            return false
        }

        else if(radiobtnGSTYesEditOrg.isChecked && (txtPanEditOrg.text.toString().isBlank() || !CommonUtils.isValidPANDetail(txtPanEditOrg.text.toString()))){
            CommonUtils.showDialog(this, getString(R.string.enter_correct_pandetails_msg))
            txtPanEditOrg.requestFocus()
            return false
        }

        return true
    }

    fun updateUserCompanyAPI(token: String?,
                             company_id : Number?,
                           company_name: String?,
                           reg_address: String?,
                           area: String?,
                           landmark: String?,
                           country_id:String?,
                           state_id: String?,
                           city_id: String?,
                           postal_code: String?,
                           contact_person_first_name: String?,
                           contact_person_last_name: String?,
                           mobile_number: String?,
                           alternate_number: String?,
                           email: String?,
                           fiscal_year_id: String?,
                           pan_number: String?,
                           cin_number: String?,
                           term_balance: String?,
                             default_term: String?,
                             gst_register: String?,
                             gst_tin_number: String?){

        viewModel.updateUserCompany(token, company_id,company_name,
            reg_address,
            area,
            landmark,
            country_id,
            state_id,
            city_id,
            postal_code,
            contact_person_first_name,
            contact_person_last_name,
            mobile_number,
            alternate_number,
            email,
            fiscal_year_id,
            pan_number,
            cin_number,
            term_balance,
            default_term,
            gst_register,
            gst_tin_number
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
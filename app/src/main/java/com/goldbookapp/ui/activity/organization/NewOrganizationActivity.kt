package com.goldbookapp.ui.activity.organization

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.PopupMenu
import android.widget.RadioGroup
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
import com.goldbookapp.databinding.NewOrganizationActivityBinding
import com.goldbookapp.model.CompanyAddressModel
import com.goldbookapp.model.GetItemCategoriesModel
import com.goldbookapp.model.LoginModel
import com.goldbookapp.ui.activity.viewmodel.NewOrgnizationViewModel
import com.goldbookapp.ui.activity.viewmodel.ViewModelFactory
import com.goldbookapp.utils.CommonUtils
import com.goldbookapp.utils.CommonUtils.Companion.clickWithDebounce
import com.goldbookapp.utils.Constants
import com.goldbookapp.utils.PreferenceHelper
import com.goldbookapp.utils.PreferenceHelper.get
import com.goldbookapp.utils.PreferenceHelper.set
import com.goldbookapp.utils.Status
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import com.hamzasharuf.networkmonitor.ConnectivityStateHolder
import com.hamzasharuf.networkmonitor.Event
import com.hamzasharuf.networkmonitor.NetworkEvents
import kotlinx.android.synthetic.main.new_organization_activity.*
import kotlinx.android.synthetic.main.toolbar_layout.*

class NewOrganizationActivity : AppCompatActivity() {

    lateinit var selectedTermBalanceID: String
    lateinit var selectedTermBalanceName: String
    lateinit var companyAddress: CompanyAddressModel
    var isGSTRegistered: String = "1"
    private lateinit var viewModel: NewOrgnizationViewModel
    lateinit var binding: NewOrganizationActivityBinding

    lateinit var prefs: SharedPreferences

    lateinit var getItemCategoriesModel: GetItemCategoriesModel.Data.ItemCatInfo

    lateinit var loginModel: LoginModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.new_organization_activity)

        setupViewModel()
        setupUIandListner()


    }

    private fun setupViewModel() {
        viewModel =
            ViewModelProvider(this, ViewModelFactory(ApiHelper(RetrofitBuilder.apiService))).get(
                NewOrgnizationViewModel::class.java
            )
        binding.setLifecycleOwner(this)

    }

    private fun setupUIandListner() {

        prefs = PreferenceHelper.defaultPrefs(this)
        loginModel = Gson().fromJson(
            prefs[Constants.PREF_LOGIN_DETAIL_KEY, ""],
            LoginModel::class.java
        )

        imgLeft.setImageResource(R.drawable.ic_back)
        tvTitle.setText(R.string.new_organization)

        prefs.edit().remove(Constants.PREF_COMPANY_ADDRESS_KEY).apply()


        imgLeft?.clickWithDebounce {
            onBackPressed()
        }

        btnSaveAdd_Org?.clickWithDebounce {
            if (performValidation()) {
                if (NetworkUtils.isConnected()) {
                    addOrganizationAPI(
                        loginModel?.data?.bearer_access_token,
                        txtOrgNameNewOrg.text.toString().trim(),
                        companyAddress.location.toString().trim(),
                        companyAddress.area.toString().toString().trim(),
                        companyAddress.landmark.toString().trim(),
                        companyAddress.country_id.toString(),
                        companyAddress.state_id.toString(),
                        companyAddress.city_id.toString(),
                        companyAddress.pincode.toString().trim(),
                        txtFirstNameNewOrg.text.toString().trim(),
                        txtLastNameNewOrg.text.toString().trim(),
                        txtMobileNewOrg.text.toString().trim(),
                        txtAddNumberNewOrg.text.toString().trim(),
                        txtEmailNewOrg.text.toString()
                            .trim(), /*txtFinYearNewOrg.text.toString().trim()*/
                        "1",
                        txtPanNewOrg.text.toString().trim(),
                        txtCorpIdNoNewOrg.text.toString().trim(),
                        selectedTermBalanceID,
                        selectedTermBalanceName,
                        isGSTRegistered,
                        txtGSTINNewOrg.text.toString().trim()
                    )
                }
            }
        }

        cardCompanyAddNewOrg.clickWithDebounce {
            startActivity(
                Intent(
                    this,
                    CompanyAddressDetailsActivity::class.java
                )
            )
        }
        txtTermBalNewOrg.doAfterTextChanged { tvTermBalNewOrg.error = null }
        radiogrpNewOrg.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { radioGroup, checkedId ->
            when (checkedId) {
                radiobtnGSTYesNewOrg.id -> {
                    tvGSTINNewOrg.isEnabled = true; isGSTRegistered = "1"
                    tvGSTINNewOrg.visibility = View.VISIBLE
                    tvPANNewOrg.visibility = View.VISIBLE
                }
                radiobtnGSTNoNewOrg.id -> {
                    tvGSTINNewOrg.isEnabled = false; isGSTRegistered = "0"
                    tvGSTINNewOrg.visibility = View.GONE
                    tvPANNewOrg.visibility = View.GONE
                }
            }
        })
        txtTermBalNewOrg.clickWithDebounce {

            openDefaultTermPopup(txtTermBalNewOrg)
        }
        txtGSTINNewOrg.doAfterTextChanged { tvGSTINNewOrg.error = null }
        saveCompanyAddressModel()
    }

    fun saveCompanyAddressModel() {


        companyAddress = CompanyAddressModel(
            "",
            "",
            "",
            loginModel.data?.company_info?.country_id.toString(),
            loginModel.data?.company_info?.country_name,
            "",
            "",
            "",
            "",
            ""
        )

        val prefs = PreferenceHelper.defaultPrefs(this)
        prefs[Constants.PREF_COMPANY_ADDRESS_KEY] = Gson().toJson(companyAddress) //setter

    }

    fun openDefaultTermPopup(view: View) {
        val popupMenu: PopupMenu = PopupMenu(this, view)
        popupMenu.menu.add(
            Menu.NONE,
            1,
            1,
            getString(R.string.debit_credit)
        ) //add(groupId, itemId, order, title);
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
            txtTermBalNewOrg.setText(item.title)
          //  selectedTermBalanceID = item.itemId.toString()
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


        if (prefs.contains(Constants.PREF_COMPANY_ADDRESS_KEY)) {
            companyAddress = Gson().fromJson(
                prefs[Constants.PREF_COMPANY_ADDRESS_KEY, ""],
                CompanyAddressModel::class.java
            )
            var addressStringBuilder: StringBuilder = StringBuilder()
            addressStringBuilder
                .append(companyAddress.location.toString().trim()).append(", ")
                .append(companyAddress.area.toString().trim()).append(", ")
                .append(companyAddress.landmark.toString().trim()).append(", ")
                .append(companyAddress.country_name.toString().trim()).append(", ")
                .append(companyAddress.state_name.toString().trim()).append(", ")
                .append(companyAddress.city_name.toString().trim()).append(", ")
                .append(companyAddress.pincode.toString().trim()).append(", ")

            tv_company_address.text =
                CommonUtils.removeUnwantedComma(addressStringBuilder.toString())

            tvAddAddressNewOrg.visibility = View.GONE
            linear_company_address.visibility = View.VISIBLE
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
        if (txtFirstNameNewOrg.text.toString().isBlank()) {
            CommonUtils.showDialog(
                this,
                getString(R.string.enter_first_name_msg)/*"Please Enter First Name"*/
            )
            txtFirstNameNewOrg.requestFocus()
            return false
        } else if (txtLastNameNewOrg.text.toString().isBlank()) {
            //tvGenderEditInputLayout?.error = getString(R.string.gender_validation_msg)
            CommonUtils.showDialog(
                this,
                getString(R.string.enter_last_name_msg)/*"Please Enter Last Name"*/
            )
            txtLastNameNewOrg.requestFocus()
            return false
        } else if (txtOrgNameNewOrg.text.toString().isBlank()) {
            CommonUtils.showDialog(this, getString(R.string.enter_organization_name_msg))
            txtOrgNameNewOrg.requestFocus()
            return false
        } else if (txtMobileNewOrg.text.toString().isBlank()) {
            CommonUtils.showDialog(
                this,
                getString(R.string.enter_mobile_no_msg)/*"Please Enter Mobile Number"*/
            )
            txtMobileNewOrg.requestFocus()
            return false
        } else if (txtMobileNewOrg.text?.length!! < 10) {
            CommonUtils.showDialog(
                this,
                getString(R.string.enter_valid_mobileno_msg)/*"Please Enter Valid Mobile Number"*/
            )
            txtMobileNewOrg.requestFocus()
            return false
        }
        //alternate no.
        /*else if(txtAddNumberNewOrg.text.toString().isBlank()){
            CommonUtils.showDialog(this, "Please enter additional number")
            return false
        }*/
        else if (txtEmailNewOrg.text.toString().isBlank()) {
            CommonUtils.showDialog(this, getString(R.string.enter_email_msg))
            txtEmailNewOrg.requestFocus()
            return false
        } else if (txtEmailNewOrg.text.toString().isBlank() || !CommonUtils.isValidEmail(
                txtEmailNewOrg.text.toString()
            )
        ) {
            CommonUtils.showDialog(this, getString(R.string.email_validation_msg))
            txtEmailNewOrg.requestFocus()
            return false
        } else if (!prefs.contains(Constants.PREF_COMPANY_ADDRESS_KEY) || companyAddress.state_name.isNullOrBlank()) {
            CommonUtils.showDialog(this, getString(R.string.enter_address_details_msg))
            return false
        } else if (radiogrpNewOrg.getCheckedRadioButtonId() == -1) { // no radio buttons are checked
            Toast.makeText(
                this,
                getString(R.string.select_radio_gst_reg_company_yesno_msg),
                Toast.LENGTH_LONG
            ).show()
            return false
        } else if (radiobtnGSTYesNewOrg.isChecked && txtGSTINNewOrg.text.toString().isBlank()) {
            tvGSTINNewOrg.error = getString(R.string.enter_gstin_msg)/*"Please Enter GSTIN"*/
            CommonUtils.showDialog(this, getString(R.string.enter_gstin_msg))
            return false
        } else if (radiobtnGSTYesNewOrg.isChecked && !CommonUtils.isValidGSTNo(txtGSTINNewOrg.text.toString())/*txtGSTINNewOrg.text!!.length < 15*/) {
            //tvGSTINNewOrg.error = getString(R.string.enter_valid_gstin_msg)/*"Please Enter Valid GSTIN"*/
            CommonUtils.showDialog(this, getString(R.string.enter_valid_gstin_msg))
            return false
        }
        /*else if(txtFinYearNewOrg.text.toString().isBlank()){
            CommonUtils.showDialog(this, "Please enter Financial Year")
            return false
        }*/ else if (txtTermBalNewOrg.text.toString().isBlank()) {
            CommonUtils.showDialog(
                this,
                getString(R.string.enter_term_balance_msg)/*"Please Enter Term Balance"*/
            )
            txtTermBalNewOrg.requestFocus()
            return false
        } else if (radiobtnGSTYesNewOrg.isChecked && (txtPanNewOrg.text.toString()
                .isBlank() || !CommonUtils.isValidPANDetail(txtPanNewOrg.text.toString()))
        ) {
            CommonUtils.showDialog(
                this,
                getString(R.string.enter_correct_pandetails_msg)/*"Please Enter Correct PAN Details"*/
            )
            txtPanNewOrg.requestFocus()
            return false
        }
//         else if((isGSTRegistered == "1" && txtPanNewOrg.text.toString().isBlank() /*|| !CommonUtils.isValidPANDetail(txtPanNewOrg.text.toString())*/)){
//            CommonUtils.showDialog(this, "Please Enter Correct PAN Details")
//            txtPanNewOrg.requestFocus()
//            return false
//        }
//        else if((isGSTRegistered == "1" && txtPanNewOrg.text?.length!! < 10 || !CommonUtils.isValidPANDetail(txtPanNewOrg.text.toString()))){
//            CommonUtils.showDialog(this, "Please Enter Valid PAN Details")
//            txtPanNewOrg.requestFocus()
//            return false
//        }/*else if((txtCorpIdNoNewOrg.text.toString().isBlank() )){
//             CommonUtils.showDialog(this, "Please Enter Corporate Id No.")
//             return false
//         }*/

        companyAddress = Gson().fromJson(
            prefs[Constants.PREF_COMPANY_ADDRESS_KEY, ""],
            CompanyAddressModel::class.java
        )

        return true
    }


    fun openNameTitlePopup(view: View?) {
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

    fun addOrganizationAPI(
        token: String?,
        company_name: String?,
        reg_address: String?,
        area: String?,
        landmark: String?,
        country_id: String?,
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
        gst_tin_number: String?
    ) {

        viewModel.addUserCompany(
            token, company_name,
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
                        /* Log.v("..setupObservers..", "..Success...")*/
                        if (it.data?.status == true) {
                            Toast.makeText(
                                this,
                                it.data?.message,
                                Toast.LENGTH_LONG
                            )
                                .show()

                            onBackPressed()

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
                        /*Log.v("..setupObservers..", "..ERROR...")*/
                        CommonUtils.hideProgress()
                        Toast.makeText(this, it.data?.errormessage?.message, Toast.LENGTH_LONG)
                            .show()
                    }
                    Status.LOADING -> {
                        /* Log.v("..setupObservers..", "..LOADING...")*/
                        CommonUtils.showProgress(this)
                    }
                }
            }
        })
    }

}
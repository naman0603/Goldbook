package com.goldbookapp.ui.activity.supplier

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.blankj.utilcode.util.NetworkUtils
import com.goldbookapp.R
import com.goldbookapp.api.ApiHelper
import com.goldbookapp.api.RetrofitBuilder
import com.goldbookapp.databinding.SupplierDetailActivityBinding
import com.goldbookapp.model.ChangeStatusSupplierModel
import com.goldbookapp.model.GetListSupplierModel
import com.goldbookapp.model.LoginModel
import com.goldbookapp.model.UserWiseRestrictionModel
import com.goldbookapp.ui.activity.viewmodel.SupplierDetailsViewModal
import com.goldbookapp.ui.activity.viewmodel.ViewModelFactory
import com.goldbookapp.ui.adapter.SupplierDetailAdapter
import com.goldbookapp.ui.adapter.SupplierDetailModel
import com.goldbookapp.utils.CommonUtils
import com.goldbookapp.utils.CommonUtils.Companion.clickWithDebounce
import com.goldbookapp.utils.CommonUtils.Companion.isValidClickPressed
import com.goldbookapp.utils.Constants
import com.goldbookapp.utils.PreferenceHelper
import com.goldbookapp.utils.PreferenceHelper.get
import com.goldbookapp.utils.Status
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson
import com.hamzasharuf.networkmonitor.ConnectivityStateHolder
import com.hamzasharuf.networkmonitor.Event
import com.hamzasharuf.networkmonitor.NetworkEvents
import kotlinx.android.synthetic.main.supplier_detail_activity.*
import kotlinx.android.synthetic.main.toolbar_layout.*
import kotlinx.android.synthetic.main.toolbar_layout.view.*

class SupplierDetailsActivity : AppCompatActivity() {

    lateinit var binding: SupplierDetailActivityBinding

    private lateinit var viewModel: SupplierDetailsViewModal
    private lateinit var changeStatusSupplierModel: ChangeStatusSupplierModel
    lateinit var loginModel: LoginModel
    lateinit var popupMenu: PopupMenu
    lateinit var supplierDetailModel: SupplierDetailModel
    var tabposition: Int = 0

    var supplier_id: String? = ""
    private var status: String = "2"

    internal lateinit var adapter: SupplierDetailAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.supplier_detail_activity)

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
                    userWiseRestriction(loginModel.data?.bearer_access_token)
                }
                false->{
                    popupMenu.menu.getItem(1).setVisible(true)
                }

            }
            if (supplier_id != null && !supplier_id!!.isBlank()) {

                supplierDetailAPI(
                    loginModel?.data?.bearer_access_token,
                    loginModel?.data?.company_info?.id,
                    supplier_id
                )
                binding.root.imgRight.clickWithDebounce {

                    popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
                        if (this::supplierDetailModel.isInitialized) {
                            when (item.itemId) {

                                R.id.actionInactiveItemData ->
                                    when (status) {
                                        "1" -> { // active to inactive case
                                            // 2 inactive
                                            status = "2"
                                            popupMenu.menu.getItem(0).title =
                                                getString(R.string.mark_as_active)
                                            changeStatusSupplierAPI(
                                                loginModel?.data?.bearer_access_token,
                                                supplier_id,
                                                status
                                            )
                                        }
                                        "2" -> {//inactive to active case
                                            //1 active
                                            status = "1"
                                            popupMenu.menu.getItem(0).title =
                                                getString(R.string.mark_as_inactive)
                                            changeStatusSupplierAPI(
                                                loginModel?.data?.bearer_access_token,
                                                supplier_id,
                                                status
                                            )
                                        }
                                    }
                                R.id.actionDeleteItemData ->
                                    ensureDeleteDialog(supplierDetailModel.vendors?.display_name.toString())

                            }
                        }
                        true
                    })
                    popupMenu.show()
                }
            }
        }

        if (!ConnectivityStateHolder.isConnected) {
            // Network is not available
            CommonUtils.showDialog(this, getString(R.string.please_check_internet_msg))
        }
    }


    private fun setupViewModel() {
        viewModel =
            ViewModelProvider(this, ViewModelFactory(ApiHelper(RetrofitBuilder.apiService))).get(
                SupplierDetailsViewModal::class.java
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
        imgRight.setImageResource(R.drawable.ic_more)

        imgLeft?.clickWithDebounce{
            onBackPressed()
        }
        //setup tab layout
        tabLayoutSuppDeta.removeAllTabs()
        tabLayoutSuppDeta.addTab(
            tabLayoutSuppDeta.newTab().setText(getString(R.string.contact_info))
        )
        tabLayoutSuppDeta.addTab(
            tabLayoutSuppDeta.newTab().setText(getString(R.string.transactions))
        )
        tabLayoutSuppDeta.tabGravity = TabLayout.GRAVITY_FILL

        imgRight2?.clickWithDebounce {
            if (this::supplierDetailModel.isInitialized) {
                startActivity(
                    Intent(this, EditSupplierActivity::class.java)
                        .putExtra(Constants.SUPPLIER_DETAIL_KEY, Gson().toJson(supplierDetailModel))
                )
            }
        }

        if (intent.extras?.containsKey(Constants.SUPPLIER_DETAIL_KEY)!!) {
            var supplier_str: String? = intent.getStringExtra(Constants.SUPPLIER_DETAIL_KEY)
            var supplierDetailModel: GetListSupplierModel.Data344525142 = Gson().fromJson(
                supplier_str,
                GetListSupplierModel.Data344525142::class.java
            )
            supplier_id = supplierDetailModel.vendor_id
            popupMenu = PopupMenu(this, imgRight)
            popupMenu.menuInflater.inflate(R.menu.popup_menu_customer_detail, popupMenu.menu)
        }

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
            if (data.permission!!.get(i).startsWith(getString(R.string.supp))) {
                // Restriction check for Customer
                when (data.permission!!.get(i).equals(getString(R.string.supp_add_edit), true)) {
                    true -> {
                        binding.root.imgRight2.visibility = View.VISIBLE
                    }else->{

                }
                }
            }
            if (data.permission!!.get(i).startsWith(getString(R.string.supp))) {
                // Restriction check for Customer
                when (data.permission!!.get(i).equals(getString(R.string.supp_delete), true)) {
                    true -> {
                        popupMenu.menu.getItem(1).setVisible(true)
                    }else->{

                }
                }
            }

        }
    }





    // is editable 0 case (transaction are there for the supplier)
    private fun deleteSuppDialog(customer: String) {
        val builder = AlertDialog.Builder(this)
        val InActiveClick = { dialog: DialogInterface, which: Int ->
            changeStatusSupplierAPI(loginModel?.data?.bearer_access_token, supplier_id, "2")

        }
        val dialogdismiss = { dialog: DialogInterface, which: Int ->
            dialog.dismiss()
        }
        with(builder)
        {
            setTitle(context.getString(R.string.delSuppDialog1Title))
            setMessage(context.getString(R.string.suppDialog1Message))
            setPositiveButton(context.getString(R.string.Cancel), dialogdismiss)
            setNeutralButton(
                context.getString(R.string.mark_as_inactive),
                DialogInterface.OnClickListener(function = InActiveClick)
            )
            show()
        }
    }

    private fun ensureDeleteDialog(supplier: String) {
        val builder = AlertDialog.Builder(this)
        val DeleteClick = { dialog: DialogInterface, which: Int ->
            //supplier delete api call
            deleteContact(loginModel?.data?.bearer_access_token, supplier_id)

        }
        val dialogdismiss = { dialog: DialogInterface, which: Int ->
            dialog.dismiss()
        }
        with(builder)
        {
            setTitle(context.getString(R.string.delSuppDialog2Title))
            setMessage(context.getString(R.string.suppDialog2Message))
            setPositiveButton(context.getString(R.string.Cancel), dialogdismiss)
            setNeutralButton(
                context.getString(R.string.Delete),
                DialogInterface.OnClickListener(function = DeleteClick)
            )
            show()
        }
    }

    private fun deleteContact(token: String?, contact_id: String?) {
        if (NetworkUtils.isConnected()) {
            if (isValidClickPressed()) {
                viewModel.deleteContact(token, contact_id).observe(this, Observer {
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
                                        deleteSuppDialog(supplierDetailModel.vendors?.display_name.toString())
                                    }
                                }

                                CommonUtils.hideProgress()

                            }
                            Status.ERROR -> {
                                CommonUtils.hideProgress()

                            }
                            Status.LOADING -> {
                                CommonUtils.showProgress(this@SupplierDetailsActivity)
                            }
                        }
                    }
                })
            }
        }
    }

    fun supplierDetailAPI(
        token: String?,
        company_id: String?,
        customer_id: String?
    ) {

        if (NetworkUtils.isConnected()) {
            viewModel.supplierDetail(token, company_id, customer_id).observe(this, Observer {
                it?.let { resource ->
                    when (resource.status) {
                        Status.SUCCESS -> {
                            if (it.data?.status == true) {
                                binding.llSuppDetailRoot.visibility = View.VISIBLE
                                supplierDetailModel = it.data
                                binding.root.tvTitle.text = supplierDetailModel.vendors?.display_name
                                fill_fine_cash_card_details(supplierDetailModel)

                                when (supplierDetailModel.vendors?.status?.equals("1", true)) {
                                    true -> {
                                        status = "1"
                                        popupMenu.menu.getItem(0).title =
                                            getString(R.string.mark_as_inactive)
                                    }
                                    false -> {
                                        status = "2"
                                        popupMenu.menu.getItem(0).title =
                                            getString(R.string.mark_as_active)
                                    }else->{

                                }
                                }
                                setupTabViewPager(supplierDetailModel)

                                binding.tvDebitSuppDeta.isSelected = true
                                binding.tvCreditSuppDeta.isSelected = true
                                binding.tvSilverSuppDeta.isSelected = true


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

                        }
                        Status.ERROR -> {
                            CommonUtils.hideProgress()

                        }
                        Status.LOADING -> {
                            if (!this::adapter.isInitialized)
                                CommonUtils.showProgress(this@SupplierDetailsActivity)
                        }
                    }
                }
            })
        }
    }

    private fun fill_fine_cash_card_details(supplierDetailModel: SupplierDetailModel) {
        when (supplierDetailModel.vendors!!.display_fine_balance) {
            "0.000" -> {
                binding.tvDebitSuppDeta.text = supplierDetailModel.vendors!!.display_fine_balance
                binding.tvDebitSuppDeta.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.header_black_text
                    )
                )
            }

            else -> {
                binding.tvDebitSuppDeta.text =
                    supplierDetailModel.vendors!!.display_fine_balance + " " + supplierDetailModel.vendors!!.display_fine_default_term

                if (supplierDetailModel.vendors!!.display_fine_default_term.equals("Dr", ignoreCase = true)) {
                    binding.tvDebitSuppDeta.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.debit_color
                        )
                    )
                } else
                    binding.tvDebitSuppDeta.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.credit_color
                        )
                    )
            }
        }
        when (supplierDetailModel.vendors!!.cash_balance) {
            "0.00" -> {
                binding.tvCreditSuppDeta.text = supplierDetailModel.vendors!!.cash_balance
                binding.tvCreditSuppDeta.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.header_black_text
                    )
                )
            }
            else -> {
                binding.tvCreditSuppDeta.text =
                    supplierDetailModel.vendors!!.cash_balance + " " + supplierDetailModel.vendors!!.cash_balance_type

                if (supplierDetailModel.vendors!!.cash_balance_type.equals("Dr", ignoreCase = true))
                    binding.tvCreditSuppDeta.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.debit_color
                        )
                    )
                else
                    binding.tvCreditSuppDeta.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.credit_color
                        )
                    )

            }
        }

        when (supplierDetailModel.vendors!!.display_silver_fine_balance) {
            "0.00" -> {
                binding.tvSilverSuppDeta.text = supplierDetailModel.vendors!!.display_silver_fine_balance
                binding.tvSilverSuppDeta.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.header_black_text
                    )
                )
            }
            else -> {
                binding.tvSilverSuppDeta.text =
                    supplierDetailModel.vendors!!.display_silver_fine_balance + " " + supplierDetailModel.vendors!!.display_silver_fine_default_term

                if (supplierDetailModel.vendors!!.display_silver_fine_default_term.equals("Dr", ignoreCase = true))
                    binding.tvSilverSuppDeta.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.debit_color
                        )
                    )
                else
                    binding.tvSilverSuppDeta.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.credit_color
                        )
                    )

            }
        }

    }

    fun changeStatusSupplierAPI(
        token: String?,
        vendor_id: String?,
        status: String?
    ) {

        if (NetworkUtils.isConnected()) {
            if (isValidClickPressed()) {
                viewModel.changeStatusSupplier(token, vendor_id, status).observe(this, Observer {
                    it?.let { resource ->
                        when (resource.status) {
                            Status.SUCCESS -> {
                                if (it.data?.status == true) {

                                    changeStatusSupplierModel = it.data
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
                                CommonUtils.showProgress(this@SupplierDetailsActivity)
                            }
                        }
                    }
                })
            }
        }
    }

    private fun setupTabViewPager(supplierDetailModel: SupplierDetailModel) {

        if (!this::adapter.isInitialized) {
            adapter = SupplierDetailAdapter(
                this, supportFragmentManager,
                binding.tabLayoutSuppDeta.tabCount,
                supplierDetailModel
            )
            binding.viewPagerSuppDeta.adapter = adapter
            binding.viewPagerSuppDeta.addOnPageChangeListener(
                TabLayout.TabLayoutOnPageChangeListener(
                    tabLayoutSuppDeta
                )
            )
            binding.tabLayoutSuppDeta.addOnTabSelectedListener(object :
                TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    binding.viewPagerSuppDeta.currentItem = tab.position
                    tabposition = tab.position
                }

                override fun onTabUnselected(tab: TabLayout.Tab) {}
                override fun onTabReselected(tab: TabLayout.Tab) {}
            })

            binding.viewPagerSuppDeta.currentItem = tabposition
        }

    }

}
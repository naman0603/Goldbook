package com.goldbookapp.ui.activity.item

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import com.blankj.utilcode.util.NetworkUtils
import com.bumptech.glide.Glide
import com.goldbookapp.R
import com.goldbookapp.api.ApiHelper
import com.goldbookapp.api.RetrofitBuilder
import com.goldbookapp.databinding.ItemDetailActivityBinding
import com.goldbookapp.model.*
import com.goldbookapp.ui.activity.viewmodel.ItemDetailViewModel
import com.goldbookapp.ui.activity.viewmodel.ViewModelFactory
import com.goldbookapp.ui.adapter.ViewPagerAdapter
import com.goldbookapp.utils.CommonUtils
import com.goldbookapp.utils.CommonUtils.Companion.clickWithDebounce
import com.goldbookapp.utils.CommonUtils.Companion.isValidClickPressed
import com.goldbookapp.utils.Constants
import com.goldbookapp.utils.PreferenceHelper
import com.goldbookapp.utils.PreferenceHelper.get
import com.goldbookapp.utils.Status
import com.google.gson.Gson
import com.hamzasharuf.networkmonitor.ConnectivityStateHolder
import com.hamzasharuf.networkmonitor.Event
import com.hamzasharuf.networkmonitor.NetworkEvents
import kotlinx.android.synthetic.main.item_detail_activity.*
import kotlinx.android.synthetic.main.item_image_dialog.*
import kotlinx.android.synthetic.main.toolbar_layout.*

class ItemDetailActivity : AppCompatActivity() {

    private lateinit var viewModel: ItemDetailViewModel
    lateinit var popupMenu: PopupMenu
    lateinit var binding: ItemDetailActivityBinding
    private var status: String = "2"
    lateinit var loginModel: LoginModel
    private lateinit var changeStatusItemModel: ChangeStatusItemModel
    lateinit var itemDetailModel: ItemDetailModel.Data.Item
    lateinit var itemDetailImagesUrls: ArrayList<String>

    var item_id: String? = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.item_detail_activity)

        setupViewModel()
        setupUIandListner()

    }

    private fun setupViewModel() {
        viewModel =
            ViewModelProvider(this, ViewModelFactory(ApiHelper(RetrofitBuilder.apiService))).get(
                ItemDetailViewModel::class.java
            )
        binding.setLifecycleOwner(this)
        binding.itemDetailViewModel = viewModel
    }

    private fun setupUIandListner() {

        val prefs = PreferenceHelper.defaultPrefs(this)
        loginModel = Gson().fromJson(
            prefs[Constants.PREF_LOGIN_DETAIL_KEY, ""],
            LoginModel::class.java
        ) //getter

        imgLeft.setImageResource(R.drawable.ic_back)
        imgRight2.setImageResource(R.drawable.ic_edit)
        tvTitle.setText("Item Name")
        imgRight.setImageResource(R.drawable.ic_more)


        imgLeft?.clickWithDebounce {
            onBackPressed()
        }

        imgRight2?.clickWithDebounce {
            if (this::itemDetailModel.isInitialized) {
                startActivity(
                    Intent(this, NewItemActivity::class.java)
                        .putExtra(Constants.ITEM_DETAIL_KEY, Gson().toJson(itemDetailModel))
                        .putExtra(Constants.IS_FOR_EDIT,"true")
                )
                finish()
            }
        }

        binding.imgItemData.clickWithDebounce {
            showDialog();
        }

        imgRight.clickWithDebounce {

            popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
                if (this::itemDetailModel.isInitialized) {
                    when (item.itemId) {

                        R.id.actionInactiveItemDeta ->
                            when (status) {
                                "1" -> { // active to inactive case
                                    // 2 inactive
                                    status = "2"
                                    popupMenu.menu.getItem(0).title =
                                        getString(R.string.mark_as_active)
                                    changeStatusItemAPI(
                                        loginModel?.data?.bearer_access_token,
                                        item_id,
                                        status
                                    )
                                }
                                "2" -> {//inactive to active case
                                    //1 active
                                    status = "1"
                                    popupMenu.menu.getItem(0).title =
                                        getString(R.string.mark_as_inactive)
                                    changeStatusItemAPI(
                                        loginModel?.data?.bearer_access_token,
                                        item_id,
                                        status
                                    )
                                }
                            }
                        R.id.actiondeleteItemDeta ->
                            ensureDeleteDialog(itemDetailModel.item_name.toString())

                    }
                }
                true
            })
            popupMenu.show()
        }
        if (intent.extras?.containsKey(Constants.ITEM_DETAIL_KEY)!!) {
            var item_str: String? = intent.getStringExtra(Constants.ITEM_DETAIL_KEY)
            var itemDetailModel: GetItemListModel.Data1077697879 = Gson().fromJson(
                item_str,
                GetItemListModel.Data1077697879::class.java
            )
            item_id = itemDetailModel.item_id
            //item_quantity = itemDetailModel.quantity
            popupMenu = PopupMenu(this, imgRight)
            popupMenu.menuInflater.inflate(R.menu.popup_menu_item_detail, popupMenu.menu)

        }


        viewModel.itemDetails.observe(this) {
            /*Log.v("..Item..","..observe..")*/
            if (it.data?.item?.image?.size!! > 0) {
                binding.imgItemData.visibility = View.VISIBLE
                Glide.with(this).load(it.data?.item?.image?.get(0)?.item_image).circleCrop()
                    .into(imgItemData)
            } else {
                binding.imgItemData.visibility = View.GONE
            }
            // pending
            tvStockHandItemDeta.text =
                it.data?.item?.stock_in_hand + " " + it.data?.item?.maintain_stock_in
            /*Constants.WEIGHT_GM_APPEND*/

            tvSalesMcItemDeta.text =
                it.data?.item?.sales_making_charges + " / " + it.data?.item?.unit_name
            tvPurchaseMcItemDeta.text =
                it.data?.item?.purchase_making_charges + " / " + it.data?.item?.unit_name
        }

    }

    private fun deleteItem(token: String?, item_id: String?) {
        if (NetworkUtils.isConnected()) {
            if (isValidClickPressed()) {
                viewModel.deleteItem(token, item_id).observe(this, Observer {
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
                                        deleteItemDialog(itemDetailModel.item_name.toString())
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
            if (data.permission!!.get(i).startsWith(getString(R.string.items))) {
                // Restriction check for Customer
                when (data.permission!!.get(i).endsWith(getString(R.string.add_edit), true)) {
                    true -> {
                        imgRight2.visibility = View.VISIBLE
                    }
                    else->{

                    }
                }
            }
            if (data.permission!!.get(i).startsWith(getString(R.string.items))) {
                // Restriction check for Customer
                when (data.permission!!.get(i).endsWith(getString(R.string.del), true)) {
                    true -> {
                        popupMenu.menu.getItem(1).setVisible(true)
                    }
                    else->{

                    }
                }
            }

        }
    }





    fun changeStatusItemAPI(
        token: String?,
        item_id: String?,
        status: String?
    ) {

        if (NetworkUtils.isConnected()) {
            if (isValidClickPressed()) {
                viewModel.changeStatusItem(token, item_id, status).observe(this, Observer {
                    it?.let { resource ->
                        when (resource.status) {
                            Status.SUCCESS -> {
                                if (it.data?.status == true) {

                                    changeStatusItemModel = it.data
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
                false->{
                    defaultEnableAllButtonnUI()

                }

            }
            if (!item_id?.isBlank()!!) {

                itemDetailAPI(loginModel?.data?.bearer_access_token, item_id)
            }
        }

        if (!ConnectivityStateHolder.isConnected) {
            // Network is not available
            CommonUtils.showDialog(this, getString(R.string.please_check_internet_msg))

        }
    }

    private fun defaultDisableAllButtonnUI() {
        popupMenu.menu.getItem(1).setVisible(false)
        imgRight2.visibility = View.GONE
    }

    private fun defaultEnableAllButtonnUI() {
        popupMenu.menu.getItem(1).setVisible(true)
        imgRight2.visibility = View.VISIBLE
    }

    private fun showDialog() {
        val dialog = Dialog(this, R.style.FullWidth_Dialog)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        );

        dialog.setCancelable(false)
        dialog.setContentView(R.layout.item_image_dialog)
        dialog.imgCloseItemDeta.clickWithDebounce {
            dialog.dismiss();
        }
        if (this::itemDetailModel.isInitialized) {
            itemDetailImagesUrls = arrayListOf()
            if (itemDetailModel.image?.size!! > 0) {
                for (i in 0 until itemDetailModel.image?.size!!)
//                Glide.with(this).load(itemDetailModel.image?.get(i)?.image_url).circleCrop()
//                    .into(dialog.item_image)
                    itemDetailImagesUrls.add(itemDetailModel.image?.get(i)?.item_image!!)
            }

        }

        val dotsIndicator = dialog.dotsindicatorItemDeta
        val viewPager = dialog.viewpagerItemDeta
        val adapter = ViewPagerAdapter(itemDetailImagesUrls)
        viewPager.adapter = adapter
        dotsIndicator.setViewPager(viewPager)

        dialog.show()

    }

    private fun deleteItemDialog(customer: String) {
        val builder = AlertDialog.Builder(this)
        val InActiveClick = { dialog: DialogInterface, which: Int ->
            changeStatusItemAPI(loginModel?.data?.bearer_access_token, item_id, "2")

        }
        val dialogdismiss = { dialog: DialogInterface, which: Int ->
            dialog.dismiss()
        }
        with(builder)
        {
            setTitle(context.getString(R.string.delItemDialog1Title))
            setMessage(context.getString(R.string.itemDialog1Message))
            setPositiveButton(context.getString(R.string.Cancel), dialogdismiss)
            setNeutralButton(
                context.getString(R.string.mark_as_inactive),
                DialogInterface.OnClickListener(function = InActiveClick)
            )
            show()
        }
    }

    private fun ensureDeleteDialog(item_name: String) {
        val builder = AlertDialog.Builder(this)
        val DeleteClick = { dialog: DialogInterface, which: Int ->
            //item delete api call
            deleteItem(loginModel?.data?.bearer_access_token, item_id)
        }
        val dialogdismiss = { dialog: DialogInterface, which: Int ->
            dialog.dismiss()
        }
        with(builder)
        {
            setTitle(context.getString(R.string.delItemDialog2Title))
            setMessage(context.getString(R.string.itemDialog2Message))
            setPositiveButton(context.getString(R.string.Cancel), dialogdismiss)
            setNeutralButton(
                context.getString(R.string.Delete),
                DialogInterface.OnClickListener(function = DeleteClick)
            )
            show()
        }
    }

    fun itemDetailAPI(
        token: String?,
        item_id: String?
    ) {

        if (NetworkUtils.isConnected()) {

            viewModel.getItemDetails(token, item_id).observe(this, Observer {
                it?.let { resource ->
                    when (resource.status) {
                        Status.SUCCESS -> {
                            if (it.data?.status == true) {
                                llitemDetail.visibility = View.VISIBLE
                                itemDetailModel = it.data.data?.item!!
                                tvTitle.setText(it.data.data?.item?.item_name)
                                when (itemDetailModel.status?.equals("1", true)) {
                                    true -> {
                                        status = "1"
                                        popupMenu.menu.getItem(0).title =
                                            getString(R.string.mark_as_inactive)
                                    }
                                    false -> {
                                        status = "2"
                                        popupMenu.menu.getItem(0).title =
                                            getString(R.string.mark_as_active)
                                    }
                                    else->{

                                    }
                                }
                                tvMaintainStockInValue.setText(itemDetailModel.maintain_stock_in)
                                tvUnitValue.setText(itemDetailModel.unit_name)
                               /* when (itemDetailModel.item_stock_type.equals("entry", true)) {
                                    true -> {
                                        tvMaintainStockInValue.setText(itemDetailModel.maintain_stock_in)
                                    }
                                    else -> {
                                        tvItemStockType.setText(resources.getString(R.string.touchwise))
                                    }
                                }*/

                                if (itemDetailModel.item_preferred_vendor?.size!!.equals(0)) {
                                    tvPreferredVendorItemDeta.visibility = View.GONE
                                    itemdetail_PrefVendorLabel.visibility = View.GONE

                                } else {
                                    itemdetail_PrefVendorLabel.visibility = View.VISIBLE
                                    tvPreferredVendorItemDeta.visibility = View.VISIBLE
                                    var sb = StringBuilder()
                                    for (i in 0 until itemDetailModel?.item_preferred_vendor?.size!!) {
                                        if ((i + 1).equals(itemDetailModel?.item_preferred_vendor?.size!!)) {
                                            sb = sb.append(
                                                itemDetailModel?.item_preferred_vendor?.get(i)?.vendor
                                            )
                                        } else {
                                            sb = sb.append(
                                                itemDetailModel?.item_preferred_vendor?.get(i)?.vendor
                                            ).append("\n")
                                        }

                                        tvPreferredVendorItemDeta.text = sb
                                    }
                                }

                                viewModel.itemDetails.postValue(it.data)

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
                            CommonUtils.showProgress(this)
                        }
                    }
                }
            })
        }
    }
}
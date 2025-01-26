package com.goldbookapp.ui.activity.purchase

import android.Manifest
import android.app.*
import android.content.*
import android.database.Cursor
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.*
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintJob
import android.print.PrintManager
import android.provider.MediaStore
import android.provider.Telephony
import android.text.Html
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.NetworkUtils
import com.bumptech.glide.Glide
import com.goldbookapp.BuildConfig
import com.goldbookapp.R
import com.goldbookapp.api.ApiHelper
import com.goldbookapp.api.ApiService
import com.goldbookapp.api.RetrofitBuilder
import com.goldbookapp.databinding.PurchaseBillDetailActivityBinding
import com.goldbookapp.model.*
import com.goldbookapp.permissions.PermissionHandler
import com.goldbookapp.permissions.Permissions
import com.goldbookapp.ui.activity.PdfDocumentAdapter
import com.goldbookapp.ui.activity.PrintJobMonitorService
import com.goldbookapp.ui.activity.viewmodel.PurchaseDetailsViewModel
import com.goldbookapp.ui.activity.viewmodel.ViewModelFactory
import com.goldbookapp.ui.adapter.IssueReceiveAdapter
import com.goldbookapp.ui.adapter.SaleDetailBill_ItemAdapter
import com.goldbookapp.utils.CommonUtils
import com.goldbookapp.utils.CommonUtils.Companion.clickWithDebounce
import com.goldbookapp.utils.CommonUtils.Companion.isValidClickPressed
import com.goldbookapp.utils.Constants
import com.goldbookapp.utils.PreferenceHelper
import com.goldbookapp.utils.PreferenceHelper.get
import com.goldbookapp.utils.PreferenceHelper.set
import com.goldbookapp.utils.Status
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hamzasharuf.networkmonitor.ConnectivityStateHolder
import com.hamzasharuf.networkmonitor.Event
import com.hamzasharuf.networkmonitor.NetworkEvents
import kotlinx.android.synthetic.main.custom_image_dialog.*
import kotlinx.android.synthetic.main.new_purchase_activity.*
import kotlinx.android.synthetic.main.purchase_bill_detail_activity.*
import kotlinx.android.synthetic.main.purchase_bill_detail_activity.ll_closingbal_Newpurchase
import kotlinx.android.synthetic.main.purchase_bill_detail_activity.view.*
import kotlinx.android.synthetic.main.sales_bill_detail_activity.*
import kotlinx.android.synthetic.main.toolbar_layout.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.*
import java.math.BigDecimal
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

class PurchaseBillDetailActivity : AppCompatActivity() {
    var isFromWhatsapp: Boolean = false;

    var fileSavePath = ""
    private val PERMISSION_REQUEST_CODE = 101
    private lateinit var notificationBuilder: NotificationCompat.Builder

    private lateinit var notificationManager: NotificationManager

    private var mgr: PrintManager? = null

    lateinit var binding: PurchaseBillDetailActivityBinding

    private lateinit var viewModel: PurchaseDetailsViewModel

    lateinit var loginModel: LoginModel
    lateinit var prefs: SharedPreferences
    lateinit var purchaseDetailModel: SaleDetailModel.Data
    lateinit var popupMenu: PopupMenu
    private lateinit var adapter: SaleDetailBill_ItemAdapter
    var report_type: String = ""
    var isFromDownload: Boolean = false
    var filename: String = ""

    var isFromThread: Boolean = true

    var purchaseID: String? = ""

    var imageURL: String? = ""
    private lateinit var issueReceiveadapter: IssueReceiveAdapter
    var purchaseLineList = ArrayList<SalesLineModel.SaleLineModelDetails>()
    var debit_short_term: String = ""
    var credit_short_term: String = ""

    var fineDefaultTermList: List<ItemDefaultTermModel.Data.Default_term>? = null
    var subTotalTerm: String = ""
    var subTotalTermValue: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.purchase_bill_detail_activity)
        val view = binding.root
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
            getDefaultTerm()

        }

        if (!ConnectivityStateHolder.isConnected) {
            // Network is not available
            CommonUtils.showDialog(this, getString(R.string.please_check_internet_msg))

        }
    }

    private fun defaultDisableAllButtonUI() {
        imgRight2.visibility = View.GONE
        popupMenu.menu.getItem(3).setVisible(false)
        popupMenu.menu.getItem(2).setVisible(false)
        popupMenu.menu.getItem(1).setVisible(false)
    }


    private fun defaultEnableAllButtonnUI() {
        imgRight.visibility = View.VISIBLE
        imgRight2.visibility = View.VISIBLE
        popupMenu.menu.getItem(3).setVisible(true)
        popupMenu.menu.getItem(2).setVisible(true)
        popupMenu.menu.getItem(1).setVisible(true)
    }

    private fun setupViewModel() {
        viewModel =
            ViewModelProvider(this, ViewModelFactory(ApiHelper(RetrofitBuilder.apiService))).get(
                PurchaseDetailsViewModel::class.java
            )
        binding.setLifecycleOwner(this)

    }

    private fun setupUIandListner() {

         prefs = PreferenceHelper.defaultPrefs(this)
        loginModel = Gson().fromJson(
            prefs[Constants.PREF_LOGIN_DETAIL_KEY, ""],
            LoginModel::class.java
        ) //getter

        imgLeft.setImageResource(R.drawable.ic_back)
        imgRight2.setImageResource(R.drawable.ic_edit)
        imgRight.setImageResource(R.drawable.ic_more)


        imgLeft?.clickWithDebounce {
            onBackPressed()
        }
        mgr = getSystemService(Context.PRINT_SERVICE) as PrintManager

        imgRight2?.clickWithDebounce {
            if (this::purchaseDetailModel.isInitialized) {
                startActivity(
                    Intent(this, NewPurchaseActivity::class.java)
                        .putExtra(Constants.PURCHASE_DETAIL_KEY, Gson().toJson(purchaseDetailModel))
                )
                finish()
            }
        }

        clearPref()
        //recyclerviewsetup for added item details
        binding.root.rv_purchasebilldetail_item.layoutManager = LinearLayoutManager(this)
        adapter = SaleDetailBill_ItemAdapter(arrayListOf())
        binding.root.rv_purchasebilldetail_item.adapter = adapter

        // issue receive adapter
        debit_short_term = loginModel?.data!!.company_info!!.general_settings!!.debit_short_term!!
        credit_short_term = loginModel?.data!!.company_info!!.general_settings!!.credit_short_term!!
        binding.rvIssueReceiveList.layoutManager = LinearLayoutManager(this)
        issueReceiveadapter = IssueReceiveAdapter(arrayListOf(), "purchase", true,debit_short_term,credit_short_term)
        binding.rvIssueReceiveList.adapter = issueReceiveadapter

        if(intent.extras?.containsKey(Constants.ModuleID)!!){
            purchaseID = intent.getStringExtra(Constants.ModuleID)
        }

        if (intent.extras?.containsKey(Constants.PURCHASE_RECENT_TRANS_DETAIL_KEY)!!) {
           // purchaseID = intent.getStringExtra(Constants.ModuleID)
            var purchase_str: String? = intent.getStringExtra(Constants.PURCHASE_RECENT_TRANS_DETAIL_KEY)
            var purchaseDetailModel: DashboardDetailsModel.Data.Recent_transactions = Gson().fromJson(
                purchase_str,
                DashboardDetailsModel.Data.Recent_transactions::class.java
            )
            purchaseID = purchaseDetailModel.module_id
            tvTitle.text = purchaseDetailModel.transaction_number
            tv_purchasebilldetail_custname.text = purchaseDetailModel.display_name
            tv_purchasebilldetail_noofitems.text =
                purchaseDetailModel.no_of_items.toString() + " item"
            tv_purchasebilldetail_transactiondate.text = purchaseDetailModel.transaction_date

        }

        if (intent.extras?.containsKey(Constants.PURCHASE_DETAIL_KEY)!!) {
            var purchase_str: String? = intent.getStringExtra(Constants.PURCHASE_DETAIL_KEY)
            var purchaseDetailModel: SearchListPurchaseModel.DataPurchase = Gson().fromJson(
                purchase_str,
                SearchListPurchaseModel.DataPurchase::class.java
            )
            purchaseID = purchaseDetailModel.transaction_id
            tvTitle.text = purchaseDetailModel.invoice_number
            tv_purchasebilldetail_custname.text = purchaseDetailModel.contact_name
            tv_purchasebilldetail_noofitems.text =
                purchaseDetailModel.total_items.toString() + " item"
            tv_purchasebilldetail_transactiondate.text = purchaseDetailModel.transaction_date

        }

        imgLeft?.clickWithDebounce {
            onBackPressed()
        }


        imgRight.clickWithDebounce {

            if (purchaseDetailModel.transactionData?.is_gst_applicable.toString().contains("1")) {
                if (this::popupMenu.isInitialized) {
                    popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
                        if (this::purchaseDetailModel.isInitialized) {
                            when (item.itemId) {
                                R.id.sharesubmenuitem1 -> {
                                    isFromWhatsapp = false
                                    voucherTextAPI(
                                        loginModel?.data?.bearer_access_token,
                                        purchaseID
                                    )
                                }
                                R.id.sharesubmenuitem2 -> {
                                    isFromWhatsapp = true
                                    voucherTextAPI(
                                        loginModel?.data?.bearer_access_token,
                                        purchaseID
                                    )
                                }
                                R.id.action_download -> {

                                    checkandRequestPermission()
                                    isFromDownload = true
                                }
                                R.id.action_print -> {

                                    checkandRequestPermission()
                                    isFromDownload = false
                                }
                                R.id.action_delete ->
                                    ensureDeleteDialog(purchaseDetailModel.transactionData?.invoice_number!!)
                            }
                        }
                        true
                    })
                }

            } else {
                if (this::popupMenu.isInitialized) {
                    popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
                        if (this::purchaseDetailModel.isInitialized) {
                            when (item.itemId) {
                                R.id.sharesubmenuitem1 -> {
                                    isFromWhatsapp = false
                                    voucherTextAPI(
                                        loginModel?.data?.bearer_access_token,
                                        purchaseID
                                    )
                                }
                                R.id.sharesubmenuitem2 -> {
                                    isFromWhatsapp = true
                                    voucherTextAPI(
                                        loginModel?.data?.bearer_access_token,
                                        purchaseID
                                    )
                                }
                                R.id.downloadsubmenuitem1 -> {
                                    if (purchaseDetailModel.transactionData?.is_gst_applicable.toString()
                                            .contains("1")
                                    ) {
                                        report_type = ""
                                    } else {
                                        report_type = ""
                                    }

                                    checkandRequestPermission()
                                    isFromDownload = true
                                }
                                R.id.downloadsubmenuitem2 -> {
                                    if (purchaseDetailModel.transactionData?.is_gst_applicable.toString()
                                            .contains("1")
                                    ) {
                                        report_type = ""
                                    } else {
                                        report_type = "Purchase Non Gst 3"
                                    }

                                    checkandRequestPermission()
                                    isFromDownload = true
                                }

                                /*R.id.action_download -> {
                                    report_type = ""
                                    checkandRequestPermission()
                                    isFromDownload = true
                                }*/

                                R.id.printsubmenuitem1 -> {
                                    if (purchaseDetailModel.transactionData?.is_gst_applicable.toString()
                                            .contains("1")
                                    ) {
                                        report_type = ""

                                    } else {
                                       // report_type = "estimate"
                                        report_type = ""
                                    }

                                    checkandRequestPermission()
                                    isFromDownload = false
                                }
                                R.id.printsubmenuitem2 -> {
                                    if (purchaseDetailModel.transactionData?.is_gst_applicable.toString()
                                            .contains("1")
                                    ) {
                                        report_type = ""
                                    } else {
                                        //report_type = "invoice"
                                        report_type = "Purchase Non Gst 3"
                                    }

                                    checkandRequestPermission()
                                    isFromDownload = false
                                }

                               /* R.id.action_print ->{
                                    report_type = ""
                                    checkandRequestPermission()
                                    isFromDownload = false
                                }*/

                                R.id.action_delete ->

                                    ensureDeleteDialog(purchaseDetailModel.transactionData?.invoice_number!!)
                            }
                        }
                        true
                    })
                }

            }
            popupMenu.show()
        }

        iv_purchasebilldetail_attachmentone.clickWithDebounce {

            if (!imageURL?.isBlank()!!) {
                showFullImage(imageURL)
            }
        }


    }

    private fun clearPref() {
        if (prefs.contains(Constants.PREF_SALES_LINE_INFO_KEY)) {
            prefs.edit().remove(Constants.PREF_SALES_LINE_INFO_KEY).apply()
        }
        if (prefs.contains(Constants.PREF_OPENINGSTOCK_CALC_INFO_KEY)) {
            prefs.edit().remove(Constants.PREF_OPENINGSTOCK_CALC_INFO_KEY).apply()
        }
        if (prefs.contains(Constants.PREF_SALES_TAX_ANALYSIS_LIST_KEY)) {
            prefs.edit().remove(Constants.PREF_SALES_TAX_ANALYSIS_LIST_KEY).apply()
        }
    }

    fun getDefaultTerm() {
        if (NetworkUtils.isConnected()) {
            viewModel.getDefaultTerm(
                loginModel?.data?.bearer_access_token
            ).observe(this, Observer {
                it?.let { resource ->
                    when (resource.status) {
                        Status.SUCCESS -> {

                            if (it.data?.status == true) {
                                fineDefaultTermList = it.data.data!!.default_term
                                subTotalTerm = fineDefaultTermList!!.get(1).default_short_term!!
                                subTotalTermValue = fineDefaultTermList!!.get(1).default_term_value!!

                                if (purchaseID?.isNotEmpty()!!) {

                                    purchaseDetailAPI(
                                        loginModel?.data?.bearer_access_token,
                                        purchaseID
                                    )
                                }
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


                        }
                        Status.ERROR -> {

                        }
                        Status.LOADING -> {

                        }
                    }
                }
            })
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
            if (data.permission!!.get(i).startsWith(getString(R.string.pur))) {
                // Restriction check for  Purchase
                when (data.permission!!.get(i).endsWith(getString(R.string.add_edit), true)) {
                    true -> {
                        imgRight2.visibility = View.VISIBLE
                    }else->{

                }
                }
            }
            if (data.permission!!.get(i).startsWith(getString(R.string.pur))) {
                // Restriction check for Purchase
                when (data.permission!!.get(i).endsWith(getString(R.string.print), true)) {
                    true -> {
                        popupMenu.menu.getItem(1).setVisible(true)

                    }else->{

                }
                }
            }
            if (data.permission!!.get(i).startsWith(getString(R.string.pur))) {
                // Restriction check for Purchase
                when (data.permission!!.get(i).endsWith(getString(R.string.download), true)) {
                    true -> {
                        popupMenu.menu.getItem(2).setVisible(true)

                    }else->{

                }
                }
            }
            if (data.permission!!.get(i).startsWith(getString(R.string.pur))) {
                // Restriction check for Purchase
                when (data.permission!!.get(i).endsWith(getString(R.string.del), true)) {
                    true -> {
                        popupMenu.menu.getItem(3).setVisible(true)

                    }else->{

                }
                }
            }

        }
    }


    private fun calltovoucherPrint() {
        progressBar_Purchasebilldetail.visibility = View.VISIBLE
        val thread = Thread(Runnable {
            try {
                //Your code goes here
                voucherPrint()
                isFromThread = true
            } catch (e: java.lang.Exception) {
                isFromThread = false
            }
        })
        thread.start()
        val SDK_INT = Build.VERSION.SDK_INT
        if (SDK_INT > 21) {
            val policy: StrictMode.ThreadPolicy = StrictMode.ThreadPolicy.Builder()
                .permitAll().build()
            StrictMode.setThreadPolicy(policy)
            //your codes here
            if (!isFromThread)
                voucherPrint()
        }
    }

    private fun voucherPrint() {
        if (NetworkUtils.isConnected()) {
            val apiInterface: ApiService =
                RetrofitBuilder.createService(ApiService::class.java)
            val call: Call<ResponseBody> = apiInterface.voucherPrint(
                loginModel?.data?.bearer_access_token,
                "purchase",
                purchaseID,
                report_type
            )

            call.enqueue(object : Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    //not required
                    CommonUtils.somethingWentWrong(this@PurchaseBillDetailActivity)
                    progressBar_Purchasebilldetail.visibility = View.GONE
                }

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {


                    getFileNamefromHeader(response.headers().toString())
                    val writtenToDisk: Boolean = writeResponseBodyToDisk(response.body()!!)
                    if (writtenToDisk) {
                        when (isFromDownload) {
                            true -> {
                                val toast: Toast = Toast.makeText(
                                    this@PurchaseBillDetailActivity,
                                    "PDF saved at " + fileSavePath.drop(20)/*getString(R.string.voucher_downloaded_successfully)*/,
                                    Toast.LENGTH_LONG
                                )
                                toast.setGravity(Gravity.CENTER, 0, 0)
                                toast.show()
                                // showDownloadNotification()
                                val rootDirtory = File(
                                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                                    "GoldBook"
                                )
                                val myDirectory = File(rootDirtory, "Purchase")
                                val file = File(
                                    myDirectory,
                                    filename
                                )
                                val pdfUri = FileProvider.getUriForFile(
                                    applicationContext,
                                    BuildConfig.APPLICATION_ID,
                                    file
                                )
                                val intent = Intent(Intent.ACTION_VIEW)
                                intent.setDataAndType(pdfUri, "application/pdf")
                                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                startActivity(intent)

                            }
                            false -> {
                                print(
                                    "Purchase PDF",
                                    PdfDocumentAdapter(
                                        this@PurchaseBillDetailActivity,
                                        filename,
                                        "Purchase_Voucher"
                                    ),
                                    PrintAttributes.Builder()
                                        .setMediaSize(PrintAttributes.MediaSize.ISO_A4).build()
                                )

                            }
                        }

                        progressBar_Purchasebilldetail.visibility = View.GONE
                    }

                }

            })
        }


    }

    fun voucherTextAPI(
        token: String?,
        transaction_id: String?
    ) {

        if (NetworkUtils.isConnected()) {

            viewModel.voucherText(token, "purchase", transaction_id).observe(this, Observer {
                it?.let { resource ->
                    when (resource.status) {
                        Status.SUCCESS -> {
                            if (it.data?.status == true) {
                                val sharetext: String = it.data.data!!

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    if (isFromWhatsapp) {
                                        sendWhatsAppMsg(
                                            Html.fromHtml(
                                                sharetext,
                                                Html.FROM_HTML_MODE_COMPACT
                                            ).toString()
                                        )
                                    } else {
                                        sendSMS(
                                            Html.fromHtml(
                                                sharetext,
                                                Html.FROM_HTML_MODE_COMPACT
                                            ).toString()
                                        )
                                    }

                                } else {
                                    if (isFromWhatsapp) {
                                        sendWhatsAppMsg(Html.fromHtml(sharetext).toString())
                                    } else {
                                        sendSMS(Html.fromHtml(sharetext).toString())
                                    }
                                }

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

    fun sendWhatsAppMsg(txtMsg: String) {
        val waIntent = Intent(Intent.ACTION_SEND)
        waIntent.type = "text/plain"
        val text = txtMsg
        waIntent.setPackage("com.whatsapp")
        if (waIntent != null) {
            waIntent.putExtra(Intent.EXTRA_TEXT, text) //
            startActivity(Intent.createChooser(waIntent, text))
        } else {
            Toast.makeText(this, "WhatsApp is not installed", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun sendSMS(txtMsg: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) // At least KitKat
        {
            val defaultSmsPackageName =
                getDefaultSmsAppPackageName(this)
            val sendIntent = Intent(Intent.ACTION_SEND)
            sendIntent.type = "text/plain"
            sendIntent.putExtra(Intent.EXTRA_TEXT, txtMsg)
            if (defaultSmsPackageName != null) // Can be null in case that there is no default, then the user would be able to choose
            // any app that support this intent.
            {
                sendIntent.setPackage(defaultSmsPackageName)
            }
            startActivity(sendIntent)
        } else  // For early versions, do what worked for you before.
        {


            val smsIntent = Intent(Intent.ACTION_VIEW)
            smsIntent.type = "vnd.android-dir/mms-sms"
            smsIntent.putExtra("sms_body", txtMsg)
            try {
                startActivity(smsIntent)
                Log.i("Finished sending SMS...", "")
            } catch (ex: ActivityNotFoundException) {
                Toast.makeText(
                    this@PurchaseBillDetailActivity,
                    "SMS faild, please try again later.", Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    @Nullable
    fun getDefaultSmsAppPackageName(context: Context): String? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) try {
            return Telephony.Sms.getDefaultSmsPackage(context)
        } catch (e: Throwable) {
        }
        val intent = Intent(Intent.ACTION_VIEW)
            .addCategory(Intent.CATEGORY_DEFAULT).setType("vnd.android-dir/mms-sms")
        val resolveInfoList = context.packageManager.queryIntentActivities(intent, 0)
        return if (!resolveInfoList.isEmpty()) resolveInfoList[0].activityInfo.packageName else null
    }

    fun showDownloadNotification() {
        try {
            val file = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                filename
            )
            val pdfUri = FileProvider.getUriForFile(
                applicationContext,
                BuildConfig.APPLICATION_ID,
                file
            )
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(pdfUri, "application/pdf")
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

            //use the flag FLAG_UPDATE_CURRENT to override any notification already there

            val contentIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )

            notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notificationChannel =
                    NotificationChannel("id", "an", NotificationManager.IMPORTANCE_LOW)
                notificationChannel.description = "no sound"
                notificationChannel.setSound(null, null)
                notificationChannel.enableLights(false)
                notificationChannel.lightColor = Color.BLUE
                notificationChannel.enableVibration(false)
                notificationManager.createNotificationChannel(notificationChannel)
            }


            notificationBuilder = NotificationCompat.Builder(this, "id")
                .setSmallIcon(R.drawable.ic_stat_picture_as_pdf)
                .setContentTitle("${filename} Voucher downloaded")
                .setContentText("Tap to Open PDF")
                .setAutoCancel(true)
                .setContentIntent(contentIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            notificationManager.notify(10, notificationBuilder.build())
        } catch (e: Exception) {
            // Log.e("notification", "Notification $e")
        }
    }

    private fun print(
        name: String, adapter: PrintDocumentAdapter,
        attrs: PrintAttributes
    ): PrintJob? {
        startService(Intent(this, PrintJobMonitorService::class.java))
        return mgr!!.print(name, adapter, attrs)
    }

    private fun getFileNamefromHeader(headerString: String): String {
        val contentDisposition: String =
            headerString
        if (contentDisposition != null && "" != contentDisposition) {
            // Get filename from the Content-Disposition header.
            val pattern: Pattern =
                Pattern.compile("filename=['\"]?([^'\"\\s]+)['\"]?")
            val matcher: Matcher = pattern.matcher(contentDisposition)
            if (matcher.find()) {
                filename = (matcher.group(1))
                /* Log.v("filename",filename.toString())*/

            }
        }
        return filename
    }

    private fun writeResponseBodyToDisk(body: ResponseBody): Boolean {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            // only for Q and newer versions
            try {
                val pdfInputStream: InputStream = body.byteStream()

                val values = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, filename)
                    put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
                    put(MediaStore.Downloads.IS_PENDING, 1)
                }
                val resolver = this.contentResolver

                val collection =
                    MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)

                val itemUri = resolver.insert(collection, values)

                if (itemUri != null) {
                    resolver.openFileDescriptor(itemUri, "w").use { parcelFileDescriptor ->
                        ParcelFileDescriptor.AutoCloseOutputStream(parcelFileDescriptor)
                            .write(pdfInputStream.readBytes())
                    }
                    values.clear()
                    values.put(MediaStore.Downloads.IS_PENDING, 0)
                    values.put(
                        MediaStore.Downloads.RELATIVE_PATH,
                        "Download/" + "GoldBook/" + "Purchase"
                    );

                    resolver.update(itemUri, values, null, null)
                }
                fileSavePath = getPath(itemUri)!!
                return true
            } catch (e: Exception) {
                e.printStackTrace()
                return false
            }
        } else {
            return try {
                // todo change the file location/name according to your needs
                val rootDirtory = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    "GoldBook"
                )
                val myDirectory = File(rootDirtory, "Purchase")
                try {
                    if (!myDirectory.exists()) {
                        myDirectory.mkdirs()
                    }
                } catch (e: Exception) {
                }

                val futureStudioIconFile = File(
                    myDirectory,
                    filename
                )
                if (!futureStudioIconFile.exists()) futureStudioIconFile.createNewFile()
                var inputStream: InputStream? = null
                var outputStream: OutputStream? = null
                try {
                    val fileReader = ByteArray(4096)
                    val fileSize = body.contentLength()
                    var fileSizeDownloaded: Long = 0
                    inputStream = body.byteStream()
                    outputStream = FileOutputStream(futureStudioIconFile)
                    while (true) {
                        val read: Int = inputStream.read(fileReader)
                        if (read == -1) {
                            break
                        }
                        outputStream.write(fileReader, 0, read)
                        fileSizeDownloaded += read.toLong()

                    }
                    fileSavePath = futureStudioIconFile.path
                    outputStream.flush()
                    true
                } catch (e: IOException) {
                    false
                } finally {
                    if (inputStream != null) {
                        inputStream.close()
                    }
                    if (outputStream != null) {
                        outputStream.close()
                    }
                }
            } catch (e: IOException) {
                false
            }
        }

    }

    fun getPath(uri: Uri?): String? {
        val returnCursor: Cursor? = this.contentResolver.query(uri!!, null, null, null, null)
        val columnIndex = returnCursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        returnCursor.moveToFirst();
        val path = returnCursor.getString(columnIndex)
        return path
    }

    /*private fun checkPermission(): Boolean {
        val result = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            PERMISSION_REQUEST_CODE
        )
    }*/
    private fun checkandRequestPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
             Permissions.check(
            this /*context*/,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            null,
            object : PermissionHandler() {
                override fun onGranted() {
                    calltovoucherPrint()
                }

                override fun onDenied(context: Context?, deniedPermissions: ArrayList<String>?) {
                    super.onDenied(context, deniedPermissions)
                    Toast.makeText(
                        applicationContext,
                        "Permission required to save voucher",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            })
        }else{
            calltovoucherPrint()
        }

    }

    fun showFullImage(imgpath: String?) {
        val builder = Dialog(this)
        builder.requestWindowFeature(Window.FEATURE_NO_TITLE)
        builder.getWindow()?.setBackgroundDrawable(
            ColorDrawable(Color.TRANSPARENT)
        )
        builder.setContentView(R.layout.custom_image_dialog)

        builder.setOnDismissListener(DialogInterface.OnDismissListener {
            //nothing;
        })
        builder.btnCancel.clickWithDebounce {
            builder.dismiss()
        }
        val imageView = ImageView(this)
        Glide.with(this).load(imgpath).placeholder(R.drawable.ic_user_placeholder)
            .into(builder.imgFull)

        builder.show()
    }

    private fun ensureDeleteDialog(purchase: String) {
        val builder = AlertDialog.Builder(this)
        val DeleteClick = { dialog: DialogInterface, which: Int ->
            //purchase  delete api call
            deletePurchase(
                loginModel?.data?.bearer_access_token,
                purchaseDetailModel.transactionData?.transaction_id
            )
        }
        val dialogdismiss = { dialog: DialogInterface, which: Int ->
            dialog.dismiss()
        }
        with(builder)
        {
            setTitle(context.getString(R.string.delPurchaseDialog1Title))
            setMessage(context.getString(R.string.purchaseDialog1Message))
            setPositiveButton(context.getString(R.string.Cancel), dialogdismiss)
            setNeutralButton(
                context.getString(R.string.Delete),
                DialogInterface.OnClickListener(function = DeleteClick)
            )
            show()
        }
    }

    private fun deletePurchase(token: String?, salesID: String?) {
        if (NetworkUtils.isConnected()) {
            if (isValidClickPressed()) {
                viewModel.deletePurchase(token, salesID).observe(this, Observer {
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
                                finish()

                            }
                            Status.ERROR -> {
                                CommonUtils.hideProgress()

                                Toast.makeText(
                                    this,
                                    it.data?.errormessage?.message,
                                    Toast.LENGTH_LONG
                                )
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
    }


    fun purchaseDetailAPI(
        token: String?,
        transaction_id: String?
    ) {

        if (NetworkUtils.isConnected()) {

            viewModel.purchaseDetail(token, transaction_id).observe(this, Observer {
                it?.let { resource ->
                    when (resource.status) {
                        Status.SUCCESS -> {
                            if (it.data?.status == true) {
                                llPurchaseBillDetail_root.visibility = View.VISIBLE
                                purchaseDetailModel = it.data.data!!

                                popupMenu = PopupMenu(this, imgRight)
                                if (purchaseDetailModel.transactionData?.is_gst_applicable.toString()
                                        .contains("1")
                                ) {
                                    popupMenu.menuInflater.inflate(
                                        R.menu.popup_menu_purchase_detail,
                                        popupMenu.menu
                                    )
                                    //popupMenu.menu.getItem(0).setVisible(false)
                                } else {
                                    popupMenu.menuInflater.inflate(
                                        R.menu.popup_menu_sales_detail,
                                        popupMenu.menu
                                    )
                                   // popupMenu.menu.getItem(0).setVisible(false)
                                    popupMenu.menu.getItem(3).setVisible(true)
                                }

                                tvTitle.text = purchaseDetailModel.transactionData?.invoice_number
                                tv_purchasebilldetail_custname.text =
                                    purchaseDetailModel.transactionData?.display_name
                                /* tv_purchasebilldetail_noofitems.text =
                                    purchaseDetailModel.transactionData??.no_of_items.toString() + " item"*/
                                tv_purchasebilldetail_transactiondate.text =
                                    purchaseDetailModel.transactionData?.transaction_date

                                retrieveListforitem(purchaseDetailModel.transactionData?.item!!)
                                addIRTDatainPref()
                                getIssueReceiveDataFromPref()

                                if (purchaseDetailModel.transactionData!!.image != null && purchaseDetailModel.transactionData!!.image?.size!! > 0) {
                                    binding.tvAttachmentLabelPurchase.visibility = View.VISIBLE
                                    binding.ivPurchasebilldetailAttachmentone.visibility = View.VISIBLE
                                    imageURL = purchaseDetailModel.transactionData!!.image?.get(0)?.image
                                    Glide.with(this).load(imageURL).circleCrop()
                                        .placeholder(R.drawable.ic_user_placeholder)
                                        .into(binding.ivPurchasebilldetailAttachmentone)
                                }

                                if(!purchaseDetailModel.transactionData?.total_gross_wt.equals("0.000")){
                                    binding.llNewpurchaseMetalgold.visibility = View.VISIBLE
                                    /*binding.tvNewpurchaseSubtotallabelSilver.visibility = View.VISIBLE
                                    binding.tvNewpurchaseSubtotalCol1Silver.visibility = View.VISIBLE
                                    binding.tvNewpurchaseSubtotalCol2Silver.visibility = View.VISIBLE*/
                                }else{
                                    binding.llNewpurchaseMetalgold.visibility = View.GONE

                                }

                                if(!purchaseDetailModel.transactionData?.silver_total_gross_wt.equals("0.000")){
                                    binding.llNewpurchaseMetalsilver.visibility = View.VISIBLE
                                    /*binding.tvNewpurchaseSubtotallabelSilver.visibility = View.VISIBLE
                                    binding.tvNewpurchaseSubtotalCol1Silver.visibility = View.VISIBLE
                                    binding.tvNewpurchaseSubtotalCol2Silver.visibility = View.VISIBLE*/
                                }else{
                                    binding.llNewpurchaseMetalsilver.visibility = View.GONE

                                }

                                if(!purchaseDetailModel.transactionData?.other_total_gross_wt.equals("0.000")){
                                    binding.llNewpurchaseMetalother.visibility = View.VISIBLE
                                }else{
                                    binding.llNewpurchaseMetalother.visibility = View.GONE
                                }
                                binding.tvNewpurchaseItemquantity.setText("Qty: "+ purchaseDetailModel.transactionData?.total_quantity)
                                binding.tvNewpurchaseGrossWt.setText("G: "+ purchaseDetailModel.transactionData?.total_gross_wt /*+ Constants.WEIGHT_GM_APPEND*/)
                                binding.tvNewpurchaseLessWt.setText("L: "+ purchaseDetailModel.transactionData?.total_less_wt /*+ Constants.WEIGHT_GM_APPEND*/)
                                binding.tvNewpurchaseNetWt.setText("N: "+ purchaseDetailModel.transactionData?.total_net_wt /*+ Constants.WEIGHT_GM_APPEND*/)
                                binding.tvNewpurchaseFineWt.setText("F: "+ purchaseDetailModel.transactionData?.total_fine_wt /*+ Constants.WEIGHT_GM_APPEND*/)
                             //   binding.tvNewpurchaseMiscCharges.setText(Constants.AMOUNT_RS_APPEND + purchaseDetailModel.transactionData?.total_misc_charges)
                                binding.tvNewpurchaseSubtotalCol1.setText(purchaseDetailModel.transactionData?.total_fine_wt)
                                binding.tvNewpurchaseSubtotalCol2.setText(purchaseDetailModel.transactionData?.final_total_amount)

                                binding.tvNewpurchaseSubtotalCol1Silver.setText(purchaseDetailModel.transactionData?.silver_total_fine_wt)


                                when(binding.tvNewpurchaseSubtotalCol1.text){
                                    "0.000"->{
                                        binding.tvNewpurchaseSubtotalCol1.setText(purchaseDetailModel.transactionData?.total_fine_wt)
                                        binding.tvNewpurchaseSubtotalCol1.setTextColor(
                                            ContextCompat.getColor(
                                                this,
                                                R.color.header_black_text
                                            )
                                        )
                                    }
                                    else->{
                                        binding.tvNewpurchaseSubtotalCol1.setText(purchaseDetailModel.transactionData?.total_fine_wt + " " + subTotalTerm)
                                        if (subTotalTermValue.equals("credit")) {
                                            binding.tvNewpurchaseSubtotalCol1.setTextColor(
                                                ContextCompat.getColor(
                                                    this,
                                                    R.color.credit_color
                                                )
                                            )
                                        }
                                    }
                                }

                                when(binding.tvNewpurchaseSubtotalCol2.text){
                                    "0.00"->{
                                        binding.tvNewpurchaseSubtotalCol2.setText(purchaseDetailModel.transactionData?.final_total_amount)
                                        binding.tvNewpurchaseSubtotalCol2.setTextColor(
                                            ContextCompat.getColor(
                                                this,
                                                R.color.header_black_text
                                            )
                                        )
                                    }
                                    else->{
                                        binding.tvNewpurchaseSubtotalCol2.setText(purchaseDetailModel.transactionData?.final_total_amount + " " + subTotalTerm)
                                        if (subTotalTermValue.equals("credit")) {
                                            binding.tvNewpurchaseSubtotalCol2.setTextColor(
                                                ContextCompat.getColor(
                                                    this,
                                                    R.color.credit_color
                                                )
                                            )
                                        }
                                    }
                                }

                                when(binding.tvNewpurchaseSubtotalCol1Silver.text){
                                    "0.000"->{
                                        binding.tvNewpurchaseSubtotalCol1Silver.setText(purchaseDetailModel.transactionData?.silver_total_fine_wt)
                                        binding.tvNewpurchaseSubtotalCol1Silver.setTextColor(
                                            ContextCompat.getColor(
                                                this,
                                                R.color.header_black_text
                                            )
                                        )
                                    }
                                    else->{
                                        binding.tvNewpurchaseSubtotalCol1Silver.setText(purchaseDetailModel.transactionData?.silver_total_fine_wt + " " + subTotalTerm)
                                        if (subTotalTermValue.equals("credit")) {
                                            binding.tvNewpurchaseSubtotalCol1Silver.setTextColor(
                                                ContextCompat.getColor(
                                                    this,
                                                    R.color.credit_color
                                                )
                                            )
                                        }
                                    }
                                }



                                binding.tvNewpurchaseTotalDueGold.setText(purchaseDetailModel.transactionData?.total_fine_wt_with_IRT)
                                binding.tvNewpurchaseTotalDueSilver.setText(purchaseDetailModel.transactionData?.total_silver_fine_wt_with_IRT)
                                binding.tvNewpurchaseTotalDueCash.setText(Constants.AMOUNT_RS_APPEND + purchaseDetailModel.transactionData?.grand_total)

                                binding.tvNewpurchaseSilverItemquantity.setText("Qty: "+ purchaseDetailModel.transactionData?.silver_total_quantity)
                                binding.tvNewpurchaseSilverGrossWt.setText("G: "+ purchaseDetailModel.transactionData?.silver_total_gross_wt /*+ Constants.WEIGHT_GM_APPEND*/)
                                binding.tvNewpurchaseSilverLessWt.setText("L: "+ purchaseDetailModel.transactionData?.silver_total_less_wt /*+ Constants.WEIGHT_GM_APPEND*/)
                                binding.tvNewpurchaseSilverNetWt.setText("N: "+ purchaseDetailModel.transactionData?.silver_total_net_wt /*+ Constants.WEIGHT_GM_APPEND*/)
                                binding.tvNewpurchaseSilverFineWt.setText("F: "+ purchaseDetailModel.transactionData?.silver_total_fine_wt /*+ Constants.WEIGHT_GM_APPEND*/)

                                binding.tvNewpurchaseOtherItemquantity.setText("Qty: "+ purchaseDetailModel.transactionData?.other_total_quantity)
                                binding.tvNewpurchaseOtherGrossWt.setText("G: "+ purchaseDetailModel.transactionData?.other_total_gross_wt /*+ Constants.WEIGHT_GM_APPEND*/)
                                binding.tvNewpurchaseOtherLessWt.setText("L: 0.000" /*+ Constants.WEIGHT_GM_APPEND*/)
                                binding.tvNewpurchaseOtherNetWt.setText("N: "+ purchaseDetailModel.transactionData?.other_total_net_wt /*+ Constants.WEIGHT_GM_APPEND*/)
                                binding.tvNewpurchaseOtherFineWt.setText("F: 0.000")



                                updateUIofTotalDue(purchaseDetailModel)

                                when (purchaseDetailModel.transactionData?.is_tcs_applicable.equals(
                                    "1"
                                )) {
                                    true -> {
                                        binding.radioTCSNewpurchase.visibility = View.VISIBLE
                                        binding.tvNewpurchaseTdstcsCol1.visibility = View.VISIBLE
                                        binding.tvNewpurchaseTcstdsCol2.visibility = View.VISIBLE

                                    }
                                    false -> {
                                        binding.radioTCSNewpurchase.visibility = View.GONE
                                        binding.tvNewpurchaseTdstcsCol1.visibility = View.GONE
                                        binding.tvNewpurchaseTcstdsCol2.visibility = View.GONE
                                    }
                                }

                                when (purchaseDetailModel.transactionData?.is_tds_applicable.equals(
                                    "1"
                                )) {
                                    true -> {
                                        binding.radioTDSNewpurchase.visibility = View.VISIBLE
                                        binding.tvNewpurchaseTdstcsCol1.visibility = View.VISIBLE
                                        binding.tvNewpurchaseTcstdsCol2.visibility = View.VISIBLE

                                    }
                                    false -> {
                                        binding.radioTDSNewpurchase.visibility = View.GONE
                                        binding.tvNewpurchaseTdstcsCol1.visibility = View.GONE
                                        binding.tvNewpurchaseTcstdsCol2.visibility = View.GONE
                                    }
                                }

                                if (!purchaseDetailModel.transactionData?.is_tds_applicable.equals("1") && !purchaseDetailModel.transactionData?.is_tcs_applicable.equals(
                                        "1"
                                    )
                                ) {
                                    binding.radiogroupTDSTCSNewpurchase.visibility = View.GONE

                                } else {
                                    binding.radiogroupTDSTCSNewpurchase.visibility = View.VISIBLE

                                }


                                if (purchaseDetailModel.transactionData?.tds_tcs_enable.equals("tcs")) {
                                    binding.radioTCSNewpurchase.isChecked = true
                                    binding.tvNewpurchaseTcstdsCol2.setText(purchaseDetailModel.transactionData!!.tcs_amount)

                                    binding.tvNewpurchaseTdstcsCol1.mLabelView!!.setText(
                                        purchaseDetailModel.transactionData?.tcsData!!.ledger_name
                                    )
                                }

                                if (purchaseDetailModel.transactionData?.tds_tcs_enable.equals("tds")) {
                                    binding.radioTDSNewpurchase.isChecked = true
                                    binding.tvNewpurchaseTcstdsCol2.setText("-"+purchaseDetailModel.transactionData!!.tds_amount)

                                    binding.tvNewpurchaseTdstcsCol1.mLabelView!!.setText(
                                        purchaseDetailModel.transactionData?.tdsData!!.ledger_name
                                    )
                                }


                                //condition for gst purchase
                                when (purchaseDetailModel.transactionData?.is_gst_applicable.toString()
                                    .contains("1")) {
                                    true -> {
                                        if (
                                            (purchaseDetailModel.transactionData?.sgst_amount!!.toBigDecimal()> BigDecimal.ZERO
                                                    && !purchaseDetailModel.transactionData?.sgst_amount!!.isBlank()) &&
                                            (purchaseDetailModel.transactionData?.place_of_supply_id.equals(
                                                loginModel.data!!.branch_info!!.state_id.toString()
                                            ))
                                        ) {
                                            binding.tvNewpurchaseSgstCol0.visibility = View.VISIBLE
                                            binding.tvNewpurchaseSgstCol1.visibility = View.VISIBLE
                                            binding.tvNewpurchaseSgstCol2.visibility = View.VISIBLE

                                            binding.tvNewpurchaseSgstCol1.mLabelView!!.setText(
                                                purchaseDetailModel.transactionData?.sgstData?.ledger_name
                                            )
                                            binding.tvNewpurchaseSgstCol2.setText(Constants.AMOUNT_RS_APPEND + purchaseDetailModel.transactionData?.sgst_amount)
                                        } else {
                                            binding.tvNewpurchaseSgstCol0.visibility = View.GONE
                                            binding.tvNewpurchaseSgstCol1.visibility = View.GONE
                                            binding.tvNewpurchaseSgstCol2.visibility = View.GONE
                                        }

                                        if (
                                            (purchaseDetailModel.transactionData?.cgst_amount!!.toBigDecimal()> BigDecimal.ZERO
                                                    && !purchaseDetailModel.transactionData?.cgst_amount!!.isBlank()) &&
                                            (purchaseDetailModel.transactionData?.place_of_supply_id.equals(
                                                loginModel.data!!.branch_info!!.state_id.toString()
                                            ))
                                        ) {
                                            binding.tvNewpurchaseCgstCol0.visibility = View.VISIBLE
                                            binding.tvNewpurchaseCgstCol1.visibility = View.VISIBLE
                                            binding.tvNewpurchaseCgstCol2.visibility = View.VISIBLE
                                            binding.tvNewpurchaseCgstCol1.mLabelView!!.setText(
                                                purchaseDetailModel.transactionData?.cgstData?.ledger_name
                                            )
                                            binding.tvNewpurchaseCgstCol2.setText(Constants.AMOUNT_RS_APPEND + purchaseDetailModel.transactionData?.cgst_amount)

                                        } else {
                                            binding.tvNewpurchaseCgstCol0.visibility = View.GONE
                                            binding.tvNewpurchaseCgstCol1.visibility = View.GONE
                                            binding.tvNewpurchaseCgstCol2.visibility = View.GONE
                                        }

                                        if ((purchaseDetailModel.transactionData?.igst_amount!!.toBigDecimal()> BigDecimal.ZERO && !purchaseDetailModel.transactionData?.igst_amount!!.isBlank()) &&
                                            (!purchaseDetailModel.transactionData?.place_of_supply_id.equals(
                                                loginModel.data!!.branch_info!!.state_id.toString()
                                            ))
                                        ) {
                                            binding.tvNewpurchaseIgstCol0.visibility = View.VISIBLE
                                            binding.tvNewpurchaseIgstCol1.visibility = View.VISIBLE
                                            binding.tvNewpurchaseIgstCol2.visibility = View.VISIBLE
                                            binding.tvNewpurchaseIgstCol1.mLabelView!!.setText(
                                                purchaseDetailModel.transactionData?.igstData?.ledger_name
                                            )
                                            binding.tvNewpurchaseIgstCol2.setText(Constants.AMOUNT_RS_APPEND + purchaseDetailModel.transactionData?.igst_amount)
                                        } else {
                                            binding.tvNewpurchaseIgstCol0.visibility = View.GONE
                                            binding.tvNewpurchaseIgstCol1.visibility = View.GONE
                                            binding.tvNewpurchaseIgstCol2.visibility = View.GONE
                                        }


                                    }
                                    false -> {

                                        //for non-gst purchase
                                        binding.tvNewpurchaseSgstCol0.visibility = View.GONE
                                        binding.tvNewpurchaseSgstCol1.visibility = View.GONE
                                        binding.tvNewpurchaseSgstCol2.visibility = View.GONE

                                        binding.tvNewpurchaseCgstCol0.visibility = View.GONE
                                        binding.tvNewpurchaseCgstCol1.visibility = View.GONE
                                        binding.tvNewpurchaseCgstCol2.visibility = View.GONE

                                        binding.tvNewpurchaseIgstCol0.visibility = View.GONE
                                        binding.tvNewpurchaseIgstCol1.visibility = View.GONE
                                        binding.tvNewpurchaseIgstCol2.visibility = View.GONE

                                        binding.radiogroupTDSTCSNewpurchase.visibility = View.GONE
                                        binding.tvNewpurchaseTdstcsCol1.visibility = View.GONE
                                        binding.tvNewpurchaseTcstdsCol2.visibility = View.GONE

                                    }

                                }

                                if (purchaseDetailModel.transactionData?.is_show_round_off.equals("1")){
                                    binding.tvNewpurchaseRoundOffCol0.visibility = View.VISIBLE
                                    binding.tvNewpurchaseRoundoffCol1.visibility = View.VISIBLE
                                    binding.tvNewpurchaseRoundoffCol2.visibility = View.VISIBLE

                                    binding.tvNewpurchaseRoundoffCol1.mLabelView!!.setText(
                                        purchaseDetailModel.transactionData?.roundOffLedgerData?.ledger_name
                                    )
                                    binding.tvNewpurchaseRoundoffCol2.setText(Constants.AMOUNT_RS_APPEND + purchaseDetailModel.transactionData?.round_off_total)

                                }else{
                                    binding.tvNewpurchaseRoundOffCol0.visibility = View.GONE
                                    binding.tvNewpurchaseRoundoffCol1.visibility = View.GONE
                                    binding.tvNewpurchaseRoundoffCol2.visibility = View.GONE
                                }

/*
                                when (!purchaseDetailModel.transactionData?.round_off_total.equals("0.00")) {
                                    true -> {
                                        binding.tvNewpurchaseRoundOffCol0.visibility = View.VISIBLE
                                        binding.tvNewpurchaseRoundoffCol1.visibility = View.VISIBLE
                                        binding.tvNewpurchaseRoundoffCol2.visibility = View.VISIBLE

                                        binding.tvNewpurchaseRoundoffCol1.mLabelView!!.setText(
                                            purchaseDetailModel.transactionData?.roundOffLedgerData?.ledger_name
                                        )
                                        binding.tvNewpurchaseRoundoffCol2.setText(Constants.AMOUNT_RS_APPEND + purchaseDetailModel.transactionData?.round_off_total)

                                    }
                                    false -> {
                                        binding.tvNewpurchaseRoundOffCol0.visibility = View.GONE
                                        binding.tvNewpurchaseRoundoffCol1.visibility = View.GONE
                                        binding.tvNewpurchaseRoundoffCol2.visibility = View.GONE
                                    }
                                }
*/

                                updateOpeningFineOpeningCash(purchaseDetailModel)
                                updateClosingFineClosingCash(purchaseDetailModel)


                                // closing bal set
                                ll_closingbal_Newpurchase.visibility = View.VISIBLE


                                if (purchaseDetailModel.transactionData?.remarks != null) {
                                    tv_purchasebilldetail_notes.text =
                                        purchaseDetailModel.transactionData?.remarks
                                }


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

    private fun getIssueReceiveDataFromPref() {
        if (prefs.contains(Constants.PREF_SALES_LINE_INFO_KEY)) {
            val collectionType =
                object :
                    TypeToken<ArrayList<SalesLineModel.SaleLineModelDetails>>() {}.type
            purchaseLineList =
                Gson().fromJson(
                    prefs[Constants.PREF_SALES_LINE_INFO_KEY, ""],
                    collectionType
                )
            setupIssueReceiveAdapter()
        }
    }

    private fun setupIssueReceiveAdapter() {
        when(purchaseLineList.size>0){
            true->{
                binding.rvIssueReceiveList.visibility = View.VISIBLE
                issueReceiveadapter.apply {
                    addissueReceiveList(purchaseLineList)
                    notifyDataSetChanged()
                }
            }else->{

        }
        }

    }

    private fun addIRTDatainPref() {
        purchaseLineList.clear()
        for (i in 0 until purchaseDetailModel.IRTData!!.size) {

            if (!purchaseDetailModel.IRTData!!.get(i).transaction_type.equals("")) {

                val saleIRTModel = SalesLineModel.SaleLineModelDetails(
                    purchaseDetailModel.IRTData!!.get(i).IRTDetails!!.cash_amount,
                    purchaseDetailModel.IRTData!!.get(i).IRTDetails!!.cash_ledger,
                    purchaseDetailModel.IRTData!!.get(i).IRTDetails!!.cash_ledger_name,
                    purchaseDetailModel.IRTData!!.get(i).IRTDetails!!.cash_description,
                    purchaseDetailModel.IRTData!!.get(i).IRTDetails!!.bank_amount,
                    purchaseDetailModel.IRTData!!.get(i).IRTDetails!!.bank_ledger,
                    purchaseDetailModel.IRTData!!.get(i).IRTDetails!!.bank_ledger_name,
                    purchaseDetailModel.IRTData!!.get(i).IRTDetails!!.bank_mode,
                    purchaseDetailModel.IRTData!!.get(i).IRTDetails!!.cheque_number,
                    purchaseDetailModel.IRTData!!.get(i).IRTDetails!!.cheque_date,
                    purchaseDetailModel.IRTData!!.get(i).IRTDetails!!.favouring_name,
                    purchaseDetailModel.IRTData!!.get(i).IRTDetails!!.deuct_charges,
                    purchaseDetailModel.IRTData!!.get(i).IRTDetails!!.deuct_charges_percentage,
                    purchaseDetailModel.IRTData!!.get(i).IRTDetails!!.bank_final_amt,
                    purchaseDetailModel.IRTData!!.get(i).IRTDetails!!.recipient_bank,
                    purchaseDetailModel.IRTData!!.get(i).IRTDetails!!.account_no,
                    purchaseDetailModel.IRTData!!.get(i).IRTDetails!!.ifs_code,
                    purchaseDetailModel.IRTData!!.get(i).IRTDetails!!.utr_number,
                    purchaseDetailModel.IRTData!!.get(i).IRTDetails!!.bank_description,
                    purchaseDetailModel.IRTData!!.get(i).IRTDetails!!.rcm_gold_rate,
                    purchaseDetailModel.IRTData!!.get(i).IRTDetails!!.rate_cut_amount,
                    purchaseDetailModel.IRTData!!.get(i).IRTDetails!!.rate_cut_fine_term,
                    purchaseDetailModel.IRTData!!.get(i).IRTDetails!!.metal_type_id_rate_cut,
                    purchaseDetailModel.IRTData!!.get(i).IRTDetails!!.rate_cut_fine,
                    purchaseDetailModel.IRTData!!.get(i).IRTDetails!!.item_id,
                    purchaseDetailModel.IRTData!!.get(i).IRTDetails!!.item_name,
                    purchaseDetailModel.IRTData!!.get(i).IRTDetails!!.metal_type_id_metal,
                    purchaseDetailModel.IRTData!!.get(i).IRTDetails!!.maintain_stock_in_name_metal,
                    purchaseDetailModel.IRTData!!.get(i).IRTDetails!!.gross_wt,
                    purchaseDetailModel.IRTData!!.get(i).IRTDetails!!.less_wt,
                    purchaseDetailModel.IRTData!!.get(i).IRTDetails!!.net_wt,
                    purchaseDetailModel.IRTData!!.get(i).IRTDetails!!.touch,
                    purchaseDetailModel.IRTData!!.get(i).IRTDetails!!.wast,
                    purchaseDetailModel.IRTData!!.get(i).IRTDetails!!.fine_wt,
                    "","","","","","",
                    purchaseDetailModel.IRTData!!.get(i).IRTDetails!!.type,
                    purchaseDetailModel.IRTData!!.get(i).IRTDetails!!.transaction_type,
                    purchaseDetailModel.IRTData!!.get(i).IRTDetails!!.transaction_title

                )

                purchaseLineList.add(saleIRTModel)
            }
        }

        prefs[Constants.PREF_SALES_LINE_INFO_KEY] = Gson().toJson(purchaseLineList)
    }

    private fun updateUIofTotalDue(purchaseDetailModel: SaleDetailModel.Data) {

        if(!purchaseDetailModel.transactionData?.total_fine_wt_with_IRT.equals("0.000") &&
            purchaseDetailModel.transactionData?.total_silver_fine_wt_with_IRT.equals("0.000")){
            binding.llNewpurchaseSilverTotaldue.visibility = View.GONE
            binding.tvNewpurchaseTotaldueGoldLabel.visibility =View.VISIBLE
            binding.tvNewpurchaseTotalDueGold.visibility =View.VISIBLE
        }

        if(!purchaseDetailModel.transactionData?.total_fine_wt_with_IRT.equals("0.000") &&
            !purchaseDetailModel.transactionData?.total_silver_fine_wt_with_IRT.equals("0.000")){
            binding.llNewpurchaseSilverTotaldue.visibility = View.VISIBLE
            binding.tvNewpurchaseTotaldueGoldLabel.visibility =View.VISIBLE
            binding.tvNewpurchaseTotalDueGold.visibility =View.VISIBLE
        }



        if (purchaseDetailModel.transactionData?.total_fine_wt_with_IRT.equals("0.000") &&
            !purchaseDetailModel.transactionData?.total_silver_fine_wt_with_IRT.equals("0.000")) {
            binding.llNewpurchaseSilverTotaldue.visibility = View.GONE
            binding.tvNewpurchaseTotaldueGoldLabel.setText("S: ")
            binding.tvNewpurchaseTotalDueGold.setText(purchaseDetailModel.transactionData?.total_silver_fine_wt_with_IRT)
            when (binding.tvNewpurchaseTotalDueGold.text) {
                "0.000" -> {
                    binding.tvNewpurchaseTotalDueGold.text =
                        purchaseDetailModel.transactionData?.total_silver_fine_wt_with_IRT
                    binding.tvNewpurchaseTotalDueGold.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.header_black_text
                        )
                    )
                }

                else -> {
                    binding.tvNewpurchaseTotalDueGold.text =
                        purchaseDetailModel.transactionData?.total_silver_fine_wt_with_IRT + " " +
                                purchaseDetailModel.transactionData?.total_silver_fine_wt_with_IRT_term
                    if (purchaseDetailModel.transactionData?.total_silver_fine_wt_with_IRT_short_term.equals(
                            "Dr",
                            ignoreCase = true
                        )
                    ) {
                        binding.tvNewpurchaseTotalDueGold.setTextColor(
                            ContextCompat.getColor(
                                this,
                                R.color.debit_color
                            )
                        )
                    } else
                        binding.tvNewpurchaseTotalDueGold.setTextColor(
                            ContextCompat.getColor(
                                this,
                                R.color.credit_color
                            )
                        )
                }
            }

            when (binding.tvNewpurchaseTotalDueCash.text) {
                "0.00" -> {
                    binding.tvNewpurchaseTotalDueCash.text =
                        purchaseDetailModel.transactionData?.grand_total
                    binding.tvNewpurchaseTotalDueCash.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.header_black_text
                        )
                    )
                }
                else -> {
                    binding.tvNewpurchaseTotalDueCash.text =
                        purchaseDetailModel.transactionData?.grand_total + " " +
                                purchaseDetailModel.transactionData?.grand_total_term
                    if (purchaseDetailModel.transactionData?.grand_total_short_term.equals(
                            "Dr",
                            ignoreCase = true
                        )
                    ) {
                        binding.tvNewpurchaseTotalDueCash.setTextColor(
                            ContextCompat.getColor(
                                this,
                                R.color.debit_color
                            )
                        )
                    } else
                        binding.tvNewpurchaseTotalDueCash.setTextColor(
                            ContextCompat.getColor(
                                this,
                                R.color.credit_color
                            )
                        )
                }
            }

        } else {
            binding.tvNewpurchaseTotaldueGoldLabel.setText("G: ")
            updateTotalDuewithDrCr(purchaseDetailModel)
        }

        /*if(!purchaseDetailModel.transactionData?.total_silver_fine_wt_with_IRT.equals("0.000")){
            binding.llNewinvoiceSilverTotaldue.visibility = View.VISIBLE
        }else{
            binding.llNewinvoiceSilverTotaldue.visibility = View.GONE
        }*/


        if(purchaseDetailModel.transactionData?.total_fine_wt_with_IRT.equals("0.000") &&
            purchaseDetailModel.transactionData?.total_silver_fine_wt_with_IRT.equals("0.000")){
            binding.llNewpurchaseSilverTotaldue.visibility = View.GONE
            binding.tvNewpurchaseTotaldueGoldLabel.visibility = View.GONE
            binding.tvNewpurchaseTotalDueGold.visibility = View.GONE
        }else{
            //  binding.llNewinvoiceSilverTotaldue.visibility = View.VISIBLE
            //  tv_newinvoice_totaldue_gold_label.visibility = View.VISIBLE
            // tv_newinvoice_totalDue_gold.visibility = View.VISIBLE
        }
    }


    private fun updateTotalDuewithDrCr(purchaseDetailModel: SaleDetailModel.Data) {
        when (binding.tvNewpurchaseTotalDueGold.text) {
            "0.000" -> {
                binding.tvNewpurchaseTotalDueGold.text =
                    purchaseDetailModel.transactionData?.total_fine_wt_with_IRT
                binding.tvNewpurchaseTotalDueGold.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.header_black_text
                    )
                )
            }

            else -> {
                binding.tvNewpurchaseTotalDueGold.text =
                    purchaseDetailModel.transactionData?.total_fine_wt_with_IRT + " " +
                            purchaseDetailModel.transactionData?.total_fine_wt_with_IRT_term
                if (purchaseDetailModel.transactionData?.total_fine_wt_with_IRT_short_term.equals(
                        "Dr",
                        ignoreCase = true
                    )
                ) {
                    binding.tvNewpurchaseTotalDueGold.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.debit_color
                        )
                    )
                } else
                    binding.tvNewpurchaseTotalDueGold.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.credit_color
                        )
                    )
            }
        }

        when (binding.tvNewpurchaseTotalDueSilver.text) {
            "0.000" -> {
                binding.tvNewpurchaseTotalDueSilver.text =
                    purchaseDetailModel.transactionData?.total_silver_fine_wt_with_IRT
                binding.tvNewpurchaseTotalDueSilver.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.header_black_text
                    )
                )
            }

            else -> {
                binding.tvNewpurchaseTotalDueSilver.text =
                    purchaseDetailModel.transactionData?.total_silver_fine_wt_with_IRT + " " +
                            purchaseDetailModel.transactionData?.total_silver_fine_wt_with_IRT_term
                if (purchaseDetailModel.transactionData?.total_silver_fine_wt_with_IRT_short_term.equals(
                        "Dr",
                        ignoreCase = true
                    )
                ) {
                    binding.tvNewpurchaseTotalDueSilver.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.debit_color
                        )
                    )
                } else
                    binding.tvNewpurchaseTotalDueSilver.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.credit_color
                        )
                    )
            }
        }

        when (binding.tvNewpurchaseTotalDueCash.text) {
            "0.00" -> {
                binding.tvNewpurchaseTotalDueCash.text =
                    purchaseDetailModel.transactionData?.grand_total
                binding.tvNewpurchaseTotalDueCash.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.header_black_text
                    )
                )
            }
            else -> {
                binding.tvNewpurchaseTotalDueCash.text =
                    purchaseDetailModel.transactionData?.grand_total + " " +
                            purchaseDetailModel.transactionData?.grand_total_term
                if (purchaseDetailModel.transactionData?.grand_total_short_term.equals(
                        "Dr",
                        ignoreCase = true
                    )
                ) {
                    binding.tvNewpurchaseTotalDueCash.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.debit_color
                        )
                    )
                } else
                    binding.tvNewpurchaseTotalDueCash.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.credit_color
                        )
                    )
            }
        }
    }


    private fun updateClosingFineClosingCash(purchaseDetailModel: SaleDetailModel.Data) {
        if (purchaseDetailModel.transactionData?.closing_fine_balance!!.startsWith(
                "-"
            )
        ) {

            val clos_fine_bal: String =
                purchaseDetailModel.transactionData?.closing_fine_balance!!.toString()
                    .trim()
                    .substring(1)
            binding.tvCloBalFineWtDetailpurchase.text = clos_fine_bal

        }
        when (purchaseDetailModel.transactionData?.closing_fine_balance!!) {
            "0.000" -> {
                binding.tvCloBalFineWtDetailpurchase.text =
                    purchaseDetailModel.transactionData?.closing_fine_balance!!
                binding.tvCloBalFineWtDetailpurchase.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.header_black_text
                    )
                )
            }
            else -> {
                binding.tvCloBalFineWtDetailpurchase.text =
                    purchaseDetailModel.transactionData?.closing_fine_balance!! + " " +
                            purchaseDetailModel.transactionData?.closing_fine_balance_term
                if (purchaseDetailModel.transactionData?.closing_fine_balance_short_term.equals(
                        "Dr",
                        ignoreCase = true
                    )
                ) {
                    binding.tvCloBalFineWtDetailpurchase.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.debit_color
                        )
                    )
                } else
                    binding.tvCloBalFineWtDetailpurchase.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.credit_color
                        )
                    )
            }
        }

        if (purchaseDetailModel.transactionData?.closing_silver_fine_balance!!.startsWith("-")) {

            val clos_fine_bal: String =
                purchaseDetailModel.transactionData?.closing_silver_fine_balance!!.toString()
                    .trim()
                    .substring(1)
            binding.tvCloBalSilverDetailpurchase.text = clos_fine_bal

        }
        when (purchaseDetailModel.transactionData?.closing_silver_fine_balance!!) {
            "0.000" -> {
                binding.tvCloBalSilverDetailpurchase.text =
                    purchaseDetailModel.transactionData?.closing_silver_fine_balance!!
                binding.tvCloBalSilverDetailpurchase.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.header_black_text
                    )
                )
            }
            else -> {
                binding.tvCloBalSilverDetailpurchase.text =
                    purchaseDetailModel.transactionData?.closing_silver_fine_balance!! + " " +
                            purchaseDetailModel.transactionData?.closing_silver_fine_balance_term
                if (purchaseDetailModel.transactionData?.closing_silver_fine_balance_short_term.equals(
                        "Dr",
                        ignoreCase = true
                    )
                ) {
                    binding.tvCloBalSilverDetailpurchase.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.debit_color
                        )
                    )
                } else
                    binding.tvCloBalSilverDetailpurchase.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.credit_color
                        )
                    )
            }
        }


        if (purchaseDetailModel.transactionData?.closing_cash_balance!!.startsWith(
                "-"
            )
        ) {

            val clos_cash_bal: String =
                purchaseDetailModel.transactionData?.closing_cash_balance!!.toString()
                    .trim().substring(1)
            binding.tvCloBalCashDetailpurchase.text = clos_cash_bal
        }

        when (purchaseDetailModel.transactionData?.closing_cash_balance!!) {
            "0.00" -> {
                binding.tvCloBalCashDetailpurchase.text =
                    purchaseDetailModel.transactionData?.closing_cash_balance!!
                binding.tvCloBalCashDetailpurchase.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.header_black_text
                    )
                )
            }
            else -> {
                binding.tvCloBalCashDetailpurchase.text =
                    purchaseDetailModel.transactionData?.closing_cash_balance!! + " " +
                            purchaseDetailModel.transactionData?.closing_cash_balance_term
                if (purchaseDetailModel.transactionData?.closing_cash_balance_short_term.equals(
                        "Dr",
                        ignoreCase = true
                    )
                ) {
                    binding.tvCloBalCashDetailpurchase.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.debit_color
                        )
                    )
                } else
                    binding.tvCloBalCashDetailpurchase.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.credit_color
                        )
                    )
            }

        }

    }

    private fun updateOpeningFineOpeningCash(purchaseDetailModel: SaleDetailModel.Data) {
        if (purchaseDetailModel.transactionData?.opening_fine_balance!!.startsWith("-")) {

            val open_fine_bal: String =
                purchaseDetailModel.transactionData?.opening_fine_balance!!.toString()
                    .trim()
                    .substring(1)
            binding.tvOpenBalFineDetailPurchase.text = open_fine_bal

        }
        when (purchaseDetailModel.transactionData?.opening_fine_balance!!) {
            "0.000" -> {
                binding.tvOpenBalFineDetailPurchase.text =
                    purchaseDetailModel.transactionData?.opening_fine_balance!!
                binding.tvOpenBalFineDetailPurchase.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.header_black_text
                    )
                )
            }
            else -> {
                binding.tvOpenBalFineDetailPurchase.text =
                    purchaseDetailModel.transactionData?.opening_fine_balance!! + " " +
                            purchaseDetailModel.transactionData?.opening_fine_balance_term
                if (purchaseDetailModel.transactionData?.opening_fine_balance_short_term.equals(
                        "Dr",
                        ignoreCase = true
                    )
                ) {
                    binding.tvOpenBalFineDetailPurchase.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.debit_color
                        )
                    )
                } else
                    binding.tvOpenBalFineDetailPurchase.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.credit_color
                        )
                    )
            }
        }

        if (purchaseDetailModel.transactionData?.opening_silver_fine_balance!!.startsWith("-")) {

            val open_fine_bal: String =
                purchaseDetailModel.transactionData?.opening_silver_fine_balance!!.toString()
                    .trim()
                    .substring(1)
            binding.tvOpenBalFinesilverpurchase.text = open_fine_bal

        }
        when (purchaseDetailModel.transactionData?.opening_silver_fine_balance!!) {
            "0.000" -> {
                binding.tvOpenBalFinesilverpurchase.text =
                    purchaseDetailModel.transactionData?.opening_silver_fine_balance!!
                binding.tvOpenBalFinesilverpurchase.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.header_black_text
                    )
                )
            }
            else -> {
                binding.tvOpenBalFinesilverpurchase.text =
                    purchaseDetailModel.transactionData?.opening_silver_fine_balance!! + " " +
                            purchaseDetailModel.transactionData?.opening_silver_fine_balance_term
                if (purchaseDetailModel.transactionData?.opening_silver_fine_balance_short_term.equals(
                        "Dr",
                        ignoreCase = true
                    )
                ) {
                    binding.tvOpenBalFinesilverpurchase.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.debit_color
                        )
                    )
                } else
                    binding.tvOpenBalFinesilverpurchase.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.credit_color
                        )
                    )
            }
        }


        if (purchaseDetailModel.transactionData?.opening_cash_balance!!.startsWith(
                "-"
            )
        ) {

            val open_cash_bal: String =
                purchaseDetailModel.transactionData?.opening_cash_balance!!.toString()
                    .trim().substring(1)
            binding.tvOpenBalCashDetailPurchase.text = open_cash_bal
        }

        when (purchaseDetailModel.transactionData?.opening_cash_balance!!) {
            "0.00" -> {
                binding.tvOpenBalCashDetailPurchase.text =
                    purchaseDetailModel.transactionData?.opening_cash_balance!!
                binding.tvOpenBalCashDetailPurchase.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.header_black_text
                    )
                )
            }
            else -> {
                binding.tvOpenBalCashDetailPurchase.text =
                    purchaseDetailModel.transactionData?.opening_cash_balance!! + " " +
                            purchaseDetailModel.transactionData?.opening_cash_balance_term
                if (purchaseDetailModel.transactionData?.opening_cash_balance_short_term.equals(
                        "Dr",
                        ignoreCase = true
                    )
                ) {
                    binding.tvOpenBalCashDetailPurchase.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.debit_color
                        )
                    )
                } else
                    binding.tvOpenBalCashDetailPurchase.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.credit_color
                        )
                    )
            }


        }
    }

    private fun retrieveListforitem(purchasebillitemlist: List<SaleDetailModel.Item1427117511>?) {
        adapter.apply {
            if (purchasebillitemlist != null) {
                addsalebillrow_item(purchasebillitemlist)
            }
            notifyDataSetChanged()
        }

        var item_quantity_unit: StringBuilder = StringBuilder()
        var count: Int = 0
        for (item in purchasebillitemlist!!) {
            if (count == purchasebillitemlist.size - 1) {
                item_quantity_unit.append(item.item_quantity?.toString())
                    .append(" ").append(item.item_unit_name?.trim().toString())
//                tv_salesbilldetail_items_desc.visibility = View.GONE
            } else {
                item_quantity_unit.append(item.item_quantity?.toString())
                    .append(" ").append(item.item_unit_name?.trim().toString()).append(", ")

            }
            when (item.item_quantity!!.toInt() > 0) {
                true -> {
                    tv_purchasebilldetail_items_desc.visibility = View.VISIBLE
                    tv_purchasebilldetail_items_desc.text = item_quantity_unit.toString()
                }
                false -> {
                    tv_purchasebilldetail_items_desc.visibility = View.GONE
                }
            }

            count++
        }


    }



}
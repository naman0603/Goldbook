package com.goldbookapp.ui.activity.payment

import ReceiptDetailModel
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
import com.goldbookapp.databinding.PaymentDetailActivityBinding
import com.goldbookapp.model.*
import com.goldbookapp.permissions.PermissionHandler
import com.goldbookapp.permissions.Permissions
import com.goldbookapp.ui.activity.PdfDocumentAdapter
import com.goldbookapp.ui.activity.PrintJobMonitorService
import com.goldbookapp.ui.activity.viewmodel.PaymentDetailViewModel
import com.goldbookapp.ui.activity.viewmodel.ViewModelFactory
import com.goldbookapp.ui.adapter.IssueReceiveAdapter
import com.goldbookapp.ui.adapter.SaleDetailBill_ItemAdapter
import com.goldbookapp.utils.CommonUtils
import com.goldbookapp.utils.CommonUtils.Companion.clickWithDebounce
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
import kotlinx.android.synthetic.main.payment_detail_activity.*
import kotlinx.android.synthetic.main.receipt_detail_activity.*
import kotlinx.android.synthetic.main.toolbar_layout.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.*
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

class PaymentDetailActivity : AppCompatActivity() {
    var isFromWhatsapp: Boolean = false;
    var fileSavePath = ""
    private lateinit var notificationBuilder: NotificationCompat.Builder
    private lateinit var notificationManager: NotificationManager
    private var mgr: PrintManager? = null
    private val PERMISSION_REQUEST_CODE = 101
    lateinit var popupMenu: PopupMenu
    var filename: String = ""
    lateinit var prefs: SharedPreferences
    // default report type is blank for payment/receipt.
    var report_type: String = ""
    var isFromDownload: Boolean = false

    var isFromThread: Boolean = true
    private lateinit var viewModel: PaymentDetailViewModel
    lateinit var loginModel: LoginModel
    lateinit var paymentDetailModel: ReceiptDetailModel.Data
    private lateinit var adapter: SaleDetailBill_ItemAdapter

    lateinit var binding: PaymentDetailActivityBinding

    var paymentID: String? = ""

    private lateinit var issueReceiveadapter: IssueReceiveAdapter
    var paymentLineList = ArrayList<SalesLineModel.SaleLineModelDetails>()
    var imageURL: String? = ""
    var debit_short_term: String = ""
    var credit_short_term: String = ""

    var fineDefaultTermList: List<ItemDefaultTermModel.Data.Default_term>? = null
    var subTotalTerm: String = ""
    var subTotalTermValue: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.payment_detail_activity)
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
            when (loginModel.data!!.user_info!!.user_type.equals("user", true)) {
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

    private fun setupUIandListner() {

         prefs = PreferenceHelper.defaultPrefs(this)
        loginModel = Gson().fromJson(
            prefs[Constants.PREF_LOGIN_DETAIL_KEY, ""],
            LoginModel::class.java
        ) //getter

        imgLeft.setImageResource(R.drawable.ic_back)
        imgRight2.setImageResource(R.drawable.ic_edit)
        imgRight.setImageResource(R.drawable.ic_more)

        //tvTitle.setText("ABC Jewellers")
        popupMenu = PopupMenu(this, imgRight)
        popupMenu.menuInflater.inflate(R.menu.popup_menu_purchase_detail, popupMenu.menu)
       // popupMenu.menu.getItem(0).setVisible(false)
        imgLeft?.clickWithDebounce {
            onBackPressed()
        }
        mgr = getSystemService(Context.PRINT_SERVICE) as PrintManager
        imgRight.clickWithDebounce {


            popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
                when (item.itemId) {

                    R.id.sharesubmenuitem1 -> {
                        isFromWhatsapp = false
                        voucherTextAPI(loginModel?.data?.bearer_access_token, paymentID)
                    }
                    R.id.sharesubmenuitem2 -> {
                        isFromWhatsapp = true
                        voucherTextAPI(loginModel?.data?.bearer_access_token, paymentID)
                    }
                    R.id.action_download -> {
                        /*if (checkPermission()) {
                            calltovoucherPrint()

                        } else {
                            requestPermission()
                        }*/
                        checkandRequestPermission()
                        isFromDownload = true
                    }
                    R.id.action_print -> {
                       /* if (checkPermission()) {
                            calltovoucherPrint()

                        } else {
                            requestPermission()
                        }*/
                        checkandRequestPermission()
                        isFromDownload = false

                    }
                    R.id.action_delete ->

                        ensureDeleteDialog(paymentDetailModel.transactionData!!.invoice_number.toString())
                }
                true
            })
            popupMenu.show()
        }

        imgRight2?.clickWithDebounce {

            if (this::paymentDetailModel.isInitialized) {
                startActivity(
                    Intent(this, NewPaymentActivity::class.java)
                        .putExtra(Constants.PAYMENT_DETAIL_KEY, Gson().toJson(paymentDetailModel))
                )
                this.finish()
            }
        }

        clearPref()
        //recyclerviewsetup
        rv_paymentdetail_item.layoutManager = LinearLayoutManager(this)
        adapter = SaleDetailBill_ItemAdapter(arrayListOf())
        rv_paymentdetail_item.adapter = adapter

        debit_short_term = loginModel?.data!!.company_info!!.general_settings!!.debit_short_term!!
        credit_short_term = loginModel?.data!!.company_info!!.general_settings!!.credit_short_term!!
        binding.rvIssueReceiveList.layoutManager = LinearLayoutManager(this)
        issueReceiveadapter = IssueReceiveAdapter(arrayListOf(), "payment", true,debit_short_term,credit_short_term)
        binding.rvIssueReceiveList.adapter = issueReceiveadapter

        if(intent.extras?.containsKey(Constants.ModuleID)!!){
            paymentID = intent.getStringExtra(Constants.ModuleID)
        }

        if (intent.extras?.containsKey(Constants.PAYMENT_RECENT_TRANS_DETAIL_KEY)!!) {
            //paymentID = intent.getStringExtra(Constants.ModuleID)
            var payment_str: String? = intent.getStringExtra(Constants.PAYMENT_RECENT_TRANS_DETAIL_KEY)
            var paymentDetailModel: DashboardDetailsModel.Data.Recent_transactions = Gson().fromJson(
                payment_str,
                DashboardDetailsModel.Data.Recent_transactions::class.java
            )
            paymentID = paymentDetailModel.module_id
            tvTitle.text = paymentDetailModel.transaction_number
            tvNamePayDetail.text = paymentDetailModel.display_name
            tv_Paymentdetail_noofitems.text = paymentDetailModel.no_of_items.toString() + " item"
            tv_Paymentdetail_transactiondate.text = paymentDetailModel.transaction_date

        }

        if (intent.extras?.containsKey(Constants.PAYMENT_DETAIL_KEY)!!) {
            var payment_str: String? = intent.getStringExtra(Constants.PAYMENT_DETAIL_KEY)
            var paymentDetailModel: SearchListSalesModel.Data1465085328 = Gson().fromJson(
                payment_str,
                SearchListSalesModel.Data1465085328::class.java
            )

            paymentID = paymentDetailModel.transaction_id
            tvTitle.text = paymentDetailModel.invoice_number
            tvNamePayDetail.text = paymentDetailModel.contact_name
            tv_Paymentdetail_noofitems.text = paymentDetailModel.total_items.toString() + " item"
            tv_Paymentdetail_transactiondate.text = paymentDetailModel.transaction_date

        }
        imgLeft?.clickWithDebounce {
            onBackPressed()
        }

        iv_paymentdetail_attachmentone.clickWithDebounce {
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
    }

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
                                subTotalTerm = fineDefaultTermList!!.get(0).default_short_term!!
                                subTotalTermValue = fineDefaultTermList!!.get(0).default_term_value!!

                                if (paymentID?.isNotEmpty()!!) {
                                    paymentDetailAPI( loginModel?.data?.bearer_access_token, paymentID)
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
            if (data.permission!!.get(i).startsWith(getString(R.string.payment))) {
                // Restriction check for  Purchase
                when (data.permission!!.get(i).endsWith(getString(R.string.add_edit), true)) {
                    true -> {
                        imgRight2.visibility = View.VISIBLE
                    }
                    else->{

                    }
                }
            }
            if (data.permission!!.get(i).startsWith(getString(R.string.payment))) {
                // Restriction check for Purchase
                when (data.permission!!.get(i).endsWith(getString(R.string.print), true)) {
                    true -> {
                        popupMenu.menu.getItem(1).setVisible(true)
                    }
                    else->{

                    }
                }
            }
            if (data.permission!!.get(i).startsWith(getString(R.string.payment))) {
                // Restriction check for Purchase
                when (data.permission!!.get(i).endsWith(getString(R.string.download), true)) {
                    true -> {
                        popupMenu.menu.getItem(2).setVisible(true)
                    }
                    else->{

                    }
                }
            }
            if (data.permission!!.get(i).startsWith(getString(R.string.payment))) {
                // Restriction check for Purchase
                when (data.permission!!.get(i).endsWith(getString(R.string.del), true)) {
                    true -> {
                        popupMenu.menu.getItem(3).setVisible(true)
                    }
                    else->{

                    }
                }
            }

        }
    }


    private fun calltovoucherPrint() {
        progressBar_Paymentbilldetail.visibility = View.VISIBLE
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
                "payment",
                paymentID,
                report_type
            )

            call.enqueue(object : Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    //not required
                    CommonUtils.somethingWentWrong(this@PaymentDetailActivity)
                    progressBar_Paymentbilldetail.visibility = View.GONE
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
                                    this@PaymentDetailActivity,
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
                                val myDirectory = File(rootDirtory, "Payment")
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
                                    "Payment PDF",
                                    PdfDocumentAdapter(
                                        this@PaymentDetailActivity,
                                        filename,
                                        "Payment_Voucher"
                                    ),
                                    PrintAttributes.Builder()
                                        .setMediaSize(PrintAttributes.MediaSize.ISO_A4).build()
                                )

                            }
                        }

                        progressBar_Paymentbilldetail.visibility = View.GONE

                    }

                }

            })
        } else {
            CommonUtils.showDialog(this, getString(R.string.please_check_internet_msg))
        }

    }

    fun voucherTextAPI(
        token: String?,
        transaction_id: String?
    ) {

        if (NetworkUtils.isConnected()) {

            viewModel.voucherText(token, "payment", transaction_id).observe(this, Observer {
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
                    this@PaymentDetailActivity,
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

        }
    }

    private fun print(
        name: String, adapter: PrintDocumentAdapter,
        attrs: PrintAttributes
    ): PrintJob? {
        startService(Intent(this, PrintJobMonitorService::class.java))
        return mgr!!.print(name, adapter, attrs)
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
                        "Download/" + "GoldBook/" + "Payment"
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
                val myDirectory = File(rootDirtory, "Payment")
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

            }
        }
        return filename
    }

   /* private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            PERMISSION_REQUEST_CODE
        )
    }

    private fun checkPermission(): Boolean {
        val result = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        return result == PackageManager.PERMISSION_GRANTED
    }*/

    private fun ensureDeleteDialog(payment: String) {
        val builder = AlertDialog.Builder(this)
        val DeleteClick = { dialog: DialogInterface, which: Int ->
            //payment  delete api call
            deletePayment(loginModel?.data?.bearer_access_token, paymentDetailModel.transactionData!!.transaction_id)
        }
        val dialogdismiss = { dialog: DialogInterface, which: Int ->
            dialog.dismiss()
        }
        with(builder)
        {
            setTitle(context.getString(R.string.delPaymentDialog1Title))
            setMessage(context.getString(R.string.paymentDialog1Message))
            setPositiveButton(context.getString(R.string.Cancel), dialogdismiss)
            setNeutralButton(
                context.getString(R.string.Delete),
                DialogInterface.OnClickListener(function = DeleteClick)
            )
            show()
        }
    }

    private fun deletePayment(token: String?, transaction_id: String?) {
        if (NetworkUtils.isConnected()) {
            if (CommonUtils.isValidClickPressed()) {
                viewModel.deletePayment(token, transaction_id).observe(this, Observer {
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


    fun paymentDetailAPI(
        token: String?,
        transaction_id: String?
    ) {

        if (NetworkUtils.isConnected()) {

            viewModel.paymentDetail(token, transaction_id).observe(this, Observer {
                it?.let { resource ->
                    when (resource.status) {
                        Status.SUCCESS -> {
                            if (it.data?.status == true) {
                                llPaymentDetail_root.visibility = View.VISIBLE
                                paymentDetailModel = it.data.data!!

                                tvTitle.text =paymentDetailModel.transactionData!!.invoice_number
                                tvNamePayDetail.text = paymentDetailModel.transactionData!!.display_name
                                tv_Paymentdetail_transactiondate.text = paymentDetailModel.transactionData!!.transaction_date

                                if (paymentDetailModel.transactionData!!.image != null && paymentDetailModel.transactionData!!.image?.size!! > 0) {
                                    binding.tvAttachmentLabelPayment.visibility = View.VISIBLE
                                    binding.ivPaymentdetailAttachmentone.visibility = View.VISIBLE
                                    imageURL = paymentDetailModel.transactionData!!.image?.get(0)?.image
                                    Glide.with(this).load(imageURL).circleCrop()
                                        .placeholder(R.drawable.ic_user_placeholder)
                                        .into(binding.ivPaymentdetailAttachmentone)
                                }

                                retrieveListforitempayment(paymentDetailModel.transactionData?.item!!)
                                if (loginModel.data?.branch_info?.branch_type.equals("1", true)) {
                                    //GST branch
                                    binding.llNewpaymentMetalweights.visibility = View.GONE
                                    binding.llNewpaymentSubtotalRoot.visibility = View.GONE
                                } else {
                                    binding.llNewpaymentMetalweights.visibility = View.VISIBLE
                                    binding.llNewpaymentSubtotalRoot.visibility = View.VISIBLE
                                }


                                binding.tvNewpaymentItemquantity.setText("Qty: "+ paymentDetailModel.transactionData?.total_quantity)
                                binding.tvNewpaymentGrossWt.setText("G: "+ paymentDetailModel.transactionData?.total_gross_wt /*+ Constants.WEIGHT_GM_APPEND*/)
                                binding.tvNewpaymentLessWt.setText("L: "+ paymentDetailModel.transactionData?.total_less_wt /*+ Constants.WEIGHT_GM_APPEND*/)
                                binding.tvNewpaymentNetWt.setText("N: "+ paymentDetailModel.transactionData?.total_net_wt /*+ Constants.WEIGHT_GM_APPEND*/)
                                binding.tvNewpaymentFineWt.setText("F: "+ paymentDetailModel.transactionData?.total_fine_wt /*+ Constants.WEIGHT_GM_APPEND*/)
                                //binding.tvNewpaymentMiscCharges.setText(Constants.AMOUNT_RS_APPEND + paymentDetailModel.transactionData?.total_misc_charges)
                                binding.tvNewpaymentSilverItemquantity.setText("Qty: "+ paymentDetailModel.transactionData?.silver_total_quantity)
                                binding.tvNewpaymentSilverGrossWt.setText("G: "+ paymentDetailModel.transactionData?.silver_total_gross_wt /*+ Constants.WEIGHT_GM_APPEND*/)
                                binding.tvNewpaymentSilverLessWt.setText("L: "+ paymentDetailModel.transactionData?.silver_total_less_wt /*+ Constants.WEIGHT_GM_APPEND*/)
                                binding.tvNewpaymentSilverNetWt.setText("N: "+ paymentDetailModel.transactionData?.silver_total_net_wt /*+ Constants.WEIGHT_GM_APPEND*/)
                                binding.tvNewpaymentSilverFineWt.setText("F: "+ paymentDetailModel.transactionData?.silver_total_fine_wt /*+ Constants.WEIGHT_GM_APPEND*/)

                                binding.tvNewpaymentOtherItemquantity.setText("Qty: "+ paymentDetailModel.transactionData?.other_total_quantity)
                                binding.tvNewpaymentOtherGrossWt.setText("G: "+ paymentDetailModel.transactionData?.other_total_gross_wt /*+ Constants.WEIGHT_GM_APPEND*/)
                                binding.tvNewpaymentOtherLessWt.setText("L: 0.000"  /*+ Constants.WEIGHT_GM_APPEND*/)
                                binding.tvNewpaymentOtherNetWt.setText("N: "+ paymentDetailModel.transactionData?.other_total_net_wt /*+ Constants.WEIGHT_GM_APPEND*/)
                                binding.tvNewpaymentOtherFineWt.setText("F: 0.000" /*+ Constants.WEIGHT_GM_APPEND*/)


                                binding.tvNewpaymentSubtotalCol1.setText(paymentDetailModel.transactionData?.total_fine_wt)
                                binding.tvNewpaymentSubtotalCol2.setText(paymentDetailModel.transactionData?.final_total_amount)
                                binding.tvNewpaymentSubtotalCol1Silver.setText(paymentDetailModel.transactionData?.silver_total_fine_wt)
                               // binding.tvNewpaymentSubtotalCol2.setText(paymentDetailModel.transactionData?.silver_total_amount)

                                when (binding.tvNewpaymentSubtotalCol1.text) {
                                    "0.000" -> {
                                        binding.tvNewpaymentSubtotalCol1.setText(paymentDetailModel.transactionData?.total_fine_wt)
                                        binding.tvNewpaymentSubtotalCol1.setTextColor(
                                            ContextCompat.getColor(
                                                this,
                                                R.color.header_black_text
                                            )
                                        )
                                    }
                                    else -> {
                                        binding.tvNewpaymentSubtotalCol1.setText(paymentDetailModel.transactionData?.total_fine_wt + " " + subTotalTerm)
                                        if (subTotalTermValue.equals("debit")) {
                                            binding.tvNewpaymentSubtotalCol1.setTextColor(
                                                ContextCompat.getColor(
                                                    this,
                                                    R.color.debit_color
                                                )
                                            )
                                        }

                                    }
                                }

                                when (binding.tvNewpaymentSubtotalCol2.text) {
                                    "0.00" -> {
                                        binding.tvNewpaymentSubtotalCol2.setText(paymentDetailModel.transactionData?.final_total_amount)
                                        binding.tvNewpaymentSubtotalCol2.setTextColor(
                                            ContextCompat.getColor(
                                                this,
                                                R.color.header_black_text
                                            )
                                        )
                                    }
                                    else -> {
                                        binding.tvNewpaymentSubtotalCol2.setText(paymentDetailModel.transactionData?.final_total_amount + " " + subTotalTerm)
                                        if (subTotalTermValue.equals("debit")) {
                                            binding.tvNewpaymentSubtotalCol2.setTextColor(
                                                ContextCompat.getColor(
                                                    this,
                                                    R.color.debit_color
                                                )
                                            )

                                        }
                                    }
                                }

                                when (binding.tvNewpaymentSubtotalCol1Silver.text) {
                                    "0.000" -> {
                                        binding.tvNewpaymentSubtotalCol1Silver.setText(paymentDetailModel.transactionData?.silver_total_fine_wt)
                                        binding.tvNewpaymentSubtotalCol1Silver.setTextColor(
                                            ContextCompat.getColor(
                                                this,
                                                R.color.header_black_text
                                            )
                                        )
                                    }
                                    else -> {
                                        binding.tvNewpaymentSubtotalCol1Silver.setText(paymentDetailModel.transactionData?.silver_total_fine_wt + " " + subTotalTerm)
                                        if (subTotalTermValue.equals("debit")) {
                                            binding.tvNewpaymentSubtotalCol1Silver.setTextColor(
                                                ContextCompat.getColor(
                                                    this,
                                                    R.color.debit_color
                                                )
                                            )
                                        }
                                    }
                                }



                                binding.tvNewpaymentTotalDueGold.setText(paymentDetailModel.transactionData?.total_fine_wt_with_IRT)
                                binding.tvNewpaymentTotalDueSilver.setText(paymentDetailModel.transactionData?.total_silver_fine_wt_with_IRT)
                                binding.tvNewpaymentTotalDueCash.setText(Constants.AMOUNT_RS_APPEND + paymentDetailModel.transactionData?.grand_total)
                                binding.tvPaymentdetailNotes.text =
                                    paymentDetailModel.transactionData?.remarks



                                if(!paymentDetailModel.transactionData?.total_gross_wt.equals("0.000")){
                                    binding.llNewpaymentMetalgold.visibility = View.VISIBLE
                                   // binding.tvNewpaymentSubtotallabelSilver.visibility = View.VISIBLE

                                }else{
                                    binding.llNewpaymentMetalgold.visibility = View.GONE
                                    //binding.tvNewpaymentSubtotallabelSilver.visibility = View.GONE

                                }

                                if (!paymentDetailModel.transactionData?.silver_total_gross_wt.equals("0.000")) {
                                    binding.llNewpaymentMetalsilver.visibility = View.VISIBLE

                                } else {
                                    binding.llNewpaymentMetalsilver.visibility = View.GONE

                                }

                                if (!paymentDetailModel.transactionData?.other_total_gross_wt.equals("0.000")) {
                                    binding.llNewpaymentMetalother.visibility = View.VISIBLE


                                } else {
                                    binding.llNewpaymentMetalother.visibility = View.GONE

                                }

                                updateUIofTotalDue(paymentDetailModel)

                                addIRTDatainPref()
                                getIssueReceiveDataFromPref()
                                updateOpeningFineOpeningCash(paymentDetailModel)
                                updateClosingFineClosingCash(paymentDetailModel)


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

    private fun updateUIofTotalDue(paymentDetailModel: ReceiptDetailModel.Data) {
        if (!paymentDetailModel.transactionData?.total_fine_wt_with_IRT.equals("0.000") &&
            paymentDetailModel.transactionData?.total_silver_fine_wt_with_IRT.equals("0.000")
        ) {
            binding.llNewpaymentSilverTotaldue.visibility = View.GONE
            binding.tvNewpaymentTotaldueGoldLabel.visibility = View.VISIBLE
            binding.tvNewpaymentTotalDueGold.visibility = View.VISIBLE
        }

        if (!paymentDetailModel.transactionData?.total_fine_wt_with_IRT.equals("0.000") &&
            !paymentDetailModel.transactionData?.total_silver_fine_wt_with_IRT.equals("0.000")
        ) {
            binding.llNewpaymentSilverTotaldue.visibility = View.VISIBLE
            binding.tvNewpaymentTotaldueGoldLabel.visibility = View.VISIBLE
            binding.tvNewpaymentTotalDueGold.visibility = View.VISIBLE
        }



        if (paymentDetailModel.transactionData?.total_fine_wt_with_IRT.equals("0.000") &&
            !paymentDetailModel.transactionData?.total_silver_fine_wt_with_IRT.equals("0.000")
        ) {
            binding.llNewpaymentSilverTotaldue.visibility = View.GONE
            binding.tvNewpaymentTotaldueGoldLabel.setText("S: ")
            binding.tvNewpaymentTotalDueGold.setText(paymentDetailModel.transactionData?.total_silver_fine_wt_with_IRT)
            when (binding.tvNewpaymentTotalDueGold.text) {
                "0.000" -> {
                    binding.tvNewpaymentTotalDueGold.text =
                        paymentDetailModel.transactionData?.total_silver_fine_wt_with_IRT
                    binding.tvNewpaymentTotalDueGold.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.header_black_text
                        )
                    )
                }

                else -> {
                    binding.tvNewpaymentTotalDueGold.text =
                        paymentDetailModel.transactionData?.total_silver_fine_wt_with_IRT + " " +
                                paymentDetailModel.transactionData?.total_silver_fine_wt_with_IRT_term
                    if (paymentDetailModel.transactionData?.total_silver_fine_wt_with_IRT_short_term.equals(
                            "Dr",
                            ignoreCase = true
                        )
                    ) {
                        binding.tvNewpaymentTotalDueGold.setTextColor(
                            ContextCompat.getColor(
                                this,
                                R.color.debit_color
                            )
                        )
                    } else
                        binding.tvNewpaymentTotalDueGold.setTextColor(
                            ContextCompat.getColor(
                                this,
                                R.color.credit_color
                            )
                        )
                }
            }

            when (binding.tvNewpaymentTotalDueCash.text) {
                "0.00" -> {
                    binding.tvNewpaymentTotalDueCash.text =
                        paymentDetailModel.transactionData?.grand_total
                    binding.tvNewpaymentTotalDueCash.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.header_black_text
                        )
                    )
                }
                else -> {
                    binding.tvNewpaymentTotalDueCash.text =
                        paymentDetailModel.transactionData?.grand_total + " " +
                                paymentDetailModel.transactionData?.grand_total_term
                    if (paymentDetailModel.transactionData?.grand_total_short_term.equals(
                            "Dr",
                            ignoreCase = true
                        )
                    ) {
                        binding.tvNewpaymentTotalDueCash.setTextColor(
                            ContextCompat.getColor(
                                this,
                                R.color.debit_color
                            )
                        )
                    } else
                        binding.tvNewpaymentTotalDueCash.setTextColor(
                            ContextCompat.getColor(
                                this,
                                R.color.credit_color
                            )
                        )
                }
            }

        } else {
            binding.tvNewpaymentTotaldueGoldLabel.setText("G: ")
            updateTotalDuewithDrCr(paymentDetailModel)
        }

        /*if(!paymentDetailModel.transactionData?.total_silver_fine_wt_with_IRT.equals("0.000")){
            binding.llNewinvoiceSilverTotaldue.visibility = View.VISIBLE
        }else{
            binding.llNewinvoiceSilverTotaldue.visibility = View.GONE
        }*/


        if (paymentDetailModel.transactionData?.total_fine_wt_with_IRT.equals("0.000") &&
            paymentDetailModel.transactionData?.total_silver_fine_wt_with_IRT.equals("0.000")
        ) {
            binding.llNewpaymentSilverTotaldue.visibility = View.GONE
            binding.tvNewpaymentTotaldueGoldLabel.visibility = View.GONE
            binding.tvNewpaymentTotalDueGold.visibility = View.GONE
        } else {
            //  binding.llNewinvoiceSilverTotaldue.visibility = View.VISIBLE
            //  tv_newinvoice_totaldue_gold_label.visibility = View.VISIBLE
            // tv_newinvoice_totalDue_gold.visibility = View.VISIBLE
        }
    }

    private fun updateTotalDuewithDrCr(paymentDetailModel: ReceiptDetailModel.Data) {
        when (binding.tvNewpaymentTotalDueGold.text) {
            "0.000" -> {
                binding.tvNewpaymentTotalDueGold.text =
                    paymentDetailModel.transactionData?.total_fine_wt_with_IRT
                binding.tvNewpaymentTotalDueGold.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.header_black_text
                    )
                )
            }

            else -> {
                binding.tvNewpaymentTotalDueGold.text =
                    paymentDetailModel.transactionData?.total_fine_wt_with_IRT + " " +
                            paymentDetailModel.transactionData?.total_fine_wt_with_IRT_term
                if (paymentDetailModel.transactionData?.total_fine_wt_with_IRT_short_term.equals(
                        "Dr",
                        ignoreCase = true
                    )
                ) {
                    binding.tvNewpaymentTotalDueGold.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.debit_color
                        )
                    )
                } else
                    binding.tvNewpaymentTotalDueGold.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.credit_color
                        )
                    )
            }
        }

        when (binding.tvNewpaymentTotalDueSilver.text) {
            "0.000" -> {
                binding.tvNewpaymentTotalDueSilver.text =
                    paymentDetailModel.transactionData?.total_silver_fine_wt_with_IRT
                binding.tvNewpaymentTotalDueSilver.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.header_black_text
                    )
                )
            }

            else -> {
                binding.tvNewpaymentTotalDueSilver.text =
                    paymentDetailModel.transactionData?.total_silver_fine_wt_with_IRT + " " +
                            paymentDetailModel.transactionData?.total_silver_fine_wt_with_IRT_term
                if (paymentDetailModel.transactionData?.total_silver_fine_wt_with_IRT_short_term.equals(
                        "Dr",
                        ignoreCase = true
                    )
                ) {
                    binding.tvNewpaymentTotalDueSilver.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.debit_color
                        )
                    )
                } else
                    binding.tvNewpaymentTotalDueSilver.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.credit_color
                        )
                    )
            }
        }

        when (binding.tvNewpaymentTotalDueCash.text) {
            "0.00" -> {
                binding.tvNewpaymentTotalDueCash.text =
                    paymentDetailModel.transactionData?.grand_total
                binding.tvNewpaymentTotalDueCash.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.header_black_text
                    )
                )
            }
            else -> {
                binding.tvNewpaymentTotalDueCash.text =
                    paymentDetailModel.transactionData?.grand_total + " " +
                            paymentDetailModel.transactionData?.grand_total_term
                if (paymentDetailModel.transactionData?.grand_total_short_term.equals(
                        "Dr",
                        ignoreCase = true
                    )
                ) {
                    binding.tvNewpaymentTotalDueCash.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.debit_color
                        )
                    )
                } else
                    binding.tvNewpaymentTotalDueCash.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.credit_color
                        )
                    )
            }
        }
    }

    private fun addIRTDatainPref() {
        paymentLineList.clear()
        for (i in 0 until paymentDetailModel.IRTData!!.size) {

            if (!paymentDetailModel.IRTData!!.get(i).transaction_type.equals("")) {

                val saleIRTModel = SalesLineModel.SaleLineModelDetails(
                    paymentDetailModel.IRTData!!.get(i).IRTDetails!!.cash_amount,
                    paymentDetailModel.IRTData!!.get(i).IRTDetails!!.cash_ledger,
                    paymentDetailModel.IRTData!!.get(i).IRTDetails!!.cash_ledger_name,
                    paymentDetailModel.IRTData!!.get(i).IRTDetails!!.cash_description,
                    paymentDetailModel.IRTData!!.get(i).IRTDetails!!.bank_amount,
                    paymentDetailModel.IRTData!!.get(i).IRTDetails!!.bank_ledger,
                    paymentDetailModel.IRTData!!.get(i).IRTDetails!!.bank_ledger_name,
                    paymentDetailModel.IRTData!!.get(i).IRTDetails!!.bank_mode,
                    paymentDetailModel.IRTData!!.get(i).IRTDetails!!.cheque_number,
                    paymentDetailModel.IRTData!!.get(i).IRTDetails!!.cheque_date,
                    paymentDetailModel.IRTData!!.get(i).IRTDetails!!.favouring_name,
                    paymentDetailModel.IRTData!!.get(i).IRTDetails!!.deuct_charges,
                    paymentDetailModel.IRTData!!.get(i).IRTDetails!!.deuct_charges_percentage,
                    paymentDetailModel.IRTData!!.get(i).IRTDetails!!.bank_final_amt,
                    paymentDetailModel.IRTData!!.get(i).IRTDetails!!.recipient_bank,
                    paymentDetailModel.IRTData!!.get(i).IRTDetails!!.account_no,
                    paymentDetailModel.IRTData!!.get(i).IRTDetails!!.ifs_code,
                    paymentDetailModel.IRTData!!.get(i).IRTDetails!!.utr_number,
                    paymentDetailModel.IRTData!!.get(i).IRTDetails!!.bank_description,
                    paymentDetailModel.IRTData!!.get(i).IRTDetails!!.rcm_gold_rate,
                    paymentDetailModel.IRTData!!.get(i).IRTDetails!!.rate_cut_amount,
                    paymentDetailModel.IRTData!!.get(i).IRTDetails!!.rate_cut_fine_term,
                    paymentDetailModel.IRTData!!.get(i).IRTDetails!!.metal_type_id_rate_cut,
                    paymentDetailModel.IRTData!!.get(i).IRTDetails!!.rate_cut_fine,
                    paymentDetailModel.IRTData!!.get(i).IRTDetails!!.item_id,
                    paymentDetailModel.IRTData!!.get(i).IRTDetails!!.item_name,
                    paymentDetailModel.IRTData!!.get(i).IRTDetails!!.metal_type_id_metal,
                    paymentDetailModel.IRTData!!.get(i).IRTDetails!!.maintain_stock_in_name_metal,
                    paymentDetailModel.IRTData!!.get(i).IRTDetails!!.gross_wt,
                    paymentDetailModel.IRTData!!.get(i).IRTDetails!!.less_wt,
                    paymentDetailModel.IRTData!!.get(i).IRTDetails!!.net_wt,
                    paymentDetailModel.IRTData!!.get(i).IRTDetails!!.touch,
                    paymentDetailModel.IRTData!!.get(i).IRTDetails!!.wast,
                    paymentDetailModel.IRTData!!.get(i).IRTDetails!!.fine_wt,
                    paymentDetailModel.IRTData!!.get(i).IRTDetails!!.adjustment_fine,
                    paymentDetailModel.IRTData!!.get(i).IRTDetails!!.metal_type_id_adjustments,
                    paymentDetailModel.IRTData!!.get(i).IRTDetails!!.adjustment_amount,
                    paymentDetailModel.IRTData!!.get(i).IRTDetails!!.adjustment_ledger,
                    paymentDetailModel.IRTData!!.get(i).IRTDetails!!.adjustment_ledger_name,
                    paymentDetailModel.IRTData!!.get(i).IRTDetails!!.adjustment_description,
                    paymentDetailModel.IRTData!!.get(i).IRTDetails!!.type,
                    paymentDetailModel.IRTData!!.get(i).IRTDetails!!.transaction_type,
                    paymentDetailModel.IRTData!!.get(i).IRTDetails!!.transaction_title

                )

                paymentLineList.add(saleIRTModel)
            }
        }

        prefs[Constants.PREF_SALES_LINE_INFO_KEY] = Gson().toJson(paymentLineList)
    }

    private fun getIssueReceiveDataFromPref() {
        if (prefs.contains(Constants.PREF_SALES_LINE_INFO_KEY)) {
            val collectionType =
                object :
                    TypeToken<ArrayList<SalesLineModel.SaleLineModelDetails>>() {}.type
            paymentLineList =
                Gson().fromJson(
                    prefs[Constants.PREF_SALES_LINE_INFO_KEY, ""],
                    collectionType
                )
            setupIssueReceiveAdapter()
        }
    }

    private fun setupIssueReceiveAdapter() {
        when(paymentLineList.size>0){
            true->{
                binding.rvIssueReceiveList.visibility = View.VISIBLE
                issueReceiveadapter.apply {
                    addissueReceiveList(paymentLineList)
                    notifyDataSetChanged()
                }
            }
            else->{

            }
        }
    }

    private fun retrieveListforitempayment(items: List<SaleDetailModel.Item1427117511>) {
        adapter.apply {
            addsalebillrow_item(items)
            notifyDataSetChanged()
        }

        var item_quantity_unit: StringBuilder = StringBuilder()
        var count: Int = 0
        for (item in items!!) {
            if (count == items.size - 1) {
                item_quantity_unit.append(item.item_quantity?.toString())
                    .append(" ").append(item.item_unit_name?.trim().toString())
                //                tv_salesbilldetail_items_desc.visibility = View.GONE
            } else {
                item_quantity_unit.append(item.item_quantity?.toString())
                    .append(" ").append(item.item_unit_name?.trim().toString()).append(", ")

            }
            when (item.item_quantity!!.toInt() > 0) {
                true -> {
                    tv_Paymentdetail_items_desc.visibility = View.VISIBLE
                    tv_Paymentdetail_items_desc.text = item_quantity_unit.toString()
                }
                false -> {
                    tv_Paymentdetail_items_desc.visibility = View.GONE
                }
            }

            count++
        }
    }

    private fun updateOpeningFineOpeningCash(paymentDetailModel: ReceiptDetailModel.Data) {
        if (paymentDetailModel.transactionData?.opening_fine_balance!!.startsWith("-")) {
            val open_fine_bal: String =
                paymentDetailModel.transactionData?.opening_fine_balance.toString().trim().substring(1)
            binding.tvOpenBalFineDetailPayment.text = open_fine_bal
        } else {
            binding.tvOpenBalFineDetailPayment.text =
                paymentDetailModel.transactionData?.opening_fine_balance
        }


        when (binding.tvOpenBalFineDetailPayment.text) {
            "0.000" -> {
                binding.tvOpenBalFineDetailPayment.text =
                    paymentDetailModel.transactionData?.opening_fine_balance
                binding.tvOpenBalFineDetailPayment.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.header_black_text
                    )
                )
            }

            else -> {
                binding.tvOpenBalFineDetailPayment.text =
                    paymentDetailModel.transactionData?.opening_fine_balance + " " + paymentDetailModel.transactionData?.opening_fine_balance_term
                if (paymentDetailModel.transactionData?.opening_fine_balance_short_term.equals(
                        "Dr",
                        ignoreCase = true
                    )
                ) {
                    binding.tvOpenBalFineDetailPayment.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.debit_color
                        )
                    )
                } else
                    binding.tvOpenBalFineDetailPayment.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.credit_color
                        )
                    )
            }
        }


        if (paymentDetailModel.transactionData?.opening_silver_fine_balance!!.startsWith("-")) {
            val open_fine_bal: String =
                paymentDetailModel.transactionData?.opening_silver_fine_balance.toString().trim().substring(1)
            binding.tvOpenBalFineSilverDetailPayment.text = open_fine_bal
        } else {
            binding.tvOpenBalFineSilverDetailPayment.text =
                paymentDetailModel.transactionData?.opening_silver_fine_balance
        }


        when (binding.tvOpenBalFineSilverDetailPayment.text) {
            "0.000" -> {
                binding.tvOpenBalFineSilverDetailPayment.text =
                    paymentDetailModel.transactionData?.opening_silver_fine_balance
                binding.tvOpenBalFineSilverDetailPayment.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.header_black_text
                    )
                )
            }

            else -> {
                binding.tvOpenBalFineSilverDetailPayment.text =
                    paymentDetailModel.transactionData?.opening_silver_fine_balance + " " + paymentDetailModel.transactionData?.opening_silver_fine_balance_term
                if (paymentDetailModel.transactionData?.opening_silver_fine_balance_short_term.equals(
                        "Dr",
                        ignoreCase = true
                    )
                ) {
                    binding.tvOpenBalFineSilverDetailPayment.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.debit_color
                        )
                    )
                } else
                    binding.tvOpenBalFineSilverDetailPayment.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.credit_color
                        )
                    )
            }
        }


        if (paymentDetailModel.transactionData?.opening_cash_balance!!.startsWith("-")) {
            val open_cash_bal: String =
                paymentDetailModel.transactionData?.opening_cash_balance.toString().trim().substring(1)
            binding.tvOpenBalCashDetailPayment.text = open_cash_bal
        } else {
            binding.tvOpenBalCashDetailPayment.text =
                paymentDetailModel.transactionData?.opening_cash_balance
        }

        when (binding.tvOpenBalCashDetailPayment.text) {
            "0.00" -> {
                binding.tvOpenBalCashDetailPayment.text =
                    paymentDetailModel.transactionData?.opening_cash_balance
                binding.tvOpenBalCashDetailPayment.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.header_black_text
                    )
                )
            }
            else -> {
                binding.tvOpenBalCashDetailPayment.text =
                    paymentDetailModel.transactionData?.opening_cash_balance + " " + paymentDetailModel.transactionData?.opening_cash_balance_term
                if (paymentDetailModel.transactionData?.opening_cash_balance_short_term.equals(
                        "Dr",
                        ignoreCase = true
                    )
                ) {
                    binding.tvOpenBalCashDetailPayment.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.debit_color
                        )
                    )
                } else
                    binding.tvOpenBalCashDetailPayment.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.credit_color
                        )
                    )
            }


        }

    }

    private fun updateClosingFineClosingCash(paymentDetailModel: ReceiptDetailModel.Data) {

        if (paymentDetailModel.transactionData?.closing_fine_balance!!.startsWith("-")) {
            val open_fine_bal: String =
                paymentDetailModel.transactionData?.closing_fine_balance.toString().trim().substring(1)
            binding.tvCloBalFineWtNewpayment.text = open_fine_bal
        } else {
            binding.tvCloBalFineWtNewpayment.text =
                paymentDetailModel.transactionData?.closing_fine_balance
        }

        when (binding.tvCloBalFineWtNewpayment.text) {
            "0.000" -> {
                binding.tvCloBalFineWtNewpayment.text =
                    paymentDetailModel.transactionData?.closing_fine_balance
                binding.tvCloBalFineWtNewpayment.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.header_black_text
                    )
                )
            }

            else -> {
                binding.tvCloBalFineWtNewpayment.text =
                    paymentDetailModel.transactionData?.closing_fine_balance + " " + paymentDetailModel.transactionData?.closing_fine_balance_term
                if (paymentDetailModel.transactionData?.closing_fine_balance_short_term.equals(
                        "Dr",
                        ignoreCase = true
                    )
                ) {
                    binding.tvCloBalFineWtNewpayment.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.debit_color
                        )
                    )
                } else
                    binding.tvCloBalFineWtNewpayment.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.credit_color
                        )
                    )
            }
        }


        if (paymentDetailModel.transactionData?.closing_silver_fine_balance!!.startsWith("-")) {
            val open_fine_bal: String =
                paymentDetailModel.transactionData?.closing_silver_fine_balance.toString().trim().substring(1)
            binding.tvCloBalSilverNewpayment.text = open_fine_bal
        } else {
            binding.tvCloBalSilverNewpayment.text =
                paymentDetailModel.transactionData?.closing_silver_fine_balance
        }

        when (binding.tvCloBalSilverNewpayment.text) {
            "0.000" -> {
                binding.tvCloBalSilverNewpayment.text =
                    paymentDetailModel.transactionData?.closing_silver_fine_balance
                binding.tvCloBalSilverNewpayment.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.header_black_text
                    )
                )
            }

            else -> {
                binding.tvCloBalSilverNewpayment.text =
                    paymentDetailModel.transactionData?.closing_silver_fine_balance + " " + paymentDetailModel.transactionData?.closing_silver_fine_balance_term
                if (paymentDetailModel.transactionData?.closing_silver_fine_balance_short_term.equals(
                        "Dr",
                        ignoreCase = true
                    )
                ) {
                    binding.tvCloBalSilverNewpayment.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.debit_color
                        )
                    )
                } else
                    binding.tvCloBalSilverNewpayment.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.credit_color
                        )
                    )
            }
        }


        if (paymentDetailModel.transactionData?.closing_cash_balance!!.startsWith("-")) {
            val open_cash_bal: String =
                paymentDetailModel.transactionData?.closing_cash_balance.toString().trim().substring(1)
            binding.tvCloBalCashNewpayment.text = open_cash_bal
        } else {
            binding.tvCloBalCashNewpayment.text =
                paymentDetailModel.transactionData?.closing_cash_balance
        }

        when (binding.tvCloBalCashNewpayment.text) {
            "0.00" -> {
                binding.tvCloBalCashNewpayment.text =
                    paymentDetailModel.transactionData?.closing_cash_balance
                binding.tvCloBalCashNewpayment.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.header_black_text
                    )
                )
            }
            else -> {
                binding.tvCloBalCashNewpayment.text =
                    paymentDetailModel.transactionData?.closing_cash_balance + " " + paymentDetailModel.transactionData?.closing_cash_balance_term
                if (paymentDetailModel.transactionData?.closing_cash_balance_short_term.equals(
                        "Dr",
                        ignoreCase = true
                    )
                ) {
                    binding.tvCloBalCashNewpayment.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.debit_color
                        )
                    )
                } else
                    binding.tvCloBalCashNewpayment.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.credit_color
                        )
                    )
            }


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

    private fun setupViewModel() {
        viewModel =
            ViewModelProvider(this, ViewModelFactory(ApiHelper(RetrofitBuilder.apiService))).get(
                PaymentDetailViewModel::class.java
            )
        binding.setLifecycleOwner(this)

    }

   /* override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> if (grantResults.size > 0 && grantResults[0] === PackageManager.PERMISSION_GRANTED) {
                //downloadReport(reportsType)
                calltovoucherPrint()
            } else {
                Toast.makeText(
                    applicationContext,
                    "Permission required to save voucher",
                    Toast.LENGTH_SHORT
                )
                    .show()
                requestPermission()
            }
        }
    }*/
}
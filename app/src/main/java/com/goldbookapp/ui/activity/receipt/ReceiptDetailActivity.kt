package com.goldbookapp.ui.activity.receipt

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
import com.goldbookapp.databinding.ReceiptDetailActivityBinding
import com.goldbookapp.model.*
import com.goldbookapp.permissions.PermissionHandler
import com.goldbookapp.permissions.Permissions
import com.goldbookapp.ui.activity.PdfDocumentAdapter
import com.goldbookapp.ui.activity.PrintJobMonitorService
import com.goldbookapp.ui.activity.viewmodel.ReceiptDetailViewModel
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
import kotlinx.android.synthetic.main.receipt_detail_activity.*
import kotlinx.android.synthetic.main.receipt_detail_activity.view.*
import kotlinx.android.synthetic.main.toolbar_layout.*
import kotlinx.android.synthetic.main.toolbar_layout.view.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.*
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

class ReceiptDetailActivity : AppCompatActivity() {
    var isFromWhatsapp: Boolean = false
    var fileSavePath = ""
    private lateinit var notificationBuilder: NotificationCompat.Builder
    private lateinit var notificationManager: NotificationManager
    private var mgr: PrintManager? = null
    private val PERMISSION_REQUEST_CODE = 101
    lateinit var popupMenu: PopupMenu
    var filename: String = ""

    // default report type is blank for payment/receipt.
    var report_type: String = ""
    var isFromDownload: Boolean = false

    var isFromThread: Boolean = true
    lateinit var prefs: SharedPreferences
    private lateinit var viewModel: ReceiptDetailViewModel
    lateinit var loginModel: LoginModel

    //lateinit var receiptDetailModel: ReceiptDetailModel.ReceiptDetail
    lateinit var receiptDetailModel: ReceiptDetailModel.Data
    private lateinit var adapter: SaleDetailBill_ItemAdapter

    lateinit var binding: ReceiptDetailActivityBinding

    var receiptID: String? = ""

    var imageURL: String? = ""
    private lateinit var issueReceiveadapter: IssueReceiveAdapter
    var receiptLineList = ArrayList<SalesLineModel.SaleLineModelDetails>()
    var debit_short_term: String = ""
    var credit_short_term: String = ""

    var fineDefaultTermList: List<ItemDefaultTermModel.Data.Default_term>? = null
    var subTotalTerm: String = ""
    var subTotalTermValue: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.receipt_detail_activity)
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
                    //defaultDisableAllButtonnUI()
                    userWiseRestriction(loginModel.data?.bearer_access_token)
                }
                // user_type -> admin or super_admin or any other
                false -> {
                    //defaultEnableAllButtonnUI()

                }
            }

        }

        if (!ConnectivityStateHolder.isConnected) {
            // Network is not available
            CommonUtils.showDialog(this, getString(R.string.please_check_internet_msg))

        }
    }

    private fun defaultDisableAllButtonnUI() {
        binding.root.imgRight.visibility = View.VISIBLE
        binding.root.imgRight2.visibility = View.GONE
        popupMenu.menu.getItem(3).setVisible(false)
        popupMenu.menu.getItem(2).setVisible(false)
        popupMenu.menu.getItem(1).setVisible(false)
    }

    private fun defaultEnableAllButtonnUI() {
        binding.root.imgRight.visibility = View.VISIBLE
        binding.root.imgRight2.visibility = View.VISIBLE
        popupMenu.menu.getItem(3).setVisible(true)
        popupMenu.menu.getItem(2).setVisible(true)
        popupMenu.menu.getItem(1).setVisible(true)
    }

    private fun setupViewModel() {
        viewModel =
            ViewModelProvider(this, ViewModelFactory(ApiHelper(RetrofitBuilder.apiService))).get(
                ReceiptDetailViewModel::class.java
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
        popupMenu = PopupMenu(this, imgRight)
        //popupMenu.menuInflater.inflate(R.menu.popup_menu_receipt_detail, popupMenu.menu)
        popupMenu.menuInflater.inflate(R.menu.popup_menu_purchase_detail, popupMenu.menu)
       // popupMenu.menu.getItem(0).setVisible(false)

        // issue receive adapter
        debit_short_term = loginModel?.data!!.company_info!!.general_settings!!.debit_short_term!!
        credit_short_term = loginModel?.data!!.company_info!!.general_settings!!.credit_short_term!!
        binding.rvIssueReceiveListDetail.layoutManager = LinearLayoutManager(this)
        issueReceiveadapter = IssueReceiveAdapter(arrayListOf(), "receipt", true, debit_short_term, credit_short_term)
        binding.rvIssueReceiveListDetail.adapter = issueReceiveadapter
        binding.rvIssueReceiveListDetail.isClickable = false
        binding.rvIssueReceiveListDetail.isEnabled = false

        clearPref()
        imgLeft?.clickWithDebounce {
            onBackPressed()
        }
        mgr = getSystemService(Context.PRINT_SERVICE) as PrintManager

        imgRight.clickWithDebounce {

            popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
                when (item.itemId) {

                    R.id.sharesubmenuitem1 -> {
                        isFromWhatsapp = false
                        voucherTextAPI(loginModel?.data?.bearer_access_token, receiptID)
                    }
                    R.id.sharesubmenuitem2 -> {
                        isFromWhatsapp = true
                        voucherTextAPI(loginModel?.data?.bearer_access_token, receiptID)
                    }
                    R.id.action_download -> {
                        /* if (checkPermission()) {
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
                        /*Toast.makeText(this, "You Clicked : " + item.title, Toast.LENGTH_SHORT).show()*/
                        ensureDeleteDialog(receiptDetailModel.transactionData!!.invoice_number.toString())
                }
                true
            })
            popupMenu.show()
        }

        imgRight2?.clickWithDebounce {
            if (this::receiptDetailModel.isInitialized) {
                startActivity(
                    Intent(this, NewReceiptActivity::class.java)
                        .putExtra(Constants.RECEIPT_DETAIL_KEY, Gson().toJson(receiptDetailModel))
                )
                finish()
            }
        }

        //recyclerviewsetup
        binding.root.rv_Receiptdetail_item.layoutManager = LinearLayoutManager(this)
        adapter = SaleDetailBill_ItemAdapter(arrayListOf())
        binding.root.rv_Receiptdetail_item.adapter = adapter

        if(intent.extras?.containsKey(Constants.ModuleID)!!){
            receiptID = intent.getStringExtra(Constants.ModuleID)
        }

        if (intent.extras?.containsKey(Constants.RECEIPT_RECENT_TRANS_DETAIL_KEY)!!) {
         //   receiptID = intent.getStringExtra(Constants.ModuleID)
            var receipt_str: String? = intent.getStringExtra(Constants.RECEIPT_RECENT_TRANS_DETAIL_KEY)
            var receiptDetailModel: DashboardDetailsModel.Data.Recent_transactions = Gson().fromJson(
                receipt_str,
                DashboardDetailsModel.Data.Recent_transactions::class.java
            )

            receiptID = receiptDetailModel.module_id
            tvTitle.text = receiptDetailModel.transaction_number
            tv_Receiptdetail_custname.text = receiptDetailModel.display_name
            tv_Receiptdetail_noofitems.text = receiptDetailModel.no_of_items.toString() + " item"
            tv_Receiptdetail_transactiondate.text = receiptDetailModel.transaction_date

        }

        if (intent.extras?.containsKey(Constants.RECEIPT_DETAIL_KEY)!!) {
            var receipt_str: String? = intent.getStringExtra(Constants.RECEIPT_DETAIL_KEY)
            var receiptDetailModel: SearchListReceipt.DataReceipt = Gson().fromJson(
                receipt_str,
                SearchListReceipt.DataReceipt::class.java
            )

            receiptID = receiptDetailModel.transaction_id
            tvTitle.text = receiptDetailModel.invoice_number
            tv_Receiptdetail_custname.text = receiptDetailModel.contact_name
            tv_Receiptdetail_noofitems.text = receiptDetailModel.total_items.toString() + " item"
            tv_Receiptdetail_transactiondate.text = receiptDetailModel.transaction_date


        }


        imgLeft?.clickWithDebounce {
            onBackPressed()
        }


        iv_Receiptdetail_attachmentone.clickWithDebounce {

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

                                if (receiptID?.isNotEmpty()!!) {
                                    receciptDetailAPI(
                                        loginModel?.data?.bearer_access_token, receiptID
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
            if (data.permission!!.get(i).startsWith(getString(R.string.receipt))) {
                // Restriction check for  Purchase
                when (data.permission!!.get(i).endsWith(getString(R.string.add_edit), true)) {
                    true -> {
                        binding.root.imgRight2.visibility = View.VISIBLE
                    }else->{

                }
                }
            }
            if (data.permission!!.get(i).startsWith(getString(R.string.receipt))) {
                // Restriction check for Purchase
                when (data.permission!!.get(i).endsWith(getString(R.string.print), true)) {
                    true -> {
                        popupMenu.menu.getItem(1).setVisible(true)
                    }else->{

                }
                }
            }
            if (data.permission!!.get(i).startsWith(getString(R.string.receipt))) {
                // Restriction check for Purchase
                when (data.permission!!.get(i).endsWith(getString(R.string.download), true)) {
                    true -> {
                        popupMenu.menu.getItem(2).setVisible(true)
                    }else->{

                }
                }
            }
            if (data.permission!!.get(i).startsWith(getString(R.string.receipt))) {
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
        progressBar_Receiptdetail.visibility = View.VISIBLE
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
                "receipt",
                receiptID,
                report_type
            )

            call.enqueue(object : Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    //not required
                    CommonUtils.somethingWentWrong(this@ReceiptDetailActivity)
                    progressBar_Receiptdetail.visibility = View.GONE

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
                                    this@ReceiptDetailActivity,
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
                                val myDirectory = File(rootDirtory, "Receipt")
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
                                    "Receipt PDF",
                                    PdfDocumentAdapter(
                                        this@ReceiptDetailActivity,
                                        filename,
                                        "Receipt_Voucher"
                                    ),
                                    PrintAttributes.Builder()
                                        .setMediaSize(PrintAttributes.MediaSize.ISO_A4).build()
                                )

                            }
                        }

                        progressBar_Receiptdetail.visibility = View.GONE

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

            viewModel.voucherText(token, "receipt", transaction_id).observe(this, Observer {
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
                    this@ReceiptDetailActivity,
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
                        "Download/" + "GoldBook/" + "Receipt"
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
                val myDirectory = File(rootDirtory, "Receipt")
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


    private fun ensureDeleteDialog(receipt: String) {
        val builder = AlertDialog.Builder(this)
        val DeleteClick = { dialog: DialogInterface, which: Int ->
            //receipt  delete api call
            deleteReceipt(
                loginModel?.data?.bearer_access_token,
                receiptDetailModel.transactionData?.transaction_id
            )
        }
        val dialogdismiss = { dialog: DialogInterface, which: Int ->
            dialog.dismiss()
        }
        with(builder)
        {
            setTitle(context.getString(R.string.delReceiptDialog1Title))
            setMessage(context.getString(R.string.receiptDialog1Message))
            setPositiveButton(context.getString(R.string.Cancel), dialogdismiss)
            setNeutralButton(
                context.getString(R.string.Delete),
                DialogInterface.OnClickListener(function = DeleteClick)
            )
            show()
        }
    }

    private fun deleteReceipt(token: String?, transaction_id: String?) {
        if (NetworkUtils.isConnected()) {
            if (isValidClickPressed()) {
                viewModel.deleteReceipt(token, transaction_id).observe(this, Observer {
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

    fun receciptDetailAPI(
        token: String?,
        transaction_id: String?
    ) {

        if (NetworkUtils.isConnected()) {

            viewModel.receiptDetail(token, transaction_id).observe(this, Observer {
                it?.let { resource ->
                    when (resource.status) {
                        Status.SUCCESS -> {
                            if (it.data?.status == true) {
                                llReceiptDetail_root.visibility = View.VISIBLE
                                receiptDetailModel = it.data.data!!


                               // popupMenu = PopupMenu(this, imgRight)
                                tvTitle.text= receiptDetailModel.transactionData!!.invoice_number
                                tv_Receiptdetail_transactiondate.text = receiptDetailModel.transactionData!!.transaction_date
                                tv_Receiptdetail_custname.text = receiptDetailModel.transactionData!!.display_name
                                if (receiptDetailModel.transactionData!!.image != null && receiptDetailModel.transactionData!!.image?.size!! > 0) {
                                    binding.tvAttachmentLabelReceipt.visibility = View.VISIBLE
                                    binding.ivReceiptdetailAttachmentone.visibility = View.VISIBLE
                                    imageURL =
                                        receiptDetailModel.transactionData!!.image?.get(0)?.image
                                    Glide.with(this).load(imageURL).circleCrop()
                                        .placeholder(R.drawable.ic_user_placeholder)
                                        .into(binding.ivReceiptdetailAttachmentone)
                                }

                                retrieveListforitemreceipt(receiptDetailModel.transactionData?.item!!)

                                if (loginModel.data?.branch_info?.branch_type.equals("1", true)) {
                                    //GST branch
                                    binding.llNewreceiptMetalweights.visibility = View.GONE
                                    binding.llNewReceiptSubtotalRoot.visibility = View.GONE
                                } else {
                                    binding.llNewreceiptMetalweights.visibility = View.VISIBLE
                                    binding.llNewReceiptSubtotalRoot.visibility = View.VISIBLE
                                }


                                binding.tvNewreceiptItemquantity.setText("Qty: "+ receiptDetailModel.transactionData?.total_quantity)
                                binding.tvNewreceiptGrossWt.setText("G: "+ receiptDetailModel.transactionData?.total_gross_wt /*+ Constants.WEIGHT_GM_APPEND*/)
                                binding.tvNewreceiptLessWt.setText("L: "+ receiptDetailModel.transactionData?.total_less_wt /*+ Constants.WEIGHT_GM_APPEND*/)
                                binding.tvNewreceiptNetWt.setText("N: "+ receiptDetailModel.transactionData?.total_net_wt /*+ Constants.WEIGHT_GM_APPEND*/)
                                binding.tvNewreceiptFineWt.setText("F: "+ receiptDetailModel.transactionData?.total_fine_wt /*+ Constants.WEIGHT_GM_APPEND*/)

                                binding.tvNewreceiptSilverItemquantity.setText("Qty: "+ receiptDetailModel.transactionData?.silver_total_quantity)
                                binding.tvNewreceiptSilverGrossWt.setText("G: "+ receiptDetailModel.transactionData?.silver_total_gross_wt /*+ Constants.WEIGHT_GM_APPEND*/)
                                binding.tvNewreceiptSilverLessWt.setText("L: "+ receiptDetailModel.transactionData?.silver_total_less_wt /*+ Constants.WEIGHT_GM_APPEND*/)
                                binding.tvNewreceiptSilverNetWt.setText("N: "+ receiptDetailModel.transactionData?.silver_total_net_wt /*+ Constants.WEIGHT_GM_APPEND*/)
                                binding.tvNewreceiptSilverFineWt.setText("F: "+ receiptDetailModel.transactionData?.silver_total_fine_wt /*+ Constants.WEIGHT_GM_APPEND*/)

                                binding.tvNewreceiptOtherItemquantity.setText("Qty: "+ receiptDetailModel.transactionData?.other_total_quantity)
                                binding.tvNewreceiptOtherGrossWt.setText("G: "+ receiptDetailModel.transactionData?.other_total_gross_wt /*+ Constants.WEIGHT_GM_APPEND*/)
                                binding.tvNewreceiptOtherLessWt.setText("L: 0.000" /*+ Constants.WEIGHT_GM_APPEND*/)
                                binding.tvNewreceiptOtherNetWt.setText("N: "+ receiptDetailModel.transactionData?.other_total_net_wt /*+ Constants.WEIGHT_GM_APPEND*/)
                                binding.tvNewreceiptOtherFineWt.setText("F: 0.000" /*+ Constants.WEIGHT_GM_APPEND*/)


                                //binding.tvNewreceiptMiscCharges.setText(Constants.AMOUNT_RS_APPEND + receiptDetailModel.transactionData?.total_misc_charges)
                                binding.tvNewreceiptSubtotalCol1.setText(receiptDetailModel.transactionData?.total_fine_wt)
                                binding.tvNewreceiptSubtotalCol2.setText(receiptDetailModel.transactionData?.final_total_amount)
                                binding.tvNewreceiptSubtotalCol1Silver.setText(receiptDetailModel.transactionData?.silver_total_fine_wt)

                                when (binding.tvNewreceiptSubtotalCol1.text) {
                                    "0.000"-> {
                                        binding.tvNewreceiptSubtotalCol1.setText(receiptDetailModel.transactionData?.total_fine_wt)
                                        binding.tvNewreceiptSubtotalCol1.setTextColor(
                                            ContextCompat.getColor(
                                                this,
                                                R.color.header_black_text
                                            )
                                        )
                                    }
                                    else -> {
                                        binding.tvNewreceiptSubtotalCol1.setText(receiptDetailModel.transactionData?.total_fine_wt + " " + subTotalTerm)
                                        if (subTotalTermValue.equals("credit")) {
                                            binding.tvNewreceiptSubtotalCol1.setTextColor(
                                                ContextCompat.getColor(
                                                    this,
                                                    R.color.credit_color
                                                )
                                            )
                                        }
                                    }
                                }

                                when (binding.tvNewreceiptSubtotalCol2.text) {
                                    "0.00" -> {
                                        binding.tvNewreceiptSubtotalCol2.setText(receiptDetailModel.transactionData?.final_total_amount)
                                        binding.tvNewreceiptSubtotalCol2.setTextColor(
                                            ContextCompat.getColor(
                                                this,
                                                R.color.header_black_text
                                            )
                                        )
                                    }
                                    else->{
                                        binding.tvNewreceiptSubtotalCol2.setText(receiptDetailModel.transactionData?.final_total_amount + " " + subTotalTerm)
                                        if (subTotalTermValue.equals("credit")) {
                                            binding.tvNewreceiptSubtotalCol2.setTextColor(
                                                ContextCompat.getColor(
                                                    this,
                                                    R.color.credit_color
                                                )
                                            )
                                        }
                                    }
                                }

                                when(binding.tvNewreceiptSubtotalCol1Silver.text){
                                    "0.000"->{
                                        binding.tvNewreceiptSubtotalCol1Silver.setText(receiptDetailModel.transactionData?.silver_total_fine_wt)
                                        binding.tvNewreceiptSubtotalCol1Silver.setTextColor(
                                            ContextCompat.getColor(
                                                this,
                                                R.color.header_black_text
                                            )
                                        )
                                    }
                                    else->{
                                        binding.tvNewreceiptSubtotalCol1Silver.setText(receiptDetailModel.transactionData?.silver_total_fine_wt + " " + subTotalTerm)
                                        if (subTotalTermValue.equals("credit")) {
                                            binding.tvNewreceiptSubtotalCol1Silver.setTextColor(
                                                ContextCompat.getColor(
                                                    this,
                                                    R.color.credit_color
                                                )
                                            )
                                        }
                                    }
                                }

                                binding.tvNewreceiptTotalDueGold.setText(receiptDetailModel.transactionData?.total_fine_wt_with_IRT)
                                binding.tvNewreceiptTotalDueSilver.setText(receiptDetailModel.transactionData?.total_silver_fine_wt_with_IRT)
                                binding.tvNewreceiptTotalDueCash.setText(Constants.AMOUNT_RS_APPEND + receiptDetailModel.transactionData?.grand_total)
                                binding.tvReceiptdetailNotes.text =
                                    receiptDetailModel.transactionData?.remarks


                                if(!receiptDetailModel.transactionData?.total_gross_wt.equals("0.000")){
                                    binding.llNewreceiptMetalgold.visibility = View.VISIBLE
                                    //binding.tvNewreceiptSubtotallabel.visibility = View.VISIBLE
                                    /*binding.tvNewreceiptSubtotalCol1.visibility = View.VISIBLE
                                    binding.tvNewreceiptSubtotalCol2.visibility = View.VISIBLE*/
                                }else{
                                    binding.llNewreceiptMetalgold.visibility = View.GONE
                                    /*binding.tvNewreceiptSubtotalCol1.visibility = View.GONE
                                    binding.tvNewreceiptSubtotalCol2.visibility = View.GONE*/
                                }

                                if (!receiptDetailModel.transactionData?.silver_total_gross_wt.equals("0.000")) {
                                    binding.llNewreceiptMetalsilver.visibility = View.VISIBLE
                                   // binding.llNewreceiptSubtotalSilver.visibility = View.VISIBLE
                                    /*binding.tvNewreceiptSubtotallabelSilver.visibility = View.VISIBLE
                                    binding.tvNewreceiptSubtotalCol1Silver.visibility = View.VISIBLE
                                    binding.tvNewreceiptSubtotalCol2Silver.visibility = View.VISIBLE*/
                                } else {
                                    binding.llNewreceiptMetalsilver.visibility = View.GONE
                                   // binding.llNewreceiptSubtotalSilver.visibility = View.GONE
                                    /*binding.tvNewreceiptSubtotallabelSilver.visibility = View.GONE
                                    binding.tvNewreceiptSubtotalCol1Silver.visibility = View.GONE
                                    binding.tvNewreceiptSubtotalCol2Silver.visibility = View.GONE*/
                                }

                                if (!receiptDetailModel.transactionData?.other_total_gross_wt.equals("0.000")) {
                                    binding.llNewreceiptMetalother.visibility = View.VISIBLE
                                    //binding.llNewreceiptSubtotalOther.visibility = View.VISIBLE
                                    /*binding.tvNewreceiptSubtotallabelOther.visibility = View.VISIBLE
                                    binding.tvNewreceiptSubtotalCol1Other.visibility = View.VISIBLE
                                    binding.tvNewreceiptSubtotalCol2Other.visibility = View.VISIBLE*/

                                } else {
                                    binding.llNewreceiptMetalother.visibility = View.GONE
                                   // binding.llNewreceiptSubtotalOther.visibility = View.GONE
                                    /*binding.tvNewreceiptSubtotallabelOther.visibility = View.GONE
                                    binding.tvNewreceiptSubtotalCol1Other.visibility = View.GONE
                                    binding.tvNewreceiptSubtotalCol2Other.visibility = View.GONE*/
                                }



                                updateUIofTotalDue(receiptDetailModel)

                                addIRTDatainPref()
                                getIssueReceiveDataFromPref()
                                updateOpeningFineOpeningCash(receiptDetailModel)
                                updateClosingFineClosingCash(receiptDetailModel)

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


    private fun updateUIofTotalDue(receiptDetailModel: ReceiptDetailModel.Data) {

        if (!receiptDetailModel.transactionData?.total_fine_wt_with_IRT.equals("0.000") &&
            receiptDetailModel.transactionData?.total_silver_fine_wt_with_IRT.equals("0.000")
        ) {
            binding.llNewreceiptSilverTotaldue.visibility = View.GONE
            binding.tvNewreceiptTotaldueGoldLabel.visibility = View.VISIBLE
            binding.tvNewreceiptTotalDueGold.visibility = View.VISIBLE
        }

        if (!receiptDetailModel.transactionData?.total_fine_wt_with_IRT.equals("0.000") &&
            !receiptDetailModel.transactionData?.total_silver_fine_wt_with_IRT.equals("0.000")
        ) {
            binding.llNewreceiptSilverTotaldue.visibility = View.VISIBLE
            binding.tvNewreceiptTotaldueGoldLabel.visibility = View.VISIBLE
            binding.tvNewreceiptTotalDueGold.visibility = View.VISIBLE
        }



        if (receiptDetailModel.transactionData?.total_fine_wt_with_IRT.equals("0.000") &&
            !receiptDetailModel.transactionData?.total_silver_fine_wt_with_IRT.equals("0.000")
        ) {
            binding.llNewreceiptSilverTotaldue.visibility = View.GONE
            binding.tvNewreceiptTotaldueGoldLabel.setText("S: ")
            binding.tvNewreceiptTotalDueGold.setText(receiptDetailModel.transactionData?.total_silver_fine_wt_with_IRT)
            when (binding.tvNewreceiptTotalDueGold.text) {
                "0.000" -> {
                    binding.tvNewreceiptTotalDueGold.text =
                        receiptDetailModel.transactionData?.total_silver_fine_wt_with_IRT
                    binding.tvNewreceiptTotalDueGold.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.header_black_text
                        )
                    )
                }

                else -> {
                    binding.tvNewreceiptTotalDueGold.text =
                        receiptDetailModel.transactionData?.total_silver_fine_wt_with_IRT + " " +
                                receiptDetailModel.transactionData?.total_silver_fine_wt_with_IRT_term
                    if (receiptDetailModel.transactionData?.total_silver_fine_wt_with_IRT_short_term.equals(
                            "Dr",
                            ignoreCase = true
                        )
                    ) {
                        binding.tvNewreceiptTotalDueGold.setTextColor(
                            ContextCompat.getColor(
                                this,
                                R.color.debit_color
                            )
                        )
                    } else
                        binding.tvNewreceiptTotalDueGold.setTextColor(
                            ContextCompat.getColor(
                                this,
                                R.color.credit_color
                            )
                        )
                }
            }

            when (binding.tvNewreceiptTotalDueCash.text) {
                "0.00" -> {
                    binding.tvNewreceiptTotalDueCash.text =
                        receiptDetailModel.transactionData?.grand_total
                    binding.tvNewreceiptTotalDueCash.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.header_black_text
                        )
                    )
                }
                else -> {
                    binding.tvNewreceiptTotalDueCash.text =
                        receiptDetailModel.transactionData?.grand_total + " " +
                                receiptDetailModel.transactionData?.grand_total_term
                    if (receiptDetailModel.transactionData?.grand_total_short_term.equals(
                            "Dr",
                            ignoreCase = true
                        )
                    ) {
                        binding.tvNewreceiptTotalDueCash.setTextColor(
                            ContextCompat.getColor(
                                this,
                                R.color.debit_color
                            )
                        )
                    } else
                        binding.tvNewreceiptTotalDueCash.setTextColor(
                            ContextCompat.getColor(
                                this,
                                R.color.credit_color
                            )
                        )
                }
            }

        } else {
            binding.tvNewreceiptTotaldueGoldLabel.setText("G: ")
            updateTotalDuewithDrCr(receiptDetailModel)
        }

        /*if(!receiptDetailModel.data?.total_silver_fine_wt_with_IRT.equals("0.000")){
            binding.llNewinvoiceSilverTotaldue.visibility = View.VISIBLE
        }else{
            binding.llNewinvoiceSilverTotaldue.visibility = View.GONE
        }*/


        if (receiptDetailModel.transactionData?.total_fine_wt_with_IRT.equals("0.000") &&
            receiptDetailModel.transactionData?.total_silver_fine_wt_with_IRT.equals("0.000")
        ) {
            binding.llNewreceiptSilverTotaldue.visibility = View.GONE
            binding.tvNewreceiptTotaldueGoldLabel.visibility = View.GONE
            binding.tvNewreceiptTotalDueGold.visibility = View.GONE
        } else {
            //  binding.llNewinvoiceSilverTotaldue.visibility = View.VISIBLE
            //  tv_newinvoice_totaldue_gold_label.visibility = View.VISIBLE
            // tv_newinvoice_totalDue_gold.visibility = View.VISIBLE
        }
    }

    private fun updateTotalDuewithDrCr(receiptDetailModel: ReceiptDetailModel.Data) {
        when (binding.tvNewreceiptTotalDueGold.text) {
            "0.000" -> {
                binding.tvNewreceiptTotalDueGold.text =
                    receiptDetailModel.transactionData?.total_fine_wt_with_IRT
                binding.tvNewreceiptTotalDueGold.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.header_black_text
                    )
                )
            }

            else -> {
                binding.tvNewreceiptTotalDueGold.text =
                    receiptDetailModel.transactionData?.total_fine_wt_with_IRT + " " +
                            receiptDetailModel.transactionData?.total_fine_wt_with_IRT_term
                if (receiptDetailModel.transactionData?.total_fine_wt_with_IRT_short_term.equals(
                        "Dr",
                        ignoreCase = true
                    )
                ) {
                    binding.tvNewreceiptTotalDueGold.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.debit_color
                        )
                    )
                } else
                    binding.tvNewreceiptTotalDueGold.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.credit_color
                        )
                    )
            }
        }

        when (binding.tvNewreceiptTotalDueSilver.text) {
            "0.000" -> {
                binding.tvNewreceiptTotalDueSilver.text =
                    receiptDetailModel.transactionData?.total_silver_fine_wt_with_IRT
                binding.tvNewreceiptTotalDueSilver.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.header_black_text
                    )
                )
            }

            else -> {
                binding.tvNewreceiptTotalDueSilver.text =
                    receiptDetailModel.transactionData?.total_silver_fine_wt_with_IRT + " " +
                            receiptDetailModel.transactionData?.total_silver_fine_wt_with_IRT_term
                if (receiptDetailModel.transactionData?.total_silver_fine_wt_with_IRT_short_term.equals(
                        "Dr",
                        ignoreCase = true
                    )
                ) {
                    binding.tvNewreceiptTotalDueSilver.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.debit_color
                        )
                    )
                } else
                    binding.tvNewreceiptTotalDueSilver.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.credit_color
                        )
                    )
            }
        }

        when (binding.tvNewreceiptTotalDueCash.text) {
            "0.00" -> {
                binding.tvNewreceiptTotalDueCash.text =
                    receiptDetailModel.transactionData?.grand_total
                binding.tvNewreceiptTotalDueCash.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.header_black_text
                    )
                )
            }
            else -> {
                binding.tvNewreceiptTotalDueCash.text =
                    receiptDetailModel.transactionData?.grand_total + " " +
                            receiptDetailModel.transactionData?.grand_total_term
                if (receiptDetailModel.transactionData?.grand_total_short_term.equals(
                        "Dr",
                        ignoreCase = true
                    )
                ) {
                    binding.tvNewreceiptTotalDueCash.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.debit_color
                        )
                    )
                } else
                    binding.tvNewreceiptTotalDueCash.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.credit_color
                        )
                    )
            }
        }
    }


    private fun addIRTDatainPref() {
        receiptLineList.clear()
        for (i in 0 until receiptDetailModel.IRTData!!.size) {

            if (!receiptDetailModel.IRTData!!.get(i).transaction_type.equals("")) {

                val saleIRTModel = SalesLineModel.SaleLineModelDetails(
                    receiptDetailModel.IRTData!!.get(i).IRTDetails!!.cash_amount,
                    receiptDetailModel.IRTData!!.get(i).IRTDetails!!.cash_ledger,
                    receiptDetailModel.IRTData!!.get(i).IRTDetails!!.cash_ledger_name,
                    receiptDetailModel.IRTData!!.get(i).IRTDetails!!.cash_description,
                    receiptDetailModel.IRTData!!.get(i).IRTDetails!!.bank_amount,
                    receiptDetailModel.IRTData!!.get(i).IRTDetails!!.bank_ledger,
                    receiptDetailModel.IRTData!!.get(i).IRTDetails!!.bank_ledger_name,
                    receiptDetailModel.IRTData!!.get(i).IRTDetails!!.bank_mode,
                    receiptDetailModel.IRTData!!.get(i).IRTDetails!!.cheque_number,
                    receiptDetailModel.IRTData!!.get(i).IRTDetails!!.cheque_date,
                    receiptDetailModel.IRTData!!.get(i).IRTDetails!!.favouring_name,
                    receiptDetailModel.IRTData!!.get(i).IRTDetails!!.deuct_charges,
                    receiptDetailModel.IRTData!!.get(i).IRTDetails!!.deuct_charges_percentage,
                    receiptDetailModel.IRTData!!.get(i).IRTDetails!!.bank_final_amt,
                    receiptDetailModel.IRTData!!.get(i).IRTDetails!!.recipient_bank,
                    receiptDetailModel.IRTData!!.get(i).IRTDetails!!.account_no,
                    receiptDetailModel.IRTData!!.get(i).IRTDetails!!.ifs_code,
                    receiptDetailModel.IRTData!!.get(i).IRTDetails!!.utr_number,
                    receiptDetailModel.IRTData!!.get(i).IRTDetails!!.bank_description,
                    receiptDetailModel.IRTData!!.get(i).IRTDetails!!.rcm_gold_rate,
                    receiptDetailModel.IRTData!!.get(i).IRTDetails!!.rate_cut_amount,
                    receiptDetailModel.IRTData!!.get(i).IRTDetails!!.rate_cut_fine_term,
                    receiptDetailModel.IRTData!!.get(i).IRTDetails!!.metal_type_id_rate_cut,
                    receiptDetailModel.IRTData!!.get(i).IRTDetails!!.rate_cut_fine,
                    receiptDetailModel.IRTData!!.get(i).IRTDetails!!.item_id,
                    receiptDetailModel.IRTData!!.get(i).IRTDetails!!.item_name,
                    receiptDetailModel.IRTData!!.get(i).IRTDetails!!.metal_type_id_metal,
                    receiptDetailModel.IRTData!!.get(i).IRTDetails!!.maintain_stock_in_name_metal,
                    receiptDetailModel.IRTData!!.get(i).IRTDetails!!.gross_wt,
                    receiptDetailModel.IRTData!!.get(i).IRTDetails!!.less_wt,
                    receiptDetailModel.IRTData!!.get(i).IRTDetails!!.net_wt,
                    receiptDetailModel.IRTData!!.get(i).IRTDetails!!.touch,
                    receiptDetailModel.IRTData!!.get(i).IRTDetails!!.wast,
                    receiptDetailModel.IRTData!!.get(i).IRTDetails!!.fine_wt,
                    receiptDetailModel.IRTData!!.get(i).IRTDetails!!.adjustment_fine,
                    receiptDetailModel.IRTData!!.get(i).IRTDetails!!.metal_type_id_adjustments,
                    receiptDetailModel.IRTData!!.get(i).IRTDetails!!.adjustment_amount,
                    receiptDetailModel.IRTData!!.get(i).IRTDetails!!.adjustment_ledger,
                    receiptDetailModel.IRTData!!.get(i).IRTDetails!!.adjustment_ledger_name,
                    receiptDetailModel.IRTData!!.get(i).IRTDetails!!.adjustment_description,
                    receiptDetailModel.IRTData!!.get(i).IRTDetails!!.type,
                    receiptDetailModel.IRTData!!.get(i).IRTDetails!!.transaction_type,
                    receiptDetailModel.IRTData!!.get(i).IRTDetails!!.transaction_title

                )

                receiptLineList.add(saleIRTModel)
            }
        }

        prefs[Constants.PREF_SALES_LINE_INFO_KEY] = Gson().toJson(receiptLineList)
    }

    private fun getIssueReceiveDataFromPref() {
        if (prefs.contains(Constants.PREF_SALES_LINE_INFO_KEY)) {
            val collectionType =
                object :
                    TypeToken<ArrayList<SalesLineModel.SaleLineModelDetails>>() {}.type
            receiptLineList =
                Gson().fromJson(
                    prefs[Constants.PREF_SALES_LINE_INFO_KEY, ""],
                    collectionType
                )
            setupIssueReceiveAdapter()
        }
    }

    private fun setupIssueReceiveAdapter() {
        when (receiptLineList.size > 0) {
            true -> {
                binding.rvIssueReceiveListDetail.visibility = View.VISIBLE
                issueReceiveadapter.apply {
                    addissueReceiveList(receiptLineList)
                    notifyDataSetChanged()
                }
            }else->{

        }
        }

    }

    private fun updateClosingFineClosingCash(receiptDetailModel: ReceiptDetailModel.Data) {
        if (receiptDetailModel.transactionData?.closing_fine_balance!!.startsWith(
                "-"
            )
        ) {

            val clos_fine_bal: String =
                receiptDetailModel.transactionData.closing_fine_balance.toString()
                    .trim()
                    .substring(1)
            binding.tvCloBalFineWtDetailReceipt.text = clos_fine_bal

        }
        when (receiptDetailModel.transactionData.closing_fine_balance) {
            "0.000" -> {
                binding.tvCloBalFineWtDetailReceipt.text =
                    receiptDetailModel.transactionData.closing_fine_balance
                binding.tvCloBalFineWtDetailReceipt.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.header_black_text
                    )
                )
            }
            else -> {
                binding.tvCloBalFineWtDetailReceipt.text =
                    receiptDetailModel.transactionData.closing_fine_balance + " " +
                            receiptDetailModel.transactionData?.closing_fine_balance_term
                if (receiptDetailModel.transactionData.closing_fine_balance_short_term.equals(
                        "Dr",
                        ignoreCase = true
                    )
                ) {
                    binding.tvCloBalFineWtDetailReceipt.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.debit_color
                        )
                    )
                } else
                    binding.tvCloBalFineWtDetailReceipt.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.credit_color
                        )
                    )
            }
        }

        if (receiptDetailModel.transactionData.closing_silver_fine_balance!!.startsWith(
                "-"
            )
        ) {

            val clos_fine_bal: String =
                receiptDetailModel.transactionData.closing_silver_fine_balance.toString()
                    .trim()
                    .substring(1)
            binding.tvCloBalSilverDetailReceipt.text = clos_fine_bal

        }
        when (receiptDetailModel.transactionData.closing_silver_fine_balance) {
            "0.000" -> {
                binding.tvCloBalSilverDetailReceipt.text =
                    receiptDetailModel.transactionData.closing_silver_fine_balance
                binding.tvCloBalSilverDetailReceipt.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.header_black_text
                    )
                )
            }
            else -> {
                binding.tvCloBalSilverDetailReceipt.text =
                    receiptDetailModel.transactionData.closing_silver_fine_balance + " " +
                            receiptDetailModel.transactionData?.closing_silver_fine_balance_short_term
                if (receiptDetailModel.transactionData.closing_silver_fine_balance_short_term.equals(
                        "Dr",
                        ignoreCase = true
                    )
                ) {
                    binding.tvCloBalSilverDetailReceipt.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.debit_color
                        )
                    )
                } else
                    binding.tvCloBalSilverDetailReceipt.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.credit_color
                        )
                    )
            }
        }


        if (receiptDetailModel.transactionData?.closing_cash_balance!!.startsWith(
                "-"
            )
        ) {

            val clos_cash_bal: String =
                receiptDetailModel.transactionData?.closing_cash_balance!!.toString()
                    .trim().substring(1)
            binding.tvCloBalCashDetailReceipt.text = clos_cash_bal
        }

        when (receiptDetailModel.transactionData?.closing_cash_balance!!) {
            "0.00" -> {
                binding.tvCloBalCashDetailReceipt.text =
                    receiptDetailModel.transactionData?.closing_cash_balance!!
                binding.tvCloBalCashDetailReceipt.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.header_black_text
                    )
                )
            }
            else -> {
                binding.tvCloBalCashDetailReceipt.text =
                    receiptDetailModel.transactionData?.closing_cash_balance!! + " " +
                            receiptDetailModel.transactionData?.closing_cash_balance_term
                if (receiptDetailModel.transactionData?.closing_cash_balance_short_term.equals(
                        "Dr",
                        ignoreCase = true
                    )
                ) {
                    binding.tvCloBalCashDetailReceipt.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.debit_color
                        )
                    )
                } else
                    binding.tvCloBalCashDetailReceipt.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.credit_color
                        )
                    )
            }

        }

    }


    private fun updateOpeningFineOpeningCash(receiptDetailModel: ReceiptDetailModel.Data) {
        if (receiptDetailModel.transactionData?.opening_fine_balance!!.startsWith("-")) {

            val open_fine_bal: String =
                receiptDetailModel.transactionData?.opening_fine_balance!!.toString()
                    .trim()
                    .substring(1)
            binding.tvOpenBalFineDetailReceipt.text = open_fine_bal

        }
        when (receiptDetailModel.transactionData?.opening_fine_balance!!) {
            "0.000" -> {
                binding.tvOpenBalFineDetailReceipt.text =
                    receiptDetailModel.transactionData?.opening_fine_balance!!
                binding.tvOpenBalFineDetailReceipt.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.header_black_text
                    )
                )
            }
            else -> {
                binding.tvOpenBalFineDetailReceipt.text =
                    receiptDetailModel.transactionData?.opening_fine_balance!! + " " +
                            receiptDetailModel.transactionData?.opening_fine_balance_term

                if (receiptDetailModel.transactionData?.opening_fine_balance_short_term.equals(
                        "Dr",
                        ignoreCase = true
                    )
                ) {
                    binding.tvOpenBalFineDetailReceipt.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.debit_color
                        )
                    )
                } else
                    binding.tvOpenBalFineDetailReceipt.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.credit_color
                        )
                    )
            }
        }


        if (receiptDetailModel.transactionData?.opening_silver_fine_balance!!.startsWith("-")) {

            val open_fine_bal: String =
                receiptDetailModel.transactionData?.opening_silver_fine_balance!!.toString()
                    .trim()
                    .substring(1)
            binding.tvOpenBalFineSilverDetailReceipt.text = open_fine_bal

        }
        when (receiptDetailModel.transactionData?.opening_silver_fine_balance!!) {
            "0.000" -> {
                binding.tvOpenBalFineSilverDetailReceipt.text =
                    receiptDetailModel.transactionData?.opening_silver_fine_balance!!
                binding.tvOpenBalFineSilverDetailReceipt.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.header_black_text
                    )
                )
            }
            else -> {
                binding.tvOpenBalFineSilverDetailReceipt.text =
                    receiptDetailModel.transactionData?.opening_silver_fine_balance!! + " " +
                            receiptDetailModel.transactionData?.opening_silver_fine_balance_term

                if (receiptDetailModel.transactionData?.opening_silver_fine_balance_short_term.equals(
                        "Dr",
                        ignoreCase = true
                    )
                ) {
                    binding.tvOpenBalFineSilverDetailReceipt.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.debit_color
                        )
                    )
                } else
                    binding.tvOpenBalFineSilverDetailReceipt.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.credit_color
                        )
                    )
            }
        }


        if (receiptDetailModel.transactionData?.opening_cash_balance!!.startsWith(
                "-"
            )
        ) {

            val open_cash_bal: String =
                receiptDetailModel.transactionData?.opening_cash_balance!!.toString()
                    .trim().substring(1)
            binding.tvOpenBalCashDetailReceipt.text = open_cash_bal
        }

        when (receiptDetailModel.transactionData?.opening_cash_balance!!) {
            "0.00" -> {
                binding.tvOpenBalCashDetailReceipt.text =
                    receiptDetailModel.transactionData?.opening_cash_balance!!
                binding.tvOpenBalCashDetailReceipt.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.header_black_text
                    )
                )
            }
            else -> {
                binding.tvOpenBalCashDetailReceipt.text =
                    receiptDetailModel.transactionData?.opening_cash_balance!! + " " +
                            receiptDetailModel.transactionData?.opening_cash_balance_term
                if (receiptDetailModel.transactionData?.opening_cash_balance_short_term.equals(
                        "Dr",
                        ignoreCase = true
                    )
                ) {
                    binding.tvOpenBalCashDetailReceipt.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.debit_color
                        )
                    )
                } else
                    binding.tvOpenBalCashDetailReceipt.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.credit_color
                        )
                    )
            }


        }
    }


    private fun retrieveListforitemreceipt(items: List<SaleDetailModel.Item1427117511>) {
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
                       tv_Receiptdetail_items_desc.visibility = View.VISIBLE
                       tv_Receiptdetail_items_desc.text = item_quantity_unit.toString()
                   }
                   false -> {
                       tv_Receiptdetail_items_desc.visibility = View.GONE
                   }
               }

               count++
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


}
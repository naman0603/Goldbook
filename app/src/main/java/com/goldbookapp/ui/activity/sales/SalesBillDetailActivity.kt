package com.goldbookapp.ui.activity.sales

import android.Manifest
import android.app.*
import android.content.*
import android.database.Cursor
import android.graphics.Color
import android.graphics.Color.TRANSPARENT
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
import com.goldbookapp.databinding.SalesBillDetailActivityBinding
import com.goldbookapp.model.*
import com.goldbookapp.permissions.PermissionHandler
import com.goldbookapp.permissions.Permissions
import com.goldbookapp.ui.activity.PdfDocumentAdapter
import com.goldbookapp.ui.activity.PrintJobMonitorService
import com.goldbookapp.ui.activity.viewmodel.SaleDetailsViewModel
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
import kotlinx.android.synthetic.main.new_invoice_activity.*
import kotlinx.android.synthetic.main.sales_bill_detail_activity.*
import kotlinx.android.synthetic.main.sales_bill_detail_activity.ll_newinvoice_metalgold
import kotlinx.android.synthetic.main.sales_bill_detail_activity.tv_newinvoice_subtotalCol1
import kotlinx.android.synthetic.main.sales_bill_detail_activity.tv_newinvoice_subtotalCol1_Silver
import kotlinx.android.synthetic.main.sales_bill_detail_activity.tv_newinvoice_subtotalCol2
import kotlinx.android.synthetic.main.sales_bill_detail_activity.view.*
import kotlinx.android.synthetic.main.toolbar_layout.*
import kotlinx.android.synthetic.main.toolbar_layout.view.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.*
import java.math.BigDecimal
import java.util.regex.Matcher
import java.util.regex.Pattern


class SalesBillDetailActivity : AppCompatActivity() {

    var fileSavePath = ""
    var isFromWhatsapp: Boolean = false;
    private lateinit var notificationBuilder: NotificationCompat.Builder
    private lateinit var notificationManager: NotificationManager
    lateinit var popupMenu: PopupMenu
    private val PERMISSION_REQUEST_CODE = 101

    lateinit var binding: SalesBillDetailActivityBinding
    private lateinit var viewModel: SaleDetailsViewModel
    private var mgr: PrintManager? = null
    var filename: String = ""
    var webUrl: String = ""
    var report_type: String = ""
    var isFromDownload: Boolean = false

    var isFromThread: Boolean = true

    lateinit var loginModel: LoginModel

    lateinit var saleDetailModel: SaleDetailModel.Data

    private lateinit var adapter: SaleDetailBill_ItemAdapter

    var salesID: String? = ""

    var imageURL: String? = ""
    lateinit var prefs: SharedPreferences
    var salesLineList = ArrayList<SalesLineModel.SaleLineModelDetails>()
    private lateinit var issueReceiveadapter: IssueReceiveAdapter
    var is_Igst_enable: Boolean = false
    var debit_short_term: String = ""
    var credit_short_term: String = ""

    var fineDefaultTermList: List<ItemDefaultTermModel.Data.Default_term>? = null
    var subTotalTerm: String = ""
    var subTotalTermValue: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.sales_bill_detail_activity)
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
        binding.root.imgRight.visibility = View.VISIBLE
        binding.root.imgRight2.visibility = View.GONE
        popupMenu.menu.getItem(3).setVisible(false)
        popupMenu.menu.getItem(2).setVisible(false)
        popupMenu.menu.getItem(1).setVisible(false)
    }

    private fun defaultEnableAllButtonUI() {
        binding.root.imgRight.visibility = View.VISIBLE
        binding.root.imgRight2.visibility = View.VISIBLE
        popupMenu.menu.getItem(3).setVisible(true)
        popupMenu.menu.getItem(2).setVisible(true)
        popupMenu.menu.getItem(1).setVisible(true)
    }

    private fun setupViewModel() {
        viewModel =
            ViewModelProvider(this, ViewModelFactory(ApiHelper(RetrofitBuilder.apiService))).get(
                SaleDetailsViewModel::class.java
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

        clearPref()
        imgRight2?.clickWithDebounce {

            if (this::saleDetailModel.isInitialized) {
                startActivity(
                    Intent(this, NewInvoiceActivity::class.java)
                        .putExtra(Constants.SALES_DETAIL_KEY, Gson().toJson(saleDetailModel))
                )
                finish()
            }

        }

        //recyclerviewsetup
        binding.root.rv_salesbilldetail_item.layoutManager = LinearLayoutManager(this)
        adapter = SaleDetailBill_ItemAdapter(arrayListOf())
        binding.root.rv_salesbilldetail_item.adapter = adapter

        // issue receive adapter
        debit_short_term = loginModel?.data!!.company_info!!.general_settings!!.debit_short_term!!
        credit_short_term = loginModel?.data!!.company_info!!.general_settings!!.credit_short_term!!
        binding.rvIssueReceiveList.layoutManager = LinearLayoutManager(this)
        issueReceiveadapter =
            IssueReceiveAdapter(arrayListOf(), "sales", true, debit_short_term, credit_short_term)
        binding.rvIssueReceiveList.adapter = issueReceiveadapter

        if (intent.extras?.containsKey(Constants.ModuleID)!!) {
            salesID = intent.getStringExtra(Constants.ModuleID)
        }


        if (intent.extras?.containsKey(Constants.SALES_RECENT_TRANS_DETAIL_KEY)!!) {
            //salesID = intent.getStringExtra(Constants.ModuleID)
            var sale_str: String? = intent.getStringExtra(Constants.SALES_RECENT_TRANS_DETAIL_KEY)
            var saleDetailModel: DashboardDetailsModel.Data.Recent_transactions = Gson().fromJson(
                sale_str,
                DashboardDetailsModel.Data.Recent_transactions::class.java
            )

            salesID = saleDetailModel.module_id
            tvTitle.text = saleDetailModel.transaction_number
            tv_salesbilldetail_custname.text = saleDetailModel.display_name
            tv_salesbilldetail_noofitems.text = saleDetailModel.no_of_items.toString() + " item"
            tv_salesbilldetail_transactiondate.text = saleDetailModel.transaction_date

        }
        if (intent.extras?.containsKey(Constants.SALES_DETAIL_KEY)!!) {
            var sale_str: String? = intent.getStringExtra(Constants.SALES_DETAIL_KEY)
            var saleDetailModel: SearchListSalesModel.Data1465085328 = Gson().fromJson(
                sale_str,
                SearchListSalesModel.Data1465085328::class.java
            )

            salesID = saleDetailModel.transaction_id
            tvTitle.text = saleDetailModel.invoice_number
            tv_salesbilldetail_custname.text = saleDetailModel.contact_name
            tv_salesbilldetail_noofitems.text = saleDetailModel.total_items.toString() + " item"
            tv_salesbilldetail_transactiondate.text = saleDetailModel.transaction_date


        }

        imgLeft?.clickWithDebounce {
            onBackPressed()
        }



        imgRight.clickWithDebounce {

            if (saleDetailModel.transactionData?.is_gst_applicable.toString().contains("1")) {
                if (this::popupMenu.isInitialized) {
                    popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
                        if (this::saleDetailModel.isInitialized) {
                            when (item.itemId) {
                                //gst
                                R.id.sharesubmenuitem1 -> {
                                    isFromWhatsapp = false
                                    voucherTextAPI(loginModel?.data?.bearer_access_token, salesID)
                                }
                                R.id.sharesubmenuitem2 -> {
                                    isFromWhatsapp = true
                                    voucherTextAPI(loginModel?.data?.bearer_access_token, salesID)
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

                                    ensureDeleteDialog(saleDetailModel.transactionData?.transaction_id!!)
                            }
                        }
                        true
                    })
                }

            } else {
                if (this::popupMenu.isInitialized) {
                    popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
                        if (this::saleDetailModel.isInitialized) {
                            when (item.itemId) {

                                R.id.sharesubmenuitem1 -> {
                                    isFromWhatsapp = false
                                    voucherTextAPI(loginModel?.data?.bearer_access_token, salesID)
                                }
                                R.id.sharesubmenuitem2 -> {
                                    isFromWhatsapp = true
                                    voucherTextAPI(loginModel?.data?.bearer_access_token, salesID)
                                }
                                R.id.downloadsubmenuitem1 -> {
                                    if (saleDetailModel.transactionData?.is_gst_applicable.toString()
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
                                    if (saleDetailModel.transactionData?.is_gst_applicable.toString()
                                            .contains("1")
                                    ) {
                                        report_type = ""
                                    } else {
                                        report_type = "Sale Non Gst 3"
                                    }

                                    checkandRequestPermission()
                                    isFromDownload = true
                                }

                                /* R.id.action_download -> {
                                     report_type = ""
                                     checkandRequestPermission()
                                     isFromDownload = true
                                 }*/

                                /*R.id.action_print ->{
                                    report_type = ""
                                    checkandRequestPermission()
                                    isFromDownload = false
                                }*/

                                R.id.printsubmenuitem1 -> {
                                    if (saleDetailModel.transactionData?.is_gst_applicable.toString()
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
                                    if (saleDetailModel.transactionData?.is_gst_applicable.toString()
                                            .contains("1")
                                    ) {
                                        report_type = ""
                                    } else {
                                        report_type = "Sale Non Gst 3"
                                    }

                                    checkandRequestPermission()
                                    isFromDownload = false
                                }

                                R.id.action_delete ->

                                    ensureDeleteDialog(saleDetailModel.transactionData?.invoice_number!!)
                            }
                        }
                        true
                    })
                }

            }
            popupMenu.show()
        }

        iv_salesbilldetail_attachmentone.clickWithDebounce {

            if (!imageURL?.isBlank()!!) {
                showFullImage(
                    imageURL
                )
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
                                subTotalTermValue =
                                    fineDefaultTermList!!.get(0).default_term_value!!

                                if (salesID?.isNotEmpty()!!) {

                                    saleDetailAPI(
                                        loginModel?.data?.bearer_access_token,
                                        salesID
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
            if (data.permission!!.get(i).startsWith(getString(R.string.sales))) {
                // Restriction check for Customer
                when (data.permission!!.get(i).endsWith(getString(R.string.add_edit), true)) {
                    true -> {
                        binding.root.imgRight2.visibility = View.VISIBLE
                    }
                    else -> {

                    }
                }
            }
            if (data.permission!!.get(i).startsWith(getString(R.string.sales))) {
                // Restriction check for Customer
                when (data.permission!!.get(i).endsWith(getString(R.string.del), true)) {
                    true -> {
                        popupMenu.menu.getItem(3).setVisible(true)
                    }
                    else -> {

                    }
                }
            }
            if (data.permission!!.get(i).startsWith(getString(R.string.sales))) {
                // Restriction check for Customer
                when (data.permission!!.get(i).endsWith(getString(R.string.print), true)) {
                    true -> {
                        popupMenu.menu.getItem(1).setVisible(true)
                    }
                    else -> {

                    }
                }
            }
            if (data.permission!!.get(i).startsWith(getString(R.string.sales))) {
                // Restriction check for Customer
                when (data.permission!!.get(i).endsWith(getString(R.string.download), true)) {
                    true -> {
                        popupMenu.menu.getItem(2).setVisible(true)
                    }
                    else -> {

                    }
                }
            }
        }
    }


    private fun ensureDeleteDialog(sale: String) {
        val builder = AlertDialog.Builder(this)
        val DeleteClick = { dialog: DialogInterface, which: Int ->
            //itemCategory delete api call

            deleteSale(
                loginModel?.data?.bearer_access_token,
                saleDetailModel.transactionData?.transaction_id
            )
        }
        val dialogdismiss = { dialog: DialogInterface, which: Int ->
            dialog.dismiss()
        }
        with(builder)
        {
            setTitle(context.getString(R.string.delSaleDialog1Title))
            setMessage(context.getString(R.string.saleDialog1Message))
            setPositiveButton(context.getString(R.string.Cancel), dialogdismiss)
            setNeutralButton(
                context.getString(R.string.Delete), DialogInterface.OnClickListener(
                    function = DeleteClick
                )
            )
            show()
        }
    }

    private fun calltovoucherPrint() {
        progressBar_Salebilldetail.visibility = View.VISIBLE
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
                "sales",
                salesID,
                report_type
            )

            call.enqueue(object : Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    //not required
                    CommonUtils.somethingWentWrong(this@SalesBillDetailActivity)
                    progressBar_Salebilldetail.visibility = View.GONE
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
                                    this@SalesBillDetailActivity,
                                    "PDF saved at " + fileSavePath.drop(
                                        20
                                    )/*getString(R.string.voucher_downloaded_successfully)*/,
                                    Toast.LENGTH_LONG
                                )
                                toast.setGravity(Gravity.CENTER, 0, 0)
                                toast.show()
                                // showDownloadNotification()
                                val rootDirtory = File(
                                    Environment.getExternalStoragePublicDirectory(
                                        Environment.DIRECTORY_DOWNLOADS
                                    ), "GoldBook"
                                )
                                val myDirectory = File(rootDirtory, "Sales")
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
                                    "Sales PDF",
                                    PdfDocumentAdapter(
                                        this@SalesBillDetailActivity,
                                        filename,
                                        "Sales_Voucher"
                                    ),
                                    PrintAttributes.Builder()
                                        .setMediaSize(PrintAttributes.MediaSize.ISO_A4).build()
                                )

                            }
                        }

                        progressBar_Salebilldetail.visibility = View.GONE

                    }

                }

            })
        }


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
                        "Download/" + "GoldBook/" + "Sales"
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
                val myDirectory = File(rootDirtory, "Sales")
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


    private fun deleteSale(token: String?, transaction_id: String?) {
        if (NetworkUtils.isConnected()) {
            if (isValidClickPressed()) {
                viewModel.deleteSale(token, transaction_id).observe(this, Observer {
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


    fun saleDetailAPI(
        token: String?,
        transaction_id: String?
    ) {

        if (NetworkUtils.isConnected()) {

            viewModel.saleDetail(token, transaction_id).observe(this, Observer {
                it?.let { resource ->
                    when (resource.status) {

                        Status.SUCCESS -> {

                            if (it.data?.status == true) {
                                Log.v("sucessdetail1", "")
                                llSalesBillDetail_root.visibility = View.VISIBLE
                                saleDetailModel = it.data.data!!

                                popupMenu = PopupMenu(this, binding.root.imgRight)
                                /*popupMenu.menu.getItem(3).setVisible(true)
                                popupMenu.menu.getItem(2).setVisible(true)
                                popupMenu.menu.getItem(1).setVisible(true)*/

                                if (saleDetailModel.transactionData?.is_gst_applicable.toString()
                                        .contains("1")
                                ) {
                                    popupMenu.menuInflater.inflate(
                                        R.menu.popup_menu_purchase_detail,
                                        popupMenu.menu
                                    )
                                    // popupMenu.menu.getItem(0).setVisible(false)
                                } else {
                                    popupMenu.menuInflater.inflate(
                                        R.menu.popup_menu_sales_detail,
                                        popupMenu.menu
                                    )
                                    // popupMenu.menu.getItem(0).setVisible(false)
                                    popupMenu.menu.getItem(3).setVisible(true)

                                }

                                binding.root.tvTitle.text =
                                    saleDetailModel.transactionData!!.invoice_number
                                binding.tvSalesbilldetailCustname.text =
                                    saleDetailModel.transactionData!!.display_name
                                /* binding.tvSalesbilldetailNoofitems.text =
                                     saleDetailModel.transactionData?.invoice_number.toString() + " item"*/
                                binding.tvSalesbilldetailTransactiondate.text =
                                    saleDetailModel.transactionData!!.transaction_date

                                retrieveListforitem(saleDetailModel.transactionData?.item!!)


                                if (saleDetailModel.transactionData!!.image != null && saleDetailModel.transactionData!!.image?.size!! > 0) {
                                    binding.tvAttachmentLabel.visibility = View.VISIBLE
                                    binding.ivSalesbilldetailAttachmentone.visibility = View.VISIBLE
                                    imageURL =
                                        saleDetailModel.transactionData!!.image?.get(0)?.image
                                    Glide.with(this).load(imageURL).circleCrop()
                                        .placeholder(R.drawable.ic_user_placeholder)
                                        .into(binding.ivSalesbilldetailAttachmentone)
                                }

                                if (!saleDetailModel.transactionData?.total_gross_wt.equals("0.000")) {
                                    ll_newinvoice_metalgold.visibility = View.VISIBLE
                                } else {
                                    ll_newinvoice_metalgold.visibility = View.GONE
                                }

                                if (!saleDetailModel.transactionData?.silver_total_gross_wt.equals("0.000")) {
                                    binding.llNewinvoiceMetalsilver.visibility = View.VISIBLE

                                } else {
                                    binding.llNewinvoiceMetalsilver.visibility = View.GONE

                                }

                                if (!saleDetailModel.transactionData?.other_total_gross_wt.equals("0.000")) {
                                    binding.llNewinvoiceMetalother.visibility = View.VISIBLE

                                } else {
                                    binding.llNewinvoiceMetalother.visibility = View.GONE

                                }



                                binding.tvNewinvoiceItemquantity.setText("Qty: " + saleDetailModel.transactionData?.total_quantity)
                                binding.tvNewinvoiceGrossWt.setText("G: " + saleDetailModel.transactionData?.total_gross_wt /*+ Constants.WEIGHT_GM_APPEND*/)
                                binding.tvNewinvoiceLessWt.setText("L: " + saleDetailModel.transactionData?.total_less_wt /*+ Constants.WEIGHT_GM_APPEND*/)
                                binding.tvNewinvoiceNetWt.setText("N: " + saleDetailModel.transactionData?.total_net_wt /*+ Constants.WEIGHT_GM_APPEND*/)
                                binding.tvNewinvoiceFineWt.setText("F: " + saleDetailModel.transactionData?.total_fine_wt /*+ Constants.WEIGHT_GM_APPEND*/)
                                // binding.tvNewinvoiceMiscCharges.setText(Constants.AMOUNT_RS_APPEND + saleDetailModel.transactionData?.total_misc_charges)
                                //binding.tvNewinvoiceSubtotalCol1.setText(Constants.AMOUNT_RS_APPEND + saleDetailModel.transactionData?.sub_total)
                                //binding.tvNewinvoiceSubtotalCol2.setText(Constants.AMOUNT_RS_APPEND + saleDetailModel.transactionData?.sub_total)

                                binding.tvNewinvoiceSilverItemquantity.setText("Qty: " + saleDetailModel.transactionData?.silver_total_quantity)
                                binding.tvNewinvoiceSilverGrossWt.setText("G: " + saleDetailModel.transactionData?.silver_total_gross_wt /*+ Constants.WEIGHT_GM_APPEND*/)
                                binding.tvNewinvoiceSilverLessWt.setText("L: " + saleDetailModel.transactionData?.silver_total_less_wt /*+ Constants.WEIGHT_GM_APPEND*/)
                                binding.tvNewinvoiceSilverNetWt.setText("N: " + saleDetailModel.transactionData?.silver_total_net_wt /*+ Constants.WEIGHT_GM_APPEND*/)
                                binding.tvNewinvoiceSilverFineWt.setText("F: " + saleDetailModel.transactionData?.silver_total_fine_wt /*+ Constants.WEIGHT_GM_APPEND*/)

                                binding.tvNewinvoiceOtherItemquantity.setText("Qty: " + saleDetailModel.transactionData?.other_total_quantity)
                                binding.tvNewinvoiceOtherGrossWt.setText("G: " + saleDetailModel.transactionData?.other_total_gross_wt /*+ Constants.WEIGHT_GM_APPEND*/)
                                binding.tvNewinvoiceOtherLessWt.setText("L: " + "0.000" /*+ Constants.WEIGHT_GM_APPEND*/)
                                binding.tvNewinvoiceOtherNetWt.setText("N: " + saleDetailModel.transactionData?.other_total_net_wt /*+ Constants.WEIGHT_GM_APPEND*/)
                                binding.tvNewinvoiceOtherFineWt.setText("F: " + "0.000" /*+ Constants.WEIGHT_GM_APPEND*/)


                                binding.tvNewinvoiceSubtotalCol1.setText(saleDetailModel.transactionData?.total_fine_wt)
                                binding.tvNewinvoiceSubtotalCol2.setText(saleDetailModel.transactionData?.final_total_amount)
                                binding.tvNewinvoiceSubtotalCol1Silver.setText(saleDetailModel.transactionData?.silver_total_fine_wt)

                                when (binding.tvNewinvoiceSubtotalCol1.text) {
                                    "0.000" -> {
                                        binding.tvNewinvoiceSubtotalCol1.setText(saleDetailModel.transactionData?.total_fine_wt)
                                        tv_newinvoice_subtotalCol1.setTextColor(
                                            ContextCompat.getColor(
                                                this,
                                                R.color.header_black_text
                                            )
                                        )
                                    }
                                    else -> {
                                        binding.tvNewinvoiceSubtotalCol1.setText(saleDetailModel.transactionData?.total_fine_wt + " " + subTotalTerm)
                                        if (subTotalTermValue.equals("debit")) {
                                            tv_newinvoice_subtotalCol1.setTextColor(
                                                ContextCompat.getColor(
                                                    this,
                                                    R.color.debit_color
                                                )
                                            )
                                        }
                                    }
                                }

                                when (binding.tvNewinvoiceSubtotalCol2.text) {
                                    "0.00" -> {
                                        binding.tvNewinvoiceSubtotalCol2.setText(saleDetailModel.transactionData?.final_total_amount)
                                        tv_newinvoice_subtotalCol2.setTextColor(
                                            ContextCompat.getColor(
                                                this,
                                                R.color.header_black_text
                                            )
                                        )
                                    }
                                    else -> {
                                        binding.tvNewinvoiceSubtotalCol2.setText(saleDetailModel.transactionData?.final_total_amount + " " + subTotalTerm)
                                        if (subTotalTermValue.equals("debit")) {
                                            tv_newinvoice_subtotalCol2.setTextColor(
                                                ContextCompat.getColor(
                                                    this,
                                                    R.color.debit_color
                                                )
                                            )
                                        }
                                    }
                                }

                                when (binding.tvNewinvoiceSubtotalCol1Silver.text) {
                                    "0.000" -> {
                                        binding.tvNewinvoiceSubtotalCol1Silver.setText(
                                            saleDetailModel.transactionData?.silver_total_fine_wt
                                        )
                                        tv_newinvoice_subtotalCol1_Silver.setTextColor(
                                            ContextCompat.getColor(
                                                this,
                                                R.color.header_black_text
                                            )
                                        )
                                    }
                                    else -> {
                                        binding.tvNewinvoiceSubtotalCol1Silver.setText(
                                            saleDetailModel.transactionData?.silver_total_fine_wt + " " + subTotalTerm
                                        )

                                        if (subTotalTermValue.equals("debit")) {
                                            tv_newinvoice_subtotalCol1_Silver.setTextColor(
                                                ContextCompat.getColor(
                                                    this,
                                                    R.color.debit_color
                                                )
                                            )
                                        }
                                    }
                                }
                                binding.tvNewinvoiceTotalDueGold.setText(saleDetailModel.transactionData?.total_fine_wt_with_IRT)
                                binding.tvNewinvoiceTotalDueSilver.setText(saleDetailModel.transactionData?.total_silver_fine_wt_with_IRT)
                                binding.tvNewinvoiceTotalDueCash.setText(Constants.AMOUNT_RS_APPEND + saleDetailModel.transactionData?.grand_total)


                                updateUIofTotalDue()
                                //updateTotalDuewithCrDr()

                                //condition for gst invoice
                                when (saleDetailModel.transactionData?.is_gst_applicable.toString()
                                    .contains("1")) {
                                    true -> {
                                        if (
                                            (saleDetailModel.transactionData?.sgst_amount!!.toBigDecimal() > BigDecimal.ZERO
                                                    && !saleDetailModel.transactionData?.sgst_amount!!.isBlank()) &&
                                            (saleDetailModel.transactionData?.place_of_supply_id.equals(
                                                loginModel.data!!.branch_info!!.state_id.toString()
                                            ))
                                        ) {
                                            binding.tvNewinvoiceSgstCol0.visibility = View.VISIBLE
                                            binding.tvNewinvoiceSgstCol1.visibility = View.VISIBLE
                                            binding.tvNewinvoiceSgstCol2.visibility = View.VISIBLE

                                            binding.tvNewinvoiceSgstCol1.mLabelView!!.setText(
                                                saleDetailModel.transactionData?.sgstData?.ledger_name
                                            )
                                            binding.tvNewinvoiceSgstCol2.setText(Constants.AMOUNT_RS_APPEND + saleDetailModel.transactionData?.sgst_amount)
                                        } else {
                                            binding.tvNewinvoiceSgstCol0.visibility = View.GONE
                                            binding.tvNewinvoiceSgstCol1.visibility = View.GONE
                                            binding.tvNewinvoiceSgstCol2.visibility = View.GONE
                                        }

                                        if (
                                            (saleDetailModel.transactionData?.cgst_amount!!.toBigDecimal() > BigDecimal.ZERO
                                                    && !saleDetailModel.transactionData?.cgst_amount!!.isBlank()) &&
                                            (saleDetailModel.transactionData?.place_of_supply_id.equals(
                                                loginModel.data!!.branch_info!!.state_id.toString()
                                            ))
                                        ) {
                                            binding.tvNewinvoiceCgstCol0.visibility = View.VISIBLE
                                            binding.tvNewinvoiceCgstCol1.visibility = View.VISIBLE
                                            binding.tvNewinvoiceCgstCol2.visibility = View.VISIBLE
                                            binding.tvNewinvoiceCgstCol1.mLabelView!!.setText(
                                                saleDetailModel.transactionData?.cgstData?.ledger_name
                                            )
                                            binding.tvNewinvoiceCgstCol2.setText(Constants.AMOUNT_RS_APPEND + saleDetailModel.transactionData?.cgst_amount)

                                        } else {
                                            binding.tvNewinvoiceCgstCol0.visibility = View.GONE
                                            binding.tvNewinvoiceCgstCol1.visibility = View.GONE
                                            binding.tvNewinvoiceCgstCol2.visibility = View.GONE
                                        }

                                        if ((saleDetailModel.transactionData?.igst_amount!!.toBigDecimal() > BigDecimal.ZERO && !saleDetailModel.transactionData?.igst_amount!!.isBlank()) &&
                                            (!saleDetailModel.transactionData?.place_of_supply_id.equals(
                                                loginModel.data!!.branch_info!!.state_id.toString()
                                            ))
                                        ) {
                                            binding.tvNewinvoiceIgstCol0.visibility = View.VISIBLE
                                            binding.tvNewinvoiceIgstCol1.visibility = View.VISIBLE
                                            binding.tvNewinvoiceIgstCol2.visibility = View.VISIBLE
                                            binding.tvNewinvoiceIgstCol1.mLabelView!!.setText(
                                                saleDetailModel.transactionData?.igstData?.ledger_name
                                            )
                                            binding.tvNewinvoiceIgstCol2.setText(Constants.AMOUNT_RS_APPEND + saleDetailModel.transactionData?.igst_amount)
                                        } else {
                                            binding.tvNewinvoiceIgstCol0.visibility = View.GONE
                                            binding.tvNewinvoiceIgstCol1.visibility = View.GONE
                                            binding.tvNewinvoiceIgstCol2.visibility = View.GONE
                                        }

                                        when (saleDetailModel.transactionData?.is_tcs_applicable.equals(
                                            "1"
                                        )) {
                                            true -> {
                                                binding.radioTCSNewInvoice.visibility = View.VISIBLE
                                                binding.tvNewinvoiceTdstcsCol1.visibility =
                                                    View.VISIBLE
                                                binding.tvNewinvoiceTcstdsCol2.visibility =
                                                    View.VISIBLE

                                            }
                                            false -> {
                                                binding.radioTCSNewInvoice.visibility = View.GONE
                                                binding.tvNewinvoiceTdstcsCol1.visibility =
                                                    View.GONE
                                                binding.tvNewinvoiceTcstdsCol2.visibility =
                                                    View.GONE
                                            }
                                        }

                                        when (saleDetailModel.transactionData?.is_tds_applicable.equals(
                                            "1"
                                        )) {
                                            true -> {
                                                binding.radioTDSNewInvoice.visibility = View.VISIBLE
                                                binding.tvNewinvoiceTdstcsCol1.visibility =
                                                    View.VISIBLE
                                                binding.tvNewinvoiceTcstdsCol2.visibility =
                                                    View.VISIBLE

                                            }
                                            false -> {
                                                binding.radioTDSNewInvoice.visibility = View.GONE
                                                binding.tvNewinvoiceTdstcsCol1.visibility =
                                                    View.GONE
                                                binding.tvNewinvoiceTcstdsCol2.visibility =
                                                    View.GONE
                                            }
                                        }

                                        if (!saleDetailModel.transactionData?.is_tds_applicable.equals(
                                                "1"
                                            ) && !saleDetailModel.transactionData?.is_tcs_applicable.equals(
                                                "1"
                                            )
                                        ) {
                                            binding.radiogroupTDSTCSNewInvoice.visibility =
                                                View.GONE

                                        } else {
                                            binding.radiogroupTDSTCSNewInvoice.visibility =
                                                View.VISIBLE

                                        }


                                        if (saleDetailModel.transactionData?.tds_tcs_enable.equals("tcs")) {
                                            binding.radioTCSNewInvoice.isChecked = true
                                            binding.tvNewinvoiceTcstdsCol2.setText(saleDetailModel.transactionData!!.tcs_amount)

                                            binding.tvNewinvoiceTdstcsCol1.mLabelView!!.setText(
                                                saleDetailModel.transactionData?.tcsData!!.ledger_name
                                            )
                                        }

                                        if (saleDetailModel.transactionData?.tds_tcs_enable.equals("tds")) {
                                            binding.radioTDSNewInvoice.isChecked = true
                                            binding.tvNewinvoiceTcstdsCol2.setText("-" + saleDetailModel.transactionData!!.tds_amount)

                                            binding.tvNewinvoiceTdstcsCol1.mLabelView!!.setText(
                                                saleDetailModel.transactionData?.tdsData!!.ledger_name
                                            )
                                        }


                                    }
                                    false -> {
                                        //for non-gst invoice
                                        binding.tvNewinvoiceSgstCol0.visibility = View.GONE
                                        binding.tvNewinvoiceSgstCol1.visibility = View.GONE
                                        binding.tvNewinvoiceSgstCol2.visibility = View.GONE

                                        binding.tvNewinvoiceCgstCol0.visibility = View.GONE
                                        binding.tvNewinvoiceCgstCol1.visibility = View.GONE
                                        binding.tvNewinvoiceCgstCol2.visibility = View.GONE

                                        binding.tvNewinvoiceIgstCol0.visibility = View.GONE
                                        binding.tvNewinvoiceIgstCol1.visibility = View.GONE
                                        binding.tvNewinvoiceIgstCol2.visibility = View.GONE

                                        binding.radiogroupTDSTCSNewInvoice.visibility = View.GONE
                                        binding.tvNewinvoiceTdstcsCol1.visibility = View.GONE
                                        binding.tvNewinvoiceTcstdsCol2.visibility = View.GONE
                                    }
                                }

                                if (saleDetailModel.transactionData?.is_show_round_off.equals("1")) {
                                    binding.llRoundOff.visibility = View.VISIBLE

                                    binding.tvNewinvoiceRoundOffCol0.visibility = View.VISIBLE
                                    binding.tvNewinvoiceRoundoffCol1.visibility = View.VISIBLE
                                    binding.tvNewinvoiceRoundoffCol2.visibility = View.VISIBLE

                                    binding.tvNewinvoiceRoundoffCol1.mLabelView!!.setText(
                                        saleDetailModel.transactionData?.roundOffLedgerData?.ledger_name
                                    )
                                    binding.tvNewinvoiceRoundoffCol2.setText(Constants.AMOUNT_RS_APPEND + saleDetailModel.transactionData?.round_off_total)

                                }else{
                                    binding.llRoundOff.visibility = View.GONE

                                    binding.tvNewinvoiceRoundOffCol0.visibility = View.GONE
                                    binding.tvNewinvoiceRoundoffCol1.visibility = View.GONE
                                    binding.tvNewinvoiceRoundoffCol2.visibility = View.GONE
                                }
/*
                                when (!saleDetailModel.transactionData?.round_off_total.equals("0.00")) {
                                    true -> {
                                        binding.tvNewinvoiceRoundOffCol0.visibility = View.VISIBLE
                                        binding.tvNewinvoiceRoundoffCol1.visibility = View.VISIBLE
                                        binding.tvNewinvoiceRoundoffCol2.visibility = View.VISIBLE

                                        binding.tvNewinvoiceRoundoffCol1.mLabelView!!.setText(
                                            saleDetailModel.transactionData?.roundOffLedgerData?.ledger_name
                                        )
                                        binding.tvNewinvoiceRoundoffCol2.setText(Constants.AMOUNT_RS_APPEND + saleDetailModel.transactionData?.round_off_total)


                                    }
                                    false -> {
                                        binding.tvNewinvoiceRoundOffCol0.visibility = View.GONE
                                        binding.tvNewinvoiceRoundoffCol1.visibility = View.GONE
                                        binding.tvNewinvoiceRoundoffCol2.visibility = View.GONE
                                    }
                                }
*/

                                addIRTDatainPref()
                                getIssueReceiveDataFromPref()
                                updateOpeningFineOpeningCash(saleDetailModel)
                                updateClosingFineClosingCash(saleDetailModel)

                                if (saleDetailModel.transactionData?.remarks != null) {
                                    binding.tvSalesbilldetailNotes.text =
                                        saleDetailModel.transactionData?.remarks
                                }


                                if (saleDetailModel.transactionData?.image != null && saleDetailModel.transactionData?.image?.size!! > 0
                                ) {
                                    binding.tvAttachmentLabel.visibility = View.VISIBLE
                                    binding.ivSalesbilldetailAttachmentone.visibility = View.VISIBLE
                                    /* imageURL = saleDetailModel.transactionData?.image?.get(0)?.image
                                     Glide.with(this).load(imageURL).circleCrop()
                                         .placeholder(R.drawable.ic_user_placeholder).into(
                                             binding.ivSalesbilldetailAttachmentone
                                         )*/
                                }


                            } else {
                                Log.v("sucessfail", "")
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

    private fun updateUIofTotalDue() {
        if (!saleDetailModel.transactionData?.total_fine_wt_with_IRT.equals("0.000") &&
            saleDetailModel.transactionData?.total_silver_fine_wt_with_IRT.equals("0.000")
        ) {
            binding.llNewinvoiceSilverTotaldue.visibility = View.GONE
            binding.tvNewinvoiceTotaldueGoldLabel.visibility = View.VISIBLE
            binding.tvNewinvoiceTotalDueGold.visibility = View.VISIBLE
        }

        if (!saleDetailModel.transactionData?.total_fine_wt_with_IRT.equals("0.000") &&
            !saleDetailModel.transactionData?.total_silver_fine_wt_with_IRT.equals("0.000")
        ) {
            binding.llNewinvoiceSilverTotaldue.visibility = View.VISIBLE
            binding.tvNewinvoiceTotaldueGoldLabel.visibility = View.VISIBLE
            binding.tvNewinvoiceTotalDueGold.visibility = View.VISIBLE
        }



        if (saleDetailModel.transactionData?.total_fine_wt_with_IRT.equals("0.000") &&
            !saleDetailModel.transactionData?.total_silver_fine_wt_with_IRT.equals("0.000")
        ) {
            binding.llNewinvoiceSilverTotaldue.visibility = View.GONE
            binding.tvNewinvoiceTotaldueGoldLabel.setText("S: ")
            binding.tvNewinvoiceTotalDueGold.setText(saleDetailModel.transactionData?.total_silver_fine_wt_with_IRT)
            when (binding.tvNewinvoiceTotalDueGold.text) {
                "0.000" -> {
                    binding.tvNewinvoiceTotalDueGold.text =
                        saleDetailModel.transactionData?.total_silver_fine_wt_with_IRT
                    binding.tvNewinvoiceTotalDueGold.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.header_black_text
                        )
                    )
                }

                else -> {
                    binding.tvNewinvoiceTotalDueGold.text =
                        saleDetailModel.transactionData?.total_silver_fine_wt_with_IRT + " " +
                                saleDetailModel.transactionData?.total_silver_fine_wt_with_IRT_term
                    if (saleDetailModel.transactionData?.total_silver_fine_wt_with_IRT_short_term.equals(
                            "Dr",
                            ignoreCase = true
                        )
                    ) {
                        binding.tvNewinvoiceTotalDueGold.setTextColor(
                            ContextCompat.getColor(
                                this,
                                R.color.debit_color
                            )
                        )
                    } else
                        binding.tvNewinvoiceTotalDueGold.setTextColor(
                            ContextCompat.getColor(
                                this,
                                R.color.credit_color
                            )
                        )
                }
            }

            when (binding.tvNewinvoiceTotalDueCash.text) {
                "0.00" -> {
                    binding.tvNewinvoiceTotalDueCash.text =
                        saleDetailModel.transactionData?.grand_total
                    binding.tvNewinvoiceTotalDueCash.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.header_black_text
                        )
                    )
                }
                else -> {
                    binding.tvNewinvoiceTotalDueCash.text =
                        saleDetailModel.transactionData?.grand_total + " " +
                                saleDetailModel.transactionData?.grand_total_term
                    if (saleDetailModel.transactionData?.grand_total_short_term.equals(
                            "Dr",
                            ignoreCase = true
                        )
                    ) {
                        binding.tvNewinvoiceTotalDueCash.setTextColor(
                            ContextCompat.getColor(
                                this,
                                R.color.debit_color
                            )
                        )
                    } else
                        binding.tvNewinvoiceTotalDueCash.setTextColor(
                            ContextCompat.getColor(
                                this,
                                R.color.credit_color
                            )
                        )
                }
            }

        } else {
            binding.tvNewinvoiceTotaldueGoldLabel.setText("G: ")
            updateTotalDuewithDrCr(saleDetailModel)
        }

        /*if(!saleDetailModel.transactionData?.total_silver_fine_wt_with_IRT.equals("0.000")){
            binding.llNewinvoiceSilverTotaldue.visibility = View.VISIBLE
        }else{
            binding.llNewinvoiceSilverTotaldue.visibility = View.GONE
        }*/


        if (saleDetailModel.transactionData?.total_fine_wt_with_IRT.equals("0.000") &&
            saleDetailModel.transactionData?.total_silver_fine_wt_with_IRT.equals("0.000")
        ) {
            binding.llNewinvoiceSilverTotaldue.visibility = View.GONE
            binding.tvNewinvoiceTotaldueGoldLabel.visibility = View.GONE
            binding.tvNewinvoiceTotalDueGold.visibility = View.GONE
        } else {
            //  binding.llNewinvoiceSilverTotaldue.visibility = View.VISIBLE
            //  tv_newinvoice_totaldue_gold_label.visibility = View.VISIBLE
            // tv_newinvoice_totalDue_gold.visibility = View.VISIBLE
        }

    }

    private fun updateTotalDuewithDrCr(saleDetailModel: SaleDetailModel.Data) {
        when (binding.tvNewinvoiceTotalDueGold.text) {
            "0.000" -> {
                binding.tvNewinvoiceTotalDueGold.text =
                    saleDetailModel.transactionData?.total_fine_wt_with_IRT
                binding.tvNewinvoiceTotalDueGold.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.header_black_text
                    )
                )
            }

            else -> {
                binding.tvNewinvoiceTotalDueGold.text =
                    saleDetailModel.transactionData?.total_fine_wt_with_IRT + " " +
                            saleDetailModel.transactionData?.total_fine_wt_with_IRT_term
                if (saleDetailModel.transactionData?.total_fine_wt_with_IRT_short_term.equals(
                        "Dr",
                        ignoreCase = true
                    )
                ) {
                    binding.tvNewinvoiceTotalDueGold.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.debit_color
                        )
                    )
                } else
                    binding.tvNewinvoiceTotalDueGold.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.credit_color
                        )
                    )
            }
        }

        when (binding.tvNewinvoiceTotalDueSilver.text) {
            "0.000" -> {
                binding.tvNewinvoiceTotalDueSilver.text =
                    saleDetailModel.transactionData?.total_silver_fine_wt_with_IRT
                binding.tvNewinvoiceTotalDueSilver.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.header_black_text
                    )
                )
            }

            else -> {
                binding.tvNewinvoiceTotalDueSilver.text =
                    saleDetailModel.transactionData?.total_silver_fine_wt_with_IRT + " " +
                            saleDetailModel.transactionData?.total_silver_fine_wt_with_IRT_term
                if (saleDetailModel.transactionData?.total_silver_fine_wt_with_IRT_short_term.equals(
                        "Dr",
                        ignoreCase = true
                    )
                ) {
                    binding.tvNewinvoiceTotalDueSilver.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.debit_color
                        )
                    )
                } else
                    binding.tvNewinvoiceTotalDueSilver.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.credit_color
                        )
                    )
            }
        }

        when (binding.tvNewinvoiceTotalDueCash.text) {
            "0.00" -> {
                binding.tvNewinvoiceTotalDueCash.text =
                    saleDetailModel.transactionData?.grand_total
                binding.tvNewinvoiceTotalDueCash.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.header_black_text
                    )
                )
            }
            else -> {
                binding.tvNewinvoiceTotalDueCash.text =
                    saleDetailModel.transactionData?.grand_total + " " +
                            saleDetailModel.transactionData?.grand_total_term
                if (saleDetailModel.transactionData?.grand_total_short_term.equals(
                        "Dr",
                        ignoreCase = true
                    )
                ) {
                    binding.tvNewinvoiceTotalDueCash.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.debit_color
                        )
                    )
                } else
                    binding.tvNewinvoiceTotalDueCash.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.credit_color
                        )
                    )
            }
        }
    }

    private fun updateTotalDuewithCrDr() {
        when (binding.tvNewinvoiceTotalDueGold.text) {
            "0.000" -> {
                binding.tvNewinvoiceTotalDueGold.text =
                    saleDetailModel.transactionData?.total_fine_wt_with_IRT
                binding.tvNewinvoiceTotalDueGold.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.header_black_text
                    )
                )
            }

            else -> {
                binding.tvNewinvoiceTotalDueGold.text =
                    saleDetailModel.transactionData?.total_fine_wt_with_IRT + " " +
                            saleDetailModel.transactionData?.total_fine_wt_with_IRT_term
                if (saleDetailModel.transactionData?.total_fine_wt_with_IRT_short_term.equals(
                        "Dr",
                        ignoreCase = true
                    )
                ) {
                    binding.tvNewinvoiceTotalDueGold.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.debit_color
                        )
                    )
                } else
                    binding.tvNewinvoiceTotalDueGold.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.credit_color
                        )
                    )
            }
        }

        when (binding.tvNewinvoiceTotalDueSilver.text) {
            "0.000" -> {
                binding.tvNewinvoiceTotalDueSilver.text =
                    saleDetailModel.transactionData?.total_silver_fine_wt_with_IRT
                binding.tvNewinvoiceTotalDueSilver.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.header_black_text
                    )
                )
            }

            else -> {
                binding.tvNewinvoiceTotalDueSilver.text =
                    saleDetailModel.transactionData?.total_silver_fine_wt_with_IRT + " " +
                            saleDetailModel.transactionData?.total_silver_fine_wt_with_IRT_term
                if (saleDetailModel.transactionData?.total_silver_fine_wt_with_IRT_short_term.equals(
                        "Dr",
                        ignoreCase = true
                    )
                ) {
                    binding.tvNewinvoiceTotalDueSilver.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.debit_color
                        )
                    )
                } else
                    binding.tvNewinvoiceTotalDueSilver.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.credit_color
                        )
                    )
            }
        }

        when (binding.tvNewinvoiceTotalDueCash.text) {
            "0.00" -> {
                binding.tvNewinvoiceTotalDueCash.text =
                    saleDetailModel.transactionData?.grand_total
                binding.tvNewinvoiceTotalDueCash.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.header_black_text
                    )
                )
            }
            else -> {
                binding.tvNewinvoiceTotalDueCash.text =
                    saleDetailModel.transactionData?.grand_total + " " +
                            saleDetailModel.transactionData?.grand_total_term
                if (saleDetailModel.transactionData?.grand_total_short_term.equals(
                        "Dr",
                        ignoreCase = true
                    )
                ) {
                    binding.tvNewinvoiceTotalDueCash.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.debit_color
                        )
                    )
                } else
                    binding.tvNewinvoiceTotalDueCash.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.credit_color
                        )
                    )
            }
        }
    }

    private fun updateClosingFineClosingCash(saleDetailModel: SaleDetailModel.Data) {
        if (saleDetailModel.transactionData?.closing_fine_balance!!.startsWith("-")) {
            val open_fine_bal: String =
                saleDetailModel.transactionData?.closing_fine_balance.toString().trim().substring(1)
            binding.tvCloBalFineWtDetailInvoice.text = open_fine_bal
        } else {
            binding.tvCloBalFineWtDetailInvoice.text =
                saleDetailModel.transactionData?.closing_fine_balance
        }

        when (binding.tvCloBalFineWtDetailInvoice.text) {
            "0.000" -> {
                binding.tvCloBalFineWtDetailInvoice.text =
                    saleDetailModel.transactionData?.closing_fine_balance
                binding.tvCloBalFineWtDetailInvoice.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.header_black_text
                    )
                )
            }

            else -> {
                binding.tvCloBalFineWtDetailInvoice.text =
                    saleDetailModel.transactionData?.closing_fine_balance + " " +
                            saleDetailModel.transactionData?.closing_fine_balance_term
                if (saleDetailModel.transactionData?.closing_fine_balance_short_term.equals(
                        "Dr",
                        ignoreCase = true
                    )
                ) {
                    binding.tvCloBalFineWtDetailInvoice.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.debit_color
                        )
                    )
                } else
                    binding.tvCloBalFineWtDetailInvoice.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.credit_color
                        )
                    )
            }
        }


        if (saleDetailModel.transactionData?.closing_silver_fine_balance!!.startsWith("-")) {
            val open_silver_fine_bal: String =
                saleDetailModel.transactionData?.closing_silver_fine_balance.toString().trim()
                    .substring(1)
            binding.tvCloBalSilverDetailInvoice.text = open_silver_fine_bal
        } else {
            binding.tvCloBalSilverDetailInvoice.text =
                saleDetailModel.transactionData?.closing_silver_fine_balance
        }

        when (binding.tvCloBalSilverDetailInvoice.text) {
            "0.000" -> {
                binding.tvCloBalSilverDetailInvoice.text =
                    saleDetailModel.transactionData?.closing_silver_fine_balance
                binding.tvCloBalSilverDetailInvoice.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.header_black_text
                    )
                )
            }

            else -> {
                binding.tvCloBalSilverDetailInvoice.text =
                    saleDetailModel.transactionData?.closing_silver_fine_balance + " " +
                            saleDetailModel.transactionData?.closing_silver_fine_balance_term
                if (saleDetailModel.transactionData?.closing_silver_fine_balance_short_term.equals(
                        "Dr",
                        ignoreCase = true
                    )
                ) {
                    binding.tvCloBalSilverDetailInvoice.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.debit_color
                        )
                    )
                } else
                    binding.tvCloBalSilverDetailInvoice.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.credit_color
                        )
                    )
            }
        }

        if (saleDetailModel.transactionData?.closing_cash_balance!!.startsWith("-")) {
            val open_cash_bal: String =
                saleDetailModel.transactionData?.closing_cash_balance.toString().trim().substring(1)
            binding.tvCloBalCashDetailInvoice.text = open_cash_bal
        } else {
            binding.tvCloBalCashDetailInvoice.text =
                saleDetailModel.transactionData?.closing_cash_balance
        }

        when (binding.tvCloBalCashDetailInvoice.text) {
            "0.00" -> {
                binding.tvCloBalCashDetailInvoice.text =
                    saleDetailModel.transactionData?.closing_cash_balance
                binding.tvCloBalCashDetailInvoice.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.header_black_text
                    )
                )
            }
            else -> {
                binding.tvCloBalCashDetailInvoice.text =
                    saleDetailModel.transactionData?.closing_cash_balance + " " +
                            saleDetailModel.transactionData?.closing_cash_balance_term
                if (saleDetailModel.transactionData?.closing_cash_balance_short_term.equals(
                        "Dr",
                        ignoreCase = true
                    )
                ) {
                    binding.tvCloBalCashDetailInvoice.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.debit_color
                        )
                    )
                } else
                    binding.tvCloBalCashDetailInvoice.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.credit_color
                        )
                    )
            }


        }
    }

    private fun updateOpeningFineOpeningCash(saleDetailModel: SaleDetailModel.Data) {
        if (saleDetailModel.transactionData?.opening_fine_balance!!.startsWith("-")) {
            val open_fine_bal: String =
                saleDetailModel.transactionData?.opening_fine_balance.toString().trim().substring(1)
            binding.tvOpenBalFineDetailInvoice.text = open_fine_bal
        } else {
            binding.tvOpenBalFineDetailInvoice.text =
                saleDetailModel.transactionData?.opening_fine_balance
        }


        when (binding.tvOpenBalFineDetailInvoice.text) {
            "0.000" -> {
                binding.tvOpenBalFineDetailInvoice.text =
                    saleDetailModel.transactionData?.opening_fine_balance
                binding.tvOpenBalFineDetailInvoice.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.header_black_text
                    )
                )
            }

            else -> {
                binding.tvOpenBalFineDetailInvoice.text =
                    saleDetailModel.transactionData?.opening_fine_balance + " " +
                            saleDetailModel.transactionData?.opening_fine_balance_term
                if (saleDetailModel.transactionData?.opening_fine_balance_short_term.equals(
                        "Dr",
                        ignoreCase = true
                    )
                ) {
                    binding.tvOpenBalFineDetailInvoice.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.debit_color
                        )
                    )
                } else
                    binding.tvOpenBalFineDetailInvoice.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.credit_color
                        )
                    )
            }
        }

        if (saleDetailModel.transactionData?.opening_silver_fine_balance!!.startsWith("-")) {
            val open_silver_fine_bal: String =
                saleDetailModel.transactionData?.opening_silver_fine_balance.toString().trim()
                    .substring(1)
            binding.tvOpenBalFinesilverDetailInvoice.text = open_silver_fine_bal
        } else {
            binding.tvOpenBalFinesilverDetailInvoice.text =
                saleDetailModel.transactionData?.opening_silver_fine_balance
        }

        when (binding.tvOpenBalFinesilverDetailInvoice.text) {
            "0.000" -> {
                binding.tvOpenBalFinesilverDetailInvoice.text =
                    saleDetailModel.transactionData?.opening_silver_fine_balance
                binding.tvOpenBalFinesilverDetailInvoice.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.header_black_text
                    )
                )
            }

            else -> {
                binding.tvOpenBalFinesilverDetailInvoice.text =
                    saleDetailModel.transactionData?.opening_silver_fine_balance + " " +
                            saleDetailModel.transactionData?.opening_silver_fine_balance_term
                if (saleDetailModel.transactionData?.opening_silver_fine_balance_short_term.equals(
                        "Dr",
                        ignoreCase = true
                    )
                ) {
                    binding.tvOpenBalFinesilverDetailInvoice.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.debit_color
                        )
                    )
                } else
                    binding.tvOpenBalFinesilverDetailInvoice.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.credit_color
                        )
                    )
            }
        }

        if (saleDetailModel.transactionData?.opening_cash_balance!!.startsWith("-")) {
            val open_cash_bal: String =
                saleDetailModel.transactionData?.opening_cash_balance.toString().trim().substring(1)
            binding.tvOpenBalCashDetailInvoice.text = open_cash_bal
        } else {
            binding.tvOpenBalCashDetailInvoice.text =
                saleDetailModel.transactionData?.opening_cash_balance
        }

        when (binding.tvOpenBalCashDetailInvoice.text) {
            "0.00" -> {
                binding.tvOpenBalCashDetailInvoice.text =
                    saleDetailModel.transactionData?.opening_cash_balance
                binding.tvOpenBalCashDetailInvoice.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.header_black_text
                    )
                )
            }
            else -> {
                binding.tvOpenBalCashDetailInvoice.text =
                    saleDetailModel.transactionData?.opening_cash_balance + " " +
                            saleDetailModel.transactionData?.opening_cash_balance_term
                if (saleDetailModel.transactionData?.opening_cash_balance_short_term.equals(
                        "Dr",
                        ignoreCase = true
                    )
                ) {
                    binding.tvOpenBalCashDetailInvoice.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.debit_color
                        )
                    )
                } else
                    binding.tvOpenBalCashDetailInvoice.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.credit_color
                        )
                    )
            }


        }
    }


    private fun addIRTDatainPref() {
        salesLineList.clear()
        for (i in 0 until saleDetailModel.IRTData!!.size) {

            if (!saleDetailModel.IRTData!!.get(i).transaction_type.equals("")) {
                val saleIRTModel = SalesLineModel.SaleLineModelDetails(
                    saleDetailModel.IRTData!!.get(i).IRTDetails!!.cash_amount,
                    saleDetailModel.IRTData!!.get(i).IRTDetails!!.cash_ledger,
                    saleDetailModel.IRTData!!.get(i).IRTDetails!!.cash_ledger_name,
                    saleDetailModel.IRTData!!.get(i).IRTDetails!!.cash_description,
                    saleDetailModel.IRTData!!.get(i).IRTDetails!!.bank_amount,
                    saleDetailModel.IRTData!!.get(i).IRTDetails!!.bank_ledger,
                    saleDetailModel.IRTData!!.get(i).IRTDetails!!.bank_ledger_name,
                    saleDetailModel.IRTData!!.get(i).IRTDetails!!.bank_mode,
                    saleDetailModel.IRTData!!.get(i).IRTDetails!!.cheque_number,
                    saleDetailModel.IRTData!!.get(i).IRTDetails!!.cheque_date,
                    saleDetailModel.IRTData!!.get(i).IRTDetails!!.favouring_name,
                    saleDetailModel.IRTData!!.get(i).IRTDetails!!.deuct_charges,
                    saleDetailModel.IRTData!!.get(i).IRTDetails!!.deuct_charges_percentage,
                    saleDetailModel.IRTData!!.get(i).IRTDetails!!.bank_final_amt,
                    saleDetailModel.IRTData!!.get(i).IRTDetails!!.recipient_bank,
                    saleDetailModel.IRTData!!.get(i).IRTDetails!!.account_no,
                    saleDetailModel.IRTData!!.get(i).IRTDetails!!.ifs_code,
                    saleDetailModel.IRTData!!.get(i).IRTDetails!!.utr_number,
                    saleDetailModel.IRTData!!.get(i).IRTDetails!!.bank_description,
                    saleDetailModel.IRTData!!.get(i).IRTDetails!!.rcm_gold_rate,
                    saleDetailModel.IRTData!!.get(i).IRTDetails!!.rate_cut_amount,
                    saleDetailModel.IRTData!!.get(i).IRTDetails!!.rate_cut_fine_term,
                    saleDetailModel.IRTData!!.get(i).IRTDetails!!.metal_type_id_rate_cut,
                    saleDetailModel.IRTData!!.get(i).IRTDetails!!.rate_cut_fine,
                    saleDetailModel.IRTData!!.get(i).IRTDetails!!.item_id,
                    saleDetailModel.IRTData!!.get(i).IRTDetails!!.item_name,
                    saleDetailModel.IRTData!!.get(i).IRTDetails!!.metal_type_id_metal,
                    saleDetailModel.IRTData!!.get(i).IRTDetails!!.maintain_stock_in_name_metal,
                    saleDetailModel.IRTData!!.get(i).IRTDetails!!.gross_wt,
                    saleDetailModel.IRTData!!.get(i).IRTDetails!!.less_wt,
                    saleDetailModel.IRTData!!.get(i).IRTDetails!!.net_wt,
                    saleDetailModel.IRTData!!.get(i).IRTDetails!!.touch,
                    saleDetailModel.IRTData!!.get(i).IRTDetails!!.wast,
                    saleDetailModel.IRTData!!.get(i).IRTDetails!!.fine_wt,
                    "", "", "", "", "", "",
                    saleDetailModel.IRTData!!.get(i).IRTDetails!!.type,
                    saleDetailModel.IRTData!!.get(i).IRTDetails!!.transaction_type,
                    saleDetailModel.IRTData!!.get(i).IRTDetails!!.transaction_title

                )

                salesLineList.add(saleIRTModel)
            }
        }
        prefs[Constants.PREF_SALES_LINE_INFO_KEY] = Gson().toJson(salesLineList)

    }


    private fun getIssueReceiveDataFromPref() {
        if (prefs.contains(Constants.PREF_SALES_LINE_INFO_KEY)) {
            val collectionType =
                object :
                    TypeToken<ArrayList<SalesLineModel.SaleLineModelDetails>>() {}.type
            salesLineList =
                Gson().fromJson(
                    prefs[Constants.PREF_SALES_LINE_INFO_KEY, ""],
                    collectionType
                )
            setupIssueReceiveAdapter()
        }
    }

    private fun setupIssueReceiveAdapter() {
        when (salesLineList.size > 0) {
            true -> {
                binding.rvIssueReceiveList.visibility = View.VISIBLE
                issueReceiveadapter.apply {
                    addissueReceiveList(salesLineList)
                    notifyDataSetChanged()
                }
            }
            else -> {

            }
        }

    }

    fun voucherTextAPI(
        token: String?,
        transaction_id: String?
    ) {

        if (NetworkUtils.isConnected()) {

            viewModel.voucherText(token, "sales", transaction_id).observe(this, Observer {
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

            Log.i("Send SMS", "")
            val smsIntent = Intent(Intent.ACTION_VIEW)
            smsIntent.type = "vnd.android-dir/mms-sms"
            smsIntent.putExtra("sms_body", txtMsg)
            try {
                startActivity(smsIntent)
                Log.i("Finished sending SMS...", "")
            } catch (ex: ActivityNotFoundException) {
                Toast.makeText(
                    this@SalesBillDetailActivity,
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


    private fun retrieveListforitem(salebillitemlist: List<SaleDetailModel.Item1427117511>) {
        adapter.apply {
            addsalebillrow_item(salebillitemlist)
            notifyDataSetChanged()
        }
        var item_quantity_unit: StringBuilder = StringBuilder()
        var count: Int = 0
        for (item in salebillitemlist!!) {
            if (count == salebillitemlist.size - 1) {
                item_quantity_unit.append(item.item_quantity?.toString())
                    .append(" ").append(item.item_unit_name?.trim().toString())
//                tv_salesbilldetail_items_desc.visibility = View.GONE
            } else {
                item_quantity_unit.append(item.item_quantity?.toString())
                    .append(" ").append(item.item_unit_name?.trim().toString()).append(", ")

            }
            when (item.item_quantity!!.toInt() > 0) {
                true -> {
                    tv_salesbilldetail_items_desc.visibility = View.VISIBLE
                    tv_salesbilldetail_items_desc.text = item_quantity_unit.toString()
                }
                false -> {
                    tv_salesbilldetail_items_desc.visibility = View.GONE
                }
            }

            count++
        }


    }

    fun showFullImage(imgpath: String?) {
        val builder = Dialog(this)
        builder.requestWindowFeature(Window.FEATURE_NO_TITLE)
        builder.getWindow()?.setBackgroundDrawable(
            ColorDrawable(TRANSPARENT)
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


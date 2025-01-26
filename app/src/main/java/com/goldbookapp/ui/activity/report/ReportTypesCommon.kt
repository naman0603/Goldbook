package com.goldbookapp.ui.activity.report

import android.Manifest
import android.app.DatePickerDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.database.Cursor
import android.graphics.Color
import android.net.Uri
import android.os.*
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintJob
import android.print.PrintManager
import android.provider.MediaStore
import android.view.Gravity
import android.view.Menu
import android.view.View
import android.widget.ArrayAdapter
import android.widget.PopupMenu
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.NetworkUtils
import com.goldbookapp.BuildConfig
import com.goldbookapp.R
import com.goldbookapp.api.ApiHelper
import com.goldbookapp.api.ApiService
import com.goldbookapp.api.RetrofitBuilder
import com.goldbookapp.databinding.ActivityReporttypesCommonBinding
import com.goldbookapp.model.*
import com.goldbookapp.permissions.PermissionHandler
import com.goldbookapp.permissions.Permissions
import com.goldbookapp.ui.activity.PdfDocumentAdapter
import com.goldbookapp.ui.activity.PrintJobMonitorService
import com.goldbookapp.ui.activity.viewmodel.ReportTypesCommonViewModel
import com.goldbookapp.ui.activity.viewmodel.ViewModelFactory
import com.goldbookapp.ui.adapter.CashBankPrintAdapter
import com.goldbookapp.ui.adapter.ContactReportPrintAdapter
import com.goldbookapp.ui.adapter.DayReportPrintAdapter
import com.goldbookapp.ui.adapter.SalePurchaseReportPrintAdapter
import com.goldbookapp.utils.CommonUtils
import com.goldbookapp.utils.CommonUtils.Companion.clickWithDebounce
import com.goldbookapp.utils.CommonUtils.Companion.hideKeyboardnew
import com.goldbookapp.utils.Constants
import com.goldbookapp.utils.PreferenceHelper
import com.goldbookapp.utils.PreferenceHelper.get
import com.goldbookapp.utils.Status
import com.google.gson.Gson
import com.hamzasharuf.networkmonitor.ConnectivityStateHolder
import com.hamzasharuf.networkmonitor.Event
import com.hamzasharuf.networkmonitor.NetworkEvents
import kotlinx.android.synthetic.main.activity_reporttypes_common.*
import kotlinx.android.synthetic.main.toolbar_layout.*
import kotlinx.android.synthetic.main.toolbar_layout.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.collections.ArrayList


class ReportTypesCommon : AppCompatActivity() {
    var isFromDateSelected: Boolean = false
    lateinit var data: UserWiseRestrictionModel.Data
    val c = Calendar.getInstance()
    var reportsTrackNo: String? = "0"
    var selectedPeriod: String? = null

    var item_category_id: String? = null
    var item_id: String? = null
    var cash_ledger_id: String? = null
    var bank_ledger_id: String? = null
    var tcs_ledger_id: String? = null

    var filename: String = ""
    var fileSavePath = ""
    var all_item_categories: String? = "0"
    var all_items: String? = "0"
    var all_contacts: String? = "0"
    var all_cash_ledgers: String? = "0"
    var all_bank_ledgers: String? = "0"
    var all_ledgers: String? = "0"

    //if type=""
    var allLedgerType: String = ""

    var reportApiCallType: String = "download"
    var job: GlobalScope? = null

    private lateinit var notificationBuilder: NotificationCompat.Builder
    private lateinit var notificationManager: NotificationManager

    private lateinit var itemCatList: List<ReportsItemCategoryModel.Categories>
    private lateinit var itemsList: List<ReportsItemModel.Items>
    var itemCatNameList: List<String>? = null
    var itemNameList: List<String>? = null
    var reportcontactNameList: List<String>? = null
    var reportcontactList: List<ReportSupportContactsModel.Contacts>? = null

    private var mgr: PrintManager? = null

    var type_of_contact: String? = "customer"

    var isFromThread: Boolean = true
    var isFromGenerateThread: Boolean = true
    var isFromDownload: Boolean = false

    private lateinit var adapter: SalePurchaseReportPrintAdapter
    private lateinit var contactReportadapter: ContactReportPrintAdapter
    private lateinit var dayReportadapter: DayReportPrintAdapter
    lateinit var cashBankAdapter: CashBankPrintAdapter


    lateinit var contactNameAdapter: ArrayAdapter<String>
    lateinit var categoryNameAdapter: ArrayAdapter<String>
    lateinit var itemNameAdapter: ArrayAdapter<String>

    // all for bank name variables
    lateinit var cashNameAdapter: ArrayAdapter<String>
    lateinit var bankNameAdapter: ArrayAdapter<String>
    lateinit var tcsNameAdapter: ArrayAdapter<String>


    private lateinit var type: String
    var bankledgerDetailsList: ArrayList<SearchLedgerModel.LedgerDetails>? = null
    var cashledgerDetailsList: ArrayList<SearchLedgerModel.LedgerDetails>? = null
    var tcsledgerDetailsList: ArrayList<SearchLedgerModel.LedgerDetails>? = null
    var bankledgerNameList: List<String>? = null
    var cashledgerNameList: List<String>? = null
    var tcsledgerNameList: List<String>? = null


    var selectedContactID: String? = ""

    val PROGRESS_UPDATE = "progress_update"
    private val PERMISSION_REQUEST_CODE = 1

    private var storeparse = Date()
    var reportsType: String? = "contact"

    //var contactAllcontactsisChecked: Boolean = false
    lateinit var prefs: SharedPreferences
    lateinit var loginModel: LoginModel
    lateinit var reportGeneratePDFModel: ReportGeneratePDFModel
    private lateinit var viewModel: ReportTypesCommonViewModel
    lateinit var binding: ActivityReporttypesCommonBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_reporttypes_common)


        setupViewModel()
        setupUIandListner()
        if (intent.extras != null && intent.extras!!.containsKey(Constants.ReportsTrackNo)) {
            reportsTrackNo = intent.getStringExtra(Constants.ReportsTrackNo)
            visibleUIAccordingToPaymentRowNo(reportsTrackNo)
        }
        // common for all(6 report types)
        btnRepoTGenerateReport.clickWithDebounce {

            // reportApiCallType = "generate"
            isFromDownload = true
            reportApiCallType = "download"
            /*if (checkPermission()) {

                callToCommon(reportApiCallType)

            } else {
                requestPermission()
            }*/
            checkandRequestPermission()
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        CommonUtils.hideProgress()
    }

    override fun onResume() {
        super.onResume()

        NetworkEvents.observe(
            this, androidx.lifecycle.Observer {
                if (it is Event.ConnectivityEvent) {
                    handleConnectivityChange()
                }
            }
        )

    }

    private fun handleConnectivityChange() {
        if (ConnectivityStateHolder.isConnected) {
            // Network is available
            CommonUtils.hideInternetDialog()
            when (loginModel.data!!.user_info!!.user_type.equals("user", true)) {
                // user type user
                true -> {
                    // apply restriciton to "user" ignore for "admin/superadmin" etc
                    defaultDisableAllButtonnUI()
                    userWiseRestriction(loginModel.data?.bearer_access_token)
                }
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
        binding.root.imgRight.visibility = View.GONE
        binding.root.imgRight2.visibility = View.GONE
    }

    private fun defaultEnableAllButtonnUI() {
        binding.root.imgRight.visibility = View.VISIBLE
        binding.root.imgRight2.visibility = View.VISIBLE
    }

    private fun userWiseRestriction(token: String?) {
        if (NetworkUtils.isConnected()) {

            viewModel.userWiseRestriction(loginModel?.data?.bearer_access_token)
                .observe(this, androidx.lifecycle.Observer {
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
        this.data = data
        for (i in 0 until data.permission!!.size) {
            if (data.permission!!.get(i).contains(getString(R.string.reprt))) {
                when (reportsType) {

                    "contact" -> {
                        if (data.permission!!.get(i)
                                .startsWith(getString(R.string.ledger_statement))
                        ) {
                            when (data.permission!!.get(i)
                                .endsWith(getString(R.string.report_print_download), true)) {
                                true -> {
                                    binding.root.imgRight2.visibility = View.VISIBLE
                                    binding.root.imgRight.visibility = View.VISIBLE
                                }
                                else -> {

                                }
                            }
                        }
                    }
                    "summary_contact" -> {
                        if (data.permission!!.get(i)
                                .startsWith(getString(R.string.ledger_statement))
                        ) {
                            when (data.permission!!.get(i)
                                .endsWith(getString(R.string.report_print_download), true)) {
                                true -> {
                                    binding.root.imgRight2.visibility = View.VISIBLE
                                    binding.root.imgRight.visibility = View.VISIBLE
                                }
                                else -> {

                                }
                            }
                        }
                    }
                    "stock" -> {
                        if (data.permission!!.get(i)
                                .startsWith(getString(R.string.stock_statement))
                        ) {
                            when (data.permission!!.get(i)
                                .endsWith(getString(R.string.report_print_download), true)) {
                                true -> {
                                    binding.root.imgRight2.visibility = View.VISIBLE
                                    binding.root.imgRight.visibility = View.VISIBLE
                                }
                                else -> {

                                }
                            }
                        }
                    }
                    "sales" -> {
                        if (data.permission!!.get(i).startsWith(getString(R.string.sales_))) {
                            when (data.permission!!.get(i)
                                .endsWith(getString(R.string.report_print_download), true)) {
                                true -> {
                                    binding.root.imgRight2.visibility = View.VISIBLE
                                    binding.root.imgRight.visibility = View.VISIBLE
                                }
                                else -> {

                                }
                            }
                        }
                    }
                    "purchase" -> {
                        if (data.permission!!.get(i).startsWith(getString(R.string.purchase_))) {
                            when (data.permission!!.get(i)
                                .endsWith(getString(R.string.report_print_download), true)) {
                                true -> {
                                    binding.root.imgRight2.visibility = View.VISIBLE
                                    binding.root.imgRight.visibility = View.VISIBLE
                                }
                                else -> {

                                }
                            }
                        }
                    }
                    "day" -> {
                        if (data.permission!!.get(i).startsWith(getString(R.string.day_book_))) {
                            when (data.permission!!.get(i)
                                .endsWith(getString(R.string.report_print_download), true)) {
                                true -> {
                                    binding.root.imgRight2.visibility = View.VISIBLE
                                    binding.root.imgRight.visibility = View.VISIBLE
                                }
                                else -> {

                                }
                            }
                        }
                    }
                    "cashbank" -> {
                        if (data.permission!!.get(i).startsWith(getString(R.string.cash_book_))) {
                            when (data.permission!!.get(i)
                                .endsWith(getString(R.string.report_print_download), true)) {
                                true -> {
                                    binding.root.imgRight2.visibility = View.VISIBLE
                                    binding.root.imgRight.visibility = View.VISIBLE
                                }
                                else -> {

                                }
                            }
                        }
                    }
                    "ledger" -> {
                        if (data.permission!!.get(i).contains(getString(R.string.ledgr_report))) {
                            when (data.permission!!.get(i)
                                .endsWith(getString(R.string.report_print_download), true)) {
                                true -> {
                                    binding.root.imgRight2.visibility = View.VISIBLE
                                    binding.root.imgRight.visibility = View.VISIBLE
                                }
                                else -> {

                                }
                            }
                        }
                    }
                }
            }

        }
    }


    private fun callToCommon(reportApiCallType: String) {

        val SDK_INT = Build.VERSION.SDK_INT
        if (SDK_INT > 21) {
            val policy: StrictMode.ThreadPolicy = StrictMode.ThreadPolicy.Builder()
                .permitAll().build()
            StrictMode.setThreadPolicy(policy)
        }
        GlobalScope.launch(Dispatchers.Main) {
            when (reportApiCallType) {
                "download" -> calltoDownloadReport()
                "generate" -> calltoGenerateReport()
            }
        }


    }

    private fun calltoDownloadReport() {
        //progressBar_generated_report.visibility = View.VISIBLE


        downloadReport(reportsType)

    }

    private fun calltoGenerateReport() {
        ll_generated_report_ns.visibility = View.VISIBLE

        generateReport(reportsType)

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
                .setContentTitle("${filename} Report downloaded")
                .setContentText("Tap to Open PDF")
                .setAutoCancel(true)
                .setContentIntent(contentIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            notificationManager.notify(1, notificationBuilder.build())
        } catch (e: Exception) {

        }
    }

    private fun isNotificationVisible(): Boolean {
        val notificationIntent = Intent(this, ReportTypesCommon::class.java)
        val test = PendingIntent.getActivity(
            this,
            1,
            notificationIntent,
            PendingIntent.FLAG_NO_CREATE
        )
        return test != null
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
                    when (reportsType) {
                        "contact" -> values.put(
                            MediaStore.Downloads.RELATIVE_PATH,
                            "Download/" + "GoldBook/" + "Reports/" + "Contact"
                        );
                        "summary_contact" -> values.put(
                            MediaStore.Downloads.RELATIVE_PATH,
                            "Download/" + "GoldBook/" + "Reports/" + "SummaryContact"
                        );
                        "total_stock_summary" -> values.put(
                            MediaStore.Downloads.RELATIVE_PATH,
                            "Download/" + "GoldBook/" + "Reports/" + "Stock"
                        );
                        "sales" -> values.put(
                            MediaStore.Downloads.RELATIVE_PATH,
                            "Download/" + "GoldBook/" + "Reports/" + "Invoice"
                        );
                        "purchase" -> values.put(
                            MediaStore.Downloads.RELATIVE_PATH,
                            "Download/" + "GoldBook/" + "Reports/" + "Receipt"
                        );
                        "day" -> values.put(
                            MediaStore.Downloads.RELATIVE_PATH,
                            "Download/" + "GoldBook/" + "Reports/" + "Day"
                        );
                        "cashbank" -> values.put(
                            MediaStore.Downloads.RELATIVE_PATH,
                            "Download/" + "GoldBook/" + "Reports/" + "Cashbank"
                        );
                        "ledger" -> values.put(
                            MediaStore.Downloads.RELATIVE_PATH,
                            "Download/" + "GoldBook/" + "Reports/" + "Ledger"
                        );
                        "tagged_stock_summary" -> values.put(
                            MediaStore.Downloads.RELATIVE_PATH,
                            "Download/" + "GoldBook/" + "Reports/" + "TaggedStock"
                        );
                        "stock_item_details" -> values.put(
                            MediaStore.Downloads.RELATIVE_PATH,
                            "Download/" + "GoldBook/" + "Reports/" + "ItemStock"
                        );
                    }
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
                val root = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    "GoldBook"
                )
                val rootDirtory = File(root, "Reports")
                var myDirectory = File(rootDirtory, "Contact")
                when (reportsType) {
                    "contact" -> myDirectory = File(
                        rootDirtory,
                        "Contact"
                    )
                    "summary_contact" -> myDirectory = File(
                        rootDirtory,
                        "SummaryContact"
                    )
                    "total_stock_summary" -> myDirectory = File(
                        rootDirtory,
                        "Stock"
                    )
                    "sales" -> myDirectory = File(
                        rootDirtory,
                        "Invoice"
                    )
                    "purchase" -> myDirectory = File(
                        rootDirtory,
                        "Receipt"
                    )
                    "day" -> myDirectory = File(rootDirtory, "Day")
                    "cashbank" -> myDirectory = File(
                        rootDirtory,
                        "Cashbank"
                    )
                    "ledger" -> myDirectory = File(
                        rootDirtory,
                        "Ledger"
                    )
                    "tagged_stock_summary" -> myDirectory = File(
                        rootDirtory,
                        "TaggedStock"
                    )
                    "stock_item_details" -> myDirectory = File(
                        rootDirtory,
                        "ItemStock"
                    )
                }

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


    override fun onBackPressed() {
        // super.onBackPressed()
        imgRight2.visibility = View.GONE
        imgRight.visibility = View.GONE
        if (ll_generated_report_ns.visibility == View.VISIBLE) {
            rlRoot_report_common.visibility = View.VISIBLE
            ll_generated_report_ns.visibility = View.GONE

        } else {
            finish()
        }


    }

    private fun visibleUIAccordingToPaymentRowNo(reportsTrackNo: String?) {
        rlRoot_report_common.visibility = View.VISIBLE
        when (reportsTrackNo) {
            // Contact A/c Statement
            "1" -> {
                tvTitle.setText(R.string.contactaccountstatement)
                llRepoTContact.visibility = View.VISIBLE
                reportsType = "contact"
                type_of_contact = ""
                showContactsFieldsnSetCode()


            }
            // stock statement
            "2" -> {
                tvTitle.setText(R.string.stockstatement)
                llRepoTStock.visibility = View.VISIBLE
                reportsType = "total_stock_summary"
                type_of_contact = ""
                selectedContactID = ""
                showStockFieldsnSetCode()
            }

            // sales register
            "3" -> {
                tvTitle.setText(R.string.salesregister)
                llRepoTSale.visibility = View.VISIBLE
                reportsType = "sales"
                showSalesFieldsnSetCode()
            }
            // purchase register
            "4" -> {
                tvTitle.setText(R.string.purchaseregister)
                llRepoTPurchase.visibility = View.VISIBLE
                reportsType = "purchase"
                showPurchaseCutFieldsnSetCode()
            }
            // daybook
            "5" -> {
                tvTitle.setText(R.string.daybook)
                llRepoTDay.visibility = View.VISIBLE
                reportsType = "day"
                type_of_contact = ""
                selectedContactID = ""
                showDaybookFieldsnSetCode()
            }
            // cashbankbook
            "6" -> {
                tvTitle.setText(R.string.cashbankbook)
                llRepoTCashBank.visibility = View.VISIBLE
                reportsType = "cashbank"
                type_of_contact = ""
                selectedContactID = ""
                showCashbookFieldsnSetCode()
            }
            //Ledger
            "7" -> {
                tvTitle.setText(R.string.ledgerReport)
                llRepoTLedger.visibility = View.VISIBLE
                reportsType = "ledger"
                type_of_contact = ""
                selectedContactID = ""
                showLedgerFieldsnSetCode()
            }
            "8" -> {
                tvTitle.setText(R.string.tagStockReport)
                llRepoTStock.visibility = View.VISIBLE
                reportsType = "tagged_stock_summary"
                type_of_contact = ""
                selectedContactID = ""
                showTaggedStockFieldsnSetCode()
            }
            "9" -> {
                tvTitle.setText(R.string.stockreport)
                llRepoTStock.visibility = View.VISIBLE
                reportsType = "stock_item_details"
                type_of_contact = ""
                selectedContactID = ""
                showItemStockFieldsnSetCode()
            }
            "10" -> {
                tvTitle.setText(R.string.summarycontactaccountstatement)
                llRepoTContact.visibility = View.VISIBLE
                reportsType = "summary_contact"
                type_of_contact = ""
                showContactsFieldsnSetCode()


            }
        }

    }

    private fun showItemStockFieldsnSetCode() {
        ly_stock_all_item.visibility = View.VISIBLE
        tvRepoTSelItems.visibility = View.VISIBLE
        txtRepoTStockFromDate.isEnabled = true
        txtRepoTStockToDate.isEnabled = true

        callItemsApiAgain()
        switchAllItems.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                all_items = "1"
                callItemsApiAgain()
                txtRepoTSelItems.setText("")
                tvRepoTSelItems.visibility = View.GONE

            } else {
                all_items = "0"
                callItemsApiAgain()
                tvRepoTSelItems.visibility = View.VISIBLE

            }
        }

        // set current month first and last date default (in from and to date)
        txtRepoTStockFromDate.setText(SimpleDateFormat("dd-MMM-yy").format(getFirstDateOfMonth(Date())))
        txtRepoTStockToDate.setText(SimpleDateFormat("dd-MMM-yy").format(getLastDateOfMonth(Date())))
        txtRepoTStockFromDate.clickWithDebounce {

            openDatePicker(reportsType, true)
        }
        txtRepoTStockToDate.clickWithDebounce {

            openDatePicker(reportsType, false)
        }
        llRepoTStock.setOnFocusChangeListener(View.OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                hideKeyboardnew(this);
            }
        })
    }

    private fun showTaggedStockFieldsnSetCode() {
        ly_stock_all_item.visibility = View.GONE
        tvRepoTSelItems.visibility = View.GONE
        txtRepoTStockFromDate.isEnabled = false
        txtRepoTStockToDate.isEnabled = false
        // set current month first and last date default (in from and to date)
        txtRepoTStockFromDate.setText(SimpleDateFormat("dd-MMM-yy").format(Date()))
        txtRepoTStockToDate.setText(SimpleDateFormat("dd-MMM-yy").format(Date()))

        llRepoTStock.setOnFocusChangeListener(View.OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                hideKeyboardnew(this);
            }
        })
    }


    private fun setupViewModel() {
        viewModel =
            ViewModelProvider(this, ViewModelFactory(ApiHelper(RetrofitBuilder.apiService))).get(
                ReportTypesCommonViewModel::class.java
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

        mgr = getSystemService(Context.PRINT_SERVICE) as PrintManager

        imgLeft?.clickWithDebounce {

            onBackPressed()
        }


    }

    private fun getReportSupportContacts(searchContactInputString: String) {
        reportcontactList = ArrayList<ReportSupportContactsModel.Contacts>()
        reportcontactNameList = ArrayList<String>()
        viewModel.getSearchContacts(
            loginModel.data?.bearer_access_token,
            loginModel?.data?.company_info?.id,
            searchContactInputString,
            ""
        ).observe(this,
            androidx.lifecycle.Observer {
                it?.let { resource ->
                    when (resource.status) {
                        Status.SUCCESS -> {

                            if (it.data?.status == true) {
                                reportcontactList = it.data.data

                                reportcontactNameList =
                                    reportcontactList?.map { it.display_name.toString() }

                                contactNameAdapter = ArrayAdapter<String>(
                                    this,
                                    android.R.layout.simple_dropdown_item_1line,
                                    reportcontactNameList!!
                                )
                                when (reportsType) {

                                    "contact" -> {
                                        txtRepoTSelContact.setAdapter(contactNameAdapter)
                                        txtRepoTSelContact.threshold = 1
                                        txtRepoTSelContact.setOnItemClickListener { adapterView, _, position, _
                                            ->
                                            val selected: String =
                                                adapterView.getItemAtPosition(position).toString()
                                            val pos: Int? = reportcontactNameList?.indexOf(selected)

                                            selectedContactID =
                                                pos?.let { it1 -> reportcontactList?.get(it1)?.contact_id }

                                        }

                                    }
                                    "summary_contact" -> {
                                        txtRepoTSelContact.setAdapter(contactNameAdapter)
                                        txtRepoTSelContact.threshold = 1
                                        txtRepoTSelContact.setOnItemClickListener { adapterView, _, position, _
                                            ->
                                            val selected: String =
                                                adapterView.getItemAtPosition(position).toString()
                                            val pos: Int? = reportcontactNameList?.indexOf(selected)

                                            selectedContactID =
                                                pos?.let { it1 -> reportcontactList?.get(it1)?.contact_id }

                                        }

                                    }

                                    "sales" -> {
                                        txtRepoTSalesSelContact.setAdapter(contactNameAdapter)
                                        txtRepoTSalesSelContact.threshold = 1
                                        txtRepoTSalesSelContact.setOnItemClickListener { adapterView, _, position, _
                                            ->
                                            val selected: String =
                                                adapterView.getItemAtPosition(position).toString()
                                            val pos: Int? = reportcontactNameList?.indexOf(selected)

                                            selectedContactID =
                                                pos?.let { it1 -> reportcontactList?.get(it1)?.contact_id }

                                        }

                                    }
                                    "purchase" -> {
                                        txtRepoTPurchaseSelContact.setAdapter(contactNameAdapter)
                                        txtRepoTPurchaseSelContact.threshold = 1
                                        txtRepoTPurchaseSelContact.setOnItemClickListener { adapterView, _, position, _
                                            ->
                                            val selected: String =
                                                adapterView.getItemAtPosition(position).toString()
                                            val pos: Int? = reportcontactNameList?.indexOf(selected)

                                            selectedContactID =
                                                pos?.let { it1 -> reportcontactList?.get(it1)?.contact_id }

                                        }
                                    }

                                    else -> {
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


                        }
                        Status.ERROR -> {

                        }
                        Status.LOADING -> {

                        }
                    }
                }
            })
    }

    // cash/bank (ledger api call)
    private fun getLedgerDetails(type: String?) {

        if (NetworkUtils.isConnected()) {

            viewModel.getSearchLedger(loginModel?.data?.bearer_access_token, type)
                .observe(this, androidx.lifecycle.Observer {
                    it?.let { resource ->
                        when (resource.status) {
                            Status.SUCCESS -> {
                                if (it.data?.status == true) {

                                    when (type) {

                                        "cashbank" -> {
                                            cashledgerDetailsList =
                                                it.data.data as ArrayList<SearchLedgerModel.LedgerDetails>?
                                            cashledgerNameList =
                                                cashledgerDetailsList?.map { it.name.toString() }
                                            cashNameAdapter = ArrayAdapter<String>(
                                                this,
                                                android.R.layout.simple_dropdown_item_1line,
                                                cashledgerNameList!!
                                            )
                                            txtRepoTSelCashLedger.setAdapter(cashNameAdapter)
                                            txtRepoTSelCashLedger.threshold = 1
                                            txtRepoTSelCashLedger.setOnItemClickListener { adapterView, _, position, _
                                                ->
                                                val selected: String =
                                                    adapterView.getItemAtPosition(position)
                                                        .toString()
                                                val pos: Int? =
                                                    cashledgerNameList?.indexOf(selected)
                                                txtRepoTSelCashLedger.setText(selected)
                                                txtRepoTSelCashLedger.setSelection(selected.length)
                                                cash_ledger_id =
                                                    pos?.let { it1 -> cashledgerDetailsList?.get(it1)?.ledger_id }

                                            }
                                        }

                                        "bank" -> {
                                            bankledgerDetailsList =
                                                it.data.data as ArrayList<SearchLedgerModel.LedgerDetails>?
                                            bankledgerNameList =
                                                bankledgerDetailsList?.map { it.name.toString() }
                                            bankNameAdapter = ArrayAdapter<String>(
                                                this,
                                                android.R.layout.simple_dropdown_item_1line,
                                                bankledgerNameList!!
                                            )
                                            txtRepoTSelBankLedger.setAdapter(bankNameAdapter)
                                            txtRepoTSelBankLedger.threshold = 1
                                            txtRepoTSelBankLedger.setOnItemClickListener { adapterView, _, position, _
                                                ->
                                                val selected: String =
                                                    adapterView.getItemAtPosition(position)
                                                        .toString()
                                                val pos: Int? =
                                                    bankledgerNameList?.indexOf(selected)
                                                txtRepoTSelBankLedger.setText(selected)
                                                txtRepoTSelBankLedger.setSelection(selected.length)
                                                bank_ledger_id =
                                                    pos?.let { it1 -> bankledgerDetailsList?.get(it1)?.ledger_id }

                                            }

                                        }

                                        "cash" -> {
                                            cashledgerDetailsList =
                                                it.data.data as ArrayList<SearchLedgerModel.LedgerDetails>?
                                            cashledgerNameList =
                                                cashledgerDetailsList?.map { it.name.toString() }
                                            cashNameAdapter = ArrayAdapter<String>(
                                                this,
                                                android.R.layout.simple_dropdown_item_1line,
                                                cashledgerNameList!!
                                            )
                                            txtRepoTSelCashLedger.setAdapter(cashNameAdapter)
                                            txtRepoTSelCashLedger.threshold = 1
                                            txtRepoTSelCashLedger.setOnItemClickListener { adapterView, _, position, _
                                                ->
                                                val selected: String =
                                                    adapterView.getItemAtPosition(position)
                                                        .toString()
                                                val pos: Int? =
                                                    cashledgerNameList?.indexOf(selected)
                                                txtRepoTSelCashLedger.setText(selected)
                                                txtRepoTSelCashLedger.setSelection(selected.length)
                                                cash_ledger_id =
                                                    pos?.let { it1 -> cashledgerDetailsList?.get(it1)?.ledger_id }

                                            }
                                        }
                                        allLedgerType -> {
                                            tcsledgerDetailsList =
                                                it.data.data as ArrayList<SearchLedgerModel.LedgerDetails>?
                                            tcsledgerNameList =
                                                tcsledgerDetailsList?.map { it.name.toString() }
                                            tcsNameAdapter = ArrayAdapter<String>(
                                                this,
                                                android.R.layout.simple_dropdown_item_1line,
                                                tcsledgerNameList!!
                                            )
                                            txtRepoTLedger.setAdapter(tcsNameAdapter)
                                            txtRepoTLedger.threshold = 1
                                            txtRepoTLedger.setOnItemClickListener { adapterView, _, position, _
                                                ->
                                                val selected: String =
                                                    adapterView.getItemAtPosition(position)
                                                        .toString()
                                                val pos: Int? =
                                                    tcsledgerNameList?.indexOf(selected)
                                                txtRepoTLedger.setText(selected)
                                                txtRepoTLedger.setSelection(selected.length)
                                                tcs_ledger_id =
                                                    pos?.let { it1 -> tcsledgerDetailsList?.get(it1)?.ledger_id }

                                            }
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

    private fun downloadReport(reportsType: String?) {
        when (reportsType) {

            "contact" -> {
                if (performValidationContact()) {
                    progressBar_RepoTCommon.visibility = View.VISIBLE
                    if (NetworkUtils.isConnected()) {
                        val apiInterface: ApiService =
                            RetrofitBuilder.createService(ApiService::class.java)
                        val call: Call<ResponseBody> = apiInterface.downloadReport(
                            loginModel?.data?.bearer_access_token,
                            binding.txtRepoTContactFromDate.text.toString(),
                            binding.txtRepoTContactToDate.text.toString(),
                            reportsType,
                            all_contacts,
                            type_of_contact,
                            selectedContactID,
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            all_ledgers,
                            tcs_ledger_id
                        )

                        call.enqueue(object : Callback<ResponseBody> {
                            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                                //not required
                                CommonUtils.somethingWentWrong(this@ReportTypesCommon)
                                progressBar_RepoTCommon.visibility = View.GONE
                            }

                            override fun onResponse(
                                call: Call<ResponseBody>,
                                response: Response<ResponseBody>
                            ) {


                                getFileNamefromHeader(response.headers().toString())
                                val writtenToDisk: Boolean =
                                    writeResponseBodyToDisk(response.body()!!)
                                if (writtenToDisk) {
                                    when (isFromDownload) {
                                        true -> {
                                            val toast: Toast = Toast.makeText(
                                                this@ReportTypesCommon,
                                                "PDF stored at " + fileSavePath.drop(20)/*getString(R.string.report_downloaded_successfully)*/,
                                                Toast.LENGTH_LONG
                                            )
                                            toast.setGravity(Gravity.CENTER, 0, 0)
                                            toast.show()
                                            val root = File(
                                                Environment.getExternalStoragePublicDirectory(
                                                    Environment.DIRECTORY_DOWNLOADS
                                                ), "GoldBook"
                                            )
                                            val rootDirtory = File(root, "Reports")
                                            var myDirectory = File(rootDirtory, "Contact")
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
                                            //showDownloadNotification()
                                        }
                                        false -> {
                                            print(
                                                "$reportsType PDF",
                                                PdfDocumentAdapter(
                                                    this@ReportTypesCommon,
                                                    filename, "Contact"
                                                ),
                                                PrintAttributes.Builder()
                                                    .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                                                    .build()
                                            )
                                        }
                                    }

                                    if (!CommonUtils.isPDFSupported(
                                            this@ReportTypesCommon,
                                            response.headers().toString()
                                        )
                                    ) {
                                        Toast.makeText(
                                            this@ReportTypesCommon,
                                            getString(R.string.pdf_notinstalled_msg),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    progressBar_RepoTCommon.visibility = View.GONE


                                }

                            }

                        })
                    }
                }


            }

            "summary_contact" -> {
                if (performValidationContact()) {
                    progressBar_RepoTCommon.visibility = View.VISIBLE
                    if (NetworkUtils.isConnected()) {
                        val apiInterface: ApiService =
                            RetrofitBuilder.createService(ApiService::class.java)
                        val call: Call<ResponseBody> = apiInterface.downloadReport(
                            loginModel?.data?.bearer_access_token,
                            binding.txtRepoTContactFromDate.text.toString(),
                            binding.txtRepoTContactToDate.text.toString(),
                            reportsType,
                            all_contacts,
                            type_of_contact,
                            selectedContactID,
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            all_ledgers,
                            tcs_ledger_id
                        )

                        call.enqueue(object : Callback<ResponseBody> {
                            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                                //not required
                                CommonUtils.somethingWentWrong(this@ReportTypesCommon)
                                progressBar_RepoTCommon.visibility = View.GONE
                            }

                            override fun onResponse(
                                call: Call<ResponseBody>,
                                response: Response<ResponseBody>
                            ) {


                                getFileNamefromHeader(response.headers().toString())
                                val writtenToDisk: Boolean =
                                    writeResponseBodyToDisk(response.body()!!)
                                if (writtenToDisk) {
                                    when (isFromDownload) {
                                        true -> {
                                            val toast: Toast = Toast.makeText(
                                                this@ReportTypesCommon,
                                                "PDF stored at " + fileSavePath.drop(20)/*getString(R.string.report_downloaded_successfully)*/,
                                                Toast.LENGTH_LONG
                                            )
                                            toast.setGravity(Gravity.CENTER, 0, 0)
                                            toast.show()
                                            val root = File(
                                                Environment.getExternalStoragePublicDirectory(
                                                    Environment.DIRECTORY_DOWNLOADS
                                                ), "GoldBook"
                                            )
                                            val rootDirtory = File(root, "Reports")
                                            var myDirectory = File(rootDirtory, "SummaryContact")
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
                                            //showDownloadNotification()
                                        }
                                        false -> {
                                            print(
                                                "$reportsType PDF",
                                                PdfDocumentAdapter(
                                                    this@ReportTypesCommon,
                                                    filename, "SummaryContact"
                                                ),
                                                PrintAttributes.Builder()
                                                    .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                                                    .build()
                                            )
                                        }
                                    }

                                    if (!CommonUtils.isPDFSupported(
                                            this@ReportTypesCommon,
                                            response.headers().toString()
                                        )
                                    ) {
                                        Toast.makeText(
                                            this@ReportTypesCommon,
                                            getString(R.string.pdf_notinstalled_msg),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    progressBar_RepoTCommon.visibility = View.GONE


                                }

                            }

                        })
                    }
                }


            }

            "total_stock_summary" -> {
                progressBar_RepoTCommon.visibility = View.VISIBLE
                // if (performValidationStock()) {
                if (NetworkUtils.isConnected()) {
                    val apiInterface: ApiService =
                        RetrofitBuilder.createService(ApiService::class.java)
                    val call: Call<ResponseBody> = apiInterface.downloadReport(
                        loginModel?.data?.bearer_access_token,
                        binding.txtRepoTStockFromDate.text.toString(),
                        binding.txtRepoTStockToDate.text.toString(),
                        reportsType,
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        ""
                    )

                    call.enqueue(object : Callback<ResponseBody> {
                        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                            //not required
                            CommonUtils.somethingWentWrong(this@ReportTypesCommon)
                            progressBar_RepoTCommon.visibility = View.GONE
                        }

                        override fun onResponse(
                            call: Call<ResponseBody>,
                            response: Response<ResponseBody>
                        ) {

                            getFileNamefromHeader(response.headers().toString())
                            val writtenToDisk: Boolean =
                                writeResponseBodyToDisk(response.body()!!)
                            if (writtenToDisk) {
                                when (isFromDownload) {
                                    true -> {
                                        val toast: Toast = Toast.makeText(
                                            this@ReportTypesCommon,
                                            "PDF stored at " + fileSavePath.drop(20)/*getString(R.string.report_downloaded_successfully)*/,
                                            Toast.LENGTH_LONG
                                        )
                                        toast.setGravity(Gravity.CENTER, 0, 0)
                                        toast.show()
                                        val root = File(
                                            Environment.getExternalStoragePublicDirectory(
                                                Environment.DIRECTORY_DOWNLOADS
                                            ), "GoldBook"
                                        )
                                        val rootDirtory = File(root, "Reports")
                                        var myDirectory = File(rootDirtory, "Stock")
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
                                        //showDownloadNotification()
                                    }
                                    false -> {
                                        print(
                                            "$reportsType PDF",
                                            PdfDocumentAdapter(
                                                this@ReportTypesCommon,
                                                filename, "Stock"
                                            ),
                                            PrintAttributes.Builder()
                                                .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                                                .build()
                                        )
                                    }
                                }

                                if (!CommonUtils.isPDFSupported(
                                        this@ReportTypesCommon,
                                        response.headers().toString()
                                    )
                                ) {
                                    Toast.makeText(
                                        this@ReportTypesCommon,
                                        getString(R.string.pdf_notinstalled_msg),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                progressBar_RepoTCommon.visibility = View.GONE
                            }

                        }

                    })
                }
                // }

            }
            "sales" -> {
                //downloadReport()
                if (performValidationSales()) {
                    progressBar_RepoTCommon.visibility = View.VISIBLE
                    if (NetworkUtils.isConnected()) {
                        val apiInterface: ApiService =
                            RetrofitBuilder.createService(ApiService::class.java)
                        val call: Call<ResponseBody> = apiInterface.downloadReport(
                            loginModel?.data?.bearer_access_token,
                            binding.txtRepoTSalesFromDate.text.toString(),
                            binding.txtRepoTSalesToDate.text.toString(),
                            reportsType,
                            all_contacts,
                            type_of_contact,
                            selectedContactID,
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            ""
                        )

                        call.enqueue(object : Callback<ResponseBody> {
                            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                                //not required
                                CommonUtils.somethingWentWrong(this@ReportTypesCommon)
                                progressBar_RepoTCommon.visibility = View.GONE
                            }

                            override fun onResponse(
                                call: Call<ResponseBody>,
                                response: Response<ResponseBody>
                            ) {

                                getFileNamefromHeader(response.headers().toString())

                                val writtenToDisk: Boolean =
                                    writeResponseBodyToDisk(response.body()!!)
                                if (writtenToDisk) {
                                    when (isFromDownload) {
                                        true -> {
                                            val toast: Toast = Toast.makeText(
                                                this@ReportTypesCommon,
                                                "PDF stored at " + fileSavePath.drop(20)/*getString(R.string.report_downloaded_successfully)*/,
                                                Toast.LENGTH_LONG
                                            )
                                            toast.setGravity(Gravity.CENTER, 0, 0)
                                            toast.show()
                                            val root = File(
                                                Environment.getExternalStoragePublicDirectory(
                                                    Environment.DIRECTORY_DOWNLOADS
                                                ), "GoldBook"
                                            )
                                            val rootDirtory = File(root, "Reports")
                                            var myDirectory = File(rootDirtory, "Invoice")
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
                                                "$reportsType PDF",
                                                PdfDocumentAdapter(
                                                    this@ReportTypesCommon,
                                                    filename, "Invoice"
                                                ),
                                                PrintAttributes.Builder()
                                                    .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                                                    .build()
                                            )
                                        }
                                    }
                                    if (!CommonUtils.isPDFSupported(
                                            this@ReportTypesCommon,
                                            response.headers().toString()
                                        )
                                    ) {
                                        Toast.makeText(
                                            this@ReportTypesCommon,
                                            getString(R.string.pdf_notinstalled_msg),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    progressBar_RepoTCommon.visibility = View.GONE
                                }
                            }

                        })
                    }
                }

            }
            "purchase" -> {
                //downloadReport()
                if (performValidationPurchase()) {
                    progressBar_RepoTCommon.visibility = View.VISIBLE
                    if (NetworkUtils.isConnected()) {
                        val apiInterface: ApiService =
                            RetrofitBuilder.createService(ApiService::class.java)
                        val call: Call<ResponseBody> = apiInterface.downloadReport(
                            loginModel?.data?.bearer_access_token,
                            binding.txtRepoTPurchaseFromDate.text.toString(),
                            binding.txtRepoTPurchaseToDate.text.toString(),
                            reportsType,
                            all_contacts,
                            type_of_contact,
                            selectedContactID,
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            ""
                        )

                        call.enqueue(object : Callback<ResponseBody> {
                            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                                //not required
                                CommonUtils.somethingWentWrong(this@ReportTypesCommon)
                                progressBar_RepoTCommon.visibility = View.GONE
                            }

                            override fun onResponse(
                                call: Call<ResponseBody>,
                                response: Response<ResponseBody>
                            ) {

                                getFileNamefromHeader(response.headers().toString())
                                val writtenToDisk: Boolean =
                                    writeResponseBodyToDisk(response.body()!!)
                                if (writtenToDisk) {
                                    when (isFromDownload) {
                                        true -> {
                                            val toast: Toast = Toast.makeText(
                                                this@ReportTypesCommon,
                                                "PDF stored at " + fileSavePath.drop(20)/*getString(R.string.report_downloaded_successfully)*/,
                                                Toast.LENGTH_LONG
                                            )
                                            toast.setGravity(Gravity.CENTER, 0, 0)
                                            toast.show()
                                            val root = File(
                                                Environment.getExternalStoragePublicDirectory(
                                                    Environment.DIRECTORY_DOWNLOADS
                                                ), "GoldBook"
                                            )
                                            val rootDirtory = File(root, "Reports")
                                            var myDirectory = File(rootDirtory, "Receipt")
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
                                            // showDownloadNotification()
                                        }
                                        false -> {
                                            print(
                                                "$reportsType PDF",
                                                PdfDocumentAdapter(
                                                    this@ReportTypesCommon,
                                                    filename, "Receipt"
                                                ),
                                                PrintAttributes.Builder()
                                                    .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                                                    .build()
                                            )
                                        }
                                    }
                                    if (!CommonUtils.isPDFSupported(
                                            this@ReportTypesCommon,
                                            response.headers().toString()
                                        )
                                    ) {
                                        Toast.makeText(
                                            this@ReportTypesCommon,
                                            getString(R.string.pdf_notinstalled_msg),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    progressBar_RepoTCommon.visibility = View.GONE
                                }
                            }

                        })
                    }
                }

            }
            "day" -> {
                progressBar_RepoTCommon.visibility = View.VISIBLE
                if (NetworkUtils.isConnected()) {
                    val apiInterface: ApiService =
                        RetrofitBuilder.createService(ApiService::class.java)
                    val call: Call<ResponseBody> = apiInterface.downloadReport(
                        loginModel?.data?.bearer_access_token,
                        binding.txtRepoTDayFromDate.text.toString(),
                        binding.txtRepoTDayToDate.text.toString(),
                        reportsType,
                        all_contacts,
                        type_of_contact,
                        selectedContactID,
                        "",
                        "",
                        "",
                        "",
                        binding.txtRepoTDayPeriod.text.toString(),
                        "",
                        "",
                        "",
                        "",
                        "",
                        ""
                    )

                    call.enqueue(object : Callback<ResponseBody> {
                        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                            //not required
                            CommonUtils.somethingWentWrong(this@ReportTypesCommon)
                            progressBar_RepoTCommon.visibility = View.GONE
                        }

                        override fun onResponse(
                            call: Call<ResponseBody>,
                            response: Response<ResponseBody>
                        ) {

                            getFileNamefromHeader(response.headers().toString())
                            val writtenToDisk: Boolean =
                                writeResponseBodyToDisk(response.body()!!)
                            if (writtenToDisk) {
                                when (isFromDownload) {
                                    true -> {
                                        val toast: Toast = Toast.makeText(
                                            this@ReportTypesCommon,
                                            "PDF stored at " + fileSavePath.drop(20)/*getString(R.string.report_downloaded_successfully)*/,
                                            Toast.LENGTH_LONG
                                        )
                                        toast.setGravity(Gravity.CENTER, 0, 0)
                                        toast.show()
                                        val root = File(
                                            Environment.getExternalStoragePublicDirectory(
                                                Environment.DIRECTORY_DOWNLOADS
                                            ), "GoldBook"
                                        )
                                        val rootDirtory = File(root, "Reports")
                                        var myDirectory = File(rootDirtory, "Day")
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
                                        //showDownloadNotification()
                                    }
                                    false -> {
                                        print(
                                            "$reportsType PDF",
                                            PdfDocumentAdapter(
                                                this@ReportTypesCommon,
                                                filename, "Day"
                                            ),
                                            PrintAttributes.Builder()
                                                .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                                                .build()
                                        )
                                    }
                                }
                                if (!CommonUtils.isPDFSupported(
                                        this@ReportTypesCommon,
                                        response.headers().toString()
                                    )
                                ) {
                                    Toast.makeText(
                                        this@ReportTypesCommon,
                                        getString(R.string.pdf_notinstalled_msg),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                progressBar_RepoTCommon.visibility = View.GONE
                            }
                        }

                    })
                }


            }
            "cashbank" -> {
                if (performValidationCashBank()) {
                    progressBar_RepoTCommon.visibility = View.VISIBLE
                    if (NetworkUtils.isConnected()) {
                        val apiInterface: ApiService =
                            RetrofitBuilder.createService(ApiService::class.java)
                        val call: Call<ResponseBody> = apiInterface.downloadReport(
                            loginModel?.data?.bearer_access_token,
                            binding.txtRepoTCashBankFromDate.text.toString(),
                            binding.txtRepoTCashBankToDate.text.toString(),
                            reportsType,
                            all_contacts,
                            type_of_contact,
                            selectedContactID,
                            all_cash_ledgers,
                            cash_ledger_id,
                            all_bank_ledgers,
                            bank_ledger_id,
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            ""
                        )

                        call.enqueue(object : Callback<ResponseBody> {
                            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                                //not required
                                CommonUtils.somethingWentWrong(this@ReportTypesCommon)
                                progressBar_RepoTCommon.visibility = View.GONE
                            }

                            override fun onResponse(
                                call: Call<ResponseBody>,
                                response: Response<ResponseBody>
                            ) {

                                getFileNamefromHeader(response.headers().toString())
                                val writtenToDisk: Boolean =
                                    writeResponseBodyToDisk(response.body()!!)
                                if (writtenToDisk) {
                                    when (isFromDownload) {
                                        true -> {
                                            val toast: Toast = Toast.makeText(
                                                this@ReportTypesCommon,
                                                "PDF stored at " + fileSavePath.drop(20)/*getString(R.string.report_downloaded_successfully)*/,
                                                Toast.LENGTH_LONG
                                            )
                                            toast.setGravity(Gravity.CENTER, 0, 0)
                                            toast.show()
                                            val root = File(
                                                Environment.getExternalStoragePublicDirectory(
                                                    Environment.DIRECTORY_DOWNLOADS
                                                ), "GoldBook"
                                            )
                                            val rootDirtory = File(root, "Reports")
                                            var myDirectory = File(rootDirtory, "Cashbank")
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
                                            //  showDownloadNotification()
                                        }
                                        false -> {
                                            print(
                                                "$reportsType PDF",
                                                PdfDocumentAdapter(
                                                    this@ReportTypesCommon,
                                                    filename, "Cashbank"
                                                ),
                                                PrintAttributes.Builder()
                                                    .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                                                    .build()
                                            )
                                        }
                                    }
                                    if (!CommonUtils.isPDFSupported(
                                            this@ReportTypesCommon,
                                            response.headers().toString()
                                        )
                                    ) {
                                        Toast.makeText(
                                            this@ReportTypesCommon,
                                            getString(R.string.pdf_notinstalled_msg),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    progressBar_RepoTCommon.visibility = View.GONE
                                }
                            }

                        })
                    }
                }

            }
            "ledger" -> {
                if (performValidationLedger()) {
                    progressBar_RepoTCommon.visibility = View.VISIBLE
                    if (NetworkUtils.isConnected()) {
                        val apiInterface: ApiService =
                            RetrofitBuilder.createService(ApiService::class.java)
                        val call: Call<ResponseBody> = apiInterface.downloadReport(
                            loginModel?.data?.bearer_access_token,
                            binding.txtRepoTLedgerFromDate.text.toString(),
                            binding.txtRepoTLedgerToDate.text.toString(),
                            reportsType,
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            all_ledgers,
                            tcs_ledger_id
                        )

                        call.enqueue(object : Callback<ResponseBody> {
                            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                                //not required
                                CommonUtils.somethingWentWrong(this@ReportTypesCommon)
                                progressBar_RepoTCommon.visibility = View.GONE
                            }

                            override fun onResponse(
                                call: Call<ResponseBody>,
                                response: Response<ResponseBody>
                            ) {

                                getFileNamefromHeader(response.headers().toString())
                                val writtenToDisk: Boolean =
                                    writeResponseBodyToDisk(response.body()!!)
                                if (writtenToDisk) {
                                    when (isFromDownload) {
                                        true -> {
                                            val toast: Toast = Toast.makeText(
                                                this@ReportTypesCommon,
                                                "PDF stored at " + fileSavePath.drop(20)/*getString(R.string.report_downloaded_successfully)*/,
                                                Toast.LENGTH_LONG
                                            )
                                            toast.setGravity(Gravity.CENTER, 0, 0)
                                            toast.show()
                                            val root = File(
                                                Environment.getExternalStoragePublicDirectory(
                                                    Environment.DIRECTORY_DOWNLOADS
                                                ), "GoldBook"
                                            )
                                            val rootDirtory = File(root, "Reports")
                                            var myDirectory = File(rootDirtory, "Ledger")
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
                                            //  showDownloadNotification()
                                        }
                                        false -> {
                                            print(
                                                "$reportsType PDF",
                                                PdfDocumentAdapter(
                                                    this@ReportTypesCommon,
                                                    filename, "Ledger"
                                                ),
                                                PrintAttributes.Builder()
                                                    .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                                                    .build()
                                            )
                                        }
                                    }
                                    if (!CommonUtils.isPDFSupported(
                                            this@ReportTypesCommon,
                                            response.headers().toString()
                                        )
                                    ) {
                                        Toast.makeText(
                                            this@ReportTypesCommon,
                                            getString(R.string.pdf_notinstalled_msg),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    progressBar_RepoTCommon.visibility = View.GONE
                                }
                            }

                        })
                    }
                }

            }
            "tagged_stock_summary" -> {
                progressBar_RepoTCommon.visibility = View.VISIBLE
                //if (performValidationLedger()) {
                if (NetworkUtils.isConnected()) {
                    val apiInterface: ApiService =
                        RetrofitBuilder.createService(ApiService::class.java)
                    val call: Call<ResponseBody> = apiInterface.downloadReport(
                        loginModel?.data?.bearer_access_token,
                        binding.txtRepoTStockFromDate.text.toString(),
                        binding.txtRepoTStockToDate.text.toString(),
                        reportsType,
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        ""
                    )

                    call.enqueue(object : Callback<ResponseBody> {
                        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                            //not required
                            CommonUtils.somethingWentWrong(this@ReportTypesCommon)
                            progressBar_RepoTCommon.visibility = View.GONE
                        }

                        override fun onResponse(
                            call: Call<ResponseBody>,
                            response: Response<ResponseBody>
                        ) {

                            getFileNamefromHeader(response.headers().toString())
                            val writtenToDisk: Boolean =
                                writeResponseBodyToDisk(response.body()!!)
                            if (writtenToDisk) {
                                when (isFromDownload) {
                                    true -> {
                                        val toast: Toast = Toast.makeText(
                                            this@ReportTypesCommon,
                                            "PDF stored at " + fileSavePath.drop(20)/*getString(R.string.report_downloaded_successfully)*/,
                                            Toast.LENGTH_LONG
                                        )
                                        toast.setGravity(Gravity.CENTER, 0, 0)
                                        toast.show()
                                        val root = File(
                                            Environment.getExternalStoragePublicDirectory(
                                                Environment.DIRECTORY_DOWNLOADS
                                            ), "GoldBook"
                                        )
                                        val rootDirtory = File(root, "Reports")
                                        var myDirectory = File(rootDirtory, "TaggedStock")
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
                                        //  showDownloadNotification()
                                    }
                                    false -> {
                                        print(
                                            "$reportsType PDF",
                                            PdfDocumentAdapter(
                                                this@ReportTypesCommon,
                                                filename, "Ledger"
                                            ),
                                            PrintAttributes.Builder()
                                                .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                                                .build()
                                        )
                                    }
                                }
                                if (!CommonUtils.isPDFSupported(
                                        this@ReportTypesCommon,
                                        response.headers().toString()
                                    )
                                ) {
                                    Toast.makeText(
                                        this@ReportTypesCommon,
                                        getString(R.string.pdf_notinstalled_msg),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                progressBar_RepoTCommon.visibility = View.GONE
                            }
                        }

                    })
                }
                // }
            }
            "stock_item_details" -> {
                if (performValidationStock()) {
                    progressBar_RepoTCommon.visibility = View.VISIBLE
                    if (NetworkUtils.isConnected()) {
                        val apiInterface: ApiService =
                            RetrofitBuilder.createService(ApiService::class.java)
                        val call: Call<ResponseBody> = apiInterface.downloadReport(
                            loginModel?.data?.bearer_access_token,
                            binding.txtRepoTStockFromDate.text.toString(),
                            binding.txtRepoTStockToDate.text.toString(),
                            reportsType,
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            all_items,
                            item_id,
                            "",
                            ""
                        )

                        call.enqueue(object : Callback<ResponseBody> {
                            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                                //not required
                                CommonUtils.somethingWentWrong(this@ReportTypesCommon)
                                progressBar_RepoTCommon.visibility = View.GONE
                            }

                            override fun onResponse(
                                call: Call<ResponseBody>,
                                response: Response<ResponseBody>
                            ) {

                                getFileNamefromHeader(response.headers().toString())
                                val writtenToDisk: Boolean =
                                    writeResponseBodyToDisk(response.body()!!)
                                if (writtenToDisk) {
                                    when (isFromDownload) {
                                        true -> {
                                            val toast: Toast = Toast.makeText(
                                                this@ReportTypesCommon,
                                                "PDF stored at " + fileSavePath.drop(20)/*getString(R.string.report_downloaded_successfully)*/,
                                                Toast.LENGTH_LONG
                                            )
                                            toast.setGravity(Gravity.CENTER, 0, 0)
                                            toast.show()
                                            val root = File(
                                                Environment.getExternalStoragePublicDirectory(
                                                    Environment.DIRECTORY_DOWNLOADS
                                                ), "GoldBook"
                                            )
                                            val rootDirtory = File(root, "Reports")
                                            var myDirectory = File(rootDirtory, "ItemStock")
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
                                            //  showDownloadNotification()
                                        }
                                        false -> {
                                            print(
                                                "$reportsType PDF",
                                                PdfDocumentAdapter(
                                                    this@ReportTypesCommon,
                                                    filename, "Ledger"
                                                ),
                                                PrintAttributes.Builder()
                                                    .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                                                    .build()
                                            )
                                        }
                                    }
                                    if (!CommonUtils.isPDFSupported(
                                            this@ReportTypesCommon,
                                            response.headers().toString()
                                        )
                                    ) {
                                        Toast.makeText(
                                            this@ReportTypesCommon,
                                            getString(R.string.pdf_notinstalled_msg),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    progressBar_RepoTCommon.visibility = View.GONE
                                }
                            }

                        })
                    }
                }
            }

        }
    }

    private fun print(
        name: String, adapter: PrintDocumentAdapter,
        attrs: PrintAttributes
    ): PrintJob? {
        startService(Intent(this, PrintJobMonitorService::class.java))
        return mgr!!.print(name, adapter, attrs)
    }

    private fun generateReport(reportsType: String?) {
        imgRight2.setImageResource(R.drawable.ic_print)
        imgRight.setImageResource(R.drawable.ic_download)
        imgRight.visibility = View.GONE
        imgRight2.visibility = View.GONE
        imgRight.clickWithDebounce {

            isFromDownload = true
            reportApiCallType = "download"
            callToCommon(reportApiCallType)

        }
        imgRight2.clickWithDebounce {

            isFromDownload = false
            //calltoDownloadReport()
            reportApiCallType = "download"
            callToCommon(reportApiCallType)

        }
        when (reportsType) {

            "contact" -> {
                if (performValidationContact()) {
                    if (NetworkUtils.isConnected()) {
                        viewModel.contactReportPrint(
                            loginModel?.data?.bearer_access_token,
                            binding.txtRepoTContactFromDate.text.toString(),
                            binding.txtRepoTContactToDate.text.toString(),
                            reportsType,
                            all_contacts,
                            type_of_contact,
                            selectedContactID,
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            ""
                        )
                            .observe(this, androidx.lifecycle.Observer {
                                it?.let { resource ->
                                    when (resource.status) {
                                        Status.SUCCESS -> {
                                            if (it.data?.status == true) {
                                                fillContactReportOnScreenData(
                                                    it.data.data,
                                                    it.data.records
                                                )


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
            "summary_contact" -> {
                if (performValidationContact()) {
                    if (NetworkUtils.isConnected()) {
                        viewModel.contactReportPrint(
                            loginModel?.data?.bearer_access_token,
                            binding.txtRepoTContactFromDate.text.toString(),
                            binding.txtRepoTContactToDate.text.toString(),
                            reportsType,
                            all_contacts,
                            type_of_contact,
                            selectedContactID,
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            ""
                        )
                            .observe(this, androidx.lifecycle.Observer {
                                it?.let { resource ->
                                    when (resource.status) {
                                        Status.SUCCESS -> {
                                            if (it.data?.status == true) {
                                                fillContactReportOnScreenData(
                                                    it.data.data,
                                                    it.data.records
                                                )


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
            "total_stock_summary" -> {
                //if (performValidationStock()) {
                if (NetworkUtils.isConnected()) {
                    viewModel.stocktPrint(
                        loginModel?.data?.bearer_access_token,
                        binding.txtRepoTStockFromDate.text.toString(),
                        binding.txtRepoTStockToDate.text.toString(),
                        reportsType,
                        all_contacts,
                        type_of_contact,
                        selectedContactID,
                        "",
                        "",
                        "",
                        "",
                        "",
                        all_item_categories,
                        item_category_id,
                        all_items,
                        item_id
                    )
                        .observe(this, androidx.lifecycle.Observer {
                            it?.let { resource ->
                                when (resource.status) {
                                    Status.SUCCESS -> {
                                        if (it.data?.status == true) {
                                            fillStockOnScreenData(it.data.data, it.data.records)

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
                //  }

            }
            "sales" -> {
                //downloadReport()
                if (performValidationSales()) {
                    if (NetworkUtils.isConnected()) {
                        viewModel.salesPurchaseReportPrint(
                            loginModel?.data?.bearer_access_token,
                            binding.txtRepoTSalesFromDate.text.toString(),
                            binding.txtRepoTSalesToDate.text.toString(),
                            reportsType,
                            all_contacts,
                            type_of_contact,
                            selectedContactID,
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            ""
                        )
                            .observe(this, androidx.lifecycle.Observer {
                                it?.let { resource ->
                                    when (resource.status) {
                                        Status.SUCCESS -> {
                                            if (it.data?.status == true) {
                                                fillSalesPruchaseReportOnScreenData(
                                                    it.data.data,
                                                    it.data.records
                                                )

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
            "purchase" -> {
                //downloadReport()
                if (performValidationPurchase()) {
                    if (NetworkUtils.isConnected()) {
                        viewModel.salesPurchaseReportPrint(
                            loginModel?.data?.bearer_access_token,
                            binding.txtRepoTPurchaseFromDate.text.toString(),
                            binding.txtRepoTPurchaseToDate.text.toString(),
                            reportsType,
                            all_contacts,
                            type_of_contact,
                            selectedContactID,
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            ""
                        )
                            .observe(this, androidx.lifecycle.Observer {
                                it?.let { resource ->
                                    when (resource.status) {
                                        Status.SUCCESS -> {
                                            if (it.data?.status == true) {
                                                fillSalesPruchaseReportOnScreenData(
                                                    it.data.data,
                                                    it.data.records
                                                )

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
            "day" -> {
                if (NetworkUtils.isConnected()) {
                    viewModel.dayReportPrint(
                        loginModel?.data?.bearer_access_token,
                        binding.txtRepoTDayFromDate.text.toString(),
                        binding.txtRepoTDayToDate.text.toString(),
                        reportsType,
                        all_contacts,
                        type_of_contact,
                        selectedContactID,
                        "",
                        "",
                        "",
                        "",
                        binding.txtRepoTDayPeriod.text.toString(),
                        "",
                        "",
                        "",
                        ""
                    )
                        .observe(this, androidx.lifecycle.Observer {
                            it?.let { resource ->
                                when (resource.status) {
                                    Status.SUCCESS -> {
                                        if (it.data?.status == true) {
                                            fillDayOnScreenData(it.data.data, it.data.records)

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
            "cashbank" -> {
                if (performValidationCashBank()) {
                    if (NetworkUtils.isConnected()) {
                        viewModel.cashbankReportPrint(
                            loginModel?.data?.bearer_access_token,
                            binding.txtRepoTCashBankFromDate.text.toString(),
                            binding.txtRepoTCashBankToDate.text.toString(),
                            reportsType,
                            all_contacts,
                            type_of_contact,
                            selectedContactID,
                            all_cash_ledgers,
                            cash_ledger_id,
                            all_bank_ledgers,
                            bank_ledger_id,
                            "",
                            "",
                            "",
                            "",
                            ""
                        )
                            .observe(this, androidx.lifecycle.Observer {
                                it?.let { resource ->
                                    when (resource.status) {
                                        Status.SUCCESS -> {
                                            if (it.data?.status == true) {
                                                fillCashBankOnScreenData(
                                                    it.data.data,
                                                    it.data.records
                                                )

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
            "ledger" -> {
                if (performValidationLedger()) {
                    if (NetworkUtils.isConnected()) {
                        viewModel.ledgerReportPrint(
                            loginModel?.data?.bearer_access_token,
                            binding.txtRepoTLedgerFromDate.text.toString(),
                            binding.txtRepoTLedgerToDate.text.toString(),
                            reportsType,
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            all_ledgers,
                            tcs_ledger_id
                        )
                            .observe(this, androidx.lifecycle.Observer {
                                it?.let { resource ->
                                    when (resource.status) {
                                        Status.SUCCESS -> {
                                            if (it.data?.status == true) {
                                                fillLedgerOnScreenData(
                                                    it.data.data,
                                                    it.data.records
                                                )

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
            "tagged_stock_summary" -> {
                // if (performValidationLedger()) {
                if (NetworkUtils.isConnected()) {
                    viewModel.ledgerReportPrint(
                        loginModel?.data?.bearer_access_token,
                        binding.txtRepoTLedgerFromDate.text.toString(),
                        binding.txtRepoTLedgerToDate.text.toString(),
                        reportsType,
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        ""
                    )
                        .observe(this, androidx.lifecycle.Observer {
                            it?.let { resource ->
                                when (resource.status) {
                                    Status.SUCCESS -> {
                                        if (it.data?.status == true) {
                                            fillLedgerOnScreenData(
                                                it.data.data,
                                                it.data.records
                                            )

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
                //  }

            }
            "stock_item_details" -> {
                if (performValidationStock()) {
                    if (NetworkUtils.isConnected()) {
                        viewModel.ledgerReportPrint(
                            loginModel?.data?.bearer_access_token,
                            binding.txtRepoTLedgerFromDate.text.toString(),
                            binding.txtRepoTLedgerToDate.text.toString(),
                            reportsType,
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            ""
                        )
                            .observe(this, androidx.lifecycle.Observer {
                                it?.let { resource ->
                                    when (resource.status) {
                                        Status.SUCCESS -> {
                                            if (it.data?.status == true) {
                                                fillLedgerOnScreenData(
                                                    it.data.data,
                                                    it.data.records
                                                )

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
        }
    }

    private fun fillStockOnScreenData(data: StockPrintModel.Data, records: String?) {
        binding.rlRootReportCommon.visibility = View.GONE
        // header set
        binding.grTvCompanyname.text = loginModel.data!!.company_info!!.company_name
        binding.grTvBranchname.text = loginModel.data!!.branch_info!!.branch_name
        binding.grFromToDate.text =
            "Period: ${data.dateArray!!.from_date} to ${data.dateArray!!.to_date}"
        when (records) {
            "1" -> {
                //  imgRight.visibility = View.VISIBLE
                //  imgRight2.visibility = View.VISIBLE
                checkUserRestrictionafterGeneratingReport()

                binding.rvGrStock.visibility = View.VISIBLE
                binding.tvNoEntries.visibility = View.GONE
                binding.rvGrStock.layoutManager = LinearLayoutManager(this@ReportTypesCommon)
                cashBankAdapter = CashBankPrintAdapter(null, data.categories, null, "2")
                binding.rvGrStock.adapter = cashBankAdapter

                binding.grLlContactTotalNClosingbal.visibility = View.VISIBLE
                binding.grContactTotalLabel.text = "Grand-Total:"
                binding.grContactTotalFinewt.text =
                    "${Constants.NETWT_APPEND}${data.grand_total_net_wt}"
                binding.grContactTotalAmount.text =
                    "${Constants.FINEWT_APPEND}${data.grand_total_fine_wt}"
                binding.grContactTotalTopline.visibility = View.GONE

            }
            "0" -> {
                binding.root.imgRight.visibility = View.GONE
                binding.root.imgRight2.visibility = View.GONE

                binding.grLlContactTotalNClosingbal.visibility = View.GONE
                binding.tvNoEntries.visibility = View.VISIBLE
                binding.rvGrStock.visibility = View.GONE

            }
        }

    }

    //Use for cashbank
    private fun fillCashBankOnScreenData(data: CashBankPrintModel.Data, records: String?) {
        rlRoot_report_common.visibility = View.GONE
        // header set
        binding.grTvCompanyname.text = loginModel.data!!.company_info!!.company_name
        binding.grTvBranchname.text = loginModel.data!!.branch_info!!.branch_name
        binding.grFromToDate.text =
            "Period: ${data.dateArray!!.from_date} to ${data.dateArray!!.to_date}"
        when (records) {
            "1" -> {
                //  imgRight.visibility = View.VISIBLE
                //  imgRight2.visibility = View.VISIBLE
                checkUserRestrictionafterGeneratingReport()

                binding.rvGrCashbankbook.visibility = View.VISIBLE
                binding.tvNoEntries.visibility = View.GONE
                binding.rvGrCashbankbook.layoutManager = LinearLayoutManager(this@ReportTypesCommon)
                cashBankAdapter = CashBankPrintAdapter(data.ledgers, null, null, "1")
                binding.rvGrCashbankbook.adapter = cashBankAdapter


            }
            "0" -> {
                binding.root.imgRight.visibility = View.GONE
                binding.root.imgRight2.visibility = View.GONE

                binding.tvNoEntries.visibility = View.VISIBLE
                binding.rvGrCashbankbook.visibility = View.GONE
            }
        }


    }

    private fun fillLedgerOnScreenData(data: CashBankPrintModel.Data, records: String?) {
        binding.rlRootReportCommon.visibility = View.GONE
        // header set
        binding.grTvCompanyname.text = loginModel.data!!.company_info!!.company_name
        binding.grTvBranchname.text = loginModel.data!!.branch_info!!.branch_name
        binding.grFromToDate.text =
            "Period: ${data.dateArray!!.from_date} to ${data.dateArray!!.to_date}"
        when (records) {
            "1" -> {
                //  imgRight.visibility = View.VISIBLE
                //   imgRight2.visibility = View.VISIBLE
                checkUserRestrictionafterGeneratingReport()

                binding.rvGrCashbankbook.visibility = View.VISIBLE
                binding.tvNoEntries.visibility = View.GONE
                binding.rvGrCashbankbook.layoutManager = LinearLayoutManager(this@ReportTypesCommon)
                cashBankAdapter = CashBankPrintAdapter(null, null, data.ledgers, "3")
                binding.rvGrCashbankbook.adapter = cashBankAdapter


            }
            "0" -> {
                binding.root.imgRight.visibility = View.GONE
                binding.root.imgRight2.visibility = View.GONE

                binding.tvNoEntries.visibility = View.VISIBLE
                binding.rvGrCashbankbook.visibility = View.GONE
            }
        }


    }

    private fun fillDayOnScreenData(
        data: DayPrintModel.Data,
        records: String?
    ) {
        rlRoot_report_common.visibility = View.GONE
        // header set
        gr_tv_companyname.text = loginModel.data!!.company_info!!.company_name
        gr_tv_branchname.text = loginModel.data!!.branch_info!!.branch_name
        gr_from_to_date.text =
            "Period: ${data.dateArray!!.from_date} to ${data.dateArray!!.to_date}"
        when (records) {
            "1" -> {
                //  imgRight.visibility = View.VISIBLE
                //  imgRight2.visibility = View.VISIBLE
                checkUserRestrictionafterGeneratingReport()

                binding.rvGrDay.visibility = View.VISIBLE
                binding.tvNoEntries.visibility = View.GONE
                binding.rvGrDay.layoutManager = LinearLayoutManager(this@ReportTypesCommon)
                dayReportadapter = DayReportPrintAdapter(data.line_entries)
                binding.rvGrDay.adapter = dayReportadapter

                binding.grLlContactTotalNClosingbal.visibility = View.GONE
                when (data.total_amount) {
                    "0.00" -> {
                        binding.grContactTotalFinewt.text =
                            Constants.AMOUNT_RS_APPEND + " " + data.total_amount
                        binding.grContactTotalFinewt.setTextColor(
                            ContextCompat.getColor(
                                this,
                                R.color.header_black_text
                            )
                        )
                    }

                    else -> {
                        binding.grContactTotalFinewt.text =
                            data.total_amount + " " + if (data.total_amount_term.equals(
                                    "Dr",
                                    ignoreCase = true
                                )
                            ) "Dr" else "Cr"
                        if (binding.grContactTotalFinewt.text.contains("Dr", ignoreCase = true)) {
                            binding.grContactTotalFinewt.setTextColor(
                                ContextCompat.getColor(
                                    this,
                                    R.color.debit_color
                                )
                            )
                        } else
                            binding.grContactTotalFinewt.setTextColor(
                                ContextCompat.getColor(
                                    this,
                                    R.color.credit_color
                                )
                            )
                    }
                }

                when (data.total_qty) {
                    "0.000" -> {
                        binding.grContactTotalAmount.text = data.total_qty
                        binding.grContactTotalAmount.setTextColor(
                            ContextCompat.getColor(
                                this,
                                R.color.header_black_text
                            )
                        )
                    }

                    else -> {
                        binding.grContactTotalAmount.text =
                            data.total_qty + " " + if (data.total_qty_term.equals(
                                    "Dr",
                                    ignoreCase = true
                                )
                            ) "Dr" else "Cr"
                        if (binding.grContactTotalAmount.text.contains("Dr", ignoreCase = true)) {
                            binding.grContactTotalAmount.setTextColor(
                                ContextCompat.getColor(
                                    this,
                                    R.color.debit_color
                                )
                            )
                        } else
                            binding.grContactTotalAmount.setTextColor(
                                ContextCompat.getColor(
                                    this,
                                    R.color.credit_color
                                )
                            )
                    }
                }
            }
            "0" -> {
                binding.root.imgRight.visibility = View.GONE
                binding.root.imgRight2.visibility = View.GONE

                binding.tvNoEntries.visibility = View.VISIBLE
                binding.rvGrDay.visibility = View.GONE
                binding.grLlContactTotalNClosingbal.visibility = View.GONE
            }
        }

        // gr_contact_total_finewt.text = "${Constants.AMOUNT_RS_APPEND} ${data.total_amount}"
    }

    private fun fillSalesPruchaseReportOnScreenData(
        data: SalesPurchasePrintModel.Data,
        records: String?
    ) {
        binding.rlRootReportCommon.visibility = View.GONE
        // header set
        binding.grTvCompanyname.text = loginModel.data!!.company_info!!.company_name
        binding.grTvBranchname.text = loginModel.data!!.branch_info!!.branch_name
        binding.grFromToDate.text =
            "Period: ${data.dateArray!!.from_date} to ${data.dateArray!!.to_date}"
        when (records) {
            "1" -> {
                // entries found (header)
                // imgRight.visibility = View.VISIBLE
                // imgRight2.visibility = View.VISIBLE
                checkUserRestrictionafterGeneratingReport()
                // recycler view
                binding.rvGrSalepurchase.visibility = View.VISIBLE
                binding.tvNoEntries.visibility = View.GONE
                binding.rvGrSalepurchase.layoutManager = LinearLayoutManager(this@ReportTypesCommon)
                adapter = SalePurchaseReportPrintAdapter(data.reportData)
                binding.rvGrSalepurchase.adapter = adapter

                binding.llGrGrandtotal.visibility = View.VISIBLE
                binding.grTvGrandtotalFirstcolumn.text =
                    "${Constants.NETWT_APPEND}${data.grand_total_net_wt}"
                binding.grTvGrandtotalSecondcolumn.text =
                    "${Constants.FINEWT_APPEND}${data.grand_total_fine_wt}"
                binding.grTvGrandtotalThirdcolumn.text =
                    "${Constants.AMOUNT_RS_APPEND} ${data.grand_total_amount}"
            }
            "0" -> {
                // no entries found
                binding.root.imgRight.visibility = View.GONE
                binding.root.imgRight2.visibility = View.GONE

                binding.tvNoEntries.visibility = View.VISIBLE
                binding.rvGrSalepurchase.visibility = View.GONE
                binding.llGrGrandtotal.visibility = View.GONE

                binding.grTvCompanyname.text = loginModel.data!!.company_info!!.company_name
                binding.grTvBranchname.text = loginModel.data!!.branch_info!!.branch_name
                binding.grFromToDate.text =
                    "Period: ${data.dateArray!!.from_date} to ${data.dateArray!!.to_date}"
            }
        }


    }

    private fun fillContactReportOnScreenData(
        data: ContactPrintModel.Data,
        records: String?
    ) {
        binding.rlRootReportCommon.visibility = View.GONE
        // header set
        binding.grTvCompanyname.text = loginModel.data!!.company_info!!.company_name
        binding.grTvBranchname.text = loginModel.data!!.branch_info!!.branch_name
        binding.grFromToDate.text =
            "Period: ${data.dateArray!!.from_date} to ${data.dateArray!!.to_date}"
        when (records) {
            "1" -> {
                // header openening bal row set
                //   imgRight.visibility = View.VISIBLE
                // imgRight2.visibility = View.VISIBLE
                checkUserRestrictionafterGeneratingReport()

                binding.grLlOpeningbal.visibility = View.VISIBLE

                binding.tvNoEntries.visibility = View.GONE
                when (data.reportData!!.opening_balance!!.fine_wt) {
                    "0.000" -> {
                        binding.grContactSecondcolumn.text =
                            data.reportData.opening_balance!!.fine_wt
                        binding.grContactSecondcolumn.setTextColor(
                            ContextCompat.getColor(
                                this,
                                R.color.header_black_text
                            )
                        )
                    }

                    else -> {
                        binding.grContactSecondcolumn.text =
                            data.reportData.opening_balance!!.fine_wt + " " + data.reportData.opening_balance!!.fine_wt_short_term
                        if (data.reportData.opening_balance!!.fine_wt_term.equals(
                                "Dr",
                                ignoreCase = true
                            )
                        ) {
                            binding.grContactSecondcolumn.setTextColor(
                                ContextCompat.getColor(
                                    this,
                                    R.color.debit_color
                                )
                            )
                        } else
                            binding.grContactSecondcolumn.setTextColor(
                                ContextCompat.getColor(
                                    this,
                                    R.color.credit_color
                                )
                            )
                    }
                }
                when (data.reportData.opening_balance.amount) {
                    "0.00" -> {
                        binding.grContactThirdcolumn.text =
                            Constants.AMOUNT_RS_APPEND + " " + data.reportData.opening_balance.amount
                        binding.grContactThirdcolumn.setTextColor(
                            ContextCompat.getColor(
                                this,
                                R.color.header_black_text
                            )
                        )
                    }

                    else -> {
                        binding.grContactThirdcolumn.text =
                            data.reportData.opening_balance.amount + " " + data.reportData.opening_balance!!.amount_short_term
                        if (data.reportData.opening_balance!!.amount_term.equals(
                                "Dr",
                                ignoreCase = true
                            )
                        ) {
                            gr_contact_thirdcolumn.setTextColor(
                                ContextCompat.getColor(
                                    this,
                                    R.color.debit_color
                                )
                            )
                        } else
                            binding.grContactThirdcolumn.setTextColor(
                                ContextCompat.getColor(
                                    this,
                                    R.color.credit_color
                                )
                            )
                    }
                }

                //Ledger Opening balance
                if (data.reportData.create_opening_balance!!.equals("")) {
                    binding.grLlOpeningbalLedger.visibility = View.GONE
                } else {
                    binding.grLlOpeningbalLedger.visibility = View.VISIBLE
                    binding.grLedgerFirstcolumn.text =
                        data.reportData.create_opening_balance!!.series
                    binding.grLedgerSecondcolumn.text =
                        data.reportData.create_opening_balance!!.fine_wt + " " + data.reportData.create_opening_balance!!.fine_wt_short_term
                    if (data.reportData.create_opening_balance.fine_wt_term.equals(
                            "Dr",
                            ignoreCase = true
                        )
                    ) {
                        binding.grLedgerSecondcolumn.setTextColor(
                            ContextCompat.getColor(
                                this,
                                R.color.debit_color
                            )
                        )
                    } else {
                        binding.grLedgerSecondcolumn.setTextColor(
                            ContextCompat.getColor(
                                this,
                                R.color.credit_color
                            )
                        )
                    }
                    binding.grLedgerThirdcolumn.text =
                        data.reportData.create_opening_balance!!.amount + " " + data.reportData.create_opening_balance!!.amount_short_term
                    if (data.reportData.create_opening_balance.amount_term.equals(
                            "Dr",
                            ignoreCase = true
                        )
                    ) {
                        binding.grLedgerThirdcolumn.setTextColor(
                            ContextCompat.getColor(
                                this,
                                R.color.debit_color
                            )
                        )
                    } else {
                        binding.grLedgerThirdcolumn.setTextColor(
                            ContextCompat.getColor(
                                this,
                                R.color.credit_color
                            )
                        )
                    }
                }

                // recyclerview set
                binding.rvGrContact.visibility = View.VISIBLE
                binding.rvGrContact.layoutManager = LinearLayoutManager(this@ReportTypesCommon)
                contactReportadapter = ContactReportPrintAdapter(
                    data.reportData.transactions, null, null,
                    null, "1"
                )
                binding.rvGrContact.adapter = contactReportadapter
                // total n closing bal set
                binding.grLlContactTotalNClosingbal.visibility = View.VISIBLE
                binding.grLlContactTotalNClosingbalSecondrow.visibility = View.VISIBLE
                binding.grContactTotalFinewt.text =
                    "${Constants.FINEWT_APPEND}${data.reportData.total_fine_wt}"
                binding.grContactTotalAmount.text =
                    "${Constants.AMOUNT_RS_APPEND} ${data.reportData.total_amount}"

                when (data.reportData.closing_fine_wt) {
                    "0.000" -> {
                        binding.grContactClosingFinewt.text =
                            "${Constants.FINEWT_APPEND}${data.reportData.closing_fine_wt}"
                        binding.grContactClosingFinewt.setTextColor(
                            ContextCompat.getColor(
                                this,
                                R.color.header_black_text
                            )
                        )
                    }

                    else -> {
                        binding.grContactClosingFinewt.text =
                            "${Constants.FINEWT_APPEND}${data.reportData.closing_fine_wt}" + " " + data.reportData.closing_fine_wt_short_term
                        if (data.reportData.closing_fine_wt_term.equals("Dr", ignoreCase = true)) {
                            binding.grContactClosingFinewt.setTextColor(
                                ContextCompat.getColor(
                                    this,
                                    R.color.debit_color
                                )
                            )
                        } else
                            binding.grContactClosingFinewt.setTextColor(
                                ContextCompat.getColor(
                                    this,
                                    R.color.credit_color
                                )
                            )
                    }
                }
                when (data.reportData.closing_amount) {
                    "0.00" -> {
                        binding.grContactClosingAmount.text =
                            Constants.AMOUNT_RS_APPEND + " " + data.reportData.closing_amount
                        binding.grContactClosingAmount.setTextColor(
                            ContextCompat.getColor(
                                this,
                                R.color.header_black_text
                            )
                        )
                    }

                    else -> {
                        binding.grContactClosingAmount.text =
                            Constants.AMOUNT_RS_APPEND + " " + data.reportData.closing_amount + " " + data.reportData.closing_amount_short_term
                        if (data.reportData.closing_amount_term.equals("Dr", ignoreCase = true)) {
                            binding.grContactClosingAmount.setTextColor(
                                ContextCompat.getColor(
                                    this,
                                    R.color.debit_color
                                )
                            )
                        } else
                            binding.grContactClosingAmount.setTextColor(
                                ContextCompat.getColor(
                                    this,
                                    R.color.credit_color
                                )
                            )
                    }
                }


            }
            "0" -> {
                binding.root.imgRight.visibility = View.GONE
                binding.root.imgRight2.visibility = View.GONE
                binding.tvNoEntries.visibility = View.VISIBLE
                binding.grLlOpeningbal.visibility = View.GONE
                binding.rvGrContact.visibility = View.GONE
                binding.grLlContactTotalNClosingbal.visibility = View.GONE
            }
        }
    }

    private fun checkUserRestrictionafterGeneratingReport() {
        when (loginModel.data!!.user_info!!.user_type.equals("user", true)) {
            // user type user
            true -> {
                // apply restriciton to "user" ignore for "admin/superadmin" etc
                if (this::data.isInitialized) {
                    applyUserWiseRestriction(this.data)
                }

            }
            false -> {
                defaultEnableAllButtonnUI()
            }
        }
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

    // Contact Validation
    fun performValidationContact(): Boolean {
        if (selectedContactID.isNullOrBlank()) {
            CommonUtils.showDialog(
                this,
                getString(R.string.select_contact_first_msg)/*"Please Select Contact"*/
            )
            binding.txtRepoTSelContact.requestFocus()
            return false
        }
        return true
    }

    // Stock Validation
    fun performValidationStock(): Boolean {
        /* if (all_item_categories.equals("0") && item_category_id.isNullOrBlank()) {
             CommonUtils.showDialog(this, getString(R.string.select_item_category_msg))
             binding.txtRepoTSelItemCat.requestFocus()
             return false
         } else*/ if (all_items.equals("0") && item_id.isNullOrBlank()) {
            CommonUtils.showDialog(this, getString(R.string.select_item_msg))
            binding.txtRepoTSelItems.requestFocus()
            return false
        }
        return true
    }

    // Sale Validation
    fun performValidationSales(): Boolean {
        if (all_contacts.equals("0") && selectedContactID.isNullOrBlank()) {
            CommonUtils.showDialog(
                this,
                getString(R.string.select_contact_first_msg)/*"Please Select Contact"*/
            )
            binding.txtRepoTSalesSelContact.requestFocus()
            return false
        }
        return true
    }

    // Purchase Validation
    fun performValidationPurchase(): Boolean {
        if (all_contacts.equals("0") && selectedContactID.isNullOrBlank()) {
            CommonUtils.showDialog(
                this,
                getString(R.string.select_contact_first_msg)/*"Please Select Contact"*/
            )
            binding.txtRepoTPurchaseSelContact.requestFocus()
            return false
        }
        return true
    }

    fun performValidationLedger(): Boolean {
        if (all_ledgers.equals("0") && tcs_ledger_id.isNullOrBlank()) {
            CommonUtils.showDialog(this, getString(R.string.select_all_ledger_msg))
            binding.txtRepoTLedger.requestFocus()
            return false
        }
        return true
    }

    // Cash Bank Validation
    fun performValidationCashBank(): Boolean {
        if (all_cash_ledgers.equals("0") && cash_ledger_id.isNullOrBlank() && all_bank_ledgers.equals(
                "0"
            ) && bank_ledger_id.isNullOrBlank()
        ) {
            CommonUtils.showDialog(this, getString(R.string.select_all_ledger_msg))
            binding.txtRepoTSelCashLedger.requestFocus()
            return false
        } /*else if (all_bank_ledgers.equals("0") && bank_ledger_id.isNullOrBlank()) {
            CommonUtils.showDialog(this, getString(R.string.select_bank_ledger_msg))
            binding.txtRepoTSelBankLedger.requestFocus()
            return false
        }*/
        return true
    }


    // Contact Code n Functions Start
    private fun showContactsFieldsnSetCode() {
        type_of_contact = ""
        all_contacts = "0"

        // call from contact report(direct call as no radio buttons in contact(only search contact so direct call api search contact)
        getReportSupportContacts(binding.txtRepoTSelContact.text.toString())
        // set current month first and last date default (in from and to date)
        binding.txtRepoTContactFromDate.setText(
            SimpleDateFormat("dd-MMM-yy").format(
                getFirstDateOfMonth(
                    Date()
                )
            )
        )
        binding.txtRepoTContactToDate.setText(
            SimpleDateFormat("dd-MMM-yy").format(
                getLastDateOfMonth(Date())
            )
        )

        binding.txtRepoTContactFromDate.clickWithDebounce {

            openDatePicker(reportsType, true)
        }
        binding.txtRepoTContactToDate.clickWithDebounce {

            openDatePicker(reportsType, false)
        }

        binding.llRepoTContact.setOnFocusChangeListener(View.OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                hideKeyboardnew(this);
            }
        })
    }


    // Contact Code n Functions End
    // Stock Code n Functions Start
    private fun showStockFieldsnSetCode() {
        ly_stock_all_item.visibility = View.GONE
        tvRepoTSelItems.visibility = View.GONE
        txtRepoTStockFromDate.isEnabled = false
        txtRepoTStockToDate.isEnabled = false
        /*getSearchItemCategories(txtRepoTSelItemCat.text.toString())
        callItemCatApiAgain()
        switchAllItemCategories.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                tvRepoTSelItemCat.visibility = View.GONE
                all_item_categories = "1"
                txtRepoTSelItems.setText("")
                txtRepoTSelItemCat.setText("")
                item_category_id = ""
                item_id = ""
                callItemCatApiAgain()
            } else {
                all_item_categories = "0"
                callItemCatApiAgain()
                tvRepoTSelItemCat.visibility = View.VISIBLE

            }
        }*/

        /*switchAllItems.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                all_items = "1"
                callItemsApiAgain()
                txtRepoTSelItems.setText("")
                tvRepoTSelItems.visibility = View.GONE

            } else {
                all_items = "0"
                callItemsApiAgain()
                tvRepoTSelItems.visibility = View.VISIBLE

            }
        }*/

        // set current month first and last date default (in from and to date)
        txtRepoTStockFromDate.setText(SimpleDateFormat("dd-MMM-yy").format(Date()))
        txtRepoTStockToDate.setText(SimpleDateFormat("dd-MMM-yy").format(Date()))
        txtRepoTStockFromDate.clickWithDebounce {

            openDatePicker(reportsType, true)
        }
        txtRepoTStockToDate.clickWithDebounce {

            openDatePicker(reportsType, false)
        }
        llRepoTStock.setOnFocusChangeListener(View.OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                hideKeyboardnew(this);
            }
        })
    }

    private fun callItemsApiAgain() {
        when (all_items) {
            "0" -> {
                getSearchItems(txtRepoTSelItems.text.toString(), "")
                /* if (item_category_id != null) {
                     getSearchItems(txtRepoTSelItems.text.toString(), item_category_id.toString())
                 } else getSearchItems(txtRepoTSelItems.text.toString(), "")*/
            }
            "1" -> {
                // nothing to do if all item radio select
                item_id = ""
            }
        }
    }

    private fun callItemCatApiAgain() {
        when (all_item_categories) {
            "0" -> {
                if (item_category_id != null) {
                    getSearchItems(txtRepoTSelItems.text.toString(), item_category_id.toString())
                } else getSearchItems(txtRepoTSelItems.text.toString(), "")
            }
            "1" -> getSearchItems(txtRepoTSelItems.text.toString(), "")
        }
    }

    // Call from Stock report when all item categories radio off
    fun getSearchItemCategories(searchCatString: String?) {

        if (NetworkUtils.isConnected()) {

            viewModel.getReportsItemCategories(
                loginModel?.data?.bearer_access_token,
                searchCatString
            )
                .observe(this, androidx.lifecycle.Observer {
                    it?.let { resource ->
                        when (resource.status) {
                            Status.SUCCESS -> {
                                if (it.data?.status == true) {
                                    itemCatList = it.data?.data!!
                                    itemCatNameList =
                                        itemCatList?.map { it.category_name.toString() }

                                    categoryNameAdapter = ArrayAdapter<String>(
                                        this,
                                        android.R.layout.simple_dropdown_item_1line,
                                        itemCatNameList!!
                                    )


                                    txtRepoTSelItemCat.setAdapter(categoryNameAdapter)
                                    txtRepoTSelItemCat.threshold = 1
                                    txtRepoTSelItemCat.setOnItemClickListener { adapterView, _, position, _
                                        ->
                                        val selected: String =
                                            adapterView.getItemAtPosition(position).toString()
                                        val pos: Int? = itemCatNameList?.indexOf(selected)

                                        item_category_id =
                                            pos?.let { it1 ->
                                                itemCatList.get(it1).item_category_id
                                            }
                                        txtRepoTSelItems.setText("")
                                        item_id = ""
                                        getSearchItems(
                                            txtRepoTSelItems.text.toString(),
                                            item_category_id.toString()
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

    // Call from Stock report when all items radio off
    private fun getSearchItems(searchItemString: String, item_category_id: String) {
        if (NetworkUtils.isConnected()) {

            viewModel.getReportsItems(
                loginModel?.data?.bearer_access_token,
                searchItemString,
                item_category_id
            )
                .observe(this, androidx.lifecycle.Observer {
                    it?.let { resource ->
                        when (resource.status) {
                            Status.SUCCESS -> {
                                if (it.data?.status == true) {
                                    itemsList = arrayListOf()
                                    itemsList = it.data?.data!!
                                    itemNameList = itemsList?.map { it.item_name.toString() }

                                    itemNameAdapter = ArrayAdapter<String>(
                                        this,
                                        android.R.layout.simple_dropdown_item_1line,
                                        itemNameList!!
                                    )


                                    txtRepoTSelItems.setAdapter(itemNameAdapter)
                                    txtRepoTSelItems.threshold = 1
                                    txtRepoTSelItems.setOnItemClickListener { adapterView, _, position, _
                                        ->
                                        val selected: String =
                                            adapterView.getItemAtPosition(position).toString()
                                        val pos: Int? = itemNameList?.indexOf(selected)

                                        item_id =
                                            pos?.let { it1 -> itemsList?.get(it1)?.item_id }

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

    // Stock Code n Functions End
    // Sales Code n Functions Start
    private fun showSalesFieldsnSetCode() {
        // call from sales report
        getReportSupportContacts(txtRepoTSalesSelContact.text.toString())
        switchSalesAllContacts.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                //  contactAllcontactsisChecked = true
                all_contacts = "1"
                selectedContactID = ""
                tvRepoTSalesTOC.visibility = View.VISIBLE

                rGRepoTSalesTypeOfContact.visibility = View.VISIBLE
                tvRepoTSalesSelContact.visibility = View.GONE
                txtRepoTSalesSelContact.setText("")
                updateTypeofContactSales()
            } else {
                //  contactAllcontactsisChecked = false
                all_contacts = "0"
                //type_of_contact = ""
                updateTypeofContactSales()
                tvRepoTSalesSelContact.visibility = View.VISIBLE
                tvRepoTSalesTOC.visibility = View.GONE
                rGRepoTSalesTypeOfContact.visibility = View.GONE
            }
        }

        // set current month first and last date default (in from and to date)
        txtRepoTSalesFromDate.setText(SimpleDateFormat("dd-MMM-yy").format(getFirstDateOfMonth(Date())))
        txtRepoTSalesToDate.setText(SimpleDateFormat("dd-MMM-yy").format(getLastDateOfMonth(Date())))
        txtRepoTSalesFromDate.clickWithDebounce {

            openDatePicker(reportsType, true)
        }
        txtRepoTSalesToDate.clickWithDebounce {

            openDatePicker(reportsType, false)
        }

        llRepoTSale.setOnFocusChangeListener(View.OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                hideKeyboardnew(this);
            }
        })
    }

    private fun updateTypeofContactSales() {
        when (all_contacts) {
            "0" -> {
                type_of_contact = ""
            }
            "1" -> {
                selectedContactID = ""
                rGRepoTSalesTypeOfContact.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { radioGroup, checkedId ->
                    when (checkedId) {
                        rbRepoTSalesAllCust.id -> {
                            type_of_contact = "customer"
                        }
                        rbRepoTSalesAllSupp.id -> {
                            type_of_contact = "vendor"

                        }
                        rbRepoTSalesBoth.id -> {
                            type_of_contact = "contact"
                        }
                    }
                })
            }
        }
    }

    // Sales Code n Functions End
    // Purchase Code n Functions Start
    private fun showPurchaseCutFieldsnSetCode() {
        // call from purchase report
        getReportSupportContacts(txtRepoTPurchaseSelContact.text.toString())
        switchPurchaseAllContacts.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                // all contacts true (no need of search contact)
                //contactAllcontactsisChecked = true
                all_contacts = "1"
                selectedContactID = ""
                tvRepoTPurchaseTOC.visibility = View.VISIBLE
                rGRepoTPurchaseTypeOfContact.visibility = View.VISIBLE
                tvRepoTPurchaseSelContact.visibility = View.GONE
                txtRepoTPurchaseSelContact.setText("")
                updateTypeofContactPurchase()
            } else {

                all_contacts = "0"
                //type_of_contact = ""
                updateTypeofContactPurchase()
                tvRepoTPurchaseSelContact.visibility = View.VISIBLE
                tvRepoTPurchaseTOC.visibility = View.GONE
                rGRepoTPurchaseTypeOfContact.visibility = View.GONE
            }
        }

        // set current month first and last date default (in from and to date)
        txtRepoTPurchaseFromDate.setText(
            SimpleDateFormat("dd-MMM-yy").format(
                getFirstDateOfMonth(
                    Date()
                )
            )
        )
        txtRepoTPurchaseToDate.setText(SimpleDateFormat("dd-MMM-yy").format(getLastDateOfMonth(Date())))
        txtRepoTPurchaseFromDate.clickWithDebounce {

            openDatePicker(reportsType, true)
        }
        txtRepoTPurchaseToDate.clickWithDebounce {

            openDatePicker(reportsType, false)
        }

        llRepoTPurchase.setOnFocusChangeListener(View.OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                hideKeyboardnew(this);
            }
        })
    }

    private fun updateTypeofContactPurchase() {
        when (all_contacts) {
            "0" -> {
                type_of_contact = ""
            }
            "1" -> {
                selectedContactID = ""
                rGRepoTPurchaseTypeOfContact.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { radioGroup, checkedId ->
                    when (checkedId) {
                        rbRepoTPurchaseAllCust.id -> {
                            type_of_contact = "customer"
                        }
                        rbRepoTPurchaseAllSupp.id -> {
                            type_of_contact = "vendor"

                        }
                        rbRepoTPurchaseBoth.id -> {
                            type_of_contact = "contact"
                        }
                    }
                })
            }
        }
    }

    // Purchase Code n Functions End
    // Daybook Code n Functions Start
    private fun showDaybookFieldsnSetCode() {
        // set todays date in from and to date Default.
        txtRepoTDayFromDate.setText(SimpleDateFormat("dd-MMM-yy").format(Date()))
        txtRepoTDayToDate.setText(SimpleDateFormat("dd-MMM-yy").format(Date()))
        txtRepoTDayFromDate.clickWithDebounce {

            openDatePicker(reportsType, true)
        }
        txtRepoTDayToDate.clickWithDebounce {

            openDatePicker(reportsType, false)
        }
        // default today and date set
        txtRepoTDayPeriod.setText("Today")

        txtRepoTDayPeriod.clickWithDebounce {

            openPeriodPopup(txtRepoTDayPeriod)
        }

    }

    fun openPeriodPopup(view: View) {
        val popupMenu: PopupMenu = PopupMenu(this, view)
        popupMenu.menu.add(Menu.NONE, 1, 1, "Today") //add(groupId, itemId, order, title);
        popupMenu.menu.add(Menu.NONE, 2, 2, "This Week")
        popupMenu.menu.add(Menu.NONE, 3, 3, "This Month")
        popupMenu.menu.add(Menu.NONE, 4, 4, "This Year")
        popupMenu.menu.add(Menu.NONE, 5, 5, "Custom")
        popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
            txtRepoTDayPeriod.setText(item.title)
            when (item.itemId.toString()) {
                "1" -> {
                    // today (this is default)
                    tvRepoTDayFromDate.isEnabled = false
                    tvRepoTDayToDate.isEnabled = false
                    txtRepoTDayFromDate.setText(SimpleDateFormat("dd-MMM-yy").format(Date()))
                    txtRepoTDayToDate.setText(SimpleDateFormat("dd-MMM-yy").format(Date()))
                }
                "2" -> {
                    //this week
                    tvRepoTDayFromDate.isEnabled = false
                    tvRepoTDayToDate.isEnabled = false
                    txtRepoTDayFromDate.setText(
                        SimpleDateFormat("dd-MMM-yy").format(
                            getFirstDateOfWeek(
                                Date()
                            )
                        )
                    )
                    txtRepoTDayToDate.setText(
                        SimpleDateFormat("dd-MMM-yy").format(
                            getLastDateOfWeek(
                                Date()
                            )
                        )
                    )

                }
                "3" -> {
                    // this month
                    tvRepoTDayFromDate.isEnabled = false
                    tvRepoTDayToDate.isEnabled = false
                    txtRepoTDayFromDate.setText(
                        SimpleDateFormat("dd-MMM-yy").format(
                            getFirstDateOfMonth(
                                Date()
                            )
                        )
                    )
                    txtRepoTDayToDate.setText(
                        SimpleDateFormat("dd-MMM-yy").format(
                            getLastDateOfMonth(
                                Date()
                            )
                        )
                    )

                }
                "4" -> {
                    //this year
                    tvRepoTDayFromDate.isEnabled = false
                    tvRepoTDayToDate.isEnabled = false
                    txtRepoTDayFromDate.setText(
                        SimpleDateFormat("dd-MMM-yy").format(
                            getFirstDateOfYear(
                                Date()
                            )
                        )
                    )
                    txtRepoTDayToDate.setText(
                        SimpleDateFormat("dd-MMM-yy").format(
                            getLastDateOfYear(
                                Date()
                            )
                        )
                    )
                }
                "5" -> {
                    tvRepoTDayFromDate.isEnabled = true
                    tvRepoTDayToDate.isEnabled = true
                }
            }
            true
        })

        popupMenu.show()
    }

    fun getFirstDateOfWeek(date: Date?): Date? {
        val cal = Calendar.getInstance()
        cal.time = date
        cal.add(
            Calendar.DAY_OF_MONTH,
            Calendar.MONDAY - cal[Calendar.DAY_OF_WEEK]
        )
        return cal.time
    }

    fun getLastDateOfWeek(date: Date?): Date? {
        val cal = Calendar.getInstance()
        cal.time = date
        var day = cal[Calendar.DAY_OF_YEAR]
        while (cal[Calendar.DAY_OF_WEEK] !== Calendar.SUNDAY) {
            cal[Calendar.DAY_OF_YEAR] = ++day
        }
        return cal.time
    }

    fun getFirstDateOfMonth(date: Date?): Date? {
        val cal = Calendar.getInstance()
        cal.time = date
        cal[Calendar.DAY_OF_MONTH] = cal.getActualMinimum(Calendar.DAY_OF_MONTH)
        return cal.time
    }

    fun getLastDateOfMonth(date: Date?): Date? {
        val cal = Calendar.getInstance()
        cal.time = date
        cal[Calendar.DAY_OF_MONTH] = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        return cal.time
    }

    fun getFirstDateOfYear(date: Date?): Date? {
        val cal = Calendar.getInstance()
        cal.time = date
        cal[Calendar.DAY_OF_YEAR] = cal.getActualMinimum(Calendar.DAY_OF_YEAR)
        return cal.time
    }

    fun getLastDateOfYear(date: Date?): Date? {
        val cal = Calendar.getInstance()
        cal.time = date
        cal[Calendar.DAY_OF_YEAR] = cal.getActualMaximum(Calendar.DAY_OF_YEAR)
        return cal.time
    }

    // Daybook Code n Functions End
    // CashBank Code n Functions Start
    private fun showCashbookFieldsnSetCode() {
        getLedgerDetails("cashbank")
        // getLedgerDetails("bank")

        switchAllCashLedgers.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                all_cash_ledgers = "1"
                cash_ledger_id = ""
                txtRepoTSelCashLedger.setText("")
                tvRepoTSelCashLedger.visibility = View.GONE
            } else {
                all_cash_ledgers = "0"
                tvRepoTSelCashLedger.visibility = View.VISIBLE
            }
        }

        switchAllBankLedgers.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                all_bank_ledgers = "1"
                bank_ledger_id = ""
                txtRepoTSelBankLedger.setText("")
                tvRepoTSelBankLedger.visibility = View.GONE

            } else {
                all_bank_ledgers = "0"

                tvRepoTSelBankLedger.visibility = View.VISIBLE
            }
        }

        // set current month first and last date default (in from and to date)
        txtRepoTCashBankFromDate.setText(
            SimpleDateFormat("dd-MMM-yy").format(
                getFirstDateOfMonth(
                    Date()
                )
            )
        )
        txtRepoTCashBankToDate.setText(SimpleDateFormat("dd-MMM-yy").format(getLastDateOfMonth(Date())))
        txtRepoTCashBankFromDate.clickWithDebounce {

            openDatePicker(reportsType, true)
        }
        txtRepoTCashBankToDate.clickWithDebounce {

            openDatePicker(reportsType, false)
        }

        llRepoTCashBank.setOnFocusChangeListener(View.OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                hideKeyboardnew(this);
            }
        })
    }
    // CashBank Code n Functions End

    //Ledger code n Function Start

    private fun showLedgerFieldsnSetCode() {
        getLedgerDetails(allLedgerType)

        switchAllLedgers.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                all_ledgers = "1"
                tcs_ledger_id = ""
                txtRepoTLedger.setText("")
                tvRepoTLedger.visibility = View.GONE
            } else {
                all_ledgers = "0"
                tvRepoTLedger.visibility = View.VISIBLE
            }
        }

        // set current month first and last date default (in from and to date)
        txtRepoTLedgerFromDate.setText(
            SimpleDateFormat("dd-MMM-yy").format(
                getFirstDateOfMonth(
                    Date()
                )
            )
        )
        txtRepoTLedgerToDate.setText(SimpleDateFormat("dd-MMM-yy").format(getLastDateOfMonth(Date())))
        txtRepoTLedgerFromDate.clickWithDebounce {

            openDatePicker(reportsType, true)
        }
        txtRepoTLedgerToDate.clickWithDebounce {
            openDatePicker(reportsType, false)
        }

        llRepoTLedger.setOnFocusChangeListener(View.OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                hideKeyboardnew(this);
            }
        })
    }

    fun openDatePicker(reportsType: String?, isFromDate: Boolean) {
//        val c = Calendar.getInstance()
        val sdf = SimpleDateFormat("dd-MMM-yy")
        var parse = Date()
        when (isFromDate) {
            // from date(for all reporttypes)
            true -> {
                when (reportsType) {

                    "contact" -> {
                        parse = sdf.parse(txtRepoTContactFromDate.text.toString())
                    }
                    "summary_contact" -> {
                        parse = sdf.parse(txtRepoTContactFromDate.text.toString())
                    }
                    "total_stock_summary" -> {
                        parse = sdf.parse(txtRepoTStockFromDate.text.toString())
                    }
                    "sales" -> {
                        parse = sdf.parse(txtRepoTSalesFromDate.text.toString())
                    }
                    "purchase" -> {
                        parse = sdf.parse(txtRepoTPurchaseFromDate.text.toString())
                    }
                    "day" -> {
                        parse = sdf.parse(txtRepoTDayFromDate.text.toString())
                    }
                    "cashbank" -> {
                        parse = sdf.parse(txtRepoTCashBankFromDate.text.toString())
                    }
                    "ledger" -> {
                        parse = sdf.parse(txtRepoTLedgerFromDate.text.toString())
                    }
                    "tagged_stock_summary" -> {
                        parse = sdf.parse(txtRepoTStockFromDate.text.toString())
                    }
                    "stock_item_details" -> {
                        parse = sdf.parse(txtRepoTStockFromDate.text.toString())
                    }
                }
                storeparse = parse
            }
            // To date(for all reporttypes)
            false -> {
                when (reportsType) {

                    "contact" -> {
                        parse = sdf.parse(txtRepoTContactToDate.text.toString())
                    }
                    "summary_contact" -> {
                        parse = sdf.parse(txtRepoTContactToDate.text.toString())
                    }
                    "total_stock_summary" -> {
                        parse = sdf.parse(txtRepoTStockToDate.text.toString())
                    }
                    "sales" -> {
                        parse = sdf.parse(txtRepoTSalesToDate.text.toString())
                    }
                    "purchase" -> {
                        parse = sdf.parse(txtRepoTPurchaseToDate.text.toString())
                    }
                    "day" -> {
                        parse = sdf.parse(txtRepoTDayToDate.text.toString())
                    }
                    "cashbank" -> {
                        parse = sdf.parse(txtRepoTCashBankToDate.text.toString())
                    }
                    "ledger" -> {
                        parse = sdf.parse(txtRepoTLedgerToDate.text.toString())
                    }
                    "tagged_stock_summary" -> {
                        parse = sdf.parse(txtRepoTStockToDate.text.toString())
                    }
                    "stock_item_details" -> {
                        parse = sdf.parse(txtRepoTStockFromDate.text.toString())
                    }
                }
            }
        }
        c.setTime(parse)


        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)


        val dpd = DatePickerDialog(
            this,
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->

                c.set(Calendar.YEAR, year)
                c.set(Calendar.MONTH, monthOfYear)
                c.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                // Display Selected date in textbox
                when (isFromDate) {
                    // from date(for all reporttypes)
                    true -> {
                        isFromDateSelected = true
                        when (reportsType) {
                            "contact" -> {
                                txtRepoTContactFromDate.setText(
                                    "" + String.format(
                                        "%02d",
                                        dayOfMonth
                                    ) + "-" + SimpleDateFormat("MMM").format(c.time) + "-" + year.toString()
                                        .substring(2, 4)
                                )
                                parse = sdf.parse(txtRepoTContactFromDate.text.toString())
                                storeparse = parse
                                if (parse.after(sdf.parse(txtRepoTContactToDate.text.toString()))) {
                                    txtRepoTContactToDate.setText(txtRepoTContactFromDate.text)
                                }
                            }
                            "summary_contact" -> {
                                txtRepoTContactFromDate.setText(
                                    "" + String.format(
                                        "%02d",
                                        dayOfMonth
                                    ) + "-" + SimpleDateFormat("MMM").format(c.time) + "-" + year.toString()
                                        .substring(2, 4)
                                )
                                parse = sdf.parse(txtRepoTContactFromDate.text.toString())
                                storeparse = parse
                                if (parse.after(sdf.parse(txtRepoTContactToDate.text.toString()))) {
                                    txtRepoTContactToDate.setText(txtRepoTContactFromDate.text)
                                }
                            }
                            "total_stock_summary" -> {
                                txtRepoTStockFromDate.setText(
                                    "" + String.format(
                                        "%02d",
                                        dayOfMonth
                                    ) + "-" + SimpleDateFormat("MMM").format(c.time) + "-" + year.toString()
                                        .substring(2, 4)
                                )
                                parse = sdf.parse(txtRepoTStockFromDate.text.toString())
                                storeparse = parse
                                if (parse.after(sdf.parse(txtRepoTStockToDate.text.toString()))) {
                                    txtRepoTStockToDate.setText(txtRepoTStockFromDate.text)
                                }
                            }
                            "sales" -> {
                                txtRepoTSalesFromDate.setText(
                                    "" + String.format(
                                        "%02d",
                                        dayOfMonth
                                    ) + "-" + SimpleDateFormat("MMM").format(c.time) + "-" + year.toString()
                                        .substring(2, 4)
                                )
                                parse = sdf.parse(txtRepoTSalesFromDate.text.toString())
                                storeparse = parse
                                if (parse.after(sdf.parse(txtRepoTSalesToDate.text.toString()))) {
                                    txtRepoTSalesToDate.setText(txtRepoTSalesFromDate.text)
                                }
                            }
                            "purchase" -> {
                                txtRepoTPurchaseFromDate.setText(
                                    "" + String.format(
                                        "%02d",
                                        dayOfMonth
                                    ) + "-" + SimpleDateFormat("MMM").format(c.time) + "-" + year.toString()
                                        .substring(2, 4)
                                )
                                parse = sdf.parse(txtRepoTPurchaseFromDate.text.toString())
                                storeparse = parse
                                if (parse.after(sdf.parse(txtRepoTPurchaseToDate.text.toString()))) {
                                    txtRepoTPurchaseToDate.setText(txtRepoTPurchaseFromDate.text)
                                }
                            }
                            "day" -> {
                                txtRepoTDayFromDate.setText(
                                    "" + String.format(
                                        "%02d",
                                        dayOfMonth
                                    ) + "-" + SimpleDateFormat("MMM").format(c.time) + "-" + year.toString()
                                        .substring(2, 4)
                                )
                                parse = sdf.parse(txtRepoTDayFromDate.text.toString())
                                storeparse = parse
                                if (parse.after(sdf.parse(txtRepoTDayToDate.text.toString()))) {
                                    txtRepoTDayToDate.setText(txtRepoTDayFromDate.text)
                                }
                            }
                            "cashbank" -> {
                                txtRepoTCashBankFromDate.setText(
                                    "" + String.format(
                                        "%02d",
                                        dayOfMonth
                                    ) + "-" + SimpleDateFormat("MMM").format(c.time) + "-" + year.toString()
                                        .substring(2, 4)
                                )
                                parse = sdf.parse(txtRepoTCashBankFromDate.text.toString())
                                storeparse = parse
                                if (parse.after(sdf.parse(txtRepoTCashBankToDate.text.toString()))) {
                                    txtRepoTCashBankToDate.setText(txtRepoTCashBankFromDate.text)
                                }
                            }
                            //ledger
                            "ledger" -> {
                                txtRepoTLedgerFromDate.setText(
                                    "" + String.format(
                                        "%02d",
                                        dayOfMonth
                                    ) + "-" + SimpleDateFormat("MMM").format(c.time) + "-" + year.toString()
                                        .substring(2, 4)
                                )
                                parse = sdf.parse(txtRepoTLedgerFromDate.text.toString())
                                storeparse = parse
                                if (parse.after(sdf.parse(txtRepoTLedgerToDate.text.toString()))) {
                                    txtRepoTLedgerToDate.setText(txtRepoTLedgerFromDate.text)
                                }
                            }
                            "tagged_stock_summary" -> {
                                txtRepoTStockFromDate.setText(
                                    "" + String.format(
                                        "%02d",
                                        dayOfMonth
                                    ) + "-" + SimpleDateFormat("MMM").format(c.time) + "-" + year.toString()
                                        .substring(2, 4)
                                )
                                parse = sdf.parse(txtRepoTStockFromDate.text.toString())
                                storeparse = parse
                                if (parse.after(sdf.parse(txtRepoTStockToDate.text.toString()))) {
                                    txtRepoTStockToDate.setText(txtRepoTStockFromDate.text)
                                }
                            }
                            "stock_item_details" -> {
                                txtRepoTStockFromDate.setText(
                                    "" + String.format(
                                        "%02d",
                                        dayOfMonth
                                    ) + "-" + SimpleDateFormat("MMM").format(c.time) + "-" + year.toString()
                                        .substring(2, 4)
                                )
                                parse = sdf.parse(txtRepoTStockFromDate.text.toString())
                                storeparse = parse
                                if (parse.after(sdf.parse(txtRepoTStockToDate.text.toString()))) {
                                    txtRepoTStockToDate.setText(txtRepoTStockFromDate.text)
                                }
                            }
                        }
                    }
                    // To date(for all reporttypes)
                    false -> {
                        when (reportsType) {

                            "contact" -> {
                                txtRepoTContactToDate.setText(
                                    "" + String.format(
                                        "%02d",
                                        dayOfMonth
                                    ) + "-" + SimpleDateFormat("MMM").format(c.time) + "-" + year.toString()
                                        .substring(2, 4)
                                )
                            }
                            "summary_contact" -> {
                                txtRepoTContactToDate.setText(
                                    "" + String.format(
                                        "%02d",
                                        dayOfMonth
                                    ) + "-" + SimpleDateFormat("MMM").format(c.time) + "-" + year.toString()
                                        .substring(2, 4)
                                )
                            }
                            "total_stock_summary" -> {
                                txtRepoTStockToDate.setText(
                                    "" + String.format(
                                        "%02d",
                                        dayOfMonth
                                    ) + "-" + SimpleDateFormat("MMM").format(c.time) + "-" + year.toString()
                                        .substring(2, 4)
                                )
                            }
                            "sales" -> {
                                txtRepoTSalesToDate.setText(
                                    "" + String.format(
                                        "%02d",
                                        dayOfMonth
                                    ) + "-" + SimpleDateFormat("MMM").format(c.time) + "-" + year.toString()
                                        .substring(2, 4)
                                )
                            }
                            "purchase" -> {
                                txtRepoTPurchaseToDate.setText(
                                    "" + String.format(
                                        "%02d",
                                        dayOfMonth
                                    ) + "-" + SimpleDateFormat("MMM").format(c.time) + "-" + year.toString()
                                        .substring(2, 4)
                                )
                            }
                            "day" -> {
                                txtRepoTDayToDate.setText(
                                    "" + String.format(
                                        "%02d",
                                        dayOfMonth
                                    ) + "-" + SimpleDateFormat("MMM").format(c.time) + "-" + year.toString()
                                        .substring(2, 4)
                                )
                            }
                            "cashbank" -> {
                                txtRepoTCashBankToDate.setText(
                                    "" + String.format(
                                        "%02d",
                                        dayOfMonth
                                    ) + "-" + SimpleDateFormat("MMM").format(c.time) + "-" + year.toString()
                                        .substring(2, 4)
                                )
                            }
//ledger
                            "ledger" -> {
                                txtRepoTLedgerToDate.setText(
                                    "" + String.format(
                                        "%02d",
                                        dayOfMonth
                                    ) + "-" + SimpleDateFormat("MMM").format(c.time) + "-" + year.toString()
                                        .substring(2, 4)
                                )
                            }
                            "tagged_stock_summary" -> {
                                txtRepoTStockToDate.setText(
                                    "" + String.format(
                                        "%02d",
                                        dayOfMonth
                                    ) + "-" + SimpleDateFormat("MMM").format(c.time) + "-" + year.toString()
                                        .substring(2, 4)
                                )
                            }
                            "stock_item_details" -> {
                                txtRepoTStockToDate.setText(
                                    "" + String.format(
                                        "%02d",
                                        dayOfMonth
                                    ) + "-" + SimpleDateFormat("MMM").format(c.time) + "-" + year.toString()
                                        .substring(2, 4)
                                )
                            }
                        }
                    }
                }

            },
            year,
            month,
            day
        )
        when (isFromDate) {

            // To date(for all reporttypes)
            false -> {
                when (isFromDateSelected) {
                    true -> {
                        // first tap from date then tap to date
                        when (reportsType) {
                            "contact" -> {
                                dpd.datePicker.minDate = storeparse.time
                            }
                            "summary_contact" -> {
                                dpd.datePicker.minDate = storeparse.time
                            }
                            "total_stock_summary" -> {
                                dpd.datePicker.minDate = storeparse.time
                            }
                            "sales" -> {
                                dpd.datePicker.minDate = storeparse.time
                            }
                            "purchase" -> {
                                dpd.datePicker.minDate = storeparse.time
                            }
                            "day" -> {
                                dpd.datePicker.minDate = storeparse.time
                            }
                            "cashbank" -> {
                                dpd.datePicker.minDate = storeparse.time
                            }
                            "ledger" -> {
                                dpd.datePicker.minDate = storeparse.time
                            }
                            "tagged_stock_summary" -> {
                                dpd.datePicker.minDate = storeparse.time
                            }
                            "stock_item_details" -> {
                                dpd.datePicker.minDate = storeparse.time
                            }
                        }

                    }
                    false -> {
                        // direct tap to date
                        when (reportsType) {
                            "contact" -> {
                                parse = sdf.parse(txtRepoTContactFromDate.text.toString())
                                storeparse = parse
                                dpd.datePicker.minDate = storeparse.time
                            }
                            "summary_contact" -> {
                                parse = sdf.parse(txtRepoTContactFromDate.text.toString())
                                storeparse = parse
                                dpd.datePicker.minDate = storeparse.time
                            }
                            "total_stock_summary" -> {
                                parse = sdf.parse(txtRepoTStockFromDate.text.toString())
                                storeparse = parse
                                dpd.datePicker.minDate = storeparse.time
                            }
                            "sales" -> {
                                parse = sdf.parse(txtRepoTSalesFromDate.text.toString())
                                storeparse = parse
                                dpd.datePicker.minDate = storeparse.time
                            }
                            "purchase" -> {
                                parse = sdf.parse(txtRepoTPurchaseFromDate.text.toString())
                                storeparse = parse
                                dpd.datePicker.minDate = storeparse.time
                            }
                            "day" -> {
                                parse = sdf.parse(txtRepoTDayFromDate.text.toString())
                                storeparse = parse
                                dpd.datePicker.minDate = storeparse.time
                            }
                            "cashbank" -> {
                                parse = sdf.parse(txtRepoTCashBankFromDate.text.toString())
                                storeparse = parse
                                dpd.datePicker.minDate = storeparse.time
                            }
                            "ledger" -> {
                                parse = sdf.parse(txtRepoTLedgerFromDate.text.toString())
                                storeparse = parse
                                dpd.datePicker.minDate = storeparse.time
                            }
                            "tagged_stock_summary" -> {
                                parse = sdf.parse(txtRepoTStockFromDate.text.toString())
                                storeparse = parse
                                dpd.datePicker.minDate = storeparse.time
                            }
                            "stock_item_details" -> {
                                parse = sdf.parse(txtRepoTStockFromDate.text.toString())
                                storeparse = parse
                                dpd.datePicker.minDate = storeparse.time
                            }
                        }

                    }
                }
                /*when (reportsType) {
                    "contact" -> {
                        dpd.datePicker.minDate = storeparse.time
                    }
                    "stock" -> {
                        dpd.datePicker.minDate = storeparse.time
                    }
                    "sales" -> {
                        dpd.datePicker.minDate = storeparse.time
                    }
                    "purchase" -> {
                        dpd.datePicker.minDate = storeparse.time
                    }
                    "day" -> {
                        dpd.datePicker.minDate = storeparse.time
                    }
                    "cashbank" -> {
                        dpd.datePicker.minDate = storeparse.time
                    }
                    "ledger" -> {
                        dpd.datePicker.minDate = storeparse.time
                    }
                }*/
            }
            else -> {

            }
        }

        //dpd.datePicker.minDate = Date().time
        dpd.show()

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

    /*override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> if (grantResults.size > 0 && grantResults[0] === PackageManager.PERMISSION_GRANTED) {
                //downloadReport(reportsType)
                callToCommon(reportApiCallType)
            } else {
                Toast.makeText(applicationContext, "Permission required", Toast.LENGTH_SHORT)
                    .show()
                requestPermission()
            }
        }
    }*/
    private fun checkandRequestPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            Permissions.check(
                this /*context*/,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                null,
                object : PermissionHandler() {
                    override fun onGranted() {
                        callToCommon(reportApiCallType)
                    }

                    override fun onDenied(
                        context: Context?,
                        deniedPermissions: java.util.ArrayList<String>?
                    ) {
                        super.onDenied(context, deniedPermissions)
                        Toast.makeText(
                            applicationContext,
                            "Permission required to generate Report",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                })
        }else{
            callToCommon(reportApiCallType)
        }
    }
}








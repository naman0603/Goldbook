package com.goldbookapp.ui.activity.sales

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.net.http.SslError
import android.os.Bundle
import android.print.PrintAttributes
import android.print.PrintManager
import android.view.View
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.goldbookapp.R
import com.goldbookapp.databinding.ActivityPrintWebViewBinding
import com.goldbookapp.utils.CommonUtils
import com.goldbookapp.utils.CommonUtils.Companion.clickWithDebounce
import com.goldbookapp.utils.MyWebViewClient
import com.hamzasharuf.networkmonitor.ConnectivityStateHolder
import com.hamzasharuf.networkmonitor.Event
import com.hamzasharuf.networkmonitor.NetworkEvents
import kotlinx.android.synthetic.main.activity_print_web_view.*
import kotlinx.android.synthetic.main.activity_print_web_view.view.*
import kotlinx.android.synthetic.main.privacy_policy_activity.view.*
import kotlinx.android.synthetic.main.toolbar_layout.*


class PrintWebViewActivity : AppCompatActivity() {

    private  var weburl: String? = null
    private var title: String? = null
    lateinit var binding: ActivityPrintWebViewBinding
    val MAX_PROGRESS = 100


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_print_web_view)
        //binding.loginModel = LoginModel()
        val view = binding.root

        imgLeft.setImageResource(R.drawable.ic_back)

        imgLeft.clickWithDebounce {
            onBackPressed() }

        btnPrint.clickWithDebounce{
            printPDF(view)
        }
        if(intent.extras?.containsKey("weburi")!!){
            weburl = intent.getStringExtra("weburi")
            title = intent.getStringExtra("title")
            tvTitle.setText(title)

        }

        NetworkEvents.observe(this, Observer {
            if (it is Event.ConnectivityEvent) {
                if (ConnectivityStateHolder.isConnected) {
                    // Network is available
                    CommonUtils.hideInternetDialog()
                    setupUIandListner(view)
                }

                if (!ConnectivityStateHolder.isConnected) {
                    // Network is not available
                    CommonUtils.showDialog(this, getString(R.string.please_check_internet_msg))

                }
            }
        })
    }
    override fun onDestroy() {
        super.onDestroy()
        CommonUtils.hideProgress()
    }
    private fun setupUIandListner(view: View) {
        initWebview(view)
        setWebClient(view)
        binding.webviewPrint.webViewClient = MyWebViewClient(this@PrintWebViewActivity)
        loadUrl(weburl)
    }

    private fun loadUrl(pageUrl: String?) {
        binding.webviewPrint.loadUrl(pageUrl!!)

    }

    private fun createWebPrintJob(webView: WebView) {

        //create object of print manager in your device
        val printManager = this.getSystemService(PRINT_SERVICE) as PrintManager

        //create object of print adapter
        val printAdapter = webView.createPrintDocumentAdapter()
1
        //provide name to your newly generated pdf file
        val jobName = getString(R.string.app_name) + " Print Test"

        //open print dialog
        printManager.print(jobName, printAdapter, PrintAttributes.Builder().build())
    }

    //perform click pdf creation operation on click of print button click
    fun printPDF(view: View?) {
        createWebPrintJob(binding.webviewPrint)
    }

    private fun setWebClient(view: View) {
        view.webviewPrint.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(
                view: WebView,
                newProgress: Int
            ) {
                super.onProgressChanged(view, newProgress)
                if (newProgress < MAX_PROGRESS) {
                    CommonUtils.showProgress(this@PrintWebViewActivity)
                }
                if (newProgress == MAX_PROGRESS) {
                    CommonUtils.hideProgress()
                }
            }

        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebview(view: View) {

        view.webviewPrint.settings.javaScriptEnabled = true
        view.webviewPrint.settings.loadWithOverviewMode = true
        view.webviewPrint.settings.useWideViewPort = false
        view.webviewPrint.settings.domStorageEnabled = true
        view.webviewPrint.webViewClient = object : WebViewClient() {
            override
            fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
                /*handler?.proceed()*/
                val builder: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(this@PrintWebViewActivity)
                var message = "SSL Certificate error."
                when (error!!.primaryError) {
                    SslError.SSL_UNTRUSTED -> message =
                        "The certificate authority is not trusted."
                    SslError.SSL_EXPIRED -> message = "The certificate has expired."
                    SslError.SSL_IDMISMATCH -> message = "The certificate Hostname mismatch."
                    SslError.SSL_NOTYETVALID -> message = "The certificate is not yet valid."
                }
                message += " Do you want to continue anyway?"

                builder.setTitle("SSL Certificate Error")
                builder.setMessage(message)
                builder.setPositiveButton("continue",
                    DialogInterface.OnClickListener { dialog, which -> handler!!.proceed() })
                builder.setNegativeButton("cancel",
                    DialogInterface.OnClickListener { dialog, which -> handler!!.cancel() })
                val dialog: android.app.AlertDialog? = builder.create()
                dialog!!.show()
            }

        }


    }
}
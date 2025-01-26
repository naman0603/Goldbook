package com.goldbookapp.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.SystemClock
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.util.Log
import android.util.Patterns
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.postDelayed
import androidx.core.widget.doAfterTextChanged
import com.afollestad.materialdialogs.MaterialDialog
import com.blankj.utilcode.util.StringUtils.getString
import com.goldbookapp.MyApplication
import com.goldbookapp.R
import com.jakewharton.rxbinding2.view.RxView
import hk.ids.gws.android.sclick.SClick
import io.reactivex.android.schedulers.AndroidSchedulers
//import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
//import io.reactivex.rxjava3.disposables.Disposable
import java.io.File
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.regex.Matcher
import java.util.regex.Pattern


class CommonUtils {

    companion object {
        var isCommonDialogShown :Boolean = false;
        var pDialog: Dialog;
        lateinit var internetDialog : MaterialDialog
        var dTime: Long = 600L
        var editdTime: Long = 750L
        var lastClickTime: Long = 0
        var lastProgressTime: Long = 0
        init {
            pDialog = Dialog(MyApplication.appContext)
        }

        fun isValidEmail(email: String): Boolean {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
        }
        fun isValidMobile(phone: String): Boolean {
            return Patterns.PHONE.matcher(phone).matches()
        }

        fun isValidPassword(password: String) : Boolean {
            val pattern: Pattern
            val matcher: Matcher

            // 1 character, 1 special, 1 numeric, minimum 6, Max 32
            val PASSWORD_PATTERN =
                "^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[.!#$%&'*@+/=?^_`{|}~-]).{6,32}$"

            pattern = Pattern.compile(PASSWORD_PATTERN)
            matcher = pattern.matcher(password)

            return matcher.matches();
        }

        fun showProgress(context: Context) {

            if (preventProgressOverlay()) {
                try {
                    if (pDialog.isShowing()) {
                        if (!(context as Activity).isFinishing) {
                            pDialog.dismiss()
                        }
                    }
                    pDialog = Dialog(context)
                    if (!(context as Activity).isFinishing) {
                        pDialog.show()
                        pDialog.setContentView(R.layout.progressbar_layout)
                        pDialog.window?.setBackgroundDrawable(
                            ColorDrawable(Color.TRANSPARENT)
                        )
                        pDialog.window!!.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                        pDialog.setCancelable(false)
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        fun hideProgress() {
            try {
                if (pDialog != null && pDialog.isShowing()) {
                    pDialog.dismiss()
                }
            } catch (e: Exception) {
                Log.e("hide progress error", e.toString())
            }
        }

        fun showPaginationListLoader(context: Context) {
            try {

                if (pDialog != null && pDialog.isShowing()) {
                    if (!(context as Activity).isFinishing) {
                        pDialog.dismiss()
                    }
                }
                pDialog = Dialog(context)
                if (!(context as Activity).isFinishing) {
                    pDialog.show()
                    /*pDialog.window?.setBackgroundDrawable(
                        ColorDrawable(Color.TRANSPARENT)
                    )*/
                    pDialog.setContentView(R.layout.item_loading)
                    pDialog.setCancelable(false)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun hidePaginationListLoader() {
            if (pDialog != null && pDialog.isShowing()) {
                pDialog.dismiss()
            }
        }



        fun showDialog(context: Context, message: String) {
          //  Log.v("isCommonDialogShown", isCommonDialogShown.toString())
            if(!this::internetDialog.isInitialized){
              //  Log.v("internetdialog", "not intialialized")
                internetDialog = MaterialDialog(context).show {
                    title(R.string.app_name)
                    message(text = message)
                    cancelable(false)
                    cancelOnTouchOutside(false)
                    if(message.equals(getString(R.string.please_check_internet_msg))){
                        // nothing to do.
                    }
                    else{
                        positiveButton(R.string.ok)
                    }
                }
            }else{
                if(message.equals(getString(R.string.please_check_internet_msg))){
                  //  Log.v("internetdialog", "internet msg")
                    if(isCommonDialogShown){
                      //  Log.v("isCommonDialogShown", isCommonDialogShown.toString())
                        internetDialog = MaterialDialog(context).show {
                            title(R.string.app_name)
                            message(text = message)
                            cancelable(false)
                            cancelOnTouchOutside(false)
                            if(message.equals(getString(R.string.please_check_internet_msg))){
                                // nothing to do.
                                isCommonDialogShown = false;
                            }
                            else{
                                positiveButton(R.string.ok)
                                isCommonDialogShown = true;
                            }

                        }
                    }
                    else{
                       // Log.v("isCommonDialogShown", isCommonDialogShown.toString())
                        if(context == internetDialog.context){
                            internetDialog.show {
                                title(R.string.app_name)
                                message(text = message)
                                cancelable(false)
                                cancelOnTouchOutside(false)
                                if(message.equals(getString(R.string.please_check_internet_msg))){
                                    // nothing to do.
                                    isCommonDialogShown = false;
                                }
                                else{
                                    positiveButton(R.string.ok)
                                    isCommonDialogShown = true;
                                }

                            }
                        }
                        else{
                            hideInternetDialog()
                            internetDialog = MaterialDialog(context).show {
                                title(R.string.app_name)
                                message(text = message)
                                cancelable(false)
                                cancelOnTouchOutside(false)
                                if(message.equals(getString(R.string.please_check_internet_msg))){
                                    // nothing to do.
                                    isCommonDialogShown = false;
                                }
                                else{
                                    positiveButton(R.string.ok)
                                    isCommonDialogShown = true;
                                }

                            }
                        }

                    }
                    // nothing to do.

                }
                else{
                  //  Log.v("internetdialog", " normal dialog")
                    internetDialog = MaterialDialog(context).show {
                        title(R.string.app_name)
                        message(text = message)
                        cancelable(false)
                        cancelOnTouchOutside(false)
                        if(message.equals(getString(R.string.please_check_internet_msg))){
                            // nothing to do.
                            isCommonDialogShown = false;
                        }
                        else{
                            positiveButton(R.string.ok)
                            isCommonDialogShown = true;
                        }

                    }
                }
                /*internetDialog.show {
                    title(R.string.app_name)
                    message(text = message)
                    cancelable(false)
                    cancelOnTouchOutside(false)
                    if(message.equals(getString(R.string.please_check_internet_msg))){
                        // nothing to do.
                    }
                    else{
                        positiveButton(R.string.ok)
                    }

                }*/

            }

        }
        fun hideInternetDialog(){
            //if(this::internetDialog.isInitialized){
            try {
                internetDialog.cancel()
            } catch (e: Exception) {
            }
            // }
        }


        fun isValidPANDetail(panNumber: String) : Boolean {
            val pattern: Pattern
            val matcher: Matcher

            // 1 character, 1 special, 1 numeric, minimum 6, Max 32
            val PANCARD_PATTERN =
                "[A-Z]{5}[0-9]{4}[A-Z]{1}"

            pattern = Pattern.compile(PANCARD_PATTERN)
            matcher = pattern.matcher(panNumber)

            return matcher.matches();
        }
        fun isValidTANDetail(panNumber: String) : Boolean {
            val pattern: Pattern
            val matcher: Matcher

            // 1 character, 1 special, 1 numeric, minimum 6, Max 32
            val PANCARD_PATTERN =
                "[A-Z]{4}[0-9]{5}[A-Z]{1}"

            pattern = Pattern.compile(PANCARD_PATTERN)
            matcher = pattern.matcher(panNumber)

            return matcher.matches();
        }

        // Function to validate
        // GST (Goods and Services Tax) number.
        fun isValidGSTNo(str: String?): Boolean {
            // Regex to check valid
            // GST (Goods and Services Tax) number
            val regex = ("^[0-9]{2}[A-Z]{5}[0-9]{4}"
                    + "[A-Z]{1}[0-9A-Z]{1}"
                    + "[A-Z]{1}[0-9A-Z]{1}")

            // Compile the ReGex
            val p = Pattern.compile(regex)

            // If the string is empty
            // return false
            if (str == null) {
                return false
            }

            // Pattern class contains matcher()
            // method to find the matching
            // between the given string
            // and the regular expression.
            val m = p.matcher(str)

            // Return if the string
            // matched the ReGex
            return m.matches()
        }


        //        fun hideKeyboard(context: Context,  view: View) {
//            val inputMethodManager = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
//            inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
//        }
 /*fun showKeyboard(activity: Activity): Unit {
    val inputMethodManager =
        activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.toggleSoftInputFromWindow(
        activity.currentFocus!!.windowToken,
        InputMethodManager.SHOW_FORCED,
        0
    )
}*/
        fun hideKeyboardnew(activity: Activity) {
            val imm: InputMethodManager =
                activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            //Find the currently focused view, so we can grab the correct window token from it.
            var view = activity.currentFocus
            //If no view currently has focus, create a new one, just so we can grab a window token from it
            if (view == null) {
                view = View(activity)
            }
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }

        fun hideKeyboardFromCustomdialog(dialog: Dialog, window: Window) {
            val imm: InputMethodManager =
                dialog.context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            //Find the currently focused view, so we can grab the correct window token from it.
            var view = dialog.currentFocus
            //If no view currently has focus, create a new one, just so we can grab a window token from it
            if (view == null) {
                view = window.decorView.rootView
            }
            imm.hideSoftInputFromWindow(view?.windowToken, 0)
        }

        fun removeunwantedMinusSign(inputString: String) : String {

            var result: String = inputString
            result = result.replace("^-?[0-9]\\d*(\\.\\d+)?\$".toRegex(), "")

            return result
        }
        fun removeunwantedDots(inputString: String) : String {

            var result: String = inputString
            result = result.replace("^.?[0-9]\\d*(\\.\\d+)?\$".toRegex(), "")

            return result
        }

        fun removeUnwantedComma(inputString: String) : String {

            var result: String = inputString
            result = result.replace("^(,|\\s)*|(,|\\s)*$".toRegex(), "").replace(
                "(\\,\\s*)+".toRegex(),
                ", "
            )

            return result
        }
        fun perfectDecimal(
            str: String,
            MAX_BEFORE_POINT: Int,
            MAX_DECIMAL: Int
        ): String? {
            var str = str
            if (str[0] == '.') str = "0$str"
            val max = str.length
            var rFinal = ""
            var after = false
            var i = 0
            var up = 0
            var decimal = 0
            var t: Char
            while (i < max) {
                t = str[i]
                if (t != '.' && after == false) {
                    up++
                    if (up > MAX_BEFORE_POINT) return rFinal
                } else if (t == '.') {
                    after = true
                } else {
                    decimal++
                    if (decimal > MAX_DECIMAL) return rFinal
                }
                rFinal = rFinal + t
                i++
            }
            return rFinal
        }

         fun checkForValidPositiveNegativeDecimal(
             context: Context,
             inputString: String,
             fieldName: String
         ):Boolean {
            if(!inputString.isBlank() && inputString.length > 1) {
                try {
                    val a: Double = inputString.toDouble()
                    var wastage: BigDecimal = BigDecimal(0)
                    if (inputString.toDouble() > 0.00) {
                        wastage = inputString.toBigDecimal()
                       // System.out.println(inputString + " is positive number");
                    } else if (inputString.toDouble() < 0.00) {
                        wastage = inputString.toBigDecimal().negate()
                       // System.out.println(inputString + " is negative number");
                    } else {

                       // System.out.println(inputString + " is neither positive nor negative");
                    }

                } catch (ex: NumberFormatException) {
                    Toast.makeText(context, "Please enter valid $fieldName", Toast.LENGTH_SHORT).show()
                   // System.err.println("Please enter valid $fieldName")
                    return false
                    //request for well-formatted string
                }



            }
            return true
        }
        // clear all prefs
        fun clearAllAppPrefs(prefs: SharedPreferences) {
            prefs.edit().remove(Constants.PREF_LOGIN_DETAIL_KEY).apply()
            prefs.edit().remove(Constants.PREF_COMPANY_REGISTER_KEY).apply()
            prefs.edit().remove(Constants.PREF_PROFILE_DETAIL_KEY).apply()
            prefs.edit().remove(Constants.PREF_BILLING_ADDRESS_KEY).apply()
            prefs.edit().remove(Constants.PREF_SHIPPING_ADDRESS_KEY).apply()
            prefs.edit().remove(Constants.PREF_COMPANY_ADDRESS_KEY).apply()
            prefs.edit().remove(Constants.PREF_BRANCH_ADDRESS_KEY).apply()
            prefs.edit().remove(Constants.PREF_MULTIPLE_OPENINGSTOCK).apply()
            prefs.edit().remove(Constants.PREF_ADD_ITEM_KEY).apply()
            prefs.edit().remove(Constants.PREF_SALES_LINE_INFO_KEY).apply()
            prefs.edit().remove(Constants.PREF_OPENINGSTOCK_CALC_INFO_KEY).apply()
            prefs.edit().remove(Constants.PREF_Payment_Ref_Selected_Trans_Ids).apply()
            prefs.edit().remove(Constants.PREF_Payment_Ref_Selected_Invoice_Nos).apply()
            prefs.edit().remove(Constants.PREF_SELECTED_STOCK_ID_DETAILS).apply()
            prefs.edit().remove(Constants.PREF_ADD_ITEM_PAYMENT_KEY).apply()
            prefs.edit().remove(Constants.PREF_INVENTORY_INFO_KEY).apply()
            prefs.edit().remove(Constants.PREF_ACCOUNTING_INFO_KEY).apply()
            prefs.edit().remove(Constants.FiscalYear).apply()

        }
        fun somethingWentWrong(context: Context){
            Toast.makeText(
                context,
                getString(R.string.something_went_wrong),
                Toast.LENGTH_LONG
            )
                .show()
        }
        fun applyFontToMenu(m: Menu, mContext: Context) {
            for (i in 0 until m.size()) {
                applyFontToMenuItem(m.getItem(i), mContext)
            }
        }

        fun applyFontToMenuItem(mi: MenuItem, mContext: Context) {
            if (mi.hasSubMenu()) for (i in 0 until mi.getSubMenu()!!.size()) {
                applyFontToMenuItem(mi.getSubMenu()!!.getItem(i), mContext)
            }
            val font = ResourcesCompat.getFont(mContext, R.font.proxima_nova_regular)
                /*Typeface.createFromAsset("proxima_nova_bold")*/
            val mNewTitle = SpannableString(mi.getTitle())
            mNewTitle.setSpan(
                CustomTypefaceSpan("", font),
                0,
                mNewTitle.length,
                Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )
            mi.setTitle(mNewTitle)
        }
        fun isPDFSupported(context: Context, filepath: String): Boolean {
            val i = Intent(Intent.ACTION_VIEW)
            val tempFile =
                File(filepath)
            i.setDataAndType(Uri.fromFile(tempFile), "application/pdf")
            return context.packageManager
                .queryIntentActivities(i, PackageManager.MATCH_DEFAULT_ONLY).size > 0
        }

        /**
         * Makes a substring of a string bold.
         * @param text          Full text
         * @param textToBold    Text you want to make bold
         * @return              String with bold substring
         */
        fun makeSectionOfTextBold(text: String, textToBold: String): SpannableStringBuilder? {
            val builder = SpannableStringBuilder()
            if (textToBold.length > 0 && textToBold.trim { it <= ' ' } != "") {

                //for counting start/end indexes
                val testText = text.toLowerCase(Locale.US)
//                val testTextToBold = textToBold.toLowerCase(Locale.US)
                val startingIndex = testText.indexOf(textToBold)
                val endingIndex = startingIndex + textToBold.length
                //for counting start/end indexes
                if (startingIndex < 0 || endingIndex < 0) {
                    return builder.append(text)
                } else if (startingIndex >= 0 && endingIndex >= 0) {
                    builder.append(text)
                    builder.setSpan(StyleSpan(Typeface.BOLD), startingIndex, endingIndex, 0)
                }
            } else {
                return builder.append(text)
            }
            return builder
        }
        fun getMonthByNumber(monthnum:Int):String {
            val c = Calendar.getInstance()
            val month_date = SimpleDateFormat("MMM")
            c[Calendar.MONTH] = monthnum-1
            return month_date.format(c.time)
        }



        /*fun addMarquee(textView: TextView) {
            textView.viewTreeObserver.addOnGlobalLayoutListener(object :
                ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    val pixels = textView.measuredWidth - 1
                    val params = textView.layoutParams
                    params.width = pixels
                    textView.layoutParams = params
                    textView.isSelected = true
                    textView.ellipsize = TextUtils.TruncateAt.MARQUEE
                    textView.isSingleLine = true
                    textView.marqueeRepeatLimit = -1
                    textView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            })
        }*/
        /*fun onSNACK(view: View,context: Context,snackbarMsg: String){
            //Snackbar(view)
            val snackbar = Snackbar.make(view, snackbarMsg,
                Snackbar.LENGTH_LONG).setAction("Settings") {
                context.startActivity(Intent(Settings.ACTION_DATA_ROAMING_SETTINGS))
            }
            snackbar.setActionTextColor(Color.BLUE)
            val snackbarView = snackbar.view
            snackbarView.setBackgroundColor(Color.LTGRAY)
            val textView =
                snackbarView.findViewById(com.google.android.material.R.id.snackbar_text) as TextView
            textView.setTextColor(Color.BLUE)
            textView.textSize = 28f
            snackbar.show()
        }
        fun networkChangeReceiverGlobally(view: View,context: Context){
             val networkChangeReceiver: BroadcastReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    //Log.d("app", "Network connectivity change")
                    if(NetworkUtils.isConnected()){
                        Toast.makeText(context,"connected",Toast.LENGTH_SHORT).show()
                    }
                    else{
                        Toast.makeText(context,"disconnected",Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }*/

        /*private fun showNoNetSnackbar() {
            val snack = Snackbar.make(rootView, "No Internet!", Snackbar.LENGTH_LONG) // replace root view with your view Id
            snack.setAction("Settings") {
                startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
            }
            snack.show()
        }*/
        // to restrict user for rapid clicks

       /* fun View.clickWithDebounce(debounceTime: Long = 750L, action: () -> Unit) {
            this.setOnClickListener(object : View.OnClickListener {
                private var lastClickTime: Long = 0

                override fun onClick(v: View) {
                    if (SystemClock.elapsedRealtime() - lastClickTime < debounceTime) return
                    else action()

                    lastClickTime = SystemClock.elapsedRealtime()
                }
            })
        }*/
        // for prevention of rapid clicks using rxbinding2
       fun View.clickWithDebounce( action: () -> Unit) =
           RxView.clicks(this)
               .debounce(dTime, TimeUnit.MILLISECONDS)
               .observeOn(AndroidSchedulers.mainThread())
               .subscribe { action() }


        // debounce for edittext (in place of doaftertextchange)

        /*@SuppressLint("NewApi")
        fun EditText.searchDebounce( action: (Editable?) -> Unit) {
            doAfterTextChanged { text ->
                var counter = getTag(id) as? Int ?: 0
                val handler = Handler()
                handler.removeCallbacksAndMessages(counter)
                handler.postDelayed(editdTime, ++counter) { action(text) }
                setTag(id, counter)
            }
        }*/


        fun isValidClickPressed(): Boolean {
            if (SystemClock.elapsedRealtime() - lastClickTime < dTime) {
                lastClickTime = SystemClock.elapsedRealtime()
               // Log.v("lastClickTime",lastClickTime.toString()+"false")
                return false
            }
            else {
                lastClickTime = SystemClock.elapsedRealtime()
              //  Log.v("lastClickTime",lastClickTime.toString()+"true")
                return true
            }

        }

        fun preventProgressOverlay(): Boolean {
            if (SystemClock.elapsedRealtime() - lastProgressTime < dTime) {
                lastProgressTime = SystemClock.elapsedRealtime()
                // Log.v("lastClickTime",lastClickTime.toString()+"false")
                return false
            }
            else {
                lastProgressTime = SystemClock.elapsedRealtime()
                //  Log.v("lastClickTime",lastClickTime.toString()+"true")
                return true
            }

        }

        fun gmsTokg(gms:String): String{
            return (gms.toBigDecimal().setScale(3)
                .divide("1000".toBigDecimal().setScale(3)
                )).setScale(3, RoundingMode.CEILING).toString()
        }

        fun gmsTocarrot(gms:String): String{
            return (gms.toBigDecimal().setScale(3)
                .multiply("5".toBigDecimal().setScale(3)
                )).setScale(3, RoundingMode.CEILING).toString()
        }

        fun kgTogms(kg:String): String{
            return (kg.toBigDecimal().setScale(3)
                .multiply("1000".toBigDecimal().setScale(3)
                )).setScale(3, RoundingMode.CEILING).toString()
        }

        fun carrotTogm(gms:String): String{
            return (gms.toBigDecimal().setScale(3)
                .divide("5".toBigDecimal().setScale(3)
                )).setScale(3, RoundingMode.CEILING).toString()
        }
    }



}


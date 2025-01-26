package com.goldbookapp.ui.fragment

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.NetworkUtils
import com.goldbookapp.R
import com.goldbookapp.api.ApiHelper
import com.goldbookapp.api.RetrofitBuilder
import com.goldbookapp.databinding.FragmentSupplierTransactionBinding
import com.goldbookapp.model.FiscalYearModel
import com.goldbookapp.model.LoginModel
import com.goldbookapp.model.TransactionHistoryModel
import com.goldbookapp.ui.activity.viewmodel.ViewModelFactory
import com.goldbookapp.ui.adapter.SupplierDetailModel
import com.goldbookapp.ui.adapter.TransactionsAdapter
import com.goldbookapp.ui.fragment.viewmodel.TransactionViewModel
import com.goldbookapp.utils.*
import com.goldbookapp.utils.CommonUtils.Companion.clickWithDebounce
import com.goldbookapp.utils.PreferenceHelper.get
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import com.whiteelephant.monthpicker.MonthPickerDialog
import hk.ids.gws.android.sclick.SClick
import kotlinx.android.synthetic.main.dash_fiscal_year_dialog.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class SupplierTransactionsFragment(
    var supplierDetailModel: SupplierDetailModel,
    var contxt: Context
) : Fragment() {
    private lateinit var transactionViewModel: TransactionViewModel
    private lateinit var transactionHistoryModel: TransactionHistoryModel

    lateinit var binding: FragmentSupplierTransactionBinding
    private lateinit var adapter: TransactionsAdapter
    lateinit var monthAdapter: ArrayAdapter<String>
    lateinit var monthArrayList: ArrayList<String>
    lateinit var selectedMonthGlobal: String
    lateinit var selectedYearGlobal: String
    var selectedMon: Int = -1;
    var selectedYr: Int = -1;
    lateinit var sortOrder: String
    lateinit var sortAdapter: ArrayAdapter<String>
    lateinit var sortArrayList: ArrayList<String>
    lateinit var supplier_id: String
    lateinit var loginModel: LoginModel
    var isFirstime: Boolean = false
    private var currentPage: Int = Constants.PAGE_START
    private var page_size: Int = Constants.PAGE_SIZE
    private var totalPage = 1
    private var isLastPage = false
    private var isLoading = false
    var thisFiscalSD: String? = null
    var thisFiscalED: String? = null
    var thisPreviousFiscalSD: String? = null
    var thisPreviousFiscalED: String? = null
    var currentYear = 0
    var prevYear = 0
    var nextYear = 0
    var start_date: String? = null
    var end_date: String? = null
    lateinit var dialog1: Dialog
    lateinit var prefs: SharedPreferences
    lateinit var fiscalYearModel: FiscalYearModel
    var selectFiscalYearFromMenu: String? = "0"
    lateinit var popupMenu: PopupMenu
    val c = Calendar.getInstance()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_supplier_transaction,
            container,
            false
        )
        val view = binding.root

        setupViewModel()
        setupUIandListner(view)

        return view
    }

    override fun onResume() {
        super.onResume()
        //if (!isFirstime) {
        binding.rvSuppTransactions.visibility = View.GONE
        getTransactionHistoryDetails(
            loginModel.data?.bearer_access_token, supplier_id,
            currentPage, start_date, end_date
        )
        // }

    }

    private fun setupViewModel() {
        transactionViewModel =
            ViewModelProvider(this, ViewModelFactory(ApiHelper(RetrofitBuilder.apiService))).get(
                TransactionViewModel::class.java
            )
        binding.setLifecycleOwner(this)

    }

    fun setupUIandListner(root: View) {
        prefs = PreferenceHelper.defaultPrefs(activity?.applicationContext!!)
        loginModel = Gson().fromJson(
            prefs[Constants.PREF_LOGIN_DETAIL_KEY, ""],
            LoginModel::class.java
        ) //getter

        fiscalYearModel = Gson().fromJson(
            prefs[Constants.FiscalYear, ""],
            FiscalYearModel::class.java
        )

        start_date = fiscalYearModel.start_date
        end_date = fiscalYearModel.end_date

        if (supplierDetailModel.vendors != null) {
            supplier_id = supplierDetailModel.vendors!!.id.toString()
        }
        val date = Date()
        val cal = Calendar.getInstance()
        cal.time = date
        selectedMonthGlobal = (cal[Calendar.MONTH] + 1).toString()
        selectedYearGlobal = (cal[Calendar.YEAR]).toString()

        binding.monthSpinnerSupp.text =
            "${CommonUtils.getMonthByNumber(cal[Calendar.MONTH] + 1)} - $selectedYearGlobal"
        sortOrder = "desc"

        setUpSortSpinner()
        setupFiscalYearDialog()
        getFiscalYear()

        binding.monthSpinnerSupp.clickWithDebounce {
            setupMonthYearDialog(cal)
        }

        binding.linearFiscalYearSupptransaction.clickWithDebounce {
            openFiscalYearDialog()
        }

        adapter = TransactionsAdapter(arrayListOf(), totalPage)
        binding.rvSuppTransactions.adapter = adapter

        binding.rvSuppTransactions.layoutManager = LinearLayoutManager(activity)
        binding.rvSuppTransactions.addOnScrollListener(object :
            PaginationListener(binding.rvSuppTransactions.layoutManager as LinearLayoutManager) {
            override fun loadMoreItems() {
                isLoading = true
                currentPage++
                //Toast.makeText(context,"reyscrollcurrentPage" + "$currentPage",Toast.LENGTH_SHORT).show()
                getTransactionHistoryDetails(
                    loginModel.data?.bearer_access_token,
                    supplier_id, currentPage, start_date, end_date
                )
            }

            override fun isLastPage(): Boolean {
                return isLastPage
            }

            override fun isLoading(): Boolean {
                return isLoading
            }
        })

    }

    private fun getFiscalYear() {
        currentYear = Calendar.getInstance()[Calendar.YEAR]
        prevYear = currentYear - 1
        nextYear = currentYear + 1
        // val month = Calendar.getInstance()[Calendar.MONTH] + 1

        val month_date = SimpleDateFormat("MMM")
        val month = month_date.format(Calendar.getInstance().time)
        Log.v("month", "" + month)
        val date = Calendar.getInstance()[Calendar.DATE]

        val cuerrent_date: String = currentYear.toString() + "-" + month + "-" + date
        val match_date: String = currentYear.toString() + "-" + "Mar" + "-" + "31"
        compareDates(cuerrent_date, match_date)

    }

    fun compareDates(d1: String?, d2: String?) {
        try {
            val sdf = SimpleDateFormat("yyyy-MMM-dd")
            val date1 = sdf.parse(d1)
            val date2 = sdf.parse(d2)
            if (date1.after(date2)) {
                thisFiscalSD = "01-Apr-$currentYear"
                thisFiscalED = "31-Mar-$nextYear"
                Log.v("thisFiscal", "" + thisFiscalSD + "" + thisFiscalED)
                thisPreviousFiscalSD = "01-Apr-$prevYear"
                thisPreviousFiscalED = "31-Mar-$currentYear"
                Log.v("thisPreviousFiscal", "" + thisPreviousFiscalSD + "" + thisPreviousFiscalED)

            } else {
                currentYear = currentYear - 1
                nextYear = currentYear + 1
                prevYear = currentYear - 1
                thisFiscalSD = "01-Apr-$currentYear"
                thisFiscalED = "31-Mar-$nextYear"
                Log.v("thisFiscal", "" + thisFiscalSD + "" + thisFiscalED)
                thisPreviousFiscalSD = "01-Apr-$prevYear"
                thisPreviousFiscalED = "31-Mar-$currentYear"
                Log.v("thisPreviousFiscal", "" + thisPreviousFiscalSD + "" + thisPreviousFiscalED)
            }

            binding.tvtransactionSuppFiscalYear.setText(
                "F.Y.: " + fiscalYearModel.start_date +
                        " to " + fiscalYearModel.end_date
            )
            selectFiscalYearFromMenu = fiscalYearModel.select_from


        } catch (ex: ParseException) {
            ex.printStackTrace()
        }
    }


    private fun setupFiscalYearDialog() {
        dialog1 = Dialog(requireContext(), R.style.Full_Dialog)
        dialog1.requestWindowFeature(Window.FEATURE_NO_TITLE)
        requireActivity().window.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        );

        dialog1.setCancelable(false)
        dialog1.setCanceledOnTouchOutside(false)
        dialog1.setContentView(R.layout.dash_fiscal_year_dialog)
    }

    private fun openFiscalYearDialog() {


        when (selectFiscalYearFromMenu) {
            "1" -> {
                dialog1.txtFiscalYear.setText("This Fiscal Year")
                dialog1.ly_custom_range.visibility = View.GONE
                /* start_date = thisFiscalSD
                 end_date = thisFiscalED*/
            }
            "2" -> {
                dialog1.txtFiscalYear.setText("Previous Fiscal Year")
                dialog1.ly_custom_range.visibility = View.GONE
            }
            "3" -> {
                dialog1.txtFiscalYear.setText("Custom Range")
                dialog1.ly_custom_range.visibility = View.VISIBLE
                dialog1.txtFromDate.setText(start_date)
                dialog1.txtToDate.setText(end_date)
            }
            else -> {


            }
        }


        dialog1.tvFiscalCancel.clickWithDebounce {
            dialog1.dismiss()
        }
        dialog1.txtFiscalYear.clickWithDebounce {
            openFicalYearPopup()
        }

        // Log.v("default",""+getFirstDateOfMonth(Date())+thisFiscalSD)


        dialog1.txtFromDate.clickWithDebounce {
            openDatePicker(true)
        }

        dialog1.txtToDate.clickWithDebounce {
            openDatePicker(false)
        }


        dialog1.tvFiscalSave.clickWithDebounce {
            dialog1.dismiss()
            binding.tvtransactionSuppFiscalYear.setText("F.Y.: " + start_date + " to " + end_date)
            currentPage = 1
            getTransactionHistoryDetails(
                loginModel.data?.bearer_access_token,
                supplier_id, currentPage, start_date, end_date
            )
        }



        dialog1.show()
    }

    fun openFicalYearPopup() {
        popupMenu = PopupMenu(requireContext(), dialog1.txtFiscalYear)
        popupMenu.menu.add("This Fiscal Year")
        popupMenu.menu.add("Previous Fiscal Year")
        popupMenu.menu.add("Custom Range")



        popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
            (dialog1.txtFiscalYear as TextInputEditText).setText(item.title)
            when (item.title) {
                "This Fiscal Year" -> {
                    dialog1.ly_custom_range.visibility = View.GONE
                    start_date = thisFiscalSD
                    end_date = thisFiscalED
                    selectFiscalYearFromMenu = "1"
                }
                "Previous Fiscal Year" -> {
                    dialog1.ly_custom_range.visibility = View.GONE
                    start_date = thisPreviousFiscalSD
                    end_date = thisPreviousFiscalED
                    selectFiscalYearFromMenu = "2"
                }
                "Custom Range" -> {
                    dialog1.ly_custom_range.visibility = View.VISIBLE
                    dialog1.txtFromDate.setText(thisFiscalSD)
                    dialog1.txtToDate.setText(thisFiscalED)
                    start_date = dialog1.txtFromDate.text.toString()
                    end_date = dialog1.txtToDate.text.toString()
                    selectFiscalYearFromMenu = "3"
                }

            }
            true
        })

        popupMenu.show()
    }


    fun openDatePicker(isFromDate: Boolean) {
//        val c = Calendar.getInstance()
        val sdf = SimpleDateFormat("dd-MMM-yyyy")
        var parse = Date()
        when (isFromDate) {
            // from date(for all reporttypes)
            true -> {

                parse = sdf.parse(dialog1.txtFromDate.text.toString())


                //start_date = SimpleDateFormat("dd-MMM-yyyy").format(dialog1.txtFromDate.text.toString())
            }
            // To date(for all reporttypes)
            false -> {

                parse = sdf.parse(dialog1.txtToDate.text.toString())

            }
        }
        c.setTime(parse)


        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)


        val dpd = DatePickerDialog(
            requireContext(),
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->

                c.set(Calendar.YEAR, year)
                c.set(Calendar.MONTH, monthOfYear)
                c.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                // Display Selected date in textbox
                when (isFromDate) {
                    // from date(for all reporttypes)
                    true -> {


                        dialog1.txtFromDate.setText(
                            "" + String.format(
                                "%02d",
                                dayOfMonth
                            ) + "-" + SimpleDateFormat("MMM").format(c.time) + "-" + year.toString()
                                .substring(0, 4)
                        )
                        parse = sdf.parse(dialog1.txtFromDate.text.toString())
                        start_date = SimpleDateFormat("dd-MMM-yyyy").format(parse)
                        Log.v("date", "" + start_date + parse)
                        if (parse.after(sdf.parse(dialog1.txtFromDate.text.toString()))) {
                            dialog1.txtToDate.setText(dialog1.txtFromDate.text)
                            end_date =
                                SimpleDateFormat("dd-MMM-yyyy").format(dialog1.txtToDate.text.toString())
                        }


                    }
                    // To date(for all reporttypes)
                    false -> {

                        dialog1.txtToDate.setText(
                            "" + String.format(
                                "%02d",
                                dayOfMonth
                            ) + "-" + SimpleDateFormat("MMM").format(c.time) + "-" + year.toString()
                                .substring(0, 4)
                        )
                        parse = sdf.parse(dialog1.txtToDate.text.toString())
                        end_date = SimpleDateFormat("dd-MMM-yyyy").format(parse)

                    }
                }
            },

            year,
            month,
            day
        )


        //dpd.datePicker.minDate = Date().time
        dpd.show()

    }

    private fun setupMonthYearDialog(today: Calendar) {
        val builder = MonthPickerDialog.Builder(
            contxt,
            { selectedMonth, selectedYear ->
                selectedMon = selectedMonth
                selectedYr = selectedYear

                selectedMonthGlobal = (selectedMonth + 1).toString()
                selectedYearGlobal = selectedYear.toString()

                binding.monthSpinnerSupp.text =
                    "${CommonUtils.getMonthByNumber(selectedMonth + 1)} - $selectedYearGlobal"


                binding.rvSuppTransactions.visibility = View.GONE
                getTransactionHistoryDetails(
                    loginModel.data?.bearer_access_token,
                    supplier_id, currentPage, start_date, end_date
                )

            }, today.get(Calendar.YEAR), today.get(Calendar.MONTH)
        )

        if (selectedYr == -1) {
            builder.setActivatedMonth(today.get(Calendar.MONTH))
                .setActivatedYear(today.get(Calendar.YEAR))

        } else {
            builder.setActivatedMonth(/*today.get(Calendar.MONTH)*/selectedMon)
                .setActivatedYear(/*today.get(Calendar.YEAR)*/selectedYr)

        }
        builder.setMinYear(today.get(Calendar.YEAR) - 1)
            .setMaxYear(today.get(Calendar.YEAR))
            .setTitle("Select Month & Year")
            .setOnMonthChangedListener { selectedMonth ->
            }
            .setOnYearChangedListener { selectedYear ->

            }
            .build()
            .show()
    }

    private fun setUpSortSpinner() {
        sortArrayList = arrayListOf()
        sortArrayList.clear()
        sortArrayList.add("Asc")
        sortArrayList.add("Desc")

        sortAdapter = ArrayAdapter(
            contxt,
            android.R.layout.simple_spinner_item,
            sortArrayList!!
        )
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.sortSpinnerSupp?.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {

                }

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {

                    sortOrder = sortArrayList.get(position).toLowerCase()
                    getTransactionHistoryDetails(
                        loginModel?.data?.bearer_access_token,
                        supplier_id,
                        currentPage, start_date, end_date
                    )
                }

            }


        with(binding.sortSpinnerSupp)
        {
            adapter = sortAdapter
            setSelection(1, false)
            prompt = "Sort By"
            gravity = android.view.Gravity.CENTER

        }

    }

    fun getTransactionHistoryDetails(
        token: String?, contact_id: String?, current_page: Int,
        date_range_from: String?,
        date_range_to: String?
    ) {

        if (NetworkUtils.isConnected()) {

            transactionViewModel.transactionHistory(
                token,
                contact_id,
                current_page,
                date_range_from,
                date_range_to
            )
                .observe(requireActivity(), Observer {
                    it?.let { resource ->
                        when (resource.status) {
                            Status.SUCCESS -> {
                                if (it.data?.status == true) {

                                    totalPage = it.data.total_page!!
                                    page_size = it.data.page_limit!!
                                    transactionHistoryModel = it.data
                                    setTransactionHistoryDetails(transactionHistoryModel, page_size)
                                    if (transactionHistoryModel.data!!.isEmpty()) {
                                        /*binding.tvNoRecordSuppTransaction.text =
                                            "No Transaction for the selected month"*/
                                        binding.rvSuppTransactions.visibility = View.GONE
                                        binding.suppTransationTitles.visibility = View.GONE
                                        binding.tvNoRecordSuppTransaction.visibility = View.VISIBLE
                                    } else {
                                        binding.rvSuppTransactions.visibility = View.VISIBLE
                                        binding.suppTransationTitles.visibility = View.VISIBLE
                                        binding.tvNoRecordSuppTransaction.visibility = View.GONE
                                    }
                                    isFirstime = true


                                } else {

                                    when (it.data!!.code == Constants.ErrorCode) {
                                        true -> {
                                            Toast.makeText(
                                                contxt,
                                                it.data.errormessage?.message,
                                                Toast.LENGTH_LONG
                                            )
                                                .show()
                                        }
                                        false -> {
                                            contxt?.let { it1 -> CommonUtils.somethingWentWrong(it1) }
                                        }

                                    }
                                }
                                CommonUtils.hideProgress()

                            }
                            Status.ERROR -> {
                                CommonUtils.hideProgress()

                            }
                            Status.LOADING -> {
                                CommonUtils.showProgress(contxt)
                            }
                        }
                    }
                })
        }
    }

    private fun setTransactionHistoryDetails(
        transactionHistoryModel: TransactionHistoryModel,
        pageSize: Int
    ) {

        /*   binding.tvTotalEntrieSuppTransaction.text =
               transactionHistoryModel.transactionsum?.sum_entries.toString() + Constants.ENTRIES_APPEND
           when (transactionHistoryModel.transactionsum?.debit_sum_fine_wt.toString()) {
               "0.000" -> {
                   binding.tvTotalOutwardSuppTransactionOne.text =
                       transactionHistoryModel.transactionsum?.debit_sum_fine_wt.toString()
                   binding.tvTotalOutwardSuppTransactionOne.setTextColor(
                       ContextCompat.getColor(
                           contxt,
                           R.color.header_black_text
                       )
                   )
               }
               else -> {
                   binding.tvTotalOutwardSuppTransactionOne.text =
                       transactionHistoryModel.transactionsum?.debit_sum_fine_wt.toString()
                   binding.tvTotalOutwardSuppTransactionOne.setTextColor(
                       ContextCompat.getColor(
                           contxt,
                           R.color.outwardcolor
                       )
                   )
               }
           }
           when (transactionHistoryModel.transactionsum?.debit_sum_amount.toString()) {
               "0.00" -> {
                   binding.tvTotalOutwardSuppTransactionTwo.text =
                       transactionHistoryModel.transactionsum?.debit_sum_amount.toString()
                   binding.tvTotalOutwardSuppTransactionTwo.setTextColor(
                       ContextCompat.getColor(
                           contxt,
                           R.color.header_black_text
                       )
                   )
               }
               else -> {
                   binding.tvTotalOutwardSuppTransactionTwo.text =
                       transactionHistoryModel.transactionsum?.debit_sum_amount.toString()
                   binding.tvTotalOutwardSuppTransactionTwo.setTextColor(
                       ContextCompat.getColor(
                           contxt,
                           R.color.outwardcolor
                       )
                   )
               }
           }
           when (transactionHistoryModel.transactionsum?.credit_sum_fine_wt.toString()) {
               "0.000" -> {
                   binding.tvTotalInwardSuppTransactionOne.text =
                       transactionHistoryModel.transactionsum?.credit_sum_fine_wt.toString()
                   binding.tvTotalInwardSuppTransactionOne.setTextColor(
                       ContextCompat.getColor(
                           contxt,
                           R.color.header_black_text
                       )
                   )
               }
               else -> {
                   binding.tvTotalInwardSuppTransactionOne.text =
                       transactionHistoryModel.transactionsum?.credit_sum_fine_wt.toString()
                   binding.tvTotalInwardSuppTransactionOne.setTextColor(
                       ContextCompat.getColor(
                           contxt,
                           R.color.inwardcolor
                       )
                   )
               }
           }
           when (transactionHistoryModel.transactionsum?.credit_sum_amount.toString()) {
               "0.00" -> {
                   binding.tvTotalInwardSuppTransactionTwo.text =
                       transactionHistoryModel.transactionsum?.credit_sum_amount.toString()
                   binding.tvTotalInwardSuppTransactionTwo.setTextColor(
                       ContextCompat.getColor(
                           contxt,
                           R.color.header_black_text
                       )
                   )
               }
               else -> {
                   binding.tvTotalInwardSuppTransactionTwo.text =
                       transactionHistoryModel.transactionsum?.credit_sum_amount.toString()
                   binding.tvTotalInwardSuppTransactionTwo.setTextColor(
                       ContextCompat.getColor(
                           contxt,
                           R.color.inwardcolor
                       )
                   )
               }
           }*/
        if (!transactionHistoryModel.data.isNullOrEmpty()) {
            // for(data in transactionHistoryModel.data){


            if (currentPage != Constants.PAGE_START) adapter.removeLoading()
            adapter.apply {
                addtransactions(transactionHistoryModel.data, pageSize, currentPage, totalPage)
                notifyDataSetChanged()
            }
            if (currentPage < totalPage) {
                adapter.addLoading()
            } else {
                isLastPage = true
            }
            isLoading = false
        }

    }
}
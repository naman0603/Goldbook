package com.goldbookapp.ui.activity.additem

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatAutoCompleteTextView
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.goldbookapp.R
import com.goldbookapp.api.ApiHelper
import com.goldbookapp.api.RetrofitBuilder
import com.goldbookapp.databinding.AddItemChargesActivityNewBinding
import com.goldbookapp.model.*
import com.goldbookapp.ui.activity.viewmodel.AddItemViewModel
import com.goldbookapp.ui.activity.viewmodel.ViewModelFactory
import com.goldbookapp.ui.adapter.LessWeightChargesTotalAdapter
import com.goldbookapp.utils.CommonUtils
import com.goldbookapp.utils.CommonUtils.Companion.clickWithDebounce
import com.goldbookapp.utils.Constants
import com.goldbookapp.utils.Constants.Companion.PREF_MAKING_CHARGES_BREAKUP_INFO_KEY
import com.goldbookapp.utils.PreferenceHelper
import com.goldbookapp.utils.PreferenceHelper.get
import com.goldbookapp.utils.PreferenceHelper.set
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hamzasharuf.networkmonitor.ConnectivityStateHolder
import com.hamzasharuf.networkmonitor.Event
import com.hamzasharuf.networkmonitor.NetworkEvents
import kotlinx.android.synthetic.main.toolbar_layout.*
import kotlinx.android.synthetic.main.toolbar_layout.view.*
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat

class AddItemChargesActivity : AppCompatActivity() {
    var counting :Int = 0
    var isEditOrNew: Boolean = false
    var isOtherListLoadedFromPref: Boolean = false
    var isLessWtArrayLoadedFromPref: Boolean = false
    private lateinit var adapter: LessWeightChargesTotalAdapter
    private lateinit var viewModel: AddItemViewModel
    lateinit var binding: AddItemChargesActivityNewBinding
    lateinit var popupMenu: PopupMenu

    lateinit var prefs: SharedPreferences
    lateinit var loginModel: LoginModel
    var unitArrayList: List<ItemSearchModel.ItemSearch.Unit_array>? = null
    var unitNameList: List<String>? = null
    var selectedPerNameForMakingCharges: String = ""
    var selectedPerIdForMakingCharges: String = ""

    var selectedPerNameForOtherCharges1: String = ""
    var selectedPerIdforOtherCharges1: String = ""

    var selectedPerNameForOtherCharges2: String = ""
    var selectedPerIdforOtherCharges2: String = ""

    var selectedPerNameForOtherCharges3: String = ""
    var selectedPerIdforOtherCharges3: String = ""

    var selectedPerNameForOtherCharges4: String = ""
    var selectedPerIdforOtherCharges4: String = ""

    var selectedPerNameForOtherCharges5: String = ""
    var selectedPerIdforOtherCharges5: String = ""

    lateinit var itemSearchModel: ItemSearchModel.ItemSearch
    lateinit var lessweightList: ArrayList<AddLessWeightModel.AddLessWeightModelItem>
    lateinit var lesswtTotal: ArrayList<String> // it is for total calculation of less wt charges
    lateinit var listoflesswtChargesDetail: ArrayList<CalculationPaymentModel.DataPayment.ItemPayment.LessWeights> // this list is for less wt charges adapter
    lateinit var makingChargeBreakupList: OpeningStockItemModel.OpeningStockItemModelItem.ItemChargesBreakup

    val df = DecimalFormat("0.00")
    val df1 = DecimalFormat("0.000")

    var totalmakingChrgsUpdatedValue: String = "0.00"
    var totalOtherChrgsUpdatedValue: String = "0.00"
    var totalLwChargesUpdatedValue: String = "0.00"
    var totalOfAllChrgsUpdatedValue: String = "0.00"

    var is_studded: String = ""
    var selectedItemType: String = ""
    var piecesUpdatedValue: String = "0"
    var makingChrgsUpdatedValue: String = "0.00"
    var otherChrgs1UpdatedValue: String = "0.00"
    var otherChrgs2UpdatedValue: String = "0.00"
    var otherChrgs3UpdatedValue: String = "0.00"
    var otherChrgs4UpdatedValue: String = "0.00"
    var otherChrgs5UpdatedValue: String = "0.00"

    var otherChrgs1CalculatedUpdatedValue: String = "0.00"
    var otherChrgs2CalculatedUpdatedValue: String = "0.00"
    var otherChrgs3CalculatedUpdatedValue: String = "0.00"
    var otherChrgs4CalculatedUpdatedValue: String = "0.00"
    var otherChrgs5CalculatedUpdatedValue: String = "0.00"

    lateinit var othercharge1Model: OpeningStockItemModel.OpeningStockItemModelItem.ItemChargesBreakup.ChargesArray
    lateinit var othercharge2Model: OpeningStockItemModel.OpeningStockItemModelItem.ItemChargesBreakup.ChargesArray
    lateinit var othercharge3Model: OpeningStockItemModel.OpeningStockItemModelItem.ItemChargesBreakup.ChargesArray
    lateinit var othercharge4Model: OpeningStockItemModel.OpeningStockItemModelItem.ItemChargesBreakup.ChargesArray
    lateinit var othercharge5Model: OpeningStockItemModel.OpeningStockItemModelItem.ItemChargesBreakup.ChargesArray

    var netweightUpdatedValue: String = "0.000"

    var chargeView: Int? = -1  // 0 making charge //1 to 5 other charges

    var count: Int = 1 // 1 -> making charge 2 to 6 ->used for other click add other charge


    private var new_makingchargeBreakup_pos: Int = -1
    private var new_makingcharge_pos: Int = -1
    private var new_othercharge_pos: Int = -1
    var addMakingChargeBreakupList =
        ArrayList<OpeningStockItemModel.OpeningStockItemModelItem.ItemChargesBreakup>()

    //var addMakingChargeList : OpeningStockItemModel.OpeningStockItemModelItem.ItemWtBreakup.
    lateinit var addMakingChargeModel: OpeningStockItemModel.OpeningStockItemModelItem.ItemChargesBreakup.MakingChargeArray
    var addOtherChargeList =
        ArrayList<OpeningStockItemModel.OpeningStockItemModelItem.ItemChargesBreakup.ChargesArray>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.add_item_charges_activity_new)

        setupViewModel()
        setupUIandListner()
    }

    private fun setupUIandListner() {
        prefs = PreferenceHelper.defaultPrefs(this)
        loginModel = Gson().fromJson(
            prefs[Constants.PREF_LOGIN_DETAIL_KEY, ""],
            LoginModel::class.java
        )
        binding.root.imgLeft.setImageResource(R.drawable.ic_back)
        binding.root.imgLeft.clickWithDebounce {
            onBackPressed()
        }
        tvTitle.setText(R.string.add_charges)
        if(intent.extras?.containsKey("Edit_New")!!){
            isEditOrNew = intent.getBooleanExtra("Edit_New",false)
            if(!isEditOrNew){
                addOtherChargeList.add(0,OpeningStockItemModel.OpeningStockItemModelItem.ItemChargesBreakup.ChargesArray("","","",""))
            }

            Log.d("Edit_New","$isEditOrNew")
        }
        if (intent.extras?.containsKey(Constants.SELECTED_ITEM_DATA_MODEL)!!) {
            val seleceted_item: String? = intent.getStringExtra(Constants.SELECTED_ITEM_DATA_MODEL)
            itemSearchModel = Gson().fromJson(
                seleceted_item,
                ItemSearchModel.ItemSearch::class.java
            )
            Log.d("ItemSearchModel",itemSearchModel.toString())
            piecesUpdatedValue = itemSearchModel.unit_value
            if(!itemSearchModel.net_wt.equals("")){
                netweightUpdatedValue = itemSearchModel.net_wt
                Log.v("netweightUpdatedVal1", netweightUpdatedValue)
            }
            //netweightUpdatedValue = itemSearchModel.net_wt

            Log.v("piecesUpdatedVal", piecesUpdatedValue)
            Log.v("netweightUpdatedVal", netweightUpdatedValue)

            is_studded = itemSearchModel.is_studded
            selectedItemType = itemSearchModel.item_type
            unitArrayList = itemSearchModel.unit_array
            unitNameList = unitArrayList?.map { it.name }

            when(selectedItemType.equals("Goods")){
                true->{
                    binding.txtMakingChargesValue.setText(itemSearchModel.sales_making_charges)
                    makingChrgsUpdatedValue = itemSearchModel.sales_making_charges
                    selectedPerIdForMakingCharges = itemSearchModel.unit_array.get(1).id
                    selectedPerNameForMakingCharges = itemSearchModel.maintain_stock_in_name
                    binding.txtMakingChargesPerAddCharge.setText(selectedPerNameForMakingCharges)

                }
                else->{

                }

            }

            updateValuesasPerDDSelection(false, selectedPerIdForMakingCharges, 1, false)
        }
        if(prefs.contains(PREF_MAKING_CHARGES_BREAKUP_INFO_KEY)){
            val makingChargeBreakup =
                object :
                    TypeToken<OpeningStockItemModel.OpeningStockItemModelItem.ItemChargesBreakup>() {}.type
            makingChargeBreakupList = Gson().fromJson(
                prefs[Constants.PREF_MAKING_CHARGES_BREAKUP_INFO_KEY, ""],
                makingChargeBreakup
            )
            binding.txttotalChargesValue.setText(Constants.AMOUNT_RS_APPEND + makingChargeBreakupList.total_charges)
            totalOfAllChrgsUpdatedValue = makingChargeBreakupList.total_charges
        }
        // default 1 charge visible.
        Log.d("Count_Start","$count")
        intializeOtherChargesModel(count)
        ontextChangeSetup()
        onFocusChangeSetup()

        binding.txtMakingChargesPerAddCharge.clickWithDebounce {
            //1 -> making charge
            openPerForMakingnOtherCharges(binding.txtMakingChargesPerAddCharge, 1)
        }
        //2 to 6  -> other charge click track
        binding.txtCharges1Per.clickWithDebounce {
            openPerForMakingnOtherCharges(binding.txtCharges1Per, 2)
        }
        binding.txtCharges2Per.clickWithDebounce {
            openPerForMakingnOtherCharges(binding.txtCharges2Per, 3)
        }
        binding.txtCharges3Per.clickWithDebounce {
            openPerForMakingnOtherCharges(binding.txtCharges3Per, 4)
        }
        binding.txtCharges4Per.clickWithDebounce {
            openPerForMakingnOtherCharges(binding.txtCharges4Per, 5)
        }
        binding.txtCharges5Per.clickWithDebounce {
            openPerForMakingnOtherCharges(binding.txtCharges5Per, 6)
        }

        binding.tvAddChargeAddItem.clickWithDebounce {
            count = count + 1
            Log.d("Count_Size","$count")
            addOtherChargeList.add(count-1,OpeningStockItemModel.OpeningStockItemModelItem.ItemChargesBreakup.ChargesArray("","","",""))
            Log.d("Charges_List_Add","$addOtherChargeList")
            clearAllfocus()
            intializeOtherChargesModel(count)
            visibleGoneChargeAsPerCount(count)
            Log.d("Count_Add","$count")
        }
        binding.imgCloseCharge1.clickWithDebounce {
            //tempSaveOtherCharge1Data()
            clearAllfocus()
            count = count - 1
            Log.v("imgClose1count", count.toString())

            setupTotalCharges(true, 1)
            moveNSetOtherChargesUIData(1, count)
            updateValuesasPerDDSelection(true, selectedPerIdforOtherCharges1, 2, true)
            // setupTotalCharges(false)
            visibleGoneChargeAsPerCount(count)

        }

        binding.imgCloseCharge2.clickWithDebounce {
            // tempSaveOtherCharge2Data()
            clearAllfocus()
            count = count - 1
            Log.v("imgclose2count", count.toString())
            setupTotalCharges(true, 2)
            moveNSetOtherChargesUIData(2, count)
            updateValuesasPerDDSelection(true, selectedPerIdforOtherCharges2, 3, true)
            // setupTotalCharges(false)
            visibleGoneChargeAsPerCount(count)

            /* otherChrgs2UpdatedValue = "0.00"
             binding.txtCharges2Value.setText(otherChrgs2UpdatedValue)
             binding.txtCharges2Value.setSelection(otherChrgs2UpdatedValue.length)*/
        }
        binding.imgCloseCharge3.clickWithDebounce {
            //tempSaveOtherCharge3Data()
            clearAllfocus()
            count = count - 1
            Log.v("imgClose3count", count.toString())
            setupTotalCharges(true, 3)
            moveNSetOtherChargesUIData(3, count)
            updateValuesasPerDDSelection(true, selectedPerIdforOtherCharges3, 4, true)
            // setupTotalCharges(false)
            visibleGoneChargeAsPerCount(count)

            /* otherChrgs3UpdatedValue = "0.00"
            binding.txtCharges3Value.setText(otherChrgs3UpdatedValue)
            binding.txtCharges3Value.setSelection(otherChrgs3UpdatedValue.length)*/
        }
        binding.imgCloseCharge4.clickWithDebounce {
            //tempSaveOtherCharge4Data()
            clearAllfocus()
            count = count - 1
            Log.v("imgClose4count", count.toString())
            setupTotalCharges(true, 4)
            moveNSetOtherChargesUIData(4, count)
            updateValuesasPerDDSelection(true, selectedPerIdforOtherCharges4, 5, true)
            // setupTotalCharges(false)
            visibleGoneChargeAsPerCount(count)

            /*otherChrgs4UpdatedValue = "0.00"
            binding.txtCharges4Value.setText(otherChrgs4UpdatedValue)
            binding.txtCharges4Value.setSelection(otherChrgs4UpdatedValue.length)*/
        }
        binding.imgCloseCharge5.clickWithDebounce {
            //tempSaveOtherCharge5Data()
            clearAllfocus()
            count = count - 1
            Log.v("imgClose5count", count.toString())

            setupTotalCharges(true, 5)
            moveNSetOtherChargesUIData(5, count)
            updateValuesasPerDDSelection(true, selectedPerIdforOtherCharges5, 6, true)
            // setupTotalCharges(false)
            visibleGoneChargeAsPerCount(count)

            /*otherChrgs5UpdatedValue = "0.00"
            binding.txtCharges5Value.setText(otherChrgs5UpdatedValue)
            binding.txtCharges5Value.setSelection(otherChrgs5UpdatedValue.length)*/
        }

        binding.btnSaveAddCharges.clickWithDebounce {
            clearAllfocus()
            saveMakingChargesModel()
            saveOtherChargeModelAccordingtoNoofCharges()
            saveOtherChargeInPref()
            saveItemChargesBreakupModel()
            finish()
        }
    }

    private fun intializeOtherChargesModel(count: Int) {
        Log.d("Count_Start","$count")
        when (count) {
            1 -> {
                tempSaveOtherCharge1Data()
            }
            2 -> {
                tempSaveOtherCharge2Data()
            }
            3 -> {
                tempSaveOtherCharge3Data()
            }
            4 -> {
                tempSaveOtherCharge4Data()
            }
            5 -> {
                tempSaveOtherCharge5Data()
            }
        }
    }

    // other 1 to 5 click track
    private fun moveNSetOtherChargesUIData(clickTrack: Int, count: Int) {
        Log.d("Size_Array",addOtherChargeList.size.toString())
        addOtherChargeList.removeAt(clickTrack-1)

        Log.d("Update_List_Cancel","$addOtherChargeList")

        when (clickTrack) {

            // other 1 imgclose click
            1 -> {

                when (count) {
                    0 -> {
                        // no other charges visible
                        othercharge1Model.amount = "0.00"
                        othercharge1Model.label = ""
                        othercharge1Model.unit_name = ""
                        othercharge1Model.unit_id = ""
                        binding.txtCharges1Label.setText(othercharge1Model.label)
                        binding.txtCharges1Per.setText(othercharge1Model.unit_name)

                        otherChrgs1UpdatedValue = "0.00"
                        otherChrgs1CalculatedUpdatedValue = "0.00"
                        binding.txtCharges1Value.setText(otherChrgs1UpdatedValue)
                        binding.txtCharges1Value.setSelection(otherChrgs1UpdatedValue.length)
                        Log.v("othercharge1Model", "$othercharge1Model")
                        Log.d("Update_List","$addOtherChargeList")

                    }
                    1 -> {
                        // from charge 2 to charge 1 and charge 2 -> 0
                        othercharge1Model = othercharge2Model
                        otherChrgs1UpdatedValue = otherChrgs2UpdatedValue
                        otherChrgs1CalculatedUpdatedValue = otherChrgs2CalculatedUpdatedValue
                        binding.txtCharges1Value.setText(othercharge1Model.amount)
                        binding.txtCharges1Value.setSelection(othercharge1Model.amount.length)
                        selectedPerIdforOtherCharges1 = othercharge1Model.unit_id
                        selectedPerNameForOtherCharges1 = othercharge1Model.unit_name
                        binding.txtCharges1Label.setText(othercharge1Model.label)
                        binding.txtCharges1Per.setText(othercharge1Model.unit_name)
                        Log.v("othercharge1Model", "$othercharge1Model")
                        Log.d("Update_List","$addOtherChargeList")

//                        othercharge2Model.amount = "0.00"
//                        othercharge2Model.label = ""
//                        othercharge2Model.unit_name = ""
//                        othercharge2Model.unit_id = ""
                        otherChrgs2UpdatedValue = "0.00"
                        otherChrgs2CalculatedUpdatedValue = "0.00"
                        binding.txtCharges2Value.setText(otherChrgs2UpdatedValue)
                        binding.txtCharges2Value.setSelection(otherChrgs2UpdatedValue.length)
                        binding.txtCharges2Label.setText(othercharge2Model.label)
                        binding.txtCharges2Per.setText(othercharge2Model.unit_name)
                        Log.v("othercharge2Model", "$othercharge2Model")
                        Log.d("Update_List","$addOtherChargeList")


                    }
                    2 -> {
                        // from charge 2 to charge 1 and charge 2 -> 0
                        othercharge1Model = othercharge2Model
                        otherChrgs1UpdatedValue = otherChrgs2UpdatedValue
                        otherChrgs1CalculatedUpdatedValue = otherChrgs2CalculatedUpdatedValue
                        binding.txtCharges1Value.setText(othercharge1Model.amount)
                        binding.txtCharges1Value.setSelection(othercharge1Model.amount.length)
                        selectedPerIdforOtherCharges1 = othercharge1Model.unit_id
                        selectedPerNameForOtherCharges1 = othercharge1Model.unit_name
                        binding.txtCharges1Label.setText(othercharge1Model.label)
                        binding.txtCharges1Per.setText(othercharge1Model.unit_name)
                        Log.v("othercharge1Model", "$othercharge1Model")
                        Log.d("Update_List","$addOtherChargeList")


                       /* othercharge2Model.amount = "0.00"
                        othercharge2Model.label = ""
                        othercharge2Model.unit_name = ""
                        othercharge2Model.unit_id = ""*/
                        otherChrgs2UpdatedValue = "0.00"
                        otherChrgs2CalculatedUpdatedValue = "0.00"
                        binding.txtCharges2Value.setText(otherChrgs2UpdatedValue)
                        binding.txtCharges2Value.setSelection(otherChrgs2UpdatedValue.length)
                        binding.txtCharges2Label.setText(othercharge2Model.label)
                        binding.txtCharges2Per.setText(othercharge2Model.unit_name)
                        Log.v("othercharge2Model", "$othercharge2Model")
                        Log.d("Update_List","$addOtherChargeList")

                        // from charge 3 to charge 2 and charge 3 -> 0
                        othercharge2Model = othercharge3Model
                        otherChrgs2UpdatedValue = otherChrgs3UpdatedValue
                        otherChrgs2CalculatedUpdatedValue = otherChrgs3CalculatedUpdatedValue
                        binding.txtCharges2Value.setText(othercharge2Model.amount)
                        binding.txtCharges2Value.setSelection(othercharge2Model.amount.length)
                        selectedPerIdforOtherCharges2 = othercharge2Model.unit_id
                        selectedPerNameForOtherCharges2 = othercharge2Model.unit_name
                        binding.txtCharges2Label.setText(othercharge2Model.label)
                        binding.txtCharges2Per.setText(othercharge2Model.unit_name)
                        Log.v("othercharge2Model", "$othercharge2Model")
                        Log.d("Update_List","$addOtherChargeList")


                       /* othercharge3Model.amount = "0.00"
                        othercharge3Model.label = ""
                        othercharge3Model.unit_name = ""
                        othercharge3Model.unit_id = ""*/
                        otherChrgs3UpdatedValue = "0.00"
                        otherChrgs3CalculatedUpdatedValue = "0.00"
                        binding.txtCharges3Value.setText(otherChrgs3UpdatedValue)
                        binding.txtCharges3Value.setSelection(otherChrgs3UpdatedValue.length)
                        binding.txtCharges3Label.setText(othercharge3Model.label)
                        binding.txtCharges3Per.setText(othercharge3Model.unit_name)
                        Log.v("othercharge3Model", "$othercharge3Model")
                        Log.d("Update_List","$addOtherChargeList")

                    }
                    3 -> {
                        counting += 1
                        Log.d("Counting ",counting.toString())
                        // from charge 2 to charge 1 and charge 2 -> 0
                        Log.d("Value_Text_Amount_Model","${othercharge2Model}")
                        othercharge1Model = othercharge2Model
                        otherChrgs1UpdatedValue = otherChrgs2UpdatedValue
                        otherChrgs1CalculatedUpdatedValue = otherChrgs2CalculatedUpdatedValue
                        Log.d("Value_Text_Amount_Model","${othercharge1Model}")
                        binding.txtCharges1Value.setText(othercharge1Model.amount)
                        binding.txtCharges1Value.setSelection(othercharge1Model.amount.length)
                        selectedPerIdforOtherCharges1 = othercharge1Model.unit_id
                        selectedPerNameForOtherCharges1 = othercharge1Model.unit_name
                        binding.txtCharges1Label.setText(othercharge1Model.label)
                        binding.txtCharges1Per.setText(othercharge1Model.unit_name)
                        Log.v("othercharge1Model", "$othercharge1Model")
                        Log.d("Update_List","$addOtherChargeList")
                        Log.d("Value_Text_Amount","${binding.txtCharges1Value.text}")



                       /* othercharge2Model.amount = "0.00"
                        othercharge2Model.label = ""
                        othercharge2Model.unit_name = ""
                        othercharge2Model.unit_id = ""
                        otherChrgs2UpdatedValue = "0.00"*/
                        binding.txtCharges2Value.setText(otherChrgs2UpdatedValue)
                        binding.txtCharges2Value.setSelection(otherChrgs2UpdatedValue.length)
                        binding.txtCharges2Label.setText(othercharge2Model.label)
                        binding.txtCharges2Per.setText(othercharge2Model.unit_name)
                        Log.v("othercharge2Model", "$othercharge2Model")
                        Log.d("Update_List","$addOtherChargeList")

                        // from charge 3 to charge 2 and charge 3 -> 0
                        Log.d("Value_Text_Amount_Model","${othercharge3Model}")
                        othercharge2Model = othercharge3Model
                        otherChrgs2UpdatedValue = otherChrgs3UpdatedValue
                        otherChrgs2CalculatedUpdatedValue = otherChrgs3CalculatedUpdatedValue
                        Log.d("Value_Text_Amount_Model","${othercharge2Model}")
                        Log.d("Value_Text_Amount","${othercharge2Model.amount}")
                        binding.txtCharges2Value.setText(othercharge2Model.amount)
                        binding.txtCharges2Value.setSelection(othercharge2Model.amount.length)
                        selectedPerIdforOtherCharges2 = othercharge2Model.unit_id
                        selectedPerNameForOtherCharges2 = othercharge2Model.unit_name
                        binding.txtCharges2Label.setText(othercharge2Model.label)
                        binding.txtCharges2Per.setText(othercharge2Model.unit_name)
                        Log.v("othercharge2Model", "$othercharge2Model")
                        Log.d("Update_List","$addOtherChargeList")
                        Log.d("Value_Text_Amount","${binding.txtCharges2Value.text}")


                      /*  othercharge3Model.amount = "0.00"
                        othercharge3Model.label = ""
                        othercharge3Model.unit_name = ""
                        othercharge3Model.unit_id = ""
                        otherChrgs3UpdatedValue = "0.00"*/
                        otherChrgs3CalculatedUpdatedValue = "0.00"
                        binding.txtCharges3Value.setText(otherChrgs3UpdatedValue)
                        binding.txtCharges3Value.setSelection(otherChrgs3UpdatedValue.length)
                        binding.txtCharges3Label.setText(othercharge3Model.label)
                        binding.txtCharges3Per.setText(othercharge3Model.unit_name)
                        Log.v("othercharge3Model", "$othercharge3Model")
                        Log.d("Update_List","$addOtherChargeList")


                        // from charge 4 to charge 3 and charge 4 -> 0
                        Log.d("Value_Text_Amount_Model","${othercharge4Model}")
                        othercharge3Model = othercharge4Model
                        otherChrgs3UpdatedValue = otherChrgs4UpdatedValue
                        otherChrgs3CalculatedUpdatedValue = otherChrgs4CalculatedUpdatedValue
                        Log.d("Value_Text_Amount_Model","${othercharge3Model}")
                        Log.d("Value_Text_Amount","${othercharge3Model.amount}")
                        binding.txtCharges3Value.setText(othercharge3Model.amount)
                        binding.txtCharges3Value.setSelection(othercharge3Model.amount.length)
                        selectedPerIdforOtherCharges3 = othercharge3Model.unit_id
                        selectedPerNameForOtherCharges3 = othercharge3Model.unit_name
                        binding.txtCharges3Label.setText(othercharge3Model.label)
                        binding.txtCharges3Per.setText(othercharge3Model.unit_name)
                        Log.v("othercharge3Model", "$othercharge3Model")
                        Log.d("Update_List","$addOtherChargeList")
                        Log.d("Value_Text_Amount","${binding.txtCharges3Value.text}")


                      /*  othercharge4Model.amount = "0.00"
                        othercharge4Model.label = ""
                        othercharge4Model.unit_name = ""
                        othercharge4Model.unit_id = ""*/
                        otherChrgs4UpdatedValue = "0.00"
                        otherChrgs4CalculatedUpdatedValue = "0.00"
                        binding.txtCharges4Value.setText(otherChrgs4UpdatedValue)
                        binding.txtCharges4Value.setSelection(otherChrgs4UpdatedValue.length)
                        binding.txtCharges4Label.setText(othercharge4Model.label)
                        binding.txtCharges4Per.setText(othercharge4Model.unit_name)
                        Log.v("othercharge4Model", "$othercharge4Model")
                        Log.d("Update_List","$addOtherChargeList")


                    }
                    4 -> {
                        Log.d("Value_Text_Amount_Model","Before")

                        Log.d("Value_Text_Amount_Model","${othercharge1Model}")
                        Log.d("Value_Text_Amount_Model","${othercharge2Model}")
                        Log.d("Value_Text_Amount_Model","${othercharge3Model}")
                        Log.d("Value_Text_Amount_Model","${othercharge4Model}")

                        // from charge 2 to charge 1 and charge 2 -> 0
                        othercharge1Model = othercharge2Model
                        otherChrgs1UpdatedValue = otherChrgs2UpdatedValue
                        otherChrgs1CalculatedUpdatedValue = otherChrgs2CalculatedUpdatedValue
                        binding.txtCharges1Value.setText(othercharge1Model.amount)
                        binding.txtCharges1Value.setSelection(othercharge1Model.amount.length)
                        selectedPerIdforOtherCharges1 = othercharge1Model.unit_id
                        selectedPerNameForOtherCharges1 = othercharge1Model.unit_name
                        binding.txtCharges1Label.setText(othercharge1Model.label)
                        binding.txtCharges1Per.setText(othercharge1Model.unit_name)
                        Log.v("othercharge1Model", "$othercharge1Model")
                        Log.d("Update_List","$addOtherChargeList")


                        Log.d("Value_Check","${othercharge1Model}")
                        Log.d("Value_Check","${othercharge2Model}")
                        Log.d("Value_Check","${othercharge3Model}")
                        Log.d("Value_Check","${othercharge4Model}")
                        //othercharge2Model.amount = "0.00"
                        Log.d("Value_Check_Amount","${othercharge1Model}")
                        Log.d("Value_Check_Amount","${othercharge2Model}")
                        Log.d("Value_Check_Amount","${othercharge3Model}")
                        Log.d("Value_Check_Amount","${othercharge4Model}")
                        //othercharge2Model.label = ""
                        Log.d("Value_Check_Label","${othercharge1Model}")
                        Log.d("Value_Check_Label","${othercharge2Model}")
                        Log.d("Value_Check_Label","${othercharge3Model}")
                        Log.d("Value_Check_Label","${othercharge4Model}")
                        //othercharge2Model.unit_name = ""
                        Log.d("Value_Check_Name","${othercharge1Model}")
                        Log.d("Value_Check_Name","${othercharge2Model}")
                        Log.d("Value_Check_Name","${othercharge3Model}")
                        Log.d("Value_Check_Name","${othercharge4Model}")
                      //  othercharge2Model.unit_id = ""
                        Log.d("Value_Check_Id","${othercharge1Model}")
                        Log.d("Value_Check_Id","${othercharge2Model}")
                        Log.d("Value_Check_Id","${othercharge3Model}")
                        Log.d("Value_Check_Id","${othercharge4Model}")
                        otherChrgs2UpdatedValue = "0.00"
                        otherChrgs2CalculatedUpdatedValue = "0.00"
                        binding.txtCharges2Value.setText(otherChrgs2UpdatedValue)
                        binding.txtCharges2Value.setSelection(otherChrgs2UpdatedValue.length)
                        binding.txtCharges2Label.setText(othercharge2Model.label)
                        binding.txtCharges2Per.setText(othercharge2Model.unit_name)
                        Log.v("othercharge2Model", "$othercharge2Model")
                        Log.d("Update_List","$addOtherChargeList")

                        Log.d("Value_Text_Amount_Model","After 1st")
                        Log.d("Value_Text_Amount_Model","${othercharge1Model}")
                        Log.d("Value_Text_Amount_Model","${othercharge2Model}")
                        Log.d("Value_Text_Amount_Model","${othercharge3Model}")
                        Log.d("Value_Text_Amount_Model","${othercharge4Model}")

                        // from charge 3 to charge 2 and charge 3 -> 0
                        othercharge2Model = othercharge3Model
                        otherChrgs2UpdatedValue = otherChrgs3UpdatedValue
                        otherChrgs2CalculatedUpdatedValue = otherChrgs3CalculatedUpdatedValue
                        binding.txtCharges2Value.setText(othercharge2Model.amount)
                        binding.txtCharges2Value.setSelection(othercharge2Model.amount.length)
                        selectedPerIdforOtherCharges2 = othercharge2Model.unit_id
                        selectedPerNameForOtherCharges2 = othercharge2Model.unit_name
                        binding.txtCharges2Label.setText(othercharge2Model.label)
                        binding.txtCharges2Per.setText(othercharge2Model.unit_name)
                        Log.v("othercharge2Model", "$othercharge2Model")
                        Log.d("Update_List","$addOtherChargeList")

                      /*  othercharge3Model.amount = "0.00"
                        othercharge3Model.label = ""
                        othercharge3Model.unit_name = ""
                        othercharge3Model.unit_id = ""*/
                        otherChrgs3UpdatedValue = "0.00"
                        otherChrgs3CalculatedUpdatedValue = "0.00"
                        binding.txtCharges3Value.setText(otherChrgs3UpdatedValue)
                        binding.txtCharges3Value.setSelection(otherChrgs3UpdatedValue.length)
                        binding.txtCharges3Label.setText(othercharge3Model.label)
                        binding.txtCharges3Per.setText(othercharge3Model.unit_name)
                        Log.v("othercharge3Model", "$othercharge3Model")
                        Log.d("Update_List","$addOtherChargeList")

                        Log.d("Value_Text_Amount_Model","After 2nd")
                        Log.d("Value_Text_Amount_Model","${othercharge1Model}")
                        Log.d("Value_Text_Amount_Model","${othercharge2Model}")
                        Log.d("Value_Text_Amount_Model","${othercharge3Model}")
                        Log.d("Value_Text_Amount_Model","${othercharge4Model}")

                        // from charge 4 to charge 3 and charge 4 -> 0
                        othercharge3Model = othercharge4Model
                        otherChrgs3UpdatedValue = otherChrgs4UpdatedValue
                        otherChrgs3CalculatedUpdatedValue = otherChrgs4CalculatedUpdatedValue
                        binding.txtCharges3Value.setText(othercharge3Model.amount)
                        binding.txtCharges3Value.setSelection(othercharge3Model.amount.length)
                        selectedPerIdforOtherCharges3 = othercharge3Model.unit_id
                        selectedPerNameForOtherCharges3 = othercharge3Model.unit_name
                        binding.txtCharges3Label.setText(othercharge3Model.label)
                        binding.txtCharges3Per.setText(othercharge3Model.unit_name)
                        Log.v("othercharge3Model", "$othercharge3Model")
                        Log.d("Update_List","$addOtherChargeList")

                      /*  othercharge4Model.amount = "0.00"
                        othercharge4Model.label = ""
                        othercharge4Model.unit_name = ""
                        othercharge4Model.unit_id = ""*/
                        otherChrgs4UpdatedValue = "0.00"
                        otherChrgs4CalculatedUpdatedValue = "0.00"
                        binding.txtCharges4Value.setText(otherChrgs4UpdatedValue)
                        binding.txtCharges4Value.setSelection(otherChrgs4UpdatedValue.length)
                        binding.txtCharges4Label.setText(othercharge4Model.label)
                        binding.txtCharges4Per.setText(othercharge4Model.unit_name)
                        Log.v("othercharge4Model", "$othercharge4Model")
                        Log.d("Update_List","$addOtherChargeList")

                        Log.d("Value_Text_Amount_Model","After 3rd")
                        Log.d("Value_Text_Amount_Model","${othercharge1Model}")
                        Log.d("Value_Text_Amount_Model","${othercharge2Model}")
                        Log.d("Value_Text_Amount_Model","${othercharge3Model}")
                        Log.d("Value_Text_Amount_Model","${othercharge4Model}")

                        // from charge 5 to charge 4 and charge 5 -> 0
                        othercharge4Model = othercharge5Model
                        otherChrgs4UpdatedValue = otherChrgs5UpdatedValue
                        otherChrgs4CalculatedUpdatedValue = otherChrgs5CalculatedUpdatedValue
                        binding.txtCharges4Value.setText(othercharge4Model.amount)
                        binding.txtCharges4Value.setSelection(othercharge4Model.amount.length)
                        selectedPerIdforOtherCharges4 = othercharge4Model.unit_id
                        selectedPerNameForOtherCharges4 = othercharge4Model.unit_name
                        binding.txtCharges4Label.setText(othercharge4Model.label)
                        binding.txtCharges4Per.setText(othercharge4Model.unit_name)
                        Log.v("othercharge4Model", "$othercharge4Model")
                        Log.d("Update_List","$addOtherChargeList")

                      /*  othercharge5Model.amount = "0.00"
                        othercharge5Model.label = ""
                        othercharge5Model.unit_name = ""
                        othercharge5Model.unit_id = ""*/
                        otherChrgs5UpdatedValue = "0.00"
                        otherChrgs5CalculatedUpdatedValue = "0.00"
                        binding.txtCharges5Value.setText(otherChrgs5UpdatedValue)
                        binding.txtCharges5Value.setSelection(otherChrgs5UpdatedValue.length)
                        binding.txtCharges5Label.setText(othercharge5Model.label)
                        binding.txtCharges5Per.setText(othercharge5Model.unit_name)
                        Log.v("othercharge5Model", "$othercharge5Model")
                        Log.d("Update_List","$addOtherChargeList")

                        Log.d("Value_Text_Amount_Model","After 4th")
                        Log.d("Value_Text_Amount_Model","${othercharge1Model}")
                        Log.d("Value_Text_Amount_Model","${othercharge2Model}")
                        Log.d("Value_Text_Amount_Model","${othercharge3Model}")
                        Log.d("Value_Text_Amount_Model","${othercharge4Model}")

                        Log.d("Value_Text_Amount_Model","End")
                        Log.d("Value_Text_Amount_Model","${othercharge1Model}")
                        Log.d("Value_Text_Amount_Model","${othercharge2Model}")
                        Log.d("Value_Text_Amount_Model","${othercharge3Model}")
                        Log.d("Value_Text_Amount_Model","${othercharge4Model}")


                    }
                }
            }
            // other 2 imgclose click
            2 -> {
                when (count) {
                    // zero will never the case as click from 2 imgclose
                    1 -> {
                        // charge 2 -> 0
                        othercharge2Model.amount = "0.00"
                        othercharge2Model.label = ""
                        othercharge2Model.unit_name = ""
                        othercharge2Model.unit_id = ""
                        otherChrgs2UpdatedValue = "0.00"
                        otherChrgs2CalculatedUpdatedValue = "0.00"
                        binding.txtCharges2Value.setText(otherChrgs2UpdatedValue)
                        binding.txtCharges2Value.setSelection(otherChrgs2UpdatedValue.length)
                        binding.txtCharges2Label.setText(othercharge2Model.label)
                        binding.txtCharges2Per.setText(othercharge2Model.unit_name)
                        Log.v("othercharge2Model", "$othercharge2Model")
                        Log.d("Update_List","$addOtherChargeList")

                    }
                    2 -> {
                        // from charge 3 to charge 2 and charge 3 -> 0
                        othercharge2Model = othercharge3Model
                        otherChrgs2UpdatedValue = otherChrgs3UpdatedValue
                        otherChrgs2CalculatedUpdatedValue = otherChrgs3CalculatedUpdatedValue
                        binding.txtCharges2Value.setText(othercharge2Model.amount)
                        binding.txtCharges2Value.setSelection(othercharge2Model.amount.length)
                        selectedPerIdforOtherCharges2 = othercharge2Model.unit_id
                        selectedPerNameForOtherCharges2 = othercharge2Model.unit_name
                        binding.txtCharges2Label.setText(othercharge2Model.label)
                        binding.txtCharges2Per.setText(othercharge2Model.unit_name)
                        Log.v("othercharge2Model", "$othercharge2Model")
                        Log.d("Update_List","$addOtherChargeList")

                       /* othercharge3Model.amount = "0.00"
                        othercharge3Model.label = ""
                        othercharge3Model.unit_name = ""
                        othercharge3Model.unit_id = ""*/
                        otherChrgs3UpdatedValue = "0.00"
                        otherChrgs3CalculatedUpdatedValue = "0.00"
                        binding.txtCharges3Value.setText(otherChrgs3UpdatedValue)
                        binding.txtCharges3Value.setSelection(otherChrgs3UpdatedValue.length)
                        binding.txtCharges3Label.setText(othercharge3Model.label)
                        binding.txtCharges3Per.setText(othercharge3Model.unit_name)
                        Log.v("othercharge3Model", "$othercharge3Model")
                        Log.d("Update_List","$addOtherChargeList")

                    }
                    3 -> {
                        // from charge 3 to charge 2 and charge 3 -> 0
                        othercharge2Model = othercharge3Model
                        otherChrgs2UpdatedValue = otherChrgs3UpdatedValue
                        otherChrgs2CalculatedUpdatedValue = otherChrgs3CalculatedUpdatedValue
                        binding.txtCharges2Value.setText(othercharge2Model.amount)
                        binding.txtCharges2Value.setSelection(othercharge2Model.amount.length)
                        selectedPerIdforOtherCharges2 = othercharge2Model.unit_id
                        selectedPerNameForOtherCharges2 = othercharge2Model.unit_name
                        binding.txtCharges2Label.setText(othercharge2Model.label)
                        binding.txtCharges2Per.setText(othercharge2Model.unit_name)
                        Log.v("othercharge2Model", "$othercharge2Model")
                        Log.d("Update_List","$addOtherChargeList")

                      /*  othercharge3Model.amount = "0.00"
                        othercharge3Model.label = ""
                        othercharge3Model.unit_name = ""
                        othercharge3Model.unit_id = ""*/
                        otherChrgs3UpdatedValue = "0.00"
                        otherChrgs3CalculatedUpdatedValue = "0.00"
                        binding.txtCharges3Value.setText(otherChrgs3UpdatedValue)
                        binding.txtCharges3Value.setSelection(otherChrgs3UpdatedValue.length)
                        binding.txtCharges3Label.setText(othercharge3Model.label)
                        binding.txtCharges3Per.setText(othercharge3Model.unit_name)
                        Log.v("othercharge3Model", "$othercharge3Model")
                        Log.d("Update_List","$addOtherChargeList")


                        // from charge 4 to charge 3 and charge 4 -> 0
                        othercharge3Model = othercharge4Model
                        otherChrgs3UpdatedValue = otherChrgs4UpdatedValue
                        otherChrgs3CalculatedUpdatedValue = otherChrgs4CalculatedUpdatedValue
                        binding.txtCharges3Value.setText(othercharge3Model.amount)
                        binding.txtCharges3Value.setSelection(othercharge3Model.amount.length)
                        selectedPerIdforOtherCharges3 = othercharge3Model.unit_id
                        selectedPerNameForOtherCharges3 = othercharge3Model.unit_name
                        binding.txtCharges3Label.setText(othercharge3Model.label)
                        binding.txtCharges3Per.setText(othercharge3Model.unit_name)
                        Log.v("othercharge3Model", "$othercharge3Model")
                        Log.d("Update_List","$addOtherChargeList")

                      /*  othercharge4Model.amount = "0.00"
                        othercharge4Model.label = ""
                        othercharge4Model.unit_name = ""
                        othercharge4Model.unit_id = ""*/
                        otherChrgs4UpdatedValue = "0.00"
                        otherChrgs4CalculatedUpdatedValue = "0.00"
                        binding.txtCharges4Value.setText(otherChrgs4UpdatedValue)
                        binding.txtCharges4Value.setSelection(otherChrgs4UpdatedValue.length)
                        binding.txtCharges4Label.setText(othercharge4Model.label)
                        binding.txtCharges4Per.setText(othercharge4Model.unit_name)
                        Log.v("othercharge4Model", "$othercharge4Model")
                        Log.d("Update_List","$addOtherChargeList")

                    }
                    4 -> {
                        // from charge 3 to charge 2 and charge 3 -> 0
                        othercharge2Model = othercharge3Model
                        otherChrgs2UpdatedValue = otherChrgs3UpdatedValue
                        otherChrgs2CalculatedUpdatedValue = otherChrgs3CalculatedUpdatedValue
                        binding.txtCharges2Value.setText(othercharge2Model.amount)
                        binding.txtCharges2Value.setSelection(othercharge2Model.amount.length)
                        selectedPerIdforOtherCharges2 = othercharge2Model.unit_id
                        selectedPerNameForOtherCharges2 = othercharge2Model.unit_name
                        binding.txtCharges2Label.setText(othercharge2Model.label)
                        binding.txtCharges2Per.setText(othercharge2Model.unit_name)
                        Log.v("othercharge2Model", "$othercharge2Model")
                        Log.d("Update_List","$addOtherChargeList")

                       /* othercharge3Model.amount = "0.00"
                        othercharge3Model.label = ""
                        othercharge3Model.unit_name = ""
                        othercharge3Model.unit_id = ""*/
                        otherChrgs3UpdatedValue = "0.00"
                        otherChrgs3CalculatedUpdatedValue = "0.00"
                        binding.txtCharges3Value.setText(otherChrgs3UpdatedValue)
                        binding.txtCharges3Value.setSelection(otherChrgs3UpdatedValue.length)
                        binding.txtCharges3Label.setText(othercharge3Model.label)
                        binding.txtCharges3Per.setText(othercharge3Model.unit_name)
                        Log.v("othercharge3Model", "$othercharge3Model")
                        Log.d("Update_List","$addOtherChargeList")

                        // from charge 4 to charge 3 and charge 4 -> 0
                        othercharge3Model = othercharge4Model
                        otherChrgs3UpdatedValue = otherChrgs4UpdatedValue
                        otherChrgs3CalculatedUpdatedValue = otherChrgs4CalculatedUpdatedValue
                        binding.txtCharges3Value.setText(othercharge3Model.amount)
                        binding.txtCharges3Value.setSelection(othercharge3Model.amount.length)
                        selectedPerIdforOtherCharges3 = othercharge3Model.unit_id
                        selectedPerNameForOtherCharges3 = othercharge3Model.unit_name
                        binding.txtCharges3Label.setText(othercharge3Model.label)
                        binding.txtCharges3Per.setText(othercharge3Model.unit_name)
                        Log.v("othercharge3Model", "$othercharge3Model")
                        Log.d("Update_List","$addOtherChargeList")

                     /*   othercharge4Model.amount = "0.00"
                        othercharge4Model.label = ""
                        othercharge4Model.unit_name = ""
                        othercharge4Model.unit_id = ""*/
                        otherChrgs4UpdatedValue = "0.00"
                        otherChrgs4CalculatedUpdatedValue = "0.00"
                        binding.txtCharges4Value.setText(otherChrgs4UpdatedValue)
                        binding.txtCharges4Value.setSelection(otherChrgs4UpdatedValue.length)
                        binding.txtCharges4Label.setText(othercharge4Model.label)
                        binding.txtCharges4Per.setText(othercharge4Model.unit_name)
                        Log.v("othercharge4Model", "$othercharge4Model")
                        Log.d("Update_List","$addOtherChargeList")

                        // from charge 5 to charge 4 and charge 5 -> 0
                        othercharge4Model = othercharge5Model
                        otherChrgs4UpdatedValue = otherChrgs5UpdatedValue
                        otherChrgs4CalculatedUpdatedValue = otherChrgs5CalculatedUpdatedValue
                        binding.txtCharges4Value.setText(othercharge4Model.amount)
                        binding.txtCharges4Value.setSelection(othercharge4Model.amount.length)
                        selectedPerIdforOtherCharges4 = othercharge4Model.unit_id
                        selectedPerNameForOtherCharges4 = othercharge4Model.unit_name
                        binding.txtCharges4Label.setText(othercharge4Model.label)
                        binding.txtCharges4Per.setText(othercharge4Model.unit_name)
                        Log.v("othercharge4Model", "$othercharge4Model")
                        Log.d("Update_List","$addOtherChargeList")

                       /* othercharge5Model.amount = "0.00"
                        othercharge5Model.label = ""
                        othercharge5Model.unit_name = ""
                        othercharge5Model.unit_id = ""*/
                        otherChrgs5UpdatedValue = "0.00"
                        otherChrgs5CalculatedUpdatedValue = "0.00"
                        binding.txtCharges5Value.setText(otherChrgs5UpdatedValue)
                        binding.txtCharges5Value.setSelection(otherChrgs5UpdatedValue.length)
                        binding.txtCharges5Label.setText(othercharge5Model.label)
                        binding.txtCharges5Per.setText(othercharge5Model.unit_name)
                        Log.v("othercharge5Model", "$othercharge5Model")
                        Log.d("Update_List","$addOtherChargeList")

                    }
                }
            }
            // other 3 imgclose click
            3 -> {
                when (count) {
                    // 0/1 will never the case as click from 3 imgclose
                    2 -> {
                        othercharge3Model.amount = "0.00"
                        othercharge3Model.label = ""
                        othercharge3Model.unit_name = ""
                        othercharge3Model.unit_id = ""
                        otherChrgs3UpdatedValue = "0.00"
                        otherChrgs3CalculatedUpdatedValue = "0.00"
                        binding.txtCharges3Value.setText(otherChrgs3UpdatedValue)
                        binding.txtCharges3Value.setSelection(otherChrgs3UpdatedValue.length)
                        binding.txtCharges3Label.setText(othercharge3Model.label)
                        binding.txtCharges3Per.setText(othercharge3Model.unit_name)
                        Log.v("othercharge3Model", "$othercharge3Model")
                        Log.d("Update_List","$addOtherChargeList")

                    }
                    3 -> {
                        // from charge 4 to charge 3 and charge 4 -> 0
                        othercharge3Model = othercharge4Model
                        otherChrgs3UpdatedValue = otherChrgs4UpdatedValue
                        otherChrgs3CalculatedUpdatedValue = otherChrgs4CalculatedUpdatedValue
                        binding.txtCharges3Value.setText(othercharge3Model.amount)
                        binding.txtCharges3Value.setSelection(othercharge3Model.amount.length)
                        selectedPerIdforOtherCharges3 = othercharge3Model.unit_id
                        selectedPerNameForOtherCharges3 = othercharge3Model.unit_name
                        binding.txtCharges3Label.setText(othercharge3Model.label)
                        binding.txtCharges3Per.setText(othercharge3Model.unit_name)
                        Log.v("othercharge3Model", "$othercharge3Model")
                        Log.d("Update_List","$addOtherChargeList")

                       /* othercharge4Model.amount = "0.00"
                        othercharge4Model.label = ""
                        othercharge4Model.unit_name = ""
                        othercharge4Model.unit_id = ""*/
                        otherChrgs4UpdatedValue = "0.00"
                        otherChrgs4CalculatedUpdatedValue = "0.00"
                        binding.txtCharges4Value.setText(otherChrgs4UpdatedValue)
                        binding.txtCharges4Value.setSelection(otherChrgs4UpdatedValue.length)
                        binding.txtCharges4Label.setText(othercharge4Model.label)
                        binding.txtCharges4Per.setText(othercharge4Model.unit_name)
                        Log.v("othercharge4Model", "$othercharge4Model")
                        Log.d("Update_List","$addOtherChargeList")

                    }
                    4 -> {
                        // from charge 4 to charge 3 and charge 4 -> 0
                        othercharge3Model = othercharge4Model
                        otherChrgs3UpdatedValue = otherChrgs4UpdatedValue
                        otherChrgs3CalculatedUpdatedValue = otherChrgs4CalculatedUpdatedValue
                        binding.txtCharges3Value.setText(othercharge3Model.amount)
                        binding.txtCharges3Value.setSelection(othercharge3Model.amount.length)
                        selectedPerIdforOtherCharges3 = othercharge3Model.unit_id
                        selectedPerNameForOtherCharges3 = othercharge3Model.unit_name
                        binding.txtCharges3Label.setText(othercharge3Model.label)
                        binding.txtCharges3Per.setText(othercharge3Model.unit_name)
                        Log.v("othercharge3Model", "$othercharge3Model")
                        Log.d("Update_List","$addOtherChargeList")

                      /*  othercharge4Model.amount = "0.00"
                        othercharge4Model.label = ""
                        othercharge4Model.unit_name = ""
                        othercharge4Model.unit_id = ""*/
                        otherChrgs4UpdatedValue = "0.00"
                        otherChrgs4CalculatedUpdatedValue = "0.00"
                        binding.txtCharges4Value.setText(otherChrgs4UpdatedValue)
                        binding.txtCharges4Value.setSelection(otherChrgs4UpdatedValue.length)
                        binding.txtCharges4Label.setText(othercharge4Model.label)
                        binding.txtCharges4Per.setText(othercharge4Model.unit_name)
                        Log.v("othercharge4Model", "$othercharge4Model")
                        Log.d("Update_List","$addOtherChargeList")

                        // from charge 5 to charge 4 and charge 5 -> 0
                        othercharge4Model = othercharge5Model
                        otherChrgs4UpdatedValue = otherChrgs5UpdatedValue
                        otherChrgs4CalculatedUpdatedValue = otherChrgs5CalculatedUpdatedValue
                        binding.txtCharges4Value.setText(othercharge4Model.amount)
                        binding.txtCharges4Value.setSelection(othercharge4Model.amount.length)
                        selectedPerIdforOtherCharges4 = othercharge4Model.unit_id
                        selectedPerNameForOtherCharges4 = othercharge4Model.unit_name
                        binding.txtCharges4Label.setText(othercharge4Model.label)
                        binding.txtCharges4Per.setText(othercharge4Model.unit_name)
                        Log.v("othercharge4Model", "$othercharge4Model")
                        Log.d("Update_List","$addOtherChargeList")

                       /* othercharge5Model.amount = "0.00"
                        othercharge5Model.label = ""
                        othercharge5Model.unit_name = ""
                        othercharge5Model.unit_id = ""*/
                        otherChrgs5UpdatedValue = "0.00"
                        otherChrgs5CalculatedUpdatedValue = "0.00"
                        binding.txtCharges5Value.setText(otherChrgs5UpdatedValue)
                        binding.txtCharges5Value.setSelection(otherChrgs5UpdatedValue.length)
                        binding.txtCharges5Label.setText(othercharge5Model.label)
                        binding.txtCharges5Per.setText(othercharge5Model.unit_name)
                        Log.v("othercharge5Model", "$othercharge5Model")
                        Log.d("Update_List","$addOtherChargeList")

                    }
                }
            }
            // other 4 imgclose click
            4 -> {
                // 0/1/2 will never the case as click from 4 imgclose
                when (count) {
                    // 0/1 will never the case as click from 3 imgclose
                    3 -> {
                        othercharge4Model.amount = "0.00"
                        othercharge4Model.label = ""
                        othercharge4Model.unit_name = ""
                        othercharge4Model.unit_id = ""
                        otherChrgs4UpdatedValue = "0.00"
                        otherChrgs4CalculatedUpdatedValue = "0.00"
                        binding.txtCharges4Value.setText(otherChrgs4UpdatedValue)
                        binding.txtCharges4Value.setSelection(otherChrgs4UpdatedValue.length)
                        binding.txtCharges4Label.setText(othercharge4Model.label)
                        binding.txtCharges4Per.setText(othercharge4Model.unit_name)
                        Log.v("othercharge4Model", "$othercharge4Model")
                        Log.d("Update_List","$addOtherChargeList")

                    }

                    4 -> {
                        // from charge 5 to charge 4 and charge 5 -> 0
                        othercharge4Model = othercharge5Model
                        otherChrgs4UpdatedValue = otherChrgs5UpdatedValue
                        otherChrgs4CalculatedUpdatedValue = otherChrgs5CalculatedUpdatedValue
                        binding.txtCharges4Value.setText(othercharge4Model.amount)
                        binding.txtCharges4Value.setSelection(othercharge4Model.amount.length)
                        selectedPerIdforOtherCharges4 = othercharge4Model.unit_id
                        selectedPerNameForOtherCharges4 = othercharge4Model.unit_name
                        binding.txtCharges4Label.setText(othercharge4Model.label)
                        binding.txtCharges4Per.setText(othercharge4Model.unit_name)
                        Log.v("othercharge4Model", "$othercharge4Model")
                        Log.d("Update_List","$addOtherChargeList")

                       /* othercharge5Model.amount = "0.00"
                        othercharge5Model.label = ""
                        othercharge5Model.unit_name = ""
                        othercharge5Model.unit_id = ""*/
                        otherChrgs5UpdatedValue = "0.00"
                        otherChrgs5CalculatedUpdatedValue = "0.00"
                        binding.txtCharges5Value.setText(otherChrgs5UpdatedValue)
                        binding.txtCharges5Value.setSelection(otherChrgs5UpdatedValue.length)
                        binding.txtCharges5Label.setText(othercharge5Model.label)
                        binding.txtCharges5Per.setText(othercharge5Model.unit_name)
                        Log.v("othercharge5Model", "$othercharge5Model")
                        Log.d("Update_List","$addOtherChargeList")

                    }
                }
            }
            // other 5 imgclose click
            5 -> {
                // 0/1/2/3 will never the case as click from 5 imgclose
                when (count) {
                    4 -> {
                        othercharge5Model.amount = "0.00"
                        othercharge5Model.label = ""
                        othercharge5Model.unit_name = ""
                        othercharge5Model.unit_id = ""
                        otherChrgs5UpdatedValue = "0.00"
                        otherChrgs5CalculatedUpdatedValue = "0.00"
                        binding.txtCharges5Value.setText(otherChrgs5UpdatedValue)
                        binding.txtCharges5Value.setSelection(otherChrgs5UpdatedValue.length)
                        binding.txtCharges5Label.setText(othercharge5Model.label)
                        binding.txtCharges5Per.setText(othercharge5Model.unit_name)
                        Log.v("othercharge5Model", "5) $othercharge5Model")
                        Log.d("Update_List","$addOtherChargeList")

                    }
                }
            }
        }
    }


    private fun tempSaveOtherCharge5Data() {
        othercharge5Model =
            OpeningStockItemModel.OpeningStockItemModelItem.ItemChargesBreakup.ChargesArray(
                binding.txtCharges5Value.text.toString(),
                binding.txtCharges5Label.text.toString(),
                selectedPerIdforOtherCharges5,
                selectedPerNameForOtherCharges5
            )
        Log.v("othercharge5Model", "$othercharge5Model")
    }

    private fun tempSaveOtherCharge4Data() {
        othercharge4Model =
            OpeningStockItemModel.OpeningStockItemModelItem.ItemChargesBreakup.ChargesArray(
                binding.txtCharges4Value.text.toString(),
                binding.txtCharges4Label.text.toString(),
                selectedPerIdforOtherCharges4,
                selectedPerNameForOtherCharges4
            )
        Log.v("othercharge4Model", "$othercharge4Model")
    }

    private fun tempSaveOtherCharge3Data() {
        othercharge3Model =
            OpeningStockItemModel.OpeningStockItemModelItem.ItemChargesBreakup.ChargesArray(
                binding.txtCharges3Value.text.toString(),
                binding.txtCharges3Label.text.toString(),
                selectedPerIdforOtherCharges3,
                selectedPerNameForOtherCharges3
            )
        Log.v("othercharge3Model", "$othercharge3Model")
    }

    private fun tempSaveOtherCharge2Data() {
        othercharge2Model =
            OpeningStockItemModel.OpeningStockItemModelItem.ItemChargesBreakup.ChargesArray(
                binding.txtCharges2Value.text.toString(),
                binding.txtCharges2Label.text.toString(),
                selectedPerIdforOtherCharges2,
                selectedPerNameForOtherCharges2
            )
        Log.v("othercharge2Model", "$othercharge2Model")
    }

    private fun tempSaveOtherCharge1Data() {
        othercharge1Model =
            OpeningStockItemModel.OpeningStockItemModelItem.ItemChargesBreakup.ChargesArray(
                binding.txtCharges1Value.text.toString(),
                binding.txtCharges1Label.text.toString(),
                selectedPerIdforOtherCharges1,
                selectedPerNameForOtherCharges1
            )
        Log.v("othercharge1Model", "$othercharge1Model")
    }

    private fun saveOtherChargeModelAccordingtoNoofCharges() {
        Log.d("Count","$count")
        when (count) {
            0 -> {
                // only making visible
                saveMakingChargesModel()
            }
            1 -> {
                // other 1 visible
                // Naman Code
                if(otherChrgs1UpdatedValue!="0.00"&&otherChrgs1UpdatedValue.isNotEmpty()) {
                    saveOtherChargesModel(
                        0,
                        otherChrgs1UpdatedValue,
                        binding.txtCharges1Label.text.toString().trim(),
                        selectedPerIdforOtherCharges1,
                        selectedPerNameForOtherCharges1,
                        0,
                        false
                    )
                }else{
                    saveOtherChargesModel(
                        10,
                        "",
                        "",
                        "",
                        "",
                        0,
                        true
                    )
                }
/*
               //End
                when (addOtherChargeList.size >= 1) {

                    true -> {
                        if(otherChrgs1UpdatedValue!="0.00"&&otherChrgs1UpdatedValue.isNotEmpty()) {
                            saveOtherChargesModel(
                                0,
                                otherChrgs1UpdatedValue,
                                binding.txtCharges1Label.text.toString().trim(),
                                selectedPerIdforOtherCharges1,
                                selectedPerNameForOtherCharges1,
                                0,
                                false
                            )
                        }else{
                            saveOtherChargesModel(
                                10,
                                "",
                                "",
                               "",
                                "",
                                0,
                                true
                            )
                        }
                    }
                    false -> {
                        if(otherChrgs1UpdatedValue!="0.00"&&otherChrgs1UpdatedValue.isNotEmpty()) {
                            saveOtherChargesModel(
                                0,
                                otherChrgs1UpdatedValue,
                                binding.txtCharges1Label.text.toString().trim(),
                                selectedPerIdforOtherCharges1,
                                selectedPerNameForOtherCharges1,
                                0,
                                false
                            )
                        }else{
                            saveOtherChargesModel(
                                -1,
                                otherChrgs1UpdatedValue,
                                binding.txtCharges1Label.text.toString().trim(),
                                selectedPerIdforOtherCharges1,
                                selectedPerNameForOtherCharges1,
                                0,
                                true
                            )
                        }
                    }
                }
*/

            }
            2 -> {
                // other1 n 2 visible

                // Naman Code
                saveOtherChargesModel(
                    0,
                    otherChrgs1UpdatedValue,
                    binding.txtCharges1Label.text.toString().trim(),
                    selectedPerIdforOtherCharges1,
                    selectedPerNameForOtherCharges1,
                    0,
                    false
                )
                if(otherChrgs2UpdatedValue!="0.00"&&otherChrgs2UpdatedValue.isNotEmpty()) {
                    saveOtherChargesModel(
                        1,
                        otherChrgs2UpdatedValue,
                        binding.txtCharges2Label.text.toString().trim(),
                        selectedPerIdforOtherCharges2,
                        selectedPerNameForOtherCharges2,
                        1,
                        false
                    )
                }else{
                    saveOtherChargesModel(
                        10,
                        otherChrgs2UpdatedValue,
                        binding.txtCharges2Label.text.toString().trim(),
                        selectedPerIdforOtherCharges2,
                        selectedPerNameForOtherCharges2,
                        1,
                        true
                    )
                }

                //End

                /*when (addOtherChargeList.size >= 2) {
                    true -> {
                        saveOtherChargesModel(
                            0,
                            otherChrgs1UpdatedValue,
                            binding.txtCharges1Label.text.toString().trim(),
                            selectedPerIdforOtherCharges1,
                            selectedPerNameForOtherCharges1,
                            0,
                            false
                        )
                    }
                    false -> {
                        saveOtherChargesModel(
                            0,
                            otherChrgs1UpdatedValue,
                            binding.txtCharges1Label.text.toString().trim(),
                            selectedPerIdforOtherCharges1,
                            selectedPerNameForOtherCharges1,
                            0,
                            false
                        )
                    }
                }
                when (addOtherChargeList.size >= 2) {
                    true -> {
                        if(otherChrgs2UpdatedValue!="0.00"&&otherChrgs2UpdatedValue.isNotEmpty()) {
                            saveOtherChargesModel(
                                1,
                                otherChrgs2UpdatedValue,
                                binding.txtCharges2Label.text.toString().trim(),
                                selectedPerIdforOtherCharges2,
                                selectedPerNameForOtherCharges2,
                                1,
                                false
                            )
                        }else{
                            saveOtherChargesModel(
                                10,
                                otherChrgs2UpdatedValue,
                                binding.txtCharges2Label.text.toString().trim(),
                                selectedPerIdforOtherCharges2,
                                selectedPerNameForOtherCharges2,
                                1,
                                true
                            )
                        }
                    }
                    false -> {
                        if(otherChrgs2UpdatedValue!="0.00"&&otherChrgs2UpdatedValue.isNotEmpty()) {
                            saveOtherChargesModel(
                                -1,
                                otherChrgs2UpdatedValue,
                                binding.txtCharges2Label.text.toString().trim(),
                                selectedPerIdforOtherCharges2,
                                selectedPerNameForOtherCharges2,
                                1,
                                false
                            )
                        }else{
                            saveOtherChargesModel(
                                10,
                                otherChrgs2UpdatedValue,
                                binding.txtCharges2Label.text.toString().trim(),
                                selectedPerIdforOtherCharges2,
                                selectedPerNameForOtherCharges2,
                                1,
                                true
                            )
                        }
                    }
                }

*/
            }
            3 -> {
                //Naman Code

                saveOtherChargesModel(
                    0,
                    otherChrgs1UpdatedValue,
                    binding.txtCharges1Label.text.toString().trim(),
                    selectedPerIdforOtherCharges1,
                    selectedPerNameForOtherCharges1,
                    0,
                    false
                )
                saveOtherChargesModel(
                    1,
                    otherChrgs2UpdatedValue,
                    binding.txtCharges2Label.text.toString().trim(),
                    selectedPerIdforOtherCharges2,
                    selectedPerNameForOtherCharges2,
                    0,
                    false
                )
                if(otherChrgs3UpdatedValue!="0.00"&&otherChrgs3UpdatedValue.isNotEmpty()) {
                    saveOtherChargesModel(
                        2,
                        otherChrgs3UpdatedValue,
                        binding.txtCharges3Label.text.toString().trim(),
                        selectedPerIdforOtherCharges3,
                        selectedPerNameForOtherCharges3,
                        2,
                        false
                    )
                }else{
                    saveOtherChargesModel(
                        10,
                        otherChrgs3UpdatedValue,
                        binding.txtCharges3Label.text.toString().trim(),
                        selectedPerIdforOtherCharges3,
                        selectedPerNameForOtherCharges3,
                        2,
                        true
                    )
                }
                //End Code
                /*when (addOtherChargeList.size >= 3) {
                    true -> {
                        // other1 n 2 n 3 visible
                        saveOtherChargesModel(
                            0,
                            otherChrgs1UpdatedValue,
                            binding.txtCharges1Label.text.toString().trim(),
                            selectedPerIdforOtherCharges1,
                            selectedPerNameForOtherCharges1,
                            0,
                            false
                        )

                    }
                    false -> {
                        // other1 n 2 n 3 visible
                        saveOtherChargesModel(
                            0,
                            otherChrgs1UpdatedValue,
                            binding.txtCharges1Label.text.toString().trim(),
                            selectedPerIdforOtherCharges1,
                            selectedPerNameForOtherCharges1,
                            0,
                            false
                        )

                    }
                }
                when (addOtherChargeList.size >= 3) {
                    true -> {
                        saveOtherChargesModel(
                            1,
                            otherChrgs2UpdatedValue,
                            binding.txtCharges2Label.text.toString().trim(),
                            selectedPerIdforOtherCharges2,
                            selectedPerNameForOtherCharges2,
                            0,
                            false
                        )
                    }
                    false -> {
                        saveOtherChargesModel(
                            -1,
                            otherChrgs2UpdatedValue,
                            binding.txtCharges2Label.text.toString().trim(),
                            selectedPerIdforOtherCharges2,
                            selectedPerNameForOtherCharges2,
                            0,
                            false
                        )
                    }
                }
                when (addOtherChargeList.size >= 3) {
                    true -> {
                        if(otherChrgs3UpdatedValue!="0.00"&&otherChrgs3UpdatedValue.isNotEmpty()) {
                            saveOtherChargesModel(
                                2,
                                otherChrgs3UpdatedValue,
                                binding.txtCharges3Label.text.toString().trim(),
                                selectedPerIdforOtherCharges3,
                                selectedPerNameForOtherCharges3,
                                2,
                                false
                            )
                        }else{
                            saveOtherChargesModel(
                                10,
                                otherChrgs3UpdatedValue,
                                binding.txtCharges3Label.text.toString().trim(),
                                selectedPerIdforOtherCharges3,
                                selectedPerNameForOtherCharges3,
                                2,
                                true
                            )
                        }
                    }
                    false -> {
                        if(otherChrgs3UpdatedValue!="0.00"&&otherChrgs3UpdatedValue.isNotEmpty()) {
                            saveOtherChargesModel(
                                -1,
                                otherChrgs3UpdatedValue,
                                binding.txtCharges3Label.text.toString().trim(),
                                selectedPerIdforOtherCharges3,
                                selectedPerNameForOtherCharges3,
                                2,
                                false
                            )
                        }else{
                            saveOtherChargesModel(
                                10,
                                otherChrgs3UpdatedValue,
                                binding.txtCharges3Label.text.toString().trim(),
                                selectedPerIdforOtherCharges3,
                                selectedPerNameForOtherCharges3,
                                2,
                                true
                            )
                        }
                    }
                }*/
            }
            4 -> {
                // other1 n 2 n 3 n 4 visible

            //Naman Code
                saveOtherChargesModel(
                    0,
                    otherChrgs1UpdatedValue,
                    binding.txtCharges1Label.text.toString().trim(),
                    selectedPerIdforOtherCharges1,
                    selectedPerNameForOtherCharges1,
                    0,
                    false
                )
                saveOtherChargesModel(
                    1,
                    otherChrgs2UpdatedValue,
                    binding.txtCharges2Label.text.toString().trim(),
                    selectedPerIdforOtherCharges2,
                    selectedPerNameForOtherCharges2,
                    0,
                    false
                )
                saveOtherChargesModel(
                    2,
                    otherChrgs3UpdatedValue,
                    binding.txtCharges3Label.text.toString().trim(),
                    selectedPerIdforOtherCharges3,
                    selectedPerNameForOtherCharges3,
                    0,
                    false
                )
                if(otherChrgs4UpdatedValue!="0.00"&&otherChrgs4UpdatedValue.isNotEmpty()) {
                    saveOtherChargesModel(
                        3,
                        otherChrgs4UpdatedValue,
                        binding.txtCharges4Label.text.toString().trim(),
                        selectedPerIdforOtherCharges4,
                        selectedPerNameForOtherCharges4,
                        3,
                        false
                    )
                }else{
                    saveOtherChargesModel(
                        10,
                        otherChrgs4UpdatedValue,
                        binding.txtCharges4Label.text.toString().trim(),
                        selectedPerIdforOtherCharges4,
                        selectedPerNameForOtherCharges4,
                        3,
                        true
                    )
                }

            //End Code

/*
                when (addOtherChargeList.size >= 4) {
                    true -> {
                        saveOtherChargesModel(
                            0,
                            otherChrgs1UpdatedValue,
                            binding.txtCharges1Label.text.toString().trim(),
                            selectedPerIdforOtherCharges1,
                            selectedPerNameForOtherCharges1,
                            0,
                            false
                        )
                    }
                    false -> {
                        saveOtherChargesModel(
                            0,
                            otherChrgs1UpdatedValue,
                            binding.txtCharges1Label.text.toString().trim(),
                            selectedPerIdforOtherCharges1,
                            selectedPerNameForOtherCharges1,
                            0,
                            false
                        )
                    }
                }
                when (addOtherChargeList.size >= 4) {
                    true -> {
                        saveOtherChargesModel(
                            1,
                            otherChrgs2UpdatedValue,
                            binding.txtCharges2Label.text.toString().trim(),
                            selectedPerIdforOtherCharges2,
                            selectedPerNameForOtherCharges2,
                            0,
                            false
                        )
                    }
                    false -> {
                        saveOtherChargesModel(
                            -1,
                            otherChrgs2UpdatedValue,
                            binding.txtCharges2Label.text.toString().trim(),
                            selectedPerIdforOtherCharges2,
                            selectedPerNameForOtherCharges2,
                            0,
                            false
                        )
                    }
                }
                when (addOtherChargeList.size >= 4) {
                    true -> {
                        saveOtherChargesModel(
                            2,
                            otherChrgs3UpdatedValue,
                            binding.txtCharges3Label.text.toString().trim(),
                            selectedPerIdforOtherCharges3,
                            selectedPerNameForOtherCharges3,
                            0,
                            false
                        )
                    }
                    false -> {
                        saveOtherChargesModel(
                            -1,
                            otherChrgs3UpdatedValue,
                            binding.txtCharges3Label.text.toString().trim(),
                            selectedPerIdforOtherCharges3,
                            selectedPerNameForOtherCharges3,
                            0,
                            false
                        )
                    }
                }
                when (addOtherChargeList.size >= 4) {
                    true -> {
                        if(otherChrgs4UpdatedValue!="0.00"&&otherChrgs4UpdatedValue.isNotEmpty()) {
                            saveOtherChargesModel(
                                3,
                                otherChrgs4UpdatedValue,
                                binding.txtCharges4Label.text.toString().trim(),
                                selectedPerIdforOtherCharges4,
                                selectedPerNameForOtherCharges4,
                                3,
                                false
                            )
                        }else{
                            saveOtherChargesModel(
                                10,
                                otherChrgs4UpdatedValue,
                                binding.txtCharges4Label.text.toString().trim(),
                                selectedPerIdforOtherCharges4,
                                selectedPerNameForOtherCharges4,
                                3,
                                true
                            )
                        }
                    }
                    false -> {
                        if(otherChrgs4UpdatedValue!="0.00"&&otherChrgs4UpdatedValue.isNotEmpty()) {
                            saveOtherChargesModel(
                                -1,
                                otherChrgs4UpdatedValue,
                                binding.txtCharges4Label.text.toString().trim(),
                                selectedPerIdforOtherCharges4,
                                selectedPerNameForOtherCharges4,
                                3,
                                false
                            )
                        }else{
                            saveOtherChargesModel(
                                10,
                                otherChrgs4UpdatedValue,
                                binding.txtCharges4Label.text.toString().trim(),
                                selectedPerIdforOtherCharges4,
                                selectedPerNameForOtherCharges4,
                                3,
                                true
                            )
                        }
                    }
                }
*/


            }
            5 -> {
                // other1 n 2 n 3 n 4 n 5 all visible

                //Naman Code
                saveOtherChargesModel(
                    0,
                    otherChrgs1UpdatedValue,
                    binding.txtCharges1Label.text.toString().trim(),
                    selectedPerIdforOtherCharges1,
                    selectedPerNameForOtherCharges1,
                    0,
                    false
                )
                saveOtherChargesModel(
                    1,
                    otherChrgs2UpdatedValue,
                    binding.txtCharges2Label.text.toString().trim(),
                    selectedPerIdforOtherCharges2,
                    selectedPerNameForOtherCharges2,
                    0,
                    false
                )
                saveOtherChargesModel(
                    2,
                    otherChrgs3UpdatedValue,
                    binding.txtCharges3Label.text.toString().trim(),
                    selectedPerIdforOtherCharges3,
                    selectedPerNameForOtherCharges3,
                    0,
                    false
                )
                saveOtherChargesModel(
                    3,
                    otherChrgs4UpdatedValue,
                    binding.txtCharges4Label.text.toString().trim(),
                    selectedPerIdforOtherCharges4,
                    selectedPerNameForOtherCharges4,
                    0,
                    false
                )
                if(otherChrgs5UpdatedValue!="0.00"&&otherChrgs5UpdatedValue.isNotEmpty()) {
                    Log.d("Check_Path","True True Path")
                    saveOtherChargesModel(
                        4,
                        otherChrgs5UpdatedValue,
                        binding.txtCharges5Label.text.toString().trim(),
                        selectedPerIdforOtherCharges5,
                        selectedPerNameForOtherCharges5,
                        4,
                        false
                    )
                }else{
                    saveOtherChargesModel(
                        10,
                        otherChrgs5UpdatedValue,
                        binding.txtCharges5Label.text.toString().trim(),
                        selectedPerIdforOtherCharges5,
                        selectedPerNameForOtherCharges5,
                        4,
                        true
                    )
                }
                //End Code
                /*when (addOtherChargeList.size == 5) {
                    true -> {
                        saveOtherChargesModel(
                            0,
                            otherChrgs1UpdatedValue,
                            binding.txtCharges1Label.text.toString().trim(),
                            selectedPerIdforOtherCharges1,
                            selectedPerNameForOtherCharges1,
                            0,
                            false
                        )
                    }
                    false -> {
                        saveOtherChargesModel(
                            0,
                            otherChrgs1UpdatedValue,
                            binding.txtCharges1Label.text.toString().trim(),
                            selectedPerIdforOtherCharges1,
                            selectedPerNameForOtherCharges1,
                            0,
                            false
                        )
                    }
                }
                when (addOtherChargeList.size == 5) {
                    true -> {
                        saveOtherChargesModel(
                            1,
                            otherChrgs2UpdatedValue,
                            binding.txtCharges2Label.text.toString().trim(),
                            selectedPerIdforOtherCharges2,
                            selectedPerNameForOtherCharges2,
                            0,
                            false
                        )
                    }
                    false -> {
                        saveOtherChargesModel(
                            -1,
                            otherChrgs2UpdatedValue,
                            binding.txtCharges2Label.text.toString().trim(),
                            selectedPerIdforOtherCharges2,
                            selectedPerNameForOtherCharges2,
                            0,
                            false
                        )
                    }
                }
                when (addOtherChargeList.size == 5) {
                    true -> {
                        saveOtherChargesModel(
                            2,
                            otherChrgs3UpdatedValue,
                            binding.txtCharges3Label.text.toString().trim(),
                            selectedPerIdforOtherCharges3,
                            selectedPerNameForOtherCharges3,
                            0,
                            false
                        )
                    }
                    false -> {
                        saveOtherChargesModel(
                            -1,
                            otherChrgs3UpdatedValue,
                            binding.txtCharges3Label.text.toString().trim(),
                            selectedPerIdforOtherCharges3,
                            selectedPerNameForOtherCharges3,
                            0,
                            false
                        )
                    }
                }
                when (addOtherChargeList.size == 5) {
                    true -> {
                        saveOtherChargesModel(
                            3,
                            otherChrgs4UpdatedValue,
                            binding.txtCharges4Label.text.toString().trim(),
                            selectedPerIdforOtherCharges4,
                            selectedPerNameForOtherCharges4,
                            0,
                            false
                        )
                    }
                    false -> {
                        saveOtherChargesModel(
                            -1,
                            otherChrgs4UpdatedValue,
                            binding.txtCharges4Label.text.toString().trim(),
                            selectedPerIdforOtherCharges4,
                            selectedPerNameForOtherCharges4,
                            0,
                            false
                        )
                    }
                }

                when (addOtherChargeList.size == 5) {
                    true -> {
                        if(otherChrgs5UpdatedValue!="0.00"&&otherChrgs5UpdatedValue.isNotEmpty()) {
                            Log.d("Check_Path","True True Path")
                            saveOtherChargesModel(
                                4,
                                otherChrgs5UpdatedValue,
                                binding.txtCharges5Label.text.toString().trim(),
                                selectedPerIdforOtherCharges5,
                                selectedPerNameForOtherCharges5,
                                4,
                                false
                            )
                        }else{
                            saveOtherChargesModel(
                                10,
                                otherChrgs5UpdatedValue,
                                binding.txtCharges5Label.text.toString().trim(),
                                selectedPerIdforOtherCharges5,
                                selectedPerNameForOtherCharges5,
                                4,
                                true
                            )

                        }
                    }
                    false -> {
                        if(otherChrgs5UpdatedValue!="0.00"&&otherChrgs5UpdatedValue.isNotEmpty()) {
                            Log.d("Check_Path","False True Path")

                            saveOtherChargesModel(
                                -1,
                                otherChrgs5UpdatedValue,
                                binding.txtCharges5Label.text.toString().trim(),
                                selectedPerIdforOtherCharges5,
                                selectedPerNameForOtherCharges5,
                                4,
                                false
                            )

                        }else{
                            saveOtherChargesModel(
                                10,
                                otherChrgs5UpdatedValue,
                                binding.txtCharges5Label.text.toString().trim(),
                                selectedPerIdforOtherCharges5,
                                selectedPerNameForOtherCharges5,
                                4,
                                true
                            )

                        }
                    }
                }
                */
            }
        }
    }

    private fun visibleGoneChargeAsPerCount(count: Int) {
        when (count) {
            0 -> {
                // only making visible
                binding.cardOtherCharges1.visibility = View.GONE
                binding.cardOtherCharges2.visibility = View.GONE
                binding.cardOtherCharges3.visibility = View.GONE
                binding.cardOtherCharges4.visibility = View.GONE
                binding.cardOtherCharges5.visibility = View.GONE
            }
            1 -> {
                // other1 visible
                binding.cardOtherCharges1.visibility = View.VISIBLE
                binding.cardOtherCharges2.visibility = View.GONE
                binding.cardOtherCharges3.visibility = View.GONE
                binding.cardOtherCharges4.visibility = View.GONE
                binding.cardOtherCharges5.visibility = View.GONE
            }
            2 -> {
                // other1 n 2 visible
                binding.cardOtherCharges1.visibility = View.VISIBLE
                binding.cardOtherCharges2.visibility = View.VISIBLE
                binding.cardOtherCharges3.visibility = View.GONE
                binding.cardOtherCharges4.visibility = View.GONE
                binding.cardOtherCharges5.visibility = View.GONE
            }
            3 -> {
                // other1 n 2 n 3 visible
                binding.cardOtherCharges1.visibility = View.VISIBLE
                binding.cardOtherCharges2.visibility = View.VISIBLE
                binding.cardOtherCharges3.visibility = View.VISIBLE
                binding.cardOtherCharges4.visibility = View.GONE
                binding.cardOtherCharges5.visibility = View.GONE
            }
            4 -> {
                binding.cardOtherCharges1.visibility = View.VISIBLE
                binding.cardOtherCharges2.visibility = View.VISIBLE
                binding.cardOtherCharges3.visibility = View.VISIBLE
                binding.cardOtherCharges4.visibility = View.VISIBLE
                binding.cardOtherCharges5.visibility = View.GONE
            }
            5 -> {
                binding.cardOtherCharges1.visibility = View.VISIBLE
                binding.cardOtherCharges2.visibility = View.VISIBLE
                binding.cardOtherCharges3.visibility = View.VISIBLE
                binding.cardOtherCharges4.visibility = View.VISIBLE
                binding.cardOtherCharges5.visibility = View.VISIBLE
            }
        }
        if (count == 5) {
            binding.tvAddChargeAddItem.visibility = View.GONE
        } else {
            binding.tvAddChargeAddItem.visibility = View.VISIBLE
        }
    }


    private fun onFocusChangeSetup() {
        binding.txtMakingChargesValue.setOnFocusChangeListener { v, hasFocus -> //if(!hasFocus)
            if (!hasFocus) {
                when (binding.txtMakingChargesValue.text.isNullOrBlank()) {
                    true -> {
                        makingChrgsUpdatedValue = "0.00"
                        binding.txtMakingChargesValue.setText(makingChrgsUpdatedValue)
                        binding.txtMakingChargesValue.setSelection(makingChrgsUpdatedValue.length)

                    }
                    else -> {
                        binding.txtMakingChargesValue.setText(makingChrgsUpdatedValue)
                        binding.txtMakingChargesValue.setSelection(makingChrgsUpdatedValue.length)
                        Log.v("making", " focus change dd call")
                        updateValuesasPerDDSelection(false, selectedPerIdForMakingCharges, 1, false)
                    }
                }
            }
        }
        binding.txtCharges1Value.setOnFocusChangeListener { v, hasFocus -> //if(!hasFocus)
            if (!hasFocus) {
                when (binding.txtCharges1Value.text.isNullOrBlank()) {
                    true -> {
                        otherChrgs1UpdatedValue = "0.00"
                        binding.txtCharges1Value.setText(otherChrgs1UpdatedValue)
                        binding.txtCharges1Value.setSelection(otherChrgs1UpdatedValue.length)

                    }
                    else -> {
                        when (otherChrgs1UpdatedValue) {
                            "0.00" -> {

                            }
                            else -> {
                                Log.v("txtCharges1Value", " focus change dd call")
                                binding.txtCharges1Value.setText(otherChrgs1UpdatedValue)
                                binding.txtCharges1Value.setSelection(otherChrgs1UpdatedValue.length)
                                updateValuesasPerDDSelection(
                                    true,
                                    selectedPerIdforOtherCharges1,
                                    2,
                                    false
                                )
                            }

                        }

                    }
                }
            }
        }
        binding.txtCharges2Value.setOnFocusChangeListener { v, hasFocus -> //if(!hasFocus)
            if (!hasFocus) {
                when (binding.txtCharges2Value.text.isNullOrBlank()) {
                    true -> {
                        otherChrgs2UpdatedValue = "0.00"
                        binding.txtCharges2Value.setText(otherChrgs2UpdatedValue)
                        binding.txtCharges2Value.setSelection(otherChrgs2UpdatedValue.length)

                    }
                    else -> {
                        when (otherChrgs2UpdatedValue) {
                            "0.00" -> {

                            }
                            else -> {
                                Log.v("txtCharges2Value", " focus change dd call")
                                binding.txtCharges2Value.setText(otherChrgs2UpdatedValue)
                                binding.txtCharges2Value.setSelection(otherChrgs2UpdatedValue.length)
                                updateValuesasPerDDSelection(
                                    true,
                                    selectedPerIdforOtherCharges2,
                                    3,
                                    false
                                )
                            }

                        }

                    }
                }
            }
        }
        binding.txtCharges3Value.setOnFocusChangeListener { v, hasFocus -> //if(!hasFocus)
            if (!hasFocus) {
                when (binding.txtCharges3Value.text.isNullOrBlank()) {
                    true -> {
                        otherChrgs3UpdatedValue = "0.00"
                        binding.txtCharges3Value.setText(otherChrgs3UpdatedValue)
                        binding.txtCharges3Value.setSelection(otherChrgs3UpdatedValue.length)

                    }
                    else -> {
                        Log.v("txtCharges3Value", " focus change dd call")
                        binding.txtCharges2Value.setText(otherChrgs2UpdatedValue)
                        binding.txtCharges2Value.setSelection(otherChrgs2UpdatedValue.length)
                        updateValuesasPerDDSelection(true, selectedPerIdforOtherCharges3, 4, false)
                    }
                }
            }
        }
        binding.txtCharges4Value.setOnFocusChangeListener { v, hasFocus -> //if(!hasFocus)
            if (!hasFocus) {
                when (binding.txtCharges4Value.text.isNullOrBlank()) {
                    true -> {
                        otherChrgs4UpdatedValue = "0.00"
                        binding.txtCharges4Value.setText(otherChrgs4UpdatedValue)
                        binding.txtCharges4Value.setSelection(otherChrgs4UpdatedValue.length)

                    }
                    else -> {
                        Log.v("txtCharges4Value", " focus change dd call")
                        binding.txtCharges2Value.setText(otherChrgs2UpdatedValue)
                        binding.txtCharges2Value.setSelection(otherChrgs2UpdatedValue.length)
                        updateValuesasPerDDSelection(true, selectedPerIdforOtherCharges4, 5, false)
                    }
                }
            }
        }
        binding.txtCharges5Value.setOnFocusChangeListener { v, hasFocus -> //if(!hasFocus)
            if (!hasFocus) {
                when (binding.txtCharges5Value.text.isNullOrBlank()) {
                    true -> {
                        otherChrgs5UpdatedValue = "0.00"
                        binding.txtCharges5Value.setText(otherChrgs5UpdatedValue)
                        binding.txtCharges5Value.setSelection(otherChrgs5UpdatedValue.length)

                    }
                    else -> {
                        Log.v("txtCharges5Value", " focus change dd call")
                        binding.txtCharges2Value.setText(otherChrgs2UpdatedValue)
                        binding.txtCharges2Value.setSelection(otherChrgs2UpdatedValue.length)
                        updateValuesasPerDDSelection(true, selectedPerIdforOtherCharges5, 6, false)
                    }
                }
            }
        }
        binding.txtCharges5Value.setOnFocusChangeListener { v, hasFocus -> //if(!hasFocus)
            if (!hasFocus) {
                when (binding.txtCharges5Value.text.isNullOrBlank()) {
                    true -> {
                        otherChrgs5UpdatedValue = "0.00"
                        binding.txtCharges5Value.setText(otherChrgs5UpdatedValue)
                        binding.txtCharges5Value.setSelection(otherChrgs5UpdatedValue.length)

                    }
                    else -> {
                        Log.v("txtCharges5Value", " focus change dd call")
                        binding.txtCharges2Value.setText(otherChrgs2UpdatedValue)
                        binding.txtCharges2Value.setSelection(otherChrgs2UpdatedValue.length)
                        updateValuesasPerDDSelection(true, selectedPerIdforOtherCharges5, 6, false)
                    }
                }
            }
        }
        binding.txtCharges1Label.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                tempSaveOtherCharge1Data()
            }
        }
        binding.txtCharges2Label.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                tempSaveOtherCharge2Data()
            }
        }
        binding.txtCharges3Label.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                tempSaveOtherCharge3Data()
            }
        }
        binding.txtCharges4Label.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                tempSaveOtherCharge4Data()
            }
        }
        binding.txtCharges5Label.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                tempSaveOtherCharge5Data()
            }
        }
    }

    private fun clearAllfocus() {
        binding.txtMakingChargesValue.clearFocus()
        binding.txtCharges1Value.clearFocus()
        binding.txtCharges1Label.clearFocus()
        binding.txtCharges2Value.clearFocus()
        binding.txtCharges2Label.clearFocus()
        binding.txtCharges3Value.clearFocus()
        binding.txtCharges3Label.clearFocus()
        binding.txtCharges4Value.clearFocus()
        binding.txtCharges4Label.clearFocus()
        binding.txtCharges5Value.clearFocus()
        binding.txtCharges5Label.clearFocus()
    }

    private fun ontextChangeSetup() {
        binding.txtMakingChargesValue.doAfterTextChanged {
            val str: String = binding.txtMakingChargesValue.text.toString()

            if (str.isEmpty()) return@doAfterTextChanged
            val str2: String = CommonUtils.perfectDecimal(str, 10, 2).toString()
            if (!str2.equals(str)) {

                binding.txtMakingChargesValue.setText(str2)
                binding.txtMakingChargesValue.setSelection(str2.length)
            }

            makingChrgsUpdatedValue = df.format(str2.toDouble())
        }
        binding.txtCharges1Value.doAfterTextChanged {
            val str: String = binding.txtCharges1Value.text.toString()

            if (str.isEmpty()) return@doAfterTextChanged
            val str2: String = CommonUtils.perfectDecimal(str, 10, 2).toString()
            if (!str2.equals(str)) {

                binding.txtCharges1Value.setText(str2)
                binding.txtCharges1Value.setSelection(str2.length)
            }

            otherChrgs1UpdatedValue = df.format(str2.toDouble())
            Log.v("tchangotch1", otherChrgs1UpdatedValue)
        }
        binding.txtCharges2Value.doAfterTextChanged {
            val str: String = binding.txtCharges2Value.text.toString()

            if (str.isEmpty()) return@doAfterTextChanged
            val str2: String = CommonUtils.perfectDecimal(str, 10, 2).toString()
            if (!str2.equals(str)) {

                binding.txtCharges2Value.setText(str2)
                binding.txtCharges2Value.setSelection(str2.length)
            }
            otherChrgs2UpdatedValue = df.format(str2.toDouble())
            Log.v("tchangotch2", otherChrgs2UpdatedValue)
        }
        binding.txtCharges3Value.doAfterTextChanged {
            val str: String = binding.txtCharges3Value.text.toString()

            if (str.isEmpty()) return@doAfterTextChanged
            val str2: String = CommonUtils.perfectDecimal(str, 10, 2).toString()
            if (!str2.equals(str)) {
                binding.txtCharges3Value.setText(str2)
                binding.txtCharges3Value.setSelection(str2.length)
            }
            otherChrgs3UpdatedValue = df.format(str2.toDouble())
            Log.v("tchangotch3", otherChrgs3UpdatedValue)
        }
        binding.txtCharges4Value.doAfterTextChanged {
            val str: String = binding.txtCharges4Value.text.toString()

            if (str.isEmpty()) return@doAfterTextChanged
            val str2: String = CommonUtils.perfectDecimal(str, 10, 2).toString()
            if (!str2.equals(str)) {
                binding.txtCharges4Value.setText(str2)
                binding.txtCharges4Value.setSelection(str2.length)
            }
            otherChrgs4UpdatedValue = df.format(str2.toDouble())
        }
        binding.txtCharges5Value.doAfterTextChanged {
            val str: String = binding.txtCharges5Value.text.toString()

            if (str.isEmpty()) return@doAfterTextChanged
            val str2: String = CommonUtils.perfectDecimal(str, 10, 2).toString()
            if (!str2.equals(str)) {
    
                binding.txtCharges5Value.setText(str2)
                binding.txtCharges5Value.setSelection(str2.length)
            }
            otherChrgs5UpdatedValue = df.format(str2.toDouble())
        }
    }

    private fun openPerForMakingnOtherCharges(
        txtChargesPer: AppCompatAutoCompleteTextView,
        clickTrack: Int
    ) {

        popupMenu = PopupMenu(this, txtChargesPer)
        for (i in 0 until this.unitNameList!!.size) {
            popupMenu.menu.add(
                Menu.NONE,
                i,
                i,
                this.unitNameList!!.get(i)
            ) //add(groupId, itemId, order, title);
        }

        popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
            txtChargesPer.setText(item.title)
            val selected: String = item.title.toString()
            val pos: Int? = this.unitNameList!!.indexOf(selected)


            when (clickTrack) {
                // 1 making charges
                1 -> {
                    selectedPerIdForMakingCharges =
                        pos?.let { it1 -> unitArrayList?.get(it1)?.id }.toString()
                    selectedPerNameForMakingCharges =
                        pos?.let { it1 -> unitArrayList?.get(it1)?.name }.toString()
                    updateValuesasPerDDSelection(false, selectedPerIdForMakingCharges, 1, false)
                }
                2 -> {
                    selectedPerIdforOtherCharges1 =
                        pos?.let { it1 -> unitArrayList?.get(it1)?.id }.toString()
                    Log.v("ot1id", selectedPerIdforOtherCharges1)
                    selectedPerNameForOtherCharges1 =
                        pos?.let { it1 -> unitArrayList?.get(it1)?.name }.toString()
                    updateValuesasPerDDSelection(true, selectedPerIdforOtherCharges1, 2, false)
                }
                3 -> {
                    selectedPerIdforOtherCharges2 =
                        pos?.let { it1 -> unitArrayList?.get(it1)?.id }.toString()
                    selectedPerNameForOtherCharges2 =
                        pos?.let { it1 -> unitArrayList?.get(it1)?.name }.toString()
                    updateValuesasPerDDSelection(true, selectedPerIdforOtherCharges2, 3, false)
                }
                4 -> {
                    selectedPerIdforOtherCharges3 =
                        pos?.let { it1 -> unitArrayList?.get(it1)?.id }.toString()
                    selectedPerNameForOtherCharges3 =
                        pos?.let { it1 -> unitArrayList?.get(it1)?.name }.toString()
                    updateValuesasPerDDSelection(true, selectedPerIdforOtherCharges3, 4, false)
                }
                5 -> {
                    selectedPerIdforOtherCharges4 =
                        pos?.let { it1 -> unitArrayList?.get(it1)?.id }.toString()
                    selectedPerNameForOtherCharges4 =
                        pos?.let { it1 -> unitArrayList?.get(it1)?.name }.toString()
                    updateValuesasPerDDSelection(true, selectedPerIdforOtherCharges4, 5, false)
                }
                6 -> {
                    selectedPerIdforOtherCharges5 =
                        pos?.let { it1 -> unitArrayList?.get(it1)?.id }.toString()
                    selectedPerNameForOtherCharges5 =
                        pos?.let { it1 -> unitArrayList?.get(it1)?.name }.toString()
                    updateValuesasPerDDSelection(true, selectedPerIdforOtherCharges5, 6, false)
                }
            }


            true
        })

        popupMenu.show()
    }


    private fun updateValuesasPerDDSelection(
        isFromOtherCharges: Boolean,
        selectedMakingorOtherID: String,
        clickFrom: Int,
        isFromImgClose: Boolean
    ) {
        when (isFromOtherCharges) {
            // update values from item amt (first drop down)
            false -> {
                when (selectedMakingorOtherID) {
                    "unit" -> {
                        Log.v("unit", selectedPerIdForMakingCharges)
                        val pcs: BigDecimal = piecesUpdatedValue.toBigDecimal()
                        when (piecesUpdatedValue.toBigDecimal() > BigDecimal.ZERO) {
                            true -> {
                                val makingcharge: BigDecimal =
                                    makingChrgsUpdatedValue.toBigDecimal()
                                totalmakingChrgsUpdatedValue =
                                    ((pcs.setScale(2)
                                        .multiply(makingcharge.setScale(2))
                                            )).setScale(2, RoundingMode.CEILING).toString()
                                //  setupTotalCharges(false)
                                Log.v("totalmaking", totalmakingChrgsUpdatedValue)
                            }
                            false -> {
                                totalmakingChrgsUpdatedValue = "0.00"
                            }
                        }


                    }
                    "maintain_stock_in" -> {
                        Log.v("msi", selectedPerIdForMakingCharges)
                        val netwt: BigDecimal = netweightUpdatedValue.toBigDecimal()
                        when (netweightUpdatedValue.toBigDecimal() > BigDecimal.ZERO) {
                            true -> {
                                val makingcharge: BigDecimal =
                                    makingChrgsUpdatedValue.toBigDecimal()
                                totalmakingChrgsUpdatedValue =
                                    ((netwt.setScale(3)
                                        .multiply(makingcharge.setScale(2))
                                            )).setScale(2, RoundingMode.CEILING).toString()
                                // setupTotalCharges(false)
                                Log.v("totalmaking", totalmakingChrgsUpdatedValue)
                            }
                            false -> {
                                totalmakingChrgsUpdatedValue = "0.00"
                            }
                        }

                    }
                    "fix" -> {
                        totalmakingChrgsUpdatedValue = makingChrgsUpdatedValue
                        //setupTotalCharges(false)
                    }
                }
                setupTotalCharges(false, count)
            }
            // update values from labour amt (second drop down)
            true -> {
                // other per DD
                when (selectedMakingorOtherID) {
                    "unit" -> {

                        val pcs: BigDecimal = piecesUpdatedValue.toBigDecimal()
                        when(piecesUpdatedValue.toBigDecimal() > BigDecimal.ZERO){
                            true->{
                                when (clickFrom) {
                                    // other1
                                    2 -> {
                                        Log.v("unit", selectedMakingorOtherID)
                                        val othercharge1: BigDecimal =
                                            otherChrgs1UpdatedValue.toBigDecimal()
                                        Log.v("otherChrgs1UpdatedV", otherChrgs1UpdatedValue)
                                        otherChrgs1CalculatedUpdatedValue =
                                            (othercharge1.setScale(2).multiply(pcs.setScale(2))
                                                .setScale(2, RoundingMode.CEILING).toString())
                                        Log.v("otcharge1", otherChrgs1CalculatedUpdatedValue)
                                        tempSaveOtherCharge1Data()
                                    }
                                    3 -> {
                                        Log.v("unit", selectedMakingorOtherID)
                                        val othercharge2: BigDecimal =
                                            otherChrgs2UpdatedValue.toBigDecimal()
                                        Log.v("otherChrgs2UpdatedV", otherChrgs2UpdatedValue)
                                        otherChrgs2CalculatedUpdatedValue =
                                            (othercharge2.setScale(2).multiply(pcs.setScale(2))
                                                .setScale(2, RoundingMode.CEILING).toString())
                                        Log.v("otcharge2", otherChrgs2CalculatedUpdatedValue)
                                        tempSaveOtherCharge2Data()
                                    }
                                    4 -> {
                                        Log.v("unit", selectedMakingorOtherID)
                                        val othercharge3: BigDecimal =
                                            otherChrgs3UpdatedValue.toBigDecimal()
                                        Log.v("otherChrgs3UpdatedV", otherChrgs3UpdatedValue)
                                        otherChrgs3CalculatedUpdatedValue =
                                            (othercharge3.setScale(2).multiply(pcs.setScale(2))
                                                .setScale(2, RoundingMode.CEILING).toString())
                                        Log.v("otcharge3", otherChrgs3CalculatedUpdatedValue)
                                        tempSaveOtherCharge3Data()
                                    }
                                    5 -> {
                                        Log.v("unit", selectedMakingorOtherID)
                                        val othercharge4: BigDecimal =
                                            otherChrgs4UpdatedValue.toBigDecimal()
                                        Log.v("otherChrgs4UpdatedV", otherChrgs4UpdatedValue)
                                        otherChrgs4CalculatedUpdatedValue =
                                            (othercharge4.setScale(2).multiply(pcs.setScale(2))
                                                .setScale(2, RoundingMode.CEILING).toString())
                                        Log.v("otcharge4", otherChrgs4CalculatedUpdatedValue)
                                        tempSaveOtherCharge4Data()
                                    }
                                    6 -> {
                                        Log.v("unit", selectedMakingorOtherID)
                                        val othercharge5: BigDecimal =
                                            otherChrgs5UpdatedValue.toBigDecimal()
                                        Log.v("otherChrgs5UpdatedV", otherChrgs5UpdatedValue)
                                        otherChrgs5CalculatedUpdatedValue =
                                            (othercharge5.setScale(2).multiply(pcs.setScale(2))
                                                .setScale(2, RoundingMode.CEILING).toString())
                                        Log.v("otcharge5", otherChrgs5CalculatedUpdatedValue)
                                        tempSaveOtherCharge5Data()
                                    }
                                }
                                calculationTotalOfOtherCharges(pcs)
                                setupTotalCharges(false, count)
                            }
                            else->{

                            }
                        }

                        /*when(isFromImgClose){
                            false->{
                                calculationTotalOfOtherCharges(pcs)
                                setupTotalCharges(false)
                            }
                        }*/

                    }
                    "maintain_stock_in" -> {
                        val netwt: BigDecimal = netweightUpdatedValue.toBigDecimal()
                        when(netweightUpdatedValue.toBigDecimal() > BigDecimal.ZERO){
                            true->{
                                when (clickFrom) {
                                    // other1
                                    2 -> {
                                        val othercharge1: BigDecimal =
                                            otherChrgs1UpdatedValue.toBigDecimal()
                                        otherChrgs1CalculatedUpdatedValue =
                                            (othercharge1.setScale(2).multiply(netwt.setScale(2))
                                                .setScale(2, RoundingMode.CEILING).toString())
                                        Log.v("msiotcharge1", otherChrgs1CalculatedUpdatedValue)
                                        tempSaveOtherCharge1Data()
                                    }
                                    3 -> {
                                        val othercharge2: BigDecimal =
                                            otherChrgs2UpdatedValue.toBigDecimal()
                                        otherChrgs2CalculatedUpdatedValue =
                                            (othercharge2.setScale(2).multiply(netwt.setScale(2))
                                                .setScale(2, RoundingMode.CEILING).toString())
                                        Log.v("msiotcharge2", otherChrgs2CalculatedUpdatedValue)
                                        tempSaveOtherCharge2Data()
                                    }
                                    4 -> {
                                        val othercharge3: BigDecimal =
                                            otherChrgs3UpdatedValue.toBigDecimal()
                                        otherChrgs3CalculatedUpdatedValue =
                                            (othercharge3.setScale(2).multiply(netwt.setScale(2))
                                                .setScale(2, RoundingMode.CEILING).toString())
                                        Log.v("msiotcharge3", otherChrgs3CalculatedUpdatedValue)
                                        tempSaveOtherCharge3Data()
                                    }
                                    5 -> {
                                        val othercharge4: BigDecimal =
                                            otherChrgs4UpdatedValue.toBigDecimal()
                                        otherChrgs4CalculatedUpdatedValue =
                                            (othercharge4.setScale(2).multiply(netwt.setScale(2))
                                                .setScale(2, RoundingMode.CEILING).toString())
                                        Log.v("msiotcharge4", otherChrgs4CalculatedUpdatedValue)
                                        tempSaveOtherCharge4Data()
                                    }
                                    6 -> {
                                        val othercharge5: BigDecimal =
                                            otherChrgs5UpdatedValue.toBigDecimal()
                                        otherChrgs5CalculatedUpdatedValue =
                                            (othercharge5.setScale(2).multiply(netwt.setScale(2))
                                                .setScale(2, RoundingMode.CEILING).toString())
                                        tempSaveOtherCharge5Data()
                                    }
                                }
                                calculationTotalOfOtherCharges(netwt)
                                setupTotalCharges(false, count)
                            }
                            else->{

                            }
                        }

                        /*when(isFromImgClose){
                            false->{
                                calculationTotalOfOtherCharges(netwt)
                                setupTotalCharges(false)
                            }
                        }*/


                        // Log.v("totalother",totalOtherChrgsUpdatedValue)
                    }
                    "fix" -> {
                        val netwt: BigDecimal = netweightUpdatedValue.toBigDecimal()
                        when (clickFrom) {
                            // other1
                            2 -> {
                                Log.v("fix", selectedPerIdforOtherCharges1)
                                otherChrgs1CalculatedUpdatedValue = otherChrgs1UpdatedValue
                                Log.v("fixotcharge1", otherChrgs1CalculatedUpdatedValue)
                                tempSaveOtherCharge1Data()
                            }
                            3 -> {
                                Log.v("fix", selectedPerIdforOtherCharges2)
                                otherChrgs2CalculatedUpdatedValue = otherChrgs2UpdatedValue
                                Log.v("fixotcharge2", otherChrgs2CalculatedUpdatedValue)
                                tempSaveOtherCharge2Data()
                            }
                            4 -> {
                                Log.v("fix", selectedPerIdforOtherCharges3)
                                otherChrgs3CalculatedUpdatedValue = otherChrgs3UpdatedValue
                                Log.v("fixotcharge3", otherChrgs3CalculatedUpdatedValue)
                                tempSaveOtherCharge3Data()
                            }
                            5 -> {
                                Log.v("fix", selectedPerIdforOtherCharges4)
                                otherChrgs4CalculatedUpdatedValue = otherChrgs4UpdatedValue
                                Log.v("fixotcharge4", otherChrgs4CalculatedUpdatedValue)
                                tempSaveOtherCharge4Data()
                            }
                            6 -> {
                                Log.v("fix", selectedPerIdforOtherCharges5)
                                otherChrgs5CalculatedUpdatedValue = otherChrgs5UpdatedValue
                                Log.v("fixotcharge5", otherChrgs5CalculatedUpdatedValue)
                                tempSaveOtherCharge5Data()
                            }
                        }
                        /*when(isFromImgClose){
                            false->{
                                calculationTotalOfOtherCharges(netwt)
                                setupTotalCharges(false)
                            }
                        }*/
                        calculationTotalOfOtherCharges(netwt)
                        setupTotalCharges(false, count)

                    }
                }
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

            when (isLessWtArrayLoadedFromPref) {
                false -> {
                    if (is_studded.equals("1", true)) {
                        getLessWtArrayFromPref()
                        isLessWtArrayLoadedFromPref = true
                    }
                }
                else->{

                }
            }
            getnSetMakingChrgsnOtherChargesDataFromPref()

        }

        if (!ConnectivityStateHolder.isConnected) {
            // Network is not available
            CommonUtils.showDialog(this, getString(R.string.please_check_internet_msg))

        }
    }

    private fun getnSetMakingChrgsnOtherChargesDataFromPref() {
        when (isOtherListLoadedFromPref) {
            false -> {
                if (prefs.contains(Constants.PREF_OTHER_CHARGES_KEY)) {
                    val collectionType = object :
                        TypeToken<java.util.ArrayList<OpeningStockItemModel.OpeningStockItemModelItem.ItemChargesBreakup.ChargesArray>>() {}.type
                    Log.d("Charges_List","$addOtherChargeList")

                    addOtherChargeList =
                        Gson().fromJson(prefs[Constants.PREF_OTHER_CHARGES_KEY, ""], collectionType)
                    count = addOtherChargeList.size
                    Log.d("Charges_List","Charge List :- $addOtherChargeList")
                    Log.v("Charges_List", "Count :- $count")
                    visibleGoneChargeAsPerCount(count)
                    fillOthercharges()
                    isOtherListLoadedFromPref = true
                }
            }
            else->{

            }
        }

        if (prefs.contains(Constants.PREF_MAKING_CHARGES_KEY)) {
            val collectionType = object :
                TypeToken<OpeningStockItemModel.OpeningStockItemModelItem.ItemChargesBreakup.MakingChargeArray>() {}.type
            addMakingChargeModel =
                Gson().fromJson(prefs[Constants.PREF_MAKING_CHARGES_KEY, ""], collectionType)

            fillMakingcharges()
        }
        if(prefs.contains(Constants.PREF_MAKING_CHARGES_BREAKUP_INFO_KEY)){

            val makingChargeBreakup =
                object :
                    TypeToken<OpeningStockItemModel.OpeningStockItemModelItem.ItemChargesBreakup>() {}.type
            makingChargeBreakupList = Gson().fromJson(
                prefs[Constants.PREF_MAKING_CHARGES_BREAKUP_INFO_KEY, ""],
                makingChargeBreakup
            )
            binding.txttotalChargesValue.setText(Constants.AMOUNT_RS_APPEND + makingChargeBreakupList.total_charges)
            totalOfAllChrgsUpdatedValue = makingChargeBreakupList.total_charges
        }
        //setupTotalCharges(false)
    }

    private fun fillMakingcharges() {
        binding.txtMakingChargesValue.setText(addMakingChargeModel.amount)
        binding.txtMakingChargesValue.requestFocus();
        binding.txtMakingChargesValue.selectAll();
        binding.txtMakingChargesValue.setSelectAllOnFocus(true);
        binding.txtMakingChargesPerAddCharge.setText(addMakingChargeModel.unit_name)
        selectedPerIdForMakingCharges = addMakingChargeModel.unit_id
        selectedPerNameForMakingCharges = addMakingChargeModel.unit_name

    }

    private fun fillOthercharges() {
        if (binding.cardOtherCharges1.visibility == View.VISIBLE) {
            binding.txtCharges1Value.setText(addOtherChargeList.get(0).amount)
            binding.txtCharges1Per.setText(addOtherChargeList.get(0).unit_name)
            binding.txtCharges1Label.setText(addOtherChargeList.get(0).label)
            selectedPerIdforOtherCharges1 = addOtherChargeList.get(0).unit_id
            selectedPerNameForOtherCharges1 = addOtherChargeList.get(0).unit_name
            updateValuesasPerDDSelection(true, selectedPerIdforOtherCharges1, 2, false)
        }
        if (binding.cardOtherCharges2.visibility == View.VISIBLE) {
            binding.txtCharges2Value.setText(addOtherChargeList.get(1).amount)
            binding.txtCharges2Per.setText(addOtherChargeList.get(1).unit_name)
            binding.txtCharges2Label.setText(addOtherChargeList.get(1).label)
            selectedPerIdforOtherCharges2 = addOtherChargeList.get(1).unit_id
            selectedPerNameForOtherCharges2 = addOtherChargeList.get(1).unit_name
            updateValuesasPerDDSelection(true, selectedPerIdforOtherCharges2, 3, false)
        }
        if (binding.cardOtherCharges3.visibility == View.VISIBLE) {
            binding.txtCharges3Value.setText(addOtherChargeList.get(2).amount)
            binding.txtCharges3Per.setText(addOtherChargeList.get(2).unit_name)
            binding.txtCharges3Label.setText(addOtherChargeList.get(2).label)
            selectedPerIdforOtherCharges3 = addOtherChargeList.get(2).unit_id
            selectedPerNameForOtherCharges3 = addOtherChargeList.get(2).unit_name
            updateValuesasPerDDSelection(true, selectedPerIdforOtherCharges3, 4, false)
        }
        if (binding.cardOtherCharges4.visibility == View.VISIBLE) {
            binding.txtCharges4Value.setText(addOtherChargeList.get(3).amount)
            binding.txtCharges4Per.setText(addOtherChargeList.get(3).unit_name)
            binding.txtCharges4Label.setText(addOtherChargeList.get(3).label)
            selectedPerIdforOtherCharges4 = addOtherChargeList.get(3).unit_id
            selectedPerNameForOtherCharges4 = addOtherChargeList.get(3).unit_name
            updateValuesasPerDDSelection(true, selectedPerIdforOtherCharges4, 5, false)
        }
        if (binding.cardOtherCharges5.visibility == View.VISIBLE) {
            binding.txtCharges5Value.setText(addOtherChargeList.get(4).amount)
            binding.txtCharges5Per.setText(addOtherChargeList.get(4).unit_name)
            binding.txtCharges5Label.setText(addOtherChargeList.get(4).label)
            selectedPerIdforOtherCharges5 = addOtherChargeList.get(4).unit_id
            selectedPerNameForOtherCharges5 = addOtherChargeList.get(4).unit_name
            updateValuesasPerDDSelection(true, selectedPerIdforOtherCharges5, 6, false)
        }

    }

    private fun getLessWtArrayFromPref() {

        if (prefs.contains(Constants.PREF_LESS_WEIGHT_INFO_KEY)) {
            val collectionType =
                object :
                    TypeToken<ArrayList<AddLessWeightModel.AddLessWeightModelItem>>() {}.type
            lessweightList =
                Gson().fromJson(prefs[Constants.PREF_LESS_WEIGHT_INFO_KEY, ""], collectionType)

            lesswtTotal = arrayListOf()
            listoflesswtChargesDetail = arrayListOf()

            for (data in lessweightList) {


                lesswtTotal.add(data.less_wt_total_amount)
                listoflesswtChargesDetail.add(
                    CalculationPaymentModel.DataPayment.ItemPayment.LessWeights(
                        data.less_wt_item_name,
                        data.less_wt_total_amount
                    )
                )

                if (lesswtTotal.size == 1) {
                    totalLwChargesUpdatedValue = data.less_wt_total_amount
                } else {
                    // 1 onwards
                    val tempLessWt1: BigDecimal = totalLwChargesUpdatedValue.toBigDecimal()
                    val tempLessWt2: BigDecimal = data.less_wt_total_amount.toBigDecimal()
                    totalLwChargesUpdatedValue =
                        ((tempLessWt1.setScale(2)
                            .plus(tempLessWt2.setScale(2))
                                )).setScale(2, RoundingMode.CEILING).toString()

                }
            }
            Log.v("totalLw", totalLwChargesUpdatedValue)

            setupTotalCharges(false, count)
            setupLesswtChargesAdapter()


        } else {
            binding.rvItemchargesLesswtcharges.visibility = View.GONE
        }

    }

    private fun calculationTotalOfOtherCharges(pcsOrnetwt: BigDecimal) {
        val othercharge1: BigDecimal = otherChrgs1CalculatedUpdatedValue.toBigDecimal()
        val othercharge2: BigDecimal = otherChrgs2CalculatedUpdatedValue.toBigDecimal()
        val othercharge3: BigDecimal = otherChrgs3CalculatedUpdatedValue.toBigDecimal()
        val othercharge4: BigDecimal = otherChrgs4CalculatedUpdatedValue.toBigDecimal()
        val othercharge5: BigDecimal = otherChrgs5CalculatedUpdatedValue.toBigDecimal()
        Log.v("totalotcrge1", otherChrgs1CalculatedUpdatedValue)
        Log.v("totalotcrge2", otherChrgs2CalculatedUpdatedValue)
        Log.v("totalotcrge3", otherChrgs3CalculatedUpdatedValue)
        Log.v("totalotcrge4", otherChrgs4CalculatedUpdatedValue)
        Log.v("totalotcrge5", otherChrgs5CalculatedUpdatedValue)

        //val netwt: BigDecimal = netweightUpdatedValue.toBigDecimal()

        totalOtherChrgsUpdatedValue =
            (othercharge1.setScale(2))
                .plus(
                    (othercharge2.setScale(2))
                        .plus((othercharge3.setScale(2)))
                        .plus((othercharge4.setScale(2)))
                        .plus((othercharge5.setScale(2)))
                ).setScale(2, RoundingMode.CEILING).toString()



        Log.v("totalother", totalOtherChrgsUpdatedValue)

    }


    private fun setupTotalCharges(isFromDeleteImgOther: Boolean, count: Int) {
        val totalLwCharges: BigDecimal = totalLwChargesUpdatedValue.toBigDecimal()
        val totalMakingCharges: BigDecimal = totalmakingChrgsUpdatedValue.toBigDecimal()
        val totalOtherCharges: BigDecimal = totalOtherChrgsUpdatedValue.toBigDecimal()
        Log.v("totalLwCharges", totalLwCharges.toString())
        Log.v("totalMakingCharges", totalMakingCharges.toString())
        Log.v("totalOtherCharges", totalOtherCharges.toString())

        when (this.count > 0) {
            true -> {
                totalOfAllChrgsUpdatedValue =
                    ((totalLwCharges.setScale(2)
                        .plus(totalMakingCharges.setScale(2).plus(totalOtherCharges.setScale(2)))
                            )).setScale(2, RoundingMode.CEILING).toString()
            }
            false -> {
                totalOfAllChrgsUpdatedValue =
                    ((totalLwCharges.setScale(2)
                        .plus(totalMakingCharges.setScale(2))
                            )).setScale(2, RoundingMode.CEILING).toString()
            }
        }
        binding.txttotalChargesValue.setText(Constants.AMOUNT_RS_APPEND + totalOfAllChrgsUpdatedValue)
        val totalofAllchrges: BigDecimal = totalOfAllChrgsUpdatedValue.toBigDecimal()

        when (isFromDeleteImgOther) {
            true -> {
                when (count) {
                    0 -> {
                        totalOfAllChrgsUpdatedValue =
                            ((totalofAllchrges.setScale(2)
                                .minus(totalOtherCharges)
                                    )).setScale(2, RoundingMode.CEILING).toString()
                        //makeallOtherCalcvalueZero()
                        binding.txttotalChargesValue.setText(Constants.AMOUNT_RS_APPEND + totalOfAllChrgsUpdatedValue)
                    }
                    1 -> {
                        val othercharge1: BigDecimal =
                            otherChrgs1CalculatedUpdatedValue.toBigDecimal()
                        totalOfAllChrgsUpdatedValue =
                            ((totalofAllchrges.setScale(2)
                                .minus(othercharge1)
                                    )).setScale(2, RoundingMode.CEILING).toString()
                        Log.v("othercharge1", othercharge1.toString())
                        Log.v("totalAllChrgsUV", totalOfAllChrgsUpdatedValue)
                        binding.txttotalChargesValue.setText(Constants.AMOUNT_RS_APPEND + totalOfAllChrgsUpdatedValue)
                        // otherChargesListRemovenUpdatedPref(count-1)
                    }
                    2 -> {
                        val othercharge2: BigDecimal =
                            otherChrgs2CalculatedUpdatedValue.toBigDecimal()
                        Log.v("totalAllChrgsUVbf", totalOfAllChrgsUpdatedValue)
                        totalOfAllChrgsUpdatedValue =
                            ((totalofAllchrges.setScale(2)
                                .minus(othercharge2)
                                    )).setScale(2, RoundingMode.CEILING).toString()
                        Log.v("othercharge2", othercharge2.toString())
                        Log.v("totalAllChrgsUV", totalOfAllChrgsUpdatedValue)
                        binding.txttotalChargesValue.setText(Constants.AMOUNT_RS_APPEND + totalOfAllChrgsUpdatedValue)
                        //otherChargesListRemovenUpdatedPref(count-1)
                    }
                    3 -> {
                        val othercharge3: BigDecimal =
                            otherChrgs3CalculatedUpdatedValue.toBigDecimal()
                        totalOfAllChrgsUpdatedValue =
                            ((totalofAllchrges.setScale(2)
                                .minus(othercharge3)
                                    )).setScale(2, RoundingMode.CEILING).toString()
                        // otherChargesListRemovenUpdatedPref(count-1)
                        binding.txttotalChargesValue.setText(Constants.AMOUNT_RS_APPEND + totalOfAllChrgsUpdatedValue)
                    }
                    4 -> {
                        val othercharge4: BigDecimal =
                            otherChrgs4CalculatedUpdatedValue.toBigDecimal()
                        totalOfAllChrgsUpdatedValue =
                            ((totalofAllchrges.setScale(2)
                                .minus(othercharge4)
                                    )).setScale(2, RoundingMode.CEILING).toString()
                        //otherChargesListRemovenUpdatedPref(count-1)
                        binding.txttotalChargesValue.setText(Constants.AMOUNT_RS_APPEND + totalOfAllChrgsUpdatedValue)
                    }
                    5 -> {
                        val othercharge5: BigDecimal =
                            otherChrgs5CalculatedUpdatedValue.toBigDecimal()
                        totalOfAllChrgsUpdatedValue =
                            ((totalofAllchrges.setScale(2)
                                .minus(othercharge5)
                                    )).setScale(2, RoundingMode.CEILING).toString()
                        // otherChargesListRemovenUpdatedPref(count-1)
                        binding.txttotalChargesValue.setText(Constants.AMOUNT_RS_APPEND + totalOfAllChrgsUpdatedValue)
                    }
                }
            }
            else->{

            }
        }


    }

    private fun setupLesswtChargesAdapter() {
        binding.rvItemchargesLesswtcharges.visibility = View.VISIBLE
        binding.rvItemchargesLesswtcharges.layoutManager = LinearLayoutManager(this)
        adapter = LessWeightChargesTotalAdapter(listoflesswtChargesDetail, false)
        binding.rvItemchargesLesswtcharges.setHasFixedSize(true)
        binding.rvItemchargesLesswtcharges.adapter = adapter
    }

    private fun setupViewModel() {
        viewModel =
            ViewModelProvider(this, ViewModelFactory(ApiHelper(RetrofitBuilder.apiService))).get(
                AddItemViewModel::class.java
            )
        binding.setLifecycleOwner(this)
    }

    private fun saveOtherChargesModel(
        new_othercharge_pos: Int,
        amount: String,
        label: String,
        unit_id: String,
        unit_name: String,
        pos : Int,
        remove : Boolean
    ) {
        /* if (prefs.contains(Constants.PREF_OTHER_CHARGES_KEY)) {
             val collectionType = object :
                 TypeToken<java.util.ArrayList<OpeningStockItemModel.OpeningStockItemModelItem.ItemChargesBreakup.ChargesArray>>() {}.type
             val otherChargesList: ArrayList<OpeningStockItemModel.OpeningStockItemModelItem.ItemChargesBreakup.ChargesArray> =
                 Gson().fromJson(prefs[Constants.PREF_OTHER_CHARGES_KEY, ""], collectionType)
             addOtherChargeList.addAll(otherChargesList)
         } else {
             addOtherChargeList = ArrayList()
         }*/

        Log.d("Value_Save_Button"," 1) $new_othercharge_pos  2) $amount  3) $label 4) $unit_id 5) $unit_name")

        val childModel =
            OpeningStockItemModel.OpeningStockItemModelItem.ItemChargesBreakup.ChargesArray(
                amount, label, unit_id, unit_name
            )
        Log.d("Update_List","Before :- $addOtherChargeList")
        
        //Naman Code
        if(new_othercharge_pos>=0 && !remove){
            addOtherChargeList.set(new_othercharge_pos,childModel)
            Log.d("Update_List ","2)$addOtherChargeList & Child model:- $childModel")

        }else{
            addOtherChargeList.removeAt(pos)
        }
        //End Code

/*
        if (new_othercharge_pos >= 0 && new_othercharge_pos != -1 && remove == false) {
            // Update selected item
            addOtherChargeList.set(new_othercharge_pos, childModel)
            Log.d("Update_List ","2)$addOtherChargeList & Child model:- $childModel")
        }else if(remove && new_othercharge_pos == 10){
            addOtherChargeList.removeAt(pos)
            Log.d("Update_List ","Remove)$addOtherChargeList & Child model:- $childModel")

        } else {
            // Add new item
            addOtherChargeList.add(childModel)
            Log.d("Update_List","2 else)$addOtherChargeList")
        }
*/
//        prefs[Constants.PREF_OTHER_CHARGES_KEY] = Gson().toJson(addOtherChargeList)
/*
        val modifiedList = removeTrailingZeros(addOtherChargeList)
        prefs[Constants.PREF_OTHER_CHARGES_KEY] = Gson().toJson(addOtherChargeList)

        Log.d("Charges_Final_List","$addOtherChargeList")

        Log.d("PREF_OTHER_CHARGES_KEY", "$prefs[Constants.PREF_OTHER_CHARGES_KEY]")*/
    }

    private fun saveOtherChargeInPref(){

        val modifiedList = removeTrailingZeros(addOtherChargeList)
        prefs[Constants.PREF_OTHER_CHARGES_KEY] = Gson().toJson(modifiedList)

        Log.d("Charges_Final_List","$modifiedList")

    }
    private fun removeTrailingZeros(list: ArrayList<OpeningStockItemModel.OpeningStockItemModelItem.ItemChargesBreakup.ChargesArray>): ArrayList<OpeningStockItemModel.OpeningStockItemModelItem.ItemChargesBreakup.ChargesArray> {
        var lastIndex = list.size - 1

        // Iterate from the end of the list and remove elements with amount equal to zero until a non-zero amount is encountered
        while (lastIndex >= 0 && (list[lastIndex].amount == "0.00" || list[lastIndex].amount.isEmpty() )) {
            list.removeAt(lastIndex)
            lastIndex--
        }

        return list
    }

    private fun otherChargesListRemovenUpdatedPref(otherchargePos: Int) {
        if (addOtherChargeList.size > 0) {
            if (otherchargePos >= addOtherChargeList.size) {

                //index not exists
            } else {
                // index exists
                addOtherChargeList.removeAt(otherchargePos)
                prefs[Constants.PREF_OTHER_CHARGES_KEY] = Gson().toJson(addOtherChargeList)
                binding.txttotalChargesValue.setText(Constants.AMOUNT_RS_APPEND + totalOfAllChrgsUpdatedValue)
            }
        }

    }

    private fun saveMakingChargesModel() {
        /*if (prefs.contains(Constants.PREF_MAKING_CHARGES_KEY)) {
            val collectionType = object :
                TypeToken<java.util.ArrayList<OpeningStockItemModel.OpeningStockItemModelItem.ItemChargesBreakup.MakingChargeArray>>() {}.type
            var makingChargesList: ArrayList<OpeningStockItemModel.OpeningStockItemModelItem.ItemChargesBreakup.MakingChargeArray> =
                Gson().fromJson(prefs[Constants.PREF_MAKING_CHARGES_KEY, ""], collectionType)
            addMakingChargeList.addAll(makingChargesList)
        } else {
            addMakingChargeList = ArrayList()
        }*/


        addMakingChargeModel =
            OpeningStockItemModel.OpeningStockItemModelItem.ItemChargesBreakup.MakingChargeArray(
                makingChrgsUpdatedValue,
                selectedPerIdForMakingCharges,
                selectedPerNameForMakingCharges
            )

        /*if (new_makingcharge_pos >= 0 && new_makingcharge_pos != -1) {
            // Update selected item
            addMakingChargeList.set(new_makingcharge_pos, childModel)
        } else {
            // Add new item
            addMakingChargeList.add(childModel)
        }
*/

        prefs[Constants.PREF_MAKING_CHARGES_KEY] = Gson().toJson(addMakingChargeModel)

    }

    private fun saveItemChargesBreakupModel() {
        /* if (prefs.contains(Constants.PREF_MAKING_CHARGES_BREAKUP_INFO_KEY)) {
             val collectionType = object :
                 TypeToken<java.util.ArrayList<OpeningStockItemModel.OpeningStockItemModelItem.ItemChargesBreakup>>() {}.type
             var makingChargesBraekupList: ArrayList<OpeningStockItemModel.OpeningStockItemModelItem.ItemChargesBreakup> =
                 Gson().fromJson(prefs[Constants.PREF_MAKING_CHARGES_BREAKUP_INFO_KEY, ""], collectionType)
             addMakingChargeBreakupList.addAll(makingChargesBraekupList)
         } else {
             addMakingChargeBreakupList = ArrayList()
         }*/


        val childModel = OpeningStockItemModel.OpeningStockItemModelItem.ItemChargesBreakup(
            addOtherChargeList,
            /*"",*/ addMakingChargeModel, totalOfAllChrgsUpdatedValue
        )


        /*if (new_makingchargeBreakup_pos >= 0 && new_makingchargeBreakup_pos != -1) {
            // Update selected item
            addMakingChargeBreakupList.set(new_makingchargeBreakup_pos, childModel)
        } else {
            // Add new item
            addMakingChargeBreakupList.add(childModel)
        }
*/
        prefs[Constants.PREF_MAKING_CHARGES_BREAKUP_INFO_KEY] = Gson().toJson(childModel) //setter

    }


}
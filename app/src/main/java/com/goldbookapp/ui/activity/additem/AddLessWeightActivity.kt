package com.goldbookapp.ui.activity.additem

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.InputFilter
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.blankj.utilcode.util.NetworkUtils
import com.goldbookapp.R
import com.goldbookapp.api.ApiHelper
import com.goldbookapp.api.RetrofitBuilder
import com.goldbookapp.databinding.AddLessWeightActivityNewBinding
import com.goldbookapp.model.AddLessWeightModel
import com.goldbookapp.model.ItemSearchModel
import com.goldbookapp.model.LoginModel
import com.goldbookapp.model.OpeningStockItemModel
import com.goldbookapp.ui.activity.viewmodel.AddItemViewModel
import com.goldbookapp.ui.activity.viewmodel.ViewModelFactory
import com.goldbookapp.ui.adapter.ItemDetailsAdapter
import com.goldbookapp.utils.*
import com.goldbookapp.utils.CommonUtils.Companion.clickWithDebounce
import com.goldbookapp.utils.PreferenceHelper.get
import com.goldbookapp.utils.PreferenceHelper.set
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hamzasharuf.networkmonitor.ConnectivityStateHolder
import com.hamzasharuf.networkmonitor.Event
import com.hamzasharuf.networkmonitor.NetworkEvents
import kotlinx.android.synthetic.main.add_less_weight_activity_new.*
import kotlinx.android.synthetic.main.toolbar_layout.*
import kotlinx.android.synthetic.main.toolbar_layout.view.*
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat

class AddLessWeightActivity : AppCompatActivity() {

    private var selectedPerItemId: String = ""
    private var selectedPerLabourId: String = ""
    private lateinit var viewModel: AddItemViewModel
    lateinit var binding: AddLessWeightActivityNewBinding

    lateinit var popupMenu: PopupMenu

    lateinit var prefs: SharedPreferences
    lateinit var loginModel: LoginModel
    var addlessweightList = ArrayList<AddLessWeightModel.AddLessWeightModelItem>()
    var lessweightBreakupList =
        ArrayList<OpeningStockItemModel.OpeningStockItemModelItem.ItemWtBreakup>()
    var lessweightSaveAdd: String? = "0"
    lateinit var lessweightEditModel: AddLessWeightModel.AddLessWeightModelItem
    lateinit var lessweightModel: AddLessWeightModel.AddLessWeightModelItem
    private var edit_lessweight_pos: Int = -1
    private var new_lessweight_pos: Int = -1
    private var new_lesswtBreakup_pos: Int = -1
    var itemList: ArrayList<ItemSearchModel.ItemSearch>? = null
    var itemNameList: List<String>? = null
    lateinit var ItemDetailsAdapter: ItemDetailsAdapter
    var selectedItemID: String = ""
    var selectedItemName: String = ""
    var maintainStock_name: String = ""
    var less_wt_unit_name: String = ""

    val df = DecimalFormat("0.00")
    val df1 = DecimalFormat("0.000")

    var piecesUpdatedValue: String = "0"
    var weightUpdatedValue: String = "0.000"
    var lesswtUpdatedValue: String = "0.000"
    var variationUpdateValue: String = "0.00"
    var finalweightUpdatedValue: String = "0.000"

    var itemRateUpdatedValue: String = "0.00"
    var lbrRateUpdatedValue: String = "0.00"
    var itemAmtUpdatedValue: String = "0.00"
    var labourAmtUpdatedValue: String = "0.00"
    var totalAmtUpdatedValue: String = "0.00"
    var totalLwofAllUpdatedValue: String = "0.000"
    var totalLwAmtofAllUpdatedValue: String = "0.00"

    lateinit var unitArrayList: List<ItemSearchModel.ItemSearch.Unit_array>
    var unitNameList: List<String>? = null
    var selectedPerNameForItem: String = ""


    var selectedPerNameForLabour: String = ""
    var selectedPerIdForLabour: String = ""

    var calculatedLw: String = "0.00"
    var calculatedFinalLw: String = "0.00"
    private var selectedItemUnit: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.add_less_weight_activity_new)

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
        binding.root.tvTitle.setText(R.string.less_weight)
        binding.rlAddLesswtRoot.setOnFocusChangeListener(View.OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                CommonUtils.hideKeyboardnew(this);
            }
        })

        applyingDigitFilter()
        ontextChangeSetup()
        onFocusChangeSetup()


        if (intent.extras != null && intent.extras!!.containsKey(Constants.LESS_WEIGHT_SAVE_TYPE)) {
            lessweightSaveAdd = intent.getStringExtra(Constants.LESS_WEIGHT_SAVE_TYPE)

            when (lessweightSaveAdd) {
                "1" -> {
                    /* if (intent.extras != null && intent.extras!!.containsKey(Constants.CHEQUE_SAVE_FROM_ADD_EDIT)) {
                         chequeSaveAddEdit =
                             intent.getStringExtra(Constants.CHEQUE_SAVE_FROM_ADD_EDIT)
                         when (chequeSaveAddEdit) {
                             //from edit on click of save and add
                             "1" -> {
                                 tvTitle.setText(R.string.cheque_book)
                                 chequeSaveAdd = "2"
                             }
                             //from add on click of save and add
                             "2" -> {
                                 tvTitle.setText(R.string.cheque_book)
                                 chequeSaveAdd = "3"
                             }
                         }
                     }*/
                }
                //2->Edit lessweight From Edit
                "2" -> {
                    tvTitle.setText(R.string.edit_less_weight)
                    if (intent.extras != null && intent.extras!!.containsKey(Constants.PREF_LESS_WEIGHT_INFO_KEY)) {
                        edit_lessweight_pos = intent.getIntExtra(Constants.PREF_LESS_WEIGHT_POS, 0)
                        val cheque_str: String? =
                            intent.getStringExtra(Constants.PREF_LESS_WEIGHT_INFO_KEY)
                        lessweightModel = Gson().fromJson(
                            cheque_str,
                            AddLessWeightModel.AddLessWeightModelItem::class.java
                        )

                        selectedItemUnit = lessweightModel.less_wt_maintain_stock_in_name
                        maintainStock_name = lessweightModel.less_wt_maintain_stock_in_name
                        selectedItemID = lessweightModel.less_wt_item_id
                        selectedItemName = lessweightModel.less_wt_item_name
                        selectedPerItemId = lessweightModel.less_wt_item_per
                        selectedPerNameForItem = lessweightModel.less_wt_item_per_name
                        selectedPerIdForLabour = lessweightModel.less_wt_lbr_per
                        selectedPerNameForLabour = lessweightModel.less_wt_lbr_per_name
                        binding.txtItemNameAddLessWeight.setText(lessweightModel.less_wt_item_name)
                        binding.txtWeightAddlessswt.setText(lessweightModel.less_wt_weight)
                        weightUpdatedValue = lessweightModel.less_wt_weight

                        binding.txtItemUnitAddlesswt.setText(lessweightModel.less_wt_maintain_stock_in_name)
                        selectedItemUnit = lessweightModel.less_wt_maintain_stock_in_name
                        binding.txtItemPiecesAddlessswt.setText(lessweightModel.less_wt_pieces)
                        piecesUpdatedValue = lessweightModel.less_wt_pieces
                         binding.txtLesswtAddlesswt.setText(lessweightModel.less_wt_less_wt)
                        lesswtUpdatedValue  = lessweightModel.less_wt_less_wt
                        binding.txtVariationAddlesswt.setText(lessweightModel.less_wt_variation)
                        variationUpdateValue = lessweightModel.less_wt_variation
                         binding.txtFinalwtAddlesswt.setText(lessweightModel.less_wt_final_wt)
                        finalweightUpdatedValue = lessweightModel.less_wt_final_wt
                        binding.txtItemRateAddlessswt.setText(lessweightModel.less_wt_item_rate)
                        itemRateUpdatedValue = lessweightModel.less_wt_item_rate
                        binding.txtPerAddlesswt.setText(lessweightModel.less_wt_item_per_name)
                        selectedPerItemId = lessweightModel.less_wt_item_per
                        selectedPerNameForItem = lessweightModel.less_wt_item_per_name
                        binding.txtItemAmtAddlesswt.setText(lessweightModel.less_wt_item_amount)
                        itemAmtUpdatedValue = lessweightModel.less_wt_item_amount
                        binding.txtLabourRateAddlessswt.setText(lessweightModel.less_wt_lbr_rate)
                        lbrRateUpdatedValue = lessweightModel.less_wt_lbr_rate
                        binding.txtLabourPerAddlesswt.setText(lessweightModel.less_wt_lbr_per_name)
                        selectedPerLabourId = lessweightModel.less_wt_lbr_per
                        selectedPerNameForLabour = lessweightModel.less_wt_lbr_per_name
                        binding.txtLabourAmtAddlesswt.setText(lessweightModel.less_wt_lbr_amount)
                        labourAmtUpdatedValue = lessweightModel.less_wt_lbr_amount
                        binding.txtTotalAmountAddItem.setText(lessweightModel.less_wt_total_amount)
                        totalAmtUpdatedValue = lessweightModel.less_wt_total_amount
                        unitArrayList = lessweightModel.less_wt_unit_array!!
                        unitNameList = unitArrayList?.map { it.name }

                        when (selectedItemUnit) {
                            "Kilograms" -> {
                                calculatedLw = CommonUtils.kgTogms(lessweightModel.less_wt_less_wt)
                                calculatedFinalLw =
                                    CommonUtils.kgTogms(lessweightModel.less_wt_final_wt)

                                /*binding.txtLesswtAddlesswt.setText(calculatedLw)
                                lesswtUpdatedValue = calculatedLw
                                binding.txtFinalwtAddlesswt.setText(calculatedFinalLw)
                                finalweightUpdatedValue = calculatedFinalLw*/
                            }
                            "Carat" -> {
                                calculatedLw =
                                    CommonUtils.carrotTogm(lessweightModel.less_wt_less_wt)
                                calculatedFinalLw =
                                    CommonUtils.carrotTogm(lessweightModel.less_wt_final_wt)
                               /* binding.txtLesswtAddlesswt.setText(calculatedLw)
                                lesswtUpdatedValue = calculatedLw
                                binding.txtFinalwtAddlesswt.setText(calculatedFinalLw)
                                finalweightUpdatedValue = calculatedFinalLw*/
                            }
                            "Grams" -> {
                                calculatedLw = lessweightModel.less_wt_less_wt
                                calculatedFinalLw = lessweightModel.less_wt_final_wt
                                /*binding.txtLesswtAddlesswt.setText(calculatedLw)
                                lesswtUpdatedValue = calculatedLw
                                binding.txtFinalwtAddlesswt.setText(calculatedFinalLw)
                                finalweightUpdatedValue = calculatedFinalLw*/
                            }
                        }

                    }
                }
                //Edit LessWeight From New
                "3" -> {
                    tvTitle.setText(R.string.edit_less_weight)
                    if (intent.extras != null && intent.extras!!.containsKey(Constants.PREF_LESS_WEIGHT_INFO_KEY)) {
                        new_lessweight_pos = intent.getIntExtra(Constants.PREF_LESS_WEIGHT_POS, 0)
                        val cheque_str: String? =
                            intent.getStringExtra(Constants.PREF_LESS_WEIGHT_INFO_KEY)
                        lessweightModel = Gson().fromJson(
                            cheque_str,
                            AddLessWeightModel.AddLessWeightModelItem::class.java
                        )


                        selectedItemUnit = lessweightModel.less_wt_maintain_stock_in_name
                        maintainStock_name = lessweightModel.less_wt_maintain_stock_in_name
                        selectedItemID = lessweightModel.less_wt_item_id
                        selectedItemName = lessweightModel.less_wt_item_name
                        selectedPerItemId = lessweightModel.less_wt_item_per
                        selectedPerNameForItem = lessweightModel.less_wt_item_per_name
                        selectedPerIdForLabour = lessweightModel.less_wt_lbr_per
                        selectedPerNameForLabour = lessweightModel.less_wt_lbr_per_name
                        binding.txtItemNameAddLessWeight.setText(lessweightModel.less_wt_item_name)
                        binding.txtWeightAddlessswt.setText(lessweightModel.less_wt_weight)
                        weightUpdatedValue = lessweightModel.less_wt_weight
                        lesswtUpdatedValue = lessweightModel.less_wt_less_wt
                        binding.txtItemUnitAddlesswt.setText(lessweightModel.less_wt_maintain_stock_in_name)
                        selectedItemUnit = lessweightModel.less_wt_maintain_stock_in_name
                        binding.txtItemPiecesAddlessswt.setText(lessweightModel.less_wt_pieces)
                        piecesUpdatedValue = lessweightModel.less_wt_pieces.toString()
                          binding.txtLesswtAddlesswt.setText(lessweightModel.less_wt_less_wt)
                        lesswtUpdatedValue = lessweightModel.less_wt_less_wt
                        binding.txtVariationAddlesswt.setText(lessweightModel.less_wt_variation)
                        variationUpdateValue = lessweightModel.less_wt_variation
                         binding.txtFinalwtAddlesswt.setText(lessweightModel.less_wt_final_wt)
                        finalweightUpdatedValue = lessweightModel.less_wt_final_wt
                        binding.txtItemRateAddlessswt.setText(lessweightModel.less_wt_item_rate)
                        itemRateUpdatedValue = lessweightModel.less_wt_item_rate
                        binding.txtPerAddlesswt.setText(lessweightModel.less_wt_item_per_name)
                        selectedPerItemId = lessweightModel.less_wt_item_per
                        selectedPerNameForItem =lessweightModel.less_wt_item_per_name
                        binding.txtItemAmtAddlesswt.setText(lessweightModel.less_wt_item_amount)
                        itemAmtUpdatedValue = lessweightModel.less_wt_item_amount
                        binding.txtLabourRateAddlessswt.setText(lessweightModel.less_wt_lbr_rate)
                        lbrRateUpdatedValue = lessweightModel.less_wt_lbr_rate
                        binding.txtLabourPerAddlesswt.setText(lessweightModel.less_wt_lbr_per_name)
                        selectedPerLabourId = lessweightModel.less_wt_lbr_per
                        selectedPerNameForLabour = lessweightModel.less_wt_lbr_per_name
                        binding.txtLabourAmtAddlesswt.setText(lessweightModel.less_wt_lbr_amount)
                        labourAmtUpdatedValue = lessweightModel.less_wt_lbr_amount
                        binding.txtTotalAmountAddItem.setText(lessweightModel.less_wt_total_amount)
                        totalAmtUpdatedValue = lessweightModel.less_wt_total_amount
                        unitArrayList = lessweightModel.less_wt_unit_array!!
                        unitNameList = unitArrayList?.map { it.name }

                        // convertValuesInGrams()
                        when (selectedItemUnit) {
                            "Kilograms" -> {
                                calculatedLw = CommonUtils.kgTogms(lessweightModel.less_wt_less_wt)
                                calculatedFinalLw =
                                    CommonUtils.kgTogms(lessweightModel.less_wt_final_wt)
                               /* binding.txtLesswtAddlesswt.setText(calculatedLw)
                                lesswtUpdatedValue = calculatedLw
                                binding.txtFinalwtAddlesswt.setText(calculatedFinalLw)
                                finalweightUpdatedValue = calculatedFinalLw*/
                            }
                            "Carat" -> {
                                calculatedLw =
                                    CommonUtils.carrotTogm(lessweightModel.less_wt_less_wt)
                                calculatedFinalLw =
                                    CommonUtils.carrotTogm(lessweightModel.less_wt_final_wt)
                               /* binding.txtLesswtAddlesswt.setText(calculatedLw)
                                lesswtUpdatedValue = calculatedLw
                                binding.txtFinalwtAddlesswt.setText(calculatedFinalLw)
                                finalweightUpdatedValue = calculatedFinalLw*/
                            }
                            "Grams" -> {
                                calculatedLw = lessweightModel.less_wt_less_wt
                                calculatedFinalLw = lessweightModel.less_wt_final_wt
                                /*binding.txtLesswtAddlesswt.setText(calculatedLw)
                                lesswtUpdatedValue = calculatedLw
                                binding.txtFinalwtAddlesswt.setText(calculatedFinalLw)
                                finalweightUpdatedValue = calculatedFinalLw*/
                            }
                        }
                    }
                }
            }
        }


        /*binding.txtChargesAddItem.clickWithDebounce {
            startActivity(
                Intent(this, AddItemChargesActivity::class.java))
        }


        binding.txtLessWtAddItem.clickWithDebounce {
            startActivity(
                Intent(this, LessWeightDetailsActivity::class.java))
        }*/
        binding.txtPerAddlesswt.clickWithDebounce {

            if (unitArrayList!!.size > 0) {
                openItemperMenu(unitNameList)
            }
        }
        binding.txtLabourPerAddlesswt.clickWithDebounce {

            if (unitArrayList!!.size > 0) {
                openItemperMenu(unitNameList)
            }
        }
        binding.txtLabourPerAddlesswt.clickWithDebounce {
            if (unitArrayList!!.size > 0) {

                openLabourperMenu(unitNameList)
            }
        }

        binding.btnSaveAddAddLessWeight.clickWithDebounce {
            if (performValidation()) {
                saveLessWeightModel()
                saveItemWtBreakupModel()
                startActivity(
                    Intent(
                        this,
                        AddLessWeightActivity::class.java
                    )
                )
                finish()
            }

        }


        binding.btnSaveCloseAddLessWeight.clickWithDebounce {
            if (performValidation()) {
                saveLessWeightModel()
                saveItemWtBreakupModel()
                finish()
            }

        }

    }

    fun performValidation(): Boolean {
        if (binding.txtItemNameAddLessWeight.text.toString().isBlank()) {
            CommonUtils.showDialog(this, getString(R.string.select_item_name_msg))
            binding.txtItemNameAddLessWeight.requestFocus()
            return false
        } else if (selectedItemName.isNullOrBlank()) {
            CommonUtils.showDialog(this, getString(R.string.select_item_name_msg))
            binding.txtItemNameAddLessWeight.requestFocus()
            return false
        } else if (!selectedItemName!!.length.equals(binding.txtItemNameAddLessWeight.text.toString().length)) {
            CommonUtils.showDialog(this, getString(R.string.select_item_name_msg))
            binding.txtItemNameAddLessWeight.requestFocus()
            return false
        } else if(binding.txtItemPiecesAddlessswt.text.toString().toDouble() == 0.00){
            CommonUtils.showDialog(this, getString(R.string.enter_piece_msg))
            binding.txtItemPiecesAddlessswt.requestFocus()
            return false
        } else if (binding.txtWeightAddlessswt.text.toString().toDouble() <= 0.00) {
            CommonUtils.showDialog(this, getString(R.string.enter_weight_msg))
            binding.txtWeightAddlessswt.requestFocus()
            return false
        }  else if (binding.txtLesswtAddlesswt.text.toString().toDouble() <= 0.00) {
            CommonUtils.showDialog(this, getString(R.string.enter_lessweight_msg))
            binding.txtLesswtAddlesswt.requestFocus()
            return false
        } else if (binding.txtFinalwtAddlesswt.text.toString().isBlank()) {
            CommonUtils.showDialog(this, getString(R.string.enter_finalweight_msg))
            binding.txtFinalwtAddlesswt.requestFocus()
            return false
        } else if (binding.txtItemRateAddlessswt.text.toString().isBlank()) {
            CommonUtils.showDialog(this, getString(R.string.enter_itemrate_msg))
            binding.txtItemRateAddlessswt.requestFocus()
            return false
        }

        return true
    }

    private fun updateValuesasPerDDSelection(isFromLabour: Boolean) {
        when (isFromLabour) {
            // update values from item amt (first drop down)
            false -> {
                when (selectedPerItemId) {
                    "unit" -> {
                        Log.v("unit", selectedPerItemId)
                        val pcs: BigDecimal = piecesUpdatedValue.toBigDecimal()
                        val itemrate: BigDecimal =
                            itemRateUpdatedValue.toBigDecimal()
                        val itemAmt: String =
                            ((pcs.setScale(2)
                                .multiply(itemrate.setScale(2))
                                    )).setScale(2, RoundingMode.CEILING).toString()
                        itemAmtUpdatedValue = itemAmt
                        binding.txtItemAmtAddlesswt.setText(itemAmtUpdatedValue)
                        Log.v("itemrate", itemRateUpdatedValue)
                        Log.v("pcs", piecesUpdatedValue)
                        // set total amt
                        setTotalAmt()
                    }
                    "maintain_stock_in" -> {
                        Log.v("msi", selectedPerItemId)
                        val finalWt: BigDecimal = finalweightUpdatedValue.toBigDecimal()
                        val itemrate: BigDecimal =
                            itemRateUpdatedValue.toBigDecimal()
                        val itemAmt: String =
                            ((finalWt.setScale(2)
                                .multiply(itemrate.setScale(2))
                                    )).setScale(2, RoundingMode.CEILING).toString()
                        itemAmtUpdatedValue = itemAmt
                        binding.txtItemAmtAddlesswt.setText(itemAmtUpdatedValue)
                        Log.v("itemrate", itemRateUpdatedValue)
                        Log.v("finalWt", finalweightUpdatedValue)
                        // set total amt
                        setTotalAmt()
                    }
                    "fix" -> {
                        itemAmtUpdatedValue = itemRateUpdatedValue
                        binding.txtItemAmtAddlesswt.setText(itemAmtUpdatedValue)
                        // set total amt
                        setTotalAmt()
                    }
                }
            }
            // update values from labour amt (second drop down)
            true -> {
                when (selectedPerLabourId) {
                    "unit" -> {
                        // Log.v("unit",selectedPerItemId)
                        val pcs: BigDecimal = piecesUpdatedValue.toBigDecimal()
                        val lrbrate: BigDecimal =
                            lbrRateUpdatedValue.toBigDecimal()
                        val lrbAmt: String =
                            ((pcs.setScale(2)
                                .multiply(lrbrate.setScale(2))
                                    )).setScale(2, RoundingMode.CEILING).toString()
                        labourAmtUpdatedValue = lrbAmt
                        binding.txtLabourAmtAddlesswt.setText(labourAmtUpdatedValue)
                        // set total amt
                        setTotalAmt()
                    }
                    "maintain_stock_in" -> {
                        // Log.v("msi",selectedPerItemId)
                        val finalWt: BigDecimal = finalweightUpdatedValue.toBigDecimal()
                        val lrbrate: BigDecimal =
                            lbrRateUpdatedValue.toBigDecimal()
                        val lrbAmt: String =
                            ((finalWt.setScale(2)
                                .multiply(lrbrate.setScale(2))
                                    )).setScale(2, RoundingMode.CEILING).toString()
                        labourAmtUpdatedValue = lrbAmt
                        binding.txtLabourAmtAddlesswt.setText(labourAmtUpdatedValue)
                        // set total amt
                        setTotalAmt()
                    }
                    "fix" -> {
                        labourAmtUpdatedValue = lbrRateUpdatedValue
                        //itemAmtUpdatedValue = itemRateUpdatedValue
                        binding.txtLabourAmtAddlesswt.setText(labourAmtUpdatedValue)
                        // set total amt
                        setTotalAmt()
                    }
                }
            }
        }

    }

    private fun onFocusChangeSetup() {
        binding.txtItemPiecesAddlessswt.setOnFocusChangeListener { v, hasFocus -> //if(!hasFocus)
            if (!hasFocus) {
                when (txtItemPiecesAddlessswt.text.isNullOrBlank()) {
                    true -> {
                        piecesUpdatedValue = "0"
                        binding.txtItemPiecesAddlessswt.setText(piecesUpdatedValue)
                        binding.txtItemPiecesAddlessswt.setSelection(piecesUpdatedValue.length)
                    }
                    else -> {
                        binding.txtItemPiecesAddlessswt.setText(piecesUpdatedValue)
                        binding.txtItemPiecesAddlessswt.setSelection(piecesUpdatedValue.length)

                        when (txtWeightAddlessswt.text.isNullOrBlank()) {
                            true -> {
                                weightUpdatedValue = "0.000"
                                lesswtUpdatedValue = weightUpdatedValue
                                binding.txtWeightAddlessswt.setText(weightUpdatedValue)
                                binding.txtLesswtAddlesswt.setText(lesswtUpdatedValue)
                                binding.txtWeightAddlessswt.setSelection(weightUpdatedValue.length)
                                Toast.makeText(
                                    this,
                                    getString(R.string.lesswt_st_weight_error_msg),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            else -> {
                                when (weightUpdatedValue.toBigDecimal()
                                    .compareTo(BigDecimal.ZERO) == 0) {
                                    true -> {
                                        //  Toast.makeText(this, getString(R.string.lesswt_st_weight_error_msg), Toast.LENGTH_SHORT).show()
                                    }
                                    false -> {
                                        // lesswtUpdatedValue = weightUpdatedValue
                                        binding.txtWeightAddlessswt.setText(weightUpdatedValue)
                                        binding.txtWeightAddlessswt.setSelection(weightUpdatedValue.length)

                                        val weight = weightUpdatedValue.toBigDecimal()
                                        val pcs = piecesUpdatedValue.toBigDecimal()
                                        val result1: String =
                                            ((pcs.setScale(2)
                                                .multiply(weight.setScale(3))
                                                    )).setScale(3, RoundingMode.CEILING).toString()
                                        lesswtUpdatedValue = result1
                                        binding.txtLesswtAddlesswt.setText(lesswtUpdatedValue)

                                        val lesswt: BigDecimal = lesswtUpdatedValue.toBigDecimal()
                                        val variation: BigDecimal =
                                            variationUpdateValue.toBigDecimal()
                                        val result: String =
                                            ((lesswt.setScale(3) - (lesswt.setScale(3)
                                                .multiply(variation)) / BigDecimal(
                                                100
                                            ))).setScale(3, RoundingMode.CEILING).toString()
                                        finalweightUpdatedValue = result
                                        binding.txtFinalwtAddlesswt.setText(finalweightUpdatedValue)

                                    }
                                }
                            }
                        }

                    }
                }
                // to update item amount labour amount and total amount when pieces changesd
                updateValuesasPerDDSelection(true)
                updateValuesasPerDDSelection(false)
            }
        }
        binding.txtWeightAddlessswt.setOnFocusChangeListener { v, hasFocus -> //if(!hasFocus)
            if (!hasFocus) {
                when (txtWeightAddlessswt.text.isNullOrBlank()) {
                    true -> {
                        weightUpdatedValue = "0.000"
                        lesswtUpdatedValue = weightUpdatedValue
                        binding.txtWeightAddlessswt.setText(weightUpdatedValue)
                        binding.txtLesswtAddlesswt.setText(lesswtUpdatedValue)
                        binding.txtWeightAddlessswt.setSelection(weightUpdatedValue.length)
                        Toast.makeText(
                            this,
                            getString(R.string.lesswt_st_weight_error_msg),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    else -> {
                        when (weightUpdatedValue.toBigDecimal().compareTo(BigDecimal.ZERO) == 0) {
                            true -> {
                                //  Toast.makeText(this, getString(R.string.lesswt_st_weight_error_msg), Toast.LENGTH_SHORT).show()
                            }
                            false -> {
                               // lesswtUpdatedValue = weightUpdatedValue
                                binding.txtWeightAddlessswt.setText(weightUpdatedValue)
                                binding.txtWeightAddlessswt.setSelection(weightUpdatedValue.length)

                                val weight = weightUpdatedValue.toBigDecimal()
                                val pcs = piecesUpdatedValue.toBigDecimal()
                                val result1 : String =
                                    ((pcs.setScale(2)
                                        .multiply(weight.setScale(3))
                                            )).setScale(3, RoundingMode.CEILING).toString()
                                lesswtUpdatedValue = result1
                                binding.txtLesswtAddlesswt.setText(lesswtUpdatedValue)

                                val lesswt: BigDecimal = lesswtUpdatedValue.toBigDecimal()
                                val variation: BigDecimal = variationUpdateValue.toBigDecimal()
                                val result: String =
                                    ((lesswt.setScale(3) - (lesswt.setScale(3)
                                        .multiply(variation)) / BigDecimal(
                                        100
                                    ))).setScale(3, RoundingMode.CEILING).toString()
                                finalweightUpdatedValue = result
                                binding.txtFinalwtAddlesswt.setText(finalweightUpdatedValue)

                            }
                        }

                    }
                }
                // to update item amount labour amount and total amount when pieces changesd
                updateValuesasPerDDSelection(true)
                updateValuesasPerDDSelection(false)
            }
        }
        binding.txtVariationAddlesswt.setOnFocusChangeListener { v, hasFocus -> //if(!hasFocus)
            if (!hasFocus) {
                when (txtWeightAddlessswt.text.isNullOrBlank()) {
                    true -> {
                        variationUpdateValue = "0.00"
                        binding.txtVariationAddlesswt.setText(variationUpdateValue)
                        binding.txtVariationAddlesswt.setSelection(variationUpdateValue.length)

                    }
                    else -> {
                        binding.txtVariationAddlesswt.setText(variationUpdateValue)
                        binding.txtVariationAddlesswt.setSelection(variationUpdateValue.length)

                        when (weightUpdatedValue.toBigDecimal().compareTo(BigDecimal.ZERO) > 0) {
                            true -> {
                                val lesswt: BigDecimal = lesswtUpdatedValue.toBigDecimal()
                                val variation: BigDecimal = variationUpdateValue.toBigDecimal()
                                val result: String =
                                    ((lesswt.setScale(3) - (lesswt.setScale(3)
                                        .multiply(variation)) / BigDecimal(
                                        100
                                    ))).setScale(3, RoundingMode.CEILING).toString()
                                finalweightUpdatedValue = result
                                binding.txtFinalwtAddlesswt.setText(finalweightUpdatedValue)
                            }
                            else->{

                            }
                        }


                    }
                }
                // to update item amount labour amount and total amount when pieces changesd
                updateValuesasPerDDSelection(true)
                updateValuesasPerDDSelection(false)
            }

        }
        binding.txtItemRateAddlessswt.setOnFocusChangeListener { v, hasFocus -> //if(!hasFocus)
            if (!hasFocus) {
                when (txtItemRateAddlessswt.text.isNullOrBlank()) {
                    true -> {
                        itemRateUpdatedValue = "0.00"
                        binding.txtItemRateAddlessswt.setText(itemRateUpdatedValue)
                        binding.txtItemRateAddlessswt.setSelection(itemRateUpdatedValue.length)
                    }
                    else -> {
                        when (itemRateUpdatedValue.toBigDecimal().compareTo(BigDecimal.ZERO) == 0) {
                            true -> {
                                //  Toast.makeText(this, getString(R.string.lesswt_st_weight_error_msg), Toast.LENGTH_SHORT).show()
                            }
                            false -> {
                                binding.txtItemRateAddlessswt.setText(itemRateUpdatedValue)
                                binding.txtItemRateAddlessswt.setSelection(itemRateUpdatedValue.length)


                                /*when (piecesUpdatedValue.toBigDecimal()
                                    .compareTo(BigDecimal.ZERO) > 0) {
                                    true -> {
                                        val pcs: BigDecimal = piecesUpdatedValue.toBigDecimal()
                                        val itemrate: BigDecimal =
                                            itemRateUpdatedValue.toBigDecimal()
                                        val itemAmt: String =
                                            ((pcs.setScale(2)
                                                .multiply(itemrate.setScale(2))
                                                    )).setScale(2, RoundingMode.CEILING).toString()
                                        itemAmtUpdatedValue = itemAmt
                                        binding.txtItemAmtAddlesswt.setText(itemAmt)
                                        // set total amt
                                        setTotalAmt()
                                    }
                                }*/
                            }
                        }
                    }

                }
                // to update item amount labour amount and total amount when pieces changesd
                updateValuesasPerDDSelection(true)
                updateValuesasPerDDSelection(false)
            }
        }
        binding.txtLabourRateAddlessswt.setOnFocusChangeListener { v, hasFocus -> //if(!hasFocus)
            if (!hasFocus) {
                when (txtLabourRateAddlessswt.text.isNullOrBlank()) {
                    true -> {
                        lbrRateUpdatedValue = "0.00"
                        binding.txtLabourRateAddlessswt.setText(lbrRateUpdatedValue)
                        binding.txtLabourRateAddlessswt.setSelection(lbrRateUpdatedValue.length)
                    }
                    else -> {
                        when (lbrRateUpdatedValue.toBigDecimal().compareTo(BigDecimal.ZERO) == 0) {
                            true -> {
                                //  Toast.makeText(this, getString(R.string.lesswt_st_weight_error_msg), Toast.LENGTH_SHORT).show()
                            }
                            false -> {
                                binding.txtLabourRateAddlessswt.setText(lbrRateUpdatedValue)
                                binding.txtLabourRateAddlessswt.setSelection(lbrRateUpdatedValue.length)


                                /*when (piecesUpdatedValue.toBigDecimal()
                                    .compareTo(BigDecimal.ZERO) > 0) {
                                    true -> {
                                        val pcs: BigDecimal = piecesUpdatedValue.toBigDecimal()
                                        val labourRate: BigDecimal =
                                            labourAmtUpdatedValue.toBigDecimal()
                                        val lbrAmt: String =
                                            ((pcs.setScale(2)
                                                .multiply(labourRate.setScale(2))
                                                    )).setScale(2, RoundingMode.CEILING).toString()
                                        labourAmtUpdatedValue = lbrAmt
                                        binding.txtLabourAmtAddlesswt.setText(labourAmtUpdatedValue)
                                        // set total amt
                                        setTotalAmt()
                                    }
                                }*/
                            }
                        }
                    }

                }
                // to update item amount labour amount and total amount when pieces changesd
                updateValuesasPerDDSelection(true)
                updateValuesasPerDDSelection(false)
            }
        }

    }

    private fun setTotalAmt() {
        val itemAmt: BigDecimal = itemAmtUpdatedValue.toBigDecimal()
        val labramt: BigDecimal = labourAmtUpdatedValue.toBigDecimal()
        val totalAmt: String =
            ((itemAmt.setScale(2)
                .plus(labramt.setScale(2))
                    )).setScale(2, RoundingMode.CEILING).toString()
        totalAmtUpdatedValue = totalAmt
        binding.txtTotalAmountAddItem.setText(totalAmtUpdatedValue)
    }

    private fun ontextChangeSetup() {
        binding.txtItemPiecesAddlessswt.doAfterTextChanged {

            val str: String = binding.txtItemPiecesAddlessswt.text.toString()

            if (str.isEmpty()) return@doAfterTextChanged
            val str2: String = CommonUtils.perfectDecimal(str, 13, 0).toString()
            if (!str2.equals(str)) {
                //val str3:String = df.format(str2.toDouble())
                binding.txtItemPiecesAddlessswt.setText(str2)
                binding.txtItemPiecesAddlessswt.setSelection(str2.length)
            }

            piecesUpdatedValue = str2.toInt().toString()
            Log.v("piecesupdatedValue", piecesUpdatedValue)
        }
        binding.txtWeightAddlessswt.doAfterTextChanged {

            val str: String = binding.txtWeightAddlessswt.text.toString()

            if (str.isEmpty()) return@doAfterTextChanged
            val str2: String = CommonUtils.perfectDecimal(str, 9, 3).toString()
            if (!str2.equals(str)) {
                //val str3:String = df.format(str2.toDouble())
                binding.txtWeightAddlessswt.setText(str2)
                binding.txtWeightAddlessswt.setSelection(str2.length)
            }

            weightUpdatedValue = df1.format(str2.toDouble())
        }
        binding.txtVariationAddlesswt.doAfterTextChanged {
            val str: String = binding.txtVariationAddlesswt.text.toString()

            if (str.isEmpty()) return@doAfterTextChanged
            val str2: String = CommonUtils.perfectDecimal(str, 10, 2).toString()
            if (!str2.equals(str)) {
                //val str3:String = df.format(str2.toDouble())
                binding.txtVariationAddlesswt.setText(str2)
                binding.txtVariationAddlesswt.setSelection(str2.length)
            }
            /* Log.v("wastage", (df.format(str2.toDouble())))*/
            variationUpdateValue = df.format(str2.toDouble())
        }
        binding.txtItemRateAddlessswt.doAfterTextChanged {
            val str: String = binding.txtItemRateAddlessswt.text.toString()

            if (str.isEmpty()) return@doAfterTextChanged
            val str2: String = CommonUtils.perfectDecimal(str, 10, 2).toString()
            if (!str2.equals(str)) {
                binding.txtItemRateAddlessswt.setText(str2)
                binding.txtItemRateAddlessswt.setSelection(str2.length)
            }

            itemRateUpdatedValue = df.format(str2.toDouble())
        }
        binding.txtLabourRateAddlessswt.doAfterTextChanged {
            val str: String = binding.txtLabourRateAddlessswt.text.toString()

            if (str.isEmpty()) return@doAfterTextChanged
            val str2: String = CommonUtils.perfectDecimal(str, 10, 2).toString()
            if (!str2.equals(str)) {
                binding.txtLabourRateAddlessswt.setText(str2)
                binding.txtLabourRateAddlessswt.setSelection(str2.length)
            }

            lbrRateUpdatedValue = df.format(str2.toDouble())
        }
    }

    private fun applyingDigitFilter() {
        // applying filters to edit input number decimal fields(which have 2 or 3 decimal after .)

        binding.txtWeightAddlessswt.setFilters(
            arrayOf<InputFilter>(
                DigitsInputFilter(
                    9,
                    3,
                    999999999.999
                )
            )
        )
        binding.txtLesswtAddlesswt.setFilters(
            arrayOf<InputFilter>(
                DigitsInputFilter(
                    9,
                    3,
                    999999999.999
                )
            )
        )
        binding.txtFinalwtAddlesswt.setFilters(
            arrayOf<InputFilter>(
                DigitsInputFilter(
                    9,
                    3,
                    999999999.999
                )
            )
        )
        binding.txtVariationAddlesswt.setFilters(
            arrayOf<InputFilter>(
                DigitsInputFilter(
                    10,
                    2,
                    9999999999.99
                )
            )
        )
        binding.txtItemRateAddlessswt.setFilters(
            arrayOf<InputFilter>(
                DigitsInputFilter(
                    10,
                    2,
                    9999999999.99
                )
            )
        )
        binding.txtItemAmtAddlesswt.setFilters(
            arrayOf<InputFilter>(
                DigitsInputFilter(
                    10,
                    2,
                    9999999999.99
                )
            )
        )
        binding.txtLabourRateAddlessswt.setFilters(
            arrayOf<InputFilter>(
                DigitsInputFilter(
                    10,
                    2,
                    9999999999.99
                )
            )
        )
        binding.txtLabourAmtAddlesswt.setFilters(
            arrayOf<InputFilter>(
                DigitsInputFilter(
                    10,
                    2,
                    9999999999.99
                )
            )
        )
        binding.txtTotalAmountAddItem.setFilters(
            arrayOf<InputFilter>(
                DigitsInputFilter(
                    17,
                    2,
                    99999999999999999.99
                )
            )
        )

    }

    private fun calculateTotalofLessWtTotalnAmt() {
        for (lesswt in addlessweightList) {
            totalLwofAllUpdatedValue =
                ((lesswt.less_wt_final_wt_converted.toBigDecimal().setScale(3)
                    .plus(totalLwofAllUpdatedValue.toBigDecimal().setScale(3))
                        )).setScale(3, RoundingMode.CEILING).toString()

            totalLwAmtofAllUpdatedValue =
                ((lesswt.less_wt_total_amount.toBigDecimal().setScale(2)
                    .plus(totalLwAmtofAllUpdatedValue.toBigDecimal().setScale(2))
                        )).setScale(2, RoundingMode.CEILING).toString()
        }
        Log.v("total_lw_ofall", totalLwofAllUpdatedValue)
        Log.v("total_lw_Amt_ofall", totalLwAmtofAllUpdatedValue)
    }

    private fun convertValuesInGrams() {
        when (selectedItemUnit) {
            "Kilograms" -> {
                calculatedLw = CommonUtils.kgTogms(binding.txtLesswtAddlesswt.text.toString())
                calculatedFinalLw = CommonUtils.kgTogms(binding.txtFinalwtAddlesswt.text.toString())
            }
            "Carat" -> {
                calculatedLw = CommonUtils.carrotTogm(binding.txtLesswtAddlesswt.text.toString())
                calculatedFinalLw =
                    CommonUtils.carrotTogm(binding.txtFinalwtAddlesswt.text.toString())
            }
            "Grams" -> {
                calculatedLw = binding.txtLesswtAddlesswt.text.toString()
                calculatedFinalLw = binding.txtFinalwtAddlesswt.text.toString()
            }
        }
    }


    private fun saveLessWeightModel() {
        if (prefs.contains(Constants.PREF_LESS_WEIGHT_INFO_KEY)) {
            val collectionType = object :
                TypeToken<java.util.ArrayList<AddLessWeightModel.AddLessWeightModelItem>>() {}.type
            var lessweightList: ArrayList<AddLessWeightModel.AddLessWeightModelItem> =
                Gson().fromJson(prefs[Constants.PREF_LESS_WEIGHT_INFO_KEY, ""], collectionType)
            addlessweightList.addAll(lessweightList)
        } else {
            addlessweightList = ArrayList()
        }


        convertValuesInGrams()

        val childModel = AddLessWeightModel.AddLessWeightModelItem(
            "",
            /*calculatedFinalLw*/binding.txtFinalwtAddlesswt.text.toString(),
            binding.txtItemAmtAddlesswt.text.toString(),
            selectedItemID,
            selectedItemName,
            selectedPerItemId,
            selectedPerNameForItem,
            binding.txtItemRateAddlessswt.text.toString(),
            binding.txtLabourAmtAddlesswt.text.toString(),
            selectedPerLabourId,
            selectedPerNameForLabour,
            binding.txtLabourRateAddlessswt.text.toString(),
            /*calculatedLw*/binding.txtLesswtAddlesswt.text.toString(),
            maintainStock_name,
            binding.txtItemPiecesAddlessswt.text.toString(),
            binding.txtTotalAmountAddItem.text.toString(),
            unitArrayList,
            binding.txtVariationAddlesswt.text.toString(),
            binding.txtWeightAddlessswt.text.toString(),
            calculatedLw,calculatedFinalLw
        )


        if (new_lessweight_pos >= 0 && new_lessweight_pos != -1) {
            // Update selected item
            addlessweightList.set(new_lessweight_pos, childModel)
        } else {
            // Add new item
            addlessweightList.add(childModel)
        }

        calculateTotalofLessWtTotalnAmt()
        val prefs = PreferenceHelper.defaultPrefs(this)
        prefs[Constants.PREF_LESS_WEIGHT_INFO_KEY] = Gson().toJson(addlessweightList)


    }

    private fun saveItemWtBreakupModel() {
        /* if (prefs.contains(Constants.PREF_LESS_WEIGHT_BREAKUP_INFO_KEY)) {
             val collectionType = object :
                 TypeToken<java.util.ArrayList<OpeningStockItemModel.OpeningStockItemModelItem.ItemWtBreakup>>() {}.type
             var lesswtBreakupList: ArrayList<OpeningStockItemModel.OpeningStockItemModelItem.ItemWtBreakup> =
                 Gson().fromJson(prefs[Constants.PREF_LESS_WEIGHT_BREAKUP_INFO_KEY, ""], collectionType)
             lessweightBreakupList.addAll(lesswtBreakupList)
         } else {
             lessweightBreakupList = ArrayList()
         }*/

        //calculateTotalofLessWtTotalnAmt()
        val childModel = OpeningStockItemModel.OpeningStockItemModelItem.ItemWtBreakup(
            addlessweightList.size.toString(),
            addlessweightList,
            totalLwofAllUpdatedValue,
            totalLwAmtofAllUpdatedValue

        )


        /* if (new_lesswtBreakup_pos >= 0 && new_lesswtBreakup_pos != -1) {
             // Update selected item
             lessweightBreakupList.set(new_lesswtBreakup_pos, childModel)
         } else {
             // Add new item
             lessweightBreakupList.add(childModel)
         }
 */

        val prefs = PreferenceHelper.defaultPrefs(this)
        prefs[Constants.PREF_LESS_WEIGHT_BREAKUP_INFO_KEY] = Gson().toJson(childModel)


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
            getItemSearch()
        }

        if (!ConnectivityStateHolder.isConnected) {
            // Network is not available
            CommonUtils.showDialog(this, getString(R.string.please_check_internet_msg))

        }
    }

    private fun setupViewModel() {
        viewModel =
            ViewModelProvider(this, ViewModelFactory(ApiHelper(RetrofitBuilder.apiService))).get(
                AddItemViewModel::class.java
            )
        binding.setLifecycleOwner(this)
    }

    fun getItemSearch() {
        if (NetworkUtils.isConnected()) {
            viewModel.getItemSearch(
                loginModel?.data?.bearer_access_token,
                binding.txtItemNameAddLessWeight.text.toString(),
                "",
                "other"
            ).observe(this, Observer {
                it?.let { resource ->
                    when (resource.status) {
                        Status.SUCCESS -> {
                            /*Log.v("..setupObservers..", "..Success...")*/
                            if (it.data?.status == true) {
                                itemList = it.data.data
                                itemNameList = itemList?.map { it.item_name.toString() }


                                ItemDetailsAdapter = ItemDetailsAdapter(
                                    this, true, R.layout.search_item_popup,
                                    itemList!!
                                )
                                /*  ItemDetailsAdapter.apply { addItems(itemList)
                                      notifyDataSetChanged()}*/
                                binding.txtItemNameAddLessWeight.setAdapter(ItemDetailsAdapter)
                                binding.txtItemNameAddLessWeight.threshold = 1



                                binding.txtItemNameAddLessWeight.setOnItemClickListener { adapterView, view, position, l
                                    ->
                                    val selected: String =
                                        adapterView.getItemAtPosition(position).toString()

                                    val pos: Int? = itemList?.get(0)?.item_name?.indexOf(selected)

                                    /* selectedItemID = pos?.let { it1 -> itemList?.get(it1)?.id
                                     }*/
                                    val selectedPoi =
                                        adapterView.adapter.getItem(position) as ItemSearchModel.ItemSearch?

                                    selectedItemID = selectedPoi?.id.toString()
                                    binding.txtItemNameAddLessWeight.setText(selectedPoi?.item_name)
                                    selectedItemName = selectedPoi?.item_name!!
                                    binding.txtItemNameAddLessWeight.setSelection(selectedPoi?.item_name?.length!!)

                                    binding.txtWeightAddlessswt.setText(selectedPoi.product_wt)
                                    weightUpdatedValue = selectedPoi.product_wt
                                    binding.txtItemRateAddlessswt.setText(selectedPoi.item_rate)
                                    itemRateUpdatedValue = selectedPoi.item_rate
                                    binding.txtPerAddlesswt.setText(selectedPoi.unit_array.get(0).name)
                                    selectedPerItemId = selectedPoi.unit_array.get(0).id
                                    selectedPerNameForItem = selectedPoi.unit_array.get(0).name
                                    binding.txtLabourPerAddlesswt.setText(
                                        selectedPoi.unit_array.get(
                                            0
                                        ).name
                                    )
                                    selectedPerLabourId = selectedPoi.unit_array.get(0).id
                                    selectedPerNameForLabour = selectedPoi.unit_array.get(0).name


                                    binding.txtItemUnitAddlesswt.setText(selectedPoi.unit_name)
                                    selectedItemUnit = selectedPoi.maintain_stock_in_name

                                    unitArrayList = selectedPoi.unit_array
                                    unitNameList = unitArrayList?.map { it.name }

                                    maintainStock_name = selectedPoi.maintain_stock_in_name
                                    less_wt_unit_name = selectedPoi.unit_name
                                    //  CommonUtils.hideKeyboard(this,txtItemNameAddItem)

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
                            /*Toast.makeText(this, it.data?.errormessage?.message, Toast.LENGTH_LONG)
                                .show()*/
                            /* Log.v("..setupObservers..", "..ERROR...")*/
                        }
                        Status.LOADING -> {
                            /*Log.v("..setupObservers..", "..LOADING...")*/
                        }
                    }
                }
            })
        }

    }


    private fun openItemperMenu(unitNameList: List<String>?) {

        popupMenu = PopupMenu(this, binding.txtPerAddlesswt)
        for (i in 0 until this.unitNameList!!.size) {
            popupMenu.menu.add(
                Menu.NONE,
                i,
                i,
                this.unitNameList!!.get(i)
            ) //add(groupId, itemId, order, title);
        }

        popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
            binding.txtPerAddlesswt.setText(item.title)
            val selected: String = item.title.toString()
            val pos: Int? = this.unitNameList!!.indexOf(selected)


            selectedPerItemId =
                pos?.let { it1 -> unitArrayList?.get(it1)?.id }.toString()

            selectedPerNameForItem =
                pos?.let { it1 -> unitArrayList?.get(it1)?.name }.toString()

            updateValuesasPerDDSelection(false)

            true
        })

        popupMenu.show()
    }


    private fun openLabourperMenu(unitNameList: List<String>?) {

        popupMenu = PopupMenu(this, binding.txtLabourPerAddlesswt)
        for (i in 0 until this.unitNameList!!.size) {
            popupMenu.menu.add(
                Menu.NONE,
                i,
                i,
                this.unitNameList!!.get(i)
            ) //add(groupId, itemId, order, title);
        }


        popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
            binding.txtLabourPerAddlesswt.setText(item.title)
            val selected: String = item.title.toString()
            val pos: Int? = this.unitNameList!!.indexOf(selected)

            selectedPerLabourId =
                pos?.let { it1 -> unitArrayList?.get(it1)?.id }.toString()
            selectedPerNameForLabour =
                pos?.let { it1 -> unitArrayList?.get(it1)?.name }.toString()

            updateValuesasPerDDSelection(true)
            true
        })

        popupMenu.show()
    }


}
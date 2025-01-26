package com.goldbookapp.ui.activity.additem

import android.content.SharedPreferences
import android.os.Bundle
import android.text.InputFilter
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.goldbookapp.R
import com.goldbookapp.api.ApiHelper
import com.goldbookapp.api.RetrofitBuilder
import com.goldbookapp.databinding.SimpleLesWeightActivityNewBinding
import com.goldbookapp.model.AddLessWeightModel
import com.goldbookapp.model.ItemSearchModel
import com.goldbookapp.model.LoginModel
import com.goldbookapp.model.OpeningStockItemModel
import com.goldbookapp.ui.activity.viewmodel.AddItemViewModel
import com.goldbookapp.ui.activity.viewmodel.ViewModelFactory
import com.goldbookapp.utils.CommonUtils
import com.goldbookapp.utils.CommonUtils.Companion.clickWithDebounce
import com.goldbookapp.utils.Constants
import com.goldbookapp.utils.DigitsInputFilter
import com.goldbookapp.utils.PreferenceHelper
import com.goldbookapp.utils.PreferenceHelper.get
import com.goldbookapp.utils.PreferenceHelper.set
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hamzasharuf.networkmonitor.ConnectivityStateHolder
import com.hamzasharuf.networkmonitor.Event
import com.hamzasharuf.networkmonitor.NetworkEvents
import kotlinx.android.synthetic.main.simple_les_weight_activity_new.*
import kotlinx.android.synthetic.main.toolbar_layout.*
import kotlinx.android.synthetic.main.toolbar_layout.view.*
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat

class SimpleLessWeightActivity : AppCompatActivity() {
    private var is_From_Position: Int = 0
    private lateinit var viewModel: AddItemViewModel
    lateinit var binding: SimpleLesWeightActivityNewBinding

    lateinit var prefs: SharedPreferences
    lateinit var loginModel: LoginModel
    var count: Int = 1

    var lessWeightValueOne: String = "0.000"
    var lessWeightValueTwo: String = "0.000"
    var lessWeightValueThree: String = "0.000"
    var lessWeightValueFour: String = "0.000"
    var lessWeightValueFive: String = "0.000"
    var totalOtherChrgsUpdatedValue: String = "0.000"
    var lessWtRemoveValue: String = "0.000"

    val df = DecimalFormat("0.000")

    var addlessweightList = ArrayList<AddLessWeightModel.AddLessWeightModelItem>()
    lateinit var addlessweightListPref: List<AddLessWeightModel.AddLessWeightModelItem>
    private var new_lessweight_pos: Int = -1
    var unitArrayList: List<ItemSearchModel.ItemSearch.Unit_array>? = null
    var is_From_Pref: Boolean = false
    var is_From_Updated_btn: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.simple_les_weight_activity_new)

        setupViewModel()
        setupUIandListner()


    }

    private fun setupUIandListner() {
        prefs = PreferenceHelper.defaultPrefs(this)
        loginModel = Gson().fromJson(
            prefs[Constants.PREF_LOGIN_DETAIL_KEY, ""],
            LoginModel::class.java
        )
        imgLeft.setImageResource(R.drawable.ic_back)
        tvTitle.setText("Add Less Weight")

        binding.root.imgLeft.clickWithDebounce {
            onBackPressed()
        }

        applyingDigitFilter()
        onTextChangeSetup()
        onFocusChangeSetup()
        getLessWeightFromPref()

        binding.tvAddChargeAddItem.clickWithDebounce {
            if(is_From_Pref){
                is_From_Pref = false
            }
            count = count + 1
            updateUIonAnother(count)
        }

        binding.imgCloseLessWt1.clickWithDebounce {
            is_From_Position = 1
            when (is_From_Pref) {
                true -> {
                    if (binding.txtLessWtAddWeightOne.text!!.isNotEmpty()) {
                        when (is_From_Updated_btn) {
                            //is_from_updated_btn = true --- once click done on the close from the pref data(for the second click on updated value )
                            true -> {
                                lessWtRemoveValue = lessWeightValueOne
                                updateTotalChargeAfterRemove(lessWtRemoveValue, totalOtherChrgsUpdatedValue)
                                // is_from_pref = false----remove value after update value on close
                                // from position(behave like w/o pref value )
                                updateUIAfterRemove(is_From_Position, false)
                            }
                            //is_from_updated_btn = false --- first click on close from the pref data
                            false -> {
                                lessWtRemoveValue = addlessweightListPref.get(0).less_wt_final_wt
                                updateTotalChargeAfterRemove(lessWtRemoveValue, totalOtherChrgsUpdatedValue)
                                //is_from_pref = true----remove value from pref on close from the position
                                updateUIAfterRemove(is_From_Position, true)
                            }
                        }


                    } else {
                        lessWtRemoveValue = "0.000"
                        updateTotalChargeAfterRemove(lessWtRemoveValue, totalOtherChrgsUpdatedValue)
                        updateUIAfterRemove(is_From_Position, true)
                    }
                }
                false -> {
                    if (binding.txtLessWtAddWeightOne.text!!.isNotEmpty()) {
                        lessWtRemoveValue = lessWeightValueOne
                        updateTotalChargeAfterRemove(lessWtRemoveValue, totalOtherChrgsUpdatedValue)
                        updateUIAfterRemove(is_From_Position, false)

                    } else {
                        lessWtRemoveValue = "0.000"
                        updateTotalChargeAfterRemove(lessWtRemoveValue, totalOtherChrgsUpdatedValue)
                        updateUIAfterRemove(is_From_Position, false)
                    }

                }
            }
            is_From_Updated_btn = true

        }

        binding.imgCloseLessWt2.clickWithDebounce {
            //count = count - 1
            is_From_Position = 2
            when (is_From_Pref) {
                true -> {
                    if (binding.txtLessWtAddWeightTwo.text!!.isNotEmpty()) {
                        when (is_From_Updated_btn) {
                            true -> {
                                lessWtRemoveValue = lessWeightValueTwo
                                Log.v("lesswtupdate", "" + lessWtRemoveValue)
                                updateTotalChargeAfterRemove(
                                    lessWtRemoveValue,
                                    totalOtherChrgsUpdatedValue
                                )
                                updateUIAfterRemove(is_From_Position, false)
                            }
                            false -> {
                                lessWtRemoveValue = addlessweightListPref.get(1).less_wt_final_wt
                                Log.v("lesswtupdate1", "" + lessWtRemoveValue)
                                updateTotalChargeAfterRemove(
                                    lessWtRemoveValue,
                                    totalOtherChrgsUpdatedValue
                                )
                                updateUIAfterRemove(is_From_Position, true)
                            }
                        }


                    } else {
                        lessWtRemoveValue = "0.000"
                        updateTotalChargeAfterRemove(lessWtRemoveValue, totalOtherChrgsUpdatedValue)
                        updateUIAfterRemove(is_From_Position, true)
                    }

                }
                false -> {
                    if (binding.txtLessWtAddWeightTwo.text!!.isNotEmpty()) {
                        lessWtRemoveValue = lessWeightValueTwo
                        updateTotalChargeAfterRemove(lessWtRemoveValue, totalOtherChrgsUpdatedValue)
                        updateUIAfterRemove(is_From_Position, false)

                    } else {
                        lessWtRemoveValue = "0.000"
                        updateTotalChargeAfterRemove(lessWtRemoveValue, totalOtherChrgsUpdatedValue)
                        updateUIAfterRemove(is_From_Position, false)
                    }

                }
            }
            is_From_Updated_btn = true
        }

        binding.imgCloseLessWt3.clickWithDebounce {
            is_From_Position = 3
            when (is_From_Pref) {
                true -> {
                    if (binding.txtLessWtAddWeightThree.text!!.isNotBlank()) {
                        when (is_From_Updated_btn) {
                            true -> {
                                lessWtRemoveValue = lessWeightValueThree
                                updateTotalChargeAfterRemove(
                                    lessWtRemoveValue,
                                    totalOtherChrgsUpdatedValue
                                )
                                updateUIAfterRemove(is_From_Position, false)
                            }
                            false -> {
                                lessWtRemoveValue = addlessweightListPref.get(2).less_wt_final_wt
                                updateTotalChargeAfterRemove(
                                    lessWtRemoveValue,
                                    totalOtherChrgsUpdatedValue
                                )
                                updateUIAfterRemove(is_From_Position, true)
                            }
                        }


                    } else {
                        lessWtRemoveValue = "0.000"
                        updateTotalChargeAfterRemove(lessWtRemoveValue, totalOtherChrgsUpdatedValue)
                        updateUIAfterRemove(is_From_Position, true)
                    }
                }
                false -> {
                    if (binding.txtLessWtAddWeightThree.text!!.isNotBlank()) {
                        lessWtRemoveValue = lessWeightValueThree
                        updateTotalChargeAfterRemove(lessWtRemoveValue, totalOtherChrgsUpdatedValue)
                        updateUIAfterRemove(is_From_Position, false)

                    } else {
                        lessWtRemoveValue = "0.000"
                        updateTotalChargeAfterRemove(lessWtRemoveValue, totalOtherChrgsUpdatedValue)
                        updateUIAfterRemove(is_From_Position, false)
                    }

                }
            }
            is_From_Updated_btn = true

        }
        binding.imgCloseLessWt4.clickWithDebounce {
            is_From_Position = 4
            when (is_From_Pref) {
                true -> {
                    if (binding.txtLessWtAddWeightFour.text!!.isNotEmpty()) {
                        when (is_From_Updated_btn) {
                            true -> {
                                lessWtRemoveValue = lessWeightValueFour
                                updateTotalChargeAfterRemove(
                                    lessWtRemoveValue,
                                    totalOtherChrgsUpdatedValue
                                )
                                updateUIAfterRemove(is_From_Position, false)
                            }
                            false -> {
                                lessWtRemoveValue = addlessweightListPref.get(3).less_wt_final_wt
                                updateTotalChargeAfterRemove(
                                    lessWtRemoveValue,
                                    totalOtherChrgsUpdatedValue
                                )
                                updateUIAfterRemove(is_From_Position, true)
                            }
                        }


                    } else {
                        lessWtRemoveValue = "0.000"
                        updateTotalChargeAfterRemove(lessWtRemoveValue, totalOtherChrgsUpdatedValue)
                        updateUIAfterRemove(is_From_Position, true)
                    }
                }
                false -> {
                    if (binding.txtLessWtAddWeightFour.text!!.isNotEmpty()) {
                        lessWtRemoveValue = lessWeightValueFour
                        updateTotalChargeAfterRemove(lessWtRemoveValue, totalOtherChrgsUpdatedValue)
                        updateUIAfterRemove(is_From_Position, false)

                    } else {
                        lessWtRemoveValue = "0.000"
                        updateTotalChargeAfterRemove(lessWtRemoveValue, totalOtherChrgsUpdatedValue)
                        updateUIAfterRemove(is_From_Position, false)
                    }

                }
            }
            //is_From_Updated_btn4 = true
            is_From_Updated_btn = true

        }
        binding.imgCloseLessWt5.clickWithDebounce {
            is_From_Position = 5
            when (is_From_Pref) {
                true -> {
                    if (binding.txtLessWtAddWeightFive.text!!.isNotEmpty()) {
                        lessWtRemoveValue = addlessweightListPref.get(4).less_wt_final_wt
                        updateTotalChargeAfterRemove(lessWtRemoveValue, totalOtherChrgsUpdatedValue)
                        updateUIAfterRemove(is_From_Position, true)
                    } else {
                        lessWtRemoveValue = "0.000"
                        updateTotalChargeAfterRemove(lessWtRemoveValue, totalOtherChrgsUpdatedValue)
                        updateUIAfterRemove(is_From_Position, true)
                    }
                }
                false -> {
                    if (binding.txtLessWtAddWeightFive.text!!.isNotEmpty()) {
                        lessWtRemoveValue = lessWeightValueFive
                        updateTotalChargeAfterRemove(lessWtRemoveValue, totalOtherChrgsUpdatedValue)
                        updateUIAfterRemove(is_From_Position, false)

                    } else {
                        lessWtRemoveValue = "0.000"
                        updateTotalChargeAfterRemove(lessWtRemoveValue, totalOtherChrgsUpdatedValue)
                        updateUIAfterRemove(is_From_Position, false)
                    }

                }
            }

        }

        binding.btnSaveAddLessWeightSimple.clickWithDebounce {
            clearFocus()
            saveLessWeightAccordingToLessWeight()
            saveItemWtBreakupModel()
            finish()
        }

    }

    private fun clearFocus() {
        binding.txtLessWtAddWeightOne.clearFocus()
        binding.txtLessWtAddWeightTwo.clearFocus()
        binding.txtLessWtAddWeightThree.clearFocus()
        binding.txtLessWtAddWeightFour.clearFocus()
        binding.txtLessWtAddWeightFive.clearFocus()
        binding.txtLabelAddWeightTwo.clearFocus()
        binding.txtLabelAddWeightOne.clearFocus()
        binding.txtLabelAddWeightThree.clearFocus()
        binding.txtLabelAddWeightFour.clearFocus()
        binding.txtLabelAddWeightFive.clearFocus()
    }

    private fun updateUIonAnother(count: Int) {
        when (count) {
            2 -> {
                lessWeightValueTwo = "0.000"
                lessWeightValueThree = "0.000"
                lessWeightValueFour = "0.000"
                lessWeightValueFive = "0.000"
                binding.cardlessWeightTwo.visibility = View.VISIBLE
                binding.tvAddChargeAddItem.visibility = View.VISIBLE
            }
            3 -> {

                lessWeightValueThree = "0.000"
                lessWeightValueFour = "0.000"
                lessWeightValueFive = "0.000"
                binding.cardlessWeightTwo.visibility = View.VISIBLE
                binding.cardlessWeightThree.visibility = View.VISIBLE
                binding.tvAddChargeAddItem.visibility = View.VISIBLE
            }
            4 -> {

                lessWeightValueFour = "0.000"
                lessWeightValueFive = "0.000"
                binding.cardlessWeightTwo.visibility = View.VISIBLE
                binding.cardlessWeightThree.visibility = View.VISIBLE
                binding.cardlessWeightFour.visibility = View.VISIBLE
                binding.tvAddChargeAddItem.visibility = View.VISIBLE
            }
            5 -> {
                lessWeightValueFive = "0.000"
                binding.cardlessWeightTwo.visibility = View.VISIBLE
                binding.cardlessWeightThree.visibility = View.VISIBLE
                binding.cardlessWeightFour.visibility = View.VISIBLE
                binding.cardlessWeightFive.visibility = View.VISIBLE
                binding.tvAddChargeAddItem.visibility = View.GONE

            }
        }
    }

    private fun getLessWeightFromPref() {
        if (prefs.contains(Constants.PREF_LESS_WEIGHT_BREAKUP_INFO_KEY)) {
            is_From_Pref = true
            val collectionType = object :
                TypeToken<OpeningStockItemModel.OpeningStockItemModelItem.ItemWtBreakup>() {}.type
            var lessWeightItemList: OpeningStockItemModel.OpeningStockItemModelItem.ItemWtBreakup =
                Gson().fromJson(
                    prefs[Constants.PREF_LESS_WEIGHT_BREAKUP_INFO_KEY, ""],
                    collectionType
                )

            addlessweightListPref = lessWeightItemList.less_wt_array
            totalOtherChrgsUpdatedValue = lessWeightItemList.total_less_wt
            binding.tvTotalChargeAddItem.setText(lessWeightItemList.total_less_wt)
            count = addlessweightListPref.size
            Log.v("count", "" + count)
            updateUIonAnother(addlessweightListPref.size)
            when (addlessweightListPref.size) {
                1 -> {
                    binding.txtLessWtAddWeightOne.setText(addlessweightListPref.get(0).less_wt_final_wt)
                    binding.txtLabelAddWeightOne.setText(addlessweightListPref.get(0).label)
                }
                2 -> {
                    binding.txtLessWtAddWeightOne.setText(addlessweightListPref.get(0).less_wt_final_wt)
                    binding.txtLabelAddWeightOne.setText(addlessweightListPref.get(0).label)

                    binding.txtLessWtAddWeightTwo.setText(addlessweightListPref.get(1).less_wt_final_wt)
                    binding.txtLabelAddWeightTwo.setText(addlessweightListPref.get(1).label)
                }
                3 -> {
                    binding.txtLessWtAddWeightOne.setText(addlessweightListPref.get(0).less_wt_final_wt)
                    binding.txtLabelAddWeightOne.setText(addlessweightListPref.get(0).label)

                    binding.txtLessWtAddWeightTwo.setText(addlessweightListPref.get(1).less_wt_final_wt)
                    binding.txtLabelAddWeightTwo.setText(addlessweightListPref.get(1).label)

                    binding.txtLessWtAddWeightThree.setText(addlessweightListPref.get(2).less_wt_final_wt)
                    binding.txtLabelAddWeightThree.setText(addlessweightListPref.get(2).label)

                }
                4 -> {
                    binding.txtLessWtAddWeightOne.setText(addlessweightListPref.get(0).less_wt_final_wt)
                    binding.txtLabelAddWeightOne.setText(addlessweightListPref.get(0).label)

                    binding.txtLessWtAddWeightTwo.setText(addlessweightListPref.get(1).less_wt_final_wt)
                    binding.txtLabelAddWeightTwo.setText(addlessweightListPref.get(1).label)

                    binding.txtLessWtAddWeightThree.setText(addlessweightListPref.get(2).less_wt_final_wt)
                    binding.txtLabelAddWeightThree.setText(addlessweightListPref.get(2).label)

                    binding.txtLessWtAddWeightFour.setText(addlessweightListPref.get(3).less_wt_final_wt)
                    binding.txtLabelAddWeightFour.setText(addlessweightListPref.get(3).label)
                }
                5 -> {

                    binding.txtLessWtAddWeightOne.setText(addlessweightListPref.get(0).less_wt_final_wt)
                    binding.txtLabelAddWeightOne.setText(addlessweightListPref.get(0).label)

                    binding.txtLessWtAddWeightTwo.setText(addlessweightListPref.get(1).less_wt_final_wt)
                    binding.txtLabelAddWeightTwo.setText(addlessweightListPref.get(1).label)

                    binding.txtLessWtAddWeightThree.setText(addlessweightListPref.get(2).less_wt_final_wt)
                    binding.txtLabelAddWeightThree.setText(addlessweightListPref.get(2).label)

                    binding.txtLessWtAddWeightFour.setText(addlessweightListPref.get(3).less_wt_final_wt)
                    binding.txtLabelAddWeightFour.setText(addlessweightListPref.get(3).label)

                    binding.txtLessWtAddWeightFive.setText(addlessweightListPref.get(4).less_wt_final_wt)
                    binding.txtLabelAddWeightFive.setText(addlessweightListPref.get(4).label)
                }
            }
        }

    }

    private fun saveLessWeightAccordingToLessWeight() {
        when (count) {
            1 -> {
                saveLessWeightItemModel(
                    binding.txtLabelAddWeightOne.text.toString(),
                    binding.txtLessWtAddWeightOne.text.toString()
                )
            }
            2 -> {

                saveLessWeightItemModel(
                    binding.txtLabelAddWeightOne.text.toString(),
                    binding.txtLessWtAddWeightOne.text.toString()
                )

                saveLessWeightItemModel(
                    binding.txtLabelAddWeightTwo.text.toString(),
                    binding.txtLessWtAddWeightTwo.text.toString()
                )
            }
            3 -> {
                saveLessWeightItemModel(
                    binding.txtLabelAddWeightOne.text.toString(),
                    binding.txtLessWtAddWeightOne.text.toString()
                )

                saveLessWeightItemModel(
                    binding.txtLabelAddWeightTwo.text.toString(),
                    binding.txtLessWtAddWeightTwo.text.toString()
                )

                saveLessWeightItemModel(
                    binding.txtLabelAddWeightThree.text.toString(),
                    binding.txtLessWtAddWeightThree.text.toString()
                )
            }
            4 -> {
                saveLessWeightItemModel(
                    binding.txtLabelAddWeightOne.text.toString(),
                    binding.txtLessWtAddWeightOne.text.toString()
                )

                saveLessWeightItemModel(
                    binding.txtLabelAddWeightTwo.text.toString(),
                    binding.txtLessWtAddWeightTwo.text.toString()
                )

                saveLessWeightItemModel(
                    binding.txtLabelAddWeightThree.text.toString(),
                    binding.txtLessWtAddWeightThree.text.toString()
                )
                saveLessWeightItemModel(
                    binding.txtLabelAddWeightFour.text.toString(),
                    binding.txtLessWtAddWeightFour.text.toString()
                )
            }
            5 -> {
                saveLessWeightItemModel(
                    binding.txtLabelAddWeightOne.text.toString(),
                    binding.txtLessWtAddWeightOne.text.toString()
                )

                saveLessWeightItemModel(
                    binding.txtLabelAddWeightTwo.text.toString(),
                    binding.txtLessWtAddWeightTwo.text.toString()
                )

                saveLessWeightItemModel(
                    binding.txtLabelAddWeightThree.text.toString(),
                    binding.txtLessWtAddWeightThree.text.toString()
                )
                saveLessWeightItemModel(
                    binding.txtLabelAddWeightFour.text.toString(),
                    binding.txtLessWtAddWeightFour.text.toString()
                )
                saveLessWeightItemModel(
                    binding.txtLabelAddWeightFive.text.toString(),
                    binding.txtLessWtAddWeightFive.text.toString()
                )
            }
        }

    }


    private fun updateUIAfterRemove(is_From_Position: Int, is_From_Pref: Boolean) {
        when (is_From_Pref) {
            true -> {
                //count = addlessweightListPref.size
                when (is_From_Position) {
                    1 -> {
                        when (count) {
                            1 -> {
                                binding.txtLessWtAddWeightOne.text!!.clear()
                                binding.txtLabelAddWeightOne.text!!.clear()
                                binding.tvAddChargeAddItem.visibility = View.VISIBLE
                            }
                            2 -> {
                                binding.txtLessWtAddWeightOne.setText(addlessweightListPref.get(1).less_wt_final_wt)
                                binding.txtLabelAddWeightOne.setText(addlessweightListPref.get(1).label)

                                binding.cardlessWeightTwo.visibility = View.GONE
                                binding.tvAddChargeAddItem.visibility = View.VISIBLE
                                binding.txtLessWtAddWeightThree.text!!.clear()
                                binding.txtLabelAddWeightThree.text!!.clear()
                                count = count - 1
                            }
                            3 -> {
                                binding.txtLessWtAddWeightOne.setText(addlessweightListPref.get(1).less_wt_final_wt)
                                binding.txtLabelAddWeightOne.setText(addlessweightListPref.get(1).label)

                                binding.txtLessWtAddWeightTwo.setText(addlessweightListPref.get(2).less_wt_final_wt)
                                binding.txtLabelAddWeightTwo.setText(addlessweightListPref.get(2).label)

                                binding.cardlessWeightThree.visibility = View.GONE
                                binding.tvAddChargeAddItem.visibility = View.VISIBLE

                                binding.txtLessWtAddWeightThree.text!!.clear()
                                binding.txtLabelAddWeightThree.text!!.clear()
                                count = count - 1

                            }
                            4 -> {
                                binding.txtLessWtAddWeightOne.setText(addlessweightListPref.get(1).less_wt_final_wt)
                                binding.txtLabelAddWeightOne.setText(addlessweightListPref.get(1).label)

                                binding.txtLessWtAddWeightTwo.setText(addlessweightListPref.get(2).less_wt_final_wt)
                                binding.txtLabelAddWeightTwo.setText(addlessweightListPref.get(2).label)

                                binding.txtLessWtAddWeightThree.setText(addlessweightListPref.get(3).less_wt_final_wt)
                                binding.txtLabelAddWeightThree.setText(addlessweightListPref.get(3).label)


                                binding.cardlessWeightFour.visibility = View.GONE
                                binding.cardlessWeightFive.visibility = View.GONE
                                binding.tvAddChargeAddItem.visibility = View.VISIBLE

                                binding.txtLessWtAddWeightFour.text!!.clear()
                                binding.txtLabelAddWeightFour.text!!.clear()
                                count = count - 1

                            }
                            5 -> {
                                binding.txtLessWtAddWeightOne.setText(addlessweightListPref.get(1).less_wt_final_wt)
                                binding.txtLabelAddWeightOne.setText(addlessweightListPref.get(1).label)

                                binding.txtLessWtAddWeightTwo.setText(addlessweightListPref.get(2).less_wt_final_wt)
                                binding.txtLabelAddWeightTwo.setText(addlessweightListPref.get(2).label)

                                binding.txtLessWtAddWeightThree.setText(addlessweightListPref.get(3).less_wt_final_wt)
                                binding.txtLabelAddWeightThree.setText(addlessweightListPref.get(3).label)

                                binding.txtLessWtAddWeightFour.setText(addlessweightListPref.get(4).less_wt_final_wt)
                                binding.txtLabelAddWeightFour.setText(addlessweightListPref.get(4).label)

                                binding.cardlessWeightFive.visibility = View.GONE
                                binding.tvAddChargeAddItem.visibility = View.VISIBLE
                                binding.txtLessWtAddWeightFive.text!!.clear()
                                binding.txtLabelAddWeightFive.text!!.clear()
                                count = count - 1

                            }
                        }

                    }
                    2 -> {
                        when (count) {
                            2 -> {
                                binding.cardlessWeightTwo.visibility = View.GONE
                                binding.tvAddChargeAddItem.visibility = View.VISIBLE
                                binding.txtLessWtAddWeightTwo.text!!.clear()
                                binding.txtLabelAddWeightTwo.text!!.clear()
                                count = count - 1

                            }
                            3 -> {
                                binding.txtLessWtAddWeightTwo.setText(addlessweightListPref.get(2).less_wt_final_wt)
                                binding.txtLabelAddWeightTwo.setText(addlessweightListPref.get(2).label)

                                binding.cardlessWeightThree.visibility = View.GONE
                                binding.cardlessWeightFour.visibility = View.GONE
                                binding.cardlessWeightFive.visibility = View.GONE
                                binding.tvAddChargeAddItem.visibility = View.VISIBLE

                                binding.txtLessWtAddWeightThree.text!!.clear()
                                binding.txtLabelAddWeightThree.text!!.clear()
                                count = count - 1
                            }
                            4 -> {

                                binding.txtLessWtAddWeightTwo.setText(addlessweightListPref.get(2).less_wt_final_wt)
                                binding.txtLabelAddWeightTwo.setText(addlessweightListPref.get(2).label)

                                binding.txtLessWtAddWeightThree.setText(addlessweightListPref.get(3).less_wt_final_wt)
                                binding.txtLabelAddWeightThree.setText(addlessweightListPref.get(3).label)

                                binding.cardlessWeightFour.visibility = View.GONE
                                binding.cardlessWeightFive.visibility = View.GONE
                                binding.tvAddChargeAddItem.visibility = View.VISIBLE
                                binding.txtLessWtAddWeightFour.text!!.clear()
                                binding.txtLabelAddWeightFour.text!!.clear()
                                count = count - 1
                            }
                            5 -> {
                                binding.txtLessWtAddWeightTwo.setText(addlessweightListPref.get(2).less_wt_final_wt)
                                binding.txtLabelAddWeightTwo.setText(addlessweightListPref.get(2).label)

                                binding.txtLessWtAddWeightThree.setText(addlessweightListPref.get(3).less_wt_final_wt)
                                binding.txtLabelAddWeightThree.setText(addlessweightListPref.get(3).label)

                                binding.txtLessWtAddWeightFour.setText(addlessweightListPref.get(4).less_wt_final_wt)
                                binding.txtLabelAddWeightFour.setText(addlessweightListPref.get(4).label)

                                binding.cardlessWeightFive.visibility = View.GONE
                                binding.tvAddChargeAddItem.visibility = View.VISIBLE
                                binding.txtLessWtAddWeightFive.text!!.clear()
                                binding.txtLabelAddWeightFive.text!!.clear()
                                count = count - 1

                            }
                        }
                    }
                    3 -> {
                        when (count) {
                            3 -> {
                                binding.cardlessWeightThree.visibility = View.GONE
                                binding.tvAddChargeAddItem.visibility = View.VISIBLE
                                binding.txtLessWtAddWeightThree.text!!.clear()
                                binding.txtLabelAddWeightThree.text!!.clear()
                                count = count - 1
                            }
                            4 -> {
                                binding.txtLessWtAddWeightThree.setText(addlessweightListPref.get(3).less_wt_final_wt)
                                binding.txtLabelAddWeightThree.setText(addlessweightListPref.get(3).label)


                                binding.cardlessWeightFour.visibility = View.GONE
                                binding.cardlessWeightFive.visibility = View.GONE
                                binding.tvAddChargeAddItem.visibility = View.VISIBLE
                                binding.txtLessWtAddWeightFour.text!!.clear()
                                binding.txtLabelAddWeightFour.text!!.clear()
                                count = count - 1
                            }
                            5 -> {
                                binding.txtLessWtAddWeightTwo.setText(addlessweightListPref.get(2).less_wt_final_wt)
                                binding.txtLabelAddWeightTwo.setText(addlessweightListPref.get(2).label)

                                binding.txtLessWtAddWeightThree.setText(addlessweightListPref.get(3).less_wt_final_wt)
                                binding.txtLabelAddWeightThree.setText(addlessweightListPref.get(3).label)

                                binding.txtLessWtAddWeightFour.setText(addlessweightListPref.get(4).less_wt_final_wt)
                                binding.txtLabelAddWeightFour.setText(addlessweightListPref.get(4).label)

                                binding.cardlessWeightFive.visibility = View.GONE
                                binding.tvAddChargeAddItem.visibility = View.VISIBLE
                                binding.txtLessWtAddWeightFive.text!!.clear()
                                binding.txtLabelAddWeightFive.text!!.clear()
                                count = count - 1

                            }
                        }

                    }
                    4 -> {
                        when (count) {
                            4 -> {
                                binding.cardlessWeightFour.visibility = View.GONE
                                binding.tvAddChargeAddItem.visibility = View.VISIBLE
                                binding.txtLessWtAddWeightFour.text!!.clear()
                                binding.txtLabelAddWeightFour.text!!.clear()
                                count = count - 1
                            }
                            5 -> {
                                binding.txtLessWtAddWeightFour.setText(addlessweightListPref.get(4).less_wt_final_wt)
                                binding.txtLabelAddWeightFour.setText(addlessweightListPref.get(4).label)

                                binding.cardlessWeightFive.visibility = View.GONE
                                binding.tvAddChargeAddItem.visibility = View.VISIBLE
                                binding.txtLessWtAddWeightFour.text!!.clear()
                                binding.txtLabelAddWeightFour.text!!.clear()
                                count = count - 1
                            }
                        }
                    }
                    5 -> {
                        when (count) {
                            5 -> {
                                binding.cardlessWeightFive.visibility = View.GONE
                                binding.tvAddChargeAddItem.visibility = View.VISIBLE
                                binding.txtLessWtAddWeightFive.text!!.clear()
                                binding.txtLabelAddWeightFive.text!!.clear()
                                count = count - 1
                            }
                        }
                    }

                }

            }
            false -> {
                when (is_From_Position) {
                    1 -> {
                        when (count) {
                            1 -> {
                                binding.txtLessWtAddWeightOne.text!!.clear()
                                binding.txtLabelAddWeightOne.text!!.clear()
                            }
                            2 -> {
                                binding.txtLessWtAddWeightOne.setText(binding.txtLessWtAddWeightTwo.text.toString())
                                binding.txtLabelAddWeightOne.setText(binding.txtLabelAddWeightTwo.text.toString())

                                binding.cardlessWeightTwo.visibility = View.GONE
                                binding.txtLessWtAddWeightTwo.text!!.clear()
                                binding.txtLabelAddWeightTwo.text!!.clear()
                                count = count - 1
                            }
                            3 -> {
                                binding.txtLessWtAddWeightOne.setText(binding.txtLessWtAddWeightTwo.text.toString())
                                binding.txtLabelAddWeightOne.setText(binding.txtLabelAddWeightTwo.text.toString())

                                binding.txtLessWtAddWeightTwo.setText(binding.txtLessWtAddWeightThree.text.toString())
                                binding.txtLabelAddWeightTwo.setText(binding.txtLabelAddWeightThree.text.toString())

                                binding.cardlessWeightThree.visibility = View.GONE
                                binding.cardlessWeightFour.visibility = View.GONE
                                binding.cardlessWeightFive.visibility = View.GONE

                                binding.txtLessWtAddWeightThree.text!!.clear()
                                binding.txtLabelAddWeightThree.text!!.clear()
                                count = count - 1
                            }
                            4 -> {
                                binding.txtLessWtAddWeightOne.setText(binding.txtLessWtAddWeightTwo.text.toString())
                                binding.txtLabelAddWeightOne.setText(binding.txtLabelAddWeightTwo.text.toString())

                                binding.txtLessWtAddWeightTwo.setText(binding.txtLessWtAddWeightThree.text.toString())
                                binding.txtLabelAddWeightTwo.setText(binding.txtLabelAddWeightThree.text.toString())

                                binding.txtLessWtAddWeightThree.setText(binding.txtLessWtAddWeightFour.text.toString())
                                binding.txtLabelAddWeightThree.setText(binding.txtLabelAddWeightFour.text.toString())


                                binding.cardlessWeightFour.visibility = View.GONE
                                binding.cardlessWeightFive.visibility = View.GONE

                                binding.txtLessWtAddWeightFour.text!!.clear()
                                binding.txtLabelAddWeightFour.text!!.clear()
                                count = count - 1
                            }
                            5 -> {
                                binding.txtLessWtAddWeightOne.setText(binding.txtLessWtAddWeightTwo.text.toString())
                                binding.txtLabelAddWeightOne.setText(binding.txtLabelAddWeightTwo.text.toString())

                                binding.txtLessWtAddWeightTwo.setText(binding.txtLessWtAddWeightThree.text.toString())
                                binding.txtLabelAddWeightTwo.setText(binding.txtLabelAddWeightThree.text.toString())

                                binding.txtLessWtAddWeightThree.setText(binding.txtLessWtAddWeightFour.text.toString())
                                binding.txtLabelAddWeightThree.setText(binding.txtLabelAddWeightFour.text.toString())

                                binding.txtLessWtAddWeightFour.setText(binding.txtLessWtAddWeightFive.text.toString())
                                binding.txtLabelAddWeightFour.setText(binding.txtLabelAddWeightFive.text.toString())

                                binding.cardlessWeightFive.visibility = View.GONE
                                binding.txtLessWtAddWeightFive.text!!.clear()
                                binding.txtLabelAddWeightFive.text!!.clear()
                                count = count - 1
                                binding.tvAddChargeAddItem.visibility = View.VISIBLE

                            }
                        }
                    }
                    2 -> {
                        when (count) {
                            2 -> {
                                binding.cardlessWeightTwo.visibility = View.GONE
                                binding.txtLessWtAddWeightTwo.text!!.clear()
                                binding.txtLabelAddWeightTwo.text!!.clear()
                                count = count - 1

                            }
                            3 -> {
                                binding.txtLessWtAddWeightTwo.setText(binding.txtLessWtAddWeightThree.text.toString())
                                binding.txtLabelAddWeightTwo.setText(binding.txtLabelAddWeightThree.text.toString())

                                binding.cardlessWeightThree.visibility = View.GONE
                                binding.cardlessWeightFour.visibility = View.GONE
                                binding.cardlessWeightFive.visibility = View.GONE

                                binding.txtLessWtAddWeightThree.text!!.clear()
                                binding.txtLabelAddWeightThree.text!!.clear()
                                count = count - 1

                            }
                            4 -> {
                                binding.txtLessWtAddWeightTwo.setText(binding.txtLessWtAddWeightThree.text.toString())
                                binding.txtLabelAddWeightTwo.setText(binding.txtLabelAddWeightThree.text.toString())

                                binding.txtLessWtAddWeightThree.setText(binding.txtLessWtAddWeightFour.text.toString())
                                binding.txtLabelAddWeightThree.setText(binding.txtLabelAddWeightFour.text.toString())

                                binding.cardlessWeightFour.visibility = View.GONE
                                binding.cardlessWeightFive.visibility = View.GONE
                                binding.txtLessWtAddWeightFour.text!!.clear()
                                binding.txtLabelAddWeightFour.text!!.clear()
                                count = count - 1
                            }
                            5 -> {
                                binding.txtLessWtAddWeightTwo.setText(binding.txtLessWtAddWeightThree.text.toString())
                                binding.txtLabelAddWeightTwo.setText(binding.txtLabelAddWeightThree.text.toString())

                                binding.txtLessWtAddWeightThree.setText(binding.txtLessWtAddWeightFour.text.toString())
                                binding.txtLabelAddWeightThree.setText(binding.txtLabelAddWeightFour.text.toString())

                                binding.txtLessWtAddWeightFour.setText(binding.txtLessWtAddWeightFive.text.toString())
                                binding.txtLabelAddWeightFour.setText(binding.txtLabelAddWeightFive.text.toString())

                                binding.cardlessWeightFive.visibility = View.GONE
                                binding.txtLessWtAddWeightFive.text!!.clear()
                                binding.txtLabelAddWeightFive.text!!.clear()
                                count = count - 1
                                binding.tvAddChargeAddItem.visibility = View.VISIBLE
                            }
                        }

                    }
                    3 -> {
                        when (count) {
                            3 -> {
                                binding.cardlessWeightThree.visibility = View.GONE
                                binding.txtLessWtAddWeightThree.text!!.clear()
                                binding.txtLabelAddWeightThree.text!!.clear()
                                count = count - 1
                            }
                            4 -> {

                                binding.txtLessWtAddWeightThree.setText(binding.txtLessWtAddWeightFour.text.toString())
                                binding.txtLabelAddWeightThree.setText(binding.txtLabelAddWeightFour.text.toString())

                                binding.txtLessWtAddWeightFour.setText(binding.txtLessWtAddWeightFive.text.toString())
                                binding.txtLabelAddWeightFour.setText(binding.txtLabelAddWeightFive.text.toString())

                                binding.cardlessWeightFour.visibility = View.GONE
                                binding.txtLessWtAddWeightFour.text!!.clear()
                                binding.txtLabelAddWeightFour.text!!.clear()
                                count = count - 1
                            }
                            5 -> {
                                binding.txtLessWtAddWeightTwo.setText(binding.txtLessWtAddWeightThree.text.toString())
                                binding.txtLabelAddWeightTwo.setText(binding.txtLabelAddWeightThree.text.toString())

                                binding.txtLessWtAddWeightThree.setText(binding.txtLessWtAddWeightFour.text.toString())
                                binding.txtLabelAddWeightThree.setText(binding.txtLabelAddWeightFour.text.toString())

                                binding.txtLessWtAddWeightFour.setText(binding.txtLessWtAddWeightFive.text.toString())
                                binding.txtLabelAddWeightFour.setText(binding.txtLabelAddWeightFive.text.toString())

                                binding.cardlessWeightFive.visibility = View.GONE
                                binding.txtLessWtAddWeightFive.text!!.clear()
                                binding.txtLabelAddWeightFive.text!!.clear()
                                count = count - 1
                                binding.tvAddChargeAddItem.visibility = View.VISIBLE

                            }
                        }

                    }
                    4 -> {
                        when (count) {
                            4 -> {
                                binding.cardlessWeightFour.visibility = View.GONE
                                binding.txtLessWtAddWeightFour.text!!.clear()
                                binding.txtLabelAddWeightFour.text!!.clear()
                                count = count - 1
                            }
                            5 -> {
                                binding.txtLessWtAddWeightFour.setText(binding.txtLessWtAddWeightFive.text.toString())
                                binding.txtLabelAddWeightFour.setText(binding.txtLabelAddWeightFive.text.toString())

                                binding.cardlessWeightFive.visibility = View.GONE
                                binding.txtLessWtAddWeightFour.text!!.clear()
                                binding.txtLabelAddWeightFour.text!!.clear()
                                count = count - 1
                                binding.tvAddChargeAddItem.visibility = View.VISIBLE
                            }
                        }

                    }
                    5 -> {
                        when (count) {
                            5 -> {
                                binding.tvAddChargeAddItem.visibility = View.VISIBLE
                                binding.cardlessWeightFive.visibility = View.GONE
                                binding.txtLessWtAddWeightFive.text!!.clear()
                                binding.txtLabelAddWeightFive.text!!.clear()
                                count = count - 1
                            }
                        }

                    }
                }
            }
        }

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


        val childModel = OpeningStockItemModel.OpeningStockItemModelItem.ItemWtBreakup(
            addlessweightList.size.toString(),
            addlessweightList,
            totalOtherChrgsUpdatedValue,
            ""
        )


        /* if (new_lesswtBreakup_pos >= 0 && new_lesswtBreakup_pos != -1) {
             // Update selected item
             lessweightBreakupList.set(new_lesswtBreakup_pos, childModel)
         } else {
             // Add new item
             lessweightBreakupList.add(childModel)
         }
 */


        prefs[Constants.PREF_LESS_WEIGHT_BREAKUP_INFO_KEY] = Gson().toJson(childModel)


    }


    private fun saveLessWeightItemModel(label: String, less_wt_final_wt: String) {
        /* if (prefs.contains(Constants.PREF_LESS_WEIGHT_INFO_KEY)) {
             val collectionType = object :
                 TypeToken<java.util.ArrayList<AddLessWeightModel.AddLessWeightModelItem>>() {}.type
             var lessweightList: ArrayList<AddLessWeightModel.AddLessWeightModelItem> =
                 Gson().fromJson(prefs[Constants.PREF_LESS_WEIGHT_INFO_KEY, ""], collectionType)
             addlessweightList.addAll(lessweightList)
         } else {
             addlessweightList = ArrayList()
         }*/


        val childModel = AddLessWeightModel.AddLessWeightModelItem(
            label, less_wt_final_wt, "", "", "",
            "", "", "", "", "",
            "", "", less_wt_final_wt, "", "",
            "0.00", unitArrayList, "", "", "", ""

        )


        if (new_lessweight_pos >= 0 && new_lessweight_pos != -1) {
            // Update selected item
            addlessweightList.set(new_lessweight_pos, childModel)
        } else {
            // Add new item
            addlessweightList.add(childModel)
        }


        val prefs = PreferenceHelper.defaultPrefs(this)
        prefs[Constants.PREF_LESS_WEIGHT_INFO_KEY] = Gson().toJson(addlessweightList)

    }

    private fun updateTotalChargeAfterRemove(lessWtRemoveValue: String, totlChargeValue: String) {
        val totalothercharge1: BigDecimal = totlChargeValue.toBigDecimal()
        val othercharge2: BigDecimal = lessWtRemoveValue.toBigDecimal()

        var totalOtherChrgs: String =
            (totalothercharge1.setScale(3))
                .minus((othercharge2.setScale(3)))
                .setScale(3, RoundingMode.CEILING).toString()

        totalOtherChrgsUpdatedValue = totalOtherChrgs
        binding.tvTotalChargeAddItem.setText(totalOtherChrgsUpdatedValue)
    }

    private fun applyingDigitFilter() {
        binding.txtLessWtAddWeightOne.setFilters(
            arrayOf<InputFilter>(
                DigitsInputFilter(
                    9,
                    3,
                    999999999.999
                )
            )
        )

        binding.txtLessWtAddWeightTwo.setFilters(
            arrayOf<InputFilter>(
                DigitsInputFilter(
                    9,
                    3,
                    999999999.999
                )
            )
        )

        binding.txtLessWtAddWeightThree.setFilters(
            arrayOf<InputFilter>(
                DigitsInputFilter(
                    9,
                    3,
                    999999999.999
                )
            )
        )

        binding.txtLessWtAddWeightFour.setFilters(
            arrayOf<InputFilter>(
                DigitsInputFilter(
                    9,
                    3,
                    999999999.999
                )
            )
        )

        binding.txtLessWtAddWeightFive.setFilters(
            arrayOf<InputFilter>(
                DigitsInputFilter(
                    9,
                    3,
                    999999999.999
                )
            )
        )
    }

    private fun onTextChangeSetup() {
        binding.txtLessWtAddWeightOne.doAfterTextChanged {

            val str: String = txtLessWtAddWeightOne.text.toString()

            if (str.isEmpty()) return@doAfterTextChanged
            val str2: String = CommonUtils.perfectDecimal(str, 9, 3).toString()
            if (!str2.equals(str)) {
                //val str3:String = df.format(str2.toDouble())
                binding.txtLessWtAddWeightOne.setText(str2)
                binding.txtLessWtAddWeightOne.setSelection(str2.length)
            }

            lessWeightValueOne = df.format(str2.toDouble())

        }
        binding.txtLessWtAddWeightTwo.doAfterTextChanged {

            val str: String = txtLessWtAddWeightTwo.text.toString()

            if (str.isEmpty()) return@doAfterTextChanged
            val str2: String = CommonUtils.perfectDecimal(str, 9, 3).toString()
            if (!str2.equals(str)) {
                //val str3:String = df.format(str2.toDouble())
                binding.txtLessWtAddWeightTwo.setText(str2)
                binding.txtLessWtAddWeightTwo.setSelection(str2.length)
            }

            lessWeightValueTwo = df.format(str2.toDouble())

        }
        binding.txtLessWtAddWeightThree.doAfterTextChanged {

            val str: String = txtLessWtAddWeightThree.text.toString()

            if (str.isEmpty()) return@doAfterTextChanged
            val str2: String = CommonUtils.perfectDecimal(str, 9, 3).toString()
            if (!str2.equals(str)) {
                //val str3:String = df.format(str2.toDouble())
                binding.txtLessWtAddWeightThree.setText(str2)
                binding.txtLessWtAddWeightThree.setSelection(str2.length)
            }

            lessWeightValueThree = df.format(str2.toDouble())

        }
        binding.txtLessWtAddWeightFour.doAfterTextChanged {

            val str: String = txtLessWtAddWeightFour.text.toString()

            if (str.isEmpty()) return@doAfterTextChanged
            val str2: String = CommonUtils.perfectDecimal(str, 9, 3).toString()
            if (!str2.equals(str)) {
                //val str3:String = df.format(str2.toDouble())
                binding.txtLessWtAddWeightFour.setText(str2)
                binding.txtLessWtAddWeightFour.setSelection(str2.length)
            }

            lessWeightValueFour = df.format(str2.toDouble())

        }
        binding.txtLessWtAddWeightFive.doAfterTextChanged {

            val str: String = txtLessWtAddWeightFive.text.toString()

            if (str.isEmpty()) return@doAfterTextChanged
            val str2: String = CommonUtils.perfectDecimal(str, 9, 3).toString()
            if (!str2.equals(str)) {
                //val str3:String = df.format(str2.toDouble())
                binding.txtLessWtAddWeightFive.setText(str2)
                binding.txtLessWtAddWeightFive.setSelection(str2.length)
            }

            lessWeightValueFive = df.format(str2.toDouble())

        }

    }


    private fun onFocusChangeSetup() {

        binding.txtLessWtAddWeightOne.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                when (txtLessWtAddWeightOne.text.isNullOrBlank()) {
                    true -> {
                        lessWeightValueOne = "0.000"
                        binding.txtLessWtAddWeightOne.setText(lessWeightValueOne)
                        binding.txtLessWtAddWeightOne.setSelection(lessWeightValueOne.length)

                    }
                    else -> {
                        when (lessWeightValueOne.toBigDecimal().compareTo(BigDecimal.ZERO) == 0) {
                            true -> {
                                binding.txtLessWtAddWeightOne.setText(lessWeightValueOne)
                                updateTotalCharges()
                                //  Toast.makeText(this, getString(R.string.lesswt_st_weight_error_msg), Toast.LENGTH_SHORT).show()
                            }
                            false -> {

                                binding.txtLessWtAddWeightOne.setText(lessWeightValueOne)
                                updateTotalCharges()

                            }
                        }
                    }
                }
            }
        }

        binding.txtLessWtAddWeightTwo.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                when (txtLessWtAddWeightTwo.text.isNullOrBlank()) {
                    true -> {
                        lessWeightValueTwo = "0.000"
                        binding.txtLessWtAddWeightTwo.setText(lessWeightValueTwo)
                        binding.txtLessWtAddWeightTwo.setSelection(lessWeightValueTwo.length)

                    }
                    else -> {
                        when (lessWeightValueTwo.toBigDecimal().compareTo(BigDecimal.ZERO) == 0) {
                            true -> {
                                //  Toast.makeText(this, getString(R.string.lesswt_st_weight_error_msg), Toast.LENGTH_SHORT).show()
                            }
                            false -> {

                                binding.txtLessWtAddWeightTwo.setText(lessWeightValueTwo)
                                updateTotalCharges()

                            }
                        }
                    }
                }
            }
        }
        binding.txtLessWtAddWeightThree.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                when (txtLessWtAddWeightThree.text.isNullOrBlank()) {
                    true -> {
                        lessWeightValueThree = "0.000"
                        binding.txtLessWtAddWeightThree.setText(lessWeightValueThree)
                        binding.txtLessWtAddWeightThree.setSelection(lessWeightValueThree.length)

                    }
                    else -> {
                        when (lessWeightValueThree.toBigDecimal().compareTo(BigDecimal.ZERO) == 0) {
                            true -> {
                                //  Toast.makeText(this, getString(R.string.lesswt_st_weight_error_msg), Toast.LENGTH_SHORT).show()
                            }
                            false -> {

                                binding.txtLessWtAddWeightThree.setText(lessWeightValueThree)
                                updateTotalCharges()

                            }
                        }
                    }
                }
            }
        }
        binding.txtLessWtAddWeightFour.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                when (txtLessWtAddWeightFour.text.isNullOrBlank()) {
                    true -> {
                        lessWeightValueFour = "0.000"
                        binding.txtLessWtAddWeightFour.setText(lessWeightValueFour)
                        binding.txtLessWtAddWeightFour.setSelection(lessWeightValueFour.length)

                    }
                    else -> {
                        when (lessWeightValueFour.toBigDecimal().compareTo(BigDecimal.ZERO) == 0) {
                            true -> {
                                //  Toast.makeText(this, getString(R.string.lesswt_st_weight_error_msg), Toast.LENGTH_SHORT).show()
                            }
                            false -> {

                                binding.txtLessWtAddWeightFour.setText(lessWeightValueFour)
                                updateTotalCharges()

                            }
                        }
                    }
                }
            }
        }
        binding.txtLessWtAddWeightFive.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                when (txtLessWtAddWeightFive.text.isNullOrBlank()) {
                    true -> {
                        lessWeightValueFive = "0.000"
                        binding.txtLessWtAddWeightFive.setText(lessWeightValueFive)
                        binding.txtLessWtAddWeightFive.setSelection(lessWeightValueFive.length)

                    }
                    else -> {
                        when (lessWeightValueFive.toBigDecimal().compareTo(BigDecimal.ZERO) == 0) {
                            true -> {
                                //  Toast.makeText(this, getString(R.string.lesswt_st_weight_error_msg), Toast.LENGTH_SHORT).show()
                            }
                            false -> {

                                binding.txtLessWtAddWeightFive.setText(lessWeightValueFive)
                                updateTotalCharges()

                            }
                        }
                    }
                }
            }
        }

    }

    private fun updateTotalCharges() {
        val othercharge1: BigDecimal = lessWeightValueOne.toBigDecimal()
        val othercharge2: BigDecimal = lessWeightValueTwo.toBigDecimal()
        val othercharge3: BigDecimal = lessWeightValueThree.toBigDecimal()
        val othercharge4: BigDecimal = lessWeightValueFour.toBigDecimal()
        val othercharge5: BigDecimal = lessWeightValueFive.toBigDecimal()

        var totalOtherChrgs: String =
            (othercharge1.setScale(3))
                .plus(
                    (othercharge2.setScale(3))
                        .plus((othercharge3.setScale(3)))
                        .plus((othercharge4.setScale(3)))
                        .plus((othercharge5.setScale(3)))
                ).setScale(3, RoundingMode.CEILING).toString()

        totalOtherChrgsUpdatedValue = totalOtherChrgs
        binding.tvTotalChargeAddItem.setText(totalOtherChrgsUpdatedValue)
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


}
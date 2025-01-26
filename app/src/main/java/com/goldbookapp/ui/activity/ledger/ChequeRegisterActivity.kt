package com.goldbookapp.ui.activity.ledger

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.goldbookapp.R
import com.goldbookapp.databinding.ActivityChequeRegisterBinding
import com.goldbookapp.model.AddChequeBookModel
import com.goldbookapp.model.LoginModel
import com.goldbookapp.ui.adapter.ChequeRegisterAdapter
import com.goldbookapp.utils.CommonUtils
import com.goldbookapp.utils.CommonUtils.Companion.clickWithDebounce
import com.goldbookapp.utils.Constants
import com.goldbookapp.utils.PreferenceHelper
import com.goldbookapp.utils.PreferenceHelper.get
import com.goldbookapp.utils.PreferenceHelper.set
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hamzasharuf.networkmonitor.ConnectivityStateHolder
import com.hamzasharuf.networkmonitor.Event
import com.hamzasharuf.networkmonitor.NetworkEvents
import kotlinx.android.synthetic.main.activity_cheque_register.*
import kotlinx.android.synthetic.main.toolbar_layout.*

class ChequeRegisterActivity : AppCompatActivity() {

    lateinit var binding: ActivityChequeRegisterBinding
    lateinit var prefs: SharedPreferences
    lateinit var loginModel: LoginModel
    private var currentPage: Int = Constants.PAGE_START
    lateinit var chequeList: ArrayList<AddChequeBookModel.AddChequeBookModelItem>
    lateinit var chequeEditList: ArrayList<AddChequeBookModel.AddChequeBookModelItem>
    private lateinit var adapter: ChequeRegisterAdapter
    var is_from_new_cheque: String? = "1"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_cheque_register)
        setupUIandListner()
    }

    private fun setupUIandListner() {

        prefs = PreferenceHelper.defaultPrefs(this)
        loginModel = Gson().fromJson(
            prefs[Constants.PREF_LOGIN_DETAIL_KEY, ""],
            LoginModel::class.java
        )



        imgLeft.setImageResource(R.drawable.ic_back)
        tvTitle.setText(R.string.cheque_register)



        imgLeft?.clickWithDebounce {
            finish()
        }


        cardChequeBook?.clickWithDebounce {
            when (is_from_new_cheque) {
                "0" -> {
                    startActivity(
                        Intent(this, NewChequeBookActivity::class.java).putExtra(
                            Constants.CHEQUE_SAVE_TYPE, "1"
                        )
                            .putExtra(Constants.CHEQUE_SAVE_FROM_ADD_EDIT, "1")
                    )
                }
                "1" -> {
                    startActivity(
                        Intent(this, NewChequeBookActivity::class.java).putExtra(
                            Constants.CHEQUE_SAVE_TYPE,
                            "1"
                        )
                            .putExtra(Constants.CHEQUE_SAVE_FROM_ADD_EDIT, "2")
                    )
                }
            }
        }


        if (intent.extras?.containsKey(Constants.IS_FROM_NEW_CHEQUE)!!) {
            is_from_new_cheque = intent.getStringExtra(Constants.IS_FROM_NEW_CHEQUE)

            if (is_from_new_cheque.equals("1", true)) {
                if (prefs.contains(Constants.PREF_CHEQUE_BOOK_KEY)) {
                    // prefs.edit().remove(Constants.PREF_CHEQUE_BOOK_KEY).apply()
                    val collectionType =
                        object :
                            TypeToken<ArrayList<AddChequeBookModel.AddChequeBookModelItem>>() {}.type
                    chequeList =
                        Gson().fromJson(
                            prefs[Constants.PREF_CHEQUE_BOOK_KEY, ""],
                            collectionType
                        )

                    val chequeArray: String = Gson().toJson(chequeList)

                    if (chequeList != null) {
                        ly_cheque_details.visibility = View.VISIBLE
                        recyclerViewChequeRegister.layoutManager = LinearLayoutManager(this)

                        adapter = ChequeRegisterAdapter(chequeList, true)
                        binding.recyclerViewChequeRegister.setHasFixedSize(true)
                        binding.recyclerViewChequeRegister.adapter = adapter

                    }
                }
            } else {
                if (prefs.contains(Constants.PREF_CHEQUE_BOOK_EDITKEY)) {
                    val collectionType =
                        object :
                            TypeToken<ArrayList<AddChequeBookModel.AddChequeBookModelItem>>() {}.type
                    chequeEditList =
                        Gson().fromJson(
                            prefs[Constants.PREF_CHEQUE_BOOK_EDITKEY, ""],
                            collectionType
                        )


                    val chequeArray: String = Gson().toJson(chequeEditList)

                    if (chequeEditList != null) {
                        ly_cheque_details.visibility = View.VISIBLE
                        recyclerViewChequeRegister.layoutManager = LinearLayoutManager(this)


                        adapter = ChequeRegisterAdapter(chequeEditList, true)
                        binding.recyclerViewChequeRegister.setHasFixedSize(true)
                        binding.recyclerViewChequeRegister.adapter = adapter

                    }

                }
            }
        }

    }


    override fun onResume() {
        super.onResume()
        NetworkEvents.observe(this, Observer {
            if (it is Event.ConnectivityEvent) {
                handleConnectivityChange()
            }
        })


        when (is_from_new_cheque) {
            "1" -> {
                if (prefs.contains(Constants.PREF_CHEQUE_BOOK_KEY)) {
                    val collectionType =
                        object :
                            TypeToken<ArrayList<AddChequeBookModel.AddChequeBookModelItem>>() {}.type
                    chequeList =
                        Gson().fromJson(prefs[Constants.PREF_CHEQUE_BOOK_KEY, ""], collectionType)


                    val chequeArray: String = Gson().toJson(chequeList)

                    if (chequeList != null) {
                        ly_cheque_details.visibility = View.VISIBLE
                        recyclerViewChequeRegister.layoutManager = LinearLayoutManager(this)
                        adapter = ChequeRegisterAdapter(chequeList, false)
                        binding.recyclerViewChequeRegister.setHasFixedSize(true)
                        binding.recyclerViewChequeRegister.adapter = adapter


                    }
                }
            }
            "0" -> {
                if (prefs.contains(Constants.PREF_CHEQUE_BOOK_EDITKEY)) {
                    val collectionType =
                        object :
                            TypeToken<ArrayList<AddChequeBookModel.AddChequeBookModelItem>>() {}.type
                    chequeEditList =
                        Gson().fromJson(
                            prefs[Constants.PREF_CHEQUE_BOOK_EDITKEY, ""],
                            collectionType
                        )


                    // val chequeArray: String = Gson().toJson(chequeEditList)

                    if (chequeEditList != null) {
                        ly_cheque_details.visibility = View.VISIBLE
                        recyclerViewChequeRegister.layoutManager = LinearLayoutManager(this)
                        adapter = ChequeRegisterAdapter(chequeEditList, true)
                        binding.recyclerViewChequeRegister.setHasFixedSize(true)
                        binding.recyclerViewChequeRegister.adapter = adapter

                    }
                }
            }
        }


    }
    private fun handleConnectivityChange() {
        if (ConnectivityStateHolder.isConnected)   {          // Network is available
            CommonUtils.hideInternetDialog()
            // userwise restric api call (for applying user restriction)

        }

        if (!ConnectivityStateHolder.isConnected) {
            // Network is not available
            CommonUtils.showDialog(this, getString(R.string.please_check_internet_msg))

        }
    }


    fun deleteCheque(position: Int, is_from_new: Boolean) {
        when (is_from_new) {
            //from add
            true -> {
                if (chequeList != null && chequeList!!.size > 0) {
                    if (position >= chequeList!!.size) {
                        //index not exists
                    } else {
                        // index exists


                        chequeList!!.removeAt(position)
                        adapter.notifyDataSetChanged()



                        if (chequeList!!.size > 0) {
                            prefs[Constants.PREF_CHEQUE_BOOK_KEY] = Gson().toJson(chequeList)
                            // invoiceCalculation()
                        } else {
                            prefs.edit().remove(Constants.PREF_CHEQUE_BOOK_KEY).apply()
                            //  linear_calculation_view_purchase.visibility = View.GONE
                        }
                    }
                }

            }
            //from edit
            false -> {
                if (chequeEditList != null && chequeEditList!!.size > 0) {
                    if (position >= chequeEditList!!.size) {
                        //index not exists
                    } else {
                        // index exists
                        chequeEditList!!.removeAt(position)
                        adapter.notifyDataSetChanged()

                        if (chequeEditList!!.size > 0) {
                            prefs[Constants.PREF_CHEQUE_BOOK_EDITKEY] =
                                Gson().toJson(chequeEditList)
                            // invoiceCalculation()
                        } else {
                            prefs.edit().remove(Constants.PREF_CHEQUE_BOOK_EDITKEY).apply()
                            //  linear_calculation_view_purchase.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }


}
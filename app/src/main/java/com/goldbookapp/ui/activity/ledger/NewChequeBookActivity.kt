package com.goldbookapp.ui.activity.ledger

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.goldbookapp.R
import com.goldbookapp.databinding.ActivityNewChequeBookBinding
import com.goldbookapp.model.AddChequeBookModel
import com.goldbookapp.model.LoginModel
import com.goldbookapp.utils.CommonUtils
import com.goldbookapp.utils.CommonUtils.Companion.clickWithDebounce
import com.goldbookapp.utils.Constants
import com.goldbookapp.utils.PreferenceHelper
import com.goldbookapp.utils.PreferenceHelper.get
import com.goldbookapp.utils.PreferenceHelper.set
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_new_cheque_book.*
import kotlinx.android.synthetic.main.toolbar_layout.*

class NewChequeBookActivity : AppCompatActivity() {

    lateinit var binding: ActivityNewChequeBookBinding
    lateinit var prefs: SharedPreferences
    lateinit var loginModel: LoginModel
    var totalcheque: Int? = 0
    var chequeSaveAdd: String? = "0"
    var chequeSaveAddEdit: String? = "0"
    lateinit var chequeSaveEditModel: AddChequeBookModel.AddChequeBookModelItem
    lateinit var chequeSaveNewModel: AddChequeBookModel.AddChequeBookModelItem

    private var edit_cheque_pos: Int = -1
    private var new_cheque_pos: Int = -1

    var addchequeList = ArrayList<AddChequeBookModel.AddChequeBookModelItem>()
    var editchequeList = ArrayList<AddChequeBookModel.AddChequeBookModelItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_new_cheque_book)
        setupUIandListner()
    }

    private fun setupUIandListner() {
        // add cheque
        if (intent.extras != null && intent.extras!!.containsKey(Constants.CHEQUE_SAVE_TYPE)) {
            chequeSaveAdd = intent.getStringExtra(Constants.CHEQUE_SAVE_TYPE)

            when (chequeSaveAdd) {
                "1" -> {
                    if (intent.extras != null && intent.extras!!.containsKey(Constants.CHEQUE_SAVE_FROM_ADD_EDIT)) {
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
                    }
                }
                //2->Edit Cheque From Edit
                "2" -> {
                    tvTitle.setText(R.string.edit_cheque_book)
                    if (intent.extras != null && intent.extras!!.containsKey(Constants.PREF_CHEQUE_BOOK_EDITKEY)) {
                        edit_cheque_pos = intent.getIntExtra(Constants.PREF_CHEQUE_BOOK_POS, 0)
                        val cheque_str: String? =
                            intent.getStringExtra(Constants.PREF_CHEQUE_BOOK_EDITKEY)
                        chequeSaveEditModel = Gson().fromJson(
                            cheque_str,
                            AddChequeBookModel.AddChequeBookModelItem::class.java
                        )
                        txtBookName.setText(chequeSaveEditModel.chequeName)
                        txtFromChequeBook.setText(chequeSaveEditModel.chequeFrom.toString())
                        txtToChequeBook.setText(chequeSaveEditModel.chequeTo.toString())
                        tvTotalCheque.setText(chequeSaveEditModel.totalCheque)


                    }
                }
                //Edit Cheque From New
                "3" -> {
                    tvTitle.setText(R.string.edit_cheque_book)
                    if (intent.extras != null && intent.extras!!.containsKey(Constants.PREF_CHEQUE_BOOK_KEY)) {
                        new_cheque_pos = intent.getIntExtra(Constants.PREF_CHEQUE_BOOK_POS, 0)

                        val cheque_str: String? =
                            intent.getStringExtra(Constants.PREF_CHEQUE_BOOK_KEY)
                        chequeSaveNewModel = Gson().fromJson(
                            cheque_str,
                            AddChequeBookModel.AddChequeBookModelItem::class.java
                        )
                        txtBookName.setText(chequeSaveNewModel.chequeName)
                        txtFromChequeBook.setText(chequeSaveNewModel.chequeFrom.toString())
                        txtToChequeBook.setText(chequeSaveNewModel.chequeTo.toString())
                        tvTotalCheque.setText(chequeSaveNewModel.totalCheque)


                    }
                }
            }
        }



        prefs = PreferenceHelper.defaultPrefs(this)
        loginModel = Gson().fromJson(
            prefs[Constants.PREF_LOGIN_DETAIL_KEY, ""],
            LoginModel::class.java
        )

        imgLeft.setImageResource(R.drawable.ic_back)
        tvTitle.setText(R.string.cheque_book)
        // tvRight.setText(R.string.save)


        imgLeft?.clickWithDebounce {
            onBackPressed()
        }

        if (txtFromChequeBook.text.toString().isEmpty() && txtToChequeBook.text.toString()
                .isEmpty()
        ) {
            tvTotalCheque.setText("0")
        }

        txtToChequeBook.setOnEditorActionListener { v, actionId, event ->
            return@setOnEditorActionListener when (actionId) {
                EditorInfo.IME_ACTION_DONE -> {
                    txtToChequeBook.clearFocus()

                    if (!txtFromChequeBook.text.toString().isNullOrBlank()) {
                        totalcheque =
                            txtToChequeBook.text.toString()
                                .toInt() - txtFromChequeBook.text.toString()
                                .toInt()
                        tvTotalCheque.setText(totalcheque.toString())
                        //hideKeyboardnew(this)
                    } else {
                        totalcheque = 0
                    }
                    true
                }
                else -> false
            }
        }


        btnSaveAdd_AddCheque?.clickWithDebounce {

            when (chequeSaveAdd) {
                "2" -> {
                    if (performValidation()) {
                        txtBookName.clearFocus()
                        txtFromChequeBook.clearFocus()
                        txtToChequeBook.clearFocus()
                        editChequeModel()
                        this.finish()
                        startActivity(
                            Intent(
                                this,
                                NewChequeBookActivity::class.java
                            ).putExtra(Constants.CHEQUE_SAVE_TYPE, "1")
                                .putExtra(Constants.CHEQUE_SAVE_FROM_ADD_EDIT, "1")
                        )
                        finish()
                    }
                }
                "3" -> {
                    if (performValidation()) {
                        txtBookName.clearFocus()
                        txtFromChequeBook.clearFocus()
                        txtToChequeBook.clearFocus()
                        saveChequeModel()
                        this.finish()
                        startActivity(
                            Intent(
                                this,
                                NewChequeBookActivity::class.java
                            ).putExtra(Constants.CHEQUE_SAVE_TYPE, "1")
                                .putExtra(Constants.CHEQUE_SAVE_FROM_ADD_EDIT, "2")
                        )
                        finish()
                    }
                }
            }
        }

        btnSaveCloseAddCheque?.clickWithDebounce {

            when (chequeSaveAdd) {
                "2" -> {
                    // edit
                    if (performValidation()) {
                        txtBookName.clearFocus()
                        txtFromChequeBook.clearFocus()
                        txtToChequeBook.clearFocus()
                        editChequeModel()
                        finish()
                    }
                }
                "3" -> {
                    //add
                    if (performValidation()) {
                        txtBookName.clearFocus()
                        txtFromChequeBook.clearFocus()
                        txtToChequeBook.clearFocus()
                        saveChequeModel()
                        finish()
                    }

                }
            }
            /*if (performValidation()) {
                txtBookName.clearFocus()
                txtFromChequeBook.clearFocus()
                txtToChequeBook.clearFocus()
                saveChequeModel()
                onBackPressed()
            }*/
        }

    }


    fun performCheckValidation(): Boolean {
        if (txtFromChequeBook.text.toString().isBlank()) {
            CommonUtils.showDialog(this, getString(R.string.chequebook_from_msg))
            txtFromChequeBook.requestFocus()
            return false
        } else if (txtToChequeBook.text.toString().isBlank()) {
            CommonUtils.showDialog(this, getString(R.string.chequebook_TO_msg))
            txtToChequeBook.requestFocus()
            return false
        }
        return true
    }


    fun performValidation(): Boolean {
        if (txtBookName.text.toString().isBlank()) {
            CommonUtils.showDialog(
                this,
                getString(R.string.chequebook_name_msg)/*"Please Enter Code"*/
            )
            txtBookName.requestFocus()
            return false
        } else if (txtFromChequeBook.text.toString().isBlank()) {
            CommonUtils.showDialog(this, getString(R.string.chequebook_from_msg))
            txtFromChequeBook.requestFocus()
            return false
        } else if (txtToChequeBook.text.toString().isBlank()) {
            CommonUtils.showDialog(this, getString(R.string.chequebook_TO_msg))
            txtToChequeBook.requestFocus()
            return false
        }
        return true
    }

    fun editChequeModel() {
        if (prefs.contains(Constants.PREF_CHEQUE_BOOK_EDITKEY)) {
            val collectionType = object :
                TypeToken<java.util.ArrayList<AddChequeBookModel.AddChequeBookModelItem>>() {}.type

            val chequeEditList: ArrayList<AddChequeBookModel.AddChequeBookModelItem> =
                Gson().fromJson(prefs[Constants.PREF_CHEQUE_BOOK_EDITKEY, ""], collectionType)
            editchequeList.addAll(chequeEditList)

        } else {
            editchequeList = ArrayList()
        }

        val childModel = AddChequeBookModel.AddChequeBookModelItem(
            txtFromChequeBook.text.toString().toInt(),
            txtBookName.text.toString().trim(),
            txtToChequeBook.text.toString().toInt(),
            totalcheque.toString()
        )


        if (edit_cheque_pos >= 0 && edit_cheque_pos != -1) {
            // Update selected item
            editchequeList.set(edit_cheque_pos, childModel)
        } else {
            // Add new item
            editchequeList.add(childModel)
        }
        val prefs = PreferenceHelper.defaultPrefs(this)
        prefs[Constants.PREF_CHEQUE_BOOK_EDITKEY] = Gson().toJson(editchequeList)


    }

    fun saveChequeModel() {
        if (prefs.contains(Constants.PREF_CHEQUE_BOOK_KEY)) {
            val collectionType = object :
                TypeToken<java.util.ArrayList<AddChequeBookModel.AddChequeBookModelItem>>() {}.type
            var chequesList: ArrayList<AddChequeBookModel.AddChequeBookModelItem> =
                Gson().fromJson(prefs[Constants.PREF_CHEQUE_BOOK_KEY, ""], collectionType)
            addchequeList.addAll(chequesList)
        } else {
            addchequeList = ArrayList()
        }

        val childModel = AddChequeBookModel.AddChequeBookModelItem(
            txtFromChequeBook.text.toString().toInt(),
            txtBookName.text.toString().trim(),
            txtToChequeBook.text.toString().toInt(),
            totalcheque.toString()
        )


        if (new_cheque_pos >= 0 && new_cheque_pos != -1) {
            // Update selected item
            addchequeList.set(new_cheque_pos, childModel)
        } else {
            // Add new item
            addchequeList.add(childModel)
        }

        val prefs = PreferenceHelper.defaultPrefs(this)
        prefs[Constants.PREF_CHEQUE_BOOK_KEY] = Gson().toJson(addchequeList)

    }
}
package com.goldbookapp.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.goldbookapp.R
import com.goldbookapp.databinding.QrCodeActivityBinding
import com.goldbookapp.ui.activity.user.BackToLoginActivity
import com.goldbookapp.utils.CommonUtils
import com.goldbookapp.utils.CommonUtils.Companion.clickWithDebounce
import kotlinx.android.synthetic.main.recover_account_activity.*
import kotlinx.android.synthetic.main.toolbar_layout.*

class QrCodeActivity: AppCompatActivity() {

    lateinit var binding: QrCodeActivityBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.qr_code_activity)

        imgLeft.setImageResource(R.drawable.ic_back)
        tvTitle.setText(R.string.qr_code)

        btnReset?.clickWithDebounce {
            startActivity(Intent(this, BackToLoginActivity::class.java))
        }

        imgLeft?.clickWithDebounce {
            onBackPressed()
        }

        binding.txtBack?.clickWithDebounce {
            onBackPressed()
        }

    }
    override fun onDestroy() {
        super.onDestroy()
        CommonUtils.hideProgress()
    }
}
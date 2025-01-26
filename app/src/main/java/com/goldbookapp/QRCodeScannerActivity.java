package com.goldbookapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.goldbookapp.databinding.ActivityQrcodeScannerBinding;
import com.goldbookapp.permissions.PermissionHandler;
import com.goldbookapp.permissions.Permissions;
import com.goldbookapp.ui.activity.additem.AddItemActivity;
import com.goldbookapp.utils.Constants;
import com.google.zxing.ResultPoint;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class QRCodeScannerActivity extends AppCompatActivity {

    private ActivityQrcodeScannerBinding binding;
    String transaction_type, selectedCustStateId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_qrcode_scanner);

        setupView();

        if (getIntent().hasExtra(Constants.TRANSACTION_TYPE)) {
            transaction_type = getIntent().getStringExtra(Constants.TRANSACTION_TYPE);
            selectedCustStateId = getIntent().getStringExtra(Constants.CUST_STATE_ID);
        }


    }

    private void setupView() {
//        binding.zxingViewfinderView.setCameraPreview(binding.zxingBarcodeSurface);
//        binding.zxingBarcodeSurface.decodeContinuous(this);


        binding.backIcon.setOnClickListener(v -> finish());
        // startQRScanner();


//        binding.dbvBarcode.decodeContinuous(new BarcodeCallback() {
//            @Override
//            public void barcodeResult(BarcodeResult result) {
//                updateText(result.getText());
//                beepSound();f
//            }
//
//            @Override
//            public void possibleResultPoints(List<ResultPoint> resultPoints) {
//
//            }
//        });

        // requestPermission();

        Permissions permissions = new Permissions();

        permissions.check(this /*context*/,
                Manifest.permission.CAMERA,
                null,
                new PermissionHandler() {
                    @Override
                    public void onGranted() {
                        // startQRScanner();
                        //Toast.makeText(QRCodeScannerActivity.this, "Granted", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onDenied(Context context, ArrayList<String> deniedPermissions) {
                        super.onDenied(context, deniedPermissions);
                    }
                });


        binding.barcodeScanner.decodeSingle(callback);
        binding.barcodeScanner.setStatusText("");

//        llProgress.setVisibility(View.GONE);
//        setupNfcAdapter();

    }

    //    protected void beepSound() {
//        try {
//            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
//            r.play();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
    private void startQRScanner() {
        new IntentIntegrator(this).initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (result != null) {
            if (result.getContents() == null) {
//                Toast.makeText(this,    "Cancelled",Toast.LENGTH_LONG).show();
            } else {
                updateText(result.getContents());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 0 && grantResults.length < 1) {
            requestPermission();
        } else {
//            binding.dbvBarcode.resume();
        }

    }

    void requestPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 0);
        } else {
            startQRScanner();
        }
    }

    private void updateText(String scanCode) {
        //Toast.makeText(this,    "scanCode : + " + scanCode,Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        binding.zxingBarcodeSurface.resume();
//        resumeScanner();
        binding.barcodeScanner.resume();

//        isScanned = false;
    }

    protected void resumeScanner() {
//        isScanDone = false;
//        if (!binding.dbvBarcode.isActivated())
//            binding.dbvBarcode.resume();
//        Log.d("peeyush-pause", "paused: false");
    }

    protected void pauseScanner() {
//        binding.dbvBarcode.pause();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return binding.barcodeScanner.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
    }

    private BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            if (result.getText() != null) {
                //binding.barcodeScanner.setStatusText(result.getText());
                // Toast.makeText(QRCodeScannerActivity.this,    "scanCode : + " + result.getText().toString(),Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(QRCodeScannerActivity.this, AddItemActivity.class)
                        .putExtra(Constants.QR_DETAILS, result.getText().toString())
                        .putExtra(Constants.TRANSACTION_TYPE, transaction_type)
                        .putExtra(Constants.CUST_STATE_ID, selectedCustStateId);
                startActivity(intent);
                finish();
                //tvscanResult.setText("Data found: " + result.getText());
                //System.out.println("Possible Result QRA = " + result.getText().toString());
                /*JSONObject jresponse = null;
                try {
                    jresponse = new JSONObject(result.getText());
                    jresponse.getString("group_id");



                } catch (JSONException e) {
                    e.printStackTrace();
                   *//* AlertDailogView.showAlert(QRCodeScannerActivity.this,
                            QRCodeScannerActivity.this.getResources().getString(android.R.string.dialog_alert_title), QRCodeScannerActivity.this.getResources().getString(R.string.alert_valid_qr_code),
                            QRCodeScannerActivity.this.getResources().getString(com.openshadow.mymanu.login.R.string.ok), new AlertDailogView.OnCustPopUpDialogButoonClickListener() {
                                @Override
                                public void OnButtonClick(int tag, int buttonIndex, String input) {
                                    binding.barcodeScanner.decodeSingle(callback);
                                }
                            }).show();*//*
                }*/


            }

            //you can also Add preview of scanned barcode
            //ImageView imageView = (ImageView) findViewById(R.id.barcodePreview);
            //imageView.setImageBitmap(result.getBitmapWithResultPoints(Color.YELLOW));
        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {
//            System.out.println("Possible Result points = " + resultPoints);
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
//        binding.zxingBarcodeSurface.pause();
//        pauseScanner();
        binding.barcodeScanner.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        qrCodeScannerInterface = null;
    }

//    @Override
//    public void barcodeResult(BarcodeResult result) {
//        qrCodeScannerInterface.onRetrievedResult(result.toString());
//    }
//
//    @Override
//    public void possibleResultPoints(List<ResultPoint> resultPoints) {
//
//    }
//
//    interface QRCodeScannerInterface {
//        void onRetrievedResult(String barcodeResult);
//    }
}

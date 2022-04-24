package com.example.ooler

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import com.example.ooler.databinding.ActivityMainBinding
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.widget.Button
import timber.log.Timber;

private const val ENABLE_BLUETOOTH_REQUEST_CODE = 1
private const val LOCATION_PERMISSION_REQUEST_CODE = 2

class MainActivity : Activity() {


    //-----------------------Function Overrides---------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_main)
        val scanButton = findViewById<Button>(R.id.scan_button) as Button


        scanButton.setOnClickListener{ startBleScan()}

    }

    override fun onResume() {
        super.onResume()
        if(!bluetoothAdapter.isEnabled)
        {
            promptEnableBluetooth()
        }
    }

    //---------------------Properties--------------------------------

    private lateinit var binding: ActivityMainBinding

    private val bluetoothAdapter: BluetoothAdapter by lazy{
        val bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private val btScanner by lazy{
        bluetoothAdapter.bluetoothLeScanner
    }

    private val scanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            with(result.device) {
                Timber.i(
                    "Found BLE device! Name: ${name ?: "Unnamed"}, address: $address"
                )
            }
        }
    }

    private val scanSettings = ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()

    private val isLocationPermissionGranted
        get() = hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)


    @SuppressLint("MissingPermission")



    //-------------------Private Functions------------------------
    private fun promptEnableBluetooth(){
        if(!bluetoothAdapter.isEnabled){
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, ENABLE_BLUETOOTH_REQUEST_CODE)
        }
    }

    private fun startBleScan()
    {
        if (!isLocationPermissionGranted)
        {
            requestLocationPermission()
        }
        else{
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_SCAN
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            btScanner.startScan(null, scanSettings, scanCallback)
        }
    }

    private fun requestLocationPermission(){
        if(isLocationPermissionGranted){
            return}
            requestPermission(
                Manifest.permission.ACCESS_FINE_LOCATION,
                LOCATION_PERMISSION_REQUEST_CODE
            )
    }
        private fun Activity.requestPermission(permission: String, requestCode: Int) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
        }

    private fun Context.hasPermission(permissionType: String):Boolean
    {
        return ContextCompat.checkSelfPermission(this,permissionType) ==
                PackageManager.PERMISSION_GRANTED
    }








}
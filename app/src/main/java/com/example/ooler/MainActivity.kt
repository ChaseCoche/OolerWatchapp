package com.example.ooler

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.*
import android.os.Bundle
import com.example.ooler.databinding.ActivityMainBinding
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
import timber.log.Timber


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



        scanButton.setOnClickListener{
            if(isScanning)
            {
                stopBleScan()
            }
            else {
                startBleScan()
            }
        }


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

    private val scanResults = mutableListOf<ScanResult>()

    private var isScanning = false

        set(value) {
            val scanButton = findViewById<Button>(R.id.scan_button) as Button
            field = value
            runOnUiThread { scanButton.text = if (value) " Stop Scan " else "Start Scan" }
        }


    private val btScanner by lazy{
        bluetoothAdapter.bluetoothLeScanner
    }

    private val scanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val indexQuery=scanResults.indexOfFirst{it.device.address == result.device.address}
            var flag = 0
            if(indexQuery!=-1){
                scanResults[indexQuery]=result
                flag = 1
            }
            with(result.device) {


                if(result.device.address == "84:2E:14:83:11:68" && flag!=1)
                {
                    Timber.i(
                    "Found OOLER device! Name: ${name ?: "Unnamed"}, address: $address"
                    )
                    scanResults.add(result)
                    stopBleScan()
                    connectGatt(applicationContext, false, gattCallback)

                }
            }
        }
    }


    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            val deviceAddress = gatt.device.address

            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.w("BluetoothGattCallback", "Successfully connected to $deviceAddress")
                    // TODO: Store a reference to BluetoothGatt
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.w("BluetoothGattCallback", "Successfully disconnected from $deviceAddress")
                    gatt.close()
                }
            } else {
                Log.w("BluetoothGattCallback", "Error $status encountered for $deviceAddress! Disconnecting...")
                scanResults.clear()
                gatt.close()
            }
        }
    }

   /* private
*/

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

    @SuppressLint("MissingPermission")
    private fun startBleScan()
    {
        if (!isLocationPermissionGranted)
        {
            requestLocationPermission()
        }
        else{
            btScanner.startScan(null, scanSettings, scanCallback)
            isScanning = true
        }
    }

    @SuppressLint("MissingPermission")
    private fun stopBleScan()
    {
        btScanner.stopScan(scanCallback)
        isScanning = false
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
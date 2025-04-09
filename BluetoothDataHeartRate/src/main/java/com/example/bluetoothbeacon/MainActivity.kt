package com.example.bluetoothbeacon

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.bluetoothbeacon.ui.theme.BluetoothBeaconTheme
import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import java.nio.file.WatchEvent
import java.util.UUID

class MainActivity : ComponentActivity() {
    private var mBluetoothAdapter: BluetoothAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        hasPermissions()
            enableEdgeToEdge()
            setContent {
                BluetoothBeaconTheme {
                    val context = LocalContext.current
                    val viewModel: MyViewModel = viewModel()
                    val gattCallback = remember { GattClientCallback(viewModel) }
                    val bluetoothManager =
                        context.getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
                    mBluetoothAdapter = bluetoothManager.adapter
                    val fScanning by viewModel.fScanning.observeAsState(false)


                    if (mBluetoothAdapter == null) {
                        Log.d("DBG", "Bluetooth adapter is null")
                    } else {
                        Log.d("DBG", "Bluetooth adapter is initialized successfully")
                    }

                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                        Button(
                            modifier = Modifier
                                .padding(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Black,
                            ),
                            shape = RoundedCornerShape(12.dp),
                            onClick = {
                                mBluetoothAdapter?.bluetoothLeScanner?.let { scanner ->
                                    viewModel.scanDevices(scanner)
                                }
                            }
                        ) {
                            Text(if (fScanning) "Scanning for devices..." else "Start Scanning")
                        }}
                        ShowDevices(viewModel, gattCallback)
                    }
                }
            }
    }

    /* Android Bluetooth oikeuksien juhlamalli */
    private fun hasPermissions(): Boolean {
        if (mBluetoothAdapter == null || !mBluetoothAdapter!!.isEnabled) {
            Log.d("DBG", "No Bluetooth LE capability")
            return false
        } else if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
            checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
            checkSelfPermission(Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
            Log.d("DBG", "No fine location access")
            requestPermissions(arrayOf(
                Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 1);
            return true
        }

        return true
    }
}



class MyViewModel: ViewModel() {
    companion object GattAttributes {
        const val SCAN_PERIOD: Long = 5000
    }
    val heartRate = MutableLiveData<Int?>(null)
    val scanResults = MutableLiveData<List<ScanResult>>(null)
    val fScanning = MutableLiveData(false)
    private val mResults = java.util.HashMap<String, ScanResult>()

    @SuppressLint("MissingPermission")
    fun scanDevices(scanner: BluetoothLeScanner) {
        viewModelScope.launch(Dispatchers.IO) {
            fScanning.postValue(true)
            val settings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .setReportDelay(0)
                .build()
            scanner.startScan(null, settings,leScanCallback)
            delay(SCAN_PERIOD)
            scanner.stopScan(leScanCallback)
            scanResults.postValue(mResults.values.toList())
            fScanning.postValue(false)
        }
    }
    private val leScanCallback: ScanCallback = object : ScanCallback() {
       override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            val device = result.device
            val deviceAddress = device.address
            mResults[deviceAddress] = result

            Log.d("DBG", "Device address: $deviceAddress (${result.isConnectable})")
        }
    }
}
@SuppressLint("MissingPermission")
class GattClientCallback(private val viewModel : MyViewModel) : BluetoothGattCallback() {
    private fun convertFromInteger(i: Int): UUID {
        val MSB = 0x0000000000001000L
        val LSB = -0x7fffff7fa064cb05L
        val value = (i and -0x1).toLong()
        return UUID(MSB or (value shl 32), LSB)
    }

    val HEART_RATE_SERVICE_UUID = convertFromInteger(0x180D)
    val HEART_RATE_MEASUREMENT_CHAR_UUID = convertFromInteger(0x2A37)
    val CLIENT_CHARACTERISTIC_CONFIG_UUID = convertFromInteger(0x2902)

    override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
        super.onConnectionStateChange(gatt, status, newState)
        if(status == BluetoothGatt.GATT_FAILURE) {
            Log.d("DBG", "GATT connection failure")
            return
        } else if (status == BluetoothGatt.GATT_SUCCESS) {
            Log.d("DBG", "GATT connection success")
        }
        if(newState == BluetoothProfile.STATE_CONNECTED) {
            Log.d("DBG", "GATT service connected")
            gatt.discoverServices()
        } else if(newState == BluetoothProfile.STATE_DISCONNECTED) {
            Log.d("DBG", "GATT service disconnected")
        }
    }

    override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
        super.onServicesDiscovered(gatt, status)
        if(status != BluetoothGatt.GATT_SUCCESS) {
            return
        }
        Log.d("DBG", "onServicesDiscovered()")
        for (gattService in gatt?.services!!) {
            Log.d("DBG","Service1 ${gattService.uuid}")
            if(gattService.uuid == HEART_RATE_SERVICE_UUID) {
                Log.d("DBG", "BINGO!!!")
                for (gattCharacteristic in gattService.characteristics){
                    Log.d("DBG", "Characteristics1 ${gattCharacteristic.uuid}")
                    }
                val characteristics = gatt.getService(HEART_RATE_SERVICE_UUID).getCharacteristic(HEART_RATE_MEASUREMENT_CHAR_UUID)
                gatt.setCharacteristicNotification(characteristics, true)
                val descriptor = characteristics.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_UUID)
                descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                gatt.writeDescriptor(descriptor)
                if (descriptor == null) {
                    Log.d("DBG","Descriptor is null!")
                }

            }
        }
    }

    override fun onDescriptorWrite(
        gatt: BluetoothGatt?,
        descriptor: BluetoothGattDescriptor?,
        status: Int
    ) {
        Log.d("DBG","onDescriptorWrite")
        Log.d("DBG","onDescriptorWrite status = $status")

    }

    override fun onCharacteristicChanged(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
    ) {
        Log.d("DBG","Characteristic data received")
        val data = characteristic.value
        val heartRate = data[1].toInt()
        viewModel.heartRate.postValue(heartRate)
        Log.d("DBG","heartRate: $heartRate")
       }
}

@Composable
fun HeartRate(viewModel: MyViewModel= viewModel()) {
    val bpm by viewModel.heartRate.observeAsState(null)
    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600),
            repeatMode = RepeatMode.Reverse
        )
    )
    bpm?.let {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Heart rate", color = Color.Gray)
            Text(
                text = "$bpm bpm",
                style = MaterialTheme.typography.displayMedium.copy(
                    Color.Red
                ),
                modifier = Modifier
                    .graphicsLayer(scaleX = scale, scaleY = scale)
                    .padding(top = 16.dp, bottom = 16.dp)
            )
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun ShowDevices(model: MyViewModel = viewModel(), mGattCallBack: GattClientCallback) {
    val value: List<ScanResult>? by model.scanResults.observeAsState(null)
    val context = LocalContext.current

    HeartRate(viewModel())
    LazyColumn {
        value?.forEach { result ->
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        modifier = Modifier
                            .padding(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Black,
                        ),
                        shape = RoundedCornerShape(2.dp),
                        onClick = {
                            result.device.connectGatt(context, false, mGattCallBack)
                        }
                    ) {
                        Column {
                            Text("Device:")
                            Text(
                                "${result.device.name}",
                                color = if (result.isConnectable) Color.Green else Color.Red
                            )
                        }
                    }

                }
            }
        }
    }
}


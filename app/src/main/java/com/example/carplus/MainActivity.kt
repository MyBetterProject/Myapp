package com.example.carplus


import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.carplus.databinding.ActivityMainBinding
import com.example.carplus.databinding.BluetoothActivityBinding
import java.io.IOException
import java.io.OutputStream
import java.util.UUID

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding


    private val mBluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var mBluetoothSocket: BluetoothSocket? = null

    private var outputStream: OutputStream? = null


    private lateinit var sharedPreferences: SharedPreferences


    private val DEVICE_NAME = "JDY-31-SPP"
    private val DEVICE_MAC_ADDRESS = "40:93:2C:11:7B:B6"
    private val MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")

    private var connectionAttempts = 0 // 连接尝试次数

    private var isConnected = false // 标志位，表示蓝牙是否已连接成功


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = BluetoothActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sharedPreferences = getSharedPreferences("Savedcommands", Context.MODE_PRIVATE)

        binding.save.setOnClickListener {
            val upward = binding.etUpward.text.toString()
            val downward = binding.etDownward.text.toString()
            val left = binding.etTurnleft.text.toString()
            val right = binding.etTurnright.text.toString()
            val stop = binding.etStop.text.toString()

            // 保存数据

            val editor = sharedPreferences.edit()
            editor.putString("upward", upward)
            editor.putString("downward", downward)
            editor.putString("left", left)
            editor.putString("right", right)
            editor.putString("stop", stop)
            editor.apply()

            // 显示保存成功的提示信息

            Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show()
        }

        // 从SharedPreferences中读取保存的数据并显示在EditText中

        binding.etUpward.setText(sharedPreferences.getString("upward", ""))
        binding.etDownward.setText(sharedPreferences.getString("downward", ""))
        binding.etTurnleft.setText(sharedPreferences.getString("left", ""))
        binding.etTurnright.setText(sharedPreferences.getString("right", ""))
        binding.etStop.setText(sharedPreferences.getString("stop", ""))

        binding.send.setOnClickListener {
            val commandList = mutableListOf<String>()
            commandList.add(binding.etUpward.text.toString())
            commandList.add(binding.etDownward.text.toString())
            commandList.add(binding.etTurnleft.text.toString())
            commandList.add(binding.etTurnright.text.toString())
            commandList.add(binding.etStop.text.toString())

            if (connectBluetooth()) {
                sendCommands(commandList)
            }
        }
    }





    private fun connectBluetooth(): Boolean {
        if (isConnected) {
            return true

        }

        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "设备不支持蓝牙", Toast.LENGTH_SHORT).show()
            return false

        }

        if (!mBluetoothAdapter.isEnabled) {
            Toast.makeText(this, "蓝牙未开启", Toast.LENGTH_SHORT).show()
            return true

        }

        if (connectionAttempts >= 3) {
            Toast.makeText(this, "连接失败超过3次，不再连接", Toast.LENGTH_SHORT).show()
            return true

        }

        val device: BluetoothDevice? = mBluetoothAdapter.getRemoteDevice(DEVICE_MAC_ADDRESS)
        try {
            mBluetoothSocket = device?.createRfcommSocketToServiceRecord(MY_UUID)
            mBluetoothSocket?.connect()
            Toast.makeText(this, "蓝牙连接成功", Toast.LENGTH_SHORT).show()

            connectionAttempts = 0 // 连接成功后停止继续连接


            isConnected = true


            outputStream = mBluetoothSocket?.outputStream


            return false

        } catch (e: IOException) {
            Toast.makeText(this, "蓝牙连接失败", Toast.LENGTH_SHORT).show()
            e.printStackTrace()

            // 连接失败时不返回true，而是继续执行后续代码

        }

        // 在这里添加进入活动页面的代码

        val intent = Intent(this, BluetoothActivityBinding::class.java)
        startActivity(intent)

        return true

    }



    private fun sendCommands(commands: List<String>) {
        try {
            for (command in commands) {
                outputStream?.write(command.toByteArray())
                outputStream?.flush()
            }
            Toast.makeText(this, "命令发送成功", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            Toast.makeText(this, "命令发送失败", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            outputStream?.close()
            mBluetoothSocket?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu1 -> {
                if (!CarActivity::class.java.isAssignableFrom(this::class.java)) {
                    val intent1 = Intent(this@MainActivity, CarActivity::class.java)
                    startActivity(intent1)
                }

                return true
            }
            R.id.menu2 -> {
                // 判断当前活动是否为菜单项2对应的活动

                if (this::class.java != MainActivity::class.java) {
                    val intent2 = Intent(this@MainActivity, MainActivity::class.java)
                    startActivity(intent2)
                }
                return true

            }
            // 添加其他菜单项的处理逻辑

            else -> return super.onOptionsItemSelected(item)
        }
    }



}



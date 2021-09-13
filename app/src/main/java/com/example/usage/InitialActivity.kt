package com.example.usage

import android.app.AppOpsManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.usage.Application.MainActivity
import com.example.usage.Smartphone.PhoneActivity
import com.example.usage.databinding.ActivityInitialBinding

class InitialActivity : AppCompatActivity() {

    lateinit var binding:ActivityInitialBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInitialBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
    }

    private fun init(){
        binding.buttonApp.setOnClickListener {
            if (!checkForPermission()) {
                Toast.makeText(
                    this,
                    "You need to check the permission",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
        }
        binding.buttonPhone.setOnClickListener {
            if (!checkForPermission()) {
                Toast.makeText(
                    this,
                    "You need to check the permission",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                intent = Intent(this, PhoneActivity::class.java)
                startActivity(intent)
            }
        }
        binding.buttonIO.setOnClickListener {

        }
        binding.buttonPermission.setOnClickListener {
            if (!checkForPermission()) {
                Log.i(ContentValues.TAG, "The user may not allow the access to apps usage. ")
                Toast.makeText(
                    this,
                    "Failed to retrieve app usage statistics. " +
                            "You may need to enable access for this app through " +
                            "Settings > Security > Apps with usage access",
                    Toast.LENGTH_LONG
                ).show()
                startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
            } else {
            }
        }
    }

    private fun checkForPermission(): Boolean {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(), packageName)
        return mode == AppOpsManager.MODE_ALLOWED
    }
}
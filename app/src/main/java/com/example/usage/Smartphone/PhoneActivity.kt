package com.example.usage.Smartphone

import android.R
import android.app.AppOpsManager
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.usage.databinding.ActivityPhoneBinding
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class PhoneActivity : AppCompatActivity() {

    lateinit var binding: ActivityPhoneBinding

    private val selectItems = arrayOf("Day", "Week", "Month", "Year")
    var period:String = ""

    var totalTime:Int = 0
    var begin:String = ""
    var end:String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhoneBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
    }

    private fun init(){

        initSpinner(binding.spinnerPeriod)

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

        binding.buttonRun.setOnClickListener {
            totalTime = 0
            begin = ""
            end = ""
            if (!checkForPermission()) {
                Toast.makeText(
                    this,
                    "You need to check the permission",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                calculateTotalTime()
                setTime(binding.textViewBegin, binding.textViewEnd, binding.textViewTime)
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

    private fun initSpinner(spinner: Spinner) {
        val spinnerAdapter = this?.let {
            ArrayAdapter(
                it.applicationContext, R.layout.simple_spinner_item, selectItems
            )
        }
        spinnerAdapter?.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)
        spinner.adapter = spinnerAdapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                when (position) {
                    0 -> {
                        period = "Day"
                    }
                    1 -> {
                        period = "Week"
                    }
                    2 -> {
                        period = "Month"
                    }
                    3 -> {
                        period = "Year"
                    }
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
    }

    // public List<UsageStats> queryUsageStats(int intervalType, long beginTime, long endTime)
    // intervalType = INTERVAL_BEST, INTERVAL_DAILY, INTERVAL_MONTHLY, INTERVAL_WEEKLY, INTERVAL_YEARLY

    private fun getAppUsageStats(): MutableList<UsageStats> {
        val cal = Calendar.getInstance()
        val df: DateFormat = SimpleDateFormat("yyyy-MM-dd")
        Log.d(ContentValues.TAG, "current: ${df.format(cal.time)}")
        end = df.format(cal.time)
        if(period=="Day"){
            cal.add(Calendar.DATE, -1)
            begin = df.format(cal.time)
        }else if(period=="Week"){
            cal.add(Calendar.DATE, -7)
            begin = df.format(cal.time)
        }else if(period=="Month"){
            cal.add(Calendar.MONTH, -1)
            begin = df.format(cal.time)
        }else{
            cal.add(Calendar.YEAR, -1)
            begin = df.format(cal.time)
        }
        Log.d(ContentValues.TAG, "after: ${df.format(cal.time)}")

        val usageStatsManager =
            getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val queryUsageStats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY, cal.timeInMillis, System.currentTimeMillis() // 쿼리
        )
        return queryUsageStats
    }

    private fun calculateTotalTime(){
        val applist = getAppUsageStats()
        if(applist.size>0){
            for(appinfo in applist){
                val apptime = appinfo.totalTimeInForeground
                if (apptime.toString() != "0") {
                    totalTime += apptime.toInt()
                }
            }
        }
    }

    private fun setTime(beginT:TextView, endT:TextView, timeT:TextView){
        beginT.text = begin
        endT.text = end
        val h:Int = ((totalTime/1000)/60)/60
        val m:Int = ((totalTime/1000)/60)%60
        timeT.text = h.toString() + " h" + m.toString() + " m"
    }

}
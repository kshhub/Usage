package com.example.usage.Application

import android.R
import android.app.AppOpsManager
import android.app.AppOpsManager.MODE_ALLOWED
import android.app.AppOpsManager.OPSTR_GET_USAGE_STATS
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Process.myUid
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.usage.databinding.ActivityMainBinding
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.Comparator

class MainActivity : AppCompatActivity() {

    lateinit var binding:ActivityMainBinding
    lateinit var adapter: MyAdapter

    private val selectItems = arrayOf("Day", "Week", "Month", "Year")
    var period:String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
    }

    private fun init(){

        initSpinner(binding.spinnerPeriod)

        binding.buttonPermission.setOnClickListener {
            if (!checkForPermission()) {
                Log.i(TAG, "The user may not allow the access to apps usage. ")
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
            if (!checkForPermission()) {
                Toast.makeText(
                    this,
                    "You need to check the permission",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                showAppUsageStats(getAppUsageStats())
                initRecyclerView()
            }
        }
    }

    private fun checkForPermission(): Boolean {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(OPSTR_GET_USAGE_STATS, myUid(), packageName)
        return mode == MODE_ALLOWED
    }

    private fun initRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(this,
            LinearLayoutManager.VERTICAL, false)
        adapter = MyAdapter(ArrayList<MyData>())
        val applist = getAppUsageStats()
        if(applist.size>0){
            for(appinfo in applist){
                val apptime = appinfo.totalTimeInForeground
                val apppackname = appinfo.packageName
                val appicon = packageManager.getApplicationIcon(appinfo.packageName)
                if (apptime.toString() != "0") {
                    adapter.items.add(MyData(apptime.toString() + " ms", apppackname, appicon))
                }
            }
        }
        binding.recyclerView.adapter = adapter
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
        Log.d(TAG, "current: ${df.format(cal.time)}")
        if(period=="Day"){
            cal.add(Calendar.DATE, -1)
        }else if(period=="Week"){
            cal.add(Calendar.DATE, -7)
        }else if(period=="Month"){
            cal.add(Calendar.MONTH, -1)
        }else{
            cal.add(Calendar.YEAR, -1)
        }
        Log.d(TAG, "after: ${df.format(cal.time)}")

        val usageStatsManager =
            getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val queryUsageStats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY, cal.timeInMillis, System.currentTimeMillis() // 쿼리
        )
        return queryUsageStats
    }

    private fun showAppUsageStats(usageStats: MutableList<UsageStats>) {
        usageStats.sortWith(Comparator { right, left ->
            compareValues(left.lastTimeUsed, right.lastTimeUsed)
        })

        usageStats.forEach { it ->
            Log.d(TAG, "packageName: ${it.packageName}, lastTimeUsed: ${Date(it.lastTimeUsed)}, " +
                    "totalTimeInForeground: ${it.totalTimeInForeground}")
        }
    }
    // getPackageName : 앱 이름, getLastTimeUsed : 마지막으로 사용된 시간, getTotalInForeground : Foreground에서 실행된 전체 시간, getAppLaunchCount : 실행된 횟수

}
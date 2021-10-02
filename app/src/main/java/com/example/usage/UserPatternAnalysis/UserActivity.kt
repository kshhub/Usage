package com.example.usage.UserPatternAnalysis

import android.R
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.ContentValues.TAG
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.usage.databinding.ActivityUserBinding
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.Comparator

class UserActivity : AppCompatActivity() {

    lateinit var binding: ActivityUserBinding
    lateinit var adapter: UserAdapter

    private val selectBeginItems = arrayOf("Day", "Week", "Month", "Year")
    private val selectEndItems = arrayOf("Today","Day", "Week", "Month", "Year")

    var beginDate:String = ""
    var endDate:String = ""
    var totalTime:Int= 0
    var beginDate4TextView:String = ""
    var endDate4TextView:String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
    }

    private fun init(){

        initBeginSpinner(binding.spinnerBegin)
        initEndSpinner(binding.spinnerEnd)

        binding.buttonRun.setOnClickListener {
            showAppUsageStats(getAppUsageStats())
            initRecyclerView()
            initValue()
            calculateTotalTime()
            setText(binding.textViewBegin, binding.textViewEnd, binding.textViewTime)
        }
    }

    private fun initValue(){
        totalTime = 0
        beginDate4TextView = ""
        endDate4TextView = ""
    }

    private fun initRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(this,
            LinearLayoutManager.VERTICAL, false)
        adapter = UserAdapter(ArrayList<UserData>())
        val applist = getAppUsageStats()
        if(applist.size>0){
            for(appinfo in applist){
                val apptime = appinfo.totalTimeInForeground
                val apppackname = appinfo.packageName
                val appicon = packageManager.getApplicationIcon(appinfo.packageName)
                if (apptime.toString() != "0") {
                    adapter.items.add(UserData(apptime.toString() + " ms", apppackname, appicon))
                }
            }
        }
        binding.recyclerView.adapter = adapter
    }

    private fun initBeginSpinner(spinner: Spinner) {
        val spinnerAdapter = this?.let {
            ArrayAdapter(
                it.applicationContext, R.layout.simple_spinner_item, selectBeginItems
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
                        beginDate = "Day"
                    }
                    1 -> {
                        beginDate = "Week"
                    }
                    2 -> {
                        beginDate = "Month"
                    }
                    3 -> {
                        beginDate = "Year"
                    }
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
    }

    private fun initEndSpinner(spinner: Spinner) {
        val spinnerAdapter = this?.let {
            ArrayAdapter(
                it.applicationContext, R.layout.simple_spinner_item, selectEndItems
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
                        endDate = "Today"
                    }
                    1 -> {
                        endDate = "Day"
                    }
                    2 -> {
                        endDate = "Week"
                    }
                    3 -> {
                        endDate = "Month"
                    }
                    4 -> {
                        endDate = "Year"
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
        val calBegin = Calendar.getInstance()
        val calEnd = Calendar.getInstance()
        val df: DateFormat = SimpleDateFormat("yyyy-MM-dd")
        Log.d(TAG, "current: ${df.format(calBegin.time)}")

        if(beginDate=="Day"){
            calBegin.add(Calendar.DATE, -1)
        }else if(beginDate=="Week"){
            calBegin.add(Calendar.DATE, -7)
        }else if(beginDate=="Month"){
            calBegin.add(Calendar.MONTH, -1)
        }else{
            calBegin.add(Calendar.YEAR, -1)
        }

        if(endDate=="Today") {
            calEnd.timeInMillis = System.currentTimeMillis()
        }else if(endDate=="Day"){
            calEnd.add(Calendar.DATE, -1)
        }else if(endDate=="Week"){
            calEnd.add(Calendar.DATE, -7)
        }else if(endDate=="Month"){
            calEnd.add(Calendar.MONTH, -1)
        }else{
            calEnd.add(Calendar.YEAR, -1)
        }

        Log.d(TAG, "after: ${df.format(calBegin.time)}")

        beginDate4TextView = df.format(calBegin.time)
        endDate4TextView = df.format(calEnd.time)

        val usageStatsManager =
            getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val queryUsageStats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY, calBegin.timeInMillis, calEnd.timeInMillis // 쿼리
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

    private fun setText(textBegin: TextView, textEnd: TextView, textTime: TextView){
        textBegin.text = beginDate4TextView
        textEnd.text = endDate4TextView
        val h:Int = ((totalTime/1000)/60)/60
        val m:Int = ((totalTime/1000)/60)%60
        textTime.text = h.toString() + "  h  " + m.toString() + "  m"
    }
}
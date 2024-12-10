package com.volcengine.effectone.auto.moment

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gyf.immersionbar.ktx.immersionBar
import com.volcengine.ck.moment.base.CKMoment
import com.volcengine.ck.moment.base.CKMomentBase.convertMomentTagPriorityLevelToIndex
import com.volcengine.ck.moment.base.CKMomentRequiredTag
import com.volcengine.ck.moment.base.CKMomentTimeRequirement
import com.volcengine.effectone.auto.moment.configure.CKMomentConfigureTagItemViewModel
import com.volcengine.effectone.auto.moment.configure.Constants.MOMENT_CONFIGURE_ROOT_PATH
import com.volcengine.effectone.auto.moment.configure.MomentConfigureConfigSelectArrayAdapter
import com.volcengine.effectone.auto.moment.configure.MomentConfigureHelper
import com.volcengine.effectone.auto.moment.configure.MomentConfigureTagAdapter
import com.volcengine.effectone.auto.moment.dialog.MomentConfigureSelectTimeDialog
import com.volcengine.effectone.singleton.AppSingleton
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class AutoMomentConfigureActivity : AppCompatActivity() {
    private val configureHelper by lazy {
        MomentConfigureHelper(this, MOMENT_CONFIGURE_ROOT_PATH) }

    private val momentSelectSpinner by lazy {
        findViewById<Spinner>(R.id.ck_moment_configure_moment_name_spinner)
    }
    private val templateIdView by lazy {
        findViewById<EditText>(R.id.ck_moment_configure_related_template_view)
    }
    private val timeStartView by lazy {
        findViewById<TextView>(R.id.ck_moment_configure_time_start)
    }
    private val timeEndView by lazy {
        findViewById<TextView>(R.id.ck_moment_configure_time_end)
    }
    private val saveBtn by lazy {
        findViewById<Button>(R.id.ck_moment_configure_save_btn)
    }
    private val backBtn by lazy {
        findViewById<Button>(R.id.ck_moment_configure_back_btn)
    }

    private lateinit var momentSpinnerAdapter: MomentConfigureConfigSelectArrayAdapter

    private lateinit var viewModel: CKMomentConfigureTagItemViewModel
    private lateinit var labelViewAdapter: MomentConfigureTagAdapter

    // key: ID
    private lateinit var moments: MutableMap<String, CKMoment>
    private var momentsNameMutableList = mutableListOf<String>()

    private lateinit var currentMomentsId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        immersionBar {
            transparentStatusBar()
            navigationBarColor(R.color.black)
            statusBarDarkFont(false)
        }

        initView()

        initViewModuleAndRecycleView()

        initViewCallBack()

        initConfigData()

        if(moments.isEmpty()){
            throw IllegalStateException("No load moment configs")
        }

        // default load first moment
        loadMoment(moments.entries.first().key)
    }

    override fun finish() {
        super.finish()

        configureHelper.updateConfigs(moments.values.toList())
    }

    private fun initView() {
        setContentView(R.layout.activity_auto_moment_configure)
        val locationSpinner = findViewById<Spinner>(R.id.ck_moment_configure_location_spinner)
        val locationSpinnerAdapter = ArrayAdapter(
            this, R.layout.auto_moment_configure_spinner_item, configureHelper.getAllLocation()
        )
        locationSpinner.adapter = locationSpinnerAdapter

        momentSpinnerAdapter = MomentConfigureConfigSelectArrayAdapter(
            this, R.layout.auto_moment_configure_spinner_item, momentsNameMutableList,
        ) {id ->
            moments[id]?.title ?: ""
        }


        momentSelectSpinner.adapter = momentSpinnerAdapter
        momentSelectSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                loadMoment(momentSpinnerAdapter.getItem(pos)!!)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }

        }
    }

    private fun initViewModuleAndRecycleView() {
        viewModel = ViewModelProvider(this).get(CKMomentConfigureTagItemViewModel::class.java)

        // init RecyclerView and Adapter
        val recyclerView = findViewById<RecyclerView>(R.id.ck_moment_configure_tags)
        recyclerView.layoutManager = LinearLayoutManager(this)
        labelViewAdapter = MomentConfigureTagAdapter(emptyList(), configureHelper, viewModel)
        recyclerView.adapter = labelViewAdapter

        viewModel.tagItems.observe(this, Observer { items ->
            labelViewAdapter.setItems(items)
        })

    }

    private fun initViewCallBack() {
        val addTagButton = findViewById<Button>(R.id.ck_moment_configure_add_tag_btn)
        addTagButton.setOnClickListener {
            viewModel.addNewItem()
        }

        timeStartView.setOnClickListener{
            showDatePickerDialog(it as EditText) {
                ensureTimePeriodCorrect(isStartSet = true, isEndSet = false)
            }
        }
        timeEndView.setOnClickListener{
            showDatePickerDialog(it as EditText){
                ensureTimePeriodCorrect(isStartSet = false, isEndSet = true)
            }
        }

        saveBtn.setOnClickListener {
            saveMoment(currentMomentsId)

            Toast.makeText(AppSingleton.instance, getString(R.string.ck_one_moment_configure_toast_save_moment_config),
                Toast.LENGTH_SHORT).show()
        }

        backBtn.setOnClickListener {
            finish()
        }

    }

    private fun initConfigData() {
        moments = configureHelper.loadConfigs().associateBy {
            it.config.id
        }.toMutableMap()

        momentsNameMutableList.clear()
        moments.forEach {
            momentsNameMutableList.add(it.key)
        }
        momentSpinnerAdapter.notifyDataSetChanged()
    }

    private fun loadMoment(id: String) {
        currentMomentsId = id
        val moment = moments[id]
        val req = moment!!.config.requirement

        val tagsByPriority = req.requireTags.groupBy {
            it.priority
        }

        val priorities = tagsByPriority.keys.toList().sortedDescending()

        val orderedTag = priorities.mapIndexed{idx, order ->
            viewModel.newItem().apply {
                tags = tagsByPriority[order]!!.map { it.tag }
                priority = idx + 1
                confidence = tagsByPriority[order]!!.first().prob
            }
        }

        viewModel.tagItems.value = orderedTag

        templateIdView.setText(moment.id)
        if (req.timeRequirement != null){
            req.timeRequirement?.apply {
                timeStartView.text = convertLongToDateString(endTime - timeRange)
                timeEndView.text = convertLongToDateString(endTime)
            }
        } else {
            timeStartView.text = ""
            timeEndView.text = ""
        }

        configureHelper.startEditMoment(moment)
    }

    private fun saveMoment(id: String){
        val saveTags = mutableListOf<CKMomentRequiredTag>()
        viewModel.tagItems.value?.forEach {
            for (tag in it.tags) {
                saveTags.add(
                    CKMomentRequiredTag(
                        tag = tag,
                        prob = it.confidence,
                        priority = convertMomentTagPriorityLevelToIndex(it.priority)
                    )
                )
            }
        }

        val oldMoment = moments[id]!!
        val oldConfig = oldMoment.config
        val oldRequirement = oldConfig.requirement
        val oldTimeReq = oldRequirement.timeRequirement

        val startTime = convertDateStringToUnixTime(timeStartView.text.toString())
        val endTime = convertDateStringToUnixTime(timeEndView.text.toString())

        val newMoment = oldMoment.copy(
            config = oldConfig.copy(
                requirement = oldRequirement.copy(
                    requireTags = saveTags.toList(),
                    timeRequirement = if (startTime != 0L && endTime != 0L) {
                        CKMomentTimeRequirement(
                            endTime = endTime,
                            timeRange = endTime - startTime,
                        )
                    } else {
                        null
                    }
                )
            )
        )

        moments[id] = newMoment
    }


    private fun convertLongToDateString(timeInMillis: Long): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("GMT+8") // 北京时区
        return dateFormat.format(Date(timeInMillis))
    }

    private fun convertDateStringToUnixTime(dateString: String): Long {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("GMT+8") // 北京时区
        return try {
            dateFormat.parse(dateString)?.time ?: 0
        } catch (e: ParseException) {
            0
        }
    }


    private fun showDatePickerDialog(view: EditText, afterDateSelected: ()->Unit) {

        val dateDialog = MomentConfigureSelectTimeDialog(this, object :
            MomentConfigureSelectTimeDialog.SelectTimeCallback {
            override fun onConfirmSelect(year: Int, month: Int, day: Int, hour: Int, minute: Int) {
                val calendar = Calendar.getInstance()
                calendar.set(year, month, day, hour, minute, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                view.setText(convertLongToDateString(calendar.timeInMillis))
                afterDateSelected()
            }

            override fun onCancelSelect() {
                view.setText("")
            }
        })

        dateDialog.show()
    }

    private fun ensureTimePeriodCorrect(isStartSet: Boolean, isEndSet: Boolean) {
        val startTime = convertDateStringToUnixTime(timeStartView.text.toString())
        val endTime = convertDateStringToUnixTime(timeEndView.text.toString())

        // 如果有时间没有设置不运行这个函数的逻辑
        if (startTime == 0L || endTime == 0L){
            return
        }

        if (startTime > endTime){
            Toast.makeText(this, getString(R.string.ck_one_moment_configure_toast_time_period_error),
                Toast.LENGTH_SHORT).show()

            if (isStartSet) {
                // 开始时间晚于结束时间
                timeStartView.text = convertLongToDateString(endTime - 86400000)
            }

            if (isEndSet) {
                // 结束时间早于开始时间
                timeEndView.text = convertLongToDateString(startTime + 86400000)
            }
        }

    }
}
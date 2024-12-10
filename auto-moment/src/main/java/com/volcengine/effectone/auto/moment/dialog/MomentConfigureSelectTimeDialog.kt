package com.volcengine.effectone.auto.moment.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.DatePicker
import android.widget.TimePicker
import com.volcengine.effectone.auto.moment.R


class MomentConfigureSelectTimeDialog(context: Context,
    private val callback: SelectTimeCallback
) : Dialog(context) {
    private val datePicker by lazy { findViewById<DatePicker>(R.id.date_picker) }
    private val timePicker by lazy { findViewById<TimePicker>(R.id.time_picker) }
    private val confirmBtn by lazy { findViewById<Button>(R.id.datetime_picker_confirm)}
    private val cancelBtn by lazy { findViewById<Button>(R.id.datetime_picker_cancel)}

    interface SelectTimeCallback {
        fun onConfirmSelect(year: Int, month: Int, day: Int, hour: Int, minute: Int)
        fun onCancelSelect()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_auto_moment_datetime_select)

        timePicker.setIs24HourView(true)

        confirmBtn.setOnClickListener {
            callback.onConfirmSelect(
                datePicker.year,
                datePicker.month,
                datePicker.dayOfMonth,
                timePicker.currentHour,
                timePicker.currentMinute,
            )
            super.dismiss()
        }

        cancelBtn.setOnClickListener {
            dismiss()
        }
    }

    override fun dismiss() {
        super.dismiss()

        callback.onCancelSelect()
    }
}
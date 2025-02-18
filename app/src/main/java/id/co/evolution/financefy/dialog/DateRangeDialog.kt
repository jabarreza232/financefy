package id.co.evolution.financefy.dialog
import id.co.evolution.financefy.R

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.TextView
import com.applandeo.materialcalendarview.CalendarView
import id.co.evolution.financefy.helper.Tools
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Calendar
import java.util.Locale


class DateRangeDialog:Dialog{
    private var calendarView: CalendarView? = null
    private var btnSelect: Button? = null
    private var btnCancel: Button? = null
    private var tvSelectedRange: TextView? = null
    private var listener: OnDateRangeSelectedListener? = null
    private val dateFormat: SimpleDateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())

  constructor (context: Context, listener: OnDateRangeSelectedListener?) : super(context) {

        this.listener = listener
    }

    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_date_range)
        window?.setLayout(
            android.view.WindowManager.LayoutParams.MATCH_PARENT,
            android.view.WindowManager.LayoutParams.WRAP_CONTENT
        )
        calendarView = findViewById(R.id.calendarView)
        btnSelect = findViewById(R.id.btnSelect)
        btnCancel = findViewById(R.id.btnCancel)
        tvSelectedRange = findViewById(R.id.tvSelectedRange)

//        // Mengatur listener untuk rentang tanggal
//        calendarView.setOnSelectRangeListener(object : OnSelectRangeListener {
//            override fun onSelectRange(startDate: Calendar, endDate: Calendar) {
//                val start = dateFormat.format(startDate.time)
//                val end = dateFormat.format(endDate.time)
//                tvSelectedRange.text = "Rentang Tanggal: $start - $end"
//            }
//        })

        // Event ketika tombol "Pilih Tanggal" ditekan
        btnSelect!!.setOnClickListener { v: View? ->
            val selectedDates: List<Calendar> = calendarView!!.selectedDates

            var datesList:ArrayList<String> = ArrayList<String>()



            if (selectedDates.isNotEmpty()) {
                for (i in calendarView!!.selectedDates){
                    datesList.add(Tools.getFormattedDateSimple(i.timeInMillis))
                }
                val startDate: String = dateFormat.format(selectedDates[0].getTime())
                val endDate: String =
                    dateFormat.format(selectedDates[selectedDates.size - 1].getTime())

                // Kirim hasil ke Activity
                if (listener != null) {
                    listener!!.onDateRangeSelected(startDate, endDate,datesList)
                }
                dismiss()
            } else {
                tvSelectedRange!!.text = "Pilih rentang tanggal terlebih dahulu!"
            }
        }

        // Event ketika tombol "Batal" ditekan
        btnCancel!!.setOnClickListener { v: View? -> dismiss() }
    }

    interface OnDateRangeSelectedListener {
        fun onDateRangeSelected(startDate: String?, endDate: String?, dates:List<String>)
    }
}
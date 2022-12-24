package id.co.evolution.financefy.dialog

import android.content.Context
import com.whiteelephant.monthpicker.MonthPickerDialog
import id.co.evolution.financefy.asynctask.FilterMaxMonthAsynctask
import id.co.evolution.financefy.asynctask.FilterMinYearAsynctask
import id.co.evolution.financefy.model.ModelFinance
import java.util.*
import java.util.concurrent.ExecutionException

class DialogMonthPicker(
    private var context: Context,
    private var today: Calendar,
    private var dataFinance: List<ModelFinance>,
    private var dialogMonthPickerCallback: DialogMonthPickerCallback
) {


    fun showDialogMonthPicker() {
        val builder = MonthPickerDialog.Builder(
            context,
            { selectedMonth, selectedYear ->
                // on date set }
                dialogMonthPickerCallback.onDateSet(selectedMonth, selectedYear)
            }, today.get(Calendar.YEAR), today.get(Calendar.MONTH)
        )

        try {

            builder.setMinYear(today.get(Calendar.YEAR))
                .setActivatedYear(today.get(Calendar.YEAR))

            if(dataFinance.isNotEmpty()){
                builder.setMinYear(FilterMinYearAsynctask(today, dataFinance, context).execute().get().toInt())
                    .setMaxYear((today.get(Calendar.YEAR)))
                    .setMaxMonth(FilterMaxMonthAsynctask(today, dataFinance, context).execute().get()
                        .toInt()
                    )
            }
        } catch (e: ExecutionException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        builder.build().show()
    }


    interface DialogMonthPickerCallback {
        fun onDateSet(selectedMonth: Int, selectedYear: Int)
    }
}
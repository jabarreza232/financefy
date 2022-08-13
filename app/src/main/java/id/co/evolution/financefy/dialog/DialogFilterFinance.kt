package id.co.evolution.financefy.dialog

import android.app.Dialog
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import id.co.evolution.financefy.R
import id.co.evolution.financefy.adapter.AdapterFilter
import id.co.evolution.financefy.model.ModelFilter

class DialogFilterFinance(
    val context: Context,
    val inflater: LayoutInflater,
    val dialogFilterFinanceCallback: DialogFilterFinanceCallback
) {
    val dialog: Dialog = Dialog(context)
    val dialogView: View = inflater.inflate(R.layout.dialog_choose_filter, null)

    init {
        dialog.setContentView(dialogView)
    }


    fun showDialogFilterFinance(
        listFilterType: List<ModelFilter>,
        listFilterNominal: List<ModelFilter>,
        listFilterPeriod: List<ModelFilter>, isAnalysis: Boolean = false
    ) {
        val txtSubmit = dialogView.findViewById<TextView>(R.id.txt_submit)
        val txtNominal = dialogView.findViewById<TextView>(R.id.txt_nominal)
        val rvListType: RecyclerView = dialogView.findViewById(R.id.rv_type)
        val rvListNominal: RecyclerView = dialogView.findViewById(R.id.rv_nominal)
        val rvListPeriod: RecyclerView = dialogView.findViewById(R.id.rv_periode)

        var listFilterTypeValue = listFilterType

        isAnalysis.let {
            when (it) {
                true -> {
                    listFilterTypeValue = listFilterType.subList(0,2)
                    txtNominal.visibility = View.GONE
                }
                false -> {
                    val adapterFilterNominal = AdapterFilter(
                        context, listFilterNominal
                    ) { data: List<ModelFilter>, position: Int ->
                        dialogFilterFinanceCallback.resultFilterNominal(data[position].value)
                    }

                    rvListNominal.layoutManager =
                        LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                    rvListNominal.adapter = adapterFilterNominal
                }
            }
        }

        val adapterFilterType = AdapterFilter(
            context, listFilterTypeValue
        ) { data: List<ModelFilter>, position: Int ->
            dialogFilterFinanceCallback.resultFilterType(data[position].value)
        }

        rvListType.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        rvListType.adapter = adapterFilterType


        val adapterFilterPeriod = AdapterFilter(
            context, listFilterPeriod
        ) { data: List<ModelFilter>, position: Int ->
            dialogFilterFinanceCallback.resultFilterPeriod(data[position].value)
        }
        rvListPeriod.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        rvListPeriod.adapter = adapterFilterPeriod

        txtSubmit.setOnClickListener { v: View? ->
            dialog.dismiss()
            dialogFilterFinanceCallback.onSubmit()
        }

        val window = dialog.window
        val wlp = window!!.attributes

        wlp.gravity = Gravity.CENTER
        wlp.flags = wlp.flags and WindowManager.LayoutParams.FLAG_BLUR_BEHIND.inv()
        window!!.attributes = wlp
        dialog.window!!.setLayout(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )

        dialog.show()

    }


    interface DialogFilterFinanceCallback {
        fun resultFilterType(result: String)
        fun resultFilterNominal(result: String)
        fun resultFilterPeriod(result: String)
        fun onSubmit()

    }
}
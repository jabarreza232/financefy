package id.co.evolution.financefy.dialog

import android.app.Dialog
import android.content.Context
import android.view.*
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import id.co.evolution.financefy.R
import id.co.evolution.financefy.adapter.AdapterFilter
import id.co.evolution.financefy.model.ModelFilter

open class DialogFilterFinance(
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
        val txtSubmit = findViewById<MaterialButton>(R.id.btn_submit)
        val txtNominal = findViewById<TextView>(R.id.txt_nominal)
        val rvListType: RecyclerView = findViewGroupById(R.id.rv_type)
        val rvListNominal: RecyclerView = findViewGroupById(R.id.rv_nominal)
        val rvListPeriod: RecyclerView = findViewGroupById(R.id.rv_periode)

        var listFilterTypeValue = listFilterType

        isAnalysis.let {
            when (it) {
                true -> {
                    listFilterTypeValue = listFilterType
                    txtNominal.visibility = View.GONE
                }
                false -> {
                    val adapterFilterNominal = AdapterFilter(
                        context, listFilterNominal
                    ) { data: List<Any>, position: Int ->
                        data as List<ModelFilter>
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
        ) { data: List<Any>, position: Int ->
            data as List<ModelFilter>
            dialogFilterFinanceCallback.resultFilterType(data[position].value)
        }

        rvListType.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        rvListType.adapter = adapterFilterType


        val adapterFilterPeriod = AdapterFilter(
            context, listFilterPeriod
        ) { data: List<Any>, position: Int ->
            data as List<ModelFilter>
            dialogFilterFinanceCallback.resultFilterPeriod(data[position].value)
        }
        rvListPeriod.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        rvListPeriod.adapter = adapterFilterPeriod

        txtSubmit onClick View.OnClickListener {
            dialog.dismiss()
            dialogFilterFinanceCallback.onSubmit()
        }

        val window = dialog.window
        val wlp = window!!.attributes

        wlp.gravity = Gravity.CENTER
        wlp.flags = wlp.flags and WindowManager.LayoutParams.FLAG_BLUR_BEHIND.inv()
        window.attributes = wlp
        dialog.window!!.setLayout(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )

        dialog.show()
    }

     infix fun View.onClick(onclick: View.OnClickListener) {
        setOnClickListener(onclick)
    }

     fun <T : View> findViewById(value: Int): T {
        return dialogView.findViewById(value)
    }

     fun <T : ViewGroup> findViewGroupById(value: Int): T {

        return dialogView.findViewById(value)
    }

    interface DialogFilterFinanceCallback {
        fun resultFilterType(result: String)
        fun resultFilterNominal(result: String)
        fun resultFilterPeriod(result: String)
        fun onSubmit()
    }
}
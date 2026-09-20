package id.co.evolution.financefy.dialog

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import id.co.evolution.financefy.R
import id.co.evolution.financefy.adapter.AdapterFilter
import id.co.evolution.financefy.model.ModelFilter

class DialogFilterSavings(
     context: Context,
     inflater: LayoutInflater,
     dialogFilterSavingsCallback: DialogFilterFinanceCallback
):DialogFilterFinance(context,inflater,dialogFilterSavingsCallback) {

    fun showDialogFilterSavings(
        listFilterNominal: List<ModelFilter>,
        listFilterPeriod: List<ModelFilter>, isAnalysis: Boolean = false
    ) {
        val txtSubmit = findViewById<MaterialButton>(R.id.btn_submit)
        val txtNominal = findViewById<TextView>(R.id.txt_nominal)
        val txtType = findViewById<TextView>(R.id.txt_type)
        val rvListNominal: RecyclerView = findViewGroupById(R.id.rv_nominal)
        val rvListPeriod: RecyclerView = findViewGroupById(R.id.rv_periode)
        txtType.visibility = View.GONE


        isAnalysis.let {
            when (it) {
                true -> {
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
}
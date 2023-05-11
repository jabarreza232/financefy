package id.co.evolution.financefy.dialog

import android.app.Dialog
import android.content.Context
import android.util.TypedValue
import android.view.*
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import id.co.evolution.financefy.R
import id.co.evolution.financefy.adapter.AdapterSavingsTarget
import id.co.evolution.financefy.helper.TinyDb
import id.co.evolution.financefy.helper.Tools
import id.co.evolution.financefy.model.ModelSavings
import java.util.*

class DialogSavings(
    val context: Context,
    val inflater: LayoutInflater,
    private val dialogSavingsCallback: DialogSavingsCallback
) {
    val dialog: Dialog = Dialog(context)
    val dialogView: View = inflater.inflate(R.layout.dialog_choose_savings_target, null)
    private val btnInProgress: Button = findViewById(R.id.btn_in_progress);
    private val btnAchieved: Button = findViewById(R.id.btn_achieved);
    private val btnAll: Button = findViewById(R.id.btn_all);
    private val placeEmpty: LinearLayout = findViewGroupById(R.id.place_empty)
    val value:TypedValue= TypedValue()
     var locale: Locale
     var tinyDb:TinyDb

    init {
        dialog.setContentView(dialogView)
        context.theme.resolveAttribute(android.R.attr.textColorPrimary,value,true)
        tinyDb = TinyDb(context)
        locale = if(tinyDb.getString("currency").equals("IDR",true)){
            Tools.getLocaleIDN()
        }else{
            Tools.getLocaleUS()
        }

    }

    fun showDialogSavings(mutableSavingsData: MutableList<ModelSavings>) {
        val rvListSavingsTarget: RecyclerView = findViewGroupById(R.id.rv_savings_target)

        val adapter = AdapterSavingsTarget(
            mutableSavingsData
        ) { type, data, position ->
            dialog.dismiss()
            dialogSavingsCallback.onSubmit(
                type as Tools.TYPE,
                position,
                mutableSavingsData[position].title
            )
        }
        adapter.setLocale(locale)
        var mutableListData: MutableList<ModelSavings> = mutableListOf()

        rvListSavingsTarget.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        rvListSavingsTarget.adapter = adapter

        btnInProgress.setOnClickListener {
            mutableListData = mutableListOf()
            setBackgroundButton(
                R.drawable.button_blue_background,
                R.drawable.shape_line_black,
                R.drawable.shape_line_black
            )
            setTextColorButton(R.color.white, value.resourceId, value.resourceId)

            mutableSavingsData.forEach {
                if (!it.getPercentage(context).contains(context.getString(R.string.achieved)))
                    mutableListData.add(it)
            }
            adapter.bindData(mutableListData)
            setUpPlaceEmpty(adapter)
        }

        btnAchieved.setOnClickListener {
            mutableListData = mutableListOf()
            setBackgroundButton(
                R.drawable.shape_line_black,
                R.drawable.button_blue_background,
                R.drawable.shape_line_black
            )
            setTextColorButton(value.resourceId, R.color.white, value.resourceId)

            mutableSavingsData.forEach {
                if (it.getPercentage(context).contains(context.getString(R.string.achieved)))
                    mutableListData.add(it)
            }
            adapter.bindData(mutableListData)
            setUpPlaceEmpty(adapter)
        }

        btnAll.setOnClickListener {
            setBackgroundButton(
                R.drawable.shape_line_black,
                R.drawable.shape_line_black,
                R.drawable.button_blue_background
            )
            setTextColorButton(value.resourceId, value.resourceId, R.color.white)
            adapter.bindData(mutableSavingsData)
            setUpPlaceEmpty(adapter)
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
    private fun setUpPlaceEmpty(adapter:AdapterSavingsTarget){
        placeEmpty.visibility = if (adapter.itemCount == 0)
            View.VISIBLE
        else View.GONE

    }
    private fun setBackgroundButton(
        bgButtonProgress: Int,
        bgButtonAchieved: Int,
        bgButtonAll: Int
    ) {
        btnInProgress.background = ContextCompat.getDrawable(context, bgButtonProgress)
        btnAchieved.background = ContextCompat.getDrawable(context, bgButtonAchieved)
        btnAll.background = ContextCompat.getDrawable(context, bgButtonAll)
    }

    private fun setTextColorButton(
        bgButtonProgress: Int,
        bgButtonAchieved: Int,
        bgButtonAll: Int
    ) {
        btnInProgress.setTextColor(ContextCompat.getColor(context, bgButtonProgress))
        btnAchieved.setTextColor(ContextCompat.getColor(context, bgButtonAchieved))
        btnAll.setTextColor(ContextCompat.getColor(context, bgButtonAll))
    }

    private fun <T : View> findViewById(value: Int): T {
        return dialogView.findViewById(value)
    }

    private fun <T : ViewGroup> findViewGroupById(value: Int): T {

        return dialogView.findViewById(value)
    }

    interface DialogSavingsCallback {
        fun onSubmit(type: Tools.TYPE, index: Int = 0, result: String)
    }

}
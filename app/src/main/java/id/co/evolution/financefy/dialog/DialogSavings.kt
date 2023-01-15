package id.co.evolution.financefy.dialog

import android.app.Dialog
import android.content.Context
import android.view.*
import android.widget.RelativeLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import id.co.evolution.financefy.R
import id.co.evolution.financefy.adapter.AdapterSavingsTarget

class DialogSavings(
   val context: Context,
    val inflater: LayoutInflater,
    private val dialogSavingsCallback: DialogSavingsCallback
) {
    val dialog: Dialog = Dialog(context)
    val dialogView: View = inflater.inflate(R.layout.dialog_choose_savings_target, null)

    init {
        dialog.setContentView(dialogView)
    }

    fun showDialogSavings(resourceTypeArray: MutableList<String>) {
        val rvListSavingsTarget: RecyclerView = findViewGroupById(R.id.rv_savings_target)

        val adapter = AdapterSavingsTarget(resourceTypeArray
        ) { type, data, position ->
            dialog.dismiss()
            dialogSavingsCallback.onSubmit(position, resourceTypeArray[position])
        }

        rvListSavingsTarget.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        rvListSavingsTarget.adapter = adapter

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


    private fun <T : View> findViewById(value: Int): T {
        return dialogView.findViewById(value)
    }

    private fun <T : ViewGroup> findViewGroupById(value: Int): T {

        return dialogView.findViewById(value)
    }

    interface DialogSavingsCallback {
        fun onSubmit(index:Int=0,result: String)
    }

}
package id.co.evolution.financefy.dialog

import android.app.Dialog
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import id.co.evolution.financefy.R
import id.co.evolution.financefy.adapter.AdapterFilter
import id.co.evolution.financefy.model.ModelFilter

class DialogConfirm(
     context: Context,
     inflater: LayoutInflater,
     dialogConfirm: DialogConfirm) {

    val dialog: Dialog = Dialog(context)
    val dialogConfirm:DialogConfirm = dialogConfirm
    val dialogView: View = inflater.inflate(R.layout.dialog_confirm, null)

    init {
        dialog.setContentView(dialogView)
    }

    fun showDialogConfirm(
        title: String,
        description: String
    ) {
        val txtTitle = findViewById<TextView>(R.id.txt_title)
        val txtDescription = findViewById<TextView>(R.id.txt_description)
        val btnYes = findViewById<TextView>(R.id.btn_yes)
        val btnNo = findViewById<TextView>(R.id.btn_no)
        txtTitle.text = title
        txtDescription.text = description

        btnYes onClick View.OnClickListener {
            dialog.dismiss()
            dialogConfirm.onSubmit("Yes")
        }
        btnNo onClick View.OnClickListener {
            dialog.dismiss()
            dialogConfirm.onSubmit("No")
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
    interface DialogConfirm {
        fun onSubmit(result:String)
    }
}
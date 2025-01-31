package id.co.evolution.financefy.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Window
import android.widget.TextView
import id.co.evolution.financefy.R

class DialogLoading {
    val dialog: Dialog

    constructor(context: Context) {
        dialog = Dialog(context)
//        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_loading)
        dialog.setCancelable(false)

//        dialog.getWindow()?.setBackgroundDrawable( ColorDrawable(Color.TRANSPARENT));
//        dialog.getWindow()?.setDimAmount(0.6f); // Efek blur di belakang dialog

    }

    fun show(message:String){
        val textView = dialog.findViewById<TextView>(R.id.tvLoading)
        textView.text = message
        dialog.show()
    }

    fun dismiss(){
        dialog.dismiss()
    }
}
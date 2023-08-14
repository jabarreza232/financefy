package id.co.evolution.financefy.dialog

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.view.View
import id.co.evolution.financefy.R

open class DialogFinance(val context: Context, private val dialogFinanceCallback: DialogFinanceCallback) {
    val builder: AlertDialog.Builder = AlertDialog.Builder(context)
    private lateinit var arrayCategory: Array<String>


    fun showDialog(resourceTypeArray:Int,title:String) {
        builder.setTitle(title)
        val arrayType = context.resources.getStringArray(resourceTypeArray)
        builder.setItems(arrayType) { dialog, which ->
            run {
                dialog.dismiss()
                dialogFinanceCallback.onSubmit(which,arrayType[which])
            }
        }

        val alertDialog: AlertDialog = builder.create()
        alertDialog.show()
    }



    interface DialogFinanceCallback {
        fun onSubmit(index:Int=0,result: String)
    }
}
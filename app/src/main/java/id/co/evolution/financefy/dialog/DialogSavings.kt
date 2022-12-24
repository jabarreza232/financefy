package id.co.evolution.financefy.dialog

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.view.View
import id.co.evolution.financefy.R

class DialogSavings(context: Context, private val dialogSavingsCallback: DialogFinanceCallback):DialogFinance(context,dialogSavingsCallback) {
    fun showDialogSavings(resourceTypeArray:MutableList<String>) {
        builder.setTitle("Pilih Tabungan")
        val arrayType = resourceTypeArray.toTypedArray()
        builder.setItems(arrayType) { dialog, which ->
            run {
                dialog.dismiss()
                dialogSavingsCallback.onSubmit(which,arrayType[which])
            }
        }

        val alertDialog: AlertDialog = builder.create()
        alertDialog.show()
    }

}
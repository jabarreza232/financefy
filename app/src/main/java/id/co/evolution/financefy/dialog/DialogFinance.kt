package id.co.evolution.financefy.dialog

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.view.View
import id.co.evolution.financefy.R

class DialogFinance(val context: Context, private val dialogFinanceCallback: DialogFinanceCallback) {
    val builder: AlertDialog.Builder = AlertDialog.Builder(context)
    private lateinit var arrayCategory: Array<String>


    fun showDialogCategory(resourceCategoryArray: Int) {
        arrayCategory = context.resources.getStringArray(resourceCategoryArray)
        builder.setTitle("Pilih Kategori")
        builder.setItems(arrayCategory) { dialog, which ->
            run {
                dialog.dismiss()
                dialogFinanceCallback.onSubmit(which,arrayCategory[which])
            }
        }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.show()
    }

    fun showDialogType(resourceTypeArray:Int) {
        builder.setTitle("Pilih Tipe")
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



    fun showDialogSavings(resourceTypeArray:MutableList<String>) {
        builder.setTitle("Pilih Tabungan")
        val arrayType = resourceTypeArray.toTypedArray()
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
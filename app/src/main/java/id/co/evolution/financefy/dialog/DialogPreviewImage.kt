package id.co.evolution.financefy.dialog

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.widget.ImageView
import android.widget.TextView
import id.co.evolution.financefy.R
import id.co.evolution.financefy.activity.FullScreenPhotoActivity
import id.co.evolution.financefy.helper.Tools

class DialogPreviewImage {
    val dialog: Dialog
    val context:Context
    constructor(context: Context) {
        dialog = Dialog(context)
        this.context = context
//        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_image_preview)
        dialog.setCancelable(false)

        dialog.getWindow()?.setBackgroundDrawable( ColorDrawable(Color.TRANSPARENT));
//        dialog.getWindow()?.setDimAmount(0.6f); // Efek blur di belakang dialog

    }

    fun show(filePath:String){
        val previewImage = dialog.findViewById<ImageView>(R.id.previewImage)
        val imageClose = dialog.findViewById<ImageView>(R.id.btnClose);

        previewImage.setImageBitmap(Tools.convertFileToBitmap(filePath))

       previewImage.setOnClickListener {
           val intent = Intent(context,FullScreenPhotoActivity::class.java)
           intent.putExtra("path",filePath)
           context.startActivity(intent)
       }
        imageClose.setOnClickListener { dismiss() }
        dialog.show()
    }

    fun dismiss(){
        dialog.dismiss()
    }
}
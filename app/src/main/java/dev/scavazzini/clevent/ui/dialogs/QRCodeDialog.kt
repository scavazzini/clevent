package dev.scavazzini.clevent.ui.dialogs

import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import dev.scavazzini.clevent.R

class QRCodeDialog(context: Context) : Dialog(context) {

    private val imageView: ImageView
    private val progressBar: ProgressBar
    private var bitmap: Bitmap? = null

    fun setBitmap(bitmap: Bitmap?) {
        this.bitmap = bitmap
        if (isShowing) {
            updateContent()
        }
    }

    override fun show() {
        updateContent()
        super.show()
    }

    private fun updateContent() {
        if (this.bitmap != null) {
            imageView.setImageBitmap(this.bitmap)
            imageView.visibility = View.VISIBLE
            progressBar.visibility = View.GONE
        } else {
            imageView.visibility = View.GONE
            progressBar.visibility = View.VISIBLE
        }
    }

    init {
        setContentView(R.layout.dialog_qrcode)
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        imageView = findViewById(R.id.imageview_qrcode)
        progressBar = findViewById(R.id.progress_qrcode)
    }
}

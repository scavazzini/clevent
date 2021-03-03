package dev.scavazzini.clevent.ui.dialogs

import android.app.Dialog
import android.content.Context
import dev.scavazzini.clevent.R
import dev.scavazzini.clevent.databinding.DialogNfcActionBinding
import dev.scavazzini.clevent.ui.dialogs.NFCDialog.Type.*

class NFCDialog(context: Context) : Dialog(context) {
    enum class Type { WAITING_READ_DIALOG, SUCCESS_DIALOG, ERROR_DIALOG }
    private val binding: DialogNfcActionBinding = DialogNfcActionBinding.inflate(layoutInflater)

    init {
        setContentView(binding.root)
        window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    fun withActionButton(actionText: String, action: () -> Unit) {
        binding.actionText = actionText
        binding.buttonNfcAction.setOnClickListener {
            dismiss()
            action()
        }
    }

    fun isWaitingForRead(): Boolean = binding.type == WAITING_READ_DIALOG && isShowing

    fun showSuccess(title: String = "", description: String = "") =
            show(SUCCESS_DIALOG, title, description)
    fun showError(title: String = "", description: String = "") =
            show(ERROR_DIALOG, title, description)
    fun showWaitingForRead(title: String = "", description: String = "") =
            show(WAITING_READ_DIALOG, title, description)

    private fun show(dialogType: Type, title: String = "", description: String = "") {
        val (background, iconResource) = when (dialogType) {
            SUCCESS_DIALOG -> Pair(R.color.greenSuccess, R.drawable.ic_check_circle_24)
            ERROR_DIALOG -> Pair(R.color.redError, R.drawable.ic_error_24)
            WAITING_READ_DIALOG -> Pair(R.color.colorPrimaryDark, R.drawable.ic_nfc_24)
        }
        binding.parentDialog.setBackgroundResource(background)
        binding.type = dialogType
        binding.icon = iconResource
        binding.title = title
        binding.description = description
        super.show()
    }

    override fun dismiss() {
        super.dismiss()
        binding.actionText = ""
    }
}

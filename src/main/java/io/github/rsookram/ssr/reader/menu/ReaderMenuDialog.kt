package io.github.rsookram.ssr.reader.menu

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.net.Uri

class ReaderMenuDialog(private val uri: Uri) {

    fun show(context: Context): Dialog {
        val vm = ReaderMenuViewModel(context, uri)

        val view = ReaderMenuView(context, vm)

        val dialog = AlertDialog.Builder(context)
            .setView(view)
            .setOnDismissListener { vm.onCleared() }
            .show()

        vm.onState = view::bind
        vm.onDismiss = { dialog.dismiss() }

        return dialog
    }
}

package io.github.rsookram.ssr.reader.menu

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.net.Uri
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class ReaderMenuDialog(private val uri: Uri) {

    fun show(context: Context): Dialog {
        val scope = MainScope()
        val vm = ReaderMenuViewModel(context, scope)

        val view = ReaderMenuView(context, vm)

        val dialog = AlertDialog.Builder(context)
            .setView(view)
            .setOnDismissListener { scope.cancel() }
            .show()

        vm.setUri(uri)

        vm.states
            .onEach(view::bind)
            .launchIn(scope)

        vm.onDismiss = { dialog.dismiss() }

        return dialog
    }
}

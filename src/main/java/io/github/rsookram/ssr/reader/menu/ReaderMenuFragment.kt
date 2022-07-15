package io.github.rsookram.ssr.reader.menu

import android.app.AlertDialog
import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

// TODO: Fix bug with config change while this is shown
@AndroidEntryPoint
class ReaderMenuFragment : DialogFragment() {

    private val vm: ReaderMenuViewModel by viewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = ReaderMenuView(requireContext(), vm)

        vm.states
            .onEach(view::bind)
            .launchIn(lifecycleScope)

        vm.dismiss.observe(this) { dismiss() }

        return AlertDialog.Builder(requireContext())
            .setView(view)
            .create()
    }

    companion object {

        fun newInstance(bookUri: Uri): DialogFragment =
            ReaderMenuFragment().apply {
                arguments = bundleOf(ReaderMenuViewModel.KEY_URI to bookUri)
            }
    }
}

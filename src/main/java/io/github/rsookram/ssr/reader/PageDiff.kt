package io.github.rsookram.ssr.reader

import androidx.recyclerview.widget.DiffUtil
import io.github.rsookram.page.CroppedPage

class PageDiff : DiffUtil.ItemCallback<CroppedPage>() {

    override fun areItemsTheSame(oldItem: CroppedPage, newItem: CroppedPage): Boolean =
        oldItem.page == newItem.page

    override fun areContentsTheSame(oldItem: CroppedPage, newItem: CroppedPage): Boolean =
        oldItem == newItem
}

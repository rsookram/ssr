package io.github.rsookram.ssr.reader

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.github.rsookram.page.CroppedPage
import io.github.rsookram.page.Page
import io.github.rsookram.ssr.reader.view.PageView
import io.github.rsookram.ssr.reader.view.ScaleImage

class PageAdapter(
    private val scaleImage: ScaleImage
) : ListAdapter<CroppedPage, PageAdapter.Holder>(PageDiff()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder =
        Holder(
            PageView(parent.context, scaleImage)
        )

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val page = getItem(position)
        holder.pageView.bind(page)
    }

    class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val pageView = itemView as PageView
    }
}

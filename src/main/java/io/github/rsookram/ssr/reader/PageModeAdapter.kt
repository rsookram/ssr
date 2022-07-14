package io.github.rsookram.ssr.reader

import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.github.rsookram.page.CroppedPage
import io.github.rsookram.page.Page
import io.github.rsookram.ssr.reader.view.ImageScaler
import io.github.rsookram.ssr.reader.view.PageView

class PageModeAdapter : ListAdapter<CroppedPage, PageModeAdapter.Holder>(PageDiff()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder =
        Holder(
            FrameLayout(parent.context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )

                addView(
                    PageView(
                        parent.context,
                        ImageScaler.fitCenter
                    ),
                    FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        Gravity.CENTER
                    )
                )
            }
        )

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val page = getItem(position)
        holder.pageView.bind(page)
    }

    class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val pageView = (itemView as ViewGroup).getChildAt(0) as PageView
    }
}

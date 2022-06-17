package cn.ifafu.ifafu.ui.common.recycleview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.RecyclerView

fun <VH : RecyclerView.ViewHolder> RecyclerView.Adapter<VH>.withHeaderAndFooter(
    header: SingleAdapter<*>,
    footer: SingleAdapter<*>
): ConcatAdapter {
    return ConcatAdapter(header, this, footer)
}

fun <VH : RecyclerView.ViewHolder> RecyclerView.Adapter<VH>.withHeader(
    header: SingleAdapter<*>
): ConcatAdapter {
    return ConcatAdapter(header, this)
}

fun <VH : RecyclerView.ViewHolder> RecyclerView.Adapter<VH>.withFooter(
    footer: SingleAdapter<*>
): ConcatAdapter {
    return ConcatAdapter(this, footer)
}

fun <VH : RecyclerView.ViewHolder> RecyclerView.Adapter<VH>.withHeaderAndFooter(
    @LayoutRes header: Int,
    @LayoutRes footer: Int
): ConcatAdapter {
    return ConcatAdapter(SingleAdapterImpl(header), this, SingleAdapterImpl(footer))
}

fun <VH : RecyclerView.ViewHolder> RecyclerView.Adapter<VH>.withHeader(
    @LayoutRes header: Int
): ConcatAdapter {
    return ConcatAdapter(SingleAdapterImpl(header), this)
}

fun <VH : RecyclerView.ViewHolder> RecyclerView.Adapter<VH>.withFooter(
    @LayoutRes footer: Int
): ConcatAdapter {
    return ConcatAdapter(this, SingleAdapterImpl(footer))
}

abstract class SingleAdapter<VH : RecyclerView.ViewHolder> : RecyclerView.Adapter<VH>() {
    override fun getItemCount(): Int = 1
}

internal class SingleAdapterImpl(
    @LayoutRes private val resId: Int
) : SingleAdapter<SingleAdapterImpl.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(resId, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
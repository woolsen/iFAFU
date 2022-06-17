package cn.ifafu.ifafu.ui.view

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import cn.ifafu.ifafu.R
import cn.ifafu.ifafu.bean.vo.MenuVO
import cn.ifafu.ifafu.util.DensityUtils
import cn.ifafu.ifafu.ui.view.listener.OnMenuItemClickListener

class MenuMaker {

    private lateinit var menu: Map<String, List<MenuVO>>
    private lateinit var context: Context
    private lateinit var layout: LinearLayout
    private lateinit var listener: OnMenuItemClickListener

    fun menu(menu: Map<String, List<MenuVO>>) {
        this.menu = menu
    }

    fun layout(layout: LinearLayout) {
        context = layout.context
        this.layout = layout
    }

    fun onMenuClickListener(listener: OnMenuItemClickListener) {
        this.listener = listener
    }

    fun make() {
        val rootLayout = layout
        val listener = listener
        rootLayout.removeViews(5, rootLayout.childCount - 5)
        menu.forEach { (title, itemList) ->
            rootLayout.addView(getSplitLine())
            rootLayout.addView(getMenuTitle(title))
            itemList.forEachIndexed { index, item ->
                if (index != 0) {
                    rootLayout.addView(getSplitLine(DensityUtils.dp2px(context, 20F)))
                }
                rootLayout.addView(getMenuItem(item, listener))
            }
        }
    }

    private fun getMenuItem(item: MenuVO, listener: OnMenuItemClickListener): View {
        //Tab根布局
        val itemLayout = LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                marginStart = DensityUtils.dp2px(context, 20F)
            }
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        //图标
        val itemIcon = ImageView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                    DensityUtils.dp2px(context, 30F),
                    DensityUtils.dp2px(context, 30F))
            setImageResource(item.icon)
        }

        //菜单文本
        val itemText = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(0, DensityUtils.dp2px(context, 50F)).apply {
                marginStart = DensityUtils.dp2px(context, 8F)
                weight = 1F
            }
            text = item.title
            textSize = 18F
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER_VERTICAL or Gravity.START
        }

        //箭头
        val goIcon = ImageView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                    DensityUtils.dp2px(context, 50F),
                    DensityUtils.dp2px(context, 15F))
            setImageResource(R.drawable.ic_right)
        }
        itemLayout.addView(itemIcon)
        itemLayout.addView(itemText)
        itemLayout.addView(goIcon)
        itemLayout.setOnClickListener {
            listener.onMenuItemClick(item)
        }

        return itemLayout
    }

    private fun getMenuTitle(title: String): View {
        return TextView(context).apply {
            text = title
            layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    DensityUtils.dp2px(context, 24f)).apply {
                marginStart = DensityUtils.dp2px(context, 12f)
            }
            textSize = 12f
            setTextColor(Color.parseColor("#ffffff"))
            gravity = Gravity.CENTER_VERTICAL or Gravity.START
        }
    }

    private fun getSplitLine(marginStart: Int = 0): View {
        return View(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    DensityUtils.dp2px(context, 0.5F)).apply {
                setMarginStart(marginStart)
            }
            setBackgroundColor(Color.WHITE)
        }
    }

    companion object {
        fun make(make: MenuMaker.() -> Unit) {
            val maker = MenuMaker()
            make(maker)
            maker.make()
        }
    }

}
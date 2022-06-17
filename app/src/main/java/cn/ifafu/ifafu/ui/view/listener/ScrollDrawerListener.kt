package cn.ifafu.ifafu.ui.view.listener

import android.view.View
import android.widget.LinearLayout
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView

class ScrollDrawerListener(val nav: NavigationView, val content: LinearLayout) : DrawerLayout.DrawerListener {
    override fun onDrawerStateChanged(newState: Int) {
    }

    override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
        content.layout(nav.right, 0, nav.right + content.width, nav.bottom)

    }

    override fun onDrawerClosed(drawerView: View) {
    }

    override fun onDrawerOpened(drawerView: View) {
    }

}
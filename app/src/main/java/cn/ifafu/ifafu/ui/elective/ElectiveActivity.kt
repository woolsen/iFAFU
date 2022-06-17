package cn.ifafu.ifafu.ui.elective

import android.os.Bundle
import cn.ifafu.ifafu.R
import cn.ifafu.ifafu.ui.common.BaseActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ElectiveActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.elective_activity)
        setLightUiBar()
    }

}
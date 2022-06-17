package cn.ifafu.ifafu.ui.score

import android.os.Bundle
import cn.ifafu.ifafu.R
import cn.ifafu.ifafu.ui.common.BaseActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ScoreActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.score_activity)

        setLightUiBar()
    }

}
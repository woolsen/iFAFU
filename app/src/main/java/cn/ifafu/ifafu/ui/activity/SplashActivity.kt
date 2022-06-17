package cn.ifafu.ifafu.ui.activity

import android.R.anim
import android.content.Intent
import android.os.Bundle
import android.view.Window
import android.view.WindowManager.LayoutParams
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import cn.ifafu.ifafu.IFAFU
import cn.ifafu.ifafu.R
import cn.ifafu.ifafu.constant.Constants
import cn.ifafu.ifafu.db.dao.UserDao
import cn.ifafu.ifafu.ui.examlist.ExamListActivity
import cn.ifafu.ifafu.ui.login.LoginActivity
import cn.ifafu.ifafu.ui.main.MainActivity
import cn.ifafu.ifafu.ui.timetable.TimetableActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    @Inject
    lateinit var userDao: UserDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //去掉窗口标题
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        //隐藏顶部状态栏
        window.addFlags(LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.splash_activity)

        lifecycleScope.launchWhenResumed {
            IFAFU.initConfig(application, userDao.getUsingUser())
            val user = withContext(Dispatchers.IO) {
                userDao.getUsingUser()
            }
            if (user == null) {
                startActivityThenFinishSelf(LoginActivity::class.java)
                return@launchWhenResumed
            }
            when (intent.getIntExtra("from", -1)) {
                Constants.ACTIVITY_SYLLABUS -> {
                    startActivityThenFinishSelf(TimetableActivity::class.java)
                }
                Constants.ACTIVITY_EXAM -> {
                    startActivityThenFinishSelf(ExamListActivity::class.java)
                }
                Constants.SYLLABUS_WIDGET -> {
                    startActivityThenFinishSelf(TimetableActivity::class.java)
                }
                else -> {
                    startActivityThenFinishSelf(MainActivity::class.java)
                }
            }
        }
    }

    private fun startActivityThenFinishSelf(clazz: Class<*>) {
        val intent = Intent(this, clazz).apply {
            putExtra(Constants.EXTRA_ORIGIN, Constants.ACTIVITY_SPLASH)
        }
        overridePendingTransition(anim.fade_in, anim.fade_out)
        startActivity(intent)
        finish()
    }

}
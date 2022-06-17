package cn.ifafu.ifafu.ui.main

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import cn.ifafu.ifafu.R
import cn.ifafu.ifafu.bean.vo.MenuVO
import cn.ifafu.ifafu.constant.Constants
import cn.ifafu.ifafu.ui.activity.AboutActivity
import cn.ifafu.ifafu.ui.activity.BoyaActivity
import cn.ifafu.ifafu.ui.elective.ElectiveActivity
import cn.ifafu.ifafu.ui.electricity.main.ElectricityActivity
import cn.ifafu.ifafu.ui.examlist.ExamListActivity
import cn.ifafu.ifafu.ui.feedback.FeedbackActivity
import cn.ifafu.ifafu.ui.score.ScoreActivity
import cn.ifafu.ifafu.ui.timetable.TimetableActivity
import cn.ifafu.ifafu.ui.web.WebActivity
import cn.ifafu.ifafu.ui.center.CenterActivity
import cn.ifafu.ifafu.ui.comment.CommentActivity
import cn.ifafu.ifafu.ui.information.InformationActivity

class MainMenuHandler(private val context: Context) {

    /**
     * 处理Menu点击事件
     *
     * @return 处理成功返回true，否则返回false
     */
    fun handle(menu: MenuVO): Boolean {
        when (menu.id) {
            R.id.menu_exam_list ->
                startActivityByClazz(ExamListActivity::class.java)
            R.id.menu_score_list ->
                startActivityByClazz(ScoreActivity::class.java)
            R.id.menu_electricity ->
                startActivityByClazz(ElectricityActivity::class.java)
            R.id.menu_elective ->
                startActivityByClazz(ElectiveActivity::class.java)
            R.id.menu_schedule ->
                startActivityByClazz(TimetableActivity::class.java)
            R.id.menu_web ->
                startActivityByClazz(WebActivity::class.java)
            R.id.menu_repair -> {
                val intent = WebActivity.intentFor(
                        context, Constants.REPAIR_URL, "报修服务")
                context.startActivity(intent)
            }
            R.id.menu_comment -> {
                startActivityByClazz(CommentActivity::class.java)
            }
            R.id.menu_boya -> {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse(Constants.BOYA_URL)
                    context.startActivity(intent)
                } else {
                    startActivityByClazz(BoyaActivity::class.java)
                }
            }
            R.id.menu_person_center ->
                startActivityByClazz(CenterActivity::class.java)
            R.id.menu_feedback ->
                startActivityByClazz(FeedbackActivity::class.java)
            R.id.menu_information ->
                startActivityByClazz(InformationActivity::class.java)
            R.id.menu_about_ifafu ->
                startActivityByClazz(AboutActivity::class.java)
            else -> return false
        }
        return true
    }

    private fun startActivityByClazz(clazz: Class<out Activity>) {
        val intent = Intent(context, clazz)
        context.startActivity(intent)
    }

}
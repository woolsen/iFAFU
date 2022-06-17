package cn.ifafu.ifafu.ui.main.old_theme

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import cn.ifafu.ifafu.R
import cn.ifafu.ifafu.ui.view.MenuMaker
import cn.ifafu.ifafu.constant.Constants
import cn.ifafu.ifafu.bean.vo.MenuVO
import cn.ifafu.ifafu.databinding.MainOldFragmentBinding
import cn.ifafu.ifafu.ui.main.MainMenuHandler
import cn.ifafu.ifafu.ui.main.MainViewModel
import cn.ifafu.ifafu.ui.main.old_theme.coursepreview.CoursePreviewFragment
import cn.ifafu.ifafu.ui.main.old_theme.exampreview.ExamPreviewFragment
import cn.ifafu.ifafu.ui.main.old_theme.scorepreview.ScorePreviewFragment
import cn.ifafu.ifafu.ui.setting.SettingActivity
import cn.ifafu.ifafu.ui.view.listener.OnMenuItemClickListener
import cn.ifafu.ifafu.ui.view.listener.ScrollDrawerListener
import cn.ifafu.ifafu.ui.common.BaseFragment
import com.google.android.material.navigation.NavigationView
import com.gyf.immersionbar.ImmersionBar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainOldFragment : BaseFragment(R.layout.main_old_fragment), OnMenuItemClickListener {

    private lateinit var menuHandler: MainMenuHandler

    private val mViewModel: MainOldViewModel by viewModels()
    private val activityViewModel: MainViewModel by activityViewModels()

    private lateinit var mDrawerLayout: DrawerLayout

    private lateinit var mExamPreviewFragment: ExamPreviewFragment
    private lateinit var mCoursePreviewFragment: CoursePreviewFragment
    private lateinit var mScorePreviewFragment: ScorePreviewFragment
    private lateinit var binding: MainOldFragmentBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = MainOldFragmentBinding.bind(view)
        ImmersionBar.with(this)
            .titleBar(binding.tbMainOld)
            .init()

        binding.lifecycleOwner = this
        binding.viewModel = mViewModel
        binding.activityViewModel = activityViewModel
        mDrawerLayout = binding.layoutDrawer

        mCoursePreviewFragment = CoursePreviewFragment()
        mExamPreviewFragment = ExamPreviewFragment()
        mScorePreviewFragment = ScorePreviewFragment()

        childFragmentManager.commit {
            replace(R.id.course_preview, mCoursePreviewFragment)
            replace(R.id.exam_preview, mExamPreviewFragment)
            replace(R.id.score_preview, mScorePreviewFragment)
        }

        initLeftMenu(view)
        initEvent(view)

    }

    private fun initEvent(rootView: View) {
        binding.tvName.setOnClickListener {
            mDrawerLayout.openDrawer(GravityCompat.START)
        }
        val leftMenuLayout = rootView.findViewById<NavigationView>(R.id.nav_left_menu)
        val contentLayout = rootView.findViewById<LinearLayout>(R.id.layout_content)
        if (leftMenuLayout != null && contentLayout != null) {
            val drawerListener = ScrollDrawerListener(leftMenuLayout, contentLayout)
            mDrawerLayout.addDrawerListener(drawerListener)
        }
    }

    override fun onStart() {
        super.onStart()
        mDrawerLayout.closeDrawer(GravityCompat.START)
        mExamPreviewFragment.refresh()
        mCoursePreviewFragment.refresh()
        mScorePreviewFragment.refresh()
    }

    private fun initLeftMenu(rootView: View) {
        menuHandler = MainMenuHandler(requireContext())
        val menuLayout = rootView.findViewById<LinearLayout>(R.id.layout_left_menu) ?: return
        val menu = mapOf(
            "信息查询" to mutableListOf(
                MenuVO(R.id.menu_exam_list, R.drawable.menu_score_white, "成绩查询"),
                MenuVO(R.id.menu_exam_list, R.drawable.menu_exam_white, "学生考试查询"),
//                MenuVO(R.id.menu_electricity, R.drawable.menu_elec_white, "电费查询"),
                MenuVO(R.id.menu_elective, R.drawable.menu_elective_white, "选修学分查询")
            ),
            "实用工具" to mutableListOf(
                MenuVO(R.id.menu_schedule, R.drawable.menu_syllabus_white, "我的课表"),
                MenuVO(R.id.menu_web, R.drawable.menu_web_white, "网页模式"),
//                MenuVO(R.id.menu_repair, R.drawable.main_old_tabs_repair, "报修服务"),
                MenuVO(R.id.menu_information, R.drawable.ic_information_white, "信息平台"),
                MenuVO(R.id.menu_boya, R.drawable.ic_robot_white, "校园百事通")
            ),
            "软件设置" to mutableListOf(
                MenuVO(R.id.menu_setting, R.drawable.menu_setting_white, "软件设置"),
                MenuVO(R.id.menu_user_management, R.drawable.main_old_tabs_manage, "账号管理")
            ),
            "关于软件" to mutableListOf(
                MenuVO(R.id.menu_upgrade, R.drawable.menu_update_white, "检查更新"),
                MenuVO(R.id.menu_about_ifafu, R.drawable.main_old_tabs_about, "关于iFAFU"),
                MenuVO(R.id.menu_feedback, R.drawable.ic_feedback_white, "反馈问题")
            )
        )
        MenuMaker.make {
            menu(menu)
            layout(menuLayout)
            onMenuClickListener(this@MainOldFragment)
        }
        activityViewModel.isShowComment.observe(viewLifecycleOwner) { showComment ->
            if (showComment) {
                menu["实用工具"]?.add(
                    MenuVO(
                        R.id.menu_comment,
                        R.drawable.main_old_tabs_comment,
                        "一键评教"
                    )
                )
                MenuMaker.make {
                    menu(menu)
                    layout(menuLayout)
                    onMenuClickListener(this@MainOldFragment)
                }
            }
        }
    }

    override fun onMenuItemClick(menu: MenuVO) {
        if (!menuHandler.handle(menu)) {
            when (menu.id) {
                R.id.menu_setting ->
                    requireActivity().startActivityForResult(
                        Intent(context, SettingActivity::class.java),
                        Constants.ACTIVITY_SETTING
                    )
                R.id.menu_user_management ->
                    activityViewModel.showMultiUserDialog()
                R.id.menu_upgrade ->
                    activityViewModel.upgradeApp()
            }
        }
        mDrawerLayout.closeDrawer(GravityCompat.START)
    }

}
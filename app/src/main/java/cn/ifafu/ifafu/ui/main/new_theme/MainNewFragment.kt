package cn.ifafu.ifafu.ui.main.new_theme

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import cn.ifafu.ifafu.R
import cn.ifafu.ifafu.ui.common.BaseFragment
import cn.ifafu.ifafu.constant.Constants
import cn.ifafu.ifafu.bean.vo.MenuVO
import cn.ifafu.ifafu.bean.vo.NextCourseVO
import cn.ifafu.ifafu.bean.vo.ResourceObserve
import cn.ifafu.ifafu.databinding.MainNewFragmentBinding
import cn.ifafu.ifafu.databinding.MainNewIncludeBinding
import cn.ifafu.ifafu.databinding.MainNewIncludeLeftMenuBinding
import cn.ifafu.ifafu.ui.activity.AboutActivity
import cn.ifafu.ifafu.ui.feedback.FeedbackActivity
import cn.ifafu.ifafu.ui.main.MainMenuHandler
import cn.ifafu.ifafu.ui.main.MainViewModel
import cn.ifafu.ifafu.ui.setting.SettingActivity
import cn.ifafu.ifafu.ui.view.DragLayout
import cn.ifafu.ifafu.ui.view.adapter.MenuAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainNewFragment : BaseFragment(R.layout.main_new_fragment), View.OnClickListener {

    private val viewModel: MainNewViewModel by viewModels()
    private val activityViewModel: MainViewModel by activityViewModels()

    private lateinit var binding: MainNewFragmentBinding
    private lateinit var leftMenuBinding: MainNewIncludeLeftMenuBinding
    private lateinit var contentBinding: MainNewIncludeBinding
    private lateinit var menuAdapter: MenuAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = MainNewFragmentBinding.bind(view)
        binding.lifecycleOwner = this
        binding.vm = viewModel
        binding.activityViewModel = activityViewModel

        leftMenuBinding = binding.leftMenu
        contentBinding = binding.content

        contentBinding.btnMenu.setOnClickListener(this)
        leftMenuBinding.tvNavAbout.setOnClickListener(this)
        leftMenuBinding.tvNavFeedback.setOnClickListener(this)
        leftMenuBinding.tvNavSetting.setOnClickListener(this)
        leftMenuBinding.tvNavUpdate.setOnClickListener(this)
        leftMenuBinding.tvNavCheckout.setOnClickListener(this)

        initMenu()
//        initBackground()

        viewModel.timeEvents.observe(viewLifecycleOwner, {
            contentBinding.timeline.setTimeEvents(it)
        })
        viewModel.nextCourse.observe(viewLifecycleOwner, ResourceObserve {
            onSuccess {
                contentBinding.layoutNextCourse.root.visibility = View.VISIBLE
                val vo = it.data
                if (vo is NextCourseVO.HasClass) {
                    val title =
                        if (vo.isInClass) getString(R.string.now_class) else getString(R.string.next_class)
                    setCourseText(
                        title,
                        vo.nextClass,
                        vo.address + "   " + vo.classTime,
                        vo.timeLeft
                    )
                } else if (vo is NextCourseVO.NoClass) {
                    setCourseText(vo.message, "", "", "")
                }
            }
            onFailure {
                contentBinding.layoutNextCourse.root.visibility = View.GONE
            }
        })
        viewModel.isIFAFUUser.observe(viewLifecycleOwner,  { isIFAFUUser ->
            if (isIFAFUUser) {
                if (!menuAdapter.data.any { it.id == R.id.menu_person_center }) {
                    menuAdapter.data.add(
                        MenuVO(
                            R.id.menu_person_center,
                            R.drawable.ic_person,
                            "个人中心"
                        )
                    )
                    menuAdapter.notifyItemInserted(menuAdapter.data.size - 1)
                }
            } else {
                if (menuAdapter.data.removeAll { it.id == R.id.menu_person_center }) {
                    menuAdapter.notifyDataSetChanged()
                }
            }
        })
        viewModel.weather.observe(viewLifecycleOwner, { weather ->
            if (weather == null) {
                contentBinding.layoutWeather.root.visibility = View.GONE
            } else {
                contentBinding.layoutWeather.root.visibility = View.VISIBLE
                contentBinding.layoutWeather.tvWeather1.text = ("${weather.nowTemp}℃")
                contentBinding.layoutWeather.tvWeather2.text =
                    ("${weather.cityName} | ${weather.weather}")
            }
        })
    }


    override fun onStart() {
        super.onStart()
        viewModel.updateIFAFUUser()
        viewModel.updateNextCourse()
        viewModel.updateWeather()
        viewModel.updateTimeAxis()
    }

    private fun initMenu() {
        val menus = linkedSetOf(
            MenuVO(R.id.menu_schedule, R.drawable.tab_syllabus, "课程表"),
            MenuVO(R.id.menu_exam_list, R.drawable.tab_exam, "考试计划"),
            MenuVO(R.id.menu_score_list, R.drawable.tab_score, "成绩查询"),
            MenuVO(R.id.menu_elective, R.drawable.tab_elective, "选修查询"),
            MenuVO(R.id.menu_web, R.drawable.tab_web, "网页模式"),
//            MenuVO(R.id.menu_comment, R.drawable.tab_comment, "教学评教"),
//            MenuVO(R.id.menu_electricity, R.drawable.tab_electricity, "电费查询"),
//            MenuVO(R.id.menu_repair, R.drawable.tab_repair, "报修服务"),
            MenuVO(R.id.menu_feedback, R.drawable.tab_feedback, "反馈问题"),
            MenuVO(R.id.menu_information, R.drawable.ic_information, "信息平台"),
            MenuVO(R.id.menu_boya, R.drawable.ic_robot, "校园百事通")
        )
        val menuHandler = MainMenuHandler(requireContext())
        menuAdapter = MenuAdapter {
            menuHandler.handle(it)
        }
        menuAdapter.data = menus
        menuAdapter.notifyDataSetChanged()
        contentBinding.rvMenu.adapter = menuAdapter
        activityViewModel.isShowComment.observe(viewLifecycleOwner) { showComment ->
            if (showComment) {
                menus.add(MenuVO(R.id.menu_comment, R.drawable.tab_comment, "一键评教"))
                menuAdapter.notifyDataSetChanged()
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_menu -> binding.drawerMain.open()
            R.id.tv_nav_update -> activityViewModel.upgradeApp()
            R.id.tv_nav_about -> startActivity(Intent(context, AboutActivity::class.java))
            R.id.tv_nav_feedback -> startActivity(Intent(context, FeedbackActivity::class.java))
            R.id.tv_nav_setting -> {
                val intent = Intent(context, SettingActivity::class.java)
                requireActivity().startActivityForResult(intent, Constants.ACTIVITY_SETTING)
            }
            R.id.tv_nav_checkout -> activityViewModel.showMultiUserDialog()
        }
        if (binding.drawerMain.status == DragLayout.Status.Open) {
            binding.drawerMain.close(true)
        }
    }

    private fun setCourseText(title: String, name: String, address: String, time: String) {
        contentBinding.layoutNextCourse.tvCourseTitle.text = title
        contentBinding.layoutNextCourse.tvCourseName.text = name
        contentBinding.layoutNextCourse.tvCourseAddress.text = address
        contentBinding.layoutNextCourse.tvCourseTime.text = time
    }


}
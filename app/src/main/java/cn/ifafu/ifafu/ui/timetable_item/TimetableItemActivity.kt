package cn.ifafu.ifafu.ui.timetable_item

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import cn.ifafu.ifafu.R
import cn.ifafu.ifafu.constant.WHAT_WRONG_WITH_IFAFU
import cn.ifafu.ifafu.databinding.TimetableActivityDetailBinding
import cn.ifafu.ifafu.entity.NewCourse
import cn.ifafu.ifafu.repository.TimetableRepository
import cn.ifafu.ifafu.ui.common.BaseActivity
import cn.ifafu.ifafu.ui.view.adapter.WeekItemAdapter
import cn.ifafu.ifafu.util.DataUtils
import com.bigkoo.pickerview.builder.OptionsPickerBuilder
import com.bigkoo.pickerview.view.OptionsPickerView
import com.blankj.utilcode.util.KeyboardUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class TimetableItemActivity : BaseActivity(), View.OnClickListener {

    private lateinit var binding: TimetableActivityDetailBinding

    private val timeOPV: OptionsPickerView<String> by lazy {
        OptionsPickerBuilder(this) { options1, options2, options3, _ ->
            course.weekday = options1 + 1
            course.beginNode = options2 + 1
            course.nodeLength = options3 + 1
            val timeText = "${weeks[options1]}  第${options2 + 1}节 ~ 第${options2 + options3 + 1}节"
            binding.etCourseTime.text = timeText
        }
            .setOutSideCancelable(false)
            .setCancelText("取消")
            .setSubmitText("确定")
            .setTitleText("请选择时间")
            .setTitleColor(Color.parseColor("#157efb"))
            .setTitleSize(13)
            .build()
    }

    private val weeks = listOf("周日", "周一", "周二", "周三", "周四", "周五", "周六")

    private val mWeekAdapter: WeekItemAdapter = WeekItemAdapter(this)

    private var enterType = ADD

    @Inject
    lateinit var repository: TimetableRepository

    private var course: NewCourse = NewCourse()

    companion object {

        private const val CHECK = 1
        private const val ADD = 2

        fun intentForCheck(context: Context, course: NewCourse): Intent {
            return Intent(context, TimetableItemActivity::class.java).apply {
                putExtra("course", course)
                putExtra("type", CHECK)
            }
        }

        fun intentForAdd(context: Context, year: String, term: String): Intent {
            return Intent(context, TimetableItemActivity::class.java).apply {
                putExtra("year", year)
                putExtra("term", term)
                putExtra("type", ADD)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLightUiBar()
        binding = TimetableActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        enterType = intent.getIntExtra("type", -1)

        lifecycleScope.launchWhenCreated {
            when (enterType) {
                ADD -> {
                    val year = intent.getStringExtra("year")
                    val term = intent.getStringExtra("term")
                    if (year == null || term == null) {
                        snackbar(WHAT_WRONG_WITH_IFAFU)
                        return@launchWhenCreated
                    }
                    course = NewCourse(
                        weekday = 1,
                        beginNode = 1,
                        nodeLength = 1,
                        year = year,
                        term = term,
                        local = true
                    )
                    editMode(true)
                    initCourseView(course)
                }
                CHECK -> {
                    val courseFromIntent = intent.getParcelableExtra<NewCourse>("course")
                        ?: return@launchWhenCreated
                    val id = courseFromIntent.id
                    if (id == -1) {
                        snackbar(WHAT_WRONG_WITH_IFAFU)
                        return@launchWhenCreated
                    }
                    val byId = repository.getCourseById(id)
                    if (byId == null) {
                        snackbar(WHAT_WRONG_WITH_IFAFU)
                        return@launchWhenCreated
                    }
                    course = byId
                    editMode(false)
                    initCourseView(course)
                }
                else -> {
                    snackbar(WHAT_WRONG_WITH_IFAFU)
                    return@launchWhenCreated
                }
            }
        }


        binding.weeksRv.adapter = mWeekAdapter
        binding.etCourseTime.setOnClickListener(this)

        initTimePicker()
        initToolbar()
    }

    private fun initToolbar() {
        binding.toolBar.setNavigationOnClickListener {
            finish()
        }

        binding.toolBar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.timetable_menu_edit -> editMode(true)
                R.id.timetable_menu_delete -> delete()
                R.id.timetable_menu_save -> save()
            }
            true
        }
    }

    private fun initCourseView(it: NewCourse) {
        val timeText = "${weeks[it.weekday - 1]}  第${it.beginNode} ~ ${it.endNode}节"
        binding.tvCourseTime.text = timeText
        binding.tvCourseName.text = it.name
        binding.tvCourseTeacher.text = it.teacher
        binding.tvCourseAddress.text = it.classroom

        binding.etCourseTime.text = timeText
        binding.etCourseName.setText(it.name)
        binding.etCourseTeacher.setText(it.teacher)
        binding.etCourseAddress.setText(it.classroom)

        mWeekAdapter.weekList = it.weeks
        mWeekAdapter.notifyDataSetChanged()

        timeOPV.setSelectOptions(it.weekday - 1, it.beginNode - 1, it.nodeLength - 1)
    }

    private fun initTimePicker() {
        val node1t: MutableList<String> = ArrayList()
        val node2t: MutableList<MutableList<String>> = ArrayList()
        for (i in 1..12) {
            node1t.add("第" + i + "节")
            val node2tt = ArrayList<String>()
            for (j in i..12) {
                node2tt.add("第" + j + "节")
            }
            node2t.add(node2tt)
        }
        val node1: MutableList<MutableList<String>> = ArrayList()
        val node2: MutableList<MutableList<MutableList<String>>> = ArrayList()
        for (i in 1..7) {
            node1.add(node1t)
        }
        for (i in 1..node1.size) {
            node2.add(node2t)
        }
        timeOPV.setPicker(weeks, node1, node2)
    }

    private fun editMode(edit: Boolean) {
        mWeekAdapter.editMode = edit

        binding.tvCourseAddress.isVisible = !edit
        binding.tvCourseTeacher.isVisible = !edit
        binding.tvCourseName.isVisible = !edit
        binding.tvCourseTime.isVisible = !edit

        binding.etCourseName.isVisible = edit
        binding.etCourseAddress.isVisible = edit
        binding.etCourseTeacher.isVisible = edit
        binding.etCourseTime.isVisible = edit

        binding.toolBar.menu.findItem(R.id.timetable_menu_save)?.isVisible = edit
        binding.toolBar.menu.findItem(R.id.timetable_menu_delete)?.isVisible = !edit
        binding.toolBar.menu.findItem(R.id.timetable_menu_edit)?.isVisible = !edit
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.et_course_time -> timeOPV.show()
        }
    }

    private fun save() {
        lifecycleScope.launch {
            course.weeks = mWeekAdapter.weekList
            course.name = binding.etCourseName.text.toString()
            course.classroom = binding.etCourseAddress.text.toString()
            course.teacher = binding.etCourseTeacher.text.toString()
            if (enterType == ADD) {
                course.id = course.hashCode()
                course.account = DataUtils.getSno()
            }
            repository.saveCourse(course)
            snackbar("保存成功")
            setResult(Activity.RESULT_OK)
            editMode(false)
            KeyboardUtils.hideSoftInput(this@TimetableItemActivity)
            initCourseView(course)
        }
    }

    private fun delete() {
        lifecycleScope.launch {
            repository.deleteCourse(course)
            showToast("删除成功")
            setResult(Activity.RESULT_OK)
            finish()
        }
    }

}

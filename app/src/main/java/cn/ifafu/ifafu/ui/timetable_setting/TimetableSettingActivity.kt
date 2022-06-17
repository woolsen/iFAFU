package cn.ifafu.ifafu.ui.timetable_setting

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import cn.ifafu.ifafu.R
import cn.ifafu.ifafu.databinding.TimetableActivitySettingBinding
import cn.ifafu.ifafu.entity.SyllabusSetting
import cn.ifafu.ifafu.ui.common.BaseActivity
import cn.ifafu.ifafu.ui.view.adapter.syllabus_setting.*
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.color.colorChooser
import dagger.hilt.android.AndroidEntryPoint
import me.drakeet.multitype.MultiTypeAdapter
import java.io.File

@AndroidEntryPoint
class TimetableSettingActivity : BaseActivity() {

    companion object {
        private const val REQUEST_PICK = 1001
        private const val REQUEST_CROP = 1002
    }

    private lateinit var account: String
    private lateinit var binding: TimetableActivitySettingBinding

    private val mAdapter by lazy {
        MultiTypeAdapter().apply {
            register(SeekBarItem::class, SeekBarBinder())
            register(CheckBoxItem::class, CheckBoxBinder())
            register(TextViewItem::class, TextViewBinder())
            register(ColorItem::class, ColorBinder())
        }
    }

    private val mViewModel: TimetableSettingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = TimetableActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setLightUiBar()
        binding.tbSyllabusSetting.setNavigationOnClickListener { finish() }

        initRecycleView()

        mViewModel.setting.observe(this, { setting ->
            account = setting.account
            mAdapter.items = listOf(
                SeekBarItem("一天课程的节数", setting.totalNode, "节", 8, 12) {
                    setting.totalNode = it
                    mViewModel.save()
                    setResult(Activity.RESULT_OK)
                },
                SeekBarItem("课程字体大小", setting.textSize, "sp", 8, 18) {
                    setting.textSize = it
                    mViewModel.save()
                    setResult(Activity.RESULT_OK)
                },
                CheckBoxItem("显示水平分割线", "", setting.showHorizontalLine) {
                    setting.showHorizontalLine = it
                    mViewModel.save()
                    setResult(Activity.RESULT_OK)
                },
                CheckBoxItem("显示垂直分割线", "", setting.showVerticalLine) {
                    setting.showVerticalLine = it
                    mViewModel.save()
                    setResult(Activity.RESULT_OK)
                },
                CheckBoxItem("显示上课时间", "", setting.showBeginTimeText) {
                    setting.showBeginTimeText = it
                    mViewModel.save()
                    setResult(Activity.RESULT_OK)
                },
                CheckBoxItem("标题栏深色字体", "", setting.statusDartFont) {
                    setting.statusDartFont = it
                    mViewModel.save()
                    setResult(Activity.RESULT_OK)
                },
                ColorItem("主题颜色", "按钮颜色，文本颜色（除课程文本）", setting.themeColor) { ivColor ->
                    showColorPicker(setting, ivColor)
                    setResult(Activity.RESULT_OK)
                },
                TextViewItem("导出测试数据到剪切板", "", {
                    mViewModel.outputHtml()
                    setResult(Activity.RESULT_OK)
                }, {})
            )
            mAdapter.notifyDataSetChanged()
        })
    }

    private fun initRecycleView() {

        val dividerItemDecoration = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        dividerItemDecoration.setDrawable(
            ContextCompat.getDrawable(
                this,
                R.drawable.shape_divider
            )!!
        )
        binding.rvSyllabusSetting.addItemDecoration(dividerItemDecoration)
        binding.rvSyllabusSetting.layoutManager = LinearLayoutManager(this)
        binding.rvSyllabusSetting.adapter = mAdapter
    }

    private fun crop(uri: Uri) {
        val intent = Intent("com.android.camera.action.CROP").apply {
            setDataAndType(uri, "image/*")
            putExtra("crop", "true")
            putExtra("aspectX", window.decorView.width)
            putExtra("aspectY", window.decorView.height)
            intent.putExtra("outputX", window.decorView.width) // 宽尺寸
            intent.putExtra("outputY", window.decorView.height) // 高尺寸
            intent.putExtra("scale", true) // 保持比例
            //临时方案，没想好别乱改，注意和课表背景加载的关系！！！
            val file = File(getExternalFilesDir(account), "syllabus_bg.jpg")
            putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file))
            putExtra("outputFormat", Bitmap.CompressFormat.JPEG)
        }
        startActivityForResult(intent, REQUEST_CROP)
    }

    private fun showColorPicker(setting: SyllabusSetting, ivColor: ImageView) {
        var selectColor = 0
        MaterialDialog(this)
            .title(text = "请选择颜色")
            .colorChooser(
                colors = intArrayOf(
                    Color.BLACK, Color.DKGRAY, Color.GRAY, Color.LTGRAY,
                    Color.WHITE, Color.RED, Color.GREEN, Color.BLUE,
                    Color.YELLOW, Color.CYAN
                ), initialSelection = setting.themeColor
            ) { _, color ->
                selectColor = color
            }
            .positiveButton(text = "确认") {
                val grad = ivColor.background as GradientDrawable?
                grad?.setColor(selectColor)
                setting.themeColor = selectColor
                mViewModel.save()
            }
            .negativeButton(text = "取消")
            .show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_PICK && resultCode == Activity.RESULT_OK) {
            data?.data?.let { crop(it) }
        } else if (requestCode == REQUEST_CROP && resultCode == Activity.RESULT_OK) {
            setResult(Activity.RESULT_OK)
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

}

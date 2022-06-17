package cn.ifafu.ifafu.ui.timetable

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.widget.SeekBar
import androidx.activity.viewModels
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import cn.ifafu.ifafu.R
import cn.ifafu.ifafu.bean.vo.Resource
import cn.ifafu.ifafu.bean.vo.TimetableVO
import cn.ifafu.ifafu.constant.Constants
import cn.ifafu.ifafu.constant.WHAT_WRONG_WITH_IFAFU
import cn.ifafu.ifafu.databinding.TimetableActivityBinding
import cn.ifafu.ifafu.databinding.TimetableBottomDrawerBinding
import cn.ifafu.ifafu.databinding.TimetableContentBinding
import cn.ifafu.ifafu.entity.NewCourse
import cn.ifafu.ifafu.entity.SyllabusSetting
import cn.ifafu.ifafu.ui.common.BaseActivity
import cn.ifafu.ifafu.ui.main.MainActivity
import cn.ifafu.ifafu.ui.timetable_item.TimetableItemActivity
import cn.ifafu.ifafu.ui.timetable_setting.TimetableSettingActivity
import cn.ifafu.ifafu.ui.view.LoadingDialog
import cn.ifafu.ifafu.util.ChineseNumbers
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.gyf.immersionbar.ImmersionBar
import com.yalantis.ucrop.UCrop
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class TimetableActivity : BaseActivity(), View.OnClickListener, View.OnLongClickListener {

    private val mViewModel: TimetableViewModel by viewModels()

    private var mCurrentWeek = 1
    private val mTimetablePageAdapter: TimetablePageAdapter by lazy {
        TimetablePageAdapter(
            onItemClickListener = { _, item ->
                val intent = TimetableItemActivity
                    .intentForCheck(this, item.tag as NewCourse)
                startActivityForResult(intent, Constants.ACTIVITY_SYLLABUS_ITEM)
            },
            onItemLongClickListener = { _, _ ->

            })
    }
    private val loadingDialog = LoadingDialog(this)
    private val mPreviewAdapter by lazy {
        TimetablePreviewAdapter { year, term ->
            mViewModel.switchOption(year, term)
        }
    }

    private lateinit var binding: TimetableActivityBinding
    private lateinit var contentBinding: TimetableContentBinding
    private lateinit var drawerBinding: TimetableBottomDrawerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = TimetableActivityBinding.inflate(layoutInflater)
        contentBinding = binding.content
        drawerBinding = binding.drawer
        setContentView(binding.root)
        setLightUiBar()

        initView()
        initViewModel()
    }

    private fun initViewModel() {
        mViewModel.message.observe(this, { snackbar(it) })
        mViewModel.timetableSetting.observe(this, { setSyllabusSetting(it) })
        mViewModel.background.observe(this, { uri ->
            Timber.d("set background: $uri")
            if (uri == null) {
                contentBinding.ivBackground.setImageBitmap(null)
            } else {
                Glide.with(this)
                    .load(uri)
                    .skipMemoryCache(true) // 不使用内存缓存
                    .diskCacheStrategy(DiskCacheStrategy.NONE) // 不使用磁盘缓存
                    .into(contentBinding.ivBackground)
            }
        })
        mViewModel.timetableVO.observe(this, { res ->
            when (res) {
                is Resource.Success -> {
                    mTimetablePageAdapter.updateTimetable(res.data)
                    res.handleMessage { message ->
                        snackbar(message)
                    }
                    loadingDialog.cancel()
                }
                is Resource.Loading -> {
                    loadingDialog.show("刷新中")
                }
                is Resource.Failure -> {
                    snackbar(res.message)
                    mTimetablePageAdapter.updateTimetable(TimetableVO.create(emptyList()))
                    loadingDialog.cancel()
                }
            }
        })
        mViewModel.openingDay.observe(this, { openingDay ->
            mTimetablePageAdapter.updateOpeningDay(openingDay)
            mCurrentWeek = openingDay.getCurrentWeek()
            contentBinding.viewPager.setCurrentItem(mCurrentWeek - 1, false)
            val week = if (mCurrentWeek <= 0) 1 else mCurrentWeek
            showWeekString(week)
        })
        mViewModel.timetablePreviews.observe(this, { res ->
            when (res) {
                is Resource.Success -> {
                    mPreviewAdapter.data = res.data
                    mPreviewAdapter.notifyDataSetChanged()
                    loadingDialog.cancel()
                }
                is Resource.Loading -> {
                    loadingDialog.show(res.message)
                }
                is Resource.Failure -> {
                    snackbar(res.message)
                    loadingDialog.cancel()
                }
            }
        })
//        mViewModel.askCheckTerm.observe(this, EventObserver { op ->
//            showCheckTermDialog(op)
//        })
    }

    private fun initView() {
        contentBinding.btnBack.setOnClickListener(this)
        contentBinding.btnAdd.setOnClickListener(this)
        contentBinding.btnRefresh.setOnClickListener(this)
        contentBinding.moreBtn.setOnClickListener(this)
//        tv_edit_week.setOnClickListener(this)
//        drawerBinding.editOptionTV.setOnClickListener(this)

        drawerBinding.settingMenu.setOnClickListener(this)
        drawerBinding.timeMenu.setOnClickListener(this)
        drawerBinding.backgroundMenu.setOnClickListener(this)
        drawerBinding.backgroundMenu.setOnLongClickListener {
            mViewModel.resetBackground()
            true
        }

        drawerBinding.weekSeekBar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (seekBar == null) return
                contentBinding.viewPager.setCurrentItem(progress, true)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })

        contentBinding.tvDate.text = SimpleDateFormat("M月d日", Locale.CHINA).format(Date())
        contentBinding.tvSubtitle.setOnLongClickListener(this)

        drawerBinding.timetablePreviewRv.adapter = mPreviewAdapter

        contentBinding.viewPager.adapter = mTimetablePageAdapter
        contentBinding.viewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                drawerBinding.weekSeekBar.progress = position
                showWeekString(position + 1)
            }
        })
    }

    private fun showWeekString(week: Int) {
        val weekInChinese = "第${ChineseNumbers.englishNumberToChinese((week.toString()))}周"
        val openingDay = mViewModel.openingDay.value
        if (openingDay == null) {
            contentBinding.tvSubtitle.text = weekInChinese
            return
        }
        val currentWeek = openingDay.getCurrentWeek()
        val weekStr = if (currentWeek <= 0) {
            if (week == 1) {
                if (openingDay.isCurrentTerm) {
                    "$weekInChinese(放假中)"
                } else {
                    weekInChinese
                }
            } else {
                "$weekInChinese 长按返回第一周"
            }
        } else {
            if (week == currentWeek) {
                if (openingDay.isCurrentTerm) {
                    "$weekInChinese(本周)"
                } else {
                    "$weekInChinese(非本学期)"
                }
            } else {
                if (openingDay.isCurrentTerm) {
                    "$weekInChinese 长按返回本周"
                } else {
                    "$weekInChinese 长按返回第一周"
                }
            }
        }
        contentBinding.tvSubtitle.text = weekStr
    }

    private fun setSyllabusSetting(setting: SyllabusSetting) {
        mTimetablePageAdapter.updateSetting(setting)
        val themeColor = setting.themeColor
        //设置主题色
        contentBinding.tvDate.setTextColor(themeColor)
        contentBinding.tvSubtitle.setTextColor(themeColor)
        contentBinding.btnBack.setColorFilter(themeColor)
        contentBinding.btnAdd.setColorFilter(themeColor)
        contentBinding.btnRefresh.setColorFilter(themeColor)
        contentBinding.moreBtn.setColorFilter(themeColor)
        ImmersionBar.with(this)
            .titleBarMarginTop(contentBinding.tbSyllabus)
            .statusBarDarkFont(setting.statusDartFont)
            .init()
        drawerBinding.weekSeekBar.max = setting.weekCnt - 1
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_add -> {
                val o = mPreviewAdapter.getSelected()
                if (o == null) {
                    snackbar(WHAT_WRONG_WITH_IFAFU)
                    return
                }
                val intent = TimetableItemActivity.intentForAdd(this, o.year, o.term)
                startActivityForResult(intent, Constants.ACTIVITY_SYLLABUS_ITEM)
            }
            R.id.btn_refresh -> mViewModel.updateSyllabusFromNet()
            R.id.btn_back -> onFinishActivity()
            R.id.moreBtn -> {
                binding.drawerLayout.openDrawer(Gravity.BOTTOM)
            }
            R.id.settingMenu -> {
                val intent = Intent(this, TimetableSettingActivity::class.java)
                startActivityForResult(intent, Constants.ACTIVITY_SYLLABUS_SETTING)
            }
            R.id.backgroundMenu -> {
                val intent = Intent(Intent.ACTION_PICK).apply {
                    type = "image/*"
                }
                startActivityForResult(intent, CODE_PICK)
            }
//            R.id.tv_edit_week -> {
//                showEditCurrentWeekDialog()
//            }
//            R.id.editOptionTV -> {
//                showCheckTermPicker()
//            }
//            R.id.timeMenu -> {
//                snackbar("施工中(･ェ･。)")
//            }
        }
    }

    override fun onLongClick(v: View?): Boolean {
        return when (v?.id) {
            R.id.tv_subtitle -> {
                rollbackToCurrent()
                true
            }
            else -> false
        }
    }

    /**
     * 返回当前周
     */
    private fun rollbackToCurrent() {
        contentBinding.viewPager.setCurrentItem(mCurrentWeek - 1, true)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && event?.action == KeyEvent.ACTION_DOWN) {
            if (binding.drawerLayout.isDrawerOpen(binding.drawer.root)) {
                binding.drawerLayout.closeDrawers()
            } else {
                onFinishActivity()
            }
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun onFinishActivity() {
        when (intent.getIntExtra("from", -1)) {
            Constants.SYLLABUS_WIDGET, Constants.ACTIVITY_SPLASH -> {
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(intent)
            }
        }
        finish()
    }

    /**
     * 裁剪背景
     */
    private fun crop(uri: Uri) {
        val intent = Intent("com.android.camera.action.CROP").apply {
            setDataAndType(uri, "image/*")
            putExtra("crop", "true")
            putExtra("aspectX", window.decorView.width)
            putExtra("aspectY", window.decorView.height)
            intent.putExtra("outputX", window.decorView.width) // 宽尺寸
            intent.putExtra("outputY", window.decorView.height) // 高尺寸
            intent.putExtra("scale", true) // 保持比例
            val file = File(getExternalFilesDir("background"), "syllabus.jpg")
            putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file))
            putExtra("outputFormat", Bitmap.CompressFormat.JPEG)
        }
        startActivityForResult(intent, CODE_CROP)
    }

    /**
     * 裁剪背景（当[crop]不支持时，使用调用此方法裁剪图片）
     */
    private fun crop2(uri: Uri) {
        val file = File(getExternalFilesDir("background"), "syllabus.jpg")
        UCrop.of(uri, Uri.fromFile(file))
            .withAspectRatio(window.decorView.width.toFloat(), window.decorView.height.toFloat())
//            .withMaxResultSize(window.decorView.width, window.decorView.heigh)
            .start(this)
    }

    private var backgroundUri: Uri? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CODE_CROP && resultCode != Activity.RESULT_OK) {

        } else if (resultCode == Activity.RESULT_OK) {
            if (requestCode == CODE_PICK) {
                val uri = data?.data
                if (uri == null) {
                    showToast("背景图片获取出错")
                } else {
                    crop2(uri)
                    backgroundUri = uri
                }
            } else if (requestCode == CODE_CROP) {
                mViewModel.updateBackground()
            } else if (requestCode == Constants.ACTIVITY_SYLLABUS_ITEM) {
                mViewModel.updateTimetableLocal()
            } else if (requestCode == Constants.ACTIVITY_SYLLABUS_SETTING) {
                mViewModel.updateTimetableSetting()
            } else if (requestCode == UCrop.REQUEST_CROP) {
                mViewModel.updateBackground()
            } else {
                super.onActivityResult(requestCode, resultCode, data)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    companion object {
        private const val CODE_PICK = 1001
        private const val CODE_CROP = 1002
    }

}
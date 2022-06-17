package cn.ifafu.ifafu.ui.examlist

import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DividerItemDecoration
import cn.ifafu.ifafu.R
import cn.ifafu.ifafu.ui.common.BaseActivity
import cn.ifafu.ifafu.bean.vo.Resource
import cn.ifafu.ifafu.databinding.ExamListAcitivtyBinding
import cn.ifafu.ifafu.ui.view.LoadingDialog
import cn.ifafu.ifafu.ui.view.SemesterOptionPicker
import cn.ifafu.ifafu.ui.view.adapter.ExamAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ExamListActivity : BaseActivity() {

    private val mExamAdapter = ExamAdapter()

    private val mLoadingDialog = LoadingDialog(this, "获取中")
    private val mViewModel: ExamListViewModel by viewModels()

    private val mSemesterOptionPicker by lazy {
        SemesterOptionPicker(this) { year, term ->
            mViewModel.switchYearAndTerm(year, term)
        }
    }

    private lateinit var binding: ExamListAcitivtyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLightUiBar()
        binding = bind(R.layout.exam_list_acitivty)

        binding.btnRefresh.setOnClickListener { mViewModel.refresh() }
        binding.tbExam.setNavigationOnClickListener { finish() }
        binding.tbExam.setSubtitleClickListener {
            mViewModel.semester.value?.run {
                mSemesterOptionPicker.setSemester(this)
                mSemesterOptionPicker.show()
            }
        }
        binding.tbExam.setSubtitleDrawablesRelative(
            null,
            null,
            ContextCompat.getDrawable(this, R.drawable.ic_down_little),
            null
        )

        initList()

        binding.vm = mViewModel

        mViewModel.exams.observe(this, {
            when (it) {
                is Resource.Loading -> {
                    mLoadingDialog.show()
                }
                is Resource.Failure -> {
                    mLoadingDialog.cancel()
                    snackbar(it.message)
                }
                is Resource.Success -> {
                    isShowEmptyView(it.data.isEmpty())
                    mExamAdapter.data = it.data
                    mExamAdapter.notifyDataSetChanged()
                    mLoadingDialog.cancel()
                    it.handleMessage { message ->
                        snackbar(message)
                    }
                }
            }
        })
    }

    private fun initList() {
        // 设置分割线
        val dividerItemDecoration = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        val divider = ContextCompat.getDrawable(this, R.drawable.shape_divider)!!
        dividerItemDecoration.setDrawable(divider)
        binding.rvExam.addItemDecoration(dividerItemDecoration)
        // 设置Adapter
        binding.rvExam.adapter = mExamAdapter
    }

    private fun isShowEmptyView(show: Boolean) {
        binding.viewExamEmpty.isVisible = show
        binding.rvExam.isVisible = !show
    }

}

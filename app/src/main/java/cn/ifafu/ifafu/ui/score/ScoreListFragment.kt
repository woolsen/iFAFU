package cn.ifafu.ifafu.ui.score

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import cn.ifafu.ifafu.R
import cn.ifafu.ifafu.ui.common.BaseFragment
import cn.ifafu.ifafu.ui.view.RecyclerViewDivider
import cn.ifafu.ifafu.bean.vo.Resource
import cn.ifafu.ifafu.databinding.ScoreFragmentListBinding
import cn.ifafu.ifafu.ui.view.LoadingDialog
import cn.ifafu.ifafu.ui.view.SemesterOptionPicker
import cn.ifafu.ifafu.util.trimEnd
import com.afollestad.materialdialogs.MaterialDialog
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ScoreListFragment : BaseFragment(), View.OnClickListener, Toolbar.OnMenuItemClickListener {

    private val mAdapter: ScoreListAdapter = ScoreListAdapter()

    private val iesDetailDialog by lazy {
        MaterialDialog(requireContext()).apply {
            title(text = "智育分计算详情")
            negativeButton(text = "智育分计算规则") {
                MaterialDialog(requireContext()).show {
                    title(text = "智育分计算规则")
                    message(res = R.string.score_ies_rule)
                    positiveButton(text = "收到")
                }
            }
            positiveButton(text = "好嘞~")
        }
    }

    private lateinit var binding: ScoreFragmentListBinding

    private val mSemesterOptionPicker by lazy {
        SemesterOptionPicker(requireActivity()) { year, term ->
            mViewModel.switchYearAndTerm(year, term)
        }
    }

    private val mLoadingDialog by lazy { LoadingDialog(requireContext(), "获取中") }

    private val mViewModel: ScoreViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ScoreFragmentListBinding.inflate(inflater, container, false).apply {
            this.lifecycleOwner = viewLifecycleOwner
            this.vm = mViewModel
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        //初始化监听事件
        binding.tvScoreTitle.setOnClickListener(this@ScoreListFragment)
        binding.layoutIes.setOnClickListener(this@ScoreListFragment)
        binding.layoutCnt.setOnClickListener(this@ScoreListFragment)
        binding.tbScoreList.setOnMenuItemClickListener(this)
        binding.tbScoreList.setNavigationOnClickListener {
            requireActivity().finish()
        }

        //初始化RecycleView
        binding.rvScore.addItemDecoration(
            RecyclerViewDivider(
                requireContext(), LinearLayoutManager.VERTICAL, R.drawable.shape_divider
            )
        )
        binding.rvScore.adapter = mAdapter

        //初始化ViewModel
        mViewModel.iesDetail.observe(viewLifecycleOwner, {
            it.runContentIfNotHandled {
                iesDetailDialog.show { message(text = it) }
            }
        })
        mViewModel.scoresResource.observe(viewLifecycleOwner, {
            when (it) {
                is Resource.Success -> {
                    if (it.data.isEmpty()) {
                        binding.rvScore.visibility = View.GONE
                        binding.viewExamEmpty.visibility = View.VISIBLE
                    } else {
                        binding.rvScore.visibility = View.VISIBLE
                        binding.viewExamEmpty.visibility = View.GONE
                    }
                    binding.tvCntBig.text = it.data.size.toString()
                    mAdapter.setList(it.data)
                    it.handleMessage { message ->
                        snackbar(message)
                    }
                    mLoadingDialog.cancel()
                }
                is Resource.Failure -> {
                    snackbar(it.message)
                    mLoadingDialog.cancel()
                }
                is Resource.Loading -> {
                    mLoadingDialog.show()
                }
            }
        })
        mViewModel.ies.observe(viewLifecycleOwner, { ies ->
            showIES(view, ies)
        })
    }

    private fun showIES(view: View, ies: Float) {
        val big: String
        val small: String
        if (ies.isNaN() || ies <= 0F) {
            big = "0"
            small = "分"
        } else {
            val result = ies.trimEnd(2)
            val index = result.indexOf('.')
            if (index == -1) {
                big = result
                small = "分"
            } else {
                big = result.substring(0, index)
                small = (result.substring(index) + "分")
            }
        }
        binding.tvIes1.text = big
        binding.tvIes2.text = small
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tv_score_title -> {
                mViewModel.semester.value?.run {
                    mSemesterOptionPicker.setSemester(this)
                    mSemesterOptionPicker.show()
                }
            }
            R.id.layout_ies -> mViewModel.iesCalculationDetail()
            R.id.layout_cnt -> {
                val semester = mViewModel.semester.value
                if (semester == null) {
                    snackbar("未找到学期信息")
                    return
                }
                val action =
                    ScoreListFragmentDirections.actionFragmentScoreListToFragmentScoreFilter(
                        semester.yearStr,
                        semester.termStr
                    )
                findNavController().navigate(action)
            }
        }
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menu_refresh -> {
                mViewModel.refreshScoreList()
            }
            R.id.menu_filter -> {
                val semester = mViewModel.semester.value
                if (semester == null) {
                    snackbar("未找到学期信息")
                    return true
                }
                val action =
                    ScoreListFragmentDirections.actionFragmentScoreListToFragmentScoreFilter(
                        semester.yearStr,
                        semester.termStr
                    )
                findNavController().navigate(action)
            }
        }
        return true
    }

}
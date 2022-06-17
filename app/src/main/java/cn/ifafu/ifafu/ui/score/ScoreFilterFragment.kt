package cn.ifafu.ifafu.ui.score

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.TransitionInflater
import cn.ifafu.ifafu.R
import cn.ifafu.ifafu.bean.vo.Resource
import cn.ifafu.ifafu.databinding.ScoreFragmentFilterBinding
import cn.ifafu.ifafu.entity.Score
import cn.ifafu.ifafu.ui.common.BaseFragment
import cn.ifafu.ifafu.ui.view.RecyclerViewDivider
import cn.ifafu.ifafu.ui.view.adapter.ScoreFilterAdapter
import cn.ifafu.ifafu.util.trimEnd
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ScoreFilterFragment : BaseFragment(), Toolbar.OnMenuItemClickListener {

    private val mAdapter by lazy {
        ScoreFilterAdapter(requireContext()) { score: Score, isCheck: Boolean ->
            activityViewModel.itemChecked(score, isCheck)
        }
    }

    private val activityViewModel: ScoreViewModel by activityViewModels()

    private lateinit var binding: ScoreFragmentFilterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition =
            TransitionInflater.from(requireContext()).inflateTransition(android.R.transition.move)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ScoreFragmentFilterBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
            vm = activityViewModel
        }
        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {


        //初始化监听事件
        binding.tbScoreFilter.setOnMenuItemClickListener(this)
        binding.tbScoreFilter.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        //初始化RecycleView
        binding.rvScoreFilter.adapter = mAdapter
        binding.rvScoreFilter.addItemDecoration(
            RecyclerViewDivider(
                requireContext(), LinearLayoutManager.VERTICAL, R.drawable.shape_divider
            )
        )

        //初始化ViewModel
        activityViewModel.scoresResource.observe(viewLifecycleOwner, {
            if (it is Resource.Success) {
                mAdapter.data = it.data
                mAdapter.notifyDataSetChanged()
            } else {
                snackbar("无法找到成绩列表")
            }
        })
        activityViewModel.ies.observe(viewLifecycleOwner, {
            binding.tvNowIes.text = getString(R.string.score_filter_now_ies, it.trimEnd(2))
        })
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menu_filter_all -> {
                mAdapter.setAllChecked()
                activityViewModel.allChecked()
            }
        }
        return true
    }

}

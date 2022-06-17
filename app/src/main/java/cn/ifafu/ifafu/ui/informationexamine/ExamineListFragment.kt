package cn.ifafu.ifafu.ui.informationexamine

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.DividerItemDecoration
import cn.ifafu.ifafu.R
import cn.ifafu.ifafu.annotation.InformationStatus
import cn.ifafu.ifafu.bean.dto.Information
import cn.ifafu.ifafu.databinding.InformationFragmentExamineListBinding
import cn.ifafu.ifafu.ui.common.BaseFragment
import cn.ifafu.ifafu.ui.information.InformationAdapter
import cn.ifafu.ifafu.ui.information.InformationLoadStateAdapter
import cn.ifafu.ifafu.ui.upload.ShowPictureActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filter

@AndroidEntryPoint
class ExamineListFragment : BaseFragment() {

    private val viewModel: ExamineViewModel by viewModels()
    private lateinit var binding: InformationFragmentExamineListBinding

    private var status = 0
    private val adapter: InformationAdapter by lazy {
        InformationAdapter(onItemClick = { _, _ ->
        }, onMoreClick = { v, info ->
            showMore(v, info)
        }, onPictureClick = { v, url ->
            val intent = ShowPictureActivity.intentFor(requireActivity(), url)
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                requireActivity(), v, getString(R.string.information_picture_translate_name)
            )
            startActivity(intent, options.toBundle())
        })
    }

    companion object {
        const val STATUS_ALL = Int.MAX_VALUE
        const val STATUS_PASS = Information.STATUS_PASS
        const val STATUS_REVIEWING = Information.STATUS_REVIEWING
        const val STATUS_FAILURE = Information.STATUS_FAILURE

        fun newInstance(@InformationStatus status: Int): ExamineListFragment {
            val fragment = ExamineListFragment()
            val bundle = Bundle().apply {
                putInt("status", status)
            }
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = InformationFragmentExamineListBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        status = arguments?.getInt("status")
            ?: throw IllegalArgumentException("Can't find which information status need to show")
        viewModel.status.value = status

        initAdapter(view)
        initSwipeToRefresh(view)
        initViewModel()
    }

    override fun onResume() {
        super.onResume()
        adapter.refresh()
    }

    private fun initViewModel() {
        viewModel.examineResult.observe(viewLifecycleOwner, { event ->
            event.runContentIfNotHandled { res ->
                res.handle {
                    onSuccess {
                        adapter.refresh()
                        snackbar("审核状态已变更")
                    }
                    onFailure { it.handleMessage { msg -> snackbar(msg) } }
                }
            }
        })
    }

    private fun initAdapter(root: View) {
        val decoration = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        decoration.setDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.information_shape_decoration_horizonal
            )!!
        )
        binding.informationList.addItemDecoration(decoration)

        binding.informationList.adapter = adapter.withLoadStateFooter(
            footer = InformationLoadStateAdapter(adapter)
        )

        lifecycleScope.launchWhenCreated {
            @OptIn(ExperimentalCoroutinesApi::class)
            adapter.loadStateFlow.collectLatest { loadStates ->
                binding.swipeRefresh.isRefreshing = loadStates.refresh is LoadState.Loading
                if (loadStates.refresh is LoadState.Loading) {
                    binding.swipeRefresh.isRefreshing = true
                } else {
                    if (binding.swipeRefresh.isRefreshing) {
                        snackbar("刷新成功")
                    }
                    binding.swipeRefresh.isRefreshing = false
                }
            }
        }

        lifecycleScope.launchWhenCreated {
            @OptIn(ExperimentalCoroutinesApi::class)
            viewModel.information.collectLatest { data ->
                adapter.submitData(data)
            }
        }

        lifecycleScope.launchWhenCreated {
            @OptIn(FlowPreview::class)
            adapter.loadStateFlow
                .distinctUntilChangedBy { it.refresh }
                .filter { it.refresh is LoadState.NotLoading }
                .collect { binding.informationList.scrollToPosition(0) }
        }
    }

    private fun showMore(view: View, information: Information) {
        val popupMenu = PopupMenu(requireContext(), view)
        popupMenu.menuInflater.inflate(R.menu.information_examine, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.information_menu_agree -> {
                    viewModel.changeStatus(information.id, Information.STATUS_PASS)
                }
                R.id.information_menu_disagree -> {
                    viewModel.changeStatus(information.id, Information.STATUS_FAILURE)
                }
            }
            true
        }
        popupMenu.show()
    }

    private fun initSwipeToRefresh(root: View) {
        binding.swipeRefresh.setOnRefreshListener { adapter.refresh() }
    }
}
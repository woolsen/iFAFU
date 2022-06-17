package cn.ifafu.ifafu.ui.main.old_theme.exampreview

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import cn.ifafu.ifafu.databinding.MainOldExamPreviewFragmentBinding
import cn.ifafu.ifafu.ui.examlist.ExamListActivity
import cn.ifafu.ifafu.util.setDeBoundClickListener
import cn.ifafu.ifafu.ui.common.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ExamPreviewFragment : BaseFragment() {

    private val mViewModel: ExamPreviewViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = MainOldExamPreviewFragmentBinding.inflate(inflater, container, false).apply {
            this.lifecycleOwner = viewLifecycleOwner
            this.viewModel = mViewModel
            this.root.setDeBoundClickListener(1000) {
                startActivity(Intent(context, ExamListActivity::class.java))
            }
        }

        mViewModel.exams.observe(viewLifecycleOwner, { list ->
            if (list.isEmpty()) {
                binding.list.visibility = View.GONE
                binding.emptyMessage.visibility = View.VISIBLE
            } else {
                binding.list.visibility = View.VISIBLE
                binding.emptyMessage.visibility = View.GONE
                binding.item1 = ExamPreviewItemViewModel(list[0])
                if (list.size > 1) {
                    binding.layout.visibility = View.VISIBLE
                    binding.item2 = ExamPreviewItemViewModel(list[1])
                } else {
                    binding.layout.visibility = View.GONE
                }
            }
        })

        return binding.root
    }

    fun refresh() {
        mViewModel.refresh()
    }
}
package cn.ifafu.ifafu.ui.main.old_theme.scorepreview

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import cn.ifafu.ifafu.databinding.MainOldScorePreviewFragmentBinding
import cn.ifafu.ifafu.ui.score.ScoreActivity
import cn.ifafu.ifafu.util.setDeBoundClickListener
import cn.ifafu.ifafu.ui.common.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ScorePreviewFragment : BaseFragment() {

    private val mViewModel: ScorePreviewViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = MainOldScorePreviewFragmentBinding
            .inflate(inflater, container, false).apply {
                this.lifecycleOwner = viewLifecycleOwner
                this.viewModel = mViewModel
                this.root.setDeBoundClickListener(1000) {
                    startActivity(Intent(context, ScoreActivity::class.java))
                }
            }
        return binding.root
    }

    fun refresh() {
        mViewModel.refresh()
    }
}
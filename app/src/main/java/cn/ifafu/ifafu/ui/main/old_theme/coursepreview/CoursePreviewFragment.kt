package cn.ifafu.ifafu.ui.main.old_theme.coursepreview

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import cn.ifafu.ifafu.R
import cn.ifafu.ifafu.bean.vo.NextCourseVO
import cn.ifafu.ifafu.bean.vo.ResourceObserve
import cn.ifafu.ifafu.databinding.MainOldCoursePreviewFragmentBinding
import cn.ifafu.ifafu.ui.timetable.TimetableActivity
import cn.ifafu.ifafu.util.setDeBoundClickListener
import cn.ifafu.ifafu.ui.common.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CoursePreviewFragment : BaseFragment() {

    private val mViewModel: CoursePreviewViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = MainOldCoursePreviewFragmentBinding
            .inflate(inflater, container, false).apply {
                this.viewModel = mViewModel
                this.lifecycleOwner = viewLifecycleOwner
                this.root.setDeBoundClickListener(1000) {
                    startActivity(Intent(context, TimetableActivity::class.java))
                }
            }

        mViewModel.nextCourseVO.observe(viewLifecycleOwner, ResourceObserve {
            onSuccess { res ->
                val data = res.data
                binding.tvTitle.text = data.title
                binding.tvWeekTime.text = data.dateText
                when (data) {
                    is NextCourseVO.HasClass -> {
                        binding.tvMessage.isVisible = false
                        binding.layoutCourseInfo.isVisible = true
                        binding.tvTimeLeft.text = data.timeLeft
                        if (data.isInClass) {
                            binding.tvNext.text =
                                getString(R.string.now_class_format, data.nextClass)
                            binding.tvStatus.setText(R.string.in_class)
                            binding.tvStatus.setCompoundDrawablesRelativeWithIntrinsicBounds(
                                R.drawable.ic_point_blue, 0, 0, 0
                            )
                        } else {
                            binding.tvNext.text =
                                getString(R.string.next_class_format, data.nextClass)
                            binding.tvStatus.setText(R.string.out_class)
                            binding.tvStatus.setCompoundDrawablesRelativeWithIntrinsicBounds(
                                R.drawable.ic_point_red, 0, 0, 0
                            )
                        }
                        binding.tvTotal.text = getString(
                            R.string.node_format,
                            data.numberOfClasses.first,
                            data.numberOfClasses.second
                        )
                        binding.classTime.text = data.classTime
                        binding.location.text = data.address
                    }
                    is NextCourseVO.NoClass -> {
                        binding.tvMessage.isVisible = true
                        binding.layoutCourseInfo.isVisible = false
                        binding.tvMessage.text = data.message
                    }
                }
            }
            onFailure {
                binding.tvMessage.isVisible = true
                binding.layoutCourseInfo.isVisible = false
                binding.tvMessage.text = it.message
            }
        })

        mViewModel.weather.observe(viewLifecycleOwner, {
            binding.weather.text = getString(R.string.weather_format, it.nowTemp, it.weather)
        })

        return binding.root
    }

    fun refresh() {
        mViewModel.refresh()
    }
}
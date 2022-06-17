package cn.ifafu.ifafu.ui.score

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import cn.ifafu.ifafu.R
import cn.ifafu.ifafu.databinding.ScoreFragmentDetailBinding
import cn.ifafu.ifafu.ui.view.adapter.ScoreItemAdapter
import cn.ifafu.ifafu.util.GlobalLib
import cn.ifafu.ifafu.ui.common.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ScoreDetailFragment : BaseFragment(R.layout.score_fragment_detail) {

    private val args: ScoreDetailFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstdaanceState: Bundle?) {
        val binding = ScoreFragmentDetailBinding.bind(view)

//        //沉浸状态栏
//        ImmersionBar.with(this)
//            .titleBar(tb_score_detail)
//            .statusBarDarkFont(true)
//            .init()

        getSupportActionBar()?.let { actionBar ->
            actionBar.title = "成绩查询"
            actionBar.setHomeButtonEnabled(true)
        }

        //初始化监听事件
        binding.tbScoreDetail.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        val score = args.score
        val map: MutableMap<String, String> = LinkedHashMap()
        map["课程名称"] = score.name
        map["成绩"] = if (score.score != -1F) GlobalLib.formatFloat(score.score, 2) + "分" else "无信息"
        map["学分"] = if (score.credit != -1F) GlobalLib.formatFloat(score.credit, 2) + "分" else "无信息"
        map["绩点"] = if (score.gpa != -1F) GlobalLib.formatFloat(score.gpa, 2) + "分" else "无信息"
        map["补考成绩"] = if (score.makeupScore != -1F) GlobalLib.formatFloat(
            score.makeupScore,
            2
        ) + "分" else "无信息"
        map["课程性质"] = score.nature.ifEmpty { "无信息" }
        map["课程属性"] = score.attr.ifEmpty { "无信息" }
        map["开课学院"] = score.institute.ifEmpty { "无信息" }
        map["学年"] = score.year
        map["学期"] = score.term
        map["备注"] = score.remarks

        val adapter = ScoreItemAdapter(map.toList())
        binding.adapter = adapter
    }
}
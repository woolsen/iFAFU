package cn.ifafu.ifafu.ui.informationexamine

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class ExamineListFragmentAdapter(
    private val stateList: List<Int>,
    fragmentActivity: FragmentActivity
) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = stateList.size

    override fun createFragment(position: Int): Fragment {
        return ExamineListFragment.newInstance(stateList[position])
    }

}
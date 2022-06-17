package cn.ifafu.ifafu.ui.view

import android.app.Activity
import android.graphics.Color
import cn.ifafu.ifafu.bean.bo.Semester
import com.bigkoo.pickerview.builder.OptionsPickerBuilder
import com.bigkoo.pickerview.view.OptionsPickerView

class SemesterOptionPicker(context: Activity, onOptionSelectListener: (year: Int, term: Int) -> Unit) {

    private val mOptionPicker: OptionsPickerView<String> by lazy {
        OptionsPickerBuilder(context) { options1, options2, _, _ ->
            onOptionSelectListener(options1, options2)
        }
            .setCancelText("取消")
                .setSubmitText("确定")
                .setTitleText("请选择学年与学期")
                .setTitleColor(Color.parseColor("#157efb"))
                .setTitleSize(13)
                .build()
    }

    fun setSemester(semester: Semester) {
        mOptionPicker.setNPicker(semester.yearList, semester.termList, null)
        mOptionPicker.setSelectOptions(semester.yearIndex, semester.termIndex)
    }

    fun show() {
        mOptionPicker.show()
    }
}
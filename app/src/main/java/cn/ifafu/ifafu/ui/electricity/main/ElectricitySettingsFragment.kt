package cn.ifafu.ifafu.ui.electricity.main

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.activityViewModels
import cn.ifafu.ifafu.databinding.ElectricitySettingsFragmentBinding
import com.bigkoo.pickerview.builder.OptionsPickerBuilder
import com.bigkoo.pickerview.view.OptionsPickerView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ElectricitySettingsFragment : AppCompatDialogFragment() {

    private lateinit var binding: ElectricitySettingsFragmentBinding
    private val activityViewModel by activityViewModels<ElectricityViewModel>()

    private val mOptionPicker: OptionsPickerView<String> by lazy {
        OptionsPickerBuilder(requireContext(), activityViewModel.onOptionsSelectListener)
            .setSubmitText("确认")
            .setSubmitColor(Color.BLACK)
            .setTitleText("选择校区")
            .isDialog(true)
            .setSelectOptions(0)
            .setOutSideCancelable(false)
            .build()
    }

    init {
        isCancelable = false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ElectricitySettingsFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        activityViewModel.queryOptions.observe(this, { (option1, option2) ->
            mOptionPicker.setPicker(option1, option2)
        })
        activityViewModel.defaultPickerOption.observe(this, { (op1, op2) ->
            mOptionPicker.setSelectOptions(op1, op2)
        })

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = activityViewModel
        binding.layoutBuilding.setOnClickListener {
            mOptionPicker.show()
        }
        binding.fab.setOnClickListener {
            activityViewModel.queryElecBalance()
            dismiss()
        }
    }

}
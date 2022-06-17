package cn.ifafu.ifafu.ui.upload

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.PopupMenu
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.viewModels
import androidx.annotation.IntDef
import androidx.core.app.ActivityOptionsCompat
import cn.ifafu.ifafu.R
import cn.ifafu.ifafu.bean.dto.Information
import cn.ifafu.ifafu.bean.vo.Resource
import cn.ifafu.ifafu.databinding.InformationActivityUploadBinding
import cn.ifafu.ifafu.ui.common.BaseActivity
import cn.ifafu.ifafu.ui.common.dialog.LoadingDialog
import cn.ifafu.ifafu.ui.login2.LoginActivity
import cn.ifafu.ifafu.util.setDeBoundClickListener
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class UploadActivity : BaseActivity() {

    @IntDef(PAGE_TYPE_CHECK, PAGE_TYPE_EDIT, PAGE_TYPE_UPLOAD)
    annotation class Type

    private val mLoadingDialog = LoadingDialog(this, "上传中")
    private val viewModel: UploadViewModel by viewModels()

    private lateinit var mPictureAdapter: UploadPictureAdapter

    private lateinit var contactTypeMenu: PopupMenu

    private lateinit var binding: InformationActivityUploadBinding

    companion object {
        private const val MAX_PICTURE_SIZE = 4

        /**
         * 编辑信息
         */
        const val PAGE_TYPE_EDIT = 1

        /**
         * 查看信息
         */
        const val PAGE_TYPE_CHECK = 2

        /**
         * 上传信息
         */
        const val PAGE_TYPE_UPLOAD = 3

        fun intentForEdit(context: Context, information: Information): Intent {
            val intent = Intent(context, UploadActivity::class.java)
            intent.putExtra("type", PAGE_TYPE_UPLOAD)
            intent.putExtra("information", information)
            return intent
        }

        fun intentForUpload(context: Context): Intent {
            val intent = Intent(context, UploadActivity::class.java)
            intent.putExtra("type", PAGE_TYPE_UPLOAD)
            return intent
        }

        fun intentForCheck(context: Context, information: Information): Intent {
            val intent = Intent(context, UploadActivity::class.java)
            intent.putExtra("type", PAGE_TYPE_CHECK)
            intent.putExtra("information", information)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = InformationActivityUploadBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setLightUiBar()
        binding.lifecycleOwner = this
        binding.vm = viewModel

        val pageType = intent.getIntExtra("type", PAGE_TYPE_CHECK)
        viewModel.pageType.value = pageType

        initToolbar()
        initContactTypeLayout()
        initPictureRecycleView(pageType)
        initViewModel()

        if (pageType == PAGE_TYPE_CHECK || pageType == PAGE_TYPE_EDIT) {
            val information = intent.getSerializableExtra("information") as Information
            mPictureAdapter.imageUrls.addAll(information.imageUrls)
            viewModel.originId = information.id
            viewModel.contact.value = information.contact ?: ""
            viewModel.contactType.value = information.contactType
            viewModel.content.value = information.content
        }

        binding.btnOk.setDeBoundClickListener(1000) {
            viewModel.imageUris = mPictureAdapter.imageUris
            viewModel.submit()
        }
    }

    private fun initViewModel() {
        viewModel.message.observe(this, { message ->
            showToast(message)
        })
        viewModel.contactType.observe(this, { contactType ->
            when (contactType) {
                Information.CONTACT_TYPE_QQ -> binding.etContactType.setText(("QQ"))
                Information.CONTACT_TYPE_PHONE -> binding.etContactType.setText("手机")
                Information.CONTACT_TYPE_WECHAT -> binding.etContactType.setText("微信")
                Information.CONTACT_TYPE_NULL -> binding.etContactType.setText("无")
            }
        })
        viewModel.uploadResult.observe(this, { res ->
            when (res) {
                is Resource.Success -> {
                    showToast(res.message)
                    mLoadingDialog.cancel()
                    setResult(RESULT_OK)
                    finish()
                }
                is Resource.Loading -> {
                    mLoadingDialog.show()
                }
                is Resource.Failure -> {
                    mLoadingDialog.cancel()
                    showToast(res.message)
                }
            }
        })
    }

    private fun initPictureRecycleView(@Type type: Int) {
        val launcher = registerForActivityResult(PickPictureContract()) { uri ->
            if (uri != null) {
                mPictureAdapter.imageUris.add(uri)
                mPictureAdapter.notifyItemChanged(mPictureAdapter.imageUris.size - 1)
            }
        }
        mPictureAdapter = UploadPictureAdapter(
            type = type,
            max = MAX_PICTURE_SIZE,
            onAddAction = { launcher.launch(Unit) },
            onPictureClick = { view, uri -> transition(view, uri) })
        binding.rvPicture.adapter = mPictureAdapter
    }

    private fun initContactTypeLayout() {
        contactTypeMenu = PopupMenu(this, binding.etContactType)
        contactTypeMenu.inflate(R.menu.contact_type)
        contactTypeMenu.setOnMenuItemClickListener {
            when (it?.itemId) {
                R.id.contact_type_qq ->
                    viewModel.contactType.value = Information.CONTACT_TYPE_QQ
                R.id.contact_type_wechat ->
                    viewModel.contactType.value = Information.CONTACT_TYPE_WECHAT
                R.id.contact_type_null ->
                    viewModel.contactType.value = Information.CONTACT_TYPE_NULL
                R.id.contact_type_phone ->
                    viewModel.contactType.value = Information.CONTACT_TYPE_PHONE
            }
            true
        }
        binding.etContactType.setOnClickListener {
            contactTypeMenu.show()
        }
    }

    private fun initToolbar() {
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.information_menu_name -> {
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
            true
        }
    }

    private fun transition(view: View, imageUri: Uri) {
        val intent = ShowPictureActivity.intentFor(this, imageUri)
        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
            this, view, getString(R.string.information_picture_translate_name)
        )
        startActivity(intent, options.toBundle())
    }

    inner class PickPictureContract : ActivityResultContract<Unit, Uri?>() {
        override fun createIntent(context: Context, input: Unit): Intent {
            val pickIntent =
                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickIntent.type = "image/*"
            return Intent.createChooser(pickIntent, "选择照片")
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
            return intent?.data
        }

    }
}
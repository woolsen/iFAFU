package cn.ifafu.ifafu.ui.upload

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import cn.ifafu.ifafu.ui.common.BaseViewModel
import cn.ifafu.ifafu.bean.dto.Information
import cn.ifafu.ifafu.bean.vo.Resource
import cn.ifafu.ifafu.exception.IFResponseFailureException
import cn.ifafu.ifafu.repository.InformationRepository
import com.blankj.utilcode.util.FileIOUtils
import com.blankj.utilcode.util.Utils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*
import javax.inject.Inject

@HiltViewModel
class UploadViewModel @Inject constructor(
    private val informationRepository: InformationRepository
) : BaseViewModel() {

    private val _result = MediatorLiveData<Resource<Boolean>>()
    val uploadResult: LiveData<Resource<Boolean>> = _result

    private val _message = MutableLiveData<String>()
    val message: LiveData<String>
        get() = _message

    val pageType = MutableLiveData(UploadActivity.PAGE_TYPE_CHECK)

    var originId: Long = 0
    val contactType = MutableLiveData(Information.CONTACT_TYPE_QQ)
    val contact = MediatorLiveData<String>().apply {
        this.addSource(contactType) { contactType ->
            if (contactType == Information.CONTACT_TYPE_NULL) {
                this.postValue(null)
            }
        }
    }
    val content = MutableLiveData<String>()
    var imageUris = emptyList<Uri>()

    @Suppress("DEPRECATION")
    fun inUpload(content: String, contact: String, contactType: Int) {
        viewModelScope.launch {
            val images = try {
                imageUris.map {
                    val file = getImageFile(it)
                    val shrink = shrink(BitmapFactory.decodeStream(file.inputStream()), 1024 * 1024)
                    FileIOUtils.writeFileFromIS(file, shrink)
                    file
                }
            } catch (e: Exception) {
                Timber.e(e)
                _message.postValue("获取图片出错，${e.message}")
                return@launch
            }
            _result.postValue(Resource.Loading())
            informationRepository.upload(content, contact, contactType, images)
                .flowOn(Dispatchers.IO)
                .catch {
                    if (it is IFResponseFailureException) {
                        _result.postValue(Resource.Failure(it.message))
                    } else {
                        Timber.d(it)
                        _result.postValue(Resource.Failure("上传失败, ${it.message}"))
                    }
                }
                .collectLatest {
                    _result.postValue(Resource.Success(true, "上传成功，等待管理员审核"))
                }
        }
    }

    /**
     * @param size 单位: Byte
     */
    private fun shrink(image: Bitmap, size: Int): ByteArrayInputStream {
        val baos = ByteArrayOutputStream()
        //质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        var options = 100
        //循环判断如果压缩后图片是否大于100kb,大于继续压缩
        while (baos.toByteArray().size > size) {
            baos.reset() //重置baos即清空baos
            //这里压缩options%，把压缩后的数据存放到baos中
            image.compress(Bitmap.CompressFormat.JPEG, options, baos)
            options -= 10 //每次都减少10
        }
        //把压缩后的数据baos存放到ByteArrayInputStream中
        return ByteArrayInputStream(baos.toByteArray())
    }

    private fun getImageFile(uri: Uri): File {
        val contentResolver = Utils.getApp().contentResolver
        val projection = arrayOf(MediaStore.MediaColumns.DATA)
        val cursor = contentResolver.query(uri, projection, null, null, null)
        val fileName = if (cursor != null && cursor.moveToFirst()) {
            File(cursor.getString(0)).name
        } else {
            (UUID.randomUUID().toString()) + ".jpg"
        }
        cursor?.close()
        val file = File(Utils.getApp().cacheDir, fileName)
        contentResolver.openInputStream(uri).use { inputStream ->
            FileIOUtils.writeFileFromIS(file.absolutePath, inputStream)
        }
        return file
    }

    private fun inSubmit(content: String, contact: String, contactType: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            _result.postValue(Resource.Loading())
            val res = informationRepository.edit(originId, content, contact, contactType)
            _result.postValue(res)
        }
    }

    fun submit() {
        val pageType = this.pageType.value
        if (pageType == UploadActivity.PAGE_TYPE_CHECK) {
            return
        }
        val contactType = contactType.value
        val contact = this.contact.value ?: ""
        val content = this.content.value
        if (contactType == null) {
            _message.postValue("请选择联系方式")
        } else if (contact.isBlank() && contactType != Information.CONTACT_TYPE_NULL) {
            _message.postValue("联系方式不能为空")
        } else if (content.isNullOrBlank()) {
            _message.postValue("内容不能为空")
        } else if (pageType == UploadActivity.PAGE_TYPE_UPLOAD) {
            inUpload(content, contact, contactType)
        } else if (pageType == UploadActivity.PAGE_TYPE_EDIT) {
            inSubmit(content, contact, contactType)
        }
    }

}
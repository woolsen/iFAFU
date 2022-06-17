package cn.ifafu.ifafu.ui.common.dialog.mutiluser

import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import cn.ifafu.ifafu.BuildConfig
import cn.ifafu.ifafu.R
import cn.ifafu.ifafu.entity.User
import cn.ifafu.ifafu.repository.UserRepository
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.alibaba.fastjson.JSONObject
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@Suppress("DEPRECATION")
class MultiUserDialog(
    private val context: Context,
    private val onAddClick: (MaterialDialog) -> Unit,
    private val onItemClick: (User) -> Unit
) {

    @Inject
    lateinit var userRepository: UserRepository

    private val adapter = MultiUserAdapter { _, user ->
        onItemClick(user)
    }

    private val dialog =
        MaterialDialog(context).apply {
            customView(viewRes = R.layout.dialog_multi_account)
            title(text = "多账号管理")
            negativeButton(text = "添加账号") {
                onAddClick(it)
            }
            if (BuildConfig.DEBUG) {
                neutralButton(text = "导入账号") {
                    importAccountFromClipboard()
                }
            }
        }.apply {
            this.view.findViewById<RecyclerView>(R.id.rv_list).adapter = adapter
        }

    fun setUsers(users: List<User>) {
        adapter.items = users
        adapter.notifyDataSetChanged()
    }

    fun show() {
        dialog.show()
    }

    fun cancel() {
        dialog.dismiss()
    }

    private fun importAccountFromClipboard() = GlobalScope.launch {
        try {
            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val data = cm.primaryClip
            val item = data!!.getItemAt(0)
            val content = item.text.toString()
            val list = JSONObject.parseArray(content, User::class.java)
            userRepository.saveUsers(list)
            Toast.makeText(context, "导入成功", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "导入失败", Toast.LENGTH_SHORT).show()
        }
    }
}
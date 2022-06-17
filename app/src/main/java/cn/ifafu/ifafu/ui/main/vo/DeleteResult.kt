package cn.ifafu.ifafu.ui.main.vo

import cn.ifafu.ifafu.entity.User

sealed class DeleteResult {
    object Ing: DeleteResult()
    object NeedLogin : DeleteResult()
    object Success: DeleteResult()
    class CheckTo(val user: User): DeleteResult()
}
package cn.ifafu.ifafu.ui.main.vo

import cn.ifafu.ifafu.entity.User

sealed class CheckoutResult {

    object Ing : CheckoutResult()

    class Success(val user: User): CheckoutResult()

    class Failure(val message: String): CheckoutResult()

}
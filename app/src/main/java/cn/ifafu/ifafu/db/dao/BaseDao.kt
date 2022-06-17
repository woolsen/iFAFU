package cn.ifafu.ifafu.db.dao

import cn.ifafu.ifafu.constant.Constants
import com.blankj.utilcode.util.SPUtils

abstract class BaseDao {
    protected fun getAccount(): String {
        return SPUtils.getInstance(Constants.SP_USER_INFO).getString("account")
    }
}
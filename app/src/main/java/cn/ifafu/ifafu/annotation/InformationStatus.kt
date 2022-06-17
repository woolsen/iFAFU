package cn.ifafu.ifafu.annotation

import androidx.annotation.IntDef
import cn.ifafu.ifafu.bean.dto.Information

@IntDef(
    value = [
        Information.STATUS_FAILURE,
        Information.STATUS_PASS,
        Information.STATUS_REVIEWING],
    flag = true
)
@Retention(AnnotationRetention.SOURCE)
annotation class InformationStatus
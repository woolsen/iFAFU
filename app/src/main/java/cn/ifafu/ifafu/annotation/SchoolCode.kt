package cn.ifafu.ifafu.annotation

import androidx.annotation.StringDef
import cn.ifafu.ifafu.entity.User.Companion.FAFU
import cn.ifafu.ifafu.entity.User.Companion.FAFU_JS

@MustBeDocumented
@StringDef(value = [FAFU, FAFU_JS])
annotation class SchoolCode
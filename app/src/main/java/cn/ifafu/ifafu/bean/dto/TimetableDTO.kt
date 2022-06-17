package cn.ifafu.ifafu.bean.dto

import cn.ifafu.ifafu.entity.NewCourse

data class TimetableDTO(
    val courses: List<NewCourse>
)
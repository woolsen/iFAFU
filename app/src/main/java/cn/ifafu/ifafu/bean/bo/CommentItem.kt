package cn.ifafu.ifafu.bean.bo

data class CommentItem(
        var courseName: String, //课程名称
        var teacherName: String = "", //老师名称
        var commentFullUrl: String, //评教链接（完整的URL）
        var isDone: Boolean? = null //是否已评价
)
package cn.ifafu.ifafu.service.mapper

import cn.ifafu.ifafu.entity.User
import cn.ifafu.ifafu.service.mapper.commentlist.CommentListListMapper
import cn.ifafu.ifafu.service.mapper.commentlist.CommentListListMapper2
import cn.ifafu.ifafu.service.mapper.commentlist.ICommentListMapper

object CommentListMapperProvider {

    fun get(school: String): ICommentListMapper {
        return when (school) {
            User.FAFU -> CommentListListMapper()
            User.FAFU_JS -> CommentListListMapper2()
            else -> throw IllegalArgumentException("不支持当前学校")
        }
    }

}
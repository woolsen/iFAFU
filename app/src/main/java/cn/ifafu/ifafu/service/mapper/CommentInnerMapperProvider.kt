package cn.ifafu.ifafu.service.mapper

import cn.ifafu.ifafu.entity.User
import cn.ifafu.ifafu.service.mapper.commentinner.CommentInnerMapper
import cn.ifafu.ifafu.service.mapper.commentinner.CommentInnerMapper2
import cn.ifafu.ifafu.service.mapper.commentinner.ICommentInnerMapper

object CommentInnerMapperProvider {

    fun get(school: String): ICommentInnerMapper {
        return when (school) {
            User.FAFU -> CommentInnerMapper()
            User.FAFU_JS -> CommentInnerMapper2()
            else -> throw IllegalArgumentException("不支持当前学校")
        }
    }

}
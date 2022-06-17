package cn.ifafu.ifafu.service.mapper

import cn.ifafu.ifafu.entity.User
import cn.ifafu.ifafu.service.mapper.commentcommit.CommentCommitMapper
import cn.ifafu.ifafu.service.mapper.commentcommit.ICommentCommitMapper

object CommentCommitMapperProvider {

    fun get(school: String): ICommentCommitMapper {
        return when (school) {
            User.FAFU -> CommentCommitMapper()
            User.FAFU_JS -> CommentCommitMapper()
            else -> throw IllegalArgumentException("不支持当前学校")
        }
    }

}
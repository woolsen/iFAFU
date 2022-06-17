package cn.ifafu.ifafu.service.mapper.commentlist

import cn.ifafu.ifafu.bean.bo.CommentItem
import cn.ifafu.ifafu.bean.dto.IFResponse

interface ICommentListMapper {

    fun map(html: String): IFResponse<List<CommentItem>>
}
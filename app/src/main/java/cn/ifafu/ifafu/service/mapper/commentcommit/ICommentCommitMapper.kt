package cn.ifafu.ifafu.service.mapper.commentcommit

import cn.ifafu.ifafu.bean.dto.IFResponse

interface ICommentCommitMapper {

    /**
     * 解析是否提交成功
     */
    fun map(html: String): IFResponse<Unit>

}
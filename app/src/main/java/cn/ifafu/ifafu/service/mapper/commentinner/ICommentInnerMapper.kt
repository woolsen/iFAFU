package cn.ifafu.ifafu.service.mapper.commentinner

interface ICommentInnerMapper {

    /**
     * 评教选项
     *
     * @return key对应需要提交的键, value对应需要提交的值
     */
    fun map(html: String): MutableMap<String, String>

}

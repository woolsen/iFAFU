package cn.ifafu.ifafu.bean.dto

enum class CodeEnum(val code: Int, val message: String) {
    SUCCESS(200, "成功"),
    FAILURE(400, "失败"),
    NO_AUTH(401, "未登录"),
    NOT_FOUND(404, "资源不存在");
}
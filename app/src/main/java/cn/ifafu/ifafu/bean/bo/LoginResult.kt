package cn.ifafu.ifafu.bean.bo

sealed class LoginResult {
    /**
     * 新生（特指需要修改密码的新生）
     */
    object NewStudent: LoginResult()

    class NormalStudent(val name: String): LoginResult()
}
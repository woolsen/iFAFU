package cn.ifafu.ifafu.service.user

import cn.ifafu.ifafu.service.parser.ChangePasswordParser

object ChangePasswordParserTest {

    private val html = "<script language='javascript'>alert('两次输入的新密码不相同？！');</script>\n" +
            "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n" +
            "<HTML \n" +
            "lang=gb2312>\n" +
            "  <HEAD>\n" +
            "\t\t<title>现代教学管理信息系统</title><meta http-equiv=\"X-UA-Compatible\" content=\"IE=EmulateIE7\">\n" +
            "\t\t<meta http-equiv=\"Content-Type\" content=\"text/html; charset=gb2312\">\n" +
            "\t\t<meta http-equiv=\"Content-Language\" content=\"gb2312\">\n" +
            "\t\t<meta content=\"all\" name=\"robots\">\n" +
            "\t\t<meta name=\"author\" content=\"作者信息\">\n" +
            "\t\t<meta name=\"Copyright\" content=\"版权信息\">\n" +
            "\t\t<meta name=\"description\" content=\"站点介绍\">\n" +
            "\t\t<meta name=\"keywords\" content=\"站点关键词\">\n" +
            "\t\t<link rel=\"icon\" href=\"style/base/favicon.ico\" type=\"image/x-icon\">\n" +
            "\t\t\t<link rel=\"stylesheet\" href=\"style/base/jw.css\" type=\"text/css\" media=\"all\">\n" +
            "\t\t\t\t<link rel=\"stylesheet\" href=\"style/standard/jw.css\" type=\"text/css\" media=\"all\">\n" +
            "\t\t\t\t\t<script language=\"javascript\">\n" +
            "\t\t\t    function MmCheck(){\n" +
            "\t\t\t       var obj=document.getElementById(\"TextBox3\")\n" +
            "\t\t\t       var obj1=document.getElementById(\"TextBox4\")\n" +
            "\t\t\t       if (obj.value.length<6 || obj1.value.length<6){\n" +
            "\t\t\t          alert(\"密码长度不能小于 6 位！\");\n" +
            "\t\t\t          return false;\n" +
            "\t\t\t       }\n" +
            "\t\t\t       \n" +
            "\t\t\t       \n" +
            "\t\t\t    }\n" +
            "\t\t\t    function Email_Changed(){\n" +
            "\t\t\t\t\tvar obj = document.getElementById(\"TextBox8\");\n" +
            "\t\t\t\t\tstr=obj.value;"

    @JvmStatic
    fun main(args: Array<String>) {
        val parser = ChangePasswordParser()
        println(parser.parse(html))
    }

}
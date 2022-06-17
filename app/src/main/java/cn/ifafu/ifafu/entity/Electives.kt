package cn.ifafu.ifafu.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 选修学分需求
 */
@Entity
class Electives {
    @PrimaryKey
    var account: String = ""
    var total = 0 //任意选修课毕业学分要求
    var zrkx = 0 //自然科学类
    var rwsk = 0 //人文社科类
    var ysty = 0 //艺术体育类
    var wxsy = 0 //文学素养类
    var cxcy = 0 //创新创业教育类

    fun set(total: Int, zrkx: Int, rwsk: Int, ysty: Int, wxsy: Int, cxcy: Int) {
        this.total = total
        this.zrkx = zrkx
        this.rwsk = rwsk
        this.ysty = ysty
        this.wxsy = wxsy
        this.cxcy = cxcy
    }

}
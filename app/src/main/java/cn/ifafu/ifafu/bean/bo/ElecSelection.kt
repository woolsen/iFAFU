package cn.ifafu.ifafu.bean.bo

class ElecSelection(
    val id: Int, //自己定的

    val areaAlias: String,
    val buildingAlias: String,

    /* 学付宝给的 */
    val aid: String,
    val name: String,
    val areaId: String = "",
    val area: String = "",
    val buildingId: String = "",
    val building: String = "",
    val floorId: String = "",
    val floor: String = ""

)
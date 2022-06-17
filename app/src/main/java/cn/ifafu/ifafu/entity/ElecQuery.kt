package cn.ifafu.ifafu.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
class ElecQuery {
    @PrimaryKey
    var account: String = ""
    var xfbId: String = ""
    var aid = ""
    var room = ""
    var floorId = ""
    var floor = ""
    var areaId = ""
    var area = ""
    var buildingId = ""
    var building = ""

    private fun toJsonString(): String {
        return "{\"aid\":\"" + aid + "\"," +
                "\"account\":\"" + xfbId + "\"," +
                "\"room\":{\"roomid\":\"" + room + "\",\"room\":\"" + room + "\"}," +
                "\"floor\":{\"floorid\":\"" + floorId + "\",\"floor\":\"" + floor + "\"}," +
                "\"area\":{\"area\":\"" + areaId + "\",\"areaname\":\"" + area + "\"}," +
                "\"building\":{\"buildingid\":\"" + buildingId + "\",\"building\":\"" + building + "\"}}"
    }

    fun toFiledMap(type: Query?): Map<String, String> {
        val query: String
        val funname: String
        when (type) {
            Query.APPINFO -> {
                query = "query_appinfo"
                funname = "synjones.onecard.query.appinfo"
            }
            Query.AREA -> {
                query = "query_elec_area"
                funname = "synjones.onecard.query.elec.area"
            }
            Query.BUILDING -> {
                query = "query_elec_building"
                funname = "synjones.onecard.query.elec.building"
            }
            Query.FLOOR -> {
                query = "query_elec_floor"
                funname = "synjones.onecard.query.elec.floor"
            }
            Query.ROOM -> {
                query = "query_elec_room"
                funname = "synjones.onecard.query.elec.room"
            }
            Query.ROOMINFO -> {
                query = "query_elec_roominfo"
                funname = "synjones.onecard.query.elec.roominfo"
            }
            else -> {
                query = ""
                funname = ""
            }
        }
        val filedMap: MutableMap<String, String> = HashMap()
        filedMap["jsondata"] = "{\"" + query + "\":" + toJsonString() + "}"
        filedMap["funname"] = funname
        filedMap["json"] = "true"
        return filedMap
    }

    enum class Query {
        APPINFO, AREA, BUILDING, FLOOR, ROOM, ROOMINFO
    }
}
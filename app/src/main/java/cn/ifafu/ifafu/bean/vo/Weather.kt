package cn.ifafu.ifafu.bean.vo

class Weather {
    var cityName: String = ""
    var nowTemp = 0 //当前温度
    var amTemp = 0 //上午温度
    var pmTemp = 0 //下午温度
    var weather: String = "" //田七

    constructor()
    constructor(cityName: String) {
        this.cityName = cityName
    }

    override fun toString(): String {
        return "Weather{" +
                "cityName='" + cityName + '\'' +
                ", nowTemp=" + nowTemp +
                ", amTemp=" + amTemp +
                ", pmTemp=" + pmTemp +
                ", weather='" + weather + '\'' +
                '}'
    }
}
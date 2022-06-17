package cn.ifafu.ifafu.bean.vo;

import cn.ifafu.ifafu.ui.view.timetable.DayOfWeek;

public class ScheduleItem {
    public String name; //课程名称
    public String address;//上课地点
    @DayOfWeek
    public int dayOfWeek; //星期（必须）
    public int startNode; //开始节数（必须）
    public int nodeCount; //持续节数（必须）
    public Object tag; //附加信息（id）
}

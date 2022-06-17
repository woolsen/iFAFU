package cn.ifafu.ifafu.ui.view.timetable;

public class TimetableItem {
    public String name; //课程名称
    public String address;//上课地点
    @DayOfWeek
    public int dayOfWeek; //星期（必须）
    public int startNode; //开始节数（必须）
    public int nodeCount; //持续节数（必须）
    public Object tag; //附加信息
}

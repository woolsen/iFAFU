package cn.ifafu.ifafu.ui.widget

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.View
import android.widget.RemoteViews
import cn.ifafu.ifafu.R
import cn.ifafu.ifafu.bean.vo.NextCourseVO
import cn.ifafu.ifafu.bean.vo.Resource
import cn.ifafu.ifafu.constant.Constants
import cn.ifafu.ifafu.domain.course.LoadNextCourseUseCase
import cn.ifafu.ifafu.repository.TimetableRepository
import cn.ifafu.ifafu.ui.timetable.TimetableActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class SyllabusWidget : AppWidgetProvider() {

    @Inject
    lateinit var repository: TimetableRepository

    private suspend fun updateSyllabusWidget(context: Context, remoteViews: RemoteViews) {
        withContext(Dispatchers.IO) {
            //设置跳转课表按钮监听
            val syllabusIntent: PendingIntent = run {
                val intent = Intent(context, TimetableActivity::class.java)
                intent.putExtra("from", Constants.SYLLABUS_WIDGET)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    PendingIntent.getActivity(context, 0, intent, FLAG_IMMUTABLE)
                } else {
                    PendingIntent.getActivity(context, 0, intent, 0)
                }
            }
            remoteViews.setOnClickPendingIntent(R.id.btn_go, syllabusIntent)
            //设置刷新Widget按钮监听
            val refreshIntent: PendingIntent = Intent()
                .let { intent ->
                    intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        PendingIntent.getBroadcast(context, 0, intent, FLAG_IMMUTABLE)
                    } else {
                        PendingIntent.getBroadcast(context, 0, intent, 0)
                    }
                }
            remoteViews.setOnClickPendingIntent(R.id.btn_refresh, refreshIntent)
            //更新刷新时间
            val format = SimpleDateFormat("HH:mm:ss", Locale.CHINA)
            val now = context.getString(R.string.refresh_time_format, format.format(Date()))
            remoteViews.setTextViewText(R.id.tv_refresh_time, now)

            val nextCourseUseCase = LoadNextCourseUseCase(repository)
            val op = repository.getTermOptions()?.selected
            if (op == null) {
                remoteViews.setCourseInfoLayoutVisible(false)
                remoteViews.setTextViewText(R.id.message, "查询课表失败")
                return@withContext
            }

            when (val previewRes = nextCourseUseCase.invoke(Unit)) {
                is Resource.Success -> {
                    setPreviewViewInfo(context, remoteViews, previewRes.data)
                }
                is Resource.Failure -> {
                    remoteViews.setCourseInfoLayoutVisible(false)
                    remoteViews.setTextViewText(R.id.message, previewRes.message)
                }
                else -> {}
            }
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        appWidgetIds.forEach { appWidgetId ->
            val views = RemoteViews(context.packageName, R.layout.timetable_widget)
            runBlocking {
                updateSyllabusWidget(context, views)
            }
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
            val remoteViews = RemoteViews(context.packageName, R.layout.timetable_widget)
            runBlocking {
                updateSyllabusWidget(context, remoteViews)
            }
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val provider = ComponentName(context, SyllabusWidget::class.java)
            //更新Widget
            appWidgetManager.updateAppWidget(provider, remoteViews)
        }
    }

    private fun setPreviewViewInfo(
        context: Context,
        remoteViews: RemoteViews,
        preview: NextCourseVO
    ) = with(remoteViews) {
        setTextViewText(R.id.tv_week_time, preview.dateText)
        when (preview) {
            is NextCourseVO.HasClass -> {
                //显示详情信息
                setCourseInfoLayoutVisible(true)
                setTextViewText(R.id.tv_location, preview.address)
                setTextViewText(R.id.classTime, preview.classTime)
                setTextViewText(R.id.timeLeft, preview.timeLeft)
                val numOfClass = preview.numberOfClasses
                setTextViewText(
                    R.id.tv_total,
                    context.getString(R.string.node_format, numOfClass.first, numOfClass.second)
                )
                if (preview.isInClass) {
                    setTextViewText(
                        R.id.tv_next,
                        context.getString(R.string.now_class_format, preview.nextClass)
                    )
                    setTextViewText(R.id.tv_status, context.getString(R.string.in_class))
                    setTextViewCompoundDrawables(
                        R.id.tv_status, R.drawable.ic_point_blue, 0, 0, 0
                    )
                } else {
                    setTextViewText(
                        R.id.tv_next,
                        context.getString(R.string.next_class_format, preview.nextClass)
                    )
                    setTextViewText(R.id.tv_status, context.getString(R.string.out_class))
                    setTextViewCompoundDrawables(
                        R.id.tv_status, R.drawable.ic_point_red, 0, 0, 0
                    )
                }
            }
            is NextCourseVO.NoClass -> {
                setCourseInfoLayoutVisible(false)
                setTextViewText(R.id.message, preview.message)
            }
        }
    }

    private fun RemoteViews.setCourseInfoLayoutVisible(show: Boolean) {
        if (show) {
            setViewVisibility(R.id.message, View.GONE)
            setViewVisibility(R.id.tv_next, View.VISIBLE)
            setViewVisibility(R.id.tv_location, View.VISIBLE)
            setViewVisibility(R.id.layout_class_time, View.VISIBLE)
            setViewVisibility(R.id.layout_left_time, View.VISIBLE)
        } else {
            setViewVisibility(R.id.message, View.VISIBLE)
            setViewVisibility(R.id.tv_next, View.GONE)
            setViewVisibility(R.id.tv_location, View.GONE)
            setViewVisibility(R.id.layout_class_time, View.GONE)
            setViewVisibility(R.id.layout_left_time, View.GONE)
        }
    }
}


package cn.ifafu.ifafu.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import cn.ifafu.ifafu.constant.DATABASE_NAME
import cn.ifafu.ifafu.db.converter.*
import cn.ifafu.ifafu.db.dao.*
import cn.ifafu.ifafu.entity.*

@Database(
    entities = [
        Course::class,
        User::class,
        Exam::class,
        Score::class,
        SyllabusSetting::class,
        GlobalSetting::class,
        ElecQuery::class,
        ElecUser::class,
        ElecCookie::class,
        ElectricityHistory::class,
        Electives::class,
        ScoreFilter::class,
        NewCourse::class,
        OptionDO::class,
        FirstWeek::class,
        Holiday::class
    ],
    version = 18,
    exportSchema = true
)
@TypeConverters(
    value = [
        IntTreeSetConverter::class,
        IntListConverter::class,
        StringMapConverter::class,
        LongListConverter::class,
        IntSortedSetConverter::class
    ]
)
abstract class JiaowuDatabase : RoomDatabase() {
    abstract val userDao: UserDao
    abstract val examDao: ExamDao
    abstract val scoreDao: ScoreDao
    abstract val scoreFilterDao: ScoreFilterDao
    abstract val syllabusSettingDao: SyllabusSettingDao
    abstract val globalSettingDao: GlobalSettingDao
    abstract val elecQueryDao: ElecQueryDao
    abstract val elecUserDao: ElecUserDao
    abstract val elecCookieDao: ElecCookieDao
    abstract val electivesDao: ElectivesDao
    abstract val newCourseDao: CourseDao
    abstract val openingDayDao: OpeningDayDao
    abstract val electricityHistoryDao: ElectricityHistoryDao
    abstract val holidayDao: HolidayDao

    companion object {

        fun getInstance(context: Context): JiaowuDatabase {
            return Room.databaseBuilder(context, JiaowuDatabase::class.java, DATABASE_NAME)
                .addMigrations(
                    migration_5_6,
                    migration_6_7,
                    migration_7_8,
                    migration_8_9,
                    migration_9_10,
                    migration_10_11,
                    migration_11_12,
                    migration_12_13,
                    migration_13_14,
                    migration_13_15,
                    migration_14_15,
                    migration_15_16,
                    migration_16_17,
                    migration_17_18
                )
                .build()
        }

        private val migration_5_6
            get() = object : Migration(5, 6) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.execSQL("ALTER TABLE User ADD last_login_time INTEGER NOT NULL DEFAULT 0;")
                }
            }
        private val migration_6_7
            get() = object : Migration(6, 7) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.execSQL("DROP TABLE IF EXISTS ScoreFilter")
                }
            }
        private val migration_7_8
            get() = object : Migration(7, 8) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.execSQL("DROP TABLE IF EXISTS Token")
                }
            }
        private val migration_8_9
            get() = object : Migration(8, 9) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.execSQL("CREATE TABLE IF NOT EXISTS ScoreFilter (`scoreId` INTEGER NOT NULL, `account` TEXT NOT NULL, PRIMARY KEY(`scoreId`))")
                }
            }
        private val migration_9_10
            get() = object : Migration(9, 10) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.execSQL("DROP TABLE IF EXISTS ScoreFilter")
                    database.execSQL("CREATE TABLE IF NOT EXISTS `ScoreFilter` (`scoreId` INTEGER NOT NULL, `account` TEXT NOT NULL, `isIESItem` INTEGER NOT NULL, PRIMARY KEY(`scoreId`))")
                }
            }
        private val migration_10_11
            get() = object : Migration(10, 11) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.execSQL("CREATE TABLE IF NOT EXISTS `new_course` (`id` INTEGER NOT NULL, `name` TEXT NOT NULL, `teacher` TEXT NOT NULL, `classroom` TEXT NOT NULL, `weeks` TEXT NOT NULL, `weekday` INTEGER NOT NULL, `beginNode` INTEGER NOT NULL, `nodeLength` INTEGER NOT NULL, `year` TEXT NOT NULL, `term` TEXT NOT NULL, `account` TEXT NOT NULL, PRIMARY KEY(`id`))")
                }
            }
        private val migration_11_12
            get() = object : Migration(11, 12) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.execSQL("CREATE TABLE IF NOT EXISTS `option` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `yearText` TEXT NOT NULL, `yearValue` TEXT NOT NULL, `termText` TEXT NOT NULL, `termValue` TEXT NOT NULL, `account` TEXT NOT NULL)")
                }
            }
        private val migration_12_13
            get() = object : Migration(12, 13) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.execSQL("DROP TABLE IF EXISTS `new_course`")
                    database.execSQL("CREATE TABLE IF NOT EXISTS `new_course` (`id` INTEGER NOT NULL, `name` TEXT NOT NULL, `teacher` TEXT NOT NULL, `classroom` TEXT NOT NULL, `weeks` TEXT NOT NULL, `weekday` INTEGER NOT NULL, `beginNode` INTEGER NOT NULL, `nodeLength` INTEGER NOT NULL, `year` TEXT NOT NULL, `term` TEXT NOT NULL, `account` TEXT NOT NULL, `local` INTEGER NOT NULL, PRIMARY KEY(`id`))")
                }
            }

        private val migration_13_14
            get() = object : Migration(13, 14) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.execSQL("CREATE TABLE IF NOT EXISTS `to_week` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `year` TEXT NOT NULL, `term` TEXT NOT NULL, `offset` INTEGER NOT NULL)")
                }

            }

        private val migration_14_15
            get() = object : Migration(14, 15) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.execSQL("DROP TABLE IF EXISTS `to_week`")
                    database.execSQL("CREATE TABLE IF NOT EXISTS `to_week` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `year` TEXT NOT NULL, `term` TEXT NOT NULL, `openingDay` TEXT NOT NULL)")
                }

            }

        private val migration_13_15
            get() = object : Migration(13, 15) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.execSQL("CREATE TABLE IF NOT EXISTS `to_week` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `year` TEXT NOT NULL, `term` TEXT NOT NULL, `openingDay` TEXT NOT NULL)")
                }
            }


        private val migration_15_16
            get() = object : Migration(15, 16) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.execSQL("DROP TABLE IF EXISTS `option`")
                    database.execSQL("CREATE TABLE IF NOT EXISTS `option` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `year` TEXT NOT NULL, `term` TEXT NOT NULL, `account` TEXT NOT NULL)")
                }
            }


        private val migration_16_17
            get() = object : Migration(16, 17) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.execSQL("DROP TABLE IF EXISTS `ElecHistory`")
                    database.execSQL("CREATE TABLE IF NOT EXISTS `ElecHistory` (`dorm` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, `balance` REAL NOT NULL, `type` INTEGER NOT NULL, PRIMARY KEY(`dorm`, `timestamp`))")
                }
            }

        private val migration_17_18
            get() = object : Migration(17, 18) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.execSQL("CREATE TABLE IF NOT EXISTS `holiday` (`id` INTEGER NOT NULL, `name` TEXT NOT NULL, `days` INTEGER NOT NULL, `from` TEXT NOT NULL, `changes` TEXT NOT NULL, PRIMARY KEY(`id`))")
                }
            }
    }

}
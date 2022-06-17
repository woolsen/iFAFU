package cn.ifafu.ifafu.di

import cn.ifafu.ifafu.db.IfafuDatabase
import cn.ifafu.ifafu.db.JiaowuDatabase
import cn.ifafu.ifafu.db.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped

@Module
@InstallIn(ActivityRetainedComponent::class)
object DaoModule {

    @Provides
    @ActivityRetainedScoped
    fun provideUserDao(database: JiaowuDatabase): UserDao {
        return database.userDao
    }

    @Provides
    @ActivityRetainedScoped
    fun provideElecCookieDao(database: JiaowuDatabase): ElecCookieDao {
        return database.elecCookieDao
    }

    @Provides
    @ActivityRetainedScoped
    fun provideElecQueryDao(database: JiaowuDatabase): ElecQueryDao {
        return database.elecQueryDao
    }

    @Provides
    @ActivityRetainedScoped
    fun provideElecUserDao(database: JiaowuDatabase): ElecUserDao {
        return database.elecUserDao
    }

    @Provides
    @ActivityRetainedScoped
    fun provideExamDao(database: JiaowuDatabase): ExamDao {
        return database.examDao
    }

    @Provides
    @ActivityRetainedScoped
    fun provideNewCourseDao(database: JiaowuDatabase): CourseDao {
        return database.newCourseDao
    }

    @Provides
    @ActivityRetainedScoped
    fun provideUserInfoDao(appDatabase: IfafuDatabase): UserInfoDao {
        return appDatabase.userInfoDao
    }

}
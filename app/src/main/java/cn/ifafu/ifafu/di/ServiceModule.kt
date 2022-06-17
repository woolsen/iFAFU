package cn.ifafu.ifafu.di

import cn.ifafu.ifafu.service.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped
import retrofit2.Retrofit


@Module
@InstallIn(ActivityRetainedComponent::class)
object ServiceModule {

    /**
     * 学付宝服务
     */
    @Provides
    @ActivityRetainedScoped
    fun provideXfbService(
        @Xfb retrofit: Retrofit
    ): XfbService {
        return retrofit.create(XfbService::class.java)
    }

    /**
     * iFAFU服务
     */
    @Provides
    @ActivityRetainedScoped
    fun provideIFAFUService(
        @Ifafu retrofit: Retrofit
    ): IFAFUService {
        return retrofit.create(IFAFUService::class.java)
    }

    @Provides
    @ActivityRetainedScoped
    fun provideIFAFUUserService(
        @Ifafu retrofit: Retrofit
    ): IFAFUUserService {
        return retrofit.create(IFAFUUserService::class.java)
    }

    @Provides
    @ActivityRetainedScoped
    fun provideInformationService(
        @Ifafu retrofit: Retrofit
    ): InformationService {
        return retrofit.create(InformationService::class.java)
    }

    @Provides
    @ActivityRetainedScoped
    fun provideInformationExamineService(
        @Ifafu retrofit: Retrofit
    ): ExamineService {
        return retrofit.create(ExamineService::class.java)
    }

    @Provides
    @ActivityRetainedScoped
    fun provideFeedbackService(
        @Ifafu retrofit: Retrofit
    ): FeedbackService {
        return retrofit.create(FeedbackService::class.java)
    }
}
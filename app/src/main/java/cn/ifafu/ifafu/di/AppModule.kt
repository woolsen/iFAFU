package cn.ifafu.ifafu.di

import android.content.Context
import androidx.room.Room
import cn.ifafu.ifafu.db.IfafuDatabase
import cn.ifafu.ifafu.db.JiaowuDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): JiaowuDatabase {
        return JiaowuDatabase.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideIfafuDatabase(@ApplicationContext context: Context): IfafuDatabase {
        return Room.databaseBuilder(context, IfafuDatabase::class.java, "ifafu")
            .fallbackToDestructiveMigration()
            .build()
    }


}
package cn.ifafu.ifafu.repository

import cn.ifafu.ifafu.db.JiaowuDatabase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SemesterRepository @Inject constructor(
    database: JiaowuDatabase
) : AbstractJwRepository(database.userDao)
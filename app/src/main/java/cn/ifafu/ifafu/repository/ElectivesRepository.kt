package cn.ifafu.ifafu.repository

import cn.ifafu.ifafu.bean.bo.ZFApiEnum
import cn.ifafu.ifafu.db.JiaowuDatabase
import cn.ifafu.ifafu.entity.Electives
import cn.ifafu.ifafu.service.common.ZFHttpClient
import cn.ifafu.ifafu.service.common.ZfUrlProvider
import cn.ifafu.ifafu.service.parser.ElectivesParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ElectivesRepository @Inject constructor(
    database: JiaowuDatabase,
    private val client: ZFHttpClient,
) : AbstractJwRepository(database.userDao) {

    private val electivesDao = database.electivesDao

    suspend fun get(): Electives = withContext(Dispatchers.IO) {
        val user = getUsingUser()
            ?: return@withContext Electives()
        val electives = electivesDao.electives(user.account)
        if (electives != null) {
            return@withContext electives
        }
        val response = client.ensureLogin(user) {
            val queryUrl = ZfUrlProvider.getUrl(ZFApiEnum.ELECTIVES, user)
            val mainUrl = ZfUrlProvider.getUrl(ZFApiEnum.MAIN, user)
            val html = client.get(queryUrl, mainUrl).body!!.string()
            ElectivesParser(user).parse(html)
        }
        return@withContext response.data ?: Electives()
    }

    suspend fun save(electives: Electives) = withContext(Dispatchers.Default) {
        electivesDao.save(electives)
    }
}
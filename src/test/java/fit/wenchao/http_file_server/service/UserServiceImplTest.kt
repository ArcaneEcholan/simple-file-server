package fit.wenchao.http_file_server.service

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import fit.wenchao.http_file_server.dao.po.UserPO
import fit.wenchao.http_file_server.dao.repo.UserDao
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
 class UserServiceImplTest{

    @Autowired
    lateinit var userDao:UserDao
    @Test
      fun test() {
        val list = userDao.list(QueryWrapper<UserPO>().select("id", "username").eq("id", 1))
        println(list)
    }
 }
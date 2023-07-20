package fit.wenchao.http_file_server.service

import fit.wenchao.http_file_server.constants.PermissionConstants
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class AuthServiceImplTest {

    @Autowired
    lateinit var authService: AuthService

    @Test
    fun authorizeOr() {
        val usernamePasswordToken = UsernamePasswordToken("test", "test")
        var entity =  authService.authenticate(usernamePasswordToken)

        val permissionCollection = PermissionCollectionImpl().apply {
            this.list.add(SimplePermission(PermissionConstants.SYSTEM_CONFIG.toString()))
            this.list.add(SimplePermission(PermissionConstants.ROLE.toString()))
        }
        authService.authorizeOr(entity, permissionCollection)
    }

    @Test
    fun authorizeAnd() {
    }
}


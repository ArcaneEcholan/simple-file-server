package fit.wenchao.http_file_server.service

import fit.wenchao.http_file_server.constants.PermissionConstants
import org.apache.shiro.authz.permission.WildcardPermission
import org.apache.shiro.authz.permission.WildcardPermissionResolver
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import java.io.Serializable


interface AuthToken {
    fun getPrincipal(): EntityPrincipal

    fun getCredential(): EntityCredential

    fun getTokenType(): AuthTokenType
}

interface AuthTokenType {

}

class TokenTokenType : AuthTokenType {


}

class UsernamePasswordTokenType : AuthTokenType {

}


class TokenToken : AuthToken {

    var token: String = ""

    override fun getPrincipal(): EntityPrincipal {
        return EntityPrincipalImpl().apply { value = token }
    }

    override fun getCredential(): EntityCredential {
        return EntityCredentialImpl().apply { value = token }
    }

    override fun getTokenType(): AuthTokenType {
        return TokenTokenType()
    }

}

class UsernamePasswordToken : AuthToken {

    private var principal: EntityPrincipal
    private var credential: EntityCredential


    constructor() {
        this.principal = EntityPrincipalImpl()
        this.credential = EntityCredentialImpl()
    }

    constructor(principal: String, credential: String) {
        this.principal = EntityPrincipalImpl().apply { value = principal }
        this.credential = EntityCredentialImpl().apply { value = credential }
    }

    override fun getPrincipal(): EntityPrincipal {
        return principal
    }

    override fun getCredential(): EntityCredential {
        return credential
    }

    override fun getTokenType(): AuthTokenType {
        return UsernamePasswordTokenType();
    }

}


interface EntityPrincipal {
    fun value(): String
}

interface EntityCredential {
    fun value(): String
}

class EntityPrincipalImpl : EntityPrincipal {
    var value: String = ""
    override fun value(): String {
        return value
    }
}

class EntityCredentialImpl : EntityCredential {
    var value: String = ""
    override fun value(): String {
        return value
    }
}

interface Entity {
    fun getPrincipal(): EntityPrincipal

    fun getCredential(): EntityCredential

    fun authced(): Boolean
}


class SimpleEntity : Entity {
    private var principal: EntityPrincipal
    private var credential: EntityCredential

    var authenticated: Boolean = false

    constructor(principal: EntityPrincipal, credential: EntityCredential) {
        this.principal = principal
        this.credential = credential
    }

    override fun getPrincipal(): EntityPrincipal {
        return principal
    }

    override fun getCredential(): EntityCredential {
        return credential
    }

    override fun authced(): Boolean {
        return authenticated
    }


}


interface ThreadAuthContext {
    fun bind(entity: Entity)
    fun getEntity(): Entity?
}

@Component
class ThreadAuthContextImpl : ThreadAuthContext {
    val entityObjectKey = "entity"
    var tl: ThreadLocal<MutableMap<String, Entity>> = ThreadLocal()
    override fun bind(entity: Entity) {
        tl.get() ?: tl.set(mutableMapOf())
        tl.get()[entityObjectKey] = entity
    }

    override fun getEntity(): Entity? {
        return tl.get()[entityObjectKey]
    }


}


interface PermissionCollection {
    fun getPermissions(): List<Permission>
    fun getShiroPermissions(): List<org.apache.shiro.authz.Permission>
    fun merge(perms: PermissionCollection)
}

class PermissionCollectionImpl : PermissionCollection {

    var list: MutableList<Permission> = mutableListOf()

    var permissionResolver: org.apache.shiro.authz.permission.PermissionResolver = WildcardPermissionResolver()


    override fun getPermissions(): List<Permission> {
        return list
    }

    override fun getShiroPermissions(): List<org.apache.shiro.authz.Permission> {
        list.map { WildcardPermission(it.toString(), false) }.toList().let { return it }
    }

    override fun merge(perms: PermissionCollection) {
        perms.getPermissions().forEach { permissionToMergeIn ->
            list.find {
                it.toString() == permissionToMergeIn.toString()
            } ?: run {
                list.add(permissionToMergeIn)
            }
        }

    }


}

interface Permission {

    override fun toString(): String

}


class SimplePermission(var name: String) : Permission {

    override fun toString(): String {
        return name;
    }

}

interface RequestEntryPoint {
    fun getRequiredPermissions(): PermissionCollection
}


enum class Logical {
    AND, OR
}

annotation class PermissionRequired(
    val value: Array<PermissionConstants>,
    val logical: Logical = Logical.OR)



class SpringHandlerMethodRequestEntryPoint : RequestEntryPoint {

    var handlerMethod: HandlerMethod

    constructor(handlerMethod: HandlerMethod) {
        this.handlerMethod = handlerMethod
    }

    override fun getRequiredPermissions(): PermissionCollection {
        val methodAnnotation = handlerMethod.getMethodAnnotation(PermissionRequired::class.java)
        var requiredPermissions =
            methodAnnotation?.value?.map { SimplePermission(it.name) as Permission }?.toMutableList()
        return PermissionCollectionImpl().apply { this.list = requiredPermissions ?: mutableListOf() }
    }

}

interface Role {

}

class SimpleRole : Role {
    var name: String = ""
    override fun toString(): String {
        return name
    }
}


interface IdentificationDataType {
    fun name(): String
}

class NumberIdentificationDataType : IdentificationDataType {
    override fun name(): String {
        return "Number"
    }
}

class StringIdentificationDataType : IdentificationDataType {
    override fun name(): String {
        return "String"
    }
}

interface EntityIdentification {

    fun identificationDataType(): IdentificationDataType

    fun identification(): Serializable

    fun entityType(): EntityType

}

interface EntityType {
    fun getName(): String;
}

class UserEntityType : EntityType {
    override fun getName(): String {
        return "user"
    }

}


class NumberEntityIdentification : EntityIdentification {
    var id: Long = -1L

    constructor(id: Long) {
        this.id = id
    }

    override fun identificationDataType(): IdentificationDataType {
        return NumberIdentificationDataType()
    }

    override fun identification(): Serializable {
        return id
    }

    override fun entityType(): EntityType {
        return UserEntityType()
    }

    override fun toString(): String {
        return "NumberEntityIdentification(id=$id)"
    }

}

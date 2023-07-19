package fit.wenchao.http_file_server.utils

import fit.wenchao.http_file_server.constants.EntityType
import fit.wenchao.http_file_server.exception.BackendException
import fit.wenchao.http_file_server.exception.RespCode
import fit.wenchao.http_file_server.utils.TimeUnit.DAY
import fit.wenchao.http_file_server.utils.TimeUnit.HOUR
import fit.wenchao.http_file_server.utils.TimeUnit.MINUTE
import fit.wenchao.http_file_server.utils.TimeUnit.SECOND
import fit.wenchao.http_file_server.utils.TimeUnit.WEEK
import io.jsonwebtoken.*
import org.apache.commons.lang3.StringUtils
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import java.util.*

fun string2Long(aString: String): Long? {
    try {
        return aString.toLong()
    } catch (e: NumberFormatException) {
        return null;
    }
}


const val TOKEN_PAYLOAD_ID_ATTR = "id"
const val TOKEN_PAYLOAD_TYPE_ATTR = "type"
const val TOKEN_PAYLOAD_RANDOM_FACTOR = "random-factor"

@Component
class JwtUtils : InitializingBean {

    companion object {
        var tokenExpiration: Long = 0
        var tokenSignKey: String = ""
        var jwtSubject: String = ""


        fun getIdFromToken(token: String): String? {
            return getClaimFromJWT(token, TOKEN_PAYLOAD_ID_ATTR)
        }

        fun getEntityIdFromToken(claims: Claims): Long? {
            val userIdentity = claims.get(TOKEN_PAYLOAD_ID_ATTR, String::class.java)
            return userIdentity?.let { string2Long(it) }
        }

        fun genToken(id: String, entityType: EntityType): String {
            return genToken(id, entityType, tokenExpiration)
        }

        fun genToken(id: String, entityType: EntityType, expireDuration: Long): String {
            val claim = hashMapOf<String, Any>()
            claim[TOKEN_PAYLOAD_ID_ATTR] = id
            claim[TOKEN_PAYLOAD_TYPE_ATTR] = entityType.value
            claim[TOKEN_PAYLOAD_RANDOM_FACTOR] = UUID.randomUUID().toString()
            return createToken(claim, System.currentTimeMillis() + expireDuration)
        }

        private fun verifySignature(token: String): Jws<Claims> {
            return verifySignature(tokenSignKey, token)
        }

        private fun verifySignature(signkey: String, token: String): Jws<Claims> {
            return Jwts.parser().setSigningKey(signkey).parseClaimsJws(token)
        }

        fun verifyJWT(token: String): Jws<Claims> {
            return verifySignature(token)
        }

        fun verifyJWT(signKey: String, token: String): Jws<Claims> {
            return verifySignature(signKey, token)
        }

        private fun generateExpirationDate(): Date {
            return Date(System.currentTimeMillis() + tokenExpiration)
        }

        private fun createToken(claimMap: Map<String, Any>, expireAtDate: Date): String {
            return Jwts.builder()
                .setSubject(jwtSubject)
                .setClaims(claimMap)
                .setExpiration(expireAtDate)
                .signWith(SignatureAlgorithm.HS512, tokenSignKey)
                .compact()
        }

        fun <T> getClaimFromJWT(token: String, claimName: String): T? {
            if (StringUtils.isEmpty(token)) {
                return null
            }
            val claimsJws = verifySignature(token)
            val claims = claimsJws.body
            return claims[claimName] as? T
        }



        fun verifyJWTThrowBackendExceptions(tokenSignKey: String, token: String): Jws<Claims> {
            return try {
                verifyJWT(tokenSignKey, token)
            } catch (ex: ExpiredJwtException) {
                throw BackendException(null, RespCode.TOKEN_EXPIRED)
            } catch (ex: Exception) {
                throw BackendException(null, RespCode.TOKEN_INVALID)
            }
        }

        fun verifyJWTThrowBackendExceptions(token: String): Jws<Claims> {
            return verifyJWTThrowBackendExceptions(tokenSignKey, token)
        }

        fun createToken(claimMap: Map<String, Any>, expireAt: Long): String {
            val expireAtDate = Date(expireAt)
            return createToken(claimMap, expireAtDate)
        }

        fun createToken(claimMap: Map<String, Any>): String {
            val expireAtDate = generateExpirationDate()
            return createToken(claimMap, expireAtDate)
        }
    }

    @Autowired
    lateinit var jwtProperties: JWTProperties





    override fun afterPropertiesSet() {
        jwtSubject = jwtProperties.jwtSubject
        val expirationUnit: String = jwtProperties.expirationUnit
        val tokenExpirationValue: Long = jwtProperties.tokenExpiration
        when (expirationUnit) {
            SECOND -> tokenExpiration = tokenExpirationValue * 1000
            MINUTE -> tokenExpiration = tokenExpirationValue * 1000 * 60
            HOUR -> tokenExpiration = tokenExpirationValue * 1000 * 60 * 60
            DAY -> tokenExpiration = tokenExpirationValue * 1000 * 60 * 60 * 24
            WEEK -> tokenExpiration = tokenExpirationValue * 1000 * 60 * 60 * 24 * 7
            else -> tokenExpiration = tokenExpirationValue
        }

        tokenSignKey = jwtProperties.tokenSignKey
    }
}


 object TimeUnit {
    const val MILLISECOND = "millisecond"
    const val SECOND = "second"
    const val MINUTE = "minute"
    const val HOUR = "hour"
    const val DAY = "day"
    const val WEEK = "week"
}

@Component
@ConfigurationProperties(prefix = "jwt")
class JWTProperties {

    /**
     * token的过期时间, milli
     */
    var tokenExpiration: Long = 30

    var expirationUnit: String = MINUTE

    /**
     * 生成token所需的密钥
     */
    var tokenSignKey = "111111"

    /**
     * JWT的subject
     */
    var jwtSubject = "sfs"
}


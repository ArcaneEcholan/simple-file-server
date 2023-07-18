package fit.wenchao.http_file_server.config

import fit.wenchao.http_file_server.constants.API_PREFIX
import fit.wenchao.http_file_server.interceptor.ApiLogInterceptor
import fit.wenchao.http_file_server.interceptor.AuthcInterceptor
import fit.wenchao.http_file_server.service.TokenRealm
import fit.wenchao.http_file_server.service.UsernamePasswordRealm
import org.apache.shiro.web.mgt.DefaultWebSecurityManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.server.ErrorPage
import org.springframework.boot.web.server.ErrorPageRegistrar
import org.springframework.boot.web.server.ErrorPageRegistry
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter

class AppConfig {
}

const val PATH_AUTH_USER = "/user-authentication"


@Component
class StaticServerErrorPageConfig : ErrorPageRegistrar {
    override fun registerErrorPages(registry: ErrorPageRegistry) {
        val error404Page = ErrorPage(HttpStatus.NOT_FOUND, "/index.html")
        registry.addErrorPages(error404Page)
    }
}


@Configuration
class CORSConfiguration {
    @Bean
    fun corsConfigurer(): WebMvcConfigurer {
        return object : WebMvcConfigurerAdapter() {
            override fun addCorsMappings(registry: CorsRegistry) {
                registry.addMapping("/**")
                    .allowedOrigins("*")
                    .allowedMethods("GET", "POST", "DELETE", "PUT", "OPTIONS", "PATCH")
                    .allowCredentials(true).maxAge(3600)
            }
        }
    }
}


@Configuration
class InterceptorConfig: WebMvcConfigurer {

    @Autowired
    lateinit var applicationContext: ApplicationContext

    private fun <T> getBeanFromIOC(clazz: Class<T>): T {
        return applicationContext.getBean(clazz)
    }

    private val youSeeAllInterceptors: List<Class<out HandlerInterceptor>> = mutableListOf(
        ApiLogInterceptor::class.java,
        AuthcInterceptor::class.java)

    override fun addInterceptors(registry: InterceptorRegistry) {
        for (interceptorClass in youSeeAllInterceptors) {
            val interceptorRegistration = registry.addInterceptor(getBeanFromIOC(interceptorClass))
            interceptorRegistration.addPathPatterns("/**")

            if(interceptorClass == AuthcInterceptor::class.java) {
                interceptorRegistration.excludePathPatterns(API_PREFIX + PATH_AUTH_USER)
            }
        }
    }
}


@Configuration
class ShiroConfig {

    // @Bean
    // fun shiroFilterChainDefinition(): ShiroFilterChainDefinition? {
    //     val chainDefinition = DefaultShiroFilterChainDefinition()
    //     // don't apply any filters of shiro
    //     chainDefinition.addPathDefinition("/**", "anon")
    //     return chainDefinition
    // }
    //
    // @Bean(name = ["shiroFilter"])
    // fun shiroFilterFactoryBean(
    //     securityManager: DefaultSecurityManager?,
    //     chainDefinition: ShiroFilterChainDefinition,
    // ): ShiroFilterFactoryBean? {
    //     val shiroFilterFactoryBean = ShiroFilterFactoryBean()
    //     shiroFilterFactoryBean.securityManager = securityManager
    //     shiroFilterFactoryBean.filterChainDefinitionMap = chainDefinition.filterChainMap
    //     return shiroFilterFactoryBean
    // }
    //
    // @Bean
    // fun filterRegistrationBean(): FilterRegistrationBean<DelegatingFilterProxy>? {
    //     val filterRegistrationBean = FilterRegistrationBean<DelegatingFilterProxy>()
    //     filterRegistrationBean.filter = DelegatingFilterProxy("shiroFilter")
    //     filterRegistrationBean.order = Ordered.LOWEST_PRECEDENCE
    //     filterRegistrationBean.addUrlPatterns("/*")
    //     return filterRegistrationBean
    // }

    @Bean
    fun tokenRealm(): TokenRealm {
        return TokenRealm()
    }

    @Bean
    fun usernamePasswordRealm(): UsernamePasswordRealm {
        return UsernamePasswordRealm()
    }

    @Bean
    fun securityManager(tokenRealm: TokenRealm, usernamePasswordRealm: UsernamePasswordRealm): DefaultWebSecurityManager {
        val securityManager = DefaultWebSecurityManager()
        var realms = listOf(
            tokenRealm,
            usernamePasswordRealm
        )
        securityManager.realms = realms
        return securityManager
    }
}

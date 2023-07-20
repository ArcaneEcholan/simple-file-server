package fit.wenchao.http_file_server.utils

import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.stereotype.Component

@Component
class SpringContainer : ApplicationContextAware {

    companion object {

        lateinit var applicationContext: ApplicationContext

        fun <T> getBean(type: Class<T>): T {
            return applicationContext.getBean(type)
        }
    }

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        Companion.applicationContext = applicationContext
    }
}

package fit.wenchao.http_file_server.eventListener

import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component

@Component
class AListener : ApplicationListener<AEvent> {
    override fun onApplicationEvent(event: AEvent) {
        println("I know you: ${event.src}")
    }
}
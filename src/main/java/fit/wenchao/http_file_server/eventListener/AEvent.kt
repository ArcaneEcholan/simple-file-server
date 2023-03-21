package fit.wenchao.http_file_server.eventListener

import org.springframework.context.ApplicationEvent

class AEvent(var src: String) : ApplicationEvent(src) {


}
package fit.wenchao.http_file_server.constants;

enum class EntityType(var value: String) {
    WEB_USER("user"),
    APP("app"),
    UNKNOWN("unknown")
    ;

    companion object {
        fun fromValue(value: String?): EntityType {
            return when (value) {
                WEB_USER.value -> WEB_USER
                APP.value -> APP
                else -> UNKNOWN
            }
        }
    }

}
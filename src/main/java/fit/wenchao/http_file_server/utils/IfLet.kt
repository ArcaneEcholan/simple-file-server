package fit.wenchao.http_file_server.utils

class IfLet {
}

inline fun <T> T?.iflet(notNullBlock: (T) -> Unit): Else {
    var exeElse: Boolean
    if (this != null) {
        notNullBlock(this);
        exeElse = false
    } else {
        exeElse = true
    }
    return Else(exeElse)
}


class Else(var exeElse: Boolean) {

    inline fun elselet(closure: () -> Unit) {
        if (exeElse) {
            closure()
        }
    }

}

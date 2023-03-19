package fit.wenchao.http_file_server

import org.junit.jupiter.api.Test

class Test {

    @Test
    fun test() {
        var maxSizeValue: String? = null
//        maxSizeValue = maxSizeValue?.let {
//            it
//        }

        maxSizeValue?.let {
            print(it)
            return
        }
        println("hello")

        maxSizeValue.iflet {
            println(it)
        }.elselet {
            print("null")
        }

//        println(maxSizeValue)
    }

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

class Test1 {

    fun lamb(block: (you: String, me: String) -> String) {

    }

    fun test2(): (Int, Int) -> Int {
        lamb { you, me ->
            val sum: (Int, Int) -> Int = { x, y -> x + y }

            return@lamb "";
        }
        return { x, y -> x + y };
    }
}


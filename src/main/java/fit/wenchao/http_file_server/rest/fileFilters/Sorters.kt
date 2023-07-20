package fit.wenchao.http_file_server.rest.fileFilters

class Sorters {
}

abstract class BaseComparator<T> : Comparator<T> {
    fun isNumber(arg: T): Boolean {
        if (arg is Long || arg is Int || arg is Short) {
            return true
        }
        return false
    }
}

/**
 * This sorter only sorts Longs or Strings, both param should be the same type
 *
 * If pass other types of object, always return 0
 *
 * AscComparator is the same
 */
class DescComparator<T> : BaseComparator<T>() {

    override fun compare(p0: T, p1: T): Int {
        if (isNumber(p0) && isNumber(p1)) {
            if (p0 is Long && p1 is Long) {
                return -p0.compareTo(p1)
            }
            return 0
        }

        if (p0 is String && p1 is String) {
            return -p0.compareTo(p1)
        }

        return 0
    }

}

class AscComparator<T> : BaseComparator<T>() {

    override fun compare(p0: T, p1: T): Int {
        if (isNumber(p0) && isNumber(p1)) {
            if (p0 is Long && p1 is Long) {
                return p0.compareTo(p1)
            }
            return 0
        }

        if (p0 is String && p1 is String) {
            return p0.compareTo(p1)
        }

        return 0
    }
}

/**
 * This sorter treats all kinds of objects as the same. No one bigger, no one smaller
 */
class DoNothingComparator<T> : Comparator<T> {
    override fun compare(p0: T, p1: T): Int {
        return 0
    }
}

fun getSorter(sortType: Any): Comparator<Any> {
    if (sortType == DESC) {
        return DescComparator()
    }
    if (sortType == ASC) {
        return AscComparator()
    }
    return DoNothingComparator()
}


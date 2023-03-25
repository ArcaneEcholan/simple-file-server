package fit.wenchao.http_file_server.rest

import fit.wenchao.http_file_server.model.vo.FileInfo

class FilterKeys {

}

const val ShowHiddenFile = "ShowHiddenFile"
const val SortFilename = "SortFilename"
const val SortLastModifiedTime = "SortLastModifiedTime"
const val SortFilesize = "SortFilesize"
const val SearchFilename = "SearchFilename"


//   {
//           "key": "show-hidden-file",
//           "value": "true"
//      },
//      {
//            "key": "sort-filename",
//           "value": "desc"
//      },
//         {
//            "key": "sort-lastModifiedTime",
//           "value": "none"
//      },
//         {
//            "key": "sort-filesize",
//           "value": "asc "
//      },
//         {
//            "key": "search-filename",
//           "value": "D"
//      }


interface Filter {

    fun supportFilterCondition(filterConditionKey: String): Boolean
    fun processFileList(fileInfoList: MutableList<FileInfo>, vararg args: String): MutableList<FileInfo>
}

/**
 * filter: {
 *      "key": "ShowHiddenFile"
 *      "value" : "TRUE" // "TRUE" or "FALSE"
 * }
 */
class HiddenFileFilter : Filter {
    override fun supportFilterCondition(filterConditionKey: String): Boolean {
        return filterConditionKey == ShowHiddenFile
    }

    override fun processFileList(fileInfoList: MutableList<FileInfo>, vararg args: String): MutableList<FileInfo> {
        if (args.size != 1) {
            return fileInfoList
        }
        var hide = args[0]
        if (hide == TRUE) {
            return fileInfoList.filter { fileInfo ->
                !fileInfo.name.startsWith(".")
            }.toMutableList()
        } else {
            return fileInfoList
        }
    }
}

const val DESC: String = "DESC"
const val ASC: String = "ASC"

const val TRUE: String = "TRUE"
const val FALSE: String = "FALSE"


abstract class BaseComparator<T> : Comparator<T> {
    fun isNumber(arg: T): Boolean {
        if (arg is Long || arg is Int || arg is Short) {
            return true
        }
        return false
    }
}

class DescComparator<T> : BaseComparator<T>() {

    override fun compare(p0: T, p1: T): Int {
        if (isNumber(p0) && isNumber(p1)) {
            if (p0 is Long && p1 is Long) {
                return (-(p0 - p1)).toInt()
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
                return ((p0 - p1)).toInt()
            }
            return 0
        }

        if (p0 is String && p1 is String) {
            return p0.compareTo(p1)
        }

        return 0
    }
}

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


/**
 * filter: {
 *      "key": "SortFilename"
 *      "value" : "DESC" // "DESC" or "ASC"
 * }
 */
class SortNameFilter : Filter {

    override fun supportFilterCondition(filterConditionKey: String): Boolean {
        return filterConditionKey == SortFilename
    }

    override fun processFileList(fileInfoList: MutableList<FileInfo>, vararg args: String): MutableList<FileInfo> {
        if (args.size != 1) {
            return fileInfoList
        }
        val sorter = getSorter(args[0])
        return fileInfoList.sortedWith { a, b ->
            sorter.compare(a.name, b.name)
        }.toMutableList()
    }
}

/**
 * filter: {
 *      "key": "SortFilesize"
 *      "value" : "DESC" // "DESC" or "ASC"
 * }
 */
class SortSizeFilter : Filter {

    override fun supportFilterCondition(filterConditionKey: String): Boolean {
        return filterConditionKey == SortFilesize
    }

    override fun processFileList(fileInfoList: MutableList<FileInfo>, vararg args: String): MutableList<FileInfo> {
        if (args.size != 1) {
            return fileInfoList
        }
        val sorter = getSorter(args[0])
        return fileInfoList.sortedWith { a, b ->
            sorter.compare(a.length, b.length)
        }.toMutableList()
    }
}

/**
 * filter: {
 *      "key": "SortLastModifiedTime"
 *      "value" : "DESC" // "DESC" or "ASC"
 * }
 */
class SortLastModifiedTimeFilter : Filter {

    override fun supportFilterCondition(filterConditionKey: String): Boolean {
        return filterConditionKey == SortLastModifiedTime
    }

    override fun processFileList(fileInfoList: MutableList<FileInfo>, vararg args: String): MutableList<FileInfo> {
        if (args.size != 1) {
            return fileInfoList
        }
        val sorter = getSorter(args[0])
        return fileInfoList.sortedWith { a, b ->
            sorter.compare(a.lastModifiedTime, b.lastModifiedTime)
        }.toMutableList()
    }
}

/**
 * filter: {
 *      "key": "SearchFilename"
 *      "value" : "you partial file name" // left most prefix, eg: mat -> material.docs
 * }
 */
class SearchFilenameFilter : Filter {

    override fun supportFilterCondition(filterConditionKey: String): Boolean {
        return filterConditionKey == SearchFilename
    }

    override fun processFileList(fileInfoList: MutableList<FileInfo>, vararg args: String): MutableList<FileInfo> {
        if (args.size != 1) {
            return fileInfoList
        }

        val partialFilename = args[0]

        return fileInfoList.filter { fileInfo ->
            fileInfo.name.startsWith(partialFilename)
        }.toMutableList()
    }
}

class CompositeFilter : Filter {

    /**
     * this method will not be invoked
     */
    override fun supportFilterCondition(filterConditionKey: String): Boolean {
        return true
    }


    override fun processFileList(fileInfoList: MutableList<FileInfo>, vararg args: String): MutableList<FileInfo> {
        if (args.size != 1) {
            return fileInfoList
        }

        val partialFilename = args[0]

        return fileInfoList.filter { fileInfo ->
            fileInfo.name.startsWith(partialFilename)
        }.toMutableList()
    }
}


fun getFilter(filters: MutableList<Filter>, condition: FilterCondition): Filter? {
    val key = condition.key
    val filterList = filters.filter { it.supportFilterCondition(key) }
    if (filterList.isEmpty()) {
        return null
    }
    return filterList.first()
}


class FilterCondition {

    lateinit var type: String
    lateinit var key: String
    lateinit var value: String

}
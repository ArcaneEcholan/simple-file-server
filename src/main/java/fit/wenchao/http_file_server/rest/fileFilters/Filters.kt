package fit.wenchao.http_file_server.rest.fileFilters

import fit.wenchao.http_file_server.constants.FILE
import fit.wenchao.http_file_server.constants.FOLDER
import fit.wenchao.http_file_server.model.vo.FileInfo
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component

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
@Component
class ShowHiddenFileFilter : Filter {

    override fun supportFilterCondition(filterConditionKey: String): Boolean {
        return filterConditionKey == ShowHiddenFiles
    }

    override fun processFileList(fileInfoList: MutableList<FileInfo>, vararg args: String): MutableList<FileInfo> {
        if (args.size != 1) {
            return fileInfoList
        }
        var showHiddenFiles = args[0]
        if (showHiddenFiles == FALSE) {
            return fileInfoList.filter { fileInfo -> notHiddenFile(fileInfo.name) }.toMutableList()
        }

        return fileInfoList
    }

    fun isHiddenFile(filename: String): Boolean {
        return filename.startsWith(".")
    }

    fun notHiddenFile(filename: String): Boolean {
        return !isHiddenFile(filename)
    }
}


/**
 * filter: {
 *      "key": "SortFilename"
 *      "value" : "DESC" // "DESC" or "ASC"
 * }
 */
@Component
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
            sorter.compare(a.name.lowercase(), b.name.lowercase())
        }.toMutableList()
    }
}

/**
 * filter: {
 *      "key": "SortFilesize"
 *      "value" : "DESC" // "DESC" or "ASC"
 * }
 */
@Component
class SortSizeFilter : Filter {

    override fun supportFilterCondition(filterConditionKey: String): Boolean {
        return filterConditionKey == SortFilesize
    }

    override fun processFileList(fileInfoList: MutableList<FileInfo>, vararg args: String): MutableList<FileInfo> {
        if (args.size != 1) {
            return fileInfoList
        }

        var folderList: MutableList<FileInfo> = getFolderList(fileInfoList)
        var regularFileList: MutableList<FileInfo> = getRegularFileList(fileInfoList)

        // file size sorting should exclude folders
        val sorter = getSorter(args[0])
        regularFileList = regularFileList.sortedWith { a, b ->
            sorter.compare(a.length, b.length)
        }.toMutableList()

        // add folders to back
        regularFileList.addAll(folderList)
        return regularFileList
    }

    private fun getRegularFileList(fileInfoList: MutableList<FileInfo>): MutableList<FileInfo> {
        return fileInfoList.filter { it.fileType == FILE }.toMutableList()
    }

    private fun getFolderList(fileInfoList: MutableList<FileInfo>): MutableList<FileInfo> {
        return fileInfoList.filter { it.fileType == FOLDER }.toMutableList()
    }
}

/**
 * filter: {
 *      "key": "SortLastModifiedTime"
 *      "value" : "DESC" // "DESC" or "ASC"
 * }
 */
@Component
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
@Component
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
            fileInfo.name.lowercase().startsWith(partialFilename.lowercase())
        }.toMutableList()
    }
}


fun getFilters(appCtx: ApplicationContext): MutableList<Filter> {
    val beansOfType = appCtx.getBeansOfType(Filter::class.java)
    return beansOfType.values.toMutableList()
}

fun getFilter(filters: MutableList<Filter>, condition: QueryFilesOption): Filter? {
    val key = condition.key
    val filterList = filters.filter { it.supportFilterCondition(key) }
    if (filterList.isEmpty()) {
        return null
    }
    return filterList.first()
}

fun getFilter(appCtx: ApplicationContext, filterOption: QueryFilesOption): Filter? {
    val key = filterOption.key
    val filterList = getFilters(appCtx).filter { it.supportFilterCondition(key) }
    if (filterList.isEmpty()) {
        return null
    }
    return filterList.first()
}

fun sortFileFilterOptions(filterOpts: MutableList<QueryFilesOption>): MutableList<QueryFilesOption> {
    val orderred = listOf<String>(
        ShowHiddenFiles,
        SearchFilename,
        SortFilename,
        SortFilesize,
        SortLastModifiedTime,
    )

    var comp: Comparator<QueryFilesOption> = Comparator<QueryFilesOption> { obj1, obj2 ->
        val indexOfObj1 = orderred.indexOf(obj1.key)
        val indexOfObj2 = orderred.indexOf(obj2.key)
        indexOfObj1 - indexOfObj2
    }
    return filterOpts.sortedWith(comp).toMutableList()

}

/**
 * One option is related to a filter which will be applied to the file list.
 *
 * For example, if we want to sort file list by filename, then need to apply SortFilenameFilter to file list.
 *
 * You can apply multiple filters to file list at one query.
 *
 * There is a list of preset Filters in SpringIOC, we retrieve a filter by key property of QueryFilesOption.
 */
class QueryFilesOption {

    /**
     * the option key
     */
    lateinit var key: String

    /**
     * the option value
     */
    lateinit var value: String

    /**
     * type of value, typically SINGLE, LIST is not used for now
     */
    lateinit var type: String

}


fun resolveFileListFilterOptions(optsMap: MutableMap<String, String>): MutableList<QueryFilesOption> {

    var result = mutableListOf<QueryFilesOption>()
    optsMap.forEach { filterConditionVO ->

        val key: String = filterConditionVO.key
        val value: String = filterConditionVO.value

        val filterCondition = QueryFilesOption()
        filterCondition.key = key
        filterCondition.value = value
        result.add(filterCondition)

    }

    // we sort them in a preset way
    result = sortFileFilterOptions(result)

    return result
}
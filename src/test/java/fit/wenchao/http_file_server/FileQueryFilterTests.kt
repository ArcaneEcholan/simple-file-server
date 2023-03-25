package fit.wenchao.http_file_server

import fit.wenchao.http_file_server.model.vo.FileInfo
import fit.wenchao.http_file_server.rest.*
import org.junit.jupiter.api.Test

class FileQueryFilterTests {

    @Test
    fun overall() {
        // construct filter conditionVos
        var conditionsVO = FileListFilterConditionsVO()
        var conditions: MutableList<FileListFilterConditionVO>

        conditions = mutableListOf()
        conditions.add(FileListFilterConditionVO(null, SortFilesize, DESC))
        conditions.add(FileListFilterConditionVO(null, SearchFilename, ".abc"))
        conditions.add(FileListFilterConditionVO(null, ShowHiddenFile, FALSE))
        conditionsVO.filterConditions = conditions

        var filters = mutableListOf<Filter>()
        filters.add(SortNameFilter())
        filters.add(SortSizeFilter())
        filters.add(SortLastModifiedTimeFilter())
        filters.add(SearchFilenameFilter())
        filters.add(HiddenFileFilter())

        var fileInfos = mutableListOf<FileInfo>()

        fileInfos.add(FileInfo(name = "abcdefg.txt", length = 2341234))
        fileInfos.add(FileInfo(name = ".abcdefg.txt", length = 2341234))
        fileInfos.add(FileInfo(name = ".abcxxdd", length = 6376545234))
        fileInfos.add(FileInfo(name = ".m2", length = 563443))
        fileInfos.add(FileInfo(name = "readme.md", length = 123452))

        var filterConditions = conditionsVO.resolveFileListFilterCondition()

        filterConditions = sortFileFilterConditions(filterConditions)

        filterConditions.forEach { filterCondition ->
            val filter: Filter? = getFilter(filters, filterCondition)
            filter?.let {
                fileInfos = it.processFileList(fileInfos, filterCondition.value)
            }
        }

        assert(fileInfos.size == 1)
        assert(fileInfos.first() == FileInfo(name = ".abcdefg.txt", length = 2341234))

    }
}

fun sortFileFilterConditions(filterConditions: MutableList<FilterCondition>): MutableList<FilterCondition> {
    val orderred = listOf<String>(
        ShowHiddenFile,
        SearchFilename,
        SortFilename,
        SortFilesize,
        SortLastModifiedTime,
    )

    var comp: Comparator<FilterCondition> = Comparator<FilterCondition> { obj1, obj2 ->
        val indexOfObj1 = orderred.indexOf(obj1.key)
        val indexOfObj2 = orderred.indexOf(obj2.key)
        indexOfObj1 - indexOfObj2
    }
    return filterConditions.sortedWith(comp).toMutableList()

}
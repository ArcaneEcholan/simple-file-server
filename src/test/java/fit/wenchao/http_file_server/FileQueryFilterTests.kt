package fit.wenchao.http_file_server

import fit.wenchao.http_file_server.model.vo.FileInfo
import fit.wenchao.http_file_server.rest.QueryFilesOptVO
import fit.wenchao.http_file_server.rest.FileListFilterOptsVO
import fit.wenchao.http_file_server.rest.fileFilters.*
import org.junit.jupiter.api.Test

class FileQueryFilterTests {

    @Test
    fun overall() {
        // prepare filter options, they will be sent by frontend in the real situation
        var optsVO = FileListFilterOptsVO()
        var opts: MutableList<QueryFilesOptVO>
        opts = mutableListOf()
        opts.add(QueryFilesOptVO(null, SortFilesize, DESC))
        opts.add(QueryFilesOptVO(null, SearchFilename, ".abc"))
        opts.add(QueryFilesOptVO(null, ShowHiddenFiles, FALSE))
        optsVO.queryFilesOptions = opts

        // prepare filters, they will be at springIOC in the real situation
        var filters = mutableListOf<Filter>()
        filters.add(SortNameFilter())
        filters.add(SortSizeFilter())
        filters.add(SortLastModifiedTimeFilter())
        filters.add(SearchFilenameFilter())
        filters.add(ShowHiddenFileFilter())

        // prepare raw file list, they will be retrieved from disk in the real situation
        var fileInfos = mutableListOf<FileInfo>()
        fileInfos.add(FileInfo(name = "abcdefg.txt", length = 2341234))
        fileInfos.add(FileInfo(name = ".abcdefg.txt", length = 2341234))
        fileInfos.add(FileInfo(name = ".abcxxdd", length = 6376545234))
        fileInfos.add(FileInfo(name = ".m2", length = 563443))
        fileInfos.add(FileInfo(name = "readme.md", length = 123452))
        ///////////////////////////////  prepare  ///////////////////////////////

        // we get the filter options first
        var filterOptList = optsVO.resolveFileListFilterOptions()

        // we apply them to file list
        filterOptList.forEach { filterCondition ->
            val filter: Filter? = getFilter(filters, filterCondition)
            filter?.let {
                fileInfos = it.processFileList(fileInfos, filterCondition.value)
            }
        }

        assert(fileInfos.size == 1)
        assert(fileInfos.first() == FileInfo(name = ".abcdefg.txt", length = 2341234))

    }
}


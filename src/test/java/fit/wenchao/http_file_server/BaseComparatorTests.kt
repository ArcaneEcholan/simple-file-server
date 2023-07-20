package fit.wenchao.http_file_server

import fit.wenchao.http_file_server.rest.fileFilters.ASC
import fit.wenchao.http_file_server.rest.fileFilters.getSorter
import org.junit.jupiter.api.Test

class BaseComparatorTests {

    @Test
    fun compare() {
        val sorter = getSorter(ASC)
        var compare = sorter.compare("hello", "you")
        assert(compare < 0)

        compare = sorter.compare("you", "me")
        assert(compare > 0)

        compare = sorter.compare("you", "you")
        assert(compare == 0)

        compare = sorter.compare("you", 1L)
        assert(compare == 0)


        compare = sorter.compare(1, 2)
        assert(compare == 0)

        compare = sorter.compare(1L, 2L)
        assert(compare < 0)

        compare = sorter.compare(2L, 2L)
        assert(compare == 0)


    }
}
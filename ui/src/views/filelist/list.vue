<template>
    <div style="margin: 50px 40px">
        <!-- top tool bar -->
        <div class="flex toolbar" style="padding: 20px 0px">
            <!-- upload btn -->
            <div>
                <input
                    id="upload_file"
                    type="file"
                    style="display: none"
                    name="myfile"
                    @change="do_upload_file"
                />
                <el-button type="primary" @click="open_upload_file()">
                    upload
                </el-button>
            </div>
            <!-- if show hidden files switcher-->
            <div class="flex flex-center mgl20">
                <div>
                    <span class="pdr20">show hidden files</span>
                    <el-switch
                        v-model="showHiddenFiles"
                        @change="showHiddenFilesBtnToggled"
                    ></el-switch>
                </div>
            </div>
            <!-- search files -->
            <div>
                <el-input
                    placeholder="search files"
                    v-model="searchFilesKey"
                    @input="typeSearchKey"
                ></el-input>
            </div>
        </div>
        <div>
            <div class="flex">
                <!-- back btn -->
                <div class="mgr20">
                    <el-button @click="goback()">back</el-button>
                </div>
                <!-- page path -->
                <div>
                    <span style="font-size: 30px">{{ path }}</span>
                </div>
            </div>

            <!-- sort btns -->
            <div>
                <div id="sortBtnGroup">
                    <button
                        @click="chooseSortBtn(btnModel.sortKey)"
                        v-for="btnModel in sortBtns"
                        class="no-radius"
                        :key="btnModel.name"
                    >
                        {{ btnModel.name }}
                        <font-awesome-icon
                            style="font-size: 12px"
                            :icon="`fa-solid ${btnModel.arrowStatus}`"
                        />
                    </button>
                </div>
            </div>

            <el-table :data="filelist" style="width: 100%">
                <el-table-column
                    prop="name"
                    label="name"
                    width="360"
                    :sort-method="fileNameSortMethod"
                >
                    <template #default="scope">
                        <div
                            v-if="isFile(scope.row)"
                            @click="download(scope.row.name)"
                        >
                            <span class="underline folder-name">
                                {{ scope.row.name }}
                            </span>
                        </div>
                        <div v-else>
                            <span
                                @click="intoDir(scope.row.name)"
                                class="underline file-name"
                            >
                                {{ scope.row.name + '/' }}
                            </span>
                        </div>
                    </template>
                </el-table-column>
                <el-table-column prop="length" label="文件大小" width="360">
                    <template #default="scope">
                        <readable-display
                            type="file-size"
                            :value="scope.row.length"
                        ></readable-display>
                    </template>
                </el-table-column>
                <el-table-column
                    prop="fileType"
                    label="fileType"
                    width="360"
                ></el-table-column>
                <el-table-column
                    prop="lastModifiedTime"
                    label="lastModifiedTime"
                    width="360"
                ></el-table-column>
            </el-table>
        </div>
    </div>
</template>
<script lang="ts">
import readableDisplay from '@/views/common/readable-display.vue'
import * as config_api from '@/api/config.js'
import {Message, Notification} from 'element-ui'
import {PageLocation} from '@/ts/dynamicLocation'
import * as FILE_CONSTS from '@/ts/consts/fileConstants'
import {Component, Vue, Watch} from 'vue-property-decorator'
import Client from "@/request/client";
import {get_token} from "@/ts/auth";

@Component({
    components: {
        readableDisplay,
    },
})
export default class FileListView extends Vue {
    sortBtns = [
        {
            name: 'name',
            arrowStatus: FILE_CONSTS.SORT_ARROW_NONE,
            sortKey: FILE_CONSTS.SortFilename,
        },
        {
            name: 'size',
            arrowStatus: FILE_CONSTS.SORT_ARROW_NONE,
            sortKey: FILE_CONSTS.SortFilesize,
        },
        {
            name: 'time',
            arrowStatus: FILE_CONSTS.SORT_ARROW_NONE,
            sortKey: FILE_CONSTS.SortLastModifiedTime,
        },
    ]
    /**
     * user input for file searching
     */
    searchFilesKey = ''
    /**
     * whether show hidden files, default false
     */
    showHiddenFiles =
        this.$route.query.showHiddenFiles == null
            ? false
            : this.$route.query.showHiddenFiles
    /**
     * relative path to root( root is configured on the backend, it's transparent to frontend)
     */
    path: string =
        this.$route.query.path == null
            ? '/'
            : (this.$route.query.path as string)
    /**
     * file list
     */
    filelist = []

    beforeRouteUpdate(to, from, next) {
        console.log(from.query)
        console.log(to.query)
        next()
    }

    @Watch('$route')
    onRouteChanged(new_route) {
        this.populatePageWithQuerys(new_route)
        this.fetch_file_list()
    }

    created() {
        this.populatePageWithQuerys(this.$route)
        this.fetch_file_list()
    }

    populatePageWithQuerys(route) {
        let pageQuery = route.query

        // populate path
        this.path = pageQuery.path

        // populate sort buttons group with query params
        this.sortBtns.forEach((item) => {
            let sortKey = item.sortKey
            let sortValue = pageQuery[sortKey]
            // assign arrowStatus
            if (sortValue) {
                let upOrDown = FILE_CONSTS.MAPPING_SORTMETHOD_ARROW[sortValue]
                if (upOrDown != null) {
                    item.arrowStatus = upOrDown
                }
            }
        })
    }

    /**
     * Recollect all query conditions, including path, sort conditions, then build and return HTTP GET query params
     *
     * One example for return value is: "?path=/path/to/file&name=hello&age=12"
     *
     * Invoked when one query condition changed
     */
    concatQuerys() {
        // path=xxx
        var pathQueryParam = this.buildPathQueryParam()
        // Sortxxx=xxx&Sortxxx=xxx
        var sortQuerys = this.concatSortQuerys()
        // concat
        let querys = `?${pathQueryParam}&${sortQuerys}`
        return querys
    }

    /**
     * Invoked when click one of sort-buttons
     *
     * Give an up or down arrow to the active btn which is clicked. If the button clicked is already active, change the direction of the arrow. Then collect all filter options in the page and query new file list.
     *
     * @param {*} sortKey sort option key. eg: SortFilename
     */
    chooseSortBtn(sortKey) {
        // find the clicked btn by sort key
        let targetBtn = this.sortBtns.find((item) => {
            return item.sortKey == sortKey
        })

        // no result, return
        if (!targetBtn) {
            return
        }

        // remove icon from buttons whose sort key != sortKey
        this.sortBtns
            .filter((item) => {
                return item.sortKey != sortKey
            })
            .forEach((item) => (item.arrowStatus = FILE_CONSTS.SORT_ARROW_NONE))

        // change icon direction for target sort button
        switch (targetBtn.arrowStatus) {
            case FILE_CONSTS.SORT_ARROW_NONE:
                targetBtn.arrowStatus = FILE_CONSTS.SORT_ARROW_UP
                break
            case FILE_CONSTS.SORT_ARROW_UP:
                targetBtn.arrowStatus = FILE_CONSTS.SORT_ARROW_DOWN
                break
            case FILE_CONSTS.SORT_ARROW_DOWN:
                targetBtn.arrowStatus = FILE_CONSTS.SORT_ARROW_UP
                break
            default:
                targetBtn.arrowStatus = FILE_CONSTS.SORT_ARROW_NONE
        }

        let querys = this.concatQuerys()
        this.$router.push(`/filelist${querys}`)
        // // refetch filelist
        // this.fetch_file_list();
    }

    isFile(file) {
        return file.fileType === FILE_CONSTS.FILE
    }

    dertermineIfShowHiddenFiles() {
        return this.showHiddenFiles ? FILE_CONSTS.TRUE : FILE_CONSTS.FALSE
    }

    /**
     * querys are joined by "&", eg: name=wc&age=12&address=china
     *
     * if no params is available, return empty string
     */
    concatSortQuerys() {
        let sortQuerys = ''

        // traverse each sort btn, collect sort options
        this.sortBtns.forEach((item) => {
            // only concat sort options which is not blank
            if (item.arrowStatus == FILE_CONSTS.SORT_ARROW_NONE) {
                return
            }
            console.log(item.arrowStatus)
            console.log(FILE_CONSTS.MAPPING_ARROW_SORTMETHOD)
            // map up and down to DESC and ASC
            let sortMethod =
                FILE_CONSTS.MAPPING_ARROW_SORTMETHOD[item.arrowStatus]
            // concat one query entry
            sortQuerys += `${item.sortKey}=${sortMethod}&`
        })

        // concat if hide hidden files filter option
        var ifShowHiddenFile = this.dertermineIfShowHiddenFiles()
        sortQuerys += `${FILE_CONSTS.ShowHiddenFile}=${ifShowHiddenFile}&`

        // concat file name seach key if seach input is not empty
        let seachKey = this.searchFilesKey

        if (seachKey != null && seachKey != '') {
            sortQuerys += `${FILE_CONSTS.SearchFilename}=${seachKey}&`
        }

        // remove the last "&"
        if (sortQuerys != '') {
            sortQuerys = sortQuerys.substring(0, sortQuerys.length - 1)
        }

        return sortQuerys
    }

    /**
     * Concat path param (this.path + filename) and encode it, then return path=${finalPath}
     *
     * A return value for example: path=encoded(/path/to/somewhere/filename)
     *
     * @param {*} filename we need to concat this filename to vue.path variable
     */
    encodeFilePathQueryCondition(filename) {
        var filepath = this.concat_path(this.path, filename)
        const encodedMessage = encodeURIComponent(filepath)
        var query = `path=${encodedMessage}`
        return query
    }

    /**
     * Concat path param (this.path + filename) and encode it, then return path=${finalPath}
     *
     * A return value for example: path=encoded(/path/to/somewhere/filename)
     *
     * @param {*} filename we need to concat this filename to vue.path variable
     */
    buildPathQueryParam() {
        var filepath = this.path
        const encodedMessage = encodeURIComponent(filepath)
        var query = `path=${encodedMessage}`
        return query
    }

    /**
     * starts with ?, eg: "?path=/path/to/file&name=abc&SortFilename=DESC"
     * @param {string} filename
     */
    concatQueryFileQuerys(filename) {
        let sortQuerys = this.concatSortQuerys()
        var pathParam = this.encodeFilePathQueryCondition(filename)
        var querys = `?${pathParam}&${sortQuerys}`
        return querys
    }

    intoDir(filename) {
        var querys = this.concatQueryFileQuerys(filename)

        var location = `/filelist${querys}`

        this.$router.push(location)
    }

    /**
     * When letters in seach input changed, this method will be invoked.
     *
     * This method collects all filter options of file querying in this page(including search files key), then fetch new file list
     */
    typeSearchKey(newInput) {
        let querys = this.concatQuerys()

        this.$router.push(`/filelist${querys}`)
    }

    /**
     * Collect page query filter options and fetch new filelist
     */
    showHiddenFilesBtnToggled() {
        let query = this.concatQuerys()
        this.$router.push(`/filelist${query}`)
    }

    fileNameSortMethod(fileA, fileB) {
        var nameA = fileA.name
        var nameB = fileB.name
        if (nameA != null && nameB != null) {
            var lowerNameA = nameA.toLowerCase()
            var lowerNameB = nameB.toLowerCase()
            return lowerNameA.localeCompare(lowerNameB)
        }
        return 0
    }

    /**
     * fetch all files in "this.path" dir
     */
    fetch_file_list() {
        // '/' is the default value of this.path
        if (this.path == null) {
            this.path = '/'
        }
        const queryString = `path=${this.path}&${this.concatSortQuerys()}`

        Client.getFileList(queryString, get_token())
            .then((resp) => {
                var filelist = resp.data
                this.filelist = filelist
            })
    }

    /**
     * go to parent path, equals to "cd ../"
     */
    goback() {
        // already root, do nothing
        if (this.path === '/') {
            return
        }

        const lastSplash = this.path.lastIndexOf('/')
        if (lastSplash === -1) {
            throw new Error('path error')
        }

        let parent_path = this.path.substring(0, lastSplash)

        if (parent_path === '') {
            parent_path = '/'
        }

        console.log(parent_path)

        var queryOptions = this.concatSortQuerys()

        const queryString = `path=${parent_path}&${queryOptions}`

        this.$router.push({
            path: `/filelist?${queryString}`,
        })
    }

    do_upload_file() {
        const upload_elem = document.getElementById(
            'upload_file',
        ) as HTMLInputElement

        const files = upload_elem.files

        if (files == null) {
            throw Error('files is null')
        }

        const file = files[0]

        // check file size
        // fetch file size limit first
        config_api
            .getConfigValue('max-upload-size', get_token())
            .then((resp) => {
                const limit_size = resp.data
                const filesize = file.size
                // file size exceeds the limit
                if (filesize > limit_size * 1024 * 1024) {
                    upload_elem.value = ''
                    Message.warning('Exceed upload limit: ' + limit_size + 'M')
                    return
                }

                // do upload
                const form = new FormData()
                form.append('file-body', file)
                form.append(
                    'file-info',
                    new Blob(
                        [
                            JSON.stringify({
                                path: this.path,
                            }),
                        ],
                        {
                            type: 'application/json',
                        },
                    ),
                )

                Client
                    .upload(form, get_token())
                    .then((resp) => {
                        Message.success('upload successfully')
                        let querys = this.concatQuerys()
                        this.$router.push(`/filelist${querys}`)
                    })
                    .catch((resp) => {
                        if (resp.code == null) {
                            Message.error("App Error: " + resp.message)
                        }
                    })

                upload_elem.value = ''
            })
            .catch((resp) => {
                if (resp.code === 'CONFIG_FILE_NOT_FOUND') {
                    Notification.error({
                        title: 'Config Error',
                        message: 'Config file not found',
                    })
                }
            })
    }

    // open upload file dialog
    open_upload_file() {
        document.getElementById('upload_file')!.click()
    }

    download(name) {

        var pathParam = this.encodeFilePathQueryCondition(name)

        var location = `${new PageLocation().baseURL}/file?entity-token=${get_token()}&${pathParam}`

        console.log('download location: ' + location)
        window.location.href = location
    }

    concat_path(src, part) {
        if (src.charAt(src.length - 1) === '/') {
            return src + part
        } else {
            return src + '/' + part
        }
    }
}
</script>
<style lang="scss">
@import '~@/styles/common-style.scss';

.underline {
    &:hover {
        text-decoration: underline;
    }
}

.toolbar {
    border-bottom: 1px solid $google-gray-200;
    margin-bottom: 20px;
}

.file-name {
    font-size: 20px;
    color: $google-blue;
    cursor: pointer;
}

.folder-name {
    font-size: 20px;
    color: $google-gray-400;
    cursor: pointer;
}

#sortBtnGroup {
    .no-radius {
        border-radius: 0;
    }

    button.selected {
        background-color: $google-red;
        color: white;
    }
}
</style>

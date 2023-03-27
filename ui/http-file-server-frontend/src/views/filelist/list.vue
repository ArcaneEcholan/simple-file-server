<!-- eslint-disable vue/require-v-for-key -->
<template lang="">
    <div style="margin: 50px 40px">
        <el-button @click="testvar = 'you'"></el-button>
        {{ testvar }}
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
                >
                    <template slot="append">
                        <el-button icon="el-icon-search"></el-button>
                    </template>
                </el-input>
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
                <!-- <button>
                    time
                    <font-awesome-icon
                        style="font-size: 12px"
                        icon="fa-solid fa-arrow-down"
                    />
                </button> -->

                <div id="sortBtnGroup">
                    <button
                        @click="chooseSortBtn(btnModel.sortKey)"
                        v-for="btnModel in sortBtns"
                        class="no-radius"
                    >
                        {{ btnModel.name }}
                        <font-awesome-icon
                            style="font-size: 12px"
                            :icon="`fa-solid fa-arrow-${btnModel.arrowStatus}`"
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
                    <template slot-scope="scope">
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
                    <template slot-scope="scope">
                        <readable-display
                            type="file-size"
                            :value="scope.row.length"
                        />
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
<script>
import readableDisplay from '@/views/common/readable-display.vue';
import * as file_api from '@/api/file.js';
import * as config_api from '@/api/config.js';
import { Message } from 'element-ui';
import { PageLocation } from '@/utils/dynamicLocation';
import { FILE, FOLDER } from '@/file/consts';
import * as FILE_CONSTS from '@/file/consts';
import { QueryFileOption, QueryFileOptions } from '@/file/file';

export default {
    components: {
        readableDisplay,
    },
    beforeRouteUpdate(to, from, next) {
        debugger;
        console.log(from.query);
        console.log(to.query);
        next();
    },
    data() {
        return {
            testvar: 'hello',
            sortBtns: [
                {
                    name: 'name',
                    arrowStatus: '',
                    sortKey: FILE_CONSTS.SortFilename,
                },
                {
                    name: 'size',
                    arrowStatus: '',
                    sortKey: FILE_CONSTS.SortFilesize,
                },
                {
                    name: 'time',
                    arrowStatus: '',
                    sortKey: FILE_CONSTS.SortLastModifiedTime,
                },
            ],
            SortNameIcon: '',
            SortSizeIcon: '',
            SortTimeIcon: '',
            /**
             * user input for file searching
             */
            searchFilesKey: '',
            /**
             * whether show hidden files, default false
             */
            showHiddenFiles:
                this.$route.query.showHiddenFiles == null
                    ? false
                    : this.$route.query.showHiddenFiles,
            /**
             * relative path to root( root is configured on the backend, it's transparent to frontend)
             */
            path: this.$route.query.path == null ? '/' : this.$route.query.path,
            /**
             * file list
             */
            filelist: [
                // {
                //   name: 'hello.txt',
                //   length: 324343,
                //   type: 1
                // },
                // {
                //   name: 'dir',
                //   length: 6827384682,
                //   type: 0
                // }
            ],
            tempFileListRecordForSearching: [],
            fileListWithoutHiddenFiles: [],
            fileListWithHiddenFiles: [],

            queryFilesOptions: new QueryFileOptions(),
        };
    },
    watch: {
        /**
         * handle changes when re-entering the page, mainly to obtain new paths
         */
        $route: {
            handler(new_route) {
                this.populatePageWithQuerys(new_route);
                this.fetch_file_list();
            },
        },
    },

    created() {
        this.populatePageWithQuerys(this.$route);
        this.fetch_file_list();
    },
    methods: {
        populatePageWithQuerys(route) {
            let pageQuery = route.query;

            // populate path
            this.path = pageQuery.path;

            // populate sort buttons group with query params
            this.sortBtns.forEach((item) => {
                let sortKey = item.sortKey;
                let sortValue = pageQuery[sortKey];
                // assign arrowStatus
                if (sortValue) {
                    let upOrDown =
                        FILE_CONSTS.MAPPING_SORTMETHOD_ARROW[sortValue];
                    if (upOrDown != null) {
                        item.arrowStatus = upOrDown;
                    }
                }
            });
        },
        /**
         * Recollect all query conditions, including path, sort conditions, then build and return HTTP GET query params
         *
         * One example for return value is: "?path=/path/to/file&name=hello&age=12"
         *
         * Invoked when one query condition changed
         */
        concatQuerys() {
            // path=xxx
            var pathQueryParam = this.buildPathQueryParam();
            // Sortxxx=xxx&Sortxxx=xxx
            var sortQuerys = this.concatSortQuerys();
            // concat
            let querys = `?${pathQueryParam}&${sortQuerys}`;
            return querys;
        },
        /**
         * invoked when click one of sort-buttons
         * @param {*} sortKey sort option key. eg: SortFilename
         */
        chooseSortBtn(sortKey) {
            // find the clicked btn by sort key
            let btn = this.sortBtns.find((item) => {
                return item.sortKey == sortKey;
            });

            // no result, return
            if (!btn) {
                return;
            }

            // remove icon from buttons whose sort key != sortKey
            this.sortBtns
                .filter((item) => {
                    return item.sortKey != sortKey;
                })
                .forEach((item) => (item.arrowStatus = ''));

            // change icon for target sort button
            btn.arrowStatus == ''
                ? (btn.arrowStatus = 'up')
                : btn.arrowStatus == 'up'
                    ? (btn.arrowStatus = 'down')
                    : btn.arrowStatus == 'down'
                        ? (btn.arrowStatus = 'up')
                        : '';

            let querys = this.concatQuerys();
            debugger;
            this.$router.push(`/filelist${querys}`);
            // // refetch filelist
            // this.fetch_file_list();
        },
        isFile(file) {
            return file.fileType === FILE;
        },

        /**
         * querys are joined by "&", eg: name=wc&age=12&address=china
         *
         * if no params is available, return empty string
         */
        concatSortQuerys() {
            let sortQuerys = '';
            // traverse each sort btn, collect sort options
            this.sortBtns.forEach((item) => {
                // only concat sort options which is not blank
                if (item.arrowStatus == '') {
                    return;
                }
                console.log(item.arrowStatus);
                console.log(FILE_CONSTS.MAPPING_ARROW_SORTMETHOD);
                // map up and down to DESC and ASC
                let sortMethod =
                    FILE_CONSTS.MAPPING_ARROW_SORTMETHOD[item.arrowStatus];
                // concat one query entry
                sortQuerys += `${item.sortKey}=${sortMethod}&`;
            });

            // remove the last "&"
            if (sortQuerys != '') {
                sortQuerys = sortQuerys.substring(0, sortQuerys.length - 1);
            }

            return sortQuerys;
        },
        /**
         * Concat path param (this.path + filename) and encode it, then return path=${finalPath}
         *
         * A return value for example: path=encoded(/path/to/somewhere/filename)
         *
         * @param {*} filename we need to concat this filename to vue.path variable
         */
        encodeFilePathQueryCondition(filename) {
            var filepath = this.concat_path(this.path, filename);
            const encodedMessage = encodeURIComponent(filepath);
            var query = `path=${encodedMessage}`;
            return query;
        },
        /**
         * Concat path param (this.path + filename) and encode it, then return path=${finalPath}
         *
         * A return value for example: path=encoded(/path/to/somewhere/filename)
         *
         * @param {*} filename we need to concat this filename to vue.path variable
         */
        buildPathQueryParam() {
            var filepath = this.path;
            const encodedMessage = encodeURIComponent(filepath);
            var query = `path=${encodedMessage}`;
            return query;
        },
        /**
         * starts with ?, eg: "?path=/path/to/file&name=abc&SortFilename=DESC"
         * @param {string} filename
         */
        concatQueryFileQuerys(filename) {
            let sortQuerys = this.concatSortQuerys();
            var pathParam = this.encodeFilePathQueryCondition(filename);
            var querys = `?${pathParam}&${sortQuerys}`;
            return querys;
        },
        intoDir(filename) {
            var querys = this.concatQueryFileQuerys(filename);

            var location = `/filelist${querys}`;

            this.$router.push(location);
        },
        /**
         * determine whether it is under search mode
         */
        searchingMode() {
            return this.searchFilesKey != null && this.searchFilesKey != '';
        },
        typeSearchKey(newInput) {
            // do nothing if searching input is empty
            if (!this.searchingMode()) {
                return;
            }

            var filelist = this.filelist;
            var resultFiles = filelist.filter((f) => {
                f.name.contains(this.searchFilesKey);
            });

            // temp store
            this.tempFileListRecordForSearching = this.filelist;
        },
        showHiddenFilesBtnToggled() {
            // if (this.showHiddenFiles) {
            //     this.showHiddenFiles = false;
            // } else {
            //     this.showHiddenFiles = true;
            // }
            // debugger;

            if (this.showHiddenFiles) {
                // remove hide-true, add hide-false
                this.queryFilesOptions.removeIfPresent(
                    new QueryFileOption(
                        FILE_CONSTS.HideHiddenFile,
                        FILE_CONSTS.TRUE,
                    ),
                );
                this.queryFilesOptions.putIfAbsent(
                    new QueryFileOption(
                        FILE_CONSTS.HideHiddenFile,
                        FILE_CONSTS.FALSE,
                    ),
                );
            } else {
                // remove hide-false, add hide-true
                this.queryFilesOptions.removeIfPresent(
                    new QueryFileOption(
                        FILE_CONSTS.HideHiddenFile,
                        FILE_CONSTS.FALSE,
                    ),
                );
                this.queryFilesOptions.putIfAbsent(
                    new QueryFileOption(
                        FILE_CONSTS.HideHiddenFile,
                        FILE_CONSTS.TRUE,
                    ),
                );
            }
            this.fetch_file_list();
        },
        fileNameSortMethod(fileA, fileB) {
            var nameA = fileA.name;
            var nameB = fileB.name;
            if (nameA != null && nameB != null) {
                var lowerNameA = nameA.toLowerCase();
                var lowerNameB = nameB.toLowerCase();
                return lowerNameA.localeCompare(lowerNameB);
            }
            return 0;
        },
        /**
         * fetch all files in "this.path" dir
         */
        fetch_file_list() {
            // '/' is the default value of this.path
            if (this.path == null) {
                this.path = '/';
            }
            const queryString = `path=${this.path}&${this.concatSortQuerys()}`;

            console.log(queryString);

            file_api
                .getFileList(queryString)
                .then((resp) => {
                    var filelist = resp.data;
                    this.filelist = filelist;
                    // this.fileListWithoutHiddenFiles = [];
                    // this.fileListWithHiddenFiles = filelist;

                    // // filter files that are not hidden to array fileListWithoutHiddenFiles
                    // filelist.forEach((f) => {
                    //     var filename = f.name;
                    //     if (filename != null) {
                    //         if (!filename.startsWith('.')) {
                    //             this.fileListWithoutHiddenFiles.push(f);
                    //         }
                    //     }
                    // });

                    // if (this.showHiddenFiles) {
                    //     this.filelist = this.fileListWithHiddenFiles;
                    // } else {
                    //     this.filelist = this.fileListWithoutHiddenFiles;
                    // }
                })
                .catch((resp) => {
                    if (resp.code === 'NO_FILE') {
                        Message.warning(
                            'File Not Found, please configure the correct root path',
                        );
                        return;
                    }
                });
        },
        /**
         * go to parent path, equals to "cd ../"
         */
        goback() {
            // already root, do nothing
            if (this.path === '/') {
                return;
            }

            const lastSplash = this.path.lastIndexOf('/');
            if (lastSplash === -1) {
                throw new Error('path error');
            }

            let parent_path = this.path.substring(0, lastSplash);

            if (parent_path === '') {
                parent_path = '/';
            }

            console.log(parent_path);

            var queryOptions = this.concatSortQuerys();

            const queryString = `path=${parent_path}&${queryOptions}`;

            this.$router.push({
                path: `/filelist?${queryString}`,
            });
        },
        do_upload_file() {
            const upload_elem = document.getElementById('upload_file');

            const files = upload_elem.files;
            const file = files[0];

            // check file size
            // fetch file size limit first
            config_api
                .getConfigValue('max-upload-size')
                .then((resp) => {
                    const limit_size = resp.data;
                    const filesize = file.size;
                    // file size exceeds the limit
                    if (filesize > limit_size * 1024 * 1024) {
                        upload_elem.value = '';
                        Message.warning(
                            'Exceed upload limit: ' + limit_size + 'M',
                        );
                        return;
                    }

                    // do upload
                    const form = new FormData();
                    form.append('file-body', file);
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
                    );

                    file_api
                        .upload(form)
                        .then((resp) => {
                            console.log(resp);
                        })
                        .then((resp) => {
                            Message.success('upload successfully');
                            this.fetch_file_list();
                        })
                        .catch((resp) => {
                            const code = resp.code;
                            if (code === 'FILE_ACCESS_DENIED') {
                                Message.error('FILE_ACCESS_DENIED');
                            }
                        });

                    upload_elem.value = '';
                })
                .catch((resp) => {
                    if (resp.code === 'CONFIG_FILE_NOT_FOUND') {
                        this.config_file_not_found_prompt = true;
                    }
                });
        },
        // open upload file dialog
        open_upload_file() {
            document.getElementById('upload_file').click();
        },
        download(name) {
            var pathParam = this.encodeFilePathQueryCondition(name);
            var location = `${new PageLocation().baseURL}/file?${pathParam}`;

            console.log('download location: ' + location);
            window.location.href = location;
        },
        concat_path(src, part) {
            if (src.charAt(src.length - 1) === '/') {
                return src + part;
            } else {
                return src + '/' + part;
            }
        },
    },
};
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

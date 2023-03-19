<template lang="">
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

            <div class="flex flex-center mgl20">
                <!-- if show hidden files -->
                <div>
                    <span class="pdr20">show hidden files</span>
                    <el-switch
                        v-model="showHiddenFiles"
                        @change="showHiddenFilesBtnToggled"
                    ></el-switch>
                </div>
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

            <el-table
                :data="filelist"
                style="width: 100%"
                :default-sort="{
                    prop: 'lastModifiedTime',
                    order: 'descending',
                }"
            >
                <el-table-column
                    sortable
                    prop="name"
                    label="name"
                    width="360"
                    :sort-method="fileNameSortMethod"
                >
                    <template slot-scope="scope">
                        <div
                            v-if="scope.row.type === 1"
                            @click="download(scope.row.name)"
                        >
                            <span class="underline folder-name">
                                {{ scope.row.name }}
                            </span>
                        </div>
                        <div v-else>
                            <router-link
                                :to="
                                    concat_path(
                                        '/filelist?path=' + path,
                                        scope.row.name,
                                    )
                                "
                                class="link-type"
                            >
                                <span class="underline file-name">
                                    {{ scope.row.name + '/' }}
                                </span>
                            </router-link>
                        </div>
                    </template>
                </el-table-column>
                <el-table-column
                    sortable
                    prop="length"
                    label="文件大小"
                    width="360"
                >
                    <template slot-scope="scope">
                        <readable-display
                            type="file-size"
                            :value="scope.row.length"
                        />
                    </template>
                </el-table-column>
                <el-table-column
                    sortable
                    prop="fileType"
                    label="fileType"
                    width="360"
                ></el-table-column>
                <el-table-column
                    sortable
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
export default {
    components: {
        readableDisplay,
    },
    data() {
        return {
            /**
             * whether show hidden files, default false
             */
            showHiddenFiles: false,
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
            fileListWithoutHiddenFiles: [],
            fileListWithHiddenFiles: [],
        };
    },
    watch: {
        /**
         * handle changes when re-entering the page, mainly to obtain new paths
         */
        $route: {
            handler(new_route) {
                this.path = new_route.query.path;
                this.fetch_file_list();
            },
        },
    },

    created() {
        this.fetch_file_list();
    },
    methods: {
        showHiddenFilesBtnToggled() {
            if (this.showHiddenFiles) {
                this.filelist = this.fileListWithHiddenFiles;
            } else {
                this.filelist = this.fileListWithoutHiddenFiles;
            }
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

            file_api
                .getFileList({
                    path: this.path,
                })
                .then((resp) => {
                    var filelist = resp.data;
                    this.fileListWithoutHiddenFiles = [];
                    this.fileListWithHiddenFiles = filelist;
                    filelist.forEach((f) => {
                        var filename = f.name;
                        if (filename != null) {
                            if (!filename.startsWith('.')) {
                                this.fileListWithoutHiddenFiles.push(f);
                            }
                        }
                    });

                    if (this.showHiddenFiles) {
                        this.filelist = this.fileListWithHiddenFiles;
                    } else {
                        this.filelist = this.fileListWithoutHiddenFiles;
                    }
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

            this.$router.push({
                path: '/filelist',
                query: { path: parent_path },
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
            var location = `${
                new PageLocation().baseURL
            }/file?path=${this.concat_path(this.path, name)}`;

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
</style>

<template lang="">
  <div style="margin: 50px 40px">
    <el-button @click="socket=undefined"></el-button>
    <div class="flex mgb20" style="width: 50%">
      <!-- select dir btn -->
      <div>
        <el-button :disabled="scanning" @click="select_dir_dialog.open()">select dir</el-button>
      </div>

      <!-- dir to scan -->
      <div>
        <el-input disabled :value="picked_dir_path" />
      </div>

      <!-- scan btn -->
      <div>
        <el-button :disabled="scanning" type="primary" @click="scan()">scan</el-button>
      </div>
    </div>

    <!-- results -->
    <div>
      <div class="mgb20">
        <!-- overview -->
        <el-card class="box-card">
          <!-- path -->
          <div slot="header" class="clearfix">
            <span>overview</span> <span>:</span>  <span>{{ curdir_info.path }}</span>
          </div>
          <div class="flex">
            <!-- num of files -->
            <div class="flex">
              <div> <span>num of files: </span></div>
              <div> <span>{{ curdir_info.numberOfFiles }}</span></div>
            </div>
            <div class="mgl8 mgr8"></div>
            <!-- size -->
            <div class="flex">
              <div> <span>size: </span></div>
              <div> <readable-display type="file-size" :value="curdir_info.length"></readable-display></div>
            </div>
          </div>
        </el-card>
      </div>
      <div>
        <el-card>
          <div><el-button @click="goback(curdir_info.path)">
            <i class="el-icon-back"></i></el-button></div>
          <!-- filelist -->
          <el-table-view :model="filelist" :sortable="['length']">
            <template slot-scope="scope">
              <div v-if="scope.col.prop==='name'">
                <div
                  v-if="scope.row.type===0"
                  class="underline pointer"
                  @click="indir(scope.row)"
                >
                  <span>{{ filename(scope.row) }}</span>
                </div>
                <div v-else>
                  <span>{{ filename(scope.row) }}</span>
                </div>
              </div>
              <div v-else-if="scope.col.prop==='length'">
                <readable-display type="file-size" :value="scope.row.length" />
              </div>
              <div v-else-if="scope.col.prop==='percent'">
                <el-progress :percentage="scope.row.percent"></el-progress>
              </div>
              <div v-else>
                {{ scope.row[scope.col.prop] }}
              </div>
            </template>
          </el-table-view>
        </el-card>
      </div>

    </div>
    <!-- select dir to scan dialog -->
    <el-dialog-view :model="select_dir_dialog" width="50%" :with_commit="false">
      <template #default="scope">
        <!-- back to parent dir btn -->
        <div class="mgb10">
          <el-button @click="select_dir_back()">back</el-button>
        </div>
        <!-- parent dir path -->
        <div>
          <div><el-button @click="pick_cur_dir(scope.data.cur_path)" /></div>
          <div>
            <span>{{ scope.data.cur_path }}</span>

          </div>
        </div>
        <!-- select file panel -->
        <div style="overflow: scroll; height: 300px">
          <!-- file list -->
          <el-table-view :model="select_dir_cur_dir_file_list">
            <template slot-scope="scope">
              <!-- pick btn -->
              <div v-if="scope.col.prop == undefined">
                <span class="underline pointer" @click="select_dir_pick(scope.row['id'])">选择</span>
              </div>

              <!-- dir name -->
              <div v-else-if="scope.col.prop === 'name'">
                <span
                  class="underline pointer"
                  @click="select_dir_into_dir(scope.row[scope.col.prop])"
                >
                  {{ scope.row[scope.col.prop] }}</span>
              </div>

            </template>
          </el-table-view>
        </div>
        <!-- scope.model 获取 dialog 的oop对象 -->
        <!-- scope.data 获取 dialog 的oop对象中的data部分 -->
        <!-- ...... -->
      </template>
    </el-dialog-view>
  </div>
</template>
<script>
import readableDisplay from '@/views/common/readable-display.vue'
import ElDialogView from '@/views/oop_components/ElDialogView.vue'
import ElTableView from '@/views/oop_components/ElTableView.vue'
import { OopElDialogModel, OopElTableModel } from '@/lib/index'
import * as file_api from '@/api/file.js'
import * as config_api from '@/api/config.js'
import * as disk_api from '@/api/disk.js'
import { Message } from 'element-ui'
let vue
export default {
    components: {
        readableDisplay,
        ElDialogView,
        ElTableView
    },
    data() {
        return {
            analyze_root: '',
            curdir_info: {},
            scanning: false,
            filelist: new OopElTableModel('', {
                cols: [
                    {
                        prop: 'name',
                        label: '名称'
                    }, {
                        prop: 'length',
                        label: '大小'
                    }, {
                        prop: 'numberOfFiles',
                        label: '文件数'
                    },
                    {
                        prop: 'percent',
                        label: '占比'
                    }
                ],
                do_fetch_data(data) {
                    let path
                    if (data) {
                        path = data.path
                    } else {
                        path = vue.picked_dir_path
                    }
                    disk_api.dirinfo({ path })
                        .then(resp => {
                            const files = resp.data.files
                            const curdir_info = resp.data.curDirInfo
                            vue.curdir_info = curdir_info
                            // sort
                            vue.desc_by_file_size(files)
                            // cal size percent
                            vue.cal_file_size_percent(curdir_info.length, files)

                            this.data = files

                            this.fetch_over()
                        }).catch(resp => {
                            Message.error(resp)
                        })
                }
            }),
            socket: undefined,
            picked_dir_path: '',
            select_dir_cur_dir_file_list: new OopElTableModel('权限列表', {
                cols: [
                    {
                        prop: 'name',
                        label: 'Name'
                    },
                    {
                        prop: undefined,
                        label: '操作'
                    }
                ],

                // /////////////// 必须方法 /////////////////

                // 拉取数据操作写在这里，调用后列表自动进入loading状态
                do_fetch_data(data) {
                    disk_api.filelist({ path: vue.select_dir_dialog.data.cur_path }).then(resp => {
                        let counter = 0
                        if (resp.data) {
                            for (const item of resp.data) {
                                item.id = counter++
                            }
                        }
                        this.data = resp.data
                        this.fetch_over()
                    })
                }
            }),
            select_dir_dialog: new OopElDialogModel('修改xx对话框', {

                // /////////////// 必须方法 /////////////////

                // 必须有该方法，创建和关闭对话框时被调用
                reset_data() {
                    this.data = {
                        cur_path: '/'
                    }
                },
                // 必须有该方法，打开对话框前被调用
                // data参数在open被调用时被传递
                before_open(data) {
                    // do some extra work
                    vue.select_dir_cur_dir_file_list.fetch_data()
                    this.do_open()
                },

                // /////////////// 可选方法 /////////////////
                // 点击 提交按钮 后执行
                do_commit() {
                    // request_net_work();
                    this.close()
                }
            }),
            /**
         * relative path to root( root is configured on the backend, it's transparent to frontend)
         */
            path: this.$route.query.path == null ? '/' : this.$route.query.path

        }
    },
    created() {
        vue = this
    },

    beforeRouteLeave(to, form, next) {
        if (this.socket) {
            this.socket.close()
        }
        console.log('leave page, socket disconnect')

        next()
    },
    methods: {

        goback(path) {
            if (!path) {
                return
            }
            // already root, do nothing
            if (path === '/' || path === this.picked_dir_path) {
                return
            }

            const lastSplash = path.lastIndexOf('/')
            if (lastSplash === -1) {
                throw new Error('path error')
            }

            let parent_path = path.substring(0, lastSplash)

            if (parent_path === '') {
                parent_path = '/'
            }

            console.log(parent_path)

            this.indir({ path: parent_path })
        },
        indir(dirinfo) {
            vue.filelist.fetch_data(dirinfo)
        },
        cal_file_size_percent(total, fileinfos) {
            if (fileinfos || fileinfos.length > 0) {
                for (const fileinfo of fileinfos) {
                    let percent = fileinfo.length * 100.0 / total
                    percent = parseFloat(Number(percent).toFixed(1))
                    fileinfo.percent = percent
                }
            }
        },
        desc_by_file_size(fileinfos) {
            if (fileinfos || fileinfos.length > 0) {
                fileinfos.sort((a, b) => {
                    return b.length - a.length
                })
            }
        },
        pick_cur_dir(path) {
            vue.picked_dir_path = path
            vue.select_dir_dialog.close()
        },
        filename(fileinfo) {
            if (fileinfo.type === 0) {
                if (fileinfo.name != '/') {
                    return fileinfo.name + '/'
                }
                return fileinfo.name
            }
            return fileinfo.name
        },
        scan() {
            this.scanning = true
            if (this.socket) {
                this.socket.close()
                this.socket = undefined
            }
            console.log('let backend scan disk')
            this.connect()
        },
        connect() {
            this.initWebsocket()
        },
        // ==============webSocket======================
        initWebsocket() {
            if (typeof (WebSocket) === 'undefined') {
                alert('您的浏览器不支持socket')
            } else {
                // 实例化socket
                this.socket = new WebSocket(`ws://${process.env.VUE_APP_HOST}:${process.env.VUE_APP_POST}/wsserver`)
                // 监听socket连接
                this.socket.onopen = this.open
                // 监听socket错误信息
                this.socket.onerror = this.error
                this.socket.onclose = this.close
                // 监听socket消息
                // this.socket.onmessage = this.getMessage
            }
        },
        open() {
            console.log('socket连接成功')
            disk_api.scan({ path: vue.picked_dir_path }).then(resp => {
                vue.filelist.fetch_data()
                vue.scanning = false
            }).catch(resp => {
                Message.error(resp.code)
                vue.scanning = false
            })
        },
        error() {
            console.log('连接错误')
            this.socket = undefined
        },
        close() {
            console.log('socket已经关闭')
            this.socket = undefined
        },
        // getMessage: function(msg) {
        //   console.log(msg.data)
        // },

        select_dir_pick(id) {
            const cur_path_dirs = vue.select_dir_cur_dir_file_list.data
            const pick_dir = cur_path_dirs.find((dir) => { return dir.id === id })
            const name = pick_dir.name
            const cur_path = vue.select_dir_dialog.data.cur_path
            console.log(name)
            let dir_path = cur_path
            if (cur_path === '/') {
                dir_path = dir_path + name
            } else {
                dir_path = dir_path + '/' + name
            }
            console.log(dir_path)
            vue.picked_dir_path = dir_path
            vue.select_dir_dialog.close()
        },
        select_dir_into_dir(name) {
            const cur_path = vue.select_dir_dialog.data.cur_path
            let next_path = cur_path
            if (cur_path === '/') {
                next_path = next_path + name
            } else {
                next_path = next_path + '/' + name
            }
            console.log(next_path)
            vue.select_dir_dialog.data.cur_path = next_path

            vue.select_dir_cur_dir_file_list.fetch_data()
        },
        select_dir_back() {
            if (vue.select_dir_dialog.data.cur_path === '/') {
                return
            }

            const lastSplash = vue.select_dir_dialog.data.cur_path.lastIndexOf('/')
            if (lastSplash === -1) {
                throw new Error('path error')
            }

            let parent_path = vue.select_dir_dialog.data.cur_path.substring(0, lastSplash)

            if (parent_path === '') {
                parent_path = '/'
            }

            console.log(parent_path)
            vue.select_dir_dialog.data.cur_path = parent_path

            vue.select_dir_cur_dir_file_list.fetch_data()
        },
        /**
       * fetch all files in "this.path" dir
       */
        fetch_file_list() {
            // '/' is the default value of this.path
            if (this.path == null) {
                this.path = '/'
            }

            file_api.getFileList({
                path: this.path
            }).then(resp => {
                this.filelist = resp.data
            }).catch(resp => {
                if (resp.code === 'NO_FILE') {
                    Message.warning('File Not Found, please configure the correct root path')
                    return
                }
            })
        },
        do_upload_file() {
            const upload_elem = document.getElementById('upload_file')

            const files = upload_elem.files
            const file = files[0]

            // check file size
            // fetch file size limit first
            config_api.getConfigValue('max-upload-size').then(resp => {
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
                                path: this.path
                            })
                        ],
                        {
                            type: 'application/json'
                        }
                    )
                )

                file_api.upload(form).then((resp) => {
                    console.log(resp)
                }).then((resp) => {
                    Message.success('upload successfully')
                    file_api.getFileList({
                        path: this.path
                    }).then(resp => {
                        this.filelist = resp.data
                    })
                }).catch(resp => {
                    const code = resp.code
                    if (code === 'FILE_ACCESS_DENIED') {
                        Message.error('FILE_ACCESS_DENIED')
                    }
                })

                upload_elem.value = ''
            }).catch((resp) => {
                if (resp.code === 'CONFIG_FILE_NOT_FOUND') {
                    this.config_file_not_found_prompt = true
                }
            })
        },
        // open upload file dialog
        open_upload_file() {
            document.getElementById('upload_file').click()
        },
        download(name) {
            const location = `${process.env.VUE_APP_BASE_API}file?path=` + this.concat_path(this.path, name)
            console.log('download location: ' + location)
            window.location.href = location
        },
        concat_path(src, part) {
            if (src.charAt(src.length - 1) === '/') {
                return src + part
            } else {
                return src + '/' + part
            }
        }
    }
}
</script>
<style lang="scss">
  @import '~@/styles/common-style.scss';
  .underline {
   &:hover{
    text-decoration: underline;
   }
  }

  .el-table--medium td{
    padding: 0
  }

</style>


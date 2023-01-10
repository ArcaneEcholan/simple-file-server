<template lang="">
  <div style="margin: 50px 40px">
    <div class="flex justify-between" style="padding: 20px 0px;border: 1px solid;  ">
      <div>
        <span style="font-size: 30px;  ">{{ path }}</span>
      </div>
      <div>
        <input id="upload_file" type="file" style="display: none" name="myfile" @change="do_upload_file">
        <el-button @click="open_upload_file()">upload</el-button>
      </div>
    </div>
    <div>
      <el-table :data="filelist" style="width: 100%">
        <el-table-column prop="name" label="文件名" width="360">
          <template slot-scope="scope">
            <div v-if="scope.row.type===1" @click="download(scope.row.name)">
              <span class="underline" style="font-size: 20px; color: #0086b3; cursor: pointer">{{ scope.row.name }}</span>
            </div>
            <div v-else>
              <router-link :to="concat_path('/filelist?path=' + path, scope.row.name) " class="link-type">
                <span class="underline" style="font-size: 20px; color: #d02474; cursor: pointer">{{ scope.row.name +"/" }} </span>
              </router-link>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="length" label="文件大小">
          <template slot-scope="scope">
            <readable-display type="file-size" :value="scope.row.length" />
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>
<script>
import readableDisplay from '@/views/common/readable-display.vue'
import * as file_api from '@/api/file.js'
export default {
  components: {
    readableDisplay
  },
  data() {
    return {
      path: this.$route.query.path == null ? '/' : this.$route.query.path,
      filelist: [
        {
          name: 'hello.txt',
          length: 324343,
          type: 1
        },
        {
          name: 'dir',
          length: 6827384682,
          type: 0
        }
      ]
    }
  },
  watch: {
    // 监听路由发生改变
    $route: {
      handler(new_route) {
        console.log(new_route)
        this.path = new_route.query.path
        if (this.path == null) {
          this.path = '/'
        }
        file_api.getFileList({
          path: this.path
        }).then(resp => {
          this.filelist = resp.data
        })
      }
    }
  },
  created() {
    console.log(this.path)
    if (this.path == null) {
      this.path = '/'
    }
    file_api.getFileList({
      path: this.path
    }).then(resp => {
      this.filelist = resp.data
    })
  },
  methods: {
    do_upload_file() {
      const upload_elem = document.getElementById('upload_file')

      const files = upload_elem.files
      const form = new FormData()
      form.append('file-body', files[0])
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
      })

      upload_elem.value = ''
    },
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

</style>

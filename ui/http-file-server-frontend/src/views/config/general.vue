<!--  -->
<template>
  <div style="margin: 50px 40px">
    <div v-show="config_file_not_found_prompt">
      <span>Config File Not Found</span>
    </div>

    <!-- config list -->
    <el-table
      v-show="!config_file_not_found_prompt"
      :data="config_list"
      style="width: 100%"
    >
      <!-- config name -->
      <el-table-column
        label=""
        width="180"
      >
        <template slot-scope="scope">
          <div><span>{{ scope.row.key }}</span></div>
        </template>
      </el-table-column>
      <!-- config value -->
      <el-table-column
        label=""
        width="360"
      >
        <template slot-scope="scope">
          <div class="flex">
            <!-- value input -->
            <div class="flexg8">
              <el-input v-model="scope.row.value" />
            </div>
            <!-- text -->
            <div class="flexg2"><span v-show="'max-upload-size' === scope.row.key" style="line-height: 2.5; padding: 0px 10px">M</span></div>
          </div>

        </template>
      </el-table-column>
      <!-- operation -->
      <el-table-column label="">
        <template slot-scope="scope">
          <el-button
            circle
            type="success"
            size="mini"
            icon="el-icon-check"
            @click="submit_config(scope.row.key, scope.row.value)"
          />
        </template>
      </el-table-column>
    </el-table>

  </div>
</template>

<script>

import * as configApi from '@/api/config'
import { Message } from 'element-ui'
export default {
  components: {},
  data() {
    return {
      config_file_not_found_prompt: false,
      // one config consists of key and value
      // provide key only, value is provided by api
      config_list: [{
        key: 'root'
      },
      {
        key: 'max-upload-size'
      }
      ]
    }
  },
  computed: {},
  watch: {},
  created() {
    // result config list to fill
    const result_config_list = []

    // if not key is present, do nothing
    const conf_size = this.config_list.length
    if (conf_size === 0) {
      return
    }

    let key

    // ///////////// multiple keys condition ///////////////

    // fetch the value of first key
    key = this.config_list[0].key
    let promise = Promise.resolve().then(() => {
      return configApi.getConfigValue(key)
    })

    // iterarte keys to fetch value
    for (let i = 1; i < conf_size; i++) {
      promise = promise.then(resp => {
        if (resp.code === 'NO_CONFIG_KEY') {
          // process the previous fetching task, push it to result_config_list
          result_config_list.push({ key, value: '' })
        } else {
          // process the previous fetching task, push it to result_config_list
          result_config_list.push({ key, value: resp.data })
        }

        // prepare next key to deal with
        key = this.config_list[i].key
        // fetch next value of key
        return configApi.getConfigValue(key)
      })
    }

    // the end of promise chain
    promise = promise.then((resp) => {
      if (resp.code === 'NO_CONFIG_KEY') {
        // process the previous fetching task, push it to result_config_list
        result_config_list.push({ key, value: '' })
      } else {
        // process the previous fetching task, push it to result_config_list
        result_config_list.push({ key, value: resp.data })
      }
      // assgin it to vue data
      this.config_list = result_config_list
    }).catch((resp) => {
      if (resp.code === 'CONFIG_FILE_NOT_FOUND') {
        this.config_file_not_found_prompt = true
      }
    })
  },
  mounted() {

  },
  beforeCreate() {

  },
  beforeMount() {

  },
  beforeUpdate() {

  },
  updated() {

  },
  beforeDestroy() {

  },
  destroyed() {

  },
  activated() {

  },
  methods: {
    submit_config(key, value) {
      configApi.updateConfig(key, value).then((resp) => {
        Message.success('bing go')
      }).catch(resp => {
        const code = resp.code
        if (code === 'MAX_UPLOAD_SIZE_TOO_LARGE') {
          Message.error('Max upload size must be less than 2G')
        }
        if (code === 'FRONT_END_PARAMS_ERROR') {
          if (key === 'max-upload-size') {
            Message.warning('Value must be posive Integer which is less then 2048')
          }
        }
      })
    }
  }
}
</script>
<style lang='scss' scoped>
@import '~@/styles/common-style.scss';
</style>

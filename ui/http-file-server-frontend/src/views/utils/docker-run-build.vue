<template lang="">
  <div style="margin: 50px 40px">
    <!-- result panel-->
    <div class="mgb20">
      <el-card class="box-card">
        <!-- title -->
        <div slot="header" class="clearfix">
          <span>result</span>
        </div>

        <!-- result -->
        <div class="mgb20">
          <el-input
            v-model="result"
            disabled
            type="textarea"
            :autosize="{ minRows: 2, maxRows: 4 }"
            :rows="2"
            placeholder=""
          />
        </div>

        <!-- copy btn -->
        <el-button type="primary" @click="copy">copy</el-button>
      </el-card>
    </div>

    <div class="mgb20">
      <el-card class="box-card">
        <div slot="header" class="clearfix">
          <span>basic info</span>
        </div>
        <!-- name -->
        <div class="mgb20">
          <div class="flex">
            <div class="pd10"><span>Name</span></div>
            <div class="flexg1"><el-input v-model="name" /></div>
          </div>
        </div>

        <!-- image -->
        <div class="mgb20">
          <div class="flex">
            <div class="pd10"><span>Image</span></div>
            <div class="flexg1"><el-input v-model="image" /></div>
          </div>
        </div>
        <div class="mgb20">
          <el-button
            :disabled="name == '' || image == ''"
            type="primary"
            @click="generate_docker_run"
          >generate docker run</el-button>
        </div>
      </el-card>
    </div>

    <!-- advanced configs -->
    <div>
      <el-tabs type="border-card">
        <el-tab-pane label="port">
          <div class="" style="width: 50%">
            <dynamic-row
              :rows="ports"
              title="port "
              @add="add_port_row"
              @remove="delete_export_ports_row"
            >
              <template #default="scope">
                <div class="flex">
                  <!-- host port -->
                  <div class="flexg5">
                    <taged-input v-model="scope.row.host" tag="host" />
                  </div>
                  <div style="line-height: 2">
                    <span style="margin: 0 10px">
                      <i class="el-icon-right" />
                    </span>
                  </div>
                  <!-- container port -->
                  <div class="flexg5">
                    <taged-input
                      v-model="scope.row.container"
                      tag="container"
                    />
                  </div>
                  <div style="line-height: 2">
                    <span style="margin: 0 10px" />
                  </div>
                </div>
              </template>
            </dynamic-row>
          </div>
        </el-tab-pane>
        <el-tab-pane label="volume">
          <div class="" style="width: 50%">
            <dynamic-row
              :rows="volumes"
              title="volume"
              @add="add_volume_row"
              @remove="delete_volumes_row"
            >
              <template #default="scope">
                <!-- <div class="flex">
                <div class="flexg5" style="background-color: red; ">
                  <input type="text" style="width: 100%">
                </div>
                <div class="" style="background-color: green; ">
                  <input type="text" style="width: 100%">
                </div>
              </div> -->
                <div class="flex">
                  <!-- host path -->
                  <div class="flexg5">
                    <taged-input v-model="scope.row.host" tag="host" />
                  </div>
                  <div style="line-height: 2">
                    <span style="margin: 0 10px">
                      <i class="el-icon-right" />
                    </span>
                  </div>

                  <!-- container path -->
                  <div class="flexg5">
                    <taged-input
                      v-model="scope.row.container"
                      tag="container"
                    />
                  </div>

                  <div style="line-height: 2">
                    <span style="margin: 0 10px" />
                  </div>
                </div>
              </template>
            </dynamic-row>
          </div>
        </el-tab-pane>
        <el-tab-pane label="env">
          <div class="" style="width: 50%">
            <dynamic-row
              :rows="envs"
              title="env "
              @add="add_env"
              @remove="delete_env"
            >
              <template #default="scope">
                <!-- <div class="flex">
                <div class="flexg5" style="background-color: red; ">
                  <input type="text" style="width: 100%">
                </div>
                <div class="" style="background-color: green; ">
                  <input type="text" style="width: 100%">
                </div>
              </div> -->
                <div class="flex">
                  <!-- host port -->
                  <div class="flexg5">
                    <taged-input v-model="scope.row.name" tag="name" />
                  </div>
                  <div style="line-height: 2">
                    <span style="margin: 0 10px">
                      <i class="el-icon-right" />
                    </span>
                  </div>
                  <!-- container port -->
                  <div class="flexg5">
                    <taged-input v-model="scope.row.value" tag="value" />
                  </div>
                  <div style="line-height: 2">
                    <span style="margin: 0 10px"></span>
                  </div>
                </div>
              </template>
            </dynamic-row>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>
<script>
import readableDisplay from '@/views/common/readable-display.vue'
import ElDialogView from '@/views/oop_components/ElDialogView.vue'
import TagedInput from '@/views/utils/comp/taged-input.vue'
import DynamicRow from '@/views/utils/comp/dynamic-row.vue'
import ElTableView from '@/views/oop_components/ElTableView.vue'
import { OopElDialogModel, OopElTableModel } from '@/lib/index'
import * as file_api from '@/api/file.js'
import * as config_api from '@/api/config.js'
import * as disk_api from '@/api/disk.js'
import { Message } from 'element-ui'
import { ArrayUtils, copy, uuidv4 } from '@/utils'
let vue
export default {
    components: {
        DynamicRow,
        TagedInput,
        readableDisplay,
        ElDialogView,
        ElTableView
    },
    data() {
        return {
            result: '',
            image: '',
            name: '',
            volume_config_counter: 0,
            port_config_counter: 0,

            /**
       * {
       *      host: '',
       *      container: '',
       *      id: 1,
       *  }
       */
            volumes: [],
            envs: [],
            ports: []
        }
    },
    created() {
        vue = this
    },

    methods: {
        copy() {
            if (this.result) {
                copy(this.result)
                Message.success('copy successfully')
            } else {
                Message.warning('no content')
            }
        },
        any_input_change() {
            if (this.name && this.image) {
                this.generate_docker_run()
            }
        },
        generate_docker_run() {
            let cmd = `docker run -d `
            if (this.ports && this.ports.length > 0) {
                for (const port of this.ports) {
                    cmd += `-p ${port.host}:${port.container} `
                }
            }

            if (this.volumes && this.volumes.length > 0) {
                for (const volume of this.volumes) {
                    cmd += `-v ${volume.host}:${volume.container} `
                }
            }

            if (this.envs && this.envs.length > 0) {
                for (const env of this.envs) {
                    cmd += `-e ${env.name}=${env.value} `
                }
            }

            cmd += `--name ${this.name} `
            cmd += `${this.image}`
            this.result = cmd
        },
        delete_env(row) {
            ArrayUtils.remove(this.envs, (item) => item.id === row.id)
        },
        delete_volumes_row(row) {
            ArrayUtils.remove(this.volumes, (item) => item.id === row.id)
        },
        delete_export_ports_row(row) {
            ArrayUtils.remove(this.ports, (item) => item.id === row.id)
        },
        add_env() {
            this.envs.push({
                id: uuidv4(),
                name: '',
                value: ''
            })
        },
        add_volume_row() {
            this.volumes.push({
                id: uuidv4(),
                host: '',
                container: ''
            })
        },
        add_port_row() {
            this.ports.push({
                id: uuidv4(),
                host: '',
                container: ''
            })
        }
    }
}
</script>
<style lang="scss">
@import "~@/styles/common-style.scss";
.underline {
  &:hover {
    text-decoration: underline;
  }
}

.el-table--medium td {
  padding: 0;
}
</style>

<template>
  <el-select
    :id="model.id"
    v-model="model.value"
    v-loading="model.loading"
    style="width: 100%"
    :placeholder="placeholder"
    @visible-change="handle_select"
  >
    <slot />
  </el-select>
</template>

<script>
import { OopElSelectModel } from '@/lib'
export default {
  props: {
    loading: {
      type: Boolean,
      default: false
    },
    placeholder: {
      type: String,
      default: ''
    },
    model: {
      required: true,
      type: OopElSelectModel
    }
  },
  methods: {
    handle_select(show) {
      if (!show) {
        const select = $(`#${this.model.id}`)
        select.blur()
      }
    }
  }
}
</script>

<style></style>

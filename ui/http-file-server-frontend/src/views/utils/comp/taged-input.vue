<template lang="">
  <div>
    <div style="background-color: #eee; " class="flex br5">
      <div class="pd8 ">{{ tag }}</div>
      <div class="flexg1">
        <el-input :value="value" @input="change($event)" />
      </div>
    </div>
  </div>
</template>
<script>
export default {
    props: {
        tag: {
            type: String,
            default: 'tag',
            required: false
        },
        value: {
            type: String || Number,
            required: true
        }
    },
    methods: {
        change($event) {
            this.$emit('input', $event)
        }
    }
}
</script>
<style lang="scss">
@import '~@/styles/common-style.scss';

</style>

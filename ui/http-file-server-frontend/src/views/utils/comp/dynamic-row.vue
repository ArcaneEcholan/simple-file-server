<template lang="">
  <div>
    <!-- title -->
    <div
      class="flex justify-between mgb10"
      style="width:50%"
    >
      <div
        class="mgr10 fs24"
        style="line-height: 1.2"
      >
        {{ title }}
      </div>
      <div>
        <el-button
          type="primary"
          circle
          style="width: 100%"
          icon="el-icon-plus"
          size="mini"
          @click="add_row"
        />
      </div>
    </div>
    <!-- rows -->
    <div
      v-for="row of rows"
      :key="row.id"
    >
      <div
        class="flex justify-between align-center mgb10"
      >
        <!-- custom row -->
        <div class="flexg1">
          <slot :row="row" />
        </div>

        <!-- remove btn -->
        <div>
          <el-button
            circle
            type="danger"
            icon="el-icon-delete"
            @click="remove_row(row)"
          />
        </div>
      </div>

    </div>
  </div>
</template>
<script>
export default {
    props: {
        title: {
            type: String,
            default: 'title',
            required: false
        },
        rows: {
            type: Array,
            default: () => [],
            required: false
        }
    },
    methods: {
        add_row() {
            this.$emit('add')
        },
        remove_row(row) {
            this.$emit('remove', row)
        }

    }
}
</script>
  <style lang="scss">
  @import '~@/styles/common-style.scss';

  </style>


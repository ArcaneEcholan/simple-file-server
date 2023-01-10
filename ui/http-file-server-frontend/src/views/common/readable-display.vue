<template>
  <div>
    {{ display_value }}
  </div>
</template>

<script>
import { get_display_time, get_display_file_size } from '@/utils'
export default {
  props: {
    value: '',
    type: ''
  },
  data() {
    return {}
  },
  computed: {
    display_value() {
      switch (this.type) {
        case 'time':
          return get_display_time(new Date(this.value))

        case 'file-size':
          return get_display_file_size(this.value)
        default:
          return ''
      }
    }
  }
}
</script>

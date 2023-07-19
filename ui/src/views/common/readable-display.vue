<template>
    <div>
        {{ display_value }}
    </div>
</template>

<script lang="ts">
import { get_display_time, get_display_file_size } from '@/ts/utils';
import { Component, Vue, Prop } from 'vue-property-decorator';
@Component({})
export default class ReadableDisplayComponent extends Vue {
    @Prop({ default: 0 })
    value!: number;
    @Prop({ default: '' })
    type!: string;

    get display_value() {
        switch (this.type) {
            case 'time':
                return get_display_time(new Date(this.value).getTime());
            case 'file-size':
                return get_display_file_size(this.value);
            default:
                return '';
        }
    }
}
</script>

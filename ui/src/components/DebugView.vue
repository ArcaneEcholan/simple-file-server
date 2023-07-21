<template>
    <div v-if="debug"><slot></slot></div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator';
@Component({})
export default class DebugView extends Vue {
    get debug() {
        return this.$store.getters.debugView;
    }
}
</script>
<style lang="scss" scoped></style>

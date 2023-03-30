<template>
    <div>
        <div>{{ var1 }}</div>
        <el-button @click="var1 = 'world'">btn</el-button>
        <el-button @click="gotopage">gotopage</el-button>
    </div>
</template>

<script>
export default {
    data() {
        return {
            var1: 'hello',
        };
    },

    created() {
        debugger;
        console.log('instance created');
    },

    mounted() {
        debugger;
        console.log('instance mounted');
    },

    beforeUpdate() {
        debugger;

        console.log('instance before update');
    },

    updated() {
        debugger;
        console.log('instance updated');
    },
    methods: {
        gotopage() {
            debugger;
            this.$router.push('/test1?path=you');
        },
    },

    beforeUnmount() {
        debugger;
        console.log('before unmounted');
    },

    beforeRouteEnter(to, from, next) {
        debugger;
        console.log(
            `in comp beforeRouteEnter, from ${from.path} to ${to.path}`,
        );

        next((vm) => {
            debugger;
            console.log('in comp beforeRouteEnter, vm ok');
        });
    },

    beforeRouteUpdate(to, from, next) {
        debugger;
        console.log(`in comp route update, from ${from.path} to ${to.path}`);
        next();
    },

    beforeRouteLeave(to, from, next) {
        debugger;
        console.log(`in comp route leave, from ${from.path} to ${to.path}`);
        next();
    },
};
</script>

<style></style>

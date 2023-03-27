<template>
    <div>hello</div>
</template>

<script>
export default {
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

    beforeRouteUpdate(to, from) {
        debugger;
        console.log(`in comp route update, from ${from.path} to ${to.path}`);
    },

    beforeRouteLeave(to, from, next) {
        debugger;
        console.log(`in comp route leave, from ${from.path} to ${to.path}`);
        next(false);
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
};
</script>

<style></style>

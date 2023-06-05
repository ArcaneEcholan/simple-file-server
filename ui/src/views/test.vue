<!--  -->
<template>
    <div class="">
        <div class="box">
            <span class="name-text">
                hellohellohellohellohellohellohellohellohellohello
            </span>
        </div>
    </div>
</template>

<script>
export default {
    components: {},
    data() {
        return {}
    },
    created() {
        console.log(
            this.isNameTruncated(
                'hellohellohellohellohellohellohellohellohellohellohellohellohellohellohellohellohellohellohellohello',
            ),
        )
    },
    methods: {
        isNameTruncated(name) {
            const element = document.createElement('div')
            element.style.width = '100px'
            element.style.position = 'absolute'
            element.style.visibility = 'hidden'
            element.style.whiteSpace = 'nowrap'
            element.style.fontSize = '16px'
            element.style.fontWeight = 'normal'
            // element.style.letterSpacing = '0.2em'
            element.textContent = name
            document.body.appendChild(element)
            console.log(element.offsetWidth)
            console.log(element.scrollWidth)
            const isTruncated = element.offsetWidth < element.scrollWidth
            document.body.removeChild(element)
            return isTruncated
        },
    },
}
</script>
<style lang="scss" scoped>
@import '~@/styles/common-style.scss';
.name-text {
    display: block;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 1;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}
.box {
    width: 200px;
    height: 100px;
    border: 1px solid;
}
</style>

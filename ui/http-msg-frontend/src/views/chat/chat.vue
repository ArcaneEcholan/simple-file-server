<template>
    <div>
        <!-- debug log -->
        <div v-if="debugLog" class="debug-log-panel">
            <div v-for="event of events">{{ event }}</div>
        </div>

        <div>
            <!-- pc ui -->
            <div v-if="!mobileMode()">
                <div class="pc-main-container flex">
                    <!-- left side -->
                    <div class="left-side-frame">
                        <div class="left-side-header"></div>
                        <!-- user list -->
                        <div
                            v-for="user of getOnlineUsers()"
                            :id="`useritem-${user.id}`"
                            :key="user.id"
                            class="useritem"
                            @click="pickChat(user)"
                        >
                            <div>
                                <span class="pdr10">
                                    {{ user.name }}
                                </span>
                                <sup
                                    v-if="ifUserItemHasRedDot(user)"
                                    class="little-top-red-dot"
                                ></sup>
                                <span
                                    v-if="user.ifThisDev"
                                    class="mgl10 this-dev-prompt"
                                >
                                    (This Device)
                                </span>
                            </div>
                            <span
                                v-if="!user.online"
                                class="offline-prompt mgl10"
                            >
                                (offline)
                            </span>
                        </div>
                    </div>
                    <!-- right-side -->
                    <div class="flex column flexg1">
                        <!-- <div>{{ chat.ip }}</div> -->
                        <!-- msgs -->
                        <div class="msgs-panel flex column flexg1">
                            <div
                                v-for="msg of getCurChat().msgs"
                                :id="msg.vueKeyBindId"
                                :key="msg.vueKeyBindId"
                                :class="`flex msgbox ${
                                    msg.from === getCurChat().user.id
                                        ? 'other'
                                        : 'me'
                                }`"
                            >
                                <div
                                    v-if="!msg.successSent"
                                    class="flex flex-center"
                                >
                                    <div>
                                        <i
                                            style="color: red"
                                            class="ali-international-icon-gantanhao-red-hollow"
                                        />
                                    </div>
                                </div>
                                <div
                                    :class="`msg-content ${
                                        msg.from === getCurChat().user.id
                                            ? 'other'
                                            : 'me'
                                    }`"
                                >
                                    {{ msg.text }}
                                </div>
                            </div>
                        </div>
                        <!-- input -->
                        <el-input
                            v-model="userInputMsg"
                            resize="none"
                            :disabled="!inputActive"
                            :autosize="{ minRows: 4, maxRows: 6 }"
                            type="textarea"
                            @keyup.enter.native="keyBoardEnter13"
                        ></el-input>
                    </div>
                </div>
            </div>

            <div v-else>
                <!-- mobile ui -->
                <div class="flex mobile-main-container">
                    <!-- left side-->
                    <div
                        :style="`
                            overflow:auto ;
                            border-right: 1px solid #dbdbdb;
                            ${
                            getCurChat().ip != null
                                ? 'width: 0'
                                : 'width: 100%;'
                        }
                        `"
                    >
                        <div class="left-side-header"></div>
                        <!-- user list -->
                        <div style="">
                            <div
                                v-for="user of getOnlineUsers()"
                                :id="`useritem-${user.id}`"
                                :key="user.ip"
                                class="useritem"
                                @click="pickChat(user)"
                            >
                                <div>
                                    <span class="pdr10">{{ user.ip }}</span>
                                    <sup
                                        v-if="ifUserItemHasRedDot(user)"
                                        class="little-top-red-dot"
                                    ></sup>
                                    <span
                                        v-if="user.ifThisDev === 1"
                                        class="this-dev-prompt mgl10"
                                    >
                                        (This Device)
                                    </span>
                                </div>

                                <span
                                    v-if="!user.online"
                                    class="offline-prompt mgl10"
                                >
                                    (offline)
                                </span>
                            </div>
                        </div>
                    </div>
                    <!-- right side-->
                    <div
                        v-if="getCurChat().ip != null"
                        class="flex column flexg1"
                    >
                        <div class="right-side-header flex">
                            <div
                                class="back-to-user-list-btn flex flex-center"
                                @click="back2UserList"
                            >
                                <div><i class="el-icon-back"></i></div>
                            </div>
                            <div class="flexg1 flex flex-center">
                                <div>{{ getCurChat().ip }}</div>
                            </div>
                        </div>
                        <!-- msgs -->
                        <div class="msgs-panel flex column flexg1">
                            <div
                                v-for="msg of getCurChat().msgs"
                                :id="msg.vueKeyBindId"
                                :key="msg.vueKeyBindId"
                                :class="`flex msgbox ${
                                    msg.from === getCurChat().ip
                                        ? 'other'
                                        : 'me'
                                }`"
                            >
                                <div
                                    v-if="!msg.successSent"
                                    class="flex flex-center"
                                >
                                    <div>
                                        <i
                                            style="color: red"
                                            class="ali-international-icon-gantanhao-red-hollow"
                                        />
                                    </div>
                                </div>
                                <div
                                    :class="`msg-content ${
                                        msg.from === getCurChat().ip
                                            ? 'other'
                                            : 'me'
                                    }`"
                                >
                                    {{ msg.text }}
                                </div>
                            </div>
                        </div>
                        <!-- input -->
                        <el-input
                            v-model="userInputMsg"
                            resize="none"
                            :disabled="!inputActive"
                            :autosize="{ minRows: 4, maxRows: 6 }"
                            type="textarea"
                            @keyup.enter.native="keyBoardEnter13"
                        ></el-input>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>
<script>
import * as config from '@/config/config'
import { User } from '@/model/user'
import store from '@/store'
import { VueWebSocket } from '@/webSocket/vueWebSocket.js'
import { storeOperations } from '@/store/storeOperations'
export default {
    data() {
        return {
            debugLog: false,
            reconnectTimeout: undefined,
            events: [],
            userInputMsg: '',
            inputActive: false,
            screenWidth: document.body.clientWidth
        }
    },
    destroyed() {
        this.events.push('destroyed')
        window.removeEventListener(
            'resize',
            this.updateScreenWidthWhenResizeWindow
        )
    },
    created() {
        console.log('onelineUsers,', store.state.chat.onlineUsers)
        this.events.push('created')

        window.addEventListener(
            'resize',
            this.updateScreenWidthWhenResizeWindow
        )

        console.log(config)
        this.socket = new VueWebSocket(this)
    },
    methods: {
        getOnlineUsers() {
            return store.getters.onlineUsers
        },
        getCurChat() {
            return store.getters.curChat
        },

        mobileMode() {
            return this.screenWidth < 810
        },
        scrollDomElemIntoView(msgVueKeyBindId) {
            const domElem = document.getElementById(msgVueKeyBindId)
            domElem.scrollIntoView()
        },
        ifUserItemHasRedDot(user) {
            const chat = store.getters.chats.find((chat) => chat.user.id === user.id)
            return chat.hasNewMsgs
        },
        closeMsgsPanel() {
            store.state.chat.curChat = {}
        },

        updateScreenWidthWhenResizeWindow() {
            this.screenWidth = document.body.clientWidth
            console.log(this.screenWidth)
        },
        back2UserList() {
            this.changeUserItemDivBackgroundColor2Normal(
                new User(null, store.getters.curChat.ip)
            )
            this.closeMsgsPanel()
        },
        useritemDivBackgroundColor(type) {
            switch (type) {
                case 'normal':
                    return '#e7e7e7'
                case 'hover':
                    return '#f1f1f1'
                case 'active':
                    return '#ededed'
                case 'focus':
                    return '#c5c5c5'
                default:
                    return ''
            }
        },
        keyBoardEnter13() {
            this.sendMsg()
        },
        getUserInputText() {
            return this.userInputMsg
        },
        sendMsg() {
            this.socket.sendMsg()
        },
        pickChat(user) {
            this.inputActive = true

            const chat = storeOperations.findChatById(user.id)
            store.state.chat.curChat = chat
            store.getters.curChat.hasNewMsgs = false
            this.changeUserItemDivBackgroundColor2Focus(user)

            this.lastMsgScrollIntoView()
        },
        changeUserItemDivBackgroundColor2Normal(user) {
            const divId = this.getIdOfDivWithUserItemClazz(user)
            const useritemDiv = document.getElementById(divId)
            useritemDiv.style.backgroundColor = ''
        },
        getIdOfDivWithUserItemClazz(user) {
            return `useritem-${user.id}`
        },
        changeUserItemDivBackgroundColor2Focus(user) {
            for (const onlineUser of store.getters.onlineUsers) {
                const divId = this.getIdOfDivWithUserItemClazz(onlineUser)
                const useritemDiv = document.getElementById(divId)
                if (user.id === onlineUser.id) {
                    useritemDiv.style.backgroundColor =
                        this.useritemDivBackgroundColor('focus')
                } else {
                    useritemDiv.style.backgroundColor = ''
                }
            }
        },
        lastMsgScrollIntoView() {
            this.$nextTick(() => {
                if (store.getters.curChat.hasMsgs()) {
                    const msgVueKeyBindId =
                        store.getters.curChat.getLastMsg().vueKeyBindId
                    this.scrollDomElemIntoView(msgVueKeyBindId)
                }
            })
        }
    }
}
</script>

<style lang="scss" scoped>
@import '~@/styles/common-style.scss';

.debug-log-panel {
    height: 20vh;
    overflow: auto;
    border-bottom: 1px solid;
}
.little-top-red-dot {
    background-color: red;
    height: 8px;
    width: 8px;
    border-radius: 50%;
    display: inline-block;
}

.left-side-header {
    height: 8%;
    background-color: rgb(245, 245, 245);
}

.msgs-panel {
    background-color: #f5f5f5;
    overflow: auto;
}
.pc-main-container {
    height: 80vh;
    width: 60vw;
    margin: 10vh auto;
    box-shadow: 0 0px 14px rgba(0, 0, 0, 0.12), 0 0 6px rgba(0, 0, 0, 0.04);
    border-radius: 5px;

    .left-side-frame {
        width: 30%;
        overflow: auto;
        border-right: 1px solid #dbdbdb;
    }
}

.mobile-main-container {
    height: 60vh;
    width: 100vw;
    margin: 10vh auto;
    box-shadow: 0 0px 14px rgba(0, 0, 0, 0.12), 0 0 6px rgba(0, 0, 0, 0.04);
    border-radius: 5px;

    .right-side-header {
        height: 8%;
        background-color: rgb(245, 245, 245);
        border-bottom: 1px solid #dbdbdb;
    }

    .back-to-user-list-btn {
        height: 100%;
        border-right: 1px solid #c5c5c5;
        width: 30%;
    }
}

::v-deep .el-textarea__inner {
    border-radius: 0 !important;
}

.useritem {
    padding: 15px;
    background-color: #e7e7e7;
    border-bottom: 1px solid #ddd;
    height: 75px;

    &:hover {
        background-color: #f1f1f1;
    }

    &:active {
        background-color: #ededed;
    }

    .this-dev-prompt {
        color: #11b95c;
    }

    .offline-prompt {
        color: #e91a2c;
    }
}

// .useritem picked {
//     background-color: #c5c5c5;
// }

.msgbox {
    margin: 10px;
    max-width: 70%;

    .msg-content {
        padding: 10px;
        border-radius: 5px;
        word-break: break-all;
        white-space: pre-line;

        &.me {
            background-color: #89d961;
        }

        &.other {
            background-color: #ebebeb;
        }
    }

    &.me {
        align-self: end;
    }

    &.other {
        align-self: start;
    }
}
</style>

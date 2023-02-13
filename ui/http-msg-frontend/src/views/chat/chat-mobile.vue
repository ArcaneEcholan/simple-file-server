<template lang="">
    <div style="">
        <div
            style="
                height: 80vh;
                width: 60vw;
                margin-left: auto;
                margin-right: auto;
                margin-top: 10vh;
                margin-bottom: 10vh;
                box-shadow: 0 0px 14px rgba(0, 0, 0, 0.12),
                    0 0 6px rgba(0, 0, 0, 0.04);
                border-radius: 5px;
            "
            class="flex column"
        >
            <div
                style="
                    height: 8%;
                    background-color: rgb(245, 245, 245);
                    border-bottom: 1px solid #dbdbdb;
                "
                class="flex"
            >
                <div style="width: 30%; border-right: 1px solid #dbdbdb"></div>
                <div style="padding: 1.5vh" class="flexg1">
                    <span style="padding-left: 1vh">{{ chat.ip }}</span>
                </div>
            </div>
            <div class="flex flexg1">
                <!-- user list -->
                <div
                    style="
                        width: 30%;
                        overflow: auto;
                        border-right: 1px solid #dbdbdb;
                    "
                >
                    <div
                        v-for="user of onlineUsers"
                        :id="`useritem-${user.ip}`"
                        :key="user.ip"
                        class="useritem"
                        @click="pickChat(user)"
                    >
                        <div>
                            {{ user.ip }}
                            <span
                                v-if="user.ifThisDev === 1"
                                class="mgl10"
                                style="color: #11b95c"
                            >(This Device)</span>
                        </div>
                        <div class="mgt8">
                            <span
                                style="color: rgb(161, 161, 161)"
                                class="fs12"
                            ></span>
                        </div>
                    </div>
                </div>

                <div class="flex column flexg1" style="height: 100%">
                    <!-- <div>{{ chat.ip }}</div> -->
                    <!-- msgs -->
                    <div
                        style="background-color: #f5f5f5; overflow: auto"
                        class="flex column flexg1"
                    >
                        <div
                            v-for="msg of chat.msgs"
                            :key="msg"
                            :class="`msgbox ${
                                msg.from === chat.ip ? 'other' : 'me'
                            }`"
                        >
                            {{ msg.text }}
                        </div>
                        <!-- <div class="msgbox other"> hello</div>

            <div class="msgbox me"> 最近過得怎麼樣最近過得怎麼樣最近過得怎麼樣最近過得怎麼樣最近過得怎麼樣最近過得怎麼樣最近過得怎麼樣最近過得怎麼樣最近過得怎麼樣最近過得怎麼樣最近過得怎麼樣最近過得怎麼樣最近過得怎麼樣近過得怎麼樣最近過得怎麼樣最近過得怎麼樣最近過得怎麼樣最近過得怎麼樣最近過得怎麼樣最近過得怎麼樣最近過得怎麼樣最近過得怎麼樣最近過得怎麼樣最近過得怎麼樣最近過得怎麼樣最近過得怎麼樣近過得怎麼樣最近過得怎麼樣最近過得怎麼樣最近過得怎麼樣最近過得怎麼樣最近過得怎麼樣最近過得怎麼樣最近過得怎麼樣最近過得怎麼樣最近過得怎麼樣最近過得怎麼樣最近過得怎麼樣最近過得怎麼樣近過得怎麼樣最近過得怎麼樣最近過得怎麼樣最近過得怎麼樣最近過得怎麼樣最近過得怎麼樣最近過得怎麼樣最近過得怎麼樣最近過得怎麼樣最近過得怎麼樣最近過得怎麼樣最近</div>
            <div class="msgbox me"> 你去哪裏玩了？</div> -->
                    </div>
                    <!-- input -->
                    <div class="">
                        <el-input
                            v-model="sendMsg"
                            resize="none"
                            :disabled="!inputActive"
                            :autosize="{ minRows: 4, maxRows: 6 }"
                            type="textarea"
                            style="border-radius: 0 !important"
                            @input="ifSendActive"
                            @keyup.enter.native="enterSend"
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
import { TextMsgContent } from '@/model/textMsgContent'
import { Chat, Msg } from '@/model/chat'
import { ProtoMsg } from '@/model/msg'
import { ArrayUtils } from '@/utils'
import { Message } from 'element-ui'
import { deflate } from 'zlib'
export default {
    data() {
        return {
            sendMsg: '',
            onlineUsers: [],
            chats: [],
            chat: {},
            input: '',
            sendActive: false,
            inputActive: false
        }
    },
    created() {
        console.log(config)
        this.socket = new WebSocket(config.websocketApiPrefix)
        this.socket.onopen = () => {
            console.log('connect to server')
        }
        this.socket.onerror = () => {}
        this.socket.onclose = () => {
            console.log('connect to server')
        }
        this.socket.onmessage = (event) => {
            const dataJson = event.data
            console.log(dataJson)
            const data = JSON.parse(dataJson)
            console.log(data)
            switch (data.business) {
                case 'GetOnlineUsersResponse': {
                    const userList = []
                    const onlineUsers = data.content
                    if (onlineUsers && onlineUsers.length > 0) {
                        for (const onlineUser of onlineUsers) {
                            const user = new User(
                                onlineUser.id,
                                onlineUser.ip,
                                onlineUser.name,
                                onlineUser.ifThisDev
                            )
                            userList.push(user)
                        }
                    }
                    console.log(userList)
                    this.onlineUsers = userList
                    this.chats = []
                    for (const user of userList) {
                        this.chats.push(new Chat(user, []))
                    }

                    if (this.chats.length > 0) {
                        this.chat = this.chats[0]
                    }
                    break
                }

                case 'UserOffline': {
                    const ip = data.content.ip
                    ArrayUtils.remove(
                        this.onlineUsers,
                        (user) => user.ip === ip
                    )
                    if (ip === this.chat.ip) {
                        this.chat = {}
                    }
                    break
                }

                case 'UserOnline': {
                    const ip = data.content.ip
                    if (!this.onlineUsers.find((user) => user.ip === ip)) {
                        this.onlineUsers.push(new User(null, ip, ''))
                    }
                    break
                }

                case 'SendText': {
                    const textContent = data.content
                    console.log('receive: ', textContent)
                    const text = textContent.text
                    const msg = new Msg(
                        text,
                        data.from,
                        data.target,
                        Date.now()
                    )
                    console.log(msg)
                    const chat = this.chats.find((chat) => chat.ip === msg.from)
                    console.log(chat)
                    if (chat != null) {
                        if (chat.ifThisDev === 0) {
                            chat.addMsg(msg)
                        }
                    }

                    // else {
                    //     const newChat = new Chat(data.from, [msg], data.ifThisDev)
                    //     this.chats.push(newChat)
                    // }

                    break
                }
            }
        }
    },
    methods: {
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
        enterSend() {
            this.send()
        },
        ifSendActive() {
            const text = this.sendMsg
            if (text != undefined && text !== '') {
                this.sendActive = true
            } else {
                this.sendActive = false
            }
        },
        send() {
            const target = this.chat.ip
            let text = this.sendMsg
            if (text == undefined || text === '') {
                return
            }

            text = text.trim()
            const msg = new ProtoMsg(
                null,
                'SendText',
                null,
                target,
                new TextMsgContent(text)
            )
            console.log(msg)
            this.chat.msgs.push(new Msg(text, null, target, Date.now()))
            this.sendMsg = ''

            if (!this.chat.ifThisDev) {
                this.socket.send(JSON.stringify(msg))
            }
            // if (this.chat.ip === target) {
            // }
        },

        pickChat(user) {
            this.inputActive = true
            const chat = this.chats.find((chat) => chat.ip === user.ip)
            // if (!chat) {
            //     chat = new Chat(user.ip, [], user.ifThisDev)
            //     chat.ip = user.ip
            //     this.chats.push(chat)
            // }
            this.chat = chat
            this.changeUserItemDivBackgroundColor2Focus(user)
        },
        changeUserItemDivBackgroundColor2Focus(user) {
            for (const onlineUser of this.onlineUsers) {
                const ip = onlineUser.ip
                const divId = `useritem-${ip}`
                const useritemDiv = document.getElementById(divId)
                if (user.ip === ip) {
                    useritemDiv.style.backgroundColor =
                        this.useritemDivBackgroundColor('focus')
                } else {
                    useritemDiv.style.backgroundColor = ''
                }
            }
        }
    }
}
</script>

<style lang="scss" scoped>
@import '~@/styles/common-style.scss';

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
}

// .useritem picked {
//     background-color: #c5c5c5;
// }

.msgbox {
    padding: 10px;
    max-width: 70%;
    margin: 10px;
    border-radius: 5px;
    word-break: break-all;
}

.msgbox.me {
    background-color: #89d961;
    align-self: end;
}

.msgbox.other {
    background-color: #ebebeb;
    align-self: start;
}
</style>

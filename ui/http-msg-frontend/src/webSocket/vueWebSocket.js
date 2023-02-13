import * as config from '@/config/config'
import { Chat, Msg } from '@/model/chat'
import { ProtoMsg } from '@/model/msg'
import { TextMsgContent } from '@/model/textMsgContent'
import { User } from '@/model/user'
import store from '@/store'
import { storeOperations } from '@/store/storeOperations'

export class VueWebSocket {
    vue
    reconnectTimeout
    socket
    // storeOperations

    constructor(vue) {
        this.vue = vue
        // this.storeOperations = new StoreOperations()
        this.connect()
    }
    userIsMe(user, me) {
        return user.id === me.id
    }

    connect() {
        this.socket = new WebSocket(config.websocketApiPrefix)
        this.socket.onopen = () => {
            this.reconnectTimeout && clearTimeout(this.reconnectTimeout)
            this.vue.events.push('open socket')
            console.log('connect to server')
        }
        this.socket.onerror = () => {
            this.vue.events.push('error socket')
        }
        this.socket.onclose = () => {
            this.vue.events.push('close socket')
            console.log('connect to server')
            this.reconnect()
        }
        this.socket.onmessage = (event) => {
            const dataJson = event.data
            console.log(dataJson)
            const protoMsg = JSON.parse(dataJson)
            console.log(protoMsg)
            switch (protoMsg.business) {
                case 'GetOnlineUsersResponse': {
                    const respOnlineUsersResp = protoMsg.content
                    const respOnlineUsers = respOnlineUsersResp.onlineUsers
                    const me = respOnlineUsersResp.you
                    me.ifThisDev = true
                    storeOperations.rememberMe(me)
                    console.log('me', me)
                    console.log('online users', respOnlineUsers)
                    if (respOnlineUsers && respOnlineUsers.length > 0) {
                        for (const onlineUser of respOnlineUsers) {
                            var ifThisDev = false
                            if (this.userIsMe(onlineUser, me)) {
                                ifThisDev = true
                            }
                            const user = new User(
                                onlineUser.id,
                                onlineUser.ip,
                                onlineUser.name,
                                ifThisDev
                            )
                            user.online = true
                            if (!storeOperations.findOnlineUserByIp(onlineUser.id)) {
                                // debugger
                                storeOperations.addOnlineUsers(user)
                            }
                        }
                    }

                    this.createChatsForEachOnlineUser(
                        store.getters.chats,
                        store.getters.onlineUsers
                    )

                    break
                }

                case 'UserOffline': {
                    const offlineUserIp = protoMsg.content.ip
                    const offlineUserId = protoMsg.content.id

                    let user = new User()
                    user = storeOperations.findOnlineUserById(offlineUserId)
                    storeOperations.userOffline(user)

                    // if (user) {
                    //     user.online = false
                    // }

                    break
                }

                case 'UserOnline': {
                    const onlineUserIp = protoMsg.content.ip
                    const onlineUserId = protoMsg.content.id
                    const onlineUserName = protoMsg.content.name
                    let userExists = User.Type()
                    userExists = storeOperations.findOnlineUserById(onlineUserId)
                    if (!userExists) {
                        const newUser = new User(onlineUserId, onlineUserIp, onlineUserName)
                        newUser.online = true
                        storeOperations.addOnlineUsers(newUser)

                        storeOperations.addNewChat(new Chat(newUser, []))
                    } else {
                        userExists.online = true
                    }
                    break
                }

                case 'SendText': {
                    const receivedText = protoMsg.content.text
                    console.log('receive: ', receivedText)
                    const msg = new Msg(
                        receivedText,
                        protoMsg.from,
                        protoMsg.target,
                        Date.now()
                    )
                    msg.successSent = true

                    const chat = storeOperations.findChatByIp(msg.from)

                    if (chat != null) {
                        if (chat.ifThisDev === 0) {
                            chat.addMsg(msg)
                            if (store.getters.curChat === chat) {
                                chat.hasNewMsgs = false
                            } else {
                                chat.hasNewMsgs = true
                            }

                            this.vue.lastMsgScrollIntoView()
                        }
                    }

                    break
                }
            }
        }
    }

    createChatsForEachOnlineUser(chats, onlineUsers) {
        for (const user of onlineUsers) {
            if (!chats.find((chat) => chat.user.id === onlineUsers.id)) {
                chats.push(new Chat(user, []))
            }
        }
    }

    reconnect() {
        this.vue.events.push('reconnect')
        this.connect()
        this.reconnectTimeout && clearTimeout(this.reconnectTimeout)
        this.reconnectTimeout = setTimeout(() => {
            this.reconnect()
        }, 1000)
    }

    textAllWhite(userInputText) {
        var pattern = /^[\n\r\s]+$/

        const allWhiteChar = pattern.test(userInputText)

        if (userInputText == undefined || allWhiteChar) {
            return false
        }

        return true
    }

    cleanUserInput() {
        this.vue.userInputMsg = ''
    }

    sendMsg() {
        const targetUserId = storeOperations.getCurChatUserId()
        let userInputText = this.vue.getUserInputText()

        this.cleanUserInput()

        if (!this.textAllWhite(userInputText)) {
            return
        }

        userInputText = userInputText.trim()
        const protoMsg = new ProtoMsg(
            null,
            'SendText',
            storeOperations.getMe().id,
            targetUserId,
            new TextMsgContent(userInputText)
        )
        const msg = new Msg(userInputText, storeOperations.getMe().id, targetUserId, Date.now())

        store.getters.curChat.addMsg(msg)

        const chatUser = storeOperations.findOnlineUserById(storeOperations.getCurChatUserId())
        if (chatUser && chatUser.online) {
            msg.successSent = true
        } else {
            msg.successSent = false
        }

        this.vue.lastMsgScrollIntoView()

        this.doSendMsgIfCurChatIsNotThisDev(protoMsg)
    }

    doSendMsgIfCurChatIsNotThisDev(protoMsg) {
        if (!storeOperations.curChatIsThisDevice()) {
            this.socket.send(JSON.stringify(protoMsg))
        }
    }
}

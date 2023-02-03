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
                    const respOnlineUsers = protoMsg.content
                    if (respOnlineUsers && respOnlineUsers.length > 0) {
                        for (const onlineUser of respOnlineUsers) {
                            const user = new User(
                                onlineUser.id,
                                onlineUser.ip,
                                onlineUser.name,
                                onlineUser.ifThisDev
                            )
                            user.online = true
                            if (!storeOperations.findOnlineUserByIp(onlineUser.ip)) {
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

                    let user = new User()
                    user = storeOperations.findOnlineUserByIp(offlineUserIp)

                    if (user) {
                        user.online = false
                    }

                    break
                }

                case 'UserOnline': {
                    const onlineUserIp = protoMsg.content.ip
                    let userExists = User.Type()
                    userExists = storeOperations.findOnlineUserByIp(onlineUserIp)
                    if (!userExists) {
                        const newUser = new User(null, onlineUserIp, '')
                        newUser.online = true
                        storeOperations.addOnlineUsers(newUser)

                        storeOperations.addNewChat(new Chat(onlineUserIp, [], 0))
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
            if (!chats.find((chat) => chat.ip === onlineUsers.ip)) {
                chats.push(new Chat(user.ip, [], user.ifThisDev))
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
        const targetIp = storeOperations.getCurChatIp()
        let userInputText = this.vue.getUserInputText()

        this.cleanUserInput()

        if (!this.textAllWhite(userInputText)) {
            return
        }

        userInputText = userInputText.trim()
        const protoMsg = new ProtoMsg(
            null,
            'SendText',
            null,
            targetIp,
            new TextMsgContent(userInputText)
        )
        const msg = new Msg(userInputText, null, targetIp, Date.now())

        store.getters.curChat.addMsg(msg)

        const chatUser = storeOperations.findOnlineUserByIp(storeOperations.getCurChatIp())
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

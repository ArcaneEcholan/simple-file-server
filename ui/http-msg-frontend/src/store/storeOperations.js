import store from '.'

export class StoreOperations {
    vuexStore
    constructor() {
        this.vuexStore = store
    }

    rememberMe(me) {
        this.vuexStore.state.chat.me = me
    }

    getMe() {
        return this.vuexStore.state.chat.me
    }

    findOnlineUserByIp(ip) {
        return this.vuexStore.getters.onlineUsers.find(
            (user) => user.ip === ip
        )
    }

    userOffline(user) {
        const onlineUsers = this.vuexStore.getters.onlineUsers

        const userIndex = onlineUsers.indexOf((onlineUser) => onlineUser.id === user.id)
        onlineUsers.splice(userIndex, 1)

        const chatIndex = this.vuexStore.getters.chats.indexOf(chat => chat.user.id === user.id)
        this.vuexStore.getters.chats.splice(chatIndex, 1)
    }

    findOnlineUserById(id) {
        return this.vuexStore.getters.onlineUsers.find(
            (user) => user.id === id
        )
    }

    addOnlineUsers(user) {
        this.vuexStore.getters.onlineUsers.push(user)
    }

    addNewChat(chat) {
        this.vuexStore.getters.chats.push(chat)
    }

    findChatByIp(ip) {
        return this.vuexStore.getters.chats.find(
            (chat) => chat.ip === ip
        )
    }

    findChatById(id) {
        return this.vuexStore.getters.chats.find(
            (chat) => chat.user.id === id
        )
    }

    addNewMsgToCurChat(msg) {
        this.vuexStore.getters.curChat.addMsg(msg)
    }

    getUserOfCurChat() {
        return this.findOnlineUserByIp(this.getCurChatIp())
    }

    getCurChatIp() {
        return this.vuexStore.getters.curChat.ip
    }

    getCurChatUserId() {
        return this.vuexStore.getters.curChat.user.id
    }

    curChatIsThisDevice() {
        return this.vuexStore.getters.curChat.ifThisDev
    }
}

export const storeOperations = new StoreOperations()

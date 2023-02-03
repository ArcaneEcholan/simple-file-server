import store from '.'

export class StoreOperations {
    vuexStore
    constructor() {
        this.vuexStore = store
    }

    findOnlineUserByIp(ip) {
        return this.vuexStore.getters.onlineUsers.find(
            (user) => user.ip === ip
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

    addNewMsgToCurChat(msg) {
        this.vuexStore.getters.curChat.addMsg(msg)
    }

    getUserOfCurChat() {
        return this.findOnlineUserByIp(this.getCurChatIp())
    }

    getCurChatIp() {
        return this.vuexStore.getters.curChat.ip
    }

    curChatIsThisDevice() {
        return this.vuexStore.getters.curChat.ifThisDev
    }
}

export const storeOperations = new StoreOperations()

import { uuidv4 } from '@/utils'

export class Chat {
    hasNewMsgs
    vueKeyBindId
    ip
    user
    userId
    msgs
    ifThisDev

    constructor(user, msgs) {
        this.hasNewMsgs = false
        this.vueKeyBindId = uuidv4()
        this.user = user
        this.msgs = msgs
    }

    addMsg(msg) {
        this.msgs.push(msg)
    }

    getLastMsg() {
        return this.msgs[this.msgs.length - 1]
    }

    hasMsgs() {
        return this.msgs && this.msgs.length > 0
    }
}

export class Msg {
    id
    vueKeyBindId
    text
    from
    target
    timestamp
    successSent

    constructor(text, from, target, timestamp) {
        this.id = uuidv4()
        this.vueKeyBindId = uuidv4()
        this.text = text
        this.from = from
        this.target = target
        this.timestamp = timestamp
    }
}

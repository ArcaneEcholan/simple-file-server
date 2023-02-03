export class User {
    id
    ip
    name
    ifThisDev
    online

    constructor(id, ip, name, ifThisDev) {
        this.id = id
        this.ip = ip
        this.name = name
        this.ifThisDev = ifThisDev
    }

    static Type() {
        return new User()
    }
}

export class QueryFileOption {
    key
    value

    constructor(key, value) {
        this.key = key;
        this.value = value
    }
}

export class QueryFileOptions {
    options
    constructor() {
        this.options = []
    }

    putIfAbsent(newOpt) {
        // find same key item
        let sameKeyItem = this.options.find(opt => {
            return opt.key == newOpt.key
        })

        if (sameKeyItem) {
            return
        }

        // absent
        this.options.push(newOpt)
    }

    removeIfPresent(optToDelete) {
        // remove by filtering the opposite
        this.options = this.options.filter(opt => opt.key !== optToDelete.key)
    }
}

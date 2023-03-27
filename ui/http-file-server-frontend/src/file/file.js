import * as FILE_CONSTS from '@/file/consts'

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

    get(key) {
        let findone = this.options.find(item => item.key === key)
        if (findone) {
            return findone.value
        }
        return null
    }

    resolveQuery(vueRouterQuery) {
        // export const HideHiddenFile = 'HideHiddenFile'
        // export const SortFilename = 'SortFilename'
        // export const SortLastModifiedTime = 'SortLastModifiedTime'
        // export const SortFilesize = 'SortFilesize'
        // export const SearchFilename = 'SearchFilename'
        let options = []
        FILE_CONSTS.QueryFileOpts.forEach(opt => {
            if (vueRouterQuery[opt] != null) {
                options.push(new QueryFileOption(opt, vueRouterQuery[opt]))
            }
        })
        this.options = options
    }

    toQueryString() {
        // export const HideHiddenFile = 'HideHiddenFile'
        // export const SortFilename = 'SortFilename'
        // export const SortLastModifiedTime = 'SortLastModifiedTime'
        // export const SortFilesize = 'SortFilesize'
        // export const SearchFilename = 'SearchFilename'
        let opts = this.options
        let queryString = ''
        opts.forEach(opt => {
            let key = opt.key
            let value = opt.value
            if (FILE_CONSTS.QueryFileOpts.includes(key)) {
                queryString += `${key}=${value}&`
            }
        })
        return queryString
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

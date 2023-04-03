export const FILE = 'FILE'
export const FOLDER = 'FOLDER'

export const ShowHiddenFile = 'ShowHiddenFiles'
export const SortFilename = 'SortFilename'
export const SortLastModifiedTime = 'SortLastModifiedTime'
export const SortFilesize = 'SortFilesize'
export const SearchFilename = 'SearchFilename'

export const QueryFileOpts = [
    ShowHiddenFile,
    SortFilename,
    SortLastModifiedTime,
    SortFilesize,
    SearchFilename,
]

export const DESC = 'DESC'
export const ASC = 'ASC'
export const SORT_ARROW_UP = 'up'
export const SORT_ARROW_DOWN = 'down'

let MAPPING_ARROW_SORTMETHOD = {}
MAPPING_ARROW_SORTMETHOD[SORT_ARROW_UP] = ASC
MAPPING_ARROW_SORTMETHOD[SORT_ARROW_DOWN] = DESC

let MAPPING_SORTMETHOD_ARROW = {}
MAPPING_SORTMETHOD_ARROW[ASC] = SORT_ARROW_UP
MAPPING_SORTMETHOD_ARROW[DESC] = SORT_ARROW_DOWN

export { MAPPING_ARROW_SORTMETHOD, MAPPING_SORTMETHOD_ARROW }

export const TRUE = 'TRUE'
export const FALSE = 'FALSE'

import { ArrayUtils } from '@/utils'
import { Message } from 'element-ui'
import { isPrimitiveValue } from '@/utils'

/**
 * 把所有 options 中的属性复制到 this 中，this 对象需要通过call传入
 * @param {object} options
 */
export function fetch_options_to_this(options) {
    var keys = Object.keys(options)
    keys.forEach((key) => {
        var value = options[key]
        this[key] = value
    })
}

/**
 * 检查指定的对象中是否包含或者不包含某个指定的属性
 * @param {string} classname 一个tag，可有可无，表示options对象所属的对象
 * @param {object} options 目标对象
 * @param {string} target_key 目标属性名称
 * @param {string} key_type 'function' 或 'var'
 * @param {boolean} present 应该包含还是不应该
 */
function check_option_key(classname, options, target_key, key_type, present) {
    var optionKeys = Object.keys(options)
    const key = ArrayUtils.getFirst(optionKeys, (key) => key === target_key)

    let present_correct = false
    if (present === true) {
        present_correct = key != undefined
    } else {
        present_correct = key == undefined
    }

    if (!present_correct) {
        const msg = `${classname}对象${present ? '缺少' : '不能覆盖'}${target_key}${key_type === 'function' ? '函数' : '变量'}`
        throw new Error(msg)
    }

    if (present) {
        let key_type_correct = false
        if (key_type === 'function') {
            key_type_correct = typeof options[key] === 'function'
        } else if (key_type === 'var') {
            key_type_correct = typeof options[key] !== 'function'
        }

        if (!key_type_correct) {
            const msg = `ElDialog对象${present ? '缺少' : '不能覆盖'
            }${target_key}${key_type === 'function' ? '函数' : '变量'}`
            throw new Error(msg)
        }
    }
}

/**
 * 把options对象中的所有类型为OopElComponent的属性添加一个名为 parent 的属性，指向 parent 对象
 * @param {object} options 去该对象中查找所有OopElComponent的属性
 * @param {object} parent parent 属性指向的对象
 */
function build_parent_ref(options, parent) {
    var keys = Object.keys(options)
    keys.map((key) => {
        var value = options[key]
        return value
    })
        .filter((option) => {
            return option instanceof OopElComponent
        })
        .forEach((option) => {
            option.parent = parent
        })
}

class OopElComponent { }

function uuidv4() {
    return ([1e7] + -1e3 + -4e3 + -8e3 + -1e11).replace(/[018]/g, (c) =>
        (
            c ^
            (crypto.getRandomValues(new Uint8Array(1))[0] & (15 >> (c / 4)))
        ).toString(16)
    )
}

/**
 *
 * 需要重写：
 *
 * reset_data(data) 用于重置该dialog下存储的变量
 *
 * before_open(data) 用于在打开dialog前做一些事情，比如获取数据等。做完之后手动调用
 *                   this.do_open() 方法打开dialog。此处设计的不是很合理，this.do_open()
 *                   不应该被手动调用，太不方便
 *
 * 可以重写：
 *
 * do_commit() 点击确认后执行的操作，例如，一些网络请求，如果不重写，则默认关闭对话框
 *
 * enable_commit() 点击提交后，执行提交前的一些操作，返回值是boolean；如果返回 true,正常
 * 提交，false 则阻止提交并给出提示
 *
 * before_close() 可以在关闭对话框前做一些事情
 *
 * 使用：
 * this.open(data, $event) 打开dialog
 *
 * this.do_open() 在before_open中调用this.do_open() 以打开dialog
 */
export class OopElDialogModel extends OopElComponent {
    constructor(title, options) {
        super()
        this.title = title
        this.visible = false

        build_parent_ref(options, this)

        check_option_key(
            'OopElDialogModel',
            options,
            'reset_data',
            'function',
            true
        )

        check_option_key(
            'OopElDialogModel',
            options,
            'before_open',
            'function',
            true
        )

        check_option_key(
            'OopElDialogModel',
            options,
            'open',
            'function',
            false
        )
        check_option_key(
            'OopElDialogModel',
            options,
            'commit',
            'function',
            false
        )
        check_option_key(
            'OopElDialogModel',
            options,
            'do_open',
            'function',
            false
        )
        check_option_key(
            'OopElDialogModel',
            options,
            'close',
            'function',
            false
        )

        fetch_options_to_this.call(this, options)
        this.reset_data()

        this.key_down_listener_count = 0
        this.event_map = new Map()
        this.id = uuidv4()
    }

    open(data, $event) {
    // 如果是通过点击按钮调用的此方法，那么在打开此dialog后，需要将焦点从按钮上移除
        if ($event != undefined) {
            const target = $event.currentTarget
            target.blur()
        }

        // 绑定 enter 输入事件
        const enter_commmit_event = this.enter_down()
        window.addEventListener('keydown', enter_commmit_event)
        // 保存事件，方便dialog关闭时清除事件
        this.event_map.set('keydown', enter_commmit_event)

        this.before_open(data)
    }

    commit(data) {
        var enable_commit = this.enable_commit(data)
        if (enable_commit === true) {
            this.do_commit(data)
        } else {
            Message.warning('请将表单填写完整后提交')
        }
    }

    do_commit() { }

    enable_commit() {
        return true
    }

    /**
     * 如果dialog中嵌套了一层dialog，那么嵌套打开这两个dialog时，每个dialog上都会有一个
     * enter 事件，那么此时按下 enter 会出发所有的事件，这个方法可以确定，该dialog是否是最
     * 上层的，如果是，可以触发 enter 事件
     * @returns 当且仅当该 dialog 在最上层时返回 true
     */
    topmost() {
        let topmost_id
        let topmost_value = 0

        // 拿到所有的dialog
        const el_dialog_wrappers =
            document.getElementsByClassName('el-dialog__wrapper')

        // 遍历 所有 dialog，根据 z-index 找最大的，记录元素id
        for (let i = 0; i < el_dialog_wrappers.length; i++) {
            const dialog_wrapper = el_dialog_wrappers[i]
            const display = $(dialog_wrapper).css('display')
            if (display !== 'none') {
                const zindex = $(dialog_wrapper).css('z-index')
                if (zindex > topmost_value) {
                    topmost_value = zindex
                    topmost_id = $(dialog_wrapper).attr('id')
                }
            }
        }

        return topmost_id === this.id
    }

    enter_down() {
        return (e) => {
            // 回车则执行登录方法 enter键的ASCII是13
            if (e.keyCode === 13) {
                // debugger;
                if (this.visible) {
                    if (this.topmost()) {
                        this.commit()
                    }
                }
            }
            e.stopPropagation()
        }
    }

    before_open(data) {
        this.do_open()
    }

    do_open() {
        this.visible = true
    }

    // 重置data为默认的结构，可以复写这个方法
    reset_data(data) {
        this.data = {}
    }

    // 关闭表单清除信息
    close() {
    // 清除该表单的 enter 事件
        window.removeEventListener('keydown', this.event_map.get('keydown'))

        this.before_close()
        this.visible = false
        this.reset_data()

        // 递归清除数据
        recursive_reset_data(this)
    }

    before_close() { }
}
/**
 * 递归调用 obj 的reset_data 方法，包括obj中的属性
 * @param {object} obj 目标对象
 */
function recursive_reset_data(obj) {
    var keys = Object.keys(obj)
    keys.forEach((attr_name, idx) => {
    // 不遍历 parent，防止无止境递归
        if (attr_name === 'parent') {
            return
        }

        // 拿到属性值
        const attr_value = obj[attr_name]

        // 递归终止条件：属性是基础类型
        if (isPrimitiveValue(attr_value)) {
            return
        }
        // 如果该对象有 reset_data 函数，调用
        if (
            attr_value['reset_data'] !== undefined &&
            typeof attr_value['reset_data'] === 'function'
        ) {
            attr_value['reset_data']()
            return
        }
        // 递归
        recursive_reset_data(attr_value)
    })
}

/**
 *
 * 使用：
 *
 * this.get_selected() 获取选中的值（字符串）
 * this.is_loading() 判断是否在loading
 * this.start_loading() 和 this.stope_loading() 用于开始和停止loading
 */
export class OopElSelectModel extends OopElComponent {
    constructor(options) {
        super()
        build_parent_ref(options, this)
        fetch_options_to_this.call(this, options)
        this.reset_data()
        this.value = ''
        this.id = uuidv4()
        this.loading = false
    }

    get_selected() {
        return this.value
    }
    is_loading() {
        return this.loading
    }
    stop_loading() {
        this.loading = false
    }
    start_loading() {
        this.loading = true
    }

    reset_data() {
        this.value = ''
    }
}

/**
 * this.fetch_data(data) 方法开始拉取数据；
 * this.fetch_over() 表示拉取结束，需要手动调用；
 *
 * 需要重写方法：
 * do_fetch_data(data), 参数 data 是调用 this.fetch_data(data) 的参数
 */
export class OopElTableModel extends OopElComponent {
    constructor(title, options) {
        super()
        this.data = []
        this.title = title
        this.loading = false
        this.cols = [
            { prop: 'col1_prop_name', label: 'col1_label_name' },
            { prop: 'col2_prop_name', label: 'col2_label_name' }
        ]

        build_parent_ref(options, this)

        check_option_key('OopElTableModel', options, 'cols', 'var', true)
        check_option_key(
            'OopElTableModel',
            options,
            'do_fetch_data',
            'function',
            true
        )
        check_option_key(
            'OopElTableModel',
            options,
            'fetch_data',
            'function',
            false
        )
        check_option_key(
            'OopElTableModel',
            options,
            'fetch_over',
            'function',
            false
        )
        fetch_options_to_this.call(this, options)
    }

    fetch_data(data) {
        console.log(`表格 ${this.title} 开始拉取数据`)
        this.loading = true
        this.do_fetch_data(data)
    }

    fetch_over() {
        this.loading = false
    }

    start_loading() {
        this.loading = true
    }

    do_fetch_data(data) {
        this.data = []
    }
}

/**
 *
 * 使用：
 * this.get_selected()：在选择结果后，使用该方法获取选择行的结果对象
 *
 * 需要重写：
 *
 * fetch_suggestions(query_string, cb)：query_string 是输入框中的查询字符串，cb是内置的
 * 函数，用于带回查询值
 *
 * 可以重写：
 *
 * do_select(row)：在选择一个结果后，可以在该方法中拿到该结果
 *
 */
export class OopElAutocompletionModel extends OopElComponent {
    constructor(placeholder, options) {
        super()
        build_parent_ref(options, this)

        check_option_key(
            'OopElAutocompletionModel',
            options,
            'fetch_suggestions',
            'function',
            true
        )

        fetch_options_to_this.call(this, options)
        this.placeholder = placeholder
        this.reset_data()
        this.selected = {}
        this.value = ''
    }

    get_selected() {
        return this.selected
    }

    fetch_suggestions(query_string, cb) {
        cb(['value1', 'value2', 'value3'])
    }

    reset_data() {
        this.selected = {}
        this.value = ''
    }

    do_select(row) {
        console.log('select row: ', row)
    }
}

/**
 * 使用：
 * this.do_page(data) 实现翻页
 * this.get_pageno() 获取当前页数
 * this.get_pagesize() 获取当前页大小
 *
 * 需要重写：
 * do_page(data)
 */
export class OopElPaginationModel extends OopElComponent {
    constructor(pageno, pagesize, options) {
        super()
        this.total = 0
        this.pageno = pageno
        this.pagesize = pagesize

        build_parent_ref(options, this)

        check_option_key(
            'OopElPaginationModel',
            options,
            'do_page',
            'function',
            true
        )

        fetch_options_to_this.call(this, options)
    }

    get_pageno() {
        return this.pageno
    }

    get_pagesize() {
        return this.pagesize
    }

    // 用户覆盖该方法，实现翻页
    do_page(data) { }
}

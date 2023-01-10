# OOP Components Docs

## ElDialogView

```html
<el-dialog-view :model="update_dialog" width="50%">
    <template #default="scope">
        <!-- scope.model 获取 dialog 的oop对象 -->
        <!-- scope.data 获取 dialog 的oop对象中的data部分 -->
        <!-- ...... -->
    </template>

    <!-- footer 元素可以省略，默认有提交按钮，也可以像这样重写定制 -->
    <template #footer="scope">
        <!-- scope.model 获取 dialog 的oop对象 -->
        <!-- scope.data 获取 dialog 的oop对象中的data部分 -->
        <el-button @click="scope.model.close()">取消</el-button>
        <el-button @click="scope.model.commit()" type="primary">
            提交
        </el-button>
    </template>
</el-dialog-view>

<script>
  export default {
    data() {
      return {
        update_dialog: new OopElDialogModel('修改xx对话框', {

                ///////////////// 必须方法 /////////////////

                // 必须有该方法，创建和关闭对话框时被调用
                reset_data() {
                    this.data = {
                        counter: 0,
                    };
                },
                // 必须有该方法，打开对话框前被调用
                // data参数在open被调用时被传递
                before_open(data) {
                    // do some extra work
                    this.do_open();
                },

                ///////////////// 可选方法 /////////////////

                // 默认返回true
                enable_commit() {
                    if(...) {
                        return true
                    }
                    return false;
                },
                
                // 点击 提交按钮 后执行
                do_commit() {
                    // request_net_work();
                    this.close();
                },
            }),
      };
    }
    
  };
</script>
```


## ElTableView


```html
<el-table-view :model="table">
    <template slot-scope="scope">
        <!-- scope.col.prop 获取当前列属性名 -->
        <!-- scope.row 获取当前行对象 -->
        <!-- scope.row[scope.col.prop] 获取当前行，当前列的值 -->

        <div v-if="scope.col.prop == undefined">
            <!-- ... -->
            <!--  -->
        </div>

        <!-- 定制指定的列或行 -->
        <div v-else-if="scope.col.prop === 'name'">
            <!-- ... -->
        </div>

        <div v-else>
            {{ scope.row[scope.col.prop] }}
        </div>
    </template>
</el-table-view>
<script>
  export default {
    created() {
        // 拉取数据
        this.table.fetch_data();
    },
    data() {
      return {
        table: new OopElTableModel("权限列表", {
            cols: [
                {
                    prop: "id",
                    label: "ID",
                },
                {
                    prop: "name",
                    label: "名称",
                },

                {
                    prop: "permission_name",
                    label: "权限",
                },

                {
                    prop: undefined,
                    label: "操作",
                },
            ],

            ///////////////// 必须方法 /////////////////

            // 拉取数据操作写在这里，调用后列表自动进入loading状态
            do_fetch_data(data) {
                get_art_templates(this.parent.data.art_id).then((resp) => {
                    this.data = resp.data;
                    for (let i = 0; i < resp.data.length; i++) {
                    const template = resp.data[i];
                    template.permission_name = template.artifact_share_permission.name;
                    }
                    // 调用该方法结束loading状态
                    this.fetch_over();
                });
            },
        }),
      };
    }
    
  };
</script>
```



## ElAutocompleteView

```html
<el-autocomplete-view 
    :model="auto_comp" 
    :select_prop="'name'"
    :trigger-on-focus="false">
    <template #default="scope">
        <!-- 在这里定制结果行 -->
        <!-- scope.row 获取每行结果对象 -->
        <span style="float: left">
            {{
                scope.row.owner == undefined
                ? ""
                : scope.row.owner.samAccountName
            }}
        </span>
        <span style="float: right; color: #8492a6; font-size: 13px">
            {{ scope.row.name }}
        </span>
    </template>
</el-autocomplete-view>
<script>
  export default {
    methods: {
        get_selected() {
            // 获取用户选择的值
            console.log(this.auto_comp.get_selected())
        }
    },
    data() {
      return {
        auto_comp: new OopElAutocompletionModel("placeholder", {

          ///////////////// 必须方法 /////////////////
          
          // 自动拉取数据操作写在这里  
          fetch_suggestions(query_string, cb) {
            request_server(query_string).then((resp) => {
              // page_data 为匹配的模板列表
              var result_list = resp.data;
              cb(result_list);
            });
          },


          ///////////////// 可选方法 /////////////////
          do_select(row) {
            console.log('select row: ', row);
          }
        }),
      };
    }
    
  };
</script>
```

## ElSelectView

```html
 <el-select-view 
    :placeholder="'请选择权限'" 
    :model="perm_select">
    <el-option v-for="item in perms" :key="item.id" 
        :label="item.name" 
        :value="item.id">
        <span style="color: #8492a6; font-size: 13px">
            {{ item.name }}
        </span>
    </el-option>
</el-select-view>
<script>
  export default {
    methods: {
        get_selected() {
            // 获取用户选择的值
            console.log(this.perm_select.get_selected())
        },
        // 加载动画
        start_loading() {
            this.perm_select.start_loading()
        }
        // 停止加载
        stop_loading() {
            if(this.perm_select.is_loading()) {
                this.perm_select.stop_loading()
            }
        }
    },
    data() {
        return {
            perm_select: new OopElSelectModel({}),
        };
    }
  };
</script>
```


## ElPaginationModel

```html
<el-pagination-view
    :model="pagination"
/>

<script>
  let vue;
  export default {
    created() {
        vue = this;
    },
    methods: {
        get_pageno() {
            console.log(this.pagination.get_pageno())
        },
        get_pagesize() {
            console.log(this.pagination.get_pagesize())
        },
    },
    data() {
        return {
            table: new OopElTable...(...),

            // 可以设置起始页数和每页大小
            pagination: new OopElPaginationModel(1, 10, {

                ///////////////// 必须方法 /////////////////

                // 点击触发分页后，自动调用此方法
                do_page() {
                    let page_params = {
                        pageSize: this.get_pagesize(),
                        pageNo: this.get_pageno(),
                    };
                    // 通常委托给 OopElTableModel 去拉取数据
                    vue.table.fetch_data(page_params);
                },
            }),
        };
    }
  };
</script>
```


## Component Tree

如果组件是嵌套的，可以在子组件中通过 this.parent 属性获取到父组件

```html
<script>
  export default {
    data() {
      return {
        insert_dialog: new OopElDialogModel("insert dialog", {
            auto_completion_input: new OopElAutocompletionModel("默认内容", {
                fetch_suggestions(query_string, cb) {
                    request().then((resp) => {
                        var resp_data = resp.data;
                        cb(resp_data);
                    });
                },
                do_select(row) {
                    // this.parent 指向 insert_dialog 组件
                    this.parent.data.resp_data = row;
                },
            }),
      };
    }
    
  };
</script>
```


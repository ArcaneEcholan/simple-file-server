<template>
    <div class=''>
        <el-card>
            <template #header>

            </template>
            <el-button @click="onClickAddUser">add user</el-button>
            <el-table :data="userList">
                <el-table-column label="id" prop="id"></el-table-column>
                <el-table-column label="username" prop="username"></el-table-column>
                <el-table-column label="accDir" prop="accDir">
                    <template #default="scope">
                        <el-input v-model="scope.row.accDir"></el-input>
                        <el-button @click="onClickSaveAccDir(scope.row)">save</el-button>
                    </template>
                </el-table-column>
            </el-table>
        </el-card>

        <el-dialog :visible.sync="addUserDialogVisible">
            <debug-view>
                <div>
                    username:{{ newUsername }}
                </div>
                <div>
                    password:{{ newPassword }}
                </div>

            </debug-view>
            username
            <el-input v-model="newUsername"></el-input>
            password
            <el-input v-model="newPassword"></el-input>
            <el-button @click="confirmAddUser">confirm</el-button>
        </el-dialog>
    </div>
</template>

<script lang='ts'>
import {Notification} from "element-ui";
import {Component, Vue} from 'vue-property-decorator';
import Client from "@/request/client";
import {get_token} from "@/ts/auth";
import DebugView from "@/components/DebugView.vue";

@Component({
    components: {DebugView}
})
export default class UserView extends Vue {
    userList: any[] = []

    newUsername: string | null = null
    newPassword: string | null = null
    addUserDialogVisible = false
    openAddUserDialog() {
        this.addUserDialogVisible = true
    }

    closeAddUserDialog() {
        this.addUserDialogVisible = false
    }

    onClickAddUser() {
        this.openAddUserDialog()
    }

    confirmAddUser() {
        let username = this.newUsername
        let password = this.newPassword
        Client.addUser(username!, password!, get_token()).then(resp => {
            let user = resp.data
            this.userList.push(user)
            Notification.success({
                title: 'success',
                message: 'add user success'
            })
            this.closeAddUserDialog()
        })
    }

    created() {
        Client.getUserList(get_token()).then(resp => {
            this.userList = resp.data
        })
    }

    onClickSaveAccDir(row: any) {

        let userId = row.id
        let directory = row.accDir

        Client.assignDirectoryTo(userId, directory, get_token()).then(resp => {
            Notification.success({
                title: 'success',
                message: 'assign directory success'
            })
        })
    }
}
</script>
<style lang='scss' scoped>
</style>

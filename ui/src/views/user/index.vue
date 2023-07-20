<template>
<div class=''>
    <el-card>
        <template #header>

        </template>
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
</div>
</template>

<script lang='ts'>
import {Notification} from "element-ui";
import { Component, Vue } from 'vue-property-decorator';
import Client from "@/request/client";
import {get_token} from "@/ts/auth";
@Component({})
export default class UserView extends Vue {
    userList: any[] = []

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

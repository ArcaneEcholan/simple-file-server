/**
 * This module will:
 *
 * 1. Create an initialized axios instance
 *
 * 2. Add request interceptor to log request info
 *
 * 3. Add response interceptor to handle some global response codes
 */
import axios from 'axios';
import {Message, MessageBox, Notification} from 'element-ui';
import {PageLocation} from '@/ts/dynamicLocation';
import {SUCCESS,} from '@/ts/GlobalHandledResponseCode';
import store from "@/store";
import {USER_LOGOUT} from "@/store/modules/user";

// create an axios instance
const service = axios.create({
    baseURL: new PageLocation().baseURL, // url = base url + request url
    // withCredentials: true, // send cookies when cross-domain requests
    timeout: 0, // request timeout
});

// request interceptor
service.interceptors.request.use(
    (config) => {
        let logtag = `Network Request: =====> ${config.url}\n`;
        console.log(logtag, config);
        return config;
    },
    (error) => {
        // do something with request error
        console.log(error); // for debug
        return Promise.reject(error);
    },
);

let tokenProblem = () => {
    MessageBox.confirm(
        'Your login credential is invalid, please login again',
        'Warning',
        {
            confirmButtonText: 'OK',
            type: 'warning',
        },
    ).then(() => {
        store.commit(USER_LOGOUT);
    });
};

function logResponse(httpResponse) {
    const res = httpResponse.data;
    let relativeUrl = httpResponse.config.url!.substring(
        httpResponse.config.baseURL!.length,
    );
    let logtag = `Network Response: <==== ${relativeUrl}\n`;
    console.log(logtag, res);
}

// response interceptor
service.interceptors.response.use(
    /**
     * If you want to get http information such as headers or status
     * Please return  response => response
     */

    /**
     * Determine the request status by custom code
     * Here is just an example
     * You can also judge the status by HTTP Status Code
     */
    (response) => {
        logResponse(response)

        const res = response.data;
        let code = res.code;
        let msg = res.msg

        // success
        if (code === SUCCESS) {
            return res;
        }

        // if blob, return the blob to specific request "then" handler for downloading
        if (res.code == null) {
            return response;
        }

        if (code === 'TOKEN_INVALID' || code === 'TOKEN_EXPIRED') {
            tokenProblem();
        } else {
            Message.error(code + ":" + msg);
        }
        return Promise.reject(res);
    },
    (error) => {
        Message({
            message: `无法连接服务器(${error.message})`,
            type: 'error',
            duration: 2 * 1000,
        });
        return Promise.reject();
    },
);

export default service;


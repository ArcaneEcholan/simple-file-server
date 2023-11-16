import { MessageBox, Notification } from 'element-ui';
import store from '@/store/index';
import router from '@/router/index';
import { USER_LOGOUT } from '@/store/modules/user';
class ErrorCode {
    static SUCCESS = 'SUCCESS';
}

export const SUCCESS = 'SUCCESS';
export const TOKEN_EXPIRED = 'TOKEN_EXPIRED';
export const TOKEN_INVALID = 'TOKEN_INVALID';
export const FILE_SYSTEM_ERROR = 'FILE_SYSTEM_ERROR';

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

class GlobalHandledRespCode {
    codeHandlerMapping: any = {};

    constructor() {
        this.codeHandlerMapping = {
            TOKEN_INVALID: tokenProblem,
            TOKEN_EXPIRED: tokenProblem,
            TOKEN_MISSING: (code: string, msg: string) => {
                Notification.error('OTHER ERROR');
            },
            FILE_SYSTEM_ERROR: (code: string, msg: string) => {
                Notification.error(msg);
            },
            ROOT_PATH_CONFIG_ERROR: (code: string, msg: string) => {
                Notification.error(`${code}: ${msg}`);
            }
        };
    }

    handle(code: string, msg: string) {
        // if (this.codeHandlerMapping[code] != null) {
        //     this.codeHandlerMapping[code](code, msg);
        // }
        // else {
        //     Notification.error(code);
        // }
    }

    isGlobalHandled(code: string): boolean {
        return this.codeHandlerMapping[code] != null;
    }
}

export {ErrorCode};

export default new GlobalHandledRespCode();

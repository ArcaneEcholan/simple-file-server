import {Notification} from 'element-ui';

class ErrorCode {
    static SUCCESS = 'SUCCESS';
}

export const SUCCESS = 'SUCCESS';
export const FILE_SYSTEM_ERROR = 'FILE_SYSTEM_ERROR';

class GlobalHandledRespCode {
    codeHandlerMapping: any = {};

    constructor() {
        this.codeHandlerMapping = {
            TOKEN_MISSING: (code: string, msg: string) => {
                Notification.error('OTHER ERROR');
            },
            SERVER_ERROR: (code: string, msg: string) => {
                Notification.error('SERVER ERROR');
            },
            FILE_SYSTEM_ERROR: (code: string, msg: string) => {
                Notification.error(msg);
            }
        };
    }

    handle(code: string, msg: string) {
        if (this.codeHandlerMapping[code] != null) {
            this.codeHandlerMapping[code](code, msg);
        } else {
            Notification.error(code);
        }
    }

    isGlobalHandled(code: string): boolean {
        return this.codeHandlerMapping[code] != null;
    }
}

export {ErrorCode};

export default new GlobalHandledRespCode();

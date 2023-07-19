
// permission
import {ROUTE_PATHS} from "@/ts/consts/routerPathConstants";

export const USER_MANAGEMENT = 'USER_MANAGEMENT';
export const LOG_AUDIT = 'LOG_AUDIT';
export const ROLE_MANAGEMENT = 'ROLE_MANAGEMENT';
export const TOTP_QUERY = 'TOTP_QUERY';
export const LOGIN = 'LOGIN';
export const ARTIFACT_MANAGEMENT = 'ARTIFACT_MANAGEMENT';
export const TEMPLATE_MANAGEMENT = 'TEMPLATE_MANAGEMENT';
export const DEVICE_MANAGEMENT = 'DEVICE_MANAGEMENT';

// permission mapping
export const PERM_MAPPING = {
    SYSTEM_CONFIG: 'system config',
};
// permission page mapping
export const PERM_PAGE_MAPPING = {
    LOGIN: [],
    SYSTEM_CONFIG: [ROUTE_PATHS.PATH_SYSTEM_CONFIG],
};

export class PageLocation {
    windowLocation
    hostname
    href
    protocol
    port
    origin
    baseURL
    apiPrefix

    // If you want to hard code baseURL, modify the return value.

    constructor() {
        this.apiPrefix = 'API'
        this.windowLocation = window.location
        this.origin = this.windowLocation.origin
        this.hostname = this.windowLocation.hostname
        this.href = this.windowLocation.href
        this.protocol = this.windowLocation.protocol
        this.port = this.windowLocation.port
        if (this.port === '') {
            if (this.protocol === 'http:') {
                this.port = '80'
            }
            if (this.protocol === 'https:') {
                this.port = '443'
            }
        }
        const customBaseURL = this.customBaseURL()
        if (customBaseURL.hardCode) {
            this.baseURL = customBaseURL.baseURL
        } else {
            this.baseURL = this.origin + '/' + this.apiPrefix
        }
        console.log(this.baseURL)
    }

    // In general, hardCode mode is used in develepment while dynamic mode is used in production.
    customBaseURL() {
        return {
            // baseURL: 'http://172.27.224.241:8088/API',
            baseURL: 'http://172.27.128.180:8088/api',
            // baseURL: 'http://127.0.0.1:8088/API',
            // baseURL: 'http://10.3.119.22:8088/API',
            hardCode: true
        }
    }
}

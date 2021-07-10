import axios from "axios";
import { JsonWrappedURL, LoginPayload, PasswordChangePayload, RegisterPayload } from "./protocol";

const ENDPOINT_LOGIN = "/webClient/login"
const ENDPOINT_REGISTER = "/webClient/register"
const ENDPOINT_CHANGE_PWD = "/webClient/changePassword"
const ENDPOINT_SET_SKIN = "/webClient/setSkin"
const ENDPOINT_SET_CAPE = "/webClient/setCape"
const ENDPOINT_GET_ONLINE_LIST = "/webClient/online"
const ENDPOINT_UPLOAD_USER_CONTENT = "/webClient/uploadUserContent"

let baseURL = null

export function setup(base) {
    if(typeof(base) !== "string") {
        throw new Error("Illegal Argument! Required string type but " + typeof(base) + " found!")
    }
    baseURL = base
}

export function login(username, password, callback) {
    if(baseURL === null) {
        throw new Error("Please setup the api client first!")
    }
    axios({ method: "POST", url: baseURL + ENDPOINT_LOGIN, data: LoginPayload(username, password) }).then((result) => {
        if(result.status !== 204) {
            callback()
        } else {
            callback(new WebSession(result.headers.Token))
        }
    })
}

export function register(username, password, callback) {
    axios({ method: "POST", url: baseURL + ENDPOINT_REGISTER, data: RegisterPayload(username, password) }).then((result) => {
        callback(result.status)
    })
}

export function onlineList(callback) {
    axios({ method: "GET", url: baseURL + ENDPOINT_GET_ONLINE_LIST }).then((result) => {
        callback(result.data)
    })
}

class WebSession {
    constructor(token) {
        this.token = token
    }

    changePassword(oldPassword, newPassword, callback) {
        axios({method: "POST", url: baseURL + ENDPOINT_CHANGE_PWD, data: PasswordChangePayload(oldPassword, newPassword), headers: { "Cookie": { "Token": this.token } } }).then((result) => {
            callback(result.status)
        })
    }

    setSkin(url, callback) {
        axios({method: "POST", url: baseURL + ENDPOINT_SET_SKIN, data: JsonWrappedURL(url), headers: { "Cookie": { "Token": this.token } } }).then((result) => {
            callback(result.status)
        })
    }

    setCape(url, callback) {
        axios({ method: "POST", url: baseURL + ENDPOINT_SET_CAPE, data: JsonWrappedURL(url), headers: { "Cookie": { "Token": this.token } } }).then((result) => {
            callback(result.status)
        })
    }

    uploadUserContent(content, callback) {
        // TODO Upload file to backend
        //axios({ method: "PUT", url: baseURL + ENDPOINT_UPLOAD_USER_CONTENT, data: })
    }
}

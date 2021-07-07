class _RegisterPayload {
    /**
     * @param username string 用户名
     * @param password string 密码
     */
    constructor(username, password) {
        this.username = username
        this.password = password
    }
}

class _JsonWrappedURL {
    /**
     * @param url string URL
     */
    constructor(url) {
        this.url = url
    }
}

class _LoginPayload {
    /**
     *
     * @param username string 用户名
     * @param password string 密码
     */
    constructor(username, password) {
        this.username = username
        this.password = password
    }
}

/**
 * @param old string 老密码
 * @param newPwd string 新密码
 * @returns {{new, old}} {string, string}
 * @constructor
 */
export function PasswordChangePayload(old, newPwd) {
    return { old: old, new: newPwd }
}

/**
 *
 * @param username string 用户名
 * @param password string 密码
 * @returns {_LoginPayload}
 * @constructor
 */
export function LoginPayload(username, password) {
    return new _LoginPayload(username, password)
}

/**
 *
 * @param username string 用户名
 * @param password string 密码
 * @returns {_RegisterPayload}
 * @constructor
 */
export function RegisterPayload(username, password) {
    return new _RegisterPayload(username, password)
}

/**
 *
 * @param url string URL
 * @returns {_JsonWrappedURL}
 * @constructor
 */
export function JsonWrappedURL(url) {
    return new _JsonWrappedURL(url)
}
package me.connect.sdk.java;

import java.util.Map;

public class PromiseException extends Exception {

    private String code;
    private Map userInfo;

    public PromiseException() {
        super();
        init(null, null);
    }

    public PromiseException(String message) {
        super(message);
        init(null, null);
    }

    public PromiseException(String message, Throwable cause) {
        super(message, cause);
        init(null, null);
    }

    public PromiseException(Throwable cause) {
        super(cause);
        init(null, null);
    }

    public PromiseException(String code, String message,
                            Throwable cause, Map userInfo) {
        super(message, cause);
        init(code, userInfo);
    }

    private void init(String code, Map userInfo) {
        this.code = code;
        this.userInfo = userInfo;
    }

    public String getCode() {
        return code;
    }

    public Map getUserInfo() {
        return userInfo;
    }

}

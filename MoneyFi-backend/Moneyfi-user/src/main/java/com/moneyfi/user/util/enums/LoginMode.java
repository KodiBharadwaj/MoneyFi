package com.moneyfi.user.util.enums;

public enum LoginMode {
    EMAIL_PASSWORD(1),
    GOOGLE_OAUTH(2),
    GITHUB_OAUTH(3),
    DML_SCRIPT(4),
    MAINTAINER_CREATION(5);

    private Integer loginProcessCode;
    LoginMode(Integer loginProcessCode){
        this.loginProcessCode = loginProcessCode;
    }

    public Integer getLoginProcessCode() {
        return loginProcessCode;
    }

    public static LoginMode fromCode(Integer code) {
        for (LoginMode mode : values()) {
            if (mode.getLoginProcessCode().equals(code)) {
                return mode;
            }
        }
        return null;
    }
}

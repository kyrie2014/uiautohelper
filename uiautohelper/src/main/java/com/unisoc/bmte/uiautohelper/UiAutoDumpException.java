package com.unisoc.bmte.uiautohelper;

/**
 * Created by kyrie.liu on 2018/8/27.
 */

public class UiAutoDumpException extends Exception {
    private static final long serialVersionUID = 1L;

    public UiAutoDumpException(String msg) {
        super(msg);
    }

    public UiAutoDumpException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public UiAutoDumpException(Throwable throwable) {
        super(throwable);
    }
}

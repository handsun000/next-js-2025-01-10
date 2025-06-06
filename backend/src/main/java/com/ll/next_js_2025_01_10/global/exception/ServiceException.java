package com.ll.next_js_2025_01_10.global.exception;

import com.ll.next_js_2025_01_10.global.rsData.RsData;

public class ServiceException extends RuntimeException{
    private final String resultCode;
    private final String msg;

    public ServiceException(String resultCode, String msg) {
        super(resultCode + " : " + msg);
        this.resultCode = resultCode;
        this.msg = msg;
    }

    public RsData<Void> getRsData() {
        return new RsData<>(
                resultCode,
                msg
        );
    }
}

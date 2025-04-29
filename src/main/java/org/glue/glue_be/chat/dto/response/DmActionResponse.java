package org.glue.glue_be.chat.dto.response;

import lombok.Getter;

@Getter
public class DmActionResponse {
    private int code;
    private String message;

    public DmActionResponse(int code, String message) {
        this.code = code;
        this.message = message;
    }
}

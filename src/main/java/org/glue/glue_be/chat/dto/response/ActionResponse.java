package org.glue.glue_be.chat.dto.response;

import lombok.Getter;

@Getter
public class ActionResponse {
    private int code;
    private String message;

    public ActionResponse(int code, String message) {
        this.code = code;
        this.message = message;
    }
}

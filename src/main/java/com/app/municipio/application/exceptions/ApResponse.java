package com.app.municipio.application.exceptions;

import lombok.Data;

@Data
public class ApResponse {
    private boolean success;
    private String message;
    private Integer total;
    private Object data;

    public ApResponse(boolean success, String message, Object data) {
        this.success = success;
        this.message = message;
        this.data = data;
        
    }
    public ApResponse(boolean success, String message,Integer total, Object data) {
        this.success = success;
        this.message = message;
        this.total = total;
        this.data = data;
    }
}

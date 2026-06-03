package com.transactions.web.dto;

public record StatusResponse(String status) {
    public static StatusResponse ok() {
        return new StatusResponse("ok");
    }
}

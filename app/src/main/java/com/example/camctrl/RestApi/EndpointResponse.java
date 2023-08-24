package com.example.camctrl.RestApi;

public class EndpointResponse {
    String address;
    boolean isSuccess;
    public EndpointResponse(boolean isSuccess, String address) {
        this.address = address;
        this.isSuccess = isSuccess;

    }
}

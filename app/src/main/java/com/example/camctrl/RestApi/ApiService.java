package com.example.camctrl.RestApi;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("/endpoint")
    Call<EndpointResponse> postRawJson(@Body EndpointRequest body);
}

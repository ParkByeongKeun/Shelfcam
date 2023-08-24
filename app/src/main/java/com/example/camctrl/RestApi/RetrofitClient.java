package com.example.camctrl.RestApi;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static String mToken;

    public static ApiService getApiService(String address) {
//        mToken = token;
        return getInstance(address).create(ApiService.class);
    }
    static OkHttpClient client = new OkHttpClient.Builder()
            .addInterceptor(chain -> {
                Request newRequest  = chain.request().newBuilder()
                        .build();
                return chain.proceed(newRequest);
            }).build();

    private static Retrofit getInstance(String address) {

        Gson gson = new GsonBuilder().setLenient().create();
        return new Retrofit.Builder()
                .client(client)
                .baseUrl(address)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }

//
//    Retrofit retrofit = new Retrofit.Builder()
//            .client(client)
//            .baseUrl(URL)
//            .addConverterFactory(GsonConverterFactory.create())
//            .build();
//
//    ApiService server = retrofit.create(ApiService.class);



}
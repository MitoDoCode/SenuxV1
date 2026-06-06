package com.example.senuxv1;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface ShellApi {

    @Headers("X-Auth-Token: follow me MitoDoesCode")   //token you can put anything you want to match your server script later on
    @POST("/exec")
    Call<CommandResponse> execCommand(@Body CommandRequest command);

}

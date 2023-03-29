package com.chroma.app.nie360.NetworkClasses;


import com.chroma.app.nie360.Models.EventsResponse;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface webRequests {


    @GET("events")
    Call<EventsResponse> getEvents(@Query("admin_email") String admin_email, @Query("key") String key);

    @Multipart
    @Headers({"Accept: application/json"})

    @POST("events/media/add")
    Call<ResponseBody> sendVideo(@Part("admin_email") RequestBody admin_email, @Part("event_name") RequestBody event_name  , @Part("key") RequestBody key, @Part("photo") RequestBody media_type, @Part MultipartBody.Part media, @Part("photo") RequestBody  email, @Part("photo") RequestBody phone);
}
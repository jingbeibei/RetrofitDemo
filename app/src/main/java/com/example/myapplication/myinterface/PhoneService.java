package com.example.myapplication.myinterface;

import com.example.myapplication.bean.PhoneResult;



import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by 景贝贝 on 2016/10/12.
 */

public interface PhoneService {
    @GET("/showapi_open_bus/mobile/find")
    Call<PhoneResult> getResult(@Header("apikey") String apikey,
                                @Query("num") String phone);
    //Rxjava
    @GET("/showapi_open_bus/mobile/find")
    Observable<PhoneResult> getPhoneResult(@Header("apikey") String apikey,
                                           @Query("num") String phone);
}

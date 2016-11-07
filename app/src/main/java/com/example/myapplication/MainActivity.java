package com.example.myapplication;

import android.accounts.NetworkErrorException;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.bean.PhoneResult;
import com.example.myapplication.myinterface.PhoneService;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import cn.sharesdk.framework.ShareSDK;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    private Button button;
    private EditText editText;
    private Button retrofitBtn;
    private TextView textView;
    private Button retrofitRxjavaBtn;
    private Button rxjavaBtn;
    private static final String BASE_URL = "http://apis.baidu.com";
    private static final String API_KEY = "8e13586b86e4b7f3758ba3bd6c9c9135";
    private Button shareSDK;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = (Button) findViewById(R.id.button);
        editText = (EditText) findViewById(R.id.edit);
        retrofitBtn = (Button) findViewById(R.id.retrofit_btn);
        textView = (TextView) findViewById(R.id.textView);
        retrofitRxjavaBtn = (Button) findViewById(R.id.retrofit_rxjava_btn);
        rxjavaBtn = (Button) findViewById(R.id.rxjava_btn);
        shareSDK = (Button) findViewById(R.id.share);

        //Retrofit普通查询手机归属地
        retrofitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                query();
            }
        });
        //Rxjava查询
        rxjavaBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rxjavaQuery();
            }
        });
        //retrofit+rxjava查询归属地
        retrofitRxjavaBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RxJavaQuery();
            }
        });

        //分享第三方
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                shareIntent.setType("text/plain");
                String s = editText.getText().toString();
                shareIntent.putExtra(Intent.EXTRA_TEXT, s);
                startActivity(shareIntent);
            }
        });

        shareSDK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ShareActivity.class);
                startActivity(intent);
            }
        });
    }


    private void query() {
        //1.创建Retrofit对象
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        //2.创建访问API的请求
        PhoneService service = retrofit.create(PhoneService.class);
        Call<PhoneResult> call = service.getResult(API_KEY, editText.getText().toString());
        //3.发送请求
        call.enqueue(new Callback<PhoneResult>() {
            @Override
            public void onResponse(Call<PhoneResult> call, Response<PhoneResult> response) {
                //4.处理结果
                if (response.isSuccessful()) {
                    PhoneResult result = response.body();
                    if (result != null) {
                        String city = result.getShowapi_res_body().getCity();
                        textView.setText(city);
                    }
                }
            }

            @Override
            public void onFailure(Call<PhoneResult> call, Throwable t) {

            }
        });
    }

    //Rxjava
    private void rxjavaQuery() {
        Observable.create(new Observable.OnSubscribe<PhoneResult>() {

            @Override
            public void call(Subscriber<? super PhoneResult> subscriber) {
                //请求网络获取数据
                String s = get(BASE_URL + "/showapi_open_bus/mobile/find?num=" + editText.getText().toString());
                PhoneResult pr = new Gson().fromJson(s, PhoneResult.class);
                subscriber.onNext(pr);
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<PhoneResult>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(PhoneResult phoneResult) {
                        if (phoneResult != null && !phoneResult.getShowapi_res_error().equals("0")) {
                            String city = phoneResult.getShowapi_res_body().getCity();
                            textView.setText(city);
                        }
                    }
                });
    }

    //Rxjava+Retrofit
    private void RxJavaQuery() {
        //1.创建Retrofit对象
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        //2.创建访问API的请求
        PhoneService service = retrofit.create(PhoneService.class);
        //3.发起请求
        service.getPhoneResult(API_KEY, editText.getText().toString())
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<PhoneResult>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(PhoneResult phoneResult) {
                        if (phoneResult != null && !phoneResult.getShowapi_res_error().equals("0")) {
                            String city = phoneResult.getShowapi_res_body().getCity();
                            textView.setText(city);
                        }
                    }

                });
    }

    //get请求
    public String get(String url) {
        HttpURLConnection conn = null;
        try {
            // 利用string url构建URL对象
            URL mURL = new URL(url);
            conn = (HttpURLConnection) mURL.openConnection();

            conn.setRequestMethod("GET");
            conn.setReadTimeout(5000);
            conn.setConnectTimeout(10000);
            conn.addRequestProperty("apikey", API_KEY);
//            conn.addRequestProperty("num",editText.getText().toString());
            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {

                InputStream is = conn.getInputStream();
                String response = getStringFromInputStream(is);
                return response;
            } else {
                throw new NetworkErrorException("response status is " + responseCode);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            if (conn != null) {
                conn.disconnect();
            }
        }

        return null;
    }

    private String getStringFromInputStream(InputStream is)
            throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        // 模板代码 必须熟练
        byte[] buffer = new byte[1024];
        int len = -1;
        while ((len = is.read(buffer)) != -1) {
            os.write(buffer, 0, len);
        }
        is.close();
        String state = os.toString();// 把流中的数据转换成字符串,采用的编码是utf-8(模拟器默认编码)
        os.close();
        return state;
    }
}

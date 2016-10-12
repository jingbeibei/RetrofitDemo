package com.example.myapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.myapplication.bean.PhoneResult;
import com.example.myapplication.myinterface.PhoneService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    private Button button;
    private EditText editText;
    private Button retrofitBtn;
    private TextView textView;
    private static final String BASE_URL = "http://apis.baidu.com";
    private static final String API_KEY = "8e13586b86e4b7f3758ba3bd6c9c9135";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = (Button) findViewById(R.id.button);
        editText = (EditText) findViewById(R.id.edit);
        retrofitBtn = (Button) findViewById(R.id.retrofit_btn);
        textView = (TextView) findViewById(R.id.textView);

        //查询手机归属地
        retrofitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                query();
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, "http://blog.csdn.net/chadeltu/article/details/43450713");
                startActivity(shareIntent);
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
}

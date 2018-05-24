package org.tensorflow.demo;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


/**
 * Created by ASUS on 19/02/2017.
 */

public class RetrofitClient {

    private static final String BASE_URL="http://192.168.0.30:4000/";
    private ApiService apiService;
    private static RetrofitClient instance;

    private RetrofitClient() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);

    }

    public ApiService getApiService(){
        return apiService;
    }

    public static RetrofitClient getInstance(){
        if (instance ==null){
            instance=new RetrofitClient();
        }
        return instance;
    }
}


package com.fastaccess.provider.rest;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.fastaccess.BuildConfig;
import com.fastaccess.data.service.LoginRestService;
import com.fastaccess.provider.rest.converters.GithubResponseConverter;
import com.fastaccess.provider.rest.interceptors.AuthenticationInterceptor;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Modifier;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;

/**
 * Created by Kosh on 08 Feb 2017, 8:37 PM
 */

public class LoginProvider {

    private final static Gson gson = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .excludeFieldsWithModifiers(Modifier.FINAL, Modifier.TRANSIENT, Modifier.STATIC)
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .setPrettyPrinting()
            .create();

    private static OkHttpClient provideOkHttpClient(@Nullable String authToken, @Nullable String otp) {
        OkHttpClient.Builder client = new OkHttpClient.Builder();
        if (BuildConfig.DEBUG) {
            client.addInterceptor(new HttpLoggingInterceptor()
                    .setLevel(HttpLoggingInterceptor.Level.BODY))
                    .addInterceptor(new HttpLoggingInterceptor()
                            .setLevel(HttpLoggingInterceptor.Level.HEADERS));
        }
        client.addInterceptor(new AuthenticationInterceptor(authToken, otp));
        return client.build();
    }

    private static Retrofit provideRetrofit(@Nullable String authToken, @Nullable String otp) {
        return new Retrofit.Builder()
                .baseUrl(BuildConfig.REST_URL)
                .client(provideOkHttpClient(authToken, otp))
                .addConverterFactory(new GithubResponseConverter(gson))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
    }

    public static LoginRestService getLoginRestService() {
        return new Retrofit.Builder()
                .baseUrl("https://github.com/login/oauth/")
                .client(provideOkHttpClient(null, null))
                .addConverterFactory(new GithubResponseConverter(gson))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build()
                .create(LoginRestService.class);
    }

    @NonNull public static LoginRestService getLoginRestService(@NonNull String authToken, @Nullable String otp) {
        return provideRetrofit(authToken, otp).create(LoginRestService.class);
    }
}

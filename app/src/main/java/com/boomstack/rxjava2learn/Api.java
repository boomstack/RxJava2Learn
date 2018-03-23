package com.boomstack.rxjava2learn;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Created by ethan on 23/03/2018.
 */

public interface Api {
    @GET("v2/book/{id}")
    Observable<ResponseBody> testInternet(@Path("id") int id);
}

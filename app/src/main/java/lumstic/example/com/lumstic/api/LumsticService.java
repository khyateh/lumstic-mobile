package lumstic.example.com.lumstic.api;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;

public interface LumsticService {

        @POST(value = "/api/users")
        public void loginUser(@Body Object object, Callback<ApiResponse> callback);

}
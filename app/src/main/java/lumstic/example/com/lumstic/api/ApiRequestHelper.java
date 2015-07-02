package lumstic.example.com.lumstic.api;

import org.codehaus.jackson.map.ObjectMapper;
import java.net.HttpRetryException;
import java.util.Map;
import lumstic.example.com.lumstic.Utils.LumsticApp;
import lumstic.example.com.lumstic.R;
import lumstic.example.com.lumstic.Utils.DataMapParser;
import lumstic.example.com.lumstic.Utils.Logger;
import retrofit.Callback;
import retrofit.ErrorHandler;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.android.AndroidLog;
import retrofit.client.Response;
public class ApiRequestHelper {
    public static interface onRequestComplete {
        public void onSuccess(Object object);
        public void onFailure(ApiResponse apiResponse);
    }
    private static ApiRequestHelper instance;
    private LumsticService lumsticService;
    private LumsticApp application;
    public static synchronized ApiRequestHelper init(LumsticApp application) {
        if (null == instance) {
            instance = new ApiRequestHelper();
            instance.setApplication(application);
            instance.createRestAdapter();
        }
        return instance;
    }
    /**
     * Add all the api request's here
     */


    /**
     * End api requests
     */

    /**
     * REST Adapter Configuration
     */
    private void createRestAdapter() {
        ObjectMapper objectMapper = new ObjectMapper();
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setConverter(new JacksonConverter(objectMapper))
                .setErrorHandler(new ErrorHandler() {
                    @Override
                    public Throwable handleError(RetrofitError error) {
                        application.getLogger();
                        final Response response = error.getResponse();
                        if (response != null) {
                            int statusCode = response.getStatus();
                            if (error.isNetworkError() || (500 <= statusCode && statusCode < 600)) {
                                return new HttpRetryException(Logger.TAG, statusCode);
                            }
                        }
                        return error;
                    }
                })
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setLog(new AndroidLog(Logger.TAG))
               .setEndpoint(application.getResources().getString(R.string.server_url))
                .setRequestInterceptor(getRequestInterceptor())
                .build();

        lumsticService = restAdapter.create(LumsticService.class);
    }
    private RequestInterceptor getRequestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void intercept(RequestFacade request) {
                //Add Headers here
                request.addHeader("Accept", "application/json");
                //request.addHeader("api_key", application.getString(R.string.api_key));

                if (application.getPreferences().addAuthInHeader()) {
                    String authToken = application.getPreferences().getAccessToken();
                    request.addHeader("auth_token", authToken);
                }
            }
        };
    }
    /**
     * End REST Adapter Configuration
     */
    public void setLumsticService(LumsticService lumsticService) {
        this.lumsticService = lumsticService;
    }

    public LumsticApp getApplication() {
        return application;
    }
    public void setApplication(LumsticApp application) {
        this.application = application;
    }
}
package lumstic.example.com.lumstic.api;
import java.io.Serializable;
import java.util.Map;
public class ApiResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    public static class ApiError implements Serializable {
        private static final long serialVersionUID = 1L;
        public static final String COMM_ERROR = "Unable to communicate with the server";
        public static final String GEN_ERROR= "Error";
        public static final String  RESP_ERROR= "Bad response format";
        public static final String MISSING_CONTENT_TYPE_ERROR = "Missing content type header";
        private String code;
        private String message;
        public ApiError() {
        }
        public ApiError(String code, String message) {
            this.code = code;
            this.message = message;
        }
        public static final ApiError GENERAL_ERROR = new ApiError(500 + "", GEN_ERROR);
        public static final ApiError COMMUNICATION_ERROR = new ApiError(501 + "", COMM_ERROR);
        public static final ApiError RESPONSE_ERROR = new ApiError(502 + "", RESP_ERROR);
        public static final ApiError API_VERSION_MISMATCH = new ApiError(503 + "", "API_VERSION_MISMATCH");
        public static final ApiError MISSING_CONTENT_TYPE_HEADER = new ApiError(504 + "", MISSING_CONTENT_TYPE_ERROR);
        public String getCode() {
            return code;
        }
        public void setCode(String code) {
            this.code = code;
        }
        public String getMessage() {
            return message;
        }
        public void setMessage(String message) {
            this.message = message;
        }
    }
    private boolean success;
    private Object data;
    private ApiError error;
    private Object resultDataObject;
    public ApiResponse() {
    }
    public ApiResponse(boolean success, Map<String, Object> data, String message) {
        this.success = success;
        this.data = data;
        if (!success) {
            this.error = new ApiError(500 + "", message);
        }
    }
    public boolean isSuccess() {
        return success;
    }
    public ApiResponse setSuccess(boolean success) {
        this.success = success;
        return this;
    }
    public Object getData() {
        return data;
    }

    public ApiResponse setData(Object data) {
        this.data = data;
        return this;
    }
    public ApiError getError() {
        return error;
    }

    public ApiResponse setError(ApiError error) {
        this.error = error;
        if (error != null) {
            success = false;
        }
        return this;
    }
    public Object getResultDataObject() {
        return resultDataObject;
    }
    public ApiResponse setResultDataObject(Object resultDataObject) {
        this.resultDataObject = resultDataObject;
        return this;
    }
}

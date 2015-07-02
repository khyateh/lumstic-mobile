package lumstic.example.com.lumstic.Utils;

import java.util.Map;

/**
 * Created by work on 23/4/15.
 */
    public class DataMapParser {

        public static String parseAuthToken(Map<String, Object> dataMap) {
            return (String) dataMap.get("auth_token");
        }
}

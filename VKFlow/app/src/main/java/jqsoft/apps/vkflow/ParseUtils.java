package jqsoft.apps.vkflow;

import org.json.JSONObject;

/**
 * Created by maximyudin on 26.04.16.
 */
public class ParseUtils {
    /**
     * Parse boolean from JSONObject with given name.
     *
     * @param from server response like this format: {@code field: 1}
     * @param name name of field to read
     */
    public static boolean parseBoolean(JSONObject from, String name) {
        return from != null && from.optInt(name, 0) == 1;
    }
}

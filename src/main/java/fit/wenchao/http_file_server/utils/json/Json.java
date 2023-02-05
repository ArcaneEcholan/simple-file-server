package fit.wenchao.http_file_server.utils.json;

import com.alibaba.fastjson.JSONObject;

public class Json {

    public static JSONObject json(Pair... pairs) {
        JSONObject json = new JSONObject();
        for (Pair pair : pairs) {
            json.put(pair.getKey(), pair.getValue());
        }
        return json;
    }

}

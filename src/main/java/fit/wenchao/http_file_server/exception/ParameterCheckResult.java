package fit.wenchao.http_file_server.exception;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Data
@Accessors(chain = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public
class ParameterCheckResult {
    JSONObject paramCheckMap = new JSONObject();

    public void putResult(String field, String message) {
        paramCheckMap.put(field, message);
    }
}
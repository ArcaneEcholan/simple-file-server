package fit.wenchao.http_file_server.model.chat;

import fit.wenchao.http_file_server.constants.BusinessType;
import fit.wenchao.http_file_server.constants.ContentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Accessors(chain = true)
public class Message {
    String contentType;
    String business;
    String from;
    String target;
    Object content;
}

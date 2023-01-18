package fit.wenchao.http_file_server.wss;

import fit.wenchao.http_file_server.model.vo.FileInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DirInfo extends FileInfo {
    long numberOfFiles;
}
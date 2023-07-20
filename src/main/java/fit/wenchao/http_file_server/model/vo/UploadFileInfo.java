package fit.wenchao.http_file_server.model.vo;

import javax.validation.constraints.NotBlank;

public class UploadFileInfo {

    @NotBlank
    String path;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}



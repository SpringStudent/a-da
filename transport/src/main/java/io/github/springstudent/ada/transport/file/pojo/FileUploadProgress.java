package io.github.springstudent.ada.transport.file.pojo;

import com.gysoft.jdbc.annotation.Table;

/**
 * @author ZhouNing
 * @date 2024/12/31 9:28
 **/
@Table(name = "file_upload_progress")
public class FileUploadProgress {

    private String id;

    private String fileMd5;

    private Long fileSize;

    private Long finishSize;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFileMd5() {
        return fileMd5;
    }

    public void setFileMd5(String fileMd5) {
        this.fileMd5 = fileMd5;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public Long getFinishSize() {
        return finishSize;
    }

    public void setFinishSize(Long finishSize) {
        this.finishSize = finishSize;
    }
}

package io.github.springstudent.ada.transport.file.pojo;


import com.gysoft.jdbc.annotation.Table;

import java.util.Date;

/**
 * @author ZhouNing
 * @date 2024/12/31 9:18
 **/
@Table(name = "file_info")
public class FileInfo {

    private String id;

    private String fileName;

    private String fileMd5;

    private String suffix;

    private Long fileSize;

    private Date uploadTime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileMd5() {
        return fileMd5;
    }

    public void setFileMd5(String fileMd5) {
        this.fileMd5 = fileMd5;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public Date getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(Date uploadTime) {
        this.uploadTime = uploadTime;
    }
}

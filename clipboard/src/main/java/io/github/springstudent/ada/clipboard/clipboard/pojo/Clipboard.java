package io.github.springstudent.ada.clipboard.clipboard.pojo;

import com.gysoft.jdbc.annotation.Table;

/**
 * @author ZhouNing
 * @date 2024/12/31 16:17
 **/
@Table(name = "clipboard")
public class Clipboard {

    private String id;

    private String deviceCode;

    private String fileInfoId;

    private String fileName;

    private String filePid;

    private Integer isFile;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDeviceCode() {
        return deviceCode;
    }

    public void setDeviceCode(String deviceCode) {
        this.deviceCode = deviceCode;
    }

    public String getFileInfoId() {
        return fileInfoId;
    }

    public void setFileInfoId(String fileInfoId) {
        this.fileInfoId = fileInfoId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePid() {
        return filePid;
    }

    public void setFilePid(String filePid) {
        this.filePid = filePid;
    }

    public Integer getIsFile() {
        return isFile;
    }

    public void setIsFile(Integer isFile) {
        this.isFile = isFile;
    }
}

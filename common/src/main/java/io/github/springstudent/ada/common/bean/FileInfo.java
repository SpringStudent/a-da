package io.github.springstudent.ada.common.bean;

/**
 * @author ZhouNing
 * @date 2024/12/31 10:54
 **/
public class FileInfo {
    /**
     * 文件uuid
     */
    private String fileUuid;
    /**
     * 文件md5
     */
    private String fileMd5;
    /**
     * 文件size
     */
    private Long fileSize;
    /**
     * 文件名称
     */
    private String fileName;

    public String getFileUuid() {
        return fileUuid;
    }

    public void setFileUuid(String fileUuid) {
        this.fileUuid = fileUuid;
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

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}

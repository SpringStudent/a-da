package io.github.springstudent.ada.transport.file.pojo;

import com.gysoft.jdbc.annotation.Table;

/**
 * @author ZhouNing
 * @date 2024/12/31 9:24
 **/
@Table(name = "file_chunk")
public class FileChunk {

    private String id;

    private Integer chunkNo;

    private Long chunkSize;

    private String chunkName;

    private byte[] chunkBlob;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getChunkNo() {
        return chunkNo;
    }

    public void setChunkNo(Integer chunkNo) {
        this.chunkNo = chunkNo;
    }

    public Long getChunkSize() {
        return chunkSize;
    }

    public void setChunkSize(Long chunkSize) {
        this.chunkSize = chunkSize;
    }

    public String getChunkName() {
        return chunkName;
    }

    public void setChunkName(String chunkName) {
        this.chunkName = chunkName;
    }

    public byte[] getChunkBlob() {
        return chunkBlob;
    }

    public void setChunkBlob(byte[] chunkBlob) {
        this.chunkBlob = chunkBlob;
    }
}

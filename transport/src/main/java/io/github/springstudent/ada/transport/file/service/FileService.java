package io.github.springstudent.ada.transport.file.service;

import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author ZhouNing
 * @date 2024/12/31 9:16
 **/
public interface FileService {

    /**
     * 文件快速上传验证(如果已经上传则返回业务id, 否则返回-1)
     *
     * @param md5
     * @param fileSize
     * @return String
     * @throws Exception
     */
    String quickUploadFile(String md5, Long fileSize) throws Exception;

    /**
     * 检查分块文件快速验证如果已经上传返回true，如果没有传，或者传的大小跟该块验证大小不一致则删除原始块，重新传返回false。
     *
     * @param md5
     * @param chunkNo
     * @param chunkSize
     * @return boolean
     * @throws Exception
     */
    boolean checkChunk(String md5, Integer chunkNo, Long chunkSize) throws Exception;

    /**
     * 下载文件
     *
     * @param fileInfoId 文件id
     * @param request
     * @param response
     * @throws Exception
     */
    void download(String fileInfoId, HttpServletRequest request, HttpServletResponse response) throws Exception;

    /**
     * 上传文件块
     *
     * @param chunk    块
     * @param md5      文件的md5
     * @param chunkNo  块标号
     * @param fileName 文件名称
     * @return String
     * @throws Exception
     */
    String uploadFileChunk(MultipartFile chunk, String md5, Integer chunkNo, String fileName) throws Exception;

    /**
     * 删除文件
     *
     * @param fileInfoIds
     * @throws Exception
     * @author ZhouNing
     * @date 2025/1/3 9:51
     **/
    void deleteFile(List<String> fileInfoIds) throws Exception;
}

package io.github.springstudent.ada.client.utils;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import io.github.springstudent.ada.common.bean.FileInfo;
import io.github.springstudent.ada.common.bean.RemoteClipboard;
import io.github.springstudent.ada.common.utils.EmptyUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author ZhouNing
 * @date 2024/12/31 15:34
 **/
public class RemoteUtils {
    public static final String TMP_PATH_KEY = "fileTmpPath";
    public static final String REQUEST_URL_KEY = "requestUrl";
    public static final String REQUEST_TIMEOUT_KEY = "requestTimeout";
    /**
     * 默认分块大小2M
     */
    private static final int CHUNK_SIZE = 1024 * 1024 * 2;
    /**
     * 快速上传请求uri
     */
    private static final String QUICK_UPLOAD_REQUEST = "/file/quickUploadFile";
    /**
     * 检查分块文件快速验证
     */
    private static final String CHECK_CHUNK_REQUEST = "/file/checkChunk";
    /**
     * 上传分块文件
     */
    private static final String UPLOAD_CHUNK_REQUEST = "/file/uploadFileChunk";

    private static final String DOWNLOAD_FILE_REQUEST = "/file/downloadFile";
    private static final String CLEAR_CLIPBOARD = "/clipboard/clear";
    private static final String SAVE_CLIPBOARD = "/clipboard/save";
    private static final String GET_CLIPBOARD = "/clipboard/get";

    /**
     * 文件分片上传
     *
     * @param file
     * @param requestParam
     * @return FileInfo
     * @throws Exception
     */
    public static FileInfo uploadFile(File file, Map<String, Object> requestParam) throws Exception {
        String fileTmpPath = MapUtil.getStr(requestParam, TMP_PATH_KEY);
        String reqUrl = MapUtil.getStr(requestParam, REQUEST_URL_KEY);
        int reqTimeout = MapUtil.getInt(requestParam, REQUEST_TIMEOUT_KEY, 10000);
        if (EmptyUtils.isEmpty(fileTmpPath)) {
            throw new IllegalArgumentException("上传文件临时目录不能为空");
        } else {
            FileUtil.mkdir(fileTmpPath);
        }
        if (EmptyUtils.isEmpty(reqUrl)) {
            throw new IllegalArgumentException("上传文件的请求url不能为空");
        }
        FileInfo fileInfo = new FileInfo();
        String fileMd5 = SecureUtil.md5(file);
        long fileSize = file.length();
        String fileName = FileUtil.getName(file);
        //结果赋值
        fileInfo.setFileMd5(fileMd5);
        fileInfo.setFileSize(fileSize);
        fileInfo.setFileName(fileName);
        //1.先走快速上传,快速上传结果不是-1，程序直接结束
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("md5", fileMd5);
        paramMap.put("fileSize", fileSize);
        String uploadResult = parseObj(HttpRequest.get(reqUrl + QUICK_UPLOAD_REQUEST).form(paramMap).timeout(reqTimeout).execute().body()).getStr("result");
        if (!uploadResult.equals("-1")) {
            fileInfo.setFileUuid(uploadResult);
            return fileInfo;
        }
        //2.快速上传失败，进行文件切片分片上传
        String fileUuid = "-1";
        final long numberOfChunk = fileSize % CHUNK_SIZE == 0 ? fileSize / CHUNK_SIZE : (fileSize / CHUNK_SIZE) + 1;
        for (int i = 0; i < numberOfChunk; i++) {
            long start = i * CHUNK_SIZE;
            long end = start + CHUNK_SIZE;
            if (end > fileSize) {
                end = fileSize;
            }
            File chunkFile = chunkFile(file.toPath(), start, end, fileTmpPath + File.separator + fileName);
            try {
                paramMap.clear();
                paramMap.put("md5", fileMd5);
                paramMap.put("chunkNo", i + 1);
                paramMap.put("chunkSize", FileUtil.size(chunkFile));
                boolean checkChunkResult = parseObj(HttpRequest.get(reqUrl + CHECK_CHUNK_REQUEST).form(paramMap).timeout(reqTimeout).execute().body()).getBool("result");
                //3.分片未被上传,执行上传分片逻辑
                if (!checkChunkResult) {
                    paramMap.put("file", chunkFile);
                    paramMap.put("fileName", FileUtil.getName(chunkFile));
                    String uploadChunkResult = parseObj(HttpRequest.post(reqUrl + UPLOAD_CHUNK_REQUEST).form(paramMap).timeout(reqTimeout).execute().body()).getStr("result");
                    if (!uploadChunkResult.equals("-1")) {
                        fileUuid = uploadChunkResult;
                        break;
                    }
                }
            } catch (Exception e) {
                throw e;
            } finally {
                FileUtil.del(chunkFile);
            }
        }
        if (fileUuid.equals("-1")) {
            throw new IllegalStateException("上传文件失败");
        }
        fileInfo.setFileUuid(fileUuid);
        return fileInfo;
    }

    private static JSONObject parseObj(String result) {
        JSONObject jsonObject = JSONUtil.parseObj(result);
        if (jsonObject.getInt("code").intValue() == 200) {
            return jsonObject;
        } else {
            throw new IllegalStateException(jsonObject.getStr("msg"));
        }
    }

    /**
     * 将文件分片
     *
     * @param file
     * @param start
     * @param end
     * @param chunk
     * @return File
     * @throws IOException
     */
    public static File chunkFile(Path file, long start, long end, String chunk) throws IOException {
        if (Files.notExists(file) || Files.isDirectory(file)) {
            throw new IllegalArgumentException("文件不存在:" + file);
        }
        // 分片文件名称
        Path chunkFile = Paths.get(chunk);
        try (FileChannel fileChannel = FileChannel.open(file, EnumSet.of(StandardOpenOption.READ))) {
            try (FileChannel chunkFileChannel = FileChannel.open(chunkFile, EnumSet.of(StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE))) {
                // 返回写入的数据长度
                fileChannel.transferTo(start, end - start, chunkFileChannel);
                return chunkFile.toFile();
            }
        }
    }

    /**
     * 从网络上下载文件
     *
     * @param fileInfoId
     * @param requestParam
     * @throws IOException
     */
    public static void downloadUrlFile(String fileInfoId, Map<String, Object> requestParam) throws IOException {
        String file = MapUtil.getStr(requestParam, TMP_PATH_KEY);
        String reqUrl = MapUtil.getStr(requestParam, REQUEST_URL_KEY);
        ReadableByteChannel rbc = null;
        FileOutputStream fos = null;
        try {
            URL website = new URL(reqUrl + DOWNLOAD_FILE_REQUEST + "?fileInfoId=" + fileInfoId);
            rbc = Channels.newChannel(website.openStream());
            fos = new FileOutputStream(file);
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        } catch (IOException e) {
            throw e;
        } finally {
            IoUtil.close(fos);
            IoUtil.close(rbc);
        }
    }

    public static void saveClipboard(List<RemoteClipboard> clipboards, Map<String, Object> requestParam) {
        String reqUrl = MapUtil.getStr(requestParam, REQUEST_URL_KEY);
        int reqTimeout = MapUtil.getInt(requestParam, REQUEST_TIMEOUT_KEY, 10000);
        parseObj(HttpRequest.post(reqUrl + SAVE_CLIPBOARD).body(JSONUtil.toJsonStr(clipboards)).timeout(reqTimeout).execute().body());
    }

    public static void clearClipboard(String deviceCode, Map<String, Object> requestParam) {
        String reqUrl = MapUtil.getStr(requestParam, REQUEST_URL_KEY);
        int reqTimeout = MapUtil.getInt(requestParam, REQUEST_TIMEOUT_KEY, 10000);
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("deviceCode", deviceCode);
        parseObj(HttpRequest.post(reqUrl + CLEAR_CLIPBOARD).form(paramMap).timeout(reqTimeout).execute().body());
    }

    public static List<RemoteClipboard> getClipboard(String deviceCode, Map<String, Object> requestParam) {
        String reqUrl = MapUtil.getStr(requestParam, REQUEST_URL_KEY);
        int reqTimeout = MapUtil.getInt(requestParam, REQUEST_TIMEOUT_KEY, 10000);
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("deviceCode", deviceCode);
        JSONObject result = parseObj(HttpRequest.get(reqUrl + GET_CLIPBOARD).form(paramMap).timeout(reqTimeout).execute().body());
        return buildClipboard(JSONUtil.toList(result.getStr("result"), RemoteClipboard.class));
    }

    public static List<RemoteClipboard> buildClipboard(List<RemoteClipboard> clipboards) {
        Map<String, RemoteClipboard> clipboardMap = clipboards.stream()
                .collect(Collectors.toMap(RemoteClipboard::getId, clipboard -> clipboard));
        List<RemoteClipboard> roots = new ArrayList<>();
        for (RemoteClipboard clipboard : clipboards) {
            String parentId = clipboard.getFilePid();
            if (EmptyUtils.isEmpty(parentId)) {
                roots.add(clipboard);
            } else {
                RemoteClipboard parent = clipboardMap.get(parentId);
                if (parent != null) {
                    if (parent.getChilds() == null) {
                        parent.setChilds(new ArrayList<>());
                    }
                    parent.getChilds().add(clipboard);
                }
            }
        }
        return roots;
    }
}

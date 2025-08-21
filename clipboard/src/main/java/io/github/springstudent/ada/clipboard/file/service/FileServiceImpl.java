package io.github.springstudent.ada.clipboard.file.service;

import cn.hutool.core.util.IdUtil;
import com.gysoft.jdbc.bean.Criteria;
import com.gysoft.jdbc.bean.Page;
import com.gysoft.jdbc.bean.SQL;
import com.gysoft.jdbc.bean.Sort;
import io.github.springstudent.ada.common.utils.EmptyUtils;
import io.github.springstudent.ada.clipboard.file.bean.FileException;
import io.github.springstudent.ada.clipboard.file.dao.FileChunkDao;
import io.github.springstudent.ada.clipboard.file.dao.FileInfoDao;
import io.github.springstudent.ada.clipboard.file.dao.FileUploadProgressDao;
import io.github.springstudent.ada.clipboard.file.pojo.FileChunk;
import io.github.springstudent.ada.clipboard.file.pojo.FileInfo;
import io.github.springstudent.ada.clipboard.file.pojo.FileUploadProgress;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.gysoft.jdbc.bean.FuncBuilder.count;

/**
 * @author ZhouNing
 * @date 2024/12/31 9:18
 **/
@Service
public class FileServiceImpl implements FileService {

    @Resource
    private FileInfoDao fileInfoDao;

    @Resource
    private FileUploadProgressDao fileUploadProgressDao;

    @Resource
    private FileChunkDao fileChunkDao;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String quickUploadFile(String md5, Long fileSize) throws Exception {
        List<FileInfo> fileInfos = fileInfoDao.queryWithCriteria(new Criteria().where(FileInfo::getFileMd5, md5));
        if (EmptyUtils.isNotEmpty(fileInfos)) {
            FileInfo fileInfo = fileInfos.get(0);
            fileInfo.setId(IdUtil.fastSimpleUUID());
            fileInfo.setUploadTime(new Date());
            fileInfoDao.save(fileInfo);
            return fileInfo.getId();
        }
        FileUploadProgress fileUploadProgress = fileUploadProgressDao.queryOne(new Criteria().where(FileUploadProgress::getFileMd5, md5));
        if (fileUploadProgress == null) {
            FileUploadProgress progress = new FileUploadProgress();
            progress.setId(IdUtil.fastSimpleUUID());
            progress.setFileMd5(md5);
            progress.setFileSize(fileSize);
            progress.setFinishSize(0L);
            fileUploadProgressDao.save(progress);
        }
        return "-1";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean checkChunk(String md5, Integer chunkNo, Long chunkSize) throws Exception {
        FileChunk fileChunk = fileChunkDao.queryOne(new Criteria().where(FileChunk::getChunkName, md5).and(FileChunk::getChunkNo, chunkNo));
        if (fileChunk != null && fileChunk.getChunkSize() - chunkSize == 0) {
            return true;
        } else if (fileChunk != null) {
            fileChunkDao.deleteWithCriteria(new Criteria().where(FileChunk::getChunkName, md5).and(FileChunk::getChunkNo, chunkNo));
            FileUploadProgress fileUploadProgress = fileUploadProgressDao.queryOne(new Criteria().where(FileUploadProgress::getFileMd5, md5));
            if (fileUploadProgress != null) {
                fileUploadProgress.setFinishSize(fileUploadProgress.getFinishSize() - fileChunk.getChunkSize());
                fileUploadProgressDao.update(fileUploadProgress);
            }
        }
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String uploadFileChunk(MultipartFile chunk, String md5, Integer chunkNo, String fileName) throws Exception {
        FileUploadProgress fileUploadProgress = fileUploadProgressDao.queryOne(new Criteria().where(FileUploadProgress::getFileMd5, md5));
        if (fileUploadProgress == null) {
            throw new FileException("文件上传进度未初始化");
        }
        if (!checkChunk(md5, chunkNo, chunk.getSize())) {
            FileChunk fileChunk = new FileChunk();
            fileChunk.setChunkNo(chunkNo);
            fileChunk.setChunkName(md5);
            fileChunk.setChunkBlob(chunk.getBytes());
            fileChunk.setChunkSize(chunk.getSize());
            fileChunk.setId(IdUtil.fastSimpleUUID());
            fileChunkDao.uploadFile(fileChunk, chunk);
            Long fileSize = fileUploadProgress.getFileSize();
            Long finishSize = fileUploadProgress.getFinishSize();
            if (finishSize + chunk.getSize() == fileSize) {
                //上传完成
                fileUploadProgress.setFinishSize(fileSize);
                fileUploadProgressDao.update(fileUploadProgress);
                String fileInfoId = IdUtil.fastSimpleUUID();
                FileInfo fileInfo = new FileInfo();
                fileInfo.setId(fileInfoId);
                fileInfo.setFileMd5(md5);
                fileInfo.setFileSize(fileSize);
                if (fileName.indexOf(".") != -1) {
                    fileInfo.setFileName(fileName.substring(0, fileName.lastIndexOf(".")));
                    fileInfo.setSuffix(fileName.substring(fileName.lastIndexOf(".") + 1));
                } else {
                    fileInfo.setFileName(fileName);
                }
                fileInfo.setUploadTime(new Date());
                fileInfoDao.save(fileInfo);
                return fileInfoId;
            } else if (finishSize + chunk.getSize() < fileSize) {
                fileUploadProgress.setFinishSize(fileUploadProgress.getFinishSize() + chunk.getSize());
                fileUploadProgressDao.update(fileUploadProgress);
            }
        }
        return "-1";
    }


    @Override
    public void download(String fileInfoId, HttpServletRequest request, HttpServletResponse response) throws Exception {
        FileInfo fileInfo = fileInfoDao.queryOne(fileInfoId);
        if (fileInfo == null) {
            throw new FileException("文件不存在");
        }
        Integer chunkCount = fileChunkDao.queryIntegerWithSql(new SQL().select(count("id")).from(FileChunk.class).where(FileChunk::getChunkName, fileInfo.getFileMd5()));
        if (chunkCount != null && chunkCount > 0) {
            response.setContentLengthLong(fileInfo.getFileSize());
            response.setContentType("application/octet-stream");

            String encodeFileName = null;
            if (EmptyUtils.isNotEmpty(fileInfo.getSuffix())) {
                encodeFileName = URLEncoder.encode(fileInfo.getFileName() + "." + fileInfo.getSuffix(), "UTF-8");
            } else {
                encodeFileName = URLEncoder.encode(fileInfo.getFileName(), "UTF-8");
            }
            response.setHeader("Content-Disposition", "attachment;filename=" + encodeFileName);
            int pageSize = 20;
            int pageNo = 1;
            try (OutputStream out = response.getOutputStream()) {
                while (true) {
                    List<FileChunk> chunkPage = fileChunkDao.pageQueryWithCriteria(new Page(pageNo++, pageSize), new Criteria().where(FileChunk::getChunkName, fileInfo.getFileMd5()).orderBy(new Sort(FileChunk::getChunkNo, "ASC"))).getList();
                    if (EmptyUtils.isEmpty(chunkPage)) {
                        break;
                    }
                    for (FileChunk chunk : chunkPage) {
                        byte[] bytes = chunk.getChunkBlob();
                        if (bytes != null) {
                            out.write(bytes);
                        }
                    }
                    out.flush();
                }
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFile(List<String> fileInfoIds) throws Exception {
        if (EmptyUtils.isEmpty(fileInfoIds)) {
            return;
        }
        List<FileInfo> fileInfos = fileInfoDao.queryWithCriteria(new Criteria().in(FileInfo::getId, fileInfoIds));
        if (EmptyUtils.isNotEmpty(fileInfos)) {
            List<String> md5s = new ArrayList<>();
            for (FileInfo fileInfo : fileInfos) {
                md5s.add(fileInfo.getFileMd5());
            }
            //查找这些md5对应的所有文件信息
            List<FileInfo> retainFileInfos = fileInfoDao.queryWithCriteria(new Criteria().in(FileInfo::getFileMd5, md5s));
            for (FileInfo fileInfo : retainFileInfos) {
                //如果存在其他文件和待删除文件的md5相同，则不能删除真实文件
                if (!fileInfoIds.contains(fileInfo.getId())) {
                    md5s.removeAll(Arrays.asList(fileInfo.getFileMd5()));
                }
            }
            //删除真实文件
            if (EmptyUtils.isNotEmpty(md5s)) {
                fileChunkDao.deleteWithCriteria(new Criteria().in(FileChunk::getChunkName, md5s));
                fileUploadProgressDao.deleteWithCriteria(new Criteria().in(FileUploadProgress::getFileMd5, md5s));
            }
            //删除文件信息
            fileInfoDao.batchDelete(fileInfoIds);
        }
    }
}

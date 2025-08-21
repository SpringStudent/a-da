package io.github.springstudent.ada.clipboard.file.dao;

import com.gysoft.jdbc.dao.EntityDaoImpl;
import io.github.springstudent.ada.clipboard.file.pojo.FileUploadProgress;
import org.springframework.stereotype.Repository;

/**
 * @author ZhouNing
 * @date 2024/12/31 9:31
 **/
@Repository
public class FileUploadProgressDaoImpl extends EntityDaoImpl<FileUploadProgress,String> implements FileUploadProgressDao {
}

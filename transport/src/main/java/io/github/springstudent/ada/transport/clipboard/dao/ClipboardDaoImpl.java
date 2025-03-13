package io.github.springstudent.ada.transport.clipboard.dao;

import com.gysoft.jdbc.dao.EntityDaoImpl;
import io.github.springstudent.ada.transport.clipboard.pojo.Clipboard;
import org.springframework.stereotype.Repository;

/**
 * @author ZhouNing
 * @date 2024/12/31 16:19
 **/
@Repository
public class ClipboardDaoImpl extends EntityDaoImpl<Clipboard,String> implements ClipboardDao{
}

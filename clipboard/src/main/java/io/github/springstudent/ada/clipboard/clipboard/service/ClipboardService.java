package io.github.springstudent.ada.clipboard.clipboard.service;


import io.github.springstudent.ada.clipboard.clipboard.pojo.Clipboard;

import java.util.List;

/**
 * @author ZhouNing
 * @date 2024/12/31 16:19
 **/
public interface ClipboardService {
    void clear(String deviceCode)throws Exception;

    void save(List<Clipboard> clipboards)throws Exception;

    List<Clipboard> get(String deviceCode)throws Exception;
}

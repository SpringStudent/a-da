package io.github.springstudent.ada.client.core;


/**
 * @author ZhouNing
 * @date 2024/12/9 8:40
 **/
public interface RemoteScreenListener {

    void onMouseMove(int x, int y);

    void onMousePressed(int x, int y, int button);

    void onMouseReleased(int x, int y, int button);

    void onMouseWheeled(int x, int y, int rotations);

    void onKeyPressed(int keyCode, char keyChar);

    void onKeyReleased(int keyCode, char keyChar);
}

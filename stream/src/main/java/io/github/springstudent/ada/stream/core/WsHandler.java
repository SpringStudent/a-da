package io.github.springstudent.ada.stream.core;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Map;
import java.util.concurrent.*;

/**
 * @author ZhouNing
 * @date 2025/1/20 16:29
 **/
@Component
public class WsHandler extends BinaryWebSocketHandler {

    private Map<String, CopyOnWriteArrayList<WebSocketSession>> clients = new ConcurrentHashMap<>();

    private final ScheduledExecutorService cleanupExecutor = Executors.newSingleThreadScheduledExecutor();

    @PostConstruct
    public void init() {
        cleanupExecutor.scheduleAtFixedRate(this::cleanupEmptyLists, 1, 1, TimeUnit.MINUTES);
    }

    @PreDestroy
    public void destroy() {
        cleanupExecutor.shutdown();
    }

    private void cleanupEmptyLists() {
        clients.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Map<String, String> paramMap = RequestUtils.parseRequestParam(session.getUri().toString());
        String cameraId = paramMap.get("id");
        clients.computeIfAbsent(cameraId, k -> new CopyOnWriteArrayList()).add(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        clients.values().forEach(webSocketSessions -> webSocketSessions.remove(session));
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        clients.values().forEach(webSocketSessions -> webSocketSessions.remove(session));
    }

    public void sendData(byte[] data, String id) {
        try {
            CopyOnWriteArrayList<WebSocketSession> webSocketSessions = clients.get(id);
            if (webSocketSessions != null && (webSocketSessions.size()) > 0) {
                for (WebSocketSession session : webSocketSessions) {
                    if (session.isOpen()) {
                        Thread.sleep(1);
                        session.sendMessage(new BinaryMessage(data));
                    }
                }
            }
        } catch (Exception e) {
        }
    }

}

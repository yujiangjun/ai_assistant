package com.example.api.config;

import cn.hutool.json.JSONUtil;
import com.example.api.aliyun.init.FixCallMessagePusher;
import com.example.common.WebSocketSession;
import com.example.common.enums.MessageRoleEnum;
import com.example.common.util.SpringContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ServerEndpoint(value = "/call")
@Slf4j
public class WebSocketUtil {
    /**
     * 登录连接数 应该也是线程安全的
     */
    private static int loginCount = 0;
    /**
     * user 线程安全的
     */
    private static final Map<String, WebSocketSession> userMap = new ConcurrentHashMap<>();

    /**
     * @Description: 收到消息触发事件，这个消息是连接人发送的消息
     * @Param [messageInfo, session]
     * @Return: void
     * {
     * "userId": "test2",
     * "message": "你收到了嘛？这是用户test发的消息！"
     * }
     **/
    @OnMessage
    public void onMessage(String messageInfo, Session session) throws IOException, InterruptedException {
//        if (StringUtils.isBlank(messageInfo)) {
//            return;
//        }
//        // 当前用户
//        String userIdTo = session.getPathParameters().get("userId");
//        // JSON数据
//        log.info("onMessage:{}", messageInfo);
//        Map map = JSON.parseObject(messageInfo, Map.class);
//        // 接收人
//        String userId = (String) map.get("userId");
//        // 消息内容
//        String message = (String) map.get("message");
//        // 发送给指定用户
//        sendMessageTo(message, userId);
//        log.info(DateUtil.now() + " | " + userIdTo + " 私人消息-> " + message, userId);
        System.out.println("接收到消息");
    }

    /**
     * @Description: 打开连接触发事件
     * @Param [account, session, config]
     * @Return: void
     **/
    @OnOpen
    public void onOpen(Session session) {
        log.info("ws链接服务端");
        Map<String, List<String>> requestParameterMap = session.getRequestParameterMap();
        String userId = requestParameterMap.get("userId").get(0);
        WebSocketSession webSocketSession = new WebSocketSession();

        webSocketSession.setRole(MessageRoleEnum.CUSTOMER.getCode());
        webSocketSession.setUserId(userId);
        webSocketSession.setSession(session);
        boolean containsKey = userMap.containsKey(userId);
        if (!containsKey) {
            // 添加登录用户数量
            addLoginCount();
            userMap.put(userId, webSocketSession);
        }else {
            userMap.remove(userId);
            userMap.put(userId, webSocketSession);
        }
        FixCallMessagePusher fixCallMessagePusher = SpringContext.getBean("fixCallMessagePusher", FixCallMessagePusher.class);
        fixCallMessagePusher.init();
//        session.getAsyncRemote().sendText("接收到你的链接");
//        try(InputStream ins =this.getClass().getClassLoader().getResourceAsStream("callmes1.json");
//            BufferedInputStream bufferedInputStream = new BufferedInputStream(ins);
//            BufferedReader reader = new BufferedReader(new InputStreamReader(bufferedInputStream));
//        ) {
//            String json = IoUtil.read(reader);
//            List<CallMessage> callMessageList = JSONUtil.toBean(json, new TypeReference<List<CallMessage>>() {}, true);
//
//            for (CallMessage callMessage : callMessageList) {
//                session.getAsyncRemote().sendText(JSONUtil.toJsonStr(callMessage));
//                Thread.sleep(2000);
//            }
//        }catch (Exception e){
//            log.info("e:",e);
//        }
    }
    /**
     * @Description: 关闭连接触发事件
     * @Param [session, closeReason]
     * @Return: void
     **/
    @OnClose
    public void onClose( Session session, CloseReason closeReason) {
//        boolean containsKey = userMap.containsKey(userId);
//        if (containsKey) {
//            // 删除map中用户
//            userMap.remove(userId);
//            // 减少断开连接的用户
//            reduceLoginCount();
//        }
//        log.info("关闭连接触发事件!已断开用户: " + userId);
//        log.info("当前在线人数: " + loginCount);
//        System.out.println("关闭链接");
        userMap.clear();
        log.info("连接关闭了，{}", closeReason.getReasonPhrase());

    }

    /**
     * @Description: 传输消息错误触发事件
     * @Param [error ：错误]
     * @Return: void
     **/
    @OnError
    public void onError(Throwable error) {
        userMap.clear();
        log.info("onError:", error);
    }

    /**
     * @Description: 发送指定用户信息
     * @Param [message：信息, userId：用户]
     * @Return: void
     **/
    public void sendMessageTo(String message, String userId) throws IOException {
        log.info("userMap:{}", JSONUtil.toJsonStr(userMap));
        for (WebSocketSession user : userMap.values()) {
            if (user.getUserId().equals(userId)) {
                log.info("发送消息");
                Session session = user.getSession();
                synchronized (session) {
                    if (session.isOpen()) {
                        log.info("开始发送消息");
                        session.getBasicRemote().sendText(message);
                        log.info("结束发送消息");
                    }
                }
            }
        }
    }

    /**
     * @Description: 发给所有人
     * @Param [message：信息]
     * @Return: void
     **/
//    public void sendMessageAll(String message) throws IOException {
//        for (WebSocket item : userMap.values()) {
//            item.getSession().getAsyncRemote().sendText(message);
//        }
//    }

    /**
     * @Description: 连接登录数增加
     * @Param []
     * @Return: void
     **/
    public static synchronized void addLoginCount() {
        loginCount++;
    }

    /**
     * @Description: 连接登录数减少
     * @Param []
     * @Return: void
     **/
    public static synchronized void reduceLoginCount() {
        loginCount--;
    }

    /**
     * @Description: 获取用户
     * @Param []
     * @Return: java.util.Map<java.lang.String, com.cn.webSocket.WebSocket>
     **/
    public synchronized Map<String, WebSocketSession> getUsers() {
        return userMap;
    }

}
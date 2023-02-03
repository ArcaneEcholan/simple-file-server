package fit.wenchao.http_file_server.wss;

import com.alibaba.fastjson.JSONObject;
import fit.wenchao.http_file_server.constants.BusinessType;
import fit.wenchao.http_file_server.constants.ContentType;
import fit.wenchao.http_file_server.model.chat.Message;
import fit.wenchao.http_file_server.model.chat.User;
import fit.wenchao.http_file_server.utils.IpUtil;
import fit.wenchao.http_file_server.utils.WebsocketUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.net.*;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


@Slf4j
@ServerEndpoint("/wsserver/chat")
//userId：地址的111就是这个userId"ws://localhost
// :8181
@Component
public class ChatWebSocketServer {

    /**
     * 用来记录当前在线连接数。应该把它设计成线程安全的。
     */
    private static int onlineCount = 0;
    /**
     * concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。
     */
    private static ConcurrentHashMap<String, ChatWebSocketServer> webSocketMap = new ConcurrentHashMap<>();
    /**
     * 与某个客户端的连接会话，需要通过它来给客户端发送数据
     */
    private Session session;
    /**
     * 接收userId
     */
    private String userId = "";
    private String ip = "";

    private User user;

    public User getUser() {
        return user;
    }


    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session) throws IOException {

        InetSocketAddress remoteAddress = WebsocketUtil.getRemoteAddress(session);
        System.out.println(remoteAddress.getAddress().getHostAddress());
        String ip = remoteAddress.getAddress().getHostAddress();

        if ("127.0.0.1".equals(ip)) {
            Inet4Address localIp4Address = IpUtil.getLocalIp4Address();
            if (localIp4Address != null) {
                ip = localIp4Address.getHostAddress();
            }
            else {
                throw new RuntimeException("get ip failed");
            }
        }

        System.out.println(ip);
        this.session = session;
        this.ip = ip;
        this.user = new User(UUID.randomUUID().toString(), ip, "hello", 0);

        synchronized (this) {
            if (webSocketMap.containsKey(ip)) {
                webSocketMap.remove(ip);
                webSocketMap.put(ip, this);
                //加入set中
            }
            else {
                webSocketMap.put(ip, this);
                //加入set中
                addOnlineCount();
                //在线数加1
            }
        }


        System.out.println("用户连接:" + ip + ",当前在线人数为:" + getOnlineCount());
        List<User> onlineUserList = getOnlineUsers();
        try {
            sendBack(ContentType.OBJECT,
                    BusinessType.GetOnlineUsersResponse,
                    onlineUserList);
        }
        catch (IOException e) {
            System.out.println("用户:" + userId + ",网络异常!!!!!!");
        }

        Message message = new Message();
        message.setBusiness(BusinessType.UserOnline.toString());
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("ip", ip);
        message.setContent(jsonObject);
        sendMessages2All(message);
        //String json = JSONObject.toJSONString(message);
        //Enumeration<String> keys = webSocketMap.keys();
        //while (keys.hasMoreElements()) {
        //    String curIp = keys.nextElement();
        //    ChatWebSocketServer chatWebSocketServer = webSocketMap.get(curIp);
        //    if (chatWebSocketServer != null) {
        //        if (!chatWebSocketServer.ip.equals(ip)) {
        //            try {
        //                chatWebSocketServer.session.getBasicRemote()
        //                                           .sendText(json);
        //            }
        //            catch (IOException e) {
        //                e.printStackTrace();
        //            }
        //        }
        //    }
        //}
    }

    private List<User> getOnlineUsers() {

        return webSocketMap.values()
                           .stream()
                           .map((socketServer) -> {
                               User user1 = socketServer.getUser();
                               if (ip.equals(user1.getIp())) {
                                   user1.setIfThisDev(1);
                               }
                               else {
                                   user1.setIfThisDev(0);
                               }
                               return user1;
                           })
                           .collect(Collectors.toList());
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() {
        log.info("onClose");
        synchronized (this) {
            if (webSocketMap.containsKey(ip)) {
                webSocketMap.remove(ip);
                subOnlineCount();
            }
        }

        Message message = new Message();
        message.setBusiness(BusinessType.UserOffline.toString());
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("ip", ip);
        message.setContent(jsonObject);
        sendMessages2All(message);

        System.out.println("用户退出:" + userId + ",当前在线人数为:" + getOnlineCount());
    }

    ///**
    // * 收到客户端消息后调用的方法
    // *
    // * @param message 客户端发送过来的消息*/
    @OnMessage
    public void onMessage(String message, Session session) throws IOException {
        System.out.println("用户消息:" + userId + ",报文:" + message);
        if (message == null) {
            sendBack(null, BusinessType.Exception, "params error");
            return;
        }

        Message messageObject = JSONObject.parseObject(message, Message.class);

        if (messageObject == null) {
            sendBack(null, BusinessType.Exception, "params error");
            return;
        }

        String businessString = messageObject.getBusiness();
        messageObject.setFrom(ip);
        BusinessType businessType = BusinessType.fromString(businessString);

        if (businessType.equals(BusinessType.SendText)) {
            InetSocketAddress remoteAddress = WebsocketUtil.getRemoteAddress(session);
            System.out.println(remoteAddress.getAddress().getHostAddress());
            String ip = remoteAddress.getAddress().getHostAddress();

            if ("127.0.0.1".equals(ip)) {
                Inet4Address localIp4Address = IpUtil.getLocalIp4Address();
                if (localIp4Address != null) {
                    ip = localIp4Address.getHostAddress();
                }
                else {
                    throw new RuntimeException("get ip failed");
                }
            }
            messageObject.setFrom(ip);
            sendMessage(messageObject);
            return;
        }


        ////可以群发消息
        ////消息保存到数据库、redis
        //if(StringUtils.isNotBlank(message)){
        //    try {
        //        //解析发送的报文
        //        JSONObject jsonObject = JSON.parseObject(message);
        //        //追加发送人(防止串改)
        //        jsonObject.put("fromUserId",this.userId);
        //        String toUserId=jsonObject.getString("toUserId");
        //        //传送给对应toUserId用户的websocket
        //        if(StringUtils.isNotBlank(toUserId)&&webSocketMap.containsKey(toUserId)){
        //            webSocketMap.get(toUserId).sendMessage(jsonObject.toJSONString());
        //        }else{
        //            System.out.println("请求的userId:"+toUserId+"不在该服务器上");
        //            //否则不在这个服务器上，发送到mysql或者redis
        //        }
        //    }catch (Exception e){
        //        e.printStackTrace();
        //    }
        //}
    }

    /**
     * @param session
     * @param error
     */
    @OnError
    public void onError(Session session, Throwable error) throws IOException {
        log.info("onError");
        log.info("session: {} error", session);
        error.printStackTrace();
    }

    /**
     * 实现服务器主动推送
     */
    public void sendMessage(ContentType contentType,
                            BusinessType businessType,
                            Object data,
                            String from,
                            String target) throws IOException {
        if (contentType.equals(ContentType.OBJECT)) {
            Message message = new Message(contentType.toString(),
                    businessType.toString(), from,
                    target, data);
            String json = JSONObject.toJSONString(message);
            ChatWebSocketServer chatWebSocketServer = webSocketMap.get(target);
            chatWebSocketServer.session.getBasicRemote().sendText(json);
        }
        else {
            log.error("content type :{} not support yet", ContentType.FILE);
        }
    }

    public void sendMessage(Message message) throws IOException {
        String json = JSONObject.toJSONString(message);
        ChatWebSocketServer chatWebSocketServer =
                webSocketMap.get(message.getTarget());
        if (chatWebSocketServer != null) {
            chatWebSocketServer.session.getBasicRemote().sendText(json);
        }
    }

    public void sendMessages2All(Message message) {
        String json = JSONObject.toJSONString(message);
        webSocketMap.values().stream().forEach((chatWebSocketServer -> {
            try {
                if (chatWebSocketServer != null) {
                    chatWebSocketServer.session.getBasicRemote().sendText(json);
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }));

    }

    public void sendBack(ContentType contentType, BusinessType businessType,
                         Object data) throws IOException {
        if (contentType.equals(ContentType.OBJECT)) {
            Message message = new Message(contentType.toString(), businessType.toString(), null,
                    ip, data);
            String json = JSONObject.toJSONString(message);
            this.session.getBasicRemote().sendText(json);
        }
        else {
            log.error("content type :{} not support yet", ContentType.FILE);
        }
    }


    ///**
    // * 发送自定义消息
    // * */
    //public static void sendInfo(String message,@PathParam("userId") String userId) throws IOException {
    //    System.out.println("发送消息到:"+userId+"，报文:"+message);
    //    if(StringUtils.isNotBlank(userId)&&webSocketMap.containsKey(userId)){
    //        webSocketMap.get(userId).sendMessage(message);
    //    }else{
    //        System.out.println("用户"+userId+",不在线！");
    //    }
    //}
    //
    public static synchronized int getOnlineCount() {
        return onlineCount;
    }

    public static synchronized void addOnlineCount() {
        ChatWebSocketServer.onlineCount++;
    }

    public static synchronized void subOnlineCount() {
        ChatWebSocketServer.onlineCount--;
    }
}

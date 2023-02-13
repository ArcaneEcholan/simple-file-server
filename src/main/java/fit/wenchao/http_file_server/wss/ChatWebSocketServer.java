package fit.wenchao.http_file_server.wss;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import fit.wenchao.http_file_server.constants.BusinessType;
import fit.wenchao.http_file_server.constants.ContentType;
import fit.wenchao.http_file_server.model.chat.Message;
import fit.wenchao.http_file_server.model.chat.User;
import fit.wenchao.http_file_server.utils.WebsocketUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static fit.wenchao.http_file_server.utils.json.Json.json;
import static fit.wenchao.http_file_server.utils.json.Pair.pair;


@Slf4j
@ServerEndpoint("/wsserver/chat")
//userId：地址的111就是这个userId"ws://localhost
// :8181
@Component
public class ChatWebSocketServer {

    public static void main(String[] args) {
        System.out.println(JSONObject.toJSONString(json(pair("you",
                new User()))));
    }

    /**
     * 用来记录当前在线连接数。应该把它设计成线程安全的。
     */
    private static int onlineCount = 0;
    /**
     * concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。
     */

    private static Rooms rooms = new Rooms();
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

    public String getUserId() {
        return userId;
    }

    public String getIp() {
        return ip;
    }

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session) throws IOException {

        InetSocketAddress remoteAddress = WebsocketUtil.getRemoteAddress(session);
        System.out.println(remoteAddress.getAddress().getHostAddress());
        String ip = remoteAddress.getAddress().getHostAddress();

        this.user = new User(UUID.randomUUID().toString(), ip, pickAName(), 0);

        this.session = session;
        this.ip = ip;
        this.userId = user.getId();
        log.debug("user: <{}> online, room: <{}>", this.user.getName(),
                this.user.getIp());

        rooms.joinNewUser(this, () -> {
            sendBack(null, BusinessType.TOO_CROWD, null);
        });
        addOnlineCount();

        JSONObject userOnlineResp =
                json(
                        pair("you", this.user),
                        pair("onlineUsers", usersInRoom(ip))

                );

        try {
            sendBack(ContentType.OBJECT,
                    BusinessType.GetOnlineUsersResponse,
                    userOnlineResp);
        }
        catch (IOException e) {
            System.out.println("用户:" + userId + ",网络异常!!!!!!");
        }

        Message message = new Message();
        message.setBusiness(BusinessType.UserOnline.toString());
        userOnlineResp = json(pair("ip", ip),
                pair("id", this.user.getId()),
                pair("name", this.user.getName())
                );
        message.setContent(userOnlineResp);
        sendMessages2All(message, ip);

    }

    private synchronized String pickAName() {
        return rooms.getANameFromRoom(ip);
    }

    private boolean oneInThisRoomAlreadyHasThisName(String simpleName) {
        List<User> users = rooms.usersInRoom(ip);
        for (User user : users) {
            if (user.getName().equals(simpleName)) {
                return true;
            }
        }

        return false;
    }

    private List<User> usersInRoom(String ip) {
        return rooms.usersInRoom(ip);
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() {
        log.info("onClose");
        rooms.oneLeaveRoom(ip, this.getUserId());
        //synchronized (this) {
        //    if (webSocketMap.containsKey(ip)) {
        //        webSocketMap.remove(ip);
        //        subOnlineCount();
        //    }
        //}

        Message message = new Message();
        message.setBusiness(BusinessType.UserOffline.toString());
        message.setContent(json(pair("id", this.user.getId())));
        sendMessages2All(message, ip);

        System.out.println("用户退出:" + this.user.getId() + ",当前在线人数为:" + getOnlineCount());
    }

    ///**
    // * 收到客户端消息后调用的方法
    // *
    // * @param message 客户端发送过来的消息*/
    @OnMessage
    public void onMessage(String message, Session session) throws IOException {
        System.out.println("用户消息:" + this.user.getId() + ",报文:" + message);
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
        messageObject.setFrom(this.user.getId());
        BusinessType businessType = BusinessType.fromString(businessString);

        if (businessType.equals(BusinessType.SendText)) {
            InetSocketAddress remoteAddress = WebsocketUtil.getRemoteAddress(session);
            System.out.println(remoteAddress.getAddress().getHostAddress());
            String ip = remoteAddress.getAddress().getHostAddress();
           
            sendMessage(messageObject, ip, messageObject.getTarget());
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


    public void sendMessage(Message message, String ip, String id) {
        rooms.forOneInRoom(ip, id, chatWebSocketServer -> {
                    String json = JSONObject.toJSONString(message);
                    chatWebSocketServer.session.getBasicRemote().sendText(json);
                }
        );
    }

    public void sendMessages2All(Message message, String ip) {

        String json = JSONObject.toJSONString(message);
        rooms.foreachOneInRoom(ip, (chatWebSocketServer -> {
            chatWebSocketServer.session.getBasicRemote().sendText(json);
        }));
        //webSocketMap.values().stream().forEach((chatWebSocketServer -> {
        //    try {
        //        if (chatWebSocketServer != null) {
        //            chatWebSocketServer.session.getBasicRemote().sendText(json);
        //        }
        //    }
        //    catch (IOException e) {
        //        e.printStackTrace();
        //    }
        //}));

    }

    public void sendBack(ContentType contentType, BusinessType businessType,
                         Object data) throws IOException {
        if (contentType.equals(ContentType.OBJECT)) {
            Message message = new Message(contentType.toString(), businessType.toString(), null,
                    null, data);
            String json = JSONObject.toJSONString(message, SerializerFeature.DisableCircularReferenceDetect);
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

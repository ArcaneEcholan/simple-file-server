package fit.wenchao.http_file_server.wss;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Slf4j
@ServerEndpoint("/wsserver")//userId：地址的111就是这个userId"ws://localhost:8181/wsserver/111"
@Component
public class WebSocketServer {


    static long count = 0;
    static long size = 0;

    static long dirCount = 0;

    static Map<String, DirInfo> map = new HashMap<>();

    private static boolean isSymbolicLink(File f) throws IOException {
        return !f.getAbsolutePath()
                 .equals(f.getCanonicalPath());
    }

    public static boolean isFile(File f) throws IOException {
        return isSymbolicLink(f) || f.isFile();
    }

    public static boolean isDirectory(File f) throws IOException {
        return isFile(f);
    }

    public static DirInfo scan(File targetDir) throws IOException {
        File[] files = null;
        ;
        if (targetDir.getAbsolutePath()
                     .startsWith("/proc")
                || targetDir.getAbsolutePath()
                            .startsWith("/dev")
                || targetDir.getAbsolutePath()
                            .startsWith("/sys")
                || isFile(targetDir)
                || (files = targetDir.listFiles()) == null
        ) {
            return null;
        }


        DirInfo dirInfo = new DirInfo();
        long curDirSize = 0;
        long fileSize = 0;
        for (File item : files) {

            if (isFile(item)) {

                long length = item.length();

                curDirSize += length;
                fileSize++;

                continue;
            }

            dirCount++;

            DirInfo nestDirInfo = scan(item);
            if (nestDirInfo != null) {
                map.put(item.getAbsolutePath(), nestDirInfo);
                long nestDirSize = nestDirInfo.getLength();
                long nestFileSize = nestDirInfo.getNumberOfFiles();
                curDirSize += nestDirSize;
                fileSize += nestFileSize;
            }
        }

        dirInfo.setLength(curDirSize);
        dirInfo.setNumberOfFiles(fileSize);
        return dirInfo;
    }

    static long before = 0;
    static long after = 0;



    static DiskAnalyzingContext diskAnalyzingContext;

    @Autowired
    public void setDiskAnalyzingContext(DiskAnalyzingContext diskAnalyzingContext) {
        WebSocketServer.diskAnalyzingContext = diskAnalyzingContext;
    }

 
    /**用来记录当前在线连接数。应该把它设计成线程安全的。*/
    private static int onlineCount = 0;
    /**concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。*/
    private static ConcurrentHashMap<String,WebSocketServer> webSocketMap = new ConcurrentHashMap<>();
    /**与某个客户端的连接会话，需要通过它来给客户端发送数据*/
    private Session session;
    /**接收userId*/
    private String userId="";
 
    /**
     * 连接建立成功调用的方法*/
    @OnOpen
    public void onOpen(Session session,@PathParam("userId") String userId) {
        try {
            diskAnalyzingContext.startAnalyzing();
        }
        catch (Exception e) {
            while(!diskAnalyzingContext.analyzing()) {
                diskAnalyzingContext.startAnalyzing();
            }
            //throw new RuntimeException(e);
        }
        log.debug("start disk analyzing");
        //this.session = session;
        //this.userId=userId;
        //if(webSocketMap.containsKey(userId)){
        //    webSocketMap.remove(userId);
        //    webSocketMap.put(userId,this);
        //    //加入set中
        //}else{
        //    webSocketMap.put(userId,this);
        //    //加入set中
        //    addOnlineCount();
        //    //在线数加1
        //}
        //
        //System.out.println("用户连接:"+userId+",当前在线人数为:" + getOnlineCount());
        //try {
        //    sendMessage("连接成功");
        //} catch (IOException e) {
        //    System.out.println("用户:"+userId+",网络异常!!!!!!");
        //}
    }
 
    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() {
        diskAnalyzingContext.endAnalyzing();
        log.debug("end disk analyzing");
        //if(webSocketMap.containsKey(userId)){
        //    webSocketMap.remove(userId);
        //    //从set中删除
        //    subOnlineCount();
        //}
        //System.out.println("用户退出:"+userId+",当前在线人数为:" + getOnlineCount());
    }
 
    ///**
    // * 收到客户端消息后调用的方法
    // *
    // * @param message 客户端发送过来的消息*/
    //@OnMessage
    //public void onMessage(String message, Session session) {
    //    System.out.println("用户消息:"+userId+",报文:"+message);
    //    //可以群发消息
    //    //消息保存到数据库、redis
    //    if(StringUtils.isNotBlank(message)){
    //        try {
    //            //解析发送的报文
    //            JSONObject jsonObject = JSON.parseObject(message);
    //            //追加发送人(防止串改)
    //            jsonObject.put("fromUserId",this.userId);
    //            String toUserId=jsonObject.getString("toUserId");
    //            //传送给对应toUserId用户的websocket
    //            if(StringUtils.isNotBlank(toUserId)&&webSocketMap.containsKey(toUserId)){
    //                webSocketMap.get(toUserId).sendMessage(jsonObject.toJSONString());
    //            }else{
    //                System.out.println("请求的userId:"+toUserId+"不在该服务器上");
    //                //否则不在这个服务器上，发送到mysql或者redis
    //            }
    //        }catch (Exception e){
    //            e.printStackTrace();
    //        }
    //    }
    //}
 
    /**
     *
     * @param session
     * @param error
     */
    @OnError
    public void onError(Session session, Throwable error) throws IOException {
        diskAnalyzingContext.endAnalyzing();
        log.debug("end disk analyzing");
        error.printStackTrace();
        session.close();
    }

    /**
     * 实现服务器主动推送
     */
    public void sendMessage(String message) throws IOException {
        this.session.getBasicRemote().sendText(message);
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
        WebSocketServer.onlineCount++;
    }
 
    public static synchronized void subOnlineCount() {
        WebSocketServer.onlineCount--;
    }
}

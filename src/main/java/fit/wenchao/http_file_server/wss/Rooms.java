package fit.wenchao.http_file_server.wss;

import fit.wenchao.http_file_server.function.Consumer;
import fit.wenchao.http_file_server.model.chat.User;
import fit.wenchao.http_file_server.utils.name.EnglishName;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Rooms {
    Map<String, Map<String, ChatWebSocketServer>> rooms = new HashMap<>();

    public synchronized List<User> usersInRoom(String ip) {
        Map<String, ChatWebSocketServer> chatWebSocketServers = rooms.get(ip);
        return chatWebSocketServers.values().stream()
                                   .map((ChatWebSocketServer::getUser))
                                   .collect(Collectors.toList());
    }

    public synchronized void oneLeaveRoom(String ip, String id) {
        Map<String, ChatWebSocketServer> chatWebSocketServers = rooms.get(ip);
        if (chatWebSocketServers != null) {
            chatWebSocketServers.remove(id);
            if (chatWebSocketServers.isEmpty()) {
                rooms.remove(ip);
            }
        }

    }

    Map<String, AtomicInteger> roomsNameIndex = new HashMap<>();

    public synchronized String getANameFromRoom(String ip) {
        if (roomsNameIndex.get(ip) == null) {
            roomsNameIndex.put(ip, new AtomicInteger(1));
            return EnglishName.getSingle().getIndexName(0);
        }
        else {
            int andAdd = roomsNameIndex.get(ip).getAndAdd(1);
            if (andAdd >= 200) {
                roomsNameIndex.get(ip).set(0);
                return EnglishName.getSingle().getIndexName(0);
            }
            return EnglishName.getSingle().getIndexName(andAdd);
        }
    }


    public synchronized void forOneInRoom(String ip, String id,
                                          Consumer<ChatWebSocketServer> actionForUser) {
        Map<String, ChatWebSocketServer> chatWebSocketServers = rooms.get(ip);
        ChatWebSocketServer chatWebSocketServer = chatWebSocketServers.get(id);
        if (chatWebSocketServer != null) {
            try {
                actionForUser.accept(chatWebSocketServer);
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void foreachOneInRoom(String ip, Consumer<ChatWebSocketServer> actionForUser) {
        Map<String, ChatWebSocketServer> chatWebSocketServers = rooms.get(ip);
        if(chatWebSocketServers != null)  {
            chatWebSocketServers.values().forEach(chatWebSocketServer -> {
                try {
                    actionForUser.accept(chatWebSocketServer);
                }
                catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    public static interface VoidFunc {
        void invoke() throws Exception;
    }

    public synchronized void joinNewUser(ChatWebSocketServer chatWebSocketServer,
                                         VoidFunc tooCrowd) {

        String ip = chatWebSocketServer.getUser().getIp();

        Map<String, ChatWebSocketServer> chatWebSocketServers = rooms.get(ip);
        if (chatWebSocketServers == null) {
            rooms.put(ip, new HashMap<>());
            chatWebSocketServers = rooms.get(ip);
        }

        if (chatWebSocketServers.size() >= 100) {
            try {
                tooCrowd.invoke();
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
            return;
        }

        chatWebSocketServers.put(chatWebSocketServer.getUserId(), chatWebSocketServer);
    }
}

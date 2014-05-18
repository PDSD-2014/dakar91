
import com.example.mywapp.*;
import com.example.mywapp.User;
import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {
    /* Atribute I/O  */
    private final static int MAX_CLIENTS = 6;
    private final static int PORT = 9000;
    private static ServerSocket serverSocket;
    private static Socket clientSocket;
    
    /* Atribute pentru clienti */
    private static ArrayList<User> _users = new ArrayList<>();
    private static ArrayList<User> onlineUsers = new ArrayList<>();
    
    
    public static synchronized boolean addMessage (SendMessage req, int userId) {
        for (User u : _users) {
            if (u.getImei().equals(req.toId))
                return u.addMessage(req.msg);
        }
        return false;
    }
    
    public static synchronized int addUser (User usr) {
        _users.add(usr);
        return _users.size() - 1;
    }
    
    public static int existsUser (String id) {
        for (int i = 0; i < _users.size(); i++) {
            if (_users.get(i).getImei().equals(id))
                return i;
        }
        return -1;
    }
    
    public synchronized static boolean login (String imei, String password) {
        int index;
        if ((index = existsUser(imei)) == -1) {
            onlineUsers.add(_users.get(addUser(new User(imei, SecurePasswordGenerator.nextPassword(), "User" + _users.size()))));
        } else {
            if (_users.get(index).checkPassword(password))
                onlineUsers.add(_users.get(index));
            else 
                return false;
        }
        
        return true;
    }
    
    public static ArrayList<User> getUsers() {
        return _users;
    }
    
    public static synchronized void logout (String imei) {
        for (int i = 0; i < onlineUsers.size(); i++) {
            if (onlineUsers.get(i).getImei().equals(imei)) {
                onlineUsers.remove(i);
                break;
            }
        }
    }
    
    public static synchronized ArrayList<User> getOnlineUsers() {
        return onlineUsers;
    }
        
    public static int getUserId (String imei) {
        int i = 0;
        for (User u : _users) {
            if (u.getImei().equals(imei))
                return i;
            i ++;
        }
        
        return -1;
    }
    
    public static void main (String args[]){
        System.out.println("Serverul a pornit\n\n");
        try {
            serverSocket = new ServerSocket(PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        while (true) {
            try {
                new ClientHandler(serverSocket.accept()).start();
                System.out.println("A venit o noua conexiune");                
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

class ClientHandler extends Thread {
    private final Socket socket;
    private InputStream is;
    private OutputStream os;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;
    private boolean isOnline = true;
    private boolean authenticated = false;
    private int userId;
    
    public ClientHandler (Socket clientSocket) {
        this.socket = clientSocket;
        
        try {
            is = socket.getInputStream();
            os = socket.getOutputStream();
            ois = new ObjectInputStream(is);
            oos = new ObjectOutputStream(os);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void run () { 
        try {
            while(isOnline) {                
                System.out.println("\n\nIntru in While!");  
                Request r = (Request)ois.readObject();
                treatRequest(r);
                //System.out.println("Am primit un request!");               
            }
        } catch(Exception e) {
             e.printStackTrace();
        } finally {
            try {
                if (ois != null) ois.close();
                if (is != null) is.close();
                if (oos != null) oos.close();
                if (os != null) os.close();
                if (socket != null) socket.close();
                System.out.println("Am inchis toate streamurile!\n\n\n");
            } catch(IOException e) {
                System.out.println("Couldn't close I/O streams");
            }
        }      
    }
    
    private boolean treatRequest (Request request) {
        switch(request.type) {
            case ONLINE:
                Online on = (Online)request;
                if (!authenticated && Server.login(on.fromId, null)) {
                    authenticated = true;
                    userId = Server.getUserId(on.fromId);
                    System.out.println("Utilizatorul " + on.fromId + " este online!\t" + new Date().toString());
                } else {
                    System.out.println("Login esuat pentru utilizator " + on.fromId);
                }
                return authenticated;
                
            case OFFLINE:
                if (authenticated) {
                    isOnline = false;
                    Server.logout(request.fromId);
                    System.out.println("Utilizatorul " + request.fromId + " este offline\t" + new Date().toString());
                    return true;
                } else 
                    return false;
                
                
            case GET_USERS:
                if (authenticated) {
                    System.out.println("Utilizatorul " + request.fromId + " a cerut lista de useri\t" + new Date().toString());
                    for (User u : Server.getOnlineUsers())
                        System.out.println(u.getImei());
                    try {
                        oos.writeObject(new UsersResponse(null, null, (ArrayList<User>)Server.getOnlineUsers().clone()));
                        System.out.println("Am scris pe socket lista de useri");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return true;
                } else 
                    return false;
               
            case MESSAGE:
                if (authenticated) {
                    System.out.println("Utilizatorul " + request.fromId + " a trimis un mesaj\t" + new Date().toString());
                    return Server.addMessage((SendMessage)request, userId);
                } else 
                    return false;
                
            case GET_MESSAGES:
                if (authenticated) {
                    System.out.println("Utilizatorul " + request.fromId + " a cerut mesajele cu utilizatorul " + request.toId + "\t" + new Date().toString());
                    try {
                        oos.writeObject(new MessagesResponse(null, null, Server.getUsers().get(userId).getMessages(request.toId)));
                        System.out.println("Am scris pe socket lista de mesaje\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return true;
                } else 
                    return false;
        }
        
        return false;
    }
}




final class SecurePasswordGenerator {
  private static SecureRandom random = new SecureRandom();

  public static String nextPassword() {
    return new BigInteger(40, random).toString(32);
  }
}

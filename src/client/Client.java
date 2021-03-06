package client;

import remoteobj.PubSubService;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.RemoteException;

/**
 * Created by balan016 on 2/8/18.
 */
public class Client {
    private int serverAssignedId;
    private PubSubService pubSubService;
    private UdpSubscriptionReceiver udpSubscriptionReceiver;
    private int udpListenerPort;
    private ServerPinger serverPinger;
    private String remoteURL = "//localhost/PubSubService";

    public Client(){
        serverAssignedId = -1;
        udpListenerPort = -1;
        try {
            pubSubService = (PubSubService) Naming.lookup(remoteURL);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getClientId(){
        return this.serverAssignedId;
    }

    public void setUdpListenerPort(int port) {
        udpListenerPort = port;
    }

    public void joinGroupServer(){
        if(serverAssignedId==-1){
            try {
                udpSubscriptionReceiver = new UdpSubscriptionReceiver(this);
                udpSubscriptionReceiver.start();
                serverAssignedId = pubSubService.join(InetAddress.getLocalHost(), udpListenerPort);
                if(serverAssignedId != -1){
                    System.out.println("CLIENT"+serverAssignedId+": Joined Group server. Id is "+serverAssignedId);
                    System.out.println("CLIENT"+serverAssignedId+": Listening on port "+udpListenerPort);
                    serverPinger = new ServerPinger(this,udpSubscriptionReceiver);
                    serverPinger.start();
                }
                else {
                    System.out.println("ERROR: Join unsuccessful. Maximum number of clients connected");
                    udpSubscriptionReceiver.closeSocket();
                }

            } catch (NullPointerException e) {
                System.out.println("ERROR: Join unsuccessful. Maximum number of clients connected");
                serverAssignedId = -1;
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        else {
            System.out.println("CLIENT "+serverAssignedId+": Client is already connected to group server");
        }
    }

    public void leaveGroupServer(){
        if(serverAssignedId==-1){
            System.out.println("ERROR: No connection was previously established");
        }
        else{
            try {
                udpSubscriptionReceiver.closeSocket();
                pubSubService.leave(InetAddress.getLocalHost(), udpListenerPort);
                System.out.println("CLIENT "+serverAssignedId+": Client has successfully disconnected");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void publish(String article) {
        if(serverAssignedId!=-1){
            System.out.println("CLIENT "+serverAssignedId+": Publishing "+article);
            try {
                if(pubSubService.publish(article, InetAddress.getLocalHost(), udpListenerPort) == -1)
                    System.out.println("Invalid publish article");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else {
            System.out.println("ERROR: Client must join group server before publishing");
        }
    }

    public void subscribe (String articleString) {
        if(serverAssignedId!=-1){
            System.out.println("CLIENT "+serverAssignedId+": Subscribing "+articleString);
            try {
                if(pubSubService.subscribe(articleString, InetAddress.getLocalHost(), udpListenerPort) == -1)
                    System.out.println("Invalid subscribe article");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else {
            System.out.println("ERROR: Client must join group server before subscribing");
        }
    }

    public void unsubscribe (String articleString) {
        if(serverAssignedId!=-1){
            System.out.println("CLIENT "+serverAssignedId+": Unsubscribing "+articleString);
            try {
                if(pubSubService.unsubscribe(articleString, InetAddress.getLocalHost(), udpListenerPort) == -1)
                    System.out.println("Invalid unsubscribe article");;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else {
            System.out.println("ERROR: Client must join group server before subscribing");
        }
    }

    public void pingServer() throws Exception {
        pubSubService.ping();
    }
}

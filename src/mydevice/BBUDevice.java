package mydevice;

import core.Config;
import core.Config.STATE;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import ui.ManagerDevice;

/**
 * BBU的消息逻辑控制
 * @author tbingo
 */
public class BBUDevice extends ServerSocket implements Runnable{
    /**
     * 设备类型
     */
    private String device_type = "BBU";

    /**
     * 设备唯一标示
     */
    private int device_id = 0;

    /**
     * bbu状态
     */
    private Config.STATE state = Config.STATE.LISTEN;

    /**
     * 获取设备ID号
     * @return 设备的唯一标识ID
     */
    public int getDevice_id() {
        return device_id;
    }

    /**
     * 获取设备的类型字符串
     * @return 设备类型字符串
     */
    public String getDevice_type() {
        return device_type;
    }

    /**
     * 获取设备运行状态
     * @return 设备运行状态
     */
    public STATE getState() {
        return state;
    }

    /**
     * 与界面交互的类
     */
    private ManagerDevice managerDevice = null;

    /**
     * 得到连接的状态
     * @param id 连接的id
     * @return 连接的状态
     */
    public STATE getLinkState(int id){
        Object object = serverThreads.get(id);
        if (object == null) {
            return STATE.CLOSED;
        }else{
            return ((BBUThread)object).getState();
        }
    }

    /**
     * 返回BBU上的连接数
     * @return BBU上的连接数
     */
    public int getLinkCount(){
        return serverThreads.size();
    }
    
    /**
     * 客户端连接 前键为对应的rru的设备号 值为连接的引用
     */
    private HashMap<Integer, BBUThread> serverThreads = new HashMap<Integer, BBUThread>();

    /**
     * 构造函数
     * @param manager 与界面交互的类
     * @throws Exception 监听异常
     */
    public BBUDevice(ManagerDevice manager) throws Exception {

        super(Config.SERVER_PORT);
        managerDevice = manager;
        new Thread(this).start(); //监听socket连接线程启动
    }
    
    /**
     * 发送消息给特定的客户端
     * @param msg 消息内容
     * @param clients 客户端的列表
     */
    private void sendMessageTo(String msg, List<Integer> clients){
        for (int i = 0; i < clients.size(); i++) {
            BBUThread bBUThread = serverThreads.get(clients.get(i));
            if (bBUThread != null) {
                bBUThread.sendMessage(msg);
            }
        }
    }
    
    /**
     * 判断ID 为id的设备连接没
     * @param id 设备id
     * @return 是否连接
     */
   public boolean isConnect(int id){
       BBUThread bBUThread = serverThreads.get(new Integer(id));
       if(bBUThread == null){
           return false;
       }
       return true;
   }
   
    /**
     * 发送广播消息
     * @param excepted 广播的源头客户端id，如果没有 该值为-1
     * @param msg 广播消息内容
     */
    public void sendBroadcastMsg(int excepted, String msg){
        List<Integer> clients = new LinkedList<Integer>();
        if (excepted >= 0) {
            Config.consoleOutput("BBU收到了广播消息" + msg);
        }
        // 服务器打包
        msg = "2#"+device_id+"#"+device_type+"#"+msg;        
        Iterator iter = serverThreads.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            Object keyO = entry.getKey();
            int keyI = (Integer)keyO;
            if (keyI != excepted) {
                clients.add(keyI);
            }
        }
        sendMessageTo(msg, clients);
    }

    /**
     * 发送控制和查询消息
     * @param msg 消息内容
     * @param rruId 客户端的id
     */
    public void sendMessageToSingleRRU(String msg, int rruId){
      
        List<Integer> list = new LinkedList<Integer>();
        list.add(rruId);
        sendMessageTo(msg, list);
    }

    /**
     * 破坏设备
     */
    public void brokeIt(){
        Iterator iter = serverThreads.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            Object keyO = entry.getKey();
            BBUThread val = (BBUThread) entry.getValue();
            val.brokeIt();
        }
        //serverThreads.clear();
        state = Config.STATE.BROKEN;
        Config.consoleOutput("OHhhhhh, My Goddddd, 服务器挂啦！");
    }

    /**
     * 删除某个客户端连接
     * @param key 设备id
     */
    public void removeASocket(int key){
        serverThreads.remove(key);
    }

    /**
     * 服务器的监听线程
     */
    public void run() {
        try
        {
            while (true)
            {
                Socket socket = accept();
                BBUThread bBUThread = new BBUThread(this, socket, managerDevice);
                serverThreads.put( bBUThread.getRRUId(), bBUThread);
                //设置光纤连接状态
                managerDevice.setBbuFibStatus(bBUThread.getRRUId(), true);
            }
        }catch (Exception e){
        }
        finally
        {
            try {
                close();
            } catch (IOException ex) {
            }
        }
    }

    /**
     * 添加信息
     * @param msg 信息内容
     */
    public void appendMessage(String msg){
        managerDevice.appendMessageToBBU(msg);
    }

    /**
     * 设置应用层连接状态
     * @param id 设备id
     * @param flags 是否连接
     */
    public void setConnectStatus(int id,boolean flags){
        managerDevice.setBbuAppStatus(id, flags);
    }
     /**
     * 设置光纤连接状态
     * @param id 设备id
     * @param flags 是否连接
     */
    public void setFibConnectStatus(int id,boolean flags){
        managerDevice.setBbuFibStatus(id, flags);
    }

}

package mydevice;

import core.Config;
import core.Config.ACTION;
import core.Config.STATE;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import ui.ManagerDevice;

/**
 * BBU与RRU之间的连接控制
 * @author tbingo
 */
public class BBUThread implements Runnable{
    /**
     * 对应的服务器设备
     */
    private BBUDevice bbuDevice = null;

    /**
     * 连接客户端的id号
     */
    private int RRUId = 0;

    /**
     * 为每一个客户端分配的一个socket
     */
    private Socket client = null;

    /**
     * 读入流
     */
    private BufferedReader in = null;

    /**
     * 写出流
     */
    private PrintWriter out = null;

    /**
     * 设备当前正在进行什么操作，由于是自动建立连接，初始状态就是三次握手
     */
    private ACTION action = ACTION.THREE_HAND_CONNECT;

    /**
     * 标识该连接的当前状态
     */
    private STATE state = STATE.LISTEN;

    /**
     * 三次握手是否超时
     */
    private boolean isOverdue = false;

    /**
     * 保活机制变量
     */
    private boolean isAlive = true;

    /**
     * 保活计时器
     */
    private Timer keepALiveTimer = new Timer();

    /**
     * 错误信息，如果没有错误则为空
     */
    private String errorMsg = "";
    /**
     * 与界面交互的类
     */
    private ManagerDevice manager = null;
    
    /**
     * 设置动作
     * @param action 动作
     */
    public void setAction(ACTION action) {
        this.action = action;
    }
    
    /**
     * 返回连接的RRUid值
     * @return RRUid值
     */
    public int getRRUId() {
        return RRUId;
    }

    /**
     * 返回设备运行状态
     * @return 设备运行状态
     */
    public STATE getState() {
        return state;
    }

    /**
     * 构造函数
     * @param _bbuDevice 所属的bbu设备
     * @param s socket连接 socket连接
     * @param _managerDevice 与界面交互的类
     * @throws Exception socket连接错误
     */
    public BBUThread(BBUDevice _bbuDevice, Socket s, ManagerDevice _managerDevice) throws Exception{
        bbuDevice = _bbuDevice;
        client = s;
        manager = _managerDevice;
        in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        out = new PrintWriter(client.getOutputStream(), true);
        RRUId = Integer.parseInt(in.readLine());
        new Thread(this).start();
    }

    /**
     * 三次握手连接过程
     * @throws Exception socket连接异常
     */
    private void threeHandshaking() throws Exception{
        try{
            state = STATE.CONNECTING;
            // 首先接受客户端发来的包
            int ack = 0;
            String message1 = in.readLine();
            Config.consoleOutput("BBU接收 ： " + message1);
            // 判断是否是有效的反馈信息
            String pattern = "2#(\\d+)#RRU#syn=(\\d+)";
            Matcher m = Pattern.compile(pattern).matcher(message1);
            if (m.matches()) {
                ack = Integer.parseInt(m.group(2));
                Config.consoleOutput("id = " + RRUId + "; ack = " + ack);
            }else{
                return;
            }
            manager.appendMessageToBBU("BBU接收到设备" + RRUId +"发送的握手请求 ack=" + ack);
            Thread.sleep(Config.TEMP_REST_INTERVAL);
            // 给客户端发送确认包
            String message2 = "2#" + bbuDevice.getDevice_id() + "#" + bbuDevice.getDevice_type() + "#ack=" + ack + "#syn=" + (ack + 1);
            out.println(message2);
            manager.appendMessageToBBU("BBU向设备" + RRUId +"发送的握手请求确认请求");
            Thread.sleep(Config.TEMP_REST_INTERVAL);
            Config.consoleOutput("发送反馈包" + message2);
            // 收到客户端得确认包则建立三次握手连接
            String message3 = in.readLine();
            Config.consoleOutput("BBU接收 ： " + message3);
            // 判断是否是有效的反馈信息
            pattern = "2#\\d+#RRU#ack=(\\d+)";
            m = Pattern.compile(pattern).matcher(message3);
            int ack2 = 0;
            if (m.matches()) {
                ack2 = Integer.parseInt(m.group(1));
                Config.consoleOutput("ack = " + ack2);
                if (ack2 - ack == 1) {

                }else{
                    return;
                }
            }else{
                return;
            }
            state = STATE.ESTABLISHED;
            Config.consoleOutput("三次握手成功！");
            manager.appendMessageToBBU("收到设备"+RRUId+"再次发送的握手确认信息ack="+ack2);
            //设置应用层连接状态
            bbuDevice.setConnectStatus(RRUId,true);
        }catch(Exception e){
            throw new Exception("Socket连接失败。连接失败。");
        }
        
    }

    /**
     * 保活机制
     * @throws Exception 保活信息传送错误
     */
    private void keepAlive() throws Exception{
        isAlive = false;
        String message = "3#" + bbuDevice.getDevice_id() + "#" + bbuDevice.getDevice_type() + "#isaliveornot";
        sendMessage(message);
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        String am_pm = calendar.get(Calendar.AM_PM) == Calendar.AM? " am":" pm";
        String time = hour + "时" + minute +"分"+ second+"秒"+am_pm;
        manager.appendMessageToBBU(time + ": 向设备" + RRUId + "发送保活信息");
    }

    /**
     * 一些比较费时的操作，需要新开一个进程实现，通过action值来判断具体的操作
     */
    public void run() {
        if (action == ACTION.THREE_HAND_CONNECT) {
            try{
                state = state.CONNECTING;
                Config.consoleOutput("SOCKET连接建立！");
                for (int i = 0; i < Config.MAX_SHAKE_TIMES; i++) {
                    action = ACTION.THREE_HAND_CONNECT_EACH_TIME;
                    Thread shakehandThread = new Thread(this);
                    shakehandThread.start();
                    Thread.sleep(Config.TIME_LIMIT_OF_SHAKE);
                    if (state == STATE.ESTABLISHED) {
                        break;
                    }
                    //shakehandThread.interrupt();
                    Thread.sleep(Config.MILLITIME_SLEEP_BEFORE_STATE);
                }
                if (state != STATE.ESTABLISHED) {
                    // throw new Exception("连接超时。");
                    errorMsg = "连接超时。";
                }
                action = ACTION.LISTENING;
                new Thread(this).start();
                Thread.sleep(Config.MILLITIME_SLEEP_BEFORE_ACTION);
                keepALiveTimer.schedule(new KeepaliveTask(), Config.MILLISECOND_PERIOD, Config.MILLISECOND_PERIOD);
                Thread.sleep(Config.MILLITIME_SLEEP_BEFORE_ACTION);
            }catch(Exception e){
                e.printStackTrace();
                // throw new Exception("连接失败。");
                errorMsg = "连接失败。";
            }
        }else if (action == ACTION.THREE_HAND_CONNECT_EACH_TIME) {
            try {
                threeHandshaking();
            } catch (Exception ex) {
                ex.printStackTrace();
                errorMsg = ex.getMessage();
            }
        }else if (action == ACTION.LISTENING) {
            // 接受消息线程
            Config.consoleOutput("BBU 启动监听进程。" + Thread.currentThread().getId());
            while (state == STATE.ESTABLISHED) {
                try {
                    String msgReceived = in.readLine();
                    Config.consoleOutput( Thread.currentThread().getId() + "监听进程接收的消息 : " + msgReceived);
                    handleReceiveMessage(msgReceived);
                } catch (Exception e) {
                    errorMsg = "监听进程出错了。";
                }
            }
        }
    }
    
    /**
     * 处理接收到的信息
     * @param bcmsg 收到的信息
     */
    private void handleReceiveMessage(String recvMsg){
        //如果无消息，则返回
        if(recvMsg == null ||recvMsg.length() == 0){
            return ;
        }
        //去掉消息两端的空格
        recvMsg.trim();
        char msgType = recvMsg.charAt(0);
        switch(msgType){
            case '1':
                break;
            case '2': // 广播消息
                handleBroadcastMessage(recvMsg);
                break;
            case '3': // 保活信息
                handleKeepAliveMessage(recvMsg);
                break;
            case '4':   //控制命令返回信息
                handleControlMessage(recvMsg.substring(2));
                break;
            case '5': //处理查询的返回信息
                handleQueryMessage(recvMsg.substring(2));
                break;
            default:
                break;
        }
    }

    /**
     * 处理广播信息
     * @param broadcastMsg 广播信息
     */
    private void handleBroadcastMessage(String broadcastMsg){
        String pattern = "2#(\\d+)#RRU#(.*)";
        Matcher m = Pattern.compile(pattern).matcher(broadcastMsg);
        if (m.matches()) {
            int exceptI = Integer.parseInt(m.group(1));
            String msg = m.group(2);
            Config.consoleOutput(exceptI +" --> " +msg);
            bbuDevice.sendBroadcastMsg(exceptI, msg);
        }

        String broadmsg = "接收到广播信息:";
        String arrayMsg[] = broadcastMsg.split("#");
        broadmsg+= "接收到来自"+arrayMsg[2]+arrayMsg[1]+"的广播信息，";
        broadmsg+= "信息内容为："+arrayMsg[3];
        bbuDevice.appendMessage(broadmsg);
    }
    
    /**
     * 处理控制命令的返回信息
     * @param bcmsg 控制信息的返回消息
     */
    private void handleControlMessage(String controlMsg){
        String msg = "";
        String arrayMsg[] = controlMsg.trim().split("#");
        if(arrayMsg[0] == null || arrayMsg[0].equals("3")){
            msg += "错误的控制返回信息";
        }else if(arrayMsg[0].trim().equals("0")){
            msg += "控制设备类型为"+arrayMsg[2]+",ID为"+arrayMsg[3]+"的设备，发送数据";
            msg += arrayMsg[1].trim().equals("0")?"成功":"失败";
        }else if(arrayMsg[0].trim().equals("1")){
            msg += "控制设备类型为"+arrayMsg[2]+",ID为"+arrayMsg[3]+"的设备，设置其状态'开'";
            msg += arrayMsg[1].trim().equals("0")?"成功":"失败";
        }else if(arrayMsg[0].trim().equals("2")){
            msg += "控制设备类型为"+arrayMsg[2]+",ID为"+arrayMsg[3]+"的设备，设置其状态'关'";
            msg += arrayMsg[1].trim().equals("0")?"成功":"失败";
        }
       Config.consoleOutput(msg);
       manager.appendMessageToBBU("控制信息反馈信息：" + msg);
    }

    /**
     * 处理查询消息的返回信息
     * @param queryMessage 查询的返回消息
     */
    private void handleQueryMessage(String queryMessage){
          String msg = "接收到查询消息返回信息： ";
          String arrayMsg[] = queryMessage.split("#");
          msg += "设备类型为"+ arrayMsg[0]+ " ID为"+arrayMsg[1];
          //查询MAC地址的返回信息
          if(arrayMsg[2].trim().equals("0")){
              msg += ", 查询其MAC地址,";
              //查询失败
              if(arrayMsg[3].trim().equals("ERROR")){
                  msg += "查询失败!";
              }else{    //查询成功
                  msg += "MAC地址为 "+ arrayMsg[3];
              }
          }else if(arrayMsg[2].trim().equals("1")){   //查询设备状态
              msg += ", 查询其状态,";
              //查询失败
              if(arrayMsg[3].trim().equals("ERROR")){
                  msg += "查询失败!";
              }else if(arrayMsg[3].trim().equals("ON")){
                  msg += "其状态为开 !";
              }else {
                  msg += "其状态为关 !";
              }
          }else if(arrayMsg[2].trim().equals("2")){ //查询是否空闲
              msg += ", 查询其是否空闲,";
              if(arrayMsg[3].trim().equals("ERROR")){
                  msg += "查询失败!";
              }else if(arrayMsg[3].trim().equals("Y")){
                  msg += "设备当前处于空闲! ";
              }else {
                  msg += "设备当前正忙！ ";
              }
          }else{   //查询出错了
              msg += "查询消息出错了！";
          }
          //输出消息
          Config.consoleOutput(msg);
          manager.appendMessageToBBU("查询信息反馈信息：" + msg);
    }

    /**
     * 处理保活信息
     * @param recvMsg 保活信息
     */
    private void handleKeepAliveMessage(String recvMsg) {
        String pattern = "3#\\d+#RRU#isalive=yes";
        Matcher m = Pattern.compile(pattern).matcher(recvMsg);
        if (m.matches()) {
            isAlive = true;
            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR);
            int minute = calendar.get(Calendar.MINUTE);
            int second = calendar.get(Calendar.SECOND);
            String am_pm = calendar.get(Calendar.AM_PM) == Calendar.AM? " am":" pm";
            String time = hour + "时" + minute +"分"+ second+"秒"+am_pm;
            manager.appendMessageToBBU(time + ": 接收到设备"+RRUId+"的保活信息反馈");
        }
    }
    
    /**
     * 发送信息
     * @param msg 消息内容
     */
    public void sendMessage(String msg){
         Config.consoleOutput(RRUId + "连接线程发送消息:"+ msg);
         if (state == STATE.ESTABLISHED) {
             out.println(msg);
         }
    }

    /**
     * 破坏设备
     */
    public void brokeIt(){
        state = STATE.BROKEN;
        keepALiveTimer.cancel();
        bbuDevice.setConnectStatus(RRUId, false);
        try {
            client.close();
        } catch (IOException ex) {
            Config.consoleOutput("关闭设备出错啦！");
            ex.printStackTrace();
        }
    }

    
    /**
     * 保活机制的计时器线程
     */
    class KeepaliveTask extends TimerTask{
        @Override
        public void run() {
            for (int i = 0; i < Config.TIMES_KEEP_ALIVE; i++) {
                try {
                    keepAlive();
                    Thread.sleep(Config.MILLISECOND_EACH_TIME);
                    if (isAlive) {
                        break;
                    }
                } catch (Exception ex) {
                    Config.consoleOutput("保活机制异常啦！");
                }
            }
            if (!isAlive) {
                bbuDevice.setFibConnectStatus(RRUId, false);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {

                }
                bbuDevice.setConnectStatus(RRUId, false);
                
                // 停止计时器
                keepALiveTimer.cancel();
                state = STATE.BROKEN;
                bbuDevice.removeASocket(RRUId);
//                brokeIt();
            }
        }
    }
}

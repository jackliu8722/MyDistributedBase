package mydevice;

import core.Config;
import core.Config.ACTION;
import core.Config.DEVICE_STATE;
import core.Config.STATE;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import ui.ManagerDevice;

/**
 * RRU的消息逻辑控制
 * @author tbingo
 */
public class RRUDevice implements Runnable{

    /**
     * 设备类型
     */
    private static String device_type = "RRU";

    /**
     * 设备唯一标示
     */
    private int device_id = 0;

    /**
     * 设备当前正在进行什么操作，由于是自动建立连接，初始状态就是三次握手
     */
    private ACTION action = ACTION.THREE_HAND_CONNECT;

    /**
     * 标识该连接的当前状态
     */
    private STATE state = STATE.CLOSED;

    /**
     * 套接字
     */
    private Socket socket = null;

    /**
     * 读入流
     */
    private BufferedReader in = null;

    /**
     * 写出流
     */
    private PrintWriter out = null;

    /**
     * 设备运行状态
     */
    private DEVICE_STATE device_state;
    
    /**
     * 错误信息，如果没有错误则为空
     */
    private String errorMsg = "";

    /**
     * 与界面交互的类
     */
    private ManagerDevice managerDevice = null;

    /**
     * 设置动作
     * @param action 动作
     */
    public void setAction(ACTION action) {
        this.action = action;
    }

    /**
     * 获得设备运行状态
     * @return 设备运行状态
     */
    public STATE getState() {
        return state;
    }

    /**
     * 获取设备ID号
     * @return 设备ID号
     */
    public int getDevice_id() {
        return device_id;
    }

    /**
     * 构造函数
     * @param _device_id 设备标号
     * @param md 与界面交互的类
     * @throws Exception socket连接错误
     */
    public RRUDevice(int _device_id, ManagerDevice md) throws Exception{
        device_id = _device_id;
        this.managerDevice = md;
        // TCP 连接
        socket = new Socket(InetAddress.getByName("127.0.0.1"), Config.SERVER_PORT);

        //设置光纤连接状态
        managerDevice.setFiberConnectStatus(device_id, true);

        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(),true);
        out.println(device_id);
        new Thread(this).start();
    }

    /**
     * 一些比较费时的操作，需要新开一个进程实现，通过action值来判断具体的操作
     */
    public void run() {
        if (action == ACTION.THREE_HAND_CONNECT) {
            try{
                state = state.CONNECTING;
                // 模拟三次握手
                for (int i = 0; i < Config.MAX_SHAKE_TIMES; i++) {
                    action = ACTION.THREE_HAND_CONNECT_EACH_TIME;
                    Thread shakehandThread = new Thread(this);
                    shakehandThread.start();
                    Thread.sleep(Config.TIME_LIMIT_OF_SHAKE);
                    if (state == STATE.ESTABLISHED) {
                        break;
                    }
                    shakehandThread.destroy();
                }
                if (state != STATE.ESTABLISHED) {
                    // throw new Exception("连接超时。");
                    errorMsg = "连接超时。";
                }
                action = ACTION.LISTENING;
                new Thread(this).start();
                Thread.sleep(Config.MILLITIME_SLEEP_BEFORE_ACTION);// 保证线程充分进入监听状态
            }catch(Exception e){
                // throw new Exception("建立SOCKET错误！");
                errorMsg = "建立SOCKET错误！";
            }
        }else if (action == ACTION.THREE_HAND_CONNECT_EACH_TIME) {
            threeHandshaking();
        }else if (action == ACTION.LISTENING) {
            // 接收消息进程
            Config.consoleOutput("RRU 启动监听进程。" + Thread.currentThread().getId());
            while (state == STATE.ESTABLISHED) {
                try {
                    String msgReceived = in.readLine();
                    if (msgReceived == null) {
                        throw new Exception("断开连接");
                    }
                    Config.consoleOutput( Thread.currentThread().getId() + "监听进程接收的消息 : " + msgReceived);
                    dealWithMessage(msgReceived);
                } catch (Exception e) {
                    Config.consoleOutput("SOCKET已经与BBU断开");
                    managerDevice.setFiberConnectStatus(device_id, false);
                    try {
                        Thread.sleep(500);
                        managerDevice.setAppConnectStatus(device_id, false);
                    } catch (InterruptedException ex) {

                    }
                    state = STATE.DISCONNECTED;
                    break;
                }                
            }
        }
    }

    /**
     * 发送消息进程
     */
    private void sendMessage(String messageSend){
        if (state == STATE.ESTABLISHED) {
            Config.consoleOutput("RRU 发送消息：" + messageSend);
            out.println(messageSend);
        }
    }

    /**
     * 三次握手 建立连接
     */
    private void threeHandshaking(){
        try{
            state = STATE.CONNECTING;
            // 第一次向服务器发送消息
            int first_syn = (int) (Math.random() * Config.MAX_RANDOM_SYN);
            String message1 = "2#" + device_id + "#" + device_type + "#syn=" + first_syn;
            out.println(message1);
            managerDevice.appendMessageToRRU(device_id, "设备" + device_id + "向RRU发送握手请求");
            Thread.sleep(Config.TEMP_REST_INTERVAL);
            // 接收服务器的反馈
            String message2 = in.readLine();
            Config.consoleOutput("客户端接收 ： " + message2);
            // 判断是否是有效的反馈信息
            String pattern = "2#\\d+#BBU#ack=(\\d+)#syn=(\\d+)";
            Matcher m = Pattern.compile(pattern).matcher(message2);
            int ack = 0, syn=0;
            if (m.matches()) {
                ack = Integer.parseInt(m.group(1));
                syn = Integer.parseInt(m.group(2));
                if (syn - ack == 1 && ack == first_syn) {
                }else{
                    return;
                }
            }else{
                return;
            }
            managerDevice.appendMessageToRRU(device_id, "设备" + device_id + "接收到BBU发送的握手确认ack="+ack+"&&syn="+syn);
            Thread.sleep(Config.TEMP_REST_INTERVAL);
            // 向服务器发送确认消息
            String message3 = "2#" + device_id + "#" + device_type + "#ack=" + (first_syn + 1);
            out.println(message3);
            // 对于RRU三次握手连接已经建立
            state = STATE.ESTABLISHED;

            //显示信息
            managerDevice.appendMessageToRRU(device_id, "设备" + device_id + "再次发送握手确认");
            //显示应用层连接状态
            managerDevice.setAppConnectStatus(device_id, true);
        }catch(Exception e){
            e.printStackTrace();
        }
    }


    /**
     * 破坏设备
     */
    public void brokeIt(){
        state = STATE.BROKEN;
    }

    /**
     * 解析接收到消息
     * @param args 消息内容
     */
    private void dealWithMessage(String msg){
        if(msg == null || msg.length() == 0 ){
            return ;
        }
        char messageType = msg.charAt(0);
        switch(messageType){
            case '1':                
                break;
            case '2':// 广播消息
                dealWithBroadcastCommand(msg);
                break;
            case '3': // 保活消息
                dealWithKeepAliveCommand(msg);
                break;
            case '4':
                dealWithControlCommand(msg.substring(1));
                break;
            case '5'://处理查询消息
                dealWithQueryMessage(msg.substring(2));
                break;
            default:
                break;
        }
    }

    /**
     * 解析广播内容
     * @param broadcastMsg 广播内容
     */
    private void dealWithBroadcastCommand(String broadcastMsg){
        String msg = "接收到广播信息:";
        String arrayMsg[] = broadcastMsg.split("#");
        msg+= "接收到来自"+arrayMsg[2]+"的广播信息，";
        msg+= "信息内容为："+arrayMsg[3];
        managerDevice.appendMessageToRRU(device_id, msg);
    }

    /**
     * 解析控制信息
     * @param controlMsg 控制信息内容
     */
    private void dealWithControlCommand(String controlMsg){
        String msg = "接收到控制信息:";
        String responseMsg = "";
        controlMsg.trim();
        String arrayMsg [] = controlMsg.substring(1).split("#");
        if(arrayMsg[0] == null){
            msg += "错误的控制命令";
            responseMsg = "4#3";
        }else if(arrayMsg[0].trim().equals("0")){  //控制发送数据
            msg += "  命令：发送数据";
            msg += "  数据：";
            msg += arrayMsg[1]==null?"":arrayMsg[1];
            responseMsg = "4#0#0";
        }else if(arrayMsg[0].trim().equals("1")){
            msg += "  命令：设置开状态";
            responseMsg = "4#1#0";
        }else if(arrayMsg[0].trim().equals("2")){
            msg += "  命令：设置关状态";
            responseMsg = "4#2#0";
        }else{
            msg += "错误的控制命令";
            responseMsg = "4#3";
        }
        Config.consoleOutput(msg);
        managerDevice.appendMessageToRRU(device_id, msg);
        String messageSend =responseMsg +"#"+device_type+"#"+this.device_id;
        sendMessage(messageSend);
    }
    
    /**
     * 解析查询信息
     * @param qMsg 查询信息内容
     */
    private void dealWithQueryMessage(String qMsg){
        String msg = "接收到查询消息: ";
        String responseMsg = "5#"+ device_type + "#"+ this.device_id +"#";
        String arrayMsg[] = qMsg.split("#");
        msg += "查询方类型："+arrayMsg[0];   //获取设备类型
        msg +=" 查询方ID:"+arrayMsg[1];    //获取设备ID
        if(arrayMsg[2] == null || arrayMsg.length == 0 ){
            msg += " 查询命令错误";
            responseMsg += "ERROR";
        }else if(arrayMsg[2].trim().equals("0")){
            responseMsg += arrayMsg[2] +"#";
            msg += " 查询此设备的MAC地址";
            responseMsg += "A333ABCDEF33AAAA";
        }else if(arrayMsg[2].trim().equals("1")){
            msg += " 查询此设备的状态";
            responseMsg += arrayMsg[2] +"#";
            //设备处于关状态
            if(device_state == DEVICE_STATE.OFF){
                responseMsg += "OFF";
            }else { //设备处于开状态
                responseMsg += "ON";
            }
        }else if(arrayMsg[2].trim().equals("2")){
            msg += " 查询此设备是否处于空闲状态";
            responseMsg += arrayMsg[2] +"#";
            responseMsg += "Y";
        }else{
            msg += "错误的查询命令";
            responseMsg += "ERROR";
        }
        Config.consoleOutput(msg);
        managerDevice.appendMessageToRRU(device_id, msg);
        //发送消息
        sendMessage(responseMsg);
    }

    /**
     * 处理保活信息
     * @param msg 保活信息
     */
    private void dealWithKeepAliveCommand(String msg) {
        String pattern = "3#\\d+#BBU#isaliveornot";
        Matcher m = Pattern.compile(pattern).matcher(msg);
        if (m.matches()) {
            sendMessage("3#" + device_id + "#" + device_type + "#isalive=yes");
            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR);
            int minute = calendar.get(Calendar.MINUTE);
            int second = calendar.get(Calendar.SECOND);
            String am_pm = calendar.get(Calendar.AM_PM) == Calendar.AM? " am":" pm";
            String time = hour + "时" + minute +"分"+ second+"秒"+am_pm;
            managerDevice.appendMessageToRRU(device_id, time + ": 收到来自BBU的保活信息，并发送确认");
        }
    }

    /**
     * 发送广播消息
     * @param msg 广播消息内容
     */
    public void sendBroadcastMsg(String msg){
        String messageSend = "2#" + device_id + "#" + device_type + "#" + msg;
        sendMessage(messageSend);
    }

}

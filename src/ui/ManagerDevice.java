package ui;

import core.Config;
import java.util.logging.Level;
import java.util.logging.Logger;
import mydevice.BBUDevice;
import mydevice.RRUDevice;

/**
 * 与界面的交互类
 * @author tbingo
 */
public class ManagerDevice {
    /**
     * bbu设备
     */
    private BBUDevice bbu = null;

    /**
     * bbu设备显示面板
     */
    private BBUPanel bbuPanel = null;

    /**
     * rru设备
     */
    private RRUDevice rrus[] = null;

    /**
     * rru设备显示面板
     */
    private RRUPanel rruPanels[] = null;

    /**
     * 画图面板
     */
    private DeviceCanvas deviceCanvas = null;

    /**
     * 构造函数
     */
    public ManagerDevice(){
        rrus = new RRUDevice[3];
        rruPanels = new RRUPanel[3];
    }

    /**
     * 给单个rru发送信息
     * @param msg 消息内容
     * @param rruId 目标rru的id
     * @param output 输出内容
     */
    public void sendMessageToSingleRRU(String msg,int rruId,String output){
        if(bbu.getState()== Config.STATE.BROKEN){ //设备出现故障
            appendMessageToBBU("设备已出现故障，发送消息失败，请检查设备！");
            return ;
        }
        if(!bbu.isConnect(rruId)){
            appendMessageToBBU("发送消息失败："+"设备RRU"+rruId+"未连接！");
            return ;
        }
        bbu.sendMessageToSingleRRU(msg, rruId);
        appendMessageToBBU(output);
    }

    /**
     * 向rru信息显示框中添加信息
     * @param id rru的id号
     * @param msg 消息内容
     */
    public void appendMessageToRRU(int id,String msg){
        if((id<0 || id >= rrus.length)){
            return ;
        }
        rruPanels[id].appendMessage(msg);
    }

    /**
     * 向bbu信息显示框中添加信息
     * @param msg 消息内容
     */
    public void appendMessageToBBU(String msg){
        bbuPanel.appendMessage(msg);
    }

    /**
     * 设置BBU为故障状态
     */
    public void brokeBbu(){
        bbu.brokeIt();
         bbuPanel.appendMessage("设备BBU出现故障了！");
    }

    /**
     * 判断id的RRU是否存在
     * @param id 设备id
     * @return 是否存在该设备
     */
    public boolean isConnect(int id){
        return bbu.isConnect(id);
    }

    /**
     * 设置RRU为故障状态
     * @param id 设备id
     */
    public void brokeRru(int id){
        if((id<0 || id >= rrus.length)){
            return ;
        }
        rrus[id].brokeIt();
        rruPanels[id].appendMessage("RRU"+id+"出现故障了！");
    }
    /**
     * 设置RRU的应用层连接状态
     * @param id 设备id
     * @param flags 是否处于连接状态
     */
    public void setAppConnectStatus(int id,boolean flags){
        if((id<0 || id >= rrus.length)){
            return ;
        }
        rruPanels[id].setAppConnectStatus(flags);
    }

    /**
     * 设置RRU的光纤层连接状态
     * @param id 设备id
     * @param flags 是否处于连接状态
     */
    public void setFiberConnectStatus(int id,boolean flags){
        if((id<0 || id >= rrus.length)){
            return ;
        }
        rruPanels[id].setFiberConnectStatus(flags);
    }
    /**
     * 设置BBU的应用层连接状态
     * @param id 设备id
     * @param flags 是否处于连接状态
     */
    public void setBbuAppStatus(int id,boolean flags){
        bbuPanel.setBbuAppStatus(id, flags);
    }

    /**
     * 设置BBU的光纤连接状态
     * @param id 设备id
     * @param flags 是否处于连接状态
     */
    public void setBbuFibStatus(int id,boolean flags){
        bbuPanel.setBbuAFibStatus(id, flags);
    }
    /**
     * 获取设备ID
     * @param id 数组下标
     * @return 设备ID
     */
    public int getRruID(int id){
        if((id<0 || id >= rrus.length)){
            return -1;
        }else if(rrus[id] == null){
            return -1;
        }
       return  rrus[id].getDevice_id();
    }

    /**
     * 启动BBU
     */
    public void startBBU(){
        try {
            bbu = new BBUDevice(this);
            deviceCanvas.setBbud(getBbu());
             getBbuPanel().appendMessage("BBU启动成功!");
        } catch (Exception ex) {
            getBbuPanel().appendMessage("error: BBU启动失败!"+ex.getMessage());
        }
    }

    /**
     * 启动rru
     * @param id rru的id号
     */
    public void startRRU(int id){
        if((id<0 || id >= rrus.length)){
            return ;
        }
        try {
            rrus[id] = new RRUDevice(id,this);
            this.deviceCanvas.setrruds(id, rrus[id]);
        //   rruPanels[id].appendMessage("RRU"+id+"与BBU光纤连接成功！");
        } catch (Exception ex) {
            rruPanels[id].appendMessage("error: RRU"+id+"连接失败！"+ex.getMessage());
        }
    }

    /**
     * BBU广播
     * @param msg 广播内容
     */
    public void bbuSendBroadcast(String msg){
        if(bbu.getState()== Config.STATE.BROKEN){ //设备出现故障
            appendMessageToBBU("设备已出现故障，发送消息失败，请检查设备！");
            return ;
        }
        bbu.sendBroadcastMsg(-1, msg);
        appendMessageToBBU("BBU发送广播消息："+msg);
    }

    /**
     * RRU广播
     * @param id  设备号
     * @param msg 广播消息内容
     */
    public void rruSendBroadcast(int id,String msg){
        if((id<0 || id >= rrus.length)){
            return ;
        }
         if(rrus[id].getState()== Config.STATE.BROKEN){ //设备出现故障
            rruPanels[id].appendMessage("设备RRU"+id+"已出现故障，发送消息失败，请检查设备！");
            return ;
        }
        rrus[id].sendBroadcastMsg(msg);
        rruPanels[id].appendMessage("RRU"+id+"发送广播消息，消息内容为："+msg);
    }

    /**
     * 返回rru设备
     * @param id 设备id
     * @return rru设备
     */
    public RRUDevice getRRUDevice(int id){
        if(id<0 || id>= this.rrus.length){
            return null;
        }
        return rrus[id];
    }

    /**
     * 设置rru设备
     * @param id 设备id
     * @param _dev
     */
    public void setRRUDevice(int id,RRUDevice _dev){
         if(id<0 || id>= this.rrus.length){
            return ;
         }
         rrus[id] = _dev;
    }

    /**
     * 返回rru设备面板
     * @param id 设备id
     * @return rru设备面板
     */
    public RRUPanel getRRUPanel(int id){
        if(id<0 || id>= this.rruPanels.length){
            return null;
        }
        return rruPanels[id];
    }

    /**
     * 设置rru设备面板
     * @param id 设备id
     * @param _panel rru设备面板
     */
    public void setRRUPanels(int id,RRUPanel _panel){
         if(id<0 || id>= this.rruPanels.length){
            return ;
         }
         rruPanels[id] = _panel;
    }

    /**
     * 返回bbu设备
     * @return bbu设备
     */
    public BBUDevice getBbu() {
        return bbu;
    }

    /**
     * 设置bbu设备
     * @param bbu bbu设备
     */
    public void setBbu(BBUDevice bbu) {
        this.bbu = bbu;
    }

    /**
     * 返回bbu显示面板
     * @return bbu显示面板
     */
    public BBUPanel getBbuPanel() {
        return bbuPanel;
    }

    /**
     * 设置bbu显示面板
     * @param bbuPanel bbu显示面板
     */
    public void setBbuPanel(BBUPanel bbuPanel) {
        this.bbuPanel = bbuPanel;
    }

    /**
     * 返回画布的值
     * @return 画布
     */
    public DeviceCanvas getDeviceCanvas() {
        return deviceCanvas;
    }

    /**
     * 设置画布的值
     * @param deviceCanvas 画布
     */
    public void setDeviceCanvas(DeviceCanvas deviceCanvas) {
        this.deviceCanvas = deviceCanvas;
    }
}

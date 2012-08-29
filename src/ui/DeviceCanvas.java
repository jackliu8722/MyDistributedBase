package ui;

import core.Config.STATE;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JPanel;
import mydevice.BBUDevice;
import mydevice.RRUDevice;

/**
 * 画图面板
 * @author tbingo
 */
public class DeviceCanvas extends JPanel implements Runnable{
    /**
     * 光纤长度
     */
    private int FIBER_LENGTH = 180;
    /**
     * BBU的宽度
     */
    private int BBUWIDTH = 130;
    /**
     * BBU的高度
     */
    private int BBUHEIGHT = 30;
    /**
     * RRU的宽度
     */
    private int RRUWIDTH = 40;
    /**
     * RRU的高度
     */
    private int RRUHEIGHT = 20;

    /**
     * 所有线宽
     */
    private float lineWidth = 1.0f;
    /**
     * 光纤线宽
     */
    private float fiberWidth = 1.8f;

    /**
     * 每条图例宽
     */
    private int illustrationWidth = 150;

    /**
     * 每条图例高
     */
    private int illustrationHeight = 15;

    /**
     * 图片部分的宽度
     */
    private int illustrationImgWidth = 60;
    /**
     * 图例之间的空隙宽
     */
    private int illustrationImgGapWidth = 5;

    /**
     * 图例之间的空隙高
     */
    private int illustrationImgGapHeight = 3;

    /**
     * 设备连接状态
     */
    private enum DeviceState {
        /**
         * 设备已坏
         */
        BROKEN,
        /**
         * 设备连接断开
         */
        DISCONNECTED,
        /**
         * 设备连接
         */
        CONNECTED };

    /**
     * 光纤连接状态
     */
    private enum FiberState{ 
        /**
         * 光纤连接断开
         */
        DISCONNECTED,
        /**
         * 光纤已连接
         */
        CONNECTED,
        /**
         * 光纤连接中
         */
        CONNECTING };//HALFCONNECTED

    /**
     * 构造函数
     */
    public DeviceCanvas() {
        new Thread(this).start();
    }

    /**
     * 面板作图
     * @param g 画笔
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D)g;

        // Config.consoleOutput(bbud + " " + rruds[0] + " " + rruds[1] + " " + rruds[2] + "-----------");

        // 画图例
        int width = getWidth();
        int height = getHeight();
        int beginx = width - illustrationWidth;
        int beginy = 0;
        g2.drawString("设备故障", beginx + illustrationImgWidth, beginy + illustrationHeight - illustrationImgGapHeight);//设备出现故障
        g2.drawString("设备未连接", beginx + illustrationImgWidth, beginy + 2 * illustrationHeight - illustrationImgGapHeight);//设备完好，未连接
        g2.drawString("设备连接", beginx + illustrationImgWidth, beginy + 3 * illustrationHeight - illustrationImgGapHeight);//设备完好，正确连接
        g2.drawString("断开", beginx + illustrationImgWidth, beginy + 4 * illustrationHeight - illustrationImgGapHeight);//光纤断开状态
        //g2.drawString("半连接", beginx + illustrationImgWidth, beginy + 5 * illustrationHeight - illustrationImgGapHeight);//光纤连接，应用层未连接
        g2.drawString("连接", beginx + illustrationImgWidth, beginy + 5 * illustrationHeight - illustrationImgGapHeight);//光纤，应用层连接
        //1
        setDefaultBorder(g2);
        g2.drawRect(beginx + illustrationImgGapWidth, beginy + illustrationImgGapHeight, illustrationImgWidth - 2 * illustrationImgGapWidth, illustrationHeight - 2 * illustrationImgGapHeight);
        setDeviceBroken(g2);
        g2.fillRect(beginx + illustrationImgGapWidth, beginy + illustrationImgGapHeight, illustrationImgWidth - 2 * illustrationImgGapWidth, illustrationHeight - 2 * illustrationImgGapHeight);
        beginy += illustrationHeight;
        //2
        setDefaultBorder(g2);
        g2.drawRect(beginx + illustrationImgGapWidth, beginy + illustrationImgGapHeight, illustrationImgWidth - 2 * illustrationImgGapWidth, illustrationHeight - 2 * illustrationImgGapHeight);
        setDeviceDisconnected(g2);
        g2.fillRect(beginx + illustrationImgGapWidth, beginy + illustrationImgGapHeight, illustrationImgWidth - 2 * illustrationImgGapWidth, illustrationHeight - 2 * illustrationImgGapHeight);
        beginy += illustrationHeight;
        //3
        setDefaultBorder(g2);
        g2.drawRect(beginx + illustrationImgGapWidth, beginy + illustrationImgGapHeight, illustrationImgWidth - 2 * illustrationImgGapWidth, illustrationHeight - 2 * illustrationImgGapHeight);
        setDeviceConnect(g2);
        g2.fillRect(beginx + illustrationImgGapWidth, beginy + illustrationImgGapHeight, illustrationImgWidth - 2 * illustrationImgGapWidth, illustrationHeight - 2 * illustrationImgGapHeight);
        beginy += illustrationHeight;
        //4
        setFiberDisconnected(g2);
        g2.drawLine(beginx + illustrationImgGapWidth, beginy + illustrationImgGapHeight, beginx + illustrationImgWidth - 2 * illustrationImgGapWidth, beginy + illustrationHeight - 2 * illustrationImgGapHeight);
        beginy += illustrationHeight;
        //5
//        setFiberHalfConnected(g2);
//        g2.drawLine(beginx + illustrationImgGapWidth, beginy + illustrationImgGapHeight, beginx + illustrationImgWidth - 2 * illustrationImgGapWidth, beginy + illustrationHeight - 2 * illustrationImgGapHeight);
//        beginy += illustrationHeight;
        //6
        setFiberConnected(g2);
        g2.drawLine(beginx + illustrationImgGapWidth, beginy + illustrationImgGapHeight, beginx + illustrationImgWidth - 2 * illustrationImgGapWidth, beginy + illustrationHeight - 2 * illustrationImgGapHeight);

        // 开始画图
        int centerx = width / 2;
        int centery = height / 2;
        int graphcentery = (BBUHEIGHT + FIBER_LENGTH + RRUHEIGHT) / 2 ;
        // 开始作图的抬头
        int topgraph = centery - graphcentery;
        // 画BBU
        setDefaultBorder(g2);// 黑色边框
        g2.drawRect(centerx - BBUWIDTH / 2, topgraph, BBUWIDTH, BBUHEIGHT);
        if (bbuState == DeviceState.CONNECTED) {
            setDeviceConnect(g2);
        }else if(bbuState == DeviceState.DISCONNECTED){
            setDeviceDisconnected(g2);
        }else if(bbuState == DeviceState.BROKEN){
            setDeviceBroken(g2);
        }
        g2.fillRect(centerx - BBUWIDTH / 2, topgraph, BBUWIDTH, BBUHEIGHT);
        setDefaultBorder(g2);
        FontMetrics fm = g2.getFontMetrics();
        Rectangle meatureRec = fm.getStringBounds("RRU0",g2).getBounds();
        g2.drawString("BBU", (float)(centerx - meatureRec.getWidth() / 2), (float)(topgraph + meatureRec.getHeight()));

        // BBU的光纤连接点
        int bbuendx = centerx;
        int bbuendy = topgraph + BBUHEIGHT;

        // 第一台RRU设备
        int deltax = (int) (Math.sqrt(3.0) * FIBER_LENGTH / 2.0);
        int deltay = FIBER_LENGTH / 2;
        if (connectState[0] == FiberState.DISCONNECTED) {
            setFiberDisconnected(g2);
        }else if (connectState[0] == FiberState.CONNECTING) {
            setFiberConnecting(g2, 0);
        }else if (connectState[0] == FiberState.CONNECTED) {
            setFiberConnected(g2);
        }
//        else if(connectState[0] == FiberState.HALFCONNECTED) {
//            setFiberHalfConnected(g2);
//        }
        g2.drawLine(bbuendx, bbuendy, bbuendx - deltax, bbuendy + deltay);
        setDefaultBorder(g2); //黑色边框
        g2.drawRect(bbuendx - deltax - RRUWIDTH / 2, bbuendy + deltay, RRUWIDTH, RRUHEIGHT);
        if (rruStates[0] == DeviceState.CONNECTED) {
            setDeviceConnect(g2);
        }else if(rruStates[0] == DeviceState.DISCONNECTED){
            setDeviceDisconnected(g2);
        }else if(rruStates[0] == DeviceState.BROKEN){
            setDeviceBroken(g2);
        }
        g2.fillRect(bbuendx - deltax - RRUWIDTH / 2, bbuendy + deltay, RRUWIDTH, RRUHEIGHT);
        setDefaultBorder(g2);
        g2.drawString("RRU0", (float)(bbuendx - deltax - meatureRec.getWidth() / 2), (float)(bbuendy + deltay + meatureRec.getHeight()));

        // 第二台RRU设备
        if (connectState[1] == FiberState.DISCONNECTED) {
            setFiberDisconnected(g2);
        }else if (connectState[1] == FiberState.CONNECTING) {
            setFiberConnecting(g2, 1);
        }else if (connectState[1] == FiberState.CONNECTED) {
            setFiberConnected(g2);
        }
//        else if (connectState[1] == FiberState.HALFCONNECTED) {
//            setFiberHalfConnected(g2);
//        }
        g2.drawLine(bbuendx, bbuendy, bbuendx, bbuendy + FIBER_LENGTH);
        setDefaultBorder(g2); //黑色边框
        g2.drawRect(bbuendx - RRUWIDTH / 2, bbuendy + FIBER_LENGTH, RRUWIDTH, RRUHEIGHT);
        if (rruStates[1] == DeviceState.CONNECTED) {
            setDeviceConnect(g2);
        }else if(rruStates[1] == DeviceState.DISCONNECTED){
            setDeviceDisconnected(g2);
        }else if(rruStates[1] == DeviceState.BROKEN){
            setDeviceBroken(g2);
        }
        g2.fillRect(bbuendx - RRUWIDTH / 2, bbuendy + FIBER_LENGTH, RRUWIDTH, RRUHEIGHT);
        setDefaultBorder(g2);
        g2.drawString("RRU1", (float)(bbuendx - meatureRec.getWidth() / 2), (float)(bbuendy + FIBER_LENGTH + meatureRec.getHeight()));

        // 第三台RRU设备
        if (connectState[2] == FiberState.DISCONNECTED) {
            setFiberDisconnected(g2);
        }else if (connectState[2] == FiberState.CONNECTING) {
            setFiberConnecting(g2, 2);
        }else if (connectState[2] == FiberState.CONNECTED) {
            setFiberConnected(g2);
        }
//        else if (connectState[2] == FiberState.HALFCONNECTED) {
//            setFiberHalfConnected(g2);
//        }
        g2.drawLine(bbuendx, bbuendy, bbuendx + deltax, bbuendy + deltay);
        setDefaultBorder(g2);
        g2.drawRect(bbuendx + deltax - RRUWIDTH / 2, bbuendy + deltay, RRUWIDTH, RRUHEIGHT);
        if (rruStates[2] == DeviceState.CONNECTED) {
            setDeviceConnect(g2);
        }else if(rruStates[2] == DeviceState.DISCONNECTED){
            setDeviceDisconnected(g2);
        }else if(rruStates[2] == DeviceState.BROKEN){
            setDeviceBroken(g2);
        }
        g2.fillRect(bbuendx + deltax - RRUWIDTH / 2, bbuendy + deltay, RRUWIDTH, RRUHEIGHT);
        setDefaultBorder(g2);
        g2.drawString("RRU2", (float)(bbuendx + deltax - meatureRec.getWidth() / 2), (float)(bbuendy + deltay + meatureRec.getHeight()));
    }

    /**
     * 光纤未连接
     * @param g2
     */
    private void setFiberDisconnected(Graphics2D g2){
        BasicStroke bs = new BasicStroke(fiberWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10.f, new float[]{10f, 10f}, 0.0f);
        g2.setStroke(bs);
        g2.setColor(new Color(195, 195, 195));
    }

    /**
     * 光纤半连接
     * @param g2
     */
    private void setFiberHalfConnected(Graphics2D g2){
        BasicStroke bs = new BasicStroke(fiberWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10.f, new float[]{30f, 10f}, 0.0f);
        g2.setStroke(bs);
        g2.setColor(new Color(220, 220, 0));
    }

    /**
     * 光纤连接
     * @param g2
     */
    private void setFiberConnected(Graphics2D g2){
        g2.setStroke(new BasicStroke(fiberWidth));
        g2.setColor(new Color(0, 220, 0));
    }
    /**
     * 画正在连接状态时需要的中间变量
     */
    private float[] isConnnecting = new float[]{ 0f, 0f, 0f};
    /**
     * 光纤正在连接
     * @param g2 画笔
     */
    private void setFiberConnecting(Graphics2D g2, int idI){
        BasicStroke bs = new BasicStroke(fiberWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10.f, new float[]{30f, 10f}, isConnnecting[idI]);
        isConnnecting[idI] += 5f;
        if (isConnnecting[idI] > 20f) {
            isConnnecting[idI] -= 20f;
        }
        g2.setStroke(bs);
        g2.setColor(new Color(220, 220, 0));
    }

    /**
     * 默认边框画笔
     * @param g2 画笔
     */
    private void setDefaultBorder(Graphics2D g2){
        g2.setStroke(new BasicStroke(lineWidth));
        g2.setColor(Color.BLACK); //黑色边框
    }

    /**
     * 设备未连接（设备没有坏，但是对方设备坏了）黄色
     * @param g2 画笔
     */
    private void setDeviceDisconnected(Graphics2D g2){
        g2.setStroke(new BasicStroke(lineWidth));
        g2.setColor(new Color(255, 255, 106));
    }

    /**
     * 设备坏了 红色
     * @param g2 画笔
     */
    private void setDeviceBroken(Graphics2D g2){
        g2.setStroke(new BasicStroke(lineWidth));
        g2.setColor(new Color(255, 136, 136));
    }

    /**
     * 设备正常状态
     * @param g2 画笔
     */
    private void setDeviceConnect(Graphics2D g2){
        g2.setStroke(new BasicStroke(lineWidth));
        g2.setColor(new Color(136, 255, 136));
    }

    /**
     * bbu当前状态
     */
    private DeviceState bbuState = DeviceState.DISCONNECTED;
    /**
     * 光纤连接当前状态
     */
    private FiberState connectState[] = new FiberState[]{FiberState.DISCONNECTED, FiberState.DISCONNECTED, FiberState.DISCONNECTED};

    /**
     * rru设备当前状态
     */
    private DeviceState rruStates[] = new DeviceState[]{DeviceState.DISCONNECTED, DeviceState.DISCONNECTED, DeviceState.DISCONNECTED};

    /**
     * 启动新的更新画板更新线程
     */
    public void run() {
        // 启动计时器
        Timer timer = new Timer();
        timer.schedule(new myDrawTask(), 0, 500);
    }

    /**
     * 定时更新画板任务
     */
    class myDrawTask extends TimerTask{
        @Override
        public void run() {
            // 取出所有的设备当前值
            if (bbud == null) {
                bbuState = DeviceState.DISCONNECTED;
            }else if(bbud.getState() == STATE.BROKEN){
                bbuState = DeviceState.BROKEN;
            }else if (bbud.getLinkCount() == 0) {
                bbuState = DeviceState.DISCONNECTED;
            }else{
                bbuState = DeviceState.CONNECTED;
            }
            //0
            if (rruds[0] == null || rruds[0].getState() == STATE.CLOSED || rruds[0].getState() == STATE.DISCONNECTED) {
                rruStates[0] = DeviceState.DISCONNECTED;
            }else if(rruds[0].getState() == STATE.CONNECTING){
                rruStates[0] = DeviceState.DISCONNECTED;
            }else if(rruds[0].getState() == STATE.BROKEN){
                rruStates[0] = DeviceState.BROKEN;
            }else if(rruds[0].getState() == STATE.ESTABLISHED){
                rruStates[0] = DeviceState.CONNECTED;
            }
            //1
            if (rruds[1] == null || rruds[1].getState() == STATE.CLOSED || rruds[1].getState() == STATE.DISCONNECTED) {
                rruStates[1] = DeviceState.DISCONNECTED;
            }else if(rruds[1].getState() == STATE.CONNECTING){
                rruStates[1] = DeviceState.DISCONNECTED;
            }else if(rruds[1].getState() == STATE.BROKEN){
                rruStates[1] = DeviceState.BROKEN;
            }else if(rruds[1].getState() == STATE.ESTABLISHED){
                rruStates[1] = DeviceState.CONNECTED;
            }
            //2
            if (rruds[2] == null || rruds[2].getState() == STATE.CLOSED || rruds[2].getState() == STATE.DISCONNECTED) {
                rruStates[2] = DeviceState.DISCONNECTED;
            }else if(rruds[2].getState() == STATE.CONNECTING){
                rruStates[2] = DeviceState.DISCONNECTED;
            }else if(rruds[2].getState() == STATE.BROKEN){
                rruStates[2] = DeviceState.BROKEN;
            }else if(rruds[2].getState() == STATE.ESTABLISHED){
                rruStates[2] = DeviceState.CONNECTED;
            }
            //0
            if (bbud == null || rruds[0] == null || bbud.getLinkState(rruds[0].getDevice_id()) == STATE.CLOSED) {
                connectState[0] = FiberState.DISCONNECTED;
            }else if(bbud.getLinkState(rruds[0].getDevice_id()) == STATE.CONNECTING){
                connectState[0] = FiberState.CONNECTING;
            }else if(bbud.getLinkState(rruds[0].getDevice_id()) == STATE.BROKEN){
                connectState[0]= FiberState.DISCONNECTED;//HALFCONNECTED
            }else if(bbud.getLinkState(rruds[0].getDevice_id()) == STATE.ESTABLISHED){
                connectState[0] = FiberState.CONNECTED;
            }
            //1
            if (bbud == null || rruds[1] == null || bbud.getLinkState(rruds[1].getDevice_id()) == STATE.CLOSED) {
                connectState[1] = FiberState.DISCONNECTED;
            }else if(bbud.getLinkState(rruds[1].getDevice_id()) == STATE.CONNECTING){
                connectState[1] = FiberState.CONNECTING;
            }else if(bbud.getLinkState(rruds[1].getDevice_id()) == STATE.BROKEN){
                connectState[1]= FiberState.DISCONNECTED;//HALFCONNECTED;
            }else if(bbud.getLinkState(rruds[1].getDevice_id()) == STATE.ESTABLISHED){
                connectState[1] = FiberState.CONNECTED;
            }
            //2
            if (bbud == null || rruds[2] == null || bbud.getLinkState(rruds[2].getDevice_id()) == STATE.CLOSED) {
                connectState[2] = FiberState.DISCONNECTED;
            }else if(bbud.getLinkState(rruds[2].getDevice_id()) == STATE.CONNECTING){
                connectState[2] = FiberState.CONNECTING;
            }else if(bbud.getLinkState(rruds[2].getDevice_id()) == STATE.BROKEN){
                connectState[2]= FiberState.DISCONNECTED; //HALFCONNECTED;
            }else if(bbud.getLinkState(rruds[2].getDevice_id()) == STATE.ESTABLISHED){
                connectState[2] = FiberState.CONNECTED;
            }

            // 重绘画布
            repaint();
        }
    }

    /**
     * bbu设备的引用
     */
    private BBUDevice bbud = null;
    /**
     * rru设备的引用
     */
    private RRUDevice[] rruds = new RRUDevice[3];

    /**
     * 设置bbu设备
     * @param bbud bbu设备
     */
    public void setBbud(BBUDevice bbud) {
        this.bbud = bbud;
    }

    /**
     * 设置rru设备
     * @param i 要设置的rru设备的编号
     * @param _rrud rru设备
     */
    public void setrruds(int i, RRUDevice _rrud) {
        rruds[i] = _rrud;
    }
    
}

package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;

/**
 * BBU设备控制面板
 * @author tbingo
 */
public class BBUPanel extends JPanel{

    /**
     * 窗口标题
     */
    public static final String TITLE = "BBU控制和显示";
    /**
     * 窗口宽度
     */
    public static final int WIDTH = 550;

    /**
     * 窗口高度
     */
    public static final int HEIGHT = 350;

    /**
     * 与界面交互的类
     */
    private  ManagerDevice managerDevice;

    /**
     * 消息显示标签
     */
    private JLabel messageType_label = null;

    /**
     * 消息类型下拉条
     */
    private JComboBox messageType_comboBox = null;
    
    /**
     * 消息内容标签
     */
    private JLabel messageContents_label = null;
    /**
     * 消息内容下拉条
     */
    private JComboBox messageContents_comboBox = null;
    /**
     * 设备选择标签
     */
    private JLabel device_label = null;
    /**
     * 设备选择下拉条
     */
    private JComboBox device_comboBox = null;
    /**
     * 发送按钮
     */
    private JButton send_button  = null;

    /**
     * 设备连接状态标签
     */
    private JLabel deviceConnectStatusLabel [][] = null;

    /**
     * 设备连接状态面板
     */
    private DeviceConnectStatusPanel deviceConnectStatusPanel = null;

    /**
     * 输出显示面板
     */
    private DisplayInfoPanel displayInfoPanel = null;

    /**
     * 滚动面板
     */
    private JScrollPane scrollPane = null;

    /**
     * 消息显示表格
     */
    private JTable displayTable = null;

    /**
     * 消息显示表格的内容线性表
     */
    private Vector infoVector = null;

    /**
     * 消息显示表格填充模型
     */
    private AbstractTableModel tm = null;

    /**
     * 设置面板
     */
    private SettingPanel settingPanel = null;

    /**
     * 破坏设备标签
     */
    private JLabel brokenLabel = null;

    /**
     * 设备是否被破坏标签
     */
    private JLabel isBrokenLabel = null;

    /**
     * 确认按钮
     */
    private JButton confirmButton = null;

    /**
     * 广播面板
     */
    private BroadcastPanel broadcastPanel = null;

    /**
     * 广播消息输入框
     */
    private JTextField broadcastTextField = null;

    /**
     * 发送广播按钮
     */
    private JButton broadcastButton = null;

    /**
     * 构造函数
     * @param _device 与界面交互的类
     */
    public BBUPanel(ManagerDevice _device){
        // this.mainFrame = mainFrame;
        setLayout(null);
       // setBackground(new Color(112,128,105));
        initBBUPanel();
        this.managerDevice = _device;
    }

    /**
     * 初始化
     */
    private void initBBUPanel(){

        //设置边框
        setBorder(new TitledBorder(null, TITLE, TitledBorder.DEFAULT_JUSTIFICATION,
                                  TitledBorder.DEFAULT_POSITION, null, Color.BLACK));

        this.messageType_label = new JLabel("消息类型:");
        this.messageType_comboBox = new JComboBox();
        this.messageType_comboBox.addItem("控制消息");
        this.messageType_comboBox.addItem("查询消息");

        this.messageContents_label = new JLabel("消息内容:");
        this.messageContents_comboBox = new JComboBox();
        this.messageContents_comboBox.addItem("发送数据");
        this.messageContents_comboBox.addItem("设置状态为开");
        this.messageContents_comboBox.addItem("设置状态为关");

        this.device_label = new JLabel("接收设备:");
        this.device_comboBox = new JComboBox();
        this.device_comboBox.addItem("RRU0");
        this.device_comboBox.addItem("RRU1");
        this.device_comboBox.addItem("RRU2");
        send_button = new JButton("发送");

        add(this.messageType_label);
        add(this.messageType_comboBox);
        add(this.messageContents_label);
        add(this.messageContents_comboBox);
        add(this.device_label);
        add(this.device_comboBox);
        add(this.send_button);

        this.messageType_label.setBounds(10,20,60,20);
        this.messageType_comboBox.setBounds(70, 20,80, 20);
        this.messageContents_label.setBounds(155, 20, 60, 20);
        this.messageContents_comboBox.setBounds(215, 20, 120, 20);
        this.device_label.setBounds(340, 20, 60, 20);
        this.device_comboBox.setBounds(400,20,60,20);
        this.send_button.setBounds(470,20,70,20);

        //注册事件
        this.messageType_comboBox.addActionListener(new MessageTypeActionListener());
        this.send_button.addActionListener(new SendActionListener());

        //初始化显示设备连接状态面板
        this.deviceConnectStatusPanel = new DeviceConnectStatusPanel();
        add(this.deviceConnectStatusPanel);
        this.deviceConnectStatusPanel.setBounds(10, 50, 240, 100);

        //初始化显示区
        this.displayInfoPanel = new DisplayInfoPanel();
        add(this.displayInfoPanel);
        this.displayInfoPanel.setBounds(10, 160, 530, 180);

        //初始化设置面板
        settingPanel = new SettingPanel();
        add(settingPanel);
        settingPanel.setBounds(255,50,285,45);

        //初始化广播面板
        broadcastPanel = new BroadcastPanel();
        add(broadcastPanel);
        broadcastPanel.setBounds(255,105, 285, 45);
    }

    /**
     * 消息类型事件
     */
    class MessageTypeActionListener implements ActionListener{

        public void actionPerformed(ActionEvent e) {
            if(BBUPanel.this.messageType_comboBox.getSelectedIndex() == 0){
                BBUPanel.this.messageContents_comboBox.removeAllItems();
                BBUPanel.this.messageContents_comboBox.addItem("发送数据");
                 BBUPanel.this.messageContents_comboBox.addItem("设置状态为开");
                 BBUPanel.this.messageContents_comboBox.addItem("设置状态为关");
            }else {
                 BBUPanel.this.messageContents_comboBox.removeAllItems();
                 BBUPanel.this.messageContents_comboBox.addItem("查询MAC地址");
                 BBUPanel.this.messageContents_comboBox.addItem("查询状态");
                 BBUPanel.this.messageContents_comboBox.addItem("查询是否空闲");
            }
        }

    }

    /**
     * 向显示区输入内容
     * @param message 添加的内容
     */
    public void appendMessage(String message){
        
        infoVector.add(message); //添加新的信息
        
        tm.fireTableStructureChanged(); //更新表格内容

        //让滚动条自动下移
        int rowCount = displayTable.getRowCount();

        displayTable.getSelectionModel().setSelectionInterval(rowCount-1, rowCount-1);

        Rectangle rect = displayTable.getCellRect(rowCount-1, 0, true);

        displayTable.scrollRectToVisible(rect);
    }

    /**
     * 设置应用层连接状态
     * @param id 设备id
     * @param flags 是否连接
     */
    public void setBbuAppStatus(int id,boolean flags){
        if(flags){
            deviceConnectStatusLabel[id+1][2].setText("已连接");
            appendMessage("BBU与RRU"+id+"应用层连接成功");
        }else{
            deviceConnectStatusLabel[id+1][2].setText("断开");
            appendMessage("BBU与RRU"+id+"应用层连接断开");
        }
    }

    /**
     * 设置光纤连接状态
     * @param id 设备id
     * @param flags 是否连接
     */
    public void setBbuAFibStatus(int id,boolean flags){
        if(flags){
            deviceConnectStatusLabel[id+1][1].setText("已连接");
            appendMessage("BBU与RRU"+id+"光纤连接成功");
        }else{
            deviceConnectStatusLabel[id+1][1].setText("断开");
            appendMessage("BBU与RRU"+id+"光纤连接断开");
        }
    }
    
    /**
     * 发送按钮事件
     */
    class SendActionListener implements ActionListener{

        public void actionPerformed(ActionEvent e) {
            
          //  tm.fireTableStructureChanged();
             //  tm.fireTableStructureChanged();
            String message = "";
            String outputMessage = "";
            int typeIndex = BBUPanel.this.messageType_comboBox.getSelectedIndex();
            int contentsIndex = BBUPanel.this.messageContents_comboBox.getSelectedIndex();
            int deviceIndex = BBUPanel.this.device_comboBox.getSelectedIndex();

            if(typeIndex == 0){  //控制消息
                message +="4#";
                outputMessage +="发送控制消息:";
                if(contentsIndex == 0){   //发送数据
                      message +="0#data";
                      outputMessage += "控制RRU_"+(deviceIndex)+"发送数据";
                }else if(contentsIndex == 1){  //设置状态为开
                    message +="1";
                    outputMessage +="设置RRU_"+(deviceIndex)+"的状态为开";
                }else {  //设置状态为关
                    message +="2";
                    outputMessage +="设置RRU_"+(deviceIndex)+"的状态为关";
                }
            }else{   //查询消息
                message +="5#RRU#"+managerDevice.getRruID(deviceIndex)+"#";

                outputMessage +="发送查询消息:";
                if(contentsIndex == 0){   //发送数据
                      message +="0";
                      outputMessage += "查询RRU"+(deviceIndex)+"MAC地址";
                }else if(contentsIndex == 1){  //设置状态为开
                    message +="1";
                    outputMessage +="设置RRU"+(deviceIndex)+"的状态为开";
                }else {  //设置状态为关
                    message +="2";
                    outputMessage +="设置RRU"+(deviceIndex)+"的状态为关";
                }
            }

          //  appendMessage(outputMessage);
            managerDevice.sendMessageToSingleRRU(message,deviceIndex,outputMessage);
        }

    }

    /**
     * 设备连接状态面板类
     */
    class DeviceConnectStatusPanel extends JPanel{
        public DeviceConnectStatusPanel(){


            this.setLayout(new GridLayout(4,3));
        //    this.setBackground(Color.PINK);
            setBorder(new TitledBorder(null, "设备连接状态", TitledBorder.DEFAULT_JUSTIFICATION,
                                  TitledBorder.DEFAULT_POSITION, null, Color.BLACK));
            deviceConnectStatusLabel = new JLabel[4][3];
            for(int i =0 ;i<deviceConnectStatusLabel.length;i++){
                for(int j = 0;j<deviceConnectStatusLabel[i].length;j++){
                    deviceConnectStatusLabel[i][j] = new JLabel("",SwingConstants.CENTER);
                    deviceConnectStatusLabel[i][j].setBorder(new LineBorder(Color.black,1));
                    this.add(deviceConnectStatusLabel[i][j]);
                }
            }

            deviceConnectStatusLabel[0][0].setText("设备名");
            deviceConnectStatusLabel[0][1].setText("光纤连接");
            deviceConnectStatusLabel[0][2].setText("应用层连接");
            deviceConnectStatusLabel[1][0].setText("RRU0");
            deviceConnectStatusLabel[2][0].setText("RRU1");
            deviceConnectStatusLabel[3][0].setText("RRU2");

            for(int i = 1;i<deviceConnectStatusLabel.length;i++){
                for(int j = 1;j<deviceConnectStatusLabel[i].length;j++){
                    deviceConnectStatusLabel[i][j].setText("断开");
                }
            }
        }
    }

    /**
     * 显示信息面板类
     */
    class DisplayInfoPanel extends JPanel{
           public DisplayInfoPanel(){
               
                setLayout(new BorderLayout());
                setBorder(new TitledBorder(null, "信息显示", TitledBorder.DEFAULT_JUSTIFICATION,
                                  TitledBorder.DEFAULT_POSITION, null, Color.BLACK));
               infoVector = new Vector();
  
               tm = new AbstractTableModel(){

                public int getRowCount() {
                    return infoVector.size();
                }

                public int getColumnCount() {
                    return 1;
                }

                public Object getValueAt(int rowIndex, int columnIndex) {
                    if(infoVector.isEmpty()){
                        return "";
                    }else{
                        if(rowIndex>=infoVector.size()){
                            return "";
                        }else{
                            return infoVector.elementAt(rowIndex);
                        }
                    }
                }
                public boolean isCellEditable(int row,int column){
                      return false;
                }

                public String getColumnName(int column){
                    return null;
                }
               };

               displayTable = new JTable(tm);
               displayTable.getTableHeader().setPreferredSize(new Dimension(0, 0));
               scrollPane = new JScrollPane(displayTable);
               add(scrollPane,BorderLayout.CENTER);
           }
    }

    /**
     * 设置面板类
     */
    class SettingPanel extends JPanel{
        public SettingPanel(){

            setLayout(new GridLayout(1,3,10,3));

            setBorder(new TitledBorder(null, "设置", TitledBorder.DEFAULT_JUSTIFICATION,
                                  TitledBorder.DEFAULT_POSITION, null, Color.BLACK));

            brokenLabel = new JLabel("是否正常:",SwingConstants.CENTER);
            isBrokenLabel = new JLabel("设备正常",SwingConstants.CENTER);
            add(brokenLabel);
            add(isBrokenLabel);
          //  brokenLabel.setBounds(2,15,60,20);
          //  brokenComboBox.setBounds(64,15,60,20);

            confirmButton = new JButton("设为故障");
            add(confirmButton);
         //   confirmButton.setBounds(64,72,60,20);
            confirmButton.addActionListener(new ConfirmActionListener()); //注册事件

           // add(new JPanel());
        }
    }

    /**
     * 设置设备故障状态事件监听
     */
    class ConfirmActionListener implements ActionListener{

        public void actionPerformed(ActionEvent e) {

              int isOk = JOptionPane.showConfirmDialog(null, "是否要设置设备BBU为故障状态","提示", 2);

              if(isOk!=0)
                  return ;
              isBrokenLabel.setText("设备出现故障");

              confirmButton.setEnabled(false);

              managerDevice.brokeBbu();

              //设置光纤连接状态
              if(managerDevice.isConnect(0)){
                  setBbuAFibStatus(0,false);
              }
             if(managerDevice.isConnect(1)){
                  setBbuAFibStatus(1,false);
              }
              if(managerDevice.isConnect(2)){
                  setBbuAFibStatus(2,false);
              }
             
        }

    }

    /**
     * 广播面板类
     */
    class BroadcastPanel extends JPanel{
        public BroadcastPanel(){

            setLayout(null);

            setBorder(new TitledBorder(null, "广播消息", TitledBorder.DEFAULT_JUSTIFICATION,
                                  TitledBorder.DEFAULT_POSITION, null, Color.BLACK));
            broadcastTextField = new JTextField();
            broadcastButton = new JButton("广播");
           add(broadcastTextField);
           add(broadcastButton);
           broadcastTextField.setBounds(5, 20, 200, 20);
           broadcastButton.setBounds(210,20 , 75, 20);
           broadcastButton.addActionListener(new BroadcastActionListener());

        }
    }

    /**
     * 广播事件监听
     */
    class BroadcastActionListener implements ActionListener{

        public void actionPerformed(ActionEvent e) {
            String text = BBUPanel.this.broadcastTextField.getText();
            if(text == null || text.length()== 0){
               JOptionPane.showMessageDialog(null, "广播消息为空，请重新输入！", "Warning",JOptionPane.WARNING_MESSAGE);
            }else{
               managerDevice.bbuSendBroadcast(text);
               BBUPanel.this.broadcastTextField.setText("");
            }
        }

    }
}



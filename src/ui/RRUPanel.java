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
import mydevice.RRUDevice;

/**
 * RRU的显示面板
 * @author tbingo
 */
public class RRUPanel extends JPanel{
    /**
     * 标题
     */
    private String title = "";

    /**
     * 面板宽度
     */
    public static final int WIDTH = 550;

    /**
     * 面板高度
     */
    public static final int HEIGHT = 230;

    /**
     * 与界面交互的类
     */
    private ManagerDevice managerDevice;

    /**
     * 设备id
     */
    private int device_id = 0;

    /**
     * 滚动显示面板
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
     * 消息显示面板
     */
    private MessagePanel messagePanel = null;

    /**
     * 连接状态显示标签
     */
    private JLabel statusLabels [][] = null;

    /**
     * 状态显示面板
     */
    private StatusPanel statusPanel = null;
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
     * 设置面板
     */
    private SettingPanel settingPanel = null;

    /**
     * 广播面板
     */
    private BroadcastPanel broadcastPanel = null;

    /**
     * 广播输入内容框
     */
    private JTextField broadcastTextField = null;

    /**
     * 发送广播按钮
     */
    private JButton broadcastButton = null;

    /**
     * 构造函数
     * @param _device 与界面交互的类
     * @param title 标题
     */
    public RRUPanel(ManagerDevice _device,String title){
        super();
        this.managerDevice = _device;
        this.title = title;
        setLayout(null);
     //   setBackground(Color.blue);
        initRRUPanel();
    }

     /**
     * 初始化
     */
    private void initRRUPanel(){

        //设置边框
        setBorder(new TitledBorder(null, title, TitledBorder.DEFAULT_JUSTIFICATION,
                                  TitledBorder.DEFAULT_POSITION, null, Color.BLACK));

        //设置消息显示面板
        messagePanel = new MessagePanel();

        add(messagePanel);

        messagePanel.setBounds(140, 17, 400, 203);

        //设置状态面板
        statusPanel = new StatusPanel();

        add(statusPanel);

        statusPanel.setBounds(2, 130, 138, 90);

        //设置设置面板
        settingPanel = new SettingPanel();

        add(settingPanel);

        settingPanel.setBounds(2, 17, 136, 70);

        //设置广播面板
        broadcastPanel = new BroadcastPanel();

        add(broadcastPanel);

        broadcastPanel.setBounds(2,90,136,45);
    }


    /**
     * 设置设备id
     * @param idI 设备id
     */
    public void setDevice_id(int idI){
          
            device_id = idI;
    }

    /**
     * 把信息显示在输入窗口上
     * @param msg 信息
     */
    public void appendMessage(String msg){
        infoVector.add(msg); //添加新的信息

        tm.fireTableStructureChanged(); //更新表格内容

        //让滚动条自动下移
        int rowCount = displayTable.getRowCount();

        displayTable.getSelectionModel().setSelectionInterval(rowCount-1, rowCount-1);

        Rectangle rect = displayTable.getCellRect(rowCount-1, 0, true);

        displayTable.scrollRectToVisible(rect);
    }

    /**
     * 设置应用层连接状态
     * @param flags 是否连接
     */
    public void setAppConnectStatus(boolean flags){
        if(flags){
            statusLabels[2][1].setText("已连接");
            appendMessage("RRU"+device_id+"与BBU应用层连接成功");
        }else{
            statusLabels[2][1].setText("断开");
            appendMessage("RRU"+device_id+"与BBU应用层连接断开");
        }
    }

    /**
     * 设置光纤连接状态
     * @param flags 是否连接
     */
     public void setFiberConnectStatus(boolean flags){
        if(flags){
            statusLabels[1][1].setText("已连接");
            appendMessage("RRU"+device_id+"与BBU光纤连接成功");
        }else{
            statusLabels[1][1].setText("断开");
            appendMessage("RRU"+device_id+"与BBU光纤连接已断开");
        }
    }
    /**
     * 信息显示面板
     */
    class MessagePanel extends JPanel{
        public MessagePanel(){
            setLayout(new BorderLayout());
            setBorder(new TitledBorder(null, "信息显示", TitledBorder.DEFAULT_JUSTIFICATION,
                                  TitledBorder.DEFAULT_POSITION, null, Color.BLACK));
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
     * 状态面板类
     */
    class StatusPanel extends JPanel{

        public StatusPanel(){

          //  this.setBackground(Color.PINK);

            setBorder(new TitledBorder(null, "设备状态", TitledBorder.DEFAULT_JUSTIFICATION,
                                  TitledBorder.DEFAULT_POSITION, null, Color.BLACK));
            setLayout(new GridLayout(4,2));

            statusLabels = new JLabel[4][2];
            for(int i =0 ;i<statusLabels.length;i++){
                for(int j = 0;j< statusLabels[i].length;j++){
                    statusLabels[i][j] = new JLabel("",SwingConstants.CENTER);
                    statusLabels[i][j].setBorder(new LineBorder(Color.black,1));
                    this.add(statusLabels[i][j]);
                }
            }
            statusLabels[0][0].setText("");
            statusLabels[0][1].setText("状态");
            statusLabels[1][0].setText("光纤连接");
            statusLabels[2][0].setText("应用层连接");
            statusLabels[3][0].setText("是否正常");
            statusLabels[1][1].setText("断开");
            statusLabels[2][1].setText("断开");
            statusLabels[3][1].setText("正常");

        }
    }

    /**
     * 设置面板类
     */
    class SettingPanel extends JPanel{
        public SettingPanel(){
            setBorder(new TitledBorder(null, "设置", TitledBorder.DEFAULT_JUSTIFICATION,
                                  TitledBorder.DEFAULT_POSITION, null, Color.BLACK));
            setLayout(new GridLayout(2,2,2,2));

            brokenLabel = new JLabel("是否正常:",SwingConstants.CENTER);
            isBrokenLabel = new JLabel("正常",SwingConstants.CENTER);
            
            add(brokenLabel);
            add(isBrokenLabel);
            add(new JLabel(""));
            confirmButton = new JButton("故障");
            add(confirmButton);
            confirmButton.addActionListener(new ConfirmActionListener()); //注册事件
        }
    }

    /**
     * 确实按钮事件类
     */
    class ConfirmActionListener implements ActionListener{

        public void actionPerformed(ActionEvent e) {
            //修改设备是否正常
                int isOk = JOptionPane.showConfirmDialog(null, "是否要设置设备RRU"+device_id+"为故障状态","提示", 2);

                if(isOk!=0)

                    return ;
                isBrokenLabel.setText("故障");
                statusLabels[3][1].setText("故障");
                confirmButton.setEnabled(false);
                managerDevice.brokeRru(device_id);
                managerDevice.setFiberConnectStatus(device_id, false);
                try {
                    Thread.sleep(300);
                } catch (InterruptedException ex) {
                }
                managerDevice.setAppConnectStatus(device_id, false);
        }

    }

    /**
     * 广播面板类
     */
    class BroadcastPanel extends JPanel{
        public BroadcastPanel(){

            setLayout(new GridLayout(1,2,2,2));

            setBorder(new TitledBorder(null, "广播消息", TitledBorder.DEFAULT_JUSTIFICATION,
                                  TitledBorder.DEFAULT_POSITION, null, Color.BLACK));
            broadcastTextField = new JTextField();
            broadcastButton = new JButton("广播");
           add(broadcastTextField);
           add(broadcastButton);
           broadcastButton.addActionListener(new BroadcastActionListener());

        }
    }
    /**
     * 广播事件监听
     */
    class BroadcastActionListener implements ActionListener{

        public void actionPerformed(ActionEvent e) {
            String text = RRUPanel.this.broadcastTextField.getText();
            if(text == null || text.length()== 0){
               JOptionPane.showMessageDialog(null, "广播消息为空，请重新输入！", "Warning",JOptionPane.WARNING_MESSAGE);
            }else{
               managerDevice.rruSendBroadcast(RRUPanel.this.device_id,text);
               RRUPanel.this.broadcastTextField.setText("");
            }
        }

    }
}

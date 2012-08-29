package ui;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

/**
 * 程序主窗口类
 * @author tbingo
 */
public class MainFrame extends JFrame{

    /**
     * 主界面宽度
     */
    public static final int WIDTH = 1150;

    /**
     * 主界面高度
     */
    public static final int HEIGHT = 750;

    /**
     * 主界面左侧间隙
     */
    public static final int LEFT  = 10;

    /**
     * 主界面顶侧间距
     */
    public static final int TOP = 3;

    /**
     * RRUPanel计数器，标识有多少个RRUPanel
     */
    public static final int RRU_COUNTER = 3;

    /**
     * bbu控制显示面板
     */
    private BBUPanel bbuPanel = null;

    /**
     * rru控制显示面板
     */
    private RRUPanel rruPanel[] = null;

    /**
     * 作图面板
     */
    private DeviceCanvas deviceCanvas = null;

    /**
     * 连接光纤1按钮
     */
    private JButton rru_1StartButton = null;

    /**
     * 连接光纤2按钮
     */
    private JButton rru_2StartButton = null;

    /**
     * 连接光纤3按钮
     */
    private JButton rru_3StartButton = null;

    /**
     * 连接图片显示区域面板
     */
    private GraphicsPanel graphicsPanel = null;

    /**
     * 管理类
     */
    private ManagerDevice managerDevice = new ManagerDevice();;

    /**
     * 构造函数
     * @param title 标题
     */
    public MainFrame(String title){

        super(title);

        setSize(WIDTH,HEIGHT);  //设备主界面大小

        //设置窗口位置
        int x = (Toolkit.getDefaultToolkit().getScreenSize().width - WIDTH) / 2;

        int y = (Toolkit.getDefaultToolkit().getScreenSize().height - HEIGHT) / 2;

        setLocation(x, y);

        setLayout(null);

     //   this.getContentPane().setBackground(new Color(135,206,235 ));

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        this.setResizable(false);

        initMainFrame();

        managerDevice.setBbuPanel(bbuPanel);
        managerDevice.setRRUPanels(0,rruPanel[0]);
        managerDevice.setRRUPanels(1,rruPanel[1]);
        managerDevice.setRRUPanels(2,rruPanel[2]);
        managerDevice.setDeviceCanvas(deviceCanvas);

        //启动BBU
        managerDevice.startBBU();
    }

    /**
     * 初始化主界面
     */
    private void initMainFrame(){

        //初始化作图画布
        graphicsPanel = new GraphicsPanel();
        
        this.add(graphicsPanel);
        
        graphicsPanel.setBounds(LEFT, TOP, BBUPanel.WIDTH, 345);

        //处理BBUPanel、、
        bbuPanel = new BBUPanel(managerDevice);

        this.add(bbuPanel);

        bbuPanel.setBounds(LEFT, 350, BBUPanel.WIDTH, BBUPanel.HEIGHT);

        //处理RRUPanel
        rruPanel = new RRUPanel[3];

        for(int i = 0; i < RRU_COUNTER;i++){
            rruPanel[i] = new RRUPanel(managerDevice,"RRU"+(i)+"控制和显示");
            rruPanel[i].setDevice_id(i);
            this.add(rruPanel[i]);
            rruPanel[i].setBounds(LEFT*2 + BBUPanel.WIDTH,TOP *(i+1)+i*RRUPanel.HEIGHT,RRUPanel.WIDTH,RRUPanel.HEIGHT );
        }
    }

    /**
     * 作图面板类 
     */
    class GraphicsPanel extends JPanel{
        public GraphicsPanel(){

             setBorder(new TitledBorder(null, "设备连接图", TitledBorder.DEFAULT_JUSTIFICATION,
                                  TitledBorder.DEFAULT_POSITION, null, Color.BLACK));

             setLayout(null);

             JPanel buttonPanel = new JPanel();

             buttonPanel.setLayout(new GridLayout(1,3,20,2));

             rru_1StartButton = new JButton("光纤连接0");

             rru_2StartButton = new JButton("光纤连接1");

             rru_3StartButton = new JButton("光纤连接2");

             buttonPanel.add(rru_1StartButton);
             buttonPanel.add(rru_2StartButton);
             buttonPanel.add(rru_3StartButton);

             rru_1StartButton.addActionListener(new StartActionListener());

             rru_2StartButton.addActionListener(new StartActionListener());

             rru_3StartButton.addActionListener(new StartActionListener());

             add(buttonPanel);

             deviceCanvas = new DeviceCanvas();

             add(deviceCanvas);

             deviceCanvas.setBounds(10, 17, 530, 295);

             buttonPanel.setBounds(100, 315, 350, 22);
        }
    }

    /**
     * 启动按钮事件类
     */
    class StartActionListener implements ActionListener{

        public void actionPerformed(ActionEvent e) {
            
            String actionCommand = e.getActionCommand();

            if("光纤连接0".equals(actionCommand)){
             //   rruPanel[0].startDevice(0, deviceCanvas);
                managerDevice.startRRU(0);
            }else if("光纤连接1".equals(actionCommand)){
               // rruPanel[1].startDevice(1, deviceCanvas);
                managerDevice.startRRU(1);
            }else if("光纤连接2".equals(actionCommand)){
               // rruPanel[2].startDevice(2, deviceCanvas);
                managerDevice.startRRU(2);
            }
        }

    }

    /**
     * 程序入口
     * @param args 命令行参数
     */
    public static void main(String []args){
        MainFrame mainFrame = new MainFrame("分布式基站设备仿真系统");
        try {
                UIManager.setLookAndFeel( "com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                SwingUtilities.updateComponentTreeUI(mainFrame);
                mainFrame.validate();
	} catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
        }
        mainFrame.setVisible(true);
    }
}

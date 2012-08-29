/**
 * 核心包：包含一些配置信息
 */
package core;

/**
 * 配置信息类
 * @author tbingo
 */
public class Config {
    //////////////////////////////////////三次握手//////////////////////////////////////
    /**
     * 为了演示效果，三次握手每次接受或发送包之间的休息毫秒。如果不需要，设置为0即可。
     */
    public static int TEMP_REST_INTERVAL = 300;
    /**
     * 尝试几次三次握手 否则认为连接超时
     */
    public static int MAX_SHAKE_TIMES = 500;

    /**
     * 一次“三次握手”在多少毫秒如果没成功则认为单次连接失败
     */
    public static int TIME_LIMIT_OF_SHAKE = 1500;
    
    /**
     * 最大的报文确认位
     */
    public static int MAX_RANDOM_SYN = 100;

    /**
     * 设备的状态信息
     */
    public static enum STATE {
        /**
         * 设备无连接(没有光纤)
         */
        CLOSED,
        /**
         * 表示服务器端的某个SOCKET处于监听状态，可以接受连接
         */
        LISTEN,
        /**
         * 正在连接
         */
        CONNECTING,
        /**
         * 连接已经建立
         */
        ESTABLISHED,
        /**
         * 表示设备坏了，主动断开（有光纤）
         */
        BROKEN,
        /**
         * 标识设备被动断开了（有光纤）
         */
        DISCONNECTED
    };

    /**
     * 设备执行的动作信息
     */
    public static enum ACTION{
        /**
         * 表示正在模拟应用层三次握手连接
         */
        THREE_HAND_CONNECT,
        /**
         * 表示每次三次握手过程
         */
        THREE_HAND_CONNECT_EACH_TIME,
        /**
         * 监听，也就是接收消息
         */
        LISTENING,
        /**
         * 保活机制
         */
        KEEP_ALIVE
    };

    //////////////////////////////////////保活机制//////////////////////////////////////
    /**
     * 每隔多少毫秒，BBU->RRU进行一次保活信息验证
     */
    public static int MILLISECOND_PERIOD = 4000;
    /**
     * 进行多少次重传保活信息，如果都没有反应，认为RRU已挂
     */
    public static int TIMES_KEEP_ALIVE = 3;
    /**
     * 每次超过多少时限（毫秒），进行重传
     */
    public static int MILLISECOND_EACH_TIME = 100;

    //////////////////////////////////////核心变量//////////////////////////////////////
    
    /**
     * 每次事件发生前，为了保证前面的线程已经完全进入运行状态，必须睡眠一会儿
     */
    public static int MILLITIME_SLEEP_BEFORE_ACTION = 100;
    /**
     * 每次状态发生改变前，为了保证前面的线程已经完全进入运行状态，必须睡眠一会儿
     */
    public static int MILLITIME_SLEEP_BEFORE_STATE = 100;
    /**
     * 端口号
     */
    public static int SERVER_PORT = 947;

    ////////////////////////////////控制信息以及查询信息////////////////////////////////
    /**
     * 设备状态查询时候的设备状态信息
     */
    public static enum DEVICE_STATE{
        /**
         * 设备处于开状态
         */
        ON,
        /**
         * 设备处于关状态
         */
        OFF
    }

    /**
     * 调试时候使用控制台输出信息，如果不需要控制台输出调试信息，修改这里即可
     * @param msg 信息
     */
    public static void consoleOutput(String msg){
         //System.out.println(msg);
    }
}

package echowand.net;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.SynchronousQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * IPネットワークのサブネット
 *
 * @author Yoshiki Makino
 */
public class InetSubnet implements Subnet {
    private static final Logger LOGGER = Logger.getLogger(InetSubnet.class.getName());
    private static final String CLASS_NAME = InetSubnet.class.getName();

    /**
     * ECHONET Liteが利用するポート番号
     */
    public static final short DEFAULT_PORT_NUMBER = 3610;

    private UDPNetwork udpNetwork;
    private TCPReceiver tcpReceiver;
    private TCPAcceptor tcpAcceptor;

    private NetworkInterface networkInterface;
    private List<NetworkInterface> receiverInterfaces;
    private InetAddress multicastAddress;
    private int portNumber;

    private InetAddress localAddress;
    private InetAddress loopbackAddress;
    private InetNode groupNode;
    private InetNode localNode;

    private SynchronousQueue<Frame> receiveQueue = null;

    private InetSubnetUDPReceiverThread udpReceiverThread;
    private InetSubnetTCPReceiverThread tcpReceiverThread;
    private InetSubnetTCPAcceptorThread tcpAcceptorThread;

    private boolean tcpAcceptorEnabled = false;

    /**
     * InetSubnetの初期化を行う。
     *
     * @param networkInterface ネットワークインタフェースの指定
     * @param receiverInterfaces 受信用ネットワークインタフェースの指定
     * @param loopbackAddress ループバックアドレスの指定
     * @param multicastAddress マルチキャストアドレスの指定
     * @param portNumber ポート番号の指定
     * @throws SubnetException 生成に失敗した場合
     */
    protected void initialize(NetworkInterface networkInterface, Collection<? extends NetworkInterface> receiverInterfaces, InetAddress loopbackAddress, InetAddress multicastAddress, int portNumber) throws SubnetException {
        LOGGER.entering(CLASS_NAME, "initialize", new Object[]{networkInterface, loopbackAddress, multicastAddress, portNumber});

        if (!isValidAddress(loopbackAddress)) {
            throw new SubnetException("invalid loopback address: " + loopbackAddress);
        }

        if (!isValidAddress(multicastAddress)) {
            throw new SubnetException("invalid multicast address: " + multicastAddress);
        }

        this.localAddress = null;
        this.networkInterface = networkInterface;
        this.receiverInterfaces = new LinkedList<NetworkInterface>(receiverInterfaces);
        this.loopbackAddress = loopbackAddress;
        this.multicastAddress = multicastAddress;
        this.portNumber = portNumber;

        createUDPNetwork();
        createTCPReceiver();
        createTCPAcceptor();

        LOGGER.exiting(CLASS_NAME, "initialize");
    }

    /**
     * InetSubnetの初期化を行う。
     *
     * @param localAddress ローカルアドレスの指定
     * @param receiverInterfaces 受信用ネットワークインタフェースの指定
     * @param loopbackAddress ループバックアドレスの指定
     * @param multicastAddress マルチキャストアドレスの指定
     * @param portNumber ポート番号の指定
     * @throws SubnetException 生成に失敗した場合
     */
    protected void initialize(InetAddress localAddress, Collection<? extends NetworkInterface> receiverInterfaces, InetAddress loopbackAddress, InetAddress multicastAddress, int portNumber) throws SubnetException {
        LOGGER.entering(CLASS_NAME, "initialize", new Object[]{localAddress, loopbackAddress, multicastAddress, portNumber});
        
        if (!isValidAddress(localAddress)) {
            throw new SubnetException("invalid local address: " + localAddress);
        }

        if (!isValidAddress(loopbackAddress)) {
            throw new SubnetException("invalid loopback address: " + loopbackAddress);
        }

        if (!isValidAddress(multicastAddress)) {
            throw new SubnetException("invalid multicast address: " + multicastAddress);
        }

        this.localAddress = localAddress;
        this.networkInterface = null;
        this.receiverInterfaces = new LinkedList<NetworkInterface>(receiverInterfaces);
        this.loopbackAddress = loopbackAddress;
        this.multicastAddress = multicastAddress;
        this.portNumber = portNumber;
        
        createUDPNetwork();
        createTCPReceiver();
        createTCPAcceptor();

        LOGGER.exiting(CLASS_NAME, "initialize");
    }

    /**
     * InetSubnetの初期化を行う。
     *
     * @param loopbackAddress ループバックアドレスの指定
     * @param multicastAddress マルチキャストアドレスの指定
     * @param portNumber ポート番号の指定
     * @throws SubnetException 生成に失敗した場合
     */
    protected void initialize(InetAddress loopbackAddress, InetAddress multicastAddress, int portNumber) throws SubnetException {
        LOGGER.entering(CLASS_NAME, "initialize", new Object[]{loopbackAddress, multicastAddress, portNumber});

        if (!isValidAddress(loopbackAddress)) {
            throw new SubnetException("invalid loopback address: " + localAddress);
        }

        if (!isValidAddress(multicastAddress)) {
            throw new SubnetException("invalid multicast address: " + multicastAddress);
        }

        this.localAddress = null;
        this.networkInterface = null;
        this.receiverInterfaces = new LinkedList<NetworkInterface>();
        this.loopbackAddress = loopbackAddress;
        this.multicastAddress = multicastAddress;
        this.portNumber = portNumber;

        createUDPNetwork();
        createTCPReceiver();
        createTCPAcceptor();

        LOGGER.exiting(CLASS_NAME, "initialize");
    }

    private void createUDPNetwork() {
        LOGGER.entering(CLASS_NAME, "createUDPNetwork");

        if (localAddress != null) {
            udpNetwork = new UDPNetwork(localAddress, receiverInterfaces, multicastAddress, portNumber);
        } else if (networkInterface != null) {
            udpNetwork = new UDPNetwork(networkInterface, receiverInterfaces, multicastAddress, portNumber);
        } else if (!receiverInterfaces.isEmpty()) {
            udpNetwork = new UDPNetwork(receiverInterfaces, multicastAddress, portNumber);
        } else {
            udpNetwork = new UDPNetwork(multicastAddress, portNumber);
        }

        LOGGER.exiting(CLASS_NAME, "createUDPNetwork");
    }

    private void createTCPReceiver() {
        LOGGER.entering(CLASS_NAME, "createTCPNetwork");

        tcpReceiver = new TCPReceiver();

        LOGGER.exiting(CLASS_NAME, "createTCPNetwork");
    }

    private void createTCPAcceptor() {
        LOGGER.entering(CLASS_NAME, "createTCPAcceptor");

        if (localAddress != null) {
            tcpAcceptor = new TCPAcceptor(localAddress, portNumber);
        } else {
            tcpAcceptor = new TCPAcceptor(portNumber);
        }

        LOGGER.exiting(CLASS_NAME, "createTCPAcceptor");
    }

    private UDPNetwork getUDPNetwork() {
        return udpNetwork;
    }

    private TCPReceiver getTCPReceiver() {
        return tcpReceiver;
    }

    private TCPAcceptor getTCPAcceptor() {
        return tcpAcceptor;
    }

    /**
     * TCPを有効にする。実行中に呼び出した場合には設定は変更されずfalseを返す。
     *
     * @return 設定の変更を成功した場合にはfalse、それ以外の場合にはtrue
     */
    public synchronized boolean enableTCPAcceptor() {
        if (isInService()) {
            return false;
        }

        tcpAcceptorEnabled = true;

        return true;
    }

    /**
     * TCPを無効にする。実行中に呼び出した場合には設定は変更されずfalseを返す。
     *
     * @return 設定の変更を成功した場合にはfalse、それ以外の場合にはtrue
     */
    public synchronized boolean disableTCPAcceptor() {
        if (isInService()) {
            return false;
        }

        tcpAcceptorEnabled = false;

        return true;
    }

    /**
     * TCPが有効であるかを返す。
     *
     * @return TCPが有効であればtrue、無効であればfalse
     */
    public synchronized boolean isTCPAcceptorEnabled() {
        return tcpAcceptorEnabled;
    }

    /**
     * 設定されたネットワークインタフェースを返す。
     *
     * @return 設定されたネットワークインタフェース
     */
    public NetworkInterface getNetworkInterface() {
        return networkInterface;
    }

    /**
     * このInetSubnetが実行中であるか返す。
     *
     * @return 実行中であればtrue、そうでなければfalse
     */
    public synchronized boolean isInService() {
        UDPNetwork network = getUDPNetwork();

        if (network == null) {
            return false;
        }

        return network.isInService();
    }

    private synchronized void startThreads() {
        LOGGER.entering(CLASS_NAME, "startThreads");

        receiveQueue = new SynchronousQueue<Frame>();

        udpReceiverThread = new InetSubnetUDPReceiverThread(this, getUDPNetwork(), receiveQueue);
        udpReceiverThread.start();

        tcpReceiverThread = new InetSubnetTCPReceiverThread(this, getTCPReceiver(), receiveQueue);
        tcpReceiverThread.start();

        if (tcpAcceptorEnabled) {
            tcpAcceptorThread = new InetSubnetTCPAcceptorThread(this, getTCPAcceptor());
            tcpAcceptorThread.start();
        }

        LOGGER.exiting(CLASS_NAME, "startThreads");
    }

    private synchronized void stopThreads() {
        LOGGER.entering(CLASS_NAME, "stopThreads");

        if (udpReceiverThread != null) {
            udpReceiverThread.terminate();
            udpReceiverThread = null;
        }

        if (tcpReceiverThread != null) {
            tcpReceiverThread.terminate();
            tcpReceiverThread = null;
        }

        if (tcpAcceptorThread != null) {
            tcpAcceptorThread.terminate();
            tcpAcceptorThread = null;
        }

        LOGGER.exiting(CLASS_NAME, "stopThreads");
    }

    /**
     * このInetSubnetを実行する。
     *
     * @return 停止から実行中に変更した場合はtrue、そうでなければfalse
     * @throws SubnetException 実行に失敗した場合
     */
    public synchronized boolean startService() throws SubnetException {
        LOGGER.entering(CLASS_NAME, "startService");

        if (isInService()) {
            LOGGER.exiting(CLASS_NAME, "startService", false);
            return false;
        }

        try {
            boolean result = getUDPNetwork().startService();
            if (result == false) {
                LOGGER.exiting(CLASS_NAME, "startService", false);
                return false;
            }

            result = getTCPReceiver().startService();
            if (result == false) {
                getUDPNetwork().stopService();
                LOGGER.exiting(CLASS_NAME, "startService", false);
                return false;
            }

            if (tcpAcceptorEnabled) {
                result = getTCPAcceptor().startService();
            }

            startThreads();

            LOGGER.exiting(CLASS_NAME, "startService", result);
            return true;
        } catch (NetworkException ex) {
            SubnetException exception = new SubnetException("catched exception", ex);
            LOGGER.throwing(CLASS_NAME, "startService", exception);
            throw exception;
        }
    }

    /**
     * このInetSubnetを停止する。
     *
     * @return 実行中から停止に変更した場合はtrue、そうでなければfalse
     */
    public synchronized boolean stopService() {
        LOGGER.entering(CLASS_NAME, "stopService");

        boolean result = true;

        if (!isInService()) {
            LOGGER.exiting(CLASS_NAME, "stopService", false);
            return false;
        }

        stopThreads();

        if (tcpAcceptorEnabled) {
            result &= getTCPAcceptor().stopService();
        }

        result &= getTCPReceiver().stopService();
        result &= getUDPNetwork().stopService();

        LOGGER.exiting(CLASS_NAME, "stopService", result);
        return result;
    }

    /**
     * 新たにTCP接続を確立しTCPConnectionを生成する。
     *
     * @param remoteNode TCP接続先のノード
     * @param timeout 接続確立までのタイムアウトを指定
     * @return 新たに作成されたTCPConnection
     * @throws SubnetException 接続の確立に失敗した場合
     */
    public TCPConnection newTCPConnection(Node remoteNode, int timeout) throws SubnetException {
        LOGGER.entering(CLASS_NAME, "newTCPConnection", new Object[]{remoteNode, timeout});

        TCPConnection connection;

        try {
            connection = new TCPConnection(getLocalNode().getNodeInfo(), remoteNode.getNodeInfo(), portNumber, timeout);
        } catch (NetworkException ex) {
            SubnetException exception = new SubnetException("catched exception", ex);
            LOGGER.throwing(CLASS_NAME, "newTCPConnection", exception);
            throw exception;
        }

        LOGGER.exiting(CLASS_NAME, "newTCPConnection", connection);
        return connection;
    }

    /**
     * 新たにTCP接続を確立しTCPConnectionを生成する。
     *
     * @param remoteNode TCP接続先のノード
     * @return 新たに作成されたTCPConnection
     * @throws SubnetException 接続の確立に失敗した場合
     */
    public TCPConnection newTCPConnection(Node remoteNode) throws SubnetException {
        LOGGER.entering(CLASS_NAME, "newTCPConnection", remoteNode);
        
        TCPConnection connection = newTCPConnection(remoteNode, 0);

        LOGGER.exiting(CLASS_NAME, "newTCPConnection", connection);
        return connection;
    }

    /**
     * TCPConnectionの登録を行う。 ここで登録されたTCPConnectionからの受信処理は自動的に処理されるようになる。
     *
     * @param connection 登録するTCPConnection
     * @return 追加に成功した場合にはtrue、そうでなければfalse
     */
    public boolean registerTCPConnection(TCPConnection connection) {
        LOGGER.entering(CLASS_NAME, "registerTCPConnection", connection);

        boolean result = getTCPReceiver().addConnection((TCPConnection) connection);

        LOGGER.exiting(CLASS_NAME, "createTCPConnection", result);
        return result;
    }

    /**
     * TCPConnectionの登録の抹消を行う。
     *
     * @param connection 登録を抹消するTCPConnection
     * @return 抹消に成功した場合にはtrue、そうでなければfalse
     */
    public boolean unregisterTCPConnection(TCPConnection connection) {
        LOGGER.entering(CLASS_NAME, "unregisterTCPConnection", connection);

        boolean result = getTCPReceiver().removeConnection((TCPConnection) connection);

        LOGGER.exiting(CLASS_NAME, "unregisterTCPConnection", result);
        return result;
    }

    /**
     * このInetSubnetのサブネットにフレームを転送する。
     * フレームの送信ノードや受信ノードがこのInetSubnetに含まれない場合には例外が発生する。
     *
     * @param frame 送信するフレーム
     * @return 常にtrue
     * @throws SubnetException 送信に失敗した場合
     */
    @Override
    public boolean send(Frame frame) throws SubnetException {
        LOGGER.entering(CLASS_NAME, "send", frame);

        if (!isInService()) {
            SubnetException exception = new SubnetException("not enabled");
            LOGGER.throwing(CLASS_NAME, "send", exception);
            throw exception;
        }

        if (!frame.getSender().isMemberOf(this)) {
            SubnetException exception = new SubnetException("invalid sender");
            LOGGER.throwing(CLASS_NAME, "send", exception);
            throw exception;
        }

        if (!frame.getReceiver().isMemberOf(this)) {
            SubnetException exception = new SubnetException("invalid receiver");
            LOGGER.throwing(CLASS_NAME, "send", exception);
            throw exception;
        }

        Connection connection = frame.getConnection();
        NodeInfo remoteNodeInfo = frame.getReceiver().getNodeInfo();

        try {
            if (connection != null) {
                if (!(connection instanceof TCPConnection)) {
                    SubnetException exception = new SubnetException("invalid connection: " + connection);
                    LOGGER.throwing(CLASS_NAME, "send", exception);
                    throw exception;
                }

                connection.send(frame.getCommonFrame());
            } else {
                if (!(remoteNodeInfo instanceof InetNodeInfo)) {
                    SubnetException exception = new SubnetException("invalid remote node: " + remoteNodeInfo);
                    LOGGER.throwing(CLASS_NAME, "send", exception);
                    throw exception;
                }
                getUDPNetwork().send((InetNodeInfo) remoteNodeInfo, frame.getCommonFrame());
            }

            LOGGER.exiting(CLASS_NAME, "send", true);
            return true;
        } catch (NetworkException ex) {
            SubnetException exception = new SubnetException("catched exception", ex);
            LOGGER.throwing(CLASS_NAME, "send", exception);
            throw exception;
        }
    }

    /**
     * このInetSubnetのサブネットからフレームを受信する。 少なくとも1つのフレームの受信を行うまで待機する。
     *
     * @return 受信したFrame
     * @throws SubnetException 無効なフレームを受信、あるいは受信に失敗した場合
     */
    @Override
    public Frame receive() throws SubnetException {
        LOGGER.entering(CLASS_NAME, "receive");

        if (!isInService()) {
            SubnetException exception = new SubnetException("not enabled");
            LOGGER.throwing(CLASS_NAME, "receive", exception);
            throw exception;
        }

        try {
            Frame frame = receiveQueue.take();
            LOGGER.exiting(CLASS_NAME, "receive", frame);
            return frame;
        } catch (InterruptedException ex) {
            SubnetException exception = new SubnetException("catched exception", ex);
            LOGGER.throwing(CLASS_NAME, "receive", exception);
            throw exception;
        }
    }

    /**
     * 指定されたアドレスが有効であるか返す。
     *
     * @param address アドレスの指定
     * @return アドレスが有効であればtrue、それ以外の場合にはfalse
     */
    public boolean isValidAddress(InetAddress address) {
        return true;
    }

    /**
     * 指定されたノード情報が有効であるか返す。
     *
     * @param nodeInfo ノード情報の指定
     * @return ノード情報が有効であればtrue、それ以外の場合にはfalse
     */
    public boolean isValidNodeInfo(InetNodeInfo nodeInfo) {
        return isValidAddress(nodeInfo.getAddress());
    }
    
    /**
     * このサブネットに含まれるリモートノードを表すNodeを返す。
     * @param name リモートノードの名前
     * @return リモートノードのNode
     * @throws SubnetException 適切な名前が指定されなかった場合
     */
    @Override
    public Node getRemoteNode(String name) throws SubnetException {
        try {
            InetAddress[] addrs = InetAddress.getAllByName(name);
            
            for (int i=0; i<addrs.length; i++) {
                InetAddress addr = addrs[i];
                if (isValidAddress(addr)) {
                    return new InetNode(this, addr);
                }
            }
        } catch (UnknownHostException ex) {
            throw new SubnetException("invalid name: " + name, ex);
        }
        
        throw new SubnetException("invalid name: " + name);
    }

    /**
     * リモートノードを表すNodeを生成する。
     *
     * @param addr リモートノードのIPアドレス
     * @return リモートノードのNode
     * @throws SubnetException 無効なアドレスが指定された場合
     */
    public Node getRemoteNode(InetAddress addr) throws SubnetException {
        LOGGER.entering(CLASS_NAME, "getRemoteNode", addr);
        
        if (isValidAddress(addr)) {
            InetNode inetNode = new InetNode(this, addr);
            LOGGER.exiting(CLASS_NAME, "getRemoteNode", inetNode);
            return inetNode;
        } else {
            SubnetException exception = new SubnetException("invalid address: " + addr);
            LOGGER.throwing(CLASS_NAME, "getRemoteNode", exception);
            throw exception;
        }
    }

    /**
     * リモートノードを表すNodeを生成する。
     *
     * @param nodeInfo リモートノードの情報
     * @return リモートノードのNode
     * @throws SubnetException 無効なノード情報が指定された場合
     */
    @Override
    public Node getRemoteNode(NodeInfo nodeInfo) throws SubnetException {
        LOGGER.entering(CLASS_NAME, "getRemoteNode", nodeInfo);
        
        if (nodeInfo instanceof InetNodeInfo) {
            InetNodeInfo inetNodeInfo = (InetNodeInfo) nodeInfo;

            if (isValidNodeInfo(inetNodeInfo)) {
                InetNode inetNode = new InetNode(this, (InetNodeInfo) nodeInfo);
                LOGGER.exiting(CLASS_NAME, "getRemoteNode", inetNode);
                return inetNode;
            } else {
                SubnetException exception = new SubnetException("invalid nodeInfo: " + nodeInfo);
                LOGGER.throwing(CLASS_NAME, "getRemoteNode", exception);
                throw exception;
            }
        } else {
            SubnetException exception = new SubnetException("invalid nodeInfo: " + nodeInfo);
            LOGGER.throwing(CLASS_NAME, "getRemoteNode", exception);
            throw exception;
        }
    }

    public InetAddress getLocalAddress() {
        if (localAddress != null) {
            return localAddress;
        } else {
            return loopbackAddress;
        }
    }

    /**
     * ローカルノードを表すNodeを返す。
     *
     * @return ローカルノードのNode
     */
    @Override
    public synchronized Node getLocalNode() {
        LOGGER.entering(CLASS_NAME, "getLocalNode");
        
        if (localNode == null) {
            localNode = new InetNode(this, getLocalAddress());
        }

        LOGGER.exiting(CLASS_NAME, "getLocalNode", localNode);
        return localNode;
    }

    /**
     * グループを表すNodeを返す。 このノード宛にフレームを転送するとマルチキャスト転送になる。
     *
     * @return グループのNode
     */
    @Override
    public synchronized Node getGroupNode() {
        LOGGER.entering(CLASS_NAME, "getGroupNode");
        
        if (groupNode == null) {
            groupNode = new InetNode(this, multicastAddress);
        }

        LOGGER.exiting(CLASS_NAME, "getGroupNode", groupNode);
        return groupNode;
    }
    
    public int getPortNumber() {
        LOGGER.entering(CLASS_NAME, "getPortNumber");
        
        LOGGER.exiting(CLASS_NAME, "getPortNumber", portNumber);
        return portNumber;
    }
    
    public synchronized boolean setPortNumber(int portNumber) {
        LOGGER.entering(CLASS_NAME, "setPortNumber", portNumber);
        
        if (isInService()) {
            LOGGER.exiting(CLASS_NAME, "setPortNumber", false);
            return false;
        }
        
        this.portNumber = portNumber;
        
        boolean result = true;
        result &= udpNetwork.setPortNumber(portNumber);
        result &= tcpAcceptor.setPortNumber(portNumber);
        
        LOGGER.exiting(CLASS_NAME, "setPortNumber", result);
        return result;
    }
    
    /**
     * リモートノードのポート番号を認識するかを返す。
     * ポート番号を認識する場合、ポート番号が異なる場合には異なるNodeを生成する。
     * @return リモートノードのポート番号を認識する場合にはtrue、そうでなければfalse
     */
    public boolean isRemotePortNumberEnabled() {
        LOGGER.entering(CLASS_NAME, "isRemotePortNumberEnabled");
        
        boolean result = udpNetwork.isRemotePortNumberEnabled();
        
        LOGGER.exiting(CLASS_NAME, "isRemotePortNumberEnabled", result);
        return result;
    }
    
    /**
     * リモートノードのポート番号を認識するように設定する。
     * サービス開始後に設定することはできない。
     * @return 設定に成功した場合にはtrue、そうでなければfalse
     */
    public boolean enableRemotePortNumber() {
        LOGGER.entering(CLASS_NAME, "enableRemotePortNumber");
        
        boolean result = udpNetwork.enableRemotePortNumber();
        
        LOGGER.exiting(CLASS_NAME, "enableRemotePortNumber", result);
        return result;
    }
    
    /**
     * リモートノードのポート番号を認識しないように設定する。
     * サービス開始後に設定することはできない。
     * @return 設定に成功した場合にはtrue、そうでなければfalse
     */
    public boolean disableRemotePortNumber() {
        LOGGER.entering(CLASS_NAME, "disableRemotePortNumber");
        
        boolean result = udpNetwork.disableRemotePortNumber();
        
        LOGGER.exiting(CLASS_NAME, "disableRemotePortNumber", result);
        return result;
    }
}

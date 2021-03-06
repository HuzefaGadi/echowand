package echowand.service;

import echowand.common.ClassEOJ;
import echowand.common.Data;
import echowand.common.EOJ;
import echowand.common.EPC;
import echowand.logic.AnnounceTransactionConfig;
import echowand.logic.SetGetTransactionConfig;
import echowand.logic.Transaction;
import echowand.logic.TransactionListener;
import echowand.logic.TransactionManager;
import echowand.net.Frame;
import echowand.net.Node;
import echowand.net.NodeInfo;
import echowand.net.Subnet;
import echowand.net.SubnetException;
import echowand.object.EchonetObject;
import echowand.object.EchonetObjectException;
import echowand.object.InstanceListRequestExecutor;
import echowand.object.LocalObject;
import echowand.object.LocalObjectManager;
import echowand.object.ObjectData;
import echowand.object.RemoteObject;
import echowand.object.RemoteObjectManager;
import echowand.service.result.CaptureResult;
import echowand.service.result.ResultBase;
import echowand.service.result.GetResult;
import echowand.service.result.FrameSelector;
import echowand.service.result.NotifyResult;
import echowand.service.result.ObserveResult;
import echowand.service.result.SetGetResult;
import echowand.service.result.SetResult;
import echowand.service.result.UpdateRemoteInfoResult;
import echowand.util.Pair;
import echowand.util.Selector;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ライブラリの様々な機能を提供するServiceインタフェース
 * @author ymakino
 */
public class Service {
    private static final Logger LOGGER = Logger.getLogger(Service.class.getName());
    private static final String CLASS_NAME = Service.class.getName();
    
    private Core core;
    
    /**
     * 利用するCoreを指定してServiceを生成する。
     * @param core 利用するCore
     */
    public Service(Core core) {
        LOGGER.entering(CLASS_NAME, "Service", core);
        
        this.core = core;
        
        LOGGER.exiting(CLASS_NAME, "Service");
    }
    
    /**
     * 利用するCoreを返す。
     * @return 利用するCore
     */
    public Core getCore() {
        return core;
    }
    
    /**
     * 利用するSubnetを返す。
     * @return 利用するSubnet
     */
    public Subnet getSubnet() {
        return core.getSubnet();
    }
    
    /**
     * 利用するLocalObjectManagerを返す。
     * @return 利用するLocalObjectManager
     */
    public LocalObjectManager getLocalObjectManager() {
        return core.getLocalObjectManager();
    }
    
    /**
     * 利用するRemoteObjectManagerを返す。
     * @return 利用するRemoteObjectManager
     */
    public RemoteObjectManager getRemoteObjectManager() {
        return core.getRemoteObjectManager();
    }
    
    /**
     * 利用するTransactionManagerを返す。
     * @return 利用するTransactionManager
     */
    public TransactionManager getTransactionManager() {
        return core.getTransactionManager();
    }
    
    private class ResultBaseTransactionListener implements TransactionListener {
        ResultBase result;
        
        public ResultBaseTransactionListener(ResultBase result) {
            LOGGER.entering(CLASS_NAME, "ResultBaseTransactionListener", result);
            
            this.result = result;
            
            LOGGER.entering(CLASS_NAME, "ResultBaseTransactionListener");
        }

        @Override
        public void begin(Transaction t) {
            LOGGER.entering(CLASS_NAME, "ResultBaseTransactionListener.begin", t);
            
            LOGGER.exiting(CLASS_NAME, "ResultBaseTransactionListener.begin");
        }

        @Override
        public void send(Transaction t, Subnet subnet, Frame frame, boolean success) {
            LOGGER.entering(CLASS_NAME, "ResultBaseTransactionListener.sent", new Object[]{t, subnet, frame, success});
            
            result.addRequestFrame(frame, success);
            
            LOGGER.exiting(CLASS_NAME, "ResultBaseTransactionListener.sent");
        }

        @Override
        public void receive(Transaction t, Subnet subnet, Frame frame) {
            LOGGER.entering(CLASS_NAME, "ResultBaseTransactionListener.receive", new Object[]{t, subnet, frame});
            
            result.addFrame(frame);
            
            LOGGER.exiting(CLASS_NAME, "ResultBaseTransactionListener.receive");
        }

        @Override
        public void finish(Transaction t) {
            LOGGER.entering(CLASS_NAME, "ResultBaseTransactionListener.finish", t);
            
            result.finish();
            
            LOGGER.exiting(CLASS_NAME, "ResultBaseTransactionListener.finish");
        }
    }
    
    private class ResultUpdateTransactionListener implements TransactionListener {
        private UpdateRemoteInfoResult resultUpdate;

        public ResultUpdateTransactionListener(UpdateRemoteInfoResult resultUpdate) {
            LOGGER.entering(CLASS_NAME, "ResultUpdateTransactionListener", resultUpdate);
            
            this.resultUpdate = resultUpdate;
            
            LOGGER.exiting(CLASS_NAME, "ResultUpdateTransactionListener");
        }

        @Override
        public void begin(Transaction t) {
            LOGGER.entering(CLASS_NAME, "ResultUpdateTransactionListener.begin", t);
            
            LOGGER.exiting(CLASS_NAME, "ResultUpdateTransactionListener.begin");
        }

        @Override
        public void send(Transaction t, Subnet subnet, Frame frame, boolean success) {
            LOGGER.entering(CLASS_NAME, "ResultUpdateTransactionListener.send", new Object[]{t, subnet, frame, success});
            
            LOGGER.exiting(CLASS_NAME, "ResultUpdateTransactionListener.send");
        }

        @Override
        public void receive(Transaction t, Subnet subnet, Frame frame) {
            LOGGER.entering(CLASS_NAME, "ResultUpdateTransactionListener.receive", new Object[]{t, subnet, frame});
            
            resultUpdate.addFrame(frame);
            
            LOGGER.exiting(CLASS_NAME, "ResultUpdateTransactionListener.receive");
        }

        @Override
        public void finish(Transaction t) {
            LOGGER.entering(CLASS_NAME, "ResultUpdateTransactionListener.finish", t);
            
            LOGGER.exiting(CLASS_NAME, "ResultUpdateTransactionListener.finish");
        }
    }
    
    private SetGetTransactionConfig createGetTransactionConfig(Node node, EOJ eoj, List<EPC> epcs) {
        LOGGER.entering(CLASS_NAME, "createGetTransactionConfig", new Object[]{node, eoj, epcs});
            
        SetGetTransactionConfig transactionConfig = new SetGetTransactionConfig();
        
        transactionConfig.setReceiverNode(node);
        transactionConfig.setDestinationEOJ(eoj);
        transactionConfig.setSenderNode(getSubnet().getLocalNode());
        transactionConfig.setSourceEOJ(new EOJ("0ef001"));
        
        for (EPC epc: epcs) {
            transactionConfig.addGet(epc);
        }
        
        LOGGER.exiting(CLASS_NAME, "createGetTransactionConfig", transactionConfig);
        return transactionConfig;
    }
    
    private SetGetTransactionConfig createSetTransactionConfig(Node node, EOJ eoj, List<Pair<EPC,Data>> properties) {
        LOGGER.entering(CLASS_NAME, "createSetTransactionConfig", new Object[]{node, eoj, properties});
        
        SetGetTransactionConfig transactionConfig = new SetGetTransactionConfig();
        
        transactionConfig.setReceiverNode(node);
        transactionConfig.setDestinationEOJ(eoj);
        transactionConfig.setSenderNode(getSubnet().getLocalNode());
        transactionConfig.setSourceEOJ(new EOJ("0ef001"));
        
        for (Pair<EPC,Data> property: properties) {
            transactionConfig.addSet(property.first, property.second);
        }
        
        LOGGER.exiting(CLASS_NAME, "createSetTransactionConfig", transactionConfig);
        return transactionConfig;
    }
    
    private SetGetTransactionConfig createSetGetTransactionConfig(Node node, EOJ eoj, List<Pair<EPC,Data>> properties, List<EPC> epcs) {
        LOGGER.entering(CLASS_NAME, "createSetGetTransactionConfig", new Object[]{node, eoj, properties});
        
        SetGetTransactionConfig transactionConfig = new SetGetTransactionConfig();
        
        transactionConfig.setReceiverNode(node);
        transactionConfig.setDestinationEOJ(eoj);
        transactionConfig.setSenderNode(getSubnet().getLocalNode());
        transactionConfig.setSourceEOJ(new EOJ("0ef001"));
        
        for (Pair<EPC,Data> property: properties) {
            transactionConfig.addSet(property.first, property.second);
        }
        
        for (EPC epc: epcs) {
            transactionConfig.addGet(epc);
        }
        
        LOGGER.exiting(CLASS_NAME, "createSetGetTransactionConfig", transactionConfig);
        return transactionConfig;
    }
    
    private <T> List<T> toList(T... objects) {
        LinkedList<T> list = new LinkedList<T>();
        list.addAll(Arrays.asList(objects));
        return list;
    }
    
    public GetResult doGet(Node node, ClassEOJ ceoj, EPC epc, int timeout) throws SubnetException {
        LOGGER.entering(CLASS_NAME, "doGet", new Object[]{node, ceoj, epc, timeout});
        
        GetResult getResult = doGet(node, ceoj.getAllInstanceEOJ(), toList(epc), timeout);
        
        LOGGER.exiting(CLASS_NAME, "doGet", getResult);
        return getResult;
    }
    
    public GetResult doGet(Node node, EOJ eoj, EPC epc, int timeout) throws SubnetException {
        LOGGER.entering(CLASS_NAME, "doGet", new Object[]{node, eoj, epc, timeout});
        
        GetResult getResult = doGet(node, eoj, toList(epc), timeout);
        
        LOGGER.exiting(CLASS_NAME, "doGet", getResult);
        return getResult;
    }
    
    public GetResult doGet(Node node, ClassEOJ ceoj, List<EPC> epcs, int timeout) throws SubnetException {
        LOGGER.entering(CLASS_NAME, "doGet", new Object[]{node, ceoj, epcs, timeout});
        
        GetResult getResult = doGet(node, ceoj.getAllInstanceEOJ(), epcs, timeout);
        
        LOGGER.exiting(CLASS_NAME, "doGet", getResult);
        return getResult;
    }
    
    public GetResult doGet(Node node, EOJ eoj, List<EPC> epcs, int timeout) throws SubnetException {
        LOGGER.entering(CLASS_NAME, "doGet", new Object[]{node, eoj, epcs, timeout});
        
        GetResult getResult = new GetResult();
        
        SetGetTransactionConfig transactionConfig = createGetTransactionConfig(node, eoj, epcs);
        
        Transaction transaction = new Transaction(getSubnet(), getTransactionManager(), transactionConfig);
        transaction.setTimeout(timeout);
        
        transaction.addTransactionListener(new ResultBaseTransactionListener(getResult));
        
        transaction.execute();
        
        LOGGER.exiting(CLASS_NAME, "doGet", getResult);
        return getResult;
    }
    
    public GetResult doGet(NodeInfo nodeInfo, EOJ eoj, EPC epc, int timeout) throws SubnetException {
        LOGGER.entering(CLASS_NAME, "doGet", new Object[]{nodeInfo, eoj, epc, timeout});
        
        GetResult getResult = doGet(getSubnet().getRemoteNode(nodeInfo), eoj, toList(epc), timeout);
        
        LOGGER.exiting(CLASS_NAME, "doGet", getResult);
        return getResult;
    }
    
    public GetResult doGet(NodeInfo nodeInfo, ClassEOJ ceoj, EPC epc, int timeout) throws SubnetException {
        LOGGER.entering(CLASS_NAME, "doGet", new Object[]{nodeInfo, ceoj, epc, timeout});
        
        GetResult getResult = doGet(getSubnet().getRemoteNode(nodeInfo), ceoj, toList(epc), timeout);
        
        LOGGER.exiting(CLASS_NAME, "doGet", getResult);
        return getResult;
    }
    
    public GetResult doGet(NodeInfo nodeInfo, EOJ eoj, List<EPC> epcs, int timeout) throws SubnetException {
        LOGGER.entering(CLASS_NAME, "doGet", new Object[]{nodeInfo, eoj, epcs, timeout});
        
        GetResult getResult = doGet(getSubnet().getRemoteNode(nodeInfo), eoj, epcs, timeout);
        
        LOGGER.exiting(CLASS_NAME, "doGet", getResult);
        return getResult;
    }
    
    public GetResult doGet(NodeInfo nodeInfo, ClassEOJ ceoj, List<EPC> epcs, int timeout) throws SubnetException {
        LOGGER.entering(CLASS_NAME, "doGet", new Object[]{nodeInfo, ceoj, epcs, timeout});
        
        GetResult getResult = doGet(getSubnet().getRemoteNode(nodeInfo), ceoj, epcs, timeout);
        
        LOGGER.exiting(CLASS_NAME, "doGet", getResult);
        return getResult;
    }
    
    public SetResult doSet(Node node, EOJ eoj, List<Pair<EPC, Data>> properties, int timeout, boolean responseRequired) throws SubnetException {
        LOGGER.entering(CLASS_NAME, "doSet", new Object[]{node, eoj, properties, timeout, responseRequired});
        
        SetResult setResult = new SetResult(responseRequired);
        
        SetGetTransactionConfig transactionConfig = createSetTransactionConfig(node, eoj, properties);
        transactionConfig.setResponseRequired(responseRequired);
        
        Transaction transaction = new Transaction(getSubnet(), getTransactionManager(), transactionConfig);
        transaction.setTimeout(timeout);
        
        transaction.addTransactionListener(new ResultBaseTransactionListener(setResult));
        
        transaction.execute();

        LOGGER.exiting(CLASS_NAME, "doSet", setResult);
        return setResult;
    }
    
    public SetResult doSet(Node node, EOJ eoj, EPC epc, Data data, int timeout) throws SubnetException {
        LOGGER.entering(CLASS_NAME, "doSet", new Object[]{node, eoj, epc, data, timeout});
        
        SetResult setResult = doSet(node, eoj, epc, data, timeout, true);

        LOGGER.exiting(CLASS_NAME, "doSet", setResult);
        return setResult;
    }
    
    public SetResult doSet(Node node, ClassEOJ ceoj, EPC epc, Data data, int timeout) throws SubnetException {
        LOGGER.entering(CLASS_NAME, "doSet", new Object[]{node, ceoj, epc, data, timeout});
        
        SetResult setResult = doSet(node, ceoj, epc, data, timeout, true);

        LOGGER.exiting(CLASS_NAME, "doSet", setResult);
        return setResult;
    }
    
    public SetResult doSet(Node node, EOJ eoj, List<Pair<EPC, Data>> properties, int timeout) throws SubnetException {
        LOGGER.entering(CLASS_NAME, "doSet", new Object[]{node, eoj, properties, timeout});
                
        SetResult setResult = doSet(node, eoj, properties, timeout, true);

        LOGGER.exiting(CLASS_NAME, "doSet", setResult);
        return setResult;
    }
    
    public SetResult doSet(Node node, ClassEOJ ceoj, List<Pair<EPC, Data>> properties, int timeout) throws SubnetException {
        LOGGER.entering(CLASS_NAME, "doSet", new Object[]{node, ceoj, properties, timeout});
                
        SetResult setResult = doSet(node, ceoj, properties, timeout, true);

        LOGGER.exiting(CLASS_NAME, "doSet", setResult);
        return setResult;
    }
    
    public SetResult doSet(NodeInfo nodeInfo, EOJ eoj, EPC epc, Data data, int timeout) throws SubnetException {
        LOGGER.entering(CLASS_NAME, "doSet", new Object[]{nodeInfo, eoj, epc, data, timeout});
                
        SetResult setResult = doSet(nodeInfo, eoj, epc, data, timeout, true);

        LOGGER.exiting(CLASS_NAME, "doSet", setResult);
        return setResult;
    }
    
    public SetResult doSet(NodeInfo nodeInfo, ClassEOJ ceoj, EPC epc, Data data, int timeout) throws SubnetException {
        LOGGER.entering(CLASS_NAME, "doSet", new Object[]{nodeInfo, ceoj, epc, data, timeout});
        
        SetResult setResult = doSet(nodeInfo, ceoj, epc, data, timeout, true);

        LOGGER.exiting(CLASS_NAME, "doSet", setResult);
        return setResult;
    }
    
    public SetResult doSet(NodeInfo nodeInfo, EOJ eoj, List<Pair<EPC, Data>> properties, int timeout) throws SubnetException {
        LOGGER.entering(CLASS_NAME, "doSet", new Object[]{nodeInfo, eoj, properties, timeout});
        
        SetResult setResult = doSet(nodeInfo, eoj, properties, timeout, true);

        LOGGER.exiting(CLASS_NAME, "doSet", setResult);
        return setResult;
    }
    
    public SetResult doSet(NodeInfo nodeInfo, ClassEOJ ceoj, List<Pair<EPC, Data>> properties, int timeout) throws SubnetException {
        LOGGER.entering(CLASS_NAME, "doSet", new Object[]{nodeInfo, ceoj, properties, timeout});
        
        SetResult setResult = doSet(nodeInfo, ceoj, properties, timeout, true);

        LOGGER.exiting(CLASS_NAME, "doSet", setResult);
        return setResult;
    }
    
    public SetResult doSet(Node node, EOJ eoj, EPC epc, Data data, int timeout, boolean responseRequired) throws SubnetException {
        LOGGER.entering(CLASS_NAME, "doSet", new Object[]{node, eoj, epc, data, timeout, responseRequired});
        
        LinkedList<Pair<EPC, Data>> properties = new LinkedList<Pair<EPC, Data>>();
        properties.add(new Pair<EPC, Data>(epc, data));
        SetResult setResult = doSet(node, eoj, properties, timeout, responseRequired);

        LOGGER.exiting(CLASS_NAME, "doSet", setResult);
        return setResult;
    }
    
    public SetResult doSet(Node node, ClassEOJ ceoj, EPC epc, Data data, int timeout, boolean responseRequired) throws SubnetException {
        LOGGER.entering(CLASS_NAME, "doSet", new Object[]{node, ceoj, epc, data, timeout, responseRequired});
        
        LinkedList<Pair<EPC, Data>> properties = new LinkedList<Pair<EPC, Data>>();
        properties.add(new Pair<EPC, Data>(epc, data));
        SetResult setResult = doSet(node, ceoj.getAllInstanceEOJ(), properties, timeout, responseRequired);

        LOGGER.exiting(CLASS_NAME, "doSet", setResult);
        return setResult;
    }
    
    public SetResult doSet(Node node, ClassEOJ ceoj, List<Pair<EPC, Data>> properties, int timeout, boolean responseRequired) throws SubnetException {
        LOGGER.entering(CLASS_NAME, "doSet", new Object[]{node, ceoj, properties, timeout, responseRequired});
        
        SetResult setResult = doSet(node, ceoj.getAllInstanceEOJ(), properties, timeout, responseRequired);

        LOGGER.exiting(CLASS_NAME, "doSet", setResult);
        return setResult;
    }
    
    public SetResult doSet(NodeInfo nodeInfo, EOJ eoj, EPC epc, Data data, int timeout, boolean responseRequired) throws SubnetException {
        LOGGER.entering(CLASS_NAME, "doSet", new Object[]{nodeInfo, eoj, epc, data, timeout, responseRequired});
        
        SetResult setResult = doSet(getSubnet().getRemoteNode(nodeInfo), eoj, epc, data, timeout, responseRequired);

        LOGGER.exiting(CLASS_NAME, "doSet", setResult);
        return setResult;
    }
    
    public SetResult doSet(NodeInfo nodeInfo, ClassEOJ ceoj, EPC epc, Data data, int timeout, boolean responseRequired) throws SubnetException {
        LOGGER.entering(CLASS_NAME, "doSet", new Object[]{nodeInfo, ceoj, epc, data, timeout, responseRequired});
        
        SetResult setResult = doSet(getSubnet().getRemoteNode(nodeInfo), ceoj, epc, data, timeout, responseRequired);

        LOGGER.exiting(CLASS_NAME, "doSet", setResult);
        return setResult;
    }
    
    public SetResult doSet(NodeInfo nodeInfo, EOJ eoj, List<Pair<EPC, Data>> properties, int timeout, boolean responseRequired) throws SubnetException {
        LOGGER.entering(CLASS_NAME, "doSet", new Object[]{nodeInfo, eoj, properties, timeout, responseRequired});
        
        SetResult setResult = doSet(getSubnet().getRemoteNode(nodeInfo), eoj, properties, timeout, responseRequired);

        LOGGER.exiting(CLASS_NAME, "doSet", setResult);
        return setResult;
    }
    
    public SetResult doSet(NodeInfo nodeInfo, ClassEOJ ceoj, List<Pair<EPC, Data>> properties, int timeout, boolean responseRequired) throws SubnetException {
        LOGGER.entering(CLASS_NAME, "doSet", new Object[]{nodeInfo, ceoj, properties, timeout, responseRequired});
        
        SetResult setResult = doSet(getSubnet().getRemoteNode(nodeInfo), ceoj, properties, timeout, responseRequired);

        LOGGER.exiting(CLASS_NAME, "doSet", setResult);
        return setResult;
    }
    
    public SetGetResult doSetGet(NodeInfo nodeInfo, ClassEOJ ceoj, EPC setEPC, Data setData, EPC getEPC, int timeout) throws SubnetException {
        LOGGER.entering(CLASS_NAME, "doSetGet", new Object[]{nodeInfo, ceoj, setEPC, setData, getEPC, timeout});
        
        SetGetResult setGetResult = doSetGet(getSubnet().getRemoteNode(nodeInfo), ceoj.getAllInstanceEOJ(), setEPC, setData, toList(getEPC), timeout);
        
        LOGGER.exiting(CLASS_NAME, "doSetGet", setGetResult);
        return setGetResult;
    }
    
    public SetGetResult doSetGet(NodeInfo nodeInfo, EOJ eoj, EPC setEPC, Data setData, EPC getEPC, int timeout) throws SubnetException {
        LOGGER.entering(CLASS_NAME, "doSetGet", new Object[]{nodeInfo, eoj, setEPC, setData, getEPC, timeout});
        
        SetGetResult setGetResult = doSetGet(getSubnet().getRemoteNode(nodeInfo), eoj, setEPC, setData, toList(getEPC), timeout);
        
        LOGGER.exiting(CLASS_NAME, "doSetGet", setGetResult);
        return setGetResult;
    }
    
    public SetGetResult doSetGet(Node node, ClassEOJ ceoj, EPC setEPC, Data setData, EPC getEPC, int timeout) throws SubnetException {
        LOGGER.entering(CLASS_NAME, "doSetGet", new Object[]{node, ceoj, setEPC, setData, getEPC, timeout});
        
        SetGetResult setGetResult = doSetGet(node, ceoj.getAllInstanceEOJ(), setEPC, setData, toList(getEPC), timeout);
        
        LOGGER.exiting(CLASS_NAME, "doSetGet", setGetResult);
        return setGetResult;
    }
    
    public SetGetResult doSetGet(Node node, EOJ eoj, EPC setEPC, Data setData, EPC getEPC, int timeout) throws SubnetException {
        LOGGER.entering(CLASS_NAME, "doSetGet", new Object[]{node, eoj, setEPC, setData, getEPC, timeout});
        
        SetGetResult setGetResult = doSetGet(node, eoj, setEPC, setData, toList(getEPC), timeout);
        
        LOGGER.exiting(CLASS_NAME, "doSetGet", setGetResult);
        return setGetResult;
    }
    
    public SetGetResult doSetGet(NodeInfo nodeInfo, ClassEOJ ceoj, List<Pair<EPC, Data>> properties, EPC getEPC, int timeout) throws SubnetException {
        LOGGER.entering(CLASS_NAME, "doSetGet", new Object[]{nodeInfo, ceoj, properties, getEPC, timeout});
        
        SetGetResult setGetResult = doSetGet(getSubnet().getRemoteNode(nodeInfo), ceoj.getAllInstanceEOJ(), properties, toList(getEPC), timeout);
        
        LOGGER.exiting(CLASS_NAME, "doSetGet", setGetResult);
        return setGetResult;
    }
    
    public SetGetResult doSetGet(NodeInfo nodeInfo, EOJ eoj, List<Pair<EPC, Data>> properties, EPC getEPC, int timeout) throws SubnetException {
        LOGGER.entering(CLASS_NAME, "doSetGet", new Object[]{nodeInfo, eoj, properties, getEPC, timeout});
        
        SetGetResult setGetResult = doSetGet(getSubnet().getRemoteNode(nodeInfo), eoj, properties, toList(getEPC), timeout);
        
        LOGGER.exiting(CLASS_NAME, "doSetGet", setGetResult);
        return setGetResult;
    }
    
    public SetGetResult doSetGet(Node node, ClassEOJ ceoj, List<Pair<EPC, Data>> properties, EPC getEPC, int timeout) throws SubnetException {
        LOGGER.entering(CLASS_NAME, "doSetGet", new Object[]{node, ceoj, getEPC, timeout});
        
        SetGetResult setGetResult = doSetGet(node, ceoj.getAllInstanceEOJ(), properties, toList(getEPC), timeout);
        
        LOGGER.exiting(CLASS_NAME, "doSetGet", setGetResult);
        return setGetResult;
    }
    
    public SetGetResult doSetGet(Node node, EOJ eoj, List<Pair<EPC, Data>> properties, EPC getEPC, int timeout) throws SubnetException {
        LOGGER.entering(CLASS_NAME, "doSetGet", new Object[]{node, eoj, properties, getEPC, timeout});
        
        SetGetResult setGetResult = doSetGet(node, eoj, properties, toList(getEPC), timeout);

        LOGGER.exiting(CLASS_NAME, "doSetGet", setGetResult);
        return setGetResult;
    }
    
    public SetGetResult doSetGet(NodeInfo nodeInfo, ClassEOJ ceoj, EPC setEPC, Data setData, List<EPC> epcs, int timeout) throws SubnetException {
        LOGGER.entering(CLASS_NAME, "doSetGet", new Object[]{nodeInfo, ceoj, setEPC, setData, epcs, timeout});
        
        SetGetResult setGetResult = doSetGet(getSubnet().getRemoteNode(nodeInfo), ceoj.getAllInstanceEOJ(), setEPC, setData, epcs, timeout);
        
        LOGGER.exiting(CLASS_NAME, "doSetGet", setGetResult);
        return setGetResult;
    }
    
    public SetGetResult doSetGet(NodeInfo nodeInfo, EOJ eoj, EPC setEPC, Data setData, List<EPC> epcs, int timeout) throws SubnetException {
        LOGGER.entering(CLASS_NAME, "doSetGet", new Object[]{nodeInfo, eoj, setEPC, setData, epcs, timeout});
        
        SetGetResult setGetResult = doSetGet(getSubnet().getRemoteNode(nodeInfo), eoj, setEPC, setData, epcs, timeout);
        
        LOGGER.exiting(CLASS_NAME, "doSetGet", setGetResult);
        return setGetResult;
    }
    
    public SetGetResult doSetGet(Node node, ClassEOJ ceoj, EPC setEPC, Data setData, List<EPC> epcs, int timeout) throws SubnetException {
        LOGGER.entering(CLASS_NAME, "doSetGet", new Object[]{node, ceoj, setEPC, setData, epcs, timeout});
        
        SetGetResult setGetResult = doSetGet(node, ceoj.getAllInstanceEOJ(), setEPC, setData, epcs, timeout);
        
        LOGGER.exiting(CLASS_NAME, "doSetGet", setGetResult);
        return setGetResult;
    }
    
    public SetGetResult doSetGet(Node node, EOJ eoj, EPC setEPC, Data setData, List<EPC> epcs, int timeout) throws SubnetException {
        LOGGER.entering(CLASS_NAME, "doSetGet", new Object[]{node, eoj, setEPC, setData, epcs, timeout});
        
        LinkedList<Pair<EPC, Data>> properties = new LinkedList<Pair<EPC, Data>>();
        properties.add(new Pair<EPC, Data>(setEPC, setData));
        SetGetResult setGetResult = doSetGet(node, eoj, properties, epcs, timeout);
        
        LOGGER.exiting(CLASS_NAME, "doSetGet", setGetResult);
        return setGetResult;
    }
    
    public SetGetResult doSetGet(NodeInfo nodeInfo, ClassEOJ ceoj, List<Pair<EPC, Data>> properties, List<EPC> epcs, int timeout) throws SubnetException {
        LOGGER.entering(CLASS_NAME, "doSetGet", new Object[]{nodeInfo, ceoj, properties, epcs, timeout});
        
        SetGetResult setGetResult = doSetGet(getRemoteNode(nodeInfo), ceoj.getAllInstanceEOJ(), properties, epcs, timeout);
        
        LOGGER.exiting(CLASS_NAME, "doSetGet", setGetResult);
        return setGetResult;
    }
    
    public SetGetResult doSetGet(NodeInfo nodeInfo, EOJ eoj, List<Pair<EPC, Data>> properties, List<EPC> epcs, int timeout) throws SubnetException {
        LOGGER.entering(CLASS_NAME, "doSetGet", new Object[]{nodeInfo, eoj, properties, epcs, timeout});
        
        SetGetResult setGetResult = doSetGet(getRemoteNode(nodeInfo), eoj, properties, epcs, timeout);
        
        LOGGER.exiting(CLASS_NAME, "doSetGet", setGetResult);
        return setGetResult;
    }
    
    public SetGetResult doSetGet(Node node, ClassEOJ ceoj, List<Pair<EPC, Data>> properties, List<EPC> epcs, int timeout) throws SubnetException {
        LOGGER.entering(CLASS_NAME, "doSetGet", new Object[]{node, ceoj, epcs, timeout});
        
        SetGetResult setGetResult = doSetGet(node, ceoj.getAllInstanceEOJ(), properties, epcs, timeout);
        
        LOGGER.exiting(CLASS_NAME, "doSetGet", setGetResult);
        return setGetResult;
    }
    
    public SetGetResult doSetGet(Node node, EOJ eoj, List<Pair<EPC, Data>> properties, List<EPC> epcs, int timeout) throws SubnetException {
        LOGGER.entering(CLASS_NAME, "doSetGet", new Object[]{node, eoj, properties, epcs, timeout});
        
        SetGetResult setGetResult = new SetGetResult();
        
        SetGetTransactionConfig transactionConfig = createSetGetTransactionConfig(node, eoj, properties, epcs);
        
        Transaction transaction = new Transaction(getSubnet(), getTransactionManager(), transactionConfig);
        transaction.setTimeout(timeout);
        
        transaction.addTransactionListener(new ResultBaseTransactionListener(setGetResult));
        
        transaction.execute();

        LOGGER.exiting(CLASS_NAME, "doSetGet", setGetResult);
        return setGetResult;
    }
    
    public UpdateRemoteInfoResult doUpdateRemoteInfo(int timeout) throws SubnetException {
        LOGGER.entering(CLASS_NAME, "doUpdateRemoteInfo", timeout);
        
        InstanceListRequestExecutor executor = new InstanceListRequestExecutor(
                getSubnet(), getTransactionManager(), getRemoteObjectManager());
        
        UpdateRemoteInfoResult updateRemoteInfoResult = new UpdateRemoteInfoResult(executor);
        ResultUpdateTransactionListener resultUpdateTransactionListener = new ResultUpdateTransactionListener(updateRemoteInfoResult);

        executor.setTimeout(timeout);
        executor.addTransactionListener(resultUpdateTransactionListener);
        
        executor.execute();
        
        LOGGER.exiting(CLASS_NAME, "doUpdateRemoteInfo", updateRemoteInfoResult);
        return updateRemoteInfoResult;
    }
    
    public ObserveResult doObserve(Node node, EOJ eoj, EPC epc) throws SubnetException {
        LOGGER.entering(CLASS_NAME, "doObserve", new Object[]{node, eoj, epc});
        
        ObserveResult observeResult = doObserve(new FrameSelector(node, eoj, epc));
        
        LOGGER.exiting(CLASS_NAME, "doObserve", observeResult);
        return observeResult;
    }
    
    public ObserveResult doObserve(Node node, ClassEOJ ceoj, EPC epc) throws SubnetException {
        LOGGER.entering(CLASS_NAME, "doObserve", new Object[]{node, ceoj, epc});
        
        ObserveResult observeResult = doObserve(new FrameSelector(node, ceoj, epc));
        
        LOGGER.exiting(CLASS_NAME, "doObserve", observeResult);
        return observeResult;
    }
    
    public ObserveResult doObserve(Node node, EOJ eoj, List<EPC> epcs) throws SubnetException {
        LOGGER.entering(CLASS_NAME, "doObserve", new Object[]{node, eoj, epcs});
        
        ObserveResult observeResult = doObserve(new FrameSelector(node, eoj, epcs));
        
        LOGGER.exiting(CLASS_NAME, "doObserve", observeResult);
        return observeResult;
    }
    
    public ObserveResult doObserve(Node node, ClassEOJ ceoj, List<EPC> epcs) throws SubnetException {
        LOGGER.entering(CLASS_NAME, "doObserve", new Object[]{node, ceoj, epcs});
        
        ObserveResult observeResult = doObserve(new FrameSelector(node, ceoj, epcs));
        
        LOGGER.exiting(CLASS_NAME, "doObserve", observeResult);
        return observeResult;
    }
    
    public ObserveResult doObserve(NodeInfo nodeInfo, EOJ eoj, EPC epc) throws SubnetException {
        LOGGER.entering(CLASS_NAME, "doObserve", new Object[]{nodeInfo, eoj, epc});
        
        ObserveResult observeResult = doObserve(getRemoteNode(nodeInfo), eoj, epc);
        
        LOGGER.exiting(CLASS_NAME, "doObserve", observeResult);
        return observeResult;
    }
    
    public ObserveResult doObserve(NodeInfo nodeInfo, ClassEOJ ceoj, EPC epc) throws SubnetException {
        LOGGER.entering(CLASS_NAME, "doObserve", new Object[]{nodeInfo, ceoj, epc});
        
        ObserveResult observeResult = doObserve(getRemoteNode(nodeInfo), ceoj, epc);
        
        LOGGER.exiting(CLASS_NAME, "doObserve", observeResult);
        return observeResult;
    }
    
    public ObserveResult doObserve(NodeInfo nodeInfo, EOJ eoj, List<EPC> epcs) throws SubnetException {
        LOGGER.entering(CLASS_NAME, "doObserve", new Object[]{nodeInfo, eoj, epcs});
        
        ObserveResult observeResult = doObserve(getRemoteNode(nodeInfo), eoj, epcs);
        
        LOGGER.exiting(CLASS_NAME, "doObserve", observeResult);
        return observeResult;
    }
    
    public ObserveResult doObserve(NodeInfo nodeInfo, ClassEOJ ceoj, List<EPC> epcs) throws SubnetException {
        LOGGER.entering(CLASS_NAME, "doObserve", new Object[]{nodeInfo, ceoj, epcs});
        
        ObserveResult observeResult = doObserve(getRemoteNode(nodeInfo), ceoj, epcs);
        
        LOGGER.exiting(CLASS_NAME, "doObserve", observeResult);
        return observeResult;
    }
    
    public ObserveResult doObserve(EOJ eoj, EPC epc) throws SubnetException {
        LOGGER.entering(CLASS_NAME, "doObserve", new Object[]{eoj, epc});
        
        ObserveResult observeResult = doObserve(null, toList(eoj), toList(epc));
        
        LOGGER.exiting(CLASS_NAME, "doObserve", observeResult);
        return observeResult;
    }
    
    public ObserveResult doObserve(ClassEOJ ceoj, EPC epc) throws SubnetException {
        LOGGER.entering(CLASS_NAME, "doObserve", new Object[]{ceoj, epc});
        
        ObserveResult observeResult = doObserve(null, toList(ceoj.getAllInstanceEOJ()), toList(epc));
        
        LOGGER.exiting(CLASS_NAME, "doObserve", observeResult);
        return observeResult;
    }
    
    public ObserveResult doObserve(EOJ eoj, List<EPC> epcs) throws SubnetException {
        LOGGER.entering(CLASS_NAME, "doObserve", new Object[]{eoj, epcs});
        
        ObserveResult observeResult = doObserve(null, toList(eoj), epcs);
        
        LOGGER.exiting(CLASS_NAME, "doObserve", observeResult);
        return observeResult;
    }
    
    public ObserveResult doObserve(ClassEOJ ceoj, List<EPC> epcs) throws SubnetException {
        LOGGER.entering(CLASS_NAME, "doObserve", new Object[]{ceoj, epcs});
        
        ObserveResult observeResult = doObserve(null, toList(ceoj.getAllInstanceEOJ()), epcs);
        
        LOGGER.exiting(CLASS_NAME, "doObserve", observeResult);
        return observeResult;
    }
    
    public ObserveResult doObserve(EOJ eoj) throws SubnetException {
        LOGGER.entering(CLASS_NAME, "doObserve", eoj);
        
        ObserveResult observeResult = doObserve(null, toList(eoj), null);
        
        LOGGER.exiting(CLASS_NAME, "doObserve", observeResult);
        return observeResult;
    }
    
    public ObserveResult doObserve(ClassEOJ ceoj) throws SubnetException {
        LOGGER.entering(CLASS_NAME, "doObserve", ceoj);
        
        ObserveResult observeResult = doObserve(null, toList(ceoj.getAllInstanceEOJ()), null);
        
        LOGGER.exiting(CLASS_NAME, "doObserve", observeResult);
        return observeResult;
    }
    
    public ObserveResult doObserve(EPC epc) throws SubnetException {
        LOGGER.entering(CLASS_NAME, "doObserve", epc);
        
        ObserveResult observeResult = doObserve(null, new ArrayList<EOJ>(), toList(epc));
        
        LOGGER.exiting(CLASS_NAME, "doObserve", observeResult);
        return observeResult;
    }
    
    public ObserveResult doObserve(List<EPC> epcs) throws SubnetException {
        LOGGER.entering(CLASS_NAME, "doObserve", epcs);
        
        ObserveResult observeResult = doObserve(null, new ArrayList<EOJ>(), epcs);
        
        LOGGER.exiting(CLASS_NAME, "doObserve", observeResult);
        return observeResult;
    }
    
    public ObserveResult doObserve() {
        LOGGER.entering(CLASS_NAME, "doObserve");
        
        ObserveResult observeResult = doObserve(new FrameSelector());
        
        LOGGER.exiting(CLASS_NAME, "doObserve", observeResult);
        return observeResult;
    }
    
    private Node toNodeFromNodeOrNodeInfo(Object o) throws SubnetException {
        if (o instanceof Node) {
            return (Node)o;
        }
        
        if (o instanceof NodeInfo) {
            NodeInfo nodeInfo = (NodeInfo)o;
            return getRemoteNode(nodeInfo);
        }
        
        throw new SubnetException("Invalid node: " + o);
    }
    
    private List<Node> toNodesFromNodesAndNodeInfos(List nodes) throws SubnetException {
        LinkedList<Node> newNodes = new LinkedList<Node>();
        
        if (nodes == null) {
            return newNodes;
        }
        
        for (Object object: nodes) {
            newNodes.add(toNodeFromNodeOrNodeInfo(object));
        }
        
        return newNodes;
    }
    
    public ObserveResult doObserve(List nodes, List<EOJ> eojs, List<EPC> epcs) throws SubnetException {
        LOGGER.entering(CLASS_NAME, "doObserve", new Object[]{nodes, eojs, epcs});
        
        ObserveResult observeResult = doObserve(new FrameSelector(toNodesFromNodesAndNodeInfos(nodes), eojs, epcs));
        
        LOGGER.exiting(CLASS_NAME, "doObserve", observeResult);
        return observeResult;
    }
    
    public ObserveResult doObserve(Selector<? super Frame> selector) {
        LOGGER.entering(CLASS_NAME, "doObserve", selector);
        
        ObserveResultProcessor processor = getCore().getObserveResultProsessor();
        ObserveResult observeResult = new ObserveResult(selector, processor);
        
        processor.addObserveResult(observeResult);
        
        LOGGER.exiting(CLASS_NAME, "doObserve", observeResult);
        return observeResult;
    }
    
    public boolean isCaptureEnabled() {
        return getCore().isCaptureEnabled();
    }
    
    public CaptureResult doCapture() {
        LOGGER.entering(CLASS_NAME, "doCapture");
        
        CaptureResultObserver observer = getCore().getCaptureResultObserver();
        CaptureResult captureResult = new CaptureResult(observer);
        observer.addCaptureResult(captureResult);
        
        LOGGER.exiting(CLASS_NAME, "doCapture", captureResult);
        return captureResult;
    }
    
    public ObjectData getLocalData(EOJ eoj, EPC epc) throws ObjectNotFoundException {
        LOGGER.entering(CLASS_NAME, "getLocalData", new Object[]{eoj, epc});
        
        LocalObject localObject = getLocalObject(eoj);
        
        if (localObject == null) {
            ObjectNotFoundException exception = new ObjectNotFoundException(eoj.toString());
            LOGGER.throwing(CLASS_NAME, "getLocalData", exception);
            throw exception;
        }
        
        ObjectData objectData = localObject.getData(epc);
        
        LOGGER.exiting(CLASS_NAME, "getLocalData", objectData);
        return objectData;
    }
    
    public boolean setLocalData(EOJ eoj, EPC epc, ObjectData data) throws ObjectNotFoundException {
        LOGGER.entering(CLASS_NAME, "setLocalData", new Object[]{eoj, epc, data});
        
        LocalObject localObject = getLocalObject(eoj);
        
        if (localObject == null) {
            ObjectNotFoundException exception = new ObjectNotFoundException(eoj.toString());
            LOGGER.throwing(CLASS_NAME, "setLocalData", exception);
            throw exception;
        }
        
        boolean result = localObject.forceSetData(epc, data);
        
        LOGGER.exiting(CLASS_NAME, "setLocalData", result);
        return result;
    }
    
    public ObjectData getRemoteData(NodeInfo nodeInfo, EOJ eoj, EPC epc) throws ObjectNotFoundException, SubnetException, EchonetObjectException {
        LOGGER.entering(CLASS_NAME, "getRemoteData", new Object[]{nodeInfo, eoj, epc});
        
        ObjectData objectData = getRemoteData(getRemoteNode(nodeInfo), eoj, epc);
        
        LOGGER.exiting(CLASS_NAME, "getRemoteData", objectData);
        return objectData;
    }
    
    public ObjectData getRemoteData(Node node, EOJ eoj, EPC epc) throws ObjectNotFoundException, EchonetObjectException {
        LOGGER.entering(CLASS_NAME, "getRemoteData", new Object[]{node, eoj, epc});
        
        RemoteObject remoteObject = getRemoteObject(node, eoj);
        
        if (remoteObject == null) {
            ObjectNotFoundException exception = new ObjectNotFoundException(eoj.toString());
            LOGGER.throwing(CLASS_NAME, "getRemoteData", exception);
            throw exception;
        }
        
        ObjectData objectData = remoteObject.getData(epc);
        
        LOGGER.exiting(CLASS_NAME, "getRemoteData", objectData);
        return objectData;
    }
    
    
    public boolean setRemoteData(NodeInfo nodeInfo, EOJ eoj, EPC epc, ObjectData data) throws ObjectNotFoundException, SubnetException, EchonetObjectException {
        LOGGER.entering(CLASS_NAME, "setRemoteData", new Object[]{nodeInfo, eoj, epc, data});
        
        boolean result = setRemoteData(getRemoteNode(nodeInfo), eoj, epc, data);
        
        LOGGER.exiting(CLASS_NAME, "setRemoteData", result);
        return result;
    }
    
    public boolean setRemoteData(Node node, EOJ eoj, EPC epc, ObjectData data) throws ObjectNotFoundException, EchonetObjectException {
        LOGGER.entering(CLASS_NAME, "setRemoteData", new Object[]{eoj, epc, data});
        
        RemoteObject remoteObject = getRemoteObject(node, eoj);
        
        if (remoteObject == null) {
            ObjectNotFoundException exception = new ObjectNotFoundException(eoj.toString());
            LOGGER.throwing(CLASS_NAME, "setRemoteData", exception);
            throw exception;
        }
        
        boolean result = remoteObject.setData(epc, data);
        
        LOGGER.exiting(CLASS_NAME, "setRemoteData", result);
        return result;
    }
    
    public List<Node> getRemoteNodes() {
        return new ArrayList<Node>(getRemoteObjectManager().getNodes());
    }
    
    private List<EOJ> echonetObjectsToEOJs(Collection<? extends EchonetObject> objects) {
        ArrayList<EOJ> eojs = new ArrayList<EOJ>(objects.size());
        
        for (EchonetObject object: objects) {
            eojs.add(object.getEOJ());
        }
        
        return eojs;
    }
    
    public int countLocalEOJs() {
        return getLocalObjectManager().size();
    }
    
    public EOJ getLocalEOJ(int index) {
        return getLocalObjectManager().getAtIndex(index).getEOJ();
    }
    
    public List<EOJ> getLocalEOJs() {
        return echonetObjectsToEOJs(getLocalObjectManager().getAllObjects());
    }
    
    public int countRemoteEOJs(Node node) {
        return getRemoteObjectManager().getAtNode(node).size();
    }
    
    public int countRemoteEOJs(NodeInfo nodeInfo) throws SubnetException {
        Node node = getRemoteNode(nodeInfo);
        return getRemoteObjectManager().getAtNode(node).size();
    }
    
    public EOJ getRemoteEOJ(Node node, int index) {
        return getRemoteObjectManager().getAtNode(node).get(index).getEOJ();
    }
    
    public EOJ getRemoteEOJ(NodeInfo nodeInfo, int index) throws SubnetException {
        Node node = getRemoteNode(nodeInfo);
        return getRemoteObjectManager().getAtNode(node).get(index).getEOJ();
    }
    
    public List<EOJ> getRemoteEOJs(Node node) {
        return echonetObjectsToEOJs(getRemoteObjectManager().getAtNode(node));
    }
    
    public List<EOJ> getRemoteEOJs(NodeInfo nodeInfo) throws SubnetException {
        Node node = getRemoteNode(nodeInfo);
        return echonetObjectsToEOJs(getRemoteObjectManager().getAtNode(node));
    }
    
    public Node getLocalNode() {
        return getSubnet().getLocalNode();
    }
    
    public Node getRemoteNode(String name) throws SubnetException {
        return getSubnet().getRemoteNode(name);
    }
    
    public Node getRemoteNode(NodeInfo nodeInfo) throws SubnetException {
        return getSubnet().getRemoteNode(nodeInfo);
    }
    
    public Node getGroupNode() {
        return getSubnet().getGroupNode();
    }
    
    public List<LocalObject> getLocalObjects() {
        return getLocalObjectManager().getDeviceObjects();
    }
    
    public LocalObject getLocalObject(EOJ eoj) {
        return getLocalObjectManager().get(eoj);
    }
    
    public RemoteObject getRemoteObject(NodeInfo nodeInfo, EOJ eoj) throws SubnetException {
        return getRemoteObject(getRemoteNode(nodeInfo), eoj);
    }
    
    public RemoteObject getRemoteObject(Node node, EOJ eoj) {
        return getRemoteObjectManager().get(node, eoj);
    }
    
    public boolean registerRemoteEOJ(NodeInfo nodeInfo, EOJ eoj) throws SubnetException {
        return registerRemoteEOJ(getRemoteNode(nodeInfo), eoj);
    }
    
    public boolean registerRemoteEOJ(Node node, EOJ eoj) {
        RemoteObject object = new RemoteObject(getSubnet(), node, eoj, getTransactionManager());
        return getRemoteObjectManager().add(object);
    }
    
    public NotifyResult doNotify(EOJ eoj, EPC epc, Data data, int timeout) throws SubnetException {
        LOGGER.entering(CLASS_NAME, "doNotify", new Object[]{eoj, epc, data, timeout});
        
        LinkedList<Pair<EPC, Data>> properties = new LinkedList<Pair<EPC, Data>>();
        properties.add(new Pair<EPC, Data>(epc, data));
        NotifyResult notifyResult = doNotify(getGroupNode(), eoj, properties, timeout, false);
        
        LOGGER.exiting(CLASS_NAME, "doNotify", notifyResult);
        return notifyResult;
    }
    
    public NotifyResult doNotify(EOJ eoj, List<Pair<EPC, Data>> properties, int timeout) throws SubnetException {
        LOGGER.entering(CLASS_NAME, "doNotify", new Object[]{eoj, properties, timeout});
        
        NotifyResult notifyResult = doNotify(getGroupNode(), eoj, properties, timeout, false);
        
        LOGGER.exiting(CLASS_NAME, "doNotify", notifyResult);
        return notifyResult;
    }
    
    public NotifyResult doNotify(NodeInfo nodeInfo, EOJ eoj, List<Pair<EPC, Data>> properties, int timeout) throws SubnetException {
        LOGGER.entering(CLASS_NAME, "doNotify", new Object[]{nodeInfo, eoj, properties, timeout});
        
        NotifyResult notifyResult = doNotify(getRemoteNode(nodeInfo), eoj, properties, timeout, false);
        
        LOGGER.exiting(CLASS_NAME, "doNotify", notifyResult);
        return notifyResult;
    }
    
    public NotifyResult doNotify(Node node, EOJ eoj, List<Pair<EPC, Data>> properties, int timeout) throws SubnetException {
        LOGGER.entering(CLASS_NAME, "doNotify", new Object[]{node, eoj, properties, timeout});
        
        NotifyResult notifyResult = doNotify(node, eoj, properties, timeout, false);
        
        LOGGER.exiting(CLASS_NAME, "doNotify", notifyResult);
        return notifyResult;
    }
    
    public NotifyResult doNotify(EOJ eoj, EPC epc, Data data, int timeout, boolean responseRequired) throws SubnetException {
        LOGGER.entering(CLASS_NAME, "doNotify", new Object[]{eoj, epc, data, timeout, responseRequired});
        
        LinkedList<Pair<EPC, Data>> properties = new LinkedList<Pair<EPC, Data>>();
        properties.add(new Pair<EPC, Data>(epc, data));
        NotifyResult notifyResult = doNotify(getGroupNode(), eoj, properties, timeout, responseRequired);
        
        LOGGER.exiting(CLASS_NAME, "doNotify", notifyResult);
        return notifyResult;
    }
    
    public NotifyResult doNotify(EOJ eoj, List<Pair<EPC, Data>> properties, int timeout, boolean responseRequired) throws SubnetException {
        LOGGER.entering(CLASS_NAME, "doNotify", new Object[]{eoj, properties, timeout, responseRequired});
        
        NotifyResult notifyResult = doNotify(getGroupNode(), eoj, properties, timeout, responseRequired);
        
        LOGGER.exiting(CLASS_NAME, "doNotify", notifyResult);
        return notifyResult;
    }
    
    public NotifyResult doNotify(NodeInfo nodeInfo, EOJ eoj, List<Pair<EPC, Data>> properties, int timeout, boolean responseRequired) throws SubnetException {
        LOGGER.entering(CLASS_NAME, "doNotify", new Object[]{nodeInfo, eoj, properties, timeout, responseRequired});
        
        NotifyResult notifyResult = doNotify(getRemoteNode(nodeInfo), eoj, properties, timeout, responseRequired);
        
        LOGGER.exiting(CLASS_NAME, "doNotify", notifyResult);
        return notifyResult;
    }
    
    public NotifyResult doNotify(Node node, EOJ eoj, List<Pair<EPC, Data>> properties, int timeout, boolean responseRequired) throws SubnetException {
        LOGGER.entering(CLASS_NAME, "doNotify", new Object[]{node, eoj, properties, timeout, responseRequired});
        
        NotifyResult notifyResult = new NotifyResult(responseRequired);
        
        AnnounceTransactionConfig transactionConfig = new AnnounceTransactionConfig();
        transactionConfig.setResponseRequired(responseRequired);
        
        for (Pair<EPC, Data> pair : properties) {
            transactionConfig.addAnnounce(pair.first, pair.second);
        }
        
        transactionConfig.setReceiverNode(core.getSubnet().getGroupNode());
        transactionConfig.setSenderNode(core.getSubnet().getLocalNode());
        transactionConfig.setDestinationEOJ(new EOJ("0ef001"));
        transactionConfig.setSourceEOJ(eoj);
        
        Transaction transaction = core.getTransactionManager().createTransaction(transactionConfig);
        transaction.setTimeout(timeout);
        
        transaction.addTransactionListener(new ResultBaseTransactionListener(notifyResult));
        
        transaction.execute();
        
        LOGGER.exiting(CLASS_NAME, "doNotify", notifyResult);
        return notifyResult;
    }
    
    public NotifyResult doNotifyInstanceList() throws SubnetException {
        LOGGER.entering(CLASS_NAME, "doNotifyInstanceList");
        
        NotifyResult notifyResult = doNotifyInstanceList(getGroupNode(), 0, false);
        
        LOGGER.exiting(CLASS_NAME, "doNotifyInstanceList", notifyResult);
        return notifyResult;
    }
    
    public NotifyResult doNotifyInstanceList(int timeout) throws SubnetException {
        LOGGER.entering(CLASS_NAME, "doNotifyInstanceList", timeout);
        
        NotifyResult notifyResult = doNotifyInstanceList(getGroupNode(), timeout, false);
        
        LOGGER.exiting(CLASS_NAME, "doNotifyInstanceList", notifyResult);
        return notifyResult;
    }
    
    public NotifyResult doNotifyInstanceList(int timeout, boolean responseRequired) throws SubnetException {
        LOGGER.entering(CLASS_NAME, "doNotifyInstanceList", new Object[]{timeout, responseRequired});
        
        NotifyResult notifyResult = doNotifyInstanceList(getGroupNode(), timeout, responseRequired);
        
        LOGGER.exiting(CLASS_NAME, "doNotifyInstanceList", notifyResult);
        return notifyResult;
    }
    
    public NotifyResult doNotifyInstanceList(NodeInfo nodeInfo, int timeout, boolean responseRequired) throws SubnetException {
        LOGGER.entering(CLASS_NAME, "doNotifyInstanceList", new Object[]{nodeInfo, timeout, responseRequired});
        
        NotifyResult notifyResult = doNotifyInstanceList(getRemoteNode(nodeInfo), timeout, responseRequired);
        
        LOGGER.exiting(CLASS_NAME, "doNotifyInstanceList", notifyResult);
        return notifyResult;
    }
    
    public NotifyResult doNotifyInstanceList(Node node, int timeout, boolean responseRequired) throws SubnetException {
        LOGGER.entering(CLASS_NAME, "doNotifyInstanceList", new Object[]{node, timeout, responseRequired});
        
        EOJ eoj = getCore().getNodeProfileObject().getEOJ();
        EPC epc = EPC.xD5;
        ObjectData data = getCore().getNodeProfileObject().forceGetData(epc);
        
        LinkedList<Pair<EPC, Data>> properties = new LinkedList<Pair<EPC, Data>>();
        properties.add(new Pair<EPC, Data>(epc, data.getData()));
        for (int i=0; i<data.getExtraSize(); i++) {
            properties.add(new Pair<EPC, Data>(epc, data.getExtraDataAt(i)));
        }
        
        NotifyResult notifyResult = doNotify(node, eoj, properties, timeout, responseRequired);
        
        LOGGER.exiting(CLASS_NAME, "doNotifyInstanceList", notifyResult);
        return notifyResult;
    }
}

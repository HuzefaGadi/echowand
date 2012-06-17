package echowand.object;

import echowand.common.ClassEOJ;
import echowand.common.EOJ;
import echowand.logic.TooManyObjectsException;
import echowand.util.Collector;
import echowand.util.Selector;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.logging.Logger;

/**
 * ローカルに存在するECHONETオブジェクトを管理
 * @author Yoshiki Makino
 */
public class LocalObjectManager {
    public static final Logger logger = Logger.getLogger(LocalObjectManager.class.getName());
    private static final String className = LocalObjectManager.class.getName();
    
    private HashMap<EOJ, LocalObject> objectsMap;
    private LinkedList<LocalObject>   objects;
    private UnusedEOJGenerator eojGenerator;
    
    /**
     * LocalObjectManagerを生成する。
     */
    public LocalObjectManager() {
        logger.entering(className, "LocalObjectManager");
        
        objectsMap = new HashMap<EOJ, LocalObject>();
        objects = new LinkedList<LocalObject>();
        eojGenerator = new UnusedEOJGenerator();
        
        logger.exiting(className, "LocalObjectManager");
    }
    
    /**
     * このLocalObjectManagerが管理しているオブジェクトの数を返す。
     * @return 管理しているオブジェクトの数
     */
    public int size() {
        return objects.size();
    }
    
    private synchronized void addObject(LocalObject object) {
        logger.entering(className, "addObject", object);
        
        objectsMap.put(object.getEOJ(), object);
        objects.add(object);
        
        logger.exiting(className, "addObject");
    }
    
    /**
     * ローカルオブジェクトを登録する。
     * ローカルオブジェクトのEOJは重複がないようにインスタンスコードが更新される。
     * @param object 登録するローカルのオブジェクト
     * @exception TooManyObjectsException 新しいEOJを割り当てられない場合
     */
    public void add(LocalObject object) throws TooManyObjectsException {
        logger.entering(className, "add", object);

        ClassEOJ classEOJ = object.getEOJ().getClassEOJ();
        EOJ newEOJ = eojGenerator.generate(classEOJ);
        object.setInstanceCode(newEOJ.getInstanceCode());
        addObject(object);

        logger.exiting(className, "add", object);
    }
    
    /**
     * 指定されたEOJのローカルオブジェクトを返す。
     * 存在しない場合にはnullを返す。
     * @param eoj EOJの指定
     * @return 指定されたEOJのローカルオブジェクト
     */
    public LocalObject get(EOJ eoj) {
        logger.entering(className, "get", eoj);
        
        LocalObject object = objectsMap.get(eoj);
        
        logger.exiting(className, "get", object);
        return object;
    }
    
    /**
     * Selectorが真を返すローカルオブジェクトを選択し、そのリストを返す。
     * @param selector ローカルオブジェクトの選択
     * @return 選択したローカルオブジェクトのリスト
     */
    public LinkedList<LocalObject> get(Selector<LocalObject> selector) {
        logger.entering(className, "get", selector);
        
        Collector<LocalObject> collector = new Collector<LocalObject>(selector);
        LinkedList<LocalObject> objectList = collector.collect(new ArrayList<LocalObject>(objects));
        
        logger.exiting(className, "get", objectList);
        return objectList;
    }
    
    /**
     * このLocalObjectで管理されているindex番目のローカルのオブジェクトを返す。
     * @param index ローカルオブジェクトのインデックス
     * @return index番目のローカルオブジェクト
     */
    public LocalObject getAtIndex(int index) {
        logger.entering(className, "getAtIndex", index);
        
        LocalObject object = objects.get(index);
        
        logger.exiting(className, "getAtIndex", object);
        return object;
    }
    
    /**
     * 指定されたClassEOJに属するローカルオブジェクトのリストを返す。
     * @param ceoj ClassEOJの指定
     * @return 指定されたClassEOJに属するローカルオブジェクトリスト
     */
    public LinkedList<LocalObject> getWithClassEOJ(final ClassEOJ ceoj) {
        logger.entering(className, "getWithClassEOJ", ceoj);
        
        LinkedList<LocalObject> objectList = get(new Selector<LocalObject>() {
            @Override
            public boolean select(LocalObject object) {
                return object.getEOJ().isMemberOf(ceoj);
            }
        });
        
        logger.exiting(className, "getWithClassEOJ", objectList);
        return objectList;
    }
    
    
    /**
     * 機器オブジェクトに属するローカルオブジェクトのリストを返す。
     * @return 機器オブジェクトのリスト
     */
    public LinkedList<LocalObject> getDeviceObjects() {
        logger.entering(className, "getDeviceObjects");
        
        LinkedList<LocalObject> objectList = get(new Selector<LocalObject>() {
            @Override
            public boolean select(LocalObject object) {
                return object.getEOJ().isDeviceObject();
            }
        });
        
        logger.exiting(className, "getDeviceObjects", objectList);
        return objectList;
    }
}

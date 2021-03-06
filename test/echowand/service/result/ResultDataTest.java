package echowand.service.result;

import echowand.common.Data;
import echowand.common.EOJ;
import echowand.common.EPC;
import echowand.common.ESV;
import echowand.net.InternalSubnet;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author ymakino
 */
public class ResultDataTest {
    public InternalSubnet subnet;
    
    @Before
    public void setUp() {
        subnet = new InternalSubnet("ResultDataTest");
    }

    /**
     * Test of toString method, of class ResultData.
     */
    @Test
    public void testToString() {
        ResultData resultData1 = new ResultData(subnet.getLocalNode(), ESV.Get, new EOJ("0ef001"), EPC.x80, new Data((byte)0x30), 10);
        assertNotNull(resultData1.toString());
        
        ResultData resultData2 = new ResultData(subnet.getLocalNode(), ESV.Get, new EOJ("0ef001"), EPC.x80, new Data((byte)0x30));
        assertNotNull(resultData2.toString());
        
        ResultData resultData3 = new ResultData(subnet.getLocalNode(), ESV.Get, new EOJ("0ef001"), EPC.x80, null, 10);
        assertNotNull(resultData3.toString());
        
        ResultData resultData4 = new ResultData(subnet.getLocalNode(), ESV.Get, new EOJ("0ef001"), EPC.x80, null);
        assertNotNull(resultData4.toString());
    }

    /**
     * Test of equals method, of class ResultData.
     */
    @Test
    public void testEquals() {
        ResultData resultData = new ResultData(subnet.getLocalNode(), ESV.Get, new EOJ("0ef001"), EPC.x80, new Data((byte)0x30), 10);
        ResultData resultData1 = new ResultData(subnet.getLocalNode(), ESV.Get, new EOJ("0ef001"), EPC.x80, new Data((byte)0x30), 10);
        ResultData resultData2 = new ResultData(subnet.getLocalNode(), ESV.Get, new EOJ("0ef001"), EPC.x80, null, 10);
        
        assertTrue(resultData.equals(resultData));
        assertTrue(resultData1.equals(resultData1));
        assertTrue(resultData2.equals(resultData2));
        
        assertFalse(resultData.equals(resultData1));
        assertFalse(resultData1.equals(resultData));
        
        assertFalse(resultData.equals(resultData2));
        assertFalse(resultData2.equals(resultData));
    }

    /**
     * Test of hashCode method, of class ResultData.
     */
    @Test
    public void testHashCode() {
        ResultData resultData = new ResultData(subnet.getLocalNode(), ESV.Get, new EOJ("0ef001"), EPC.x80, new Data((byte)0x30), 10);
        ResultData resultData1 = new ResultData(subnet.getLocalNode(), ESV.Get, new EOJ("0ef001"), EPC.x80, new Data((byte)0x30), 10);
        ResultData resultData2 = new ResultData(subnet.getLocalNode(), ESV.Get, new EOJ("0ef001"), EPC.x80, null, 10);
        
        assertTrue(resultData.hashCode() == resultData.hashCode());
        assertTrue(resultData1.hashCode() == resultData1.hashCode());
        assertTrue(resultData2.hashCode() == resultData2.hashCode());
        
        assertFalse(resultData.hashCode() == resultData1.hashCode());
        assertFalse(resultData.hashCode() == resultData2.hashCode());
        assertFalse(resultData1.hashCode() == resultData2.hashCode());
    }
    
}

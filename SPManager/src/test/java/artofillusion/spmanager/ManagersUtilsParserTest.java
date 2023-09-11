package artofillusion.spmanager;

import org.junit.Assert;
import org.junit.Test;

public class ManagersUtilsParserTest {
    @Test
    public void testZero() {
        String val = "0";
        testAndCompare(val);
    }
    @Test
    public void testManyZero0() {
        String val = "0000";
        testAndCompare(val);
    }

    @Test
    public void testManyZero1() {
        String val = "0000.000";
        testAndCompare(val);
    }

    @Test
    public void testOne() {
        String val = "1";
        testAndCompare(val);
    }

    @Test
    public void testMinusOne() {
        String val = "-1";
        testAndCompare(val);
    }

    @Test
    public void testPi() {
        String val = "3.14159";
        testAndCompare(val);
    }

    @Test
    public void testExponential1() {
        String val = "1234.56E2";
        testAndCompare(val);
    }

    @Test
    public void testExponential2() {
        String val = "1234.56e2";
        testAndCompare(val);
    }


    @Test
    public void testExponential3() {
        String val = "-1234.56E2";
        testAndCompare(val);
    }

    @Test
    public void testExponential4() {
        String val = "-1234.56e2";
        testAndCompare(val);
    }

    @Test
    public void testExponential5() {
        String val = "005.0000E-02";
        testAndCompare(val);
    }

    @Test
    public void testExponential6() {
        String val = "-005.0000e-02";
        testAndCompare(val);
    }



    private void testAndCompare(String val) {
        var produced = SPManagerUtils.parseDouble(val);
        var expected = Double.parseDouble(val);

        Assert.assertEquals(expected, produced, 0.000000001);

    }


    @Test(expected = NumberFormatException.class)
    public void testEmptyNative() {
        String val = "";
        var d2 = Double.parseDouble(val);
    }

    @Test(expected = NumberFormatException.class)
    public void testEmptyCustom() {
        String val = "";
        var d1 = SPManagerUtils.parseDouble(val);
    }


    @Test
    public void testParseVersion() {

        Assert.assertEquals(1002, SPManagerUtils.parseVersion("1.2"));
        Assert.assertEquals(1020, SPManagerUtils.parseVersion("1.20"));
        Assert.assertEquals(1020003, SPManagerUtils.parseVersion("1.20.3"));


    }
}

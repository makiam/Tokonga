package artofillusion.spmanager;

import org.junit.Test;

public class ManagersUtilsParserTest {
    @Test
    public void testZero() {
        String val = "0";
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

    private void testAndCompare(String val) {
        var d1 = SPManagerUtils.parseDouble(val);
        var d2 = Double.parseDouble(val);

        System.out.println(d1);
        System.out.println(d2);
    }
}

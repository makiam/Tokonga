import artofillusion.preferences.DataMap;
import org.junit.Assert;
import org.junit.Test;

public class DataMapParseLongTest {

    @Test
    public void testNullPassed() {
        String val = null;
        Assert.assertEquals(0L, DataMap.parseLong(val));
    }

    @Test
    public void testEmptyPassed() {
        String val = null;
        Assert.assertEquals(0L, DataMap.parseLong(val));
    }

    @Test
    public void testSpacesOnly() {
        String val = "          ";
        Assert.assertEquals(0L, DataMap.parseLong(val));
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
    public void testBig() {
        String val = "1234567890";
        testAndCompare(val);
    }

    @Test
    public void testMinusBig() {
        String val = "-1234567890";
        testAndCompare(val);
    }

    @Test
    public void testMaxLong() {

        String val = "9223372036854775807";
        testAndCompare(val);
    }

    @Test
    public void testMaxLongPlus() {

        String val = "9223372036854775808";
        testAndCompare(val);
    }

    @Test
    public void testMinLong() {

        String val = "-9223372036854775807";
        testAndCompare(val);
    }

    @Test
    public void testMinLongMinus() {

        String val = "-9223372036854775809";
        testAndCompare(val);
    }

    private void testAndCompare(String val) {
        long produced = DataMap.parseLong(val);
        long expected = Long.parseLong(val);

        Assert.assertEquals(expected, produced);

    }
}

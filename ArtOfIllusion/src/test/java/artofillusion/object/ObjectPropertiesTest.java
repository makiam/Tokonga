package artofillusion.object;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mockito;

public class ObjectPropertiesTest {

    @Test
    public void testObject3DPropertiesTest() {
        var ph = Mockito.mock(Object3D.class, Answers.CALLS_REAL_METHODS);
        var props = ph.getProperties();
        Assert.assertNotNull(props);
        Assert.assertEquals(0, props.length);
    }
    @Test
    public void testGetCubePropertiesTest() {
        var ph = Mockito.mock(Cube.class, Answers.CALLS_REAL_METHODS);
        var props = ph.getProperties();
        Assert.assertNotNull(props);
        Assert.assertEquals(3, props.length);
    }

    @Test
    public void testGetCurvePropertiesTest() {
        var ph = Mockito.mock(Curve.class, Answers.CALLS_REAL_METHODS);
        var props = ph.getProperties();
        Assert.assertNotNull(props);
        Assert.assertEquals(2, props.length);
    }
}

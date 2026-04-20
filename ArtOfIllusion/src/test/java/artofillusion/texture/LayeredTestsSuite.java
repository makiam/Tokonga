package artofillusion.texture;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SuiteDisplayName("Suite for Layered Texture and Mapping Tests")
@SelectClasses({LayeredTextureTest.class, LayeredMappingTest.class})
public class LayeredTestsSuite {
}

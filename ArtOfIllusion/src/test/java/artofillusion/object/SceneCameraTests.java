package artofillusion.object;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SuiteDisplayName("Scene Camera Suite")
@SelectClasses({SceneCameraTest.class, SceneCameraObjectInfoTest.class})
public class SceneCameraTests {
}

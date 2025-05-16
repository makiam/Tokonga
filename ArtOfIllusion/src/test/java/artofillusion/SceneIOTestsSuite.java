package artofillusion;



import artofillusion.animation.TrackRestoreTest;
import artofillusion.image.ImageRestoreTest;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SuiteDisplayName("Scene IO Suite")
@SelectClasses({ImageRestoreTest.class, TrackRestoreTest.class})
public class SceneIOTestsSuite {
}

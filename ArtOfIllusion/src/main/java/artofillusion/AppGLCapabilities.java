package artofillusion;

import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class AppGLCapabilities {


  private static final Logger logger = LoggerFactory.getLogger(AppGLCapabilities.class);

  // Simple lazy initialization without synchronization
  private static GLCapabilities capabilities;

  public static GLCapabilities getCapabilities() {
    if (capabilities == null) {
      GLProfile profile = GLProfile.getMaxProgrammable(true);
      GLCapabilities caps = new GLCapabilities(profile);
      logger.debug("GL Profile: {} Caps: {}", profile, caps);
      capabilities = caps;
    }
    return capabilities;
  }
}

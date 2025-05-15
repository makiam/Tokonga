package artofillusion;

import artofillusion.image.ImageMap;
import artofillusion.image.MIPMappedImage;
import artofillusion.object.ObjectInfo;
import lombok.extern.slf4j.Slf4j;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.greenrobot.eventbus.EventBus;

import java.io.DataInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
final class SceneIOUtil {

    private static EventBus bus = org.greenrobot.eventbus.EventBus.getDefault();
    /*
        Buffered read introduced in Scene version 6
     */
    public static void loadImageMapsBuffered(DataInputStream in, Scene scene) throws IOException {
        var images = in.readInt();
        log.debug("Read images: {}", images);
    }

    public static void loadImageMapsUnbuffered(DataInputStream in, Scene scene) throws IOException {
        var images = in.readInt();
        log.debug("Read images: {}", images);

        for (int i = 0; i < images; i++) {
            String className = in.readUTF();
            try {
                Class<?> cls = ArtOfIllusion.getClass(className);
                if (cls == null) {
                    throw new IOException("Unknown ImageMap class: " + className);
                }
                Constructor<?> con = cls.getConstructor(DataInputStream.class);
                scene.add((ImageMap) con.newInstance(in));
            } catch (IOException | ReflectiveOperationException | SecurityException ex) {
                throw new IOException("Error loading image: " + ex.getMessage());
            }
        }

    }

    /*
    Buffered track read introduced in Scene version 6
    */
    public static void loadTracksBuffered(DataInputStream in, Scene scene, ObjectInfo owner) throws IOException {
        var tracks = in.readInt();
        log.debug("Read tracks: {}", tracks);
    }

    /*

     */
    public static void loadTracksUnbuffered(DataInputStream in, Scene scene, ObjectInfo owner) throws IOException {
        var tracks = in.readInt();
        log.debug("Read tracks: {}", tracks);
    }

}

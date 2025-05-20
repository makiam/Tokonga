package artofillusion;

import artofillusion.animation.Track;
import artofillusion.image.ImageMap;

import artofillusion.object.ObjectInfo;
import lombok.extern.slf4j.Slf4j;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.greenrobot.eventbus.EventBus;

import java.io.DataInputStream;
import java.io.IOException;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class SceneIOUtil {

    private static EventBus bus = org.greenrobot.eventbus.EventBus.getDefault();


    public static void loadImageMaps(DataInputStream in, Scene scene) throws IOException {
        var counter = in.readInt();
        log.debug("Read images: {}", counter);

        for (int i = 0; i < counter; i++) {
            String className = in.readUTF();
            try {
                Class<?> cls = ArtOfIllusion.getClass(className);
                if (cls == null) {
                    throw new IOException("Unknown ImageMap class: " + className);
                }
                scene.add((ImageMap) cls.getConstructor(DataInputStream.class).newInstance(in));
            } catch (IOException | ReflectiveOperationException rex) {
                log.atError().setCause(rex).log("Images reading error: {}", rex.getMessage());
                throw new IOException("Error loading image: " + rex.getMessage());
            } catch(SecurityException se) {
                log.atError().setCause(se).log("Images reading error: {}", se.getMessage());
                throw new IOException("Error loading image: " + se.getMessage());
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

        for (int i = 0; i < tracks; i++) {
            String className = in.readUTF();
            try {
                Class<?> cls = ArtOfIllusion.getClass(className);
                if (cls == null) {
                    throw new IOException("Unknown Track class: " + className);
                }
                var tr = (Track<?>) cls.getConstructor(ObjectInfo.class).newInstance(owner);
                tr.initFromStream(in, scene);
                owner.addTrack(tr);
            } catch (IOException | ReflectiveOperationException rex) {
                log.atError().setCause(rex).log("Tracks reading error: {}", rex.getMessage());
                throw new IOException("Error loading Track: " + rex.getMessage());
            } catch(SecurityException se) {
                log.atError().setCause(se).log("Tracks reading error: {}", se.getMessage());
                throw new IOException("Error loading Track: " + se.getMessage());
            }
        }
    }

}

package artofillusion;

import artofillusion.image.ImageMap;

import java.util.List;

public interface ImagesContainer {
    /*
    Get all images from scene as unmodifiable List
     */
    List<ImageMap> getImages();
}

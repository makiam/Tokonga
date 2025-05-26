import artofillusion.LayoutWindow
import artofillusion.object.ObjectInfo;

LayoutWindow window;

window.getScene().getObjects().forEach {ObjectInfo info ->
    info.getTracks().reverseEach { it -> info.removeTrack(it) }
}

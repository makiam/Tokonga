package nik777.aoi;

import artofillusion.*;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class MetaData implements Plugin, ModellingTool {

    protected Map<String, Object> global = new HashMap<>();

    public String getName() {
        return "Meta Data";
    }


    @Override
    public void commandSelected(LayoutWindow window) {

    }


    @Override
    public void onSceneWindowCreated(LayoutWindow view) {

    }

    @Override
    public void onSceneWindowClosing(LayoutWindow layoutWindow) {
        MetaObject.release(layoutWindow.getScene());
    }

    @PluginRegistry.Exported(id = "nik777.MetaData.put")
    public void put(Scene scene, String key, Object value) {
        if (scene == null) {
            this.global.put(key, value);
        } else {
            System.out.println("MetaData.put: " + key + " = " + value);
            MetaObject mobj = MetaObject.of(scene);
            if (mobj == null) {
                mobj = new MetaObject(scene);
            }

            mobj.put(key, value);
        }
    }
}

package artofillusion.procedural;

import artofillusion.ui.Translate;

import java.util.function.Consumer;
import java.util.function.Supplier;

public final class ModuleSuppler<M extends ProceduralModule<?>> implements Supplier<M> {

    private final String uncategorized = Translate.text("Modules:menu.plugins");
    private final ProceduralModule.Category category;
    private final Class<M> clazz;
    private Consumer<M> init;


    public ModuleSuppler(Class<M> clazz) {
        this.clazz = clazz;
        this.init = it -> {};
        category = clazz.getAnnotation(ProceduralModule.Category.class);
    }

    public ModuleSuppler(Class<M> clazz, Consumer<M> init) {
        this(clazz);
        this.init = init;
    }

    @Override
    public M get() {
        M mod;
        try {
            mod = clazz.getConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
        init.accept(mod);
        return mod;
    }

    public String getCategory() {
        return "";
    }
}

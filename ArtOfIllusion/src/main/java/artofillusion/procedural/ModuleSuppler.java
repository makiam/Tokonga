package artofillusion.procedural;

import artofillusion.ui.Translate;

import java.util.function.Consumer;
import java.util.function.Supplier;

public final class ModuleSuppler<M extends ProceduralModule<?>> implements Supplier<M> {

    private final String uncategorized = Translate.text("Modules:menu.plugins");
    private final ProceduralModule.Category category;
    private final M instance;
    private Consumer<M> init;


    public ModuleSuppler(M instance) {
        this.instance = instance;
        this.init = it -> {};
        category = instance.getClass().getAnnotation(ProceduralModule.Category.class);
    }

    public ModuleSuppler(M instance, Consumer<M> init) {
        this(instance);
        this.init = init;
    }

    @Override
    public M get() {
        M mod = (M)instance.duplicate();
        init.accept(mod);
        return mod;
    }

    public String getCategory() {
        return "";
    }
}

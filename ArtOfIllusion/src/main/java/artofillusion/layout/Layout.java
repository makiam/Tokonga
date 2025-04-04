package artofillusion.layout;

import java.util.ArrayList;

import java.util.List;

public class Layout {

    public ViewCollection getViews() {
        return new ViewCollection(views);
    }

    private List<View> views = new ArrayList<>();

    private String name;

    public String getName() {
        return name;
    }


    public View createView() {
        return new View();
    }

    public View findView(String name) {
        return new View();
    }
}

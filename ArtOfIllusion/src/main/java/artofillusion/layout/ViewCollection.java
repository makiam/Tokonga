package artofillusion.layout;


import java.util.List;

class ViewCollection  implements IndexedSupplier<View> {

    private List<View> views;

    ViewCollection(List<View> views) {
        this.views = views;
    }

    @Override
    public View get(int index) {
        return views.get(index);
    }

    @Override
    public View get(String name) {
        return null;
    }
}

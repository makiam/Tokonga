package artofillusion.layout;

public interface IndexedSupplier<T> {
    T get(int index);
    T get(String name);
}

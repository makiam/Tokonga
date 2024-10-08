
/* Copyright (C) 2007-2009 by Peter Eastman
   Some parts copyright (C) 2006 by Nik Trevallyn-Jones
   Changes copyright (C) 2018-2024 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */
package artofillusion;

import artofillusion.plugin.*;
import artofillusion.ui.*;
import artofillusion.util.*;
import java.io.*;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.*;


import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class PluginRegistry {
    private static final XStream xstream = new XStream(new StaxDriver());
    static {
        xstream.ignoreUnknownElements();
        xstream.allowTypes(new Class[]{Extension.class, Category.class, PluginDef.class, ImportDef.class, Export.class, History.class, LogRecord.class, Resource.class});
        xstream.processAnnotations(new Class[]{Extension.class, Category.class, PluginDef.class, ImportDef.class, Export.class, History.class, LogRecord.class, Resource.class});
    }
    private static final ArrayList<ClassLoader> pluginLoaders = new ArrayList<>();
    private static final Set<Class<?>> categories = new HashSet<>();
    private static final Map<Class<?>, List<Object>> categoryClasses = new HashMap<>();
    private static final Map<String, Map<String, PluginResource>> resources = new HashMap<>();
    private static final Map<String, ExportInfo> exports = new HashMap<>();
    private static final Map<String, Object> classMap = new HashMap<>();

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Map<String, Throwable> errors = PluginRegistry.notifyPlugins(Plugin.APPLICATION_STOPPING);
            errors.forEach((plugin, ex) -> {
                //NB! At least JUL logger will not outputs log from inside shutdown thread. So I leave System.out.println here
                String out = "Plugin: " + plugin + " throw: " + ex.getMessage() + " with" + Arrays.toString(ex.getStackTrace()) + " at shutdown";
                System.out.println(out);

            });
        }, "Plugin shutdown thread"));
    }

    @SuppressWarnings("ThrowableResultIgnored")
    public static Map<String, Throwable> notifyPlugins(int message, Object... args) {
        Map<String, Throwable> errors = new HashMap<>();
        categoryClasses.getOrDefault(Plugin.class, Collections.emptyList()).forEach(plugin -> {
            try {
                ((Plugin) plugin).processMessage(message, args);
            } catch (Throwable tx) {
                log.atInfo().setCause(tx).log("Plugin: {} error due: {}", plugin.getClass().getSimpleName(), tx.getMessage());
                errors.put(plugin.getClass().getSimpleName(), tx);
            }
        });
        return errors;
    }

    /**
     * Scan all files in the Plugins directory, read in their indices, and record all plugins
     * contained in them.
     */
    public static List<String> scanPlugins() {
        Path pluginsPath = Paths.get(ArtOfIllusion.PLUGIN_DIRECTORY);
        if (Files.notExists(pluginsPath)) {
            return Arrays.asList(Translate.text("cannotLocatePlugins"));
        }

        // Scan the plugins directory, and parse the index in every jar file.

        Set<JarInfo> jars = new HashSet<>();
        List<String> results = new ArrayList<>();



        for (File file : pluginsPath.toFile().listFiles(f -> f.isFile() && f.getName().endsWith(".jar"))) {
            try {
                jars.add(new JarInfo(file));
            } catch (IOException ex) {
                // Not a zip file.
            } catch (Exception ex) {
                results.add("Error loading plugin file: " + file);
                log.atError().setCause(ex).log("Error loading file: {} due {}", file, ex.getMessage());
            }
        }
        processPlugins(jars, results);
        return results;
    }

    private static void processPlugins(Set<JarInfo> jars, List<String> results) {
        // Build a classloader for each jar, registering plugins, categories, and resources.
        // This needs to be done in the proper order to account for dependencies between plugins.

        Map<String, JarInfo> nameMap = new HashMap<>();
        while (jars.size() > 0) {
            boolean processedAny = false;
            for (JarInfo jar : new ArrayList<>(jars)) {
                // See if we've already processed all other jars it depends on.

                boolean importsOk = true;
                for (String importName : jar.imports) {
                    importsOk &= nameMap.containsKey(importName);
                    if (!importsOk) {
                        break;
                    }
                }
                if (importsOk) {
                    processJar(jar, nameMap, results);
                    processedAny = true;
                    jars.remove(jar);
                }
            }
            if (!processedAny) {

                for (JarInfo info : jars) {
                    Object source = "(plugin loaded from ClassLoader)";
                    if (info.file != null) {
                        source = info.file.getName();
                    }
                    results.add(Translate.text("cannotLoadPlugin", source));
                }

                break;
            }
        }
    }

    /**
     * Process a single jar file in the Plugins directory.
     *
     * @param jar the jar file being processed
     * @param nameMap maps plugin names to JarInfo objects
     */
    private static void processJar(JarInfo jar, Map<String, JarInfo> nameMap, List<String> results) {
        try {
            if (jar.imports.isEmpty() && jar.searchPath.isEmpty()) {
                if (jar.loader == null) {
                    jar.loader = new URLClassLoader(new URL[]{jar.file.toURI().toURL()});
                }
            } else {
                SearchlistClassLoader loader;
                if (jar.loader == null) {
                    loader = new SearchlistClassLoader(new URL[]{jar.file.toURI().toURL()});
                } else {
                    loader = new SearchlistClassLoader(jar.loader);
                }
                jar.loader = loader;
                for (String importName : jar.imports) {
                    loader.add(nameMap.get(importName).loader);
                }

                // NTJ - add URL of searchpath to class loader
                for (String uri : jar.searchPath) {

                    URL url = new URL(uri);
                    // resolve any registry-based authority
                    if (url.getAuthority() != null && url.getAuthority().startsWith("$")) {
                        uri = (String) ArtOfIllusion.class.getField(url.getAuthority().substring(1)).get(null);
                        url = new File(uri, url.getPath()).toURI().toURL();
                    }

                    loader.add(url);
                }
            }
            pluginLoaders.add(jar.loader);
            Map<String, Object> classNameMap = new HashMap<>();
            if (jar.getName() != null && !jar.getName().isEmpty()) {
                nameMap.put(jar.getName(), jar);
            }
            for (String category : jar.categories) {
                addCategory(jar.loader.loadClass(category));
            }
            for (String pluginName : jar.plugins) {
                Object plugin = jar.loader.loadClass(pluginName).getDeclaredConstructor().newInstance();
                registerPlugin(plugin);
                classNameMap.put(pluginName, plugin);
            }
            for (ExportInfo info : jar.exports) {
                info.plugin = classNameMap.get(info.className);
                registerExportedMethod(info);
            }
            jar.getResources().forEach(info -> registerResource(info.getType(), info.getId(), jar.loader, info.getName(), info.getLocale()));

        } catch (Error | Exception ex) {
            results.add(Translate.text("pluginLoadError", jar.file.getName()));
            log.atError().setCause(ex).log("Plugin from {} initialization error: {}", jar.getFile().getName(), ex.getMessage());
        }
    }

    /**
     * Get the ClassLoaders for all jar files in the Plugins directory. There is one ClassLoader
     * for every jar.
     */
    public static List<ClassLoader> getPluginClassLoaders() {
        return new ArrayList<>(pluginLoaders);
    }

    /**
     * Define a new category of plugins. A category is specified by a class or interface. After
     * adding a category, any call to {@link #registerPlugin(Object)} will check the registered object
     * to see if it is an instance of the specified class. If so, it is added to the list of plugins
     * in that category.
     */
    public static void addCategory(Class<?> category) {
        categories.add(category);
    }

    /**
     * Get all categories of plugins that have been defined.
     */
    public static List<Class<?>> getCategories() {
        return new ArrayList<>(categories);
    }

    /**
     * Register a new plugin. The specified object is checked against every defined category of plugins
     * by seeing if it is an instance of the class or interface defining each category. If so, it is
     * added to the list of plugins in that category.
     */
    public static void registerPlugin(Object plugin) {
        classMap.put(plugin.getClass().getName(), plugin);
        for (Class<?> category : categories) {
            if (category.isInstance(plugin)) {
                List<Object> instances = categoryClasses.computeIfAbsent(category, k -> new ArrayList<>());
                instances.add(plugin);
            }
        }
    }

    /**
     * Get all registered plugins in a particular category.
     */
    public static <T> List<T> getPlugins(Class<T> category) {
        List<Object> plugins = categoryClasses.get(category);
        if (plugins == null) {
            return new ArrayList<T>();
        }
        ArrayList<T> list = new ArrayList<T>(plugins.size());
        for (Object plugin : plugins) {
            list.add((T) plugin);
        }
        return list;
    }

    /**
     * Get the registered plugin object of a particular class. Unlike {@link #getPlugins(Class)},
     * the specified class name must be the exact class of the object, not a superclass or interface.
     * If multiple plugins of the same class have been registered, this returns the most recently
     * registered one.
     *
     * @param className the fully qualified name of the class of the plugin object to return
     * @return the plugin object of the specified class, or null if no matching plugin has been registered
     */
    public static Object getPluginObject(String className) {
        return classMap.get(className);
    }

    /**
     * Register a new resource. You can then call {@link #getResource(String, String)} to look up
     * a particular resource, or {@link #getResources(String)} to find all registered resources of
     * a particular type.
     *
     * @param type the type of resource being registered
     * @param id the id of this resource
     * @param loader the ClassLoader with which to load the resource
     * @param name the fully qualified name of the resource, that should be passed to
     * <code>loader.getResource()</code> to load it
     * @param locale the locale this resource represents (maybe null)
     * @throws IllegalArgumentException if there is already a registered resource with the same type, id, and locale
     */
    public static void registerResource(String type, String id, ClassLoader loader, String name, Locale locale) throws IllegalArgumentException {
        Map<String, PluginResource> resourcesForType = resources.computeIfAbsent(type, k -> new HashMap<>());
        PluginResource resource = resourcesForType.get(id);
        if (resource == null) {
            resource = new PluginResource(type, id);
            resourcesForType.put(id, resource);
        }
        resource.addResource(name, loader, locale);
    }

    public static void registerResource(String type, String id, ClassLoader loader, String name) throws IllegalArgumentException {
        registerResource(type, id, loader, name, null);
    }

    /**
     * Get a list of all type identifiers for which there are PluginResources available.
     */
    public static List<String> getResourceTypes() {
        return new ArrayList<>(resources.keySet());
    }

    /**
     * Get a list of all registered PluginResources of a particular type.
     */
    public static List<PluginResource> getResources(String type) {
        Map<String, PluginResource> resourcesForType = resources.get(type);
        if (resourcesForType == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(resourcesForType.values());
    }

    /**
     * Get the PluginResource with a particular type and id, or null if there is no such resource.
     */
    public static PluginResource getResource(String type, String id) {
        Map<String, PluginResource> resourcesForType = resources.get(type);
        if (resourcesForType == null) {
            return null;
        }
        return resourcesForType.get(id);
    }

    /**
     * Register a method which may be invoked on a plugin object. This allows external code to easily
     * use features of a plugin without needing to directly import that plugin or use reflection.
     * Use {@link #getExportedMethodIds()} to get a list of all exported methods that have been
     * registered, and {@link #invokeExportedMethod(String, Object[])} to invoke one.
     *
     * @param plugin the plugin object on which the method should be invoked
     * @param method the name of the method to invoke
     * @param id a unique identifier which may be passed to <code>invokeExportedMethod()</code>
     * to identify this method
     */
    public static void registerExportedMethod(Object plugin, String method, String id) throws IllegalArgumentException {
        ExportInfo info = new ExportInfo();
        info.plugin = plugin;
        info.method = method;
        info.id = id;
        registerExportedMethod(info);
    }

    private static void registerExportedMethod(ExportInfo export) throws IllegalArgumentException {
        if (exports.containsKey(export.id)) {
            throw new IllegalArgumentException("Multiple exported methods with id=" + export.id);
        }
        exports.put(export.id, export);
    }

    /**
     * Get a list of the identifiers of all exported methods which have been registered.
     */
    public static List<String> getExportedMethodIds() {
        return new ArrayList<>(exports.keySet());
    }

    /**
     * Invoke an exported method of a plugin object.
     *
     * @param id the unique identifier of the method to invoke
     * @param args the list of arguments to pass to the method. If the method has no arguments,
     * this may be null.
     * @return the value returned by the method after it was invoked
     * @throws NoSuchMethodException if there is no exported method with the specified ID, or if there
     * is no form of the exported method whose arguments are compatible with the specified args array.
     * @throws InvocationTargetException if the method threw an exception when it was invoked.
     */
    public static Object invokeExportedMethod(String id, Object... args) throws NoSuchMethodException, InvocationTargetException {
        ExportInfo info = exports.get(id);
        if (info == null) {
            throw new NoSuchMethodException("There is no exported method with id=" + id);
        }

        // Try to find a method to invoke.
        for (Method method : info.plugin.getClass().getMethods()) {
            if (!method.getName().equals(info.method)) {
                continue;
            }
            try {
                return method.invoke(info.plugin, args);
            } catch (IllegalArgumentException ex) {
                // Possibly the wrong version of an overloaded method, so keep trying.
            } catch (IllegalAccessException ex) {
                // This should be impossible, since getMethods() only returns public methods.

                throw new InvocationTargetException(ex);
            }
        }
        throw new NoSuchMethodException("No method found which matches the specified name and argument types.");
    }

    /**
     * This class is used to store information about the content of a jar file during initialization.
     */
    private static class JarInfo {

        @Getter
        File file;
        private Extension ext = new Extension();

        public String getName() {
            return ext.getName();
        }

        String version;
        String authors;
        final List<String> imports = new ArrayList<>();
        final List<String> plugins = new ArrayList<>();
        final List<String> categories = new ArrayList<>();
        final List<String> searchPath = new ArrayList<>();

        public List<Resource> getResources() {
            return ext.getResources();
        }

        final List<Resource> resources = new ArrayList<>();
        final List<ExportInfo> exports = new ArrayList<>();
        ClassLoader loader;

        JarInfo(File file) throws IOException {
            this.file = file;

            try(ZipFile zf = new ZipFile(file)) {
                ZipEntry ze = zf.getEntry("extensions.xml");
                if (ze != null) {
                    InputStream in = new BufferedInputStream(zf.getInputStream(ze));
                    loadExtensionsFile(in);
                    in.close();
                    return;
                }
                ze = zf.getEntry("plugins");
                if (ze != null) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(zf.getInputStream(ze)));
                    plugins.addAll(in.lines().collect(Collectors.toList()));
                    in.close();
                    return;
                }
                throw new IOException(); // No index found
            }
        }

        private void loadExtensionsFile(InputStream in) throws IOException {

            try {
                ext = (Extension)xstream.fromXML(in);
            } catch (Exception ex) {
                log.atError().setCause(ex).log("Error parsing plugin descriptor for {} due {}", file.getName(), ex.getMessage());
                throw new IOException();
            }

            version = ext.getVersion();
            authors = String.join(", ", ext.getAuthors());
            categories.addAll(ext.getCategoryList().stream().map(category -> category.getCategory()).collect(Collectors.toList()));
            resources.addAll(ext.getResources());

            ext.getImports().forEach(def -> {
                if(def.getName() == null) {
                    if(def.getUrl() == null) return;
                    searchPath.add(def.getUrl());
                } else {
                    imports.add(def.getName());
                }
            });

            ext.getPluginsList().forEach((PluginDef def) -> {
                plugins.add(def.getPluginClass());
                def.getExports().forEach(export -> {
                    exports.add(new ExportInfo(export.getId(), export.getMethod(), def.getPluginClass() ) );
                });
            });


        }

    }

    /**
     * A PluginResource represents a resource that was loaded from a plugin. Each PluginResource
     * is identified by a type and an id. Typically, the type indicates the purpose for which a
     * resource is to be used, and the id designates a specific resource of that type.
     * <p>
     * It is also possible for several different localized versions of a resource to be available,
     * possibly provided by different plugins. A single PluginResource object represents all
     * the different localized resources that share the same type and id. When you invoke one of
     * the methods to access the resource's contents, the localized version that most closely matches the
     * currently selected locale is used.
     */
    public static class PluginResource {

        /**
         * -- GETTER --
         *  Get the type of this PluginResource.
         */
        @Getter
        private final String type;
        /**
         * -- GETTER --
         *  Get the id of this PluginResource.
         */
        @Getter
        private final String id;
        private final List<String> names = new ArrayList<>();
        private final List<ClassLoader> loaders = new ArrayList<>();
        private final List<Locale> locales = new ArrayList<>();

        private PluginResource(String type, String id) {
            this.type = type;
            this.id = id;
        }

        private void addResource(String name, ClassLoader loader, Locale locale) throws IllegalArgumentException {
            if (locales.contains(locale)) {
                throw new IllegalArgumentException("Multiple resource definitions for type=" + type + ", name=" + id + ", locale=" + locale);
            }
            names.add(name);
            loaders.add(loader);
            locales.add(locale);
        }

        /**
         * Find which localized version of the resource best matches a locale.
         */
        private int findLocalizedVersion(Locale locale) {
            int bestMatch = 0, bestMatchedLevels = 0;
            for (int i = 0; i < locales.size(); i++) {
                Locale loc = locales.get(i);
                int matchedLevels = 0;
                if (loc != null && loc.getLanguage().equals(locale.getLanguage())) {
                    matchedLevels++;
                    if (loc.getCountry() == locale.getCountry()) {
                        matchedLevels++;
                        if (loc.getVariant() == locale.getVariant()) {
                            matchedLevels++;
                        }
                    }
                }
                if (matchedLevels > bestMatchedLevels) {
                    bestMatch = i;
                    bestMatchedLevels = matchedLevels;
                }
            }
            return bestMatch;
        }

        /**
         * Get an InputStream for reading this resource. If there are multiple localized versions,
         * the version which best matches the currently selected locale is used.
         */
        public InputStream getInputStream() {
            int index = findLocalizedVersion(Translate.getLocale());
            return loaders.get(index).getResourceAsStream(names.get(index));
        }

        /**
         * Get a URL for reading this resource. If there are multiple localized versions,
         * the version which best matches the currently selected locale is used.
         */
        public URL getURL() {
            int index = findLocalizedVersion(Translate.getLocale());
            return loaders.get(index).getResource(names.get(index));
        }

        /**
         * Get the fully qualified name of the resource this represents. If there are multiple localized
         * versions, the version which best matches the currently selected locale is used.
         */
        public String getName() {
            int index = findLocalizedVersion(Translate.getLocale());
            return names.get(index);
        }

        /**
         * Get the ClassLoader responsible for loading this resource. If there are multiple localized
         * versions, the version which best matches the currently selected locale is used.
         */
        public ClassLoader getClassLoader() {
            int index = findLocalizedVersion(Translate.getLocale());
            return loaders.get(index);
        }
    }

    /**
     * This class is used to store information about an "export" record in an XML file.
     */
    private static class ExportInfo {
        String method, id, className;
        Object plugin;
        ExportInfo() {
        }
        ExportInfo(String id, String method, String className) {
            this.id = id;
            this.method = method;
            this.className = className;
        }

    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface UsedViaReflection {
    }
}

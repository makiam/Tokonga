/* Copyright (C) 2007-2009 by Peter Eastman
   Some parts copyright (C) 2006 by Nik Trevallyn-Jones
   Changes copyright (C) 2016-2018 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */
package artofillusion;

import java.io.*;
import java.util.zip.*;
import java.util.*;
import java.net.*;
import java.lang.reflect.*;

import artofillusion.ui.*;
import artofillusion.util.*;
import java.util.regex.Pattern;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;

public class PluginRegistry {

    private static Unmarshaller um = null;

    static {
        try {
            um = javax.xml.bind.JAXBContext.newInstance(PluginRegistry.Extension.class).createUnmarshaller();
        } catch (JAXBException ex) {
            System.out.println("Error creating XML unmarshaller: " + ex);
        }

    }

    private static final List<ClassLoader> pluginLoaders = new ArrayList<ClassLoader>();
    private static final Set<Class> categories = new HashSet<Class>();
    private static final Map<Class, List<Object>> categoryClasses = new HashMap<Class, List<Object>>();
    private static final Map<String, Map<String, PluginResource>> resources = new HashMap<String, Map<String, PluginResource>>();
    private static final Map<String, ExportInfo> exports = new HashMap<String, ExportInfo>();
    private static final Map<String, Object> classMap = new HashMap<String, Object>();

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            PluginRegistry.notifyPlugins(Plugin.class, Plugin.APPLICATION_STOPPING);
        }, "Plugin shutdown thread"));
    }

    public static <T> List<Throwable> notifyPlugins(Class<T> category, int message, Object... args) {
        if (categoryClasses.containsKey(category)) {
            List<Throwable> errors = new ArrayList<>();
            for (Object plugin : categoryClasses.get(category)) {
                try {
                    ((Plugin) plugin).processMessage(message, args);
                } catch (Throwable tx) {
                    errors.add(tx);
                }
            }
            return errors;
        }
        return Collections.EMPTY_LIST;
    }

    /**
     * Scan all files in the Plugins directory, read in their indices, and record all plugins
     * contained in them.
     */
    public static void scanPlugins() {
        File dir = new File(ArtOfIllusion.PLUGIN_DIRECTORY);
        if (!dir.exists()) {
            Messages.error(UIUtilities.breakString(Translate.text("cannotLocatePlugins")));
            return;
        }

        // Scan the plugins directory, and parse the index in every jar file.
        HashSet<JarInfo> jars = new HashSet<>();
        for (String file : dir.list()) {
            try {
                jars.add(new JarInfo(new File(dir, file)));
            } catch (IOException ex) {
                // Not a zip file.
            } catch (Exception ex) {
                System.err.println("*** Exception loading plugin file " + file);
                ex.printStackTrace(System.err);
            }
        }
        processPlugins(jars);
    }

    /**
     * Process a set of ClassLoaders corresponding to jar files, read in their indices, and record
     * all plugins contained in them.
     */
    public static void scanPlugins(List<ClassLoader> loaders) {
        HashSet<JarInfo> jars = new HashSet<>();
        for (ClassLoader loader : loaders) {
            try {
                jars.add(new JarInfo(loader));
            } catch (IOException ex) {
                // Not a zip file.
            } catch (Exception ex) {
                System.err.println("*** Exception loading plugin classloader");
                ex.printStackTrace(System.err);
            }
        }
        processPlugins(jars);
    }

    private static void processPlugins(HashSet<JarInfo> jars) {
        // Build a classloader for each jar, registering plugins, categories, and resources.
        // This needs to be done in the proper order to account for dependencies between plugins.

        HashMap<String, JarInfo> nameMap = new HashMap<>();
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
                    processJar(jar, nameMap);
                    processedAny = true;
                    jars.remove(jar);
                }
            }
            if (!processedAny) {
                System.err.println("*** The following plugins were not loaded because their imports could not be resolved:");
                for (JarInfo info : jars) {
                    if (info.file == null) {
                        System.err.println("(plugin loaded from ClassLoader)");
                    } else {
                        System.err.println(info.file.getName());
                    }
                }
                System.err.println();
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
    private static void processJar(JarInfo jar, Map<String, JarInfo> nameMap) {
        try {
            if (jar.imports.isEmpty() && jar.searchpath.isEmpty()) {
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
                for (String uri : jar.searchpath) {

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
            HashMap<String, Object> classNameMap = new HashMap<>();
            if (jar.name != null && jar.name.length() > 0) {
                nameMap.put(jar.name, jar);
            }
            for (String category : jar.categories) {
                addCategory(jar.loader.loadClass(category));
            }
            for (String pluginName : jar.plugins) {
                Object plugin = jar.loader.loadClass(pluginName).newInstance();
                registerPlugin(plugin);
                classNameMap.put(pluginName, plugin);
            }
            for (ExportInfo info : jar.exports) {
                info.plugin = classNameMap.get(info.className);
                registerExportedMethod(info);
            }
            for (ResourceInfo info : jar.resources) {
                registerResource(info.type, info.id, jar.loader, info.name, info.getLocale());
            }
        } catch (NoClassDefFoundError | Exception ex) {
            Messages.error(UIUtilities.breakString(Translate.text("pluginLoadError", jar.file.getName())));
            System.err.println("*** Exception while initializing plugin " + jar.file.getName() + ":");
            ex.printStackTrace();
        }
    }

    /**
     * Get the ClassLoaders for all jar files in the Plugins directory. There is one ClassLoader for
     * every jar.
     */
    public static List<ClassLoader> getPluginClassLoaders() {
        return new ArrayList<>(pluginLoaders);
    }

    /**
     * Define a new category of plugins. A category is specified by a class or interface. After
     * adding a category, any call to {@link #registerPlugin(Object)} will check the registered
     * object to see if it is an instance of the specified class. If so, it is added to the list of
     * plugins in that category.
     */
    public static void addCategory(Class category) {
        categories.add(category);
    }

    /**
     * Get all categories of plugins that have been defined.
     */
    public static List<Class> getCategories() {
        return new ArrayList<>(categories);
    }

    /**
     * Register a new plugin. The specified object is checked against every defined category of
     * plugins by seeing if it is an instance of the class or interface defining each category. If
     * so, it is added to the list of plugins in that category.
     */
    public static void registerPlugin(Object plugin) {
        classMap.put(plugin.getClass().getName(), plugin);
        for (Class category : categories) {
            if (category.isInstance(plugin)) {
                List<Object> instances = categoryClasses.get(category);
                if (instances == null) {
                    instances = new ArrayList<>();
                    categoryClasses.put(category, instances);
                }
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
            return new ArrayList<>();
        }
        ArrayList<T> list = new ArrayList<>(plugins.size());
        for (Object plugin : plugins) {
            list.add((T) plugin);
        }
        return list;
    }

    /**
     * Get the registered plugin object of a particular class. Unlike {@link #getPlugins(Class)},
     * the specified class name must be the exact class of the object, not a superclass or
     * interface. If multiple plugins of the same class have been registered, this returns the most
     * recently registered one.
     *
     * @param classname the fully qualified name of the class of the plugin object to return
     * @return the plugin object of the specified class, or null if no matching plugin has been
     * registered
     */
    public static Object getPluginObject(String classname) {
        return classMap.get(classname);
    }

    /**
     * Register a new resource. You can then call {@link #getResource(String, String)} to look up a
     * particular resource, or {@link #getResources(String)} to find all registered resources of a
     * particular type.
     *
     * @param type the type of resource being registered
     * @param id the id of this resource
     * @param loader the ClassLoader with which to load the resource
     * @param name the fully qualified name of the resource, that should be passed to
     * <code>loader.getResource()</code> to load it
     * @param locale the locale this resource represents (may be null)
     * @throws IllegalArgumentException if there is already a registered resource with the same
     * type, id, and locale
     */
    public static void registerResource(String type, String id, ClassLoader loader, String name, Locale locale) throws IllegalArgumentException {
        Map<String, PluginResource> resourcesForType = resources.get(type);
        if (resourcesForType == null) {
            resourcesForType = new HashMap<>();
            resources.put(type, resourcesForType);
        }
        PluginResource resource = resourcesForType.get(id);
        if (resource == null) {
            resource = new PluginResource(type, id);
            resourcesForType.put(id, resource);
        }
        resource.addResource(name, loader, locale);
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
     * Register a method which may be invoked on a plugin object. This allows external code to
     * easily use features of a plugin without needing to directly import that plugin or use
     * reflection. Use {@link #getExportedMethodIds()} to get a list of all exported methods that
     * have been registered, and {@link #invokeExportedMethod(String, Object[])} to invoke one.
     *
     * @param plugin the plugin object on which the method should be invoked
     * @param method the name of the method to invoke
     * @param id a unique identifier which may be passed to <code>invokeExportedMethod()</code> to
     * identify this method
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
     * @param args the list of arguments to pass to the method. If the method has no arguments, this
     * may be null.
     * @return the value returned by the method after it was invoked
     * @throws NoSuchMethodException if there is no exported method with the specified ID, or if
     * there is no form of the exported method whose arguments are compatible with the specified
     * args array.
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
     * This class is used to store information about the content of a jar file during
     * initialization.
     */
    private static class JarInfo {

        File file;
        String name, version;
        ArrayList<String> imports, plugins, categories, searchpath;
        ArrayList<ResourceInfo> resources;
        ArrayList<ExportInfo> exports;
        ClassLoader loader;

        JarInfo(File file) throws IOException {
            this.file = file;
            imports = new ArrayList<>();
            plugins = new ArrayList<>();
            categories = new ArrayList<>();
            searchpath = new ArrayList<>();
            resources = new ArrayList<>();
            exports = new ArrayList<>();
            ZipFile zf = new ZipFile(file);
            try {
                ZipEntry ze = zf.getEntry("extensions.xml");
                if (ze != null) {

                    InputStream in = new BufferedInputStream(zf.getInputStream(ze));
                    loadExtensionsFile(in);
                    return;
                }
                ze = zf.getEntry("plugins");
                if (ze != null) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(zf.getInputStream(ze)));
                    loadPluginsFile(in);
                    return;
                }
                throw new IOException(); // No index found
            } finally {
                zf.close();
            }
        }

        JarInfo(ClassLoader loader) throws IOException {
            this.loader = loader;
            imports = new ArrayList<>();
            plugins = new ArrayList<>();
            categories = new ArrayList<>();
            resources = new ArrayList<>();
            exports = new ArrayList<>();
            InputStream in = loader.getResourceAsStream("extensions.xml");
            if (in != null) {
                loadExtensionsFile(new BufferedInputStream(in));
                in.close();
                return;
            }
            in = loader.getResourceAsStream("plugins");
            if (in != null) {
                loadPluginsFile(new BufferedReader(new InputStreamReader(in)));
                in.close();
                return;
            }
            throw new IOException(); // No index found
        }

        private void loadExtensionsFile(InputStream in) throws IOException {

            Extension extension = null;
            try {
                extension = (Extension) um.unmarshal(in);
            } catch (JAXBException ex) {
                System.err.print("*** Exception while parsing extensions.xml for plugin " + file.getName() + ":");
                ex.printStackTrace();
                throw new IOException();
            }

            name = extension.name;

            for (ExportInfo ei : extension.categories) {
                categories.add(ei.className);
            }

            resources.addAll(extension.resources);

            for (ClassImportInfo ci : extension.imports) {
                if (ci.name != null) {
                    imports.add(ci.name);
                } else if (ci.url != null) {
                    searchpath.add(ci.url);
                }
            }

            for (ExportInfo ei : extension.plugins) {
                plugins.add(ei.className);
                if (ei.export.isEmpty()) {
                    continue;
                }
                for (ExportInfo ex : ei.export) {
                    ex.className = ei.className;
                    exports.add(ex);
                }
            }
        }

        private void loadPluginsFile(BufferedReader in) throws IOException {
            String className = in.readLine();
            while (className != null) {
                plugins.add(className.trim());
                className = in.readLine();
            }
        }
    }

    /**
     * A PluginResource represents a resource that was loaded from a plugin. Each PluginResource is
     * identified by a type and an id. Typically the type indicates the purpose for which a resource
     * is to be used, and the id designates a specific resource of that type.
     * <p>
     * It is also possible for several different localized versions of a resource to be available,
     * possibly provided by different plugins. A single PluginResource object represents all the
     * different localized resources that share the same type and id. When you invoke one of the
     * methods to access the resource's contents, the localized version that most closely matches
     * the currently selected locale is used.
     */
    public static class PluginResource {

        private String type, id;
        private ArrayList<String> names;
        private ArrayList<ClassLoader> loaders;
        private ArrayList<Locale> locales;

        private PluginResource(String type, String id) {
            this.type = type;
            this.id = id;
            names = new ArrayList<>();
            loaders = new ArrayList<>();
            locales = new ArrayList<>();
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
         * Get the type of this PluginResource.
         */
        public String getType() {
            return type;
        }

        /**
         * Get the id of this PluginResource.
         */
        public String getId() {
            return id;
        }

        /**
         * Find which localized version of the resource best matches a locale.
         */
        private int findLocalizedVersion(Locale locale) {
            int bestMatch = 0, bestMatchedLevels = 0;
            for (int i = 0; i < locales.size(); i++) {
                Locale loc = locales.get(i);
                int matchedLevels = 0;
                if (loc != null && loc.getLanguage() == locale.getLanguage()) {
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
         * Get a URL for reading this resource. If there are multiple localized versions, the
         * version which best matches the currently selected locale is used.
         */
        public URL getURL() {
            int index = findLocalizedVersion(Translate.getLocale());
            return (loaders.get(index)).getResource(names.get(index));
        }

        /**
         * Get the fully qualified name of the resource this represents. If there are multiple
         * localized versions, the version which best matches the currently selected locale is used.
         */
        public String getName() {
            int index = findLocalizedVersion(Translate.getLocale());
            return names.get(index);
        }

        /**
         * Get the ClassLoader responsible for loading this resource. If there are multiple
         * localized versions, the version which best matches the currently selected locale is used.
         */
        public ClassLoader getClassLoader() {
            int index = findLocalizedVersion(Translate.getLocale());
            return loaders.get(index);
        }
    }

    @XmlRootElement(name = "extension")
    public static class Extension {

        @XmlAttribute
        public String name;
        @XmlAttribute
        public String version;

        @XmlElement(name = "plugin")
        public List<ExportInfo> plugins = new ArrayList<ExportInfo>();

        @XmlElement(name = "category")
        public List<ExportInfo> categories = new ArrayList<ExportInfo>();

        @XmlElement(name = "import")
        public List<ClassImportInfo> imports = new ArrayList<ClassImportInfo>();

        @XmlElement(name = "resource")
        public final List<ResourceInfo> resources = new ArrayList<ResourceInfo>();
    }

    /**
     * This class is used to store information about an "export" record in an XML file.
     */
    public static class ExportInfo {

        @XmlAttribute
        private String method;
        @XmlAttribute
        private String id;
        @XmlAttribute(name = "class")
        private String className;
        @XmlElement(name = "export")
        private List<ExportInfo> export = new ArrayList<ExportInfo>();
        Object plugin;
    }

    /**
     * This class is used to store information about an "import" record in an XML file.
     */
    private static class ClassImportInfo {

        @XmlAttribute
        private String name;
        @XmlAttribute
        private String url;
    }

    /**
     * This class is used to store information about a "resource" record in an XML file.
     */
    private static class ResourceInfo {

        private static final Pattern pattern = Pattern.compile("_");

        @XmlAttribute
        private String id;
        @XmlAttribute
        private String type;
        @XmlAttribute
        private String name;
        @XmlAttribute(name = "locale")
        private String xmlLocale;

        public Locale getLocale() {
            if (xmlLocale == null) {
                return null;
            }
            String[] parts = pattern.split(xmlLocale);
            if (parts.length == 1) {
                return new Locale(parts[0]);
            }
            if (parts.length == 2) {
                return new Locale(parts[0], parts[1]);
            }
            return new Locale(parts[0], parts[1], parts[2]);
        }

        @Override
        public String toString() {
            return "Resource: {" + "id : " + id + ", type : " + type + ", name : " + name + ", locale : " + this.getLocale() + '}';
        }

    }

}

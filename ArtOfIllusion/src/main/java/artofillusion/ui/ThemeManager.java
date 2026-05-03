/* Copyright (C) 2007 by François Guillet
   Some parts copyright 2007 by Peter Eastman
   Changes copyright (C) 2017-2026 by Maksim Khramov

 This program is free software; you can redistribute it and/or modify it under the
 terms of the GNU General Public License as published by the Free Software
 Foundation; either version 2 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful, but WITHOUT ANY
 WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.ui;

import artofillusion.ArtOfIllusion;
import artofillusion.PluginRegistry;
import artofillusion.ViewerCanvas;
import artofillusion.math.RGBColor;
import artofillusion.theme.StyleAttribute;
import artofillusion.theme.UITheme;
import artofillusion.theme.UIThemeColorSet;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.*;

/**
 * This class holds GUI customization information. Customization consists of
 * various colors used in AoI GUI as well as the look and feel of some GUI
 * elements (e.g. buttons). In this respect, the theme manager is thus a factory of GUI elements.
 *
 * @author François Guillet
 *
 */
@Slf4j
public class ThemeManager {
    private static final XStream xstream = new XStream(new StaxDriver());

    static {
        xstream.ignoreUnknownElements();
        xstream.useAttributeFor(artofillusion.theme.Button.class, "buttonClass");
        xstream.aliasAttribute("class", "buttonClass");
        xstream.aliasSystemAttribute("buttonClass", "class");
        xstream.allowTypes(new Class[]{UITheme.class, UIThemeColorSet.class, artofillusion.theme.Button.class, StyleAttribute.class, DefaultToolButton.class});
        xstream.processAnnotations(new Class[]{UITheme.class, UIThemeColorSet.class, artofillusion.theme.Button.class, StyleAttribute.class});
    }
    /**
     * This class hold all the colors used by a theme. A theme can propose several color sets.
     *
     * @author François Guillet
     *
     */
    public static class ColorSet {

        private final Color appBackground;
        private final Color paletteBackground;
        private final Color viewerBackground;
        private final Color viewerLine;
        private final Color viewerHandle;
        @Getter private final Color viewerHighlight;
        private final Color viewerSpecialHighlight;
        private final Color viewerDisabled;
        private final Color viewerSurface;
        private final Color viewerTransparent;
        private final Color viewerLowValue;
        private final Color viewerHighValue;
        private final Color dockableBarColor1;
        private final Color dockableBarColor2;
        private final Color dockableTitleColor;
        private final Color textColor;
        private final String name;

        private ColorSet(UIThemeColorSet colorSet) {
            name = colorSet.getName();
            appBackground = colorSet.getApplicationBackground();
            textColor = colorSet.getTextColor();
            dockableTitleColor = colorSet.getDockableTitleColor();
            paletteBackground = colorSet.getPaletteBackground();
            viewerBackground = colorSet.getViewerBackground();
            viewerLine = colorSet.getViewerLine();
            viewerHandle = colorSet.getViewerHandle();
            viewerHighValue = colorSet.getViewerHighValue();
            dockableBarColor1 = colorSet.getDockableBarColor1();
            dockableBarColor2 = colorSet.getDockableBarColor2();

            viewerHighlight = colorSet.getViewerHighlight();
            viewerSpecialHighlight = colorSet.getViewerSpecialHighlight();
            viewerDisabled = colorSet.getViewerDisabled();
            viewerSurface = colorSet.getViewerSurface();
            viewerTransparent = colorSet.getViewerTransparent();
            viewerLowValue = colorSet.getViewerLowValue();
        }


        public String getName() {
            return Translate.text(name);
        }
    }

    /**
     * This class stores information about a theme. This can be general purpose information such as
     * theme content and author, or very specific information such as the button styling for
     * this theme.
     *
     * @author Francois Guillet
     *
     */
    public static class ThemeInfo {

        private final String name;
        public final String author;
        public final String description;
        public final Class<?> buttonClass;
        //this is the button style parameters for this theme.
        //Relevant XML node is passed onto the button class, so it can parse
        //it and deliver button style parameters the theme will give the buttons
        //whenever it is selected.
        @Getter
        private final Object buttonProperties = null;
        //button margin is the space around each button
        public final int buttonMargin;
        //palette margin is the space around the buttons
        public final int paletteMargin;
        //the theme colorsets
        private final List<ColorSet> colorSets;

        public final PluginRegistry.PluginResource resource;
        @Getter
        private final ClassLoader loader;

        public final boolean selectable;
        @Getter
        private ButtonStyle buttonStyles;

        private ThemeInfo(PluginRegistry.PluginResource resource) throws IOException {

            InputStream is = resource.getInputStream();
            var barr = is.readAllBytes();
            is.close();

            var theme = (UITheme) xstream.fromXML(new ByteArrayInputStream(barr));

            this.resource = resource;
            URL url = resource.getURL();

            String path = url.getPath();
            int cut = path.lastIndexOf('/');
            if (cut > 0) {
                path = path.substring(0, cut + 1);
            } else {
                path = "/";
            }
            url = new URL(url.getProtocol(), url.getHost(), path);
            loader = new URLClassLoader(new URL[]{url});

            name = theme.getName();
            author = theme.getAuthor();
            description = theme.getDescription();
            selectable = theme.isSelectable();

            var btn = theme.getButton();

            if (btn == null) {
                buttonClass = DefaultToolButton.class;
            } else {
                String className = btn.getButtonClass();
                Class<?> cls = DefaultToolButton.class;
                try {
                    cls = resource.getClassLoader().loadClass(className);
                } catch (ReflectiveOperationException | SecurityException ex) {
                    log.atError().setCause(ex).log("Unable to invoke method: {}", ex.getMessage());
                }

                // Parse the button styles for this theme
                for(var sa: btn.getStyles()) {
                    if(buttonStyles == null) {
                        buttonStyles = new ButtonStyle(sa);
                        continue;
                    }
                    buttonStyles.add(new ButtonStyle(sa));
                }

                buttonClass = cls;
            }


            paletteMargin = theme.getPaletteMargin();
            buttonMargin = theme.getButtonMargin();

            colorSets = new ArrayList<>();
            theme.getColorSets().forEach(colorSet -> colorSets.add(new ColorSet(colorSet)));


        }

        public String getName() {
            return Translate.text(name);
        }

        public ColorSet[] getColorSets() {
            return colorSets.toArray(ColorSet[]::new);
        }
    }

    /**
     * Nested ButtonStyle class.
     * Forms a chain of ButtonStyle objects for a particular Theme.
     * ButtonStyle objects store all the attributes of the defining XML as
     * elements of a Map. These values can be accessed by calling
     * {@link #getAttribute(String)}.
     */
    public static class ButtonStyle {

        protected Class<?> ownerType = EditingTool.class;

        protected int width = -1;
        protected int height = -1;

        @Getter
        protected final Map<String, String> attributes = new HashMap<>();
        protected ButtonStyle next = null;

        public ButtonStyle(StyleAttribute sa) {
            this.attributes.putAll(sa.getAttributes());

            if(attributes.containsKey("owner")) {
                try {
                    ownerType = ArtOfIllusion.getClass(attributes.get("owner"));
                } catch (ClassNotFoundException ex) {
                    log.atDebug().setCause(ex).log("Unable to identify ButtonStyle.owner: {}", ex.getMessage());
                }
            }

            if(attributes.containsKey("size")) {
                var value = attributes.get("size");
                int cut = value.indexOf(',');
                if (cut >= 0) {
                    width = Integer.parseInt(value.substring(0, cut).trim());
                    height = Integer.parseInt(value.substring(cut + 1).trim());
                } else {
                    width = height = Integer.parseInt(value.trim());
                }
            }
        }

        /**
         * Add new ButtonStyle to this ButtonStyle.
         */
        protected void add(ButtonStyle style){
            if(next == null) {
                next = style;
            } else {
                next.add(style);
            }
        }

        /**
         * Get the ButtonStyle associated with <i>owner</i>
         */
        public ButtonStyle getStyle(Object owner) {
            if (ownerType != null && ownerType.isInstance(owner)) {
                return this;
            }
            return next == null ? null : next.getStyle(owner);
        }

        /**
         * get the named attribute value.
         */
        public String getAttribute(String name) {
            return attributes.get(name);
        }
    }

    @Getter
    private static ThemeInfo selectedTheme;
    @Getter
    private static ThemeInfo defaultTheme;
    @Getter
    private static ColorSet selectedColorSet;
    private static ThemeInfo[] themeList;
    private static Map<String, ThemeInfo> themeIdMap;

    /**
     * icon to use if no other icon can be found
     */
    private static final ImageIcon notFoundIcon;

    // initialize the ...NotFoundIcon objects
    static {
        ImageIcon icon;
        try {
            var url = Class.forName("artofillusion.ArtOfIllusion").getResource("artofillusion/Icons/iconNotFound.png");
            icon = new ImageIcon(url);
        } catch (NullPointerException | ClassNotFoundException e) {
            BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_BYTE_INDEXED);
            Graphics2D graphics = (Graphics2D) image.getGraphics();
            graphics.setColor(new Color(128, 128, 128));
            graphics.fillRect(0, 0, 16, 16);
            graphics.setColor(new Color(200, 100, 100));
            graphics.fillOval(3, 3, 10, 10);
            graphics.setColor(Color.WHITE);
            graphics.fillRect(7, 4, 2, 4);
            graphics.fillOval(7, 10, 2, 2);
            icon = new ImageIcon(image);
        }

        notFoundIcon = icon;
    }

    /**
     * Set the currently selected theme.
     */
    public static void setSelectedTheme(ThemeInfo theme) {
        selectedTheme = theme;
        setSelectedColorSet(theme.colorSets.get(0));
        applyButtonProperties();
    }

    /**
     * Set the currently selected color set.
     */
    public static void setSelectedColorSet(ColorSet colorSet) {
        selectedColorSet = colorSet;
        applyThemeColors();
    }

    /**
     * Get a list of all available themes.
     */
    public static List<ThemeManager.ThemeInfo> getThemes() {
        return Collections.unmodifiableList(Arrays.asList(themeList));
    }

    private static void applyThemeColors() {
        ColorSet set = selectedColorSet;

        ViewerCanvas.backgroundColor = set.viewerBackground;
        ViewerCanvas.lineColor = set.viewerLine;
        ViewerCanvas.handleColor = set.viewerHandle;
        ViewerCanvas.highlightColor = set.viewerHighlight;
        ViewerCanvas.specialHighlightColor = set.viewerSpecialHighlight;
        ViewerCanvas.disabledColor = set.viewerDisabled;
        Color viewerSurface = set.viewerSurface;
        Color viewerTransparent = set.viewerTransparent;
        Color viewerLowValue = set.viewerLowValue;
        Color viewerHighValue = set.viewerHighValue;

        ViewerCanvas.surfaceColor = viewerSurface;
        ViewerCanvas.surfaceRGBColor = new RGBColor(viewerSurface.getRed() / 255.0, viewerSurface.getGreen() / 255.0, viewerSurface.getBlue() / 255.0);
        ViewerCanvas.transparentColor = new RGBColor(viewerTransparent.getRed() / 255.0, viewerTransparent.getGreen() / 255.0, viewerTransparent.getBlue() / 255.0);
        ViewerCanvas.lowValueColor = new RGBColor(viewerLowValue.getRed() / 255.0, viewerLowValue.getGreen() / 255.0, viewerLowValue.getBlue() / 255.0);
        ViewerCanvas.highValueColor = new RGBColor(viewerHighValue.getRed() / 255.0, viewerHighValue.getGreen() / 255.0, viewerHighValue.getBlue() / 255.0);
    }

    /**
     * apply the button properties for the selected Theme
     */
    private static void applyButtonProperties() {
        Class<?> buttonClass = selectedTheme.buttonClass;
        try {
            Method m = buttonClass.getMethod("setProperties", Object.class);
            m.invoke(buttonClass, selectedTheme.getButtonProperties());
        } catch (NoSuchMethodException e) {
            // missing method is quite normal - silently ignore
        } catch (ReflectiveOperationException | SecurityException ex) {
            log.atError().setCause(ex).log("Error applying Button properties: {}", ex.getMessage());
        }
    }

    /**
     * search for the named icon in the selected and default themes, returning
     * the URL of the first found icon.
     *
     * @param name the name of the icon (without path or suffix)
     *
     * @return the URL of the first found icon, or <i>null</i> if no icon
     * were found.
     */
    private static URL getIconURL(String name) {
        ThemeInfo source = selectedTheme;
        ThemeInfo defaultSource = defaultTheme;
        int colon = name.indexOf(':');
        if (colon > -1) {
            defaultSource = themeIdMap.get(name.substring(0, colon));
            name = name.substring(colon + 1);
        }

        var url = source.loader.getResource(name + ".png");
        if (url == null) {
            url = source.loader.getResource(name + ".gif");
        }
        if (url == null && defaultSource != null) {
            url = defaultSource.loader.getResource(name + ".png");
        }
        if (url == null && defaultSource != null) {
            url = defaultSource.loader.getResource(name + ".gif");
        }

        return url;
    }

    /**
     * return the URL for the "notFound" icon for the selected Theme and the
     * style associated with the specified owner.
     *
     * @param owner the owner of the button icon that could not be found.
     *
     * @return the URL of the matching notFound icon, or <i>null</i> if no
     * matching icon were found.
     */
    public static URL getNotFoundURL(Object owner) {
        String notFound = null;
        ButtonStyle bstyle = getButtonStyle(owner);

        if (bstyle != null) {
            notFound = bstyle.getAttribute("notFound");
        }
        if (notFound == null) {
            notFound = "iconNotFound";
        }

        return getIconURL(notFound);
    }

    /**
     * return the notFound icon most appropriate to the slected Theme and the
     * specified owner.
     *
     * @param owner the owner of the button icon which could not be found.
     *
     * @return an ImageIcon of the notFound icon. The method never returns
     * <i>null</i>.
     */
    public static ImageIcon getNotFoundIcon(Object owner) {
        URL url = getNotFoundURL(owner);
        if (url != null) {
            return new ImageIcon(url);
        } else {
            return notFoundIcon;
        }
    }

    /**
     * compatibility method.
     *
     * @deprecated this method allows pre 2.7 plugins to continue to function.
     * Such code should be ported to the new API as soon as possible.
     */
    @Deprecated
    public static ToolButton getToolButton(Object owner, String iconName, String selectedIconName) {
        log.warn("Deprecated method called");
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();

        if (trace.length > 1) {
            StackTraceElement frame = trace[1];
            String name = frame.getClassName();
            int cut = name.lastIndexOf('.');

            if (frame.getFileName() == null) {
                log.atInfo().log("Called from {}.{}() (unknown source)", (cut > 0 ? name.substring(cut + 1) : name), frame.getMethodName());
            } else {
                log.atInfo().log("Called from {}:{}", frame.getFileName(), frame.getLineNumber());
            }
        }

        return getToolButton(owner, iconName);
    }

    /**
     * Creates a ToolButton according to the current theme
     *
     * @param owner The button owner
     * @param iconName The name of the icon to display on the button, without extension
     * @return the ToolButton generated according to the selected Theme.
     */
    public static ToolButton getToolButton(Object owner, String iconName) {
        Class<?> buttonClass = selectedTheme.buttonClass;
        Constructor<?> ctor;
        URL url = getIconURL(iconName);
        ImageIcon selected = null;

        if (url != null) {

            /*
             * look for the selected icon from the *same classloader*
             * Simply calling getIconURL() would allow the selectedIcon to
             * be loaded from a different theme, with strange results.
             */
            // generate a URL on the same path (classloader) as icon
            String path = url.getFile();
            int cut = path.lastIndexOf('/');
            if (cut > 0) {
                path = path.substring(0, cut) + "/selected" + path.substring(cut);
            }
            try {
                selected = new ImageIcon(new URL(url.getProtocol(), url.getHost(), path));
            } catch (Throwable t) {
                selected = null;
            }
        }

        // warning: ImageIcon is happy to return a non-null Image with size<=0
        if (selected != null && selected.getIconWidth() > 0) {
            try {
                ctor = buttonClass.getConstructor(Object.class, ImageIcon.class, ImageIcon.class);
                return (ToolButton) ctor.newInstance(owner, new ImageIcon(url), selected);
            } catch (Throwable t) {
                log.atError().setCause(t).log("Could not find a usable constructor for ToolButton: {}: {} due {}", buttonClass.getName(), iconName, t.getLocalizedMessage());
            }
        }

        if (url == null) {
            url = getNotFoundURL(owner);
        }

        // if we found a single icon of some form, then use that
        if (url != null) {
            try {
                ctor = buttonClass.getConstructor(Object.class, ImageIcon.class);
                return (ToolButton) ctor.newInstance(owner, new ImageIcon(url));
            } catch (Exception t) {
                log.atError().setCause(t).log("Could not find a usable constructor for ToolButton: {}: {} due {}", buttonClass.getName(), iconName, t.getLocalizedMessage());
            }
        }

        // if all else fails, use the notFoundIcon.
        return new DefaultToolButton(owner, notFoundIcon);
    }

    /**
     * Given an icon file name, this method returns the icon according to the
     * currently selected theme. If no such icon is available within the
     * current theme, the icon is looked for in the default theme.
     * This method will first look for a .gif file, then for a.png one.
     *
     * @param iconName The file name of the icon, without extension.
     *
     * @return the ImageIcon matching the name. If no such icon were found,
     * then <i>null</i> is returned.
     */
    public static ImageIcon getIcon(String iconName) {
        URL url = getIconURL(iconName);
        if (url == null) {
            return null;
        }
        return new ImageIcon(url);
    }

    /**
     * Returns the background color of the application (not to be mistaken for the view background)
     */
    public static Color getAppBackgroundColor() {
        return selectedColorSet.appBackground;
    }

    /**
     * Returns the tool palette background color
     */
    public static Color getPaletteBackgroundColor() {
        return selectedColorSet.paletteBackground;
    }

    /**
     * Returns the first color of the dockable widgets title bar gradient painting
     */
    public static Color getDockableBarColor1() {
        return selectedColorSet.dockableBarColor1;
    }

    /**
     * Returns the second color of the dockable widgets title bar gradient painting
     */
    public static Color getDockableBarColor2() {
        return selectedColorSet.dockableBarColor2;
    }

    /**
     * Returns the text color of the dockable widgets title bar text
     */
    public static Color getDockableTitleColor() {
        return selectedColorSet.dockableTitleColor;
    }

    /**
     * Returns the color of the text to use for widgets.
     * Can also be used as foreground color.
     */
    public static Color getTextColor() {
        return selectedColorSet.textColor;
    }

    /**
     * This is invoked during startup to initialize the list of installed themes.
     */
    public static void initThemes() {
        if (themeList != null) {
            throw new IllegalStateException("The themes have already been initialized.");
        }
        themeIdMap = new HashMap<>();


        List<PluginRegistry.PluginResource> resources = PluginRegistry.getResources("UITheme");
        List<ThemeInfo> list = new ArrayList<>();
        for (PluginRegistry.PluginResource resource: resources) {
            try {
                list.add(new ThemeInfo(resource));
            } catch (IOException ex) {
                log.atError().setCause(ex).log("Unable to init themes: {}", ex.getMessage());
            }
        }
        themeList = list.toArray(new ThemeInfo[0]);
        for (ThemeInfo themeInfo: themeList) {
            themeIdMap.put(themeInfo.resource.getId(), themeInfo);
        }
        defaultTheme = themeIdMap.get("default");
        setSelectedTheme(defaultTheme);
    }

    /**
     * Returns the palette margin to use for tool palette display
     */
    public static int getPaletteMargin() {
        return selectedTheme.paletteMargin;
    }

    /**
     * Returns the button margin to use for tool palette display
     */
    public static int getButtonMargin() {
        return selectedTheme.buttonMargin;
    }

    /**
     * Returns the ButtonStyle for the current Theme and the specified owner.
     */
    public static ButtonStyle getButtonStyle(Object owner) {
        return selectedTheme.buttonStyles == null ? null: selectedTheme.buttonStyles.getStyle(owner);
    }

}

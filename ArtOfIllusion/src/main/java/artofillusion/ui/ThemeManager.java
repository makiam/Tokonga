/* Copyright (C) 2007 by François Guillet
   Some parts copyright 2007 by Peter Eastman
   Changes copyright (C) 2017-2024 by Maksim Khramov

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
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.awt.image.BufferedImage;
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

    /**
     * This class hold all the colors used by a theme. A theme can propose several color sets.
     *
     * @author François Guillet
     *
     */
    public static class ColorSet {

        public final Color appBackground;
        public final Color paletteBackground;
        public final Color viewerBackground;
        public final Color viewerLine;
        public final Color viewerHandle;
        @Getter public final Color viewerHighlight;
        public final Color viewerSpecialHighlight;
        public final Color viewerDisabled;
        public final Color viewerSurface;
        public final Color viewerTransparent;
        public final Color viewerLowValue;
        public final Color viewerHighValue;
        public final Color dockableBarColor1;
        public final Color dockableBarColor2;
        public final Color dockableTitleColor;
        public final Color textColor;
        private final String name;

        private ColorSet(Node node) {
            name = getAttribute(node, "name");
            NodeList list = node.getChildNodes();
            node = getNodeFromNodeList(list, "applicationbackground");
            appBackground = getColorFromNode(node);
            node = getNodeFromNodeList(list, "palettebackground");
            paletteBackground = getColorFromNode(node);
            node = getNodeFromNodeList(list, "viewerbackground");
            viewerBackground = getColorFromNode(node);
            node = getNodeFromNodeList(list, "viewerline");
            viewerLine = getColorFromNode(node);
            node = getNodeFromNodeList(list, "viewerhandle");
            viewerHandle = getColorFromNode(node);
            node = getNodeFromNodeList(list, "viewerhighlight");
            viewerHighlight = getColorFromNode(node);
            node = getNodeFromNodeList(list, "viewerspecialhighlight");
            viewerSpecialHighlight = getColorFromNode(node);
            node = getNodeFromNodeList(list, "viewerdisabled");
            viewerDisabled = getColorFromNode(node);
            node = getNodeFromNodeList(list, "viewersurface");
            viewerSurface = getColorFromNode(node);
            node = getNodeFromNodeList(list, "viewerlowvalue");
            viewerLowValue = getColorFromNode(node);
            node = getNodeFromNodeList(list, "viewerhighvalue");
            viewerHighValue = getColorFromNode(node);
            node = getNodeFromNodeList(list, "viewertransparent");
            viewerTransparent = getColorFromNode(node);
            node = getNodeFromNodeList(list, "dockablebarcolor1");
            dockableBarColor1 = getColorFromNode(node);
            node = getNodeFromNodeList(list, "dockablebarcolor2");
            dockableBarColor2 = getColorFromNode(node);
            node = getNodeFromNodeList(list, "dockabletitlecolor");
            dockableTitleColor = getColorFromNode(node);
            node = getNodeFromNodeList(list, "textcolor");
            textColor = getColorFromNode(node);
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
        public final Object buttonProperties;
        //button margin is the space around each button
        public final int buttonMargin;
        //palette margin is the space around the buttons
        public final int paletteMargin;
        //the theme colorsets
        private final ColorSet[] colorSets;
        public final boolean classicToolBarButtons;
        public final PluginRegistry.PluginResource resource;
        @Getter
        private final ClassLoader loader;
        //public final String pathRoot;
        public final boolean selectable;
        protected ButtonStyle buttonStyles;

        private ThemeInfo(PluginRegistry.PluginResource resource) throws IOException, SAXException, ParserConfigurationException {
            InputStream is = resource.getInputStream();
            DocumentBuilder builder;
            builder = documentBuilderFactory.newDocumentBuilder();
            Document document = builder.parse(is);
            is.close();
            Node rootNode = document.getDocumentElement();
            NodeList themeNodeList = rootNode.getChildNodes();
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

            Node node = getNodeFromNodeList(themeNodeList, "name");
            name = (node == null ? "" : node.getFirstChild().getNodeValue());
            node = getNodeFromNodeList(themeNodeList, "author");
            author = (node == null ? "" : node.getFirstChild().getNodeValue());
            node = getNodeFromNodeList(themeNodeList, "description");
            description = (node == null ? "" : node.getFirstChild().getNodeValue());
            node = getNodeFromNodeList(themeNodeList, "selectable");
            selectable = (node == null ? true : Boolean.parseBoolean(node.getFirstChild().getNodeValue()));
            node = getNodeFromNodeList(themeNodeList, "button");
            if (node == null) {
                buttonClass = DefaultToolButton.class;
                buttonProperties = null;
                classicToolBarButtons = false;
            } else {
                String className = getAttribute(node, "class");
                Object properties = null;
                Class<?> cls = DefaultToolButton.class;
                try {
                    cls = resource.getClassLoader().loadClass(className);
                    Method m = cls.getMethod("readPropertiesFromXMLNode", Node.class);
                    properties = m.invoke(className, node);
                } catch (ReflectiveOperationException | SecurityException ex) {
                    log.atError().setCause(ex).log("Unable to invoke method: {}", ex.getMessage());
                }

                // parse the button styles for this theme
                ButtonStyle bstyle = null;
                NodeList list = node.getChildNodes();

                for (int i = 0; i < list.getLength(); i++) {
                    Node kid = list.item(i);
                    if (kid.getNodeName().equals("style")) {
                        if (bstyle == null) {
                            bstyle = new ButtonStyle(kid);
                        } else {
                            bstyle.add(kid);
                        }
                    }
                }

                buttonClass = cls;
                buttonProperties = properties;
                buttonStyles = bstyle;
                String s = getAttribute(node, "useintoolbars");
                classicToolBarButtons = s != null && !Boolean.parseBoolean(s);
            }
            node = getNodeFromNodeList(themeNodeList, "palettemargin");
            paletteMargin = getIntegerValueFromNode(node);
            node = getNodeFromNodeList(themeNodeList, "buttonmargin");
            buttonMargin = getIntegerValueFromNode(node);
            //color sets
            int count = 0;
            for (int i = 0; i < themeNodeList.getLength(); i++) {
                node = themeNodeList.item(i);
                if (node.getNodeName().equals("colorset")) {
                    count++;
                }
            }
            colorSets = new ColorSet[count];
            count = 0;
            for (int i = 0; i < themeNodeList.getLength(); i++) {
                node = themeNodeList.item(i);
                if (node.getNodeName().equals("colorset")) {
                    colorSets[count++] = new ColorSet(node);
                }
            }
        }

        public String getName() {
            return Translate.text(name);
        }

        public ColorSet[] getColorSets() {
            return colorSets.clone();
        }
    }

    /**
     * nested ButtonStyle class.
     * Forms a chain of ButtonStyle objects for a particular Theme.
     * ButtonStyle objects store all the attributes of the defining XML as
     * elements of a Map. These values can be accessed by calling
     * {@link #getAttribute(String)}.
     */
    public static class ButtonStyle {

        protected Class<?> ownerType;
        protected int width = -1;
        protected int height = -1;

        protected final Map<String, String> attributes = new HashMap<>();
        protected ButtonStyle next;

        /**
         * create a new ButtonStyle by parsing the XML represented by node.
         *
         * @param node the XML defining the style.
         */
        public ButtonStyle(Node node) {
            String name, value;

            // stash all attributes in the attributes map
            NamedNodeMap kids = node.getAttributes();
            for (int i = 0; i < kids.getLength(); i++) {
                node = kids.item(i);
                name = node.getNodeName();
                value = node.getNodeValue();
                attributes.put(name, value);

                if (name.equalsIgnoreCase("owner")) {
                    try {
                        ownerType = ArtOfIllusion.getClass(value);
                    } catch (ClassNotFoundException ex) {
                        log.atDebug().setCause(ex).log("Unable to identify ButtonStyle.owner: {}", ex.getMessage());
                    }
                }

                if (name.equalsIgnoreCase("size")) {
                    int cut = value.indexOf(',');
                    if (cut >= 0) {
                        width = Integer.parseInt(value.substring(0, cut).trim());
                        height = Integer.parseInt(value.substring(cut + 1).trim());
                    } else {
                        width = height = Integer.parseInt(value.trim());
                    }
                }
            }

            if (ownerType == null) {
                ownerType = EditingTool.class;
            }
        }

        /**
         * add a new ButtonNode to this ButtonNode.
         */
        protected void add(Node node) {
            if (next == null) {
                next = new ButtonStyle(node);
            } else {
                next.add(node);
            }
        }

        /**
         * get the ButtonStyle associated with <i>owner</i>
         */
        public ButtonStyle getStyle(Object owner) {
            if (ownerType != null && ownerType.isInstance(owner)) {
                return this;
            }
            return (next == null ? null : next.getStyle(owner));
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
    private static DocumentBuilderFactory documentBuilderFactory; //XML parsing

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
        setSelectedColorSet(theme.colorSets[0]);
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
        ViewerCanvas.backgroundColor = new Color(set.viewerBackground.getRed(),
                set.viewerBackground.getGreen(),
                set.viewerBackground.getBlue());
        ViewerCanvas.lineColor = new Color(set.viewerLine.getRed(),
                set.viewerLine.getGreen(),
                set.viewerLine.getBlue());
        ViewerCanvas.handleColor = new Color(set.viewerHandle.getRed(),
                set.viewerHandle.getGreen(),
                set.viewerHandle.getBlue());
        ViewerCanvas.highlightColor = new Color(set.viewerHighlight.getRed(),
                set.viewerHighlight.getGreen(),
                set.viewerHighlight.getBlue());
        ViewerCanvas.specialHighlightColor = new Color(set.viewerSpecialHighlight.getRed(),
                set.viewerSpecialHighlight.getGreen(),
                set.viewerSpecialHighlight.getBlue());
        ViewerCanvas.disabledColor = new Color(set.viewerDisabled.getRed(),
                set.viewerDisabled.getGreen(),
                set.viewerDisabled.getBlue());
        Color viewerSurface = new Color(set.viewerSurface.getRed(),
                set.viewerSurface.getGreen(),
                set.viewerSurface.getBlue());
        Color viewerTransparent = new Color(set.viewerTransparent.getRed(),
                set.viewerTransparent.getGreen(),
                set.viewerTransparent.getBlue());
        Color viewerLowValue = new Color(set.viewerLowValue.getRed(),
                set.viewerLowValue.getGreen(),
                set.viewerLowValue.getBlue());
        Color viewerHighValue = new Color(set.viewerHighValue.getRed(),
                set.viewerHighValue.getGreen(),
                set.viewerHighValue.getBlue());
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
            m.invoke(buttonClass, selectedTheme.buttonProperties);
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
            } catch (Throwable t) {
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
        documentBuilderFactory = DocumentBuilderFactory.newInstance();
        List<PluginRegistry.PluginResource> resources = PluginRegistry.getResources("UITheme");
        ArrayList<ThemeInfo> list = new ArrayList<>();
        for (PluginRegistry.PluginResource resource : resources) {
            try {
                ThemeInfo themeInfo = new ThemeInfo(resource);
                list.add(themeInfo);
            } catch (IOException | ParserConfigurationException | SAXException ex) {
                log.atError().setCause(ex).log("Unable to init themes: {}", ex.getMessage());
            }
        }
        themeList = list.toArray(new ThemeInfo[0]);
        for (ThemeInfo themeInfo : themeList) {
            themeIdMap.put(themeInfo.resource.getId(), themeInfo);
        }
        defaultTheme = themeIdMap.get("default");
        setSelectedTheme(defaultTheme);
    }

    private static int getIntegerValueFromNode(Node node) {
        if (node != null) {
            String s = getAttribute(node, "value");
            if (s != null) {
                return Integer.parseInt(s);
            }
        }
        return 0;
    }

    private static Color getColorFromNode(Node node) {
        if (node == null) {
            return new Color(0, 0, 0);
        }
        String s = getAttribute(node, "R");
        int r = 0;
        if (s != null) {
            r = Integer.parseInt(s);
        }
        int g = 0;
        s = getAttribute(node, "G");
        if (s != null) {
            g = Integer.parseInt(s);
        }
        int b = 0;
        s = getAttribute(node, "B");
        if (s != null) {
            b = Integer.parseInt(s);
        }
        return new Color(r, g, b);
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
     * returns the ButtonStyle for the current Theme and the specified owner.
     */
    public static ButtonStyle getButtonStyle(Object owner) {
        return (selectedTheme.buttonStyles != null ? selectedTheme.buttonStyles.getStyle(owner) : null);
    }

    /**
     * Utility for parsing XML Documents: gets a named node from a node list. Returns null if the node does not
     * exist.
     *
     * @param nl The node list
     * @param nodeName The node name
     * @return The node named nodeName
     */
    private static Node getNodeFromNodeList(NodeList nl, String nodeName) {
        for (int i = 0; i < nl.getLength(); ++i) {
            Node n = nl.item(i);
            if (n.getNodeName().equals(nodeName)) {
                return n;
            }
        }
        return null;
    }

    /**
     * Utility for parsing XML Documents: gets a named attribute value from a node
     *
     * @param name The attribute name
     * @param node Description of the Parameter
     * @return The attribute value
     */
    private static String getAttribute(Node node, String name) {
        NamedNodeMap nm = node.getAttributes();
        if (nm == null) {
            return null;
        }
        Node nn = nm.getNamedItem(name);
        if (nn == null) {
            return null;
        }
        return nn.getNodeValue();
    }
}

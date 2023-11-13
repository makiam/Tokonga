/*
 *  Copyright (C) 2003 by Francois Guillet
 *  Changes copyright 2023 by Maksim Khramov
 *  This program is free software; you can redistribute it and/or modify it under the
 *  terms of the GNU General Public License as published by the Free Software
 *  Foundation; either version 2 of the License, or (at your option) any later version.
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY
 *  WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 *  PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 */
package artofillusion.spmanager;

import artofillusion.*;
import artofillusion.ui.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.*;

/**
 * Description of the Class
 *
 * @author pims
 * @created 6 juillet 2004
 */
@Slf4j
public class SPManagerUtils {

    public static final DocumentBuilderFactory factory;
    public static DocumentBuilder builder;

    static {
        factory = DocumentBuilderFactory.newInstance();
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException ex) {
            log.atError().setCause(ex).log("Bad parser configuration");
        }
    }

    /**
     * Description of the Method
     */
    public static void updateAllAoIWindows() {
        for (EditingWindow allWindow : ArtOfIllusion.getWindows()) {
            if (allWindow instanceof LayoutWindow) {
                ((LayoutWindow) allWindow).rebuildScriptsMenu();
            }
        }
    }

    /**
     * Gets a named node from a node list. Returns null if the node does not
     * exist.
     *
     * @param nl The node list
     * @param nodeName The node name
     * @return The node named nodeName
     */
    public static Node getNodeFromNodeList(NodeList nl, String nodeName, int index) {
        for (int i = 0; i < nl.getLength(); ++i) {
            Node n = nl.item(i);
            if (n.getNodeName().equals(nodeName)) {
                if (index-- == 0) {
                    return n;
                }
            }
        }
        return null;
    }

    /**
     * Gets a named attribute value from a node
     *
     * @param name The attribute name
     * @param node Description of the Parameter
     * @return The attribute value
     */
    public static String getAttribute(Node node, String name) {
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

    /**
     * Gets a value from a named child node
     *
     * @param name The child node name
     * @param node The node
     * @return The attribute value
     */
    public static String getNodeValue(Node node, String name, String defaultVal, int index) {
        NodeList nl = node.getChildNodes();
        if (nl.getLength() == 0) {
            return defaultVal;
        }

        Node n = getNodeFromNodeList(nl, name, index);
        if (n == null) {
            return defaultVal;
        }

        n = n.getChildNodes().item(0);
        if (n == null) {
            return defaultVal;
        }

        String value = n.getNodeValue();
        if (value == null) {
            value = defaultVal;
        }

        return value;
    }

    /**
     * method to parse digits into an int
     */
    public static int parseInt(String val, int start, int max) throws NumberFormatException {
        if (val == null || val.length() <= start) {
            return 0;
        }

        if (max < 0 || max > val.length()) {
            max = val.length();
        }

        char c;

        // throw exception if the value cannot be a number
        c = val.charAt(start);
        if (!(Character.isDigit(c) || "+-".indexOf(c) >= 0)) {
            throw new NumberFormatException(val);
        }

        int result = 0;
        for (int i = start; i < max; i++) {
            c = val.charAt(i);
            if (Character.isDigit(c)) {
                result = (result * 10) + Character.digit(c, 10);
            } else {
                break;
            }
        }

        return result;
    }

    /**
     * parse a double value from a String.
     *
     * The string is parsed from the first (zeroeth) char up to the first
     * char which is not valid in a double representation
     * (i.e., digit, '.', 'e', 'E', '+', or '-').
     */
    public static double parseDouble(String val) throws NumberFormatException {
        if (val == null || val.length() == 0) {
            return 0;
        }

        char c;

        // throw exception if the value cannot be a number
        c = val.charAt(0);
        if (!(Character.isDigit(c) || "+-.".indexOf(c) >= 0)) {
            throw new NumberFormatException(val);
        }

        long result = 0;
        long frac = 0;
        int exp = 0;
        boolean mantissa = true;
        int sign = 1, esign = 1;

        int max = val.length();
        for (int i = 0; i < max; i++) {
            c = val.charAt(i);

            if (result == 0 && (c == '-' || c == '+')) {
                sign = (c == '-' ? -1 : 1);
            } else if (frac == 0 && c == '.') {
                frac = 1;
            } else if (mantissa == true && (c == 'e' || c == 'E')) {
                mantissa = false;
            } else if (Character.isDigit(c)) {
                if (mantissa) {
                    result = (result * 10) + Character.digit(c, 10);

                    if (frac > 0) {
                        frac *= 10;
                    }
                    log.atDebug().log("Test; c={}; frac={}", c, frac);
                } else {
                    if (exp == 0 && (c == '-' || c == '+')) {
                        esign = (c == '-' ? -1 : 1);
                    } else {
                        exp = (exp * 10) + Character.digit(c, 10);
                    }
                }
            } else {
                break;
            }
        }

        if (frac == 0) {
            frac = 1;
        }
        log.atDebug().log("Parse double: esign: {}; exp: {}; sign: {}; result: {}; frac: {}", esign, exp, sign, result, frac);

        return (Math.pow(10.0, esign * exp) * sign * result) / frac;
    }

    /**
     * parse a version string into a numeric value.
     *
     * Each component is parsed as an int and scaled into a 3-digit
     * column.
     * <br>Eg 1.2 is parsed into 1002; 1.20 is parsed into 1020; and 1.20.3
     * is parsed into 1020003
     *
     */
    public static long parseVersion(String val) throws NumberFormatException {
        long result = parseInt(val, 0, -1);

        int pos = 0;
        while ((pos = val.indexOf('.', pos)) >= 0) {
            result = (result * 1000) + parseInt(val, pos + 1, -1);
            pos++;
        }

        return result;
    }
}

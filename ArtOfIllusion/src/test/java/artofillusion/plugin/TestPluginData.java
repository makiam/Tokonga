/* Copyright 2024 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.plugin;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Locale;

class TestPluginData {
    static XStream xstream;

    @BeforeAll
    static void init() {
        xstream = new XStream(new StaxDriver());
        xstream.ignoreUnknownElements();
        xstream.allowTypes(new Class[]{Extension.class, Category.class, PluginDef.class, ImportDef.class, Export.class, History.class, LogRecord.class, Resource.class, External.class, Fileset.class, FilesetItem.class});
        xstream.processAnnotations(new Class[]{Extension.class, Category.class, PluginDef.class, ImportDef.class, Export.class, History.class, LogRecord.class, Resource.class, External.class, Fileset.class, FilesetItem.class});
    }
    @Test
    void testReadEmptyExtension() throws IOException {
        Extension ext = (Extension)xstream.fromXML(TestPluginData.class.getResource("/artofillusion/plugin/EmptyExtension.xml").openStream());
        Assertions.assertEquals("Extension", ext.getName());
    }

    @Test
    void testReadCombinedExtension0() throws IOException {
        Extension ext = (Extension)xstream.fromXML(TestPluginData.class.getResource("/artofillusion/plugin/Combined0.xml").openStream());
        Assertions.assertEquals("Extension", ext.getName());
        Assertions.assertEquals(2, ext.getCategoryList().size());

        ext.getCategoryList().forEach(cc -> System.out.println(cc.getCategory()));
    }

    @Test
    void testReadCombinedExtension1() throws IOException {
        Extension ext = (Extension)xstream.fromXML(TestPluginData.class.getResource("/artofillusion/plugin/Combined1.xml").openStream());
        Assertions.assertEquals("Extension", ext.getName());
        Assertions.assertEquals(0, ext.getCategoryList().size());
        Assertions.assertEquals(2, ext.getPluginsList().size());

    }

    @Test
    void testReadCombinedExtension2() throws IOException {
        Extension ext = (Extension)xstream.fromXML(TestPluginData.class.getResource("/artofillusion/plugin/Combined2.xml").openStream());
        Assertions.assertEquals("Extension", ext.getName());
        Assertions.assertEquals(0, ext.getCategoryList().size());
        Assertions.assertEquals(1, ext.getImports().size());

    }

    @Test
    void testGetPluginExports() throws IOException {
        Extension ext = (Extension)xstream.fromXML(TestPluginData.class.getResource("/artofillusion/plugin/Combined3.xml").openStream());
        Assertions.assertEquals("Extension", ext.getName());
        Assertions.assertEquals(1, ext.getPluginsList().size());

        Assertions.assertEquals(2, ext.getPluginsList().get(0).getExports().size());
        String def = ext.getPluginsList().get(0).getExports().get(1).getDescription();
        Assertions.assertEquals("Some description", def);
    }

    @Test
    void testBadXml() {
        Assertions.assertThrows(com.thoughtworks.xstream.mapper.CannotResolveClassException.class, () -> {
            xstream.fromXML(TestPluginData.class.getResource("/artofillusion/plugin/noextension.xml").openStream());
        });
    }

    @Test
    void testReadComments() throws IOException {

        Extension ext = (Extension)xstream.fromXML(TestPluginData.class.getResource("/artofillusion/plugin/History.xml").openStream());
        Assertions.assertEquals("HIDPlugin", ext.getName());

        System.out.println(ext.getComments());

    }

    @Test
    void testReadHistory() throws IOException {
        Extension ext = (Extension)xstream.fromXML(TestPluginData.class.getResource("/artofillusion/plugin/History.xml").openStream());
        Assertions.assertEquals("HIDPlugin", ext.getName());
        var history = ext.getHistory();
        Assertions.assertNotNull(history);
        Assertions.assertNotNull(history.getRecords());

        history.getRecords().forEach(System.out::println);

    }

    @Test
    void testReadHistoryEmpty()  throws IOException {
        Extension ext = (Extension)xstream.fromXML(TestPluginData.class.getResource("/artofillusion/plugin/HistoryEmpty.xml").openStream());
        Assertions.assertEquals("HIDPlugin", ext.getName());
        var history = ext.getHistory();
        Assertions.assertNotNull(history);
        Assertions.assertNotNull(history.getRecords());
    }

    @Test
    void testReadResources() throws IOException {
        Extension ext = (Extension)xstream.fromXML(TestPluginData.class.getResource("/artofillusion/plugin/Resources.xml").openStream());
        Assertions.assertEquals(5, ext.getResources().size());
        var res = ext.getResources().get(4);

        Assertions.assertEquals(Locale.GERMAN, res.getLocale());
    }

    @Test
    void testResourceConstructor() {
        var res = new Resource("UITheme", "gary.ElectricWax", "Theme1/theme.xml");
        Assertions.assertEquals("UITheme", res.getType());
        Assertions.assertEquals("gary.ElectricWax", res.getId());
        Assertions.assertEquals("Theme1/theme.xml", res.getName());
        Assertions.assertNull(res.getLocale());
    }

    @Test
    void testReadPreferencesPluginDescriptor() throws IOException {
        Extension ext = (Extension)xstream.fromXML(TestPluginData.class.getResource("/artofillusion/plugin/PreferencesPluginExtension.xml").openStream());
        Assertions.assertNotNull(ext);
        Assertions.assertEquals(2, ext.getAuthors().size());
    }

    @Test
    void testReadFileset() throws IOException {
        Extension ext = (Extension)xstream.fromXML(TestPluginData.class.getResource("/artofillusion/plugin/HelpPlugin.xml").openStream());
        Assertions.assertNotNull(ext);
        Assertions.assertNotNull(ext.getHistory());
        Assertions.assertNotNull(ext.getFileset());
        ext.getFileset().getFiles().forEach(System.out::println);
    }

    @Test
    void testReadExternals() throws IOException {
        Extension ext = (Extension)xstream.fromXML(TestPluginData.class.getResource("/artofillusion/plugin/HelpPlugin.xml").openStream());
        Assertions.assertNotNull(ext);
        Assertions.assertEquals(5, ext.getExternals().size());
    }



}

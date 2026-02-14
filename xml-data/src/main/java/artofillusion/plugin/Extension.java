/* Copyright 2024-2026 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.plugin;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import lombok.Data;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@XStreamAlias("extension")
@Getter
public class Extension {
    @XStreamAsAttribute
    private String name;
    @XStreamAsAttribute
    private String version;
    @XStreamImplicit
    @XStreamAlias("author")
    private final List<String> authors = new ArrayList<>();
    @XStreamAlias("date")
    private String date;
    @XStreamAlias("description")
    private String description;

    public List<Category> getCategoryList() {
        return categoryList == null ? List.of() : categoryList;
    }

    @XStreamImplicit
    private final List<Category> categoryList = new ArrayList<>();

    public List<PluginDef> getPluginsList() {
        return pluginsList == null ? List.of() : pluginsList;
    }

    @XStreamImplicit
    private final List<PluginDef> pluginsList = new ArrayList<>();

    public List<ImportDef> getImports() {
        return imports == null ? List.of() : imports;
    }

    @XStreamImplicit
    private final List<ImportDef> imports = new ArrayList<>();

    public String getComments() {
        return comments.strip();
    }

    @XStreamAlias("comments")
    private String comments;

    private History history;

    private Fileset fileset;

    public List<Resource> getResources() {
        return resources == null ? List.of() : resources;
    }

    @XStreamImplicit
    private final List<Resource> resources = new ArrayList<>();

    @XStreamImplicit
    private final List<External> externals = new ArrayList<>();


}

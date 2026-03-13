/* Copyright 2024 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.plugin;


import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import lombok.Data;

import java.util.Locale;

@XStreamAlias("resource")
@Data
public class Resource {

    @XStreamAsAttribute
    private String type;

    @XStreamAsAttribute
    private String id;

    @XStreamAsAttribute
    private String name;

    @XStreamAsAttribute
    private Locale locale = null;

    public Resource(String type, String id, String name, Locale locale) {
        this(type, id, name);
        this.locale = locale;
    }

    public Resource(String type, String id, String name) {
        this.type = type;
        this.id = id;
        this.name = name;
    }
}

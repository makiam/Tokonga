/* Copyright 2024 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.plugin;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.converters.extended.ToAttributedValueConverter;
import lombok.Data;

@XStreamAlias("export")
@Data
@XStreamConverter(value = ToAttributedValueConverter.class, strings = {"description"})
public class Export {
    @XStreamAlias("id") private String id;
    @XStreamAlias("method") private String method;

    public String getDescription() {
        return description.strip();
    }

    private String description;
}

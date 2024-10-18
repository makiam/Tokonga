/* Copyright 2024 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.plugin

import com.thoughtworks.xstream.annotations.XStreamAlias
import com.thoughtworks.xstream.annotations.XStreamAsAttribute
import com.thoughtworks.xstream.annotations.XStreamConverter
import com.thoughtworks.xstream.annotations.XStreamImplicit
import com.thoughtworks.xstream.converters.extended.ToAttributedValueConverter


@XStreamAlias("fileset")
class Fileset {
//    @XStreamImplicit
//    private val files: MutableList<FilesetItem?>? = ArrayList<FilesetItem?>()
//
//    fun getRecords(): MutableList<FilesetItem?> {
//        return (if (files == null) mutableListOf<FilesetItem?>() else files)
//    }
}

//@XStreamAlias("file")
//@XStreamConverter(ToAttributedValueConverter::class, strings = ["source"])
//class FilesetItem(@XStreamAlias("todir") @XStreamAsAttribute val target: String, val source: String)

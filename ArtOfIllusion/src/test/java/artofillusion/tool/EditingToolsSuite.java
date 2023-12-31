/* Copyright (C) 2022-2023 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.tool;

import artofillusion.tool.help.TestToolsHelp;
import artofillusion.tool.hint.TestToolsHints;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 *
 * @author MaksK
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({TestToolWhichClicks.class, TestToolsHelp.class, TestToolsHints.class})
public class EditingToolsSuite {
}

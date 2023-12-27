/* Copyright (C) 2023 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.test.util;

import javax.swing.UIManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 *
 * @author MaksK
 */
@Slf4j
public class SetupLookAndFeel implements BeforeAllCallback {

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        log.atInfo().log("Set App LAF");
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    }
    
}

/* Copyright (C) 2026 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.script;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Checks that output produced by Groovy scripts is directed to the stream passed to
 * {@link GroovyScriptEngine#setOutput(java.io.PrintStream)}.
 * <p>
 * The engine used to rely on the output writer of {@code CompilerConfiguration}, which is deprecated in Groovy 6
 * and has no effect. Scripts resolve the {@code out} variable from the script binding instead.
 *
 * @author maksim.khramov
 */
@DisplayName("Groovy Script Engine Output Test")
class GroovyScriptEngineOutputTest {

    private final GroovyScriptEngine engine = new GroovyScriptEngine(getClass().getClassLoader());

    @Test
    @DisplayName("Script output goes to the configured stream")
    void scriptOutputGoesToConfiguredStream() throws Exception {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        engine.setOutput(new PrintStream(stream, true));

        engine.executeScript("println 'hello from script'", Map.of());

        assertTrue(text(stream).contains("hello from script"), "Actual output: [" + text(stream) + "]");
    }

    @Test
    @DisplayName("Tool script output goes to the configured stream")
    void toolScriptOutputGoesToConfiguredStream() throws Exception {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        engine.setOutput(new PrintStream(stream, true));

        ToolScript script = engine.createToolScript("println 'hello from tool script'");
        script.execute(null);

        assertTrue(text(stream).contains("hello from tool script"), "Actual output: [" + text(stream) + "]");
    }

    @Test
    @DisplayName("Compiled script follows the stream set after it was compiled")
    void compiledScriptFollowsLatestStream() throws Exception {
        ByteArrayOutputStream first = new ByteArrayOutputStream();
        ByteArrayOutputStream second = new ByteArrayOutputStream();

        engine.setOutput(new PrintStream(first, true));
        ToolScript script = engine.createToolScript("println 'message'");
        engine.setOutput(new PrintStream(second, true));
        script.execute(null);

        assertEquals("", text(first), "Script wrote to the stream replaced by setOutput()");
        assertTrue(text(second).contains("message"), "Actual output: [" + text(second) + "]");
    }

    private String text(ByteArrayOutputStream stream) {
        return stream.toString(StandardCharsets.UTF_8);
    }
}

/* Copyright (C) 2013 by Peter Eastman
   Changes copyright (C) 2020-2026 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.script;

import artofillusion.*;
import groovy.lang.*;
import org.codehaus.groovy.control.*;
import org.codehaus.groovy.runtime.StackTraceUtils;
import java.io.*;
import java.util.*;

/**
 * This ScriptEngine implements the Groovy scripting language.
 */
public class GroovyScriptEngine implements ScriptEngine {

    @SuppressWarnings("java:S3416")
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ArtOfIllusion.class);

    private final CompilerConfiguration config = new CompilerConfiguration();
    private final GroovyShell shell;
    private final StringBuilder imports = new StringBuilder();
    private int numImports = 0;

    private final Map<String, Script> cache = new HashMap<>();
    private final Binding binding = new Binding();
    private volatile PrintWriter scriptOutput = new PrintWriter(System.out, true);

    public GroovyScriptEngine(ClassLoader parent) {
        binding.setVariable("undo", new WindowApiClosure(null) {
            public Void doCall() {
                getLayout().undoCommand();
                return null; }
        });
        binding.setVariable("redo", new WindowApiClosure(null) {
            public Void doCall() {
                getLayout().redoCommand();
                return null; }
        });
        binding.setVariable("log", new groovy.lang.Closure<Void>(null) {

            public Void doCall() { return null; }
            public Void doCall(String format, Object... arguments) {
                logger.info(format, arguments);
                return null;
            }
            public Void doCall(Object... arguments) {
                logger.info("Args: {}", Arrays.toString(arguments));
                return null;
            }
        });
        shell = new GroovyShell(parent, binding, config);

    }

    @Override
    public String getName() {
        return ScriptRunner.Language.GROOVY.name;
    }

    @Override
    public String getFilenameExtension() {
        return ScriptRunner.Language.GROOVY.fileNameExtension;
    }

    @Override
    public void setOutput(PrintStream out) {
        // CompilerConfiguration.setOutput() is deprecated in Groovy 6 and has no effect.
        // Script.print()/println()/printf() resolve the "out" variable from the script binding.
        scriptOutput = new PrintWriter(out, true);
        binding.setVariable("out", scriptOutput);
    }

    @Override
    public void addImport(String packageOrClass) {
        imports.append("import ").append(packageOrClass).append(";\n");
        numImports++;
    }

    @Override
    public void executeScript(String scriptBody, Map<String, Object> variables) throws ScriptException {
        variables.forEach(shell::setVariable);
        String hash = imports + scriptBody;

        try {
            Script script = cache.computeIfAbsent(hash, shell::parse);
            script.run();

        } catch (GroovyRuntimeException gre) {
            Throwable tt = StackTraceUtils.sanitize(gre);
            int ln = tt.getStackTrace()[0].getLineNumber() - numImports;
            throw new ScriptException(tt.getMessage(), ln);
        }
    }

    @Override
    public ToolScript createToolScript(String script) throws ScriptException {
        try {
            return new CompiledToolScript(shell.parse(imports + script));
        } catch (CompilationFailedException e) {
            throw new ScriptException(e.getMessage(), -1);
        }
    }

    @Override
    public ObjectScript createObjectScript(String script) throws ScriptException {
        try {
            return new CompiledObjectScript(shell.parse(imports + script));
        } catch (CompilationFailedException e) {
            throw new ScriptException(e.getMessage(), -1, e);
        }
    }

    /**
     * Inner class used to represent a compiled ToolScript.
     */
    private class CompiledToolScript implements ToolScript {

        private final Script script;

        public CompiledToolScript(Script script) {
            this.script = script;
        }

        @Override
        public void execute(LayoutWindow window) throws ScriptException {
            script.setProperty("out", scriptOutput);
            script.setProperty("window", window);
            script.setProperty("scene", window.getScene());
            try {
                script.run();
            } catch (Exception ex) {
                int line = -1;
                for (StackTraceElement element : ex.getStackTrace()) {
                    if (element.getClassName().equals(script.getClass().getName())) {
                        line = element.getLineNumber() - numImports;
                        break;
                    }
                }
                throw new ScriptException(ex.getMessage(), line, ex);
            }
        }
    }

    /**
     * Inner class used to represent a compiled ObjectScript.
     */
    private class CompiledObjectScript implements ObjectScript {

        private final Script script;

        public CompiledObjectScript(Script script) {
            this.script = script;
        }

        @Override
        public void execute(ScriptedObjectController controller) {
            script.setProperty("out", scriptOutput);
            script.setProperty("script", controller);
            script.run();
        }
    }

    private class WindowApiClosure extends Closure<Void> {

        LayoutWindow getLayout() {
            return (LayoutWindow) shell.getContext().getVariable("window");
        }

        /**
         * Constructor used when the "this" object for the Closure is null.
         * This is rarely the case in normal Groovy usage.
         *
         * @param owner the Closure owner
         */
        public WindowApiClosure(Object owner) {
            super(owner);
        }
    }
}

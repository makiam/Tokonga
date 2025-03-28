/* Copyright (C) 2001-2011 by David M. Turner <novalis@novalis.org>
   Changes copyright (C) 2017-2025 by Maksim Khramov

   Various bug fixes and enhancements added by Peter Eastman, Aug. 25, 2001.

   This program is free software; you can redistribute it and/or modify it
   under the terms of the GNU General Public License as published by the Free
   Software Foundation; either version 2 of the License, or (at your option)
   any later version.

   This program is distributed in the hope that it will be useful, but
   WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
   or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
   for more details. */

package artofillusion.procedural;

import artofillusion.*;
import artofillusion.math.*;
import artofillusion.ui.*;
import buoy.event.*;
import buoy.widget.*;
import java.awt.*;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class OPort {

    public final Module module;
    public int oport = 0;
    public Arg[] args = {new Arg("Arg1", 0)};

    OPort(Module m, int p, Arg... i) {
        module = m;
        oport = p;
        args = i;
    }

    OPort(Module m, int p) {
        module = m;
        oport = p;
    }

    OPort(Module m) {
        module = m;
        oport = 0;
    }

    IOPort getOPort() {
        if (module == null) {
            log.atDebug().log("Can't get an IOPort when there's no module.");
            return null;
        }
        return module.getOutputPorts()[oport];
    }
}

class Arg {

    public final String name;
    public final int iport;

    Arg(String s, int i) {
        name = s;
        iport = i;
    }
}

class Token {

    public static final char FUNCTION = '&';
    public static final char VARIABLE = '$';
    public static final char NUMBER = '#';
    public static final char END = '@';
    public static final char PLUS = '+';
    public static final char MINUS = '-';
    public static final char MUL = '*';
    public static final char EXP = '^';
    public static final char DIV = '/';
    public static final char MOD = '%';
    public static final char RET = ';';
    public static final char ASSIGN = '=';
    public static final char LP = '(';
    public static final char RP = ')';
    public static final char COMMA = ',';

    public String strValue;
    public double numValue;
    public char ty;

    static final Map<String, OPort> funMap = createFunMap();
    //    static Hashtable portMap = createPortMap ();

    static Map<String, OPort> createFunMap() {
        Map<String, OPort> fm = new Hashtable<>();
        //For version two, pull these out of a config file
        fm.put("sin", new OPort(new SineModule(new Point()), 0));
        fm.put("cos", new OPort(new CosineModule(new Point()), 0));
        fm.put("sqrt", new OPort(new SqrtModule(new Point()), 0));
        fm.put("pow", new OPort(new PowerModule(new Point()), 0, new Arg[]{
            new Arg("Base", 1),
            new Arg("Exponent", 0)
        }));
        fm.put("log", new OPort(new LogModule(new Point()), 0));
        fm.put("angle", new OPort(new PolarModule(new Point()), 1,
                new Arg[]{
                    new Arg("X", 0),
                    new Arg("Y", 1)
                }));
        fm.put("min", new OPort(new MinModule(new Point()), 0, new Arg[]{
            new Arg("Value 1", 1),
            new Arg("Value 2", 0)
        }));
        fm.put("max", new OPort(new MaxModule(new Point()), 0, new Arg[]{
            new Arg("Value 1", 1),
            new Arg("Value 2", 0)
        }));
        fm.put("abs", new OPort(new AbsModule(new Point()), 0));
        fm.put("exp", new OPort(new ExpModule(new Point()), 0));
        fm.put("bias", new OPort(new BiasModule(new Point()), 0, new Arg[]{
            new Arg("Input", 1),
            new Arg("Bias", 0)
        }));
        fm.put("gain", new OPort(new GainModule(new Point()), 0, new Arg[]{
            new Arg("Input", 1),
            new Arg("Gain", 0)
        }));

        return fm;
    }

    public Token(char c) {
        ty = c;
    }

    public boolean equals(char c) {
        return ty == c;
    }

    @Override
    public String toString() {
        return "type: " + ty + " strValue: " + strValue + " numValue: " + numValue;
    }

    public String getDescription() {
        if (ty == END) {
            return "end of expression";
        }
        return String.valueOf(ty);
    }
}

/**
 * This is a Module which outputs an expression applied to three numbers.
 */
@Slf4j
@ProceduralModule.Category("Modules:menu.functions")
public class ExprModule extends ProceduralModule<ExprModule> {

    private Map<String, OPort> varTable;
    private Module[] inputModules;
    private Module[] myModules;
    private List<Module> moduleVec;
    OPort compiled;
    Token[] tokens;
    Token currTok;
    int tokIdx;

    private String expr;
    private List<String> errors;

    public ExprModule() {
        this(new Point());
    }

    public ExprModule(Point position) {
        super("expr", new IOPort[]{new NumericInputPort(IOPort.LEFT, "Value 1", "(0)"),
            new NumericInputPort(IOPort.LEFT, "Value 2", "(0)"),
            new NumericInputPort(IOPort.LEFT, "Value 3", "(0)")},
                new IOPort[]{new IOPort(IOPort.NUMBER, IOPort.OUTPUT, IOPort.RIGHT, "Result")},
                position);
        inputModules = linkFrom;
        setExpr("x");
        layout();
    }

    @Override
    public final void init(PointInfo p) {

        for (int i = myModules.length - 1; i >= 0; i--) {
            if (myModules[i] == null) {
                log.atDebug().log("There's a null module in the module list at {} of {}, skipping", i, myModules.length);
            }
            myModules[i].init(p);
        }
    }

    @Override
    public final double getAverageValue(int which, double blur) {
        return compiled.module.getAverageValue(compiled.oport, blur);
    }

    @Override
    public final double getValueError(int which, double blur) {
        return compiled.module.getValueError(compiled.oport, blur);
    }

    @Override
    public final void getValueGradient(int which, Vec3 grad, double blur) {
        compiled.module.getValueGradient(compiled.oport, grad, blur);
    }

    @Override
    public void setInput(IOPort which, IOPort port) {
        super.setInput(which, port);
        inputModules = linkFrom;
        initVarTable();
        compile();
    }

    /* Write out the expression */
 /* Allow the user to set the parameters. */
    @Override
    public boolean edit(final ProcedureEditor editor, Scene theScene) {
        final BTextField exprField = new BTextField(expr, 40);
        exprField.addEventLink(ValueChangedEvent.class, new Object() {
            void processEvent() {
                try {
                    setExpr(exprField.getText().toLowerCase());
                    editor.updatePreview();
                } catch (Exception ex) {
                    // Ignore.
                }
            }
        });
        ComponentsDialog dlg = new ComponentsDialog(editor.getParentFrame(), "Set Expression:", new Widget[]{exprField},
                new String[]{"Calculate:"});
        if (!dlg.clickedOk()) {
            return false;
        }
        errors.clear();
        try {
            setExpr(exprField.getText().toLowerCase());
        } catch (Exception ex) {
            addError("The expression could not be evaluated.");
        }
        if (!errors.isEmpty()) {
            displayErrors(editor.getParentFrame());
            return edit(editor, theScene);
        }
        errors.clear();
        layout();
        return true;
    }

    /* Create a duplicate of this module. */
    @Override
    public ExprModule duplicate() {
        ExprModule mod = new ExprModule(new Point(bounds.x, bounds.y));

        mod.setExpr(expr);
        return mod;
    }

    @Override
    public void writeToStream(DataOutputStream out, Scene theScene) throws IOException {
        out.writeUTF(expr);
    }

    public final void setExpr(String e) {
        expr = e;
        name = "[" + expr + "]";
        lex(expr);
        compile();
    }

    /* Read in the expression. */
    @Override
    public void readFromStream(DataInputStream in, Scene theScene) throws IOException {
        inputModules = linkFrom;
        setExpr(in.readUTF());
        layout();
    }

    public final boolean isCompiled() {
        return !(compiled == null);
    }

    void addToken(Token token) {
        log.atDebug().log("Adding token {}", token);

        if (tokIdx >= tokens.length) {
            Token[] oldtokens = tokens;
            tokens = new Token[tokens.length * 2];
            System.arraycopy(oldtokens, 0, tokens, 0, tokens.length);
        }
        tokens[tokIdx++] = token;
        currTok = token;
    }

    void lex(String str) {
        tokIdx = 0;
        tokens = new Token[100];
        StringTokenizer st = new StringTokenizer(expr, "+-/*%;=,()^ ", true);

        String tok = " ";
        boolean get = true;
        while (!get || st.hasMoreTokens()) {
            if (get) {
                tok = st.nextToken();
            } else {
                get = true;
            }

            char c = tok.charAt(0);
            if (c != ' ') {
                switch (c) {
                    case ';':
                    case '(':
                    case '/':
                    case '+':
                    case '%':
                    case '^':
                    case ',':
                    case ')':
                    case '*':
                    case '-':
                    case '=':
                        addToken(new Token(c));
                        break;
                    case '.':
                    case '0':
                    case '1':
                    case '2':
                    case '3':
                    case '4':
                    case '5':
                    case '6':
                    case '7':
                    case '8':
                    case '9':
                        addToken(new Token(Token.NUMBER));
                        currTok.numValue = Double.parseDouble(tok);
                        break;
                    default:
                        addToken(new Token(Token.VARIABLE));
                        String name = tok;
                        get = true;
                        while (get && st.hasMoreTokens()) {
                            tok = st.nextToken();
                            c = tok.charAt(0);
                            if (c != ' ') {
                                get = false;
                            }
                        }

                        if (c == Token.LP) {
                            currTok.ty = Token.FUNCTION;
                        }
                        currTok.strValue = name;
                        break;
                }
            }
        }
        /*
        if (!currTok.equals (Token.RET)) {
           addToken (new Token (Token.RET));
        }
         */
        currTok = new Token(Token.RET);
        tokIdx = 0;
    }

    void initVarTable() {
        varTable = new Hashtable<>();
        moduleVec = new Vector<>();

        CoordinateModule x, y, z, t;
        x = new CoordinateModule(new Point(), CoordinateModule.X);
        y = new CoordinateModule(new Point(), CoordinateModule.Y);
        z = new CoordinateModule(new Point(), CoordinateModule.Z);
        t = new CoordinateModule(new Point(), CoordinateModule.T);

        varTable.put("x", new OPort(x));
        varTable.put("y", new OPort(y));
        varTable.put("z", new OPort(z));
        varTable.put("t", new OPort(t));

        for (int i = 0; i < inputModules.length; i++) {
            if (inputModules[i] != null) {
                OPort inp = new OPort(inputModules[i], linkFromIndex[i]);
                varTable.put("input" + (i + 1), inp);
            } else {
                varTable.put("input" + (i + 1), createNumberPort(0));
            }
        }

        varTable.put("e", createNumberPort(Math.E));
        varTable.put("pi", createNumberPort(Math.PI));

    }

    void compile() {
        clearModules();
        tokIdx = 0;
        initVarTable();
        getToken();
        compiled = expr(false);
        myModules = moduleVec.toArray(new Module[0]);
        if (compiled == null) {
            compiled = new OPort(new NumberModule(new Point(), 0.0), 0);
        }
        log.atDebug().log("Compiled: {}", compiled);
    }

    OPort expr(boolean get) {
        OPort left = term(get);
        for (;;) {
            switch (currTok.ty) {
                case Token.PLUS:
                    left = binOp(SumModule.class, left, term(true));
                    break;
                case Token.MINUS:
                    left = binOp(DifferenceModule.class, left, term(true));
                    break;
                default:
                    return left;
            }
        }
    }

    void addModule(Module m) {
        if (m == null) {
            log.atDebug().log("I don't want to add a null module to the module list at position {}", moduleVec.size());
            try {
                m.init(null); //error!
            } catch (Exception e) {
                log.atError().setCause(e).log("Init error: {}", e.getMessage());
            }

        } else if (!moduleVec.contains(m)) {
            log.atDebug().log("Adding module {} to the module list at position {}", m, moduleVec.size());
            moduleVec.add(m);
        }
    }

    void clearModules() {
        myModules = new Module[0];
    }

    OPort term(boolean get) {
        OPort left = exponent(get);
        for (;;) {
            switch (currTok.ty) {
                case Token.MUL:
                    left = binOp(ProductModule.class, left, exponent(true));
                    break;
                case Token.DIV:
                    left = binOp(RatioModule.class, left, exponent(true));
                    break;
                case Token.MOD:
                    left = binOp(ModModule.class, left, exponent(true));
                    break;
                default:
                    return left;
            }
        }
    }

    OPort exponent(boolean get) {
        OPort left = prim(get);

        for (;;) {
            if (currTok.ty == Token.EXP) {
                OPort right = prim(true);
                OPort pow = getOPort("pow");
                IOPort[] inp = pow.module.getInputPorts();
                IOPort inport = inp[pow.args[0].iport];
                pow.module.setInput(inport, left.getOPort());
                inport = inp[pow.args[1].iport];
                pow.module.setInput(inport, right.getOPort());
                addModule(pow.module);
                left = pow;
            } else {
                return left;
            }
        }
    }

    OPort prim(boolean get) {
        OPort port;

        if (get) {
            getToken();
        }

        switch (currTok.ty) {
            case Token.NUMBER:
                port = createNumberPort(currTok.numValue);
                getToken();
                return port;
            case Token.FUNCTION:
                Token name = currTok;
                getToken(); //eat LP
                getToken(); //load first arg
                return function(name.strValue);

            case Token.VARIABLE:
                if (currTok.strValue == null) {
                    log.debug("No variable: {}", currTok.strValue);
                    return null;
                }
                port = varTable.get(currTok.strValue);
                if (port == null) {
                    addError("There was no value assigned variable " + currTok.strValue + ".");
                    port = createNumberPort(0.0f);
                }
                addModule(port.module);
                getToken();
                return port;

            case Token.MINUS:
                return binOp(DifferenceModule.class, createNumberPort(0.0), prim(true));
            case Token.LP:
                port = expr(true);
                if (currTok.ty != Token.RP) {
                    addError("Missing ).  Found " + currTok.getDescription() + " instead.");
                }
                getToken();
                return port;
            default:
                addError("Found " + currTok.getDescription() + " where a number or variable was expected.");
                return null;
        }
    }

    Token getToken() {
        Token tok = tokens[tokIdx++];
        if (tok == null) {
            tok = new Token(Token.END);
        }

        currTok = tok;
        return tok;
    }

    OPort function(String name) {

        OPort func = getOPort(name);

        List<OPort> s = new Vector<>();
        //get args
        while (currTok.ty != Token.RP && currTok.ty != Token.END) {
            s.add(expr(false));
            //skip comma
            if (currTok.ty == Token.COMMA) {
                getToken();
            }
        }
        getToken(); // eat RP

        if (s.size() != func.args.length) {
            addError(name + " expects " + func.args.length + " arguments, but you called it with " + s.size() + ".");

            /*            for (int i = 0; i < func.args.length; i ++) {
                System.out.print (func.args [i] + ", ");
            } */
            return null;
        }

        for (int i = 0; i < s.size(); i++) {
            OPort arg = s.get(i);
            IOPort[] inp = func.module.getInputPorts();
            IOPort inport = inp[func.args[i].iport];
            func.module.setInput(inport, arg.getOPort());
            addModule(arg.module);
        }
        addModule(func.module);
        return func;

    }

    OPort getOPort(String name) {
        //name = "artofillusion.procedural." +name;
        if (!Token.funMap.containsKey(name)) {
            log.atDebug().log("No such function: {}", name);
            return null;
        }
        OPort op = Token.funMap.get(name);
        return new OPort(op.module.duplicate(), op.oport, op.args);
    }

    //no need to add NumberModules to module list - they don't depend
    //on the point
    OPort createNumberPort(double v) {
        return new OPort(new NumberModule(new Point(), v));
    }

    OPort binOp(Class<?> parentClass, OPort left, OPort right) {
        var parentM = ExprModule.createModule(parentClass);
        Arg[] args = {new Arg("Arg1", 0), new Arg("Arg1", 1)};
        OPort parent = new OPort(parentM);
        parent.args = args;

        IOPort[] inp = parentM.getInputPorts();
        IOPort inport = inp[parent.args[0].iport];
        parentM.setInput(inport, left.getOPort());

        inport = inp[parent.args[1].iport];
        parentM.setInput(inport, right.getOPort());
        /*
        link (parent, left, 0);
        link (parent, right, 1);*/
        log.atDebug().log("Creating binOp: {} ({}, {})", parentClass.getName(), left, right);
        addModule(parentM);
        return parent;//new OPort (parent);
    }

    void link(OPort consumer, OPort producer, int inIdx) {
        IOPort[] inp = consumer.module.getInputPorts();
        IOPort inport = inp[consumer.args[inIdx].iport];
        consumer.module.setInput(inport, producer.getOPort());
    }

    /* Add a message to the list of errors. */
    private void addError(String msg) {
        if (errors == null) {
            System.err.println(msg);
        } else {
            errors.add(msg);
        }
    }

    /* Display the error messages in a dialog. */
    //TODO Use generic errors displayer
    private void displayErrors(BFrame fr) {
        String[] msg = new String[errors.size() + 1];

        msg[0] = "Your expression contains the following errors:";
        for (int i = 0; i < errors.size(); i++) {
            msg[i + 1] = errors.get(i);
        }
        new BStandardDialog("", msg, BStandardDialog.INFORMATION).showMessageDialog(fr);
    }

    private static Module createModule(Class<?> moduleClass) {

        try {
            Constructor<?> cons = moduleClass.getConstructor(Point.class);
            return (Module) cons.newInstance(new Point());
        } catch (ReflectiveOperationException | SecurityException e) {
            log.atError().setCause(e).log("Unable to create module: {}: {}", moduleClass.getName(), e.getMessage());
            return new NumberModule(new Point(), 0.1234567);
        }
    }
}

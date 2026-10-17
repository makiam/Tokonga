.. _beanshell_migration:

Migrating Scripts From BeanShell
################################

Art of Illusion currently supports two scripting languages, BeanShell and Groovy, but BeanShell support is going to
be removed in the near future: Groovy will then be the only scripting language. There is no automatic converter, but
Groovy understands most of the Java-like syntax that BeanShell accepted, so for many scripts a rename is all that is
needed. Groovy also compiles scripts to Java bytecode, so a converted script normally runs much faster than it did
before.

This chapter describes how to convert existing BeanShell scripts and scripted objects to Groovy. Converting them in
advance is safe: the current version still runs both languages, so a converted script keeps working. The scripting
feature itself is described in the :ref:`Scripting<scripting>` chapter.

Where BeanShell scripts can be found
************************************

* **Scripts/Tools** - tool scripts, listed in the **Tools -> Scripts** menu.
* **Scripts/Objects** - object scripts, listed when you create a scripted object.
* **Scripts/Startup** - startup scripts, executed every time AoI starts.
* Inside saved scene files (``.aoi``): every scripted object stores the script text *and* the language it was written
  in.

Once BeanShell support is removed, files with the ``.bsh`` extension will no longer be listed or executed. Nothing is
deleted automatically: the script text stays in the scene file, so a scene can still be opened afterwards, and an
older build can be used if a script has to be run in the meantime. Because of that, converting a scripted object is
not urgent, but it should be done before BeanShell support is removed.

Converting a script file
************************

#. Make a copy of the file and rename it from ``.bsh`` to ``.groovy``.
#. Keep the top of the script as it is. AoI supplies the entry point variables in both languages: ``window``
   (the scene window) for tool and startup scripts, ``script`` (the scripted object controller) for object scripts,
   and ``out`` (the script output stream). You never declare the entry point method yourself.
#. Go through the script and fix the differences listed in the next sections.
#. Put the converted file back into the same directory. Startup scripts are executed the next time AoI is launched.
#. Test it in the script editor: **Tools -> Edit Tool Script...**, then **Load**, **Run**. Anything printed by the
   script appears in the **Script Output** window.

Converting scripted objects inside a scene
******************************************

#. Open the scene and find the object whose script was written in BeanShell.
#. Open the object's script editor (double click the object, or use its editor window).
#. Convert the script text as described in this chapter and set the **Language** box to *Groovy*.
#. Click **OK** and save the scene. The scene now stores *Groovy* for that object.

As long as an object still says *BeanShell*, it keeps working in the current version. Once BeanShell support is
removed, such an object can no longer be evaluated, because the script engine for it no longer exists. The script text
itself is not lost, so an object can be converted at any time, or opened in an older build.

Syntax compared
***************

Most constructs are spelled the same way in both languages. The table lists the ones that are worth a look while
converting.

+----------------------------------------------+-------------------------------------------------------------+
| BeanShell                                    | Groovy                                                      |
+==============================================+=============================================================+
| ``int n = scene.getNumObjects();``           | ``int n = scene.getNumObjects()`` (semicolons optional)     |
+----------------------------------------------+-------------------------------------------------------------+
| ``n = 5;`` (loose, untyped)                  | ``def n = 5`` (also works untyped, ``def`` is recommended)  |
+----------------------------------------------+-------------------------------------------------------------+
| ``print()`` (empty line)                     | ``println()`` - Groovy has no ``print()`` without arguments |
+----------------------------------------------+-------------------------------------------------------------+
| ``print(x);``                                | ``print(x)`` or ``println(x)``                              |
+----------------------------------------------+-------------------------------------------------------------+
| ``"value: " + x``                            | same, or ``"value: ${x}"``                                  |
+----------------------------------------------+-------------------------------------------------------------+
| ``for (int i = 0; i < n; i++) { ... }``      | same, or ``for (i in 0..<n) { ... }``                       |
+----------------------------------------------+-------------------------------------------------------------+
| ``list.size();``                             | ``list.size()`` (``list.size`` does *not* work)             |
+----------------------------------------------+-------------------------------------------------------------+
| ``new Vec3[] {v1, v2}``                      | same (a brace initializer needs an explicit ``new``)        |
+----------------------------------------------+-------------------------------------------------------------+
| ``Vec3[] v = {v1, v2}`` (no ``new``)         | ``Vec3[] v = [v1, v2]`` (list converted to array)           |
+----------------------------------------------+-------------------------------------------------------------+
| ``new Runnable() { ... }`` (anonymous class) | same, or a closure ``{ ... }``                              |
+----------------------------------------------+-------------------------------------------------------------+
| ``import artofillusion.*;``                  | same (the same packages are imported automatically)         |
+----------------------------------------------+-------------------------------------------------------------+

Arrays and lists
****************

The Java form with an explicit ``new`` works unchanged in both languages::

   Vec3[] row = new Vec3[] {v1, v2}

The brace initializer *without* ``new`` is accepted by BeanShell, but Groovy rejects it: ``Vec3[] row = {v1, v2}``
fails, because a brace is parsed as a closure in Groovy. Write a list literal instead: it is converted to the declared
array type::

   Vec3[] row = [v1, v2]
   Vec3[][] grid = [row, row]
   float[] smoothness = [1.0, 1.0]

The conversion happens only when the target type is known from the context. On its own a list literal stays a
``List``, and a ``List`` is not accepted where a method or constructor expects an array::

   def grid = [[v1, v2]]               // a List of Lists, not a Vec3[][]
   new SplineMesh(grid, ...)           // fails - the constructor wants arrays

Declare the variable with the array type, which is usually the smallest change, or convert explicitly with ``as``::

   Vec3[][] grid = [[v1, v2]]          // converted on assignment
   new SplineMesh([[v1, v2]] as Vec3[][], ...)

Differences that can change behaviour
*************************************

These are the cases where a script may still compile, but do something different from what it did before.

**Integer division.** In Groovy ``/`` always produces a decimal result, even for two integers: ``7/2`` is ``3.5``
(``BigDecimal``), not ``3``. Use ``7.intdiv(2)``, cast with ``(int)(a/b)``, or keep the values as ``double``.
The ``%`` operator and comparisons such as ``7/2 == 3.5`` behave as expected.

**Division by zero.** Groovy throws ``ArithmeticException`` for division by zero with integers *and* with ``double``
values (``7/0``, ``7.0/0.0``), where Java-style code often relies on ``double`` division producing ``Infinity``.
Arithmetic with ``float`` values keeps the Java behaviour (``7f/0f`` is ``Infinity``), but its result is a ``double``
(``1.0f + 1.0f`` is a ``Double``).

**Comparisons with NaN.** Groovy compares floating point numbers by ``Double.compareTo``, which makes ``Double.NaN``
"greater than everything": ``Double.NaN == Double.NaN`` is true, ``Double.NaN != Double.NaN`` is false,
``1.0 < Double.NaN`` is true and ``Double.NaN > 1.0`` is true. In Java and BeanShell, by contrast, ``==`` is false,
``!=`` is true and every ordering comparison is false. The usual Java idiom for testing NaN, ``x != x``, therefore no
longer works: use ``Double.isNaN(x)``.

**Comparing values.** Groovy ``==`` compares values rather than references - numbers are compared numerically
(``1 == 1L`` is true) and other objects through ``equals()`` - so equal strings and numbers test equal even when they
are different objects (``===`` tests identity). Comparing computed decimals for exact equality is still unreliable -
``1/3 == 0.3333333333333333`` is false - so compare floating point results with a tolerance.

**Truth of a condition.** Groovy treats ``0``, ``0.0``, an empty string, an empty collection and ``null`` as false.
Write explicit comparisons (``if (count > 0)``) if the intent is not obvious.

**Implicit return values.** A method or closure returns the value of its last expression, even without ``return``.
A method that ends without producing a value returns ``null``, and using it in arithmetic fails with
*Ambiguous method overloading*. Add an explicit ``return`` when the value matters.

**Property shorthand.** ``object.property`` works only for real getters (``getProperty()`` / ``isProperty()``) and
public fields. Methods keep their parentheses: ``list.size()``, not ``list.size``.

**Stricter method lookup.** Groovy resolves method calls dynamically, but the method has to exist and the argument
types have to be compatible: a call that BeanShell resolved loosely can raise ``MissingMethodException``.

**BeanShell-only API.** Anything from the BeanShell library has no equivalent and must be rewritten:
``bsh.*`` classes, ``this.interpreter``, ``source()``, ``eval()``, ``set()``, ``get()``, ``unset()``, and the loose
typing tricks built around them.

Groovy features you can use
***************************

Once a script runs, it is worth knowing what Groovy adds. All of this is optional.

* String interpolation: ``"radius ${r} at time ${script.getTime()}"``.
* Collections and closures: ``[1, 2, 3].collect { it * 2 }``, ``.findAll { }``, ``.each { }``, ``.sort { }``,
  map literals ``[x: 1, y: 2]``.
* Ranges: ``(1..10)``, ``(0..<n)``.
* Safe navigation ``obj?.getName()`` and the Elvis operator ``value ?: fallback``.
* The power operator ``**`` (``10**2``) and explicit conversions with ``as`` (``[1, 2] as int[]``).
* Optional semicolons, optional types, string multiplication (``"-" * 20``), regular expression operators.

Groovy scripts also have three helper variables provided by AoI: ``log`` writes to the application log
(``log.info("Value is {}", 3)``), while ``undo()`` and ``redo()`` perform the corresponding action on the current
scene window and therefore make sense in tool scripts.

Examples
********

A tool script that reports the size of the scene. BeanShell version:

.. code-block:: java

   int n = scene.getNumObjects();
   print("Objects in scene: " + n);

Groovy version - in this case only the call to ``print`` had to be changed:

.. code-block:: groovy

   int n = scene.getNumObjects()
   println "Objects in scene: " + n

An object script that builds a sphere growing with the animation time. BeanShell version:

.. code-block:: java

   double r = 1.0 + 0.5*Math.sin(script.getTime());
   script.addObject(new Sphere(r, r, r), script.getCoordinates());

Groovy version - the script runs as it is, the ``def`` is only a matter of style:

.. code-block:: groovy

   def r = 1.0 + 0.5 * Math.sin(script.getTime())
   script.addObject(new Sphere(r, r, r), script.getCoordinates())

A script that needs a real fix. BeanShell version:

.. code-block:: java

   int half = count/2;
   total = half + 1;

Groovy version - ``count/2`` would produce ``0.5`` for ``count == 1``, so integer division has to be requested
explicitly:

.. code-block:: groovy

   def half = count.intdiv(2)
   def total = half + 1

See also
********

* :ref:`Scripting<scripting>` - the scripting feature itself, including the **Script Output** window.
* The scripting tutorial on the Art of Illusion website for worked examples of object and tool scripts.
* The `Scripting forum <https://sourceforge.net/p/aoi/discussion/228117/>`_ of the Art of Illusion project, where
  converted scripts and questions about them are often posted.

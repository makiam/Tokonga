/* Copyright (C) 2011 by Peter Eastman
   Changes copyright (C) 2022-2023 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.procedural;

import artofillusion.*;
import artofillusion.ui.*;
import buoy.event.*;
import buoy.widget.*;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

/**
 * This is the menu that appears in the procedure editor window.  It displays modules organized into categories,
 * and allows the user to add them to their procedure.
 */

public class ModuleMenu extends CustomWidget
{
  private final ProcedureEditor editor;
  private final List<Category> categories = new ArrayList<>();
  private final double expandedFraction[];
  private int expandedCategory;
  private Module newModule;
  private boolean isDraggingModule;

  private final Color categoryColor = new Color(0.9f, 0.9f, 0.9f);
  private final Color selectedCategoryColor = new Color(0.7f, 0.7f, 1.0f);
  
  public ModuleMenu(ProcedureEditor editor)
  {
    this.editor = editor;
    addEventLink(RepaintEvent.class, this, "paint");
    addEventLink(MousePressedEvent.class, this, "mousePressed");
    addEventLink(MouseDraggedEvent.class, this, "mouseDragged");
    addEventLink(MouseReleasedEvent.class, this, "mouseReleased");
    
    Category category;
    categories.add(category = new Category("menu.values"));
    category.add(new Entry("menu.numberModule", NumberModule.class));
    category.add(new Entry("menu.colorModule", ColorModule.class));
    category.add(new Entry("menu.xModule", CoordinateModule.class, null, CoordinateModule.X));
    category.add(new Entry("menu.yModule", CoordinateModule.class, null, CoordinateModule.Y));
    category.add(new Entry("menu.zModule", CoordinateModule.class, null, CoordinateModule.Z));
    category.add(new Entry("menu.timeModule", CoordinateModule.class, null, CoordinateModule.T));
    if (editor.getOwner().allowViewAngle())
      category.add(new Entry("menu.viewAngleModule", ViewAngleModule.class));
    if (editor.getOwner().allowParameters())
      category.add(new Entry("menu.parameterModule", ParameterModule.class));
    category.add(new Entry("menu.commentModule", CommentModule.class));

    categories.add(category = new Category("menu.operators"));
    category.add(new Entry("menu.addModule", SumModule.class));
    category.add(new Entry("menu.subtractModule", DifferenceModule.class));
    category.add(new Entry("menu.multiplyModule", ProductModule.class));
    category.add(new Entry("menu.divideModule", RatioModule.class));
    category.add(new Entry("menu.powModule", PowerModule.class));
    category.add(new Entry("menu.modModule", ModModule.class));
    category.add(new Entry("menu.greaterThanModule", CompareModule.class));
    category.add(new Entry("menu.minModule", MinModule.class));
    category.add(new Entry("menu.maxModule", MaxModule.class));

    categories.add(category = new Category("menu.functions"));
    category.add(new Entry("menu.expressionModule", ExprModule.class));
    category.add(new Entry("menu.customFunctionModule", FunctionModule.class));
    category.add(new Entry("menu.scaleShiftModule", ScaleShiftModule.class));
    category.add(new Entry("menu.absModule", AbsModule.class));
    category.add(new Entry("menu.blurModule", BlurModule.class));
    category.add(new Entry("menu.clipModule", ClipModule.class));
    category.add(new Entry("menu.interpolateModule", InterpModule.class));
    category.add(new Entry("menu.sineModule", SineModule.class));
    category.add(new Entry("menu.cosineModule", CosineModule.class));
    category.add(new Entry("menu.sqrtModule", SqrtModule.class));
    category.add(new Entry("menu.expModule", ExpModule.class));
    category.add(new Entry("menu.logModule", LogModule.class));
    category.add(new Entry("menu.biasModule", BiasModule.class));
    category.add(new Entry("menu.gainModule", GainModule.class));
    category.add(new Entry("menu.randomModule", RandomModule.class));

    categories.add(category = new Category("menu.colorFunctions"));
    category.add(new Entry("menu.customColorFunctionModule", SpectrumModule.class));
    category.add(new Entry("menu.blendModule", BlendModule.class));
    category.add(new Entry("menu.addColorModule", ColorSumModule.class));
    category.add(new Entry("menu.subtractColorModule", ColorDifferenceModule.class));
    category.add(new Entry("menu.multiplyColorModule", ColorProductModule.class));
    category.add(new Entry("menu.lighterModule", ColorLightenModule.class));
    category.add(new Entry("menu.darkerModule", ColorDarkenModule.class));
    category.add(new Entry("menu.scaleColorModule", ColorScaleModule.class));
    category.add(new Entry("menu.RGBModule", RGBModule.class));
    category.add(new Entry("menu.HSVModule", HSVModule.class));
    category.add(new Entry("menu.HLSModule", HLSModule.class));

    categories.add(category = new Category("menu.transforms"));
    category.add(new Entry("menu.linearModule", TransformModule.class));
    category.add(new Entry("menu.polarModule", PolarModule.class));
    category.add(new Entry("menu.sphericalModule", SphericalModule.class));
    category.add(new Entry("menu.jitterModule", JitterModule.class));

    categories.add(category = new Category("menu.patterns"));
    category.add(new Entry("menu.noiseModule", NoiseModule.class));
    category.add(new Entry("menu.turbulenceModule", TurbulenceModule.class));
    category.add(new Entry("menu.gridModule", GridModule.class));
    category.add(new Entry("menu.cellsModule", CellsModule.class));
    category.add(new Entry("menu.marbleModule", MarbleModule.class));
    category.add(new Entry("menu.woodModule", WoodModule.class));
    category.add(new Entry("menu.checkerModule", CheckerModule.class));
    category.add(new Entry("menu.bricksModule", BrickModule.class));
    category.add(new Entry("menu.imageModule", ImageModule.class));
    
    List<Module> plugins = PluginRegistry.getPlugins(Module.class);
    if (!plugins.isEmpty())
    {
      final Category uncategorized = new Category("menu.plugins");
      categories.add(uncategorized);
      plugins.forEach(module -> {
          uncategorized.add(new Entry(module.getName(), module.getClass()));
      });
    }
    
    expandedFraction = new double[categories.size()];
    expandedFraction[expandedCategory] = 1.0;

    // Compute the width of the menu.

    FontMetrics metrics = Toolkit.getDefaultToolkit().getFontMetrics(editor.getFont());
    int width = 0;
    for (Category cat : categories)
    {
      width = Math.max(width, metrics.stringWidth(cat.name));
      for (Entry entry : cat.entries)
        width = Math.max(width, metrics.stringWidth(entry.name));
    }
    setPreferredSize(new Dimension(width + 20, 50));
  }

  private void paint(RepaintEvent ev)
  {
    Graphics2D g = ev.getGraphics();
    
    FontMetrics metrics = g.getFontMetrics(editor.getFont());
    int y = 0;
    int categoryHeight = metrics.getAscent()+metrics.getDescent()+10;
    int entryHeight = metrics.getAscent()+metrics.getDescent()+5;
    int width = getBounds().width;


    // Loop over categories.

    for (int i = 0; i < categories.size(); i++)
    {
      Category cat = categories.get(i);
      g.setColor(i == expandedCategory ? selectedCategoryColor : categoryColor);
      cat.bounds = new Rectangle(1, y, width, categoryHeight-2);
      g.fill3DRect(cat.bounds.x, cat.bounds.y, cat.bounds.width, cat.bounds.height, true);
      g.setColor(Color.BLACK);
      g.drawString(cat.name, (width-metrics.stringWidth(cat.name))/2,
          y+(categoryHeight/2)+(metrics.getAscent()/2));
      y += categoryHeight;
      if (expandedFraction[i] > 0)
      {
        // This category is expanded, so draw its entries.

        int endy = y+(int)(expandedFraction[i]*entryHeight*cat.entries.size());
        g.setClip(0, 0, width, endy);
        for (Entry entry : cat.entries)
        {
          g.setColor(Color.WHITE);
          Rectangle bounds = new Rectangle(0, y, width, entryHeight);
          g.fill3DRect(bounds.x, bounds.y, bounds.width, bounds.height, true);
          g.setColor(Color.BLACK);
          g.drawString(entry.name, (width-metrics.stringWidth(entry.name))/2,
              y+(entryHeight/2)+(metrics.getAscent()/2));
          y += entryHeight;
          if (i == expandedCategory)
            entry.bounds = bounds;
        }
        y = endy;
        g.setClip(null);
      }
      else
      {
        for (Entry entry : cat.entries)
          entry.bounds = null;
      }
    }
  }

  private void mousePressed(MousePressedEvent ev)
  {
    newModule = null;
    isDraggingModule = false;
    for (int i = 0; i < categories.size(); i++)
    {
      Category cat = categories.get(i);
      if (expandedCategory != i && cat.bounds.contains(ev.getPoint()))
      {
        // They clicked on a new category.  Animate collapsing the old category and expanding the new one.

        expandedCategory = i;
        new Timer(10, new ExpandAnimator()).start();
        return;
      }
      for (Entry entry : cat.entries)
      {
        if (entry.bounds != null && entry.bounds.contains(ev.getPoint()))
        {
          // They clicked on an entry, so construct a Module object.

          Object args[] = new Object[entry.args.length];
          Class<?>[] argTypes = new Class<?>[entry.args.length];
          for (int j = 0; j < args.length; j++)
          {
            args[j] = (entry.args[j] == null ? new Point() : entry.args[j]);
            argTypes[j] = args[j].getClass();
            if (argTypes[j] == Integer.class)
              argTypes[j] = Integer.TYPE;
          }
          try
          {
            newModule = entry.moduleClass.getDeclaredConstructor(argTypes).newInstance(args);
          }
          catch (Exception ex)
          {
            ex.printStackTrace();
          }
          return;
        }
      }
    }
  }

  private void mouseDragged(MouseDraggedEvent ev)
  {
    if (newModule == null)
      return;
    Point screenPoint = ev.getPoint();
    SwingUtilities.convertPointToScreen(screenPoint, getComponent());
    if (!isDraggingModule)
    {
      Point viewPoint = new Point(screenPoint);
      SwingUtilities.convertPointFromScreen(viewPoint, editor.getParent().getComponent());
      if (viewPoint.x >= 0 && viewPoint.y >= 0 && viewPoint.x < editor.getParent().getBounds().width && viewPoint.y < editor.getParent().getBounds().height)
      {
        // They dragged from the ModuleMenu into the editor, so add the Module to the procedure and begin dragging it.

        isDraggingModule = true;
        editor.saveState(false);
        Point editorPoint = new Point(screenPoint);
        SwingUtilities.convertPointFromScreen(editorPoint, editor.getComponent());
        Point modulePosition = new Point(editorPoint);
        modulePosition.x -= newModule.getBounds().width/2;
        modulePosition.y -= newModule.getBounds().height/2;
        newModule.setPosition(modulePosition.x, modulePosition.y);
        editor.addModule(newModule);
        editor.mousePressed(new MousePressedEvent(this, ev.getWhen(), ev.getModifiers(), editorPoint.x, editorPoint.y, ev.getClickCount(), ev.isPopupTrigger(), ev.getButton()));
      }
    }
    else
    {
      // Continue dragging the module.

      Point editorPoint = new Point(screenPoint);
      SwingUtilities.convertPointFromScreen(editorPoint, editor.getComponent());
      editor.mouseDragged(new MouseDraggedEvent(editor, ev.getWhen(), ev.getModifiers(), editorPoint.x, editorPoint.y));
    }
  }

  private void mouseReleased(MouseReleasedEvent ev)
  {
    if (newModule == null)
      return;
    if (!isDraggingModule)
    {
      // They just clicked on an entry, so add the Module to the procedure.

      Rectangle bounds = editor.getParent().getBounds();
      Point p = new Point((int) (0.5*bounds.width*Math.random()), (int) (0.5*bounds.height*Math.random()));
      p = SwingUtilities.convertPoint(editor.getParent().getComponent(), p, editor.getComponent());
      newModule.setPosition(p.x, p.y);
      editor.saveState(false);
      editor.addModule(newModule);
      editor.repaint();
    }
    else
    {
      // Finish dragging the Module.

      Point screenPoint = ev.getPoint();
      SwingUtilities.convertPointToScreen(screenPoint, getComponent());
      Point editorPoint = new Point(screenPoint);
      SwingUtilities.convertPointFromScreen(editorPoint, editor.getComponent());
      editor.mouseReleased();
    }
  }

  private static class Category
  {
    public String name;
    public ArrayList<Entry> entries = new ArrayList<>();
    public Rectangle bounds;

    public Category(String name)
    {
      this.name = Translate.text(name);
    }

    public void add(Entry entry)
    {
      entries.add(entry);
    }
  }

  private static class Entry
  {
    public String name;
    public Class<? extends Module> moduleClass;
    public Object[] args;
    public Rectangle bounds;

    public Entry(String name, Class<? extends Module> moduleClass)
    {
      this(name, moduleClass, new Object[] {new Point()});
    }

    public Entry(String name, Class<? extends Module> moduleClass, Object... args)
    {
      this.name = Translate.text(name);
      this.moduleClass = moduleClass;
      this.args = args;
    }
  }

  /**
   * This class animates expanding and contracting categories.
   */
  private class ExpandAnimator implements ActionListener
  {
    private double fraction = 0;
    private double initialFraction[], finalFraction[];

    public ExpandAnimator()
    {
      initialFraction = expandedFraction.clone();
      finalFraction = new double[initialFraction.length];
      finalFraction[expandedCategory] = 1;
    }

    @Override
    public void actionPerformed(ActionEvent ev)
    {
      fraction = Math.min(fraction+0.05, 1.0);
      for (int i = 0; i < expandedFraction.length; i++)
        expandedFraction[i] = fraction*finalFraction[i] + (1-fraction)*initialFraction[i];
      repaint();
      if (fraction == 1.0)
        ((Timer) ev.getSource()).stop();
    }
  }
}

/*
	Copyright (C) 2008 by Peter H. ("TroY")
    Changes copyright (C) 2025-2026 by Maksim Khramov

	This program is free software; you can redistribute it and/or modify it under the
	terms of the GNU General Public License as published by the Free Software
	Foundation; either version 2 of the License, or (at your option) any later version.

	This program is distributed in the hope that it will be useful, but WITHOUT ANY 
	WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
	PARTICULAR PURPOSE.  See the GNU General Public License for more details.
*/

package troy.procedural;

import artofillusion.procedural.*;
import artofillusion.math.*;
import artofillusion.procedural.Module;
import artofillusion.ui.*;
import artofillusion.*;
import buoy.widget.*;
import buoy.event.*;
import java.awt.*;
import java.io.*;

/**
 * A module for use in procedurals. Is able to display discrete values
 * at discrete points.
 * 
 * Based on various class by Peter Eastman, such as NumberModule.java
 * and ComponentsDialog.java
 * 
 * @author TroY
 */
@ProceduralModule.Category("Debug")
public class DebugModule extends ProceduralModule<DebugModule>
{
	//protected double X, Y, Z, T;
	
	protected final int colorSizeX = 20;
	protected final int colorSizeY = 20;
	
	protected int fontStartX = 0;
	protected int fontStartY = 0;
	
	protected RGBColor color = null;
	
	protected final BButton btnEvalNumeric = new BButton(Translate.text("Evaluate Number"));
	protected final BButton btnEvalColor = new BButton(Translate.text("Evaluate Color"));
	protected final BLabel lblNumeric  = new BLabel("-");
	protected final CustomWidget colorWidget = new CustomWidget();
	
	protected final ValueField fieldX = new ValueField(0.0, ValueField.NONE);
	protected final ValueField fieldY = new ValueField(0.0, ValueField.NONE);
	protected final ValueField fieldZ = new ValueField(0.0, ValueField.NONE);
	protected final ValueField fieldT = new ValueField(0.0, ValueField.NONE);
	
	public DebugModule()
	{
		this(new Point());
	}
	
	/** Create a simple input -> output module. */
	public DebugModule(Point position)
	{
		super("Debug", new IOPort [] {
								new IOPort(IOPort.NUMBER, IOPort.INPUT, IOPort.LEFT, "Input", "(0.0)"),
								new IOPort(IOPort.COLOR,  IOPort.INPUT, IOPort.LEFT, "Color Input", "(black)")
								},
						new IOPort [] {
								new IOPort(IOPort.NUMBER, IOPort.OUTPUT, IOPort.RIGHT, "Output", "(0.0)"),
								new IOPort(IOPort.COLOR,  IOPort.OUTPUT, IOPort.RIGHT, "Color Output", "(black)")
								},
						position);
		
		// Register listener for dialog
		btnEvalNumeric.addEventLink(CommandEvent.class, this, "evtEvalNum");
		btnEvalColor.addEventLink(CommandEvent.class, this, "evtEvalCol");
		
		// Color sample
		colorWidget.setPreferredSize(new Dimension(colorSizeX, colorSizeY));
		colorWidget.setMaximumSize(new Dimension(colorSizeX, colorSizeY));
		colorWidget.setBackground(Color.black);
	}
	
	/** Just pass any values. */
	@Override
	public double getAverageValue(int which, double blur)
	{
		if (linkFrom[0] == null)
			return 0.0;
		
		return linkFrom[0].getAverageValue(linkFromIndex[0], blur);
	}
	
	/** Just pass any values. */
	@Override
	public double getValueError(int which, double blur)
	{
		if (linkFrom[0] == null)
			return 0.0;
		
		return linkFrom[0].getValueError(linkFromIndex[0], blur);
	}
	
	/** Just pass any values. */
	@Override
	public void getValueGradient(int which, Vec3 grad, double blur)
	{
		if (linkFrom[0] == null)
		{
			grad.set(0.0, 0.0, 0.0);
			return;
		}
		else
			linkFrom[0].getValueGradient(linkFromIndex[0], grad, blur);
		
		grad.set(grad.x, grad.y, grad.z);
	}
	
	/** Just pass any values. */
	@Override
	public void getColor(int which, RGBColor c, double blur)
	{
		if (linkFrom[1] == null)
		{
			c.setRGB(0.0f, 0.0f, 0.0f);
			return;
		}
		
		linkFrom[1].getColor(which, c, blur);
	}
	
	/** Travel through the whole module-chain and init all visited modules. */
	protected void traversalAndInit(PointInfo p, Module pos)
	{
		for (Module m : pos.linkFrom)
		{
			if (m != null)
			{
				m.init(p);
				traversalAndInit(p, m);
			}
		}
	}
	
	/** Return the value for the currently plugged chain. */
	protected double getValueForPosition()
	{
		PointInfo p = new PointInfo();
		p.x = fieldX.getValue();
		p.y = fieldY.getValue();
		p.z = fieldZ.getValue();
		p.t = fieldT.getValue();
		
		p.xsize = 0.1;
		p.ysize = 0.1;
		p.zsize = 0.1;
		
		traversalAndInit(p, this);
		return getAverageValue(0, 0.0);
	}
	
	/** Calc the module's size. */
	@Override
	public void calcSize()
	{
		// Calc default size for the text part, ports and stuff
		bounds.width  = defaultMetrics.stringWidth(name) + IOPort.SIZE * 4;
		bounds.height = defaultMetrics.getMaxAscent() + defaultMetrics.getMaxDescent() + IOPort.SIZE * 4;
		
		// Add more space for the color part if needed.
		// Use IOPort.SIZE as a "natural" margin.
		if (colorSizeX + IOPort.SIZE * 5 > bounds.width)
			bounds.width = colorSizeX + IOPort.SIZE * 5;
		
		if (colorSizeY + IOPort.SIZE * 5 > bounds.height)
			bounds.height = colorSizeY + IOPort.SIZE * 5;
		
		// Save for private use
		fontStartX = bounds.x + (bounds.width - defaultMetrics.stringWidth(name)) / 2;
		fontStartY = bounds.y + (defaultMetrics.getAscent() / 2) + IOPort.SIZE * 2;
	}
	
	/** Draw the module. */
	@Override
	protected void drawContents(Graphics2D g)
	{
		// Draw regular parent
		g.setColor(Color.black);
		g.setFont(defaultFont);
		g.drawString(name, fontStartX, fontStartY);
		
		// Draw additional color info
		if (color == null)
			g.setColor(Color.black);
		else
			g.setColor(color.getColor());
		g.fillRect(bounds.x + bounds.width / 2 - colorSizeX / 2,
							fontStartY + IOPort.SIZE,
							colorSizeX, colorSizeY);
	}
	
	/** Fire up a simple dialog to set the parameters. */
	@Override
	public boolean edit(BFrame fr, Scene theScene)
	{
		Widget[] components = new Widget [] {fieldX, fieldY, fieldZ, fieldT};
		String[] labels = new String [] {"X:", "Y:", "Z:", "T:"};
		
		// Like it's done in ComponentsDialog
		FormContainer center = new FormContainer(new double[] {0.0, 1.0}, new double[6]);
		int i;
		for (i = 0; i < components.length; i++)
		{
			center.add(new BLabel(labels[i]), 0, i, new LayoutInfo(LayoutInfo.EAST, LayoutInfo.NONE, new Insets(2, 0, 2, 5), null));
			center.add(components[i], 1, i, new LayoutInfo(LayoutInfo.WEST, LayoutInfo.BOTH, new Insets(2, 0, 2, 0), null));
		}
		
		center.add(btnEvalNumeric, 0, i, new LayoutInfo(LayoutInfo.EAST, LayoutInfo.NONE, new Insets(2, 0, 2, 5), null));
		center.add(lblNumeric, 1, i, new LayoutInfo(LayoutInfo.WEST, LayoutInfo.BOTH, new Insets(2, 0, 2, 0), null));
		i++;
		
		center.add(btnEvalColor, 0, i, new LayoutInfo(LayoutInfo.EAST, LayoutInfo.NONE, new Insets(2, 0, 2, 5), null));
		center.add(colorWidget, 1, i, new LayoutInfo(LayoutInfo.WEST, LayoutInfo.BOTH, new Insets(2, 0, 2, 0), null));
		
		PanelDialog pdlg = new PanelDialog(fr, Translate.text("debugmodule:caption.panelDialog"), center);
		
		if (!pdlg.clickedOk())
			return false;
		
		// Get numerical value
		double val = getValueForPosition();
		//val *= 1e5;
		//val = Math.rint(val);
		//val /= 1e5;
		String valstring = Double.toString(val);
		if (valstring.length() > 5)
			valstring = valstring.substring(0, 4) + "...";
		name = "Debug: " + valstring;
		
		// Get the color
		color = new RGBColor();
		getColor(0, color, 0.0);
		
		layout();
		return true;
	}

	/**
	 * Called from within the dialog: Evaluates the numerical value.
	 */
	protected void evtEvalNum(CommandEvent e)
	{
		// Get the value
		lblNumeric.setText(Double.toString(getValueForPosition()));

		// Update UI
		Widget w = e.getWidget();
		while (!(w instanceof PanelDialog))
			w = w.getParent();

		((PanelDialog)w).pack();
	}

	/**
	 * Called from within the dialog: Evaluates the color value.
	 */
	protected void evtEvalCol(CommandEvent e)
	{
		// Only used to init all modules
		getValueForPosition();
		
		// Now get the color
		color = new RGBColor();
		getColor(0, color, 0.0);
		colorWidget.setBackground(color.getColor());
	}
	
	/**
	 * Copy'n'paste. A manual refresh is needed because this class can't
	 * assure all necessary modules are already connected.
	 */
	@Override
	public DebugModule duplicate()
	{
		DebugModule mod = new DebugModule(new Point(bounds.x, bounds.y));
		mod.fieldX.setValue(fieldX.getValue());
		mod.fieldY.setValue(fieldY.getValue());
		mod.fieldZ.setValue(fieldZ.getValue());
		mod.fieldT.setValue(fieldT.getValue());
		mod.name = "Debug: <" + Translate.text("debugmodule:lbl.refresh") + ">";
		return mod;
	}
	
	/**
	 * Save it.
	 */
	@Override
	public void writeToStream(DataOutputStream out, Scene theScene) throws IOException
	{
		out.writeDouble(fieldX.getValue());
		out.writeDouble(fieldY.getValue());
		out.writeDouble(fieldZ.getValue());
		out.writeDouble(fieldT.getValue());
	}
	
	/**
	 * Read it. A manual refresh is needed because this class can't
	 * assure all necessary modules are already connected.
	 */
	@Override
	public void readFromStream(DataInputStream in, Scene theScene) throws IOException
	{
		fieldX.setValue(in.readDouble());
		fieldY.setValue(in.readDouble());
		fieldZ.setValue(in.readDouble());
		fieldT.setValue(in.readDouble());
		name = "Debug: <" + Translate.text("debugmodule:lbl.refresh") + ">";
		layout();
	}
}

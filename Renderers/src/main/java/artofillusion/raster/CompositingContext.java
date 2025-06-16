/* Copyright (C) 2007-2008 by Peter Eastman
   Changes copyright (C) 2023-2025 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY 
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.raster;

import artofillusion.*;
import artofillusion.material.*;
import artofillusion.math.*;
import artofillusion.texture.*;
import java.util.*;

/**
 * This class holds temporary information used while compositing fragments to form the final
 * image. One instance of it is created for every worker thread.
 */
public class CompositingContext {

    public Vec3[] tempVec;
    public TextureSpec surfSpec = new TextureSpec();
    public MaterialSpec matSpec = new MaterialSpec();
    public Camera camera;
    public RGBColor addColor = new RGBColor();
    public RGBColor multColor = new RGBColor();
    public RGBColor subpixelMult = new RGBColor();
    public RGBColor subpixelColor = new RGBColor();
    public RGBColor totalColor = new RGBColor();
    public RGBColor totalTransparency = new RGBColor();
    public List<ObjectMaterialInfo> materialStack = new ArrayList<>();

    public CompositingContext(Camera camera) {
        this.camera = (camera == null ? null : camera.duplicate());

        tempVec = new Vec3[4];
        for (int i = 0; i < tempVec.length; i++) {
            tempVec[i] = new Vec3();
        }

    }

    /**
     * This is called when rendering is finished. It nulls out fields to help garbage collection.
     */
    public void cleanup() {
        tempVec = null;
        surfSpec = null;
        matSpec = null;
        camera = null;
        addColor = null;
        multColor = null;
        subpixelMult = null;
        subpixelColor = null;
        totalColor = null;
        totalTransparency = null;
        materialStack = null;
    }
}

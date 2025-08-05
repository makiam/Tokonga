/* Copyright (C) 2025 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.animation;

import artofillusion.LayoutWindow;
import artofillusion.Scene;
import org.junit.jupiter.api.DisplayName;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

@DisplayName("Dummy Track")
class DummyTrack extends Track<DummyTrack> {

    @Override
    public void edit(LayoutWindow win) {

        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void apply(double time) {

        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public DummyTrack duplicate(Object parent) {

        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void copy(DummyTrack tr) {

        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public double[] getKeyTimes() {

        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int moveKeyframe(int which, double time) {

        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void deleteKeyframe(int which) {

        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isNullTrack() {

        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void writeToStream(DataOutputStream out, Scene scene) throws IOException {
    }

    @Override
    public void initFromStream(DataInputStream in, Scene scene) throws IOException {
    }
}

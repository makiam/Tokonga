/* Copyright (C) 2025-2026 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.animation;

import artofillusion.LayoutWindow;
import artofillusion.Scene;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class TrackGraphTest {

    static final Score score = Mockito.mock(Score.class);
    static final LayoutWindow layout = Mockito.mock(LayoutWindow.class);
    static final Scene scene = Mockito.mock(Scene.class);

    @BeforeAll
    public static void setUpClass() {
        Mockito.when(score.getStartTime()).thenReturn(0.0 );
        Mockito.when(score.getScale()).thenReturn(1.0 );

        Mockito.when(layout.getScene()).thenReturn(scene);
        Mockito.when(scene.getFramesPerSecond()).thenReturn(30);
    }

    @Test
    void selectionChangedForEmptyTracksAndNoKeyframes() {
        Mockito.when(score.getSelectedKeyframes()).thenReturn(new SelectionInfo[0]);
        TrackGraph g = new TrackGraph(layout, score);
        g.setTracks(new Track[0]);
        g.selectionChanged();
        TrackGraph.TrackInfo[] tracks = g.getTracks();
        Assertions.assertEquals(0, tracks.length);
    }
}

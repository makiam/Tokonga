package artofillusion.animation;

import artofillusion.LayoutWindow;
import artofillusion.Scene;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class TrackGraphTest {

    static Score score = Mockito.mock(Score.class);
    static LayoutWindow layout = Mockito.mock(LayoutWindow.class);
    static Scene scene = Mockito.mock(Scene.class);

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

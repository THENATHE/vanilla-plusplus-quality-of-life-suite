package suitehudqa;
import java.util.ArrayList;
import java.util.List;
public final class HudObservation {
    public static final List<int[]> details = new ArrayList<>();
    public static void text(int x,int y,int width,int height) { details.add(new int[]{x,y,width,height}); }
}

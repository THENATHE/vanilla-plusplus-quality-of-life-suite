package suitehudqa;
import java.util.ArrayList;
import java.util.List;
import org.joml.Matrix3x2fc;
import org.joml.Vector2f;
public final class HudObservation {
    public static boolean inDetails;
    public static boolean inMap;
    public static boolean inMapCanvas;
    public static final List<int[]> details = new ArrayList<>();
    public static final List<float[]> mapLabelBounds = new ArrayList<>();
    public static final List<float[]> mapBounds = new ArrayList<>();
    public static final List<float[]> detailDrawBounds = new ArrayList<>();
    public static void text(int x,int y,int width,int height) { details.add(new int[]{x,y,width,height}); }
    public static void map(Matrix3x2fc pose, float left, float top, float right, float bottom) {
        mapBounds.add(bounds(pose, left, top, right, bottom));
    }
    public static void detail(Matrix3x2fc pose, float left, float top, float right, float bottom) {
        detailDrawBounds.add(bounds(pose, left, top, right, bottom));
    }
    private static float[] bounds(Matrix3x2fc pose, float left, float top, float right, float bottom) {
        float minX = Float.POSITIVE_INFINITY, minY = Float.POSITIVE_INFINITY;
        float maxX = Float.NEGATIVE_INFINITY, maxY = Float.NEGATIVE_INFINITY;
        for (float x : new float[]{left, right}) {
            for (float y : new float[]{top, bottom}) {
                Vector2f point = pose.transformPosition(x, y, new Vector2f());
                minX = Math.min(minX, point.x); minY = Math.min(minY, point.y);
                maxX = Math.max(maxX, point.x); maxY = Math.max(maxY, point.y);
            }
        }
        return new float[]{minX, minY, maxX, maxY};
    }
}

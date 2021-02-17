package sk.henrichg.phoneprofilesplus;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.Overlay;

public class CurrentLocationOverlayOSM extends Overlay {

    //private Paint paint = new Paint();
    private Paint accuracyPaint = new Paint();
    private final int colorFill;
    private final int colorStroke;
    private final GeoPoint location;
    private final Point screenCoords = new Point();
    private final float accuracy;

    public CurrentLocationOverlayOSM(GeoPoint location, float accuracyInMeters, int colorFill, int colorStoke) {
        super();

        this.location = location;
        this.accuracy = accuracyInMeters;
        this.colorFill = colorFill;
        this.colorStroke = colorStoke;
        this.accuracyPaint.setStrokeWidth(5);
        this.accuracyPaint.setAntiAlias(true);
    }

    @Override
    public void onDetach(MapView view){
        //paint = null;
        accuracyPaint = null;
    }

    @Override
    public void draw(final Canvas c, final MapView map, final boolean shadow) {

        if (shadow) {
            return;
        }

        if (location != null) {
            final Projection pj = map.getProjection();
            pj.toPixels(location, screenCoords);

            if (accuracy > 0) {  //Set this to a minimum pixel size to hide if accuracy high enough
                final float accuracyRadius = pj.metersToEquatorPixels(accuracy);

                /* Draw the inner shadow. */
                accuracyPaint.setAntiAlias(false);
                accuracyPaint.setColor(colorFill);
                accuracyPaint.setAlpha(80);
                accuracyPaint.setStyle(Paint.Style.FILL);
                c.drawCircle(screenCoords.x, screenCoords.y, accuracyRadius, accuracyPaint);

                /* Draw the edge. */
                accuracyPaint.setAntiAlias(true);
                accuracyPaint.setColor(colorStroke);
                accuracyPaint.setAlpha(255);
                accuracyPaint.setStyle(Paint.Style.STROKE);
                c.drawCircle(screenCoords.x, screenCoords.y, accuracyRadius, accuracyPaint);
            }
        }
    }
}
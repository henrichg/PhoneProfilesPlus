package sk.henrichg.phoneprofilesplus;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Paint.Style;
import android.graphics.Path.Direction;
import android.text.Layout;
import android.text.Spanned;
import android.text.style.LeadingMarginSpan;

@SuppressWarnings("unused")
public final class ImprovedBulletSpan implements LeadingMarginSpan {
    private Path mBulletPath;
    private final int bulletRadius;
    private final int gapWidth;
    private final int color;

    public int getLeadingMargin(boolean first) {
        return 2 * this.bulletRadius + this.gapWidth;
    }

    public void drawLeadingMargin(Canvas canvas, Paint paint, int x, int dir,
                                  int top, int baseline, int bottom, CharSequence text,
                                  int start, int end, boolean first, Layout layout) {

        if (((Spanned)text).getSpanStart(this) == start) {
            Style style = paint.getStyle();
            paint.setStyle(Style.FILL);

            float yPosition;
            if (layout != null) {
                int line = layout.getLineForOffset(start);
                yPosition = (float)layout.getLineBaseline(line) - (float)this.bulletRadius * 2.0F;
            } else {
                yPosition = (float)(top + bottom) / 2.0F;
            }

            float xPosition = (float)(x + dir * this.bulletRadius);

            if (canvas.isHardwareAccelerated()) {
                if (this.mBulletPath == null) {
                    this.mBulletPath = new Path();
                    this.mBulletPath.addCircle(0.0F, 0.0F, (float)this.bulletRadius, Direction.CW);
                }

                canvas.save();
                canvas.translate(xPosition, yPosition);
                canvas.drawPath(this.mBulletPath, paint);
                canvas.restore();
            } else {
                canvas.drawCircle(xPosition, yPosition, (float)this.bulletRadius, paint);
            }

            paint.setStyle(style);
        }

    }

    public final int getBulletRadius() {
        return this.bulletRadius;
    }

    public final int getGapWidth() {
        return this.gapWidth;
    }

    public final int getColor() {
        return this.color;
    }

    ImprovedBulletSpan(int bulletRadius, int gapWidth, int color) {
        this.bulletRadius = bulletRadius;
        this.gapWidth = gapWidth;
        this.color = color;
    }

}

package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.Layout;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.util.Patterns;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.TextView;

// Source: https://stackoverflow.com/a/37205216/2863059
public class PPLinkMovementMethod extends LinkMovementMethod {


    //private final String TAG = PPLinkMovementMethod.class.getSimpleName();

    private final OnPPLinkMovementMethodListener mListener;
    private final GestureDetector mGestureDetector;
    private final Context mContext;
    private TextView mWidget;
    private Spannable mBuffer;

    public enum LinkType {

        /**
         * Indicates that phone link was clicked
         */
        PHONE,

        /**
         * Identifies that URL was clicked
         */
        WEB_URL,

        /**
         * Identifies that Email Address was clicked
         */
        EMAIL_ADDRESS,

        /**
         * Indicates that none of above mentioned were clicked
         */
        NONE
    }

    /**
     * Interface used to handle Long clicks on the {@link TextView} and taps
     * on the phone, web, mail links inside of {@link TextView}.
     */
    public interface OnPPLinkMovementMethodListener {

        /**
         * This method will be invoked when user press and hold
         * finger on the {@link TextView}
         *
         * @param linkUrl      link from URLSpan which contains link on which user presses.
         * @param linkTypeUrl  Type of the link in linkUrl can be one of {@link LinkType} enumeration
         * @param linkText     Text which contains link on which user presses.
         * @param linkTypeText Type of the link in linkText can be one of {@link LinkType} enumeration
         */
        void onLinkClicked(final String linkUrl, PPLinkMovementMethod.LinkType linkTypeUrl,
                           final String linkText, PPLinkMovementMethod.LinkType linkTypeText);

        /**
         * @param text Whole text of {@link TextView}
         */
        void onLongClick(final String text);
    }


    public PPLinkMovementMethod(final OnPPLinkMovementMethodListener listener, final Context context) {
        mListener = listener;
        mGestureDetector = new GestureDetector(context, new SimpleOnGestureListener());
        mContext = context;
    }

    @Override
    public boolean onTouchEvent(final TextView widget, final Spannable buffer, final MotionEvent event) {

        mWidget = widget;
        mBuffer = buffer;
        mGestureDetector.onTouchEvent(event);

        return false;
    }

    /**
     * Detects various gestures and events.
     * Notify users when a particular motion event has occurred.
     */
    class SimpleOnGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent event) {
            // Notified when a tap occurs.
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            // Notified when a long press occurs.
            final String text = mBuffer.toString().replace(" \u21D2", "");

            if (mListener != null) {
                //Log.e(TAG, "----> Long Click Occurs on TextView with ID: " + mWidget.getId() + "\n" +
                //        "Text: " + text + "\n<----");

                mListener.onLongClick(text);
            }
        }

        private LinkType getLinkTye(String link) {
            String emailLink = link.replace("mailto:", "");
            if (Patterns.PHONE.matcher(link).matches())
                return LinkType.PHONE;
            else if (Patterns.WEB_URL.matcher(link).matches())
                return LinkType.WEB_URL;
            else if (Patterns.EMAIL_ADDRESS.matcher(emailLink).matches())
                return LinkType.EMAIL_ADDRESS;
            else
                return LinkType.NONE;
        }

        private void doLinkType(LinkType linkType, String link, String emailText) {
            if (linkType == LinkType.WEB_URL) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                try {
                    mContext.startActivity(intent);
                } catch (Exception e) {
                    PPApplication.recordException(e);
                }
            } else if (linkType == LinkType.EMAIL_ADDRESS) {
                String emailLink = link.replace("mailto:", "");

                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:")); // only email apps should handle this
                String[] email = {emailLink};
                intent.putExtra(Intent.EXTRA_EMAIL, email);
                try {
                    mContext.startActivity(Intent.createChooser(intent, emailText));
                } catch (Exception e) {
                    PPApplication.recordException(e);
                }
            }
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent event) {
            // Notified when tap occurs.
            final String linkText = getLinkText(mWidget, mBuffer, event).replace(" \u21D2", "");
            final String linkUrl = getLinkURL(mWidget, mBuffer, event);

            LinkType linkTypeUrl = getLinkTye(linkUrl);
            LinkType linkTypeText = getLinkTye(linkText);

            if (linkTypeUrl != LinkType.NONE) {
                doLinkType(linkTypeUrl, linkUrl, linkText);
            } else if (linkTypeText != LinkType.NONE) {
                doLinkType(linkTypeText, linkUrl, linkText);
            }

            if (mListener != null) {
                //Log.e("PPLinkMovementMethod.onSingleTapConfirmed", "----> Tap Occurs on TextView with ID: " + mWidget.getId() + "\n" +
                //        "Link Url: " + linkUrl + "\n" +
                //        "Link Text: " + linkText + "\n" +
                //        "Link Type Url: " + linkTypeUrl + "\n" +
                //        "Link Type Text: " + linkTypeText + "\n<----");

                mListener.onLinkClicked(linkUrl, linkTypeUrl, linkText, linkTypeText);
            }

            return false;
        }

        private String getLinkText(final TextView widget, final Spannable buffer, final MotionEvent event) {

            int x = (int) event.getX();
            int y = (int) event.getY();

            x -= widget.getTotalPaddingLeft();
            y -= widget.getTotalPaddingTop();

            x += widget.getScrollX();
            y += widget.getScrollY();

            Layout layout = widget.getLayout();
            int line = layout.getLineForVertical(y);
            int off = layout.getOffsetForHorizontal(line, x);

            ClickableSpan[] link = buffer.getSpans(off, off, ClickableSpan.class);

            if (link.length != 0) {
                return buffer.subSequence(buffer.getSpanStart(link[0]),
                        buffer.getSpanEnd(link[0])).toString();
            }

            return "";
        }

        private String getLinkURL(final TextView widget, final Spannable buffer, final MotionEvent event) {

            int x = (int) event.getX();
            int y = (int) event.getY();

            x -= widget.getTotalPaddingLeft();
            y -= widget.getTotalPaddingTop();

            x += widget.getScrollX();
            y += widget.getScrollY();

            Layout layout = widget.getLayout();
            int line = layout.getLineForVertical(y);
            int off = layout.getOffsetForHorizontal(line, x);

            URLSpan[] link = buffer.getSpans(off, off, URLSpan.class);

            if (link.length != 0) {
                //Log.e("PPLinkMovementMethod", link[0].getURL());
                return link[0].getURL();

                /*return buffer.subSequence(buffer.getSpanStart(link[0]),
                        buffer.getSpanEnd(link[0])).toString();*/
            }

            return "";
        }
    }

}

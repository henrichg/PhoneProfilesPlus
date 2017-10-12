package com.andraskindler.quickscroll;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.*;
import android.widget.AbsListView.OnScrollListener;

@SuppressWarnings("ResourceType")
public class QuickScroll extends View {

    // IDs
    protected static final int ID_PIN = 512;
    protected static final int ID_PIN_TEXT = 513;
    // type statics
    public static final int TYPE_POPUP = 0;
    public static final int TYPE_INDICATOR = 1;
    public static final int TYPE_POPUP_WITH_HANDLE = 2;
    public static final int TYPE_INDICATOR_WITH_HANDLE = 3;
    // style statics
    public static final int STYLE_NONE = 0;
    public static final int STYLE_HOLO = 1;
    // base colors
    public static final int GREY_DARK = Color.parseColor("#e0585858");
    public static final int GREY_LIGHT = Color.parseColor("#f0888888");
    //public static final int GREY_SCROLLBAR = Color.parseColor("#64404040");
    public static final int BLUE_LIGHT = Color.parseColor("#FF33B5E5");
    public static final int BLUE_LIGHT_SEMITRANSPARENT = Color.parseColor("#8033B5E5");
    protected static final int SCROLLBAR_MARGIN = 10;
    // base variables
    protected boolean isScrolling;
    protected AlphaAnimation fadeInAnimation, fadeOutAnimation;
    protected TextView scrollIndicatorTextView;
    protected Scrollable scrollable;
    protected ListView listView;
    protected int groupPosition;
    protected int itemCount;
    protected int type;
    protected boolean isInitialized = false;
    protected static final int TEXT_PADDING = 4;
    // handlebar variables
    protected View handleBar;
    // indicator variables
    protected RelativeLayout scrollIndicator;

    // default constructors
    public QuickScroll(Context context) {
        super(context);
    }

    public QuickScroll(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public QuickScroll(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * Initializing the QuickScroll, this function must be called.
     * <p/>
     *
     * @param type       the QuickScroll type. Available inputs: <b>QuickScroll.TYPE_POPUP</b> or <b>QuickScroll.TYPE_INDICATOR</b>
     * @param list       the ListView
     * @param scrollable the adapter, must implement Scrollable interface
     */
    @SuppressWarnings("ResourceType")
    public void init(final int type, final ListView list, final Scrollable scrollable, final int style, final int scrollBarColor) {
        if (isInitialized) return;

        this.type = type;
        listView = list;
        this.scrollable = scrollable;
        groupPosition = -1;
        fadeInAnimation = new AlphaAnimation(.0f, 1.0f);
        fadeInAnimation.setFillAfter(true);
        fadeOutAnimation = new AlphaAnimation(1.0f, .0f);
        fadeOutAnimation.setFillAfter(true);
        fadeOutAnimation.setAnimationListener(new AnimationListener() {

            public void onAnimationStart(Animation animation) {
            }

            public void onAnimationRepeat(Animation animation) {
            }

            public void onAnimationEnd(Animation animation) {
                isScrolling = false;
            }
        });
        isScrolling = false;

        listView.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (isScrolling && (event.getAction() == MotionEvent.ACTION_MOVE || event.getAction() == MotionEvent.ACTION_DOWN)) {
                    return true;
                }
                return false;
            }
        });

        final RelativeLayout.LayoutParams containerParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        final RelativeLayout container = new RelativeLayout(getContext());
        container.setBackgroundColor(Color.TRANSPARENT);
        containerParams.addRule(RelativeLayout.ALIGN_TOP, getId());
        containerParams.addRule(RelativeLayout.ALIGN_BOTTOM, getId());
        container.setLayoutParams(containerParams);

        if (this.type == TYPE_POPUP || this.type == TYPE_POPUP_WITH_HANDLE) {
            scrollIndicatorTextView = new TextView(getContext());
            scrollIndicatorTextView.setTextColor(Color.WHITE);
            scrollIndicatorTextView.setVisibility(View.INVISIBLE);
            scrollIndicatorTextView.setGravity(Gravity.CENTER);
            final RelativeLayout.LayoutParams popupParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            popupParams.addRule(RelativeLayout.CENTER_IN_PARENT);
            scrollIndicatorTextView.setLayoutParams(popupParams);
            setPopupColor(GREY_LIGHT, GREY_DARK, 1, Color.WHITE, 1);
            setTextPadding(TEXT_PADDING, TEXT_PADDING, TEXT_PADDING, TEXT_PADDING);
            container.addView(scrollIndicatorTextView);
        } else {
            scrollIndicator = createPin();
            scrollIndicatorTextView = scrollIndicator.findViewById(ID_PIN_TEXT);
            (scrollIndicator.findViewById(ID_PIN)).getLayoutParams().width = 25;
            container.addView(scrollIndicator);
        }

        // setting scrollbar width
        final float density = getResources().getDisplayMetrics().density;
        getLayoutParams().width = (int) (30 * density);
        scrollIndicatorTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 32);

        // scrollbar setup
        if (style != STYLE_NONE) {
            final RelativeLayout layout = new RelativeLayout(getContext());
            final RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            params.addRule(RelativeLayout.ALIGN_LEFT, getId());
            params.addRule(RelativeLayout.ALIGN_TOP, getId());
            params.addRule(RelativeLayout.ALIGN_RIGHT, getId());
            params.addRule(RelativeLayout.ALIGN_BOTTOM, getId());
            layout.setLayoutParams(params);

            final View scrollbar = new View(getContext());
            scrollbar.setBackgroundColor(scrollBarColor);
            final RelativeLayout.LayoutParams scrollBarParams = new RelativeLayout.LayoutParams(1, LayoutParams.MATCH_PARENT);
            scrollBarParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
            scrollBarParams.topMargin = SCROLLBAR_MARGIN;
            scrollBarParams.bottomMargin = SCROLLBAR_MARGIN;
            scrollbar.setLayoutParams(scrollBarParams);
            layout.addView(scrollbar);
            ViewGroup.class.cast(listView.getParent()).addView(layout);
            // creating the handlebar
            if (this.type == TYPE_INDICATOR_WITH_HANDLE || this.type == TYPE_POPUP_WITH_HANDLE) {
                handleBar = new View(getContext());
                setHandlebarColor(BLUE_LIGHT, BLUE_LIGHT, BLUE_LIGHT_SEMITRANSPARENT);
                final RelativeLayout.LayoutParams handleParams = new RelativeLayout.LayoutParams((int) (12 * density), (int) (36 * density));
                handleBar.setLayoutParams(handleParams);
                ((RelativeLayout.LayoutParams) handleBar.getLayoutParams()).addRule(RelativeLayout.CENTER_HORIZONTAL);
                layout.addView(handleBar);

                listView.setOnScrollListener(new OnScrollListener() {

                    public void onScrollStateChanged(AbsListView view, int scrollState) {
                    }

                    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                        if (!isScrolling && totalItemCount - visibleItemCount > 0) {
                            moveHandlebar(getHeight() * firstVisibleItem / (totalItemCount - visibleItemCount));
                        }
                    }
                });
            }
        }

        isInitialized = true;

        ViewGroup.class.cast(listView.getParent()).addView(container);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Adapter adapter = listView.getAdapter();

        if (adapter == null)
            return false;

        if (adapter instanceof HeaderViewListAdapter) {
            adapter = ((HeaderViewListAdapter) adapter).getWrappedAdapter();
        }

        itemCount = adapter.getCount();
        if (itemCount == 0)
            return false;
        if (event.getActionMasked() == MotionEvent.ACTION_CANCEL) {
            if (type == TYPE_POPUP || type == TYPE_INDICATOR) {
                scrollIndicatorTextView.startAnimation(fadeOutAnimation);
            } else {
                if (type == TYPE_INDICATOR_WITH_HANDLE || type == TYPE_POPUP_WITH_HANDLE)
                    handleBar.setSelected(false);
                scrollIndicator.startAnimation(fadeOutAnimation);
            }
        }
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (type == TYPE_INDICATOR || type == TYPE_INDICATOR_WITH_HANDLE) {
                    scrollIndicator.startAnimation(fadeInAnimation);
                    scrollIndicator.setPadding(0, 0, getWidth(), 0);
                } else
                    scrollIndicatorTextView.startAnimation(fadeInAnimation); scroll(event.getY());
                isScrolling = true;
                return true;
            case MotionEvent.ACTION_MOVE:
                scroll(event.getY());
                return true;
            case MotionEvent.ACTION_UP:
                if (type == TYPE_INDICATOR_WITH_HANDLE || type == TYPE_POPUP_WITH_HANDLE)
                    handleBar.setSelected(false);
                if (type == TYPE_INDICATOR || type == TYPE_INDICATOR_WITH_HANDLE)
                    scrollIndicator.startAnimation(fadeOutAnimation);
                else
                    scrollIndicatorTextView.startAnimation(fadeOutAnimation);
                return true;
            default:
                return false;
        }
    }

    @SuppressLint("NewApi")
    protected void scroll(final float height) {
        if (type == TYPE_INDICATOR || type == TYPE_INDICATOR_WITH_HANDLE) {
            float move = height - (scrollIndicator.getHeight() / 2);

            if (move < 0)
                move = 0;
            else if (move > getHeight() - scrollIndicator.getHeight())
                move = getHeight() - scrollIndicator.getHeight();

            // scrollIndicator.setTranslationY(move);
            ViewHelper.setTranslationY(scrollIndicator, move);
        }

        if (type == TYPE_INDICATOR_WITH_HANDLE || type == TYPE_POPUP_WITH_HANDLE) {
            handleBar.setSelected(true);
            moveHandlebar(height - (handleBar.getHeight() / 2));
        }

        int position = (int) ((height / getHeight()) * itemCount);
        if (listView instanceof ExpandableListView) {
            final int groupPosition = ExpandableListView.getPackedPositionGroup(((ExpandableListView) listView).getExpandableListPosition(position));
            if (groupPosition != -1)
                this.groupPosition = groupPosition;
        }

        if (position < 0)
            position = 0;
        else if (position >= itemCount)
            position = itemCount - 1;
        scrollIndicatorTextView.setText(scrollable.getIndicatorForPosition(position, groupPosition));
        listView.setSelection(scrollable.getScrollPosition(position, groupPosition));
    }

    @SuppressLint("NewApi")
    protected void moveHandlebar(final float where) {
        float move = where;
        if (move < SCROLLBAR_MARGIN)
            move = SCROLLBAR_MARGIN;
        else if (move > getHeight() - handleBar.getHeight() - SCROLLBAR_MARGIN)
            move = getHeight() - handleBar.getHeight() - SCROLLBAR_MARGIN;

        // handleBar.setTranslationY(move);
        ViewHelper.setTranslationY(handleBar, move);
    }

    /**
     * Sets the fade in and fade out duration of the indicator; default is 150 ms.
     * <p/>
     *
     * @param millis the fade duration in milliseconds
     */
    public void setFadeDuration(long millis) {
        fadeInAnimation.setDuration(millis);
        fadeOutAnimation.setDuration(millis);
    }

    /**
     * Sets the indicator colors, when QuickScroll.TYPE_INDICATOR is selected as type.
     * <p/>
     *
     * @param background the background color of the square
     * @param tip        the background color of the tip triangle
     * @param text       the color of the text
     */
    public void setIndicatorColor(final int background, final int tip, final int text) {
        if (type == TYPE_INDICATOR || type == TYPE_INDICATOR_WITH_HANDLE) {
            ((Pin) scrollIndicator.findViewById(ID_PIN)).setColor(tip);
            scrollIndicatorTextView.setTextColor(text);
            scrollIndicatorTextView.setBackgroundColor(background);
        }
    }

    /**
     * Sets the popup colors, when QuickScroll.TYPE_POPUP is selected as type.
     * <p/>
     *
     * @param backgroundcolor the background color of the TextView
     * @param bordercolor     the background color of the border surrounding the TextView
     * @param textcolor       the color of the text
     */
    @SuppressLint("ObsoleteSdkInt")
    public void setPopupColor(final int backgroundcolor, final int bordercolor, final int borderwidthDPI, final int textcolor, float cornerradiusDPI) {

        final GradientDrawable popupbackground = new GradientDrawable();
        popupbackground.setCornerRadius(cornerradiusDPI * getResources().getDisplayMetrics().density);
        popupbackground.setStroke((int) (borderwidthDPI * getResources().getDisplayMetrics().density), bordercolor);
        popupbackground.setColor(backgroundcolor);

        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN)
            scrollIndicatorTextView.setBackgroundDrawable(popupbackground);
        else
            scrollIndicatorTextView.setBackground(popupbackground);

        scrollIndicatorTextView.setTextColor(textcolor);
    }

    /**
     * Sets the width and height of the TextView containing the indicatortext. Default is WRAP_CONTENT, WRAP_CONTENT.
     * <p/>
     *
     * @param widthDP  width in DP
     * @param heightDP height in DP
     */
    public void setSize(final int widthDP, final int heightDP) {
        final float density = getResources().getDisplayMetrics().density;
        scrollIndicatorTextView.getLayoutParams().width = (int) (widthDP * density);
        scrollIndicatorTextView.getLayoutParams().height = (int) (heightDP * density);
    }

    /**
     * Sets the padding of the TextView containing the indicatortext. Default is 4 dp.
     * <p/>
     *
     * @param paddingLeftDP   left padding in DP
     * @param paddingTopDP    top param in DP
     * @param paddingBottomDP bottom param in DP
     * @param paddingRightDP  right param in DP
     */
    public void setTextPadding(final int paddingLeftDP, final int paddingTopDP, final int paddingBottomDP, final int paddingRightDP) {
        final float density = getResources().getDisplayMetrics().density;
        scrollIndicatorTextView.setPadding((int) (paddingLeftDP * density), (int) (paddingTopDP * density), (int) (paddingRightDP * density), (int) (paddingBottomDP * density));

    }

    /**
     * Turns on fixed size for the TextView containing the indicatortext. Do not use with setSize()! This mode looks good if the indicatortext length is fixed, e.g. it's always two characters long.
     * <p/>
     *
     * @param sizeEMS number of characters in the indicatortext
     */
    public void setFixedSize(final int sizeEMS) {
        scrollIndicatorTextView.setEms(sizeEMS);
    }

    /**
     * Set the textsize of the TextView containing the indicatortext.
     *
     * @param unit - use TypedValue statics
     * @param size - the size according to the selected unit
     */
    public void setTextSize(final int unit, final float size) {
        scrollIndicatorTextView.setTextSize(unit, size);
    }

    /**
     * Set the colors of the handlebar.
     *
     * @param inactive     - color of the inactive handlebar
     * @param activebase   - base color of the active handlebar
     * @param activestroke - stroke of the active handlebar
     */
    @SuppressLint("ObsoleteSdkInt")
    public void setHandlebarColor(final int inactive, final int activebase, final int activestroke) {
        if (type == TYPE_INDICATOR_WITH_HANDLE || type == TYPE_POPUP_WITH_HANDLE) {
            final float density = getResources().getDisplayMetrics().density;
            final GradientDrawable bg_inactive = new GradientDrawable();
            bg_inactive.setCornerRadius(density);
            bg_inactive.setColor(inactive);
            bg_inactive.setStroke((int) (5 * density), Color.TRANSPARENT);
            final GradientDrawable bg_active = new GradientDrawable();
            bg_active.setCornerRadius(density);
            bg_active.setColor(activebase);
            bg_active.setStroke((int) (5 * density), activestroke);
            final StateListDrawable states = new StateListDrawable();
            states.addState(new int[]{android.R.attr.state_selected}, bg_active);
            states.addState(new int[]{android.R.attr.state_enabled}, bg_inactive);

            if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN)
                handleBar.setBackgroundDrawable(states);
            else
                handleBar.setBackground(states);
        }
    }

    protected RelativeLayout createPin() {
        final RelativeLayout pinLayout = new RelativeLayout(getContext());
        pinLayout.setVisibility(View.INVISIBLE);

        final Pin pin = new Pin(getContext());
        pin.setId(ID_PIN);
        final RelativeLayout.LayoutParams pinParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        pinParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        pinParams.addRule(RelativeLayout.ALIGN_BOTTOM, ID_PIN_TEXT);
        pinParams.addRule(RelativeLayout.ALIGN_TOP, ID_PIN_TEXT);
        pin.setLayoutParams(pinParams);
        pinLayout.addView(pin);

        final TextView indicatorTextView = new TextView(getContext());
        indicatorTextView.setId(ID_PIN_TEXT);
        final RelativeLayout.LayoutParams indicatorParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        indicatorParams.addRule(RelativeLayout.LEFT_OF, ID_PIN);
        indicatorTextView.setLayoutParams(indicatorParams);
        indicatorTextView.setTextColor(Color.WHITE);
        indicatorTextView.setGravity(Gravity.CENTER);
        indicatorTextView.setBackgroundColor(GREY_LIGHT);
        pinLayout.addView(indicatorTextView);

        return pinLayout;
    }

}

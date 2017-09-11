package com.thomaslorincz.chord_diagram;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;
import android.widget.TextView;

import com.thomaslorincz.chord_diagram.R;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Thomas on 15/08/2017.
 * ChordDiagram is a view group that displays a chord diagram. Rotation support is in development.
 */

public class ChordDiagram extends ViewGroup {
    private Map<String, Item> mItems = new LinkedHashMap<>();
    private Map<Integer, Link> mLinks = new LinkedHashMap<>();

    private ChordDiagramView mChordDiagramView;

    private RectF mDiagramBounds = new RectF();

    private float mRadius;
    private int mRingThickness;

    private Paint mItemPaint;
    private Paint mLinkPaint;
    private Paint mTextPaint;

    private int mDiagramRotation;
    private Scroller mScroller;
    private ValueAnimator mScrollAnimator;
    private ObjectAnimator mAutoCenterAnimator;
    private GestureDetector mDetector;

    // XML attributes
    private boolean mShowText;
    private int mItemStyle;

    private boolean calibrated = false;

    /**
     * Items are styled to be arcs of a circle.
     */
    public static final int ITEM_STYLE_ARC = 0;

    /**
     * Items are styled to be nodes.
     */
    public static final int ITEM_STYLE_NODE = 1;

    /**
     * The initial fling velocity is divided by this amount.
     */
    public static final int FLING_VELOCITY_DOWNSCALE = 4;

    private class Item {
        private String mLabel;
        private TextView mTextView;
        private float mLabelAngle;
        private float mLabelStartX;
        private float mLabelStartY;
        private int mColour;
        private float mStartAngle;
        private float mEndAngle;
        private int mNumConnections;
        private int mNumUnassigned;

        private Item(String label, int colour) {
            this.mLabel = label;
            this.mTextView = new TextView(getContext());
            this.mTextView.setText(label);
            this.mTextView.setTextColor(Color.BLACK);
            this.mColour = colour;
        }
    }

    private class Link {
        private Item mItem1;
        private float mEndpointAngle1;
        private Item mItem2;
        private float mEndpointAngle2;

        private Link(Item item1, Item item2) {
            this.mItem1 = item1;
            this.mItem2 = item2;
        }
    }

    public ChordDiagram(Context context) {
        super(context);
        init();
    }

    public ChordDiagram(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(
                attrs, R.styleable.ChordDiagram, 0, 0
        );
        try {
            mShowText = typedArray.getBoolean(R.styleable.ChordDiagram_showText, false);
            mItemStyle = typedArray.getInt(R.styleable.ChordDiagram_itemStyle, 0);
        } finally {
            typedArray.recycle();
        }
        init();
    }

    /**
     * Returns true if the text label should be visible.
     *
     * @return True if the text label should be visible, false otherwise.
     */
    public boolean getShowText() {return mShowText;}

    /**
     * Controls whether the text label is visible or not. Setting this property to
     * false allows the chord diagram graphic to take up the entire visible area of
     * the control.
     *
     * @param showText true if the text label should be visible, false otherwise.
     */
    public void setShowText(boolean showText) {
        mShowText = showText;
        invalidate();
    }

    /**
     * Returns the current rotation of the chord diagram graphic.
     *
     * @return The current chord diagram rotation, in degrees.
     */
    public int getDiagramRotation() {
        return mDiagramRotation;
    }

    /**
     * Set the current rotation of the pie graphic. Setting this value may change
     * the current item.
     *
     * @param rotation The current pie rotation, in degrees.
     */
    public void setDiagramRotation(int rotation) {
        rotation = (((rotation % 360) + 360) % 360);
        mDiagramRotation = rotation;
        mChordDiagramView.rotateTo(rotation);

        for (Map.Entry<String, Item> entry : mItems.entrySet()) {
            Item it = entry.getValue();
            double angle = (it.mLabelAngle - rotation);
            Log.d("Test 1", String.valueOf(it.mLabelAngle - rotation));
            Log.d("Test 2", String.valueOf(rotation - it.mLabelAngle));
            Log.d("Bounds x", String.valueOf(mDiagramBounds.centerX()));
            Log.d("Bounds y", String.valueOf(mDiagramBounds.centerY()));
            float dx = (float) ((Math.cos(-1 * Math.toRadians(angle)) * mRadius) + mDiagramBounds.centerX());
            float dy = (float) ((Math.sin(-1 * Math.toRadians(angle)) * mRadius) + mDiagramBounds.centerY());
            Log.d("Dx", String.valueOf(dx));
            Log.d("Dy", String.valueOf(dy));
            it.mTextView.setX(dx);
            it.mTextView.setY(dy);
        }

    }

    public int getItemStyle() {
        return mItemStyle;
    }

    public void setItemStyle(int style) {
        if ((style == 0) || (style == 1)) {
            mItemStyle = style;
        }
    }

    private void init() {
        calibrated = false;

        // Set up the paint for the items.
        mItemPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        // Set up the paint for the links.
        mLinkPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLinkPaint.setStyle(Paint.Style.STROKE);

        // Set up the paint for the label text
        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(Color.BLACK);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setTextSize(24.0f);

        // Add a child view to draw the diagram. Putting this in a child view
        // makes it possible to draw it on a separate hardware layer that rotates
        // independently
        mChordDiagramView = new ChordDiagramView(getContext());
        addView(mChordDiagramView);
        mChordDiagramView.rotateTo(mDiagramRotation);

        // Set up an animator to animate the PieRotation property. This is used to
        // correct the pie's orientation after the user lets go of it.
        mAutoCenterAnimator = ObjectAnimator.ofInt(ChordDiagram.this, "DiagramRotation", 0);

        // Add a listener to hook the onAnimationEnd event so that we can do
        // some cleanup when the pie stops moving.
        mAutoCenterAnimator.addListener(new Animator.AnimatorListener() {
            public void onAnimationStart(Animator animator) {}

            public void onAnimationEnd(Animator animator) {
                mChordDiagramView.decelerate();
                for (Map.Entry<String, Item> entry : mItems.entrySet()) {
                    entry.getValue().mTextView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                }
            }

            public void onAnimationCancel(Animator animator) {}

            public void onAnimationRepeat(Animator animator) {}
        });


        // Create a Scroller to handle the fling gesture.
        mScroller = new Scroller(getContext(), null, true);
        // The scroller doesn't have any built-in animation functions--it just supplies
        // values when we ask it to. So we have to have a way to call it every frame
        // until the fling ends. This code (ab)uses a ValueAnimator object to generate
        // a callback on every animation frame. We don't use the animated value at all.
        mScrollAnimator = ValueAnimator.ofFloat(0, 1);
        mScrollAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                tickScrollAnimation();
            }
        });

        // Create a gesture detector to handle onTouch messages
        mDetector = new GestureDetector(ChordDiagram.this.getContext(), new GestureListener());

        // Turn off long press--this control doesn't use it, and if long press is enabled,
        // you can't scroll for a bit, pause, then scroll some more (the pause is interpreted
        // as a long press, apparently)
        mDetector.setIsLongpressEnabled(false);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        // Do nothing. Do not call the superclass method--that would start a layout pass
        // on this view's children. ChordDiagram lays out its children in onSizeChanged().
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Try for a width based on our minimum
        int minw = getPaddingLeft() + getPaddingRight() + getSuggestedMinimumWidth();
        int minh = (getPaddingBottom() + getPaddingTop() + getSuggestedMinimumHeight());

        int w = Math.max(minw, MeasureSpec.getSize(widthMeasureSpec));
        int h = Math.max(minh, MeasureSpec.getSize(heightMeasureSpec));

        int d = Math.min(w, h);

        setMeasuredDimension(d, d);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        float xpad = (float) (getPaddingLeft() + getPaddingRight());
        float ypad = (float) (getPaddingTop() + getPaddingBottom());

        float ww = (float) w - xpad;
        float hh = (float) h - ypad;

        // Figure out how big we can make the pie.
        float diameter = Math.min(ww, hh);
//        mRingThickness = (int) (0.05 * diameter);
        mRingThickness = 1;
        mRadius = (diameter / 2) - mRingThickness;

        mDiagramBounds = new RectF(0.0f, 0.0f, diameter, diameter);
        mDiagramBounds.offsetTo(getPaddingLeft(), getPaddingTop());


        // Lay out the child view that actually draws the pie.
        mChordDiagramView.layout(
                (int) mDiagramBounds.left,
                (int) mDiagramBounds.top,
                (int) mDiagramBounds.right,
                (int) mDiagramBounds.bottom);
        mChordDiagramView.setPivot(mDiagramBounds.centerX(), mDiagramBounds.centerY());


        for (Map.Entry<String, Item> entry : mItems.entrySet()) {
            Item it = entry.getValue();
            it.mTextView.measure(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
            float centreAngle = ((it.mStartAngle + it.mEndAngle) / 2);
            it.mTextView.layout(
                    getXCoord(centreAngle) - (it.mTextView.getMeasuredWidth() / 2),
                    getYCoord(centreAngle) - (it.mTextView.getMeasuredHeight() / 2),
                    getXCoord(centreAngle) + (it.mTextView.getMeasuredWidth() / 2),
                    getYCoord(centreAngle) + (it.mTextView.getMeasuredHeight() / 2));
            it.mTextView.setX(getXCoord(centreAngle) - (it.mTextView.getMeasuredWidth() / 2));
            it.mTextView.setY(getYCoord(centreAngle) - (it.mTextView.getMeasuredHeight() / 2));
        }
        onDataChanged();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    private void onDataChanged() {
        assignItemAngles();
        assignLinkAngles();
        onScrollFinished();
    }

    private void assignItemAngles() {
        float startAngle = 0.0f;
        float endAngle;

        // Assign angles to arcs that have connections.
        for (Map.Entry<String, Item> entry : mItems.entrySet()) {
            Item item = entry.getValue();
            item.mNumUnassigned = item.mNumConnections;
            item.mStartAngle = startAngle;
            endAngle = (startAngle + (360.0f / mItems.size()));
            item.mEndAngle = endAngle;
            float centreAngle = ((item.mStartAngle + item.mEndAngle) / 2);
            item.mLabelAngle = centreAngle;
            item.mLabelStartX = getXCoord(centreAngle);
            item.mLabelStartY = getYCoord(centreAngle);
            startAngle = endAngle;
        }
    }

    private void assignLinkAngles() {
        for (Map.Entry<Integer, Link> entry : mLinks.entrySet()) {
            Link link = entry.getValue();
            if (mItemStyle == 0) {
                float sweepAngle = (link.mItem1.mEndAngle - link.mItem1.mStartAngle);
                float distribution = (sweepAngle / (link.mItem1.mNumConnections + 1));
                int angleIndex = ((link.mItem1.mNumConnections - link.mItem1.mNumUnassigned) + 1);
                link.mEndpointAngle1 = (link.mItem1.mStartAngle + (distribution * angleIndex));
                link.mItem1.mNumUnassigned--;

                sweepAngle = (link.mItem2.mEndAngle - link.mItem2.mStartAngle);
                distribution = (sweepAngle / (link.mItem2.mNumConnections + 1));
                angleIndex = ((link.mItem2.mNumConnections - link.mItem2.mNumUnassigned) + 1);
                link.mEndpointAngle2 = (link.mItem2.mStartAngle + (distribution * angleIndex));
                link.mItem2.mNumUnassigned--;
            } else {
                link.mEndpointAngle1 = link.mItem1.mStartAngle;
                link.mEndpointAngle2 = link.mItem2.mStartAngle;
            }
        }
    }

    public void addItem(String label, int colour) {
        if (mItems.get(label) == null) {
            Item item = new Item(label, colour);
            addView(item.mTextView);
            mItems.put(label, item);
            onDataChanged();
        }
    }

    public void deleteItem(String label) {
        mItems.remove(label);
        removeView(mItems.get(label).mTextView);
    }

    public void addLink(String first, String second) {
        Item item1 = mItems.get(first);
        Item item2 = mItems.get(second);
        if ((item1 != item2) && (item1 != null) && (item2 != null)) {
            int hashCode = (first.hashCode() + second.hashCode());
            if (mLinks.get(hashCode) == null) {
                Link newLink = new Link(item1, item2);
                item1.mNumConnections++;
                item2.mNumConnections++;
                mLinks.put(hashCode, newLink);
                onDataChanged();
            }
        }
    }

    public void deleteLink(String first, String second) {
        Item item1 = mItems.get(first);
        Item item2 = mItems.get(second);
        if (item1 != null && item2 != null) {
            mLinks.remove(first.hashCode() + second.hashCode());
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Let the GestureDetector interpret this event
        boolean result = mDetector.onTouchEvent(event);

        // If the GestureDetector doesn't want this event, do some custom processing.
        // This code just tries to detect when the user is done scrolling by looking
        // for ACTION_UP events.
        if (!result) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                // User is done scrolling, it's now safe to do things like autocenter
                stopScrolling();
                result = true;
            }
        }
        return result;
    }

    /**
     * Extends {@link GestureDetector.SimpleOnGestureListener} to provide custom gesture
     * processing.
     */
    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            // Set the pie rotation directly.
            float scrollTheta = vectorToScalarScroll(
                    distanceX,
                    distanceY,
                    e2.getX() - mDiagramBounds.centerX(),
                    e2.getY() - mDiagramBounds.centerY());
            int rotation = getDiagramRotation() - (int) scrollTheta / FLING_VELOCITY_DOWNSCALE;
            setDiagramRotation(rotation);
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            // Set up the Scroller for a fling
            float scrollTheta = vectorToScalarScroll(
                    velocityX,
                    velocityY,
                    e2.getX() - mDiagramBounds.centerX(),
                    e2.getY() - mDiagramBounds.centerY());
            mScroller.fling(
                    0,
                    getDiagramRotation(),
                    0,
                    (int) scrollTheta / FLING_VELOCITY_DOWNSCALE,
                    0,
                    0,
                    Integer.MIN_VALUE,
                    Integer.MAX_VALUE);

            // Start the animator and tell it to animate for the expected duration of the fling.
            mScrollAnimator.setDuration(mScroller.getDuration());
            mScrollAnimator.start();
            return true;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            // The user is interacting with the pie, so we want to turn on acceleration
            // so that the interaction is smooth.
            mChordDiagramView.accelerate();
            for (Map.Entry<String, Item> entry : mItems.entrySet()) {
                entry.getValue().mTextView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            }
            if (isAnimationRunning()) {
                stopScrolling();
            }
            return true;
        }
    }

    private void tickScrollAnimation() {
        if (!mScroller.isFinished()) {
            mScroller.computeScrollOffset();
            setDiagramRotation(mScroller.getCurrY());
        } else {
            mScrollAnimator.cancel();
            onScrollFinished();
        }
    }

    private boolean isAnimationRunning() {
        return !mScroller.isFinished();
    }

    /**
     * Called when the user finishes a scroll action.
     */
    private void onScrollFinished() {
        mChordDiagramView.decelerate();
        for (Map.Entry<String, Item> entry : mItems.entrySet()) {
            entry.getValue().mTextView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
    }

    /**
     * Force a stop to all pie motion. Called when the user taps during a fling.
     */
    private void stopScrolling() {
        mScroller.forceFinished(true);
        onScrollFinished();
    }

    /**
     * Internal child class that draws the chord diagram view onto a separate hardware layer
     * when necessary.
     */
    private class ChordDiagramView extends View {
        /**
         * Construct a ChordDiagramView
         *
         * @param context
         */
        public ChordDiagramView(Context context) {
            super(context);
        }

        /**
         * Enable hardware acceleration (consumes memory)
         */
        public void accelerate() {
            setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }

        /**
         * Disable hardware acceleration (releases memory)
         */
        public void decelerate() {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            drawItems(canvas);
            drawLinks(canvas);
        }

        public void rotateTo(float diagramRotation) {
            setRotation(diagramRotation);
        }

        public void setPivot(float x, float y) {
            setPivotX(x);
            setPivotY(y);
        }

        private void drawItems(Canvas canvas) {
            for (Map.Entry<String, Item> entry : mItems.entrySet()) {
                Item item = entry.getValue();
                mItemPaint.setColor(item.mColour);
                if (mItemStyle == 0) { // Items are arcs
                    mItemPaint.setStyle(Paint.Style.FILL);
//                    mItemPaint.setStrokeWidth(mRingThickness);
                    Path path = new Path();

                    float sweepAngle = (item.mEndAngle - item.mStartAngle);
                    if (mItems.size() == 1) {
                        canvas.drawCircle(
                                mDiagramBounds.centerX(),
                                mDiagramBounds.centerY(),
                                mRadius, mItemPaint
                        );
                    } else {
                        path.moveTo(mDiagramBounds.centerX(), mDiagramBounds.centerY());
                        path.lineTo(getXCoord(item.mStartAngle), getYCoord(item.mStartAngle));
                        path.arcTo(mDiagramBounds, item.mStartAngle, sweepAngle);
                        canvas.drawPath(path, mItemPaint);
                    }
                    Paint testPaint = new Paint();
                    testPaint.setColor(Color.WHITE);
                    testPaint.setStyle(Paint.Style.FILL);
                    canvas.drawCircle(mDiagramBounds.centerX(), mDiagramBounds.centerY(), mRadius - 20, testPaint);
                } else { // Items are nodes
                    mItemPaint.setStyle(Paint.Style.FILL);
                    float centreAngle = ((item.mStartAngle + item.mEndAngle) / 2);
                    canvas.drawCircle(getXCoord(centreAngle), getYCoord(centreAngle), 20, mItemPaint);
                }
            }
        }

        private void drawLinks(Canvas canvas) {
            mLinkPaint.setStrokeWidth(5);
            for (Map.Entry<Integer, Link> entry : mLinks.entrySet()) {
                Link link = entry.getValue();
                Item item1 = link.mItem1;
                Item item2 = link.mItem2;

                int startX = getXCoord(link.mEndpointAngle1);
                int startY = getYCoord(link.mEndpointAngle1);
                int endX = getXCoord(link.mEndpointAngle2);
                int endY = getYCoord(link.mEndpointAngle2);

                drawBezier(canvas, item1.mColour, item2.mColour, startX, startY, endX, endY);
            }
        }

        private void drawBezier(Canvas canvas, int firstColour, int secondColour, int startX,
                                int startY, int endX, int endY) {
            int firstRed = Color.red(firstColour);
            int firstGreen = Color.green(firstColour);
            int firstBlue = Color.blue(firstColour);
            int secondRed = Color.red(secondColour);
            int secondGreen = Color.green(secondColour);
            int secondBlue = Color.blue(secondColour);

            float previousX = 0.0f;
            float previousY = 0.0f;
            float centreX = mDiagramBounds.centerX();
            float centreY = mDiagramBounds.centerY();

            for (float t = 0; t < 1; t += 0.01) {
                Path path = new Path();

                if (t == 0) {
                    previousX = startX;
                    previousY = startY;
                }

                path.moveTo(previousX, previousY);

                float bx = (((1 - t) * (((1 - t) * startX) + (t * centreX))) + (t * (((1 - t) * centreX) + (t * endX))));
                float by = (((1 - t) * (((1 - t) * startY) + (t * centreY))) + (t * (((1 - t) * centreY) + (t * endY))));

                int red = (int) Math.abs((t * secondRed) + ((1 - t) * firstRed));
                int green = (int) Math.abs((t * secondGreen) + ((1 - t) * firstGreen));
                int blue = (int) Math.abs((t * secondBlue) + ((1 - t) * firstBlue));

                mLinkPaint.setARGB(255, red, green, blue);
                path.lineTo(bx, by);
                canvas.drawPath(path, mLinkPaint);

                previousX = bx;
                previousY = by;
            }
        }
    }

    /**
     * Helper method for translating (x,y) scroll vectors into scalar rotation of the pie.
     *
     * @param dx The x component of the current scroll vector.
     * @param dy The y component of the current scroll vector.
     * @param x  The x position of the current touch, relative to the pie center.
     * @param y  The y position of the current touch, relative to the pie center.
     * @return The scalar representing the change in angular position for this scroll.
     */
    private static float vectorToScalarScroll(float dx, float dy, float x, float y) {
        // get the length of the vector
        float l = (float) Math.sqrt(dx * dx + dy * dy);

        // decide if the scalar should be negative or positive by finding
        // the dot product of the vector perpendicular to (x,y).
        float crossX = -y;
        float crossY = x;

        float dot = (crossX * dx + crossY * dy);
        float sign = Math.signum(dot);

        return l * sign;
    }

    private int getXCoord(float theta) {
        return (int) ((mRadius * Math.cos(Math.toRadians(theta))) + mDiagramBounds.centerX());
    }

    private int getYCoord(float theta) {
        return (int) ((mRadius * Math.sin(Math.toRadians(theta))) + mDiagramBounds.centerY());
    }

    private float getAngle(float x, float y) {
        return (float) Math.atan(y / x);
    }

    private float getArcLength(float theta) {
        return (float) (2 * Math.PI * mRadius * (theta / 360.0f));
    }
}

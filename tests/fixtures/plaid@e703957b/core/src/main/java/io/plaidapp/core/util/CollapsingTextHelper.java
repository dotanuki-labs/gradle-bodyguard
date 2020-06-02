/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.plaidapp.core.util;

import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Interpolator;
import androidx.annotation.FloatRange;
import androidx.core.math.MathUtils;
import androidx.core.text.TextDirectionHeuristicsCompat;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import com.google.android.material.R;

/**
 * Adapted from design support lib.
 */
public final class CollapsingTextHelper {
    private static final boolean DEBUG_DRAW = false;
    private static final Paint DEBUG_DRAW_PAINT;

    static {
        DEBUG_DRAW_PAINT = DEBUG_DRAW ? new Paint() : null;
        if (DEBUG_DRAW_PAINT != null) {
            DEBUG_DRAW_PAINT.setAntiAlias(true);
            DEBUG_DRAW_PAINT.setColor(Color.MAGENTA);
        }
    }

    private final View mView;
    private final Rect mExpandedBounds;
    private final Rect mCollapsedBounds;
    private final RectF mCurrentBounds;
    private final TextPaint mTextPaint;
    private boolean mDrawTitle;
    private float mExpandedFraction;
    private int mExpandedTextGravity = Gravity.CENTER_VERTICAL;
    private int mCollapsedTextGravity = Gravity.CENTER_VERTICAL;
    private float mExpandedTextSize = 15;
    private float mCollapsedTextSize = 15;
    private int mExpandedTextColor;
    private int mCollapsedTextColor;
    private float mExpandedDrawY;
    private float mCollapsedDrawY;
    private float mExpandedDrawX;
    private float mCollapsedDrawX;
    private float mCurrentDrawX;
    private float mCurrentDrawY;
    private CharSequence mText;
    private CharSequence mTextToDraw;
    private boolean mIsRtl;
    private boolean mUseTexture;
    private Bitmap mExpandedTitleTexture;
    private Paint mTexturePaint;
    private float mTextureAscent;
    private float mTextureDescent;
    private float mScale;
    private float mCurrentTextSize;
    private boolean mBoundsChanged;
    private Interpolator mPositionInterpolator;
    private Interpolator mTextSizeInterpolator;

    public CollapsingTextHelper(View view) {
        mView = view;
        mTextPaint = new TextPaint();
        mTextPaint.setAntiAlias(true);
        mCollapsedBounds = new Rect();
        mExpandedBounds = new Rect();
        mCurrentBounds = new RectF();
    }

    /**
     * Set the value indicating the current scroll value. This decides how much of the
     * background will be displayed, as well as the title metrics/positioning.
     * <p/>
     * A value of {@code 0.0} indicates that the layout is fully expanded.
     * A value of {@code 1.0} indicates that the layout is fully collapsed.
     */
    public void setExpansionFraction(@FloatRange(from = 0f, to = 1f) float fraction) {
        fraction = MathUtils.clamp(fraction, 0f, 1f);
        if (fraction != mExpandedFraction) {
            mExpandedFraction = fraction;
            calculateCurrentOffsets();
        }
    }

    public float getExpansionFraction() {
        return mExpandedFraction;
    }

    public void draw(Canvas canvas) {
        final int saveCount = canvas.save();
        if (mTextToDraw != null && mDrawTitle) {
            float x = mCurrentDrawX;
            float y = mCurrentDrawY;
            final boolean drawTexture = mUseTexture && mExpandedTitleTexture != null;
            final float ascent;
            final float descent;
            // Update the TextPaint to the current text size
            mTextPaint.setTextSize(mCurrentTextSize);
            if (drawTexture) {
                ascent = mTextureAscent * mScale;
                descent = mTextureDescent * mScale;
            } else {
                ascent = mTextPaint.ascent() * mScale;
                descent = mTextPaint.descent() * mScale;
            }
            if (DEBUG_DRAW) {
                // Just a debug tool, which drawn a Magneta rect in the text bounds
                canvas.drawRect(mCurrentBounds.left, y + ascent, mCurrentBounds.right, y + descent,
                        DEBUG_DRAW_PAINT);
            }
            if (drawTexture) {
                y += ascent;
            }
            if (mScale != 1f) {
                canvas.scale(mScale, mScale, x, y);
            }
            if (drawTexture) {
                // If we should use a texture, draw it instead of text
                canvas.drawBitmap(mExpandedTitleTexture, x, y, mTexturePaint);
            } else {
                canvas.drawText(mTextToDraw, 0, mTextToDraw.length(), x, y, mTextPaint);
            }
        }
        canvas.restoreToCount(saveCount);
    }

    /**
     * Set the title to display
     *
     * @param text
     */
    public void setText(CharSequence text) {
        if (text == null || !text.equals(mText)) {
            mText = text;
            mTextToDraw = null;
            clearTexture();
            recalculate();
        }
    }

    public CharSequence getText() {
        return mText;
    }

    public void setTextSizeInterpolator(Interpolator interpolator) {
        mTextSizeInterpolator = interpolator;
        recalculate();
    }

    public void setPositionInterpolator(Interpolator interpolator) {
        mPositionInterpolator = interpolator;
        recalculate();
    }

    public void setExpandedBounds(int left, int top, int right, int bottom) {
        if (!rectEquals(mExpandedBounds, left, top, right, bottom)) {
            mExpandedBounds.set(left, top, right, bottom);
            mBoundsChanged = true;
            onBoundsChanged();
        }
    }

    public Rect getExpandedBounds() {
        return mExpandedBounds;
    }

    public Point getTextTopLeft() {
        return new Point((int) mCurrentBounds.left,
                (int) (mCurrentBounds.bottom + mTextPaint.ascent()));
    }

    public void setCollapsedBounds(int left, int top, int right, int bottom) {
        if (!rectEquals(mCollapsedBounds, left, top, right, bottom)) {
            mCollapsedBounds.set(left, top, right, bottom);
            mBoundsChanged = true;
            onBoundsChanged();
        }
    }

    public int getExpandedTextGravity() {
        return mExpandedTextGravity;
    }

    public void setExpandedTextGravity(int gravity) {
        if (mExpandedTextGravity != gravity) {
            mExpandedTextGravity = gravity;
            recalculate();
        }
    }

    public int getCollapsedTextGravity() {
        return mCollapsedTextGravity;
    }

    public void setCollapsedTextGravity(int gravity) {
        if (mCollapsedTextGravity != gravity) {
            mCollapsedTextGravity = gravity;
            recalculate();
        }
    }

    public void setCollapsedTextAppearance(int resId) {
        TypedArray a = mView.getContext().obtainStyledAttributes(resId, R.styleable.TextAppearance);
        if (a.hasValue(R.styleable.TextAppearance_android_textColor)) {
            mCollapsedTextColor = a.getColor(
                    R.styleable.TextAppearance_android_textColor, mCollapsedTextColor);
        }
        if (a.hasValue(R.styleable.TextAppearance_android_textSize)) {
            mCollapsedTextSize = a.getDimensionPixelSize(
                    R.styleable.TextAppearance_android_textSize, (int) mCollapsedTextSize);
        }
        a.recycle();
        recalculate();
    }

    public void setExpandedTextAppearance(int resId) {
        TypedArray a = mView.getContext().obtainStyledAttributes(resId, R.styleable.TextAppearance);
        if (a.hasValue(R.styleable.TextAppearance_android_textColor)) {
            mExpandedTextColor = a.getColor(
                    R.styleable.TextAppearance_android_textColor, mExpandedTextColor);
        }
        if (a.hasValue(R.styleable.TextAppearance_android_textSize)) {
            mExpandedTextSize = a.getDimensionPixelSize(
                    R.styleable.TextAppearance_android_textSize, (int) mExpandedTextSize);
        }
        a.recycle();
        recalculate();
    }

    public Typeface getTypeface() {
        return mTextPaint.getTypeface();
    }

    public void setTypeface(Typeface typeface) {
        if (typeface == null) {
            typeface = Typeface.DEFAULT;
        }
        if (mTextPaint.getTypeface() != typeface) {
            mTextPaint.setTypeface(typeface);
            recalculate();
        }
    }

    public float getCollapsedTextSize() {
        return mCollapsedTextSize;
    }

    public void setCollapsedTextSize(float textSize) {
        if (mCollapsedTextSize != textSize) {
            mCollapsedTextSize = textSize;
            recalculate();
        }
    }

    public float getExpandedTextSize() {
        return mExpandedTextSize;
    }

    public void setExpandedTextSize(float textSize) {
        if (mExpandedTextSize != textSize) {
            mExpandedTextSize = textSize;
            recalculate();
        }
    }

    public int getExpandedTextColor() {
        return mExpandedTextColor;
    }

    public void setExpandedTextColor(int textColor) {
        if (mExpandedTextColor != textColor) {
            mExpandedTextColor = textColor;
            recalculate();
        }
    }

    public int getCollapsedTextColor() {
        return mCollapsedTextColor;
    }

    public void setCollapsedTextColor(int textColor) {
        if (mCollapsedTextColor != textColor) {
            mCollapsedTextColor = textColor;
            recalculate();
        }
    }

    public float getCurrentTextSize() {
        return mCurrentTextSize;
    }

    private static float lerp(float startValue, float endValue, float fraction,
                              Interpolator interpolator) {
        if (interpolator != null) {
            fraction = interpolator.getInterpolation(fraction);
        }
        return AnimUtils.lerp(startValue, endValue, fraction);
    }

    private static boolean rectEquals(Rect r, int left, int top, int right, int bottom) {
        return !(r.left != left || r.top != top || r.right != right || r.bottom != bottom);
    }

    private void onBoundsChanged() {
        mDrawTitle = mCollapsedBounds.width() > 0 && mCollapsedBounds.height() > 0
                && mExpandedBounds.width() > 0 && mExpandedBounds.height() > 0;
    }

    private void calculateCurrentOffsets() {
        final float fraction = mExpandedFraction;
        interpolateBounds(fraction);
        mCurrentDrawX = lerp(mExpandedDrawX, mCollapsedDrawX, fraction,
                mPositionInterpolator);
        mCurrentDrawY = lerp(mExpandedDrawY, mCollapsedDrawY, fraction,
                mPositionInterpolator);
        setInterpolatedTextSize(lerp(mExpandedTextSize, mCollapsedTextSize,
                fraction, mTextSizeInterpolator));
        if (mCollapsedTextColor != mExpandedTextColor) {
            // If the collapsed and expanded text colors are different, blend them based on the
            // fraction
            mTextPaint.setColor(blendColors(mExpandedTextColor, mCollapsedTextColor, fraction));
        } else {
            mTextPaint.setColor(mCollapsedTextColor);
        }
        ViewCompat.postInvalidateOnAnimation(mView);
    }

    private void calculateBaseOffsets() {
        // We then calculate the collapsed text size, using the same logic
        mTextPaint.setTextSize(mCollapsedTextSize);
        float width = mTextToDraw != null ?
                mTextPaint.measureText(mTextToDraw, 0, mTextToDraw.length()) : 0;
        final int collapsedAbsGravity = GravityCompat.getAbsoluteGravity(mCollapsedTextGravity,
                mIsRtl ? ViewCompat.LAYOUT_DIRECTION_RTL : ViewCompat.LAYOUT_DIRECTION_LTR);
        switch (collapsedAbsGravity & Gravity.VERTICAL_GRAVITY_MASK) {
            case Gravity.BOTTOM:
                mCollapsedDrawY = mCollapsedBounds.bottom;
                break;
            case Gravity.TOP:
                mCollapsedDrawY = mCollapsedBounds.top - mTextPaint.ascent();
                break;
            case Gravity.CENTER_VERTICAL:
            default:
                float textHeight = mTextPaint.descent() - mTextPaint.ascent();
                float textOffset = (textHeight / 2) - mTextPaint.descent();
                mCollapsedDrawY = mCollapsedBounds.centerY() + textOffset;
                break;
        }
        switch (collapsedAbsGravity & Gravity.HORIZONTAL_GRAVITY_MASK) {
            case Gravity.CENTER_HORIZONTAL:
                mCollapsedDrawX = mCollapsedBounds.centerX() - (width / 2);
                break;
            case Gravity.RIGHT:
                mCollapsedDrawX = mCollapsedBounds.right - width;
                break;
            case Gravity.LEFT:
            default:
                mCollapsedDrawX = mCollapsedBounds.left;
                break;
        }
        mTextPaint.setTextSize(mExpandedTextSize);
        width = mTextToDraw != null
                ? mTextPaint.measureText(mTextToDraw, 0, mTextToDraw.length()) : 0;
        final int expandedAbsGravity = GravityCompat.getAbsoluteGravity(mExpandedTextGravity,
                mIsRtl ? ViewCompat.LAYOUT_DIRECTION_RTL : ViewCompat.LAYOUT_DIRECTION_LTR);
        switch (expandedAbsGravity & Gravity.VERTICAL_GRAVITY_MASK) {
            case Gravity.BOTTOM:
                mExpandedDrawY = mExpandedBounds.bottom;
                break;
            case Gravity.TOP:
                mExpandedDrawY = mExpandedBounds.top - mTextPaint.ascent();
                break;
            case Gravity.CENTER_VERTICAL:
            default:
                float textHeight = mTextPaint.descent() - mTextPaint.ascent();
                float textOffset = (textHeight / 2) - mTextPaint.descent();
                mExpandedDrawY = mExpandedBounds.centerY() + textOffset;
                break;
        }
        switch (expandedAbsGravity & Gravity.HORIZONTAL_GRAVITY_MASK) {
            case Gravity.CENTER_HORIZONTAL:
                mExpandedDrawX = mExpandedBounds.centerX() - (width / 2);
                break;
            case Gravity.RIGHT:
                mExpandedDrawX = mExpandedBounds.right - width;
                break;
            case Gravity.LEFT:
            default:
                mExpandedDrawX = mExpandedBounds.left;
                break;
        }
        // The bounds have changed so we need to clear the texture
        clearTexture();
    }

    private void interpolateBounds(float fraction) {
        mCurrentBounds.left = lerp(mExpandedBounds.left, mCollapsedBounds.left,
                fraction, mPositionInterpolator);
        mCurrentBounds.top = lerp(mExpandedDrawY, mCollapsedDrawY,
                fraction, mPositionInterpolator);
        mCurrentBounds.right = lerp(mExpandedBounds.right, mCollapsedBounds.right,
                fraction, mPositionInterpolator);
        mCurrentBounds.bottom = lerp(mExpandedBounds.bottom, mCollapsedBounds.bottom,
                fraction, mPositionInterpolator);
    }

    private boolean calculateIsRtl(CharSequence text) {
        final boolean defaultIsRtl = ViewCompat.getLayoutDirection(mView)
                == ViewCompat.LAYOUT_DIRECTION_RTL;
        return (defaultIsRtl
                ? TextDirectionHeuristicsCompat.FIRSTSTRONG_RTL
                : TextDirectionHeuristicsCompat.FIRSTSTRONG_LTR).isRtl(text, 0, text.length());
    }

    private void setInterpolatedTextSize(final float textSize) {
        if (mText == null) return;
        final float availableWidth;
        final float newTextSize;
        boolean updateDrawText = false;
        if (isClose(textSize, mCollapsedTextSize)) {
            availableWidth = mCollapsedBounds.width();
            newTextSize = mCollapsedTextSize;
            mScale = 1f;
        } else {
            availableWidth = mExpandedBounds.width();
            newTextSize = mExpandedTextSize;
            if (isClose(textSize, mExpandedTextSize)) {
                // If we're close to the expanded text size, snap to it and use a scale of 1
                mScale = 1f;
            } else {
                // Else, we'll scale down from the expanded text size
                mScale = textSize / mExpandedTextSize;
            }
        }
        if (availableWidth > 0) {
            updateDrawText = (mCurrentTextSize != newTextSize) || mBoundsChanged;
            mCurrentTextSize = newTextSize;
            mBoundsChanged = false;
        }
        if (mTextToDraw == null || updateDrawText) {
            mTextPaint.setTextSize(mCurrentTextSize);
            // If we don't currently have text to draw, or the text size has changed, ellipsize...
            final CharSequence title = TextUtils.ellipsize(mText, mTextPaint,
                    availableWidth, TextUtils.TruncateAt.END);
            if (mTextToDraw == null || !mTextToDraw.equals(title)) {
                mTextToDraw = title;
            }
            mIsRtl = calculateIsRtl(mTextToDraw);
        }
        // Use our texture if the scale isn't 1.0
        mUseTexture = mScale != 1f;
        if (mUseTexture) {
            // Make sure we have an expanded texture if needed
            ensureExpandedTexture();
        }
        ViewCompat.postInvalidateOnAnimation(mView);
    }

    private void ensureExpandedTexture() {
        if (mExpandedTitleTexture != null || mExpandedBounds.isEmpty()
                || TextUtils.isEmpty(mTextToDraw)) {
            return;
        }
        mTextPaint.setTextSize(mExpandedTextSize);
        mTextPaint.setColor(mExpandedTextColor);
        mTextureAscent = mTextPaint.ascent();
        mTextureDescent = mTextPaint.descent();
        final int w = Math.round(mTextPaint.measureText(mTextToDraw, 0, mTextToDraw.length()));
        final int h = Math.round(mTextureDescent - mTextureAscent);
        if (w <= 0 && h <= 0) {
            return; // If the width or height are 0, return
        }
        mExpandedTitleTexture = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(mExpandedTitleTexture);
        c.drawText(mTextToDraw, 0, mTextToDraw.length(), 0, h - mTextPaint.descent(), mTextPaint);
        if (mTexturePaint == null) {
            // Make sure we have a paint
            mTexturePaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        }
    }

    private void recalculate() {
        if (mView.getHeight() > 0 && mView.getWidth() > 0) {
            // If we've already been laid out, calculate everything now otherwise we'll wait
            // until a layout
            calculateBaseOffsets();
            calculateCurrentOffsets();
        }
    }

    private void clearTexture() {
        if (mExpandedTitleTexture != null) {
            mExpandedTitleTexture.recycle();
            mExpandedTitleTexture = null;
        }
    }

    /**
     * Returns true if {@code value} is 'close' to it's closest decimal value. Close is currently
     * defined as it's difference being < 0.001.
     */
    private static boolean isClose(float value, float targetValue) {
        return Math.abs(value - targetValue) < 0.001f;
    }

    /**
     * Blend {@code color1} and {@code color2} using the given ratio.
     *
     * @param ratio of which to blend. 0.0 will return {@code color1}, 0.5 will give an even blend,
     *              1.0 will return {@code color2}.
     */
    private static int blendColors(int color1, int color2, float ratio) {
        final float inverseRatio = 1f - ratio;
        float a = (Color.alpha(color1) * inverseRatio) + (Color.alpha(color2) * ratio);
        float r = (Color.red(color1) * inverseRatio) + (Color.red(color2) * ratio);
        float g = (Color.green(color1) * inverseRatio) + (Color.green(color2) * ratio);
        float b = (Color.blue(color1) * inverseRatio) + (Color.blue(color2) * ratio);
        return Color.argb((int) a, (int) r, (int) g, (int) b);
    }
}

/*
 * http://www.35-35.net/aozora/
 */

package com.soso.sgui.text;

import java.awt.Rectangle;

import javax.swing.text.BoxView;
import javax.swing.text.Element;
import javax.swing.text.View;


/** n */
class SBoxView extends BoxView {

    protected SBoxView(Element elem, int axis) {
        super(elem, axis);
    }

    protected boolean isAfter(int x, int y, Rectangle innerAlloc) {
        return super.isBefore(x, y, innerAlloc);
    }

    protected boolean isBefore(int x, int y, Rectangle innerAlloc) {
        return super.isAfter(x, y, innerAlloc);
    }

    protected void layoutMinorAxis(int targetSpan, int axis, int[] offsets, int[] spans) {
        for (int i = 0; i < getViewCount(); i++) {
            View view = getView(i);
            int span = (int) view.getMaximumSpan(axis);
            if (span < targetSpan) {
                float alignment = view.getAlignment(axis);
                offsets[i] = (int) ((targetSpan - span) * alignment);
                spans[i] = span;
            } else {
                span = (int) view.getMinimumSpan(axis);
                offsets[i] = 0;
                spans[i] = Math.max(span, targetSpan);
            }
        }
    }

    protected void layoutMajorAxis(int targetSpan, int axis, int[] offsets, int[] spans) {
        long newSum;
        long sum = 0L;
        int count = getViewCount();
        for (int i = 0; i < count; i++) {
            View view = getView(i);
            spans[i] = (int) view.getPreferredSpan(axis);
            sum += spans[i];
        }

        long rest = targetSpan - sum;
        float max = 0.0F;
        int[] newSpans = null;
        if (rest != 0L) {
            newSum = 0L;
            newSpans = new int[count];

            for (int i = 0; i < count; i++) {
                View view = getView(i);
                int span;
                if (rest < 0L) {
                    span = (int) view.getMinimumSpan(axis);
                    newSpans[i] = spans[i] - span;
                } else {
                    span = (int) view.getMaximumSpan(axis);
                    newSpans[i] = span - spans[i];
                }
                newSum += span;
            }
            float diff = Math.abs(newSum - sum);
            max = rest / diff;
            max = Math.min(max, 1.0F);
            max = Math.max(max, -1F);
        }
        int offset = 0;
        for (int i = 0; i < count; i++) {
            offsets[i] = offset;
            if (rest != 0L) {
                float f2 = max * newSpans[i];
                spans[i] += Math.round(f2);
            }
            offset = (int) Math.min((long) offset + (long) spans[i], 0x7fffffffL);
        }

        if (offsets.length > 0 && targetSpan < 0x7fffffff && targetSpan > offsets[0] + spans[0]) {
            int span = targetSpan - offsets[0] - spans[0];
            for (int i = 0; i < offsets.length; i++)
                offsets[i] += span;

        }
    }

    protected View getViewAtPoint(int x, int y, Rectangle alloc) {
        int count = getViewCount();
        switch (getAxis()) {
        case BoxView.X_AXIS:
            for (int i = 0; i < count; i++)
                if (x > alloc.x + getOffset(BoxView.X_AXIS, i)) {
                    childAllocation(i, alloc);
                    return getView(i);
                }

            childAllocation(count - 1, alloc);
            return getView(count - 1);
        case BoxView.Y_AXIS:
            for (int i = 0; i < count; i++)
                if (y > alloc.y + getOffset(BoxView.Y_AXIS, i)) {
                    childAllocation(i, alloc);
                    return getView(i);
                }

            childAllocation(count - 1, alloc);
            return getView(count - 1);
        }
        throw new IllegalStateException(String.valueOf(getAxis()));
    }
}

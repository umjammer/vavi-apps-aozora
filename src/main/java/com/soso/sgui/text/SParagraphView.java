/*
 * http://www.35-35.net/aozora/
 */

package com.soso.sgui.text;

import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;

import javax.swing.SizeRequirements;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.BoxView;
import javax.swing.text.Element;
import javax.swing.text.FlowView;
import javax.swing.text.ParagraphView;
import javax.swing.text.Position;
import javax.swing.text.TabExpander;
import javax.swing.text.TabableView;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;


/** m */
final class SParagraphView extends ParagraphView {

    /** k */
    static class SFlowStrategy extends FlowView.FlowStrategy {

        protected final int layoutRow(FlowView fv, int rowIndex, int pos) {
            View view = fv.getView(rowIndex);
            int start = fv.getFlowStart(rowIndex);
            int span = fv.getFlowSpan(rowIndex);
            int offset = fv.getEndOffset();
            TabExpander e = (fv instanceof TabExpander) ? (TabExpander) fv : null;
            int desiredSpan = span;
            int x = start;
            int axis = fv.getFlowAxis();
            boolean flag = false;
            do {
                View view1 = createView(fv, pos, span, rowIndex);
                if (pos >= offset || span < 0 || view1 == null || span == 0 && view1.getPreferredSpan(axis) > 0.0F)
                    break;
                int desiredSpan1 = (int) ((view1 instanceof TabableView) ? ((TabableView) view1).getTabbedSpan(start, e) : view1.getPreferredSpan(axis));
                if (view1.getBreakWeight(axis, pos, span) >= 3000) {
                    int count = view.getViewCount();
                    if (count > 0) {
                        view1 = view1.breakView(axis, pos, start, span);
                        desiredSpan1 = view1 == null ? 0
                                                     : (view1 instanceof TabableView) ? (int) ((TabableView) view1).getTabbedSpan(start, e)
                                                                                      : (int) view1.getPreferredSpan(axis);
                    }
                    flag = true;
                }
                span -= desiredSpan1;
                start += desiredSpan1;
                if (view1 != null) {
                    view.append(view1);
                    pos = view1.getEndOffset();
                }
            } while (!flag);
            if (span < 0)
                adjustRow(fv, rowIndex, desiredSpan, x);
            else if (view.getViewCount() == 0) {
                View view2 = createView(fv, pos, 0x7fffffff, rowIndex);
                view.append(view2);
            }
            return view.getEndOffset();
        }

        protected SFlowStrategy() {
        }
    }

    /** o */
    static class Row extends SBoxView {

        protected final void loadChildren(ViewFactory f) {
        }

        public final AttributeSet getAttributes() {
            View parent = getParent();
            if (parent != null)
                return parent.getAttributes();
            else
                return null;
        }

        public final Shape modelToView(int pos, Shape a, Position.Bias b) throws BadLocationException {
            Rectangle bounds = a.getBounds();
            View view = getViewAtPosition(pos, bounds);
            if (view != null && !view.getElement().isLeaf()) {
                return super.modelToView(pos, a, b);
            } else {
                int h = bounds.height;
                int y = bounds.y;
                Shape shape = super.modelToView(pos, a, b);
                bounds = shape.getBounds();
                bounds.height = h;
                bounds.y = y;
                return bounds;
            }
        }

        public final int getStartOffset() {
            int offset = 0x7fffffff;
            int count = getViewCount();
            for (int i = 0; i < count; i++) {
                View view = getView(i);
                offset = Math.min(offset, view.getStartOffset());
            }

            return offset;
        }

        public final int getEndOffset() {
            int offset = 0;
            int count = getViewCount();
            for (int i = 0; i < count; i++) {
                View view = getView(i);
                offset = Math.max(offset, view.getEndOffset());
            }

            return offset;
        }

        protected final void layoutMinorAxis(int targetSpan, int axis, int[] offsets, int[] spans) {
            baselineLayout(targetSpan, axis, offsets, spans);
        }

        protected final SizeRequirements calculateMinorAxisRequirements(int axis, SizeRequirements r) {
            return baselineRequirements(axis, r);
        }

        protected final int getViewIndexAtPosition(int pos) {
            if (pos < getStartOffset() || pos >= getEndOffset())
                return -1;
            for (int i = 0; i < getViewCount(); i++) {
                View view = getView(i);
                if (pos >= view.getStartOffset() && pos < view.getEndOffset())
                    return i;
            }

            return -1;
        }

        protected final short getTopInset() {
            return super.getTopInset();
        }

        protected final short getBottomInset() {
            return super.getBottomInset();
        }

        public final String toString() {
            StringBuilder sb = new StringBuilder(super.toString());
            try {
                sb.append("[");
                int count = getViewCount();
                for (int i = 0; i < count; i++) {
                    if (i != 0)
                        sb.append(", ");
                    sb.append(getView(count));
                }

                sb.append("]");
            } catch (Exception ignored) {
            }
            return sb.toString();
        }

        protected Row(Element elem) {
            super(elem, BoxView.X_AXIS);
        }
    }

    protected SParagraphView(Element elem) {
        super(elem);
        setAxis(BoxView.X_AXIS);
        this.strategy = flowStrategy;
    }

    public float nextTabStop(float x, int tabOffset) {
        int h = getFontMetrics().getHeight() * 2;
        return (x / h + 1.0F) * h;
    }

    public float getAlignment(int axis) {
        switch (axis) {
        case X_AXIS:
            return 0.5F;
        case Y_AXIS:
            return 0.0F;
        }
        throw new IllegalArgumentException("Invalid axis: " + axis);
    }

    public int getFlowSpan(int index) {
        View child = getView(index);
        int adjust = 0;
        Row row = (Row) child;
        if (child != null)
            adjust = row.getTopInset() + row.getBottomInset();
        int span = layoutSpan - adjust;
        return span;
    }

    public int getFlowStart(int index) {
        View child = getView(index);
        short adjust = 0;
        Row row = (Row) child;
        if (child != null)
            adjust = row.getTopInset();
        return adjust;
    }

    protected boolean isAfter(int x, int y, Rectangle innerAlloc) {
        return super.isBefore(x, y, innerAlloc);
    }

    protected boolean isBefore(int x, int y, Rectangle innerAlloc) {
        return super.isAfter(x, y, innerAlloc);
    }

    protected void layoutMinorAxis(int targetSpan, int axis, int[] offsets, int[] spans) {
        int count = getViewCount();

        for (int i = 0; i < count; i++) {
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
        long sum = 0L;
        int count = getViewCount();
        for (int i = 0; i < count; i++) {
            View view = getView(i);
            spans[i] = (int) view.getPreferredSpan(axis);
            sum += spans[i];
        }

        long rest = targetSpan - sum;
        float scale = 0.0F;
        int[] ai2 = null;
        
        if (rest != 0L) {
            long allSpan = 0L;
            ai2 = new int[count];
            
            for (int i = 0; i < count; i++) {
                View view = getView(i);
                int span;
                if (rest < 0L) {
                    span = (int) view.getMinimumSpan(axis);
                    // ai2;
                    // k2;
                    // ai1[k2];
                    // j3;
                    ai2[i] -= spans[i]; // TODO ???
                } else {
                    span = (int) view.getMaximumSpan(axis);
                    // ai2;
                    // k2;
                    // j3;
                    // ai1[k2];
                    ai2[i] -= span; // TODO ???
                    // _L3:
                }
                // JVM INSTR isub ;
                // JVM INSTR iastore ;
                allSpan += span;
            }
            float restSpan = Math.abs(allSpan - sum);
            scale = rest / restSpan;
            scale = Math.min(scale, 1.0F);
            scale = Math.max(scale, -1F);
        }
        int offset = 0;
        for (int i = count - 1; i >= 0; i--) {
            offsets[i] = offset;
            if (rest != 0L) {
                float restSpan = scale * ai2[i];
                spans[i] += Math.round(restSpan);
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
        case X_AXIS:
            for (int i = 0; i < count; i++)
                if (x > alloc.x + getOffset(0, i)) {
                    childAllocation(i, alloc);
                    return getView(i);
                }

            childAllocation(count - 1, alloc);
            return getView(count - 1);
        case Y_AXIS:
            for (int i = 0; i < count; i++)
                if (y > alloc.y + getOffset(1, i)) {
                    childAllocation(i, alloc);
                    return getView(i);
                }

            childAllocation(count - 1, alloc);
            return getView(count - 1);
        }
        throw new IllegalStateException(String.valueOf(getAxis()));
    }

    protected View createRow() {
        return new Row(getElement());
    }

    private FontMetrics getFontMetrics() {
        Graphics g = getGraphics();
        if (g == null)
            throw new IllegalStateException("Graphics has not been initialized.");
        else
            return g.getFontMetrics();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        try {
            sb.append("[");
            int count = getViewCount();
            for (int i = 0; i < count; i++) {
                if (i != 0)
                    sb.append(", ");
                sb.append(getView(count));
            }

            sb.append("]");
        } catch (Exception ignored) {
        }
        return sb.toString();
    }

    private static final SFlowStrategy flowStrategy = new SFlowStrategy();
}

/*
 * http://www.35-35.net/aozora/
 */

package com.soso.sgui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;


public class SFlowLayout implements LayoutManager {

    class class_d {
    }

    static class Comp extends Component {

        Comp() {
            Dimension size = new Dimension(0, 0);
            setPreferredSize(size);
            setMaximumSize(size);
            setMinimumSize(size);
            setSize(size);
            setVisible(false);
            setEnabled(false);
        }

        public final void paint(Graphics g) {
        }

        int id;
        int type;

        static final int WARP = 1;
        static final int SPACE = 2;
        static final int BAND = 3;
        static final int GLUE = 4;
        static final int LINE_GLUE = 5;
    }

    class Group {

        private Group(SFlowLayout layout) {
            components = new ArrayList<Component>();
            a = 0;
            size_b = 0;
            count_c = 0;
            isVisible = false;
        }

        final int size() {
            return components.size();
        }

        final void addComponent(Component comp) {
            components.add(comp);
        }

        final Component getComponent(int index) {
            return components.get(index);
        }

        Group(SFlowLayout layout, class_d d1) {
            this(layout);
        }

        private List<Component> components;
        int a;
        int size_b;
        int count_c;
        boolean isVisible;
    }

    public static Component createWrap() {
        return createWrap(-1);
    }

    public static Component createWrap(int id) {
        Comp c = new Comp();
        c.id = id;
        c.type = Comp.WARP;
        return c;
    }

    public static Component createSpace(int id) {
        if (id < 0) {
            throw new IllegalArgumentException("negative value");
        }
        Comp c = new Comp();
        c.id = id;
        c.type = Comp.SPACE;
        return c;
    }

    public static Component createBand(int id) {
        if (id < 0) {
            throw new IllegalArgumentException("negative value");
        }
        Comp c = new Comp();
        c.id = id;
        c.type = Comp.BAND;
        return c;
    }

    public static Component createGlue() {
        Comp c = new Comp();
        c.type = Comp.GLUE;
        return c;
    }

    public static Component createLineGlue() {
        Comp c = new Comp();
        c.type = Comp.LINE_GLUE;
        return c;
    }

    public SFlowLayout() {
        origin = LEFT_TOP;
        axis = HORIZONTAL;
        align = LEADING;
        valign = LEADING;
        fix = ORIGIN_SIDE;
        itemSpace = 5;
        lineSpace = 5;
        topMargin = 5;
        bottomMargin = 5;
        leftMargin = 5;
        rightMargin = 5;
        nowrap = false;
    }

    public int getOrigin() {
        return origin;
    }

    public void setOrigin(int origin) {
        if (origin == LEFT_TOP || origin == LEFT_BOTTOM || origin == RIGHT_TOP || origin == RIGHT_BOTTOM) {
            this.origin = origin;
        } else {
            throw new IllegalArgumentException();
        }
    }

    public int getAxis() {
        return axis;
    }

    public void setAxis(int axis) {
        if (axis == HORIZONTAL || axis == VERTICAL) {
            this.axis = axis;
        } else {
            throw new IllegalArgumentException();
        }
    }

    public int getAlign() {
        return align;
    }

    public void setAlign(int align) {
        if (align == LEADING || align == CENTER || align == TRAILING || align == BOTH) {
            this.align = align;
        } else {
            throw new IllegalArgumentException();
        }
    }

    public int getValign() {
        return valign;
    }

    public void setValign(int valign) {
        if (valign == LEADING || valign == CENTER || valign == TRAILING || valign == BOTH) {
            this.valign = valign;
        } else {
            throw new IllegalArgumentException();
        }
    }

    public int getFix() {
        return fix;
    }

    public void setFix(int fix) {
        if (fix == ORIGIN_SIDE || fix == CENTER || fix == REVERSE_SIDE) {
            this.fix = fix;
        } else {
            throw new IllegalArgumentException();
        }
    }

    public void setSpace(int space) {
        if (space < 0) {
            throw new IllegalArgumentException("negative value");
        }
        this.itemSpace = space;
        this.lineSpace = space;
    }

    public void setSpace(int item, int line) {
        if (item < 0)
            throw new IllegalArgumentException("negative value");
        if (line < 0) {
            throw new IllegalArgumentException("negative value");
        }
        this.itemSpace = item;
        this.lineSpace = line;
    }

    public int getItemSpace() {
        return itemSpace;
    }

    public void setItemSpace(int itemSpace) {
        if (itemSpace < 0) {
            throw new IllegalArgumentException("negative value");
        }
        this.itemSpace = itemSpace;
    }

    public int getLineSpace() {
        return lineSpace;
    }

    public void setLineSpace(int lineSpace) {
        if (lineSpace < 0) {
            throw new IllegalArgumentException("negative value");
        }
        this.lineSpace = lineSpace;
    }

    public void setMargin(int margin) {
        if (margin < 0) {
            throw new IllegalArgumentException("negative value");
        }
        topMargin = margin;
        leftMargin = margin;
        rightMargin = margin;
        bottomMargin = margin;
    }

    public void setMargin(int top, int left, int right, int bottom) {
        if (top < 0)
            throw new IllegalArgumentException("negative value");
        if (left < 0)
            throw new IllegalArgumentException("negative value");
        if (right < 0)
            throw new IllegalArgumentException("negative value");
        if (bottom < 0) {
            throw new IllegalArgumentException("negative value");
        }
        topMargin = top;
        leftMargin = left;
        rightMargin = right;
        bottomMargin = bottom;
    }

    public int getTopMargin() {
        return topMargin;
    }

    public void setTopMargin(int topMargin) {
        if (topMargin < 0) {
            throw new IllegalArgumentException("negative value");
        }
        this.topMargin = topMargin;
    }

    public int getBottomMargin() {
        return bottomMargin;
    }

    public void setBottomMargin(int bottomMargin) {
        if (bottomMargin < 0) {
            throw new IllegalArgumentException("negative value");
        }
        this.bottomMargin = bottomMargin;
    }

    public int getLeftMargin() {
        return leftMargin;
    }

    public void setLeftMargin(int leftMargin) {
        if (leftMargin < 0) {
            throw new IllegalArgumentException("negative value");
        }
        this.leftMargin = leftMargin;
    }

    public int getRightMargin() {
        return rightMargin;
    }

    public void setRightMargin(int rightMargin) {
        if (rightMargin < 0) {
            throw new IllegalArgumentException("negative value");
        }
        this.rightMargin = rightMargin;
    }

    public boolean isNowrap() {
        return nowrap;
    }

    public void setNowrap(boolean nowrap) {
        this.nowrap = nowrap;
    }

    public void addLayoutComponent(String name, Component comp) {
    }

    public void removeLayoutComponent(Component comp) {
    }

    public Dimension minimumLayoutSize(Container parent) {
        return preferredLayoutSize(parent);
    }

    public Dimension preferredLayoutSize(Container parent) {
        synchronized (parent.getTreeLock()) {
            int pw = parent.getHeight();
            int ph = parent.getWidth();
            boolean isVertical = axis == VERTICAL;
            Insets insets = parent.getInsets();
            int bw = leftMargin + rightMargin + insets.left + insets.right;
            int bh = topMargin + bottomMargin + insets.top + insets.bottom;
            Dimension size = new Dimension(bw, bh);
            int i2 = 0;
            int x2 = 0;
            int w2 = 0;
            int h2 = 0;
            int i3 = 0;

            int j3;
            if (isVertical) {
                j3 = pw - bh;
            } else {
                j3 = ph - bw;
            }

            int count = parent.getComponentCount();
            int l3 = -1;

            for (int i = 0; i < count; i++) {
                Component component = parent.getComponent(i);
                int h1;
                if (component instanceof Comp) {
                    Comp comp = (Comp) component;
                    switch (comp.type) {
                    case Comp.WARP:
                        i2 = 0;
                        if (comp.id >= 0) {
                            x2 = h2 + comp.id;
                        } else {
                            x2 = h2 + lineSpace;
                        }
                        break;
                    case Comp.SPACE:
                        if (l3 < 0)
                            l3 = comp.id;
                        else
                            l3 += comp.id;
                        break;
                    case Comp.BAND:
                        if (comp.id > 0) {
                            h1 = x2 + comp.id;
                            if (h1 > h2)
                                h2 = h1;
                        }
                        break;
                    case Comp.GLUE:
                        break;
                    case Comp.LINE_GLUE:
                        i2 = comp.id < 0 ? pw : 0;
                        x2 = h2 + lineSpace;
                        break;
                    default:
                        break;
                    }

                } else if (component.isVisible()) {
                    int w1;
                    Dimension dimension1 = component.getPreferredSize();
                    if (isVertical) {
                        h1 = dimension1.height;
                        w1 = dimension1.width;
                    } else {
                        h1 = dimension1.width;
                        w1 = dimension1.height;
                    }
                    if (h1 > i3)
                        i3 = h1;
                    if (i2 == 0) {
                        if ((i2 = h1) > w2)
                            w2 = i2;
                        h2 = x2 + w1;
                    } else {
                        int i5;
                        if (l3 < 0) {
                            i5 = i2 + itemSpace + h1;
                        } else {
                            i5 = i2 + l3 + h1;
                            l3 = -1;
                        }
                        if (i5 > j3 && !nowrap) {
                            if ((i2 = h1) > w2)
                                w2 = i2;
                            h2 = (x2 = h2 + lineSpace) + w1;
                        } else {
                            if ((i2 = i5) > w2)
                                w2 = i2;
                            int j5 = x2 + w1;
                            if (j5 > h2) {
                                h2 = j5;
                            }
                        }
                    }
                }
            }

            if (w2 < i3)
                w2 = i3;
            if (isVertical) {
                size.width += h2;
                size.height += w2;
            } else {
                size.width += w2;
                size.height += h2;
            }
            return size;
        }
    }

    public void layoutContainer(Container parent) {
        synchronized (parent.getTreeLock()) {
            // JVM INSTR monitorenter ;
            int ph = parent.getHeight();
            int pw = parent.getWidth();
            boolean isVertical = axis == VERTICAL;
            Insets inset = parent.getInsets();
            int bw = leftMargin + rightMargin + inset.left + inset.right;
            int bh = topMargin + bottomMargin + inset.top + inset.bottom;
            int i2 = 0;
            int x2 = 0;
            int k2 = 0;
            int h2;
            int w2;

            if (isVertical) { // goto _L2; else goto _L1
// _L1:
                h2 = ph - bh;
                // j1;
                // k1;
                // goto _L3
                w2 = pw - bw;
// _L2:
            } else {
                h2 = pw - bw;
                // i1;
                // l1;
                // _L3:
                w2 = ph - bh;
            }
            // JVM INSTR isub ;
            // int i3;
            // i3;
            List<Group> groups = new ArrayList<Group>();
            Group group = null;
            int j3 = 0;
            int count = parent.getComponentCount();
            int l3 = -1;

// _L20:
            for (int i = 0; i < count; i++) { // goto _L5; else goto _L4
// _L4:
                Component component = parent.getComponent(i);
                int w1;
                int h1;
                if (component instanceof Comp) {
                    Comp comp = (Comp) component;
                    switch (comp.type) {
                    case Comp.WARP:
                        group = new Group(this, null);
                        groups.add(group);
                        i2 = 0;
                        if (comp.id < 0)
                            x2 = k2 + lineSpace;
                        else
                            x2 = k2 + comp.id;
                        break;
                    case Comp.SPACE:
                        if (l3 < 0)
                            l3 = comp.id;
                        else
                            l3 += comp.id;
                        break;
                    case Comp.BAND:
                        if (comp.id > 0) {
                            if (group == null) {
                                group = new Group(this, null);
                                groups.add(group);
                            }
                            if (comp.id > group.size_b)
                                group.size_b = comp.id;
                            int l4 = x2 + comp.id;
                            if (l4 > k2)
                                k2 = l4;
                        }
                        break;
                    case Comp.GLUE:
                        if (group == null) {
                            group = new Group(this, null);
                            groups.add(group);
                        }
                        group.addComponent(comp);
                        group.count_c++;
                        break;
                    case Comp.LINE_GLUE:
                        i2 = 0;
                        x2 = k2 + lineSpace;
                        group = new Group(this, null);
                        group.isVisible = true;
                        groups.add(group);
                        j3++;
                        group = new Group(this, null);
                        groups.add(group);
                        break;
                    }
                    continue; // 825
                }
                if (!component.isVisible())
                    continue; // 825
                Dimension dimension = component.getPreferredSize();
                component.setSize(dimension);
                if (isVertical) {
                    w1 = dimension.height;
                    h1 = dimension.width;
                } else {
                    w1 = dimension.width;
                    h1 = dimension.height;
                }
                if (group == null) {
                    group = new Group(this, null);
                    groups.add(group);
                }
                if (i2 == 0) { // goto _L7; else goto _L6
// _L6:
                    if (isVertical) { // goto _L9; else goto _L8
// _L8:
                    // component;
                    // j2;
                    // 0;
                    // goto _L10
                        component.setLocation(x2, 0);
// _L9:
                    } else {
                        // component;
                        // 0;
                        // j2;
// _L10:
                        component.setLocation(0, x2);
                    }
                    // setLocation();
                    i2 = w1;
                    k2 = x2 + h1;
                    group.addComponent(component);
                    group.a = i2;
                    // goto _L11
// _L7:
                } else {
                    int k5;
                    if (l3 < 0) {
                        k5 = i2 + itemSpace + w1;
                    } else {
                        k5 = i2 + l3 + w1;
                        l3 = -1;
                    }
                    if (k5 > h2 && !nowrap) { // goto _L13; else goto _L12
// _L12:
                        i2 = w1;
                        k2 = (x2 = k2 + lineSpace) + h1;
                        if (isVertical) { // goto _L15; else goto _L14
// _L14:
                        // component;
                        // j2;
                        // 0;
                        // goto _L16
                            component.setLocation(x2, 0);
// _L15:
                        } else {
                            // component;
                            // 0;
                            // j2;
// _L16:
                            component.setLocation(0, x2);
                        }
                        // setLocation();
                        group = new Group(this, null);
                        groups.add(group);
                        group.addComponent(component);
                        group.a = i2;
                        // goto _L11
// _L13:
                    } else {
                        if (isVertical) { // goto _L18; else goto _L17
// _L17:
                        // component;
                        // j2;
                        // k5 - i5;
                        // goto _L19
                            component.setLocation(x2, k5 - w1);
// _L18:
                        } else {
                            // component;
                            // k5 - i5;
                            // j2;
// _L19:
                            component.setLocation(k5 - w1, x2);
                        }
                        // setLocation();
                        i2 = k5;
                        int l5 = x2 + h1;
                        if (l5 > k2)
                            k2 = l5;
                        group.addComponent(component);
                        group.a = i2;
                        if (group.size_b < h1)
//                            continue; /* Loop/switch isn't completed */
                            break;
// _L11:
                    }
                }
                group.size_b = h1;
// 825
                // i4++;
                // goto _L20
// _L5:
            }

            Group[] gs = groups.toArray(new Group[groups.size()]);
            a(gs, isVertical, k2);
            a(gs, isVertical);
            b(gs, isVertical, h2);
            a(gs, isVertical, k2, w2, j3);
            for (int i = 0; i < count; i++) {
                Component comp = parent.getComponent(i);
                if (comp.isVisible()) {
                    Point point = comp.getLocation();
                    point.x += leftMargin;
                    point.y += topMargin;
                    comp.setLocation(point.x, point.y);
                }
            }

//            return;
        }
        // Exception exception;
        // exception;
        // obj;
        // JVM INSTR monitorexit ;
        // throw exception;
    }

    private void a(Group[] groups, boolean isVertical, int i1) {
        boolean flag1;
        boolean flag2;
label0: {
            if (origin == LEFT_TOP)
                return;
            flag1 = false;
            flag2 = false;
            if (origin == RIGHT_TOP) {
                if (!isVertical) {
                    flag1 = true;
                    break label0;
                }
            } else {
                if (origin == LEFT_BOTTOM) {
                    if (isVertical)
                        flag1 = true;
                    else
                        flag2 = true;
                    break label0;
                }
                if (origin != RIGHT_BOTTOM)
                    break label0;
                flag1 = true;
            }
            flag2 = true;
        }
        for (int i = 0; i < groups.length; i++) {
            Group group = groups[i];
            if (group.isVisible)
                continue;
            int count = group.size();
            for (int j = 0; j < count; j++) {
                Component comp = group.getComponent(j);
                if (comp instanceof Comp)
                    continue;
                Point point = comp.getLocation();
                if (flag1)
                    if (isVertical)
                        point.y = group.a - comp.getHeight() - point.y;
                    else
                        point.x = group.a - comp.getWidth() - point.x;
                if (flag2)
                    if (isVertical)
                        point.x = i1 - comp.getWidth() - point.x;
                    else
                        point.y = i1 - comp.getHeight() - point.y;
                comp.setLocation(point.x, point.y);
            }
        }
    }

    private void a(Group[] groups, boolean isVertical) {

        if (fix == ORIGIN_SIDE)
            return;

        boolean isCenter = fix == CENTER;
// _L15:
        for (int i = 0; i < groups.length; i++) { // goto _L2; else goto _L1
// _L1:
            Group group = groups[i];
            if (group.isVisible)
                continue; // 260
            int count = group.size();
// _L14:
            for (int j = 0; j < count; j++) { // goto _L4; else goto _L3
// _L3:
                Component component = group.getComponent(j);
                if (component instanceof Comp)
                    continue; // 254
                Point point = component.getLocation();
                if (isVertical) { // goto _L6; else goto _L5
// _L5:
                    int p1 = isCenter ? (group.size_b - component.getWidth()) / 2 : group.size_b - component.getWidth();
                    if (origin == LEFT_TOP || origin == LEFT_BOTTOM) { // goto _L8; else goto _L7
// _L7:
                    // point;
                    // point.x + l1;
                    // goto _L9
                        point.x += p1;
// _L8:
                    } else {
                        // point;
                        // point.x - l1;
                        // _L9:
                        point.x -= p1;
                    }
                    // x;
                    // goto _L10
// _L6:
                } else {
                    int p1 = isCenter ? (group.size_b - component.getHeight()) / 2 : group.size_b - component.getHeight();
                    if (origin == LEFT_TOP || origin == RIGHT_TOP) { // goto _L12; else goto _L11
// _L11:
                    // point;
                    // point.y + l1;
                    // goto _L13
                        point.y += p1;
// _L12:
                    } else {
                        // point;
                        // point.y - l1;
// _L13:
                        point.y -= p1;
                    }
                    // y;
// _L10:
                }
                component.setLocation(point.x, point.y);
// 254
                // goto _L14
// _L4:
            }
// 260
            // i1++;
            // goto _L15
// _L2:
        }
    }

    private void b(Group[] groups, boolean isVertical, int i1) {
        boolean flag1 = true;
        if (align == LEADING && origin == LEFT_TOP ||
            !isVertical && align == LEADING && origin == LEFT_BOTTOM ||
            isVertical && align == LEADING && origin == RIGHT_TOP ||
            align == TRAILING && origin == RIGHT_BOTTOM ||
            !isVertical && align == TRAILING && origin == RIGHT_TOP ||
            isVertical && align == TRAILING && origin == LEFT_BOTTOM)
            flag1 = false;
        boolean alignCenter = false;
        boolean alignBoth = false;
        if (align == CENTER)
            alignCenter = true;
        else if (align == BOTH)
            alignBoth = true;
        boolean flag4 = false;
        if (origin == RIGHT_BOTTOM || !isVertical && origin == RIGHT_TOP || isVertical && origin == LEFT_BOTTOM)
            flag4 = true;
        
// _L17:
        for (int i = 0; i < groups.length; i++) { // goto _L2; else goto _L1
// _L1:
            Group group = groups[i];

            if (group.isVisible)
                continue; // 731

            int max_k1 = i1 - group.a;
            int count = group.size();

            int quotient;
            int reminder;
            int l2;
            Component component1 = null;

            if (group.count_c > 0 && max_k1 > 0) { // goto _L4; else goto _L3
// _L3:
                quotient = max_k1 / group.count_c;
                reminder = max_k1 % group.count_c;
                l2 = 0;
// _L10:
                for (int j = 0; j < count; j++) {
                    if (flag4) { // goto _L6; else goto _L5
// _L5:
                    // t1;
                    // l1 - i3 - 1;
                    // goto _L7
                        component1 = group.getComponent(count - j - 1);
// _L6:
                    } else {
                        // t1;
                        // i3;
// _L7:
                        component1 = group.getComponent(j);
                    }
                    // a();

                    // JVM INSTR dup ;
                    // Component component1;
                    // component1;
                    // JVM INSTR instanceof #2 <Class I>;
                    if (component1 instanceof Comp) {
                        // JVM INSTR ifeq 362;
                        // goto _L8 _L9
                        // _L8:
                        // break MISSING_BLOCK_LABEL_326;
                        // _L9:
                        // break MISSING_BLOCK_LABEL_362;
                        Comp comp = (Comp) component1;
                        if (comp.type == 4) {
                            l2 += quotient;
                            if (reminder > 0) {
                                l2++;
                                reminder--;
                            }
                        }
                        // break MISSING_BLOCK_LABEL_420;
                    } else {
                        if (l2 > 0) {
                            Point point1 = component1.getLocation();
                            if (isVertical)
                                point1.y += l2;
                            else
                                point1.x += l2;
                            component1.setLocation(point1.x, point1.y);
                        }
                    }
// 420
                    // if(true) // goto _L10; else goto _L4
// _L4: 429
                }
                continue; // 731
            }

            if (!flag1 || max_k1 == 0)
                continue; // 731

            if (alignBoth && max_k1 > 0 && count > 1) { // goto _L12; else goto _L11
// _L11:
                quotient = max_k1 / (count - 1);
                reminder = max_k1 % (count - 1);
                l2 = 0;
// _L16:
                for (int i3 = 0; i3 < count; i3++) {
                    if (l2 > 0) { // break MISSING_BLOCK_LABEL_569;
                        if (flag4) { // goto _L14; else goto _L13
// _L13:
                            // t1;
                            // l1 - i3 - 1;
                            // goto _L15
                            component1 = group.getComponent(count - i3 - 1);
// _L14:
                        } else {
                            // t1;
                            // i3;
// _L15:
                            component1 = group.getComponent(i3);
                        }
                        // a();
                        // JVM INSTR dup ;
                        // component1;
                        // getLocation();
                        Point point2 = component1.getLocation();
                        // point2;
                        if (isVertical)
                            point2.y += l2;
                        else
                            point2.x += l2;
                        component1.setLocation(point2.x, point2.y);
                    }
// 569
                    l2 += quotient;
                    if (reminder > 0) {
                        l2++;
                        reminder--;
                    }
                    // if(true) // goto _L16; else goto _L12
// _L12:
                }
                continue; // 731
            }
// 596
            for (int j = 0; j < count; j++) {
                Component component = group.getComponent(j);
                if ((component) instanceof Comp)
                    continue;
                Point point = component.getLocation();
                if (alignCenter || alignBoth) {
                    int j3 = max_k1 / 2;
                    if (isVertical)
                        point.y += j3;
                    else
                        point.x += j3;
                } else if (isVertical)
                    point.y += max_k1;
                else
                    point.x += max_k1;
                component.setLocation(point.x, point.y);
                // goto _L17
            }
// 731
// _L2:
        }
    }

    private void a(Group[] groups, boolean isVertical, int i1, int j1, int k1) {
        boolean flag1 = true;
        if (valign == LEADING && origin == LEFT_TOP ||
            !isVertical && valign == LEADING && origin == RIGHT_TOP ||
            isVertical && valign == LEADING && origin == LEFT_BOTTOM ||
            valign == TRAILING && origin == RIGHT_BOTTOM ||
            !isVertical && valign == TRAILING && origin == LEFT_BOTTOM ||
            isVertical && valign == TRAILING && origin == RIGHT_TOP)
            flag1 = false;
        boolean valignCenter = false;
        boolean valignBoth = false;
        if (valign == CENTER)
            valignCenter = true;
        else if (valign == BOTH)
            valignBoth = true;
        boolean flag4 = false;
        if (origin == RIGHT_BOTTOM || !isVertical && origin == LEFT_BOTTOM || isVertical && origin == RIGHT_TOP)
            flag4 = true;
        int l1;
        int quotient;
        int reminder;
        int j3;
        l1 = j1 - i1;

        if (k1 > 0 && l1 > 0) { // break MISSING_BLOCK_LABEL_405;

            quotient = l1 / k1;
            reminder = l1 % k1;
            j3 = 0;
// _L8:
            for (int i = 0; i < groups.length; i++) { // goto _L2; else goto _L1
// _L1:
                Group group;
                if (flag4) { // goto _L4; else goto _L3
// _L3:
                // at;
                // at.length - l3 - 1;
                // goto _L5
                    group = groups[groups.length - i - 1];
// _L4:
                } else {
                    // at;
                    // l3;
// _L5:
                    group = groups[i];
                }
                // JVM INSTR aaload ;
                // JVM INSTR dup ;
                // t t3;
                // t3;

                // d;
                // JVM INSTR ifeq 301;
                // goto _L6 _L7
                if (group.isVisible) {
// _L6:
                    // break MISSING_BLOCK_LABEL_280;
// _L7:
                    // break MISSING_BLOCK_LABEL_301;
                    j3 += quotient;
                    if (reminder > 0) {
                        j3++;
                        reminder--;
                    }
                    continue; // 398
                } else {
                    if (j3 <= 0)
                        continue; // 398
                }

                int count = group.size();
                for (int j = 0; j < count; j++) {
                    Component component2 = group.getComponent(j);
                    if ((component2) instanceof Comp)
                        continue;
                    Point point2 = component2.getLocation();
                    if (isVertical)
                        point2.x += j3;
                    else
                        point2.y += j3;
                    component2.setLocation(point2.x, point2.y);
                }
                // goto _L8
// _L2:
            }

            return;
        }
// 405
        if (flag1 && l1 != 0) { // break MISSING_BLOCK_LABEL_865;

            if (valignBoth && l1 > 0 && groups.length > 1) { // break MISSING_BLOCK_LABEL_606;
                quotient = l1 / (groups.length - 1);
                reminder = l1 % (groups.length - 1);
                j3 = 0;
                
// _L14:
                for (int j = 0; j < groups.length; j++) { // goto _L10; else goto _L9
// _L9:
                    Group group;
                    if (flag4) { // goto _L12; else goto _L11
// _L11:
                    // at;
                    // at.length - l3 - 1;
                    // goto _L13
                        group = groups[groups.length - j - 1];
// _L12:
                    } else {
                        // at;
                        // l3;
// _L13:
                        group = groups[j];
                    }
                    // JVM INSTR aaload ;
                    // t3;
                    if (j3 > 0) {
                        int count = group.size();
                        for (int k = 0; k < count; k++) {
                            Component component3 = group.getComponent(k);
                            if (component3 instanceof Comp)
                                continue;
                            Point point3 = component3.getLocation();
                            if (isVertical)
                                point3.x += j3;
                            else
                                point3.y += j3;
                            component3.setLocation(point3.x, point3.y);
                        }

                    }
                    j3 += quotient;
                    if (reminder > 0) {
                        j3++;
                        reminder--;
                    }
                    // goto _L14
// _L10:
                }
                return;
            }
// 606
            if (valignCenter || valignBoth) {
                int j2 = l1 / 2;
                for (int j = 0; j < groups.length; j++) {
                    Group group = groups[j];
                    if (group.isVisible)
                        continue;
                    int count = group.size();
                    for (int k = 0; k < count; k++) {
                        Component component1 = group.getComponent(k);
                        if (component1 instanceof Comp)
                            continue;
                        Point point1 = component1.getLocation();
                        if (isVertical)
                            point1.x += j2;
                        else
                            point1.y += j2;
                        component1.setLocation(point1.x, point1.y);
                    }
                }
                return;
            }

            for (int i = 0; i < groups.length; i++) {
                Group group = groups[i];
                if (group.isVisible)
                    continue;
                int count = group.size();
                for (int j = 0; j < count; j++) {
                    Component component = group.getComponent(j);
                    if (component instanceof Comp)
                        continue;
                    Point point = component.getLocation();
                    if (isVertical)
                        point.x += l1;
                    else
                        point.y += l1;
                    component.setLocation(point.x, point.y);
                }
            }
        }
// 865
    }

    // origin
    public static final int LEFT_TOP = 1;
    public static final int LEFT_BOTTOM = 2;
    public static final int RIGHT_TOP = 3;
    public static final int RIGHT_BOTTOM = 4;

    // axis
    public static final int HORIZONTAL = 1;
    public static final int VERTICAL = 2;

    // align
    public static final int LEADING = 1;
    public static final int CENTER = 2;
    public static final int TRAILING = 3;
    public static final int BOTH = 4;

    // fix
    public static final int ORIGIN_SIDE = 1;
    public static final int REVERSE_SIDE = 3;

    private int origin;
    private int axis;
    private int align;
    private int valign;
    private int fix;
    private int itemSpace;
    private int lineSpace;
    private int topMargin;
    private int bottomMargin;
    private int leftMargin;
    private int rightMargin;
    private boolean nowrap;
}

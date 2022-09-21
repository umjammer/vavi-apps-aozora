/*
 * http://www.35-35.net/aozora/
 */

package com.soso.sgui.letter;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;


/**
 * Represents one character.
 */
public abstract class SLetterCell {

    protected SLetterCell() {
        constraints = new LinkedHashSet<>();
        decorators = new LinkedHashSet<>();
    }

    public void addConstraint(SLetterConstraint constraint) {
        if (constraint == null)
            throw new IllegalArgumentException("Constraint cannot be null");
        for (SLetterConstraint aConstraint : getConstraints()) {
            if (!aConstraint.isConcurrentable(constraint))
                removeConstraint(aConstraint);
        }

        constraints.add(constraint);
    }

    public boolean removeConstraint(SLetterConstraint constraint) {
        return constraints.remove(constraint);
    }

    public SLetterConstraint[] getConstraints() {
        SLetterConstraint[] constraints = this.constraints.toArray(new SLetterConstraint[0]);
        if (constraints == null)
            constraints = new SLetterConstraint[0];
        return constraints;
    }

    public boolean isConstraintSet() {
        return constraints.size() != 0;
    }

    public boolean isConstraintSet(SLetterConstraint constraint) {
        for (SLetterConstraint sLetterConstraint : constraints)
            if (sLetterConstraint.equals(constraint))
                return true;

        return false;
    }

    public void addDecorator(SLetterCellDecorator decorator) {
        if (decorator == null) {
            throw new IllegalArgumentException("Decorator cannot be null");
        }
        decorators.add(decorator);
    }

    public SLetterCellDecorator[] getDecorators() {
        return decorators.toArray(new SLetterCellDecorator[0]);
    }

    public boolean removeDecorator(SLetterCellDecorator decorator) {
        decorator.removeDecoration(this);
        return decorators.remove(decorator);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        sb.append("[constraints=");
        sb.append(constraints != null ? Arrays.toString(constraints.toArray()) : "null");
        sb.append(']');
        sb.append(getText());
        return sb.toString();
    }

    protected void setParent(SLetterPane letterPane) {
        this.letterPane = letterPane;
    }

    public SLetterPane getParent() {
        return letterPane;
    }

    protected SLetterConstraint.ORIENTATION getOrientation() {
        for (SLetterConstraint constraint : getConstraints()) {
            if (constraint instanceof SLetterConstraint.ORIENTATION)
                return (SLetterConstraint.ORIENTATION) constraint;
        }

        SLetterPane pane = getParent();
        if (pane != null)
            return pane.getOrientation();
        else
            return null;
    }

    public abstract void paintCell(Graphics g, Rectangle cellBounds);

    public abstract void paintRuby(Graphics g, Rectangle rubyBounds);

    public abstract String getText();

    private Collection<SLetterConstraint> constraints;
    private SLetterPane letterPane;
    private Collection<SLetterCellDecorator> decorators;
}

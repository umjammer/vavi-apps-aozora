/*
 * http://www.35-35.net/aozora/
 */

package com.soso.sgui.letter;

import java.util.Collection;
import java.util.LinkedHashSet;

import static javax.swing.SwingUtilities.invokeLater;


final class SLetterPaneObserverSupport {

    SLetterPaneObserverSupport() {
        observers = new LinkedHashSet<>();
    }

    void addObserver(SLetterPaneObserver observer) {
        if (observer == null) {
            throw new IllegalArgumentException("Observer cannot be null");
        }
        observers.add(observer);
    }

    void removeObserver(SLetterPaneObserver observer) {
        observers.remove(observer);
    }

    void orientationChanged(final SLetterConstraint.ORIENTATION oldValue, final SLetterConstraint.ORIENTATION newValue) {
        for (final SLetterPaneObserver observer : observers) {
            invokeLater(() -> observer.orientationChanged(oldValue, newValue));
        }
    }

    void cellAdded(final SLetterCell cell) {
        for (final SLetterPaneObserver observer : observers) {
            invokeLater(() -> observer.cellAdded(cell));
        }
    }

    void cellRemoved(final SLetterCell cell) {
        for (final SLetterPaneObserver observer : observers) {
            invokeLater(() -> observer.cellRemoved(cell));
        }
    }

    void rowCountChanged(final int oldValue, final int newValue) {
        for (final SLetterPaneObserver observer : observers) {
            invokeLater(() -> observer.rowCountChanged(oldValue, newValue));
        }
    }

    void colCountChanged(final int oldValue, final int newValue) {
        for (final SLetterPaneObserver observer : observers) {
            invokeLater(() -> observer.colCountChanged(oldValue, newValue));
        }
    }

    void rowRangeChanged(final int oldValue, final int newValue) {
        for (final SLetterPaneObserver observer : observers) {
            invokeLater(() -> observer.rowRangeChanged(oldValue, newValue));
        }
    }

    void colRangeChanged(final int oldValue, final int newValue) {
        for (final SLetterPaneObserver observer : observers) {
            invokeLater(() -> observer.colRangeChanged(oldValue, newValue));
        }
    }

    void fontRangeRatioChanged(final float oldValue, final float newValue) {
        for (final SLetterPaneObserver observer : observers) {
            invokeLater(() -> observer.fontRangeRatioChanged(oldValue, newValue));
        }
    }

    void rowSpaceChanged(final int oldValue, final int newValue) {
        for (final SLetterPaneObserver observer : observers) {
            invokeLater(() -> observer.rowSpaceChanged(oldValue, newValue));
        }
    }

    void colSpaceChanged(final int oldValue, final int newValue) {
        for (final SLetterPaneObserver observer : observers) {
            invokeLater(() -> observer.colSpaceChanged(oldValue, newValue));
        }
    }

    void letterBorderRendererChanged(final SLetterBorderRenderer oldValue, final SLetterBorderRenderer newValue) {
        for (final SLetterPaneObserver observer : observers) {
            invokeLater(() -> observer.borderRendererChanged(oldValue, newValue));
        }
    }

    void rowColCountChangableChanged(final boolean value) {
        for (final SLetterPaneObserver observer : observers) {
            invokeLater(() -> observer.rowColCountChangeableChanged(value));
        }
    }

    void fontSizeChangeableChanged(final boolean value) {
        for (final SLetterPaneObserver observer : observers) {
            invokeLater(() -> observer.fontSizeChangeableChanged(value));
        }
    }

    private final Collection<SLetterPaneObserver> observers;
}

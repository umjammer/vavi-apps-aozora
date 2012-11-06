/*
 * http://www.35-35.net/aozora/
 */

package com.soso.sgui.letter;

import java.util.Collection;
import java.util.LinkedHashSet;

import javax.swing.SwingUtilities;


final class SLetterPaneObserverSupport {

    SLetterPaneObserverSupport() {
        observers = new LinkedHashSet<SLetterPaneObserver>();
    }

    final void addObserver(SLetterPaneObserver observer) {
        if (observer == null) {
            throw new IllegalArgumentException("Observer cannot be null");
        }
        observers.add(observer);
    }

    final void removeObserver(SLetterPaneObserver observer) {
        observers.remove(observer);
    }

    final void orientationChanged(final SLetterConstraint.ORIENTATION oldValue, final SLetterConstraint.ORIENTATION newValue) {
        for (final SLetterPaneObserver observer : observers) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    observer.orientationChanged(oldValue, newValue);
                }
            });
        }
    }

    final void cellAdded(final SLetterCell cell) {
        for (final SLetterPaneObserver observer : observers) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    observer.cellAdded(cell);
                }
            });
        }
    }

    final void cellRemoved(final SLetterCell cell) {
        for (final SLetterPaneObserver observer : observers) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    observer.cellRemoved(cell);
                }
            });
        }
    }

    final void rowCountChanged(final int oldValue, final int newValue) {
        for (final SLetterPaneObserver observer : observers) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    observer.rowCountChanged(oldValue, newValue);
                }
            });
        }
    }

    final void colCountChanged(final int oldValue, final int newValue) {
        for (final SLetterPaneObserver observer : observers) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    observer.colCountChanged(oldValue, newValue);
                }
            });
        }
    }

    final void rowRangeChanged(final int oldValue, final int newValue) {
        for (final SLetterPaneObserver observer : observers) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    observer.rowRangeChanged(oldValue, newValue);
                }
            });
        }
    }

    final void colRangeChanged(final int oldValue, final int newValue) {
        for (final SLetterPaneObserver observer : observers) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    observer.colRangeChanged(oldValue, newValue);
                }
            });
        }
    }

    final void fontRangeRatioChanged(final float oldValue, final float newValue) {
        for (final SLetterPaneObserver observer : observers) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    observer.fontRangeRatioChanged(oldValue, newValue);
                }
            });
        }
    }

    final void rowSpaceChanged(final int oldValue, final int newValue) {
        for (final SLetterPaneObserver observer : observers) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    observer.rowSpaceChanged(oldValue, newValue);
                }
            });
        }
    }

    final void colSpaceChanged(final int oldValue, final int newValue) {
        for (final SLetterPaneObserver observer : observers) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    observer.colSpaceChanged(oldValue, newValue);
                }
            });
        }
    }

    final void letterBorderRendarerChanged(final SLetterBorderRendarer oldValue, final SLetterBorderRendarer newValue) {
        for (final SLetterPaneObserver observer : observers) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    observer.borderRendarerChanged(oldValue, newValue);
                }
            });
        }
    }

    final void rowColCountChangableChanged(final boolean value) {
        for (final SLetterPaneObserver observer : observers) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    observer.rowColCountChangableChanged(value);
                }
            });
        }
    }

    final void fontSizeChangableChanged(final boolean value) {
        for (final SLetterPaneObserver observer : observers) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    observer.fontSizeChangableChanged(value);
                }
            });
        }
    }

    private Collection<SLetterPaneObserver> observers;
}

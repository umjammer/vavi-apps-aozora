/*
 * http://www.35-35.net/aozora/
 */

package com.soso.aozora.event;

import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import com.soso.aozora.boot.AozoraContext;
import com.soso.aozora.core.AozoraEnv;
import com.soso.aozora.data.AozoraComment;
import com.soso.aozora.viewer.AozoraCommentDecorator;


public class AozoraListenerManager implements AozoraListener {

    public AozoraListenerManager(AozoraContext context) {
        this.context = context;
    }

    private AozoraContext getAzContext() {
        return context;
    }

    public void add(AozoraListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    public void remove(AozoraListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    private AozoraListener[] getListeners() {
        synchronized (listeners) {
            return listeners.toArray(new AozoraListener[listeners.size()]);
        }
    }

    public void lineModeChanged(final AozoraEnv.LineMode lineMode) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    lineModeChanged(lineMode);
                }
            });
        } else if (lineMode != getAzContext().getLineMode()) {
            getAzContext().setLineMode(lineMode);
        } else {
            for (AozoraListener listener : getListeners()) {
                try {
                    listener.lineModeChanged(lineMode);
                } catch (Exception e) {
                    getAzContext().log(e);
                }
            }
        }
    }

    public void cacheUpdated(final String cacheID) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    cacheUpdated(cacheID);
                }
            });
        } else {
            for (AozoraListener listener : getListeners()) {
                try {
                    listener.cacheUpdated(cacheID);
                } catch (Exception e) {
                    getAzContext().log(e);
                }
            }
        }
    }

    public void cacheDeleted(final String cacheID) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    cacheDeleted(cacheID);
                }
            });
        } else {
            for (AozoraListener listener : getListeners()) {
                try {
                    listener.cacheDeleted(cacheID);
                } catch (Exception e) {
                    getAzContext().log(e);
                }
            }
        }
    }

    public void commentAdded(final AozoraComment comment) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    commentAdded(comment);
                }
            });
        } else {
            for (AozoraListener listener : getListeners()) {
                try {
                    listener.commentAdded(comment);
                } catch (Exception e) {
                    getAzContext().log(e);
                }
            }
        }
    }

    public void commentTypeChanged(final AozoraCommentDecorator.CommentType commentType) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    commentTypeChanged(commentType);
                }
            });
        } else if (commentType != getAzContext().getSettings().getCommentType()) {
            getAzContext().getSettings().setCommentType(commentType);
        } else {
            for (AozoraListener listener : getListeners()) {
                try {
                    listener.commentTypeChanged(commentType);
                } catch (Exception e) {
                    getAzContext().log(e);
                }
            }
        }
    }

    private final AozoraContext context;

    private final List<AozoraListener> listeners = new ArrayList<AozoraListener>();
}

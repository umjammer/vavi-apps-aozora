/*
 * http://www.35-35.net/aozora/
 */

package com.soso.aozora.event;

import java.util.EventListener;

import com.soso.aozora.core.AozoraEnv;
import com.soso.aozora.data.AozoraComment;
import com.soso.aozora.viewer.AozoraCommentDecorator;


public interface AozoraListener extends EventListener {

    public abstract void lineModeChanged(AozoraEnv.LineMode lineMode);

    public abstract void cacheUpdated(String cacheID);

    public abstract void cacheDeleted(String cacheID);

    public abstract void commentAdded(AozoraComment comment);

    public abstract void commentTypeChanged(AozoraCommentDecorator.CommentType commentType);
}

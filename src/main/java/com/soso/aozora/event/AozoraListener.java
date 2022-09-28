/*
 * http://www.35-35.net/aozora/
 */

package com.soso.aozora.event;

import java.util.EventListener;

import com.soso.aozora.core.AozoraEnv;
import com.soso.aozora.data.AozoraComment;
import com.soso.aozora.viewer.AozoraCommentDecorator;


public interface AozoraListener extends EventListener {

    void lineModeChanged(AozoraEnv.LineMode lineMode);

    void cacheUpdated(String cacheID);

    void cacheDeleted(String cacheID);

    void commentAdded(AozoraComment comment);

    void commentTypeChanged(AozoraCommentDecorator.CommentType commentType);
}

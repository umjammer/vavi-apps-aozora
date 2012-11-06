/*
 * http://www.35-35.net/aozora/
 */

package com.soso.aozora.event;

import com.soso.aozora.core.AozoraEnv;
import com.soso.aozora.data.AozoraComment;
import com.soso.aozora.viewer.AozoraCommentDecorator;


public class AozoraListenerAdapter implements AozoraListener {

    public void lineModeChanged(AozoraEnv.LineMode lineMode) {
    }

    public void cacheUpdated(String cacheID) {
    }

    public void cacheDeleted(String cacheID) {
    }

    public void commentAdded(AozoraComment comment) {
    }

    public void commentTypeChanged(AozoraCommentDecorator.CommentType commentType) {
    }
}

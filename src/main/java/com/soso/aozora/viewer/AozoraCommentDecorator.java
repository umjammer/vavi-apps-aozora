/*
 * http://www.35-35.net/aozora/
 */

package com.soso.aozora.viewer;

import com.soso.aozora.boot.AozoraContext;
import com.soso.aozora.data.AozoraComment;
import com.soso.sgui.letter.SLetterCell;
import com.soso.sgui.letter.SLetterCellDecorator;
import com.soso.sgui.letter.SLetterPane;


public abstract class AozoraCommentDecorator implements SLetterCellDecorator {

    public enum CommentType {

        ballone("吹き出し表示", "新しいコメントの吹き出しが一番上にきます。"),
        linenote("行間に表示", "新しいコメントが一番コントラストが強くなります。"),
        none("表示しない", "コメントの投稿も出来なくなります。");

        public String getTitle() {
            return title;
        }

        public String getDescription() {
            return description;
        }

        private final String title;
        private final String description;

        CommentType(String title, String description) {
            this.title = title;
            this.description = description;
        }
    }

    static AozoraCommentDecorator newInstance(AozoraContext context, AozoraComment comment, SLetterPane textPane, SLetterCell firstCell) {
        CommentType commentType = context.getSettings().getCommentType();
        if (commentType == null)
            return null;

        switch (commentType) {
        case ballone:
            return new AozoraCommentBalloonDecorator(context, comment, textPane, firstCell);
        case linenote:
            return new AozoraCommentNoteDecorator(context, comment, textPane, firstCell);
        case none:
            return null;
        }
        throw new UnsupportedOperationException("No Decorator is mapped for CommentType[" + commentType + "]");
    }

    protected AozoraCommentDecorator(AozoraContext context, AozoraComment comment, SLetterPane textPane, SLetterCell firstCell) {
        this.context = context;
        this.comment = comment;
        this.textPane = textPane;
        this.firstCell = firstCell;
    }

    protected AozoraContext getAzContext() {
        return context;
    }

    protected AozoraComment getComment() {
        return comment;
    }

    protected SLetterPane getTextPane() {
        return textPane;
    }

    protected SLetterCell getFirstCell() {
        return firstCell;
    }

    private final AozoraContext context;
    private final AozoraComment comment;
    private final SLetterPane textPane;
    private final SLetterCell firstCell;
}

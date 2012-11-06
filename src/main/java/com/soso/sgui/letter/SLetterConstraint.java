/*
 * http://www.35-35.net/aozora/
 */

package com.soso.sgui.letter;

import com.soso.sgui.text.CharacterUtil;


public interface SLetterConstraint {

    public enum ORIENTATION implements SLetterConstraint {

        LRTB,
        TBRL,
        RLTB,
        TBLR;

        public final boolean isConcurrentable(SLetterConstraint constraint) {
            return !(constraint instanceof ORIENTATION);
        }

        public final boolean isHorizonal() {
            switch (this) {
            case LRTB:
            case RLTB:
                return true;
            case TBLR:
            case TBRL:
                return false;
            }
            throw new IllegalStateException("UnKnoun ORIENTATION " + this);
        }

        public final boolean isLeftToRight() {
            switch (this) {
            case LRTB:
            case TBLR:
                return true;
            case RLTB:
            case TBRL:
                return false;
            }
            throw new IllegalStateException("UnKnoun ORIENTATION " + this);
        }

        public final boolean isTopToButtom() {
            switch (this) {
            case LRTB:
            case RLTB:
            case TBLR:
            case TBRL:
                return true;
            }
            throw new IllegalStateException("UnKnoun ORIENTATION " + this);
        }
    }

    public enum OVERLAY implements SLetterConstraint {
        FORCE_OVER,
        LINE_TAIL_OVER,
        HALF_OVER,
        HALF_OVER_HEAD,
        HALF_OVER_TAIL;

        public final boolean isConcurrentable(SLetterConstraint constraint) {
            if (!(constraint instanceof OVERLAY))
                return true;
            if (constraint == HALF_OVER_HEAD && this == HALF_OVER_TAIL)
                return false;
            if (constraint == HALF_OVER_TAIL && this == HALF_OVER_HEAD)
                return false;
            return constraint != this;
        }
    }

    public enum TRANS implements SLetterConstraint {
        PUNCTURETE,
        SMALLCHAR;
        public final boolean isConcurrentable(SLetterConstraint constraint) {
            return !(constraint instanceof TRANS);
        }

    }

    public enum ROTATE implements SLetterConstraint {
        GENERALLY,
        REVERSE,
        LR_MIRROR;
        public final boolean isConcurrentable(SLetterConstraint constraint) {
            if (!(constraint instanceof ROTATE))
                return true;
            if (constraint == LR_MIRROR && this != LR_MIRROR)
                return true;
            return constraint != LR_MIRROR && this == LR_MIRROR;
        }

        public static float getHorizontalAlign(char c) {
            if (CharacterUtil.isToRotateKana(c))
                return CharacterUtil.getHorizontalAlign(c);
            else
                return 0.5F;
        }
    }

    public enum BREAK implements SLetterConstraint {
        NEW_PAGE,
        NEW_LINE,
        NEW_LINE_IF_LINE_TAIL,
        BACK_IF_LINE_HEAD;

        public final boolean isConcurrentable(SLetterConstraint constraint) {
            if (!(constraint instanceof BREAK))
                return true;
            switch ((BREAK) constraint) {
            case NEW_PAGE:
                return this == NEW_LINE_IF_LINE_TAIL || this == BACK_IF_LINE_HEAD;
            case NEW_LINE:
                return this == NEW_LINE_IF_LINE_TAIL || this == BACK_IF_LINE_HEAD;
            case NEW_LINE_IF_LINE_TAIL:
                return this == NEW_PAGE || this == NEW_LINE;
            case BACK_IF_LINE_HEAD:
                return this == NEW_PAGE || this == NEW_LINE;
            }
            return false;
        }
    }

    public enum SELECTION implements SLetterConstraint {
        SELECTED;
        public final boolean isConcurrentable(SLetterConstraint constraint) {
            return !(constraint instanceof SELECTION);
        }
    }

    public abstract boolean isConcurrentable(SLetterConstraint constraint);
}

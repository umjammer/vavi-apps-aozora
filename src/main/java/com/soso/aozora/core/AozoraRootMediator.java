/*
 * http://www.35-35.net/aozora/
 */

package com.soso.aozora.core;

import java.awt.Color;
import java.awt.Font;

import com.soso.aozora.data.AozoraAuthor;
import com.soso.aozora.data.AozoraAuthorParserHandler;
import com.soso.aozora.data.AozoraComment;
import com.soso.aozora.data.AozoraWork;
import com.soso.aozora.data.AozoraWorkParserHandler;


public interface AozoraRootMediator {

    public abstract void showViewer(AozoraAuthor author, AozoraWork work);

    public abstract void showViewer(AozoraAuthor author, AozoraWork work, int position);

    public abstract void showViewer(AozoraAuthor author, AozoraWork work, AozoraComment comment);

    public abstract void showLookAndFeelChooser();

    public abstract void showFontChooser();

    public abstract void setViewerFont(Font font);

    public abstract void showColorChooser();

    public abstract void setViewerForeground(Color color);

    public abstract void setViewerBackground(Color color);

    public abstract void showBookmarkList(int x, int y);

    public abstract AozoraAuthor getAozoraAuthor(String authorID);

    public abstract void getAozoraAuthorAsynchronous(String authorID, AozoraAuthorParserHandler callback);

    public abstract AozoraWork getAozoraWork(String workID);

    public abstract void getAozoraWorkAsynchronous(String workID, AozoraWorkParserHandler callback);

    public abstract void focusAuthor(AozoraAuthor author);

    public abstract void focusWork(AozoraWork work);

    public abstract void aboutAozora();

    public abstract void aboutPremier();
}

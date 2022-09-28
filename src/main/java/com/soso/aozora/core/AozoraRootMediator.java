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

    void showViewer(AozoraAuthor author, AozoraWork work);

    void showViewer(AozoraAuthor author, AozoraWork work, int position);

    void showViewer(AozoraAuthor author, AozoraWork work, AozoraComment comment);

    void showLookAndFeelChooser();

    void showFontChooser();

    void setViewerFont(Font font);

    void showColorChooser();

    void setViewerForeground(Color color);

    void setViewerBackground(Color color);

    void showBookmarkList(int x, int y);

    AozoraAuthor getAozoraAuthor(String authorID);

    void getAozoraAuthorAsynchronous(String authorID, AozoraAuthorParserHandler callback);

    AozoraWork getAozoraWork(String workID);

    void getAozoraWorkAsynchronous(String workID, AozoraWorkParserHandler callback);

    void focusAuthor(AozoraAuthor author);

    void focusWork(AozoraWork work);
}

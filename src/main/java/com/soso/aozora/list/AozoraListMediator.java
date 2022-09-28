/*
 * http://www.35-35.net/aozora/
 */

package com.soso.aozora.list;


interface AozoraListMediator extends AozoraTreeSelectionManager {

    void loadWorks(AozoraAuthorNode authorNode);

    boolean isSearchResult();

    void setSearchEnabled(boolean enabled);

    void setAuthorLoaded(boolean isAuthorLoaded);

    void addAozoraAuthorNode(AozoraAuthorNode authorNode);

    AozoraAuthorNode getAozoraAuthorNode(String authorID);

    void focusNext(AozoraAuthorNode fromAuthorNode);

    void focusPrev(AozoraAuthorNode fromAuthorNode);
}

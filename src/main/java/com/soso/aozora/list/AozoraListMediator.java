/*
 * http://www.35-35.net/aozora/
 */

package com.soso.aozora.list;


interface AozoraListMediator extends AozoraTreeSelectionManager {

    public abstract void loadWorks(AozoraAuthorNode authorNode);

    public abstract boolean isSearchResult();

    public abstract void setSearchEnabled(boolean enabled);

    public abstract void setAuthorLoaded(boolean isAuthorLoaded);

    public abstract void addAozoraAuthorNode(AozoraAuthorNode authorNode);

    public abstract AozoraAuthorNode getAozoraAuthorNode(String authorID);

    public abstract void focusNext(AozoraAuthorNode fromAuthorNode);

    public abstract void focusPrev(AozoraAuthorNode fromAuthorNode);
}

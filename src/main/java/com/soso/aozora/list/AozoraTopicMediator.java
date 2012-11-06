/*
 * http://www.35-35.net/aozora/
 */

package com.soso.aozora.list;


interface AozoraTopicMediator extends AozoraTreeSelectionManager {

    public abstract void focusNext(AozoraTopicFolderTreePane fromTreePane);

    public abstract void focusPrev(AozoraTopicFolderTreePane fromTreePane);
}

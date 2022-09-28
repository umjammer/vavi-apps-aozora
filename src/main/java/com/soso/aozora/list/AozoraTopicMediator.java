/*
 * http://www.35-35.net/aozora/
 */

package com.soso.aozora.list;


interface AozoraTopicMediator extends AozoraTreeSelectionManager {

    void focusNext(AozoraTopicFolderTreePane fromTreePane);

    void focusPrev(AozoraTopicFolderTreePane fromTreePane);
}

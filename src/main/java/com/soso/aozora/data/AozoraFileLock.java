/*
 * http://www.35-35.net/aozora/
 */

package com.soso.aozora.data;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileLock;


public class AozoraFileLock {

    AozoraFileLock(File file) throws IOException {
        isReleased = false;
        this.file = file;
        this.file.getParentFile().mkdirs();
        fos = new FileOutputStream(this.file);
        lock = fos.getChannel().tryLock();
        if (lock == null || !lock.isValid())
            throw new IOException("ロックを取得できません。" + this.file);
        else
            return;
    }

    void release() throws IOException {
        try {
            if (lock != null) {
                lock.release();
                lock = null;
            }
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                    fos = null;
                }
            } finally {
                if (file.exists())
                    file.delete();
            }
        }
        isReleased = true;
    }

    boolean isReleased() {
        return isReleased;
    }

    private final File file;
    private FileOutputStream fos;
    private FileLock lock;
    private boolean isReleased;
}

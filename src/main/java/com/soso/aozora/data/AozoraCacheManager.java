/*
 * http://www.35-35.net/aozora/
 */

package com.soso.aozora.data;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import com.soso.aozora.boot.AozoraContext;
import com.soso.aozora.core.AozoraUtil;


public class AozoraCacheManager {

    private static class FileName {

        private String next() {
            synchronized (this) {
                long next = System.currentTimeMillis();
                if (next <= last) {
                    next = last + 1L;
                    if (next < 0L)
                        throw new IllegalStateException("キャッシュファイル名のlong値がオーバーフローしました。");
                }
                last = next;
                return Long.toString(next);
            }
        }

        private void update(String name) {
            long update = Long.parseLong(name);
            synchronized (this) {
                if (last < update)
                    last = update;
            }
        }

        private long last;
    }

    private static class CacheMap implements Comparable<CacheMap> {

        private static class CacheMapEntry implements Comparable<CacheMapEntry> {

            private File getFile() {
                return file;
            }

            private String getName() {
                return name;
            }

            public int compareTo(CacheMapEntry another) {
                return getFile().compareTo(another.getFile());
            }

            private File file;
            private String name;

            private CacheMapEntry(String name, File file) {
                if (name == null)
                    throw new IllegalArgumentException("name cannot be null");
                if (file == null) {
                    throw new IllegalArgumentException("file cannot be null");
                }
                this.name = name;
                this.file = file;
            }
        }

        private String getID() {
            return id;
        }

        private void setLastModified(long lastModified) {
            this.lastModified = lastModified;
        }

        private long getLastModified() {
            return lastModified;
        }

        private String[] getNames() {
            synchronized (entryList) {
                sort();
                int size = entryList.size();
                String[] names = new String[size];
                for (int i = 0; i < size; i++)
                    names[i] = entryList.get(i).getName();

                return names;
            }
        }

        private File getFile(String name) {
            CacheMapEntry entry = getEntry(name);
            if (entry != null)
                return entry.getFile();
            else
                return null;
        }

        private CacheMapEntry getEntry(String name) {
            synchronized (entryList) {
                for (CacheMapEntry entry : entryList) {
                    if (entry.getName().equals(name))
                        return entry;
                }
            }
            return null;
        }

        private CacheMapEntry getEntry(File file) {
            synchronized (entryList) {
                for (CacheMapEntry entry : entryList) {
                    if (entry.getFile().equals(file))
                        return entry;
                }
            }
            return null;
        }

        private void putFile(String name, File file) {
            synchronized (entryList) {
                if (getEntry(file) != null)
                    throw new IllegalArgumentException("Dublicated cache file");
                CacheMapEntry oldEntry = getEntry(name);
                if (oldEntry != null)
                    entryList.remove(oldEntry);
                CacheMapEntry newEntry = new CacheMapEntry(name, file);
                entryList.add(newEntry);
            }
        }

        private void sort() {
            synchronized (entryList) {
                Collections.sort(entryList);
            }
        }

        private void clear() {
            synchronized (entryList) {
                entryList.clear();
            }
        }

        public int compareTo(CacheMap another) {
            int compareLastModified = Long.valueOf(getLastModified()).compareTo(Long.valueOf(another.getLastModified()));
            if (compareLastModified != 0)
                return compareLastModified;
            else
                return id.compareTo(another.id);
        }

        private final String id;
        private final List<CacheMapEntry> entryList;
        private long lastModified;

        private CacheMap(String id) {
            this.id = id;
            entryList = new ArrayList<CacheMapEntry>();
        }
    }

    public AozoraCacheManager(AozoraContext context, File baseDir, boolean isReadOnly) throws IOException {
        this.context = context;
        this.baseDir = baseDir;
        if (!baseDir.exists())
            baseDir.mkdirs();
        this.isReadOnly = isReadOnly;
        if (!isReadOnly)
            tryLock();
        loadMap();
    }

    private AozoraContext getAzContext() {
        return context;
    }

    private void tryLock() throws IOException {
        if (isReadOnly())
            throw new IOException("キャッシュは読み取り専用モードです。");
        synchronized (this) {
            if (lock != null)
                throw new IOException("ロック取得済みです。");
            File lockFile = new File(baseDir, LOCL_FILE);
            lock = new AozoraFileLock(lockFile);
        }
    }

    private void releaseLock() throws IOException {
        if (lock != null && !lock.isReleased())
            lock.release();
    }

    private void checkUpdate() throws IOException {
        if (isReadOnly())
            throw new IOException("キャッシュは読み取り専用モードです。");
        if (lock == null)
            throw new IOException("ロックがありません。");
        if (lock.isReleased())
            throw new IOException("ロックは開放済みです。");
    }

    public boolean isReadOnly() {
        return isReadOnly;
    }

    private File getCacheDir(String id) {
        return new File(baseDir, id);
    }

    public String[] getIDs() {
        Collection<String> idList = new HashSet<String>();
        synchronized (cacheMaps) {
            sortMaps();
            for (CacheMap cacheMap : cacheMaps) {
                idList.add(cacheMap.getID());
            }
        }
        return idList.toArray(new String[idList.size()]);
    }

    public boolean isCached(String id) throws IOException {
        return getCacheMap(id) != null;
    }

    private CacheMap getCacheMap(String id) throws IOException {
        synchronized (cacheMaps) {
            for (CacheMap cacheMap : cacheMaps) {
                if (cacheMap.getID().equals(id))
                    return cacheMap;
            }
        }
        return null;
    }

    public byte[] getCacheBytes(String id, URL url) throws IOException {
        return getCacheBytes(id, urlToName(url));
    }

    public byte[] getCacheBytes(String id, String name) throws IOException {
        InputStream in = null;
        try {
            in = getCacheStream(id, name);
            if (in == null)
                return null;
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) != -1)
                byteOut.write(buf, 0, len);
            byte[] abyte0 = byteOut.toByteArray();
            return abyte0;
        } finally {
            if (in != null)
                in.close();
        }
    }

    public InputStream getCacheStream(String id, URL url) throws IOException {
        return getCacheStream(id, urlToName(url));
    }

    public InputStream getCacheStream(String id, String name) throws IOException {
        synchronized (cacheMaps) {
            for (CacheMap cacheMap : cacheMaps) {
                if (cacheMap.getID().equals(id)) {
                    File file = cacheMap.getFile(name);
                    if (file != null && file.exists() && file.canRead())
                        return new FileInputStream(file);
                }
            }
        }
        return null;
    }

    public void putCache(AozoraAuthor author, AozoraWork work) throws IOException {
        InputStream in = null;
        final String id = work.getID();
        URL contentURL = new URL(work.getContentURL());
        try {
            putCache(id, contentURL);
            in = getCacheStream(work.getID(), contentURL);
            new AozoraContentsParser(getAzContext(), new AozoraContentsParserHandler() {
                public void img(URL src, String alt, boolean isGaiji) {
                    try {
                        putCache(id, src);
                    } catch (IOException e) {
                        getAzContext().log(e);
                        getAzContext().log("画像のキャッシュに失敗しましたが、無視します。" + src);
                    }
                }

                public void ruby(String s, String s1) {
                }

                public void characters(String s) {
                }

                public void newLine() {
                }

                public void otherElement(String s) {
                }

                public void parseFinished() {
                }
            }).parse(in, contentURL);
            putCache(id, "AozoraAuthor", AozoraAuthorParser.toBytes(author));
            putCache(id, "AozoraWork", AozoraWorkParser.toBytes(work));
        } finally {
            if (in != null)
                in.close();
        }
    }

    public void putCache(String id, URL url) throws IOException {
        putCache(id, urlToName(url), AozoraUtil.getInputStream(url));
    }

    public void putCache(String id, String name, byte[] bytes) throws IOException {
        putCache(id, name, new ByteArrayInputStream(bytes));
    }

    public void putCache(String id, String name, InputStream in) throws IOException {
        checkUpdate();
        synchronized (cacheMaps) {
            CacheMap cacheMap = getCacheMap(id);
            if (cacheMap == null)
                cacheMap = new CacheMap(id);
            File oldFile = cacheMap.getFile(name);
            if (oldFile != null && oldFile.exists())
                oldFile.delete();
            String fileName = this.fileName.next();
            File cacheDir = getCacheDir(id);
            if (!cacheDir.exists())
                cacheDir.mkdirs();
            File file = new File(cacheDir, fileName);
            OutputStream out = null;
            try {
                out = new FileOutputStream(file);
                byte buf[] = new byte[1024];
                int len;
                while ((len = in.read(buf)) != -1)
                    out.write(buf, 0, len);
                out.flush();
            } finally {
                out.close();
                if (in != null)
                    in.close();
            }
            cacheMap.putFile(name, file);
            storeMap(cacheMap);
            if (!cacheMaps.contains(cacheMap))
                cacheMaps.add(cacheMap);
            getAzContext().log(" cache | store | " + cacheMap.getID() + " | " + file.getName() + " | " + name);
            getAzContext().getListenerManager().cacheUpdated(cacheMap.getID());
        }
    }

    private String urlToName(URL url) {
        return url.toString();
    }

    public void removeCache(String id) throws IOException {
        synchronized (cacheMaps) {
            CacheMap cacheMap = getCacheMap(id);
            if (cacheMap != null) {
                cacheMap.clear();
                cacheMaps.remove(cacheMap);
            }
            File cacheDir = getCacheDir(id);
            for (File file : cacheDir.listFiles()) {
                file.delete();
            }

            cacheDir.delete();
            getAzContext().log(" cache | remove | " + cacheMap.getID());
            getAzContext().getListenerManager().cacheDeleted(cacheMap.getID());
        }
    }

    private void sortMaps() {
        synchronized (cacheMaps) {
            Collections.sort(cacheMaps);
        }
    }

    private void loadMap() throws IOException {
        synchronized (cacheMaps) {
            getAzContext().log("キャッシュのロードを開始します。base=" + baseDir);
            for (File cacheDir : baseDir.listFiles()) {
                if (!cacheDir.isDirectory())
                    continue;
                File cacheMapFile = new File(cacheDir, CACHE_MAP_FILE);
                if (!cacheMapFile.exists()) {
                    getAzContext().log("キャッシュマップがありません。");
                    continue;
                }
                long lastModified = cacheMapFile.lastModified();
                BufferedReader in = null;
                try {
                    in = new BufferedReader(new InputStreamReader(new FileInputStream(cacheMapFile), "UTF-8"));
                    CacheMap cacheMap = new CacheMap(cacheDir.getName());
                    cacheMap.setLastModified(lastModified);
                    String line;
                    while ((line = in.readLine()) != null) {
                        int sp = line.indexOf('\t');
                        if (sp == -1 || sp == line.length())
                            throw new IllegalStateException(cacheMap.getID() + " のキャッシュマップが不正です。:" + cacheMapFile);
                        String fileStr = line.substring(0, sp);
                        String name = line.substring(sp + 1);
                        File file = new File(cacheDir, fileStr);
                        if (!file.exists())
                            getAzContext().log(cacheMap.getID() + " の存在しないキャッシュを無視します。url=" + name + ";file=" + file);
                        else if (!file.isFile())
                            getAzContext().log(cacheMap.getID() + " のファイルではないキャッシュを無視します。url=" + name + ";file=" + file);
                        else
                            try {
                                fileName.update(fileStr);
                                cacheMap.putFile(name, file);
                                getAzContext().log(" cache | load | " + cacheMap.getID() + " | " + file.getName() + " | " + name);
                            } catch (NumberFormatException e) {
                                getAzContext().log(e);
                                getAzContext().log(cacheMap.getID() + " の不正なファイル名のキャッシュを破棄します。;url=" + name + ";file=" + file);
                            }
                    }
                    cacheMaps.add(cacheMap);
                    getAzContext().log(cacheMap.getID() + " のキャッシュをロードしました。");
                } finally {
                    in.close();
                }
            }
            sortMaps();
            getAzContext().log("キャッシュのロードを完了しました。");
        }
    }

    private void storeMap(CacheMap cacheMap) throws IOException {
        Writer out = null;
        try {
            checkUpdate();
            File cacheDir = new File(baseDir, cacheMap.getID());
            File cacheMapFile = new File(cacheDir, CACHE_MAP_FILE);
            out = new OutputStreamWriter(new FileOutputStream(cacheMapFile, false), "UTF-8");
            synchronized (cacheMap) {
                for (String name : cacheMap.getNames()) {
                    File file = cacheMap.getFile(name);
                    String fileStr = file.getName();
                    out.append(fileStr).append('\t').append(name).append('\n');
                }
            }
            out.flush();
            cacheMap.setLastModified(cacheMapFile.lastModified());
            sortMaps();
        } finally {
            out.close();
        }
    }

    protected void finalize() throws Throwable {
        try {
            releaseLock();
        } finally {
            super.finalize();
        }
    }

    private static final String CACHE_MAP_FILE = "cache.map";
    private static final String LOCL_FILE = "cache.lock";

    private final AozoraContext context;
    private final File baseDir;
    private final boolean isReadOnly;
    private final List<CacheMap> cacheMaps = new ArrayList<CacheMap>();
    private final FileName fileName = new FileName();
    private AozoraFileLock lock;
}

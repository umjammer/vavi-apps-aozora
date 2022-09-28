/*
 * http://www.35-35.net/aozora/
 */

package com.soso.aozora.list;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.soso.aozora.core.AozoraEnv;
import com.soso.aozora.core.AozoraUtil;


class AozoraIndex {

    private static class Cache {
        static void put(URL url, AozoraIndex index) {
            synchronized (CACHE) {
                CACHE.put(url, index);
            }
        }

        static AozoraIndex get(URL url) {
            synchronized (CACHE) {
                for (URL urlA : CACHE.keySet()) {
                    if (urlA.equals(url))
                        return CACHE.get(urlA);
                }
            }
            return null;
        }

        private static final Map<URL, AozoraIndex> CACHE = new HashMap<>();
    }

    static AozoraIndex getIndex() throws IOException {
        return getIndex(AozoraEnv.getIndexURL());
    }

    static AozoraIndex getIndex(URL url) throws IOException {
        AozoraIndex index = Cache.get(url);
        if (index == null) {
            index = new AozoraIndex();
            index.load(url);
            Cache.put(url, index);
        }
        return index;
    }

    private AozoraIndex() {
    }

    Set<String> getWorkIDs() {
        return Collections.unmodifiableSet(workIDs);
    }

    boolean isContainsWorkID(String workID) {
        return workIDs.contains(workID);
    }

    String getAuthorID(String workID) {
        return workAuthorIndex.get(workID);
    }

    String getTranslatorID(String workID) {
        return workTranslatorIndex.get(workID);
    }

    void load(URL url) throws IOException {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(AozoraUtil.getInputStream(url), StandardCharsets.UTF_8))) {
            String row;
            while ((row = in.readLine()) != null)
                loadRow(row);
        }
    }

    private void loadRow(String row) {
        int i1 = row.indexOf(',');
        if (i1 != -1 && i1 != 0) {
            String workID = row.substring(0, i1);
            int i2 = row.indexOf(',', i1 + 1);
            if (i2 != -1) {
                boolean isIndex = false;
                if (i1 + 1 < i2) {
                    String authorID = row.substring(i1 + 1, i2);
                    workAuthorIndex.put(workID, authorID);
                    isIndex = true;
                }
                if (i2 + 1 < row.length()) {
                    String translatorID = row.substring(i2 + 1);
                    workTranslatorIndex.put(workID, translatorID);
                    isIndex = true;
                }
                if (isIndex)
                    workIDs.add(workID);
            }
        }
    }

    private final Map<String, String> workAuthorIndex = new HashMap<>();
    private final Map<String, String> workTranslatorIndex = new HashMap<>();
    private final Set<String> workIDs = new HashSet<>();
}

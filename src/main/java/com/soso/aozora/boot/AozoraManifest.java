/*
 * http://www.35-35.net/aozora/
 */

package com.soso.aozora.boot;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.jar.Manifest;

import com.soso.aozora.core.AozoraEnv;
import com.soso.aozora.core.AozoraUtil;


public class AozoraManifest {

    static AozoraManifest getThisVersion() {
        try {
            ClassLoader ccl = Thread.currentThread().getContextClassLoader();
            Enumeration<?> urls = ccl.getResources(MANIFEST_PATH);
            while (urls.hasMoreElements()) {
                InputStream in = null;
                try {
                    in = AozoraUtil.getInputStream((URL) urls.nextElement());
                    if (in != null) {
                        Manifest manifest = new Manifest(in);
                        String title = manifest.getMainAttributes().getValue("Implementation-Title");
                        if (AOZORA_IMPLEMENTATION_TITLE.equals(title)) {
                            AozoraManifest aozoraManifest = new AozoraManifest(manifest);
                            return aozoraManifest;
                        }
                    }
                } finally {
                    if (in != null)
                        in.close();
                }
            }
            throw new FileNotFoundException("META-INF/MANIFEST.MF for Aozora Viewer");
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    static AozoraManifest getOnlineVersion() {
        InputStream in = null;
        try {
            URL manifestURL = new URL(AozoraEnv.Env.AOZORA_MANIFEST_URL.getString());
            try {
                in = AozoraUtil.getInputStream(manifestURL);
                Manifest manifest = new Manifest(in);
                AozoraManifest aozoraManifest = new AozoraManifest(manifest);
                return aozoraManifest;
            } finally {
                if (in != null)
                    in.close();
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private AozoraManifest(Manifest manifest) {
        this.manifest = manifest;
    }

    public String getSpecificationTitle() {
        return getManifest().getMainAttributes().getValue("Specification-Title");
    }

    public String getSpecificationVersion() {
        return getManifest().getMainAttributes().getValue("Specification-Version");
    }

    public String getSpecificationVendor() {
        return getManifest().getMainAttributes().getValue("Specification-Vendor");
    }

    public String getImplementationVersion() {
        return getManifest().getMainAttributes().getValue("Implementation-Version");
    }

    public String getImplementationVendor() {
        return getManifest().getMainAttributes().getValue("Implementation-Vendor");
    }

    public URL getImplementationVendorURL() {
        try {
            return new URL(getManifest().getMainAttributes().getValue("Implementation-Vendor-URL"));
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Date getBuildDate() {
        try {
            String str = getManifest().getMainAttributes().getValue("Build-Date");
            return new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ssZ").parse(str);
        } catch (ParseException e) {
            throw new IllegalStateException(e);
        }
    }

    private Manifest getManifest() {
        return manifest;
    }

    private static final String MANIFEST_PATH = "META-INF/MANIFEST.MF";
    private static final String AOZORA_IMPLEMENTATION_TITLE = "Aozora Viewer";

    private final Manifest manifest;
}

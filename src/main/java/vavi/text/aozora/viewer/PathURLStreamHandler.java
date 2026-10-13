/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.text.aozora.viewer;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLStreamHandler;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;


/**
 * Makes a directory of any file system, as the inside of a zip archive, the base url of
 * an html, so that the images of the html are read from the directory.
 * <p>
 * A url made by {@code new URL(base, spec)} takes the handler of the base, so the relative
 * urls of the images are resolved against the directory. The url does not have the path of
 * the directory itself, which is not seen as a part of the name of an image.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2026/10/13 umjammer initial version <br>
 */
public class PathURLStreamHandler extends URLStreamHandler {

    private static final String PROTOCOL = "path";

    private final Path dir;

    private PathURLStreamHandler(Path dir) {
        this.dir = dir;
    }

    /** @param dir the directory which the relative urls are resolved against */
    public static URL toURL(Path dir) {
        try {
            return new URL(PROTOCOL, "", -1, "/", new PathURLStreamHandler(dir));
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e);
        }
    }

    /** the file of the url, a name which is percent encoded is decoded */
    Path resolve(URL url) {
        String name = url.getPath().replaceFirst("^/+", "");
        if (name.contains("%"))
            name = URLDecoder.decode(name, StandardCharsets.UTF_8);
        return dir.resolve(name);
    }

    @Override
    protected URLConnection openConnection(URL url) {
        Path path = resolve(url);
        return new URLConnection(url) {
            @Override
            public void connect() throws IOException {
                if (!Files.exists(path))
                    throw new IOException("not found: " + path);
                connected = true;
            }

            @Override
            public InputStream getInputStream() throws IOException {
                connect();
                return Files.newInputStream(path);
            }

            @Override
            public long getContentLengthLong() {
                try {
                    return Files.size(path);
                } catch (IOException e) {
                    return -1;
                }
            }

            @Override
            public String getContentType() {
                return guessContentTypeFromName(path.getFileName().toString());
            }
        };
    }
}

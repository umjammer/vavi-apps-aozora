/*
 * http://www.35-35.net/aozora/
 */

package com.soso.aozora.boot;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class AozoraServerSocket implements Runnable {

    AozoraServerSocket(int port) throws IOException {
        serverSocket = new ServerSocket(port);
    }

    private AozoraContext getContext() {
        return context;
    }

    synchronized void accept(AozoraContext context) {
        if (this.context != null)
            throw new IllegalStateException("context already initialized.");
        this.context = context;
        if (thread != null) {
            throw new IllegalStateException(thread.getName() + " already initialized.");
        }
        thread = new Thread(this, "AozoraServerSocket");
        thread.start();
        AozoraSplashWindow.setProgress(AozoraSplashWindow.PROGRESS.SOCKET_ACCEPT);
    }

    public void run() {
        while (serverSocket != null) {
            try {
                Socket socket = null;
                try {
                    socket = serverSocket.accept();
                    getContext().log("boot|" + socket.getInetAddress() + "から接続を受付ました。");
                    BufferedWriter out = null;
                    try {
                        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
                        BufferedReader in = null;
                        try {
                            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
                            out.write(HELLO + "\n");
                            out.flush();
                            String hello = in.readLine();
                            if (hello == null || !hello.equals(HELLO)) {
                                socket.close();
                                throw new IllegalStateException("Unknown hello : " + hello);
                            }
                            List<String> argList = new ArrayList<String>();
                            String line;
                            while ((line = in.readLine()) != null && line.length() != 0) {
                                argList.add(line);
                            }
                            String[] args = argList.toArray(new String[argList.size()]);
                            getContext().log("boot|リクエストを処理します。" + Arrays.toString(args));
                            context.applyArgs(args);
                            out.write("OK");
                            out.flush();
                        } finally {
                            try {
                                in.close();
                            } catch (Exception e) {
                                e.printStackTrace(System.err);
                            }
                        }
                    } finally {
                        try {
                            out.close();
                        } catch (Exception e) {
                            e.printStackTrace(System.err);
                        }
                    }
                } finally {
                    socket.close();
                }
            } catch (Exception e) {
                e.printStackTrace(System.err);
            }
        }
    }

    protected void finalize() throws Throwable {
        try {
            serverSocket.close();
            serverSocket = null;
        } finally {
            super.finalize();
        }
    }

    static final String HELLO = "Hello! This is AozoraViewer.";
    private ServerSocket serverSocket;
    private AozoraContext context;
    private Thread thread;
}

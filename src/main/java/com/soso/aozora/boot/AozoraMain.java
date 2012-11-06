/*
 * http://www.35-35.net/aozora/
 */

package com.soso.aozora.boot;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;


public class AozoraMain {

    private static class ExitCountDownLabel extends JLabel implements Runnable {

        private int sec;
        private Runnable exitProgram;
        private boolean stopExit;

        private void setExitSeccondText(int sec) {
            String text = "あと " + sec + " 秒で自動終了します。";
            AozoraLog.getInstance().log(text);
            setText(text);
        }

        public void run() {
            for (; !stopExit && sec > 0; sec--)
                try {
                    setExitSeccondText(sec);
                    Thread.sleep(1000L);
                } catch (Exception e) {
                    AozoraLog.getInstance().log(e);
                }

            if (stopExit) {
                AozoraLog.getInstance().log("終了をキャンセルしました。");
            } else {
                AozoraLog.getInstance().log("自動終了します。");
                exitProgram.run();
            }
        }

        private void stopExit() {
            stopExit = true;
        }

        ExitCountDownLabel(int sec, Runnable exitProgram) {
            this.exitProgram = exitProgram;
            this.sec = sec;
            setText("自動終了カウントダウン");
            new Thread(this, "AozoraMain_ExitCountDownLabel").start();
        }
    }

    private JFrame frame;

    public static void main(String[] args) {
        AozoraMain main = new AozoraMain();
        try {
            AozoraSplashWindow.showSplash();
        } finally {
            try {
                main.prepareMain(args);
} catch (Exception e) {
 e.printStackTrace(System.err);
            } finally {
                try {
                    AozoraSplashWindow.disposeSplash();
} catch (Exception e) {
 e.printStackTrace(System.err);
                } finally {
                    main.showMain();
                }
            }
        }
    }

    private void prepareMain(String[] args) {
        boolean isServer = AozoraBootLoader.initServerSocket();
        if (!isServer && args != null && args.length != 0) {
            boolean requestAnoser = AozoraBootLoader.requestAnotherVM(args);
            if (requestAnoser) {
                System.out.println("Request will load on another AozoraViewer, bye.");
                System.exit(0);
                return;
            }
        }
        frame = new JFrame();
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setSize(Math.min(screen.width, 1024), Math.min(screen.height, 768));
        frame.setDefaultCloseOperation(0);
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                Runnable exitProgram = new Runnable() {
                    public void run() {
                        try {
                            AozoraLog.getInstance().log("Aozora Viewer を終了します。");
                            frame.dispose();
                            System.exit(0);
                        } catch (Exception e) {
                            e.printStackTrace(System.err);
                            System.exit(1);
                        }
                    }
                };
                JPanel messagePane = new JPanel();
                messagePane.setLayout(new BoxLayout(messagePane, 1));
                messagePane.add(new JLabel("Aozora Viewer を終了します。よろしいですか？"));
                ExitCountDownLabel exitCountDownLabel = new ExitCountDownLabel(10, exitProgram);
                messagePane.add(exitCountDownLabel);
                if (JOptionPane.showInternalConfirmDialog(frame.getLayeredPane(), messagePane, "Aozora Viewer 終了の確認", 2, 2) == 0)
                    exitProgram.run();
                else
                    exitCountDownLabel.stopExit();
            }
        });
        AozoraContext context = AozoraBootLoader.load(frame, args);
        frame.setTitle(context.getManifest().getSpecificationTitle() + " powered by " + context.getManifest().getSpecificationVendor());
    }

    private void showMain() {
        frame.setVisible(true);
    }
}

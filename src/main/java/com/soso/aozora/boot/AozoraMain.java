/*
 * http://www.35-35.net/aozora/
 */

package com.soso.aozora.boot;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;


public class AozoraMain {

    static Logger logger = Logger.getLogger(AozoraMain.class.getName());

    private static class ExitCountDownLabel extends JLabel implements Runnable {

        private int sec;
        private Runnable exitProgram;
        private boolean stopExit;

        private void setExitSeccondText(int sec) {
            String text = "あと " + sec + " 秒で自動終了します。";
            logger.info(text);
            setText(text);
        }

        public void run() {
            for (; !stopExit && sec > 0; sec--)
                try {
                    setExitSeccondText(sec);
                    Thread.sleep(1000L);
                } catch (Exception e) {
                    logger.log(Level.SEVERE, e.getMessage(), e);
                }

            if (stopExit) {
                logger.info("終了をキャンセルしました。");
            } else {
                logger.info("自動終了します。");
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

    public static void main(String[] args) throws Exception {
        AozoraMain main = new AozoraMain();
        main.prepareMain(args);
        main.showMain();
    }

    private void prepareMain(String[] args) {
        frame = new JFrame();
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setSize(Math.min(screen.width, 1024), Math.min(screen.height, 768));
        frame.setDefaultCloseOperation(0);
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                Runnable exitProgram = new Runnable() {
                    public void run() {
                        try {
                            logger.info("Aozora Viewer を終了します。");
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

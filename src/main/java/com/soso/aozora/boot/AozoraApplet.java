/*
 * http://www.35-35.net/aozora/
 */

package com.soso.aozora.boot;

import javax.swing.JApplet;


public class AozoraApplet extends JApplet {

    public void init() {
        String args = getParameter("ARGS");
        AozoraBootLoader.load(this, args == null ? null : args.split(" "));
    }
}

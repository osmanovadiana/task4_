package ru.vsu.cs.course1;

import ru.vsu.cs.util.SwingUtils;

import javax.swing.*;
import java.util.Locale;


public class Program {

    public static void main(String[] args) throws Exception {
        Locale.setDefault(Locale.ROOT);
        SwingUtils.setDefaultFont("Microsoft Sans Serif", 18);
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new FrameMain().setVisible(true);
            }
        });
    }
}

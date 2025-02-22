package io.github.brqnko.bytekin.test.util;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class PrintCapture {

    public static String captureOutput(Runnable runnable) {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(outContent);

        System.setOut(ps);

        runnable.run();

        System.setOut(new PrintStream(System.out));

        return outContent.toString().replace("\r", "");
    }

}

package io.github.brqnko.bytekin.test.util;

import org.junit.jupiter.api.Assertions;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * A custom ClassLoader with customizable class transformation
 */
public class TestClassLoader extends ClassLoader {

    private final ClassTransformer transformer;

    public TestClassLoader(ClassLoader parent, ClassTransformer transformer) {
        super(parent);
        this.transformer = transformer;
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        try {
            byte[] classData = loadClassData(name.replaceAll("\\.", "/") + ".class");

            byte[] transformed = transformer.apply(name, classData);

            try {
                return defineClass(name, transformed, 0, transformed.length);
            } catch (Exception e) {
                return super.loadClass(name);
            }
        } catch (IOException e) {
            Assertions.fail(e);
        }
        return super.loadClass(name);
    }

    private byte[] loadClassData(String name) throws IOException {
        InputStream stream = getClass().getClassLoader().getResourceAsStream(name);
        assert stream != null;
        int size = stream.available();
        byte[] buff = new byte[size];
        DataInputStream in = new DataInputStream(stream);
        in.readFully(buff);
        in.close();
        return buff;
    }

    @FunctionalInterface
    public interface ClassTransformer {

        byte[] apply(String name, byte[] bytes);

    }
}

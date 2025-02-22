package io.github.brqnko.bytekin.test.inject;

import io.github.brqnko.bytekin.injection.At;
import io.github.brqnko.bytekin.injection.CallbackInfo;
import io.github.brqnko.bytekin.injection.Inject;
import io.github.brqnko.bytekin.injection.ModifyClass;
import io.github.brqnko.bytekin.test.inject.target.InjectTestTargetQ;
import io.github.brqnko.bytekin.test.util.PrintCapture;
import io.github.brqnko.bytekin.test.util.QMappingProvider;
import io.github.brqnko.bytekin.test.util.TestClassLoader;
import io.github.brqnko.bytekin.transformer.BytekinTransformer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.Opcodes;

@ModifyClass(className = "io.github.brqnko.bytekin.test.inject.target.InjectTestTarget")
public class InjectAtHeadTest {

    @Test
    void injectAtHeadTest() throws Exception {
        BytekinTransformer transformer = new BytekinTransformer(new QMappingProvider(), InjectAtHeadTest.class);

        // create a new class loader with the transformer
        TestClassLoader loader = new TestClassLoader(
                Test.class.getClassLoader(),
                (name, bytes) -> transformer.transform(name, bytes, Opcodes.ASM9));

        // load the class
        Class<?> clazz = loader.loadClass("io.github.brqnko.bytekin.test.inject.target.InjectTestTargetQ");

        // run the method and capture the output
        String capture = PrintCapture.captureOutput(() -> {
            try {
                // invoke the method
                String ret = (String) clazz.getMethod("runGameLoopQ", String[][].class, int.class)
                        .invoke(clazz.getConstructor().newInstance(), new String[0][0], 0);

                Assertions.assertEquals("injected return value", ret);
            } catch (Exception e) {
                Assertions.fail(e);
            }
        });

        Assertions.assertEquals("Inject at head\n", capture);
    }

    /**
     * This method will be called at the head of the target method
     */
    @SuppressWarnings("unused")
    @Inject(methodName = "runGameLoop", methodDesc = "([[Ljava/lang/String;I)Ljava/lang/String;", at = At.HEAD)
    public static CallbackInfo injectAtHead(InjectTestTargetQ self, String[][] str, int i) {
        System.out.println("Inject at head");

        // cancel original method with a return value 'injected return value'
        return new CallbackInfo(true, "injected return value", null);
    }

}

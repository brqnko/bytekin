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
public class InjectAtReturnTest {

    @Test
    void injectAtReturnTest() throws Exception {
        BytekinTransformer transformer = new BytekinTransformer.Builder(InjectAtReturnTest.class)
                .mapping(new QMappingProvider())
                .build();

        TestClassLoader loader = new TestClassLoader(
                Test.class.getClassLoader(),
                (name, bytes) -> transformer.transform(name, bytes, Opcodes.ASM9));

        Class<?> clazz = loader.loadClass("io.github.brqnko.bytekin.test.inject.target.InjectTestTargetQ");

        String capture = PrintCapture.captureOutput(() -> {
            try {
                String ret = (String) clazz.getMethod("runGameLoopQ", String[][].class, int.class)
                        .invoke(clazz.getConstructor().newInstance(), new String[0][0], 0);
                Assertions.assertEquals("injected return value", ret);
            } catch (Exception e) {
                Assertions.fail(e);
            }
        });

        Assertions.assertEquals("Running game loop\nInject at return\n", capture);

    }

    /**
     * This method will be called before return statement is executed
     */
    @SuppressWarnings("unused")
    @Inject(methodName = "runGameLoop", methodDesc = "([[Ljava/lang/String;I)Ljava/lang/String;", at = At.RETURN)
    public static CallbackInfo injectAtReturn(InjectTestTargetQ self, String[][] str, int i) {
        System.out.println("Inject at return");

        return new CallbackInfo(true, "injected return value", null);
    }

}

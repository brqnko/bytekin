package io.github.brqnko.bytekin.test.invoke;

import io.github.brqnko.bytekin.injection.CallbackInfo;
import io.github.brqnko.bytekin.injection.Invoke;
import io.github.brqnko.bytekin.injection.ModifyClass;
import io.github.brqnko.bytekin.injection.Shift;
import io.github.brqnko.bytekin.test.invoke.target.InvokeTestTargetQ;
import io.github.brqnko.bytekin.test.util.PrintCapture;
import io.github.brqnko.bytekin.test.util.QMappingProvider;
import io.github.brqnko.bytekin.test.util.TestClassLoader;
import io.github.brqnko.bytekin.transformer.BytekinTransformer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.Opcodes;

@ModifyClass(className = "io.github.brqnko.bytekin.test.invoke.target.InvokeTestTarget")
public class InvokeAfterTest {

    @Test
    void invokeAfterTest() throws Exception {
        BytekinTransformer transformer = new BytekinTransformer(new QMappingProvider(), InvokeAfterTest.class);

        TestClassLoader loader = new TestClassLoader(
                Test.class.getClassLoader(),
                (name, bytes) -> transformer.transform(name, bytes, Opcodes.ASM9));

        Class<?> clazz = loader.loadClass("io.github.brqnko.bytekin.test.invoke.target.InvokeTestTargetQ");

        String capture = PrintCapture.captureOutput(() -> {
            try {
                clazz.getMethod("runGameLoopQ", String[][].class, int.class)
                        .invoke(clazz.getConstructor().newInstance(), new String[0][0], 0);
            } catch (Exception e) {
                Assertions.fail(e);
            }
        });

        Assertions.assertEquals("before invoke\ninvokeAfter called\nafter invoke\n", capture);
    }

    /**
     * This method will be invoked after the 'invokeTarget' method in the 'InvokeTestTarget' called
     */
    @SuppressWarnings("unused")
    @Invoke(
            targetMethodName = "runGameLoop",
            targetMethodDesc = "([[Ljava/lang/String;I)Ljava/lang/String;",
            invokeMethodOwner = "io.github.brqnko.bytekin.test.invoke.target.InvokeTestTarget",
            invokeMethodName = "invokeTarget",
            invokeMethodDesc = "([[Ljava/lang/String;I)Ljava/lang/String;",
            shift = Shift.AFTER
    )
    public static CallbackInfo invokeAfter(InvokeTestTargetQ self, String[][] args, int i, String[][] invokeArgs, int invokeI) {
        System.out.println("invokeAfter called");

        return new CallbackInfo(false, null, null);
    }

}

package io.github.brqnko.bytekin.test.invoke.target;

@SuppressWarnings("unused")
public class InvokeTestTargetQ {

    public String runGameLoopQ(String[][] str, int i) {
        System.out.println("before invoke");

        String temp = invokeTargetQ(str, i);

        System.out.println("after invoke");

        return "original return value";
    }

    private String invokeTargetQ(String[][] str, int arr) {
        for (String[] strings : str) {
            for (String string : strings) {
                System.out.println(string);
            }
        }
        return "invoke target";
    }

}

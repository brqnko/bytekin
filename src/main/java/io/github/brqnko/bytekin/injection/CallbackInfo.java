package io.github.brqnko.bytekin.injection;

public class CallbackInfo {

    public static final String CALLBACK_OWNER = "io/github/brqnko/bytekin/injection/CallbackInfo";
    public static final String CALLBACK_DESC = "L" + CALLBACK_OWNER + ";";

    public static final String FIELD_CANCELLED = "cancelled";
    public static final String FIELD_RETURN_VALUE = "returnValue";
    public static final String FIELD_MODIFY_ARGS = "modifyArgs";

    public final boolean cancelled;
    public final Object returnValue;
    public final Object[] modifyArgs;

    public CallbackInfo(boolean cancelled, Object returnValue, Object[] modifyArgs) {
        this.cancelled = cancelled;
        this.returnValue = returnValue;
        this.modifyArgs = modifyArgs;
    }

    public static CallbackInfo empty() {
        return new CallbackInfo(false, null, null);
    }
}

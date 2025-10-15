package io.github.brqnko.bytekin.data;

import io.github.brqnko.bytekin.injection.RedirectType;
import lombok.Data;

@Data
public class RedirectData {

    private final String targetMethodName;
    private final String targetMethodDesc;

    private final RedirectType type;
    private final String owner;
    private final String name;
    private final String desc;
    private final int ordinal;

    private final String hookMethodOwner;
    private final String hookMethodName;
}

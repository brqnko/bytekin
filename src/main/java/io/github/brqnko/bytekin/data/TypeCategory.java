package io.github.brqnko.bytekin.data;

/**
 * Represents the return type of method
 */
public enum TypeCategory {

    VOID,
    BOOLEAN,
    BYTE,
    SHORT,
    CHAR,
    INT,
    LONG,
    FLOAT,
    DOUBLE,
    OBJECT,
    ARRAY;

    /**
     * Parses the return type from descriptor
     *
     * @param desc the descriptor
     * @return the return type
     */
    public static TypeCategory parse(String desc) {
        switch (desc.charAt(0)) {
            case 'V':
                return VOID;
            case 'Z':
                return BOOLEAN;
            case 'B':
                return BYTE;
            case 'S':
                return SHORT;
            case 'C':
                return CHAR;
            case 'I':
                return INT;
            case 'J':
                return LONG;
            case 'F':
                return FLOAT;
            case 'D':
                return DOUBLE;
            case 'L':
                return OBJECT;
            case '[':
                return ARRAY;
            default:
                throw new IllegalArgumentException("Invalid descriptor: " + desc);
        }
    }
}

package io.github.brqnko.bytekin.util;

import io.github.brqnko.bytekin.data.TypeCategory;
import io.github.brqnko.bytekin.data.TypeData;
import lombok.experimental.UtilityClass;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class DescriptorParser {

    public TypeData parseReturnType(String desc) {
        boolean skip = false;
        String typeString = null;
        for (int i = 0; i < desc.length(); i++) {
            char c = desc.charAt(i);
            switch (c) {
                case 'L':
                    skip = true;
                    break;
                case ';':
                    skip = false;
                    break;
                case ')':
                    if (!skip) {
                        typeString = desc.substring(i + 1);
                    }
                    break;
            }
        }

        if (typeString == null) {
            throw new IllegalStateException("Could not find return type in descriptor: " + desc);
        }

        TypeCategory returnType = TypeCategory.parse(typeString);
        return new TypeData(returnType, returnType == TypeCategory.OBJECT ? typeString.substring(1, typeString.length() - 1) : typeString);
    }

    public List<TypeData> parseParameterTypes(String desc) {
        List<TypeData> types = new ArrayList<>();
        String params = desc.substring(desc.indexOf('(') + 1, desc.indexOf(')'));
        int i = 0;
        while (i < params.length()) {
            char c = params.charAt(i);
            if (c == 'L') {
                int semicolonIndex = params.indexOf(';', i);
                String typeString = params.substring(i, semicolonIndex + 1);
                TypeCategory rt = TypeCategory.parse(typeString);
                types.add(new TypeData(rt, typeString.substring(1, typeString.length() - 1)));
                i = semicolonIndex + 1;
            } else if (c == '[') {
                int arrayStart = i;
                while (params.charAt(i) == '[') {
                    i++;
                }
                if (params.charAt(i) == 'L') {
                    int semicolonIndex = params.indexOf(';', i);
                    String typeString = params.substring(arrayStart, semicolonIndex + 1);
                    TypeCategory rt = TypeCategory.parse(typeString);
                    if (rt == TypeCategory.OBJECT && typeString.startsWith("[")) {
                        rt = TypeCategory.ARRAY;
                    }
                    types.add(new TypeData(rt, typeString));
                    i = semicolonIndex + 1;
                } else {
                    String typeString = params.substring(arrayStart, i + 1);
                    TypeCategory rt = TypeCategory.parse(typeString);
                    if (rt == TypeCategory.OBJECT && typeString.startsWith("[")) {
                        rt = TypeCategory.ARRAY;
                    }
                    types.add(new TypeData(rt, typeString));
                    i++;
                }
            } else {
                String typeString = String.valueOf(c);
                TypeCategory rt = TypeCategory.parse(typeString);
                types.add(new TypeData(rt, typeString));
                i++;
            }
        }
        return types;
    }

    public String parseDescriptor(Method method) {
        StringBuilder desc = new StringBuilder("(");
        for (Class<?> parameterType : method.getParameterTypes()) {
            if (parameterType.isPrimitive()) {
                desc.append(parameterType.getTypeName());
            } else {
                desc.append("L").append(parameterType.getTypeName().replaceAll("\\.", "/")).append(";");
            }
        }
        desc.append(")");
        Class<?> returnType = method.getReturnType();
        if (returnType.isPrimitive()) {
            desc.append(returnType.getTypeName());
        } else {
            desc.append("L").append(returnType.getTypeName().replaceAll("\\.", "/")).append(";");
        }
        return desc.toString();
    }

}

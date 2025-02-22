package io.github.brqnko.bytekin.transformer;

import io.github.brqnko.bytekin.injection.ModifyClass;
import io.github.brqnko.bytekin.logging.ILogger;
import io.github.brqnko.bytekin.logging.impl.StdLogger;
import io.github.brqnko.bytekin.mapping.IMappingProvider;
import io.github.brqnko.bytekin.mapping.impl.EmptyMappingProvider;

import java.util.HashMap;
import java.util.Map;

public class BytekinTransformer {

    private final ILogger logger;

    private final Map<String, BytekinClassTransformer> transformers = new HashMap<>();

    public BytekinTransformer(ILogger logger, IMappingProvider mapping, Class<?>... classes) {

        this.logger = logger;

        for (Class<?> clazz : classes) {
            ModifyClass modifyClass = clazz.getAnnotation(ModifyClass.class);
            if (modifyClass == null) {
                logger.log("Class " + clazz.getName() + " does not have ModifyClass annotation");
                continue;
            }

            String className = mapping.getClassName(modifyClass.className());
            if (className == null) {
                logger.log("Class " + modifyClass.className() + " not found in mapping");
            }

            if (transformers.containsKey(className)) {
                logger.log("Class " + className + " already has a transformer");
                continue;
            }

            BytekinClassTransformer transformer = new BytekinClassTransformer(logger, mapping, clazz, className);
            transformers.put(className, transformer);
        }
    }

    public BytekinTransformer(IMappingProvider mapping, Class<?>... classes) {
        this(new StdLogger(), mapping, classes);
    }

    public BytekinTransformer(ILogger logger, Class<?>... classes) {
        this(logger, new EmptyMappingProvider(), classes);
    }

    public BytekinTransformer(Class<?>... classes) {
        this(new StdLogger(), new EmptyMappingProvider(), classes);
    }

    public byte[] transform(String className, byte[] bytes, int api) {
        BytekinClassTransformer transformer = transformers.get(className);
        if (transformer == null) {
            return bytes;
        }

        return transformer.transform(bytes, api);
    }

}

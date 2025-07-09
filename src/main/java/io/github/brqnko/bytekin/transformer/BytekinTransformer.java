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

    public byte[] transform(String className, byte[] bytes, int api) {
        BytekinClassTransformer transformer = transformers.get(className);
        if (transformer == null) {
            return bytes;
        }

        return transformer.transform(bytes, api);
    }

    public static class Builder {

        private final Class<?>[] classes;

        private ILogger logger;
        private IMappingProvider mapping;

        public Builder(Class<?>... classes) {
            this.classes = classes;
        }

        public Builder logger(ILogger logger) {
            this.logger = logger;
            return this;
        }

        public Builder mapping(IMappingProvider mapping) {
            this.mapping = mapping;
            return this;
        }

        public BytekinTransformer build() {
            if (logger == null) {
                logger = new StdLogger();
            }

            if (mapping == null) {
                mapping = new EmptyMappingProvider();
            }

            return new BytekinTransformer(logger, mapping, classes);
        }
    }
}

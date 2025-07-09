package io.github.brqnko.bytekin.transformer;

import io.github.brqnko.bytekin.data.Injection;
import io.github.brqnko.bytekin.data.Invocation;
import io.github.brqnko.bytekin.injection.ModifyClass;
import io.github.brqnko.bytekin.logging.ILogger;
import io.github.brqnko.bytekin.logging.impl.StdLogger;
import io.github.brqnko.bytekin.mapping.IMappingProvider;
import io.github.brqnko.bytekin.mapping.impl.EmptyMappingProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BytekinTransformer {

    private final ILogger logger;

    private final Map<String, BytekinClassTransformer> transformers;

    public BytekinTransformer(ILogger logger, Map<String, BytekinClassTransformer> transformers) {

        this.logger = logger;
        this.transformers = transformers;
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

        private final Map<String, List<Injection>> injections = new HashMap<>();
        private final Map<String, List<Invocation>> invocations = new HashMap<>();

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

        public Builder inject(String className, Injection injection) {
            this.injections.computeIfAbsent(className, k -> new ArrayList<>()).add(injection);
            return this;
        }

        public Builder invoke(String className, Invocation invocation) {
            this.invocations.computeIfAbsent(className, k -> new ArrayList<>()).add(invocation);
            return this;
        }

        public BytekinTransformer build() {
            if (logger == null) {
                logger = new StdLogger();
            }

            if (mapping == null) {
                mapping = new EmptyMappingProvider();
            }

            Map<String, BytekinClassTransformer> transformers = new HashMap<>();

            // load from annotation
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

            // load from injection
            this.injections.forEach((className, injections) -> {
                className = mapping.getClassName(className);

                BytekinClassTransformer transformer = transformers.computeIfAbsent(className, k -> new BytekinClassTransformer());
                for (Injection injection : injections) {
                    transformer.addInjection(logger, mapping, injection, className);
                }
            });

            // load from invocations
            this.invocations.forEach((className, invocations) -> {
                className = mapping.getClassName(className);

                BytekinClassTransformer transformer = transformers.computeIfAbsent(className, k -> new BytekinClassTransformer());
                for (Invocation invocation : invocations) {
                    transformer.addInvocation(logger, mapping, invocation, className);
                }
            });

            return new BytekinTransformer(logger, transformers);
        }
    }
}

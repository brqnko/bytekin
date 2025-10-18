package io.github.brqnko.bytekin.transformer;

import io.github.brqnko.bytekin.data.ConstantModification;
import io.github.brqnko.bytekin.data.Injection;
import io.github.brqnko.bytekin.data.Invocation;
import io.github.brqnko.bytekin.data.RedirectData;
import io.github.brqnko.bytekin.data.VariableModification;
import io.github.brqnko.bytekin.injection.ModifyClass;
import io.github.brqnko.bytekin.mapping.IMappingProvider;
import io.github.brqnko.bytekin.mapping.impl.EmptyMappingProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BytekinTransformer {

    private final Map<String, BytekinClassTransformer> transformers;

    public BytekinTransformer(Map<String, BytekinClassTransformer> transformers) {
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

        private IMappingProvider mapping;

        private final Map<String, List<Injection>> injections = new HashMap<>();
        private final Map<String, List<Invocation>> invocations = new HashMap<>();
        private final Map<String, List<RedirectData>> redirects = new HashMap<>();
        private final Map<String, List<ConstantModification>> constantModifications = new HashMap<>();
        private final Map<String, List<VariableModification>> variableModifications = new HashMap<>();

        public Builder(Class<?>... classes) {
            this.classes = classes;
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

        public Builder redirect(String className, RedirectData redirect) {
            this.redirects.computeIfAbsent(className, k -> new ArrayList<>()).add(redirect);
            return this;
        }

        public Builder modifyConstant(String className, ConstantModification modification) {
            this.constantModifications.computeIfAbsent(className, k -> new ArrayList<>()).add(modification);
            return this;
        }

        public Builder modifyVariable(String className, VariableModification modification) {
            this.variableModifications.computeIfAbsent(className, k -> new ArrayList<>()).add(modification);
            return this;
        }

        public BytekinTransformer build() {
            if (mapping == null) {
                mapping = new EmptyMappingProvider();
            }

            Map<String, BytekinClassTransformer> transformers = new HashMap<>();

            // load from annotation
            for (Class<?> clazz : classes) {
                ModifyClass modifyClass = clazz.getAnnotation(ModifyClass.class);
                if (modifyClass == null) {
                    continue;
                }

                String className = mapping.getClassName(modifyClass.className());
                if (transformers.containsKey(className)) {
                    continue;
                }

                BytekinClassTransformer transformer = new BytekinClassTransformer(mapping, clazz, className);
                transformers.put(className, transformer);
            }

            // load from injection
            this.injections.forEach((className, injections) -> {
                className = mapping.getClassName(className);

                BytekinClassTransformer transformer = transformers.computeIfAbsent(className, k -> new BytekinClassTransformer());
                for (Injection injection : injections) {
                    transformer.addInjection(mapping, injection, className);
                }
            });

            // load from invocations
            this.invocations.forEach((className, invocations) -> {
                className = mapping.getClassName(className);

                BytekinClassTransformer transformer = transformers.computeIfAbsent(className, k -> new BytekinClassTransformer());
                for (Invocation invocation : invocations) {
                    transformer.addInvocation(mapping, invocation, className);
                }
            });

            this.redirects.forEach((className, redirects) -> {
                className = mapping.getClassName(className);

                BytekinClassTransformer transformer = transformers.computeIfAbsent(className, k -> new BytekinClassTransformer());
                for (RedirectData redirect : redirects) {
                    transformer.addRedirect(mapping, redirect, className);
                }
            });

            this.constantModifications.forEach((className, modifications) -> {
                className = mapping.getClassName(className);

                BytekinClassTransformer transformer = transformers.computeIfAbsent(className, k -> new BytekinClassTransformer());
                for (ConstantModification modification : modifications) {
                    transformer.addConstantModification(mapping, modification, className);
                }
            });

            this.variableModifications.forEach((className, modifications) -> {
                className = mapping.getClassName(className);

                BytekinClassTransformer transformer = transformers.computeIfAbsent(className, k -> new BytekinClassTransformer());
                for (VariableModification modification : modifications) {
                    transformer.addVariableModification(mapping, modification, className);
                }
            });

            return new BytekinTransformer(transformers);
        }
    }
}

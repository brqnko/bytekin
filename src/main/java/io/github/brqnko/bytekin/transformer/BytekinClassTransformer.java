package io.github.brqnko.bytekin.transformer;

import io.github.brqnko.bytekin.logging.ILogger;
import io.github.brqnko.bytekin.mapping.IMappingProvider;
import io.github.brqnko.bytekin.target.MethodData;
import io.github.brqnko.bytekin.transformer.api.IBytekinMethodTransformer;
import io.github.brqnko.bytekin.transformer.visitor.BytekinClassVisitor;
import lombok.Getter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.util.List;
import java.util.Map;

@Getter
public class BytekinClassTransformer {

    private final Map<MethodData, List<IBytekinMethodTransformer>> methodTransformers;

    public BytekinClassTransformer(ILogger logger, IMappingProvider mapping, Class<?> clazz, String className) {

        this.methodTransformers = IBytekinMethodTransformer.createTransformers(logger, mapping, clazz, className);
    }

    public byte[] transform(byte[] bytes, int api) {

        ClassReader reader = new ClassReader(bytes);
        ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);

        reader.accept(new BytekinClassVisitor(api, writer, this), 0);

        return writer.toByteArray();
    }

}

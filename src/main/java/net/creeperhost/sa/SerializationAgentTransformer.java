package net.creeperhost.sa;

import org.objectweb.asm.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.ProtectionDomain;

/**
 * Created by covers1624 on 31/7/23.
 */
public class SerializationAgentTransformer implements ClassFileTransformer {

    private static final String OBJECT_INPUT_STREAM = ObjectInputStream.class.getName().replace('.', '/');
    private static final String FILTER_OBJECT_INPUT_STREAM = FilterObjectInputStream.class.getName().replace('.', '/');

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] bytes) throws IllegalClassFormatException {
        String jCName = className.replace('/', '.');
        if (!SerializationAgent.toTransform.contains(jCName)) {
            return bytes;
        }

        Logger.debug("Transforming " + className);

        ClassReader cr = new ClassReader(bytes);
        ClassWriter cw = new ClassWriter(0);
        boolean[] transformed = new boolean[1];
        cr.accept(new ClassVisitor(Opcodes.ASM9, cw) {
            @Override
            public MethodVisitor visitMethod(int access, String mName, String mDesc, String signature, String[] exceptions) {
                return new MethodVisitor(Opcodes.ASM9, super.visitMethod(access, mName, mDesc, signature, exceptions)) {
                    @Override
                    public void visitTypeInsn(int opcode, String type) {
                        if (opcode == Opcodes.NEW && type.equals(OBJECT_INPUT_STREAM)) {
                            Logger.debug(" Transforming NEW inside " + mName + mDesc);
                            type = FILTER_OBJECT_INPUT_STREAM;
                            transformed[0] = true;
                        }
                        super.visitTypeInsn(opcode, type);
                    }

                    @Override
                    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
                        if (opcode == Opcodes.INVOKESPECIAL && owner.equals(OBJECT_INPUT_STREAM)) {
                            Logger.debug(" Transforming INVOKESPECIAL inside " + mName + mDesc);
                            owner = FILTER_OBJECT_INPUT_STREAM;
                            transformed[0] = true;
                        }
                        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
                    }
                };
            }
        }, 0);
        if (transformed[0]) {
            bytes = cw.toByteArray();
            if (SerializationAgent.DEBUG) {
                try {
                    Path path = Paths.get("asm/serializationagent/" + className + ".class");
                    if (!Files.exists(path.getParent())) {
                        Files.createDirectories(path.getParent());
                    }
                    Files.write(path, bytes);
                } catch (IOException ex) {
                    Logger.error("Failed to write file.", ex);
                }
            }
        }
        return bytes;
    }
}

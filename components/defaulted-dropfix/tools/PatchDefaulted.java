import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Changes only Defaulted's bookkeeping/refresh order; no Minecraft classes are changed. */
public final class PatchDefaulted implements Opcodes {
    private static final String OWNER = "net/atlas/defaulted/utils/ReferentialDataComponentMap";
    private static final String MAP = "net/minecraft/core/component/PatchedDataComponentMap";
    private static final String PATCH = "Lnet/minecraft/core/component/DataComponentPatch;";

    public static void main(String[] args) throws Exception {
        ClassReader reader = new ClassReader(Files.readAllBytes(Path.of(args[0])));
        ClassNode node = new ClassNode();
        reader.accept(node, 0);
        if (!node.name.equals(OWNER)) throw new IllegalArgumentException("Unexpected class");
        Set<String> mutators = Set.of(
            "set(Lnet/minecraft/core/component/DataComponentType;Ljava/lang/Object;)V",
            "remove(Lnet/minecraft/core/component/DataComponentType;)V",
            "applyPatch(" + PATCH + ")V",
            "applyPatch(Lnet/minecraft/core/component/DataComponentType;Ljava/lang/Object;)V"
        );
        int initialized = 0, refreshed = 0;
        for (MethodNode method : node.methods) {
            if (mutators.contains(method.name + method.desc)) {
                // Resolve a changed parent before recording the pending operation, and
                // before the outer vanilla mutator performs its ownership check.
                InsnList prefix = new InsnList();
                prefix.add(new VarInsnNode(ALOAD, 0));
                prefix.add(new MethodInsnNode(INVOKEVIRTUAL, OWNER, "get",
                    "()Lnet/minecraft/core/component/DataComponentMap;", false));
                prefix.add(new InsnNode(POP));
                method.instructions.insert(prefix);
                initialized++;
            }
            if (method.name.equals("lambda$new$0") && method.desc.equals(
                    "(Ljava/util/function/Supplier;)Lnet/minecraft/core/component/DataComponentMap;")) {
                for (AbstractInsnNode insn : method.instructions.toArray()) {
                    if (insn instanceof MethodInsnNode call && call.owner.equals(MAP)
                            && call.name.equals("restorePatch") && call.desc.equals("(" + PATCH + ")V")) {
                        // Stack: original, snapshot. Keep snapshot intact across clear;
                        // the original asPatch() already established copy-on-write.
                        InsnList clear = new InsnList();
                        clear.add(new InsnNode(SWAP));
                        clear.add(new InsnNode(DUP));
                        clear.add(new MethodInsnNode(INVOKEVIRTUAL, MAP, "clearPatch", "()V", false));
                        clear.add(new InsnNode(SWAP));
                        method.instructions.insertBefore(call, clear);
                        // Unlike raw restorePatch, applyPatch normalizes removal markers
                        // against the new prototype and honors map ownership.
                        call.name = "applyPatch";
                        refreshed++;
                    }
                }
            }
        }
        if (initialized != 4 || refreshed != 1) {
            throw new IllegalStateException("Unexpected bytecode: " + initialized + "/" + refreshed);
        }
        ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        Files.write(Path.of(args[1]), writer.toByteArray());
    }
}

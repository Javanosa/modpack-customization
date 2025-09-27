package modpacktweaker.core;

import static modpacktweaker.core.ClassHandler.*;
import static modpacktweaker.core.ClassTransformer.*;
import static org.objectweb.asm.Opcodes.*;

import java.io.PrintWriter;
import java.util.List;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.util.TraceClassVisitor;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.Packet;

public class TCallsDebug {
	static void transformMinecraft(ClassNode classNode) {
		
		for(MethodNode method : classNode.methods) {
			if(method.name.equals(obf ? "ag" : "startGame") && method.desc.equals("()V")) {
				for(AbstractInsnNode node : method.instructions.toArray()) {
					if(node.getOpcode() == LDC &&"Minecraft 1.7.10".equals(((LdcInsnNode) node).cst)) {
						((LdcInsnNode) node).cst = "Minecraft Lord of the Rings";
						/*InsnList list = new InsnList();
						list.add(new VarInsnNode(ALOAD, 0)); // world
						list.add(new VarInsnNode(ALOAD, 1)); // chunk
						list.add(new VarInsnNode(ALOAD, 2)); // player
						list.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(HooksDebug.class), "moreInfo", 
							"(Lnet/minecraft/world/World;Lnet/minecraft/world/chunk/Chunk;Lnet/minecraft/entity/player/EntityPlayerMP;)V", false));
						
						method.instructions.insert(node, list);*/
						log.info(coreprefix+": Patched Minecraft.startGame()");
						break;
					}
				}
			}
		}
		
		/*PrintWriter printWriter = new PrintWriter(new LogWriter());
        ClassVisitor classVisitor = new TraceClassVisitor(printWriter);
        classNode.accept(classVisitor);
        printWriter.flush();*/
	}
}

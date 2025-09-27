package modpacktweaker.core;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;

import modpacktweaker.Setup;

import static modpacktweaker.core.TCallsDebug.*;

public class ClassHandler {
	
	public static final void callByIndex(ClassNode c, int i) {
		switch(i) {
			case 0: // net.minecraft.client.Minecraft
				transformMinecraft(c);							break;
		}
	}
	
	static byte[] transform(final byte[] classBytes, final int i) {
		if(i == 1) {
			Setup.startup();
			return classBytes;
		}
		
		
        //try {
            final ClassNode classNode = new ClassNode();
            final ClassReader classReader = new ClassReader(classBytes);
            classReader.accept(classNode, 0);
            final ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS/* | ClassWriter.COMPUTE_FRAMES*/);
            //System.out.println(classNode.name+" "+i);
            callByIndex(classNode, i);
            
            classNode.accept(classWriter);
            //return classWriter.toByteArray();
            
            byte[] bytes = classWriter.toByteArray();
            //saveClass(bytes, classNode.name+".class");
            return bytes;
        //}
        //catch (final Exception e) {
        //    e.printStackTrace();
        //}
        //return classBeingTransformed;
    }
	
	public static AbstractInsnNode lastBackwards(AbstractInsnNode last, int code) {
		while(last != null && last.getOpcode() != code) {
			last = last.getPrevious();
		}
		return last;
	}
	
	/*public static class LogWriter extends java.io.Writer {
        private final StringBuilder stringBuilder = new StringBuilder();

        @Override
        public void write(char[] cbuf, int off, int len) {
            stringBuilder.append(cbuf, off, len);
        }

        @Override
        public void flush() {
            ClassTransformer.log.info(stringBuilder.toString());
            stringBuilder.setLength(0);
        }

        @Override
        public void close() {}
    }*/
}

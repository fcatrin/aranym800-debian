package atari800;

public abstract class NativeInterface {
    static {
    	//boolean is64 = System.getProperty("sun.arch.data.model").equals("64");
    	//String postfix = is64?"64":"";
        System.loadLibrary("atari800");
    }
    
	public static native void init(NativeClient nativeClient);
}

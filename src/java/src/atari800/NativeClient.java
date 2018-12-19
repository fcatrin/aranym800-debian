package atari800;

public interface NativeClient {
	public void initPalette(int colors[]);
	public void displayScreen(byte atari_screen[]);

	public int   getKbHits(int key, int loc);
	public int[] pollKeyEvent();
	
	public boolean getWindowClosed();
	
	public void sleep(long msec);
	
	public void initGraphics(
			int scaleh, int scalew,
			int atari_width, int atari_height,
			int atari_visible_width,
			int atari_left_margin);
	
	public int initSound(
			int sampleRate, int bitsPerSample, int channels, 
			boolean isSigned, boolean bigEndian, 
			int bufferSize);	
	public void soundExit();
	public int  soundAvailable();
	public int  soundWrite(byte samples[], int len);
	public void soundPause();
	public void soundContinue();
	
	public int checkThreadStatus();
}

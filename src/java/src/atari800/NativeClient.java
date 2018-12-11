package atari800;

public interface NativeClient {
	public void initPalette(int colors[]);
	public void displayScreen(int atari_screen[]);
	public int  getKbhits(int key, int loc);
	public int  pollKeyEvent(int atari_event[]);
	public boolean getWindowClosed();
	public void sleep(long msec);
}

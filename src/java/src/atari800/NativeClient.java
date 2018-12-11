package atari800;

public interface NativeClient {
	public void initPalette(int colors[]);
	public void displayScreen(int atari_screen[]);
}

package atari800.swt.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;

import atari800.NativeClient;
import xtvapps.core.Log;
import xtvapps.core.swt.CustomWidget;
import xtvapps.core.swt.SWTUtils;
import xtvapps.core.swt.UITask;

public class EmulatorWidget extends CustomWidget implements NativeClient {
	private static final String LOGTAG = EmulatorWidget.class.getSimpleName();
	private static final int KEY_PRESSED  = 401;
	private static final int KEY_RELEASED = 402;
	
	private int atariWidth;
	private int atariLeftMargin;
	private int width;
	private int height;
	private byte[] pixels;
	private PaletteData paletteData;

	List<KeyEvent> keyQueue = new ArrayList<KeyEvent>();
	Set<String> kbHits = new HashSet<String>();
	
	private SourceDataLine line;

	private ImageData imageData;
	
	private float scale = 1.5f;

	public EmulatorWidget(Composite parent) {
		super(parent);
	}

	@Override
	protected void onPaint(PaintEvent e) {
		if (paletteData == null || pixels == null) return;
		
		if (imageData == null) {
			imageData = new ImageData(width, height, 8, paletteData, width, pixels);
		} else {
			imageData.setPixels(0, 0, width, pixels, 0);
		}
		Image image = new Image(getDisplay(), imageData);
		e.gc.drawImage(image, 0, 0, width, height, 0, 0, (int)(width*scale), (int)(height*scale));
		image.dispose();
	}

	public void initGraphics(int scalew, int scaleh, int atariWidth, int atariHeight, int atariVisibleWidth, int atariLeftMargin) {
		this.atariWidth      = atariWidth;
		this.atariLeftMargin = atariLeftMargin;
		
		width = atariVisibleWidth;
		height = atariHeight;
		
		int size = width*height;
		pixels = new byte[size];
		
		UITask task = new UITask() {

			@Override
			public void run() {
				SWTUtils.setSize(EmulatorWidget.this,  (int)(width*scale), (int)(height*scale));
				redraw();
				getShell().pack();
				Log.d(LOGTAG, "initGraphics " + width + "x" + height);
			}
		};
		task.execute();
	}

	public void displayScreen(byte[] atari_screen) {
		int ao = atariLeftMargin;
		int po = 0;
		for(int h = 0; h < height; h++){
			System.arraycopy(atari_screen, ao, pixels, po, width);
			ao += atariWidth;
			po += width;
		}
		this.postInvalidate();
	}

	public void initPalette(int[] colors) {
		RGB palette[] = new RGB[colors.length];
		for(int i=0; i<colors.length; i++){
			int entry = colors[i];
			
			int red   = (entry>>>16) & 0xff;
			int green = ((entry>>>8) & 0xff);
			int blue  = (entry & 0xff);

			RGB rgb = new RGB(red, green, blue);
			palette[i] = rgb;
		}
		
		paletteData = new PaletteData(palette);
	}

	private String buildKeyCode(int key, int loc) {
		return String.format("%d.%d", key, loc);
	}
	
	@Override
	protected void onKeyPressed(KeyEvent event) {
		event.data = true;
		keyQueue.add(event);
		
		int key = KeyMapper.map(event.keyCode);
		int loc = KeyMapper.mapLocation(event.keyLocation);
		kbHits.add(buildKeyCode(key, loc));
	}

	@Override
	protected void onKeyReleased(KeyEvent event) {
		event.data = false;
		keyQueue.add(event);

		int key = KeyMapper.map(event.keyCode);
		int loc = KeyMapper.mapLocation(event.keyLocation);
		kbHits.remove(buildKeyCode(key, loc));
	}

	@Override
	public int getKbHits(int key, int loc) {
		// Log.d(LOGTAG, "pollKeyEvent");
		return kbHits.contains(buildKeyCode(key, loc)) ? 1 : 0;
	}

	int atari_event[] = new int[4];

	@Override
	public int[] pollKeyEvent() {
		if (keyQueue.isEmpty()){
			return null;
		}
		KeyEvent event = (KeyEvent)keyQueue.get(0);
		keyQueue.remove(0);

		int type = ((boolean)event.data) ? KEY_PRESSED : KEY_RELEASED;
		int key  = event.keyCode;
		char uni = event.character;
		int loc  = event.keyLocation;
		
		atari_event[0] = type;
		atari_event[1] = KeyMapper.map(key);
		atari_event[2] = (int)uni;
		atari_event[3] = KeyMapper.mapLocation(loc);
		
		return atari_event;
	}

	@Override
	public boolean getWindowClosed() {
		return isDisposed();
	}

	@Override
	public int initSound(int sampleRate, int bitsPerSample, int channels, boolean isSigned, boolean bigEndian, int bufferSize){
		AudioFormat format = new AudioFormat(sampleRate, bitsPerSample, channels, isSigned, bigEndian);
		DataLine.Info info = new DataLine.Info(SourceDataLine.class,format);

		try {
			if (line != null) line.close();
			line = (SourceDataLine) AudioSystem.getLine(info);
			line.open(format, bufferSize);
		} catch(Exception e) {
			e.printStackTrace();
		}

		return line.getBufferSize();
	}


	@Override
	public void sleep(long msec) {
		try {
			Thread.sleep(msec);
		} catch(Exception e) {}
	}

	@Override
	public void soundExit() {
		line.close();
	}

	@Override
	public int soundAvailable() {
		int result = line.available();
		return result;
	}

	@Override
	public int soundWrite(byte[] samples, int len) {
		return line.write(samples,0, len);
	}

	@Override
	public void soundPause() {
		line.stop();
	}

	@Override
	public void soundContinue() {
		line.start();
	}

	@Override
	public int checkThreadStatus() {
		return 0;
	}
	
	private static class KeyMapper {
		private static Map<Integer, Integer> keymap = new HashMap<Integer, Integer>();
		
		public static int map(int keyCode) {
			if (keymap.containsKey(keyCode)) return keymap.get(keyCode);
			return keyCode;
		}
		
		public static int mapLocation(int location) {
			switch(location) {
			case SWT.LEFT   : return 2;
			case SWT.RIGHT  : return 3;
			case SWT.KEYPAD : return 4;
			default: return 1;
			}
		}
		
		static {
			keymap.put(SWT.F1, 112);
		}
	}

}

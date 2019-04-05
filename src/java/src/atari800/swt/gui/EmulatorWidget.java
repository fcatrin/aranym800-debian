package atari800.swt.gui;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

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
	
	private int atariWidth;
	private int atariLeftMargin;
	private int width;
	private int height;
	private byte[] pixels;
	private PaletteData paletteData;

	private SourceDataLine line;

	public EmulatorWidget(Composite parent) {
		super(parent);
	}

	@Override
	protected void onPaint(PaintEvent e) {
		if (paletteData == null || pixels == null) return;
		
		ImageData imageData = new ImageData(width, height, 8, paletteData, width, pixels);
		Image image = new Image(getDisplay(), imageData);
		
		e.gc.drawImage(image, 0, 0);
	}

	public void initGraphics(int scalew, int scaleh, int atariWidth, int atariHeight, int atariVisibleWidth, int atariLeftMargin) {
		try {
		this.atariWidth      = atariWidth;
		this.atariLeftMargin = atariLeftMargin;
		
		width = atariVisibleWidth;
		height = atariHeight;
		
		int size = width*height;
		pixels = new byte[size];
		
		UITask task = new UITask() {

			@Override
			public void run() {
				SWTUtils.setSize(EmulatorWidget.this, width, height);
				redraw();
				getShell().pack();
				Log.d(LOGTAG, "initGraphics " + width + "x" + height);
			}
		};
		task.execute();
		
		} catch (Exception e) {
			e.printStackTrace();
			Runtime.getRuntime().exit(0);
		}
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

	@Override
	public int getKbHits(int key, int loc) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int[] pollKeyEvent() {
		// TODO Auto-generated method stub
		return null;
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
		// TODO Auto-generated method stub
		return 0;
	}

}

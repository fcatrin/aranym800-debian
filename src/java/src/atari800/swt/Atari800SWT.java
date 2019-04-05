package atari800.swt;

import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

import org.eclipse.swt.widgets.Display;

import atari800.NativeClient;
import atari800.NativeInterface;
import atari800.swt.gui.EmulatorWindow;
import xtvapps.core.Log;
import xtvapps.core.swt.AsyncProcessor;
import xtvapps.core.swt.AsyncTask;
import xtvapps.core.swt.SWTUtils;

public class Atari800SWT implements NativeClient {
	private static final String LOGTAG = Atari800SWT.class.getSimpleName();
	
	private static EmulatorWindow emulatorWindow;
	SourceDataLine line;
	
	static boolean closed = false;
	
	public static void main(String[] args) throws IOException  {
		SWTUtils.display = new Display();

		Log.TRACE = true;
		Log.d(LOGTAG, "app started");
		
		AsyncTask.asyncProcessor = new AsyncProcessor(SWTUtils.display);
		AsyncTask.asyncProcessor.start();
		
		emulatorWindow = new EmulatorWindow(SWTUtils.display);

		NativeInterface.init(new Atari800SWT());
		Thread t = new Thread() {
			@Override
			public void run() {
				Log.d(LOGTAG, "NativeInterface started");
				NativeInterface.main(args);
			}
		};
		t.start();

		emulatorWindow.open();
		
		AsyncTask.asyncProcessor.shutdown();
		SWTUtils.display.dispose();
		closed = true;
	}

	@Override
	public void initPalette(int[] colors) {
		Log.d(LOGTAG, "initPalette");
		emulatorWindow.initPalette(colors);
	}

	@Override
	public void displayScreen(byte[] atari_screen) {
		emulatorWindow.displayScreen(atari_screen);
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
		return closed;
	}

	@Override
	public void initGraphics(int scaleh, int scalew, int atari_width, int atari_height, int atari_visible_width,
			int atari_left_margin) {
		Log.d(LOGTAG, "initGraphics");
		emulatorWindow.initGraphics(scalew, scaleh, atari_width, atari_height, atari_visible_width, atari_left_margin);
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

}

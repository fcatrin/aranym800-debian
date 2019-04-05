package atari800.swt;

import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

import org.eclipse.swt.widgets.Display;

import atari800.NativeClient;
import atari800.swt.gui.EmulatorWindow;
import xtvapps.core.swt.AsyncProcessor;
import xtvapps.core.swt.AsyncTask;
import xtvapps.core.swt.SWTUtils;

public class Atari800SWT implements NativeClient {
	
	SourceDataLine line;
	
	public static void main(String[] args) throws IOException  {
		SWTUtils.display = new Display();

		
		AsyncTask.asyncProcessor = new AsyncProcessor(SWTUtils.display);
		AsyncTask.asyncProcessor.start();
		
		EmulatorWindow emulatorWindow = new EmulatorWindow(SWTUtils.display);
		emulatorWindow.open();
		
		AsyncTask.asyncProcessor.shutdown();
		SWTUtils.display.dispose();
	}

	@Override
	public void initPalette(int[] colors) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void displayScreen(byte[] atari_screen) {
		// TODO Auto-generated method stub
		
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
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void initGraphics(int scaleh, int scalew, int atari_width, int atari_height, int atari_visible_width,
			int atari_left_margin) {
		// TODO Auto-generated method stub
		
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

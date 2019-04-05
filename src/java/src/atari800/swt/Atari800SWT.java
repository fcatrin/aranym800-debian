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

public class Atari800SWT {
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

		NativeInterface.init(emulatorWindow.getEmulatorWidget());
		Thread t = new Thread() {
			@Override
			public void run() {
				Log.d(LOGTAG, "NativeInterface started");
				NativeInterface.main(args);
			}
		};
		t.start();
		emulatorWindow.getEmulatorWidget().forceFocus();
		emulatorWindow.open();
		
		AsyncTask.asyncProcessor.shutdown();
		SWTUtils.display.dispose();
		closed = true;
	}
}

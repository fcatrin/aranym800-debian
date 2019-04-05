package atari800.swt;

import java.io.IOException;

import org.eclipse.swt.widgets.Display;

import atari800.swt.gui.EmulatorWindow;
import xtvapps.core.swt.AsyncProcessor;
import xtvapps.core.swt.AsyncTask;
import xtvapps.core.swt.SWTUtils;

public class Atari800SWT {

	public static void main(String[] args) throws IOException  {
		SWTUtils.display = new Display();

		
		AsyncTask.asyncProcessor = new AsyncProcessor(SWTUtils.display);
		AsyncTask.asyncProcessor.start();
		
		EmulatorWindow emulatorWindow = new EmulatorWindow(SWTUtils.display);
		emulatorWindow.open();
		
		AsyncTask.asyncProcessor.shutdown();
		SWTUtils.display.dispose();
	}

}

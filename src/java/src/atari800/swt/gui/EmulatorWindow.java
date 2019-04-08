package atari800.swt.gui;

import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import xtvapps.core.swt.SWTUtils;

public class EmulatorWindow {

	private Shell shell;
	private EmulatorWidget emulatorWidget;

	public EmulatorWindow(Display display) {
	    shell = new Shell(display);
	    shell.setText("Atari800");
	    
	    GridLayout gridLayout = new GridLayout(1, false);
	    // gridLayout.marginWidth = Style.WINDOW_MARGIN;
	    // gridLayout.marginHeight = Style.WINDOW_MARGIN;
   	    shell.setLayout(gridLayout);
   	    
   	    emulatorWidget = new EmulatorWidget(shell, 1.5f);
	}

	public void open() {
	    shell.pack();
	    shell.setSize(1024, 640);
	    SWTUtils.centerOnScreen(shell);
	    shell.open();
	    
	    SWTUtils.mainLoop(shell);
	}


	public EmulatorWidget getEmulatorWidget() {
		return emulatorWidget;
	}

}

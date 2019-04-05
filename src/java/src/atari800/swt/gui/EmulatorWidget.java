package atari800.swt.gui;

import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;

import xtvapps.core.Log;
import xtvapps.core.swt.CustomWidget;
import xtvapps.core.swt.SWTUtils;
import xtvapps.core.swt.UITask;

public class EmulatorWidget extends CustomWidget {
	private static final String LOGTAG = EmulatorWidget.class.getSimpleName();
	
	private int atariWidth;
	private int atariLeftMargin;
	private int width;
	private int height;
	private byte[] pixels;
	private PaletteData paletteData;

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

}

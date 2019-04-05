package atari800.swt.gui;

import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;

import xtvapps.core.swt.BackBuffer;
import xtvapps.core.swt.CustomWidget;

public class EmulatorWidget extends CustomWidget {

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
		ImageData imageData = new ImageData(width, height, 8, paletteData, width, pixels);
		Image image = new Image(getDisplay(), imageData);
		
		e.gc.drawImage(image, 0, 0);
	}

	public void initGraphics(int scalew, int scaleh, int atariWidth, int atariHeight, int atariVisibleWidth, int atariLeftMargin) {
		this.atariWidth      = atariWidth;
		this.atariLeftMargin = atariLeftMargin;
		
		width = atariVisibleWidth;
		height = atariHeight;
		
		int size = width*height;
		pixels = new byte[size];
	}

	public void displayScreen(byte[] atari_screen) {
		int ao = atariLeftMargin;
		int po = 0;
		for(int h = 0; h < height; h++){
			System.arraycopy(atari_screen, ao, pixels, po, width);
			ao += atariWidth;
			po += width;
		}
		redraw();
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

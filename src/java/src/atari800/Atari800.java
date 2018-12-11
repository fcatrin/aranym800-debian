package atari800;
/*
 * atari800.java - Java NestedVM port of atari800
 *
 * Copyright (C) 2007-2008 Perry McFarlane
 * Copyright (C) 1998-2013 Atari800 development team (see DOC/CREDITS)
 *
 * This file is part of the Atari800 emulator project which emulates
 * the Atari 400, 800, 800XL, 130XE, and 5200 8-bit computers.
 *
 * Atari800 is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Atari800 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Atari800; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
*/
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.*;
import javax.sound.sampled.*;
import java.applet.*;

class AtariCanvas extends Canvas implements KeyListener {
	byte pixels[];
	MemoryImageSource mis;
	IndexColorModel icm;
	Image image;
	int atari_width;
	int atari_height;
	int atari_visible_width;
	int atari_left_margin;
	int width;
	int height;
	int scalew;
	int scaleh;
	int size;
	boolean windowClosed = false;
	Vector keyqueue;
	Hashtable kbhits;
	byte[][] paletteTable;
	byte[] temp;

	public void paint(Graphics g) {
		update(g);
	}

	public void update(Graphics g) {
		g.drawImage(image,0,0,width*scalew,height*scaleh,null);
	}

	public void init(){
		width = atari_visible_width;
		temp = new byte[width];
		height = atari_height;
		size = width*height;
		pixels = new byte[size];
		for(int i=0;i<size;i++){
			pixels[i]=0;
		}
		keyqueue = new Vector();
		addKeyListener(this);
		kbhits = new Hashtable();
	}

	/* Init the palette*/
	/* colortable is a runtime memory pointer*/
	public void initPalette(int colors[]){
		paletteTable = new byte[3][256];
		int entry=0;
		for(int i=0; i<256; i++){
			try {
				entry = colors[i];
			} catch(Exception e) {
				System.err.println(e);
			}
			paletteTable[0][i]=(byte)((entry>>>16)&0xff);
			paletteTable[1][i]=(byte)((entry>>>8)&0xff);
			paletteTable[2][i]=(byte)(entry&0xff);
		}
		icm = new IndexColorModel(8,256,paletteTable[0],paletteTable[1],paletteTable[2]);
		mis = new MemoryImageSource(width,height,icm,pixels,0,width);
		mis.setAnimated(true);
		mis.setFullBufferUpdates(true);
		image = createImage(mis);
	}

	public void displayScreen(int atari_screen[]){
		int ao = atari_left_margin;
		int po = 0;
		for(int h=0; h<240;h++){
			try {
				System.arraycopy(atari_screen, ao,pixels,po,width);
			} catch(Exception e) {
				System.err.println(e);
			}
			ao += atari_width;
			po += width;
		}
		mis.newPixels();
		repaint();
	}

	/* called when the user closes the window */
	public void setWindowClosed() {
		windowClosed = true;
	}

	// KeyListener methods:

	public void keyPressed(KeyEvent event) {
		char chr = event.getKeyChar();
		int key = event.getKeyCode();
		int loc = event.getKeyLocation();
		keyqueue.addElement(event);
		Integer[] val = new Integer[2];
		val[0] = new Integer(key);
		val[1] = new Integer(loc);
		kbhits.put(Arrays.asList(val), new Boolean(true));
		//System.err.println("keyPressed: "+key+" location: "+loc);
	}

	public void keyReleased(KeyEvent event) {
		char chr = event.getKeyChar();
		int key = event.getKeyCode();
		int loc = event.getKeyLocation();
		keyqueue.addElement(event);
		Integer[] val = new Integer[2];
		val[0] = new Integer(key);
		val[1] = new Integer(loc);
		kbhits.remove(Arrays.asList(val));
	}

	public void keyTyped(KeyEvent event) {
	}

	/* get a keyboard key state */
	int getKbhits(int key, int loc){
		Integer[] val = new Integer[2];
		val[0] = new Integer(key);
		val[1] = new Integer(loc);
		if  (kbhits.get(Arrays.asList(val)) != null ){
			return 1;
		}
		else{ 
			return 0;
		}
	}

	/* event points to an array of 4 values*/
	int pollKeyEvent(int atari_event[]){
		if (keyqueue.isEmpty()){
			return 0;
		}
		KeyEvent event = (KeyEvent)keyqueue.firstElement();
		keyqueue.removeElement(event);
		int type = event.getID();
		int key = event.getKeyCode();
		char uni = event.getKeyChar();
		int loc = event.getKeyLocation();
		try{
			/* write the data to the array pointed to by event*/
			atari_event[0] = type;
			atari_event[1] = key;
			atari_event[2] = (int)uni;
			atari_event[3] = loc;
		} catch(Exception e) {
			System.err.println(e);
		}
		return 1;
	}

	/* 1 if the Window was closed */
	boolean getWindowClosed(){
		return windowClosed;
	}
}

public class Atari800 extends Applet implements Runnable, NativeClient {
	AtariCanvas canvas;
	Frame frame;
	SourceDataLine line;
	byte[] soundBuffer;
	boolean isApplet;
	Thread thread;
	private volatile boolean threadSuspended;

	//Applet constructor:
	public Atari800() {
		isApplet = true;
	}

	//Application constructor:
	public Atari800(Frame f) {
		isApplet = false;
		frame = f;
	}

	@Override
	public void initGraphics(int scalew, int scaleh, int atari_width, int atari_height, int atari_visible_width, int atari_left_margin){
		canvas = new AtariCanvas();
		canvas.atari_width = atari_width;
		canvas.atari_height = atari_height;
		canvas.atari_visible_width = atari_visible_width;
		canvas.atari_left_margin = atari_left_margin;
		canvas.init();
		canvas.setFocusTraversalKeysEnabled(false); //allow Tab key to work
		canvas.setFocusable(true);
		if (!isApplet) {
			frame.addWindowListener(new WindowAdapter() {
				public void windowsGainedFocus(WindowEvent e) {
					canvas.requestFocusInWindow();
				}
				public void windowClosing(WindowEvent e) {
					canvas.setWindowClosed();
				}
			});
		}
		canvas.requestFocusInWindow();
		canvas.setSize(new Dimension(canvas.width*scalew,canvas.height*scaleh));
		canvas.scalew = scalew;
		canvas.scaleh = scaleh;
		if (isApplet) {
			this.add(canvas);
			canvas.requestFocus();
		}
		else {
			frame.add(canvas);
			frame.setResizable(false);
			frame.pack();
			Insets insets = frame.getInsets();
			frame.setSize(new Dimension(canvas.width*scalew+insets.left+insets.right, canvas.height*scaleh+insets.top+insets.bottom));
			frame.setVisible(true);
		}
	}

	@Override
	public int initSound(int sampleRate, int bitsPerSample, int channels, boolean isSigned, boolean bigEndian, int bufferSize){
		AudioFormat format = new AudioFormat(sampleRate, bitsPerSample, channels, isSigned, bigEndian);
		DataLine.Info info = new DataLine.Info(SourceDataLine.class,format);

		try {
			if (line != null) line.close();
			line = (SourceDataLine) AudioSystem.getLine(info);
			line.open(format, bufferSize);
			soundBuffer = new byte[line.getBufferSize()];
		} catch(Exception e) {
			System.err.println(e);
		}
		return line.getBufferSize();
	}

	//Applet init
	public void init() {
		//System.out.println("init()");
		setLayout(null);
	}

	//Applet start
	public synchronized void start() {
		//System.out.println("start()");
		if (thread == null) {
			thread = new Thread(this);
			thread.start();
		}
		threadSuspended = false;
		thread.interrupt();
	}

	//Applet stop
	public synchronized void stop() {
		//System.out.println("stop()");
		threadSuspended = true;
	}

	//Applet destroy
	public void destroy() {
		//System.out.println("destroy()");
		if (line!=null) line.close();
		if (canvas!=null) this.remove(canvas);
		Thread dead = thread;
		thread = null;
		dead.interrupt();
	}

	//Applet run thread
	public void run() {
		//System.out.println("run()");
		String args = getParameter("args");
		this.main2(args.split("\\s"));
	}

	public void paint(Graphics g) {
		//System.out.println("paint");
		if (canvas!=null) canvas.paint(g);
	}

	public static void main(String[] args) {
		Frame f = new Frame();
		final Atari800 app = new Atari800(f);
		f.setTitle("atari800");
		app.main2(args);
	}

	// used by both Application and Applet
	public void main2(String[] args) {
		//Place holder for command line arguments
		String[] appArgs = new String[args.length +1];
		try {
			final Thread thisThread = Thread.currentThread();
			//Application name
			appArgs[0] = "atari800";
			//Fill in the rest of the command line arguments
			for(int i=0;i<args.length;i++) appArgs[i+1] = args[i];

			//Make a Runtime instantiation
			final Runtime rt;

			String className = "atari800_runtime";
			/*TO use the interpreter:*/
			/*
			if(className.endsWith(".mips")){
				rt = new org.ibex.nestedvm.Interpreter(className);
			} else {*/
				Class c = Class.forName(className);
				if(!Runtime.class.isAssignableFrom(c)) { 
					System.err.println(className + " isn't a MIPS compiled class");
					System.exit(1); 
				}
				rt = (Runtime) c.newInstance();
			//}
			rt.setCallJavaCB(new Runtime.CallJavaCB() {
				public int call(int a, int b, int c, int d) {
					switch(a) {
						case 12:
							/*static int JAVANVM_SoundPause(void){
								return _call_java(11, 0, 0, 0);
							}*/
							line.stop();
							return 0;
						case 13:
							/*static int JAVANVM_SoundContinue(void){
								return _call_java(12, 0, 0, 0);
							}*/
							line.start();
							return 0;
						case 14:
							/*static int JAVANVM_CheckThreadStatus(void){
								return _call_java(13, 0, 0, 0);
							}*/
							if (isApplet && threadSuspended) {
								if (line!=null) line.stop();
								try {
									synchronized(this) {
										while (threadSuspended && thread == thisThread) {
										   	wait();
										}
									}
								} catch (InterruptedException e) {}
								if (thread != thisThread) {
									return 1;
								}
								if (line!=null) line.start();
							}
							return 0;
						default:
							return 0;
					}
				 }
			});
			if (isApplet) {
			}
			//Run the emulator:
			if (!isApplet) {
				System.exit(rt.run(appArgs));
			} else {
				rt.run(appArgs);
			}
		} catch(Exception e) {
			System.err.println(e);
		}
	}

	@Override
	public void initPalette(int[] colors) {
		canvas.initPalette(colors);
	}

	@Override
	public void displayScreen(int[] atari_screen) {
		canvas.displayScreen(atari_screen);
	}

	@Override
	public int getKbhits(int key, int loc) {
		return canvas.getKbhits(key, loc);
	}

	@Override
	public int pollKeyEvent(int[] atari_event) {
		return canvas.pollKeyEvent(atari_event);
	}

	@Override
	public boolean getWindowClosed() {
		return canvas.getWindowClosed();
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
		return line.available();
	}

	@Override
	public int soundWrite(byte[] samples, int len) {
		return line.write(samples,0, len);
	}
	
}

/*
vim:ts=4:sw=4:
*/

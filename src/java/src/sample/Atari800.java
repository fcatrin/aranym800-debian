package sample;
/*
 * Atari800.java - Java port of atari800
 *
 * Copyright (C) 2007-2008 Perry McFarlane
 * Copyright (C) 1998-2013 Atari800 development team (see DOC/CREDITS)
 * Copyright (C) 2018      Franco Catrin
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

import java.applet.Applet;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

import atari800.NativeClient;
import atari800.NativeInterface;

public class Atari800 extends Applet implements Runnable, NativeClient {
	private static final long serialVersionUID = 1L;
	
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
	public void initGraphics(int scalew, int scaleh, int width, int height, int visibleWidth, int leftMargin){
		canvas = new AtariCanvas(width, height, visibleWidth, leftMargin);
		canvas.setFocusTraversalKeysEnabled(false); //allow Tab key to work
		canvas.setFocusable(true);
		if (!isApplet) {
			frame.addWindowListener(new WindowAdapter() {
				
				@Override
				public void windowGainedFocus(WindowEvent e) {
					canvas.requestFocusInWindow();
				}

				@Override
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
		} else {
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
			e.printStackTrace();
		}

		return line.getBufferSize();
	}

	//Applet init
	public void init() {
		setLayout(null);
	}

	//Applet start
	public synchronized void start() {
		if (thread == null) {
			thread = new Thread(this);
			thread.start();
		}
		threadSuspended = false;
		thread.interrupt();
	}

	//Applet stop
	public synchronized void stop() {
		threadSuspended = true;
	}

	//Applet destroy
	public void destroy() {
		if (line!=null) line.close();
		if (canvas!=null) this.remove(canvas);
		
		Thread dead = thread;
		thread = null;
		dead.interrupt();
	}

	//Applet run thread
	public void run() {
		String args = getParameter("args");
		this.main2(args.split("\\s"));
	}

	public void paint(Graphics g) {
		if (canvas!=null) canvas.paint(g);
	}

	public static void main(String[] args) {
		Frame f = new Frame();
		final Atari800 app = new Atari800(f);
		f.setTitle("atari800");
		app.main2(args);
	}

	Thread thisThread;
	
	// used by both Application and Applet
	public void main2(String[] args) {
		String[] appArgs = new String[args.length +1];
		try {
			thisThread = Thread.currentThread();
			
			//Application name
			appArgs[0] = "atari800";
			//Fill in the rest of the command line arguments
			for(int i=0;i<args.length;i++) appArgs[i+1] = args[i];

			NativeInterface.init(this);
			NativeInterface.main(appArgs);
		} catch(Exception e) {
			System.err.println(e);
		}
	}

	@Override
	public void initPalette(int[] colors) {
		canvas.initPalette(colors);
	}

	@Override
	public void displayScreen(byte[] atari_screen) {
		canvas.displayScreen(atari_screen);
	}

	@Override
	public int getKbHits(int key, int loc) {
		return canvas.getKbHits(key, loc) ? 1 : 0;
	}

	@Override
	public int[] pollKeyEvent() {
		return canvas.pollKeyEvent();
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
	}
	
}

package sample;

/*
 * AtariCanvas.java - Java port of atari800 - sample application
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

import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.IndexColorModel;
import java.awt.image.MemoryImageSource;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class AtariCanvas extends Canvas implements KeyListener {
	private static final long serialVersionUID = 1L;
	
	byte pixels[];
	MemoryImageSource mis;
	IndexColorModel icm;
	Image image;
	
	int atariWidth;
	int atariLeftMargin;
	
	int width;
	int height;
	int scalew;
	int scaleh;

	boolean windowClosed = false;
	
	List<KeyEvent> keyQueue = new ArrayList<KeyEvent>();
	Set<String> kbHits = new HashSet<String>();
	
	byte[][] paletteTable;

	public void paint(Graphics g) {
		update(g);
	}

	public void update(Graphics g) {
		g.drawImage(image,0,0,width*scalew,height*scaleh,null);
	}

	public AtariCanvas(int atariWidth, int atariHeight, int atariVisibleWidth, int atariLeftMargin) {
		
		this.atariWidth      = atariWidth;
		this.atariLeftMargin = atariLeftMargin;
		
		width = atariVisibleWidth;
		height = atariHeight;
		
		int size = width*height;
		pixels = new byte[size];

		addKeyListener(this);
	}

	/* Init the palette*/
	public void initPalette(int colors[]){
		paletteTable = new byte[3][256];

		for(int i=0; i<256; i++){
			int entry = colors[i];
			paletteTable[0][i]=(byte)((entry>>>16)&0xff);
			paletteTable[1][i]=(byte)((entry>>>8)&0xff);
			paletteTable[2][i]=(byte)(entry&0xff);
		}
		icm = new IndexColorModel(8, 256, paletteTable[0], paletteTable[1], paletteTable[2]);
		mis = new MemoryImageSource(width, height, icm, pixels, 0, width);
		mis.setAnimated(true);
		mis.setFullBufferUpdates(true);
		image = createImage(mis);
	}

	public void displayScreen(byte atari_screen[]){
		int ao = atariLeftMargin;
		int po = 0;
		for(int h = 0; h < height; h++){
			System.arraycopy(atari_screen, ao, pixels, po, width);
			ao += atariWidth;
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

	private String buildCode(int key, int loc) {
		return String.format("%d.%d", key, loc);
	}
	
	public void keyPressed(KeyEvent event) {
		keyQueue.add(event);
		
		int key = event.getKeyCode();
		int loc = event.getKeyLocation();
		kbHits.add(buildCode(key, loc));
	}

	public void keyReleased(KeyEvent event) {
		keyQueue.add(event);

		int key = event.getKeyCode();
		int loc = event.getKeyLocation();
		kbHits.remove(buildCode(key, loc));
	}

	public void keyTyped(KeyEvent event) {}

	/* get a keyboard key state */
	boolean getKbHits(int key, int loc){
		return kbHits.contains(buildCode(key, loc));
	}

	int atari_event[] = new int[4];
	
	int[] pollKeyEvent(){
		if (keyQueue.isEmpty()){
			return null;
		}
		KeyEvent event = (KeyEvent)keyQueue.get(0);
		keyQueue.remove(0);

		int type = event.getID();
		int key  = event.getKeyCode();
		char uni = event.getKeyChar();
		int loc  = event.getKeyLocation();
		
		atari_event[0] = type;
		atari_event[1] = key;
		atari_event[2] = (int)uni;
		atari_event[3] = loc;
		
		return atari_event;
	}

	/* 1 if the Window was closed */
	boolean getWindowClosed(){
		return windowClosed;
	}
}

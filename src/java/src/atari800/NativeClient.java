package atari800;
/*
 * Java port of atari800
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

public interface NativeClient {
	public void initPalette(int colors[]);
	public void displayScreen(byte atari_screen[]);

	public int   getKbHits(int key, int loc);
	public int[] pollKeyEvent();
	
	public boolean getWindowClosed();
	
	public void sleep(long msec);
	
	public void initGraphics(
			int scaleh, int scalew,
			int atari_width, int atari_height,
			int atari_visible_width,
			int atari_left_margin);
	
	public int initSound(
			int sampleRate, int bitsPerSample, int channels, 
			boolean isSigned, boolean bigEndian, 
			int bufferSize);	
	public void soundExit();
	public int  soundAvailable();
	public int  soundWrite(byte samples[], int len);
	public void soundPause();
	public void soundContinue();
	
	public int checkThreadStatus();
}

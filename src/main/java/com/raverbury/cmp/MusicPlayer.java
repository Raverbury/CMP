package com.raverbury.cmp;

import com.raverbury.cmp.Track;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import org.json.*;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.concurrent.ThreadLocalRandom;

public class MusicPlayer extends JFrame {
	Track tracks[];

	int currentTrackInd;
	int maxNumTracks;
	boolean isPlaying;

	File file;
	AudioInputStream audioIn = null;
	Clip clip = null;

	String dirPath;

	JButton prevFlavorButton;
	JButton toggleButton;
	JButton nextFlavorButton;
	JButton prevTrackButton;
	JButton randomButton;
	JButton nextTrackButton;
	JLabel trackLabel;
	JLabel flavorLabel;

	public MusicPlayer () {
		super("Custom Music Player 2.0");
		dirPath = "./resources/";

		autoCorrectPath();
		initTracks();
		initApp();

		currentTrackInd = 0;
		maxNumTracks = tracks.length;
		// auto change dir to \tracks after reading metadata
		dirPath += "tracks/";

		setPlayingState(false);
		setLabel();
		randomize();
	}

	private void initTracks () {
		String jsString = readMetadata();
		JSONArray jsTracks = new JSONArray(jsString);
		tracks = new Track[jsTracks.length()];
		for (int i = 0; i < jsTracks.length(); i++) {
			tracks[i] = new Track(jsTracks.getJSONObject(i));
			// tracks[i].dump();
		}
	}

	private void initApp () {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(500, 300);
		setResizable(false);
		setLayout(null);
		setVisible(true);

		prevFlavorButton = new JButton("Prev flavor");
		prevFlavorButton.setBounds(50, 185, 100, 50);
		add(prevFlavorButton);

		toggleButton = new JButton("Toggle");
		toggleButton.setBounds(200, 115, 100, 50);
		add(toggleButton);

		nextFlavorButton = new JButton("Next flavor");
		nextFlavorButton.setBounds(350, 185, 100, 50);
		add(nextFlavorButton);

		prevTrackButton = new JButton("Prev track");
		prevTrackButton.setBounds(50, 115, 100, 50);
		add(prevTrackButton);

		randomButton = new JButton("Random");
		randomButton.setBounds(200, 185, 100, 50);
		add(randomButton);

		nextTrackButton = new JButton("Next track");
		nextTrackButton.setBounds(350, 115, 100, 50);
		add(nextTrackButton);

		trackLabel = new JLabel("Loading");
		trackLabel.setHorizontalAlignment(JLabel.CENTER);
		trackLabel.setBounds(0, 25, 500, 30);
		add(trackLabel);	

		flavorLabel = new JLabel("Loading");
		flavorLabel.setHorizontalAlignment(JLabel.CENTER);
		flavorLabel.setBounds(0, 60, 500, 30);
		add(flavorLabel);

		prevFlavorButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				prevFlavor();
			}
		});

		randomButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				randomize();
			}
		});

		nextFlavorButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				nextFlavor();
			}
		});

		prevTrackButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				prevTrack();
			}
		});

		toggleButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				toggle();
			}
		});

		nextTrackButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				nextTrack();
			}
		});
	}

	// event callbacks
	private void nextTrack () {
		// tracks[currentTrackInd].resetFlavor();
		playTrackAt(currentTrackInd + 1);		
	}

	private void prevTrack () {
		// tracks[currentTrackInd].resetFlavor();
		playTrackAt(currentTrackInd - 1);
	}

	private void toggle () {
		if (isPlaying) stopTrack();
		else playTrackAt(currentTrackInd);
	}

	private void nextFlavor () {
		if (!tracks[currentTrackInd].nextFlavor() && isPlaying) return;
		isPlaying = false;
		playTrackAt(currentTrackInd);
	}

	private void prevFlavor () {
		if (!tracks[currentTrackInd].prevFlavor() && isPlaying) return;
		isPlaying = false;
		playTrackAt(currentTrackInd);
	}

	private void randomize () { // can optimize this but won't, too lazy
		int oldTrackInd = currentTrackInd;
		int oldFlavorInd = getCurrentFlavorInd();
		int num = 0;
		do { // reroll if get the same results as the current track+flavor and if there are other choices
			num = ThreadLocalRandom.current().nextInt(0, maxNumTracks);
			tracks[currentTrackInd].randomize();
		} while (oldTrackInd == num && oldFlavorInd == getCurrentFlavorInd() && (maxNumTracks > 1 || (maxNumTracks == 1 && tracks[num].getMaxNumFlavors() > 1)));
		if (tracks[num].getMaxNumFlavors() > 1) isPlaying = false;
		playTrackAt(num);
	}

	// one-time setups
	private void autoCorrectPath () {
		try {
			FileReader reader = new FileReader(dirPath + "metadata.json");
		} catch (Exception e) { // to allow compile_and_run.bat to run normally
			log("Could not find the file specified. Attempting to auto-correct file path...");
			dirPath = "./../" + dirPath;
		}
		try {
			FileReader reader = new FileReader(dirPath + "metadata.json");
			log("Success.");
		} catch (Exception e) {
			log("Still could not find the file specified.");
			e.printStackTrace();
		}
	}

	private String readMetadata() {
		String content = null;
		try {
			String filePath = dirPath + "metadata.json";
    	File file = new File(filePath); // For example, foo.txt
    	FileReader reader = new FileReader(filePath);
			char[] chars = new char[(int) file.length()];
			reader.read(chars);
    	content = new String(chars);
			reader.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
    return content;
	}

	// core functions
	private void playTrackAt (int index) {
		if (!setIndex(index) && isPlaying) return; // don't re-play the track that's already playing
		try {
			if (clip != null) {
				clip.stop();
				clip.close();
				try {
					audioIn.close();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}

			setPlayingState(true);
			setLabel();
			log("Playing: " + getCurrentTrackNameF() + " (" + getCurrentFlavorNameF() + ")");

			file = new File(dirPath + getCurrentTrackFilename());
			audioIn = AudioSystem.getAudioInputStream(file);
			clip = AudioSystem.getClip();
			clip.open(audioIn);

			// set loop pos, where the magic truly happens
			clip.setMicrosecondPosition(getCurrentFlavorStart());
			int loopStartFrame = clip.getFramePosition();
			clip.setMicrosecondPosition(getCurrentFlavorEnd());
			int loopEndFrame = clip.getFramePosition();
			clip.setMicrosecondPosition(0);
			clip.setLoopPoints(0, -1);
			clip.loop(Clip.LOOP_CONTINUOUSLY);
			clip.setLoopPoints(loopStartFrame, loopEndFrame);
			// disappointing magic, I know
		} catch (UnsupportedAudioFileException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (LineUnavailableException ex) {
			ex.printStackTrace();
		}
	}

	private void stopTrack () {
		setPlayingState(false);
		if (clip != null) {
			log("Stopped");
			clip.stop();
			clip.close();
			try {
				audioIn.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	// managers
	private boolean setIndex (int newIndex) {
		int oldIndex = currentTrackInd;
		if (newIndex < 0) newIndex = maxNumTracks - 1;
		else if (newIndex > maxNumTracks - 1) newIndex = 0;
		currentTrackInd = newIndex;
		return oldIndex != currentTrackInd;
	}

	private void setPlayingState (boolean newState) {
		isPlaying = newState;
		setToggleButton();
	}

	private void setLabel () {
		trackLabel.setText(getCurrentTrackNameF());
		flavorLabel.setText(getCurrentFlavorNameF());
	}

	private void setToggleButton () {
		if (isPlaying) toggleButton.setText("Playing");
		else toggleButton.setText("Stopped");
	}

	// getters, track level
	private String getCurrentTrackName () {
		return tracks[currentTrackInd].getTrackName();
	}

	private String getCurrentTrackNameF () {
		return getCurrentTrackName() + " [" + String.valueOf(currentTrackInd+1) + "/" + maxNumTracks + "]";
	}

	private String getCurrentTrackFilename () {
		return tracks[currentTrackInd].getFilename();
	}

	// getters, flavor level
	private int getCurrentFlavorInd () {
		return tracks[currentTrackInd].getFlavorInd();
	}

	private int getCurrentFlavorStart () {
		return tracks[currentTrackInd].getStartPos();
	}

	private int getCurrentFlavorEnd () {
		return tracks[currentTrackInd].getEndPos();
	}

	private String getCurrentFlavorName () {
		return tracks[currentTrackInd].getFlavorName();
	}

	private String getCurrentFlavorNameF () {
		return tracks[currentTrackInd].getFlavorNameF();
	}

	// misc
	private void log (Object msg) {
		System.out.println(msg);
	}
}

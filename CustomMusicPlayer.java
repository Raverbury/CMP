import java.io.*;
import java.net.URL;
import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.Scanner;

public class CustomMusicPlayer {

	public static void main(String Args[]) {

		class TrackInfo {
			String filename;
			String trackname;
			int startLoopPos;
			int endLoopPos;
		}

		class Track {
			File file;
			AudioInputStream audioIn = null;
			Clip clip = null;
			int currentTrack;
			TrackInfo[] playlist;
			boolean playing;

			public Track () {
				file = null;
				audioIn = null;
				clip = null;
				currentTrack = 0;
				playlist = null;
				playing = false;			
			}

			public void load(TrackInfo[] _playlist) {
				playlist = _playlist;
			}

			private String stop() {
				if (clip != null) {
					System.out.println("Stopped current track");
					clip.stop();
					clip.close();
					try {
						audioIn.close();
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
				playing = false;
				return "Stopped";
			}

			public String toggle() {
				if (playing) return stop();
				return play(currentTrack);
			}

			public String play(int trackIndex) {
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
					playing = true;
					currentTrack = trackIndex;
					file = new File(playlist[currentTrack].filename);
					audioIn = AudioSystem.getAudioInputStream(file);
					clip = AudioSystem.getClip();
					clip.open(audioIn);
					System.out.println("Now playing: " + playlist[currentTrack].trackname);
					//set loop pos
					clip.setMicrosecondPosition(playlist[currentTrack].startLoopPos);
					int startLoopFrame = clip.getFramePosition();
					clip.setMicrosecondPosition(playlist[currentTrack].endLoopPos);
					int endLoopFrame = clip.getFramePosition();
					clip.setMicrosecondPosition(0);
					clip.setLoopPoints(0, -1);
					clip.loop(Clip.LOOP_CONTINUOUSLY);
					clip.setLoopPoints(startLoopFrame, endLoopFrame);
				} catch (UnsupportedAudioFileException ex) {
					ex.printStackTrace();
				} catch (IOException ex) {
					ex.printStackTrace();
				} catch (LineUnavailableException ex) {
					ex.printStackTrace();
				}
				return playlist[currentTrack].trackname;
			}

			public String playDefault() {
				return play(0);
			}

			public String next() {
				if (currentTrack >= playlist.length - 1) return play(0);
				return play(currentTrack + 1);
			}

			public String prev() {
				if (currentTrack <= 0) return play(playlist.length - 1);
				return play(currentTrack - 1);
			}
		}

		//count number of tracks
		int numTracks = 0;
		try {
			File registry = new File("metadata.txt");
			Scanner sc = new Scanner(registry);
			while (sc.hasNextLine()) {
				char c = sc.nextLine().charAt(0);
				if (c != '#') numTracks++;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		//init array to store data of tracks
		TrackInfo[] trackInfoList = new TrackInfo[numTracks];
		for (int i = 0; i < trackInfoList.length; i++) {
			trackInfoList[i] = new TrackInfo();
		}
		
		//read data of tracks
		try {
			File registry = new File("metadata.txt");
			Scanner sc = new Scanner(registry);
			int i = 0;
			while (sc.hasNextLine()) {
				String tmp = sc.nextLine();
				if (tmp.charAt(0) != '#') {
					String[] words = tmp.split(", ");
					trackInfoList[i].filename = "track/" + words[0];
					trackInfoList[i].trackname = words[1];
					trackInfoList[i].startLoopPos = Integer.parseInt(words[2]);
					trackInfoList[i].endLoopPos = Integer.parseInt(words[3]);
					i++;
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		// build window + music player
		JFrame window = new JFrame("Custom Music Player");
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setSize(500, 300);
		window.setLayout(null);
		window.setVisible(true);

		JButton nextButton = new JButton("Next");
		nextButton.setBounds(350, 150, 100, 50);
		window.add(nextButton);

		JButton toggleButton = new JButton("Toggle");
		toggleButton.setBounds(200, 150, 100, 50);
		window.add(toggleButton);

		JButton prevButton = new JButton("Prev");
		prevButton.setBounds(50, 150, 100, 50);
		window.add(prevButton);

		JLabel label = new JLabel(trackInfoList[0].trackname);
		label.setHorizontalAlignment(JLabel.CENTER);
		label.setBounds(100, 50, 300, 50);
		window.add(label);

		//play default
		Track track = new Track();
		track.load(trackInfoList);
		track.playDefault();	

		nextButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				String str = track.next();
				label.setText(str);
			}
		});

		toggleButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				String str = track.toggle();
				label.setText(str);
			}
		});

		prevButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				String str = track.prev();
				label.setText(str);
			}
		});
	}
}
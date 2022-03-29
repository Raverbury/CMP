package com.raverbury.cmp;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.concurrent.ThreadLocalRandom;

public class Track {
	String trackName;
	String filename;
	TrackFlavor flavors[];

	int maxNumFlavors;
	int currentFlavorInd;

	public Track (JSONObject jsTracks) {
		trackName = jsTracks.getString("trackname");
		filename = jsTracks.getString("filename");
		JSONArray jsFlavors = jsTracks.getJSONArray("flavors");
		flavors = new TrackFlavor[jsFlavors.length()];
		
		for (int i = 0; i < jsFlavors.length(); i++) {
			flavors[i] = new TrackFlavor(jsFlavors.getJSONObject(i));
			// flavors[i].dump();
		}

		maxNumFlavors = flavors.length;
		currentFlavorInd = 0;
	}

	// core functions
	public boolean nextFlavor () {
		return setFlavor(currentFlavorInd + 1);
	}

	public boolean prevFlavor () {
		return setFlavor(currentFlavorInd - 1);
	}

	public void resetFlavor () {
		currentFlavorInd = 0;
	}

	public void randomize () {
		int num = ThreadLocalRandom.current().nextInt(0, maxNumFlavors);
		setFlavor(num);
	}

	// managers
	private boolean setFlavor (int newIndex) {
		int oldIndex = currentFlavorInd;
		if (newIndex < 0) newIndex = maxNumFlavors - 1;
		else if (newIndex > maxNumFlavors - 1) newIndex = 0;
		currentFlavorInd = newIndex;
		return oldIndex != currentFlavorInd;
	}

	// getters, track level
	public String getFilename () {
		return filename;
	}

	public String getTrackName () {
		return trackName;
	}

	public int getFlavorInd () {
		return currentFlavorInd;
	}

	public int getMaxNumFlavors () {
		return maxNumFlavors;
	}

	// getters, flavor level
	public int getStartPos () {
		return flavors[currentFlavorInd].getStartPos();
	}

	public int getEndPos () {
		return flavors[currentFlavorInd].getEndPos();
	}

	public String getFlavorName () {
		return flavors[currentFlavorInd].getFlavorName();
	}

	public String getFlavorNameF() {
		return flavors[currentFlavorInd].getFlavorName() + " [" + String.valueOf(currentFlavorInd+1) + "/" + maxNumFlavors + "]";
	}

	// misc
	public void dump () {
		String msg = ">>>>>Track: " + trackName + "(" + filename + ")";
		System.out.println(msg);
	}
}

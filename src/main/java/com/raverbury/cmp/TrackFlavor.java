package com.raverbury.cmp;

import org.json.JSONObject;

public class TrackFlavor {
	String flavorName;
	int loopStartPos;
	int loopEndPos;

  public TrackFlavor (JSONObject jsFlavor) {
		flavorName = jsFlavor.getString("flavorName");
		loopStartPos = jsFlavor.getInt("loopStartPos");
		loopEndPos = jsFlavor.getInt("loopEndPos");
	}

	// getters, flavor level
	public int getStartPos() {
		return loopStartPos;
	}

	public int getEndPos() {
		return loopEndPos;
	}

	public String getFlavorName () {
		return flavorName;
	}

	// misc
	public void dump() {
		String msg = "Flavor: " + flavorName + "(" + loopStartPos + " ~ " + loopEndPos + ")";
		System.out.println(msg);
	}
}
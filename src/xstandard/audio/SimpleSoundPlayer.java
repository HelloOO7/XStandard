package xstandard.audio;

import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;

public class SimpleSoundPlayer {

	public void setAndPlayClip(AudioClip c) {
		pauseClip();
		currentClip = c;
		playClip();
	}

	private AudioClip currentClip = null;

	private final LineListener dummyLineListener = (LineEvent event) -> {
	};

	public boolean isPlaying(){
		return currentClip != null && currentClip.isActive();
	}
	
	public void pauseClip() {
		if (currentClip != null) {
			currentClip.stop();
			currentClip.clip.removeLineListener(dummyLineListener);
		}
	}

	public void playClip() {
		if (currentClip != null) {
			currentClip.clip.addLineListener(dummyLineListener);
			currentClip.start();
		}
	}
	
	public void pauseOrUnpause(){
		if (currentClip != null){
			if (currentClip.isActive()){
				pauseClip();
			}
			else {
				playClip();
			}
		}
	}
}

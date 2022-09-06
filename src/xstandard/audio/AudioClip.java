package xstandard.audio;

import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

public class AudioClip {

	public static final float VOLUME_RANGE = 100f;

	public AudioInfo info;
	public Clip clip;

	public AudioClip(AudioInfo i, Clip c) {
		info = i;
		clip = c;
	}

	public boolean isActive() {
		return clip.isActive();
	}

	public void stop() {
		clip.stop();
	}

	public void start() {
		if (info.isLooped) {
			clip.loop(-1);
		} else {
			clip.start();
		}
	}

	private float volumeCoef = 100f;
	private float volume = 100f;

	public float getFrame() {
		return (clip.getMicrosecondPosition() / 1000000f) * 30f;
	}

	public float getFrameCount() {
		return (clip.getMicrosecondLength() / 1000000f) * 30f;
	}
	
	public void setFrame(float frame) {
		clip.setMicrosecondPosition((long)((frame / 30f) * 1000000f));
	}

	public float getVolume() {
		return volume;
	}

	public void setVolume(float vol) {
		volume = vol;
		float volumeInternal = (volume / VOLUME_RANGE) * (volumeCoef / VOLUME_RANGE);
		FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
		gainControl.setValue(20f * (float) Math.log10(volumeInternal));
	}

	public void setMasterVolume(float vol) {
		volumeCoef = vol;
		setVolume(volume);
	}
}

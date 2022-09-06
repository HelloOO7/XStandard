package xstandard.audio;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;

public abstract class AbstractAudioFile {

	public abstract AudioInfo getInfo();

	public abstract byte[] getChannelData(int ch);

	public AudioClip getClip() {
		try {
			AudioInfo i = getInfo();
			AudioFormat format = new AudioFormat(i.sampleRate, i.bitsPerSample, i.channelCount, true, false);
			Clip clip = AudioSystem.getClip();
			
			int bytesPerSample = i.bitsPerSample / 8;
			int bytesPerChanneledSample = bytesPerSample * i.channelCount;
			byte[] bytes = new byte[i.sampleCount * bytesPerChanneledSample];
			byte[][] dBytes = new byte[i.channelCount][];
			for (int j = 0; j < i.channelCount; j++) {
				dBytes[j] = getChannelData(j);
			}

			for (int j = 0; j < i.sampleCount; j++) {
				for (int l = 0; l < i.channelCount; l++) {
					for (int k = 0; k < bytesPerSample; k++) {
						bytes[j * bytesPerChanneledSample + l * bytesPerSample + k] = dBytes[l][j * bytesPerSample + k];
					}
				}
			}
			
			AudioInputStream stream = new AudioInputStream(new ByteArrayInputStream(bytes), format, i.sampleCount);
			clip.open(stream);
			clip.setLoopPoints(i.loopStart, i.loopEnd);
			return new AudioClip(i, clip);
		} catch (LineUnavailableException | IOException ex) {
			Logger.getLogger(AbstractAudioFile.class.getName()).log(Level.SEVERE, null, ex);
			return null;
		} catch (IllegalArgumentException ex){
			System.err.println("WARN: No audio device detected or the audio device does not support 16-bit unsigned PCM.");
			return null;
		}
	}
}

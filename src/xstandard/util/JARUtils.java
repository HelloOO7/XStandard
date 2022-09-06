package xstandard.util;

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public class JARUtils {

	public static String getBuildDate() {
		try {
			URL res = JARUtils.class.getResource(JARUtils.class.getSimpleName() + ".class");
			URLConnection conn = res.openConnection();
			if (conn instanceof JarURLConnection) {
				Manifest mf = ((JarURLConnection) conn).getManifest();
				Attributes atts = mf.getMainAttributes();
				String buildDate = atts.getValue("Build-Date");
				return buildDate;
			}
		} catch (IOException ex) {
		}
		return null;
	}
}

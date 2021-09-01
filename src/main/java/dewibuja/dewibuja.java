package dewibuja;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.Arrays;

public class dewibuja {
	public static final String dir = System.getProperty("java.io.tmpdir") + "/" + run.class.getPackage().getName();

	public static void loadBundledJar(String jarResource) throws Throwable {
		load(readAllBytes(String.class.getResourceAsStream(jarResource)));
	}

	public static void load(byte[] content) throws Throwable {
		File jar = new File(dir, Arrays.hashCode(content) + ".jar");

		if (!jar.exists()) {
			jar.getParentFile().mkdirs();
			Files.write(jar.toPath(), content);
		}

		dewibuja.load(jar.toURL(), (URLClassLoader) ClassLoader.getSystemClassLoader());
	}

	public static void load(URL url, URLClassLoader cl) throws Throwable {
		Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
		method.setAccessible(true);
		method.invoke(cl, url);
	}

	public static byte[] readAllBytes(InputStream in) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		for (int i = in.read(); i != -1; i = in.read()) {
			bos.write(i);
		}

		in.close();
		return bos.toByteArray();
	}

}

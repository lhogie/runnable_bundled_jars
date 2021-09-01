package dewibuja;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

public class run {

	public static void main(String[] args) throws Throwable {
		JarFile bigJar = getBigJar();

		Properties p = loadAllBundledJars(bigJar);
		Class mainClass = Class.forName(p.getProperty("main-class").trim());
		Method mainMethod = mainClass.getDeclaredMethod("main", new String[0].getClass());
		mainMethod.invoke(null, new Object[] { args });
	}

	private static JarFile getBigJar() throws IOException {
		String cp = System.getProperty("java.class.path");

		if (cp.indexOf(File.pathSeparator) >= 0)
			throw new IllegalStateException("classpath must contains one single entry");

		return new JarFile(cp);
	}

	public static Properties loadAllBundledJars(JarFile bigJar) throws Throwable {
		Properties p = new Properties();

		for (JarEntry e : bigJar.stream().collect(Collectors.toList())) {
			if (e.getName().endsWith(".properties")) {
				p.load(bigJar.getInputStream(e));
			} else if (e.getName().endsWith(".jar")) {
				dewibuja.load(dewibuja.readAllBytes(bigJar.getInputStream(e)));
			}
		}

		return p;
	}

}

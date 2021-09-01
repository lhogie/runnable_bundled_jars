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
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

public class run
{
	public static final String dir = System.getProperty("java.io.tmpdir") + "/"
			+ run.class.getPackage().getName();

	public static void main(String[] args) throws Throwable
	{
		JarFile bigJar = getBigJar();

		Properties p = loadAllBundledJars(bigJar,
				(URLClassLoader) ClassLoader.getSystemClassLoader());
		Class mainClass = Class.forName(p.getProperty("main-class").trim());
		Method mainMethod = mainClass.getDeclaredMethod("main", new String[0].getClass());
		mainMethod.invoke(null, new Object[] { args });
	}

	private static JarFile getBigJar() throws IOException
	{
		String cp = System.getProperty("java.class.path");

		if (cp.indexOf(File.pathSeparator) >= 0)
			throw new IllegalStateException("classpath must contains one single entry");

		return new JarFile(cp);
	}

	public static Properties loadAllBundledJars(JarFile bigJar, URLClassLoader to)
			throws Throwable
	{
		Properties p = new Properties();

		for (JarEntry e : bigJar.stream().collect(Collectors.toList()))
		{
			if (e.getName().endsWith(".properties"))
			{
				p.load(bigJar.getInputStream(e));
			}
			else if (e.getName().endsWith(".jar"))
			{
				byte[] content = readAllBytes(bigJar.getInputStream(e));
				File jar = new File(dir, Arrays.hashCode(content) + ".jar");

				if ( ! jar.exists())
				{
					jar.getParentFile().mkdirs();
					Files.write(jar.toPath(), content);
				}

				load(jar.toURL(), to);
			}
		}

		return p;
	}

	private static void load(URL url, URLClassLoader cl) throws Throwable
	{
		Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
		method.setAccessible(true);
		method.invoke(cl, url);
	}

	public static byte[] readAllBytes(InputStream in) throws IOException
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		for (int i = in.read(); i != - 1; i = in.read())
		{
			bos.write(i);
		}

		in.close();
		return bos.toByteArray();
	}

}

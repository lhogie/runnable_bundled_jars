package rbj;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.Scanner;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

public class exec
{
	public static void main(String[] args) throws Throwable
	{
		JarFile bigJar = getBigJar();

		String mainClassName = load(bigJar,
				(URLClassLoader) ClassLoader.getSystemClassLoader());

		Class mainClass = Class.forName(mainClassName);
		Method mainMethod = mainClass.getDeclaredMethod("main", new String[0].getClass());
		mainMethod.invoke(null, new Object[] { args });
	}

	private static String load(JarFile bigJar, URLClassLoader to) throws Throwable
	{
		String mainClass = null;

		for (JarEntry e : bigJar.stream().collect(Collectors.toList()))
		{
			if (e.getName().equals("main_class.txt"))
			{
				mainClass = new Scanner(bigJar.getInputStream(e)).nextLine();
			}
			else if (e.getName().endsWith(".jar"))
			{
				File f = new File(System.getProperty("java.io.tmpdir"), e.getName());
				Files.copy(bigJar.getInputStream(e), f.toPath(), REPLACE_EXISTING);
				load(f.toURL(), to);
			}
		}

		return mainClass;
	}

	private static JarFile getBigJar() throws IOException
	{
		String cp = System.getProperty("java.class.path");

		if (cp.indexOf(File.pathSeparator) >= 0)
			throw new IllegalStateException("classpath must contains one single entry");

		return new JarFile(cp);
	}

	private static void load(URL url, URLClassLoader cl)
			throws NoSuchMethodException, SecurityException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException
	{
		Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
		method.setAccessible(true);
		method.invoke(cl, url);
	}
}

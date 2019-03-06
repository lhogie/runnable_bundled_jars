package rbj;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;

public class assemble
{
	public static void main(String... args) throws Throwable
	{
		if (args.length < 3)
		{
			System.err.println("Usage: java " + assemble.class.getName()
					+ " BUNDLED_JAR MAIN_CLASS JAR1 JAR2 ...");
			System.exit(1);
		}

		File bigJar = new File(args[0]);
		String classname = args[1];
		List<File> jars = jars = Arrays.asList(args).subList(2, args.length).stream()
				.map(e -> new File(e)).collect(Collectors.toList());

		assemble(bigJar, classname, jars);
	}

	public static void assemble(File bigJar, String classname, List<File> jars)
			throws Throwable
	{
		Manifest manifest = new Manifest();
		manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
		manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS,
				exec.class.getName());

		JarOutputStream jos = new JarOutputStream(new FileOutputStream(bigJar), manifest);

		for (File jar : jars)
		{
			jos.putNextEntry(new ZipEntry(jar.getName()));
			jos.write(Files.readAllBytes(jar.toPath()));
			jos.closeEntry();
		}

		{
			String classFileName = exec.class.getName().replace('.', '/') + ".class";
			jos.putNextEntry(new ZipEntry(classFileName));
			jos.write(Files.readAllBytes(
					Paths.get(exec.class.getResource("/" + classFileName).toURI())));
			jos.closeEntry();
		}

		{
			jos.putNextEntry(new ZipEntry("main_class.txt"));
			jos.write(classname.getBytes());
			jos.closeEntry();
		}

		jos.close();
	}
}

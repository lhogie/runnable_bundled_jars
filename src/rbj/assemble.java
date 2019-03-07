package rbj;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
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

		assemble(new File(args[0]), args[1], Arrays.asList(args).subList(2, args.length)
				.stream().map(e -> new File(e)).collect(Collectors.toList()));
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

		{ // adds the RBJ run class into the JAR
			String classFileName = exec.class.getName().replace('.', '/') + ".class";
			jos.putNextEntry(new ZipEntry(classFileName));
			URL res = exec.class.getResource("/" + classFileName);
			jos.write(Files.readAllBytes(Paths.get(res.toURI())));
			jos.closeEntry();
		}

		{ // tells which is the main class RBJ must execute
			jos.putNextEntry(new ZipEntry("main_class.txt"));
			jos.write(classname.getBytes());
			jos.closeEntry();
		}

		jos.close();
	}
}

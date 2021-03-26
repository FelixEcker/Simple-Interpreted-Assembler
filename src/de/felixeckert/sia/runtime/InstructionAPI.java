package de.felixeckert.sia.runtime;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class InstructionAPI {
	public static final ArrayList<IProcessorInstruction> INSTRUCTIONS = new ArrayList<>();
	
	public static void registerInstruction(String name, int code, IProcessorInstruction impl) {
		de.felixeckert.sia.compiler.Compiler.API_OPS.put(name, code);
		INSTRUCTIONS.add(impl);
	}
	
	public static void checkAndExecute(byte instruction) {
		Iterator<IProcessorInstruction> i = INSTRUCTIONS.iterator();
		
		boolean found = false;
		while (i.hasNext()) {
			IProcessorInstruction inst = (IProcessorInstruction) i.next();
			if (inst.getCode() == (int) instruction) {
				inst.execute();
				found = true;
				break;
			}
		}
		
		if (!found) Processor.PCOUNTER++;
	}
	
	public static void loadAPIImpl(String path, String mainClass) {
		try {
			// Create a JarFile
	        JarFile jarFile = new JarFile(path);
	
	        // Get the entries
	        Enumeration<JarEntry> e = jarFile.entries();
	
	        // Create a URL for the jar
	        URL[] urls = { new URL("jar:file:" + new File(path).getAbsolutePath() +"!/") };
	        URLClassLoader cl = URLClassLoader.newInstance(urls);
	
	        while (e.hasMoreElements())
	        {
	            JarEntry je = (JarEntry) e.nextElement();
	
	            // Skip directories
	            if(je.isDirectory() || !je.getName().endsWith(".class"))
	            {
	                continue;
	            }
	
	            // -6 because of .class
	            String className = je.getName().substring(0,je.getName().length()-6);
	            className = className.replace('/', '.');

	            // Load the class
	            if (className.matches(mainClass)) {
	            	@SuppressWarnings("unchecked")
					Class<APIImpl> clazz = (Class<APIImpl>) cl.loadClass(className);
	            	clazz.getDeclaredMethod("init", null).invoke(clazz.newInstance(), null);
	            } else {
		            cl.loadClass(className);
	            }
	        }
	        
	        jarFile.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void registerAPIImpls() {
		//TODO: Add LOADING!!!!!!!!111!11111111111!!!!!
		String raw;
		try {
			raw = new String(Files.readAllBytes(Paths.get("siaApis.lst")), StandardCharsets.US_ASCII);
			String[] rawList = raw.split(":end");
			
			for (int i = 0; i < rawList.length; i++) {
				String[] split = rawList[i].replace(" ", "").replace("\n", "").replace("\r", "").split(";");

				loadAPIImpl(System.getProperty("user.dir")+"/"+split[0].split("=")[1], split[1].split("=")[1]);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

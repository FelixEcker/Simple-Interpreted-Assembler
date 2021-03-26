package de.felixeckert.sia;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import de.felixeckert.sia.compiler.Compiler;
import de.felixeckert.sia.runtime.InstructionAPI;
import de.felixeckert.sia.runtime.Processor;

public class Main {
	public static final String version = "v (-1)";
	
	public static void main(String[] args) throws IOException {
		File f = new File(System.getProperty("user.dir"));
		File[] fs = f.listFiles();
		
		// Load APIs from file if the list exists
		for (File f1 : fs) {
			if (f1.isFile()) {
				if (f1.getName().matches("siaApis.lst")) {
					InstructionAPI.registerAPIImpls();
				}
			}
		}
		
		for (int i = 0; i < args.length; i++) {
			String s = args[i];
			switch (s) {
			case "-linst":
				Processor.LINST = true;
				continue;
			case "-c":
				Compiler.compile(new String(Files.readAllBytes(Paths.get(args[i+1])), StandardCharsets.US_ASCII), args[i+2]);
				System.exit(0);
			case "-h":
				printInfo();
				System.exit(0);
			case "-cr":
				Compiler.compile(new String(Files.readAllBytes(Paths.get(args[i+1])), StandardCharsets.US_ASCII), "temp.bin");
				
				System.out.println("==========================================");
				
				Processor.init();
				Processor.PROGRAM = Files.readAllBytes(Paths.get("temp.bin"));
				Processor.execute();
				(new File("temp.bin")).delete();	
				System.exit(Processor.RETURN_CODE);
			case "-t":
				System.out.println("NO TEST IMPLEMENTED");
				printInfo();
				System.exit(0);
			default:
				Processor.init();
				Processor.PROGRAM = Files.readAllBytes(Paths.get(args[0]));
				Processor.execute();
				System.exit(Processor.RETURN_CODE);
			}
		}
	}

	private static void printInfo() {
		System.out.println("_____/\\\\\\\\\\\\\\\\\\\\\\_____/\\\\\\\\\\\\\\\\\\\\\\______/\\\\\\\\\\\\\\\\\\____        \r\n" + 
				" ___/\\\\\\/////////\\\\\\__\\/////\\\\\\///_____/\\\\\\\\\\\\\\\\\\\\\\\\\\__       \r\n" + 
				"  __\\//\\\\\\______\\///_______\\/\\\\\\_______/\\\\\\/////////\\\\\\_      \r\n" + 
				"   ___\\////\\\\\\______________\\/\\\\\\______\\/\\\\\\_______\\/\\\\\\_     \r\n" + 
				"    ______\\////\\\\\\___________\\/\\\\\\______\\/\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\_    \r\n" + 
				"     _________\\////\\\\\\________\\/\\\\\\______\\/\\\\\\/////////\\\\\\_   \r\n" + 
				"      __/\\\\\\______\\//\\\\\\_______\\/\\\\\\______\\/\\\\\\_______\\/\\\\\\_  \r\n" + 
				"       _\\///\\\\\\\\\\\\\\\\\\\\\\/_____/\\\\\\\\\\\\\\\\\\\\\\__\\/\\\\\\_______\\/\\\\\\_ \r\n" + 
				"        ___\\///////////______\\///////////___\\///________\\///__");
		System.out.println("        (Simple Interpreted Assembler) "+version);
		System.out.println("--------------+------------------------------------------------");
		System.out.println("default: <in> | Runs a file from bytecode");
		System.out.println("-c <in> <out> | Compiles a source file into byte code");
		System.out.println("-cr <in>      | Compiles and instantly runs a file");
		System.out.println("-t            | Runs a test (if implemented)");
	}
}

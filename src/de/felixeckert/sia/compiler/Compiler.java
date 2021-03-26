package de.felixeckert.sia.compiler;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import de.felixeckert.sia.util.InfiniteList;

public class Compiler {
	public static HashMap<String, Integer> OPS = new HashMap<>();
	public static HashMap<String, Integer> API_OPS = new HashMap<>();
	
	public static ArrayList<ICompilerPlugin> PLUGINS = new ArrayList<>();
	
	private static String inst = "";
	
	public static void init() {
		if (OPS.isEmpty()) {
			OPS.put("NOP", 255);
			OPS.put("<", 254);
			OPS.put(">", 253);	
			OPS.put("%", 252);
			OPS.put("=", 251);
			OPS.put("RETC", 250);
			OPS.put("LBL", 0);
			OPS.put("SET", 1);
			OPS.put("REM", 2);
			OPS.put("CPY", 3);
			OPS.put("MOV", 4);
			OPS.put("JMP", 5);
			OPS.put("JE", 6);
			OPS.put("JNE", 7);
			OPS.put("CMP", 8);
			OPS.put("ADD", 9);
			OPS.put("SUB", 10);
			OPS.put("DIV", 11);
			OPS.put("MUL", 12);
			OPS.put("CPY_REG", 13);
			OPS.put("INT_NEG", 14);
			OPS.put("CON_APP", 15);
		}
	}
	
	private static String format(String in) {
		return in.replace("\n", " ").replace("\r", "");
	}
	
	public static HashMap<String, Integer> createLabelMap(String[] source) {
		HashMap<String, Integer> map = new HashMap<>();
		
		for (int i = 0; i < source.length; i++) {
			String s = source[i];
			
			if (s.startsWith(":")) {
				s = s.replace(":", "");
				
				// Set instruction to LABEL instruction
				source[i] = "LBL";
				
				// Store a reference to the labels program address for replacement of label references
				map.put(s, i);
			}
		}
		
		System.out.printf("Found and replaced %s labels...\n", map.size());
		
		return map;
	}
	
	public static void compile(String source, String output) {
		long startTime = System.currentTimeMillis();
		init();
		
		source = format(source);
		
		// Split source into words and compile from there on
		String[] raw = source.split(" ");
		
		PLUGINS.forEach((v) -> {
			if (v.stage() == 0) {
				v.run(raw);
			}
		});
		
		InfiniteList<Integer> cmp = new InfiniteList<>();
		HashMap<String, Integer> labels = createLabelMap(raw);
		
		PLUGINS.forEach((v) -> {
			if (v.stage() == 1) {
				v.run(raw);
			}
		});
		
		for (int i = 0; i < raw.length; i++) {
			inst = raw[i];
			
			PLUGINS.forEach((v) -> {
				if (v.stage() == 2) {
					v.run(inst);
				}
			});
			
			//System.out.println(inst);
			
			if ((OPS.containsKey(inst) || API_OPS.containsKey(inst) )&& !inst.startsWith(":")) {
				if (OPS.containsKey(inst)) {
					cmp.set(i, OPS.get(inst));
				} else {
					cmp.set(i, API_OPS.get(inst));
				}
			} else {
				
				// Check if memory address
				if (inst.startsWith("$")) {
					inst = inst.replace("$", "");
					
					try {
						cmp.set(i, Integer.parseInt(inst, 16));
						
					} catch (Exception e) {
						System.err.printf("COMPILATION ERROR: INVALID HEX NUMBER \"%s\"\n", inst);
					}
				} else if (inst.startsWith("_")) {
					try {
						if (labels.get(inst.replace("_", "")) == null) throw new NullPointerException();
						cmp.set(i, labels.get(inst.replace("_", "")));
						
					} catch (NullPointerException e) {
						System.err.printf("COMPILATION ERROR: LABEL \"%s\" NOT DEFINED\n", inst);
					}
				} else if (inst.startsWith("CHR_")) {
					String s = inst.split("CHR_")[1];
					
					switch (s) {
					case "!_":
						cmp.set(i, 32);
						break;
					case "NL":
						cmp.set(i, 0x0a);
						break;
					default:
						cmp.set(i, (int) s.toCharArray()[0]);
						break;
					}
				} else if (!inst.matches("")) {
					try {
						cmp.set(i, Integer.parseInt(inst));
					} catch (Exception e) {
						System.err.printf("COMPILATION ERROR: INVALID NUMBER FORMAT \"%s\"\n", inst);
					}
				} else {
					cmp.set(i, OPS.get("NOP"));
				}
			}
		}
		
		System.out.printf("Compilation finished in %s ms for %s instructions\n", System.currentTimeMillis()-startTime, cmp.size());
	
		write(output, cmp.toArray());
	}
	
	private static void write(String output, Object[] array) {
		byte[] bin = new byte[array.length];
		int[] temp = new int[array.length];
		for (int i = 0; i < array.length; i++) {
			if (array[i] == null) {
				temp[i] = OPS.get("NOP");
				continue;
			}
			temp[i] = (int) array[i];
		}
		for (int i = 0; i < array.length; i++) {
			bin[i] = (byte) temp[i];
		}
		
		try {
			OutputStream os = new FileOutputStream(output);
			os.write(bin);	
			os.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

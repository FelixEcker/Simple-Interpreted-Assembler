package de.felixeckert.sia.runtime;

import java.io.IOException;
import java.util.Properties;

import de.felixeckert.sia.util.ByteUtil;
import de.felixeckert.sia.util.InfiniteList;

public class Processor {
	public static byte[] PROGRAM;
	public static InfiniteList<Integer> MEMORY = new InfiniteList<>();

	private static Properties settings = new Properties();
	
	private static boolean runWithSleep = false;
	private static long sleepTime = 250;
	public static boolean LINST = false;
	
	public static int RETURN_CODE  = 0;
	
	public static int MEM_LOG_ADDR = 0x5;
	public static int SHIFT_255    = 1;
	
	public static int PCOUNTER  = 0;
	public static int COMPARE   = 0;
	public static int MEM_SPACE = 0;

	public static void init() {
		try {
			settings.load(Processor.class.getResourceAsStream("/settings.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		sleepTime = Long.parseLong((String) settings.get("sleepTime"));
		runWithSleep = Boolean.valueOf((String) settings.get("runWithSleep"));
	}
	
	public static void execute() {
		if (runWithSleep) {
			executeWSleep();
			return;
		}
		
		while (PCOUNTER < PROGRAM.length) {
			nextInstruction();
		}
		
		// Print out Memory and Compare Register
		System.out.println("Program finished at instruction: "+PCOUNTER+" with code "+RETURN_CODE);
		System.out.println("COMPARE: "+COMPARE);
		System.out.println("MEMORY: ");
		for (int i = 0; i < MEMORY.size(); i++) {
			if (MEMORY.get(i) != null) {
				System.out.println("$"+Integer.toHexString(i)+": "+MEMORY.get(i));	
			} else {
				System.out.println("$"+Integer.toHexString(i)+": null");	
			}
		}
	}
	
	public static void executeWSleep() {
		while (PCOUNTER < PROGRAM.length) {
			nextInstruction();
			
			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		// Print out Memory and Compare Register
		System.out.println("COMPARE: "+COMPARE);
		System.out.println("MEMORY: ");
		for (int i = 0; i < MEMORY.size(); i++) {
			if (MEMORY.get(i) != null) {
				System.out.println("$"+Integer.toHexString(i)+": "+MEMORY.get(i));	
			} else {
				System.out.println("$"+Integer.toHexString(i)+": null");	
			}
		}
	}
	
	public static void nextInstruction() {
		if (PCOUNTER >= PROGRAM.length) {
			return;
		}
		
		byte inst = PROGRAM[PCOUNTER];
		
		if (LINST) System.out.printf("INST $%s CODE %s;\n", PCOUNTER, inst);
		
		switch (inst) {
		case (byte) 0xff:
			PCOUNTER++;
			break;
		case 0x0:
			PCOUNTER++;
			break;
		case 0x1:
			setByte();
			break;
		case 0x2:
			remByte();
			break;
		case 0x3:
			copyByte();
			break;
		case 0x4:
			moveByte();
			break;
		case 0x5:
			jumpInst();
			break;
		case 0x6:
			jumpEqual();
			break;
		case 0x7:
			jumpNotEqual();
			break;
		case 0x8:
			compare();
			break;
		case 0x9:
			add();
			break;
		case 0xa:
			sub();
			break;
		case 0xb:
			div();
			break;
		case 0xc:
			mul();
			break;
		case 0xd:
			copyReg();
			break;
		case 0xe:
			SHIFT_255 = SHIFT_255 == 1 ? 0 : 1;
			PCOUNTER++;
			break;
		case 0xf:
			consoleAppend();
			break;
		case (byte) 250:
			RETURN_CODE = PROGRAM[PCOUNTER+1];
			PCOUNTER += 2;
			break;
		default:
			InstructionAPI.checkAndExecute(inst);
			break;
		}
	}

	private static void setByte() {
		//System.out.println(PROGRAM[PCOUNTER]);
		MEMORY.set(ByteUtil.shiftTo255(PROGRAM[PCOUNTER+1]), SHIFT_255 == 1 ? ByteUtil.shiftTo255(PROGRAM[PCOUNTER+2]) : (int) PROGRAM[PCOUNTER+2]);
		if (PROGRAM[PCOUNTER+1] == MEM_LOG_ADDR) System.out.println("SET: "+PROGRAM[PCOUNTER+2]);
		PCOUNTER += 3;
	}
	
	private static void remByte() {
		MEMORY.set(ByteUtil.shiftTo255(PROGRAM[PCOUNTER+1]), null);
		PCOUNTER += 2;
	}
	
	private static void copyByte() {
		MEMORY.set(ByteUtil.shiftTo255(PROGRAM[PCOUNTER+2]), MEMORY.get(ByteUtil.shiftTo255(PROGRAM[PCOUNTER+1])));
		if (PROGRAM[PCOUNTER+2] == MEM_LOG_ADDR) System.out.println("INST: "+PCOUNTER+" CPY: "+MEMORY.get(PROGRAM[PCOUNTER+1]));
		PCOUNTER += 3;
	}
	
	private static void moveByte() {
		MEMORY.set(ByteUtil.shiftTo255(PROGRAM[PCOUNTER+1]), MEMORY.get(ByteUtil.shiftTo255(PROGRAM[PCOUNTER+1])));
		
		if (PROGRAM[PCOUNTER+1] == MEM_LOG_ADDR) System.out.println("MOV: "+PROGRAM[PCOUNTER+2]);
		
		MEMORY.set(ByteUtil.shiftTo255(PROGRAM[PCOUNTER+1]), null);
		PCOUNTER += 3;
	}
	
	private static void jumpInst() {
		PCOUNTER = ByteUtil.shiftTo255(PROGRAM[PCOUNTER+1]);
		
		PCOUNTER += 2;
	}
	
	private static void jumpEqual() {
		if (COMPARE == 1) {
			PCOUNTER = ByteUtil.shiftTo255(PROGRAM[PCOUNTER+1]);
		} else {
			PCOUNTER += 2;
		}
	}
	
	private static void jumpNotEqual() {
		if (COMPARE != 1) {
			PCOUNTER = ByteUtil.shiftTo255(PROGRAM[PCOUNTER+1]);
		} else {
			PCOUNTER += 2;
		}
	}
	
	private static void compare() {
		int compareType = PROGRAM[PCOUNTER+1];
		int v1 = MEMORY.get(ByteUtil.shiftTo255(PROGRAM[PCOUNTER+2]));
		int v2 = MEMORY.get(ByteUtil.shiftTo255(PROGRAM[PCOUNTER+3]));
		
		switch (compareType) {
		case -5:
			COMPARE = v1 == v2 ? 1 : 0;
			break;
		case -2:
			COMPARE = v1 > v2 ? 1 : 0;
			break;
		case -3:
			COMPARE = v1 < v2 ? 1 : 0;
			break;
		case -4:
			if (v1 == 0 || v2 == 0) { COMPARE = 0; break; }
			COMPARE = v1 % v2  == 0 ? 1 : 0;
			break;
		}
		
		PCOUNTER += 4;
	}
	
	private static void add() {
		int v1 = MEMORY.get(ByteUtil.shiftTo255(PROGRAM[PCOUNTER+1]));
		int v2 = MEMORY.get(ByteUtil.shiftTo255(PROGRAM[PCOUNTER+2]));
		
		MEMORY.set(ByteUtil.shiftTo255(PROGRAM[PCOUNTER+3]), v1+v2);
		PCOUNTER += 4;
	}
	
	private static void sub() {
		int v1 = MEMORY.get(ByteUtil.shiftTo255(PROGRAM[PCOUNTER+1]));
		int v2 = MEMORY.get(ByteUtil.shiftTo255(PROGRAM[PCOUNTER+2]));
		
		MEMORY.set(ByteUtil.shiftTo255(PROGRAM[PCOUNTER+3]), v1-v2);
		PCOUNTER += 4;
	}
	
	private static void mul() {
		int v1 = MEMORY.get(ByteUtil.shiftTo255(PROGRAM[PCOUNTER+1]));
		int v2 = MEMORY.get(ByteUtil.shiftTo255(PROGRAM[PCOUNTER+2]));;
		
		MEMORY.set(ByteUtil.shiftTo255(PROGRAM[PCOUNTER+3]), v1*v2);
		PCOUNTER += 4;
	}
	
	private static void div() {
		int v1 = MEMORY.get(ByteUtil.shiftTo255(PROGRAM[PCOUNTER+1]));
		int v2 = MEMORY.get(ByteUtil.shiftTo255(PROGRAM[PCOUNTER+2]));
		
		if (v1 == 0 || v2 == 0) {
			System.err.println("DIVIDE BY ZERO ERROR ON INSTRUCTION "+PCOUNTER);
			System.exit(-1);
		}
		
		MEMORY.set(ByteUtil.shiftTo255(PROGRAM[PCOUNTER+3]), v1/v2);
		PCOUNTER += 4;
	}
	
	private static void copyReg() {
		// 0 = COMPARE
		// 1 = PCOUNTER
		
		int reg = MEMORY.get(ByteUtil.shiftTo255(PROGRAM[PCOUNTER+1]));;
		int dest = MEMORY.get(ByteUtil.shiftTo255(PROGRAM[PCOUNTER+2]));;
		
		switch (reg) {
		case 0:
			MEMORY.set(dest, COMPARE);
			if (dest == MEM_LOG_ADDR) System.out.println("CMP: "+COMPARE);
			break;
		case 1:
			MEMORY.set(dest, PCOUNTER);
			if (dest == MEM_LOG_ADDR) System.out.println("PCOUNTER: "+PCOUNTER);
			break;
		}
		
		PCOUNTER += 3;
	}
	
	private static void consoleAppend() {
		int adress = PROGRAM[PCOUNTER+1];
		int asChar = PROGRAM[PCOUNTER+2];
		
		if (adress == 1) {
			if (asChar == 1) {
				System.out.print((char)(int)MEMORY.get(ByteUtil.shiftTo255(PROGRAM[PCOUNTER+3])));
			} else {
				System.out.print(MEMORY.get(ByteUtil.shiftTo255(PROGRAM[PCOUNTER+3])));
			}
		} else {		
			if (asChar == 1) {
				System.out.print((char)ByteUtil.shiftTo255(PROGRAM[PCOUNTER+3]));
			} else if (asChar == 0) {
				System.out.print(ByteUtil.shiftTo255(PROGRAM[PCOUNTER+3]));
			}
		}
		
		PCOUNTER += 4;
	}
}

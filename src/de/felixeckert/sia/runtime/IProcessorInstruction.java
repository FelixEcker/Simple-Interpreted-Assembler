package de.felixeckert.sia.runtime;

public interface IProcessorInstruction {
	void execute();
	int getCode();
}

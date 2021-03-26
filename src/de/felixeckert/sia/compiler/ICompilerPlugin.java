package de.felixeckert.sia.compiler;

public interface ICompilerPlugin {
	int stage(); // In which stage of compilation it should be run
	int run(Object o);
}

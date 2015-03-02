package com.github.nill14.parsers.dependency.impl;

import java.io.PrintStream;

import org.slf4j.Logger;

interface StringConsumer {
	void process(String arg);
}

class InfoLogConsumer implements StringConsumer {

	private final Logger log;
	
	public InfoLogConsumer(Logger log) {
		this.log = log;
	}
	
	@Override
	public void process(String arg) {
		log.info(arg);
	}
}

class DebugLogConsumer implements StringConsumer {

	private final Logger log;
	
	public DebugLogConsumer(Logger log) {
		this.log = log;
	}
	
	@Override
	public void process(String arg) {
		log.debug(arg);
	}
}

class PrintStreamConsumer implements StringConsumer {

	private final PrintStream stream;
	
	public PrintStreamConsumer(PrintStream stream) {
		this.stream = stream;
	}
	
	@Override
	public void process(String arg) {
		stream.println(arg);
	}
}
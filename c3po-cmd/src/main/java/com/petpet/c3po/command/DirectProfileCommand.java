package com.petpet.c3po.command;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.petpet.c3po.parameters.DirectProfileParams;
import com.petpet.c3po.parameters.Params;
import com.ifs.directprofiler.core.Controller;

public class DirectProfileCommand implements Command {

	private static final Logger LOG = LoggerFactory
			.getLogger(DirectProfileCommand.class);
	private DirectProfileParams params;
	private long time = -1L;

	@Override
	public void setParams(Params params) {
		if (params != null && params instanceof DirectProfileParams) {
			this.params = (DirectProfileParams) params;
		}

	}

	@Override
	public void execute() {
		LOG.info("Starting direct profiling command.");
		long start = System.currentTimeMillis();
		Controller ctrl = new Controller();
		try {
			ctrl.Execute(this.params.getInputLocation(),
					this.params.getOutnputLocation());
			System.out.println("I finished profiling all the data.");
		} catch (IOException e) {
			LOG.error(e.getMessage());
			System.err.println("An error occurred: " + e.getMessage());
			return;
		}

		long end = System.currentTimeMillis();
		this.setTime(end - start);

	}

	@Override
	public long getTime() {
		return this.time;
	}

	protected void setTime(long time) {
		this.time = time;
	}

}

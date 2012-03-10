package replicatorg.drivers.commands;

import static replicatorg.util.Preconditions.*;

import replicatorg.drivers.Driver;
import replicatorg.drivers.RetryException;

public class OpenValve implements DriverCommand {
	private final int toolIndex;
	
	public OpenValve(int toolIndex) {
		this.toolIndex = checkNonNegative(toolIndex,
				"Tool indices should not be negative");
	}
	
	@Deprecated public OpenValve() {
		this.toolIndex = -1;  // Magic number to infer current tool
	}
	
	@Override
	public void run(Driver driver) throws RetryException {
		driver.openValve(toolIndex);
	}
}

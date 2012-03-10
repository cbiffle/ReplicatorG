package replicatorg.drivers.commands;

import static replicatorg.util.Preconditions.checkNonNegative;
import replicatorg.drivers.Driver;
import replicatorg.drivers.RetryException;

public class CloseValve implements DriverCommand {
	private final int toolIndex;
	
	public CloseValve(int toolIndex) {
		this.toolIndex = checkNonNegative(toolIndex,
				"Tool indices should not be negative");
	}
	
	@Deprecated public CloseValve() {
		this.toolIndex = -1;  // Magic number to infer current tool
	}

	@Override
	public void run(Driver driver) throws RetryException {
		driver.closeValve(toolIndex);
	}
}

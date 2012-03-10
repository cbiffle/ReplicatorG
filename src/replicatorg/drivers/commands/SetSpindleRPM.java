package replicatorg.drivers.commands;

import static replicatorg.util.Preconditions.*;

import replicatorg.drivers.Driver;
import replicatorg.drivers.RetryException;

public class SetSpindleRPM implements DriverCommand {
	private final int toolIndex;
	private final double rpm;
	
	public SetSpindleRPM(double rpm, int toolIndex) {
		this.toolIndex = checkNonNegative(toolIndex,
				"Tool indices should not be negative");
		this.rpm = rpm;
	}
	
	@Deprecated public SetSpindleRPM(double rpm) {
		this.toolIndex = -1;  // Magic number to infer current tool index
		this.rpm = rpm;
	}
	
	@Override
	public void run(Driver driver) throws RetryException {
		driver.setSpindleRPM(rpm, toolIndex);
	}
}
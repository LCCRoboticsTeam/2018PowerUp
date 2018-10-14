package org.usfirst.frc.team5514.robot;

import edu.wpi.first.wpilibj.SerialPort;
import edu.wpi.first.wpilibj.command.Command;

public class StartMotor extends Command {

	SerialPort sm_port;
	boolean done;
	
	public StartMotor(SerialPort port) {
		sm_port = port;
		System.out.println("StartMotor Initalized....");
		done = false;
	}
	
	@Override
	protected void initialize()
	{
		
		byte[] sensorData = new byte[64];
		
		byte[] startMotor = new byte[] {'M', 'S', '0', '1', '\n'};	
		sm_port.write(startMotor, startMotor.length);
		
		if (sm_port.getBytesReceived() >= 9)
		{
			sensorData = sm_port.read(9);
			for (int x = 0; x < 9; x++)
			{
				System.out.printf("%x ", sensorData[x]);
			}
			System.out.println("");
			System.out.flush();
			done = true;
		}
	}

	@Override
	protected void execute()
	{
		System.out.println("StartMotor Execute.");
	}
	
	@Override
	protected boolean isFinished() {
		return done;
	}

}

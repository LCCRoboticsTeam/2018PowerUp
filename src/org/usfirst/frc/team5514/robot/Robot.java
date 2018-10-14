/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/
	
package org.usfirst.frc.team5514.robot;


import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;

import edu.wpi.first.wpilibj.ADXRS450_Gyro;
import edu.wpi.first.wpilibj.GenericHID;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.PIDController;
import edu.wpi.first.wpilibj.PIDOutput;
import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.SPI.Port;
import edu.wpi.first.wpilibj.SerialPort;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.drive.MecanumDrive;
import edu.wpi.first.wpilibj.livewindow.LiveWindow;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
	
	

/**	
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the build.properties file in the
 * project.
 */	
public class Robot extends IterativeRobot {
	
	MecanumDrive myDrive;
	
	double rotateToAngleRate;
	XboxController mecstick = new XboxController(0);
	Joystick launchpad = new Joystick(1);
	
	Timer autoTimer = new Timer();
	
	final int kFrontLeftCIM = 0;
	final int kFrontLeft775 = 1;
	final int kFrontRightCIM = 2;
	final int kFrontRight775 = 3;
	final int kBackLeftCIM = 4;
	final int kBackLeft775 = 5;
	final int kBackRightCIM = 6;
	final int kBackRight775 = 7;
	final int kIntakeRight = 8;
	final int kIntakeLeft = 9;
	final int kSlide = 11;
	final int kClimbPole = 10;
	final int kWinch = 12;
	
	
	double aSpeed;
	
	private static final String kDefaultAuto = "Default";
	private static final String kCustomAuto = "My Auto";
	ADXRS450_Gyro gyro = new ADXRS450_Gyro(Port.kOnboardCS0);
	private String m_autoSelected;
	private SendableChooser<String> m_chooser = new SendableChooser<>();
	PIDOutput pid_output;
	PIDController turnController;
	
	long device;
	SerialPort port;
	boolean lidar_ready;
	
	WPI_TalonSRX slide;
	WPI_TalonSRX intakeL;
	WPI_TalonSRX intakeR;
	WPI_TalonSRX winch;
	WPI_TalonSRX climbPole;
	
	public Robot() {
		
		/*WPI_TalonSRX frontLeftMotor = new WPI_TalonSRX(kFrontLeftCIM);
		WPI_TalonSRX rearLeftMotor = new WPI_TalonSRX(kBackLeftCIM);
		WPI_TalonSRX frontRightMotor = new WPI_TalonSRX(kFrontRightCIM);
		WPI_TalonSRX rearRightMotor = new WPI_TalonSRX(kBackRightCIM);
		*/
		WPI_TalonSRX frontLeft775 = new WPI_TalonSRX(kFrontLeft775);
		WPI_TalonSRX frontRight775 = new WPI_TalonSRX(kFrontRight775);
		WPI_TalonSRX backLeft775 = new WPI_TalonSRX(kBackLeft775);
		WPI_TalonSRX backRight775 = new WPI_TalonSRX(kBackRight775);
		
		slide = new WPI_TalonSRX(kSlide);
		intakeL = new WPI_TalonSRX(kIntakeLeft);
		intakeR = new WPI_TalonSRX(kIntakeRight);
		winch = new WPI_TalonSRX(kWinch);
		climbPole = new WPI_TalonSRX(kClimbPole);
		
		//Spark PWILights = new Spark(kLights);
		//PWILights.set(.1);
		
		/*
		turnController = new PIDController(0.03, 0.0, 0.0, 0.0, gyro, pid_output);
		turnController.setInputRange(-180.0f, 180.0f);
		turnController.setOutputRange(-1.0, 1.0);
		turnController.setContinuous(true);
		*/
		
		//device = SweepJNI.construct("/dev/ttyUSB0", 115200);
		//sweepData = new List<SweepSample> ();
		
		// Set 775Pros as followers by changing TalonSRX
		myDrive = new MecanumDrive(frontLeft775, backLeft775, frontRight775, backRight775);
		//myDrive.setDeadband(0.3);
		
		/*port = new SerialPort(115200, SerialPort.Port.kUSB);
		lidar_ready = false;
		*/
		//SmartDashboard.putData("Start Motor", new StartMotor(port));
		LiveWindow.add(myDrive);
		
	}
	
	/**
	 * This function is run when the robot is first started up and should be
	 * used for any initialization code.
	 */
	@Override
	public void robotInit() {
		
		
		//slaveMotor.changeControlMode(CANTalon.ControlMode.Follower);
		
		CameraServer.getInstance().startAutomaticCapture();
		
		gyro.calibrate();
		
		m_chooser.addDefault("Default Auto", kDefaultAuto);
		m_chooser.addObject("My Auto", kCustomAuto);
		//SmartDashboard.putData("Auto choices", m_chooser);
		
		
	}
	
	/**
	 * This autonomous (along with the chooser code above) shows how to select
	 * between different autonomous modes using the dashboard. The sendable
	 * chooser code works with the Java SmartDashboard. If you prefer the
	 * LabVIEW Dashboard, remove all of the chooser code and uncomment the
	 * getString line to get the auto name from the text box below the Gyro
	 *
	 * <p>You can add additional auto modes by adding additional comparisons to
	 * the switch structure below with additional strings. If using the
	 * SendableChooser make sure to add them to the chooser code above as well.
	 */
	@Override
	public void autonomousInit() {
		m_autoSelected = m_chooser.getSelected();
		
		autoTimer.reset();
		autoTimer.start();
		
		
		
		// autoSelected = SmartDashboard.getString("Auto Selector",
		// defaultAuto);
		System.out.println("Auto selected: " + m_autoSelected);
	}
	
	/*public void setNeutralMode(NeutralMode mode) {
		
		
		
	}
	*/
	/**
	 * This function is called periodically during autonomous.
	 */
	@Override
	public void autonomousPeriodic() {
		switch (m_autoSelected) {
			case kCustomAuto:
				// Put custom auto code here
				break;
			case kDefaultAuto:
			default:
				//while (autoTimer.hasPeriodPassed(1.0) == false) { 
					//System.out.println("Hi");
					//myDrive.driveCartesian(0.5, 0.0, 0.0);
				if (autoTimer.get() < 3.0) {
					
					myDrive.driveCartesian(0.0, -0.35, 0.0);
					
				} else {
					
					myDrive.driveCartesian(0.0, 0.0, 0.0);
				
				}
				// Put default auto code here
				break;
		}
	}
	
	
	/**
	 * This function is called periodically during operator control.
	 */
	@Override
	public void teleopPeriodic() {
		
			//Rotate Values
			double rotateAxis;
			double rotateValue;
			double rotateDeadzone;
			
			//Move Values
			double moveDeadzone;
			double moveXAxis;
			double moveYAxis;
			double moveXValue;
			double moveYValue;
			
			//rotateAxis = mecstick.getTriggerAxis(GenericHID.Hand.kLeft) + (mecstick.getTriggerAxis(GenericHID.Hand.kRight)*-1);
			
			//Dpad Controls
			int POV = mecstick.getPOV();
			double POVXAxis;
			double POVYAxis;
			double POVSpeed = 0.5;
			
			//Speed Limits
			double speedLimit;
			double rotateLimit;
			
			//Launchpad Buttons
			boolean LPSafety = launchpad.getRawButton(4);
			boolean LPWinch = launchpad.getRawButton(5);
			boolean LPPoleDown = launchpad.getRawButton(6);
			boolean LPPoleUp = launchpad.getRawButton(7);
			boolean LPCUp = launchpad.getRawButton(8);
			boolean LPCDown = launchpad.getRawButton(9);
			boolean LPIntakeForward = launchpad.getRawButton(10);
			boolean LPIntakeBack = launchpad.getRawButton(11);
			
			SmartDashboard.putBoolean("IntakeForward", LPIntakeForward);
			SmartDashboard.putBoolean("IntakeBack", LPIntakeBack);
			
			double LPDial = launchpad.getRawAxis(6);
			
			//Climbing
			if (LPSafety == true) {
				
				//Telescoping Pole 
				if (LPPoleUp == true) {
					
					climbPole.set(.2);
					winch.set(-.2);
					
				} else if (LPPoleDown == true) {
					
					climbPole.set(-.2);
					
				} else {
					
					climbPole.set(0);
					winch.set(0);
					
				}
				
				//Winch
				if (LPWinch == true) {
					
					winch.set(.2);
					
				} else {
					
					winch.set(0);
					
				}
				
			}
			
			//Carriage
			if (LPCUp) {
				
				slide.set(-1);
				System.out.println("Up is working" + slide.get());
				
			} else if (LPCDown) {
				
				slide.set(1);
				System.out.println("Down is working" + slide.get());
				
			} else {
				
				slide.set(0);
				
			}
			
			//Intake Wheels
			LPDial = (1 - ((LPDial + 1.0) / 2.0));
			SmartDashboard.putNumber("LPDial", LPDial);
			
			if (LPIntakeForward) {
				
				intakeL.set(-(LPDial));
				intakeR.set(-(LPDial));
				
			} else if (LPIntakeBack) {
				intakeL.set((LPDial));
				intakeR.set((LPDial));
				
			} else {
				
				intakeL.set(0);
				intakeR.set(0);
				
			}
			
			boolean AButton = mecstick.getAButton();
			
			//Autoturn
			/*if (AButton == true) {
				
				double gyroAngle = gyro.getAngle();
				double finalAngle = gyroAngle + 180;
				while (finalAngle > (gyroAngle + 20)) {
					
					myDrive.driveCartesian(0.0, 0.0, aSpeed);
					gyroAngle = gyro.getAngle();
					System.out.print(gyroAngle);
					System.out.print(":");
					System.out.println(finalAngle);
				}
				
			}*/
			
		
			//System.out.println(POV);
			
			//System.out.println(gyroAngle);
			if (POV == 0) {
				
				POVYAxis = -POVSpeed;
				POVXAxis = 0;
				
			} else if (POV == 90) {
				
				POVXAxis = -POVSpeed;
				POVYAxis = 0;
				
			} else if (POV == 180) {
				
				POVYAxis = POVSpeed;
				POVXAxis = 0;
				
			} else if (POV == 270) {
				
				POVXAxis = POVSpeed;
				POVYAxis = 0;
				
			} else {
				
				POVXAxis = 0;
				POVYAxis = 0;
				
			}
			
			
			rotateAxis = mecstick.getX(GenericHID.Hand.kRight)*-1;
			
			rotateDeadzone = 0.2;
			
			if (Math.abs(rotateAxis) < rotateDeadzone) {
				
				rotateValue = 0;
				
			} else if (rotateAxis < 0) {
				
				rotateValue = rotateAxis + rotateDeadzone;
				
			} else if (rotateAxis > 0) {
				
				rotateValue = rotateAxis - rotateDeadzone;
				
			} else {
				
				rotateValue = 0;
				
			}
			
			moveDeadzone = .1;
			
			moveXAxis = mecstick.getX(GenericHID.Hand.kLeft)*-1.0;
			
			if (Math.abs(moveXAxis) < moveDeadzone) {
				
				moveXValue = 0;
				
			} else if (moveXAxis < 0) {
				
				moveXValue = moveXAxis + moveDeadzone;
				
			} else if (moveXAxis > 0) {
				
				moveXValue = moveXAxis - moveDeadzone;
				
			} else {
				
				moveXValue = 0;
				
			}
			
			moveYAxis = mecstick.getY(GenericHID.Hand.kLeft);
			
			if (Math.abs(moveYAxis) < moveDeadzone) {
				
				moveYValue = 0;
				
			} else if (moveYAxis < 0) {
				
				moveYValue = moveYAxis + moveDeadzone;
				
			} else if (moveYAxis > 0) {
				
				moveYValue = moveYAxis - moveDeadzone;
				
			} else {
				
				moveYValue = 0;
				
			}
			
			/*if (YButton == true) {
				
				//double curveAngle = gyro.getAngle();
				
				//turnController.setSetpoint(0.0f);
				
				//turnController.enable();
				
				////System.out.println(rotateToAngleRate);
				myDrive.drivePolar(0.4, 0.0, 0.0);
				
			}*/
			
			//System.out.println(rotateValue);
			
			//myDrive.driveCartesian(mecstick.getX(), mecstick.getY(), mecstick.getZ());
			//myDrive.driveCartesian(mecstick.getX(GenericHID.Hand.kRight)*-1.0, mecstick.getY(GenericHID.Hand.kLeft), rotateAxis);
			//myDrive.driveCartesian(mecstick.getX(GenericHID.Hand.kLeft)*-1.0, mecstick.getY(GenericHID.Hand.kLeft), rotateAxis);
				
			rotateLimit = .9;
			speedLimit = .8;
			
			//System.out.println(moveXValue);
			
			myDrive.driveCartesian(((moveXValue * speedLimit)+ POVXAxis), ((moveYValue * speedLimit) + POVYAxis), (rotateValue * rotateLimit));
			//myDrive.driveCartesian(-((moveYValue * speedLimit) + POVYAxis), -((moveXValue * speedLimit)+ POVXAxis), (rotateValue * rotateLimit));
			
			
		/*
		boolean YButton = mecstick.getYButton();
		byte[] sensorData = new byte[64];
		
		if (YButton == true) {
			
			byte[] startMotor = new byte[] {'M', 'S', '0', '1', '\n'};	
			port.write(startMotor, startMotor.length);
			
			if (port.getBytesReceived() >= 9)
			{
				sensorData = port.read(9);
				for (int x = 0; x < 9; x++)
				{
					System.out.printf("%x ", sensorData[x]);
				}
				System.out.println("");
				System.out.flush();
			}

		}
		
		
		
		boolean AButton = mecstick.getAButton();
		if (AButton == true) {
			
			// This is the stop motor command
			byte[] stopMotor = new byte[] {'M', 'S', '0', '0', '\n'};
			port.write(stopMotor, stopMotor.length);
			
			if (port.getBytesReceived() >= 9)
			{
				sensorData = port.read(9);
				for (int x = 0; x < 9; x++)
				{
					System.out.printf("%x ", sensorData[x]);
				}
				System.out.println("");
				System.out.flush();
			}
		}
		
		boolean XButton = mecstick.getXButton();
		if (XButton == true) {
			byte[] checkReady = new byte[] {'M', 'Z', '\n'};
			port.write(checkReady, checkReady.length);
			Timer.delay(0.01);
			
			int numToRead = port.getBytesReceived();
			if (numToRead > 0)
			{
				sensorData = port.read(numToRead);
				for (int x = 0; x < numToRead; x++)
				{
					System.out.printf("%x ", sensorData[x]);
				}
				System.out.println("");
				System.out.flush();
				
				if (sensorData[2] == 0x30 && sensorData[3] == 0x30)
				{
					lidar_ready = true;
					System.out.println("Lidar Ready");
					
					byte[] startData = new byte[] {'D', 'S', '\n'};
					port.write(startData, startData.length);
					Timer.delay(0.01);
					
					
					int bytesReady = port.getBytesReceived();
					while (bytesReady != 6)
					{
						bytesReady = port.getBytesReceived();
						Timer.delay(0.01);
					}
					
					if (bytesReady > 0)
					{
						sensorData = port.read(6);
						
						for (int x = 0; x < 6; x++)
						{
							System.out.printf("0x%02x ", sensorData[x]);
						}
						System.out.println("");
						System.out.flush();
					}
					
					
				}
				else
				{
					lidar_ready = false;
					System.out.println("Lidar NOT Ready");
					
					
					byte[] stopData = new byte[] {'D', 'X', '\n'};
					port.write(stopData, stopData.length);
					Timer.delay(0.01);
					
					int bytesReady = port.getBytesReceived();
					
					if (bytesReady > 0)
					{
						sensorData = port.read(bytesReady);
						
						for (int x = 0; x < bytesReady; x++)
						{
							System.out.printf("0x%02x ", sensorData[x]);
						}
						System.out.println("");
						System.out.flush();
					}
					
				}
			}
			
			
		}
		
		boolean BButton = mecstick.getBButton();
		if (BButton == true) 
		{
			
				
				int bytesReady = port.getBytesReceived();
			
				if (bytesReady >= 0)
				{
					sensorData = port.read(bytesReady);
					
					for (int x = 0; x < bytesReady; x++)
					{
						System.out.printf("0x%02x ", sensorData[x]);
					}
					System.out.println("");
					System.out.flush();
					
					
					int angle_int = (sensorData[2] << 8) + sensorData[1];
					float angle = (float) (angle_int / 16.0);
					short distance = (short) ((sensorData[4] << 8) + (sensorData[3]));
					byte signal = sensorData[5];
					
					System.out.printf("Angle: %f, Distance: %d, Strength: %d\n", angle, distance, signal);
					
				}
	
		}
		
		
		myDrive.driveCartesian(0.0, 0.0, 0.0);

		*/
	}
	
	
	/**
	 * This function is called periodically during test mode.
	 */

	
	
	/*@Override
	public void testPeriodic() {
		
		while (isEnabled()) {
		
			WPI_TalonSRX slide = new WPI_TalonSRX(kSlide);
			WPI_TalonSRX intakeL = new WPI_TalonSRX(kIntakeLeft);
			WPI_TalonSRX intakeR = new WPI_TalonSRX(kIntakeRight);
			WPI_TalonSRX winch = new WPI_TalonSRX(kWinch);
			WPI_TalonSRX climbPole = new WPI_TalonSRX(kClimbPole);
			
			double rotateAxis;
			double rotateValue;
			double rotateDeadzone;
			
			double moveDeadzone;
			double moveXAxis;
			double moveYAxis;
			double moveXValue;
			double moveYValue;
			//rotateAxis = mecstick.getTriggerAxis(GenericHID.Hand.kLeft) + (mecstick.getTriggerAxis(GenericHID.Hand.kRight)*-1);
			
			int POV = mecstick.getPOV();
			double POVXAxis;
			double POVYAxis;
			double POVSpeed = 0.5;
			
			double speedLimit;
			double rotateLimit;
			
			boolean LPSafety = launchpad.getRawButton(4);
			boolean LPWinch = launchpad.getRawButton(5);
			boolean LPPoleDown = launchpad.getRawButton(6);
			boolean LPPoleUp = launchpad.getRawButton(7);
			boolean LPCUp = launchpad.getRawButton(8);
			boolean LPCDown = launchpad.getRawButton(9);
			boolean LPIntakeForward = launchpad.getRawButton(10);
			boolean LPIntakeBack = launchpad.getRawButton(11);
			
			double LPDial = launchpad.getRawAxis(6);
			
			if (LPSafety == true) {
				
				if (LPPoleUp == true) {
					
					climbPole.set(.2);
					winch.set(-.2);
					
				} else {
					
					climbPole.set(0);
					winch.set(0);
					
				}
				
				if (LPPoleDown == true) {
					
					climbPole.set(-.2);
					
				} else {
					
					climbPole.set(0);
					
				}
				
				if (LPWinch == true) {
					
					winch.set(.2);
					
				} else {
					
					winch.set(0);
					
				}
				
			}
			
			if (LPCUp == true) {
				
				slide.set(.2);
				
			} else {
				
				slide.set(0);
				
			}
			
			if (LPCDown == true) {
				
				slide.set(-.2);
				
			} else {
				
				slide.set(0);
				
			}
			
			if (LPIntakeForward == true) {
				
				intakeL.set(-(LPDial/4100));
				intakeR.set((LPDial/4100));
				
			} else {
				
				intakeL.set(0);
				intakeR.set(0);
				
			}
			
			if (LPIntakeBack == true) {
				
				intakeL.set((LPDial/4100));
				intakeR.set((LPDial/4100));
				
			} else {
				
				intakeL.set(0);
				intakeR.set(0);
				
			}
			
			//System.out.println(LPButton8);
			//System.out.flush();
			
			
			
			//SmartDashboard.putNumber("AutoRotationSpeed", aSpeed);
			
			//System.out.println(aSpeed);
			
			if (LPButton10 == true) {
				
				aSpeed = -3.5;
				
			}
			
			boolean YButton = mecstick.getYButton();
			//System.out.println(YButton);
			if (YButton == true) {
				
				System.out.println("CALIBRATING");
				gyro.calibrate();
				System.out.println("CALIBRATION COMPLETED");
				
			}

			if (LPButton8 == true) {
			
				aSpeed = -.1;
				
			}
			//boolean AButton = mecstick.getAButton();
					
			if (AButton == true) {
				
				CameraServer.getInstance().startAutomaticCapture();
				
				//aSpeed = -.1;
				
				//PWILights.set(.1);
				
			}
			
			
			
			int count = 1;
			if (count == 1) {
				//SweepJNI.setMotorSpeed(device, 5);
				
				//SweepJNI.startScanning(device); 
			//count--;
			System.out.println("hello world");
			System.out.flush();
			}
			
			
			//System.out.println(YButton);
			
			
			
			//System.out.println(AButton);
			if (AButton == true) {
				
				double gyroAngle = gyro.getAngle();
				double finalAngle = gyroAngle + 180;
				while (finalAngle > (gyroAngle + 20)) {
					
					myDrive.driveCartesian(0.0, 0.0, aSpeed);
					gyroAngle = gyro.getAngle();
					System.out.print(gyroAngle);
					System.out.print(":");
					System.out.println(finalAngle);
				}
				
			}
			
		
			//System.out.println(POV);
			
			//System.out.println(gyroAngle);
			if (POV == 0) {
				
				POVYAxis = -POVSpeed;
				POVXAxis = 0;
				
			} else if (POV == 90) {
				
				POVXAxis = -POVSpeed;
				POVYAxis = 0;
				
			} else if (POV == 180) {
				
				POVYAxis = POVSpeed;
				POVXAxis = 0;
				
			} else if (POV == 270) {
				
				POVXAxis = POVSpeed;
				POVYAxis = 0;
				
			} else {
				
				POVXAxis = 0;
				POVYAxis = 0;
				
			}
			
			
			rotateAxis = mecstick.getX(GenericHID.Hand.kRight)*-1;
			
			rotateDeadzone = 0.2;
			
			if (Math.abs(rotateAxis) < rotateDeadzone) {
				
				rotateValue = 0;
				
			} else if (rotateAxis < 0) {
				
				rotateValue = rotateAxis + rotateDeadzone;
				
			} else if (rotateAxis > 0) {
				
				rotateValue = rotateAxis - rotateDeadzone;
				
			} else {
				
				rotateValue = 0;
				
			}
			
			moveDeadzone = .1;
			
			moveXAxis = mecstick.getX(GenericHID.Hand.kLeft)*-1.0;
			
			if (Math.abs(moveXAxis) < moveDeadzone) {
				
				moveXValue = 0;
				
			} else if (moveXAxis < 0) {
				
				moveXValue = moveXAxis + moveDeadzone;
				
			} else if (moveXAxis > 0) {
				
				moveXValue = moveXAxis - moveDeadzone;
				
			} else {
				
				moveXValue = 0;
				
			}
			
			moveYAxis = mecstick.getY(GenericHID.Hand.kLeft);
			
			if (Math.abs(moveYAxis) < moveDeadzone) {
				
				moveYValue = 0;
				
			} else if (moveYAxis < 0) {
				
				moveYValue = moveYAxis + moveDeadzone;
				
			} else if (moveYAxis > 0) {
				
				moveYValue = moveYAxis - moveDeadzone;
				
			} else {
				
				moveYValue = 0;
				
			}
			
			if (YButton == true) {
				
				//double curveAngle = gyro.getAngle();
				
				//turnController.setSetpoint(0.0f);
				
				//turnController.enable();
				
				System.out.println(rotateToAngleRate);
				myDrive.drivePolar(0.4, 0.0, 0.0);
				
			}
			
			//System.out.println(rotateValue);
			
			//myDrive.driveCartesian(mecstick.getX(), mecstick.getY(), mecstick.getZ());
			//myDrive.driveCartesian(mecstick.getX(GenericHID.Hand.kRight)*-1.0, mecstick.getY(GenericHID.Hand.kLeft), rotateAxis);
			//myDrive.driveCartesian(mecstick.getX(GenericHID.Hand.kLeft)*-1.0, mecstick.getY(GenericHID.Hand.kLeft), rotateAxis);
				
			rotateLimit = 1;
			speedLimit = .8;
			
			//System.out.println(moveXValue);
			
			myDrive.driveCartesian((moveXValue * speedLimit)+ POVXAxis, (moveYValue * speedLimit) + POVYAxis, rotateValue * rotateLimit);
				
			
			
			
			if (mecstick.getTrigger() == true)
			{
				myDrive.driveCartesian(0.0, -0.1, 0.0 );
			}
			 
			//Timer.delay(0.02);
			
			
		}
	}
	
	public void pidWrite(double output) {
		rotateToAngleRate = output;
	}*/
}

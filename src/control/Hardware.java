package control;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.SerialPort;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Hardware
{
	private static final String PORT = "COM3";
	
	private static Hardware hardware = null;
	private Climate climate;
	
	private SerialPort serialPort;
	private OutputStream output;
	private InputStream input;
	
	public static Hardware getInstance()
	{
		if (hardware == null)
			hardware = new Hardware();

		return hardware;
	}

	public Hardware()
	{
		try
		{
			CommPortIdentifier portId = CommPortIdentifier.getPortIdentifier(PORT);
			
			serialPort = (SerialPort) portId.open(this.getClass().getName(), 2000);
			serialPort.setSerialPortParams(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
			
			input = serialPort.getInputStream();
			output = serialPort.getOutputStream();
		} catch (NoSuchPortException e)
		{
			// No such port
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		
		try
		{
			/* Wait for connection to establish */
			while (input.available() <= 0)
			{
				writeToArduino(0x00);
			}
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private void writeToArduino(int command)
	{
		if (output != null)
		{
			try
			{
				output.write(command);
				output.flush();
			} catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	public synchronized void close()
	{
		if (serialPort != null)
		{
			serialPort.close();
		}
	}

	@Override
	public void finalize() throws Throwable
	{
		close();
		super.finalize();
	}
	
	private void setDeviceState(char c, boolean state)
	{
		writeToArduino((state ? 0x20 : 0x10) | (c - 'A'));
	}
	
	private char[] getDevicesForClimate(Climate climate)
	{
		switch (climate)
		{
		case COLD:
			return new char[] { 'C' };

		case WARM:
			return new char[] { 'B' };
			
		case MOIST:
			return new char[] { 'A' };
			
		default: // NORMAL
			return new char[0];
		}
	}
	
	private void setClimateState(Climate climate, boolean state)
	{
		char[] devices = getDevicesForClimate(climate);
		
		for (char c : devices)
		{
			setDeviceState(c, state);
		}
	}
	
	public void setClimate(Climate climate)
	{
		setClimateState(this.climate, false);
		this.climate = climate;
		setClimateState(this.climate, true);
	}

	public void sprayScent(Scent scent)
	{
		writeToArduino(0x10 | (scent.ordinal() + 3));
	}
}

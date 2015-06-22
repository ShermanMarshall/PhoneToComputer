import java.io.*;
import java.net.*;

//A CLI Java implementation to send data to a communicable device
public class PhoneConnection {
	String data, ip; 
	boolean success;
	
	public PhoneConnection (String ip) {
		data = "";
		this.ip = ip;
	}
	
	public static boolean validateIP (String ip) {
		String[] quad = ip.split("\\.");
		for (String s : quad) 
			try {
				int num = Integer.parseInt(s);
				if ((num > 255) || (num < 0))
					return false;
			} catch (NumberFormatException nfe) { 
				return false; 
			}
		return true;
	}
	
	public boolean read() {
		boolean complete = true;
		try {
			ServerSocket ss = new ServerSocket(30000, 1, InetAddress.getByName(ip));
			Socket s = ss.accept();
			InputStream is = s.getInputStream();
			char x; byte[] buffer; int size;
			
			while ((x = (char) is.read()) != ':')
				data += x;
			
			size = Integer.parseInt(data);
			buffer = new byte[size];			
			int num = is.read(buffer, 0, size);
			
			data = new String(buffer);
			is.close();
			s.close();
			
			if (num == size) { 				
				System.out.println(data);
				outputData(data);
			} else 
				System.out.println("Error: input data not fully received");
		} catch (IOException e) {
			System.out.println(e.getMessage());
			complete = false;
		} catch (NumberFormatException nfe) { 
			System.out.println(nfe.getMessage());
			complete = false;
		}
		return complete;
	}
	
	//Write output to file for access since copy/paste doesn't work in the 
	//Windows CLI (yay Windows!)
	public void outputData (String output) {
		try {
			PrintWriter pw = new PrintWriter(new FileWriter("Data/output.txt"));
			pw.write(output);
			pw.close();
		} catch (IOException e) {
			System.out.println("Error: " + e.getMessage());
		}
	}
	public boolean write(String[] args) {
		boolean complete = true;
		for (int x = 1; x < args.length; x++)
			data += args[x];
		try {
			ServerSocket ss = new ServerSocket(30000, 1, InetAddress.getByName(ip));
			Socket s = ss.accept();
			OutputStream os = s.getOutputStream();
			data = Integer.toString(data.length()) + ":" + data;
			os.write(data.getBytes());
			os.close();
			s.close();
		} catch (IOException e) {
			System.out.println(e.getMessage());
			complete = false;
		}
		return complete;
	}
	
	public static void main(String[] args) {
		String data = "";
		if (args.length == 0) {
			System.out.println("Error: An IP address is required as the first argument");
			return;
		}
		if (!PhoneConnection.validateIP(args[0])) {
			System.out.println("Error: Invalid IP. First argument must be the device's valid IPv4 address");
			return;
		}
		PhoneConnection pc = new PhoneConnection(args[0]);
		
		if (args.length == 1)
			pc.success = pc.read();
		else
			pc.success = pc.write(args);
		
		if (pc.success)
			System.out.println("Data transmit successfully");
	}
}
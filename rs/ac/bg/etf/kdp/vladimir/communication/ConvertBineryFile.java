package rs.ac.bg.etf.kdp.nikola.communication;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class ConvertBineryFile {
	
	//Staticki metod koji sluzi za pretvaranje fajla u bajt kod i slanje tih fajlova 
	//na socket
	public static void sendBineryFile(File file, ObjectInputStream in, ObjectOutputStream out)
			throws ClassNotFoundException, IOException {
		int bytes = 0;
		
		FileInputStream fileInputStream = new FileInputStream(file);
		
		out.writeObject(file.getName());
		out.writeLong(file.length());
		out.flush();
		
		byte[] buffer = new byte[4 * 1024];
		while ((bytes = fileInputStream.read(buffer)) != -1) {
			out.write(buffer, 0, bytes);
			out.flush();
			
		}
		fileInputStream.close();
	}

	//Staticki metod koji prima binarni fajl sa socketa i ucitava ga 
	public static File receiveBineryFile(ObjectInputStream in, ObjectOutputStream out)
			throws ClassNotFoundException, IOException {
		makeDirectory();
		int bytes = 0;

		String s = (String) in.readObject();
		
		
		File file = new File("Junk\\"+s);
		
		file.createNewFile();
		if(file.getName().equals("ERROR404")) {return null;}
		
		FileOutputStream fileOutputStream = new FileOutputStream(file);

		long size = in.readLong(); 
		byte[] buffer = new byte[4 * 1024];
		while (size > 0 && (bytes = in.read(buffer, 0, (int) Math.min(buffer.length, size))) != -1) {
			fileOutputStream.write(buffer, 0, bytes);
			size -= bytes; 
		}
		fileOutputStream.close();
		
		File copyfile= new File(s);
		
		try {
			copyContent(file, copyfile);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return copyfile;
	}
	
	//---------------------------------------------------------------------------
	//Metod za kopiranje podatka izmedju dva fajla
	private static void copyContent(File a, File b) throws Exception {
		FileInputStream in = new FileInputStream(a);
		FileOutputStream out = new FileOutputStream(b);

		try {

			int n;
			while ((n = in.read()) != -1) {
				out.write(n);
			}
		} finally {
			if (in != null) {
				in.close();
			}
			if (out != null) {
				out.close();
			}
		}
	}
	private static void makeDirectory() {
		File theDir = new File("Junk");
		if (!theDir.exists()){
		    theDir.mkdirs();
		}
	}
}



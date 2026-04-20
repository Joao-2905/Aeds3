package CRUD.util;

import java.io.*;

public class FileManeger {

	public static RandomAccessFile open(String path) throws IOException{
		
		File file = new File(path);
		
		if(!file.exists()) {
			file.createNewFile();
		}
		
		return new RandomAccessFile(file, "rw");
	}
}

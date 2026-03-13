package CRUD.dao;

import CRUD.util.*;
import CRUD.model.*;

import java.io.*;

public class GenreDAO {
	
	private final String FILE = "data/genre.bin";
	
	public GenreDAO() throws IOException{
		initFile();
	}
	
	private void initFile() throws IOException {
		
		RandomAccessFile raf = FileManeger.open(FILE);
		
		if (raf.length() == 0) {
            raf.writeInt(0); // último ID
        }

        raf.close();
	}
	
	public int create(Genre g) throws IOException {
		
		RandomAccessFile raf = FileManeger.open(FILE);
		
		raf.seek(0);
		
		int lastID = raf.readInt();
		int newID = lastID + 1;
		
		raf.seek(0);
		raf.writeInt(newID);
		
		byte[] nameBytes = g.getName().getBytes("UTF-8");
	    short nameSize = (short) nameBytes.length;
		
		//lápide + id + tamNome + nome
		int recordSize = 1 + 4 + 2 + nameSize;
		
		raf.seek(raf.length());
		
		raf.writeInt(recordSize);
		
		// lápide
        raf.writeBoolean(true);
        raf.writeInt(newID);
		raf.writeShort(nameSize);
		raf.write(nameBytes);
		
		raf.close();
		return newID;
	}
	
	public Genre read(int id) throws IOException{
		
		RandomAccessFile raf = FileManeger.open(FILE);
		raf.seek(4); // pula header
		
		while(raf.getFilePointer() < raf.length()) {
			
			long pos = raf.getFilePointer();
			
			int recordSize = raf.readInt();
			
			boolean active = raf.readBoolean();
			int tmpID = raf.readInt();
			
			if(active && tmpID == id) {
				
				short nameSize = raf.readShort();
				byte[] nameBytes = new byte[nameSize];
				raf.readFully(nameBytes);
				String name = new String(nameBytes, "UTF-8");
				
				raf.close();
				return new Genre(tmpID, name);
			}
			raf.seek(pos + recordSize + 4);
			
		}
		
		raf.close();
		return null;
	}
	
	public boolean update(Genre g) throws IOException {
		
		RandomAccessFile raf = FileManeger.open(FILE);
		
		raf.seek(4);//pula header
		
		while(raf.getFilePointer() < raf.length()) {
			
			long pos = raf.getFilePointer();
			
			int recordSize = raf.readInt();
					
			boolean active = raf.readBoolean();
			int tmpID = raf.readInt();
			
			
			if(active && tmpID == g.getID()) {
				
				raf.seek(pos + 4);
				raf.writeBoolean(false);
				
				raf.close();
				
				createWithId(g);
				
				return true;
			}
			
			raf.seek(pos + recordSize + 4);
		}
		
		raf.close();
		return false;
	}
	
	private void createWithId(Genre g) throws IOException{
		
		RandomAccessFile raf = FileManeger.open(FILE);
		
		raf.seek(raf.length());
		
		byte[] nameBytes = g.getName().getBytes("UTF-8");
		short nameSize = (short) nameBytes.length;
		
		//lápide + ID + tamNome + nome
		int recordSize = 1 + 4 + 2 + nameSize;
		
		raf.writeInt(recordSize);
		
		raf.writeBoolean(true);
		raf.writeInt(g.getID());
		
		raf.writeShort(nameSize);
		raf.write(nameBytes);
		
		raf.close();
	}
	
	public boolean delete(int id) throws IOException {
		
		RandomAccessFile raf = FileManeger.open(FILE);
		
		raf.seek(4);
		
		while(raf.getFilePointer() < raf.length()) {
			
			long pos = raf.getFilePointer();
			
			int recordSize = raf.readInt();
			
			boolean active = raf.readBoolean();
			int tmpID = raf.readInt();
			
			if(active && tmpID == id) {
				
				raf.seek(pos + 4);
				raf.writeBoolean(false);
				
				raf.close();
				return true;
			}
			
			raf.seek(pos + recordSize + 4);
			
		}
		
		raf.close();
		return false;
	}
	
	public void listAll() throws IOException {
		
		RandomAccessFile raf = FileManeger.open(FILE);
		
		raf.seek(4); // pula header
		
		System.out.println("\n--- Genêros ---");
		
		while(raf.getFilePointer() < raf.length()) {
			
			long pos = raf.getFilePointer();
			
			int recordSize = raf.readInt();
			
			boolean active = raf.readBoolean();
			
            if(active) {
            	int id = raf.readInt();
            	
            	short nameSize = raf.readShort();
				byte[] nameBytes = new byte[nameSize];
				raf.readFully(nameBytes);
				String name = new String(nameBytes, "UTF-8");
			
            	 System.out.println(
                         "ID: " + id + " | " + "Gênero: " +  name 
                     );
            	
            }
            raf.seek(pos + recordSize + 4);
		}
		
		raf.close();
	}
	
}

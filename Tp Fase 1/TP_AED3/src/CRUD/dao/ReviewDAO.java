package CRUD.dao;

import CRUD.util.*;
import CRUD.model.*;

import java.io.*;

public class ReviewDAO {
	
	private final String FILE = "data/review.bin";
	
	public ReviewDAO() throws IOException{
		initFile();
	}
	
	private void initFile() throws IOException{
		
		RandomAccessFile raf = FileManeger.open(FILE);
		
		if (raf.length() == 0) {
            raf.writeInt(0); // último ID
        }

        raf.close();
	}
	
	public int create(Review r) throws IOException {
		
		RandomAccessFile raf = FileManeger.open(FILE);
		
		raf.seek(0);
		
		int lastID = raf.readInt();
		int newID = lastID + 1;
		
		raf.seek(0);
		raf.writeInt(newID);
		
		byte[] noteBytes = r.getNote().getBytes();
		short noteSize = (short) noteBytes.length;
		
		//lápide + id + userID + filmID + rating + tamanho comentario + comentario
		int recordSize = 1 + 4 + 4 + 4 + 2 + 2 + noteSize;
		
		raf.seek(raf.length());
		raf.writeInt(recordSize);
		raf.writeBoolean(true);
		
		raf.writeInt(newID);
		raf.writeInt(r.getUserID());
		raf.writeInt(r.getFilmID());
		raf.writeShort(r.getRating());
		raf.writeShort(noteSize);
		raf.write(noteBytes);
		
		raf.close();
		
		return newID;
	}
	
	public Review read(int ID) throws IOException {
		
		RandomAccessFile raf = FileManeger.open(FILE);
		raf.seek(4); // pula header
		
		while(raf.getFilePointer() < raf.length()) {
			
			long pos = raf.getFilePointer();
			
			int recordSize = raf.readInt();
			
			boolean active = raf.readBoolean();
			int tmpID = raf.readInt();
			
			if(active && tmpID == ID) {
				
				int userID = raf.readInt();
				int filmID = raf.readInt();
				short rating = raf.readShort();
				
				short noteSize = raf.readShort();
				byte[] noteBytes = new byte[noteSize];
				raf.readFully(noteBytes);
				String note = new String(noteBytes, "UTF-8");
				
				raf.close();
				return new Review(tmpID, userID, filmID, rating, note);
			}
			
			raf.seek(pos + recordSize + 4);
			
		}
		
		raf.close();
		return null;
	}
	
	public boolean update (Review r) throws IOException{
		
		RandomAccessFile raf = FileManeger.open(FILE);
		
		raf.seek(4);//pula header
		
		while(raf.getFilePointer() < raf.length()) {
			
			long pos = raf.getFilePointer();
			
			int recordSize = raf.readInt();
					
			boolean active = raf.readBoolean();
			int tmpID = raf.readInt();
			
			
			if(active && tmpID == r.getID()) {
				
				raf.seek(pos + 4);
				raf.writeBoolean(false);
				
				raf.close();
				
				createWithID(r);
				
				return true;
			}
			
			raf.seek(pos + recordSize + 4);
		}
			
		raf.close();	
		return false;
	}
	
	public void createWithID(Review r) throws IOException {
		
		RandomAccessFile raf = FileManeger.open(FILE);
		
		byte[] noteBytes = r.getNote().getBytes();
		short noteSize = (short) noteBytes.length;
		
		//lápide + id + userID + filmID + rating + tamanho comentario + comentario
		int recordSize = 1 + 4 + 4 + 4 + 2 + 2 + noteSize;
		
		raf.seek(raf.length());
		raf.writeInt(recordSize);
		raf.writeBoolean(true);
		
		raf.writeInt(r.getID());
		raf.writeInt(r.getUserID());
		raf.writeInt(r.getFilmID());
		raf.writeShort(r.getRating());
		raf.writeShort(noteSize);
		raf.write(noteBytes);
		
		raf.close();
		
	}
	
	public boolean delete(int ID) throws IOException {
		
		RandomAccessFile raf = FileManeger.open(FILE);
		raf.seek(4);//pula header
		
		while(raf.getFilePointer() < raf.length()) {
			
			long pos = raf.getFilePointer();
			
			int recordSize = raf.readInt();
			
			boolean active = raf.readBoolean();
			int tmpID = raf.readInt();
			
			if(active && tmpID == ID) {
				
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
		
		System.out.println("\n--- Reviews ---");
		
		while(raf.getFilePointer() < raf.length()) {
			
			long pos = raf.getFilePointer();
			
			int recordSize = raf.readInt();
			boolean active = raf.readBoolean();
			
			if(active) {
				
				int id = raf.readInt();
				int userID = raf.readInt();
				int filmID = raf.readInt();
				short rating = raf.readShort();
				
				short noteSize = raf.readShort();
				byte[] noteBytes = new byte[noteSize];
				raf.readFully(noteBytes);
				String note = new String(noteBytes, "UTF-8");
				
				System.out.println(
                       "ID: " + id + " | " + "Usuário: " + userID + " | " + "ID do filme: " + filmID + " | "+ "Nota (0-5): " + rating + " | " + "Comentário: " + note
                    );
				
			}
			raf.seek(pos + recordSize + 4);
			
		}
		
		raf.close();
	}
	
}

package CRUD.dao;

import CRUD.util.*;
import CRUD.model.*;

import java.io.*;

public class UserDAO {
	
	private final String FILE = "data/user.bin";
	
	public UserDAO() throws IOException{
		initFile();
	}
	
	private void initFile() throws IOException {
		
	    System.out.println("INIT USER DAO");

	    RandomAccessFile raf = FileManeger.open(FILE);

	    if (raf.length() == 0) {

	        raf.writeInt(0); // último ID

	        raf.close();

	        // cria admin padrão
	        User admin = new User(
	                "admin",
	                "admin@admin.com",
	                "123",
	                true
	        );

	        create(admin);

	        System.out.println("Administrador padrão criado:");
	        System.out.println("Email: admin@admin.com");
	        System.out.println("Senha: 123");

	    } else {
	        raf.close();
	    }
	}
	
	//create
	public int create(User x) throws IOException{

	    RandomAccessFile raf = FileManeger.open(FILE);

	    raf.seek(0);

	    int lastID = raf.readInt();
	    int newID = lastID + 1;

	    raf.seek(0);
	    raf.writeInt(newID);

	    byte[] nameBytes = x.getUsername().getBytes("UTF-8");
	    short nameSize = (short) nameBytes.length;

	    byte[] emailBytes = x.getEmail().getBytes("UTF-8");
	    short emailSize = (short) emailBytes.length;

	    // XOR na senha
	    String senhaCripto = Crypto.xor(x.getPassword());

	    byte[] passwordBytes = senhaCripto.getBytes("UTF-8");
	    short passwordSize = (short) passwordBytes.length;

	    //lápide, ID, tam nome, nome, tam email, email, tam senha, senha, administrador
	    int recordSize = 1 + 4 + 2 + nameSize + 2 + emailSize + 2 + passwordSize + 1;

	    raf.seek(raf.length());

	    raf.writeInt(recordSize);

	    //lápide
	    raf.writeBoolean(true);

	    raf.writeInt(newID);

	    raf.writeShort(nameSize);
	    raf.write(nameBytes);

	    raf.writeShort(emailSize);
	    raf.write(emailBytes);

	    raf.writeShort(passwordSize);
	    raf.write(passwordBytes);

	    // administrador
	    raf.writeBoolean(x.isAdministrator());

	    raf.close();

	    return newID;
	}
	
	public User read(int idBusca) throws IOException {

	    RandomAccessFile raf = FileManeger.open(FILE);

	    raf.seek(4);

	    while (raf.getFilePointer() < raf.length()) {

	        long pos = raf.getFilePointer();

	        int recordSize = raf.readInt();
	        boolean active = raf.readBoolean();

	        if(!active){
	            raf.seek(pos + 4 + recordSize);
	            continue;
	        }

	        int id = raf.readInt();

	        short nameSize = raf.readShort();
	        byte[] nameBytes = new byte[nameSize];
	        raf.readFully(nameBytes);
	        String username = new String(nameBytes, "UTF-8");

	        short emailSize = raf.readShort();
	        byte[] emailBytes = new byte[emailSize];
	        raf.readFully(emailBytes);
	        String email = new String(emailBytes, "UTF-8");

	        short passwordSize = raf.readShort();
	        byte[] passwordBytes = new byte[passwordSize];
	        raf.readFully(passwordBytes);

	        String senhaCripto = new String(passwordBytes, "UTF-8");
	        String password = Crypto.xor(senhaCripto);

	        boolean admin = raf.readBoolean();

	        if(id == idBusca){
	            raf.close();
	            return new User(id, username, email, password, admin);
	        }

	        raf.seek(pos + 4 + recordSize);
	    }

	    raf.close();
	    return null;
	}
	
	//update 
	public boolean update(User x) throws IOException {

	    RandomAccessFile raf = FileManeger.open(FILE);

	    raf.seek(4); // pula header

	    while(raf.getFilePointer() < raf.length()) {

	        long pos = raf.getFilePointer();

	        int recordSize = raf.readInt();

	        boolean active = raf.readBoolean();
	        int tmpID = raf.readInt();

	        if(active && tmpID == x.getID()) {

	            // marca lápide
	            raf.seek(pos + 4);
	            raf.writeBoolean(false);

	            raf.close();

	            // recria registro atualizado no final
	            createWithId(x);

	            return true;
	        }

	        raf.seek(pos + recordSize + 4);
	    }

	    raf.close();

	    return false;
	}

	private void createWithId(User x) throws IOException{

	    RandomAccessFile raf = FileManeger.open(FILE);

	    byte[] nameBytes = x.getUsername().getBytes("UTF-8");
	    short nameSize = (short) nameBytes.length;

	    byte[] emailBytes = x.getEmail().getBytes("UTF-8");
	    short emailSize = (short) emailBytes.length;

	    String senhaCripto = Crypto.xor(x.getPassword());
	    byte[] passwordBytes = senhaCripto.getBytes("UTF-8");
	    short passwordSize = (short) passwordBytes.length;

	    int recordSize = 1 + 4 + 2 + nameSize + 2 + emailSize + 2 + passwordSize + 1;

	    raf.seek(raf.length());

	    raf.writeInt(recordSize);

	    raf.writeBoolean(true);
	    raf.writeInt(x.getID());

	    raf.writeShort(nameSize);
	    raf.write(nameBytes);

	    raf.writeShort(emailSize);
	    raf.write(emailBytes);

	    raf.writeShort(passwordSize);
	    raf.write(passwordBytes);

	    raf.writeBoolean(x.isAdministrator());

	    raf.close();
	}
	
	//delete (lápide)
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

	    raf.seek(4);

	    System.out.println("\n--- Users ---");

	    while (raf.getFilePointer() < raf.length()) {

	        long pos = raf.getFilePointer();

	        int recordSize = raf.readInt();
	        boolean active = raf.readBoolean();

	        if(!active){
	            raf.seek(pos + 4 + recordSize);
	            continue;
	        }

	        int id = raf.readInt();

	        short nameSize = raf.readShort();
	        byte[] nameBytes = new byte[nameSize];
	        raf.readFully(nameBytes);
	        String username = new String(nameBytes, "UTF-8");

	        short emailSize = raf.readShort();
	        byte[] emailBytes = new byte[emailSize];
	        raf.readFully(emailBytes);
	        String email = new String(emailBytes, "UTF-8");

	        short passwordSize = raf.readShort();
	        byte[] passwordBytes = new byte[passwordSize];
	        raf.readFully(passwordBytes);

	        String senhaCripto = new String(passwordBytes, "UTF-8");
	        String password = Crypto.xor(senhaCripto);

	        boolean admin = raf.readBoolean();

	        System.out.println(
	            id + " | " + username + " | " + email + " | " + password + " | admin: " + admin
	        );

	        raf.seek(pos + 4 + recordSize);
	    }

	    raf.close();
	}
	
	public User readByEmail(String emailBusca) throws IOException {

	    RandomAccessFile raf = FileManeger.open(FILE);

	    raf.seek(4); // pula header

	    while (raf.getFilePointer() < raf.length()) {

	        long pos = raf.getFilePointer();

	        int recordSize = raf.readInt();
	        boolean active = raf.readBoolean();

	        if(!active){
	            raf.seek(pos + 4 + recordSize);
	            continue;
	        }

	        int id = raf.readInt();

	        short nameSize = raf.readShort();
	        byte[] nameBytes = new byte[nameSize];
	        raf.readFully(nameBytes);
	        String username = new String(nameBytes, "UTF-8");

	        short emailSize = raf.readShort();
	        byte[] emailBytes = new byte[emailSize];
	        raf.readFully(emailBytes);
	        String email = new String(emailBytes, "UTF-8");

	        short passwordSize = raf.readShort();
	        byte[] passwordBytes = new byte[passwordSize];
	        raf.readFully(passwordBytes);

	        String senhaCripto = new String(passwordBytes, "UTF-8");
	        String password = Crypto.xor(senhaCripto);

	        boolean admin = raf.readBoolean();

	        if(email.equals(emailBusca)) {

	            raf.close();
	            return new User(id, username, email, password, admin);
	        }

	        raf.seek(pos + 4 + recordSize);
	    }

	    raf.close();
	    return null;
	}

}


package CRUD.index;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class IndexEntry {

    private int id;
    private long endereco;

    public IndexEntry(int id, long endereco) {
        this.id = id;
        this.endereco = endereco;
    }

    public IndexEntry() {}

    public int getId() {
        return id;
    }

    public long getEndereco() {
        return endereco;
    }

    public void setEndereco(long endereco) {
        this.endereco = endereco;
    }
    
    public void write(DataOutput out) throws IOException {
        out.writeInt(id);
        out.writeLong(endereco);
    }

    public void read(DataInput in) throws IOException {
        id = in.readInt();
        endereco = in.readLong();
    }
}
package main.java.CRUD.index;

public class IndexEntry {

    private int id;
    private long endereco;

    public IndexEntry(int id, long endereco) {
        this.id = id;
        this.endereco = endereco;
    }

    public int getId() {
        return id;
    }

    public long getEndereco() {
        return endereco;
    }

    public void setEndereco(long endereco) {
        this.endereco = endereco;
    }
}
package CRUD.index;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Bucket {

    private int profundidadeLocal;
    private int capacidade;
    private int quantidade;
    private IndexEntry[] entradas;

    public Bucket(int profundidadeLocal, int capacidade) {
        this.profundidadeLocal = profundidadeLocal;
        this.capacidade = capacidade;
        this.quantidade = 0;
        this.entradas = new IndexEntry[capacidade];
    }

    // =============================
    // LÓGICA NORMAL
    // =============================

    public boolean estaCheio() {
        return quantidade >= capacidade;
    }

    public int getProfundidadeLocal() {
        return profundidadeLocal;
    }

    public void setProfundidadeLocal(int profundidadeLocal) {
        this.profundidadeLocal = profundidadeLocal;
    }

    public int getCapacidade() {
        return capacidade;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public IndexEntry buscar(int id) {
        for (int i = 0; i < quantidade; i++) {
            if (entradas[i].getId() == id) {
                return entradas[i];
            }
        }
        return null;
    }

    public boolean inserir(IndexEntry e) {
        if (estaCheio()) return false;

        entradas[quantidade++] = e;
        return true;
    }

    public boolean remover(int id) {
        for (int i = 0; i < quantidade; i++) {
            if (entradas[i].getId() == id) {
                entradas[i] = entradas[quantidade - 1];
                entradas[quantidade - 1] = null;
                quantidade--;
                return true;
            }
        }
        return false;
    }

    public void limpar() {
        for (int i = 0; i < quantidade; i++) {
            entradas[i] = null;
        }
        quantidade = 0;
    }

    // ❗ NÃO use mais getEntradas direto no split
    public IndexEntry[] getEntradas() {
        return entradas;
    }

    // ✅ NOVO: retorna só entradas válidas
    public List<IndexEntry> getEntradasValidas() {
        List<IndexEntry> lista = new ArrayList<>();
        for (int i = 0; i < quantidade; i++) {
            lista.add(entradas[i]);
        }
        return lista;
    }

    // =============================
    // PERSISTÊNCIA (BINÁRIO)
    // =============================

    public void write(DataOutput out) throws IOException {
        out.writeInt(profundidadeLocal);
        out.writeInt(quantidade);

        for (int i = 0; i < capacidade; i++) {
            if (i < quantidade) {
                out.writeBoolean(true);
                out.writeInt(entradas[i].getId());
                out.writeLong(entradas[i].getEndereco());
            } else {
                out.writeBoolean(false);
                out.writeInt(0);
                out.writeLong(0);
            }
        }
    }

    public void read(DataInput in) throws IOException {
        profundidadeLocal = in.readInt();
        quantidade = in.readInt();

        entradas = new IndexEntry[capacidade];

        for (int i = 0; i < capacidade; i++) {
            boolean existe = in.readBoolean();
            int id = in.readInt();
            long endereco = in.readLong();

            if (existe) {
                entradas[i] = new IndexEntry(id, endereco);
            }
        }
    }

    // =============================
    // TAMANHO FIXO (CRUCIAL)
    // =============================

    public int tamanhoEmBytes() {
        return 4
             + 4
             + capacidade * (1 + 4 + 8);
    }
}
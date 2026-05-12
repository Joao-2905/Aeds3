package main.java.CRUD.index;

import java.util.ArrayList;
import java.util.List;

public class HashExtensivel {

    private int profundidadeGlobal;
    private int capacidadeBucket;
    private List<Bucket> diretorio;

    public HashExtensivel(int capacidadeBucket) {
        if (capacidadeBucket <= 0) {
            throw new IllegalArgumentException("A capacidade do bucket deve ser maior que zero.");
        }

        this.profundidadeGlobal = 1;
        this.capacidadeBucket = capacidadeBucket;
        this.diretorio = new ArrayList<>();

        Bucket b0 = new Bucket(1, capacidadeBucket);
        Bucket b1 = new Bucket(1, capacidadeBucket);

        diretorio.add(b0);
        diretorio.add(b1);
    }

    private int hash(int chave) {
        return chave;
    }

    private int indiceDiretorio(int chave) {
        int mascara = (1 << profundidadeGlobal) - 1;
        return hash(chave) & mascara;
    }

    public void inserir(int id, long endereco) {
        inserirInterno(new IndexEntry(id, endereco));
    }

    private void inserirInterno(IndexEntry novaEntrada) {
        int indice = indiceDiretorio(novaEntrada.getId());
        Bucket bucket = diretorio.get(indice);

        IndexEntry existente = bucket.buscar(novaEntrada.getId());
        if (existente != null) {
            existente.setEndereco(novaEntrada.getEndereco());
            return;
        }

        if (!bucket.estaCheio()) {
            bucket.getEntradas().add(novaEntrada);
            return;
        }

        dividirBucket(indice);
        inserirInterno(novaEntrada);
    }

    public Long buscar(int id) {
        int indice = indiceDiretorio(id);
        IndexEntry e = diretorio.get(indice).buscar(id);
        return (e == null) ? null : e.getEndereco();
    }

    public boolean remover(int id) {
        int indice = indiceDiretorio(id);
        return diretorio.get(indice).remover(id);
    }

    private void duplicarDiretorio() {
        int tamanhoAtual = diretorio.size();

        for (int i = 0; i < tamanhoAtual; i++) {
            diretorio.add(diretorio.get(i));
        }

        profundidadeGlobal++;
    }

    private void dividirBucket(int indice) {
        Bucket bucketAntigo = diretorio.get(indice);
        int profundidadeLocalAntiga = bucketAntigo.getProfundidadeLocal();

        if (profundidadeLocalAntiga == profundidadeGlobal) {
            duplicarDiretorio();
        }

        Bucket novoBucket = new Bucket(profundidadeLocalAntiga + 1, capacidadeBucket);
        bucketAntigo.setProfundidadeLocal(profundidadeLocalAntiga + 1);

        int bitDiferenciador = 1 << profundidadeLocalAntiga;

        for (int i = 0; i < diretorio.size(); i++) {
            if (diretorio.get(i) == bucketAntigo && (i & bitDiferenciador) != 0) {
                diretorio.set(i, novoBucket);
            }
        }

        List<IndexEntry> antigas = new ArrayList<>(bucketAntigo.getEntradas());
        bucketAntigo.limpar();

        for (IndexEntry entrada : antigas) {
            inserirInterno(entrada);
        }
    }

    public void exibir(String tipo) {
        System.out.println("\n=== HASH EXTENSÍVEL ===");
        System.out.println("Profundidade global: " + profundidadeGlobal);

        for (int i = 0; i < diretorio.size(); i++) {
            Bucket b = diretorio.get(i);
            System.out.print("Dir[" + i + "] -> PL=" + b.getProfundidadeLocal() + " | ");

            for (IndexEntry e : b.getEntradas()) {
                System.out.print("(Id do " + tipo + ": " + e.getId()
                        + " -> posição no arquivo (byte offset): " + e.getEndereco() + ") ");
            }

            System.out.println();
        }
    }

    public List<Bucket> getDiretorio() {
        return diretorio;
    }

    public int getProfundidadeGlobal() {
        return profundidadeGlobal;
    }

    public int getCapacidadeBucket() {
        return capacidadeBucket;
    }
}
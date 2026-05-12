package com.filmes.crud.index;

import com.filmes.crud.dao.HashDAO;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HashExtensivel {

    private int profundidadeGlobal;
    private int capacidadeBucket;
    private List<Bucket> diretorio;

    private List<Long> diretorioPosicoes;
    private HashDAO hashDAO;
    private boolean persistente;

    public HashExtensivel(int capacidadeBucket) {
        if (capacidadeBucket <= 0) {
            throw new IllegalArgumentException("A capacidade do bucket deve ser maior que zero.");
        }

        this.profundidadeGlobal = 1;
        this.capacidadeBucket = capacidadeBucket;
        this.diretorio = new ArrayList<>();
        this.diretorioPosicoes = new ArrayList<>();
        this.persistente = false;

        Bucket b0 = new Bucket(1, capacidadeBucket);
        Bucket b1 = new Bucket(1, capacidadeBucket);

        diretorio.add(b0);
        diretorio.add(b1);
    }

    public HashExtensivel(int capacidadeBucket, String nomeHash) {
        if (capacidadeBucket <= 0) {
            throw new IllegalArgumentException("A capacidade do bucket deve ser maior que zero.");
        }

        this.profundidadeGlobal = 1;
        this.capacidadeBucket = capacidadeBucket;
        this.diretorio = new ArrayList<>();
        this.diretorioPosicoes = new ArrayList<>();
        this.hashDAO = new HashDAO(capacidadeBucket, nomeHash);
        this.persistente = true;

        try {
            if (hashDAO.existeHash()) {
                hashDAO.carregarDiretorio(this);
            } else {
                Bucket b0 = new Bucket(1, capacidadeBucket);
                Bucket b1 = new Bucket(1, capacidadeBucket);

                long pos0 = hashDAO.criarBucket(b0);
                long pos1 = hashDAO.criarBucket(b1);

                diretorio.add(b0);
                diretorio.add(b1);

                diretorioPosicoes.add(pos0);
                diretorioPosicoes.add(pos1);

                salvarDiretorioEmDisco();
            }
        } catch (IOException e) {
            throw new RuntimeException("Erro ao inicializar hash extensível persistente.", e);
        }
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
        salvarDiretorioEmDisco();
    }

    private void inserirInterno(IndexEntry novaEntrada) {
        int indice = indiceDiretorio(novaEntrada.getId());
        Bucket bucket = diretorio.get(indice);

        IndexEntry existente = bucket.buscar(novaEntrada.getId());
        if (existente != null) {
            existente.setEndereco(novaEntrada.getEndereco());
            salvarBucketEmDisco(indice);
            return;
        }

        if (!bucket.estaCheio()) {
            bucket.getEntradas().add(novaEntrada);
            salvarBucketEmDisco(indice);
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
        boolean removeu = diretorio.get(indice).remover(id);

        if (removeu) {
            salvarBucketEmDisco(indice);
            salvarDiretorioEmDisco();
        }

        return removeu;
    }

    private void duplicarDiretorio() {
        int tamanhoAtual = diretorio.size();

        for (int i = 0; i < tamanhoAtual; i++) {
            diretorio.add(diretorio.get(i));

            if (persistente) {
                diretorioPosicoes.add(diretorioPosicoes.get(i));
            }
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

        long novaPosicao = -1;

        if (persistente) {
            try {
                novaPosicao = hashDAO.criarBucket(novoBucket);
            } catch (IOException e) {
                throw new RuntimeException("Erro ao criar novo bucket no arquivo.", e);
            }
        }

        int bitDiferenciador = 1 << profundidadeLocalAntiga;

        for (int i = 0; i < diretorio.size(); i++) {
            if (diretorio.get(i) == bucketAntigo && (i & bitDiferenciador) != 0) {
                diretorio.set(i, novoBucket);

                if (persistente) {
                    diretorioPosicoes.set(i, novaPosicao);
                }
            }
        }

        List<IndexEntry> antigas = new ArrayList<>(bucketAntigo.getEntradas());
        bucketAntigo.limpar();

        salvarBucketsUnicosEmDisco();

        for (IndexEntry entrada : antigas) {
            inserirInterno(entrada);
        }

        salvarDiretorioEmDisco();
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

    private void salvarBucketEmDisco(int indice) {
        if (!persistente) {
            return;
        }

        try {
            hashDAO.salvarBucket(diretorio.get(indice), diretorioPosicoes.get(indice));
        } catch (IOException e) {
            throw new RuntimeException("Erro ao salvar bucket em disco.", e);
        }
    }

    private void salvarBucketsUnicosEmDisco() {
        if (!persistente) {
            return;
        }

        try {
            List<Long> posicoesSalvas = new ArrayList<>();

            for (int i = 0; i < diretorio.size(); i++) {
                long pos = diretorioPosicoes.get(i);

                if (!posicoesSalvas.contains(pos)) {
                    hashDAO.salvarBucket(diretorio.get(i), pos);
                    posicoesSalvas.add(pos);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Erro ao salvar buckets únicos em disco.", e);
        }
    }

    private void salvarDiretorioEmDisco() {
        if (!persistente) {
            return;
        }

        try {
            hashDAO.salvarDiretorio(profundidadeGlobal, diretorioPosicoes);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao salvar diretório do hash em disco.", e);
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

    public void setProfundidadeGlobal(int profundidadeGlobal) {
        this.profundidadeGlobal = profundidadeGlobal;
    }

    public void setCapacidadeBucket(int capacidadeBucket) {
        this.capacidadeBucket = capacidadeBucket;
    }

    public void setDiretorio(List<Long> diretorioPosicoes) {
        this.diretorioPosicoes = diretorioPosicoes;
        this.diretorio = new ArrayList<>();

        if (!persistente || hashDAO == null) {
            return;
        }

        try {
            Map<Long, Bucket> bucketsCarregados = new HashMap<>();

            for (Long pos : diretorioPosicoes) {
                Bucket bucket = bucketsCarregados.get(pos);

                if (bucket == null) {
                    bucket = hashDAO.lerBucket(pos);
                    bucketsCarregados.put(pos, bucket);
                }

                diretorio.add(bucket);
            }
        } catch (IOException e) {
            throw new RuntimeException("Erro ao carregar buckets do hash em disco.", e);
        }
    }
}
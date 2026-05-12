package com.filmes.crud.dao;

import com.filmes.crud.index.Bucket;
import com.filmes.crud.index.HashExtensivel;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class HashDAO {

    private String pathDiretorio;
    private String pathBuckets;
    private int capacidadeBucket;

    // =============================
    // CONSTRUTOR (COM NOME)
    // =============================
    public HashDAO(int capacidadeBucket, String nome) {
        this.capacidadeBucket = capacidadeBucket;
        this.pathDiretorio = "data/" + nome + "_diretorio.bin";
        this.pathBuckets = "data/" + nome + "_buckets.bin";
    }

    // =============================
    // GARANTE QUE PASTA EXISTE
    // =============================
    private void garantirPasta(String path) {
        File file = new File(path);
        if (file.getParentFile() != null) {
            file.getParentFile().mkdirs();
        }
    }

    // =============================
    // VERIFICA SE JÁ EXISTE HASH
    // =============================
    public boolean existeHash() {
        File dir = new File(pathDiretorio);
        File buckets = new File(pathBuckets);

        return dir.exists() && dir.length() > 0 &&
               buckets.exists() && buckets.length() > 0;
    }

    // =============================
    // CRIAR BUCKET
    // =============================
    public long criarBucket(Bucket b) throws IOException {

        garantirPasta(pathBuckets);

        RandomAccessFile file = new RandomAccessFile(pathBuckets, "rw");

        long pos = file.length();
        file.seek(pos);

        b.write(file);

        file.close();
        return pos;
    }

    // =============================
    // LER BUCKET
    // =============================
    public Bucket lerBucket(long pos) throws IOException {

        garantirPasta(pathBuckets);

        RandomAccessFile file = new RandomAccessFile(pathBuckets, "r");

        file.seek(pos);
        Bucket b = new Bucket(0, capacidadeBucket);
        b.read(file);

        file.close();
        return b;
    }

    // =============================
    // SALVAR BUCKET
    // =============================
    public void salvarBucket(Bucket b, long pos) throws IOException {

        garantirPasta(pathBuckets);

        RandomAccessFile file = new RandomAccessFile(pathBuckets, "rw");

        file.seek(pos);
        b.write(file);

        file.close();
    }

    // =============================
    // SALVAR DIRETÓRIO
    // =============================
    public void salvarDiretorio(int profundidadeGlobal, List<Long> diretorio) throws IOException {

        garantirPasta(pathDiretorio);

        DataOutputStream out = new DataOutputStream(
                new FileOutputStream(pathDiretorio)
        );

        out.writeInt(profundidadeGlobal);
        out.writeInt(capacidadeBucket);
        out.writeInt(diretorio.size());

        for (Long p : diretorio) {
            out.writeLong(p);
        }

        out.close();
    }

    // =============================
    // CARREGAR DIRETÓRIO
    // =============================
    public void carregarDiretorio(HashExtensivel hash) throws IOException {

        garantirPasta(pathDiretorio);

        File file = new File(pathDiretorio);

        if (!file.exists() || file.length() == 0) {
            return;
        }

        DataInputStream in = new DataInputStream(
                new FileInputStream(file)
        );

        int profundidadeGlobal = in.readInt();
        int capacidade = in.readInt();
        int tamanho = in.readInt();

        List<Long> diretorio = new ArrayList<>();

        for (int i = 0; i < tamanho; i++) {
            diretorio.add(in.readLong());
        }

        this.capacidadeBucket = capacidade;

        hash.setProfundidadeGlobal(profundidadeGlobal);
        hash.setDiretorio(diretorio);
        hash.setCapacidadeBucket(capacidade);

        in.close();
    }
}

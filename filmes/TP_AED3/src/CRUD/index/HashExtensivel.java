package CRUD.index;

import CRUD.dao.HashDAO;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HashExtensivel {

    private int profundidadeGlobal;
    private int capacidadeBucket;
    private List<Long> diretorio;

    private HashDAO dao;

    // =============================
    // CONSTRUTOR (NOVO COM NOME)
    // =============================
    public HashExtensivel(int capacidadeBucket, String nome) throws IOException {

        if (capacidadeBucket <= 0) {
            throw new IllegalArgumentException("Capacidade inválida");
        }

        this.capacidadeBucket = capacidadeBucket;
        this.dao = new HashDAO(capacidadeBucket, nome); // 🔥 AQUI MUDA
        this.diretorio = new ArrayList<>();

        if (dao.existeHash()) {
            dao.carregarDiretorio(this);
        } else {
            inicializar();
        }
    }

    // =============================
    // INICIALIZAÇÃO
    // =============================
    private void inicializar() throws IOException {

        profundidadeGlobal = 1;

        Bucket b0 = new Bucket(1, capacidadeBucket);
        Bucket b1 = new Bucket(1, capacidadeBucket);

        long p0 = dao.criarBucket(b0);
        long p1 = dao.criarBucket(b1);

        diretorio.add(p0);
        diretorio.add(p1);

        dao.salvarDiretorio(profundidadeGlobal, diretorio);
    }

    // =============================
    // HASH
    // =============================
    private int hash(int chave) {
        return chave;
    }

    private int indiceDiretorio(int chave) {
        int mascara = (1 << profundidadeGlobal) - 1;
        return hash(chave) & mascara;
    }

    // =============================
    // INSERT
    // =============================
    public void inserir(int id, long endereco) throws IOException {
        inserirInterno(new IndexEntry(id, endereco));
    }

    private void inserirInterno(IndexEntry entrada) throws IOException {

        int indice = indiceDiretorio(entrada.getId());
        long posBucket = diretorio.get(indice);

        Bucket bucket = dao.lerBucket(posBucket);

        // já existe → update
        IndexEntry existente = bucket.buscar(entrada.getId());
        if (existente != null) {
            existente.setEndereco(entrada.getEndereco());
            dao.salvarBucket(bucket, posBucket);
            return;
        }

        // espaço disponível
        if (!bucket.estaCheio()) {
            bucket.inserir(entrada);
            dao.salvarBucket(bucket, posBucket);
            return;
        }

        // precisa dividir
        dividirBucket(indice);
        inserirInterno(entrada);
    }

    // =============================
    // BUSCAR
    // =============================
    public Long buscar(int id) throws IOException {

        int indice = indiceDiretorio(id);
        long pos = diretorio.get(indice);

        Bucket b = dao.lerBucket(pos);

        IndexEntry e = b.buscar(id);
        return (e == null) ? null : e.getEndereco();
    }

    // =============================
    // REMOVER
    // =============================
    public boolean remover(int id) throws IOException {

        int indice = indiceDiretorio(id);
        long pos = diretorio.get(indice);

        Bucket b = dao.lerBucket(pos);

        boolean removido = b.remover(id);

        if (removido) {
            dao.salvarBucket(b, pos);
        }

        return removido;
    }

    // =============================
    // DUPLICAR DIRETÓRIO
    // =============================
    private void duplicarDiretorio() {

        int tamanho = diretorio.size();

        for (int i = 0; i < tamanho; i++) {
            diretorio.add(diretorio.get(i));
        }

        profundidadeGlobal++;
    }

    // =============================
    // DIVIDIR BUCKET
    // =============================
    private void dividirBucket(int indice) throws IOException {

        long posAntigo = diretorio.get(indice);
        Bucket bucketAntigo = dao.lerBucket(posAntigo);

        int pl = bucketAntigo.getProfundidadeLocal();

        if (pl == profundidadeGlobal) {
            duplicarDiretorio();
        }

        // novo bucket
        Bucket novoBucket = new Bucket(pl + 1, capacidadeBucket);
        long posNovo = dao.criarBucket(novoBucket);

        // atualiza profundidade
        bucketAntigo.setProfundidadeLocal(pl + 1);

        int bit = 1 << pl;

        // atualiza diretório
        for (int i = 0; i < diretorio.size(); i++) {
            if (diretorio.get(i).equals(posAntigo) && (i & bit) != 0) {
                diretorio.set(i, posNovo);
            }
        }

        // copiar antes de limpar
        List<IndexEntry> antigas = new ArrayList<>();
        IndexEntry[] entradas = bucketAntigo.getEntradas();

        for (int i = 0; i < bucketAntigo.getQuantidade(); i++) {
            antigas.add(entradas[i]);
        }

        // limpar bucket antigo
        bucketAntigo.limpar();
        dao.salvarBucket(bucketAntigo, posAntigo);

        // reinserir
        for (IndexEntry e : antigas) {
            inserirInterno(e);
        }

        dao.salvarDiretorio(profundidadeGlobal, diretorio);
    }

    // =============================
    // GETTERS / SETTERS
    // =============================
    public void setProfundidadeGlobal(int pg) {
        this.profundidadeGlobal = pg;
    }

    public void setDiretorio(List<Long> dir) {
        this.diretorio = dir;
    }

    public void setCapacidadeBucket(int cap) {
        this.capacidadeBucket = cap;
    }

    // =============================
    // DEBUG
    // =============================
    public void exibir(String tipo) throws IOException {

        System.out.println("\n=== HASH EXTENSÍVEL ===");
        System.out.println("Profundidade global: " + profundidadeGlobal);

        for (int i = 0; i < diretorio.size(); i++) {

            long pos = diretorio.get(i);
            Bucket b = dao.lerBucket(pos);

            System.out.print("Dir[" + i + "] -> PL=" + b.getProfundidadeLocal() + " | ");

            IndexEntry[] entradas = b.getEntradas();

            for (int j = 0; j < b.getQuantidade(); j++) {
                IndexEntry e = entradas[j];

                System.out.print("(Id " + tipo + ": " + e.getId() +
                        " -> pos: " + e.getEndereco() + ") ");
            }

            System.out.println();
        }
    }
}
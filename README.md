# Aeds3 🎬
Sistema para avaliação de filmes

---

## ▶️ Como executar (sem Eclipse)

1. Baixe o arquivo `filmes.jar`  
   > Recomenda-se salvar em uma **pasta vazia**

2. Abra o **Prompt de Comando (cmd)** na pasta onde o arquivo foi salvo  
   - Dica: clique na barra de endereço da pasta, digite `cmd` e pressione Enter

3. Execute o comando:

    java -jar filmes.jar

---

## ⚠️ Observação importante

O sistema cria automaticamente uma pasta chamada `data` no local onde o `.jar` for executado.

- Essa pasta conterá os arquivos `.bin` utilizados pelo sistema
- Exemplo:
  - Se o `.jar` estiver na Área de Trabalho → a pasta `data` será criada lá

👉 Por isso, é recomendado executar o programa em uma **pasta dedicada/vazia**

---

## 👤 Tipos de usuários

O sistema possui dois tipos de usuários:

### 👥 Usuário
- Acesso à própria conta
- Criação de avaliações *(funcionalidade ainda não disponível)*

### 🛠️ Administrador
- Possui todas as permissões de um usuário
- Acesso completo aos CRUDs:
  - Usuários
  - Gêneros
  - Filmes

---

## 🔐 Controle de administradores

- Apenas um **administrador** pode promover outro usuário a administrador
- Para evitar problemas, o sistema já vem com um **administrador padrão**

👉 As credenciais desse administrador são exibidas automaticamente na **primeira execução do sistema**

# Aeds3 🎬
Sistema para avaliação de filmes

---

## ▶️ Como executar

1. Baixe o arquivo `Tp_fase3.zip`  
   > Pode extrair logo em seguida

2. Abra o **Prompt de Comando (cmd)** na pasta onde o arquivo foi salvo  
   - Dica: clique na barra de endereço da pasta, digite `cmd` e pressione Enter

3. Execute o comando:
    'cd jar'
   Em seguida:
    'java -jar crud-0.0.1-SNAPSHOT.jar'

4. Abra o navegador:
    'http://localhost:8080'
---

## ⚠️ Observação importante

Se tiver problemas ao executar o site, use '.\mvnw.cmd clean package' antes de 'java -jar crud-0.0.1-SNAPSHOT.jar'.

O sistema atualmente possui duas pastas data, a da raíz está em processo de modificação. Tudo feito no site atualizara a pasta 'data' dentro da pasta 'target'.

---

## 👤 Tipos de usuários

O sistema possui dois tipos de usuários:

### 👥 Usuário
- Acesso à própria conta
- Criação de avaliações
- Visualização de avaliações
- Possibilidade de ordenar por meio da ordenação externa

### 🛠️ Administrador
- Possui todas as permissões de um usuário (menos uso da ordenação externa)
- Acesso completo aos CRUDs:
  - Usuários
  - Gêneros
  - Filmes
  - Avaliações
- Possibilidade de ordenar por meio da Árvore B+

---

## 🔐 Controle de administradores

- Apenas um **administrador** pode promover outro usuário a administrador
- Para evitar problemas, o sistema já vem com um **administrador padrão**

👉 As credenciais desse administrador são exibidas automaticamente na **primeira execução do sistema**

- email: admin@admin.com
- senha: 123

package CRUD.controller;

import CRUD.dao.UserDAO;
import CRUD.model.User;

public class UserController {

    private UserDAO dao;

    public UserController() throws Exception {
        dao = new UserDAO();
    }

    // criar usuário
    public int createUser(String username, String email, String senha, boolean admin) throws Exception {

        if (username == null || username.trim().isEmpty()) {
            System.out.println("Username inválido.");
            return -1;
        }

        if (email == null || email.trim().isEmpty()) {
            System.out.println("Email inválido.");
            return -1;
        }

        if (senha == null || senha.trim().isEmpty()) {
            System.out.println("Senha inválida.");
            return -1;
        }

        if (dao.readByEmail(email) != null) {
            System.out.println("Email já cadastrado.");
            return -1;
        }

        User user = new User(username, email, senha, admin);

        return dao.create(user);
    }

    // login
    public User login(String email, String senha) throws Exception {

        if (email == null || email.trim().isEmpty()) {
            return null;
        }

        if (senha == null || senha.trim().isEmpty()) {
            return null;
        }

        User user = dao.readByEmail(email);

        if (user != null && user.getPassword().equals(senha)) {
            return user;
        }

        return null;
    }

    // deletar usuário
    public boolean deleteUser(int id) throws Exception {

        if (id <= 0) {
            System.out.println("ID inválido.");
            return false;
        }

        User user = dao.read(id);

        if (user == null) {
            System.out.println("Usuário não encontrado.");
            return false;
        }

        if (user.isAdministrator()) {
            System.out.println("Não é permitido remover um administrador.");
            return false;
        }

        return dao.delete(id);
    }

    // listar usuários
    public void listUsers() throws Exception {
        dao.listAll();
    }

    // promover para admin
    public boolean promoteAdmin(int id) throws Exception {

        if (id <= 0) {
            System.out.println("ID inválido.");
            return false;
        }

        User user = dao.read(id);

        if (user == null) {
            System.out.println("Usuário não encontrado.");
            return false;
        }

        user.setAdministrator(true);

        return dao.update(user);
    }

    // remover permissão admin
    public boolean removeAdmin(int targetID, User adminLogado) throws Exception {

        if (targetID <= 0) {
            System.out.println("ID inválido.");
            return false;
        }

        if (targetID == adminLogado.getID()) {
            System.out.println("Você não pode remover sua própria permissão de administrador.");
            return false;
        }

        User user = dao.read(targetID);

        if (user == null) {
            System.out.println("Usuário não encontrado.");
            return false;
        }

        user.setAdministrator(false);

        return dao.update(user);
    }

    // trocar própria senha
    public boolean changePassword(User usuario, String novaSenha) throws Exception {

        if (usuario == null) {
            System.out.println("Usuário inválido.");
            return false;
        }

        if (novaSenha == null || novaSenha.trim().isEmpty()) {
            System.out.println("Nova senha inválida.");
            return false;
        }

        usuario.setPassword(novaSenha);

        return dao.update(usuario);
    }

    // atualizar usuário
    public boolean updateUser(int id, String username, String email) throws Exception {

        if (id <= 0) {
            System.out.println("ID inválido.");
            return false;
        }

        User user = dao.read(id);

        if (user == null) {
            System.out.println("Usuário não encontrado.");
            return false;
        }

        if (username == null || username.trim().isEmpty()) {
            System.out.println("Username inválido.");
            return false;
        }

        if (email == null || email.trim().isEmpty()) {
            System.out.println("Email inválido.");
            return false;
        }

        User existente = dao.readByEmail(email);
        if (existente != null && existente.getID() != id) {
            System.out.println("Email já cadastrado para outro usuário.");
            return false;
        }

        user.setUsername(username);
        user.setEmail(email);

        return dao.update(user);
    }

    // exibir hash extensível
    public void exibirIndice() throws Exception {
        dao.exibirIndice();
    }
}
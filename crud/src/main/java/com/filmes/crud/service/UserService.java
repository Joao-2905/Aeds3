package com.filmes.crud.service;

import java.util.List;

import com.filmes.crud.dao.UserDAO;
import com.filmes.crud.dao.ReviewDAO;
import com.filmes.crud.model.User;

public class UserService {

    private final UserDAO dao;

    public UserService() throws Exception {
        dao = new UserDAO();
    }

    public int createUser(String username, String email, String senha, boolean admin) throws Exception {

        username = normalizarUsername(username);
        email = normalizarEmail(email);
        senha = normalizarSenha(senha);

        validarUsername(username);
        validarEmail(email);
        validarSenha(senha);

        if (dao.readByEmail(email) != null) {
            throw new Exception("Email já cadastrado.");
        }

        if (dao.readByUsername(username) != null) {
            throw new Exception("Username já cadastrado.");
        }

        User user = new User(username, email, senha, admin);

        return dao.create(user);
    }

    public User login(String email, String senha) throws Exception {

        email = normalizarEmail(email);
        senha = normalizarSenha(senha);

        validarEmail(email);
        validarSenha(senha);

        User user = dao.readByEmail(email);

        if (user != null && user.getPassword().equals(senha)) {
            return user;
        }

        return null;
    }

    public User readUser(int id) throws Exception {

        if (id <= 0) {
            throw new Exception("ID inválido.");
        }

        return dao.read(id);
    }

    public List<User> listUsersToList() throws Exception {
        return dao.readAllToList();
    }

    public void listUsers() throws Exception {
        dao.listAll();
    }

    public boolean deleteUser(int id) throws Exception {

        if (id <= 0) {
            throw new Exception("ID inválido.");
        }

        User user = dao.read(id);

        if (user == null) {
            throw new Exception("Usuário não encontrado.");
        }

        if (user.isAdministrator()) {

            int totalAdmins = dao.countAdmins();

            if (totalAdmins <= 1) {
                throw new Exception("O sistema precisa ter pelo menos 1 administrador.");
            }
        }

        ReviewDAO reviewDAO = new ReviewDAO();
        reviewDAO.deleteByUserID(id);

        return dao.delete(id);
    }

    public boolean promoteAdmin(int id) throws Exception {

        if (id <= 0) {
            throw new Exception("ID inválido.");
        }

        User user = dao.read(id);

        if (user == null) {
            throw new Exception("Usuário não encontrado.");
        }

        if (user.isAdministrator()) {
            return true;
        }

        user.setAdministrator(true);

        return dao.update(user);
    }

    public boolean removeAdmin(int targetID, User adminLogado) throws Exception {

        if (targetID <= 0) {
            throw new Exception("ID inválido.");
        }

        if (adminLogado == null) {
            throw new Exception("Administrador logado inválido.");
        }

        User user = dao.read(targetID);

        if (user == null) {
            throw new Exception("Usuário não encontrado.");
        }

        if (!user.isAdministrator()) {
            throw new Exception("O usuário informado não é administrador.");
        }

        int totalAdmins = dao.countAdmins();

        if (totalAdmins <= 1) {
            throw new Exception("O sistema precisa ter pelo menos 1 administrador.");
        }

        user.setAdministrator(false);

        return dao.update(user);
    }

    public boolean changePassword(User usuario, String novaSenha) throws Exception {

        if (usuario == null) {
            throw new Exception("Usuário inválido.");
        }

        novaSenha = normalizarSenha(novaSenha);

        validarSenha(novaSenha);

        if (usuario.getPassword().equals(novaSenha)) {
            throw new Exception("A nova senha não pode ser igual à senha anterior.");
        }

        usuario.setPassword(novaSenha);

        return dao.update(usuario);
    }

    public boolean updateUser(int id, String username, String email) throws Exception {

        if (id <= 0) {
            throw new Exception("ID inválido.");
        }

        User user = dao.read(id);

        if (user == null) {
            throw new Exception("Usuário não encontrado.");
        }

        username = normalizarUsername(username);
        email = normalizarEmail(email);

        validarUsername(username);
        validarEmail(email);

        User existente = dao.readByEmail(email);

        if (existente != null && existente.getID() != id) {
            throw new Exception("Email já cadastrado para outro usuário.");
        }

        User existenteUsername = dao.readByUsername(username);

        if (existenteUsername != null && existenteUsername.getID() != id) {
            throw new Exception("Username já cadastrado para outro usuário.");
        }

        user.setUsername(username);
        user.setEmail(email);

        return dao.update(user);
    }

    public boolean updateUserComplete(int id, String username, String email, String senha, boolean administrator) throws Exception {

        if (id <= 0) {
            throw new Exception("ID inválido.");
        }

        User user = dao.read(id);

        if (user == null) {
            throw new Exception("Usuário não encontrado.");
        }

        username = normalizarUsername(username);
        email = normalizarEmail(email);

        validarUsername(username);
        validarEmail(email);

        User existente = dao.readByEmail(email);

        if (existente != null && existente.getID() != id) {
            throw new Exception("Email já cadastrado para outro usuário.");
        }

        User existenteUsername = dao.readByUsername(username);

        if (existenteUsername != null && existenteUsername.getID() != id) {
            throw new Exception("Username já cadastrado para outro usuário.");
        }

        if (user.isAdministrator() && !administrator) {

            int totalAdmins = dao.countAdmins();

            if (totalAdmins <= 1) {
                throw new Exception("O sistema precisa ter pelo menos 1 administrador.");
            }
        }

        user.setUsername(username);
        user.setEmail(email);

        if (senha != null && !senha.trim().isEmpty()) {

            senha = normalizarSenha(senha);

            validarSenha(senha);

            if (user.getPassword().equals(senha)) {
                throw new Exception("A nova senha não pode ser igual à senha anterior.");
            }

            user.setPassword(senha);
        }

        user.setAdministrator(administrator);

        return dao.update(user);
    }

    public boolean updateOwnProfile(int id, String email, String senha) throws Exception {

        if (id <= 0) {
            throw new Exception("ID inválido.");
        }

        User user = dao.read(id);

        if (user == null) {
            throw new Exception("Usuário não encontrado.");
        }

        email = normalizarEmail(email);
        senha = normalizarSenha(senha);

        validarEmail(email);
        validarSenha(senha);

        User existente = dao.readByEmail(email);

        if (existente != null && existente.getID() != id) {
            throw new Exception("Email já cadastrado para outro usuário.");
        }

        if (user.getPassword().equals(senha)) {
            throw new Exception("A nova senha não pode ser igual à senha anterior.");
        }

        user.setEmail(email);
        user.setPassword(senha);

        return dao.update(user);
    }

    public boolean deleteOwnProfile(int id) throws Exception {

        if (id <= 0) {
            throw new Exception("ID inválido.");
        }

        User user = dao.read(id);

        if (user == null) {
            throw new Exception("Usuário não encontrado.");
        }

        if (user.isAdministrator()) {

            int totalAdmins = dao.countAdmins();

            if (totalAdmins <= 1) {
                throw new Exception("O sistema precisa ter pelo menos 1 administrador.");
            }
        }

        ReviewDAO reviewDAO = new ReviewDAO();
        reviewDAO.deleteByUserID(id);

        return dao.delete(id);
    }

    public void exibirIndice() throws Exception {
        dao.exibirIndice();
    }

    private void validarUsername(String username) throws Exception {

        if (username == null || username.trim().isEmpty()) {
            throw new Exception("Username inválido.");
        }
    }

    private void validarEmail(String email) throws Exception {

        if (email == null || email.trim().isEmpty()) {
            throw new Exception("Email inválido.");
        }

        if (!email.contains("@")) {
            throw new Exception("Email inválido.");
        }
    }

    private void validarSenha(String senha) throws Exception {

        if (senha == null || senha.trim().isEmpty()) {
            throw new Exception("Senha inválida.");
        }
    }

    private String normalizarUsername(String username) {
        return username == null ? null : username.trim();
    }

    private String normalizarEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    private String normalizarSenha(String senha) {
        return senha == null ? null : senha.trim();
    }
}
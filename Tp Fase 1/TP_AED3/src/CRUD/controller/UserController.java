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

        if(dao.readByEmail(email) != null) {
            System.out.println("Email já cadastrado.");
            return -1;
        }

        User user = new User(username, email, senha, admin);

        return dao.create(user);
    }

    // login
    public User login(String email, String senha) throws Exception {

        User user = dao.readByEmail(email);

        if(user != null && user.getPassword().equals(senha)) {
            return user;
        }

        return null;
    }

    // deletar usuário
    public boolean deleteUser(int id) throws Exception {

        User user = dao.read(id);

        if(user == null) {
            System.out.println("Usuário não encontrado.");
            return false;
        }

        if(user.isAdministrator()) {
            System.out.println("Não é permitido remover um administrador.");
            return false;
        }

        return dao.delete(id);
    }

    // listar usuários
    public void listUsers() throws Exception {
        dao.listAll();
    }
    
    //promover para adm
    public boolean promoteAdmin(int id) throws Exception {

        User user = dao.read(id);

        if(user == null)
            return false;

        user.setAdministrator(true);

        return dao.update(user);
    }
    
    //remover permissão adm
    public boolean removeAdmin(int targetID, User adminLogado) throws Exception {

        if(targetID == adminLogado.getID()) {
            System.out.println("Você não pode remover sua própria permissão de administrador.");
            return false;
        }

        User user = dao.read(targetID);

        if(user == null) {
            System.out.println("Usuário não encontrado.");
            return false;
        }

        user.setAdministrator(false);

        return dao.update(user);
    }
    
    //trocar própria senha
    public boolean changePassword(User usuario, String novaSenha) throws Exception {

        usuario.setPassword(novaSenha);

        return dao.update(usuario);
    }
    
    //atualizar usuário
    public boolean updateUser(int id, String username, String email) throws Exception {

        User user = dao.read(id);

        if(user == null) {
            System.out.println("Usuário não encontrado.");
            return false;
        }

        user.setUsername(username);
        user.setEmail(email);

        return dao.update(user);
    }

}
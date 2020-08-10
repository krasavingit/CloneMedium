package ru.javamentor.controller.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.javamentor.model.User;
import ru.javamentor.service.role.RoleService;
import ru.javamentor.service.user.UserService;

import java.util.List;

/**
 * Rest контроллер для топиков
 *
 * @version 1.0
 * @author Java Mentor
 */
@RestController
@RequestMapping("/api")
public class UserRestControllers {

    private UserService userService;

    private RoleService roleService;

    @Autowired
    public UserRestControllers(UserService userService, RoleService roleService)
    {
        this.userService = userService;
        this.roleService = roleService;
    }

    /**
     * метод получения всех пользователей админом
     *
     * @return ResponseEntity, который содержит List пользователей и статус ОК
     */
    @GetMapping("/admin/allUsers")
    public ResponseEntity<List<User>> getAllUsers() {
        return new ResponseEntity<> (userService.getAllUsers(), HttpStatus.OK);
    }

    /**
     * метод получения авторизованного пользователя
     *
     * @return ResponseEntity, который содержит авторизованного пользователя и статус ОК
     */
    @GetMapping("/admin/currentUser")
    public ResponseEntity<User> getAllUsers(@AuthenticationPrincipal User auth) {
        return new ResponseEntity<> (auth, HttpStatus.OK);
    }

    /**
     * метод для добавления нового пользователя админом
     * @param user - пользователь которого необходимо добавить
     * @return ResponseEntity, который содержит статус ОК
     */
    @PostMapping("/admin/addUser")
    public ResponseEntity<User> addUser(@RequestBody User user) {
        userService.addUser(user);
        return new ResponseEntity<>(HttpStatus.OK);
    }
    /**
     * метод для удаления пользователя админом
     * @param id - уникальный id пользователя которого необходимо удалить
     * @return ResponseEntity, который содержит статус ОК
     */
    @DeleteMapping("/admin/remove/{id}")
    public ResponseEntity<User> removeUser(@PathVariable Long id) {
        userService.removeUser(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
    /**
     * метод для активации или деактивации пользователя админом
     * @param id - уникальный id пользователя которого необходимо активировать или деактивировать
     * @return ResponseEntity, который содержит статус ОК
     */
    @GetMapping("/admin/enable/{id}")
    public ResponseEntity<User> enableUser(@PathVariable Long id) {
        User user = userService.getUserById(id);
        user.setLockStatus(!(user.getLockStatus()));

        userService.updateUser(user);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * метод для обновления пользователя админом
     * @param user - пользовательно которого необходимо обновить
     * @return ResponseEntity, который содержит статус ОК
     */
    @PutMapping("/admin/update")
    public ResponseEntity<User> updateUser(@RequestBody User user) {
        userService.updateUser(user);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}

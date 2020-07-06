package ru.javamentor.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import ru.javamentor.model.Comment;
import org.springframework.web.bind.annotation.RequestParam;
import ru.javamentor.model.Theme;
import ru.javamentor.model.User;
import ru.javamentor.service.RoleService;
import ru.javamentor.service.ThemeService;
import ru.javamentor.service.UserService;
import ru.javamentor.util.validation.ValidatorFormEditUser;

import java.security.Principal;
import java.util.List;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
public class UserController {

    private UserService userService;
    private RoleService roleService;
    private final ValidatorFormEditUser validatorFormEditUser;
    private ThemeService themeService;

    @Autowired
    public UserController(UserService userService, RoleService roleService, ValidatorFormEditUser validatorFormEditUser, ThemeService themeService) {
        this.userService = userService;
        this.roleService = roleService;
        this.validatorFormEditUser = validatorFormEditUser;
        this.themeService = themeService;
    }

    @GetMapping("/user")
    public String showUser(Model model, Principal principal) {
        User user = (User) ((Authentication) principal).getPrincipal();
        User userDB = userService.getUserById(user.getId());
        model.addAttribute("user", userDB);
        themeService.showThemes(model, userDB);
        return "userPage";
    }

    // TODO Ошибки в изменении пароля и присвоении роли
    @PostMapping("/user/edit_profile")
    public String upgrade(@ModelAttribute("user") User user, @RequestParam(name = "themes", required = false) Set<Long> themesIds, Model model, BindingResult bindingResult) {
        validatorFormEditUser.validate(user, bindingResult);
        if (bindingResult.hasErrors()) {
            return "userPage";
        }
        User userDB = userService.getUserById(user.getId());
        userDB.setRole(roleService.getRoleById(2L));
        userDB.setFirstName(user.getFirstName());
        userDB.setLastName(user.getLastName());
        themeService.changeThemes(themesIds, userDB);
        if (!user.getPassword().equals("")) {
            userDB.setPassword(user.getPassword());
        }
        if (userService.updateUser(userDB)) {
            return "redirect:/user";
        } else {
            model.addAttribute("message", "invalidData");
        }
        return "redirect:/user";
    }


}
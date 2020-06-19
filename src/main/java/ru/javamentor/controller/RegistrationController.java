package ru.javamentor.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.javamentor.model.Role;
import ru.javamentor.model.User;
import ru.javamentor.service.RoleService;
import ru.javamentor.service.UserService;
import ru.javamentor.util.validation.UserValidator;
import ru.javamentor.util.validation.ValidatorFormAddUser;


import javax.validation.Valid;

@Controller
@RequestMapping("/registration")
public class RegistrationController {

    private UserService userService;
    private RoleService roleService;
    private ValidatorFormAddUser userValidator;

    public RegistrationController(UserService userService, RoleService roleService, ValidatorFormAddUser userValidator) {
        this.userService = userService;
        this.roleService = roleService;
        this.userValidator = userValidator;
    }

    @GetMapping
    public String showFormRegistration(User user) {
        return "registration_form";
    }

    @GetMapping("/info")
    public String activationInfoPage(@ModelAttribute("regUser") User user,
                                     Model model,
                                     @ModelAttribute("resend") String resend,
                                     RedirectAttributes redirectAttributes) {
        if (user == null) {
            redirectAttributes.addFlashAttribute("warning", resend);
            return "redirect:/login";
        }

        model.addAttribute("regUser", user);
        boolean flag;
        if (resend != null && !resend.equals("")) {
            flag = true;
        } else {
            flag = false;
        }
        model.addAttribute("flag",flag);
        return "activationInfo";
    }

    @PostMapping
    public String registrationUser(@Valid User user, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        userValidator.validate(user, bindingResult);
        if (bindingResult.hasErrors()) {
            return "registration_form";
        }
        Role roleUser = roleService.getRoleByName("USER");
        user.setRole(roleUser);
        userService.addUser(user);
        redirectAttributes.addFlashAttribute("regUser", user);
        return "redirect:/registration/info";
    }

    @GetMapping("/activate/{code}")
    public String activate(RedirectAttributes redirectAttributes, @PathVariable String code) {
        boolean isActivated = userService.activateUser(code);

        if(isActivated) {
            redirectAttributes.addFlashAttribute("message", "User successfully activated");
        } else {
            redirectAttributes.addFlashAttribute("warning", "Activation code not found!");
        }

        return "redirect:/login";
    }

    @PostMapping("/resend")
    public String resend(@RequestParam("code") String code, RedirectAttributes redirectAttributes) {
        if (code == null && code.equals("")) {
            //return "redirect:/login";
            redirectAttributes.addFlashAttribute("resend", "Email уже подтвержден!");
        }
        User user = userService.findByActivationCode(code);
        if((user != null) && !(user.getActivationCode() == null)) {
            userService.sendCode(user);
            redirectAttributes.addFlashAttribute("resend", "Вам повторно отправлено на почту письмо. Проверьте почту чтобы подтвердить свой Email.");
            //model.addAttribute("message", "Письмо отправлено");
        } else {
            redirectAttributes.addFlashAttribute("resend", "Email уже подтвержден!");
        }
        redirectAttributes.addFlashAttribute("regUser", user);
        return "redirect:/registration/info";
    }
}

package com.urfusoftware.controllers;

import com.urfusoftware.domain.User;
import com.urfusoftware.repositories.RoleRepository;
import com.urfusoftware.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.BeanDefinitionDsl;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.management.relation.Role;

@Controller
public class UserController {
    @Autowired
    private UserRepository userRepository;
    private RoleRepository roleRepository;

    @GetMapping("/user-list")
    public String userList(Model model) {
        model.addAttribute("users", userRepository.findAll());

        return "user-list";
    }

    @GetMapping("{/user}")
    public String userEditForm(@PathVariable User user, Model model) {
        model.addAttribute("user", user);
        model.addAttribute("roles", roleRepository.findAll());
        return "user-edit";
    }
}

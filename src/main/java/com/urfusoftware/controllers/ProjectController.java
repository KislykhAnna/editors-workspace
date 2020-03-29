package com.urfusoftware.controllers;

import com.urfusoftware.domain.Project;
import com.urfusoftware.domain.Report;
import com.urfusoftware.domain.User;
import com.urfusoftware.repositories.ProjectRepository;
import com.urfusoftware.repositories.RoleRepository;
import com.urfusoftware.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
@PreAuthorize("hasAnyAuthority('Администратор', 'Менеджер')")
public class ProjectController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private ProjectRepository projectRepository;

    @GetMapping("/project")
    public String project(@AuthenticationPrincipal User currentUser, Model model) {
        model.addAttribute("manager", userRepository.findByRole(roleRepository.findByName("Менеджер")));

        List<User> users = getUsers("Переводчик", "Старший переводчик");

        model.addAttribute("translator", users);

        users = getUsers("Редактор", "Старший редактор");

        model.addAttribute("editor", users);
        model.addAttribute("user", currentUser);
        return "create-project";
    }

    @PostMapping("/project")
    public String addProject(@RequestParam String title,
                             @RequestParam User manager,
                             @RequestParam User translator,
                             @RequestParam User editor) {
        Project newProject = new Project(title, "В работе", manager, translator, editor);
        projectRepository.save(newProject);
        return "redirect:/";
    }

    private List<User> getUsers(String firstRole, String secondRole) {
        return Stream.concat(userRepository.findByRole(roleRepository.findByName(firstRole)).stream(), userRepository.findByRole(roleRepository.findByName(secondRole)).stream())
                .collect(Collectors.toList());
    }

    @GetMapping("/projects")
    public String loadProjectPage(@AuthenticationPrincipal User currentUser, Model model) {
        List<Project> projects = projectRepository.findAllByOrderByIdAsc();
        for (Project project: projects) {
            project.setOpened();
        }
        model.addAttribute("projects", projects);
        model.addAttribute("currentUser", currentUser);

        return "projects";
    }

    @PostMapping("/projects/{projectId}")
    public String acceptProject(@PathVariable String projectId) {
        Project project = projectRepository.findById((long)(Integer.parseInt(projectId))).orElse(null);
        project.setStatus("Завершен");
        projectRepository.save(project);
        return "redirect:/projects";
    }
}
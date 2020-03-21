package com.urfusoftware.controllers;

import com.urfusoftware.domain.Report;
import com.urfusoftware.domain.User;
import com.urfusoftware.repositories.ReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import sun.invoke.empty.Empty;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
public class ReportController {
    @Autowired
    private ReportRepository reportRepository;

    @Value("${upload.path}")
    private String uploadPath;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    @GetMapping("/reports/add")
    public String reports(Model model) {
        model.addAttribute("tomorrow", dateFormat.format(Calendar.getInstance().getTime()));
        return "create-report";
    }

    @PostMapping("/reports/add")
    public String reportSave(
            @AuthenticationPrincipal User user,
            @RequestParam String title,
            @RequestParam String timeSpent,
            @RequestParam String reportDate,
            @RequestParam("reportLink") MultipartFile reportFile,
            @RequestParam("resultLink") MultipartFile resultFile,
            @RequestParam String comments,
            RedirectAttributes attributes) throws ParseException, IOException {
        if (title.isEmpty() || comments.isEmpty() || reportFile.isEmpty() || resultFile.isEmpty()) {
            attributes.addFlashAttribute("message", "ОШИБКА! Поля не могут быть пустыми!");
            return "redirect:/reports/add";
        } else {
            Report report = new Report();

            report.setTitle(title);
            report.setReportLink(setLink(reportFile));
            report.setResultLink(setLink(resultFile));
            report.setAccepted(false);
            report.setUser(user);
            report.setDate(dateFormat.parse(reportDate));
            report.setTimeSpent(Integer.parseInt(timeSpent));
            report.setComments(comments);
            reportRepository.save(report);
        }
        return "redirect:/main";
    }

    @GetMapping("/reports")
    private String getReports(@AuthenticationPrincipal User currentUser, Model model) {
        model.addAttribute("user", currentUser);
        List<Report> userReports = new ArrayList<>();

        for (Report report : reportRepository.findAllByOrderByIdAsc()) {
            if (report.getUser().getId().equals(currentUser.getId())) {
                String reportDate = report.getDate().toString().substring(0, 10);
                String dateInRusFormat = reportDate.substring(8, 10) +
                        "." + reportDate.substring(5, 7) +
                        "." + reportDate.substring(0, 4);
                report.setStringDate(dateInRusFormat);
                userReports.add(report);
            }
        }

        if (currentUser.getRole().getName().equals("Администратор") ||
                currentUser.getRole().getName().equals("Менеджер")) {
            model.addAttribute("checkForPermission", true);
        } else {
            model.addAttribute("checkForPermission", false);
        }
        model.addAttribute("reports", userReports);

        return "reports";
    }

    @GetMapping("/reports/{fileName}")
    public void getFile( HttpServletResponse response,
                         @PathVariable("fileName") String fileName,
                         @RequestHeader String referer) throws IOException {
        if(referer != null && !referer.isEmpty()) { }
        Path filePath = Paths.get(uploadPath, fileName);
        if (Files.exists(filePath))
        {
            response.addHeader("Content-Disposition", "attachment; filename=" + fileName.substring(37));
            Files.copy(filePath, response.getOutputStream());
            response.getOutputStream().flush();
        }
    }

    @PostMapping("/reports/{reportId}")
    private String acceptReport(@PathVariable String reportId) {
        Report report = reportRepository.findById((long)(Integer.parseInt(reportId))).orElse(null);
        report.setAccepted(true);
        reportRepository.save(report);
        return "redirect:/reports";
    }

    private String setLink(MultipartFile file) throws IOException {
        if (file != null) {
            File uploadDir = new File(uploadPath);

            if (!uploadDir.exists()) {
                uploadDir.mkdir();
            }

            String uuidFile = UUID.randomUUID().toString();
            String link = uuidFile + "." + file.getOriginalFilename();
            file.transferTo(new File(uploadPath + "/" + link));
            return link;
        }
        return "";
    }
}
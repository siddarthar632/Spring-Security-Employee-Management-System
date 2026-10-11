package com.example.employee.controller;

import com.example.employee.model.Employee;
import com.example.employee.repository.EmployeeRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/employees")
public class EmployeeController {
    private final EmployeeRepository employeeRepository;

    public EmployeeController(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("employees", employeeRepository.findAll());
        return "employees";
    }

    @GetMapping("/new")
    public String newEmployee(Model model) {
        model.addAttribute("employee", new Employee());
        return "employee-form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute Employee employee,
                         BindingResult result, Model model,
                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "employee-form";
        }
        if (employeeRepository.existsByEmail(employee.getEmail().trim().toLowerCase())) {
            model.addAttribute("formError", "An employee with this email already exists.");
            return "employee-form";
        }
        employee.setEmail(employee.getEmail().trim().toLowerCase());
        employeeRepository.save(employee);
        redirectAttributes.addFlashAttribute("message", "Employee created successfully.");
        return "redirect:/employees";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));
        model.addAttribute("employee", employee);
        return "employee-form";
    }

    @PostMapping("/update/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute Employee employee,
                         BindingResult result, Model model,
                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "employee-form";
        }
        Employee existing = employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));
        employeeRepository.findAll().stream()
                .filter(e -> e.getEmail().equalsIgnoreCase(employee.getEmail())
                        && !e.getId().equals(id))
                .findAny()
                .ifPresent(e -> { throw new IllegalArgumentException("That email is already used by another employee."); });

        existing.setName(employee.getName());
        existing.setEmail(employee.getEmail().trim().toLowerCase());
        existing.setDepartment(employee.getDepartment());
        employeeRepository.save(existing);
        redirectAttributes.addFlashAttribute("message", "Employee updated successfully.");
        return "redirect:/employees";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        employeeRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("message", "Employee deleted.");
        return "redirect:/employees";
    }
}

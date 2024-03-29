package springbootapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import springbootapp.dao.UserServiceImpl;
import springbootapp.model.Role;
import springbootapp.model.User;
import springbootapp.repository.RoleRepository;

import java.util.*;

@Controller
@RequestMapping("/")
public class UserController {

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private RoleRepository roleRepository;
    @GetMapping("/user")
    public String showUser(Model model) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String usernameForNavbar = user.getUsername();
        Set<Role> rolesForNavbar = user.getRoles();
        model.addAttribute("user", user);
        model.addAttribute("usernameForNavbar", usernameForNavbar);
        model.addAttribute("rolesForNavbar", rolesForNavbar);
        return "user";
    }

    @GetMapping("/admin")
    public ModelAndView home(Authentication authentication) {
        List<User> listUser = userService.listAll();
        User user = (User) authentication.getPrincipal();
        ModelAndView mav = new ModelAndView();
        String usernameForNavbar = user.getUsername();
        Set<Role> rolesForNavbar = user.getRoles();
        mav.setViewName("admin");
        mav.addObject("listUser", listUser);
        mav.addObject("usernameForNavbar", usernameForNavbar);
        mav.addObject("rolesForNavbar", rolesForNavbar);
        return mav;
    }

    @PostMapping(value = "/new", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ModelAndView newCustomerForm (@RequestBody MultiValueMap<String, String> formData) {
        //Long id = new Long(Objects.requireNonNull(formData.getFirst("id")));
        String password = Objects.requireNonNull(formData.getFirst("password"));
        String username = Objects.requireNonNull(formData.getFirst("username"));

        ModelAndView mav = new ModelAndView("redirect:/admin");
        User user = new User();
        user.setPassword(password);
        user.setUsername(username);

        List<String> role_ids = formData.getOrDefault("roles", new ArrayList<String>());
        HashSet<Role> roles = new HashSet<Role>();
        for (String role_id : role_ids) {
            roles.add(roleRepository.getRoleById(new Long(role_id)));
        }

        user.setRoles(roles);//поменять в зависимости от типа передаваемых значений
        userService.saveUser(user);
        return mav;
    }

    @GetMapping("/new")
    public ModelAndView newCustomerForm() {
        ModelAndView mav = new ModelAndView("new_user");

        User user = new User();
        mav.addObject("user", user);

        List<Role> roles = roleRepository.findAll();
        mav.addObject("allRoles", roles);

        return mav;
    }

    @GetMapping("/edit/{id}")
    public ModelAndView editUserForm(@PathVariable long id) {

        User user = userService.findUserById(id);
        ModelAndView mav = new ModelAndView("edit_user");
        mav.addObject("user", user);

        List<Role> roles = roleRepository.findAll();
        mav.addObject("allRoles", roles);
        return mav;
    }

    @PostMapping(value = "/edit", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ModelAndView editUserForm(@RequestBody MultiValueMap<String, String> formData) {
//        System.out.println(formData);
//        System.out.println(formData.get("roles"));
        Long id = new Long(Objects.requireNonNull(formData.getFirst("id")));
        String password = Objects.requireNonNull(formData.getFirst("password"));
        String username = Objects.requireNonNull(formData.getFirst("username"));

        ModelAndView mav = new ModelAndView("redirect:/admin");

        System.out.println(id);
        System.out.println(formData.getFirst("id"));

        User user = userService.findUserById(id);
        user.setId(id);
        user.setPassword(password);
        user.setUsername(username);
        List<String> role_ids = formData.getOrDefault("roles", new ArrayList<String>());
        HashSet<Role> roles = new HashSet<Role>();
        for (String role_id : role_ids) {
            roles.add(roleRepository.getRoleById(new Long(role_id)));
        }
        user.setRoles(roles);
//        Set<Role> role = roleRepository.getRolesById(id);
//        user.setRoles(role);//поменять в зависимости от типа передаваемых значений
        userService.saveUser(user);
        return mav;
    }

    @GetMapping("/delete/{id}")
    public String deleteUserForm(@PathVariable long id) {
        userService.deleteUser(id);
        return "redirect:/admin";
    }

    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public String saveUser(@ModelAttribute("user") User user) {
        userService.saveUser(user);
        return "redirect:/admin";
    }
}
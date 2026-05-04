package controllers;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import model.User;
import repository.UserRepository;

@Controller
public class LoginController {
	 @Autowired
	    private UserRepository userRepository;

	    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
	    
	 // Hiển thị form login
	    @GetMapping("/login")
	    public String loginForm() {
	        return "login"; // Tên file login.html
	    }

	    @PostMapping("/do-login")
	    public String login(@RequestParam String username,
	                        @RequestParam String password,
	                        HttpSession session,
	                        Model model) {

	        User user = userRepository.findByUsername(username);
	        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
	            session.setAttribute("loggedInUser", user);
	            return "redirect:/home";
	        } else {
	            model.addAttribute("error", "Sai tài khoản hoặc mật khẩu");
	            return "login";
	        }
	    }

	    @GetMapping("/logout")
	    public String logout(HttpSession session) {
	        session.invalidate();
	        return "redirect:/home";
	    }
	    
	    
	    @GetMapping("/register")
	    public String registerForm() {
	        return "register"; // Tên file register.html
	    }

	    @PostMapping("/do-register")
	    public String register(@RequestParam String username,
	                           @RequestParam String email,
	                           @RequestParam String password,
	                           Model model) {

	        if (userRepository.findByUsername(username) != null) {
	            model.addAttribute("error", "Tài khoản đã tồn tại");
	            return "register";
	        }

	        User newUser = new User();
	        newUser.setUsername(username);
	        newUser.setEmail(email); // Thêm email
	        newUser.setPassword(passwordEncoder.encode(password));

	        userRepository.save(newUser);
	        return "redirect:/login";
	    }
}

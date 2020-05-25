package com.example.login;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import javax.sql.DataSource;
import java.security.Principal;

@SpringBootApplication
public class LoginApplication {
	@Bean
	UserDetailsManager memory(){

		return new InMemoryUserDetailsManager();
	}

	@Bean
	InitializingBean initializer(UserDetailsManager  manager){
		return () ->{
			UserDetails gbansal = User.withDefaultPasswordEncoder().username("gbansal").password("password").roles("USER").build();
			manager.createUser(gbansal);
			UserDetails gourav = User.withUserDetails(gbansal).username("gourav").build();
			manager.createUser(gourav);
		};
	}


	public static void main(String[] args) {
		SpringApplication.run(LoginApplication.class, args);
	}

}

@ControllerAdvice
class PrincipalControllerAdvice{

	@ModelAttribute ("currentUser")
	Principal principal(Principal p){
		return p;
	}
}

@Controller
class LoginController{
	@GetMapping("/")
	String index(Model model){
		return "hidden";
	}
	@GetMapping("/login")
	String login(){
		return "login";
	}
	@GetMapping("/logout-success")
	String logout(){
		return "logout";
	}
}

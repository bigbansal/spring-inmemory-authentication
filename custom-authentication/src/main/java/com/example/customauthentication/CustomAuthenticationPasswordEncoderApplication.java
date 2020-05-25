package com.example.customauthentication;

import org.apache.commons.logging.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.MessageDigestPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SpringBootApplication
public class CustomAuthenticationPasswordEncoderApplication {

	//@Bean
	PasswordEncoder oldPasswordEncoder(){
		String md5= "MD5";
		return new DelegatingPasswordEncoder(md5, Collections.singletonMap(md5, new MessageDigestPasswordEncoder(md5)));

	}

	@Bean
	PasswordEncoder passwordEncoder(){
		return  PasswordEncoderFactories.createDelegatingPasswordEncoder();

	}
	@Bean
	CustomUserDetailsService customUserDetailsService(){
		Collection<UserDetails> users = Arrays.asList(
			new CustomUserDetails("gourav",oldPasswordEncoder().encode("bansal"),true, "USER"),
				new CustomUserDetails("bansal",oldPasswordEncoder().encode("bansal"),true, "USER","ADMIN")
		);
		return new CustomUserDetailsService(users);
	}


	public static void main(String[] args) {
		SpringApplication.run(CustomAuthenticationPasswordEncoderApplication.class, args);
	}

}

@Configuration
@EnableWebSecurity
class CustomSecurityConfiguration extends WebSecurityConfigurerAdapter{

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.httpBasic();
		http.authorizeRequests().anyRequest().authenticated();
	}

}

@RestController
class GreetingRestController{

	@GetMapping("/greeting")
	String greet(Principal p){
		return "Hello " + p.getName() + "!";
	}
}

class CustomUserDetailsService implements UserDetailsService, UserDetailsPasswordService {

	private Logger log=  LoggerFactory.getLogger(getClass());
	private final Map<String, UserDetails > users= new ConcurrentHashMap<>();


	public CustomUserDetailsService(Collection<UserDetails> seedUsers){
		seedUsers.forEach(user -> this.users.put(user.getUsername(), user));
		this.users.forEach((k, v) -> log.info( v.getUsername() + "=" +v.getPassword()));
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

		if(this.users.containsKey(username)){
			return this.users.get(username);
		}
		throw new UsernameNotFoundException("Could Notfound username");
	}

	@Override
	public UserDetails updatePassword(UserDetails userDetails, String newPassword) {
		log.info("prompted to update password for user" + userDetails.getUsername() + " to " + newPassword);
		this.users.put(userDetails.getUsername(), new CustomUserDetails(
				userDetails.getUsername(),
				newPassword,
				userDetails.isEnabled(),
				userDetails.getAuthorities().stream().map( GrantedAuthority::getAuthority).toArray(String[]::new)
		));
		return this.loadUserByUsername(userDetails.getUsername());
	}
}

class CustomUserDetails implements  UserDetails {

	private final Set<GrantedAuthority> authorities ;
	private final String username, password;
	private final boolean active;

	public CustomUserDetails(String username, String password, boolean active, 	String... authorities) {
		this.username = username;
		this.password = password;
		this.active = active;
		this.authorities = Stream
				.of(authorities)
				.map(SimpleGrantedAuthority::new)
				.collect(Collectors.toSet());
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return this.authorities;
	}

	@Override
	public String getPassword() {
		return this.password;
	}

	@Override
	public String getUsername() {
		return this.username;
	}

	@Override
	public boolean isAccountNonExpired() {
		return this.active;
	}

	@Override
	public boolean isAccountNonLocked() {
		return this.active;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return this.active;
	}

	@Override
	public boolean isEnabled() {
		return this.active;
	}
}
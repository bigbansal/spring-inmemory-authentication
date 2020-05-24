package com.example.jdbc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.beans.factory.InitializingBean;

import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.context.annotation.Bean;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import javax.sql.DataSource;

@SpringBootApplication

public class JdbcApplication {
	@Bean
	UserDetailsManager memory(DataSource ds){
		JdbcUserDetailsManager jdbcUserDetailsManager = new JdbcUserDetailsManager();
		jdbcUserDetailsManager.setDataSource(ds);
		return jdbcUserDetailsManager;
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
		SpringApplication.run(JdbcApplication.class, args);
	}

}

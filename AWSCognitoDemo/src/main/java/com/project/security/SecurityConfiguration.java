package com.project.security;

import com.project.auth.AwsCognitoJwtAuthFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

	@Autowired
	private AwsCognitoJwtAuthFilter awsCognitoJwtAuthenticationFilter;

	@Override
	protected void configure(HttpSecurity http) throws Exception {

		//http.headers().cacheControl();
		http
			//	.csrf().disable()
				.authorizeRequests()
				.antMatchers(publicAccess()).permitAll()
				.antMatchers(memberLevelAccess()).hasRole("MEMBERS")
				.antMatchers(adminLevelAccess()).hasRole("ADMIN")
				//.anyRequest().authenticated()
				.and()
				.addFilterBefore(awsCognitoJwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
	}

	private String[] publicAccess(){
		return new String[] {
				"*/health"
		};
	}

	private String[] memberLevelAccess(){
		return new String[] {
				"/api/**"
		};
	}

	private String[] adminLevelAccess(){
		return new String[] {
				"/admin/**"
		};
	}

}
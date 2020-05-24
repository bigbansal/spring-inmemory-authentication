package com.example.bootcamp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.UUID;

@SpringBootApplication
public class BootcampApplication {

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

	@Bean
	RestTemplate restTemplate (){
		return new RestTemplate();
	}
	public static void main(String[] args) {
		SpringApplication.run(BootcampApplication.class, args);
	}

}

@Component
class LoggingFilter implements javax.servlet.Filter{
	private final Log log= LogFactory.getLog(getClass());

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {

	}

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
		Assert.isTrue(servletRequest instanceof HttpServletRequest, "this assumes you have http request");
		HttpServletRequest httpServletRequest = HttpServletRequest.class.cast(servletRequest);
		String uri= httpServletRequest.getRequestURI();
		this.log.info("new reuqest for "+ uri);
		long time= System.currentTimeMillis();
		filterChain.doFilter(servletRequest, servletResponse);
		long delta=System.currentTimeMillis()-time;
		this.log.info("request for: " +uri +"took   " + delta + " ms");

	}

	@Override
	public void destroy() {

	}
}


@Component
@Aspect
class LoggingAspect{
	private final 	Log log = LogFactory.getLog(getClass());

	@Around("execution ( * com.example..*.*(..)  )")
	public Object log(ProceedingJoinPoint pjp) throws Throwable{
		this.log.info("before :  "+pjp.toString());
		Object ojbect = pjp.proceed();
		this.log.info("after :  "+pjp.toString());
		return ojbect;
	}
}


@RestController
class IsbnRestController {
	private final RestTemplate restTemplate;

	IsbnRestController(RestTemplate restTemplate ){
		this.restTemplate = restTemplate;
	}

	@GetMapping("/books/{isbn}")
	String lookupBookByIsbn(@PathVariable("isbn") String isbn){
		ResponseEntity<String> exchange = this.restTemplate
				.exchange("https://www.googleapis.com/books/v1/volumes?q=isbn:"+isbn, HttpMethod.GET,
						null, String.class);
		return exchange.getBody();
	}
}
@Component("uuid")
class UuidService {
	public String buildUuid(){
		return UUID.randomUUID().toString();
	}
}

@Component
class Bar {
	private final Foo foo;
	private final Log log= LogFactory.getLog(getClass());

	Bar(Foo foo, @Value("#{ uuid.buildUuid() }") String uuid,
		@Value("#{ 2 > 1}") boolean proceed){
		this.foo = foo;
		this.log.info("Uuid: "+ uuid);
		this.log.info("proceed: "+ proceed);

	}

}

@Component
class Foo{

}
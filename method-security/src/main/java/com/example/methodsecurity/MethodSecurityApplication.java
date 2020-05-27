package com.example.methodsecurity;

import lombok.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.data.repository.query.SecurityEvaluationContextExtension;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.security.RolesAllowed;
import javax.persistence.*;
import javax.swing.text.html.Option;
import javax.transaction.Transactional;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@EnableJpaAuditing
@EnableGlobalMethodSecurity(
		prePostEnabled = true,
		jsr250Enabled = true,
		securedEnabled = true
)
@SpringBootApplication
public class MethodSecurityApplication {

	@Bean
	AuditorAware auditor(){
		return new AuditorAware() {
			@Override
			public Optional getCurrentAuditor() {
				SecurityContext context = SecurityContextHolder.getContext();
				Authentication authentication = context.getAuthentication();
				if( null!= authentication)
				{Optional.ofNullable(authentication.getName());}
				return Optional.empty();
			}
		};
	}
	@Bean
	SecurityEvaluationContextExtension securityEvaluationContextExtension(){
		return new SecurityEvaluationContextExtension();
	}

	public static void main(String[] args) {
		SpringApplication.run(MethodSecurityApplication.class, args);
	}

}



@Transactional
@Component
class Runner implements ApplicationRunner{

	private final Logger log = LoggerFactory.getLogger(getClass());
	private final UserRepository userRepository;
	private final AuthorityRepository authorityRepository;
	private final MessageRepository messageRepository;
	private final UserDetailsService userDetailsService;
	public Runner(UserRepository userRepository, AuthorityRepository authorityRepository, MessageRepository messageRepository, UserDetailsService userDetailsService) {
		this.userRepository = userRepository;
		this.authorityRepository = authorityRepository;
		this.messageRepository = messageRepository;
		this.userDetailsService = userDetailsService;
	}

	private void authenticate (String username){
		UserDetails user = this.userDetailsService.loadUserByUsername(username);
		Authentication authentication = new UsernamePasswordAuthenticationToken(user, user.getPassword(),
				user.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		//install some data

		Authority user = this.authorityRepository.save(new Authority("USER"));
		Authority admin = this.authorityRepository.save(new Authority("ADMIN"));

		User bansal= this.userRepository.save(new User("bansal","bansal", admin, user));
		Message messageForBansal =  this.messageRepository.save( new Message("Hi Bansal!", bansal));
		messageForBansal =  this.messageRepository.save( new Message("Hi Bansal!1", bansal));
		messageForBansal =  this.messageRepository.save( new Message("Hi Bansal!2", bansal));
		messageForBansal =  this.messageRepository.save( new Message("Hi Bansal!3", bansal));
		messageForBansal =  this.messageRepository.save( new Message("Hi Bansal!4", bansal));
		messageForBansal =  this.messageRepository.save( new Message("Hi Bansal!5", bansal));

		User gourav= this.userRepository.save(new User("gourav","bansal", user));
		messageForBansal =  this.messageRepository.save( new Message("Hi Gourav!5", gourav));

		log.info("gourav:" + gourav.toString());
		log.info("bansal:" + bansal.toString());

		attemptAccess( bansal.getEmail(), gourav.getEmail(),
				messageForBansal.getId(),
				this.messageRepository::findByIdRolesAllowed);

		attemptAccess( bansal.getEmail(), gourav.getEmail(),
				messageForBansal.getId(),
				this.messageRepository::findByIdSecured);

		attemptAccess( bansal.getEmail(), gourav.getEmail(),
				messageForBansal.getId(),
				this.messageRepository::findByIdPreAuthorize);

		/*attemptAccess( bansal.getEmail(), gourav.getEmail(),
				messageForBansal.getId(),
				this.messageRepository::findByIdPostAuthorize);
*/
		authenticate(bansal.getEmail());
		this.messageRepository.findMessagesFor(PageRequest.of(0,5))
		.forEach( msg -> log.info((msg.getText())));

		authenticate(gourav.getEmail());
		this.messageRepository.findMessagesFor(PageRequest.of(0,5))
				.forEach( msg -> log.info((msg.getText())));

		log.info("Audited" + this.messageRepository.save( new Message("Hi Bansal!5", bansal)));
	}

	void attemptAccess(String adminUser, String regularUser, Long msgId,
					   Function<Long, Message> fn){
		authenticate(adminUser);
		log.info("result for Bansal - " +fn.apply(msgId));

		try {
			authenticate(regularUser);
			log.info("result for Gourav - " + fn.apply(msgId));
		} catch(Throwable e){
			log.error("Oops Could not obtain the result");
		}
	}
}

@Service
class UserRepositoryUserDetailsService implements UserDetailsService{

	private final UserRepository userRepository;

	public UserRepositoryUserDetailsService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	public static class UserUserDetails implements  UserDetails{

		private final User user;
		private final Set<GrantedAuthority> authorities;



		public UserUserDetails(User user) {
			this.user = user;
			this.authorities = this.user.getAuthorities()
			.stream()
			.map( au -> new SimpleGrantedAuthority("ROLE_" +au.getAuthority()))
			.collect(Collectors.toSet());
		}

		@Override
		public Collection<? extends GrantedAuthority> getAuthorities() {
			return this.authorities;
		}

		@Override
		public String getPassword() {
			return this.user.getPassword();
		}

		@Override
		public String getUsername() {
			return this.user.getEmail();
		}

		@Override
		public boolean isAccountNonExpired() {
			return true;
		}

		@Override
		public boolean isAccountNonLocked() {
			return true;
		}

		@Override
		public boolean isCredentialsNonExpired() {
			return true;
		}

		@Override
		public boolean isEnabled() {
			return true;
		}

		public User getUser() {
			return user;
		}
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user= this.userRepository.findByEmail(username);
		if(null != user)
		{
			return new UserUserDetails(user);
		}
		else throw new UsernameNotFoundException("Could Not find Username");
	}
}


interface  MessageRepository  extends JpaRepository<Message, Long>{
	String QUERY = "select m from Message m where m.id= ?1";

	@Query(QUERY)
	@RolesAllowed("ROLE_ADMIN")
	Message  findByIdRolesAllowed(Long id);

	@Query(QUERY)
	@Secured("ROLE_ADMIN")
	Message findByIdSecured(Long id);

	@Query(QUERY)
	@PreAuthorize("hasRole('ADMIN')")
	Message findByIdPreAuthorize(Long id);

	@Query(QUERY)
	@PostAuthorize("@authz.check ( returnObject, principal?.user )")
	Message findByIdPostAuthorize(Long id);


	@Query("select m from Message m where m.to.id = ?#{ principal?.user?.id }")
	Page<Message> findMessagesFor(Pageable pageable);
}

@Service("authz")
class AuthService{
	private final Logger log = LoggerFactory.getLogger(getClass());
	public boolean check(Message message, User user){
		log.info("checking ..."+ user.getEmail());

		return message.getTo().getId().equals(user.getId());
	}
}

interface  UserRepository  extends JpaRepository<User, Long>{
	User findByEmail (String email);
}

interface  AuthorityRepository  extends JpaRepository<Authority, Long>{

}

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@EntityListeners(AuditingEntityListener.class)
class Message{

	@Id
	@GeneratedValue
	private Long Id;

	private String text;

	@OneToOne
	private User to;

	@CreatedBy
	private String createdBy;

	@CreatedDate
	@Temporal(TemporalType.TIMESTAMP)
	private Date created;

	public Message(String text, User to) {
		this.text = text;
		this.to = to;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public User getTo() {
		return to;
	}

	public void setTo(User to) {
		this.to = to;
	}

	public Long getId() {
		return Id;
	}

	public void setId(Long id) {
		Id = id;
	}
}
@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(exclude="authorities")
@Data
class User{
	@Id
	@GeneratedValue
	private Long Id;

	private String email, password;

	public User(String email, String password, Set<Authority> authorities) {
		this.email = email;
		this.password = password;
		this.authorities.addAll(authorities);
	}

	public User(String email, String password) {
		this( email, password, new HashSet<>());
	}

	public User(String email, String password, Authority... authorities) {
		this(email, password, new HashSet<>(Arrays.asList(authorities)));
	}

	@ManyToMany(mappedBy = "users")
	private List<Authority> authorities = new ArrayList<>();

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public List<Authority> getAuthorities() {
		return authorities;
	}

	public void setAuthorities(List<Authority> authorities) {
		this.authorities = authorities;
	}

	public Long getId() {
		return Id;
	}

	public void setId(Long id) {
		Id = id;
	}
}

@Entity
@AllArgsConstructor
@ToString(exclude = "users")
@NoArgsConstructor
@Data
class Authority{
	@Id
	@GeneratedValue
	private Long Id;

	private String authority;

	public Authority(String authority) {
		this.authority = authority;
	}

	public Authority(String authority, Set<User> users) {
		this.authority = authority;
		this.users.addAll(users);
	}

	@ManyToMany(cascade = {
			CascadeType.PERSIST, CascadeType.MERGE
	})
	@JoinTable(
			name="authority_user",
			joinColumns = @JoinColumn(name="authority_id"),
			inverseJoinColumns = @JoinColumn(name="user_id")
	)
	private List<User> users = new ArrayList<>();

	public String getAuthority() {
		return authority;
	}

	public void setAuthority(String authority) {
		this.authority = authority;
	}

	public List<User> getUsers() {
		return users;
	}

	public void setUsers(List<User> users) {
		this.users = users;
	}
}
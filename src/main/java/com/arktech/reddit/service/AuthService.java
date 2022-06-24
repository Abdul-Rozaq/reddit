package com.arktech.reddit.service;

import java.time.Instant;
import java.util.UUID;

import javax.transaction.Transactional;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.arktech.reddit.dto.AuthenticationResponse;
import com.arktech.reddit.dto.LoginRequest;
import com.arktech.reddit.dto.RefreshTokenRequest;
import com.arktech.reddit.dto.RegisterRequest;
import com.arktech.reddit.entity.NotificationEmail;
import com.arktech.reddit.entity.User;
import com.arktech.reddit.entity.VerificationToken;
import com.arktech.reddit.exception.RedditException;
import com.arktech.reddit.repository.UserRepository;
import com.arktech.reddit.repository.VerificationTokenRepository;

import static com.arktech.reddit.util.Constants.ACTIVATION_EMAIL;
import static com.arktech.reddit.util.Constants.ACTIVATION_EMAIL_LINK;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class AuthService {
	
	private PasswordEncoder passwordEncoder;
	private AuthenticationManager authenticationManager;
	private JwtProvider jwtProvider;
	private RefreshTokenService refreshTokenService;
	private UserRepository userRepository;
	private VerificationTokenRepository verificationTokenRepository;
	private MailContentBuilder mailContentBuilder;
	private MailService mailService;

	// create a new user
	@Transactional
	public void signup(RegisterRequest registerRequest) {
		User user = new User();
		user.setUsername(registerRequest.getUsername());
		user.setEmail(registerRequest.getEmail());
		user.setPassword(encodePassword(registerRequest.getPassword()));
		user.setCreated(Instant.now());
		user.setEnabled(false);
		
		User _user = userRepository.save(user);
		
		String token = generateVerificationtoken(_user);
		String message = mailContentBuilder.build(ACTIVATION_EMAIL + ACTIVATION_EMAIL_LINK + token);
		
		mailService.sendMail(new NotificationEmail("Please Activate Your Account", user.getEmail(), message));
	}
	
	// verification token to be sent to mail
	private String generateVerificationtoken(User user) {
		String token = UUID.randomUUID().toString();
		
		VerificationToken verificationToken = new VerificationToken();
		verificationToken.setToken(token);
		verificationToken.setUser(user);
		
		VerificationToken _verificationToken = verificationTokenRepository.save(verificationToken);
		return _verificationToken.getToken();
	}
	
	public void verifyAccount(String token) {
		VerificationToken _verificationToken = verificationTokenRepository
				.findByToken(token)
				.orElseThrow(() -> new RedditException("Invalid token"));
		
		fetchUserAndEnable(_verificationToken);
	}
	
	@Transactional
	private void fetchUserAndEnable(VerificationToken verificationToken) {
		String username = verificationToken.getUser().getUsername();
		User user = userRepository.findByUsername(username).orElseThrow(() -> new RedditException("User not found with id: " + username));
		
		user.setEnabled(true);
		userRepository.save(user);
	}
	
	public AuthenticationResponse login(LoginRequest loginRequest) {
		Authentication authenticate = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
		);
		
		SecurityContextHolder.getContext().setAuthentication(authenticate);
		String token = jwtProvider.generateToken(authenticate);
		
		new AuthenticationResponse();
		return AuthenticationResponse.builder()
				.token(token)
				.refreshToken(refreshTokenService.generateRefreshToken().getToken())
				.expiresAt(Instant.now().plusMillis(jwtProvider.getJwtExpirationInMillis()))
				.username(loginRequest.getUsername())
				.build();
	}
	
	public AuthenticationResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
		refreshTokenService.validateRefreshToken(refreshTokenRequest.getRefreshToken());
		String token = jwtProvider.generateTokenWithUsername(refreshTokenRequest.getUsername());
		
		new AuthenticationResponse();
		return AuthenticationResponse.builder()
				.token(token)
				.refreshToken(refreshTokenRequest.getRefreshToken())
				.expiresAt(Instant.now().plusMillis(jwtProvider.getJwtExpirationInMillis()))
				.username(refreshTokenRequest.getUsername())
				.build();
	}

	// encode password
	private String encodePassword(String password) {
		return passwordEncoder.encode(password);
	}

	public User getCurrentUser() {
		org.springframework.security.core.userdetails.User principal = 
				(org.springframework.security.core.userdetails.User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		
		return userRepository
				.findByUsername(principal.getUsername())
                .orElseThrow(() -> new RedditException("User name not found - " + principal.getUsername()));
	}
}

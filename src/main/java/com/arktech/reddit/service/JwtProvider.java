package com.arktech.reddit.service;

import java.time.Instant;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import static java.util.Date.from;

@Service
public class JwtProvider {
	
	@Value("${jwt.expiration.time}")
	private Long jwtExpirationInMillis;
	@Value("${jwt.key}")
	private String jwtKey;
	
	public String generateToken(Authentication authentication) {
		org.springframework.security.core.userdetails.User principal = (User) authentication.getPrincipal();
		
		return Jwts.builder()
				.setSubject(principal.getUsername())
				.setIssuedAt(from(Instant.now()))
				.claim("authority", principal.getAuthorities())
				.signWith(Keys.hmacShaKeyFor(jwtKey.getBytes()))
				.setExpiration(from(Instant.now().plusMillis(jwtExpirationInMillis)))
				.compact();
	}
	
	public String generateTokenWithUsername(String username) {
		return Jwts.builder()
				.setSubject(username)
				.setIssuedAt(Date.from(Instant.now()))
				.signWith(Keys.hmacShaKeyFor(jwtKey.getBytes()))
				.setExpiration(Date.from(Instant.now().plusMillis(jwtExpirationInMillis)))
				.compact();
	}

	public boolean validateToken(String jwt) {
		Jwts
			.parser()
			.setSigningKey(Keys.hmacShaKeyFor(jwtKey.getBytes()))
			.parseClaimsJws(jwt);
		return true;
	}

	public String getUsernameFromJWT(String jwt) {
		Claims claims = Jwts
							.parser()
							.setSigningKey(Keys.hmacShaKeyFor(jwtKey.getBytes()))
							.parseClaimsJws(jwt)
							.getBody();
		
		return claims.getSubject();
	}
	
	public Long getJwtExpirationInMillis() {
		return jwtExpirationInMillis;
	}
	
	public String getJwtKey() {
		return jwtKey;
	}
}

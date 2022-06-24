package com.arktech.reddit.service;

import java.util.List;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.arktech.reddit.dto.CommentDto;
import com.arktech.reddit.entity.Comment;
import com.arktech.reddit.entity.NotificationEmail;
import com.arktech.reddit.entity.Post;
import com.arktech.reddit.entity.User;
import com.arktech.reddit.exception.RedditException;
import com.arktech.reddit.mapper.CommentMapper;
import com.arktech.reddit.repository.CommentRepository;
import com.arktech.reddit.repository.PostRepository;
import com.arktech.reddit.repository.UserRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
@Transactional
public class CommentService {

	private static final String POST_URL = "";
	
	private CommentMapper commentMapper;
	private PostRepository postRepository;
	private CommentRepository commentRepository;
	private UserRepository userRepository;
	private AuthService authService;
	private MailContentBuilder mailContentBuilder;
	private MailService mailService;
	
	public void createComment(CommentDto commentDto) {
		// Get post or throw error
		Post post = postRepository.findById(commentDto.getPostId())
				.orElseThrow(() -> new RedditException("Post not found: " + commentDto.getPostId()));
		
		// convert commentDto to comment
		Comment comment = commentMapper.map(commentDto, post, authService.getCurrentUser());
		commentRepository.save(comment);
		
		// create and send mail to author of post
		String message = mailContentBuilder.build(post.getUser().getUsername() + " posted a comment on your post." + POST_URL);
		sendCommentNotification(message, post.getUser());
	}

	public List<CommentDto> getCommentsByPost(Long postId) {
		Post post = postRepository.findById(postId)
				.orElseThrow(() -> new RedditException("Post not found: " + postId));
		
		return commentRepository.findByPost(post)
				.stream()
				.map(commentMapper::mapToDto)
				.collect(Collectors.toList());
	}

	public List<CommentDto> getCommentsByUser(String username) {
		User user = userRepository.findByUsername(username)
				.orElseThrow(() -> new RedditException("User not found: " + username));
		
		return commentRepository.findAllByUser(user)
				.stream()
				.map(commentMapper::mapToDto)
				.collect(Collectors.toList());
	}
	
	private void sendCommentNotification(String message, User user) {
		mailService.sendMail(new NotificationEmail(
				user.getUsername() + " commented on your post",
				user.getEmail(),
				message));
	}

}

package com.arktech.reddit.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.arktech.reddit.dto.CommentDto;
import com.arktech.reddit.service.CommentService;

import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/api/comments")
public class CommentController {

	private CommentService commentService;
	
	@PostMapping
	public ResponseEntity<Void> createComment(@RequestBody CommentDto commentDto) {
		commentService.createComment(commentDto);
		return new ResponseEntity<>(HttpStatus.CREATED);
	}
	
	@GetMapping("/post/{postId}")
	public ResponseEntity<List<CommentDto>> getAllCommentsForPost(@PathVariable("postId") Long postId) {
		return new ResponseEntity<>(commentService.getCommentsByPost(postId), HttpStatus.OK);
	}
	
	@GetMapping("/user/{username}")
	public ResponseEntity<List<CommentDto>> getAllCommentsForUser(@PathVariable("username") String username ) {
		return new ResponseEntity<>(commentService.getCommentsByUser(username), HttpStatus.OK);
	}
}

package com.arktech.reddit.service;

import java.util.List;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.arktech.reddit.dto.PostRequest;
import com.arktech.reddit.dto.PostResponse;
import com.arktech.reddit.entity.Post;
import com.arktech.reddit.entity.Subreddit;
import com.arktech.reddit.entity.User;
import com.arktech.reddit.exception.RedditException;
import com.arktech.reddit.mapper.PostMapper;
import com.arktech.reddit.repository.PostRepository;
import com.arktech.reddit.repository.SubredditRepository;
import com.arktech.reddit.repository.UserRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
@Transactional
public class PostService {
	
	private PostRepository postRepository;
	private SubredditRepository subredditRepository;
	private UserRepository userRepository;
	private AuthService authService;
	private PostMapper postMapper;
	
	
	public void save(PostRequest postRequest) {
		Subreddit subreddit = subredditRepository
				.findByName(postRequest.getSubredditName())
				.orElseThrow(() -> new RedditException("Subreddit not found: " + postRequest.getSubredditName()));
		
		postRepository.save(postMapper.map(postRequest, subreddit, authService.getCurrentUser()));
	}

	public List<PostResponse> getAllPosts() {
		return postRepository.findAll()
				.stream()
				.map(postMapper::mapToDto)
				.collect(Collectors.toList());
	}

	public PostResponse getPost(Long id) {
		Post post = postRepository.findById(id)
				.orElseThrow(() -> new RedditException("Post not found: " + id));
		
		return postMapper.mapToDto(post);
	}

	public List<PostResponse> getPostsBySubreddit(Long subredditId) {
		Subreddit subreddit = subredditRepository.findById(subredditId)
				.orElseThrow(() -> new RedditException("Subreddit not found with ID: " + subredditId));
		
		List<Post> posts = postRepository.findAllBySubreddit(subreddit);
		
		return posts.stream()
				.map(postMapper::mapToDto)
				.collect(Collectors.toList());
	}

	public List<PostResponse> getPostsByUsername(String name) {
		User user = userRepository.findByUsername(name)
				.orElseThrow(() -> new RedditException("User not found: " + name));
		
		List<Post> posts = postRepository.findByUser(user);
		
		return posts.stream()
				.map(postMapper::mapToDto)
				.collect(Collectors.toList());
	}
}

package com.arktech.reddit.service;

import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.arktech.reddit.dto.VoteDto;
import com.arktech.reddit.entity.Post;
import com.arktech.reddit.entity.Vote;
import com.arktech.reddit.exception.RedditException;
import com.arktech.reddit.repository.PostRepository;
import com.arktech.reddit.repository.VoteRepository;
import com.arktech.reddit.util.VoteType;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class VoteService {

	private VoteRepository voteRepository;
	private PostRepository postRepository;
	private AuthService authService;
	
	@Transactional
	public void vote(VoteDto voteDto) {
		Post post = postRepository.findById(voteDto.getPostId())
				.orElseThrow(() -> new RedditException("Post not found with ID: " + voteDto.getPostId()));
		
		Optional<Vote> voteByPostAndUser = voteRepository.findTopByPostAndUserOrderByVoteIdDesc(post, authService.getCurrentUser());
		
		if (voteByPostAndUser.isPresent() && voteByPostAndUser.get().getVoteType().equals(voteDto.getVoteType()))
			throw new RedditException("You have already " + voteDto.getVoteType() + "'d for this post");
		
		if (VoteType.UPVOTE.equals(voteDto.getVoteType())) {
			post.setVoteCount(post.getVoteCount() + 1);
		} else {
			post.setVoteCount(post.getVoteCount() - 1);
		}
		
		voteRepository.save(mapToVote(voteDto, post));
		postRepository.save(post);
		
	}

	private Vote mapToVote(VoteDto voteDto, Post post) {
		return Vote.builder()
				.voteType(voteDto.getVoteType())
				.post(post)
				.user(authService.getCurrentUser())
				.build();
	}
}

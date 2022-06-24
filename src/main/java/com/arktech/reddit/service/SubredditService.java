package com.arktech.reddit.service;

import java.util.List;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.arktech.reddit.dto.SubredditDto;
import com.arktech.reddit.entity.Subreddit;
import com.arktech.reddit.exception.RedditException;
import com.arktech.reddit.mapper.SubredditMapper;
import com.arktech.reddit.repository.SubredditRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class SubredditService {
	
	private final SubredditRepository subredditRepository;
	private SubredditMapper subredditMapper;

	@Transactional
	public List<SubredditDto> getAll() {
		return subredditRepository
				.findAll()
				.stream()
				.map(subredditMapper::mapSubredditToDto)
				.collect(Collectors.toList());
	}

	@Transactional
	public SubredditDto getSubreddit(Long id) {
		Subreddit subreddit = subredditRepository.findById(id)
                .orElseThrow(() -> new RedditException("Subreddit not found with id: " + id));
		
        return subredditMapper.mapSubredditToDto(subreddit);
	}

	@Transactional
	public SubredditDto save(SubredditDto subredditDto) {
        Subreddit subreddit = subredditRepository.save(subredditMapper.mapDtoToSubreddit(subredditDto));
        subredditDto.setId(subreddit.getId());
        
        return subredditDto;
	}
	
	// map dto to subreddit
//	private Subreddit mapToSubreddit(SubredditDto subredditDto) {
//		return Subreddit
//				.builder()
//				.name("/r/" + subredditDto.getName())
//                .description(subredditDto.getDescription())
//                .user(authService.getCurrentUser())
//                .createdDate(Instant.now()).build();
//	}

	// map subreddit to dto
//	private SubredditDto mapToDto(Subreddit subreddit) {
//        return SubredditDto
//        		.builder()
//        		.name(subreddit.getName())
//                .id(subreddit.getId())
//                .numberOfPosts(subreddit.getPosts().size())
//                .build();
//    }

}

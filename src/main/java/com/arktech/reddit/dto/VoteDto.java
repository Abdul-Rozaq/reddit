package com.arktech.reddit.dto;

import com.arktech.reddit.util.VoteType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoteDto {

	private VoteType voteType;
	private Long postId;
}

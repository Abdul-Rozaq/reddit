package com.arktech.reddit.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.arktech.reddit.dto.CommentDto;
import com.arktech.reddit.entity.Comment;
import com.arktech.reddit.entity.Post;
import com.arktech.reddit.entity.User;

@Mapper(componentModel = "spring")
public interface CommentMapper {

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "text", source = "commentDto.text")
	@Mapping(target = "post", source = "post")
	@Mapping(target = "createdDate", expression = "java(java.time.Instant.now())")
	Comment map(CommentDto commentDto, Post post, User currentUser);
	
	@Mapping(target = "postId", expression = "java(comment.getPost().getPostId())")
	@Mapping(target = "username", expression = "java(comment.getUser().getUsername())")
	CommentDto mapToDto(Comment comment);

}

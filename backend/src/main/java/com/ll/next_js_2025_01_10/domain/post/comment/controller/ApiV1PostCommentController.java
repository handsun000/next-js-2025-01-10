package com.ll.next_js_2025_01_10.domain.post.comment.controller;

import com.ll.next_js_2025_01_10.domain.member.member.entity.Member;
import com.ll.next_js_2025_01_10.domain.post.comment.dto.PostCommentDto;
import com.ll.next_js_2025_01_10.domain.post.comment.entity.PostComment;
import com.ll.next_js_2025_01_10.domain.post.post.entity.Post;
import com.ll.next_js_2025_01_10.domain.post.post.service.PostService;
import com.ll.next_js_2025_01_10.global.exception.ServiceException;
import com.ll.next_js_2025_01_10.global.rq.Rq;
import com.ll.next_js_2025_01_10.global.rsData.RsData;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/posts/{postId}/comments")
@RequiredArgsConstructor
public class ApiV1PostCommentController {
    private final PostService postService;
    private final Rq rq;

    @GetMapping
    public List<PostCommentDto> items(
            @PathVariable long postId
    ) {
        List<PostComment> comments = postService.findById(postId).get().getComments();

        return comments.stream()
                .map(PostCommentDto::new)
                .toList();
    }

    @DeleteMapping("/{id}")
    public RsData<Void> items(
            @PathVariable long postId,
            @PathVariable long id
    ) {
        Member member = rq.getActor();

        Post post = postService.findById(postId)
                .orElseThrow(() -> new ServiceException("404-1", "%d번 글은 존재하지 않습니다.".formatted(postId)));

        PostComment comment = post.getCommentById(id)
                .orElseThrow(() -> new ServiceException("404-2", "%d번 댓글은 존재하지 않습니다.".formatted(id)));

        comment.checkActorCanDelete(member);

        post.removeComment(comment);

        return new RsData<>(
                "200-1",
                "%d번 댓글이 삭제되었습니다.".formatted(id)
        );
    }

    record ResBodyModify(
            String content
    ){}

    @PutMapping("/{id}")
    @Transactional
    public RsData<PostCommentDto> items(
            @PathVariable long postId,
            @PathVariable long id,
            @RequestBody ResBodyModify resBody
    ) {
        Member member = rq.getActor();

        Post post = postService.findById(postId)
                .orElseThrow(() -> new ServiceException("404-1", "%d번 글은 존재하지 않습니다.".formatted(postId)));

        PostComment comment = post.getCommentById(id)
                .orElseThrow(() -> new ServiceException("404-2", "%d번 댓글은 존재하지 않습니다.".formatted(id)));

        comment.checkActorCanModify(member);

        comment.modify(resBody.content);

        return new RsData<>(
                "200-1",
                "%d번 댓글이 수정되었습니다.".formatted(id),
                new PostCommentDto(comment)
        );
    }

}

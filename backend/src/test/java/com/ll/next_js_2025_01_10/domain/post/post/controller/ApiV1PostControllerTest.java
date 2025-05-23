package com.ll.next_js_2025_01_10.domain.post.post.controller;

import com.ll.next_js_2025_01_10.domain.member.member.entity.Member;
import com.ll.next_js_2025_01_10.domain.member.member.service.MemberService;
import com.ll.next_js_2025_01_10.domain.post.post.entity.Post;
import com.ll.next_js_2025_01_10.domain.post.post.service.PostService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public class ApiV1PostControllerTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private PostService postService;

    @Autowired
    private MemberService memberService;

    @Test
    @DisplayName("1번글 조회")
    void t1() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        get("/api/v1/posts/1")
                )
                .andDo(print());

        Post post = postService.findById(1).get();

        resultActions
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("item"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(post.getId()))
                .andExpect(jsonPath("$.createDate").value(Matchers.startsWith(post.getCreateDate().toString().substring(0, 10))))
                .andExpect(jsonPath("$.createDate").value(Matchers.startsWith(post.getModifyDate().toString().substring(0, 10))))
                .andExpect(jsonPath("$.authorId").value(post.getAuthor().getId()))
                .andExpect(jsonPath("$.authorName").value(post.getAuthor().getName()))
                .andExpect(jsonPath("$.title").value(post.getTitle()))
                .andExpect(jsonPath("$.content").value(post.getContent()))
                .andExpect(jsonPath("$.published").value(post.isPublished()))
                .andExpect(jsonPath("$.listed").value(post.isListed()));
    }

    @Test
    @DisplayName("존재하지 않는 1000000번글 조회, 404")
    void v2() throws Exception {
        mvc
                .perform(
                        get("/api/v1/posts/1000000")
                )
                .andDo(print())
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("item"))
                .andExpect(jsonPath("$.resultCode").value("404-1"))
                .andExpect(jsonPath("$.msg").value("해당 데이터가 존재하지 않습니다."));
    }

    @Test
    @WithUserDetails("user3")
    @DisplayName("글 작성")
    void v3() throws Exception {

        Member actor = memberService.findByUsername("user1").get();
        String authToken = memberService.genAuthToken(actor);

        ResultActions resultActions = mvc
                .perform(
                        post("/api/v1/posts")
                                .header("Authorization", "Bearer " + authToken)
                                .content("""
                                        {
                                            "title" : "제목 new",
                                            "content" : "내용 new",
                                            "published" : "true",
                                            "listed" : "true"
                                        }
                                        """.stripIndent())
                                .contentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
                )
                .andDo(print());

        Post post = postService.findLatest().get();
        assertThat(actor).isEqualTo(post.getAuthor());

        resultActions
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("write"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.resultCode").value("201-1"))
                .andExpect(jsonPath("$.msg").value("%s번 글이 작성 되었습니다.".formatted(post.getId())))
                .andExpect(jsonPath("$.data.id").value(post.getId()))
                .andExpect(jsonPath("$.data.createDate").value(Matchers.startsWith(post.getCreateDate().toString().substring(0, 10))))
                .andExpect(jsonPath("$.data.modifyDate").value(Matchers.startsWith(post.getModifyDate().toString().substring(0, 10))))
                .andExpect(jsonPath("$.data.authorId").value(post.getAuthor().getId()))
                .andExpect(jsonPath("$.data.authorName").value(post.getAuthor().getName()))
                .andExpect(jsonPath("$.data.title").value(post.getTitle()))
                .andExpect(jsonPath("$.data.content").value(post.getContent()))
                .andExpect(jsonPath("$.data.published").value(post.isPublished()))
                .andExpect(jsonPath("$.data.listed").value(post.isListed()));
    }

    @Test
    @DisplayName("글 작성, with no input")
    void t4() throws Exception {
        Member actor = memberService.findByUsername("user1").get();
        String authToken = memberService.genAuthToken(actor);

        mvc
                .perform(
                        post("/api/v1/posts")
                                .header("Authorization", "Bearer " + authToken)
                                .content("""
                                        {
                                            "title" : "",
                                            "content" : ""
                                        }
                                        """)
                                .contentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
                )
                .andDo(print())
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("write"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultCode").value("400-1"))
                .andExpect(jsonPath("$.msg").value("""
                        content-Length-length must be between 2 and 10000000
                        content-NotBlank-must not be blank
                        title-Length-length must be between 2 and 100
                        title-NotBlank-must not be blank
                        """.stripIndent().trim()));
    }

    @Test
    @DisplayName("글 작성, with no actor")
    void t5() throws Exception {

        mvc
                .perform(
                        post("/api/v1/posts")
                                .content("""
                                        {
                                            "title" : "제목 new",
                                            "content" : "내용 new"
                                        }
                                        """)
                                .contentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
                )
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.resultCode").value("401-1"))
                .andExpect(jsonPath("$.msg").value("사용자 인증정보가 올바르지 않습니다."));
    }

    @Test
    @DisplayName("글 수정")
    void t6() throws Exception {

        Member member = memberService.findByUsername("user1").get();
        String authToken = memberService.genAuthToken(member);

        Post post = postService.findById(1).get();

        LocalDateTime oldModifyDate = post.getModifyDate();

        ResultActions resultActions = mvc
                .perform(
                        put("/api/v1/posts/1")
                                .header("Authorization", "Bearer " + authToken)
                                .content("""
                                        {
                                            "title" : "제목 new",
                                            "content" : "내용 new",
                                            "published" : "true",
                                            "listed" : "false"
                                        }
                                        """)
                                .contentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("modify"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.msg").value("%s번 글이 수정되었습니다.".formatted(post.getId())))
                .andExpect(jsonPath("$.data.id").value(post.getId()))
                .andExpect(jsonPath("$.data.createDate").value(Matchers.startsWith(post.getCreateDate().toString().substring(0, 10))))
                .andExpect(jsonPath("$.data.modifyDate").value(Matchers.not(Matchers.startsWith(oldModifyDate.toString().substring(0, 25)))))
                .andExpect(jsonPath("$.data.authorId").value(post.getAuthor().getId()))
                .andExpect(jsonPath("$.data.authorName").value(post.getAuthor().getName()))
                .andExpect(jsonPath("$.data.title").value("제목 new"))
                .andExpect(jsonPath("$.data.content").value("내용 new"))
                .andExpect(jsonPath("$.data.published").value(true))
                .andExpect(jsonPath("$.data.listed").value(false));
    }

    @Test
    @DisplayName("글 수정, with no input ")
    void t7() throws Exception {

        Member member = memberService.findByUsername("user1").get();
        String authToken = memberService.genAuthToken(member);

        ResultActions resultActions = mvc
                .perform(
                        put("/api/v1/posts/1")
                                .header("Authorization", "Bearer " + authToken)
                                .content("""
                                        {
                                            "title" : "",
                                            "content" : ""
                                        }
                                        """)
                                .contentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("modify"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultCode").value("400-1"))
                .andExpect(jsonPath("$.msg").value("""
                        content-Length-length must be between 2 and 10000000
                        content-NotBlank-must not be blank
                        title-Length-length must be between 2 and 100
                        title-NotBlank-must not be blank
                        """.stripIndent().trim()));
    }

    @Test
    @DisplayName("글 수정, with no actor ")
    void t8() throws Exception {

        ResultActions resultActions = mvc
                .perform(
                        put("/api/v1/posts/1")
                                .content("""
                                        {
                                            "title" : "제목 new",
                                            "content" : "내용 new"
                                        }
                                        """)
                                .contentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
                )
                .andDo(print());

        resultActions
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.resultCode").value("401-1"))
                .andExpect(jsonPath("$.msg").value("사용자 인증정보가 올바르지 않습니다."));
    }

    @Test
    @DisplayName("글 수정, with no permission ")
    void t9() throws Exception {

        Member member = memberService.findByUsername("user2").get();
        String authToken = memberService.genAuthToken(member);

        ResultActions resultActions = mvc
                .perform(
                        put("/api/v1/posts/1")
                                .header("Authorization", "Bearer " + authToken)
                                .content("""
                                        {
                                            "title" : "제목 new",
                                            "content" : "내용 new"
                                        }
                                        """)
                                .contentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("modify"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.resultCode").value("403-1"))
                .andExpect(jsonPath("$.msg").value("작성자만 글을 수정할 권한이 있습니다."));
    }

    @Test
    @DisplayName("글 삭제")
    @WithUserDetails("user1")
    void t10() throws Exception {
        mvc
                .perform(
                        delete("/api/v1/posts/1")
                )
                .andDo(print())
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("delete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.msg").value("1번 글이 삭제되었습니다."));

        assertThat(postService.findById(1)).isEmpty();
    }

    @Test
    @DisplayName("글 삭제, with not existing post id")
    void t11() throws Exception {
        Member member = memberService.findByUsername("user1").get();
        String authToken = memberService.genAuthToken(member);

        mvc
                .perform(
                        delete("/api/v1/posts/1000000")
                                .header("Authorization", "Bearer " + authToken)
                )
                .andDo(print())
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("delete"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultCode").value("404-1"))
                .andExpect(jsonPath("$.msg").value("해당 데이터가 존재하지 않습니다."));
    }

    @Test
    @DisplayName("글 삭제, with no actor")
    void t12() throws Exception {
        mvc
                .perform(
                        delete("/api/v1/posts/1000000")
                )
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.resultCode").value("401-1"))
                .andExpect(jsonPath("$.msg").value("사용자 인증정보가 올바르지 않습니다."));
    }

    @Test
    @DisplayName("글 삭제, with no permission")
    void t13() throws Exception {
        Member member = memberService.findByUsername("user2").get();
        String authToken = memberService.genAuthToken(member);

        mvc
                .perform(
                        delete("/api/v1/posts/1")
                                .header("Authorization", "Bearer " + authToken)
                )
                .andDo(print())
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("delete"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.resultCode").value("403-1"))
                .andExpect(jsonPath("$.msg").value("작성자만 글을 삭제할 권한이 있습니다."));
    }

    @Test
    @DisplayName("비공개글 6번글 조회, with 작성자")
    void t14() throws Exception {
        Member member = memberService.findByUsername("user4").get();
        String authToken = memberService.genAuthToken(member);

        ResultActions resultActions = mvc
                .perform(
                        get("/api/v1/posts/6")
                                .header("Authorization", "Bearer " + authToken)
                )
                .andDo(print());

        Post post = postService.findById(6).get();

        resultActions
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("item"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(post.getId()))
                .andExpect(jsonPath("$.createDate").value(Matchers.startsWith(post.getCreateDate().toString().substring(0, 10))))
                .andExpect(jsonPath("$.createDate").value(Matchers.startsWith(post.getModifyDate().toString().substring(0, 10))))
                .andExpect(jsonPath("$.authorId").value(post.getAuthor().getId()))
                .andExpect(jsonPath("$.authorName").value(post.getAuthor().getName()))
                .andExpect(jsonPath("$.title").value(post.getTitle()))
                .andExpect(jsonPath("$.content").value(post.getContent()));
    }

    @Test
    @DisplayName("비공개글 6번글 조회, with no actor")
    void t15() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        get("/api/v1/posts/6")
                )
                .andDo(print());

        resultActions
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.resultCode").value("401-1"))
                .andExpect(jsonPath("$.msg").value("로그인이 필요합니다."));
    }

    @Test
    @DisplayName("비공개글 6번글 조회, with no permission")
    void t16() throws Exception {
        Member member = memberService.findByUsername("user1").get();
        String authToken = memberService.genAuthToken(member);

        ResultActions resultActions = mvc
                .perform(
                        get("/api/v1/posts/6")
                                .header("Authorization", "Bearer " + authToken)
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("item"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.resultCode").value("403-1"))
                .andExpect(jsonPath("$.msg").value("비공개글은 작성자만 볼 수 있습니다."));
    }

    @Test
    @DisplayName("다건 조회")
    void t17() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        get("/api/v1/posts?page=1&pageSize=3")
                )
                .andDo(print());

        Page<Post> postPage = postService
                .findByListedPaged(true, 1, 3);

        resultActions
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItems").value(postPage.getTotalElements()))
                .andExpect(jsonPath("$.totalPages").value(postPage.getTotalPages()))
                .andExpect(jsonPath("$.currentPageNumber").value(1))
                .andExpect(jsonPath("$.pageSize").value(3));

        List<Post> posts = postPage.getContent();

        for (int i = 0; i < posts.size(); i++) {
            Post post = posts.get(i);
            resultActions
                    .andExpect(jsonPath("$.items[%d].id".formatted(i)).value(post.getId()))
                    .andExpect(jsonPath("$.items[%d].createDate".formatted(i)).value(Matchers.startsWith(post.getCreateDate().toString().substring(0, 25))))
                    .andExpect(jsonPath("$.items[%d].modifyDate".formatted(i)).value(Matchers.startsWith(post.getModifyDate().toString().substring(0, 25))))
                    .andExpect(jsonPath("$.items[%d].authorId".formatted(i)).value(post.getAuthor().getId()))
                    .andExpect(jsonPath("$.items[%d].authorName".formatted(i)).value(post.getAuthor().getName()))
                    .andExpect(jsonPath("$.items[%d].title".formatted(i)).value(post.getTitle()))
                    .andExpect(jsonPath("$.items[%d].content".formatted(i)).doesNotExist())
                    .andExpect(jsonPath("$.items[%d].published".formatted(i)).value(post.isPublished()))
                    .andExpect(jsonPath("$.items[%d].listed".formatted(i)).value(post.isListed()));
        }
    }

    @Test
    @DisplayName("다건 조회 with searchKeyword=축구")
    void t18() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        get("/api/v1/posts?page=1&pageSize=3&searchKeyword=축구")
                )
                .andDo(print());

        Page<Post> postPage = postService
                .findByListedPaged(true, "title", "축구", 1, 3);

        resultActions
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItems").value(postPage.getTotalElements()))
                .andExpect(jsonPath("$.totalPages").value(postPage.getTotalPages()))
                .andExpect(jsonPath("$.currentPageNumber").value(1))
                .andExpect(jsonPath("$.pageSize").value(3));

        List<Post> posts = postPage.getContent();

        for (int i = 0; i < posts.size(); i++) {
            Post post = posts.get(i);
            resultActions
                    .andExpect(jsonPath("$.items[%d].id".formatted(i)).value(post.getId()))
                    .andExpect(jsonPath("$.items[%d].createDate".formatted(i)).value(Matchers.startsWith(post.getCreateDate().toString().substring(0, 25))))
                    .andExpect(jsonPath("$.items[%d].modifyDate".formatted(i)).value(Matchers.startsWith(post.getModifyDate().toString().substring(0, 25))))
                    .andExpect(jsonPath("$.items[%d].authorId".formatted(i)).value(post.getAuthor().getId()))
                    .andExpect(jsonPath("$.items[%d].authorName".formatted(i)).value(post.getAuthor().getName()))
                    .andExpect(jsonPath("$.items[%d].title".formatted(i)).value(post.getTitle()))
                    .andExpect(jsonPath("$.items[%d].content".formatted(i)).doesNotExist())
                    .andExpect(jsonPath("$.items[%d].published".formatted(i)).value(post.isPublished()))
                    .andExpect(jsonPath("$.items[%d].listed".formatted(i)).value(post.isListed()));
        }
    }

    @Test
    @DisplayName("다건 조회 with searchKeyword=16")
    void t19() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        get("/api/v1/posts?page=1&pageSize=3&searchKeywordType=content&searchKeyword=16")
                )
                .andDo(print());

        Page<Post> postPage = postService
                .findByListedPaged(true, "content", "16", 1, 3);

        resultActions
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItems").value(postPage.getTotalElements()))
                .andExpect(jsonPath("$.totalPages").value(postPage.getTotalPages()))
                .andExpect(jsonPath("$.currentPageNumber").value(1))
                .andExpect(jsonPath("$.pageSize").value(3));

        List<Post> posts = postPage.getContent();

        for (int i = 0; i < posts.size(); i++) {
            Post post = posts.get(i);
            resultActions
                    .andExpect(jsonPath("$.items[%d].id".formatted(i)).value(post.getId()))
                    .andExpect(jsonPath("$.items[%d].createDate".formatted(i)).value(Matchers.startsWith(post.getCreateDate().toString().substring(0, 25))))
                    .andExpect(jsonPath("$.items[%d].modifyDate".formatted(i)).value(Matchers.startsWith(post.getModifyDate().toString().substring(0, 25))))
                    .andExpect(jsonPath("$.items[%d].authorId".formatted(i)).value(post.getAuthor().getId()))
                    .andExpect(jsonPath("$.items[%d].authorName".formatted(i)).value(post.getAuthor().getName()))
                    .andExpect(jsonPath("$.items[%d].title".formatted(i)).value(post.getTitle()))
                    .andExpect(jsonPath("$.items[%d].content".formatted(i)).doesNotExist())
                    .andExpect(jsonPath("$.items[%d].published".formatted(i)).value(post.isPublished()))
                    .andExpect(jsonPath("$.items[%d].listed".formatted(i)).value(post.isListed()));
        }
    }

    @Test
    @DisplayName("내글, 다건 조회")
    void t20() throws Exception {

        Member author = memberService.findByUsername("user4").get();
        String authToken = memberService.genAuthToken(author);

        ResultActions resultActions = mvc
                .perform(
                        get("/api/v1/posts/mine?page=1&pageSize=3")
                                .header("Authorization", "Bearer " + authToken)
                )
                .andDo(print());

        Page<Post> postPage = postService
                .findByAuthorPaged(author, 1, 3);

        resultActions
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("mine"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItems").value(postPage.getTotalElements()))
                .andExpect(jsonPath("$.totalPages").value(postPage.getTotalPages()))
                .andExpect(jsonPath("$.currentPageNumber").value(1))
                .andExpect(jsonPath("$.pageSize").value(3));

        List<Post> posts = postPage.getContent();

        for (int i = 0; i < posts.size(); i++) {
            Post post = posts.get(i);
            resultActions
                    .andExpect(jsonPath("$.items[%d].id".formatted(i)).value(post.getId()))
                    .andExpect(jsonPath("$.items[%d].createDate".formatted(i)).value(Matchers.startsWith(post.getCreateDate().toString().substring(0, 20))))
                    .andExpect(jsonPath("$.items[%d].modifyDate".formatted(i)).value(Matchers.startsWith(post.getModifyDate().toString().substring(0, 20))))
                    .andExpect(jsonPath("$.items[%d].authorId".formatted(i)).value(post.getAuthor().getId()))
                    .andExpect(jsonPath("$.items[%d].authorName".formatted(i)).value(post.getAuthor().getName()))
                    .andExpect(jsonPath("$.items[%d].title".formatted(i)).value(post.getTitle()))
                    .andExpect(jsonPath("$.items[%d].content".formatted(i)).doesNotExist())
                    .andExpect(jsonPath("$.items[%d].published".formatted(i)).value(post.isPublished()))
                    .andExpect(jsonPath("$.items[%d].listed".formatted(i)).value(post.isListed()));
        }
    }

    @Test
    @DisplayName("내글, 다건 조회 with searchKeyword=수영")
    void t21() throws Exception {

        Member author = memberService.findByUsername("user4").get();
        String authToken = memberService.genAuthToken(author);

        ResultActions resultActions = mvc
                .perform(
                        get("/api/v1/posts/mine?page=1&pageSize=3&searchKeyword=수영")
                                .header("Authorization", "Bearer " + authToken)
                )
                .andDo(print());

        Page<Post> postPage = postService
                .findByAuthorPaged(author, "title", "수영", 1, 3);

        resultActions
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("mine"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItems").value(postPage.getTotalElements()))
                .andExpect(jsonPath("$.totalPages").value(postPage.getTotalPages()))
                .andExpect(jsonPath("$.currentPageNumber").value(1))
                .andExpect(jsonPath("$.pageSize").value(3));

        List<Post> posts = postPage.getContent();

        for (int i = 0; i < posts.size(); i++) {
            Post post = posts.get(i);
            resultActions
                    .andExpect(jsonPath("$.items[%d].id".formatted(i)).value(post.getId()))
                    .andExpect(jsonPath("$.items[%d].createDate".formatted(i)).value(Matchers.startsWith(post.getCreateDate().toString().substring(0, 25))))
                    .andExpect(jsonPath("$.items[%d].modifyDate".formatted(i)).value(Matchers.startsWith(post.getModifyDate().toString().substring(0, 25))))
                    .andExpect(jsonPath("$.items[%d].authorId".formatted(i)).value(post.getAuthor().getId()))
                    .andExpect(jsonPath("$.items[%d].authorName".formatted(i)).value(post.getAuthor().getName()))
                    .andExpect(jsonPath("$.items[%d].title".formatted(i)).value(post.getTitle()))
                    .andExpect(jsonPath("$.items[%d].content".formatted(i)).doesNotExist())
                    .andExpect(jsonPath("$.items[%d].published".formatted(i)).value(post.isPublished()))
                    .andExpect(jsonPath("$.items[%d].listed".formatted(i)).value(post.isListed()));
        }
    }

    @Test
    @DisplayName("내글, 다건 조회 with searchKeyword=16")
    void t22() throws Exception {
        Member author = memberService.findByUsername("user4").get();
        String authToken = memberService.genAuthToken(author);

        ResultActions resultActions = mvc
                .perform(
                        get("/api/v1/posts/mine?page=1&pageSize=3&searchKeywordType=content&searchKeyword=16")
                                .header("Authorization", "Bearer " + authToken)
                )
                .andDo(print());

        Page<Post> postPage = postService
                .findByAuthorPaged(author, "content", "16", 1, 3);

        resultActions
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("mine"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItems").value(postPage.getTotalElements()))
                .andExpect(jsonPath("$.totalPages").value(postPage.getTotalPages()))
                .andExpect(jsonPath("$.currentPageNumber").value(1))
                .andExpect(jsonPath("$.pageSize").value(3));

        List<Post> posts = postPage.getContent();

        for (int i = 0; i < posts.size(); i++) {
            Post post = posts.get(i);
            resultActions
                    .andExpect(jsonPath("$.items[%d].id".formatted(i)).value(post.getId()))
                    .andExpect(jsonPath("$.items[%d].createDate".formatted(i)).value(Matchers.startsWith(post.getCreateDate().toString().substring(0, 25))))
                    .andExpect(jsonPath("$.items[%d].modifyDate".formatted(i)).value(Matchers.startsWith(post.getModifyDate().toString().substring(0, 25))))
                    .andExpect(jsonPath("$.items[%d].authorId".formatted(i)).value(post.getAuthor().getId()))
                    .andExpect(jsonPath("$.items[%d].authorName".formatted(i)).value(post.getAuthor().getName()))
                    .andExpect(jsonPath("$.items[%d].title".formatted(i)).value(post.getTitle()))
                    .andExpect(jsonPath("$.items[%d].content".formatted(i)).doesNotExist())
                    .andExpect(jsonPath("$.items[%d].published".formatted(i)).value(post.isPublished()))
                    .andExpect(jsonPath("$.items[%d].listed".formatted(i)).value(post.isListed()));
        }
    }

    @Test
    @DisplayName("관리자는 통계를 볼 수 있다.")
    @WithUserDetails("admin")
    void t23() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        get("/api/v1/posts/statistics")
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPostCount").isNumber())
                .andExpect(jsonPath("$.totalPublishedPostCount").isNumber())
                .andExpect(jsonPath("$.totalListedPostCount").isNumber());
    }

    @Test
    @DisplayName("일반 유저는 통계를 볼 수 없다.")
    @WithUserDetails("user1")
    void t24() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        get("/api/v1/posts/statistics")
                )
                .andDo(print());

        resultActions
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.resultCode").value("403-1"))
                .andExpect(jsonPath("$.msg").value("접근 권한이 없습니다."));
    }
}

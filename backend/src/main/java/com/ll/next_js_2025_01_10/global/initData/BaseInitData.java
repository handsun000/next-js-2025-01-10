
package com.ll.next_js_2025_01_10.global.initData;

import com.ll.next_js_2025_01_10.domain.member.member.entity.Member;
import com.ll.next_js_2025_01_10.domain.member.member.service.MemberService;
import com.ll.next_js_2025_01_10.domain.post.post.entity.Post;
import com.ll.next_js_2025_01_10.domain.post.post.service.PostService;
import com.ll.next_js_2025_01_10.global.app.AppConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Transactional;

@Configuration
@RequiredArgsConstructor
public class BaseInitData {
    @Autowired
    @Lazy
    private BaseInitData self;

    private final PostService postService;
    private final MemberService memberService;

    @Bean
    public ApplicationRunner baseInitDataApplicationRunner() {
        return args -> {
            self.work1();
            self.work2();
        };
    }

    @Transactional
    public void work1() {
        if (memberService.count() > 0) return;

        Member memberSystem = memberService.join("system", "1234system", "시스템");
        if (AppConfig.isNotProd()) memberSystem.setApiKey("system");
        Member memberAdmin = memberService.join("admin", "1234admin", "관리자");
        if (AppConfig.isNotProd()) memberAdmin.setApiKey("admin");
        Member memberUser1 = memberService.join("user1", "1234user1", "유저1");
        if (AppConfig.isNotProd()) memberUser1.setApiKey("user1");
        Member memberUser2 = memberService.join("user2", "1234user2", "유저2");
        if (AppConfig.isNotProd()) memberUser2.setApiKey("user2");
        Member memberUser3 = memberService.join("user3", "1234user3", "유저3");
        if (AppConfig.isNotProd()) memberUser3.setApiKey("user3");
        Member memberUser4 = memberService.join("user4", "1234user4", "유저4");
        if (AppConfig.isNotProd()) memberUser4.setApiKey("user4");
    }

    @Transactional
    public void work2() {
        if (postService.count() > 0) return;

        Member user1 = memberService.findByUsername("user1").get();
        Member user2 = memberService.findByUsername("user2").get();
        Member user3 = memberService.findByUsername("user3").get();
        Member user4 = memberService.findByUsername("user4").get();

        Post post1 = postService.write(user1,"축구 하실 분?", "14시 까지 22명을 모아야 합니다.", true, true);
        post1.addComment(user2, "저요!");
        post1.addComment(user1, "저도할래요");
        post1.addComment(user3, "저요!");

        Post post2 = postService.write(user1, "배구 하실 분?", "15시 까지 12명을 모아야 합니다.", true, true);
        post2.addComment(user2, "저요!");
        post2.addComment(user1, "저는 빠질게요");
        post2.addComment(user3, "저요!");

        Post post3 = postService.write(user2, "농구 하실 분?", "16시 까지 10명을 모아야 합니다.", true, true);
        post3.addComment(user2, "저요!");
        post3.addComment(user1, "저도할래요");
        post3.addComment(user3, "저요!");

        Post post4 = postService.write(user3, "당구 하실 분?", "17시 까지 11명을 모아야 합니다.", true, true);
        Post post5 = postService.write(user4, "볼링 하실 분?", "18시 까지 14명을 모아야 합니다.", true, true);
        Post post6 = postService.write(user4, "수영 하실 분?", "19시 까지 16명을 모아야 합니다.", false, false);
        Post post7 = postService.write(user4, "수영 하실 분?", "19시 까지 16명을 모아야 합니다.", true, false);
        Post post8 = postService.write(user4, "수영 하실 분?", "19시 까지 16명을 모아야 합니다.", false, true);


    }
}
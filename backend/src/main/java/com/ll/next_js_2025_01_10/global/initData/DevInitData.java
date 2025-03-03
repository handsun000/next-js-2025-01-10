package com.ll.next_js_2025_01_10.global.initData;

import com.ll.next_js_2025_01_10.domain.member.member.service.MemberService;
import com.ll.next_js_2025_01_10.domain.post.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;

@Profile("dev")
@Configuration
@RequiredArgsConstructor
public class DevInitData {
    private final MemberService memberService;
    private final PostService postService;
    @Autowired
    @Lazy
    private DevInitData self;

    @Bean
    public ApplicationRunner devInitDataApplicationRunner() {
        return args -> {
            com.ll.spring_doc_2025_01_09.standard.util.Ut.file.downloadByHttp("http://localhost:8080/v3/api-docs/apiV1", ".");

            String cmd = "echo y | npx --package typescript --package openapi-typescript openapi-typescript apiV1.json -o ../frontend/src/lib/backend/apiV1/schema.d.ts";
            com.ll.spring_doc_2025_01_09.standard.util.Ut.cmd.runAsync(cmd);
        };
    }
}
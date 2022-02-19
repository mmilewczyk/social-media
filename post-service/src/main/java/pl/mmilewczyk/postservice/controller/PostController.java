package pl.mmilewczyk.postservice.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.mmilewczyk.postservice.model.dto.PostRequest;
import pl.mmilewczyk.postservice.model.dto.PostResponse;
import pl.mmilewczyk.postservice.service.PostService;

@RestController
@RequestMapping(path = "api/v1/posts")
public record PostController(PostService postService) {

    @PostMapping
    public ResponseEntity<PostResponse> createNewPost(@RequestBody PostRequest postRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(postService.createNewPost(postRequest));
    }

    @GetMapping("/{username}")
    public ResponseEntity<Page<PostResponse>> getSomeonePostsByUsername(@PathVariable("username") String username, Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(postService.getSomeonePostsByUsername(username, pageable));
    }
}

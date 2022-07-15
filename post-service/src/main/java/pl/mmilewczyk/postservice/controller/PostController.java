package pl.mmilewczyk.postservice.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.mmilewczyk.postservice.model.dto.PostRequest;
import pl.mmilewczyk.postservice.model.dto.PostResponse;
import pl.mmilewczyk.postservice.model.dto.PostResponseLite;
import pl.mmilewczyk.postservice.service.PostService;

import static org.springframework.http.ResponseEntity.status;

@RestController
@CrossOrigin (origins = "*")
@RequestMapping(path = "api/v1/posts")
public record PostController(PostService postService) {

    @PostMapping
    public ResponseEntity<PostResponse> createNewPost(@RequestBody PostRequest postRequest) {
        return status(HttpStatus.CREATED).body(postService.createNewPost(postRequest));
    }

    @GetMapping("/all")
    public ResponseEntity<Page<PostResponseLite>> getAllLatestPosts() {
        return status(HttpStatus.OK).body(postService.getAllLatestPosts());
    }

    @GetMapping("/search/followed")
    public ResponseEntity<Page<PostResponseLite>> getAllLatestPostsOfFollowedPeople() {
        return status(HttpStatus.OK).body(postService.getAllLatestPostsOfFollowedPeople());
    }

    @GetMapping("/{username}")
    public ResponseEntity<Page<PostResponseLite>> getSomeonePostsByUsername(@PathVariable("username") String username) {
        return status(HttpStatus.OK).body(postService.getSomeonePostsByUsername(username));
    }

    @GetMapping("/search/id")
    public ResponseEntity<PostResponse> getPostById(@RequestParam("id") Long postId) {
        return status(HttpStatus.OK).body(postService.getPostById(postId));
    }

    @GetMapping("/search/title")
    public ResponseEntity<Page<PostResponseLite>> getPostByTitle(@RequestParam("title") String title) {
        return status(HttpStatus.OK).body(postService.getPostByTitle(title));
    }

    @DeleteMapping("/{postId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePostById(@PathVariable("postId") Long postId) {
        postService.deletePostById(postId);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostResponse> updatePostById(@RequestBody PostRequest postRequest,
                                                       @PathVariable("id") Long postId) {
        return status(HttpStatus.CREATED).body(postService.updatePostById(postRequest, postId));
    }
}

package pl.mmilewczyk.clients.post;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("POST-SERVICE")
public interface PostClient {

    String BASE_URL = "api/v1/posts/";

    @GetMapping(BASE_URL + "search/id")
    ResponseEntity<PostResponse> getPostById(@RequestParam("id") Long postId);

    @PostMapping(BASE_URL)
    ResponseEntity<PostResponse> createNewPost(@RequestBody PostRequest postRequest);
}

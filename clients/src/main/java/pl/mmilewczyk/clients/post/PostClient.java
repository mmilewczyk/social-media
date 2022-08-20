package pl.mmilewczyk.clients.post;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("POST-SERVICE")
public interface PostClient {

    @GetMapping("api/v1/posts/search/id")
    ResponseEntity<PostResponse> getPostById(@RequestParam("id") Long postId);

}

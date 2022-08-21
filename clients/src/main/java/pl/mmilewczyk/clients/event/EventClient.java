package pl.mmilewczyk.clients.event;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("EVENT-SERVICE")
public interface EventClient {

    @GetMapping("api/v1/events")
    ResponseEntity<EventResponse> getEventById(@RequestParam Long eventId);
}

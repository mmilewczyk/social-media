package pl.mmilewczyk.clients.event;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("EVENT-SERVICE")
public interface EventClient {

    String BASE_URL = "api/v1/events/";

    @GetMapping(BASE_URL)
    ResponseEntity<EventResponse> getEventById(@RequestParam Long eventId);

    @GetMapping(BASE_URL + "tech/isUserAdminOrModerator")
    boolean isEventAdminOrModerator(@RequestParam Long userId, @RequestParam Long eventId);
}

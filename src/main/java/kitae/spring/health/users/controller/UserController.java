package kitae.spring.health.users.controller;

import kitae.spring.health.response.Response;
import kitae.spring.health.users.dto.UserDTO;
import kitae.spring.health.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<Response<UserDTO>> getMyUserDetails() {
        return ResponseEntity.ok(userService.getMyUserDetails());
    }
}

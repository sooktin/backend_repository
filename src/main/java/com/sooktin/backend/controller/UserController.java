package com.sooktin.backend.controller;

import com.sooktin.backend.domain.User;
import com.sooktin.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("/register")
    public String showRegister(){
        return "good";
    }
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user){
        try {
            User registeredUser = userService.registerUser(user);
            return ResponseEntity.ok("회원가입에 성공하셨습니다. 이메일 인증을 위하여 이메일함을 확인해주세요. ");
        } catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/2fa")
    public ResponseEntity<?> confirmEmail(@RequestParam("token") String token) {
        String result = userService.confirmEmail(token);
        if (result.equals("이메일 인증에 성공하였습니다.")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }
}

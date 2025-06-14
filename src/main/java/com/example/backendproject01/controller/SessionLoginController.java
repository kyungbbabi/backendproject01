package com.example.backendproject01.controller;

import com.example.backendproject01.dto.JoinRequest;
import com.example.backendproject01.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/")
public class SessionLoginController {

    private final UserService userService;

    // Model은 컨트롤러에서 HTML로 데이터를 전달하는 수단이고, addAttribute는 "이 데이터를 HTML에서 사용할 수 있게 해줘" 라는 의미입니다
    @GetMapping("/join")
    public String joinPage(Model model) {

        model.addAttribute("siteName", "Backend Project 01");
        model.addAttribute("loginType", "session-login");
        model.addAttribute("joinRequest", new JoinRequest());
        return "join";

    }

    @PostMapping("/join")
    public String join(JoinRequest joinRequest, BindingResult bindingResult) {

        if (userService.checkLoginIdDuplicate(joinRequest.getLoginId())) {
            bindingResult.addError(new FieldError("joinRequest", "loginId", "로그인 아이디가 중복됩니다."));
        }
        if (!joinRequest.getPassword().equals(joinRequest.getPasswordCheck())) {
            bindingResult.addError(new FieldError("joinRequest", "passwordCheck", "비밀번호가 일치하지 않습니다."));
        }

        if(bindingResult.hasErrors()) {
            return "join";
        }

        // validation 에러가 있으면 다시 폼으로
        userService.join(joinRequest);
        return "redirect:/session-login";
    }
}

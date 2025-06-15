package com.example.backendproject01.controller;

import com.example.backendproject01.dto.JoinRequest;
import com.example.backendproject01.dto.LoginRequest;
import com.example.backendproject01.entity.User;
import com.example.backendproject01.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
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
    public String join(JoinRequest joinRequest, BindingResult bindingResult, Model model) {

        if (userService.checkLoginIdDuplicate(joinRequest.getLoginId())) {
            bindingResult.addError(new FieldError("joinRequest", "loginId", "로그인 아이디가 중복됩니다."));
        }
        if (!joinRequest.getPassword().equals(joinRequest.getPasswordCheck())) {
            bindingResult.addError(new FieldError("joinRequest", "passwordCheck", "비밀번호가 일치하지 않습니다."));
        }

        // validation 에러가 있으면 다시 폼으로
        if(bindingResult.hasErrors()) {
            model.addAttribute("siteName", "Backend Project 01");
            model.addAttribute("loginType", "session-login");
            return "join";
        }

        userService.join(joinRequest);
        return "/login";
    }

    @GetMapping("/login")
    public String loginPage(Model model) {

        model.addAttribute("siteName", "Backend Project 01");
        model.addAttribute("loginType", "session-login");
        model.addAttribute("loginRequest", new LoginRequest());

        return "login";

    }

    @PostMapping("/login")
    public String login(LoginRequest loginRequest, HttpServletRequest httpServletRequest){
        try {
            // 1️⃣ 로그인 검증 및 사용자 정보 가져오기, UserService에서 아이디/비밀번호 확인 후 User 객체 반환
            User user = userService.login(loginRequest);

            // 세션에 로그인 정보 저장, 이제 다른 페이지에서도 session.getAttribute("user")로 접근 가능
            HttpSession session = httpServletRequest.getSession();
            session.setAttribute("user", user);

            return "redirect:/";

        } catch (Exception e) {
            return "/login";
        }

    }

    @GetMapping("/")
    public String indexPage(Model model, HttpServletRequest httpServletRequest) {

        HttpSession session = httpServletRequest.getSession();
        if (session != null) {
            User user = (User) session.getAttribute("user");
            if (user != null) {
                model.addAttribute("loginUser", user);
                model.addAttribute("siteName", "Backend Project 01");

                return "index";
            }
        }
        return "redirect:/login";
    }

}

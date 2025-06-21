package com.example.backendproject01.controller;

import com.example.backendproject01.dto.MemoRequest;
import com.example.backendproject01.entity.Memo;
import com.example.backendproject01.entity.User;
import com.example.backendproject01.service.MemoSessionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/memosession")
public class MemoSessionController {

    private final MemoSessionService memoSessionServiceService;

    /** 세션에서 사용자 정보 추출하는 공통 메서드 */
//    private User getLoginUser(HttpServletRequest request) {
//        HttpSession session = request.getSession();
//        return (User) session.getAttribute("user");
//    }

    @GetMapping
    public String memoList(HttpServletRequest request) {

        HttpSession session = request.getSession();
        User loginUser = (User) session.getAttribute("loginUser");

        List<Memo> memos;
        return "";

    }

    @GetMapping("/{id}")
    public String memoDetail(@PathVariable Long id, HttpServletRequest request) {

        HttpSession session = request.getSession();
        User loginUser = (User) session.getAttribute("loginUser");

        Memo memo = memoSessionServiceService.readMemoById(id);

        return "redirect:/memo/" + memo.getTitle();

    }

    @GetMapping("/create")
    public String createMemo(Model model, HttpServletRequest request) {

        HttpSession session = request.getSession();
        User loginUser = (User) session.getAttribute("loginUser");

        model.addAttribute("memoRequest", new MemoRequest());

        return "memo/create";
    }

    @PostMapping("/create")
    public String createMemo(MemoRequest memoRequest, HttpServletRequest request) {

        HttpSession session = request.getSession();
        User loginUser = (User) session.getAttribute("loginUser");

        Memo createMemo = memoSessionServiceService.createMemo(memoRequest);
        return "redirect:/memo/" + createMemo.getTitle();

    }

    @GetMapping("/edit")
    public String editMemo(@PathVariable Long id, Model model, HttpServletRequest request) {

        HttpSession session = request.getSession();
        User loginUser = (User) session.getAttribute("loginUser");

        Memo memo = memoSessionServiceService.readMemoById(id);
        model.addAttribute("memo", memo);

        return "memo/edit";

    }

    @PostMapping("/edit")
    public String editMemo(@PathVariable Long id, MemoRequest memoRequest, HttpServletRequest request) {

        HttpSession session = request.getSession();
        User loginUser = (User) session.getAttribute("loginUser");

        Memo memo = memoSessionServiceService.readMemoById(id);

        try {
            memoSessionServiceService.updateMemo(id, memoRequest);
            return "redirect:/memo/" + id;
        } catch (Exception e) {
            return "redirect:/memo/" + id;
        }

    }

    @PostMapping("/delete")
    public String deleteMemo(@PathVariable Long id, HttpServletRequest request) {

        HttpSession session = request.getSession();
        User loginUser = (User) session.getAttribute("loginUser");

        memoSessionServiceService.deleteMemo(memoSessionServiceService.readMemoById(id));
        return "redirect:/memo/";

    }

    @GetMapping("/search")
    public String searchMemo(@RequestParam(required = false) String keyword, Model model, HttpServletRequest request) {

        HttpSession session = request.getSession();
        User loginUser = (User) session.getAttribute("loginUser");

        List<Memo> memos;
        if (keyword != null && !keyword.trim().isEmpty()) {
            memos = memoSessionServiceService.searchMemoByKeyword(keyword);
        } else {
            memos = memoSessionServiceService.getAllMemo();
        }

        model.addAttribute("memos", memos);
        return "memo";

    }

}

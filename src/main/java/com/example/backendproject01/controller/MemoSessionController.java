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

    private final MemoSessionService memoSessionService;

    /** 세션에서 사용자 정보 추출하는 공통 메서드 */
    private User getLoginUser(HttpServletRequest request) {
        HttpSession session = request.getSession();
        return (User) session.getAttribute("user");
    }

    @GetMapping
    public String memoList(HttpServletRequest request, Model model) {

        User loginUser = getLoginUser(request);
        if (loginUser == null) {
            return "redirect:/login";
        }

        List<Memo> memos = memoSessionService.getAllMemo();
        model.addAttribute("memos", memos);
        model.addAttribute("loginUser", loginUser);

        return "memo/list";
    }

    @GetMapping("/{id}")
    public String memoDetail(@PathVariable Long id, HttpServletRequest request, Model model) {

        User loginUser = getLoginUser(request);
        if (loginUser == null) {
            return "redirect:/login";
        }

        Memo memo = memoSessionService.readMemoById(id);
        model.addAttribute("memo", memo);
        model.addAttribute("loginUser", loginUser);

        return "memo/detail";
    }

    @GetMapping("/create")
    public String createMemo(Model model, HttpServletRequest request) {

        User loginUser = getLoginUser(request);
        if (loginUser == null) {
            return "redirect:/login";
        }

        model.addAttribute("memoRequest", new MemoRequest());
        model.addAttribute("loginUser", loginUser);

        return "memo/create";
    }

    @PostMapping("/create")
    public String createMemo(MemoRequest memoRequest, HttpServletRequest request) {

        User loginUser = getLoginUser(request);
        if (loginUser == null) {
            return "redirect:/login";
        }

        // User 매개변수 전달
        Memo createMemo = memoSessionService.createMemo(memoRequest, loginUser);
        return "redirect:/memosession/" + createMemo.getId();
    }

    @GetMapping("/{id}/edit")
    public String editMemo(@PathVariable Long id, Model model, HttpServletRequest request) {

        User loginUser = getLoginUser(request);
        if (loginUser == null) {
            return "redirect:/login";
        }

        Memo memo = memoSessionService.readMemoById(id);

        // 권한 체크
        if (!memo.canBeEditedBy(loginUser)) {
            return "redirect:/memosession/" + id + "?error=unauthorized";
        }

        model.addAttribute("memo", memo);
        model.addAttribute("loginUser", loginUser);

        return "memo/edit";
    }

    @PostMapping("/{id}/edit")
    public String editMemo(@PathVariable Long id, MemoRequest memoRequest, HttpServletRequest request) {

        User loginUser = getLoginUser(request);
        if (loginUser == null) {
            return "redirect:/login";
        }

        try {
            Memo memo = memoSessionService.readMemoById(id);

            // 권한 체크
            if (!memo.canBeEditedBy(loginUser)) {
                return "redirect:/memosession/" + id + "?error=unauthorized";
            }

            memoSessionService.updateMemo(id, memoRequest);
            return "redirect:/memosession/" + id;
        } catch (Exception e) {
            return "redirect:/memosession/" + id + "?error=update_failed";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteMemo(@PathVariable Long id, HttpServletRequest request) {

        User loginUser = getLoginUser(request);
        if (loginUser == null) {
            return "redirect:/login";
        }

        try {
            Memo memo = memoSessionService.readMemoById(id);

            // 권한 체크
            if (!memo.canBeDeletedBy(loginUser)) {
                return "redirect:/memosession/" + id + "?error=unauthorized";
            }

            memoSessionService.deleteMemo(memo);
            return "redirect:/memosession";
        } catch (Exception e) {
            return "redirect:/memosession/" + id + "?error=delete_failed";
        }
    }

    @GetMapping("/search")
    public String searchMemo(@RequestParam(required = false) String keyword, Model model, HttpServletRequest request) {

        User loginUser = getLoginUser(request);
        if (loginUser == null) {
            return "redirect:/login";
        }

        List<Memo> memos;
        if (keyword != null && !keyword.trim().isEmpty()) {
            memos = memoSessionService.searchMemoByKeyword(keyword);
        } else {
            memos = memoSessionService.getAllMemo();
        }

        model.addAttribute("memos", memos);
        model.addAttribute("keyword", keyword);
        model.addAttribute("loginUser", loginUser);

        return "memo/search";
    }
}
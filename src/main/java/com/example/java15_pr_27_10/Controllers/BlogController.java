package com.example.java15_pr_27_10.Controllers;

import com.example.java15_pr_27_10.Models.Post;
import com.example.java15_pr_27_10.repo.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class BlogController {
    @Autowired
    private PostRepository postRepository;
    @GetMapping("/posts")
    public String Post(Model model){
        Iterable<Post> posts = postRepository.findAll();
        model.addAttribute("list",posts);
        return "posts";
    }

}

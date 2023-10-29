package com.example.java15_pr_27_10.repo;

import com.example.java15_pr_27_10.Models.Post;
import org.springframework.data.repository.CrudRepository;

public interface PostRepository extends CrudRepository<Post,Long> {

}

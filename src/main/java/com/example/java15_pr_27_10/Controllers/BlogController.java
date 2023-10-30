package com.example.java15_pr_27_10.Controllers;

import com.azure.storage.blob.*;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobAccessPolicy;
import com.azure.storage.blob.models.BlobSignedIdentifier;
import com.azure.storage.blob.models.PublicAccessType;
import com.example.java15_pr_27_10.Models.Post;
import com.example.java15_pr_27_10.repo.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;

@Controller
public class BlogController {
    @Autowired
    private PostRepository postRepository;
    private BlobServiceClient client;
    private BlobContainerClient container;
    public BlogController(){
        BlobServiceClientBuilder builder = new BlobServiceClientBuilder();
        client = builder.connectionString("UseDevelopmentStorage=true").buildClient();
        BlobSignedIdentifier identifier = new BlobSignedIdentifier().setId("test policy")
                .setAccessPolicy(new BlobAccessPolicy().setStartsOn(OffsetDateTime.now())
                        .setExpiresOn(OffsetDateTime.now().plusDays(7))
                        .setPermissions("cd")); //permission for create and delete

        ArrayList<BlobSignedIdentifier> identifiers = new ArrayList<BlobSignedIdentifier>();
        identifiers.add(identifier);
        container = client.createBlobContainerIfNotExists("posts");
        container.setAccessPolicy(PublicAccessType.CONTAINER,identifiers);
    }
    @GetMapping("/posts")
    public String Post(Model model){
        Iterable<Post> posts = postRepository.findAll();
        model.addAttribute("list",posts);
        return "posts";
    }
    @GetMapping("/posts/create")
    public String create(Model model)
    {
        return "create";
    }
    @PostMapping("/posts/create")
    public String create(@RequestParam String header, @RequestParam String context, @RequestParam MultipartFile image, Model model ) throws FileNotFoundException {
        Post post = new Post();
        post.setContext(context);
        post.setHeader(header);
//        String path = new ClassPathResource("resources/static/").getPath() +image.getOriginalFilename();
        try(
                var stream = image.getInputStream();
                )
        {
            BlobClient cl = container.getBlobClient(image.getOriginalFilename());
            cl.upload(stream,stream.available(),true);
            post.setImage(cl.getBlobUrl());
        }
        catch (Exception ex)
        {
            System.out.println(ex.getMessage());
        }


        postRepository.save(post);
        return "redirect:/posts";
    }

}

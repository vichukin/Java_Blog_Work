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
import org.springframework.web.bind.annotation.PathVariable;
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
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class BlogController {
    @Autowired
    private PostRepository postRepository;
    private BlobServiceClient client;
    private BlobContainerClient container;
    public BlogController(){
        BlobServiceClientBuilder builder = new BlobServiceClientBuilder();
        client = builder.connectionString("UseDevelopmentStorage=true").buildClient();
        //ВАЖНО!!!!
        //azurite --silent --location c:\azurite --debug c:\azurite\debug.log --skipApiVersionCheck
        //Запускать Azurite ИМЕННО с этой команды, иначе выдаст ошибку
        //ВАЖНО!!!!
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
    public String Post(@RequestParam(required = false) String header,@RequestParam(required = false) String desc, Model model){

        Iterable<Post> buf = postRepository.findAll();
        List<Post> posts = new ArrayList<>();
        for(var item: buf)
            posts.add(item);
        if(header!=null&&!header.equals(""))
        {
            model.addAttribute("header",header);
            posts = posts.stream().filter(t->t.getHeader().contains(header)).collect(Collectors.toList());
        }
        if(desc!=null&&!desc.equals(""))
        {
            model.addAttribute("desc",desc);
            posts = posts.stream().filter(t->t.getContext().contains(desc)).collect(Collectors.toList());
        }

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

    @GetMapping("/posts/edit/{id}")
    public String edit(@PathVariable(value = "id")long id,Model model){
        Post post = postRepository.findById(id).orElse(null);
        if(post!=null)
        {
            model.addAttribute("item",post);
            return "edit";

        }
        else
            return "redirect:/posts";
    }
    @PostMapping("/posts/edit")
    public String edit( @RequestParam long id,@RequestParam String header, @RequestParam String context, @RequestParam MultipartFile image,Model model)
    {
        Post post = postRepository.findById(id).orElse(null);
        if(!context.equals(post.getContext()))
            post.setContext(context);
        if(!header.equals(post.getHeader()))
            post.setHeader(header);
        if(image!=null)
        {
            BlobClient cl = container.getBlobClient(post.getImage());
            cl.deleteIfExists();
            cl = container.getBlobClient(image.getOriginalFilename());
            try {
                cl.upload(image.getInputStream());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            post.setImage(cl.getBlobUrl());

        }
        postRepository.save(post);
        return "redirect:/posts";
    }
    @PostMapping("/posts/delete")
    public String delete(@RequestParam long id)
    {
        Post post = postRepository.findById(id).orElse(null);
        if(post!=null)
        {
            BlobClient cl = container.getBlobClient(post.getImage());
            cl.deleteIfExists();
            postRepository.delete(post);

        }
        return "redirect:/posts";
    }

}

package com.aelkz.springboot.skeleton.controller;

import javax.validation.Valid;
import javax.validation.constraints.Size;

import com.aelkz.springboot.skeleton.model.User;
import com.aelkz.springboot.skeleton.model.Info;
import com.aelkz.springboot.skeleton.service.UserService;
import com.aelkz.springboot.skeleton.util.MavenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping(produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
@Validated //required for @Valid on method parameters such as @RequesParam, @PathVariable, @RequestHeader
public class UserController extends BaseController {

    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final String HEADER_USER_ID = "userId";
    public static final String HEADER_TOKEN = "token";

    @Autowired
    UserService userService;

    @RequestMapping(path = "/v1/users", method = RequestMethod.GET)
    @ApiOperation(
            value = "Get all users",
            notes = "Returns first N users specified by the size parameter with page offset specified by page parameter.",
            response = Page.class)
    public Page<User> getAll(
            @ApiParam("The size of the page to be returned") @RequestParam(required = false) Integer size,
            @ApiParam("Zero-based page index") @RequestParam(required = false) Integer page) {

        if (size == null) {
            size = DEFAULT_PAGE_SIZE;
        }
        if (page == null) {
            page = 0;
        }

        Pageable pageable = new PageRequest(page, size);
        Page<User> users = userService.findAll(pageable);

        return users;
    }

    @RequestMapping(path = "/v1/user/{id}", method = RequestMethod.GET)
    @ApiOperation(
            value = "Get user by id",
            notes = "Returns user for id specified.",
            response = User.class)
    @ApiResponses(value = {@ApiResponse(code = 404, message = "User not found") })
    public ResponseEntity<User> get(@ApiParam("User id") @PathVariable("id") Long id) {

        User user = userService.findOne(id);
        return (user == null ? ResponseEntity.status(HttpStatus.NOT_FOUND) : ResponseEntity.ok()).body(user);
    }

    @RequestMapping(path = "/v1/user", method = RequestMethod.PUT, consumes = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
    @ApiOperation(
            value = "Create new or update existing user",
            notes = "Creates new or updates exisitng user. Returns created/updated user with id.",
            response = User.class)
    public ResponseEntity<User> add(
            @Valid @RequestBody User user,
            @Valid @Size(max = 40, min = 8, message = "user id size 8-40") @RequestHeader(name = HEADER_USER_ID) String userId,
            @Valid @Size(max = 40, min = 2, message = "token size 2-40") @RequestHeader(name = HEADER_TOKEN, required = false) String token) {

        user = userService.save(user);
        return ResponseEntity.ok().body(user);
    }

    @RequestMapping(path = "/v1/info", method = RequestMethod.GET)
    @ApiOperation(
            value = "Get application info",
            notes = "Returns application information.",
            response = String.class)
    @ApiResponses(value = {@ApiResponse(code = 404, message = "unknown error") })
    public ResponseEntity<Info> get() {
        return ResponseEntity.ok().body(new Info(MavenUtils.getInstance().artifactVersion(), "raphael abreu", "raphael.alex@gmail.com"));
    }

    @InitBinder("user")
    protected void initBinder(WebDataBinder binder) {
        binder.addValidators(new UserValidator());
    }
}
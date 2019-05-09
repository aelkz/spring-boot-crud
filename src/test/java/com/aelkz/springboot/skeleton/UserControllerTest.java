package com.aelkz.springboot.skeleton;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Date;
import java.util.UUID;

import javax.persistence.EntityManager;

import com.aelkz.springboot.skeleton.model.Address;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.aelkz.springboot.skeleton.model.User;
import com.aelkz.springboot.skeleton.model.User.Gender;
import com.aelkz.springboot.skeleton.service.UserService;
import com.aelkz.springboot.skeleton.controller.UserController;

import net.minidev.json.JSONArray;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@Transactional
public class UserControllerTest extends BaseControllerTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private UserService userService;

    private User testUser;
    private long timestamp;

    @Before
    public void setup() throws Exception {
        super.setup();

        timestamp = new Date().getTime();

        // create test users
        userService.save(createUser("Jack", "Bauer", Gender.M));
        userService.save(createUser("Chloe", "O'Brian", Gender.F));
        userService.save(createUser("Kim", "Bauer", Gender.F));
        userService.save(createUser("David", "Palmer", Gender.M));
        userService.save(createUser("Michelle", "Dessler", Gender.F));

        Page<User> users = userService.findAll(new PageRequest(0, UserController.DEFAULT_PAGE_SIZE));
        assertNotNull(users);
        assertEquals(5L, users.getTotalElements());

        testUser = users.getContent().get(0);

        //refresh entity with any changes that have been done during persistence including Hibernate conversion
        //example: java.util.Date field is injected with either with java.sql.Date (if @Temporal(TemporalType.DATE) is used)
        //or java.sql.Timestamp
        entityManager.refresh(testUser);
    }

    @Test
    public void getUserById() throws Exception {
        Long id = testUser.getId();

        mockMvc.perform(get("/v1/user/{id}", id))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(JSON_MEDIA_TYPE))
                .andExpect(jsonPath("$.id", is(id.intValue())))
                .andExpect(jsonPath("$.firstName", is(testUser.getFirstName())))
                .andExpect(jsonPath("$.lastName", is(testUser.getLastName())))
                .andExpect(jsonPath("$.dateOfBirth", isA(Number.class)))
        ;
    }

    /**
     * Test JSR-303 bean validation.
     */
    @Test
    public void createUserValidationErrorLastName() throws Exception {
        //user with missing last name
        User user = createUser("first", null, Gender.M);

        String content = json(user);
        mockMvc.perform(
                put("/v1/user")
                        .accept(JSON_MEDIA_TYPE)
                        .content(content)
                        .contentType(JSON_MEDIA_TYPE))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(JSON_MEDIA_TYPE))
                .andExpect(jsonPath("$", isA(JSONArray.class)))
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$.[?(@.field == 'lastName')].message", hasItem("must not be null")))
        ;
    }

    /**
     * Test custom bean validation.
     */
    @Test
    public void createUserValidationErrorHandle() throws Exception {
        //user with missing handle - custom validation
        User user = createUser("first", "last", Gender.M);
        user.setHandle(null);
        String content = json(user);
        mockMvc.perform(
                put("/v1/user")
                        .accept(JSON_MEDIA_TYPE)
                        .header(UserController.HEADER_USER_ID, UUID.randomUUID())
                        .content(content)
                        .contentType(JSON_MEDIA_TYPE))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(JSON_MEDIA_TYPE))
                .andExpect(jsonPath("$", isA(JSONArray.class)))
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$.[?(@.field == 'handle')].message", hasItem("must not be null")))
        ;
    }

    /**
     * Test JSR-303 bean object graph validation with nested entities.
     */
    @Test
    public void createUserValidationAddress() throws Exception {
        User user = createUser("first", "last", Gender.M);
        user.addAddress(new Address("line1", "city", "state", "zip"));
        user.addAddress(new Address()); //invalid address

        String content = json(user);
        mockMvc.perform(
                put("/v1/user")
                        .accept(JSON_MEDIA_TYPE)
                        .content(content)
                        .contentType(JSON_MEDIA_TYPE))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(JSON_MEDIA_TYPE))
                .andExpect(jsonPath("$.length()", is(4)))
                .andExpect(jsonPath("$.[?(@.field == 'addresses[].line1')].message", hasItem("must not be null")))
                .andExpect(jsonPath("$.[?(@.field == 'addresses[].state')].message", hasItem("must not be null")))
                .andExpect(jsonPath("$.[?(@.field == 'addresses[].city')].message", hasItem("must not be null")))
                .andExpect(jsonPath("$.[?(@.field == 'addresses[].zip')].message", hasItem("must not be null")))
        ;
    }

    @Test
    public void createUserValidationToken() throws Exception {
        User user = createUser("first", "last", Gender.M);
        String content = json(user);
        mockMvc.perform(
                put("/v1/user")
                        .header(UserController.HEADER_USER_ID, UUID.randomUUID())
                        .header(UserController.HEADER_TOKEN, "1") //invalid token
                        .accept(JSON_MEDIA_TYPE)
                        .content(content)
                        .contentType(JSON_MEDIA_TYPE))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(JSON_MEDIA_TYPE))
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$.[?(@.field == 'add.token')].message", hasItem("token size 2-40")))
        ;
    }

    @Test
    public void createUserValidationUserId() throws Exception {
        User user = createUser("first", "last", Gender.M);
        String content = json(user);
        mockMvc.perform(
                put("/v1/user")
                        .accept(JSON_MEDIA_TYPE)
                        .content(content)
                        .contentType(JSON_MEDIA_TYPE))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(JSON_MEDIA_TYPE))
                .andExpect(jsonPath("$.message", containsString("Missing request header '"+ UserController.HEADER_USER_ID)))
        ;
    }

    @Test
    public void createUser() throws Exception {
        User user = createUser("first", "last", Gender.M);
        String content = json(user);

        mockMvc.perform(
                put("/v1/user")
                        .header(UserController.HEADER_USER_ID, UUID.randomUUID())
                        .accept(JSON_MEDIA_TYPE)
                        .content(content)
                        .contentType(JSON_MEDIA_TYPE))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", isA(Number.class)))
                .andExpect(jsonPath("$.firstName", is(user.getFirstName())))
                .andExpect(jsonPath("$.lastName", is(user.getLastName())))
                .andExpect(jsonPath("$.dateOfBirth", isA(Number.class)))
                .andExpect(jsonPath("$.dateOfBirth", is(user.getDateOfBirth().getTime())))
        ;
    }

    @Test
    public void createUserWithDateVerification() throws Exception {
        User user = createUser("first", "last", Gender.M);
        String content = json(user);

        mockMvc.perform(
                put("/v1/user")
                        .header(UserController.HEADER_USER_ID, UUID.randomUUID())
                        .accept(JSON_MEDIA_TYPE)
                        .content(content)
                        .contentType(JSON_MEDIA_TYPE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", isA(Number.class)))
                .andExpect(jsonPath("$.firstName", is(user.getFirstName())))
                .andExpect(jsonPath("$.lastName", is(user.getLastName())))
                .andExpect(jsonPath("$.dateOfBirth", isA(Number.class)))
                .andExpect(jsonPath("$.dateOfBirth", is(user.getDateOfBirth().getTime())))
        ;

    }

    @Test
    public void requestBodyValidationInvalidJsonValue() throws Exception {
        testUser.setGender(Gender.M);
        String content = json(testUser);
        //payload with invalid gender
        content = content.replaceFirst("(\"gender\":\")(M)(\")", "$1Q$3");

        mockMvc.perform(
                put("/v1/user")
                        .header(UserController.HEADER_USER_ID, UUID.randomUUID())
                        .accept(JSON_MEDIA_TYPE)
                        .content(content)
                        .contentType(JSON_MEDIA_TYPE))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(JSON_MEDIA_TYPE))
                .andExpect(jsonPath("$.message", containsString("Cannot deserialize value of type `com.aelkz.springboot.skeleton.model.User$Gender`")))
        ;
    }

    @Test
    public void requestBodyValidationInvalidJson() throws Exception {
        String content = json("not valid json");
        mockMvc.perform(
                put("/v1/user")
                        .header(UserController.HEADER_USER_ID, UUID.randomUUID())
                        .accept(JSON_MEDIA_TYPE)
                        .content(content)
                        .contentType(JSON_MEDIA_TYPE))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(JSON_MEDIA_TYPE))
                .andExpect(jsonPath("$.message", containsString("Cannot construct instance of `com.aelkz.springboot.skeleton.model.User`")))
        ;
    }

    @Test
    public void handleHttpRequestMethodNotSupportedException() throws Exception {
        String content = json(testUser);

        mockMvc.perform(
                delete("/v1/user") //not supported method
                        .header(UserController.HEADER_USER_ID, UUID.randomUUID())
                        .accept(JSON_MEDIA_TYPE)
                        .content(content)
                        .contentType(JSON_MEDIA_TYPE))
                .andDo(print())
                .andExpect(status().isMethodNotAllowed())
                .andExpect(content().string(""))
        ;
    }

    private User createUser(String first, String last, Gender gender) {
        User user = new User(first, last);

        user.setCpf("11111111111");
        user.setGender(gender);
        user.setHandle("@"+first);

        user.setDateOfBirth(new Date(timestamp));
        return user;
    }

}
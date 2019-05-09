package com.aelkz.springboot.skeleton;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.aelkz.springboot.skeleton.model.User;
import com.aelkz.springboot.skeleton.service.UserService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.NestedServletException;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@Transactional
public class UserControllerMockedTest extends BaseControllerTest {

    @MockBean
    private UserService userService;

    private User testUser;

    @Before
    public void setup() throws Exception {
        super.setup();

        testUser = new User(1L, "Raphael", "@aelkz", "11111111111");

        when(userService.findOne(1L)).thenReturn(testUser);
    }

    @Test
    public void getUserById() throws Exception {
        mockMvc.perform(get("/v1/user/{id}", 1))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(JSON_MEDIA_TYPE))
                .andExpect(jsonPath("$.id", is(testUser.getId().intValue())))
                .andExpect(jsonPath("$.firstName", is(testUser.getFirstName())))
                .andExpect(jsonPath("$.lastName", is(testUser.getLastName())))
        ;
    }

    @Test(expected = NestedServletException.class)
    public void handleGenericException() throws Exception {
        when(userService.findOne(1L)).thenThrow(new RuntimeException("Failed to get user by id"));

        mockMvc.perform(get("/v1/user/{id}", 1))
                .andDo(print())
                .andExpect(status().is5xxServerError())
                .andExpect(content().string(""))
        ;
    }
}
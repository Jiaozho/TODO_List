package com.iftech.todo.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TodoControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    JdbcTemplate jdbcTemplate;

    /**
     * 测试前清理数据，保证用例之间相互独立。
     */
    @BeforeEach
    void clean() throws Exception {
        jdbcTemplate.update("DELETE FROM todo_item");
    }

    /**
     * 覆盖核心功能链路：新增 -> 列表 -> 切换完成态 -> 删除 -> 列表为空。
     */
    @Test
    void create_list_toggle_delete() throws Exception {
        mockMvc.perform(get("/api/todos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        String body1 = objectMapper.writeValueAsString(new Object() {
            public final String title = "t1";
            public final String description = "d1";
            public final String category = "学习";
            public final int priority = 3;
            public final String dueDate = "2026-01-02";
        });

        String created1 = mockMvc.perform(post("/api/todos").contentType(MediaType.APPLICATION_JSON).content(body1))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isString())
                .andExpect(jsonPath("$.completed").value(false))
                .andExpect(jsonPath("$.category").value("学习"))
                .andExpect(jsonPath("$.priority").value(3))
                .andExpect(jsonPath("$.dueDate").value("2026-01-02"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String id1 = objectMapper.readTree(created1).get("id").asText();

        String body2 = objectMapper.writeValueAsString(new Object() {
            public final String title = "t2";
            public final String category = "学习";
            public final int priority = 1;
            public final String dueDate = "2026-01-01";
        });

        String created2 = mockMvc.perform(post("/api/todos").contentType(MediaType.APPLICATION_JSON).content(body2))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.priority").value(1))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String id2 = objectMapper.readTree(created2).get("id").asText();

        mockMvc.perform(get("/api/todos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        mockMvc.perform(get("/api/todos/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0]").value("学习"));

        mockMvc.perform(get("/api/todos").queryParam("category", "学习"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        mockMvc.perform(get("/api/todos").queryParam("category", "工作"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        mockMvc.perform(get("/api/todos").queryParam("category", "学习").queryParam("sort", "priority"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(id1))
                .andExpect(jsonPath("$[1].id").value(id2));

        mockMvc.perform(get("/api/todos").queryParam("category", "学习").queryParam("sort", "dueDate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(id2))
                .andExpect(jsonPath("$[1].id").value(id1));

        mockMvc.perform(patch("/api/todos/" + id1 + "/toggle"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completed").value(true));

        mockMvc.perform(delete("/api/todos/" + id1))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/todos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(delete("/api/todos/" + id2))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/todos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}

package com.sjdscxz.taskmgr;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;MODE=PostgreSQL")
class TaskManagerApplicationTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    @Test
    void listEmptyInitially() throws Exception {
        mvc.perform(get("/api/tasks")).andExpect(status().isOk()).andExpect(jsonPath("$").isArray());
    }

    @Test
    void createAndList() throws Exception {
        var req = new TaskController.TaskRequest("Write report", "Q2 results", "TODO");
        mvc.perform(post("/api/tasks").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Write report"));
    }

    @Test
    void moveChangesStatus() throws Exception {
        var req = new TaskController.TaskRequest("Code review", null, "TODO");
        var res = mvc.perform(post("/api/tasks").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(req)))
                .andExpect(status().isCreated()).andReturn();
        var task = mapper.readTree(res.getResponse().getContentAsString());
        long id = task.get("id").asLong();

        var moveReq = new TaskController.MoveRequest("DOING", 0);
        mvc.perform(post("/api/tasks/" + id + "/move")
                        .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(moveReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DOING"));
    }

    @Test
    void deleteRemovesTask() throws Exception {
        var req = new TaskController.TaskRequest("Throwaway", null, null);
        var res = mvc.perform(post("/api/tasks").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(req)))
                .andExpect(status().isCreated()).andReturn();
        long id = mapper.readTree(res.getResponse().getContentAsString()).get("id").asLong();

        mvc.perform(delete("/api/tasks/" + id)).andExpect(status().isNoContent());
        mvc.perform(delete("/api/tasks/" + id)).andExpect(status().isNotFound());
    }

    @Test
    void blankTitleRejected() throws Exception {
        var req = new TaskController.TaskRequest("", null, null);
        mvc.perform(post("/api/tasks").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }
}

package com.sjdscxz.taskmgr;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.net.URI;
import java.time.Instant;
import java.util.List;

@SpringBootApplication
public class TaskManagerApplication {
    public static void main(String[] args) { SpringApplication.run(TaskManagerApplication.class, args); }
}

@Configuration
class WebConfig implements WebMvcConfigurer {
    @Override public void addCorsMappings(CorsRegistry r) {
        r.addMapping("/api/**").allowedOrigins("http://localhost:5173")
                .allowedMethods("GET","POST","PUT","DELETE");
    }
}

@Entity @Table(name = "tasks")
class Task {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long id;
    @NotBlank @Column(nullable = false, length = 200) String title;
    @Column(length = 2000) String description;
    @Column(nullable = false, length = 20) String status = "TODO";        // TODO | DOING | DONE
    @Column(nullable = false) int orderIndex = 0;
    @Column(nullable = false, updatable = false) Instant createdAt = Instant.now();
    @Column(nullable = false) Instant updatedAt = Instant.now();

    protected Task() {}
    Task(String title, String description) { this.title = title; this.description = description; }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getStatus() { return status; }
    public int getOrderIndex() { return orderIndex; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setTitle(String t) { this.title = t; touch(); }
    public void setDescription(String d) { this.description = d; touch(); }
    public void setStatus(String s) { this.status = s; touch(); }
    public void setOrderIndex(int o) { this.orderIndex = o; touch(); }
    private void touch() { this.updatedAt = Instant.now(); }
}

interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findAllByOrderByOrderIndexAsc();
}

@RestController @RequestMapping("/api/tasks")
class TaskController {
    private final TaskRepository repo;
    TaskController(TaskRepository repo) { this.repo = repo; }

    record TaskRequest(@NotBlank String title, String description, String status) {}
    record MoveRequest(String status, int orderIndex) {}

    @GetMapping
    public List<Task> list() { return repo.findAllByOrderByOrderIndexAsc(); }

    @PostMapping
    public ResponseEntity<Task> create(@Valid @RequestBody TaskRequest req) {
        Task t = new Task(req.title(), req.description());
        if (req.status() != null) t.setStatus(req.status());
        t.setOrderIndex((int) (repo.count()));
        Task saved = repo.save(t);
        return ResponseEntity.created(URI.create("/api/tasks/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Task> update(@PathVariable Long id, @Valid @RequestBody TaskRequest req) {
        return repo.findById(id).map(t -> {
            t.setTitle(req.title());
            t.setDescription(req.description());
            if (req.status() != null) t.setStatus(req.status());
            return ResponseEntity.ok(repo.save(t));
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/move")
    public ResponseEntity<Task> move(@PathVariable Long id, @RequestBody MoveRequest req) {
        return repo.findById(id).map(t -> {
            if (req.status() != null) t.setStatus(req.status());
            t.setOrderIndex(req.orderIndex());
            return ResponseEntity.ok(repo.save(t));
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

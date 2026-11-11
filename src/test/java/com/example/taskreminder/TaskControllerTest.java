package com.example.taskreminder;

import com.example.taskreminder.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.datasource.url=jdbc:h2:mem:webtest",
        "reminder.open-browser=false"
})
class TaskControllerTest {

    @Autowired TestRestTemplate http;
    @Autowired TaskRepository repository;

    @BeforeEach
    void clean() { repository.deleteAll(); }

    /** Regression: the first add (no session cookie yet) used to end in a 500 error page. */
    @Test
    void addingTaskOnFirstVisitRedirectsToWorkingPage() {
        var form = new LinkedMultiValueMap<String, String>();
        form.add("title", "Learn ");
        form.add("reminderTime", "17:30");
        form.add("repeatRule", "DAILY");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // The client follows the redirect back to "/", like a browser does
        ResponseEntity<String> page = http.postForEntity("/tasks", new HttpEntity<>(form, headers), String.class);
        assertThat(page.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(page.getBody()).contains("Learn").contains("Add a task");
        assertThat(repository.count()).isEqualTo(1);
    }

    @Test
    void homePageRenders() {
        ResponseEntity<String> page = http.getForEntity("/", String.class);
        assertThat(page.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(page.getBody()).contains("Add a task");
    }
}

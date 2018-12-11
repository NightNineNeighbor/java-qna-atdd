package codesquad.web;

import codesquad.domain.Answer;
import codesquad.domain.Question;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import support.test.AcceptanceTest;

public class ApiQnaAcceptanceTest extends AcceptanceTest {
    private static final Logger log = LoggerFactory.getLogger(ApiQnaAcceptanceTest.class);

    @Test
    public void create() throws Exception {
        String title = "ttt";
        String contents = "ccc";
        ResponseEntity<Void> response = basicAuthTemplate()
                .postForEntity("/api/questions/", new Question(title, contents), Void.class);
        softly.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String location = response.getHeaders().getLocation().getPath();

        Question dbQuestion = template().getForObject(location, Question.class);
        softly.assertThat(dbQuestion.getTitle()).isEqualTo(title);
        softly.assertThat(dbQuestion.getContents()).isEqualTo(contents);
        softly.assertThat(dbQuestion.getWriter()).isEqualTo(defaultUser());
    }

    @Test
    public void create_로그인_안함() throws Exception {
        ResponseEntity<Void> response = template()
                .postForEntity("/api/questions/", new Question("ttt", "ccc"), Void.class);

        softly.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void show() throws Exception {
        Question dbQuestion = template().getForObject("/api/questions/1", Question.class);
        softly.assertThat(dbQuestion).isEqualTo(defaultQuestion());
    }

    @Test
    public void update() {
        Question newQuestion = new Question("new title", "new contents");
        ResponseEntity<Void> response = basicAuthTemplate().
                postForEntity("/api/questions", newQuestion, Void.class);
        String location = response.getHeaders().getLocation().getPath();
        softly.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        Question updateQuestion = new Question("update title", "update contents");
        ResponseEntity<Question> responseEntity = basicAuthTemplate().
                exchange(location, HttpMethod.PUT, createHttpEntity(updateQuestion), Question.class);

        softly.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        softly.assertThat(updateQuestion.equalsTitleAndContents(responseEntity.getBody())).isTrue();
    }

    @Test
    public void createAnswer() {
        String newContents = "new Contents";
        ResponseEntity<Void> response = basicAuthTemplate().postForEntity("/api/questions/1/answers", newContents, Void.class);
        String location = response.getHeaders().getLocation().getPath();
        softly.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        Answer savedAnswer = template().getForObject(location, Answer.class);
        softly.assertThat(savedAnswer.getContents()).isEqualTo(newContents);
    }

    @Test
    public void updateAnswer(){
        ResponseEntity<Void> response =
                basicAuthTemplate().postForEntity("/api/questions/1/answers", "new contents", Void.class);
        String location = response.getHeaders().getLocation().getPath();
        softly.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        String updateContents = "update contents";
        ResponseEntity<Answer> responseEntity = basicAuthTemplate()
                .exchange(location, HttpMethod.PUT, createHttpEntity(updateContents), Answer.class);

        softly.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        softly.assertThat(responseEntity.getBody().getContents()).isEqualTo(updateContents);
    }

    private HttpEntity createHttpEntity(Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity(body, headers);
    }
}

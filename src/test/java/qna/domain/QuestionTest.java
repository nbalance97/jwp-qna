package qna.domain;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import javax.persistence.EntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class QuestionTest {
    public static final Question Q1 = new Question("title1", "contents1").writeBy(UserTest.JAVAJIGI);
    public static final Question Q2 = new Question("title2", "contents2").writeBy(UserTest.SANJIGI);

    @Autowired
    QuestionRepository questionRepository;

    @Autowired
    AnswerRepository answerRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    EntityManager em;

    @Test
    public void 질문과_답변_저장() {
        User savedUser = userRepository.save(UserTest.JAVAJIGI);
        Question savedQuestion = questionRepository.save(Q1);
        Answer answer = new Answer(savedUser, savedQuestion, "안녕하세요~");
        Answer save = answerRepository.save(answer);

        assertThat(save.getId()).isNotNull();
    }

    @Test
    public void 질문_답변_저장테스트2() {
        // 연관관계 설정을 꼼꼼히 해주지 않는다면 에러가 발생한다.
        // object references an unsaved transient instance : 영속 상태가 아닌 엔티티를 참조하려고 할 때 발생

        // 모두 영속 상태로 들어간다. save 메서드 호출로 인해 영속성 컨텍스트에 들어간다.
        User savedUser = userRepository.save(UserTest.JAVAJIGI);
        Question savedQuestion = questionRepository.save(Q1.writeBy(savedUser));
        Answer savedAnswer = answerRepository.save(
                new Answer(savedUser, savedQuestion, "안녕하세요~")
        );
        System.out.println(savedAnswer.getId() + "-" + savedQuestion.getId() + "-" + savedUser.getId());

        // 연관관계를 수동으로 설정해 준다.
        savedQuestion.addAnswer(savedAnswer);
        savedQuestion.writeBy(savedUser);
        savedUser.addQuestions(savedQuestion);
        savedUser.addAnswer(savedAnswer);

        // flush 해주는 시점에 데이터베이스에 저장된다.
        // 데이터베이스엔 어떤 순서로 저장이 될까? savedanswer 테이블은 user, question 외래키를 가지고 있다.
        // 따라서 user, question이 먼저 데이터베이스에 저장되어야 에러가 없을텐데.. 이건 어떻게 조정해줄 수 있을까?
        // cascade를 설정해 주면 된다.
        em.flush();
        em.clear();

        assertThat(savedAnswer.getId()).isNotNull();
    }

    @Test
    void 테스트를_위한_테스트() {
        User user = userRepository.save(new User("kim", "kim", "kim", "kim@kim.kim"));
        Question question = questionRepository.save(new Question("hello", "good"));
        question.linkUser(user);

        Answer answer = answerRepository.save(new Answer(user, question,"ㄹㅇㅋㅋ 꿀잼~"));
        question.addAnswer(answer);

        em.flush();
        em.clear();

        question = questionRepository.findById(question.getId()).get();
        System.out.println(question.getAnswers());
    }


}

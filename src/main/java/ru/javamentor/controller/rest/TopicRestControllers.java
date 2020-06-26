package ru.javamentor.controller.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.javamentor.model.Topic;
import ru.javamentor.model.User;
import ru.javamentor.service.TopicService;
import ru.javamentor.service.UserService;
import ru.javamentor.util.buffer.LikeBuffer;

import javax.servlet.http.HttpSession;
import java.security.Principal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Rest контроллер для топиков
 *
 * @version 1.0
 * @autor Java Mentor
 */
@RestController
@RequestMapping("/api")
public class TopicRestControllers {

    private final TopicService topicService;
    private final UserService userService;

    @Autowired
    public TopicRestControllers(TopicService topicService, UserService userService) {
        this.topicService = topicService;
        this.userService = userService;
    }

    /**
     * метод получения всех отмодерированных топиков
     *
     * @return ResponseEntity, который содержит List топиков
     */
    @GetMapping("/free-user/moderatedTopicsList")
    public ResponseEntity<List<Topic>> getTotalTopics() {
        return new ResponseEntity<>(topicService.getModeratedTopics(), HttpStatus.OK);
    }

    /**
     * метод получения всех неотмодерированных топиков
     *
     * @return ResponseEntity, который содержит List топиков
     */
    @GetMapping("/admin/notModeratedTopics")
    public ResponseEntity<List<Topic>> getNotModeratedTopics() {
        return new ResponseEntity<>(topicService.getNotModeratedTopics(), HttpStatus.OK);
    }

    /**
     * метод для получения одной страницы неотмодерированных топиков
     *
     * @return ResponseEntity, который содержит List неотмодерированных топиков
     */
    //TODO пока жестко задаю количество записей на странице
    @GetMapping("/admin/notModeratedTopicsPage/{page}")
    public ResponseEntity<List<Topic>> getNotModeratedTopicsPage(@PathVariable(value = "page") Integer page) {
        return new ResponseEntity<>(topicService.getNotModeratedTopicsPage(page, 5), HttpStatus.OK);
    }

    /**
     * метод для подсчета неотмодерированных топиков
     *
     * @return ResponseEntity, который содержит количество неотмодерированных топиков и статус ОК
     */
    @GetMapping("/admin/notModeratedTopicsCount")
    public ResponseEntity<Long> getNotModeratedTopicsCount() {
        return new ResponseEntity<>(topicService.getNotModeratedTopicsCount(), HttpStatus.OK);
    }

    /**
     * метод для получения топиков конкретного пользователя
     *
     * @param user - объект авторизованого пользователя
     * @return ResponseEntity, который содержит List топиков этого юзера
     */
    @GetMapping("/user/MyTopics")
    public ResponseEntity<List<Topic>> getAllTopicsOfAuthenticatedUser(@AuthenticationPrincipal User user) {
        return new ResponseEntity<>(topicService.getAllTopicsByUserId(user.getId()), HttpStatus.OK);
    }

    /**
     * метод для получения юзеров связанного с данным топиком
     *
     * @param topicId - уникальный id топика
     * @return ResponseEntity, который содержит List пользователей
     */
    @GetMapping("/user/allUsersByTopicId/{id}")
    public ResponseEntity<List<User>> getAllUsersByTopicId(@PathVariable(value = "id") Long topicId) {
        return new ResponseEntity<>(topicService.getAllUsersByTopicId(topicId), HttpStatus.OK);
    }

    /**
     * метод модерации топиков
     *
     * @param id - уникальный id топика
     * @return ResponseEntity, который содержит статус Ok
     */
    @PostMapping("/admin/topic/moderate/{id}")
    public ResponseEntity<Topic> isModerate(@PathVariable Long id) {
        Topic topic = topicService.getTopicById(id);
        topic.setModerate(true);
        topicService.updateTopic(topic);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * метод для получения топика по  id
     *
     * @param id - уникальный id топика
     * @return ResponseEntity, который содержит топик и статус ответа ОК
     */
    @GetMapping("/user/topic/{id}")
    public ResponseEntity<Topic> getTopicById(@PathVariable Long id) {
        return new ResponseEntity<>(topicService.getTopicById(id), HttpStatus.OK);
    }

    /**
     * метод для добавления топика
     *
     * @param topicData - содержимое топика
     * @param principal - хранит инфо об авторизованном пользователе
     * @return ResponseEntity, который содержит добавленный топик и статус ОК либо BAD REQUEST в случае если топик пуст
     */
    @PostMapping("/user/topic/add")
    public ResponseEntity<Topic> addTopic(@RequestBody Topic topicData, Principal principal) {
        Set<User> users = new HashSet<>();
        users.add(userService.getUserByUsername(principal.getName()));
        Topic topic = topicService.addTopic(topicData.getTitle(), topicData.getContent(), users);
        if (topic != null) {
            return new ResponseEntity<>(topic, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * метод для обновления топика
     *
     * @param topic - обновленный топик
     * @return ResponseEntity, который содержит добавленный топик и статус ОК либо BAD REQUEST в случае неудачи
     */
    @PostMapping("/user/topic/update")
    public ResponseEntity<String> updateTopic(@RequestBody Topic topic) {
        if (topicService.updateTopic(topic)) {
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>("You can't update the topic because it doesn't belong to you.", HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * метод для уадления топика
     *
     * @param id - id топика который необходимо удалить
     * @return ResponseEntity, который содержит добавленный топик и статус ОК либо BAD REQUEST в случае неудачи
     */
    @DeleteMapping("/user/topic/delete/{id}")
    public ResponseEntity<String> deleteTopic(@PathVariable Long id) {
        if (topicService.removeTopicById(id)) {
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>("You can't delete the topic because it doesn't belong to you.", HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Поиск топиков по значению связанного с ними хэштега.
     *
     * @param tag  - строковое представление хэштега
     * @param user - данные пользователя, отправившего запрос
     * @return список топиков
     */
    @GetMapping("/free-user/get-topics-of-user-by-hashtag/{tag}")
    public ResponseEntity<List<Topic>> getAllTopicsOfUserByHashtag(@PathVariable String tag, @AuthenticationPrincipal User user) {
        tag = "#" + tag;
        List<Topic> topics = topicService.getAllTopicsOfUserByHashtag(user.getId(), tag);
        return new ResponseEntity<>(topics, HttpStatus.OK);
    }

    /**
     * Поиск топиков по значению связанного с ними хэштега.
     *
     * @param tag - строковое представление хэштега
     * @return список топиков
     */
    @GetMapping("/free-user/get-all-topics-by-hashtag/{tag}")
    public ResponseEntity<List<Topic>> getAllTopicsByHashtag(@PathVariable String tag) {
        tag = "#" + tag;
        List<Topic> topics = topicService.getAllTopicsByHashtag(tag);
        return new ResponseEntity<>(topics, HttpStatus.OK);
    }

    /**
     * метод для уадления топика
     *
     * @param - id топика который необходимо удалить
     * @return ResponseEntity со статусом ОК если удаление прошло успешно , иначе BAD REQUEST
     */
    @DeleteMapping("/admin/topic/delete/{id}")
    public ResponseEntity<String> deleteTopicByAdmin(@PathVariable Long id) {
        if (topicService.removeTopicById(id)) {
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Cannot delete this topic, try again", HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * метод для получения неотмодерированного топика по id админом
     *
     * @param - id топика который необходимо получитьв ответе
     * @return ResponseEntity с необходимым топиком и ОК статус
     */
    @GetMapping("/admin/topic/{id}")
    public ResponseEntity<Topic> getNomoderatedTopicById(@PathVariable Long id) {
        return new ResponseEntity<>(topicService.getTopicById(id), HttpStatus.OK);
    }

    /**
     * метод для лайка топика
     *
     * @param topicId - id  топика который нужно лайкнуть
     * @param session - текущая сессия клиента
     * @return Увеличенное количество топиков либо ответ что лайк с текущей сессии запрещен
     */
    @GetMapping("/admin/topic/addLike/{topicId}")
    public ResponseEntity<Integer> increaseLikeOfTopic(@PathVariable Long topicId, HttpSession session) {
        if (LikeBuffer.getInstance().canLikeInThisSession(session.getId(), topicId)) {
            LikeBuffer.getInstance().addLikeToCurrentSession(session.getId(), topicId);
            return new ResponseEntity<>(topicService.increaseTopicLikes(topicId), HttpStatus.OK);
        } else return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }
}

package org.example.expert.domain.todo.service;

import org.example.expert.client.WeatherClient;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSaveResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class TodoServiceTest {
    @Mock
    private TodoRepository todoRepository;
    @Mock
    private WeatherClient weatherClient;
    @InjectMocks
    private TodoService todoService;

    @Nested
    class GetTodoTest {
        @Test
        public void 일정_조회_정상동작() {
            // given
            long todoId = 1L;
            long userId = 2L;
            User user = new User("email", "pwd", UserRole.USER);
            ReflectionTestUtils.setField(user, "id", userId);
            Todo todo = new Todo("title", "contents", "weather", user);
            ReflectionTestUtils.setField(todo,"id",todoId);

            given(todoRepository.findByIdWithUser(anyLong())).willReturn(Optional.of(todo));

            // when
            TodoResponse todoResponse = todoService.getTodo(todoId);

            //then
            assertNotNull(todoResponse);
            assertEquals(1, todoResponse.getId());
        }

        @Test
        public void 일정_조회_entity_없음() {
            // given
            long todoId = 1L;
            long userId = 2L;
            User user = new User("email", "pwd", UserRole.USER);
            ReflectionTestUtils.setField(user, "id", userId);
            Todo todo = new Todo("title", "contents", "weather", user);
            ReflectionTestUtils.setField(todo,"id",todoId);

            given(todoRepository.findByIdWithUser(anyLong())).willReturn(Optional.empty());

            // when
            InvalidRequestException exception = assertThrows(InvalidRequestException.class, ()-> todoService.getTodo(todoId));

            //then
            assertEquals("Todo not found", exception.getMessage());
        }
    }

    @Test
    void 일정을_정상적으로_등록한다() {
        // given

        String weather = "Sunny";
        given(weatherClient.getTodayWeather()).willReturn(weather);

        AuthUser authUser = new AuthUser(1L, "email", UserRole.USER);
        User user = User.fromAuthUser(authUser);
        TodoSaveRequest todoSaveRequest = new TodoSaveRequest("title", "Contents");
        Todo savedTodo = new Todo(
                todoSaveRequest.getTitle(),
                todoSaveRequest.getContents(),
                weather,
                user
        );
        long savedTodoId = 1L;
        ReflectionTestUtils.setField(savedTodo,"id", savedTodoId);
        given(todoRepository.save(any())).willReturn(savedTodo);
        // when

        TodoSaveResponse response = todoService.saveTodo(authUser, todoSaveRequest);

        //then

        assertNotNull(response);
        assertEquals(savedTodo.getId(), response.getId());
    }
}

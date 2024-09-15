package org.example.expert.domain.manager.service;

import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.manager.entity.Manager;
import org.example.expert.domain.manager.repository.ManagerRepository;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class ManagerServiceDeleteTest {

    @Mock
    private ManagerRepository managerRepository;
    @Mock
    private TodoRepository todoRepository;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private ManagerService managerService;

    @Test
    void 매니저_삭제_성공() {
        // given
        AuthUser authUser = new AuthUser(1L, "test@example.com", UserRole.USER);
        User user = User.fromAuthUser(authUser);
        Todo todo = new Todo("Title", "Contents", "Sunny", user);
        Manager manager = new Manager(todo.getUser(), todo);

        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
        given(todoRepository.findById(anyLong())).willReturn(Optional.of(todo));
        given(managerRepository.findById(anyLong())).willReturn(Optional.of(manager));

        //when & then
        assertDoesNotThrow(() -> managerService.deleteManager(authUser.getId(), 1L, 1L));
        verify(managerRepository, times(1)).delete(manager);
    }

    @Test
    void 매니저_삭제_중_일정을_찾지_못해_에러가_발생() {
        // given
        AuthUser authUser = new AuthUser(1L, "test@example.com", UserRole.USER);
        User user = User.fromAuthUser(authUser);

        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
        given(todoRepository.findById(anyLong())).willReturn(Optional.empty());

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
                managerService.deleteManager(authUser.getId(), 1L, 1L)
        );

        // then
        assertEquals("Todo not found", exception.getMessage());
    }

    @Test
    void 매니저_삭제_중_유저를_찾지_못해_에러_발생() {
        // given
        AuthUser authUser = new AuthUser(1L, "test@example.com", UserRole.USER);
        AuthUser authUser2 = new AuthUser(2L, "test1@example.com", UserRole.USER);
        User user = User.fromAuthUser(authUser2);
        Todo todo = new Todo("Title", "Contents", "Sunny", user);

        given(userRepository.findById(anyLong())).willReturn(Optional.of(User.fromAuthUser(authUser)));
        given(todoRepository.findById(anyLong())).willReturn(Optional.of(todo));

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
                managerService.deleteManager(authUser.getId(), 1L, 1L)
        );

        // then
        assertEquals("해당 일정을 만든 유저가 유효하지 않습니다.", exception.getMessage());
    }

    @Test
    void 매니저_삭제_중_매니저를_찾지_못해_에러_발생() {
        // given
        AuthUser authUser = new AuthUser(1L, "test@example.com", UserRole.USER);
        User user = User.fromAuthUser(authUser);
        Todo todo = new Todo("Title", "Contents", "Sunny", user);

        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
        given(todoRepository.findById(anyLong())).willReturn(Optional.of(todo));
        given(managerRepository.findById(anyLong())).willReturn(Optional.empty());

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
                managerService.deleteManager(authUser.getId(), 1L, 1L)
        );

        //then
        assertEquals("Manager not found", exception.getMessage());
    }

    @Test
    void 매니저_삭제_중_매니저와_일정의_매니저가_불일치() {
        // given
        AuthUser authUser = new AuthUser(1L, "test@example.com", UserRole.USER);
        User user = User.fromAuthUser(authUser);

        Todo todo = new Todo("Title", "Contents", "Sunny", user);
        ReflectionTestUtils.setField(todo, "id", 1L);

        Todo differentTodo = new Todo();
        ReflectionTestUtils.setField(differentTodo, "id", 2L);

        Manager manager = new Manager(user, differentTodo);
        ReflectionTestUtils.setField(manager, "id", 1L);

        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
        given(todoRepository.findById(anyLong())).willReturn(Optional.of(todo));
        given(managerRepository.findById(anyLong())).willReturn(Optional.of(manager));

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, ()->
                managerService.deleteManager(authUser.getId(), differentTodo.getId(), manager.getId()));
        // then
        assertEquals("해당 일정에 등록된 담당자가 아닙니다.", exception.getMessage());
    }

}

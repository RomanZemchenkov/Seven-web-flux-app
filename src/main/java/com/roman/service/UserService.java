package com.roman.service;

import com.roman.dao.entity.User;
import com.roman.dao.repository.SequenceGenerator;
import com.roman.dao.repository.UserMongoRepository;
import com.roman.service.dto.user.CreateUserDto;
import com.roman.service.dto.user.ShowUserDto;
import com.roman.service.dto.user.UpdateUserDto;
import com.roman.service.mapping.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import static com.roman.dao.entity.User.SEQUENCE_NAME;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMongoRepository userMongoRepository;
    private final UserMapper userMapper;
    private final SequenceGenerator sequenceGenerator;


    public Mono<ShowUserDto> create(Mono<CreateUserDto> dto) {
        return dto
                .map(userMapper::mapToUser)
                .flatMap(user -> sequenceGenerator.getSequenceNumber(SEQUENCE_NAME)
                        .map(id -> {
                            user.setId(id);
                            return user;
                        }))
                .flatMap(userMongoRepository::save)
                .map(userMapper::mapToShow);
    }


    public Flux<ShowUserDto> findAll() {
        Flux<User> allUsers = userMongoRepository.findAllUsers();

        return allUsers
                .publishOn(Schedulers.parallel())
                .map(userMapper::mapToShow);
    }

    public Mono<ShowUserDto> findById(String id) {
        return userMongoRepository.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Необработанная ошибка")))
                .map(userMapper::mapToShow);
    }


    public Mono<ShowUserDto> update(Mono<UpdateUserDto> dto) {
        return dto
                .flatMap(us -> userMongoRepository.findById(us.getId())
                        .switchIfEmpty(Mono.error(new RuntimeException("Пользователь не найден")))
                        .flatMap(mbu -> {
                            Query query = Query.query(Criteria.where("id").is(us.getId()));
                            Update update = new Update();
                            if (us.getUsername() != null) update.set("username", us.getUsername());
                            if (us.getEmail() != null) update.set("email", us.getEmail());

                            FindAndModifyOptions options = FindAndModifyOptions.options().returnNew(true);
                            return userMongoRepository.update(query, update, options);
                        })
                        .map(userMapper::mapToShow));
    }

    public Mono<Boolean> delete(String id) {
        return userMongoRepository.delete(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Необработанная ошибка")))
                .map(us -> Boolean.TRUE);
    }
}

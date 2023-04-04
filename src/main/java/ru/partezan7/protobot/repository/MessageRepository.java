package ru.partezan7.protobot.repository;


import org.springframework.data.repository.CrudRepository;
import ru.partezan7.protobot.entity.Message;

public interface MessageRepository extends CrudRepository<Message, Long> {
//    List<Message> findById(Integer id);
}

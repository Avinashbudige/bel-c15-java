package org.example.service;

import org.example.model.User;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final Map<Long, User> users = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public UserService() {
        // Add sample data
        create(new User(null, "John Doe", "john.doe@example.com", 25));
        create(new User(null, "Jane Smith", "jane.smith@example.com", 30));
        create(new User(null, "Bob Johnson", "bob.johnson@example.com", 35));
    }

    public List<User> getAll() {
        return new ArrayList<>(users.values());
    }

    public User getById(Long id) {
        return users.get(id);
    }

    public User create(User user) {
        if (user.getId() == null) {
            user.setId(idGenerator.getAndIncrement());
        }
        users.put(user.getId(), user);
        return user;
    }

    public User update(Long id, User user) {
        if (users.containsKey(id)) {
            user.setId(id);
            users.put(id, user);
            return user;
        }
        return null;
    }

    public boolean delete(Long id) {
        return users.remove(id) != null;
    }

    public List<User> searchByName(String name) {
        return users.values().stream()
                .filter(u -> u.getName().toLowerCase().contains(name.toLowerCase()))
                .collect(Collectors.toList());
    }
}
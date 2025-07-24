package com.pixelpals.backend;
import com.pixelpals.backend.enumeration.SkillLevel;
import com.pixelpals.backend.model.Game;
import com.pixelpals.backend.model.User;
import com.pixelpals.backend.service.UserService;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@EnableMongoRepositories(basePackages = "com.pixelpals.backend.repository")
@SpringBootApplication
public class

PixelpalsApplication {

	public static void main(String[] args) {
		SpringApplication.run(PixelpalsApplication.class, args);
	}

//	@Bean
//	public CommandLineRunner init(UserService userService) {
//		return args -> {
//			User user = new User();
//			user.setUsername("TestUser");
//			user.setEmail("test@pixelpals.com");
//			Game game = new Game();
//			Field nameField = Game.class.getDeclaredField("name");
//			nameField.setAccessible(true);
//			nameField.set(game, "Valorant");
//			Map<String, SkillLevel> skillMap = new HashMap<>();
//			skillMap.put(game.getName(), SkillLevel.INTERMEDIATE);
//			user.setSkillLevelMap(skillMap);
//			userService.createUser(user);
//		};
//	}

}


package com.pixelpals.backend;
import com.pixelpals.backend.model.Game;
import com.pixelpals.backend.repository.GameRepository;
import com.pixelpals.backend.repository.UserRepository;
import com.pixelpals.backend.model.User;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Configuration
public class DataInitializer {

    private final PasswordEncoder passwordEncoder;

    public DataInitializer(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    CommandLineRunner initGames(GameRepository gameRepository) {
        return args -> {
            if (gameRepository.count() == 0) {
                List<Game> games = List.of(
                        new Game(null, "League of Legends", "MOBA", "https://example.com/images/lol.png"),
                        new Game(null, "Valorant", "FPS", "https://example.com/images/valorant.png"),
                        new Game(null, "Minecraft", "Sandbox", "https://example.com/images/minecraft.png"),
                        new Game(null, "Fortnite", "Battle Royale", "https://example.com/images/fortnite.png"),
                        new Game(null, "Overwatch", "FPS", "https://example.com/images/overwatch.png"),
                        new Game(null, "Call of Duty", "FPS", "https://example.com/images/cod.png"),
                        new Game(null, "Among Us", "Party", "https://example.com/images/amongus.png"),
                        new Game(null, "Genshin Impact", "RPG", "https://example.com/images/genshin.png"),
                        new Game(null, "Dota 2", "MOBA", "https://example.com/images/dota2.png"),
                        new Game(null, "CS:GO", "FPS", "https://example.com/images/csgo.png"),
                        new Game(null, "World of Warcraft", "MMORPG", "https://example.com/images/wow.png"),
                        new Game(null, "The Witcher 3", "RPG", "https://example.com/images/witcher3.png"),
                        new Game(null, "Cyberpunk 2077", "RPG", "https://example.com/images/cyberpunk.png"),
                        new Game(null, "Hearthstone", "Card Game", "https://example.com/images/hearthstone.png"),
                        new Game(null, "Rocket League", "Sports", "https://example.com/images/rocketleague.png"),
                        new Game(null, "Apex Legends", "Battle Royale", "https://example.com/images/apex.png"),
                        new Game(null, "Minecraft Dungeons", "Dungeon Crawler", "https://example.com/images/minecraftdungeons.png"),
                        new Game(null, "Fall Guys", "Party", "https://example.com/images/fallguys.png"),
                        new Game(null, "Terraria", "Sandbox", "https://example.com/images/terraria.png"),
                        new Game(null, "Among Trees", "Survival", "https://example.com/images/amongtrees.png")
                );
                gameRepository.saveAll(games);
                System.out.println("Lista giochi popolata nel DB");
            }
        };
    }

    @Bean
    CommandLineRunner initAdmin(UserRepository userRepository) {
        return args -> {
            if (userRepository.findByUsername("admin").isEmpty()) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setEmail("admin@example.com");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRole("ROLE_ADMIN");
                userRepository.save(admin);
                System.out.println("Admin creato");
            }
        };
    }
}


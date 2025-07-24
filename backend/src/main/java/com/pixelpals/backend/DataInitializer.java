package com.pixelpals.backend;

import com.pixelpals.backend.model.Game;
import com.pixelpals.backend.model.Platform;
import com.pixelpals.backend.model.User;
import com.pixelpals.backend.repository.GameRepository;
import com.pixelpals.backend.repository.PlatformRepository;
import com.pixelpals.backend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;

@Configuration
public class DataInitializer {

    private final PasswordEncoder passwordEncoder;

    public DataInitializer(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public CommandLineRunner initData(
            UserRepository userRepository,
            GameRepository gameRepository,
            PlatformRepository platformRepository
    ) {
        return args -> {
            /*
            if (userRepository.findByUsername("admin").isEmpty()) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setEmail("admin@pixelpals.com");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRole("ADMIN");
                userRepository.save(admin);
            }

            if (userRepository.findByUsername("testuser").isEmpty()) {
                User testUser = new User();
                testUser.setUsername("testuser");
                testUser.setEmail("test@pixelpals.com");
                testUser.setPassword(passwordEncoder.encode("password123"));
                testUser.setRole("USER");
                userRepository.save(testUser);
            }
            */



            if (platformRepository.count() == 0) {
                List<Platform> platforms = Arrays.asList(
                    new Platform(null, "PC", "https://res.cloudinary.com/di5vaxt8r/image/upload/v1752589140/PC_nl6ner.avif"),
                    new Platform(null, "PlayStation", "https://res.cloudinary.com/di5vaxt8r/image/upload/v1752589140/PS5_tsyivw.webp"),
                    new Platform(null, "Xbox", "https://res.cloudinary.com/di5vaxt8r/image/upload/v1752589141/Xbox_one_logo.svg_s9pm2r.png"),
                    new Platform(null, "Nintendo Switch", "https://res.cloudinary.com/di5vaxt8r/image/upload/v1752589140/Nintendo_Switch_Logo.svg_ztrdy1.png"),
                    new Platform(null, "Mobile", "https://res.cloudinary.com/di5vaxt8r/image/upload/v1752589140/mobile_kygo5b.jpg")
                );
                platformRepository.saveAll(platforms);
                System.out.println("Piattaforme iniziali inserite.");
            }

/*

            if (gameRepository.count() == 0) {
                List<Game> games = Arrays.asList(
                        new Game(null, "League of Legends", "MOBA", "https://res.cloudinary.com/di5vaxt8r/image/upload/v1752588494/league-of-legends-2022-patch-schedule-all-lol-season-12-updates-changes_vyul83.jpg", false),
                        new Game(null, "Valorant", "FPS", "https://res.cloudinary.com/di5vaxt8r/image/upload/v1752588500/valorant-le-jeu-video-signe-riot-games-est-disponible_khbkuq.jpg", true),
                        new Game(null, "Hogwarts Legacy", "RPG", "https://res.cloudinary.com/di5vaxt8r/image/upload/v1752588496/Hogwarts_Legacy_zxrpol.jpg", false),
                        new Game(null, "Fortnite", "Battle Royale", "https://res.cloudinary.com/di5vaxt8r/image/upload/v1752588497/Fortnite_sa8z96.jpg", true),
                        new Game(null, "Overwatch 2", "FPS", "https://res.cloudinary.com/di5vaxt8r/image/upload/v1752588502/Overwatch2_a3ataj.png", false),
                        new Game(null, "Call of Duty: Warzone", "FPS", "https://res.cloudinary.com/di5vaxt8r/image/upload/v1752588491/COMD_Warzone_eh5own.jpg", true),
                        new Game(null, "Among Us", "Party", "https://res.cloudinary.com/di5vaxt8r/image/upload/v1752588488/Among_us_xvdn0n.jpg", false),
                        new Game(null, "Genshin Impact", "RPG", "https://res.cloudinary.com/di5vaxt8r/image/upload/v1752588496/genshin-impact_flhdje.jpg", true),
                        new Game(null, "Dota 2", "MOBA", "https://res.cloudinary.com/di5vaxt8r/image/upload/v1752588489/Dota_2_ccy5jt.jpg", false),
                        new Game(null, "CS2", "FPS", "https://res.cloudinary.com/di5vaxt8r/image/upload/v1752588497/CS2_gbnw78.jpg", true),
                        new Game(null, "World of Warcraft", "MMORPG", "https://res.cloudinary.com/di5vaxt8r/image/upload/v1752588499/World_of_WarCraft_dpwfbk.webp", false),
                        new Game(null, "The Witcher 3: Wild Hunt", "RPG", "https://res.cloudinary.com/di5vaxt8r/image/upload/v1752588501/The_Witcher_3_wb3g3q.jpg", true),
                        new Game(null, "Cyberpunk 2077", "RPG", "https://res.cloudinary.com/di5vaxt8r/image/upload/v1752588491/Cyberpunk2077_zcevmo.jpg", false),
                        new Game(null, "Apex Legends", "Battle Royale", "https://res.cloudinary.com/di5vaxt8r/image/upload/v1752588489/apex-legends_bttwqo.jpg", true),
                        new Game(null, "Minecraft", "Sandbox", "https://res.cloudinary.com/di5vaxt8r/image/upload/v1752588497/Minecraft_uxitsi.jpg", false),
                        new Game(null, "Grand Theft Auto V", "Action-Adventure", "https://res.cloudinary.com/di5vaxt8r/image/upload/v1752588492/gta_5_t2sruw.jpg", true),
                        new Game(null, "Red Dead Redemption 2", "Action-Adventure", "https://res.cloudinary.com/di5vaxt8r/image/upload/v1752588498/read_dead_redemption_2_olb15n.avif", false),
                        new Game(null, "Elden Ring", "Action RPG", "https://res.cloudinary.com/di5vaxt8r/image/upload/v1752588491/elden-ring_narrqq.jpg", true),
                        new Game(null, "Destiny 2", "FPS", "https://res.cloudinary.com/di5vaxt8r/image/upload/v1752588492/Destiny_2_jwjfr0.jpg", false),
                        new Game(null, "Rocket League", "Sports", "https://res.cloudinary.com/di5vaxt8r/image/upload/v1752588497/Rocket_league_syojbl.jpg", true),
                        new Game(null, "Stardew Valley", "Simulation", "https://res.cloudinary.com/di5vaxt8r/image/upload/v1752588502/stardew-valley_gry3ld.jpg", false),
                        new Game(null, "The Legend of Zelda: Breath of the Wild", "Action-Adventure", "https://res.cloudinary.com/di5vaxt8r/image/upload/v1752588499/The_Legend_of_Zelda_flf6nw.jpg", true)
                );
                gameRepository.saveAll(games);
                System.out.println("Giochi iniziali inseriti.");
            }*/
        };
    }
}
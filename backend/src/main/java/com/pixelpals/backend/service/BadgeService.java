package com.pixelpals.backend.service;
import com.pixelpals.backend.model.Badge;
import com.pixelpals.backend.model.User;
import com.pixelpals.backend.repository.BadgeRepository;
import com.pixelpals.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;
@Service
@RequiredArgsConstructor
public class BadgeService {
    private final BadgeRepository badgeRepository;
    private final UserRepository userRepository;
    private static final String FIRST_MATCH_BADGE_NAME = "Primo Match Completato";
    private static final String RELIABLE_PIXELPAL_BADGE_NAME = "PixelPal Affidabile";
    private static final String LEVEL_5_VETERAN_BADGE_NAME = "Veterano Livello 5";
    private static final String CASUAL_PLAYER_BADGE_NAME = "Giocatore Occasionale";
    private static final String FIELD_VETERAN_BADGE_NAME = "Veterano del Campo";
    private static final String PIXELPALS_LEGEND_BADGE_NAME = "Leggenda di PixelPals";
    @PostConstruct
    public void initBadges() {
        createBadgeIfNotExists(
                FIRST_MATCH_BADGE_NAME,
                "Completato il tuo primo match con un altro PixelPal!",
                "https://res.cloudinary.com/di5vaxt8r/image/upload/v1752760268/icons8-trophy-64_w5edvd.png"
        );
        createBadgeIfNotExists(
                RELIABLE_PIXELPAL_BADGE_NAME,
                "Mantieni un rating medio elevato e sii un PixelPal affidabile!",
                "https://res.cloudinary.com/di5vaxt8r/image/upload/v1752760267/icons8-trophy-50_2_gz8plu.png"
        );
        createBadgeIfNotExists(
                LEVEL_5_VETERAN_BADGE_NAME,
                "Hai raggiunto il livello 5! Continua così!",
                "https://res.cloudinary.com/di5vaxt8r/image/upload/v1752760267/icons8-trophy-64_1_ocz0xp.png"
        );
        // NUOVI BADGE
        createBadgeIfNotExists(
                CASUAL_PLAYER_BADGE_NAME,
                "Hai completato 5 partite! Ottimo inizio!",
                "https://res.cloudinary.com/di5vaxt8r/image/upload/v1752760267/icons8-trophy-50_jpdfsg.png"
        );
        createBadgeIfNotExists(
                FIELD_VETERAN_BADGE_NAME,
                "Hai completato 10 partite! Un vero veterano!",
                "https://res.cloudinary.com/di5vaxt8r/image/upload/v1752760267/icons8-trophy-50_1_ybyffq.png"
        );
        createBadgeIfNotExists(
                PIXELPALS_LEGEND_BADGE_NAME,
                "Hai completato 20 partite! Sei una leggenda!",
                "https://res.cloudinary.com/di5vaxt8r/image/upload/v1752760423/icons8-game-trophy-64_x4tjzu.png"
        );
    }
    private void createBadgeIfNotExists(String name, String description, String imageUrl) {
        Optional<Badge> existingBadge = badgeRepository.findByName(name);
        if (existingBadge.isEmpty()) {
            Badge newBadge = Badge.builder()
                    .name(name)
                    .description(description)
                    .imageUrl(imageUrl)
                    .build();
            badgeRepository.save(newBadge);
        }
    }
    public User checkAndAssignBadges(User user) {
        if (user.getMatchesPlayed() >= 1) {
            assignBadge(user, FIRST_MATCH_BADGE_NAME);
        }
        if (user.getNumberOfRatings() >= 5 && user.getRating() >= 4.5) {
            assignBadge(user, RELIABLE_PIXELPAL_BADGE_NAME);
        }
        if (user.getLevel() >= 5) {
            assignBadge(user, LEVEL_5_VETERAN_BADGE_NAME);
        }
        if (user.getMatchesPlayed() >= 5) {
            assignBadge(user, CASUAL_PLAYER_BADGE_NAME);
        }
        if (user.getMatchesPlayed() >= 10) {
            assignBadge(user, FIELD_VETERAN_BADGE_NAME);
        }
        if (user.getMatchesPlayed() >= 20) {
            assignBadge(user, PIXELPALS_LEGEND_BADGE_NAME);
        }
        return user;
    }
    private void assignBadge(User user, String badgeName) {
        Optional<Badge> badgeOptional = badgeRepository.findByName(badgeName);
        if (badgeOptional.isPresent()) {
            Badge badge = badgeOptional.get();
            boolean hasBadge = user.getBadges().stream()
                    .anyMatch(b -> b.getId().equals(badge.getId()));
            if (!hasBadge) {
                user.getBadges().add(badge);
                userRepository.save(user);
            }
        } else {
        }
    }
    public List<Badge> getAllBadges() {
        return badgeRepository.findAll();
    }
}

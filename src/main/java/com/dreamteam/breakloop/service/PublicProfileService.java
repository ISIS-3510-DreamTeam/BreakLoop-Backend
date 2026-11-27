package com.dreamteam.breakloop.service;

import com.dreamteam.breakloop.exception.ForbiddenException;
import com.dreamteam.breakloop.exception.NotFoundException;
import com.dreamteam.breakloop.model.FriendProgress;
import com.dreamteam.breakloop.model.PublicProfile;
import com.dreamteam.breakloop.model.UserAchievement;
import com.dreamteam.breakloop.model.UserProfile;
import com.dreamteam.breakloop.repository.FriendRepository;
import com.dreamteam.breakloop.repository.UserProfileRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PublicProfileService {

    private final UserProfileRepository profiles;
    private final FriendRepository friends;
    private final UserAchievementService achievements;

    public PublicProfileService(
            UserProfileRepository profiles, FriendRepository friends,
            UserAchievementService achievements) {
        this.profiles = profiles;
        this.friends = friends;
        this.achievements = achievements;
    }

    public PublicProfile getPublicProfile(String requesterUid, String uid) {
        UserProfile profile = requireVisible(requesterUid, uid);
        // Same source and defaults as the friends list
        FriendProgress progress = friends.findProgress(uid).join();

        return new PublicProfile(
                uid,
                profile.displayName() != null ? profile.displayName() : "",
                profile.photoUrl(),
                progress.streakOrDefault(),
                progress.petLevelOrDefault());
    }

    /** Ids of the unlocked achievements, from users/{uid}/achievements. */
    public List<String> getBadges(String requesterUid, String uid) {
        requireVisible(requesterUid, uid);

        return achievements.getUnlocked(uid).stream()
                .map(UserAchievement::getId)
                .toList();
    }

    /**
     * 404 if the user does not exist, 403 unless the requester is that user
     * or is in that user's friends.
     */
    private UserProfile requireVisible(String requesterUid, String uid) {
        UserProfile profile = profiles.find(uid)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (!uid.equals(requesterUid) && !friends.isFriend(uid, requesterUid)) {
            throw new ForbiddenException(
                    "Only the user or their friends can see this");
        }

        return profile;
    }
}

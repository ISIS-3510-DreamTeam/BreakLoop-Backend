package com.dreamteam.breakloop.service;

import com.dreamteam.breakloop.exception.ForbiddenException;
import com.dreamteam.breakloop.exception.NotFoundException;
import com.dreamteam.breakloop.model.FriendProgress;
import com.dreamteam.breakloop.model.PublicProfile;
import com.dreamteam.breakloop.model.UserProfile;
import com.dreamteam.breakloop.repository.FriendRepository;
import com.dreamteam.breakloop.repository.UserProfileRepository;
import org.springframework.stereotype.Service;

@Service
public class PublicProfileService {

    private final UserProfileRepository profiles;
    private final FriendRepository friends;

    public PublicProfileService(
            UserProfileRepository profiles, FriendRepository friends) {
        this.profiles = profiles;
        this.friends = friends;
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

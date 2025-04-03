package com.finance.user.service.profile;

import com.finance.user.model.ProfileModel;
import com.finance.user.repository.ProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProfileServiceImplementation implements ProfileService{

    @Autowired
    private ProfileRepository profileRepository;

    @Override
    public ProfileModel save(int userId, ProfileModel profile) {

        ProfileModel fetchProfile = profileRepository.findByUserId(userId);
        fetchProfile.setName(profile.getName());
        fetchProfile.setEmail(profile.getEmail());
        fetchProfile.setPhone(profile.getPhone());
        fetchProfile.setAddress(profile.getAddress());
        fetchProfile.setIncomeRange(profile.getIncomeRange());
        fetchProfile.setProfileImage(profile.getProfileImage());

        return profile;
    }

    @Override
    public ProfileModel findByUserId(int userId) {
        return profileRepository.findByUserId(userId);
    }

    @Override
    public String getNameByUserId(int userId) {
        return profileRepository.getNameFromUserId(userId);
    }
}

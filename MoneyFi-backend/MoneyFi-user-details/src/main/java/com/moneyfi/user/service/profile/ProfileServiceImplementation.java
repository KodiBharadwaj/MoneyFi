package com.moneyfi.user.service.profile;

import com.moneyfi.user.model.ProfileModel;
import com.moneyfi.user.repository.ProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProfileServiceImplementation implements ProfileService{

    private final ProfileRepository profileRepository;

    public ProfileServiceImplementation(ProfileRepository profileRepository){
        this.profileRepository = profileRepository;
    }

    @Override
    public ProfileModel save(int userId, ProfileModel profile) {

        ProfileModel fetchProfile = profileRepository.findByUserId(userId);
        fetchProfile.setName(profile.getName());
        fetchProfile.setEmail(profile.getEmail());
        fetchProfile.setPhone(profile.getPhone());
        fetchProfile.setGender(profile.getGender());
        fetchProfile.setDateOfBirth(profile.getDateOfBirth());
        fetchProfile.setMaritalStatus(profile.getMaritalStatus());
        fetchProfile.setAddress(profile.getAddress());
        fetchProfile.setIncomeRange(profile.getIncomeRange());
        fetchProfile.setProfileImage(profile.getProfileImage());

        return profileRepository.save(fetchProfile);
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

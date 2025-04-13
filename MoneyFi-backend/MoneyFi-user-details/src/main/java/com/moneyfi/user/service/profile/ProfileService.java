package com.moneyfi.user.service.profile;

import com.moneyfi.user.model.ProfileModel;

public interface ProfileService {

    ProfileModel save(Long userId, ProfileModel profile);

    ProfileModel findByUserId(Long userId);

    String getNameByUserId(Long userId);
}

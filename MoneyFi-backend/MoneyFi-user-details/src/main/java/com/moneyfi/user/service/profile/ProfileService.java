package com.moneyfi.user.service.profile;

import com.moneyfi.user.model.ProfileModel;

public interface ProfileService {

    public ProfileModel save(Long userId, ProfileModel profile);

    public ProfileModel findByUserId(Long userId);

    public String getNameByUserId(Long userId);
}

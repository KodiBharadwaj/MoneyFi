package com.moneyfi.user.service.profile;

import com.moneyfi.user.model.ProfileModel;

public interface ProfileService {

    public ProfileModel save(int userId, ProfileModel profile);

    public ProfileModel findByUserId(int userId);

    public String getNameByUserId(int userId);
}

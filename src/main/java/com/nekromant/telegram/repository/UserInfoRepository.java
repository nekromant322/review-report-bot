package com.nekromant.telegram.repository;

import com.nekromant.telegram.contants.UserType;
import com.nekromant.telegram.model.UserInfo;
import org.springframework.data.repository.CrudRepository;

import java.util.*;

public interface UserInfoRepository extends CrudRepository<UserInfo, String> {
    List<UserInfo> findAll();

    UserInfo findUserInfoByUserName(String userName);

    List<UserInfo> findAllByNotifyAboutReportsIsTrue();
    List<UserInfo> findAllByUserType(UserType userType);
}

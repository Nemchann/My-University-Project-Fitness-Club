package com.nemchann.fitnessbackend.users.service;

import com.nemchann.fitnessbackend.users.repository.ProfileRepository;
import com.nemchann.fitnessbackend.users.repository.RoleRepository;
import com.nemchann.fitnessbackend.users.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private ProfileRepository profileRepository;
    private RoleRepository roleRepository;
    private UserRepository userRepository;

    public UserService(ProfileRepository profileRepository, RoleRepository roleRepository,
                       UserRepository userRepository){
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

//    public void createUser()
}

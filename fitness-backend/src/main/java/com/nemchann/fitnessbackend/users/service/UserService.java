package com.nemchann.fitnessbackend.users.service;

import com.nemchann.fitnessbackend.booking.entity.Booking;
import com.nemchann.fitnessbackend.common.exception.*;
import com.nemchann.fitnessbackend.users.dto.*;
import com.nemchann.fitnessbackend.users.entity.Profile;
import com.nemchann.fitnessbackend.users.entity.Role;
import com.nemchann.fitnessbackend.users.entity.User;
import com.nemchann.fitnessbackend.users.enums.UserRole;
import com.nemchann.fitnessbackend.users.repository.ProfileRepository;
import com.nemchann.fitnessbackend.users.repository.RoleRepository;
import com.nemchann.fitnessbackend.users.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

//Исправить методы, чтобы в передаваемых значениях были dto
@Service
public class UserService {
    private final ProfileRepository profileRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    public UserService(ProfileRepository profileRepository, RoleRepository roleRepository,
                       UserRepository userRepository){
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }


    //Создает обычного пользователя типа CLIENT
    public UserResponseDto createUser(UserRegistrationDto userRegistrationDto){
        User user = new User();
        Profile profile = new Profile();

        rewriteUserDtoToUser(userRegistrationDto, user);
        rewriteUserDtoToProfile(userRegistrationDto, profile);

        Role defaultRole = roleRepository.findByRoleName(UserRole.CLIENT)
                .orElseThrow(() -> new RoleNotFoundException("Error: Role CLIENT not found."));

        user.setRole(defaultRole);
        profile.setUser(user);
        user.setProfile(profile);

        userRepository.save(user);
        //Заодно сохраняем и профиль пользователя
        profileRepository.save(profile);

        return mapToResponseDto(user);
    }

    public UserResponseDto createTrainer(UserRegistrationDto userRegistrationDto){
        User user = new User();
        Profile profile = new Profile();

        rewriteUserDtoToUser(userRegistrationDto, user);
        rewriteUserDtoToProfile(userRegistrationDto, profile);

        Role trainerRole = roleRepository.findByRoleName(UserRole.TRAINER)
                .orElseThrow(() -> new RoleNotFoundException("Error: Role TRAINER not found"));

        user.setRole(trainerRole);
        profile.setUser(user);
        user.setProfile(profile);

        userRepository.save(user);

        profileRepository.save(profile);

        return mapToResponseDto(user);
    }

    //Методы для переписания из dto в entity
    //Метод хеширования пароля вызывать здесь
    private void rewriteUserDtoToUser(UserRegistrationDto userRegistrationDto, User user){
        if(!isExistsLogin(userRegistrationDto.getLogin())){
            user.setLogin(userRegistrationDto.getLogin());

            String hashedPassword = passwordHash(userRegistrationDto.getPassword());
            user.setPassword(hashedPassword);
        }else{
            throw new UserAlreadyExistsException("This login is already used");
        }
    }

    private void rewriteUserDtoToProfile(UserRegistrationDto registrationDto, Profile profile){
        if(!isExistsEmail(registrationDto.getEmail())){
            profile.setSurname(registrationDto.getSurname());
            profile.setSelfname(registrationDto.getSelfname());
            profile.setPatronymic(registrationDto.getPatronymic());

            profile.setBirthday(registrationDto.getBirthday());
            profile.setPhone(registrationDto.getPhone());
            profile.setEmail(registrationDto.getEmail());
        }else{
            throw new UserAlreadyExistsException("This email is already used");
        }
    }

    //Метод для преобразования обычного entity в dto
    private UserResponseDto mapToResponseDto(User user){
        UserResponseDto userResponseDto = new UserResponseDto();
        Profile profile = user.getProfile();

        userResponseDto.setId(user.getId());
        userResponseDto.setSurname(profile.getSurname());
        userResponseDto.setSelfname(profile.getSelfname());
        userResponseDto.setLogin(user.getLogin());
        userResponseDto.setEmail(profile.getEmail());

        return userResponseDto;
    }


    //Проверка на наличие таких же логина и электронной почты в бд
    public boolean isExistsLogin(String login){
        Optional<User> userOptionalLogin = userRepository.findByLogin(login);

        return (userOptionalLogin.isPresent());
    }

    public boolean isExistsEmail(String email){
        Optional<Profile> profileOptionalEmail = profileRepository.findByEmail(email);

        return (profileOptionalEmail.isPresent());
    }


    //Исправить логику, пока что так, чтоб не было ошибок в коде
    private String passwordHash(String password){
        return "good" + password.hashCode() + "fitness";
    }

    public UserResponseDto getUserResponse(UUID id){
        Optional<User> userOptional = userRepository.findById(id);

        if(userOptional.isPresent()){
            User user = userOptional.get();

            return mapToResponseDto(user);
        }else{
            throw new UserNotFoundException("User is not found");
        }
    }

    //Поменять профиль
    @Transactional
    public UserResponseDto editProfile(UserEditingDto userEditingDto){
        Optional<User> userOptional = userRepository.findById(userEditingDto.getId());
        if(userOptional.isPresent()){
            User user = userOptional.get();
            rewriteFromUserEditingDtoToUser(userEditingDto, user);

            userRepository.save(user);

            return mapToResponseDto(user);

        }else{
            throw new UserNotFoundException("User is not found");
        }
    }

    private void rewriteFromUserEditingDtoToUser(UserEditingDto userEditingDto, User user){
        Profile profile = user.getProfile();

        profile.setSurname(userEditingDto.getSurname());
        profile.setSelfname(userEditingDto.getSelfname());
        profile.setPatronymic(userEditingDto.getPatronymic());
        profile.setBirthday(userEditingDto.getBirthday());
        profile.setPhone(userEditingDto.getPhone());
        profile.setEmail(userEditingDto.getEmail());

        profileRepository.save(profile);
    }

    //Тут подправить
    @Transactional
    public Page<UserResponseDto> findAllUsers(Pageable pageable){
        return userRepository.findAllByIsActiveTrue(pageable)
                .map(this::mapToResponseDto);
    }


    //Метод поменять пароль
    public void changePassword(UUID id, PasswordChangeDto passwordChangeDto){
        Optional<User> userOptional = userRepository.findById(id);

        if(userOptional.isPresent()){
            User user = userOptional.get();
            String actualPassword = user.getPassword();

            String oldDtoPassword = passwordHash(passwordChangeDto.getOldPassword());

            if(actualPassword.equals(oldDtoPassword)){
                String newHashedPassword = passwordHash(passwordChangeDto.getNewPassword());

                user.setPassword(newHashedPassword);

                userRepository.save(user);

            }else{
                throw new InvalidPasswordException("Not correct password");
            }


        }else{
            throw new UserNotFoundException("User is not found");
        }
    }

    //Метод удаление пользователя с его профилем
    @Transactional
    public void deleteUser(UserEditingDto userEditingDto){
        Optional<User> userOptional = userRepository.findById(userEditingDto.getId());

        if(userOptional.isPresent()){
            User user = userOptional.get();
            Profile profile = user.getProfile();

            profileRepository.delete(profile);
            userRepository.delete(user);

        }else{
            throw new RuntimeException("User not found");
        }
    }

    //Метод для входа в систему
    public UserResponseDto authentification(UserAuthentificationDto userAuthentificationDto){
        Optional<User> userOpt = userRepository.findByLogin(userAuthentificationDto.getLogin());

        if (userOpt.isPresent()){
            User user = userOpt.get();
            UserResponseDto userResponseDto = mapToResponseDto(user);
            String userHashedPassword = user.getPassword();

            String hashedPassword = passwordHash(userAuthentificationDto.getPassword());

            if (userHashedPassword.equals(hashedPassword)){
                return userResponseDto;
            }else{
                throw new InvalidPasswordException("Invalid password");
            }
        }else{
            throw new InvalidLoginException("Invalid login");
        }
    }

    public void deactivateUser(UUID id){
        Optional<User> userOptional = userRepository.findById(id);

        if (userOptional.isPresent()){
            User user = userOptional.get();
            user.setActive(false);

            userRepository.save(user);

        }else{
            throw new UserNotFoundException("User is not found");
        }
    }


    public User getUser(UUID id){
        Optional<User> userOptional = userRepository.findById(id);

        if(userOptional.isPresent()){
            return userOptional.get();
        }else{
            throw new UserNotFoundException("User is not found");
        }
    }

    public boolean isTrainer(UUID id){
        Optional<User> userOptional = userRepository.findById(id);

        if (userOptional.isPresent()){
            User user = userOptional.get();

            Role role = user.getRole();

            UserRole userRole = role.getRoleName();

            return UserRole.TRAINER.equals(userRole);

        }else{
            throw new UserNotFoundException("User is not found");
        }
    }

    public String getFullName(User user){
        if (userRepository.exists(Example.of(user))){
            Profile profile = user.getProfile();
            return profile.getSurname() + " " + profile.getSelfname();

        }else{
            throw new UserNotFoundException("User is not found");
        }
    }
}

package com.nemchann.fitnessbackend.users.mapper;

import com.nemchann.fitnessbackend.common.exception.UserAlreadyExistsException;
import com.nemchann.fitnessbackend.users.dto.UserEditingDto;
import com.nemchann.fitnessbackend.users.dto.UserRegistrationDto;
import com.nemchann.fitnessbackend.users.dto.UserResponseDto;
import com.nemchann.fitnessbackend.users.entity.Profile;
import com.nemchann.fitnessbackend.users.entity.User;
import com.nemchann.fitnessbackend.users.repository.ProfileRepository;
import com.nemchann.fitnessbackend.users.repository.RoleRepository;
import com.nemchann.fitnessbackend.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserMapper {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;

    public User rewriteUserDtoToUser(UserRegistrationDto userRegistrationDto){
        User user = new User();
        if(!isExistsLogin(userRegistrationDto.getLogin())){
            user.setLogin(userRegistrationDto.getLogin());

            String hashedPassword = passwordHash(userRegistrationDto.getPassword());
            user.setPassword(hashedPassword);

            return user;
        }else{
            throw new UserAlreadyExistsException("Данный логин уже занят");
        }
    }

    // Очень забавное хеширование пароля с солью
    private String passwordHash(String password){
        return "good" + password.hashCode() + "fitness";
    }

    // Метод для конвертации UserRegistrationDto в данные профиля
    public Profile rewriteUserDtoToProfile(UserRegistrationDto registrationDto){
        Profile profile = new Profile();
        if(!isExistsEmail(registrationDto.getEmail())){
            profile.setSurname(registrationDto.getSurname());
            profile.setSelfname(registrationDto.getSelfname());
            profile.setPatronymic(registrationDto.getPatronymic());

            profile.setBirthday(registrationDto.getBirthday());
            profile.setPhone(registrationDto.getPhone());
            profile.setEmail(registrationDto.getEmail());

            return profile;
        }else{
            throw new UserAlreadyExistsException("Данный email уже занят");
        }
    }

    // Метод для преобразования обычного entity User в UserResponseDto
    public UserResponseDto mapToResponseDto(User user){
        UserResponseDto userResponseDto = new UserResponseDto();
        Profile profile = user.getProfile();

        userResponseDto.setId(user.getId());
        userResponseDto.setSurname(profile.getSurname());
        userResponseDto.setSelfname(profile.getSelfname());
        userResponseDto.setLogin(user.getLogin());
        userResponseDto.setEmail(profile.getEmail());

        return userResponseDto;
    }

    // Проверка на наличие таких же логина и электронной почты в бд
    public boolean isExistsLogin(String login){
        Optional<User> userOptionalLogin = userRepository.findByLogin(login);

        return (userOptionalLogin.isPresent());
    }

    public boolean isExistsEmail(String email){
        Optional<Profile> profileOptionalEmail = profileRepository.findByEmail(email);

        return (profileOptionalEmail.isPresent());
    }

    // Метод-маппер для конвертации UserEditingDto в данные профиля
    public void rewriteFromUserEditingDtoToUser(UserEditingDto userEditingDto, User user){
        Profile profile = user.getProfile();
        String actualEmail = profile.getEmail();

        // Если email совпадает с текущим email пользователя или данный email не существует
        if(!isExistsEmail(userEditingDto.getEmail()) || actualEmail.equals(userEditingDto.getEmail())) {

            profile.setSurname(userEditingDto.getSurname());
            profile.setSelfname(userEditingDto.getSelfname());
            profile.setPatronymic(userEditingDto.getPatronymic());
            profile.setBirthday(userEditingDto.getBirthday());
            profile.setPhone(userEditingDto.getPhone());
            profile.setEmail(userEditingDto.getEmail());

            profileRepository.save(profile);
        }else{
            throw new UserAlreadyExistsException("Данный email уже занят");
        }
    }
}

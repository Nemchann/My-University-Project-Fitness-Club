package com.nemchann.fitnessbackend.users.service;

import com.nemchann.fitnessbackend.booking.entity.Booking;
import com.nemchann.fitnessbackend.common.InvalidPasswordException;
import com.nemchann.fitnessbackend.users.dto.UserEditingDto;
import com.nemchann.fitnessbackend.users.dto.UserRegistrationDto;
import com.nemchann.fitnessbackend.users.dto.UserResponseDto;
import com.nemchann.fitnessbackend.users.entity.Profile;
import com.nemchann.fitnessbackend.users.entity.Role;
import com.nemchann.fitnessbackend.users.entity.User;
import com.nemchann.fitnessbackend.users.enums.UserRole;
import com.nemchann.fitnessbackend.users.repository.ProfileRepository;
import com.nemchann.fitnessbackend.users.repository.RoleRepository;
import com.nemchann.fitnessbackend.users.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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
                .orElseThrow(() -> new RuntimeException("Error: Role CLIENT not found."));

        user.setRole(defaultRole);
        profile.setUser(user);

        userRepository.save(user);
        //Заодно сохраняем и профиль пользователя
        profileRepository.save(profile);

        return mapToResponseDto(user);
    }

    //Методы для переписания из dto в entity
    //Метод хеширования пароля вызывать здесь
    private void rewriteUserDtoToUser(UserRegistrationDto userRegistrationDto, User user){
        if(isExistsLogin(userRegistrationDto.getLogin())){
            user.setLogin(userRegistrationDto.getLogin());

            String hashedPassword = passwordHash(userRegistrationDto.getPassword());
            user.setPassword(hashedPassword);
        }else{
            throw new RuntimeException("This login is used");
        }
    }

    private void rewriteUserDtoToProfile(UserRegistrationDto registrationDto, Profile profile){
        if(isExistsEmail(registrationDto.getEmail())){
            profile.setSurname(registrationDto.getSurname());
            profile.setSelfname(registrationDto.getSelfname());
            profile.setPatronymic(registrationDto.getPatronymic());

            profile.setBirthday(registrationDto.getBirthday());
            profile.setPhone(registrationDto.getPhone());
            profile.setEmail(registrationDto.getEmail());
        }else{
            throw new RuntimeException("This email is used");
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
    private boolean isExistsLogin(String login){
        Optional<User> userOptionalLogin = userRepository.findByLogin(login);

        return (userOptionalLogin.isPresent());
    }

    private boolean isExistsEmail(String email){
        Optional<User> userOptionalEmail = userRepository.findByLogin(email);

        return (userOptionalEmail.isPresent());
    }


    //Исправить логику, пока что так, чтоб не было ошибок в коде
    private String passwordHash(String password){
        return "fwjlws" + password + "sfsdssv";
    }

    //Поменять профиль
    public UserResponseDto editProfile(UserEditingDto userEditingDto){
        Optional<User> userOptional = userRepository.findById(userEditingDto.getId());
        if(userOptional.isPresent()){
            User user = userOptional.get();
            rewriteFromUserEditingDtoToUser(userEditingDto, user);

            userRepository.save(user);

            return mapToResponseDto(user);

        }else{
            throw new RuntimeException("User not found");
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


    public void changePassword(String oldPassword, String newPassword, User user){

        String hashedPassword = passwordHash(oldPassword);
        if (user.getPassword().equals(hashedPassword)){
            String newHashedPassword = passwordHash(newPassword);
            user.setPassword(newHashedPassword);

            userRepository.save(user);
        }else {
            throw new InvalidPasswordException("Invalid password");
        }
    }

    public void deleteUser(User user){
        Profile profile = user.getProfile();

        profileRepository.delete(profile);
        userRepository.delete(user);
    }

    //Метод для входа в систему
    public UserResponseDto authentification(String login, String password){
        Optional<User> userOpt = userRepository.findByLogin(login);

        if (userOpt.isPresent()){
            User user = userOpt.get();
            UserResponseDto userResponseDto = mapToResponseDto(user);
            String userHashedPassword = user.getPassword();

            String hashedPassword = passwordHash(password);

            if (userHashedPassword.equals(hashedPassword)){
                return userResponseDto;
            }else{
                throw new InvalidPasswordException("Invalid password");
            }
        }else{
            throw new RuntimeException("Invalid login");
        }
    }

    public List<Booking> getUserBookings(User user){
        return user.getClientBookings();
    }
}

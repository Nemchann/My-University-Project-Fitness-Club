package com.nemchann.fitnessbackend.users.service;

import com.nemchann.fitnessbackend.booking.repository.BookingStatusRepository;
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
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class UserService {
    private final ProfileRepository profileRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    // Создает обычного пользователя типа CLIENT
    @Transactional
    public UserResponseDto createUser(UserRegistrationDto userRegistrationDto){
        User user = new User();
        Profile profile = new Profile();

        // Используем методы-мапперы
        rewriteUserDtoToUser(userRegistrationDto, user);
        rewriteUserDtoToProfile(userRegistrationDto, profile);

        Role defaultRole = roleRepository.findByRoleName(UserRole.CLIENT)
                .orElseThrow(() -> new RoleNotFoundException("Role CLIENT not found"));

        user.setRole(defaultRole);
        profile.setUser(user);
        user.setProfile(profile);

        userRepository.save(user);
        // Заодно сохраняем и профиль пользователя
        profileRepository.save(profile);

        return mapToResponseDto(user);
    }


    // Создает обычного пользователя типа TRAINER, такая же логика, как и у обычного клиента
    @Transactional
    public UserResponseDto createTrainer(UserRegistrationDto userRegistrationDto){
        User user = new User();
        Profile profile = new Profile();

        rewriteUserDtoToUser(userRegistrationDto, user);
        rewriteUserDtoToProfile(userRegistrationDto, profile);

        Role trainerRole = roleRepository.findByRoleName(UserRole.TRAINER)
                .orElseThrow(() -> new RoleNotFoundException("Role TRAINER not found"));

        user.setRole(trainerRole);
        profile.setUser(user);
        user.setProfile(profile);

        userRepository.save(user);

        profileRepository.save(profile);

        return mapToResponseDto(user);
    }

    // Методы для переписания из dto в entity

    // Метод для проверки UserRegistrationDto логина и присваивания пароля
    // Метод хеширования пароля вызывается здесь
    private void rewriteUserDtoToUser(UserRegistrationDto userRegistrationDto, User user){
        if(!isExistsLogin(userRegistrationDto.getLogin())){
            user.setLogin(userRegistrationDto.getLogin());

            String hashedPassword = passwordHash(userRegistrationDto.getPassword());
            user.setPassword(hashedPassword);
        }else{
            throw new UserAlreadyExistsException("Данный логин уже занят");
        }
    }

    // Метод для конвертации UserRegistrationDto в данные профиля
    private void rewriteUserDtoToProfile(UserRegistrationDto registrationDto, Profile profile){
        if(!isExistsEmail(registrationDto.getEmail())){
            profile.setSurname(registrationDto.getSurname());
            profile.setSelfname(registrationDto.getSelfname());
            profile.setPatronymic(registrationDto.getPatronymic());

            profile.setBirthday(registrationDto.getBirthday());
            profile.setPhone(registrationDto.getPhone());
            profile.setEmail(registrationDto.getEmail());
        }else{
            throw new UserAlreadyExistsException("Данный email уже занят");
        }
    }

    // Метод для преобразования обычного entity User в UserResponseDto
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


    // Проверка на наличие таких же логина и электронной почты в бд
    public boolean isExistsLogin(String login){
        Optional<User> userOptionalLogin = userRepository.findByLogin(login);

        return (userOptionalLogin.isPresent());
    }

    public boolean isExistsEmail(String email){
        Optional<Profile> profileOptionalEmail = profileRepository.findByEmail(email);

        return (profileOptionalEmail.isPresent());
    }


    // Очень забавное хеширование пароля с солью
    private String passwordHash(String password){
        return "good" + password.hashCode() + "fitness";
    }

    // Получить UserResponseDto по id пользователя
    public UserResponseDto getUserResponse(UUID id){
        Optional<User> userOptional = userRepository.findById(id);

        if(userOptional.isPresent()){
            User user = userOptional.get();

            return mapToResponseDto(user);
        }else{
            throw new UserNotFoundException("Данный пользователь не найден");
        }
    }

    // Изменение профиля
    @Transactional
    public UserResponseDto editProfile(UserEditingDto userEditingDto){
        Optional<User> userOptional = userRepository.findById(userEditingDto.getId());
        if(userOptional.isPresent()){
            User user = userOptional.get();
            rewriteFromUserEditingDtoToUser(userEditingDto, user);

            userRepository.save(user);

            return mapToResponseDto(user);

        }else{
            throw new UserNotFoundException("Данный пользователь не найден");
        }
    }


    // Метод-маппер для конвертации UserEditingDto в данные профиля
    private void rewriteFromUserEditingDtoToUser(UserEditingDto userEditingDto, User user){
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

    // Page всех пользователей, в т.ч. тренеров и админом
    @Transactional
    public Page<UserResponseDto> findAllUsers(Pageable pageable){
        return userRepository.findAllByIsActiveTrue(pageable)
                .map(this::mapToResponseDto);
    }


    // Метод изменения пароля
    @Transactional
    public void changePassword(UUID id, PasswordChangeDto passwordChangeDto){
        Optional<User> userOptional = userRepository.findById(id);

        if(userOptional.isPresent()){
            User user = userOptional.get();
            String actualPassword = user.getPassword(); // Действующий хеш пароля пользователя, какой он записан в БД

            // Старый пароль, записанный пользователем на фронтенде
            String oldDtoPassword = passwordHash(passwordChangeDto.getOldPassword());

            // Если актуальный пароль совпадает с записанным в графе "текущий пароль" на фронтенде
            if(actualPassword.equals(oldDtoPassword)){
                String newHashedPassword = passwordHash(passwordChangeDto.getNewPassword()); // Хешируем новый пароль

                user.setPassword(newHashedPassword);

                userRepository.save(user);

            }else{
                throw new InvalidPasswordException("Неверный старый пароль");
            }


        }else{
            throw new UserNotFoundException("Данный пользователь не найден");
        }
    }

    // Метод удаления пользователя с его профилем
    @Transactional
    public void deleteUser(UserEditingDto userEditingDto){
        Optional<User> userOptional = userRepository.findById(userEditingDto.getId());

        if(userOptional.isPresent()){
            User user = userOptional.get();
            Profile profile = user.getProfile();

            profileRepository.delete(profile);
            userRepository.delete(user);

        }else{
            throw new UserNotFoundException("Данный пользователь не найден");
        }
    }

    // Метод для входа в систему
    @Transactional
    public UserResponseDto authentification(UserAuthentificationDto userAuthentificationDto){
        Optional<User> userOpt = userRepository.findByLogin(userAuthentificationDto.getLogin());

        if (userOpt.isPresent()){
            User user = userOpt.get();
            // Если пользователь деактивирован
            if (!user.isActive()){
                throw new UserNotFoundException("Данный пользователь деактивирован");
            }

            UserResponseDto userResponseDto = mapToResponseDto(user);
            String userHashedPassword = user.getPassword(); // Актуальный хеш пароля

            // Хеш введенного пароля
            String hashedPassword = passwordHash(userAuthentificationDto.getPassword());

            // Сравнение паролей
            if (userHashedPassword.equals(hashedPassword)){
                return userResponseDto;
            }else{
                throw new InvalidPasswordException("Неверный пароль");
            }
        }else{
            throw new InvalidLoginException("Неверный логин");
        }
    }


    // Деактивация пользователя - мягкое удаление
    @Transactional
    public void deactivateUser(UUID id){
        Optional<User> userOptional = userRepository.findById(id);

        if (userOptional.isPresent()){
            User user = userOptional.get();
            user.setActive(false);

            userRepository.save(user);

        }else{
            throw new UserNotFoundException("Данный пользователь не найден");
        }
    }


    // Получение пользователя по id. Для использования другими сервисами
    public User getUser(UUID id){
        Optional<User> userOptional = userRepository.findById(id);

        if(userOptional.isPresent()){
            return userOptional.get();
        }else{
            throw new UserNotFoundException("Данный пользователь не найден");
        }
    }


    // Проверка является ли пользователь тренером
    public boolean isTrainer(UUID id){
        Optional<User> userOptional = userRepository.findById(id);

        if (userOptional.isPresent()){
            User user = userOptional.get();

            Role role = user.getRole();

            UserRole userRole = role.getRoleName();

            return UserRole.TRAINER.equals(userRole);

        }else{
            throw new UserNotFoundException("Данный пользователь не найден");
        }
    }

    // Получить Page пользователей по имени роли
    public Page<UserResponseDto> getByRoleName(String roleName, Pageable pageable){
        UserRole userRole = UserRole.valueOf(roleName.toUpperCase());

        Role role = roleRepository.findByRoleName(userRole)
                .orElseThrow(() -> new RoleNotFoundException("Данная роль не найдена"));

        return userRepository.findAllByRole(pageable, role)
                .map(this::mapToResponseDto);

    }

    // Получить Page клиентов
    public Page<UserResponseDto> getAllClients(Pageable pageable){
        Role role = roleRepository.findByRoleName(UserRole.CLIENT)
                .orElseThrow(() -> new RoleNotFoundException("Данная роль не найдена"));

        return userRepository.findAllByRole(pageable, role)
                .map(this::mapToResponseDto);
    }

    // Получить Page тренеров
    public Page<UserResponseDto> getAllTrainers(Pageable pageable){
        Role role = roleRepository.findByRoleName(UserRole.TRAINER)
                .orElseThrow(() -> new RoleNotFoundException("Данная роль не найдена"));

        return userRepository.findAllByRole(pageable, role)
                .map(this::mapToResponseDto);
    }

}

package com.nemchann.fitnessbackend.users.service;

import com.nemchann.fitnessbackend.booking.entity.Booking;
import com.nemchann.fitnessbackend.users.entity.Profile;
import com.nemchann.fitnessbackend.users.entity.Role;
import com.nemchann.fitnessbackend.users.entity.User;
import com.nemchann.fitnessbackend.users.repository.ProfileRepository;
import com.nemchann.fitnessbackend.users.repository.RoleRepository;
import com.nemchann.fitnessbackend.users.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

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


    //Метод хеширования пароля вызывать здесь
    public void createUser(String login, String password, Role role, String surname, String selfname,
                           String patronymic, Date birthday, String phone, String email){
        String roleName = role.getRoleName();
        String hashedPassword = passwordHash(password);

        if (!isExistsLoginEmail(login, email)){
            if ("CLIENT".equals(roleName)){
                User user = createClient(login, hashedPassword);
                createProfile(user, surname, selfname, patronymic, birthday, phone, email);
            }else if ("TRAINER".equals(roleName)){
                User user = createTrainer(login, hashedPassword);
                createProfile(user, surname, selfname, patronymic, birthday, phone, email);
            }else if("ADMINISTRATOR".equals(roleName)){
                User user = createAdministrator(login, hashedPassword);
                createProfile(user, surname, selfname, patronymic, birthday, phone, email);
            }else{
                return;
            }
        }


    }

    //Проверка на наличие таких же логина и электронной почты в бд
    private boolean isExistsLoginEmail(String login, String email){
        Optional<User> userOptionalLogin = userRepository.findByLogin(login);
        Optional<User> userOptionalEmail = userRepository.findByEmail(email);

        return (userOptionalLogin.isPresent() || userOptionalEmail.isPresent());

    }

    private User createClient(String login, String password){
        String roleName = "CLIENT";
        Optional<Role> roleOptional = roleRepository.findByRoleName(roleName);
        if (roleOptional.isPresent()){
            Role role = roleOptional.get();
            User user = new User(login, password, role);

            userRepository.save(user);
            return user;
        }
        return null;
    }

    private User createTrainer(String login, String password){
        String roleName = "TRAINER";
        Optional<Role> roleOptional = roleRepository.findByRoleName(roleName);
        if (roleOptional.isPresent()){
            Role role = roleOptional.get();
            User user = new User(login, password, role);

            userRepository.save(user);

            return user;
        }
        return null;
    }

    private User createAdministrator(String login, String password){
        String roleName = "ADMINISTRATOR";
        Optional<Role> roleOptional = roleRepository.findByRoleName(roleName);
        if (roleOptional.isPresent()){
            Role role = roleOptional.get();
            User user = new User(login, password, role);

            userRepository.save(user);

            return user;
        }
        return null;
    }

    private void createProfile(User user, String surname, String selfname,
                               String patronymic, Date birthday, String phone, String email){
        Profile profile = new Profile(user, surname, selfname, patronymic, birthday, phone, email);
        profileRepository.save(profile);
    }


    //Сделать из этого возвращаемое значение String
    private String passwordHash(String password){
        return password.hashCode();
    }


    public void changePassword(String oldPassword, String newPassword, User user){

        String hashedPassword = passwordHash(oldPassword);
        if (user.getPassword().equals(hashedPassword)){
            String newHashedPassword = passwordHash(newPassword);
            user.setPassword(newHashedPassword);

            userRepository.save(user);
        }
    }

    public void deleteUser(User user){
        Profile profile = user.getProfile();

        profileRepository.delete(profile);
        userRepository.delete(user);
    }

    //Метод для входа в систему
    public User authentification(String login, String password){
        Optional<User> userOpt = userRepository.findByLogin(login);

        if (userOpt.isPresent()){
            User user = userOpt.get();
            String userHashedPassword = user.getPassword();

            String hashedPassword = passwordHash(password);

            if (userHashedPassword.equals(hashedPassword)){
                return user;
            }
        }
        return null;
    }

    public List<Booking> getUserBookings(User user){
        return user.getClientBookings();
    }
}

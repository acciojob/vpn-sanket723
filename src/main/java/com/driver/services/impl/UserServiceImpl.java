package com.driver.services.impl;

import com.driver.model.Country;
import com.driver.model.CountryName;
import com.driver.model.ServiceProvider;
import com.driver.model.User;
import com.driver.repository.CountryRepository;
import com.driver.repository.ServiceProviderRepository;
import com.driver.repository.UserRepository;
import com.driver.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserRepository userRepository3;
    @Autowired
    ServiceProviderRepository serviceProviderRepository3;
    @Autowired
    CountryRepository countryRepository3;

    @Override
    public User register(String username, String password, String countryName) throws Exception{
        //create a user of given country. The originalIp of the user should be "countryCode.userId" and return the user. Note that right now user is not connected and thus connected would be false and maskedIp would be null
        //Note that the userId is created automatically by the repository layer

        if(!countryName.toUpperCase().equals("IND") && !countryName.toUpperCase().equals("AUS") && !countryName.toUpperCase().equals("USA") && !countryName.toUpperCase().equals("CHI") && !countryName.toUpperCase().equals("JPN") ){
            throw new Exception("Country not found");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);

        Country country = new Country();
        CountryName countryName1 = CountryName.valueOf(countryName.toUpperCase());
        country.setCountryName(countryName1);
        country.setCode(countryName1.toCode());
        country.setUser(user);

        user.setOriginalCountry(country);
        user = userRepository3.save(user);  // this is to get the userId assigned to user
        String originalIp = country.getCode() + "." + user.getId();
        user.setOriginalIp(originalIp);
        user.setConnected(false);
        user.setMaskedIp(null);

        user = userRepository3.save(user);

        return user;
    }

    @Override
    public User subscribe(Integer userId, Integer serviceProviderId) {
        //subscribe to the serviceProvider by adding it to the list of providers and return updated User

        User user = userRepository3.findById(userId).get();

        ServiceProvider serviceProvider = serviceProviderRepository3.findById(serviceProviderId).get();

        List<ServiceProvider> serviceProviderList = user.getServiceProviderList();
        List<User> userList = serviceProvider.getUsers();

        serviceProviderList.add(serviceProvider);
        userList.add(user);

        serviceProvider.setUsers(userList);
        user.setServiceProviderList(serviceProviderList);

        userRepository3.save(user);


        return user;
    }
}

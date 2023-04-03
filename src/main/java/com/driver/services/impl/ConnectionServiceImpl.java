package com.driver.services.impl;

import com.driver.model.Connection;
import com.driver.model.Country;
import com.driver.model.ServiceProvider;
import com.driver.model.User;
import com.driver.repository.ConnectionRepository;
import com.driver.repository.ServiceProviderRepository;
import com.driver.repository.UserRepository;
import com.driver.services.ConnectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConnectionServiceImpl implements ConnectionService {
    @Autowired
    UserRepository userRepository2;
    @Autowired
    ServiceProviderRepository serviceProviderRepository2;
    @Autowired
    ConnectionRepository connectionRepository2;

    @Override
    public User connect(int userId, String countryName) throws Exception{
        //Connect the user to a vpn by considering the following priority order.
        //1. If the user is already connected to any service provider, throw "Already connected" exception.
        //2. Else if the countryName corresponds to the original country of the user, do nothing. This means that the user wants to connect to its original country, for which we do not require a connection. Thus, return the user as it is.
        //3. Else, the user should be subscribed under a serviceProvider having option to connect to the given country.
        //If the connection can not be made (As user does not have a serviceProvider or serviceProvider does not have given country, throw "Unable to connect" exception.
        //Else, establish the connection where the maskedIp is "updatedCountryCode.serviceProviderId.userId" and return the updated user. If multiple service providers allow you to connect to the country, use the service provider having smallest id.

        User user = userRepository2.findById(userId).get();
        String originalCountryNameInString = user.getOriginalCountry().getCountryName().toString();

        if(user.getConnected()==true){
            throw new Exception("Already connected");
        }

        if(originalCountryNameInString.equals(countryName.toUpperCase())){
            return user;
        }

        List<ServiceProvider> serviceProviderList = user.getServiceProviderList();
        if(serviceProviderList==null){
            throw new Exception("Unable to connect");
        }

        Boolean flag = false;
        ServiceProvider leastIdServiceProvider = null;
        Country countryToSet = null;
        int min = Integer.MAX_VALUE;
        for(ServiceProvider s : serviceProviderList){
            for(Country c : s.getCountryList()){
                if(c.getCountryName().toString().equals(countryName.toUpperCase())){
                    if(s.getId() < min) {
                        flag = true;
                        leastIdServiceProvider = s;
                        countryToSet = c;
                        min = s.getId();
//                        user.setConnected(true);
//                        user.setOriginalCountry(c);
//                        String maskedIp = c.getCode() + "." + s.getId() + "." + userId;
//                        user.setMaskedIp(maskedIp);
//                        flag = true;
//                        min = s.getId();
                    }
                }
            }
        }

        if (flag==false){
            throw new Exception("Unable to connect");
        }

        Connection connection = new Connection();
        connection.setUser(user);
        connection.setServiceProvider(leastIdServiceProvider);

        String maskedIp = countryToSet.getCode() + "." + leastIdServiceProvider.getId() + "." + userId;
        user.setMaskedIp(maskedIp);
        user.setConnected(true);

        List<Connection> connectionList = user.getConnectionList();
        connectionList.add(connection);
        user.setConnectionList(connectionList);

        List<Connection> connectionList1 = leastIdServiceProvider.getConnectionList();
        connectionList1.add(connection);
        leastIdServiceProvider.setConnectionList(connectionList1);

        userRepository2.save(user);
        serviceProviderRepository2.save(leastIdServiceProvider);
        return user;
    }
    @Override
    public User disconnect(int userId) throws Exception {
        //If the given user was not connected to a vpn, throw "Already disconnected" exception.
        //Else, disconnect from vpn, make masked Ip as null, update relevant attributes and return updated user.

        User user = userRepository2.findById(userId).get();

        if(user.getConnected()==false){
            throw new Exception("Already disconnected");
        }

        //make it disconnected
        user.setConnected(false);
        user.setMaskedIp(null);
        List<Connection> connectionList = user.getConnectionList();
        for(Connection c : connectionList){
            if(c.getUser().equals(user)){
                connectionList.remove(c);
            }
        }
//
//        List<ServiceProvider> serviceProviderList = user.getServiceProviderList();
//        for (ServiceProvider s : serviceProviderList){
//            if(s.getUsers().contains(user)){
//                s.getUsers().remove(user);
//            }
//        }

        user = userRepository2.save(user);

        return user;
    }
    @Override
    public User communicate(int senderId, int receiverId) throws Exception {
        return null;
    }
}

package czy.miaosha.service;

import czy.miaosha.error.BusinessException;
import czy.miaosha.service.model.UserModel;

public interface UserService {

    UserModel getUser(Integer id);

    void register(UserModel userModel) throws BusinessException;

    UserModel validateLogin(String telphone, String encrptPassword) throws BusinessException;

}

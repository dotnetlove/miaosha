package czy.miaosha.service.impl;

import czy.miaosha.dao.UserDOMapper;
import czy.miaosha.dao.UserPasswordDOMapper;
import czy.miaosha.entity.UserDO;
import czy.miaosha.entity.UserPasswordDO;
import czy.miaosha.error.BusinessException;
import czy.miaosha.error.EmBusinessError;
import czy.miaosha.service.UserService;
import czy.miaosha.service.model.UserModel;
import czy.miaosha.utils.Convert;
import czy.miaosha.validator.ValidationResult;
import czy.miaosha.validator.ValidatorImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserDOMapper userDOMapper;

    @Autowired
    private UserPasswordDOMapper userPasswordDOMapper;

    @Autowired
    private ValidatorImpl validator;

    /**
     * 根据id获取用户信息
     */
    @Override
    public UserModel getUser(Integer id) {
        UserDO userDO = userDOMapper.selectByPrimaryKey(id);
        if (userDO == null) {
            return null;
        }
        UserPasswordDO userPasswordDO = userPasswordDOMapper.selectByUserId(userDO.getId());
        return Convert.convertUserModelFromUserDOAndUserPasswordDO(userDO, userPasswordDO);
    }

    /**
     * 注册用户
     */
    @Override
    @Transactional
    public void register(UserModel userModel) throws BusinessException {
        if (userModel == null) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR);
        }

        //校验
        ValidationResult result = validator.validate(userModel);
        if (result.isHasErrors()) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, result.getErrMsg());
        }

        UserDO userDO = Convert.convertUserDOFromUserModel(userModel);
        try {
            userDOMapper.insertSelective(userDO);
        } catch (DuplicateKeyException ex) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "手机号已注册");
        }

        userModel.setId(userDO.getId());

        //UserPasswordDO userPasswordDO = convertPasswordFromModel(userModel);
        UserPasswordDO userPasswordDO = Convert.convertUserPasswordDOFromUserModel(userModel);
        userPasswordDOMapper.insertSelective(userPasswordDO);

        return;
    }

    /**
     * 验证用户手机号和密码
     */
    @Override
    public UserModel validateLogin(String telphone, String encrptPassword) throws BusinessException {
        //通过手机号获取用户信息
        UserDO userDO = userDOMapper.selectByTelphone(telphone);
        if (userDO == null) {
            throw new BusinessException(EmBusinessError.USER_LOGIN_FAIL);
        }
        UserPasswordDO userPasswordDO = userPasswordDOMapper.selectByUserId(userDO.getId());
        UserModel userModel = Convert.convertUserModelFromUserDOAndUserPasswordDO(userDO, userPasswordDO);

        //对比密码是否匹配
        if (!StringUtils.equals(encrptPassword, userModel.getEncrptPassword())) {
            throw new BusinessException(EmBusinessError.USER_LOGIN_FAIL);
        }
        return userModel;
    }


}

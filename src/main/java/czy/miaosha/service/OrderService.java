package czy.miaosha.service;

import czy.miaosha.error.BusinessException;
import czy.miaosha.service.model.OrderModel;

public interface OrderService {

    OrderModel createOrder(Integer userId, Integer itemId, Integer amount) throws BusinessException;

}

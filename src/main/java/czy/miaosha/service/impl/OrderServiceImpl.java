package czy.miaosha.service.impl;

import czy.miaosha.dao.OrderDOMapper;
import czy.miaosha.dao.SequenceDOMapper;
import czy.miaosha.entity.OrderDO;
import czy.miaosha.entity.SequenceDO;
import czy.miaosha.error.BusinessException;
import czy.miaosha.error.EmBusinessError;
import czy.miaosha.service.ItemService;
import czy.miaosha.service.OrderService;
import czy.miaosha.service.UserService;
import czy.miaosha.service.model.ItemModel;
import czy.miaosha.service.model.OrderModel;
import czy.miaosha.service.model.UserModel;
import czy.miaosha.utils.Convert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderDOMapper orderDOMapper;

    @Autowired
    private SequenceDOMapper sequenceDOMapper;

    @Override
    @Transactional
    public OrderModel createOrder(Integer userId, Integer itemId, Integer promoId, Integer amount) throws BusinessException {

        //1:校验下单的商品是否存在、用户是否合法、购买数量是否正确
        ItemModel itemModel = itemService.getItemById(itemId);
        if (itemModel == null) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "商品不存在！");
        }
        UserModel userModel = userService.getUser(userId);
        if (userModel == null) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "用户不存在！");
        }
        if (amount < 0 || amount > 99) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "购买数量错误！");
        }
        //校验秒杀活动信息
        if (promoId != null) {
            //(1)校验秒杀活动是否适用该商品
            if (promoId.intValue() != itemModel.getPromoModel().getId()) {
                throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "活动信息错误！");
                //(2)校验活动是在正在进行
            } else if (itemModel.getPromoModel().getStatus().intValue() != 2) {
                throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "秒杀时间错误！");
            }
        }

        //2:落单减库存
        boolean result = itemService.decreaseStock(itemId, amount);
        if (!result) {
            throw new BusinessException(EmBusinessError.STOCK_NOT_ENOUGH);
        }

        //3:订单入库
        OrderModel orderModel = new OrderModel();
        orderModel.setUserId(userId);
        orderModel.setItemId(itemId);
        orderModel.setAmount(amount);
        //如果有活动，则设置秒杀活动价格，否则设置普通商品价格
        if (promoId != null) {
            orderModel.setItemPrice(itemModel.getPromoModel().getPromoItemPrice());
        } else {
            orderModel.setItemPrice(itemModel.getPrice());
        }
        orderModel.setPromoId(promoId);
        orderModel.setOrderPrice(orderModel.getItemPrice().multiply(new BigDecimal(amount)));
        //生成交易订单号
        orderModel.setId(generateOrderNo());
        OrderDO orderDO = Convert.convertOrderDOFromOrderModel(orderModel);
        orderDOMapper.insertSelective(orderDO);

        //4:增加商品销量
        itemService.increaseSales(itemId, amount);

        return orderModel;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    String generateOrderNo() {

        //订单号有16位
        StringBuffer stringBuffer = new StringBuffer();
        //前8位为时间信息，年月日
        LocalDateTime now = LocalDateTime.now();
        String nowDate = now.format(DateTimeFormatter.ISO_DATE).replace("-", "");
        stringBuffer.append(nowDate);
        //中间6位为自增序列
        //获取当前sequence
        int sequence = 0;
        SequenceDO sequenceDO = sequenceDOMapper.getSequenceByName("order_info");
        sequence = sequenceDO.getCurrentValue();
        sequenceDO.setCurrentValue(sequenceDO.getCurrentValue() + sequenceDO.getStep());
        sequenceDOMapper.updateByPrimaryKey(sequenceDO);

        String sequenceStr = String.valueOf(sequence);
        //不足6位数在前面补0
        for (int i = 0; i < 6 - sequenceStr.length(); i++) {
            stringBuffer.append("0");
        }
        stringBuffer.append(sequenceStr);

        //最后两位为分库分表位
        stringBuffer.append("00");

        return stringBuffer.toString();
    }


}

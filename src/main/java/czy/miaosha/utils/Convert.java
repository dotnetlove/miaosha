package czy.miaosha.utils;

import czy.miaosha.entity.*;
import czy.miaosha.service.model.ItemModel;
import czy.miaosha.service.model.OrderModel;
import czy.miaosha.service.model.PromoModel;
import czy.miaosha.service.model.UserModel;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;

import java.math.BigDecimal;

public class Convert {

    //ItemModel——>ItemStockDO
    public static ItemStockDO convertItemStockFromItemModel(ItemModel itemModel) {
        if (itemModel == null) {
            return null;
        }
        ItemStockDO itemStockDO = new ItemStockDO();
        itemStockDO.setItemId(itemModel.getId());
        itemStockDO.setStock(itemModel.getStock());
        return itemStockDO;
    }

    //ItemModel——>ItemDO
    public static ItemDO convertItemDOFromItemModel(ItemModel itemModel) {
        if (itemModel == null) {
            return null;
        }
        ItemDO itemDO = new ItemDO();
        BeanUtils.copyProperties(itemModel, itemDO);
        //ItemModel中price是BigDecimal类型的，需要手动转换成double类型（不同类型无法复制）
        itemDO.setPrice(itemModel.getPrice().doubleValue());
        return itemDO;
    }

    //ItemDO+ItemStockDO——>ItemModel
    public static ItemModel convertModelFromItemDOAndItemStockDO(ItemDO itemDO, ItemStockDO itemStockDO) {
        ItemModel itemModel = new ItemModel();
        BeanUtils.copyProperties(itemDO, itemModel);
        itemModel.setPrice(new BigDecimal(itemDO.getPrice()));
        itemModel.setStock(itemStockDO.getStock());
        return itemModel;
    }



    //UserModel———>UserDO
    public static UserDO convertUserDOFromUserModel(UserModel userModel) {
        if (userModel == null) {
            return null;
        }
        UserDO userDO = new UserDO();
        BeanUtils.copyProperties(userModel, userDO);
        return userDO;
    }

    //UserModel————>UserPasswordDO
    public static UserPasswordDO convertUserPasswordDOFromUserModel(UserModel userModel) {
        if (userModel == null) {
            return null;
        }
        UserPasswordDO userPasswordDO = new UserPasswordDO();
        userPasswordDO.setEncrptPassword(userModel.getEncrptPassword());
        userPasswordDO.setUserId(userModel.getId());
        return userPasswordDO;
    }

    //UserDO+UserPasswordDO————>UserModel(因为密码放在另一张表里，UserModel加入了另一张表的数据)
    public static UserModel convertUserModelFromUserDOAndUserPasswordDO(UserDO userDO, UserPasswordDO userPasswordDO) {
        if (userDO == null) {
            return null;
        }
        UserModel userModel = new UserModel();
        BeanUtils.copyProperties(userDO, userModel);
        if (userPasswordDO != null) {
            userModel.setEncrptPassword(userPasswordDO.getEncrptPassword());
        }
        return userModel;
    }


    //PromoDO————>PromoModel
    public static PromoModel convertPromoModelFromPromoDO(PromoDO promoDO) {
        if (promoDO == null) {
            return null;
        }
        PromoModel promoModel = new PromoModel();
        BeanUtils.copyProperties(promoDO, promoModel);
        promoModel.setPromoItemPrice(new BigDecimal(promoDO.getPromoItemPrice()));
        promoModel.setStartDate(new DateTime(promoDO.getStartDate()));
        promoModel.setEndDate(new DateTime(promoDO.getEndDate()));
        return promoModel;
    }

    //OrderModel——>OrderDO
    public static OrderDO convertOrderDOFromOrderModel(OrderModel orderModel) {
        if (orderModel == null) {
            return null;
        }
        OrderDO orderDO = new OrderDO();
        BeanUtils.copyProperties(orderModel, orderDO);
        orderDO.setItemPrice(orderModel.getItemPrice().doubleValue());
        orderDO.setOrderPrice(orderModel.getOrderPrice().doubleValue());
        return orderDO;
    }
}

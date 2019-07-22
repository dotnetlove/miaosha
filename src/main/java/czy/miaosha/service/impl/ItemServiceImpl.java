package czy.miaosha.service.impl;

import czy.miaosha.dao.ItemDOMapper;
import czy.miaosha.dao.ItemStockDOMapper;
import czy.miaosha.entity.ItemDO;
import czy.miaosha.entity.ItemStockDO;
import czy.miaosha.error.BusinessException;
import czy.miaosha.error.EmBusinessError;
import czy.miaosha.service.ItemService;
import czy.miaosha.service.PromoService;
import czy.miaosha.service.model.ItemModel;
import czy.miaosha.service.model.PromoModel;
import czy.miaosha.utils.Convert;
import czy.miaosha.validator.ValidationResult;
import czy.miaosha.validator.ValidatorImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private ValidatorImpl validator;

    @Autowired
    private ItemDOMapper itemDOMapper;

    @Autowired
    private ItemStockDOMapper itemStockDOMapper;

    @Autowired
    private PromoService promoService;

    /**
     * 创建商品
     */
    @Override
    @Transactional
    public ItemModel createItem(ItemModel itemModel) throws BusinessException {

        //校验入参(插入数据库时要先检查参数是否合法)
        ValidationResult result = validator.validate(itemModel);
        if (result.isHasErrors()) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, result.getErrMsg());
        }
        //ItemModel转换成ItemDO
        ItemDO itemDO = Convert.convertItemDOFromItemModel(itemModel);

        //写入数据库
        itemDOMapper.insertSelective(itemDO);

        //因为前端传过来的参数没有id这个参数，使用itemDO.getId()可以获取插入数据的id
        itemModel.setId(itemDO.getId());

        //ItemModel转换成ItemStockDO
        //ItemStockDO itemStockDO = this.convertItemStockFromItemModel(itemModel);
        ItemStockDO itemStockDO = Convert.convertItemStockFromItemModel(itemModel);
        itemStockDOMapper.insertSelective(itemStockDO);

        //返回创建完成的对象
        return this.getItemById(itemModel.getId());
    }


    /**
     * 商品列表
     */
    @Override
    public List<ItemModel> listItem() {
        List<ItemDO> itemDOList = itemDOMapper.listItem();
        //JDK8新特性：stream流和lambada表达式
        List<ItemModel> itemModelList = itemDOList.stream().map(itemDO -> {
            ItemStockDO itemStockDO = itemStockDOMapper.selectByItemId(itemDO.getId());
            //ItemDO+ItemStockDO——>ItemModel
            ItemModel itemModel = Convert.convertModelFromItemDOAndItemStockDO(itemDO, itemStockDO);
            return itemModel;
        }).collect(Collectors.toList());
        return itemModelList;
    }

    /**
     * 根据id获取itemModel对象
     */
    @Override
    public ItemModel getItemById(Integer id) {
        ItemDO itemDO = itemDOMapper.selectByPrimaryKey(id);
        if (itemDO == null) {
            return null;
        }
        ItemStockDO itemStockDO = itemStockDOMapper.selectByItemId(itemDO.getId());

        //把itemDO和itemStockDO转换成itemModel
        ItemModel itemModel = Convert.convertModelFromItemDOAndItemStockDO(itemDO, itemStockDO);

        //获取商品活动信息
        PromoModel promoModel = promoService.getPromoByItemId(itemModel.getId());
        if (promoModel != null && promoModel.getStatus().intValue() != 3) {
            itemModel.setPromoModel(promoModel);
        }
        return itemModel;
    }

    /**
     * 减库存
     */
    @Override
    public boolean decreaseStock(Integer itemId, Integer amount) throws BusinessException {
        //返回影响函数 等于0表示减库存失败
        int affectedRow = itemStockDOMapper.decreaseStock(itemId, amount);
        if (affectedRow > 0) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    @Transactional
    public void increaseSales(Integer itemId, Integer amount) throws BusinessException {
        itemDOMapper.increaseSales(itemId, amount);
    }
}

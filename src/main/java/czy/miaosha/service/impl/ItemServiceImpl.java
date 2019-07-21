package czy.miaosha.service.impl;

import czy.miaosha.dao.ItemDOMapper;
import czy.miaosha.dao.ItemStockDOMapper;
import czy.miaosha.entity.ItemDO;
import czy.miaosha.entity.ItemStockDO;
import czy.miaosha.error.BusinessException;
import czy.miaosha.error.EmBusinessError;
import czy.miaosha.service.ItemService;
import czy.miaosha.service.model.ItemModel;
import czy.miaosha.validator.ValidationResult;
import czy.miaosha.validator.ValidatorImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
        ItemDO itemDO = this.convertItemDOFromItemModel(itemModel);

        //写入数据库
        itemDOMapper.insertSelective(itemDO);

        //因为前端传过来的参数没有id这个参数，使用itemDO.getId()可以获取插入数据的id
        itemModel.setId(itemDO.getId());

        //ItemModel转换成ItemStockDO
        ItemStockDO itemStockDO = this.convertItemStockFromItemModel(itemModel);
        itemStockDOMapper.insertSelective(itemStockDO);

        //返回创建完成的对象
        return this.getItemById(itemModel.getId());
    }

    //ItemModel——>ItemDO
    private ItemDO convertItemDOFromItemModel(ItemModel itemModel) {
        if (itemModel == null) {
            return null;
        }
        ItemDO itemDO = new ItemDO();
        BeanUtils.copyProperties(itemModel, itemDO);
        //ItemModel中price是BigDecimal类型的，需要手动转换成double类型（不同类型无法复制）
        itemDO.setPrice(itemModel.getPrice().doubleValue());
        return itemDO;
    }

    //ItemModel——>ItemStockDO
    private ItemStockDO convertItemStockFromItemModel(ItemModel itemModel) {
        if (itemModel == null) {
            return null;
        }
        ItemStockDO itemStockDO = new ItemStockDO();
        itemStockDO.setItemId(itemModel.getId());
        itemStockDO.setStock(itemModel.getStock());
        return itemStockDO;
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
            ItemModel itemModel = this.convertModelFromEntity(itemDO, itemStockDO);
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
        ItemModel itemModel = convertModelFromEntity(itemDO, itemStockDO);

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

    //ItemDO+ItemStockDO——>ItemModel
    private ItemModel convertModelFromEntity(ItemDO itemDO, ItemStockDO itemStockDO) {
        ItemModel itemModel = new ItemModel();
        BeanUtils.copyProperties(itemDO, itemModel);
        itemModel.setPrice(new BigDecimal(itemDO.getPrice()));
        itemModel.setStock(itemStockDO.getStock());
        return itemModel;
    }
}

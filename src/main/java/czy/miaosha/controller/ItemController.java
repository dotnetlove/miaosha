package czy.miaosha.controller;

import czy.miaosha.controller.ViewVO.ItemVO;
import czy.miaosha.error.BusinessException;
import czy.miaosha.response.CommonReturnType;
import czy.miaosha.service.ItemService;
import czy.miaosha.service.model.ItemModel;
import czy.miaosha.utils.Convert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Controller("item")
@RequestMapping("/item")
@CrossOrigin(allowCredentials = "true", allowedHeaders = {"*"})
public class ItemController extends BaseController {

    @Autowired
    private ItemService itemService;

    /**
     * 创建商品
     */
    @RequestMapping(value = "/create", method = RequestMethod.POST, consumes = CONTENT_TYPE_FORMED)
    @ResponseBody
    public CommonReturnType createItem(@RequestParam(name = "title") String title,
                                       @RequestParam(name = "description") String description,
                                       @RequestParam(name = "price") BigDecimal price,
                                       @RequestParam(name = "stock") Integer stock,
                                       @RequestParam(name = "imgUrl") String imgUrl) throws BusinessException {

        ItemModel itemModel = new ItemModel();
        itemModel.setTitle(title);
        itemModel.setDescription(description);
        itemModel.setPrice(price);
        itemModel.setStock(stock);
        itemModel.setImgUrl(imgUrl);

        ItemModel itemModelForReturn = itemService.createItem(itemModel);
        ItemVO itemVO = Convert.convertItemVOFromItemModel(itemModelForReturn);
        return CommonReturnType.create(itemVO);
    }


    /**
     * 商品列表页
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public CommonReturnType listItem() {

        List<ItemModel> itemModelList = itemService.listItem();

        //使用stream API 将list内的itemModel转换成itemVO
        List<ItemVO> itemVOList = itemModelList.stream().map(itemModel -> {
            ItemVO itemVO = Convert.convertItemVOFromItemModel(itemModel);
            return itemVO;
        }).collect(Collectors.toList());
        return CommonReturnType.create(itemVOList);
    }

    /**
     * 获取商品详情页
     */
    @RequestMapping(value = "/getItem", method = RequestMethod.GET)
    @ResponseBody
    public CommonReturnType getItem(@RequestParam(name = "id") Integer id) {

        ItemModel itemModel = itemService.getItemById(id);

        ItemVO itemVO = Convert.convertItemVOFromItemModel(itemModel);

        return CommonReturnType.create(itemVO);
    }

}

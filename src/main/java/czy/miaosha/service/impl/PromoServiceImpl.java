package czy.miaosha.service.impl;

import czy.miaosha.dao.PromoDOMapper;
import czy.miaosha.entity.PromoDO;
import czy.miaosha.service.PromoService;
import czy.miaosha.service.model.PromoModel;
import czy.miaosha.utils.Convert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PromoServiceImpl implements PromoService {

    @Autowired
    private PromoDOMapper promoDOMapper;

    @Override
    public PromoModel getPromoByItemId(Integer itemId) {

        //获取对应商品的秒杀活动信息
        PromoDO promoDO = promoDOMapper.selectByItemId(itemId);
        if (promoDO == null) {
            return null;
        }
        //PromoDO————>PromoModel
        PromoModel promoModel = Convert.convertPromoModelFromPromoDO(promoDO);

        //判断当前时间是否秒杀活动即将开始或正在进行
        if (promoModel.getStartDate().isAfterNow()) {
            promoModel.setStatus(1);
        } else if (promoModel.getEndDate().isBeforeNow()) {
            promoModel.setStatus(3);
        } else {
            promoModel.setStatus(2);
        }

        return promoModel;
    }


}

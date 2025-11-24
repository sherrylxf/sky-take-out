package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class UsrServiceImpl implements UserService {
    public static final String WX_LOGIN = "https://api.weixin.qq.com/sns/jscode2session";

    @Autowired
    private WeChatProperties weChatProperties;
    @Autowired
    private UserMapper userMapper;

    @Override
    public User wxLogin(UserLoginDTO userLoginDTO) {
        // 调用微信接口服务获取openid
        String openid = getOpenId(userLoginDTO.getCode());

        // 判断openid是否为空，如果为空表示登录失败，抛出业务异常
        if(openid == null){
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }
        
        // 判断当前用户是否为新用户
        User user = userMapper.getByOpenid(openid);
        if(user == null){
            // 如果是新用户，自动完成注册
            user = User.builder()
                    .openid(openid)
                    .createTime(LocalDateTime.now())
                    .build();
            userMapper.insert(user);
        }

        return user;
    }

    /**
     * 调用微信接口服务获取openid
     * @param code 微信登录凭证
     * @return openid
     */
    private String getOpenId(String code) {
        // 构造请求参数
        Map<String, String> params = new HashMap<>();
        params.put("appid", weChatProperties.getAppid());
        params.put("secret", weChatProperties.getSecret());
        params.put("js_code", code);
        params.put("grant_type", "authorization_code");
        
        log.info("调用微信接口获取openid，appid: {}, code: {}", weChatProperties.getAppid(), code);
        String json = HttpClientUtil.doGet(WX_LOGIN, params);
        log.info("微信接口返回结果: {}", json);

        JSONObject jsonObject = JSONObject.parseObject(json);
        
        // 检查是否有错误
        String errcode = jsonObject.getString("errcode");
        String errmsg = jsonObject.getString("errmsg");
        if (errcode != null && !errcode.equals("0")) {
            log.error("微信接口调用失败，errcode: {}, errmsg: {}", errcode, errmsg);
            return null;
        }
        
        String openid = jsonObject.getString("openid");
        if (openid == null || openid.isEmpty()) {
            log.error("微信接口返回的openid为空，完整响应: {}", json);
            return null;
        }
        
        log.info("成功获取openid: {}", openid);
        return openid;
    }
}

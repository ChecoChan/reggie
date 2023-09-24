# reggie
Reggie Takeout


# reggie

#### 介绍
本系统是将学习各项技术栈用于实践练习的项目，将各项所学知识整合到一起。项目后端分为管理后台开发和前台用户端开发。管理后台包括员工登录和管理，套餐、菜品和菜系的增删改查，前台包括用户登录，菜品展示，下单及地址管理。

项目包含自定义业务异常类和异常处理器，将异常统一管理，自定义拦截器对未登录的员工/用户进行拦截。使用ThreadLocal封装员工/用户登录ID，避免多次从数据库或者缓存中读取用户信息，Controller接口使用了Restful规范的写法，使用MyBatis Plus访问MySQL，还使用了Spring Cache减轻数据库访问压力，提高页面访问速度。

接入第三方支付即可满足日常使用，将应用打包并使用Docker部署即可运行

### 访问地址
后台管理系统：在 [http://localhost:8080/backend/page/login/login.html](http://localhost:8080/backend/page/login/login.html)
    
- 默认账号：admin
- 默认密码：123456

用户端：在 [http://localhost:8080/front/page/login.html](http://localhost:8080/front/page/login.html)

- 测试账号：13000000000

#### 项目框架
1. 前端：HTML + CSS + JavaScript + Vue.js + ElementUI
2. 后端：SpringBoot + MySQL +MyBatis Plus + Spring Cache + Redis
